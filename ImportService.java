package Lam.camu.p100fx.service;

import Lam.camu.p100fx.model.DataBlock;
import Lam.camu.p100fx.model.Control;
import Lam.camu.p100fx.model.ParametFile;
import javafx.scene.image.*;
import javafx.scene.paint.Color;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/**
 * ImportService - Import dữ liệu từ các định dạng khác nhau
 * KHÔNG sử dụng AWT/ImageIO, chỉ dùng JavaFX
 */
public class ImportService {

    public interface ImportListener {
        void onProgress(int percent);
        void onComplete(DataBlock block);
        void onError(String error);
    }

    // ==================== IMPORT FROM PNG ====================

    /**
     * Import từ file PNG sử dụng JavaFX Image
     */
    public DataBlock importFromPNG(String filePath) throws IOException {
        return importFromPNG(filePath, null);
    }

    public DataBlock importFromPNG(String filePath, ImportListener listener) throws IOException {
        // Sử dụng JavaFX Image để đọc PNG
        Image image = new Image(new FileInputStream(filePath));

        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        // Tạo PixelReader để đọc từng pixel
        PixelReader pixelReader = image.getPixelReader();

        DataBlock block = new DataBlock(1, "Imported PNG", width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                int argb = colorToARGB(color);
                block.setPixel(x, y, argb);
            }

            if (listener != null) {
                listener.onProgress((y * 100) / height);
            }
        }

        if (listener != null) {
            listener.onComplete(block);
        }

        return block;
    }

    private int colorToARGB(Color color) {
        int a = (int) Math.round(color.getOpacity() * 255);
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    // ==================== IMPORT FROM ASCII ====================

    public DataBlock importFromASCII(String filePath) throws IOException {
        return importFromASCII(filePath, null);
    }

    public DataBlock importFromASCII(String filePath, ImportListener listener) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        // Remove border lines
        lines.removeIf(line -> line.startsWith("╔") || line.startsWith("╚"));

        int height = lines.size();
        int width = lines.get(0).length() - 2;  // Remove borders

        DataBlock block = new DataBlock(1, "Imported ASCII", width, height);

        for (int y = 0; y < height; y++) {
            String line = lines.get(y);
            String content = line.substring(1, line.length() - 1);

            for (int x = 0; x < width && x < content.length(); x++) {
                char c = content.charAt(x);
                if (c != ' ') {
                    int gray = getGrayFromASCII(c);
                    int color = (0xFF << 24) | (gray << 16) | (gray << 8) | gray;
                    block.setPixel(x, y, color);
                }
            }

            if (listener != null) {
                listener.onProgress((y * 100) / height);
            }
        }

        return block;
    }

    private int getGrayFromASCII(char c) {
        switch (c) {
            case '@': return 255;
            case '#': return 224;
            case '+': return 192;
            case '=': return 160;
            case '-': return 128;
            case ':': return 96;
            case '.': return 64;
            case ' ': return 0;
            default: return 128;
        }
    }

    // ==================== IMPORT FROM RAW ====================

    public DataBlock importFromRaw(String filePath) throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(filePath))) {

            int width = dis.readInt();
            int height = dis.readInt();

            DataBlock block = new DataBlock(1, "Imported RAW", width, height);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int argb = dis.readInt();
                    block.setPixel(x, y, argb);
                }
            }

            return block;
        }
    }

    // ==================== IMPORT FROM ZIP ====================

    public DataBlock importFromZip(String filePath) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(filePath))) {

            ZipEntry entry;
            DataBlock block = null;
            byte[] pixelData = null;
            int width = 0, height = 0;
            List<Control> controls = new ArrayList<>();

            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();

                if (name.equals("pixels.dat")) {
                    // Read pixel data
                    pixelData = zis.readAllBytes();

                } else if (name.equals("metadata.txt")) {
                    // Read metadata
                    byte[] metaData = zis.readAllBytes();
                    String meta = new String(metaData);

                    for (String line : meta.split("\n")) {
                        if (line.startsWith("Width:")) {
                            width = Integer.parseInt(line.split(":")[1].trim());
                        } else if (line.startsWith("Height:")) {
                            height = Integer.parseInt(line.split(":")[1].trim());
                        }
                    }

                } else if (name.equals("controls.txt")) {
                    // Read controls
                    byte[] ctrlData = zis.readAllBytes();
                    String ctrlText = new String(ctrlData);
                    for (String line : ctrlText.split("\n")) {
                        if (line.trim().isEmpty()) continue;
                        String[] parts = line.split(",");
                        if (parts.length >= 7) {
                            Control ctrl = new Control(
                                    Integer.parseInt(parts[0]),
                                    parts[1],
                                    parts[2],
                                    Integer.parseInt(parts[3]),
                                    Integer.parseInt(parts[4]),
                                    Integer.parseInt(parts[5]),
                                    Integer.parseInt(parts[6])
                            );
                            if (parts.length > 7) ctrl.setText(parts[7]);
                            controls.add(ctrl);
                        }
                    }
                }

                zis.closeEntry();
            }

            // Create block
            if (pixelData != null && width > 0 && height > 0) {
                block = new DataBlock(1, "Imported ZIP", width, height);

                // Parse pixel data (RGBA format)
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int index = (y * width + x) * 4;
                        if (index + 3 < pixelData.length) {
                            int r = pixelData[index] & 0xFF;
                            int g = pixelData[index + 1] & 0xFF;
                            int b = pixelData[index + 2] & 0xFF;
                            int a = pixelData[index + 3] & 0xFF;
                            int argb = (a << 24) | (r << 16) | (g << 8) | b;
                            block.setPixel(x, y, argb);
                        }
                    }
                }

                // Add controls
                for (Control ctrl : controls) {
                    block.addControl(ctrl);
                }
            }

            return block;
        }
    }

    // ==================== IMPORT FROM CSV ====================

    public DataBlock importFromCSV(String filePath) throws IOException {
        return importFromCSV(filePath, null);
    }

    public DataBlock importFromCSV(String filePath, ImportListener listener) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        int height = lines.size();
        int width = lines.get(0).split(",").length;

        DataBlock block = new DataBlock(1, "Imported CSV", width, height);

        for (int y = 0; y < height; y++) {
            String[] values = lines.get(y).split(",");
            for (int x = 0; x < width && x < values.length; x++) {
                try {
                    // Parse hex string (ARGB format)
                    String hex = values[x].trim();
                    if (hex.startsWith("0x") || hex.startsWith("0X")) {
                        hex = hex.substring(2);
                    }
                    int argb = Integer.parseInt(hex, 16);
                    block.setPixel(x, y, argb);
                } catch (NumberFormatException e) {
                    // Skip invalid values
                }
            }

            if (listener != null) {
                listener.onProgress((y * 100) / height);
            }
        }

        return block;
    }

    // ==================== IMPORT FROM PARAM FILE ====================

    public DataBlock importFromParam(String filePath) throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(filePath))) {

            // Check magic
            int magic = dis.readInt();
            if (magic != 0x504C414D) { // "PLAM"
                throw new IOException("Invalid file format");
            }

            // Read version
            int version = dis.readInt();

            // Read global info
            int globalWidth = dis.readInt();
            int globalHeight = dis.readInt();

            // Read number of blocks
            int numBlocks = dis.readInt();

            // Read first block
            DataBlock block = readBlock(dis);

            return block;
        }
    }

    public WritableImage importFile(File file) throws IOException {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".png")) {
            return importPNG(file);
        } else if (fileName.endsWith(".lam") || fileName.endsWith(".param")) {
            return importParam(file);
        } else {
            throw new IOException("Unsupported file format");
        }
    }
    private WritableImage importPNG(File file) throws IOException {
        // Đọc PNG bằng JavaFX Image
        Image image = new Image(file.toURI().toString());
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        WritableImage writableImage = new WritableImage(width, height);
        PixelReader reader = image.getPixelReader();
        PixelWriter writer = writableImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                writer.setArgb(x, y, reader.getArgb(x, y));
            }
        }

        return writableImage;
    }

    private WritableImage importParam(File file) throws IOException {
        // Đọc file .lam và chuyển thành image
        ParametFile pf = ParametFile.loadFromFile(file.getPath());
        // Lấy block đầu tiên
        DataBlock block = pf.getBlocks().get(0);

        WritableImage image = new WritableImage(block.getWidth(), block.getHeight());
        PixelWriter writer = image.getPixelWriter();

        for (int y = 0; y < block.getHeight(); y++) {
            for (int x = 0; x < block.getWidth(); x++) {
                writer.setArgb(x, y, block.getPixel(x, y));
            }
        }

        return image;
    }

    private DataBlock readBlock(DataInputStream dis) throws IOException {
        int id = dis.readInt();
        int x = dis.readInt();
        int y = dis.readInt();
        int width = dis.readInt();
        int height = dis.readInt();

        DataBlock block = new DataBlock(id, "Imported", width, height);

        // Read pixel count
        int pixelCount = dis.readInt();

        // Read pixels
        for (int i = 0; i < pixelCount; i++) {
            int pixel = dis.readInt();
            int px = i % width;
            int py = i / width;
            block.setPixel(px, py, pixel);
        }

        // Read controls
        int controlCount = dis.readInt();
        for (int i = 0; i < controlCount; i++) {
            Control ctrl = readControl(dis);
            block.addControl(ctrl);
        }

        return block;
    }

    private Control readControl(DataInputStream dis) throws IOException {
        int id = dis.readInt();
        String type = dis.readUTF();
        String name = dis.readUTF();
        int x = dis.readInt();
        int y = dis.readInt();
        int width = dis.readInt();
        int height = dis.readInt();

        Control ctrl = new Control(id, type, name, x, y, width, height);
        ctrl.setText(dis.readUTF());
        ctrl.setValue(dis.readUTF());
        ctrl.setSelected(dis.readBoolean());
        ctrl.setVisible(dis.readBoolean());
        ctrl.setEnabled(dis.readBoolean());
        ctrl.setBackgroundColor(dis.readInt());
        ctrl.setTextColor(dis.readInt());

        return ctrl;
    }

    // ==================== UTILITIES ====================

    /**
     * Kiểm tra định dạng file
     */
    public String detectFormat(String filePath) {
        String lower = filePath.toLowerCase();
        if (lower.endsWith(".png")) return "PNG";
        if (lower.endsWith(".txt") || lower.endsWith(".ascii")) return "ASCII";
        if (lower.endsWith(".raw")) return "RAW";
        if (lower.endsWith(".zip")) return "ZIP";
        if (lower.endsWith(".csv")) return "CSV";
        if (lower.endsWith(".param") || lower.endsWith(".lam")) return "PARAM";
        return "UNKNOWN";
    }

    /**
     * Import tự động dựa trên định dạng file
     */
    public DataBlock importAuto(String filePath) throws IOException {
        String format = detectFormat(filePath);

        switch (format) {
            case "PNG":
                return importFromPNG(filePath);
            case "ASCII":
                return importFromASCII(filePath);
            case "RAW":
                return importFromRaw(filePath);
            case "ZIP":
                return importFromZip(filePath);
            case "CSV":
                return importFromCSV(filePath);
            case "PARAM":
                return importFromParam(filePath);
            default:
                throw new IOException("Unsupported format: " + filePath);
        }
    }
}

package Lam.camu.p100fx.service;

import Lam.camu.p100fx.model.DataBlock;
import Lam.camu.p100fx.model.ParametFile;
import javafx.scene.image.*;
import javafx.scene.paint.Color;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/**
 * ExportService - Xuất dữ liệu ra các định dạng khác nhau
 * ĐÃ SỬA LỖI ép kiểu int → byte
 */
public class ExportService {

    public interface ExportListener {
        void onProgress(int percent);
        void onComplete(String message);
        void onError(String error);
    }

    // ==================== EXPORT TO PNG ====================

    public void exportToPNG(DataBlock block, String filePath) throws IOException {
        exportToPNG(block, filePath, 0, block.getHeight(), null);
    }

    public void exportToPNG(DataBlock block, String filePath,
                            int startRow, int endRow, ExportListener listener) throws IOException {

        int width = block.getWidth();
        int height = endRow - startRow;

        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            int row = startRow + y;
            for (int x = 0; x < width; x++) {
                int argb = block.getPixel(x, row);
                pixelWriter.setArgb(x, y, argb);
            }

            if (listener != null) {
                listener.onProgress((y * 100) / height);
            }
        }

        // Sử dụng PNGWriter đơn giản
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            writePNGSimple(writableImage, fos);
        }

        if (listener != null) {
            listener.onComplete("Exported to " + filePath);
        }
    }

    /**
     * Ghi PNG đơn giản - ĐÃ XỬ LÝ ÉP KIỂU AN TOÀN
     */
    private void writePNGSimple(WritableImage image, OutputStream out) throws IOException {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelReader reader = image.getPixelReader();

        // PNG Signature
        out.write(new byte[]{(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});

        // IHDR chunk
        writeInt(out, 13); // Length
        out.write("IHDR".getBytes());

        // Width (4 bytes) - ÉP KIỂU AN TOÀN
        out.write((width >> 24) & 0xFF);
        out.write((width >> 16) & 0xFF);
        out.write((width >> 8) & 0xFF);
        out.write(width & 0xFF);

        // Height (4 bytes) - ÉP KIỂU AN TOÀN
        out.write((height >> 24) & 0xFF);
        out.write((height >> 16) & 0xFF);
        out.write((height >> 8) & 0xFF);
        out.write(height & 0xFF);

        // Bit depth, color type, etc.
        out.write(8);  // Bit depth
        out.write(6);  // Color type (RGBA)
        out.write(0);  // Compression method
        out.write(0);  // Filter method
        out.write(0);  // Interlace method

        // CRC (tạm thời để 0)
        writeInt(out, 0);

        // IDAT chunks
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int y = 0; y < height; y++) {
            baos.write(0); // Filter type
            for (int x = 0; x < width; x++) {
                int argb = reader.getArgb(x, y);
                // ÉP KIỂU AN TOÀN cho từng thành phần
                baos.write((argb >> 16) & 0xFF); // R
                baos.write((argb >> 8) & 0xFF);  // G
                baos.write(argb & 0xFF);         // B
                baos.write((argb >> 24) & 0xFF); // A
            }
        }
        byte[] imageData = baos.toByteArray();

        // Nén dữ liệu (tạm thời không nén)
        writeInt(out, imageData.length);
        out.write("IDAT".getBytes());
        out.write(imageData);
        writeInt(out, 0); // CRC

        // IEND chunk
        writeInt(out, 0);
        out.write("IEND".getBytes());
        writeInt(out, 0xAE426082); // CRC for IEND
    }

    /**
     * Ghi int an toàn thành 4 bytes
     */
    private void writeInt(OutputStream out, int value) throws IOException {
        out.write((value >> 24) & 0xFF);
        out.write((value >> 16) & 0xFF);
        out.write((value >> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    // ==================== EXPORT TO COMPRESSED ====================

    public void exportCompressed(DataBlock block, String filePath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(filePath))) {

            // Pixel data
            ZipEntry pixelEntry = new ZipEntry("pixels.dat");
            zos.putNextEntry(pixelEntry);

            for (int y = 0; y < block.getHeight(); y++) {
                for (int x = 0; x < block.getWidth(); x++) {
                    int argb = block.getPixel(x, y);
                    // ÉP KIỂU AN TOÀN cho từng byte
                    zos.write((argb >> 24) & 0xFF); // A
                    zos.write((argb >> 16) & 0xFF); // R
                    zos.write((argb >> 8) & 0xFF);  // G
                    zos.write(argb & 0xFF);         // B
                }
            }
            zos.closeEntry();

            // Metadata
            ZipEntry metaEntry = new ZipEntry("metadata.txt");
            zos.putNextEntry(metaEntry);
            String meta = String.format("Width: %d\nHeight: %d\n",
                    block.getWidth(), block.getHeight());
            zos.write(meta.getBytes());
            zos.closeEntry();

// Children (DataBlock tree)
            if (!block.getChildren().isEmpty()) {
                ZipEntry childEntry = new ZipEntry("blocks.txt");
                zos.putNextEntry(childEntry);

                writeBlockTree(block, zos, 0);

                zos.closeEntry();
            }
        }
    }
    private void writeBlockTree(DataBlock block, OutputStream out, int level) throws IOException {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < level; i++) indent.append("  ");

        String line = String.format("%s%d,%s,%d,%d,%d,%d\n",
                indent.toString(),
                block.getId(),
                block.getName(),
                block.getX(),
                block.getY(),
                block.getWidth(),
                block.getHeight());

        out.write(line.getBytes());

        for (DataBlock child : block.getChildren()) {
            writeBlockTree(child, out, level + 1);
        }
    }

    // ==================== EXPORT TO RAW ====================

    public void exportToRaw(DataBlock block, String filePath) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(filePath))) {

            // Write header
            dos.writeInt(block.getWidth());
            dos.writeInt(block.getHeight());

            // Write pixel data
            for (int y = 0; y < block.getHeight(); y++) {
                for (int x = 0; x < block.getWidth(); x++) {
                    dos.writeInt(block.getPixel(x, y));
                }
            }
        }
    }

    // ==================== EXPORT TO ASCII ====================

    public void exportToASCII(DataBlock block, String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {

            int displayWidth = Math.min(block.getWidth(), 100);

            writer.println("╔" + "═".repeat(displayWidth) + "╗");

            for (int y = 0; y < block.getHeight(); y++) {
                writer.print("║");
                for (int x = 0; x < displayWidth; x++) {
                    int argb = block.getPixel(x, y);
                    if (argb == 0) {
                        writer.print(" ");
                    } else {
                        writer.print(getASCIIChar(argb));
                    }
                }
                writer.println("║");
            }

            writer.println("╚" + "═".repeat(displayWidth) + "╝");
        }
    }

    private char getASCIIChar(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        int gray = (r + g + b) / 3;

        if (gray < 32) return ' ';
        if (gray < 64) return '.';
        if (gray < 96) return ':';
        if (gray < 128) return '-';
        if (gray < 160) return '=';
        if (gray < 192) return '+';
        if (gray < 224) return '#';
        return '@';
    }

    // ==================== EXPORT TO HTML ====================

    public void exportToHTML(DataBlock block, String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {

            writer.println("<!DOCTYPE html>");
            writer.println("<html><head>");
            writer.println("<meta charset='UTF-8'>");
            writer.println("<title>Exported Design</title>");
            writer.println("<style>");
            writer.println("body { background: #333; }");
            writer.println(".pixel-grid { display: flex; flex-direction: column; }");
            writer.println(".row { display: flex; height: 2px; }");
            writer.println(".pixel { width: 2px; height: 2px; }");
            writer.println("</style>");
            writer.println("</head><body>");
            writer.println("<div class='pixel-grid'>");

            for (int y = 0; y < block.getHeight(); y++) {
                writer.print("<div class='row'>");
                for (int x = 0; x < block.getWidth(); x++) {
                    int argb = block.getPixel(x, y);
                    if (argb != 0) {
                        String color = String.format("#%06X", argb & 0xFFFFFF);
                        writer.print("<div class='pixel' style='background: " + color + ";'></div>");
                    } else {
                        writer.print("<div class='pixel' style='background: transparent;'></div>");
                    }
                }
                writer.println("</div>");
            }

            writer.println("</div>");
            writer.println("</body></html>");
        }
    }

    // ==================== EXPORT TO C HEADER ====================

    public void exportToCHeader(DataBlock block, String filePath, String arrayName) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {

            writer.println("#ifndef " + arrayName.toUpperCase() + "_H");
            writer.println("#define " + arrayName.toUpperCase() + "_H");
            writer.println();
            writer.println("#define WIDTH " + block.getWidth());
            writer.println("#define HEIGHT " + block.getHeight());
            writer.println();
            writer.println("const unsigned int " + arrayName + "[HEIGHT][WIDTH] = {");

            for (int y = 0; y < block.getHeight(); y++) {
                writer.print("    {");
                for (int x = 0; x < block.getWidth(); x++) {
                    int argb = block.getPixel(x, y);
                    writer.print(String.format("0x%08X", argb));
                    if (x < block.getWidth() - 1) writer.print(", ");
                }
                writer.println("}" + (y < block.getHeight() - 1 ? "," : ""));
            }

            writer.println("};");
            writer.println("#endif");
        }
    }
    // Trong ExportService.java
    public void export(WritableImage image, File file) throws IOException {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".png")) {
            exportToPNG(image, file);
        } else if (fileName.endsWith(".lam") || fileName.endsWith(".param")) {
            exportToParam(image, file);
        } else {
            throw new IOException("Unsupported file format");
        }
    }

    private void exportToPNG(WritableImage image, File file) throws IOException {
        // Dùng JavaFX để lưu PNG
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelReader reader = image.getPixelReader();

        try (FileOutputStream fos = new FileOutputStream(file)) {
            // Ghi PNG signature
            fos.write(new byte[]{(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});

            // Ghi dữ liệu (đơn giản hóa - trong thực tế cần nén)
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int argb = reader.getArgb(x, y);
                    fos.write((argb >> 16) & 0xFF); // R
                    fos.write((argb >> 8) & 0xFF);  // G
                    fos.write(argb & 0xFF);         // B
                    fos.write((argb >> 24) & 0xFF); // A
                }
            }
        }
    }

    private void exportToParam(WritableImage image, File file) throws IOException {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelReader reader = image.getPixelReader();

        // Tạo DataBlock
        DataBlock block = new DataBlock(1, "Exported", width, height);

        // Copy pixel data
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = reader.getArgb(x, y);
                block.setPixel(x, y, argb);
            }
        }

        // Tạo ParametFile và lưu
        ParametFile pf = new ParametFile(width, height);
        pf.addBlock(block);
        pf.saveToFile(file.getPath());
    }

    // ==================== EXPORT TO CSV ====================

    public void exportToCSV(DataBlock block, String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {

            for (int y = 0; y < block.getHeight(); y++) {
                StringBuilder line = new StringBuilder();
                for (int x = 0; x < block.getWidth(); x++) {
                    int argb = block.getPixel(x, y);
                    line.append(String.format("%08X", argb));
                    if (x < block.getWidth() - 1) {
                        line.append(",");
                    }
                }
                writer.println(line.toString());
            }
        }
    }
}
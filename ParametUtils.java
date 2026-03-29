package Lam.camu.p100fx.util;

import Lam.camu.p100fx.model.DataBlock;
import Lam.camu.p100fx.model.Control;
import Lam.camu.p100fx.model.ParametFile;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.util.*;

public class ParametUtils {

    // ==================== 1. CHUYỂN ĐỔI ĐỊNH DẠNG ====================

    /**
     * Chuyển DataBlock thành ảnh PNG
     */
    public static void blockToPNG(DataBlock block, String filePath) {
        exportBlockToPNG(block, filePath);
    }

    /**
     * Chuyển ảnh PNG thành DataBlock
     */
    public static DataBlock pngToBlock(File pngFile) {
        // TODO: Đọc PNG thành block
        // Code đọc PNG thành block
        return null;
    }

    // ==================== 2. XUẤT FILE ====================

    /**
     * Xuất block ra file PNG
     */
    public static void exportBlockToPNG(DataBlock block, String filePath) {
        int width = block.getWidth();
        int height = block.getHeight();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Duyệt mảng pixel trực tiếp (nhanh hơn gọi getPixel)
        int[] pixels = block.getPixels();

        for (int y = 0; y < height; y++) {
            int rowOffset = y * width;
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, pixels[rowOffset + x]);
            }
        }
        try {
            File file = new File(filePath);
            ImageIO.write(image, "png", file);
            System.out.println("✅ Saved PNG: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("❌ Lỗi xuất ảnh: " + e.getMessage());
        }
    }

    /**
     * Xuất block ra file ASCII
     */
    public static void exportBlockToASCII(DataBlock block, String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (int y = 0; y < block.getHeight(); y++) {
                for (int x = 0; x < block.getWidth(); x++) {
                    int argb = block.getPixel(x, y);
                    if (argb == 0) {
                        writer.print(' ');
                    } else {
                        writer.print('█');
                    }
                }
                writer.println();
            }
        }
    }

    /**
     * Xuất toàn bộ cây block ra file
     */
    public static void exportTreeToFile(DataBlock root, String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            exportTreeRecursive(writer, root, 0);
        }
    }

    private static void exportTreeRecursive(PrintWriter writer, DataBlock block, int level) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < level; i++) {
            indent.append("  ");
        }

        writer.println(indent + "├─ Block ID: " + block.getId() +
                ", Name: " + block.getName() +
                ", Size: " + block.getWidth() + "x" + block.getHeight());

        for (DataBlock child : block.getChildren()) {
            exportTreeRecursive(writer, child, level + 1);
        }
    }

    // ==================== 3. TÌM KIẾM ====================

    /**
     * Tìm block theo ID trong danh sách
     */
    public static DataBlock findBlockById(List<DataBlock> blocks, int id) {
        return blocks.stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Tìm block theo ID trong cây
     */
    public static DataBlock findBlockByIdInTree(DataBlock root, int id) {
        if (root.getId() == id) {
            return root;
        }

        for (DataBlock child : root.getChildren()) {
            DataBlock found = findBlockByIdInTree(child, id);
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    /**
     * Tìm block theo tên trong danh sách
     */
    public static DataBlock findBlockByName(List<DataBlock> blocks, String name) {
        return blocks.stream()
                .filter(b -> b.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Tìm block theo tên trong cây
     */
    public static DataBlock findBlockByNameInTree(DataBlock root, String name) {
        if (root.getName().equals(name)) {
            return root;
        }

        for (DataBlock child : root.getChildren()) {
            DataBlock found = findBlockByNameInTree(child, name);
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    /**
     * Tìm tất cả block theo tên
     */
    public static List<DataBlock> findAllBlocksByName(DataBlock root, String name) {
        List<DataBlock> result = new ArrayList<>();
        findAllBlocksByNameRecursive(root, name, result);
        return result;
    }

    private static void findAllBlocksByNameRecursive(DataBlock block, String name, List<DataBlock> result) {
        if (block.getName().equals(name)) {
            result.add(block);
        }

        for (DataBlock child : block.getChildren()) {
            findAllBlocksByNameRecursive(child, name, result);
        }
    }

    /**
     * Tìm block tại vị trí (x,y) tuyệt đối
     */
    public static DataBlock findBlockAtPosition(DataBlock root, int x, int y) {
        // Kiểm tra block hiện tại
        int absX = root.getAbsoluteX();
        int absY = root.getAbsoluteY();

        if (x >= absX && x < absX + root.getWidth() &&
                y >= absY && y < absY + root.getHeight()) {

            // Kiểm tra con trước (ưu tiên block con nếu có)
            for (DataBlock child : root.getChildren()) {
                DataBlock found = findBlockAtPosition(child, x, y);
                if (found != null) {
                    return found;
                }
            }

            return root;
        }

        return null;
    }

    // ==================== 4. TÍNH TOÁN KÍCH THƯỚC ====================

    /**
     * Tính tổng chiều cao của tất cả blocks
     */
    public static int getTotalHeight(List<DataBlock> blocks) {
        return blocks.stream()
                .mapToInt(DataBlock::getHeight)
                .sum();
    }

    /**
     * Tính tổng số pixel
     */
    public static long getTotalPixels(List<DataBlock> blocks) {
        return blocks.stream()
                .mapToLong(b -> (long) b.getWidth() * b.getHeight())
                .sum();
    }

    /**
     * Tính tổng số pixel trong cây
     */
    public static long getTotalPixelsInTree(DataBlock root) {
        long total = (long) root.getWidth() * root.getHeight();
        for (DataBlock child : root.getChildren()) {
            total += getTotalPixelsInTree(child);
        }
        return total;
    }

    /**
     * Tính dung lượng bộ nhớ (MB)
     */
    public static double getMemorySize(List<DataBlock> blocks) {
        long totalPixels = getTotalPixels(blocks);
        return (totalPixels * 4) / (1024.0 * 1024.0); // 4 bytes mỗi pixel
    }

    /**
     * Tính dung lượng bộ nhớ cho cây (MB)
     */
    public static double getMemorySizeInTree(DataBlock root) {
        long totalPixels = getTotalPixelsInTree(root);
        return (totalPixels * 4) / (1024.0 * 1024.0);
    }

    // ==================== 5. SAO CHÉP ====================

    /**
     * Sao chép block (deep copy)
     */
    public static DataBlock copyBlock(DataBlock original) {
        DataBlock copy = new DataBlock(
                original.getId() + 777,
                original.getName() + "_copy",
                original.getWidth(),
                original.getHeight()
        );

        // Tốc độ chu kỳ: Dùng System.arraycopy cho mảng pixels
        System.arraycopy(original.getPixels(), 0, copy.getPixels(), 0, original.getPixels().length);

        copy.setX(original.getX());
        copy.setY(original.getY());
        copy.setVisible(original.isVisible());
        copy.setOpacity(original.getOpacity());

        // Đệ quy copy các con
        for (DataBlock child : original.getChildren()) {
            copy.addChild(copyBlock(child));
        }
        return copy;
    }

    // ==================== 6. KIỂM TRA ====================

    /**
     * Kiểm tra block có rỗng không
     */
    public static boolean isEmpty(DataBlock block) {
        for (int y = 0; y < block.getHeight(); y++) {
            for (int x = 0; x < block.getWidth(); x++) {
                if (block.getPixel(x, y) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Đếm số pixel đã dùng
     */
    public static int countUsedPixels(DataBlock block) {
        int count = 0;
        // Duyệt chu kỳ mảng 1 chiều trực tiếp để tăng tốc
        for (int argb : block.getPixels()) {
            if (argb != 0) count++;
        }
        return count;
    }

    /**
     * Kiểm tra block có con không
     */
    public static boolean hasChildren(DataBlock block) {
        return !block.getChildren().isEmpty();
    }

    // ==================== 7. THỐNG KÊ ====================

    /**
     * In thông tin chi tiết của block
     */
    public static void printBlockInfo(DataBlock block) {
        System.out.println("┌─────────────────────────────");
        System.out.println("│ Block ID: " + block.getId());
        System.out.println("│ Name: " + block.getName());
        System.out.println("│ Size: " + block.getWidth() + " x " + block.getHeight());
        System.out.println("│ Position: (" + block.getX() + ", " + block.getY() + ")");
        System.out.println("│ Absolute Position: (" + block.getAbsoluteX() + ", " + block.getAbsoluteY() + ")");
        System.out.println("│ Children: " + block.getChildren().size());
        System.out.println("│ Used pixels: " + countUsedPixels(block));
        System.out.println("│ Modified: " + block.isModified());
        System.out.println("│ Visible: " + block.isVisible());
        System.out.println("│ Opacity: " + block.getOpacity());
        System.out.println("└─────────────────────────────");
    }

    /**
     * In thông tin cây block
     */
    public static void printTreeInfo(DataBlock root) {
        System.out.println("🌲 BLOCK TREE STRUCTURE");
        printTreeRecursive(root, 0);
    }

    private static void printTreeRecursive(DataBlock block, int level) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < level; i++) {
            indent.append("  ");
        }

        System.out.println(indent + "├─ [" + block.getId() + "] " + block.getName() +
                " (" + block.getWidth() + "x" + block.getHeight() + ")");

        for (DataBlock child : block.getChildren()) {
            printTreeRecursive(child, level + 1);
        }
    }

    /**
     * In thông tin file
     */
    public static void printFileInfo(ParametFile file) {
        System.out.println("📁 PARAMET FILE INFO");
        System.out.println("   Size: " + file.getWidth() + " x " + file.getHeight());
        List<DataBlock> blocks = file.getBlocks();
        System.out.println("   Blocks: " + file.getBlocks().size());
        System.out.println("   Total pixels: " + getTotalPixels(file.getBlocks()));
        System.out.println("   Memory: " + String.format("%.2f", getMemorySize(file.getBlocks())) + " MB");
    }
}
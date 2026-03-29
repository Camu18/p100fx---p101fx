package Lam.camu.p100fx.model;

import java.io.*;
import java.util.*;

public class ParametFile {
    private int width;
    private int height;
    private int[] pixels;
    private List<DataBlock> blocks;

    public ParametFile(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = new int[width * height];
        this.blocks = new ArrayList<>();
        int white = 0xFFFFFFFF;
        Arrays.fill(pixels, white);
    }

    public ParametFile(int width, int height, int[] pixels, List<DataBlock> blocks) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
        this.blocks = blocks != null ? blocks : new ArrayList<>();
    }

    public void addBlock(DataBlock block) {
        blocks.add(block);
    }

    public List<DataBlock> getBlocks() {
        return blocks;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[] getPixels() {
        return pixels.clone();
    }
    public int[] getPixelsRef() {
        return pixels; // dùng nội bộ khi cần sửa trực tiếp
    }

    // ✅ GIỮ TÊN CŨ - saveToFile
    public void saveToFile(String filename) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(filename, "rw")) {
            raf.writeInt(0x504C414D);
            raf.writeInt(width);
            raf.writeInt(height);
            raf.writeInt(blocks.size());

            for (DataBlock block : blocks) {
                saveBlock(raf, block);
            }

            for (int pixel : pixels) {
                raf.writeInt(pixel);
            }
            raf.writeInt(0x454E4445);
        }
    }

    private void saveBlock(RandomAccessFile raf, DataBlock block) throws IOException {
        raf.writeInt(block.getId());
        raf.writeUTF(block.getName());
        raf.writeInt(block.getX());
        raf.writeInt(block.getY());
        raf.writeInt(block.getWidth());
        raf.writeInt(block.getHeight());

        // save pixel
        int[] pixels = block.getPixels();
        for (int p : pixels) {
            raf.writeInt(p);
        }

        // 🔥 save children
        List<DataBlock> children = block.getChildren();
        raf.writeInt(children.size());
        for (DataBlock child : children) {
            saveBlock(raf, child); // recursive
        }
    }

    // ✅ GIỮ TÊN CŨ - loadFromFile
    public static ParametFile loadFromFile(String filename) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(filename, "r")) {
            int magic = raf.readInt();
            if (magic != 0x504C414D) throw new IOException("Invalid file format");

            int w = raf.readInt();
            int h = raf.readInt();
            int numBlocks = raf.readInt();

            List<DataBlock> blocks = new ArrayList<>();
            for (int i = 0; i < numBlocks; i++) {
                blocks.add(loadBlock(raf));
            }

            int[] pixels = new int[w * h];
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = raf.readInt();
            }

            int end = raf.readInt();
            if (end != 0x454E4445) throw new IOException("Missing ENDE marker");

            return new ParametFile(w, h, pixels, blocks);
        }
    }

    private static DataBlock loadBlock(RandomAccessFile raf) throws IOException {
        int id = raf.readInt();
        String name = raf.readUTF();
        int x = raf.readInt();
        int y = raf.readInt();
        int w = raf.readInt();
        int h = raf.readInt();

        DataBlock block = new DataBlock(id, name, w, h);
        block.setX(x);
        block.setY(y);

        // load pixel
        for (int i = 0; i < w * h; i++) {
            block.getPixels()[i] = raf.readInt(); // ⚡ nhanh hơn setPixel
        }
        // 🔥 load children
        int numChildren = raf.readInt();
        for (int i = 0; i < numChildren; i++) {
            DataBlock child = loadBlock(raf);
            block.addChild(child);
        }
        return block;
    }
}
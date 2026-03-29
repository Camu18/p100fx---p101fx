package Lam.camu.p100fx.model;

import java.util.*;

public class DataBlock {
    private int id;
    private String name;
    private int x, y;
    private int width, height;
    private int[] pixels;  // Mảng 1 chiều [y * width + x]
    private List<DataBlock> children;
    private DataBlock parent;
 //   private List<Control> controls;
    private boolean visible;
    private int opacity;
    private boolean modified;

    public DataBlock(int id, String name, int width, int height) {
        this.id = id;
        this.name = name;
        this.x = 0;
        this.y = 0;
        this.width = width;
        this.height = height;
        this.pixels = new int[width * height];
        this.children = new ArrayList<>();
        this.visible = true;
        this.opacity = 255;
        this.modified = false;
    }

    private int index(int x, int y) {
        return y * width + x;
    }

    public int getPixel(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return 0;
        return pixels[index(x, y)];
    }

    public void setPixel(int x, int y, int argb) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;
        int idx = y * width + x;
        if (pixels[idx] != argb) {
            pixels[idx] = argb;
            modified = true;
        }
 // vẽ đè liên tục
  //       if (x < 0 || x >= width || y < 0 || y >= height) return;
 //       pixels[index(x, y)] = argb;
//        modified = true;
    }
    // hàm này để dùng cho ParametUtils
    public void setModified(boolean modified) {
        this.modified = modified;
    }
    // ==================== TREE ====================

    public void addChild(DataBlock child) {
        child.parent = this;
        children.add(child);
        modified = true;
    }

    public void removeChild(DataBlock child) {
        children.remove(child);
        child.parent = null;
        modified = true;
    }

    public List<DataBlock> getChildren() {
        return children;
    }

    public DataBlock getParent() {
        return parent;
    }
    // ==================== POSITION ====================
    public int getAbsoluteX() {
        return parent == null ? x : parent.getAbsoluteX() + x;
    }
    public int getAbsoluteY() {
        return parent == null ? y : parent.getAbsoluteY() + y;
    }
    // ==================== GETTERS ====================

    public int[] getPixels() { return pixels; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean v) { visible = v; }
    public int getOpacity() { return opacity; }
    public void setOpacity(int o) {
        opacity = Math.max(0, Math.min(255, o));
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isModified() { return modified; }
    public void addControl(Control c) {
        if (c != null) controls.add(c);
    }

    public List<Control> getControls() {
        return controls;
    }
    private List<Control> controls = new ArrayList<>();
}
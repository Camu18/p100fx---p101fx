package Lam.camu.p100fx.model;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.*;

public class PixelCanvas {

    // ==================== FIELDS ====================

    private Canvas canvas;
    private GraphicsContext gc;
    private WritableImage image;
    private PixelWriter pixelWriter;

    private int[] pixels;
    private int width, height;

    private Color currentColor = Color.BLACK;
    private int brushSize = 1;
    private Tool currentTool = Tool.BRUSH;

    // Dirty region tracking
    private int dirtyMinX = Integer.MAX_VALUE;
    private int dirtyMaxX = -1;
    private int dirtyMinY = Integer.MAX_VALUE;
    private int dirtyMaxY = -1;
    private boolean dirty = false;

    // Delta Undo/Redo
    private Stack<UndoCommand> undoStack = new Stack<>();
    private Stack<UndoCommand> redoStack = new Stack<>();
    private Map<Integer, PixelChange> currentBatchChanges = null;
    private boolean inBatch = false;

    // Text cache
    private static class TextCache {
        WritableImage buffer;
        String lastText;
        Color lastColor;
    }
    private TextCache textCache = new TextCache();

    // ==================== INNER CLASSES ====================

    private static class PixelChange {
        final int index;
        final int oldColor;
        int newColor;

        PixelChange(int index, int oldColor, int newColor) {
            this.index = index;
            this.oldColor = oldColor;
            this.newColor = newColor;
        }

        void updateNewColor(int color) {
            this.newColor = color;
        }
    }

    private static class DeltaCommand implements UndoCommand {
        final List<PixelChange> changes;

        DeltaCommand(List<PixelChange> changes) {
            this.changes = changes;
        }

        @Override
        public void undo(PixelCanvas canvas) {
            for (PixelChange change : changes) {
                canvas.pixels[change.index] = change.oldColor;
                canvas.markDirty(change.index % canvas.width, change.index / canvas.width);
            }
            canvas.render();
        }

        @Override
        public void redo(PixelCanvas canvas) {
            for (PixelChange change : changes) {
                canvas.pixels[change.index] = change.newColor;
                canvas.markDirty(change.index % canvas.width, change.index / canvas.width);
            }
            canvas.render();
        }
    }

    private static class ResizeCommand implements UndoCommand {
        final int oldWidth, oldHeight;
        final int[] oldPixels;
        final int newWidth, newHeight;
        int[] newPixels;  // KHÔNG final để có thể gán sau

        ResizeCommand(int oldW, int oldH, int[] oldPix, int newW, int newH, int[] newPix) {
            this.oldWidth = oldW;
            this.oldHeight = oldH;
            this.oldPixels = oldPix.clone();
            this.newWidth = newW;
            this.newHeight = newH;
            this.newPixels = newPix != null ? newPix.clone() : null;
        }

        @Override
        public void undo(PixelCanvas canvas) {
            canvas.width = oldWidth;
            canvas.height = oldHeight;
            canvas.pixels = oldPixels.clone();
            canvas.image = new WritableImage(oldWidth, oldHeight);
            canvas.pixelWriter = canvas.image.getPixelWriter();
            canvas.canvas.setWidth(oldWidth);
            canvas.canvas.setHeight(oldHeight);
            canvas.renderFull();
        }

        @Override
        public void redo(PixelCanvas canvas) {
            canvas.width = newWidth;
            canvas.height = newHeight;
            canvas.pixels = newPixels.clone();
            canvas.image = new WritableImage(newWidth, newHeight);
            canvas.pixelWriter = canvas.image.getPixelWriter();
            canvas.canvas.setWidth(newWidth);
            canvas.canvas.setHeight(newHeight);
            canvas.renderFull();
        }
    }

    private interface UndoCommand {
        void undo(PixelCanvas canvas);
        void redo(PixelCanvas canvas);
    }

    // ==================== CONSTRUCTOR ====================

    public PixelCanvas(Canvas canvas, int width, int height) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.width = width;
        this.height = height;

        canvas.setWidth(width);
        canvas.setHeight(height);

        image = new WritableImage(width, height);
        pixelWriter = image.getPixelWriter();
        pixels = new int[width * height];

        clear();
    }

    // ==================== DIRTY REGION ====================

    private void markDirty(int x, int y) {
        dirtyMinX = Math.min(dirtyMinX, x);
        dirtyMaxX = Math.max(dirtyMaxX, x);
        dirtyMinY = Math.min(dirtyMinY, y);
        dirtyMaxY = Math.max(dirtyMaxY, y);
        dirty = true;
    }

    private void resetDirtyRegion() {
        dirtyMinX = Integer.MAX_VALUE;
        dirtyMaxX = -1;
        dirtyMinY = Integer.MAX_VALUE;
        dirtyMaxY = -1;
        dirty = false;
    }

    // ==================== RENDER ====================

    public void render() {
        if (!dirty) return;

        for (int y = dirtyMinY; y <= dirtyMaxY; y++) {
            if (y < 0 || y >= height) continue;
            int rowStart = y * width;
            for (int x = dirtyMinX; x <= dirtyMaxX; x++) {
                if (x < 0 || x >= width) continue;
                pixelWriter.setArgb(x, y, pixels[rowStart + x]);
            }
        }

        gc.drawImage(image, 0, 0);
        resetDirtyRegion();
    }

    public void renderFull() {
        for (int i = 0; i < pixels.length; i++) {
            int x = i % width;
            int y = i / width;
            pixelWriter.setArgb(x, y, pixels[i]);
        }
        gc.drawImage(image, 0, 0);
    }

    // ==================== BATCH OPERATIONS ====================

    public void beginBatch() {
        inBatch = true;
        currentBatchChanges = new HashMap<>();
    }

    public void endBatch() {
        if (!inBatch) return;

        if (currentBatchChanges != null && !currentBatchChanges.isEmpty()) {
            List<PixelChange> changes = new ArrayList<>(currentBatchChanges.values());
            undoStack.push(new DeltaCommand(changes));
            redoStack.clear();
        }

        currentBatchChanges = null;
        inBatch = false;
        render();
    }

    // ==================== PIXEL OPERATIONS ====================

    private int index(int x, int y) {
        return y * width + x;
    }

    public void setPixel(int x, int y, Color color) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;

        int idx = index(x, y);
        int oldColor = pixels[idx];
        int newColor = colorToARGB(color);

        if (oldColor == newColor) return;

        pixels[idx] = newColor;
        markDirty(x, y);

        if (inBatch && currentBatchChanges != null) {
            PixelChange change = currentBatchChanges.get(idx);
            if (change == null) {
                currentBatchChanges.put(idx, new PixelChange(idx, oldColor, newColor));
            } else {
                change.updateNewColor(newColor);
            }
        }
    }
    // TRẢ VỀ Color
    public Color getColor(int x, int y) {
        int argb = getPixel(x, y);
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return Color.rgb(r, g, b, a / 255.0);
    }
    // ==================== DRAW TOOLS ====================

    // ✅ THÊM METHOD NÀY - trả về int ARGB
    public int getPixel(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return 0;
        return pixels[index(x, y)];
    }
    public void drawPixel(int x, int y) {
        beginBatch();
        if (brushSize == 1) {
            setPixel(x, y, currentColor);
        } else {
            drawBrush(x, y, brushSize, currentColor);
        }
        endBatch();
    }

    public void drawBrush(int x, int y, int size, Color color) {
        int half = size / 2;
        for (int dy = -half; dy <= half; dy++) {
            for (int dx = -half; dx <= half; dx++) {
                setPixel(x + dx, y + dy, color);
            }
        }
    }

    // ==================== LINE ====================

    public void drawLine(int x1, int y1, int x2, int y2, Color color) {
        beginBatch();

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            setPixel(x1, y1, color);
            if (x1 == x2 && y1 == y2) break;

            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x1 += sx; }
            if (e2 < dx) { err += dx; y1 += sy; }
        }

        endBatch();
    }

    // ==================== RECTANGLE ====================

    public void drawRectangle(int x1, int y1, int x2, int y2, boolean filled, Color color) {
        beginBatch();

        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int maxX = Math.max(x1, x2);
        int maxY = Math.max(y1, y2);

        if (filled) {
            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    setPixel(x, y, color);
                }
            }
        } else {
            for (int x = minX; x <= maxX; x++) {
                setPixel(x, minY, color);
                setPixel(x, maxY, color);
            }
            for (int y = minY; y <= maxY; y++) {
                setPixel(minX, y, color);
                setPixel(maxX, y, color);
            }
        }

        endBatch();
    }

    // ==================== CIRCLE ====================

    public void drawCircle(int cx, int cy, int x2, int y2, boolean filled, Color color) {
        beginBatch();

        int radius = (int) Math.hypot(x2 - cx, y2 - cy);
        int x = radius;
        int y = 0;
        int err = 1 - x;

        while (x >= y) {
            if (filled) {
                drawHorizontalLine(cx - x, cx + x, cy + y, color);
                drawHorizontalLine(cx - x, cx + x, cy - y, color);
                drawHorizontalLine(cx - y, cx + y, cy + x, color);
                drawHorizontalLine(cx - y, cx + y, cy - x, color);
            } else {
                setPixel(cx + x, cy + y, color);
                setPixel(cx - x, cy + y, color);
                setPixel(cx + x, cy - y, color);
                setPixel(cx - x, cy - y, color);
                setPixel(cx + y, cy + x, color);
                setPixel(cx - y, cy + x, color);
                setPixel(cx + y, cy - x, color);
                setPixel(cx - y, cy - x, color);
            }

            y++;
            if (err < 0) {
                err += 2 * y + 1;
            } else {
                x--;
                err += 2 * (y - x + 1);
            }
        }

        endBatch();
    }

    private void drawHorizontalLine(int x1, int x2, int y, Color color) {
        for (int x = x1; x <= x2; x++) {
            setPixel(x, y, color);
        }
    }

    // ==================== FLOOD FILL ====================

    public void floodFill(int x, int y, Color newColor) {
        beginBatch();

        int target = pixels[index(x, y)];
        int replacement = colorToARGB(newColor);

        if (target == replacement) {
            endBatch();
            return;
        }

        Stack<Integer> stack = new Stack<>();
        stack.push(y * width + x);

        while (!stack.isEmpty()) {
            int pos = stack.pop();
            int px = pos % width;
            int py = pos / width;

            if (px < 0 || px >= width || py < 0 || py >= height) continue;
            if (pixels[pos] != target) continue;

            setPixel(px, py, newColor);

            stack.push(py * width + (px + 1));
            stack.push(py * width + (px - 1));
            stack.push((py + 1) * width + px);
            stack.push((py - 1) * width + px);
        }

        endBatch();
    }

    // ==================== TEXT (FIXED) ====================

    public void drawText(int x, int y, String text) {
        beginBatch();

        javafx.scene.text.Font font = javafx.scene.text.Font.font("Monospaced", 12);
        javafx.scene.text.Text textNode = new javafx.scene.text.Text(text);
        textNode.setFont(font);
        textNode.setFill(currentColor);

        double textWidth = textNode.getLayoutBounds().getWidth();
        double textHeight = textNode.getLayoutBounds().getHeight();

        // ✅ SỬA LỖI: Dùng Canvas để lấy GraphicsContext
        Canvas tempCanvas = new Canvas(textWidth + 1, textHeight + 1);
        GraphicsContext textGc = tempCanvas.getGraphicsContext2D();
        textGc.setFill(currentColor);
        textGc.fillText(text, 0, textHeight - 2);

        // Lấy ảnh từ canvas tạm
        WritableImage textImage = tempCanvas.snapshot(null, null);

        copyTextImage(x, y, textImage);

        endBatch();
    }

    private void copyTextImage(int x, int y, WritableImage textImage) {
        int textWidth = (int) textImage.getWidth();
        int textHeight = (int) textImage.getHeight();
        PixelReader reader = textImage.getPixelReader();

        for (int ty = 0; ty < textHeight; ty++) {
            for (int tx = 0; tx < textWidth; tx++) {
                int px = x + tx;
                int py = y + ty;
                if (px >= 0 && px < width && py >= 0 && py < height) {
                    Color color = reader.getColor(tx, ty);
                    if (color.getOpacity() > 0) {
                        setPixel(px, py, color);
                    }
                }
            }
        }
    }

    // ==================== UNDO / REDO ====================

    public void undo() {
        if (undoStack.isEmpty()) return;
        UndoCommand cmd = undoStack.pop();
        cmd.undo(this);
        redoStack.push(cmd);
    }

    public void redo() {
        if (redoStack.isEmpty()) return;
        UndoCommand cmd = redoStack.pop();
        cmd.redo(this);
        undoStack.push(cmd);
    }

    // ==================== RESIZE ====================

    public void resize(int newWidth, int newHeight) {
        int[] newPixels = new int[newWidth * newHeight];
        int white = colorToARGB(Color.WHITE);
        Arrays.fill(newPixels, white);

        int copyWidth = Math.min(width, newWidth);
        int copyHeight = Math.min(height, newHeight);

        for (int y = 0; y < copyHeight; y++) {
            System.arraycopy(pixels, y * width, newPixels, y * newWidth, copyWidth);
        }

        ResizeCommand resizeCmd = new ResizeCommand(
                width, height, pixels,
                newWidth, newHeight, newPixels
        );

        this.pixels = newPixels;
        this.width = newWidth;
        this.height = newHeight;

        canvas.setWidth(newWidth);
        canvas.setHeight(newHeight);

        image = new WritableImage(newWidth, newHeight);
        pixelWriter = image.getPixelWriter();

        renderFull();

        undoStack.push(resizeCmd);
        redoStack.clear();
    }

    // ==================== CLEAR ====================

    public void clear() {
        beginBatch();

        int white = colorToARGB(Color.WHITE);
        Arrays.fill(pixels, white);

        dirtyMinX = 0;
        dirtyMaxX = width - 1;
        dirtyMinY = 0;
        dirtyMaxY = height - 1;
        dirty = true;

        endBatch();
    }

    // ==================== GETTERS / SETTERS ====================

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Canvas getCanvas() { return canvas; }
    public Color getCurrentColor() { return currentColor; }
    public void setCurrentColor(Color color) { this.currentColor = color; }
    public int getBrushSize() { return brushSize; }
    public void setBrushSize(int size) { this.brushSize = Math.max(1, Math.min(size, 20)); }
    public Tool getCurrentTool() { return currentTool; }
    public void setCurrentTool(Tool tool) { this.currentTool = tool; }

    public int[] getPixelArray() {
        return pixels.clone();
    }

    public void setPixelArray(int[] newPixels) {
        if (newPixels.length == pixels.length) {
            pixels = newPixels.clone();
            renderFull();
        }
    }

    // ==================== COLOR CONVERSION ====================

    private int colorToARGB(Color color) {
        int a = (int) (color.getOpacity() * 255);
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
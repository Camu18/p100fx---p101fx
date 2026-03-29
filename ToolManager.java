package Lam.camu.p100fx.service;

import Lam.camu.p100fx.model.DataBlock;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 * Quản lý các công cụ vẽ (Brush, Eraser, Fill, Line, Rectangle, Circle, Text)
 */
public class ToolManager {

    // Các loại công cụ
    public static final int TOOL_BRUSH = 0;
    public static final int TOOL_ERASER = 1;
    public static final int TOOL_FILL = 2;
    public static final int TOOL_LINE = 3;
    public static final int TOOL_RECTANGLE = 4;
    public static final int TOOL_CIRCLE = 5;
    public static final int TOOL_TEXT = 6;
    public static final int TOOL_PICKER = 7;

    private int currentTool = TOOL_BRUSH;
    private int brushSize = 1;
    private int currentColor = 0xFF000000;  // Black
    private boolean isDrawing = false;

    // For shape tools
    private int startX, startY;
    private int endX, endY;

    // Listener để thông báo khi tool thay đổi
    private ToolChangeListener listener;

    public interface ToolChangeListener {
        void onToolChanged(int newTool);
        void onColorChanged(int newColor);
        void onBrushSizeChanged(int newSize);
    }

    public ToolManager() {
    }

    // ==================== TOOL SELECTION ====================

    public void setTool(int tool) {
        this.currentTool = tool;
        if (listener != null) {
            listener.onToolChanged(tool);
        }
        System.out.println("🛠️ Tool changed to: " + getToolName(tool));
    }

    public int getCurrentTool() {
        return currentTool;
    }

    public String getToolName(int tool) {
        switch (tool) {
            case TOOL_BRUSH: return "Brush";
            case TOOL_ERASER: return "Eraser";
            case TOOL_FILL: return "Fill";
            case TOOL_LINE: return "Line";
            case TOOL_RECTANGLE: return "Rectangle";
            case TOOL_CIRCLE: return "Circle";
            case TOOL_TEXT: return "Text";
            case TOOL_PICKER: return "Color Picker";
            default: return "Unknown";
        }
    }

    // ==================== COLOR MANAGEMENT ====================

    public void setColor(int color) {
        this.currentColor = color;
        if (listener != null) {
            listener.onColorChanged(color);
        }
    }

    public void setColor(int r, int g, int b, int a) {
        this.currentColor = (a << 24) | (r << 16) | (g << 8) | b;
        if (listener != null) {
            listener.onColorChanged(currentColor);
        }
    }

    public int getColor() {
        return currentTool == TOOL_ERASER ? 0x00000000 : currentColor;
    }

    // ==================== BRUSH SIZE ====================

    public void setBrushSize(int size) {
        this.brushSize = Math.max(1, Math.min(size, 20));
        if (listener != null) {
            listener.onBrushSizeChanged(brushSize);
        }
    }

    public int getBrushSize() {
        return brushSize;
    }

    // ==================== DRAWING ACTIONS ====================

    public void onMousePressed(DataBlock block, int x, int y, int row, int col) {
        isDrawing = true;
        startX = col;
        startY = row;
        endX = col;
        endY = row;

        switch (currentTool) {
            case TOOL_BRUSH:
            case TOOL_ERASER:
                drawPixel(block, col, row);
                break;

            case TOOL_FILL:
                floodFill(block, col, row, getColor());
                break;

            case TOOL_PICKER:
                pickColor(block, col, row);
                break;
        }
    }

    public void onMouseDragged(DataBlock block, int x, int y, int row, int col) {
        if (!isDrawing) return;

        endX = col;
        endY = row;

        switch (currentTool) {
            case TOOL_BRUSH:
            case TOOL_ERASER:
                drawLine(block, startX, startY, col, row);
                startX = col;
                startY = row;
                break;
        }
    }

    public void onMouseReleased(DataBlock block, int x, int y, int row, int col) {
        if (!isDrawing) return;

        endX = col;
        endY = row;

        switch (currentTool) {
            case TOOL_LINE:
                drawLineShape(block, startX, startY, endX, endY);
                break;

            case TOOL_RECTANGLE:
                drawRectangle(block, startX, startY, endX, endY);
                break;

            case TOOL_CIRCLE:
                drawCircle(block, startX, startY, endX, endY);
                break;
        }

        isDrawing = false;
    }

    // ==================== DRAWING ALGORITHMS ====================

    private void drawPixel(DataBlock block, int col, int row) {
        int color = getColor();

        if (brushSize == 1) {
            block.setPixel(col, row, color);
        } else {
            // Vẽ brush size lớn hơn
            int half = brushSize / 2;
            for (int dy = -half; dy <= half; dy++) {
                for (int dx = -half; dx <= half; dx++) {
                    block.setPixel(col + dx, row + dy, color);
                }
            }
        }
    }

    private void drawLine(DataBlock block, int x1, int y1, int x2, int y2) {
        // Bresenham's line algorithm
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            drawPixel(block, x1, y1);

            if (x1 == x2 && y1 == y2) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    private void drawLineShape(DataBlock block, int x1, int y1, int x2, int y2) {
        drawLine(block, x1, y1, x2, y2);
    }

    private void drawRectangle(DataBlock block, int x1, int y1, int x2, int y2) {
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);

        // Vẽ 4 cạnh
        for (int x = minX; x <= maxX; x++) {
            drawPixel(block, x, minY);
            drawPixel(block, x, maxY);
        }
        for (int y = minY; y <= maxY; y++) {
            drawPixel(block, minX, y);
            drawPixel(block, maxX, y);
        }
    }

    private void drawCircle(DataBlock block, int x1, int y1, int x2, int y2) {
        int radius = (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        int x = 0;
        int y = radius;
        int d = 3 - 2 * radius;

        drawCirclePoints(block, x1, y1, x, y);

        while (y >= x) {
            x++;
            if (d > 0) {
                y--;
                d = d + 4 * (x - y) + 10;
            } else {
                d = d + 4 * x + 6;
            }
            drawCirclePoints(block, x1, y1, x, y);
        }
    }

    private void drawCirclePoints(DataBlock block, int cx, int cy, int x, int y) {
        drawPixel(block, cx + x, cy + y);
        drawPixel(block, cx - x, cy + y);
        drawPixel(block, cx + x, cy - y);
        drawPixel(block, cx - x, cy - y);
        drawPixel(block, cx + y, cy + x);
        drawPixel(block, cx - y, cy + x);
        drawPixel(block, cx + y, cy - x);
        drawPixel(block, cx - y, cy - x);
    }

    private void floodFill(DataBlock block, int col, int row, int newColor) {
        int targetColor = block.getPixel(col, row);
        if (targetColor == newColor) return;

        // Stack-based flood fill
        java.util.Stack<int[]> stack = new java.util.Stack<>();
        stack.push(new int[]{col, row});

        while (!stack.isEmpty()) {
            int[] p = stack.pop();
            int x = p[0];
            int y = p[1];

            if (x < 0 || x >= block.getWidth() || y < 0 || y >= block.getHeight()) continue;
            if (block.getPixel(x, y) != targetColor) continue;

            block.setPixel(x, y, newColor);

            stack.push(new int[]{x + 1, y});
            stack.push(new int[]{x - 1, y});
            stack.push(new int[]{x, y + 1});
            stack.push(new int[]{x, y - 1});
        }
    }

    private void pickColor(DataBlock block, int col, int row) {
        int color = block.getPixel(col, row);
        if (color != 0) {  // Không phải transparent
            setColor(color);
        }
    }

    // ==================== RENDER PREVIEW ====================

    public void renderPreview(GraphicsContext gc, int mouseX, int mouseY) {
        if (!isDrawing) return;

        gc.setStroke(Color.rgb(255, 255, 255, 0.5));
        gc.setLineWidth(1);
        gc.setLineDashes(5);

        switch (currentTool) {
            case TOOL_LINE:
                gc.strokeLine(startX, startY, mouseX, mouseY);
                break;

            case TOOL_RECTANGLE:
                gc.strokeRect(startX, startY,
                        mouseX - startX, mouseY - startY);
                break;

            case TOOL_CIRCLE:
                double radius = Math.hypot(mouseX - startX, mouseY - startY);
                gc.strokeOval(startX - radius, startY - radius,
                        radius * 2, radius * 2);
                break;
        }
    }

    // ==================== LISTENER ====================

    public void setListener(ToolChangeListener listener) {
        this.listener = listener;
    }
}

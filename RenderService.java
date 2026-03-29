package Lam.camu.p100fx.service;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

import Lam.camu.p100fx.model.DataBlock;

public class RenderService {

    /**
     * Vẽ viewport lên canvas.
     * @param block DataBlock nguồn
     * @param canvas Canvas đích (JavaFX)
     * @param viewportX Tọa độ X góc trên bên trái của viewport trong block
     * @param viewportY Tọa độ Y góc trên bên trái của viewport trong block
     */
    public void render(DataBlock block, Canvas canvas, int viewportX, int viewportY) {
        int canvasWidth = (int) canvas.getWidth();
        int canvasHeight = (int) canvas.getHeight();

        PixelWriter writer = canvas.getGraphicsContext2D().getPixelWriter();

        // Duyệt từng pixel trên canvas
        for (int cy = 0; cy < canvasHeight; cy++) {
            int vy = viewportY + cy;
            if (vy < 0 || vy >= block.getHeight()) {
                // Nếu ngoài block, vẽ màu nền (trong suốt)
                for (int cx = 0; cx < canvasWidth; cx++) {
                    writer.setColor(cx, cy, Color.TRANSPARENT);
                }
                continue;
            }

            for (int cx = 0; cx < canvasWidth; cx++) {
                int vx = viewportX + cx;
                if (vx < 0 || vx >= block.getWidth()) {
                    writer.setColor(cx, cy, Color.TRANSPARENT);
                } else {
                    int argb = block.getPixel(vx, vy);
                    Color color = intToColor(argb);
                    writer.setColor(cx, cy, color);
                }
            }
        }
    }

    /**
     * Chuyển đổi int ARGB sang JavaFX Color.
     */
    private Color intToColor(int argb) {
        int a = (argb >> 24) & 0xff;
        int r = (argb >> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = argb & 0xff;
        return Color.rgb(r, g, b, a / 255.0);
    }
}

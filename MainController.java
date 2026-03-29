package Lam.camu.p100fx.controller;

import Lam.camu.p100fx.model.PixelCanvas;
import Lam.camu.p100fx.model.Tool;
import Lam.camu.p100fx.model.ParametFile;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.*;

public class MainController implements Initializable {

    // ==================== FXML INJECTIONS ====================
    @FXML private Canvas mainCanvas;
    @FXML private StackPane canvasContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private Label cursorPositionLabel;
    @FXML private Label zoomLabel;
    @FXML private Label toolLabel;
    @FXML private Label colorLabel;
    @FXML private Label statusBar;
    @FXML private RadioMenuItem brushMenuItem;
    @FXML private RadioMenuItem eraserMenuItem;
    @FXML private RadioMenuItem rectMenuItem;
    @FXML private RadioMenuItem circleMenuItem;
    @FXML private RadioMenuItem lineMenuItem;
    @FXML private RadioMenuItem textMenuItem;
    @FXML private RadioMenuItem fillMenuItem;
    @FXML private RadioMenuItem pickerMenuItem;
    @FXML private ToggleGroup toolsToggleGroup;
    @FXML private Canvas gridCanvas;
    @FXML private Canvas rulersCanvas;
    @FXML private CheckMenuItem showGridMenuItem;
    @FXML private CheckMenuItem showPixelGridMenuItem;
    @FXML private CheckMenuItem showRulersMenuItem;
    @FXML private Slider brushSizeSlider;
    @FXML private Label brushSizeLabel;
    @FXML private ColorPicker colorPicker;
    @FXML private HBox colorPalette;
    @FXML private HBox recentColorsBox;

    // ==================== FIELDS ====================
    private PixelCanvas pixelCanvas;
    private Stage primaryStage;
    private Tool currentTool = Tool.BRUSH;
    private Color currentColor = Color.BLACK;
    private double zoom = 1.0;
    private int dragStartX, dragStartY;
    private boolean isDragging = false;
    private ParametFile currentFile;
    private String currentFilePath;
    private boolean showGrid = false;
    private boolean showPixelGrid = false;
    private boolean showRulers = false;
    private static final int RULER_SIZE = 20;
    private int gridSize = 50;
    private Color gridColor = Color.LIGHTGRAY;
    private Color pixelGridColor = Color.rgb(200, 200, 200, 0.5);
    private int brushSize = 1;

    // Color Palette
    private List<Color> presetColors = new ArrayList<>();
    private List<Rectangle> colorSwatches = new ArrayList<>();
    private LinkedList<Color> recentColors = new LinkedList<>();
    private static final int MAX_RECENT_COLORS = 12;
    private static final int SWATCH_SIZE = 28;

    // ==================== INITIALIZE ====================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pixelCanvas = new PixelCanvas(mainCanvas, 1260, 630);
        pixelCanvas.setBrushSize(brushSize);

        mainCanvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupKeyboardShortcuts(newScene);
            }
        });

        mainCanvas.setFocusTraversable(true);
        setupBrushSizeSlider();
        initColorPalette();
        initRecentColors();
        setupColorPicker();
        updateStatusBar();
        currentFile = new ParametFile(1260, 630);

        System.out.println("✅ MainController initialized");
        System.out.println("   Canvas size: " + pixelCanvas.getWidth() + "x" + pixelCanvas.getHeight());
    }

    private void setupBrushSizeSlider() {
        if (brushSizeSlider != null) {
            brushSizeSlider.setMin(1);
            brushSizeSlider.setMax(20);
            brushSizeSlider.setValue(brushSize);
            brushSizeSlider.setShowTickLabels(true);
            brushSizeSlider.setShowTickMarks(true);
            brushSizeSlider.setMajorTickUnit(5);
            brushSizeSlider.setMinorTickCount(4);

            brushSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                brushSize = newVal.intValue();
                pixelCanvas.setBrushSize(brushSize);
                if (brushSizeLabel != null) {
                    brushSizeLabel.setText(brushSize + "px");
                }
            });
        }
    }

    // ==================== COLOR PALETTE METHODS ====================

    private void initColorPalette() {
        // Thêm các màu cơ bản
        presetColors.addAll(Arrays.asList(
                Color.BLACK, Color.DARKGRAY, Color.GRAY, Color.LIGHTGRAY, Color.WHITE,
                Color.RED, Color.DARKRED, Color.ORANGE, Color.YELLOW,
                Color.GREEN, Color.DARKGREEN, Color.CYAN,
                Color.BLUE, Color.DARKBLUE, Color.PURPLE, Color.MAGENTA, Color.PINK, Color.BROWN,
                Color.rgb(255, 192, 203), Color.rgb(173, 216, 230), Color.rgb(144, 238, 144),
                Color.rgb(255, 218, 185), Color.rgb(218, 165, 32), Color.rgb(75, 0, 130),
                Color.rgb(0, 255, 127), Color.rgb(255, 105, 180)
        ));

        // Tạo các ô màu
        for (Color color : presetColors) {
            Rectangle swatch = createColorSwatch(color);
            colorSwatches.add(swatch);
            colorPalette.getChildren().add(swatch);
        }
    }

    private void initRecentColors() {
        if (recentColorsBox == null) return;
        recentColorsBox.setAlignment(Pos.CENTER_LEFT);
        // Thêm một số màu mặc định
        addToRecentColors(Color.BLACK);
        addToRecentColors(Color.WHITE);
        addToRecentColors(Color.RED);
        addToRecentColors(Color.BLUE);
    }

    private Rectangle createColorSwatch(Color color) {
        Rectangle swatch = new Rectangle(SWATCH_SIZE, SWATCH_SIZE);
        swatch.setFill(color);
        swatch.setStroke(Color.GRAY);
        swatch.setStrokeWidth(1);
        swatch.setArcWidth(5);
        swatch.setArcHeight(5);
        swatch.setStyle("-fx-cursor: hand;");

        // Thêm border đậm cho màu đang chọn
        if (color.equals(currentColor)) {
            swatch.setStrokeWidth(3);
            swatch.setStroke(Color.WHITE);
            swatch.setEffect(new DropShadow(3, Color.BLACK));
        }

        swatch.setOnMouseClicked(e -> setCurrentColor(color));

        // Thêm tooltip
        Tooltip tooltip = new Tooltip(getColorName(color));
        Tooltip.install(swatch, tooltip);

        // Thêm context menu để thêm vào recent
        ContextMenu contextMenu = new ContextMenu();
        MenuItem addToRecent = new MenuItem("Add to Recent Colors");
        addToRecent.setOnAction(e -> addToRecentColors(color));
        contextMenu.getItems().add(addToRecent);
        swatch.setOnContextMenuRequested(e -> contextMenu.show(swatch, e.getScreenX(), e.getScreenY()));

        return swatch;
    }

    private void updateColorSwatchSelection() {
        // Update preset colors
        for (int i = 0; i < colorSwatches.size(); i++) {
            Rectangle swatch = colorSwatches.get(i);
            Color swatchColor = (Color) swatch.getFill();

            if (swatchColor.equals(currentColor)) {
                swatch.setStrokeWidth(3);
                swatch.setStroke(Color.WHITE);
                swatch.setEffect(new DropShadow(3, Color.BLACK));
            } else {
                swatch.setStrokeWidth(1);
                swatch.setStroke(Color.GRAY);
                swatch.setEffect(null);
            }
        }

        // Update recent colors
        updateRecentColorsDisplay();

        // Update ColorPicker
        if (colorPicker != null) {
            colorPicker.setValue(currentColor);
        }
    }

    private void addToRecentColors(Color color) {
        // Xóa nếu đã tồn tại
        recentColors.remove(color);
        // Thêm vào đầu
        recentColors.addFirst(color);
        // Giới hạn số lượng
        while (recentColors.size() > MAX_RECENT_COLORS) {
            recentColors.removeLast();
        }
        updateRecentColorsDisplay();
    }

    private void updateRecentColorsDisplay() {
        if (recentColorsBox == null) return;

        recentColorsBox.getChildren().clear();

        for (Color color : recentColors) {
            Rectangle swatch = new Rectangle(SWATCH_SIZE - 4, SWATCH_SIZE - 4);
            swatch.setFill(color);
            swatch.setStroke(Color.GRAY);
            swatch.setStrokeWidth(1);
            swatch.setArcWidth(4);
            swatch.setArcHeight(4);
            swatch.setStyle("-fx-cursor: hand;");

            if (color.equals(currentColor)) {
                swatch.setStrokeWidth(2);
                swatch.setStroke(Color.WHITE);
                swatch.setEffect(new DropShadow(2, Color.BLACK));
            }

            swatch.setOnMouseClicked(e -> setCurrentColor(color));

            Tooltip tooltip = new Tooltip(getColorName(color));
            Tooltip.install(swatch, tooltip);

            recentColorsBox.getChildren().add(swatch);
        }
    }

    private void setupColorPicker() {
        if (colorPicker != null) {
            colorPicker.setValue(currentColor);
            colorPicker.setOnAction(e -> setCurrentColor(colorPicker.getValue()));
        }
    }

    @FXML
    private void handleColorPickerAction() {
        if (colorPicker != null) {
            setCurrentColor(colorPicker.getValue());
        }
    }

    private String getColorName(Color color) {
        if (color.equals(Color.BLACK)) return "Black";
        if (color.equals(Color.DARKGRAY)) return "Dark Gray";
        if (color.equals(Color.GRAY)) return "Gray";
        if (color.equals(Color.LIGHTGRAY)) return "Light Gray";
        if (color.equals(Color.WHITE)) return "White";
        if (color.equals(Color.RED)) return "Red";
        if (color.equals(Color.DARKRED)) return "Dark Red";
        if (color.equals(Color.ORANGE)) return "Orange";
        if (color.equals(Color.YELLOW)) return "Yellow";
        if (color.equals(Color.GREEN)) return "Green";
        if (color.equals(Color.DARKGREEN)) return "Dark Green";
        if (color.equals(Color.CYAN)) return "Cyan";
        if (color.equals(Color.BLUE)) return "Blue";
        if (color.equals(Color.DARKBLUE)) return "Dark Blue";
        if (color.equals(Color.PURPLE)) return "Purple";
        if (color.equals(Color.MAGENTA)) return "Magenta";
        if (color.equals(Color.PINK)) return "Pink";
        if (color.equals(Color.BROWN)) return "Brown";
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    @FXML
    private void showGradientPicker() {
        Dialog<Color> dialog = new Dialog<>();
        dialog.setTitle("🌈 Gradient Color Picker");
        dialog.setHeaderText("Choose a color from gradient");

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));
        vbox.setAlignment(Pos.CENTER);

        // Gradient preview
        Rectangle gradientPreview = new Rectangle(400, 50);

        // Create gradient stops
        Stop[] stops = new Stop[] {
                new Stop(0, Color.RED),
                new Stop(0.16, Color.ORANGE),
                new Stop(0.33, Color.YELLOW),
                new Stop(0.5, Color.GREEN),
                new Stop(0.66, Color.CYAN),
                new Stop(0.83, Color.BLUE),
                new Stop(1, Color.MAGENTA)
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
        gradientPreview.setFill(gradient);

        // Color preview
        Rectangle colorPreview = new Rectangle(60, 60);
        colorPreview.setFill(currentColor);
        colorPreview.setArcWidth(10);
        colorPreview.setArcHeight(10);
        colorPreview.setStroke(Color.GRAY);
        colorPreview.setStrokeWidth(1);

        // Hue slider
        Label hueLabel = new Label("Hue: 0°");
        Slider hueSlider = new Slider(0, 360, 0);
        hueSlider.setShowTickLabels(true);
        hueSlider.setShowTickMarks(true);
        hueSlider.setMajorTickUnit(90);
        hueSlider.setBlockIncrement(10);

        // Saturation slider
        Label satLabel = new Label("Saturation: 100%");
        Slider satSlider = new Slider(0, 100, 100);
        satSlider.setShowTickLabels(true);
        satSlider.setShowTickMarks(true);

        // Brightness slider
        Label brightLabel = new Label("Brightness: 100%");
        Slider brightSlider = new Slider(0, 100, 100);
        brightSlider.setShowTickLabels(true);
        brightSlider.setShowTickMarks(true);

        // RGB display
        Label rgbLabel = new Label();

        // Update color when sliders change
        Runnable updateColor = () -> {
            double hue = hueSlider.getValue();
            double sat = satSlider.getValue() / 100;
            double bright = brightSlider.getValue() / 100;
            Color newColor = Color.hsb(hue, sat, bright);
            colorPreview.setFill(newColor);
            hueLabel.setText(String.format("Hue: %.0f°", hue));
            satLabel.setText(String.format("Saturation: %.0f%%", sat * 100));
            brightLabel.setText(String.format("Brightness: %.0f%%", bright * 100));
            rgbLabel.setText(String.format("RGB: %d, %d, %d",
                    (int)(newColor.getRed() * 255),
                    (int)(newColor.getGreen() * 255),
                    (int)(newColor.getBlue() * 255)));
        };

        hueSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateColor.run());
        satSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateColor.run());
        brightSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateColor.run());

        // Gradient click handler
        gradientPreview.setOnMouseClicked(e -> {
            double x = e.getX();
            double width = gradientPreview.getWidth();
            double pos = x / width;

            Color selectedColor;
            if (pos <= 0.16) {
                double t = pos / 0.16;
                selectedColor = interpolateColor(Color.RED, Color.ORANGE, t);
            } else if (pos <= 0.33) {
                double t = (pos - 0.16) / 0.17;
                selectedColor = interpolateColor(Color.ORANGE, Color.YELLOW, t);
            } else if (pos <= 0.5) {
                double t = (pos - 0.33) / 0.17;
                selectedColor = interpolateColor(Color.YELLOW, Color.GREEN, t);
            } else if (pos <= 0.66) {
                double t = (pos - 0.5) / 0.16;
                selectedColor = interpolateColor(Color.GREEN, Color.CYAN, t);
            } else if (pos <= 0.83) {
                double t = (pos - 0.66) / 0.17;
                selectedColor = interpolateColor(Color.CYAN, Color.BLUE, t);
            } else {
                double t = (pos - 0.83) / 0.17;
                selectedColor = interpolateColor(Color.BLUE, Color.MAGENTA, t);
            }

            double h = selectedColor.getHue();
            double s = selectedColor.getSaturation() * 100;
            double b = selectedColor.getBrightness() * 100;
            hueSlider.setValue(h);
            satSlider.setValue(s);
            brightSlider.setValue(b);
        });

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        Button selectBtn = new Button("✓ Select Color");
        selectBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        selectBtn.setOnAction(e -> {
            setCurrentColor((Color) colorPreview.getFill());
            dialog.close();
        });

        Button cancelBtn = new Button("✗ Cancel");
        cancelBtn.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(selectBtn, cancelBtn);

        vbox.getChildren().addAll(
                new Label("Click on gradient to pick color:"),
                gradientPreview,
                new Separator(),
                new Label("Or adjust manually:"),
                hueSlider, hueLabel,
                satSlider, satLabel,
                brightSlider, brightLabel,
                rgbLabel,
                new Separator(),
                colorPreview,
                buttonBox
        );

        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);

        dialog.showAndWait();
    }

    private Color interpolateColor(Color c1, Color c2, double t) {
        double r = c1.getRed() + (c2.getRed() - c1.getRed()) * t;
        double g = c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t;
        double b = c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t;
        return Color.color(r, g, b);
    }

    // ==================== UTILITY METHODS ====================
    private Window getWindow() {
        if (primaryStage != null) return primaryStage;
        if (mainCanvas != null && mainCanvas.getScene() != null)
            return mainCanvas.getScene().getWindow();
        return null;
    }

    private void refreshUI() {
        pixelCanvas.renderFull();
        drawGrid();
        drawRulers();
        updateStatusBar();
    }

    // ==================== KEYBOARD SHORTCUTS ====================
    private void setupKeyboardShortcuts(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            boolean ctrl = event.isShortcutDown();

            switch (event.getCode()) {
                case B -> { selectTool(Tool.BRUSH); event.consume(); }
                case E -> { selectTool(Tool.ERASER); event.consume(); }
                case R -> { selectTool(Tool.RECTANGLE); event.consume(); }
                case C -> { selectTool(Tool.CIRCLE); event.consume(); }
                case L -> { selectTool(Tool.LINE); event.consume(); }
                case T -> { selectTool(Tool.TEXT); event.consume(); }
                case F -> { selectTool(Tool.FILL); event.consume(); }
                case I -> { selectTool(Tool.PICKER); event.consume(); }
                case Z -> { if (ctrl) { pixelCanvas.undo(); refreshUI(); event.consume(); } }
                case Y -> { if (ctrl) { pixelCanvas.redo(); refreshUI(); event.consume(); } }
                case PLUS, EQUALS -> { if (ctrl) { zoomIn(); event.consume(); } }
                case MINUS -> { if (ctrl) { zoomOut(); event.consume(); } }
                case DIGIT0 -> { if (ctrl) { resetZoom(); event.consume(); } }
                case S -> { if (ctrl) { handleSave(); event.consume(); } }
                case O -> { if (ctrl) { handleOpen(); event.consume(); } }
                case N -> { if (ctrl) { handleNew(); event.consume(); } }
                default -> {}
            }
        });
    }

    // ==================== MOUSE HANDLERS ====================
    @FXML
    private void handleCanvasMousePressed(MouseEvent event) {
        int x = (int) (event.getX() / zoom);
        int y = (int) (event.getY() / zoom);

        if (x < 0 || x >= pixelCanvas.getWidth() || y < 0 || y >= pixelCanvas.getHeight()) {
            return;
        }

        dragStartX = x;
        dragStartY = y;
        isDragging = true;

        switch (currentTool) {
            case BRUSH -> {
                pixelCanvas.beginBatch();
                pixelCanvas.drawBrush(x, y, brushSize, currentColor);
                pixelCanvas.endBatch();
                refreshUI();
            }
            case ERASER -> {
                pixelCanvas.beginBatch();
                pixelCanvas.drawBrush(x, y, brushSize, Color.TRANSPARENT);
                pixelCanvas.endBatch();
                refreshUI();
            }
            case FILL -> {
                pixelCanvas.floodFill(x, y, currentColor);
                refreshUI();
            }
            case PICKER -> {
                Color picked = pixelCanvas.getColor(x, y);
                if (picked != null) setCurrentColor(picked);
            }
            default -> {}
        }

        updateCursorPosition(x, y);
    }

    @FXML
    private void handleCanvasMouseDragged(MouseEvent event) {
        if (!isDragging) return;

        int x = (int) (event.getX() / zoom);
        int y = (int) (event.getY() / zoom);

        if (x < 0 || x >= pixelCanvas.getWidth() || y < 0 || y >= pixelCanvas.getHeight()) {
            return;
        }

        switch (currentTool) {
            case BRUSH -> {
                pixelCanvas.beginBatch();
                pixelCanvas.drawLine(dragStartX, dragStartY, x, y, currentColor);
                pixelCanvas.endBatch();
                dragStartX = x;
                dragStartY = y;
                refreshUI();
            }
            case ERASER -> {
                pixelCanvas.beginBatch();
                pixelCanvas.drawLine(dragStartX, dragStartY, x, y, Color.TRANSPARENT);
                pixelCanvas.endBatch();
                dragStartX = x;
                dragStartY = y;
                refreshUI();
            }
            default -> {}
        }

        updateCursorPosition(x, y);
    }

    @FXML
    private void handleCanvasMouseReleased(MouseEvent event) {
        if (!isDragging) return;

        int x = (int) (event.getX() / zoom);
        int y = (int) (event.getY() / zoom);

        if (x >= 0 && x < pixelCanvas.getWidth() && y >= 0 && y < pixelCanvas.getHeight()) {
            switch (currentTool) {
                case RECTANGLE -> {
                    pixelCanvas.drawRectangle(dragStartX, dragStartY, x, y, false, currentColor);
                    refreshUI();
                }
                case CIRCLE -> {
                    pixelCanvas.drawCircle(dragStartX, dragStartY, x, y, false, currentColor);
                    refreshUI();
                }
                case LINE -> {
                    pixelCanvas.drawLine(dragStartX, dragStartY, x, y, currentColor);
                    refreshUI();
                }
                case TEXT -> showTextInputDialog(x, y);
                default -> {}
            }
        }

        isDragging = false;
        updateCursorPosition(x, y);
    }

    @FXML
    private void handleCanvasMouseMoved(MouseEvent event) {
        int x = (int) (event.getX() / zoom);
        int y = (int) (event.getY() / zoom);
        updateCursorPosition(x, y);
    }

    // ==================== TOOL SELECTION ====================
    private void selectTool(Tool tool) {
        this.currentTool = tool;
        pixelCanvas.setCurrentTool(tool);
        updateStatusBar();

        if (brushMenuItem != null) brushMenuItem.setSelected(tool == Tool.BRUSH);
        if (eraserMenuItem != null) eraserMenuItem.setSelected(tool == Tool.ERASER);
        if (rectMenuItem != null) rectMenuItem.setSelected(tool == Tool.RECTANGLE);
        if (circleMenuItem != null) circleMenuItem.setSelected(tool == Tool.CIRCLE);
        if (lineMenuItem != null) lineMenuItem.setSelected(tool == Tool.LINE);
        if (textMenuItem != null) textMenuItem.setSelected(tool == Tool.TEXT);
        if (fillMenuItem != null) fillMenuItem.setSelected(tool == Tool.FILL);
        if (pickerMenuItem != null) pickerMenuItem.setSelected(tool == Tool.PICKER);
    }

    @FXML private void selectBrush() { selectTool(Tool.BRUSH); }
    @FXML private void selectEraser() { selectTool(Tool.ERASER); }
    @FXML private void selectRectangle() { selectTool(Tool.RECTANGLE); }
    @FXML private void selectCircle() { selectTool(Tool.CIRCLE); }
    @FXML private void selectLine() { selectTool(Tool.LINE); }
    @FXML private void selectText() { selectTool(Tool.TEXT); }
    @FXML private void selectFill() { selectTool(Tool.FILL); }
    @FXML private void selectPicker() { selectTool(Tool.PICKER); }

    // ==================== COLOR MANAGEMENT ====================
    public void setCurrentColor(Color color) {
        this.currentColor = color;
        pixelCanvas.setCurrentColor(color);
        addToRecentColors(color);
        updateStatusBar();
        updateColorSwatchSelection();
    }

    // ==================== VIEW CONTROLS ====================
    @FXML private void zoomIn() { if (zoom < 5.0) { zoom *= 1.2; applyZoom(); } }
    @FXML private void zoomOut() { if (zoom > 0.2) { zoom /= 1.2; applyZoom(); } }
    @FXML private void resetZoom() { zoom = 1.0; applyZoom(); }

    private void applyZoom() {
        mainCanvas.setScaleX(zoom);
        mainCanvas.setScaleY(zoom);
        drawGrid();
        drawRulers();
        updateStatusBar();
    }

    // ==================== FILE OPERATIONS ====================
    @FXML
    private void handleNew() {
        Dialog<int[]> dialog = createNewCanvasDialog();
        dialog.showAndWait().ifPresent(dim -> {
            int newWidth = dim[0];
            int newHeight = dim[1];
            pixelCanvas.resize(newWidth, newHeight);
            currentFile = new ParametFile(newWidth, newHeight);
            currentFilePath = null;
            refreshUI();
        });
    }

    private Dialog<int[]> createNewCanvasDialog() {
        Dialog<int[]> dialog = new Dialog<>();
        dialog.setTitle("New Canvas");
        dialog.setHeaderText("Enter canvas size:");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField widthField = new TextField("1260");
        TextField heightField = new TextField("630");

        grid.add(new Label("Width:"), 0, 0);
        grid.add(widthField, 1, 0);
        grid.add(new Label("Height:"), 0, 1);
        grid.add(heightField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    int w = Integer.parseInt(widthField.getText());
                    int h = Integer.parseInt(heightField.getText());
                    if (w > 0 && h > 0 && w <= 5000 && h <= 5000) {
                        return new int[]{w, h};
                    } else {
                        showAlert("Dimensions must be between 1 and 5000", Alert.AlertType.ERROR);
                    }
                } catch (NumberFormatException e) {
                    showAlert("Invalid number format", Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        return dialog;
    }

    @FXML
    private void handleResize() {
        Dialog<int[]> dialog = createResizeDialog();
        dialog.showAndWait().ifPresent(dim -> {
            pixelCanvas.resize(dim[0], dim[1]);
            currentFile = new ParametFile(dim[0], dim[1]);
            refreshUI();
        });
    }

    private Dialog<int[]> createResizeDialog() {
        Dialog<int[]> dialog = new Dialog<>();
        dialog.setTitle("Resize Canvas");
        dialog.setHeaderText("Enter new dimensions:");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField widthField = new TextField(String.valueOf(pixelCanvas.getWidth()));
        TextField heightField = new TextField(String.valueOf(pixelCanvas.getHeight()));

        grid.add(new Label("Width:"), 0, 0);
        grid.add(widthField, 1, 0);
        grid.add(new Label("Height:"), 0, 1);
        grid.add(heightField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    int w = Integer.parseInt(widthField.getText());
                    int h = Integer.parseInt(heightField.getText());
                    if (w > 0 && h > 0 && w <= 5000 && h <= 5000) {
                        return new int[]{w, h};
                    } else {
                        showAlert("Dimensions must be between 1 and 5000", Alert.AlertType.ERROR);
                    }
                } catch (NumberFormatException e) {
                    showAlert("Invalid number format", Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        return dialog;
    }

    @FXML
    private void handleOpen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open P100FX File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("P100FX Files", "*.lam"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Window window = getWindow();
        if (window == null) return;

        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            try {
                ParametFile loaded = ParametFile.loadFromFile(file.getAbsolutePath());
                currentFile = loaded;
                currentFilePath = file.getAbsolutePath();

                int[] pixels = loaded.getPixels();
                if (pixels.length == pixelCanvas.getWidth() * pixelCanvas.getHeight()) {
                    pixelCanvas.setPixelArray(pixels);
                } else {
                    pixelCanvas.resize(loaded.getWidth(), loaded.getHeight());
                    pixelCanvas.setPixelArray(pixels);
                }

                refreshUI();
                showAlert("File loaded successfully!", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error loading file: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSave() {
        if (currentFilePath == null) {
            handleSaveAs();
            return;
        }

        try {
            int width = pixelCanvas.getWidth();
            int height = pixelCanvas.getHeight();
            int[] src = pixelCanvas.getPixelArray();

            if (currentFile == null ||
                    currentFile.getWidth() != width ||
                    currentFile.getHeight() != height) {
                currentFile = new ParametFile(width, height);
            }

            int[] dest = currentFile.getPixelsRef();
            int copyLength = Math.min(src.length, dest.length);
            System.arraycopy(src, 0, dest, 0, copyLength);

            if (src.length < dest.length) {
                for (int i = src.length; i < dest.length; i++) {
                    dest[i] = 0x00000000;
                }
            }

            currentFile.saveToFile(currentFilePath);
            showAlert("File saved successfully!", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error saving file: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save P100FX File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("P100FX Files", "*.lam")
        );

        Window window = getWindow();
        if (window == null) return;

        File file = fileChooser.showSaveDialog(window);
        if (file != null) {
            currentFilePath = file.getAbsolutePath();
            handleSave();
        }
    }

    @FXML
    private void handleExportPNG() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export as PNG");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Images", "*.png")
        );

        Window window = getWindow();
        if (window == null) return;

        File file = fileChooser.showSaveDialog(window);
        if (file != null) {
            try {
                int width = pixelCanvas.getWidth();
                int height = pixelCanvas.getHeight();
                int[] pixelArray = pixelCanvas.getPixelArray();

                BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int index = y * width + x;
                        img.setRGB(x, y, pixelArray[index]);
                    }
                }

                ImageIO.write(img, "png", file);
                showAlert("Exported to: " + file.getName(), Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error exporting: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleExit() {
        if (primaryStage != null) primaryStage.close();
    }

    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About P100FX");
        alert.setHeaderText("P100FX - Pixel Art Editor");
        alert.setContentText("Version 1.0\n\nA powerful pixel art editor with undo/redo support,\n"
                + "multiple tools, and export capabilities.\n\n"
                + "© 2024 P100FX Team");
        alert.showAndWait();
    }

    // ==================== GRID METHODS ====================
    private void drawGrid() {
        if (gridCanvas == null) return;

        GraphicsContext gc = gridCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());

        if (showPixelGrid) drawPixelGrid(gc);
        if (showGrid) drawMajorGrid(gc);
    }

    private void drawPixelGrid(GraphicsContext gc) {
        gc.setStroke(pixelGridColor);
        gc.setLineWidth(0.5);

        double scaledWidth = pixelCanvas.getWidth() * zoom;
        double scaledHeight = pixelCanvas.getHeight() * zoom;

        for (double x = 0; x <= scaledWidth; x += zoom) {
            gc.strokeLine(x, 0, x, scaledHeight);
        }

        for (double y = 0; y <= scaledHeight; y += zoom) {
            gc.strokeLine(0, y, scaledWidth, y);
        }
    }

    private void drawMajorGrid(GraphicsContext gc) {
        gc.setStroke(gridColor);
        gc.setLineWidth(1.0);

        double gridStep = gridSize * zoom;

        for (double x = 0; x <= pixelCanvas.getWidth() * zoom; x += gridStep) {
            gc.strokeLine(x, 0, x, pixelCanvas.getHeight() * zoom);
        }

        for (double y = 0; y <= pixelCanvas.getHeight() * zoom; y += gridStep) {
            gc.strokeLine(0, y, pixelCanvas.getWidth() * zoom, y);
        }
    }

    @FXML
    private void showGridSettings() {
        Dialog<GridSettings> dialog = new Dialog<>();
        dialog.setTitle("Grid Settings");
        dialog.setHeaderText("Configure Grid Display");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField gridSizeField = new TextField(String.valueOf(gridSize));
        grid.add(new Label("Grid Size (pixels):"), 0, 0);
        grid.add(gridSizeField, 1, 0);

        ColorPicker gridColorPicker = new ColorPicker(gridColor);
        grid.add(new Label("Grid Color:"), 0, 1);
        grid.add(gridColorPicker, 1, 1);

        ColorPicker pixelGridColorPicker = new ColorPicker(pixelGridColor);
        grid.add(new Label("Pixel Grid Color:"), 0, 2);
        grid.add(pixelGridColorPicker, 1, 2);

        Slider opacitySlider = new Slider(0, 1, pixelGridColor.getOpacity());
        opacitySlider.setShowTickLabels(true);
        opacitySlider.setShowTickMarks(true);
        grid.add(new Label("Pixel Grid Opacity:"), 0, 3);
        grid.add(opacitySlider, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    int newSize = Integer.parseInt(gridSizeField.getText());
                    Color newGridColor = gridColorPicker.getValue();
                    Color newPixelColor = pixelGridColorPicker.getValue();
                    double opacity = opacitySlider.getValue();

                    Color newPixelColorWithOpacity = Color.rgb(
                            (int)(newPixelColor.getRed() * 255),
                            (int)(newPixelColor.getGreen() * 255),
                            (int)(newPixelColor.getBlue() * 255),
                            opacity
                    );

                    return new GridSettings(newSize, newGridColor, newPixelColorWithOpacity);
                } catch (NumberFormatException e) {
                    showAlert("Invalid grid size", Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(settings -> {
            this.gridSize = settings.gridSize;
            this.gridColor = settings.gridColor;
            this.pixelGridColor = settings.pixelGridColor;
            drawGrid();
            showAlert("Grid settings updated!", Alert.AlertType.INFORMATION);
        });
    }

    // ==================== RULERS METHODS ====================
    private void drawRulers() {
        if (rulersCanvas == null) return;

        GraphicsContext gc = rulersCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, rulersCanvas.getWidth(), rulersCanvas.getHeight());

        if (!showRulers) return;

        gc.setFill(Color.rgb(240, 240, 240));
        gc.fillRect(0, 0, rulersCanvas.getWidth(), rulersCanvas.getHeight());
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(0.5);
        gc.strokeRect(0, 0, rulersCanvas.getWidth(), rulersCanvas.getHeight());

        drawHorizontalRuler(gc);
        drawVerticalRuler(gc);

        gc.setFill(Color.rgb(220, 220, 220));
        gc.fillRect(0, 0, RULER_SIZE, RULER_SIZE);
    }

    private void drawHorizontalRuler(GraphicsContext gc) {
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(0.5);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(9));

        double maxWidth = rulersCanvas.getWidth() - RULER_SIZE;
        double step = Math.max(5, Math.min(50, 10 * zoom));

        for (double x = 0; x <= maxWidth; x += step) {
            double posX = RULER_SIZE + x;
            if (posX > rulersCanvas.getWidth()) break;

            int value = (int) (x / zoom);

            if (value % 50 == 0) {
                gc.setLineWidth(1);
                gc.strokeLine(posX, 5, posX, 15);
                gc.fillText(String.valueOf(value), posX - 5, 18);
            } else if (value % 10 == 0) {
                gc.setLineWidth(0.5);
                gc.strokeLine(posX, 8, posX, 15);
            }
        }
    }

    private void drawVerticalRuler(GraphicsContext gc) {
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(0.5);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(9));

        double maxHeight = rulersCanvas.getHeight() - RULER_SIZE;
        double step = Math.max(5, Math.min(50, 10 * zoom));

        for (double y = 0; y <= maxHeight; y += step) {
            double posY = RULER_SIZE + y;
            if (posY > rulersCanvas.getHeight()) break;

            int value = (int) (y / zoom);

            if (value % 50 == 0) {
                gc.setLineWidth(1);
                gc.strokeLine(5, posY, 15, posY);
                gc.save();
                gc.translate(8, posY - 3);
                gc.fillText(String.valueOf(value), 0, 0);
                gc.restore();
            } else if (value % 10 == 0) {
                gc.setLineWidth(0.5);
                gc.strokeLine(8, posY, 15, posY);
            }
        }
    }

    private void adjustCanvasBounds() {
        if (mainCanvas == null) return;
        if (showRulers) {
            mainCanvas.setLayoutX(RULER_SIZE);
            mainCanvas.setLayoutY(RULER_SIZE);
        } else {
            mainCanvas.setLayoutX(0);
            mainCanvas.setLayoutY(0);
        }
    }

    // ==================== EDIT OPERATIONS ====================
    @FXML
    private void handleUndo() {
        if (pixelCanvas != null) {
            pixelCanvas.undo();
            refreshUI();
        }
    }

    @FXML
    private void handleRedo() {
        if (pixelCanvas != null) {
            pixelCanvas.redo();
            refreshUI();
        }
    }

    @FXML
    private void handleClear() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear Canvas");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This will clear the entire canvas. This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                pixelCanvas.clear();
                refreshUI();
            }
        });
    }

    // ==================== VIEW MENU HANDLERS ====================
    @FXML
    private void toggleGrid() {
        if (showGridMenuItem != null) {
            showGrid = showGridMenuItem.isSelected();
            drawGrid();
        }
    }

    @FXML
    private void togglePixelGrid() {
        if (showPixelGridMenuItem != null) {
            showPixelGrid = showPixelGridMenuItem.isSelected();
            drawGrid();
        }
    }

    @FXML
    private void toggleRulers() {
        if (showRulersMenuItem != null) {
            showRulers = showRulersMenuItem.isSelected();
            drawRulers();
            adjustCanvasBounds();
        }
    }

    // ==================== TEXT TOOL ====================
    private void showTextInputDialog(int x, int y) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Text");
        dialog.setHeaderText("Enter text to draw:");
        dialog.setContentText("Text:");

        dialog.showAndWait().ifPresent(text -> {
            if (!text.isEmpty()) {
                pixelCanvas.drawText(x, y, text);
                refreshUI();
            }
        });
    }

    // ==================== STATUS BAR ====================
    private void updateCursorPosition(int x, int y) {
        if (cursorPositionLabel != null) {
            cursorPositionLabel.setText(String.format("📍 X: %d, Y: %d", x, y));
        }
        if (statusBar != null) {
            statusBar.setText(String.format("Position: (%d, %d) | Zoom: %.0f%% | Tool: %s | Color: #%02X%02X%02X",
                    x, y, zoom * 100, currentTool.getName(),
                    (int)(currentColor.getRed() * 255),
                    (int)(currentColor.getGreen() * 255),
                    (int)(currentColor.getBlue() * 255)));
        }
    }

    private void updateStatusBar() {
        if (zoomLabel != null) zoomLabel.setText(String.format("🔍 %.0f%%", zoom * 100));
        if (toolLabel != null) toolLabel.setText("🛠️ " + currentTool.getName());
        if (colorLabel != null) {
            colorLabel.setText(String.format("🎨 #%02X%02X%02X",
                    (int)(currentColor.getRed() * 255),
                    (int)(currentColor.getGreen() * 255),
                    (int)(currentColor.getBlue() * 255)));
        }
    }

    // ==================== UTILITIES ====================
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ==================== SETTERS ====================
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setBrushSize(int size) {
        this.brushSize = size;
        if (pixelCanvas != null) pixelCanvas.setBrushSize(size);
        if (brushSizeSlider != null) brushSizeSlider.setValue(size);
        if (brushSizeLabel != null) brushSizeLabel.setText(size + "px");
    }

    // ==================== INNER CLASSES ====================
    private static class GridSettings {
        final int gridSize;
        final Color gridColor;
        final Color pixelGridColor;

        GridSettings(int gridSize, Color gridColor, Color pixelGridColor) {
            this.gridSize = gridSize;
            this.gridColor = gridColor;
            this.pixelGridColor = pixelGridColor;
        }
    }
}
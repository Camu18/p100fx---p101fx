package Lam.camu.p100fx.view.components;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class StatusBar extends HBox {

    // Left section
    private Label interfaceLabel;
    private Label blockLabel;

    // Center left
    private Label cursorLabel;
    private Label positionLabel;
    private Label toolLabel;
    private Label colorLabel;

    // Center
    private Label sizeLabel;

    // Center right
    private Label modeLabel;

    // Right section
    private Label memoryLabel;
    private Label zoomLabel;
    private Label timeLabel;
    private Label messageLabel;

    // Separators
    private Separator sep1, sep2, sep3, sep4, sep5;

    public StatusBar() {
        initialize();
        setupLayout();
        startTimeUpdater();
    }

    private void initialize() {
        // Left section
        interfaceLabel = createLabel("No file", "interface", "#88ccff");
        blockLabel = createLabel("Block: 0", "block", "#ffaa88");

        // Center left
        cursorLabel = createLabel("X:0 Y:0", "cursor", "#00ff00");
        positionLabel = createLabel("Row:0 Col:0", "position", "#ffff00");

        // Center
        sizeLabel = createLabel("0x0", "size", "#88ff88");

        // Center right
        modeLabel = createLabel("SAVED", "mode", "#00ff00");

        // Right section
        memoryLabel = createLabel("0/0 MB", "memory", "#8888ff");
        zoomLabel = createLabel("100%", "zoom", "#cccccc");
        timeLabel = createLabel("", "time", "#aaaaaa");
        messageLabel = createLabel("Ready", "message", "#ffffff");

        // Separators
        sep1 = createSeparator();
        sep2 = createSeparator();
        sep3 = createSeparator();
        sep4 = createSeparator();
        sep5 = createSeparator();

        // Tooltips
        Tooltip.install(interfaceLabel, new Tooltip("Current interface"));
        Tooltip.install(cursorLabel, new Tooltip("Cursor position on screen"));
        Tooltip.install(positionLabel, new Tooltip("Position in file"));
        Tooltip.install(sizeLabel, new Tooltip("File size (width x height)"));
        Tooltip.install(modeLabel, new Tooltip("Save status"));
        Tooltip.install(memoryLabel, new Tooltip("Memory usage"));
        Tooltip.install(zoomLabel, new Tooltip("Zoom level"));
    }
    public void setTool(String tool) {
        toolLabel.setText("Tool: " + tool);
    }

    public void setColor(String color) {
        colorLabel.setText("Color: " + color);
    }

    private void setupLayout() {
        setSpacing(10);
        setPadding(new Insets(3, 10, 3, 10));
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-background-color: #2d2d2d; -fx-border-color: #404040; -fx-border-width: 1 0 0 0;");

        // Left section
        getChildren().addAll(
                createIconLabel("📁"), interfaceLabel,
                sep1,
                createIconLabel("📦"), blockLabel,
                sep2
        );

        // Center left
        getChildren().addAll(
                createIconLabel("📍"), cursorLabel,
                sep3,
                createIconLabel("📏"), positionLabel,
                sep4
        );

        // Center
        getChildren().addAll(
                createIconLabel("📐"), sizeLabel,
                sep5
        );

        // Center right
        getChildren().add(modeLabel);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);

        // Right section
        getChildren().addAll(
                createIconLabel("💾"), memoryLabel,
                createIconLabel("🔍"), zoomLabel,
                createIconLabel("🕒"), timeLabel,
                createIconLabel("💬"), messageLabel
        );
    }

    private Label createLabel(String text, String styleClass, String color) {
        Label label = new Label(text);
        label.getStyleClass().add("status-" + styleClass);
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11px; -fx-font-family: 'Monospaced';");
        return label;
    }

    private Label createIconLabel(String icon) {
        Label label = new Label(icon);
        label.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");
        return label;
    }

    private Separator createSeparator() {
        Separator sep = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep.setStyle("-fx-background-color: #404040;");
        return sep;
    }

    private void startTimeUpdater() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(Duration.seconds(1), e -> {
                    java.time.LocalTime now = java.time.LocalTime.now();
                    timeLabel.setText(String.format("%02d:%02d:%02d",
                            now.getHour(), now.getMinute(), now.getSecond()));
                })
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
    }

    // ==================== PUBLIC METHODS ====================

    // Interface info
    public void setInterface(String name) {
        interfaceLabel.setText(name + ".param");
    }
    public void setBlock(int id, String name) {
        blockLabel.setText(String.format("Block %d: %s", id, name));
    }

    // Position
    public void setCursorPosition(int x, int y) {
        cursorLabel.setText(String.format("X:%d Y:%d", x, y));
    }

    public void setFilePosition(int row, int col) {
        positionLabel.setText(String.format("Row:%d Col:%d", row, col));
    }

    // Size
    public void setSize(int width, int height) {
        sizeLabel.setText(String.format("%dx%d", width, height));

        // Color code based on size
        if (height > 10000) {
            sizeLabel.setStyle("-fx-text-fill: #ff8800; -fx-font-size: 11px; -fx-font-family: 'Monospaced';");
        } else if (height > 5000) {
            sizeLabel.setStyle("-fx-text-fill: #ffff00; -fx-font-size: 11px; -fx-font-family: 'Monospaced';");
        } else {
            sizeLabel.setStyle("-fx-text-fill: #88ff88; -fx-font-size: 11px; -fx-font-family: 'Monospaced';");
        }
    }

    // Mode
    public void setModified(boolean modified) {
        if (modified) {
            modeLabel.setText("MODIFIED");
            modeLabel.setStyle("-fx-text-fill: #ff8800; -fx-font-weight: bold; -fx-font-size: 11px;");
        } else {
            modeLabel.setText("SAVED");
            modeLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold; -fx-font-size: 11px;");
        }
    }

    // Memory
    public void updateMemory() {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        long max = runtime.maxMemory();

        double usedMB = used / (1024.0 * 1024.0);
        double maxMB = max / (1024.0 * 1024.0);

        memoryLabel.setText(String.format("%.0f/%.0f MB", usedMB, maxMB));

        // Warning when > 80%
        if (usedMB > maxMB * 0.8) {
            memoryLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 11px; -fx-font-family: 'Monospaced';");
        } else if (usedMB > maxMB * 0.6) {
            memoryLabel.setStyle("-fx-text-fill: #ff8800; -fx-font-size: 11px; -fx-font-family: 'Monospaced';");
        } else {
            memoryLabel.setStyle("-fx-text-fill: #8888ff; -fx-font-size: 11px; -fx-font-family: 'Monospaced';");
        }
    }

    // Zoom
    public void setZoom(double zoom) {
        zoomLabel.setText(String.format("%.0f%%", zoom * 100));
    }

    // Message
    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    public void showTemporaryMessage(String message, int seconds) {
        String original = messageLabel.getText();
        messageLabel.setText(message);

        PauseTransition pause = new PauseTransition(Duration.seconds(seconds));
        pause.setOnFinished(e -> messageLabel.setText(original));
        pause.play();
    }

    // Progress (có thể thêm thanh progress sau)
    public void showProgress(String task, double progress) {
        if (progress >= 1.0) {
            setMessage(task + " complete!");
        } else {
            setMessage(String.format("%s %d%%", task, (int)(progress * 100)));
        }
    }

    // Reset
    public void reset() {
        setInterface("No file");
        setBlock(0, "None");
        setCursorPosition(0, 0);
        setFilePosition(0, 0);
        setSize(0, 0);
        setModified(false);
        setZoom(1.0);
        setMessage("Ready");
    }
}

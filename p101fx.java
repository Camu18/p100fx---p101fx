package lam.p101fx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.geometry.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class p101fx extends Application {
    private final int COLS = 8;
    private final int ROWS = 16; // 8x16
    private final Rectangle[][] grid = new Rectangle[ROWS][COLS];
    private final Color PICKED_COLOR = Color.YELLOW;
    private final Color EMPTY_COLOR = Color.web("#222222");
    private final int TOP_END = 3;
    private final int BODY_START = 4;
    private final int BODY_END = 13;
    private final int BOTTOM_START = 14;

    // Vùng màu cho các khu vực
    private final Color TOP_ACCENT_BG = Color.web("#331100");
    private final Color BODY_BG = Color.web("#222222");
    private final Color BOTTOM_ACCENT_BG = Color.web("#003333");

    private final int CELL_SIZE = 28;
    private final int CELL_PADDING = 1;

    private Map<Character, byte[]> fontMap = new HashMap<>();
    private char currentChar = 'A';

    // Components
    private ComboBox<Character> charSelector = new ComboBox<>();
    private Label statusLabel = new Label("Sẵn sàng");
    private TextArea txtResult;

    @Override
    public void start(Stage stage) {
        // BorderPane là bố cục chính
        BorderPane mainPane = new BorderPane();
        mainPane.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 10;");

        // ===== LEFT PANEL - Grid vẽ =====
        VBox leftPanel = new VBox(10);
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setStyle("-fx-padding: 10; -fx-background-color: #2a2a2a; -fx-background-radius: 5;");
        leftPanel.setPrefWidth(350);

        // Title cho grid
        Label gridTitle = new Label("PIXEL GRID 8x16");
        gridTitle.setStyle("-fx-text-fill: #00FF00; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Tạo grid với phân vùng rõ ràng
        GridPane gridPane = createGridWithZones();

        // Chú thích vùng
        HBox zoneLegend = new HBox(15);
        zoneLegend.setAlignment(Pos.CENTER);
        zoneLegend.setStyle("-fx-padding: 5;");

        Label topZone = new Label("■ Dấu trên (0-3)");
        topZone.setStyle("-fx-text-fill: #FF6600; -fx-font-size: 10px;");

        Label bodyZone = new Label("■ Thân chữ (4-13)");
        bodyZone.setStyle("-fx-text-fill: #00CC00; -fx-font-size: 10px;");

        Label bottomZone = new Label("■ Dấu dưới (14-15)");
        bottomZone.setStyle("-fx-text-fill: #00FFFF; -fx-font-size: 10px;");

        zoneLegend.getChildren().addAll(topZone, bodyZone, bottomZone);

        leftPanel.getChildren().addAll(gridTitle, gridPane, zoneLegend);

        // ===== RIGHT PANEL - Controls =====
        VBox rightPanel = new VBox(15);
        rightPanel.setAlignment(Pos.TOP_CENTER);
        rightPanel.setStyle("-fx-padding: 10; -fx-background-color: #2a2a2a; -fx-background-radius: 5;");
        rightPanel.setPrefWidth(380);

        // Header
        Label controlTitle = new Label("BẢNG ĐIỀU KHIỂN");
        controlTitle.setStyle("-fx-text-fill: #00FF00; -fx-font-size: 14px; -fx-font-weight: bold;");

        // ===== GROUP 1: Character Selection =====
        TitledPane charGroup = new TitledPane();
        charGroup.setText("KÝ TỰ");
        charGroup.setContent(createCharControl());
        charGroup.setCollapsible(false);
        charGroup.setStyle("-fx-text-fill: white; -fx-background-color: #333333; -fx-font-size: 11px;");

        // ===== GROUP 2: File Management =====
        TitledPane fileGroup = new TitledPane();
        fileGroup.setText("QUẢN LÝ FILE");
        fileGroup.setContent(createFileControl());
        fileGroup.setCollapsible(false);
        fileGroup.setStyle("-fx-text-fill: white; -fx-background-color: #333333; -fx-font-size: 13px;");

        // ===== GROUP 3: Export & Tools =====
        TitledPane exportGroup = new TitledPane();
        exportGroup.setText("XUẤT DỮ LIỆU");
        exportGroup.setContent(createExportControl());
        exportGroup.setCollapsible(false);
        exportGroup.setStyle("-fx-text-fill: white; -fx-background-color: #333333; -fx-font-size: 13px;");

        // ===== GROUP 4: Preview =====
        TitledPane previewGroup = new TitledPane();
        previewGroup.setText("PREVIEW");
        previewGroup.setContent(createPreviewControl());
        previewGroup.setCollapsible(false);
        previewGroup.setStyle("-fx-text-fill: white; -fx-background-color: #333333; -fx-font-size: 13px;");

        rightPanel.getChildren().addAll(controlTitle, charGroup, fileGroup, exportGroup, previewGroup);

        // Thêm vào BorderPane
        mainPane.setLeft(leftPanel);
        mainPane.setRight(rightPanel);
        mainPane.setBottom(createStatusBar());

        // Load dữ liệu
        initSampleData();
        updateCharSelector();

        if (!loadFromBinary()) {
            if (!fontMap.isEmpty()) {
                currentChar = fontMap.keySet().iterator().next();
                charSelector.setValue(currentChar);
                loadCharacter(currentChar);
            }
        }

        // Scene
        Scene scene = new Scene(mainPane, 950, 700);
        stage.setTitle("Font Editor 8x16 - Side by Side Layout");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.show();

        updateStatus("Sẵn sàng - Bố cục 2 cột");
    }

    private GridPane createGridWithZones() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(CELL_PADDING);
        gridPane.setVgap(CELL_PADDING);
        gridPane.setStyle("-fx-background-color: #444444; -fx-padding: 10; -fx-background-radius: 5;");
        gridPane.setAlignment(Pos.CENTER);

        // Tạo các ô
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);

                // Màu nền theo vùng
                if (r <= TOP_END) {
                    rect.setFill(TOP_ACCENT_BG);
                    rect.setStroke(Color.web("#FF6600"));
                } else if (r >= BODY_START && r <= BODY_END) {
                    rect.setFill(BODY_BG);
                    rect.setStroke(Color.web("#00CC00"));
                } else {
                    rect.setFill(BOTTOM_ACCENT_BG);
                    rect.setStroke(Color.web("#00FFFF"));
                }

                rect.setStrokeWidth(0.5);
                rect.setArcWidth(3);
                rect.setArcHeight(3);

                final int row = r, col = c;
                rect.setOnMouseClicked(e -> {
                    if (rect.getFill().equals(PICKED_COLOR)) {
                        // Trả về màu gốc theo vùng
                        if (row <= TOP_END) {
                            rect.setFill(TOP_ACCENT_BG);
                        } else if (row >= BODY_START && row <= BODY_END) {
                            rect.setFill(BODY_BG);
                        } else {
                            rect.setFill(BOTTOM_ACCENT_BG);
                        }
                    } else {
                        rect.setFill(PICKED_COLOR);
                    }
                });

                grid[row][c] = rect;
                gridPane.add(rect, c, r);
            }
        }

        // Thêm đường phân cách ngang
        addSeparatorLines(gridPane);

        // Thêm số thứ tự cột
        for (int c = 0; c < COLS; c++) {
            Label colLabel = new Label(String.valueOf(c));
            colLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 9px;");
            gridPane.add(colLabel, c, ROWS);
        }

        // Thêm số thứ tự hàng
        for (int r = 0; r < ROWS; r++) {
            Label rowLabel = new Label(String.valueOf(r));
            rowLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 9px;");
            gridPane.add(rowLabel, COLS, r);
        }

        return gridPane;
    }

    private void addSeparatorLines(GridPane gridPane) {
        // Đường phân cách giữa vùng dấu trên và thân chữ
        Line sep1 = new Line(0, 0, COLS * (CELL_SIZE + CELL_PADDING), 0);
        sep1.setStroke(Color.web("#FF6600"));
        sep1.setStrokeWidth(2);
        GridPane.setConstraints(sep1, 0, 4, COLS, 1);
        GridPane.setMargin(sep1, new Insets(-CELL_SIZE/2, 0, 0, 0));
        gridPane.getChildren().add(sep1);
    // ----------------------------------------
        // Đường phân cách chữ trên và chữ có phần ở dưới
        Line sep2 = new Line(0, 0, COLS * (CELL_SIZE + CELL_PADDING), 0);
        sep2.setStroke(Color.CYAN);
        sep2.setStrokeWidth(2);
        GridPane.setConstraints(sep2, 0, 12, COLS, 1);
        GridPane.setMargin(sep2, new Insets(-CELL_SIZE/2, 0, 0, 0));
        gridPane.getChildren().add(sep2);
   // -----------------------------------------
        // Đường phân cách giữa thân chữ và dấu dưới
        Line sep3 = new Line(0, 0, COLS * (CELL_SIZE + CELL_PADDING), 0);
        sep3.setStroke(Color.CYAN);
        sep3.setStrokeWidth(2);
        GridPane.setConstraints(sep3, 0, 14, COLS, 1);
        GridPane.setMargin(sep3, new Insets(-CELL_SIZE/2, 0, 0, 0));
        gridPane.getChildren().add(sep3);
    }

    private VBox createCharControl() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(5));

        // Hàng chọn ký tự
        HBox charRow = new HBox(5);
        charRow.setAlignment(Pos.CENTER_LEFT);

        Label lblChar = new Label("Ký tự:");
        lblChar.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

        charSelector.setPrefWidth(60);
        charSelector.setStyle("-fx-font-size: 13px;");

        Button btnPrev = new Button("◀");
        btnPrev.setStyle("-fx-font-size: 13px;");
        btnPrev.setOnAction(e -> navigateChar(-1));

        Button btnNext = new Button("▶");
        btnNext.setStyle("-fx-font-size: 13px;");
        btnNext.setOnAction(e -> navigateChar(1));

        charRow.getChildren().addAll(lblChar, charSelector, btnPrev, btnNext);

        // Menu dấu nhanh
        HBox accentRow = new HBox(5);
        accentRow.setAlignment(Pos.CENTER_LEFT);

        Label lblAccent = new Label("Chèn dấu:");
        lblAccent.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

        Button btnTopAccent = new Button("Dấu trên");
        btnTopAccent.setStyle("-fx-background-color: #FF6600; -fx-text-fill: white; -fx-font-size: 10px;");
        btnTopAccent.setOnAction(e -> showTopAccentMenu());

        Button btnBottomAccent = new Button("Dấu dưới");
        btnBottomAccent.setStyle("-fx-background-color: #00FFFF; -fx-text-fill: black; -fx-font-size: 10px;");
        btnBottomAccent.setOnAction(e -> showBottomAccentMenu());

        accentRow.getChildren().addAll(lblAccent, btnTopAccent, btnBottomAccent);

        box.getChildren().addAll(charRow, accentRow);
        return box;
    }

    private GridPane createFileControl() {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(5));

        Button btnSave = new Button("💾 Lưu ký tự");
        btnSave.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 13px;");
        btnSave.setPrefWidth(120);
        btnSave.setOnAction(e -> saveCurrentChar());

        Button btnSaveAll = new Button("💾 Lưu file");
        btnSaveAll.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 13px;");
        btnSaveAll.setPrefWidth(120);
        btnSaveAll.setOnAction(e -> saveToBinary());

        Button btnLoad = new Button("📂 Tải file");
        btnLoad.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 13px;");
        btnLoad.setPrefWidth(120);
        btnLoad.setOnAction(e -> loadFromBinary());

        Button btnList = new Button("📋 Danh sách");
        btnList.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; -fx-font-size: 13px;");
        btnList.setPrefWidth(120);
        btnList.setOnAction(e -> showCharList());

        grid.add(btnSave, 0, 0);
        grid.add(btnSaveAll, 1, 0);
        grid.add(btnLoad, 0, 1);
        grid.add(btnList, 1, 1);

        return grid;
    }

    private HBox createExportControl() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(5));

        Button btnExport = new Button("XUẤT HEX");
        btnExport.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
        btnExport.setPrefWidth(100);
        btnExport.setOnAction(e -> txtResult.setText(generateHex()));

        Button btnClear = new Button("XÓA");
        btnClear.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 12px;");
        btnClear.setPrefWidth(80);
        btnClear.setOnAction(e -> {
            clearGrid();
            txtResult.clear();
        });

        Button btnTest = new Button("TEST");
        btnTest.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; -fx-font-size: 12px;");
        btnTest.setPrefWidth(80);
        btnTest.setOnAction(e -> testCharacter());

        box.getChildren().addAll(btnExport, btnClear, btnTest);
        return box;
    }

    private VBox createPreviewControl() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(5));

        txtResult = new TextArea();
        txtResult.setPrefHeight(150);
        txtResult.setStyle("-fx-control-inner-background: #000; -fx-text-fill: #0FF; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");

        box.getChildren().add(txtResult);
        return box;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setStyle("-fx-background-color: #333333; -fx-padding: 5; -fx-border-color: #444444;");
        statusBar.setAlignment(Pos.CENTER_LEFT);

        statusLabel.setStyle("-fx-text-fill: #00FF00; -fx-font-size: 11px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label versionLabel = new Label("v2.0 - 8x16");
        versionLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 10px;");

        statusBar.getChildren().addAll(statusLabel, spacer, versionLabel);
        return statusBar;
    }

    private void showTopAccentMenu() {
        ContextMenu menu = new ContextMenu();

        MenuItem acute = new MenuItem("Sắc (´) - Hàng 0");
        acute.setOnAction(e -> drawAccent(0, 0x10));

        MenuItem grave = new MenuItem("Huyền (`) - Hàng 0");
        grave.setOnAction(e -> drawAccent(0, 0x08));

        MenuItem hook = new MenuItem("Hỏi (ˀ) - Hàng 0");
        hook.setOnAction(e -> drawAccent(0, 0x0A));

        MenuItem tilde = new MenuItem("Ngã (~) - Hàng 0");
        tilde.setOnAction(e -> drawAccent(0, 0x12));

        MenuItem circumflex = new MenuItem("Mũ (^) - Hàng 0");
        circumflex.setOnAction(e -> drawAccent(0, 0x08));

        MenuItem breve = new MenuItem("Trăng (˘) - Hàng 0");
        breve.setOnAction(e -> drawAccent(0, 0x14));

        menu.getItems().addAll(acute, grave, hook, tilde, circumflex, breve);
        menu.show(charSelector, Side.BOTTOM, 0, 0);
    }

    private void showBottomAccentMenu() {
        ContextMenu menu = new ContextMenu();

        MenuItem underdot = new MenuItem("Nặng (.) - Hàng 14");
        underdot.setOnAction(e -> drawAccent(14, 0x08));

        MenuItem comma = new MenuItem("Phẩy (,) - Hàng 14");
        comma.setOnAction(e -> drawAccent(14, 0x04));

        MenuItem semicolon = new MenuItem("Chấm phẩy (;) - Hàng 14");
        semicolon.setOnAction(e -> drawAccent(14, 0x0C));

        menu.getItems().addAll(underdot, comma, semicolon);
        menu.show(charSelector, Side.BOTTOM, 0, 0);
    }

    private void drawAccent(int row, int pattern) {
        for (int c = 0; c < COLS; c++) {
            if ((pattern & (1 << (7 - c))) != 0) {
                grid[row][c].setFill(PICKED_COLOR);
            }
        }
    }

    private void initSampleData() {
        // Tạo mẫu chữ 'A' không dấu
        byte[] charA = new byte[ROWS];
        Arrays.fill(charA, (byte)0);
        charA[4] = (byte)0x3C; // 00111100
        charA[5] = (byte)0x42; // 01000010
        charA[6] = (byte)0x42; // 01000010
        charA[7] = (byte)0x7E; // 01111110
        charA[8] = (byte)0x42; // 01000010
        charA[9] = (byte)0x42; // 01000010
        charA[10] = (byte)0x42; // 01000010
        charA[11] = (byte)0x42;
        fontMap.put('A', charA);

        // Chữ 'Â' (có dấu mũ)
        byte[] charACirc = charA.clone();
        charACirc[3] = (byte)0x08;
        fontMap.put('Â', charACirc);

        // Chữ 'Ạ' (có dấu nặng)
        byte[] charADot = charA.clone();
        charADot[14] = (byte)0x08;
        fontMap.put('Ạ', charADot);
    }

    private void navigateChar(int direction) {
        List<Character> chars = new ArrayList<>(fontMap.keySet());
        chars.sort(Character::compareTo);

        if (chars.isEmpty()) return;

        int currentIndex = chars.indexOf(currentChar);
        int newIndex = currentIndex + direction;

        if (newIndex >= 0 && newIndex < chars.size()) {
            currentChar = chars.get(newIndex);
            charSelector.setValue(currentChar);
            loadCharacter(currentChar);
        }
    }

    private void loadCharacter(char c) {
        byte[] data = fontMap.get(c);
        if (data != null) {
            for (int r = 0; r < ROWS; r++) {
                byte row = data[r];
                for (int col = 0; col < COLS; col++) {
                    if ((row & (1 << (7 - col))) != 0) {
                        grid[r][col].setFill(PICKED_COLOR);
                    } else {
                        if (r <= TOP_END) {
                            grid[r][col].setFill(TOP_ACCENT_BG);
                        } else if (r >= BODY_START && r <= BODY_END) {
                            grid[r][col].setFill(BODY_BG);
                        } else {
                            grid[r][col].setFill(BOTTOM_ACCENT_BG);
                        }
                    }
                }
            }
            updateStatus("Đã tải: " + c);
        } else {
            clearGrid();
        }
    }

    private void clearGrid() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (r <= 1) {
                    grid[r][c].setFill(TOP_ACCENT_BG);
                } else if (r >= 4&& r <= 13) {
                    grid[r][c].setFill(BODY_BG);
                } else {
                    grid[r][c].setFill(BOTTOM_ACCENT_BG);
                }
            }
        }
    }

    private byte[] getCurrentGridData() {
        byte[] data = new byte[ROWS];
        for (int r = 0; r < ROWS; r++) {
            byte rowVal = 0;
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c].getFill().equals(PICKED_COLOR)) {
                    rowVal |= (1 << (7 - c));
                }
            }
            data[r] = rowVal;
        }
        return data;
    }

    private void saveCurrentChar() {
        byte[] currentData = getCurrentGridData();
        fontMap.put(currentChar, currentData);
        updateCharSelector();
        updateStatus("Đã lưu: " + currentChar);
    }

    private void saveToBinary() {
        try (DataOutputStream dos = new DataOutputStream(
                new FileOutputStream("font_8x16.lam"))) {

            saveCurrentChar();

            dos.writeInt(fontMap.size());

            for (Map.Entry<Character, byte[]> entry : fontMap.entrySet()) {
                dos.writeChar(entry.getKey());
                byte[] data = entry.getValue();
                dos.write(data, 0, ROWS);
            }

            showAlert("Thành công", "Đã lưu " + fontMap.size() + " ký tự vào font_8x16.lam", Alert.AlertType.INFORMATION);

        } catch (IOException e) {
            showAlert("Lỗi", "Không thể lưu file: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean loadFromBinary() {
        File file = new File("font_8x16.lam");
        if (!file.exists()) return false;

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            fontMap.clear();

            int count = dis.readInt();
            for (int i = 0; i < count; i++) {
                char c = dis.readChar();
                byte[] data = new byte[ROWS];
                dis.readFully(data);
                fontMap.put(c, data);
            }

            updateCharSelector();

            if (!fontMap.isEmpty()) {
                currentChar = fontMap.keySet().iterator().next();
                charSelector.setValue(currentChar);
                loadCharacter(currentChar);
            }

            showAlert("Thành công", "Đã tải " + fontMap.size() + " ký tự từ font_8x16.lam", Alert.AlertType.INFORMATION);
            return true;

        } catch (IOException e) {
            showAlert("Lỗi", "Không thể đọc file: " + e.getMessage(), Alert.AlertType.ERROR);
            return false;
        }
    }

    private void updateCharSelector() {
        charSelector.getItems().clear();
        fontMap.keySet().stream()
                .sorted()
                .forEach(charSelector.getItems()::add);
    }

    private void showCharList() {
        Stage listStage = new Stage();
        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 15; -fx-background-color: #1a1a1a;");

        Label title = new Label("DANH SÁCH FONT (" + fontMap.size() + " ký tự)");
        title.setStyle("-fx-text-fill: #00FF00; -fx-font-size: 14px; -fx-font-weight: bold;");

        ListView<String> listView = new ListView<>();
        listView.setPrefHeight(400);
        listView.setStyle("-fx-control-inner-background: #333; -fx-text-fill: white;");

        List<Character> sortedChars = new ArrayList<>(fontMap.keySet());
        sortedChars.sort(Character::compareTo);

        for (char c : sortedChars) {
            String type = getCharType(c);
            listView.getItems().add(String.format("%c (U+%04X) - %s", c, (int)c, type));
        }

        listView.setOnMouseClicked(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.isEmpty()) {
                currentChar = selected.charAt(0);
                charSelector.setValue(currentChar);
                loadCharacter(currentChar);
                listStage.close();
            }
        });

        Button btnClose = new Button("Đóng");
        btnClose.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        btnClose.setOnAction(e -> listStage.close());

        root.getChildren().addAll(title, listView, btnClose);

        Scene scene = new Scene(root, 350, 500);
        listStage.setTitle("Danh sách font");
        listStage.setScene(scene);
        listStage.show();
    }

    private String getCharType(char c) {
        if ("ÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚÝàáâãèéêìíòóôõùúý".indexOf(c) >= 0) {
            return "Có dấu trên";
        } else if ("ẠẶẬỆỊỌỘỤỰỲỴ".indexOf(c) >= 0) {
            return "Có dấu dưới";
        } else if ("ĂÂĐÊÔƠƯ".indexOf(c) >= 0) {
            return "Có dấu phụ";
        }
        return "Không dấu";
    }

    private void testCharacter() {
        StringBuilder sb = new StringBuilder();
        sb.append("Ký tự: ").append(currentChar).append("\n");
        sb.append("Unicode: U+").append(String.format("%04X", (int)currentChar)).append("\n");
        sb.append("Vùng: ").append(getCharType(currentChar)).append("\n\n");

        byte[] data = fontMap.get(currentChar);
        if (data != null) {
            sb.append("HÀNG 0-1 (Dấu trên):\n");
            for (int r = 0; r <= 3; r++) {
                sb.append(String.format("  %2d: ", r));
                for (int bit = 7; bit >= 0; bit--) {
                    sb.append(((data[r] >> bit) & 1) == 1 ? '█' : '·');
                }
                sb.append("\n");
            }

            sb.append("\nHÀNG 4-13 (Thân chữ):\n");
            for (int r = 4; r <= 13; r++) {
                sb.append(String.format("  %2d: ", r));
                for (int bit = 7; bit >= 0; bit--) {
                    sb.append(((data[r] >> bit) & 1) == 1 ? '█' : '·');
                }
                sb.append("\n");
            }

            sb.append("\nHÀNG 14-15 (Dấu dưới):\n");
            for (int r = BOTTOM_START; r <= 15; r++) {
                sb.append(String.format("  %2d: ", r));
                for (int bit = 7; bit >= 0; bit--) {
                    sb.append(((data[r] >> bit) & 1) == 1 ? '█' : '·');
                }
                sb.append("\n");
            }
        }

        txtResult.setText(sb.toString());
    }

    private String generateHex() {
        byte[] data = getCurrentGridData();
        String hexString = IntStream.range(0, ROWS)
                .mapToObj(i -> String.format("0x%02X", data[i] & 0xFF))
                .collect(Collectors.joining(", "));
        return "// " + currentChar + " (" + getCharType(currentChar) + ")\nbyte charData[] = {" + hexString + "};";
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void updateStatus(String message) {
        statusLabel.setText("▶ " + message);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
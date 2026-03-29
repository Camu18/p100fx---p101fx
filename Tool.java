package Lam.camu.p100fx.model;

public enum Tool {
    BRUSH("Brush", "M12,12L19.07,19.07M19.07,19.07L12,12"),
    RECTANGLE("Rectangle", "M3,3H21V21H3V3Z"),
    CIRCLE("Circle", "M12,2A10,10 0 0,1 22,12A10,10 0 0,1 12,22A10,10 0 0,1 2,12A10,10 0 0,1 12,2Z"),
    LINE("Line", "M3,12H21M12,3V21"),
    TEXT("Text", "M18.5,4L19.66,8.35L18.7,8.61C18.25,7.74 17.79,6.87 17.26,6.43C16.73,6 16.11,6 15.5,6H13V16.5C13,17 13,17.5 13.33,17.75C13.67,18 14.33,18 15,18V19H9V18C9.67,18 10.33,18 10.67,17.75C11,17.5 11,17 11,16.5V6H8.5C7.89,6 7.27,6 6.74,6.43C6.21,6.87 5.75,7.74 5.3,8.61L4.34,8.35L5.5,4H18.5Z"),
    FILL("Fill", "M19,11.5C19,11.5 17,13.67 17,15A2,2 0 0,0 19,17A2,2 0 0,0 21,15C21,13.67 19,11.5 19,11.5M5.21,10L10,5.21L14.79,10M16.56,8.94L7.62,0L6.21,1.41L8.59,3.79L3.44,8.94C2.85,9.5 2.85,10.47 3.44,11.06L8.94,16.56C9.23,16.85 9.62,17 10,17C10.38,17 10.77,16.85 11.06,16.56L16.56,11.06C17.15,10.47 17.15,9.5 16.56,8.94Z"),
    ERASER("Eraser", "Tẩy xóa"),
    PICKER("Color Picker", "Chọn màu"),
    SELECT("Select", "M10,2H14V4H10V2M3,6H7V8H3V6M17,6H21V8H17V6M10,10H14V12H10V10M3,14H7V16H3V14M17,14H21V16H17V14M10,18H14V20H10V18M3,22H7V24H3V22M17,22H21V24H17V22Z");

    private final String displayName;
    private final String description;

    Tool(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    Tool(String displayName) {
        this(displayName, "");
    }

    public String getName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

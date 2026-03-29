package Lam.camu.p100fx.model;

import java.io.RandomAccessFile;
import java.io.IOException;

public class Control {

    // Constants for control types
    public static final String TYPE_BUTTON = "button";
    public static final String TYPE_TEXTFIELD = "textfield";
    public static final String TYPE_CHECKBOX = "checkbox";
    public static final String TYPE_RADIO = "radio";
    public static final String TYPE_LABEL = "label";
    public static final String TYPE_SLIDER = "slider";
    public static final String TYPE_PROGRESS = "progress";

    // Basic properties
    private int id;
    private String type;
    private String name;

    // Position and size
    private int x, y;
    private int width, height;

    // Content
    private String text;
    private String value;
    private boolean selected;

    // State
    private boolean visible;
    private boolean enabled;
    private boolean focused;

    // Appearance
    private String style;
    private int backgroundColor;  // ARGB
    private int textColor;         // ARGB
    private int borderColor;        // ARGB
    private int borderWidth;

    // Events
    private String onClick;
    private String onKeyPress;
    private String onFocus;
    private String onBlur;

    // Constructors
    public Control(int id, String type, String name, int x, int y, int width, int height) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        // Default values
        this.visible = true;
        this.enabled = true;
        this.focused = false;
        this.backgroundColor = 0xFFFFFFFF;  // White
        this.textColor = 0xFF000000;         // Black
        this.borderColor = 0xFF000000;        // Black
        this.borderWidth = 1;
    }
    // Ghi control vào file
    public void writeTo(RandomAccessFile raf) throws IOException {
        raf.writeInt(id);
        raf.writeUTF(type);
        raf.writeUTF(name != null ? name : "");
        raf.writeInt(x);
        raf.writeInt(y);
        raf.writeInt(width);
        raf.writeInt(height);
        raf.writeUTF(text != null ? text : "");
        raf.writeUTF(value != null ? value : "");
        raf.writeBoolean(selected);
        raf.writeBoolean(visible);
        raf.writeBoolean(enabled);
        raf.writeInt(backgroundColor);
        raf.writeInt(textColor);
    }

    // Đọc control từ file
    public static Control readFrom(RandomAccessFile raf) throws IOException {
        int id = raf.readInt();
        String type = raf.readUTF();
        String name = raf.readUTF();
        int x = raf.readInt();
        int y = raf.readInt();
        int width = raf.readInt();
        int height = raf.readInt();

        Control ctrl = new Control(id, type, name, x, y, width, height);
        ctrl.setText(raf.readUTF());
        ctrl.setValue(raf.readUTF());
        ctrl.setSelected(raf.readBoolean());
        ctrl.setVisible(raf.readBoolean());
        ctrl.setEnabled(raf.readBoolean());
        ctrl.setBackgroundColor(raf.readInt());
        ctrl.setTextColor(raf.readInt());

        return ctrl;
    }

    // Hit test
    public boolean contains(int px, int py) {
        return px >= x && px < x + width &&
                py >= y && py < y + height;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isFocused() { return focused; }
    public void setFocused(boolean focused) { this.focused = focused; }

    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }

    public int getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(int backgroundColor) { this.backgroundColor = backgroundColor; }

    public int getTextColor() { return textColor; }
    public void setTextColor(int textColor) { this.textColor = textColor; }

    public int getBorderColor() { return borderColor; }
    public void setBorderColor(int borderColor) { this.borderColor = borderColor; }

    public int getBorderWidth() { return borderWidth; }
    public void setBorderWidth(int borderWidth) { this.borderWidth = borderWidth; }

    public String getOnClick() { return onClick; }
    public void setOnClick(String onClick) { this.onClick = onClick; }

    public String getOnKeyPress() { return onKeyPress; }
    public void setOnKeyPress(String onKeyPress) { this.onKeyPress = onKeyPress; }

    public String getOnFocus() { return onFocus; }
    public void setOnFocus(String onFocus) { this.onFocus = onFocus; }

    public String getOnBlur() { return onBlur; }
    public void setOnBlur(String onBlur) { this.onBlur = onBlur; }

    @Override
    public String toString() {
        return String.format("Control[%d] %s '%s' at (%d,%d) %dx%d %s",
                id, type, name, x, y, width, height,
                visible ? (enabled ? "" : "[DISABLED]") : "[HIDDEN]");
    }
}

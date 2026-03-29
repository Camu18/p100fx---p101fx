package Lam.camu.p100fx.util;

import java.util.Stack;
import Lam.camu.p100fx.model.DataBlock;

/**
 * Interface cho mọi hành động có thể Undo/Redo.
 */
interface Command {
    void execute();
    void undo();
}

/**
 * Quản lý Undo/Redo.
 */
public class UndoRedoManager {
    private Stack<Command> undoStack = new Stack<>();
    private Stack<Command> redoStack = new Stack<>();

    public void execute(Command cmd) {
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear(); // mỗi lần execute mới thì xóa redo
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }

    public void undo() {
        if (!canUndo()) return;
        Command cmd = undoStack.pop();
        cmd.undo();
        redoStack.push(cmd);
    }

    public void redo() {
        if (!canRedo()) return;
        Command cmd = redoStack.pop();
        cmd.execute();
        undoStack.push(cmd);
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}

/**
 * Ví dụ Command cho thao tác setPixel.
 */
class SetPixelCommand implements Command {
    private DataBlock block;
    private int x, y;
    private int oldColor, newColor;

    public SetPixelCommand(DataBlock block, int x, int y, int newColor) {
        this.block = block;
        this.x = x;
        this.y = y;
        this.newColor = newColor;
        this.oldColor = block.getPixel(x, y);
    }

    @Override
    public void execute() {
        block.setPixel(x, y, newColor);
    }

    @Override
    public void undo() {
        block.setPixel(x, y, oldColor);
    }
}

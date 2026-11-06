package Yuki.lumiclient.keybinds;

public record EditorLayout(KeyboardLayout.Rect panel, int listTop, int rows, int footerY) {
    public static final int ROW_HEIGHT = 32;

    public static EditorLayout of(int width, int height) {
        int w = Math.min(680, width - 16);
        int h = Math.min(460, height - 16);
        KeyboardLayout.Rect panel = new KeyboardLayout.Rect((width - w) / 2, (height - h) / 2, w, h);
        int listTop = panel.y() + 84;
        int footer = panel.bottom() - 92;
        return new EditorLayout(panel, listTop, Math.max(1, (footer - listTop - 4) / 32), footer);
    }

    public int pages(int count) {
        return Math.max(1, (count + this.rows - 1) / this.rows);
    }

    public int clampPage(int page, int count) {
        return Math.clamp((long)page, 0, this.pages(count) - 1);
    }
}

package Yuki.lumiclient.keybinds;

public record KeyboardViewLayout(KeyboardLayout.Rect panel, int keyX, int keyY, double unit) {
    public static KeyboardViewLayout of(int width, int height) {
        return KeyboardViewLayout.of(width, height, KeyboardLayout.Size.FULL);
    }

    public static KeyboardViewLayout of(int width, int height, KeyboardLayout.Size size) {
        double unit = Math.max(1.0, Math.min(40.0, Math.min(((double)width - 40.0) / size.width(), ((double)height - 165.0) / size.height())));
        int w = (int)Math.ceil(size.width() * unit) + 20;
        int h = (int)Math.ceil(size.height() * unit) + 20;
        int x = (width - w) / 2;
        int y = 76 + Math.max(0, (height - 145 - h) / 2);
        return new KeyboardViewLayout(new KeyboardLayout.Rect(x, y, w, h), x + 10, y + 10, unit);
    }
}

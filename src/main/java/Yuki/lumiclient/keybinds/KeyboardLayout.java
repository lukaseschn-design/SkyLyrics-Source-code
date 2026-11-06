package Yuki.lumiclient.keybinds;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class KeyboardLayout {
    public static final double WIDTH = 23.0;
    public static final double HEIGHT = 6.5;
    public static final List<Key> KEYS = KeyboardLayout.create();

    private static List<Key> create() {
        ArrayList<Key> keys = new ArrayList<Key>();
        KeyboardLayout.add(keys, "escape", "Esc", 0.0, 0.0, 1.0, 1.0);
        for (int i = 1; i <= 12; ++i) {
            KeyboardLayout.add(keys, "f" + i, "F" + i, (double)(2 + (i - 1)) + (double)((i - 1) / 4) * 0.5, 0.0, 1.0, 1.0);
        }
        KeyboardLayout.row(keys, 15.5, 0.0, "print.screen|Prt", "scroll.lock|Scr", "pause|Pau");
        KeyboardLayout.row(keys, 0.0, 1.5, "grave.accent|`", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "minus|-", "equal|=");
        KeyboardLayout.add(keys, "backspace", "Back", 13.0, 1.5, 2.0, 1.0);
        KeyboardLayout.add(keys, "tab", "Tab", 0.0, 2.5, 1.5, 1.0);
        KeyboardLayout.row(keys, 1.5, 2.5, "q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "left.bracket|[", "right.bracket|]");
        KeyboardLayout.add(keys, "backslash", "\\", 13.5, 2.5, 1.5, 1.0);
        KeyboardLayout.add(keys, "caps.lock", "Caps", 0.0, 3.5, 1.75, 1.0);
        KeyboardLayout.row(keys, 1.75, 3.5, "a", "s", "d", "f", "g", "h", "j", "k", "l", "semicolon|;", "apostrophe|'");
        KeyboardLayout.add(keys, "enter", "Enter", 12.75, 3.5, 2.25, 1.0);
        KeyboardLayout.add(keys, "left.shift", "Shift", 0.0, 4.5, 2.25, 1.0);
        KeyboardLayout.row(keys, 2.25, 4.5, "z", "x", "c", "v", "b", "n", "m", "comma|,", "period|.", "slash|/");
        KeyboardLayout.add(keys, "right.shift", "Shift", 12.25, 4.5, 2.75, 1.0);
        KeyboardLayout.add(keys, "left.control", "Ctrl", 0.0, 5.5, 1.25, 1.0);
        KeyboardLayout.add(keys, "left.win", "Win", 1.25, 5.5, 1.25, 1.0);
        KeyboardLayout.add(keys, "left.alt", "Alt", 2.5, 5.5, 1.25, 1.0);
        KeyboardLayout.add(keys, "space", "Space", 3.75, 5.5, 6.25, 1.0);
        KeyboardLayout.add(keys, "right.alt", "Alt", 10.0, 5.5, 1.25, 1.0);
        KeyboardLayout.add(keys, "right.win", "Win", 11.25, 5.5, 1.25, 1.0);
        KeyboardLayout.add(keys, "menu", "Menu", 12.5, 5.5, 1.25, 1.0);
        KeyboardLayout.add(keys, "right.control", "Ctrl", 13.75, 5.5, 1.25, 1.0);
        KeyboardLayout.row(keys, 15.5, 1.5, "insert|Ins", "home|Home", "page.up|PgU");
        KeyboardLayout.row(keys, 15.5, 2.5, "delete|Del", "end|End", "page.down|PgD");
        KeyboardLayout.add(keys, "up", "\u2191", 16.5, 4.5, 1.0, 1.0);
        KeyboardLayout.row(keys, 15.5, 5.5, "left|\u2190", "down|\u2193", "right|\u2192");
        KeyboardLayout.row(keys, 19.0, 1.5, "num.lock|Num", "keypad.divide|/", "keypad.multiply|*", "keypad.subtract|-");
        KeyboardLayout.row(keys, 19.0, 2.5, "keypad.7|7", "keypad.8|8", "keypad.9|9");
        KeyboardLayout.row(keys, 19.0, 3.5, "keypad.4|4", "keypad.5|5", "keypad.6|6");
        KeyboardLayout.row(keys, 19.0, 4.5, "keypad.1|1", "keypad.2|2", "keypad.3|3");
        KeyboardLayout.add(keys, "keypad.add", "+", 22.0, 2.5, 1.0, 2.0);
        KeyboardLayout.add(keys, "keypad.enter", "Ent", 22.0, 4.5, 1.0, 2.0);
        KeyboardLayout.add(keys, "keypad.0", "0", 19.0, 5.5, 2.0, 1.0);
        KeyboardLayout.add(keys, "keypad.decimal", ".", 21.0, 5.5, 1.0, 1.0);
        return List.copyOf(keys);
    }

    private static List<Key> createSixty() {
        ArrayList<Key> keys = new ArrayList<Key>();
        for (Key key : KEYS) {
            if (key.x() >= 15.0 || key.y() < 1.5) continue;
            String id = key.id();
            String label = key.label();
            if (id.equals("key.keyboard.grave.accent")) {
                id = "key.keyboard.escape";
                label = "Esc";
            }
            if (id.equals("key.keyboard.right.win")) {
                id = "studiouskeybinds.fn";
                label = "Fn";
            }
            keys.add(new Key(id, label, key.x(), key.y() - 1.5, key.width(), key.height()));
        }
        return List.copyOf(keys);
    }

    private static List<Key> createSeventyFive() {
        ArrayList<Key> keys = new ArrayList<Key>();
        KeyboardLayout.add(keys, "escape", "Esc", 0.0, 0.0, 1.0, 1.0);
        for (int i = 1; i <= 12; ++i) {
            KeyboardLayout.add(keys, "f" + i, "F" + i, i, 0.0, 1.0, 1.0);
        }
        KeyboardLayout.row(keys, 13.0, 0.0, "print.screen|Prt", "insert|Ins", "delete|Del");
        for (Key key : KEYS) {
            if (key.x() >= 15.0 || key.y() < 1.5 || key.y() == 5.5 && key.x() >= 10.0) continue;
            double w = key.id().equals("key.keyboard.right.shift") ? 1.75 : key.width();
            keys.add(new Key(key.id(), key.label(), key.x(), key.y() - 0.25, w, key.height()));
        }
        KeyboardLayout.add(keys, "right.alt", "Alt", 10.0, 5.25, 1.0, 1.0);
        keys.add(new Key("studiouskeybinds.fn", "Fn", 11.0, 5.25, 1.0, 1.0));
        KeyboardLayout.add(keys, "right.control", "Ctrl", 12.0, 5.25, 1.0, 1.0);
        KeyboardLayout.add(keys, "home", "Home", 15.0, 1.25, 1.0, 1.0);
        KeyboardLayout.add(keys, "page.up", "PgU", 15.0, 2.25, 1.0, 1.0);
        KeyboardLayout.add(keys, "page.down", "PgD", 15.0, 3.25, 1.0, 1.0);
        KeyboardLayout.add(keys, "end", "End", 15.0, 4.25, 1.0, 1.0);
        KeyboardLayout.add(keys, "up", "\u2191", 14.0, 4.25, 1.0, 1.0);
        KeyboardLayout.row(keys, 13.0, 5.25, "left|\u2190", "down|\u2193", "right|\u2192");
        return List.copyOf(keys);
    }

    private static void row(List<Key> keys, double x, double y, String ... entries) {
        for (String entry : entries) {
            String[] parts = entry.split("\\|", 2);
            double d = x;
            x = d + 1.0;
            KeyboardLayout.add(keys, parts[0], parts.length == 2 ? parts[1] : parts[0].toUpperCase(Locale.ROOT), d, y, 1.0, 1.0);
        }
    }

    private static void add(List<Key> keys, String id, String label, double x, double y, double w, double h) {
        keys.add(new Key("key.keyboard." + id, label, x, y, w, h));
    }

    public record Key(String id, String label, double x, double y, double width, double height) {
        public boolean bindable() {
            return !this.id.equals("studiouskeybinds.fn");
        }

        public Rect bounds(int left, int top, double unit) {
            int x1 = left + (int)Math.round(this.x * unit);
            int y1 = top + (int)Math.round(this.y * unit);
            int x2 = left + (int)Math.round((this.x + this.width) * unit) - Math.max(2, (int)(unit * 0.09));
            int y2 = top + (int)Math.round((this.y + this.height) * unit) - Math.max(2, (int)(unit * 0.09));
            return new Rect(x1, y1, Math.max(1, x2 - x1), Math.max(1, y2 - y1));
        }
    }

    public record Rect(int x, int y, int width, int height) {
        public int right() {
            return this.x + this.width;
        }

        public int bottom() {
            return this.y + this.height;
        }

        public boolean contains(double px, double py) {
            return px >= (double)this.x && py >= (double)this.y && px < (double)this.right() && py < (double)this.bottom();
        }
    }

    public static enum Size {
        SIXTY("60", 15.0, 5.0, KeyboardLayout.createSixty()),
        SEVENTY_FIVE("75", 16.0, 6.25, KeyboardLayout.createSeventyFive()),
        FULL("100", 23.0, 6.5, KEYS);

        private final String id;
        private final double width;
        private final double height;
        private final List<Key> keys;

        private Size(String id, double width, double height, List<Key> keys) {
            this.id = id;
            this.width = width;
            this.height = height;
            this.keys = List.copyOf(keys);
        }

        public String id() {
            return this.id;
        }

        public String label() {
            return this.id + "%";
        }

        public double width() {
            return this.width;
        }

        public double height() {
            return this.height;
        }

        public List<Key> keys() {
            return this.keys;
        }

        public static Size fromId(String id) {
            for (Size size : Size.values()) {
                if (!size.id.equals(id)) continue;
                return size;
            }
            return FULL;
        }
    }
}

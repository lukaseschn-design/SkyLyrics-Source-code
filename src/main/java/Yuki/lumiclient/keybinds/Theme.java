package Yuki.lumiclient.keybinds;

import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.MutableComponent;
import org.joml.Matrix3x2fStack;

public final class Theme {
    public static final int TEXT = -920073;
    public static final int MUTED = -5787202;
    public static final int ACCENT = -6693165;
    public static final int GREEN = -9316710;
    public static final int RED = -885367;
    public static final int BORDER = -12168865;

    public Theme() {
    }

    public static MutableComponent tr(String key, Object... args) {
        return UiText.tr(key, args);
    }

    public static void rounded(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        if (w < 5 || h < 5) {
            g.fill(x, y, x + w, y + h, color);
            return;
        }
        g.fill(x + 2, y, x + w - 2, y + h, color);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, color);
        g.fill(x, y + 2, x + w, y + h - 2, color);
    }

    public static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        Theme.rounded(g, x, y + 3, w, h, -2063597568);
        Theme.rounded(g, x, y, w, h, -12168865);
        Theme.rounded(g, x + 1, y + 1, w - 2, h - 2, -15262427);
        g.fill(x + 7, y + 1, x + w - 7, y + 2, -11116176);
    }

    public static int mix(int a, int b, double amount) {
        double t = Math.clamp(amount, 0.0, 1.0);
        int color = 0;
        for (int shift : new int[]{24, 16, 8, 0}) {
            int from = a >>> shift & 0xFF;
            int to = b >>> shift & 0xFF;
            color |= (int) Math.round((double) from + (double) (to - from) * t) << shift;
        }
        return color;
    }

    public static String fit(Font font, String value, int width) {
        if (font.width(value) <= width) {
            return value;
        }
        return font.plainSubstrByWidth(value, Math.max(0, width - font.width("…"))) + "…";
    }

    public static void centeredFit(GuiGraphicsExtractor g, Font font, String text, int cx, int cy, int width, int color) {
        float scale = (float) Math.min(1.0, (double) Math.max(1, width) / (double) Math.max(1, font.width(text)));
        Matrix3x2fStack matrices = g.pose();
        matrices.pushMatrix();
        matrices.translate((float) cx, (float) cy);
        matrices.scale(scale, scale);
        int n = -font.width(text) / 2;
        g.text(font, text, n, -9 / 2, color, false);
        matrices.popMatrix();
    }
}

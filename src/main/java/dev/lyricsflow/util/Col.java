package dev.lyricsflow.util;

import java.awt.Color;

public final class Col {
    private Col() {
    }

    public static int a(int c) {
        return c >>> 24;
    }

    public static int r(int c) {
        return c >> 16 & 0xFF;
    }

    public static int g(int c) {
        return c >> 8 & 0xFF;
    }

    public static int b(int c) {
        return c & 0xFF;
    }

    public static int argb(int a, int r, int g, int b) {
        return Col.clamp(a) << 24 | Col.clamp(r) << 16 | Col.clamp(g) << 8 | Col.clamp(b);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    public static int alpha(int c, float mul) {
        if (mul >= 1.0f) {
            return c;
        }
        if (mul <= 0.0f) {
            return c & 0xFFFFFF;
        }
        return (int)((float)Col.a(c) * mul) << 24 | c & 0xFFFFFF;
    }

    public static int withAlpha(int c, int alpha) {
        return Col.clamp(alpha) << 24 | c & 0xFFFFFF;
    }

    public static int lerp(int c1, int c2, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        return Col.argb((int)((float)Col.a(c1) + (float)(Col.a(c2) - Col.a(c1)) * t), (int)((float)Col.r(c1) + (float)(Col.r(c2) - Col.r(c1)) * t), (int)((float)Col.g(c1) + (float)(Col.g(c2) - Col.g(c1)) * t), (int)((float)Col.b(c1) + (float)(Col.b(c2) - Col.b(c1)) * t));
    }

    public static int brighten(int c, float amount) {
        return Col.lerp(c, c & 0xFF000000 | 0xFFFFFF, amount);
    }

    public static int darken(int c, float amount) {
        return Col.lerp(c, c & 0xFF000000, amount);
    }

    public static int hsv(float h, float s, float v) {
        return 0xFF000000 | Color.HSBtoRGB(h, s, v) & 0xFFFFFF;
    }

    public static float[] toHsv(int c) {
        return Color.RGBtoHSB(Col.r(c), Col.g(c), Col.b(c), null);
    }
}


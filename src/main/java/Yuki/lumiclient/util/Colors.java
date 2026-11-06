/*
 * Decompiled with CFR 0.152.
 */
package Yuki.lumiclient.util;

public final class Colors {
    private Colors() {
    }

    public static int argb(int a, int r, int g, int b) {
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
    }

    public static int rgb(int r, int g, int b) {
        return Colors.argb(255, r, g, b);
    }

    public static int withAlpha(int color, int alpha) {
        return color & 0xFFFFFF | (alpha & 0xFF) << 24;
    }

    public static int withAlpha(int color, float alpha) {
        return Colors.withAlpha(color, (int)(Math.clamp(alpha, 0.0f, 1.0f) * 255.0f));
    }

    public static int alpha(int color) {
        return color >>> 24 & 0xFF;
    }

    public static int red(int color) {
        return color >> 16 & 0xFF;
    }

    public static int green(int color) {
        return color >> 8 & 0xFF;
    }

    public static int blue(int color) {
        return color & 0xFF;
    }

    public static int lerp(int from, int to, float t) {
        t = Math.clamp(t, 0.0f, 1.0f);
        int a = (int)((float)Colors.alpha(from) + (float)(Colors.alpha(to) - Colors.alpha(from)) * t);
        int r = (int)((float)Colors.red(from) + (float)(Colors.red(to) - Colors.red(from)) * t);
        int g = (int)((float)Colors.green(from) + (float)(Colors.green(to) - Colors.green(from)) * t);
        int b = (int)((float)Colors.blue(from) + (float)(Colors.blue(to) - Colors.blue(from)) * t);
        return Colors.argb(a, r, g, b);
    }

    public static int lighten(int color, float t) {
        return Colors.lerp(color, Colors.withAlpha(-1, Colors.alpha(color)), t);
    }

    public static int darken(int color, float t) {
        return Colors.lerp(color, Colors.withAlpha(-16777216, Colors.alpha(color)), t);
    }

    public static float[] rgbToHsv(int argb) {
        float min;
        float b;
        float g;
        float r = (float)Colors.red(argb) / 255.0f;
        float max = Math.max(r, Math.max(g = (float)Colors.green(argb) / 255.0f, b = (float)Colors.blue(argb) / 255.0f));
        float delta = max - (min = Math.min(r, Math.min(g, b)));
        float h = delta == 0.0f ? 0.0f : (max == r ? 60.0f * ((g - b) / delta % 6.0f) : (max == g ? 60.0f * ((b - r) / delta + 2.0f) : 60.0f * ((r - g) / delta + 4.0f)));
        if (h < 0.0f) {
            h += 360.0f;
        }
        float s = max == 0.0f ? 0.0f : delta / max;
        return new float[]{h, s, max};
    }

    public static int hsvToRgb(float h, float s, float v) {
        float b;
        float g;
        float r;
        float c = v * s;
        float x = c * (1.0f - Math.abs(h / 60.0f % 2.0f - 1.0f));
        float m = v - c;
        if (h < 60.0f) {
            r = c;
            g = x;
            b = 0.0f;
        } else if (h < 120.0f) {
            r = x;
            g = c;
            b = 0.0f;
        } else if (h < 180.0f) {
            r = 0.0f;
            g = c;
            b = x;
        } else if (h < 240.0f) {
            r = 0.0f;
            g = x;
            b = c;
        } else if (h < 300.0f) {
            r = x;
            g = 0.0f;
            b = c;
        } else {
            r = c;
            g = 0.0f;
            b = x;
        }
        return Colors.rgb(Math.round((r + m) * 255.0f), Math.round((g + m) * 255.0f), Math.round((b + m) * 255.0f));
    }
}


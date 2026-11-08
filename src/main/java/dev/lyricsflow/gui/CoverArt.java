package dev.lyricsflow.gui;

import dev.lyricsflow.LyricsFlow;
import dev.lyricsflow.media.MediaManager;
import dev.lyricsflow.render.Gfx;
import dev.lyricsflow.util.Col;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class CoverArt {
    private static final class_2960 ID = LyricsFlow.id("dynamic/cover");
    private static final class_2960 ID_ROUND = LyricsFlow.id("dynamic/cover_round");
    private static long loadedSeq = -2L;
    private static boolean has;
    private static int w;
    private static int h;
    private static volatile int[] decoded;
    private static volatile long decodedSeq;
    private static int rw;
    private static volatile int vivid;

    private CoverArt() {
    }

    public static class_2960 id() {
        return ID;
    }

    public static boolean ready() {
        MediaManager mm = MediaManager.get();
        long seq = mm.coverSeq();
        if (seq != mm.songSeq()) {
            if (has && loadedSeq != seq) {
                CoverArt.release();
            }
            return false;
        }
        if (seq == loadedSeq) {
            int[] px = decoded;
            if (px != null && decodedSeq == seq && !has) {
                CoverArt.upload(px);
            }
            return has;
        }
        loadedSeq = seq;
        CoverArt.release();
        decoded = null;
        byte[] bytes = mm.cover();
        if (bytes == null || bytes.length == 0) {
            return false;
        }
        Thread th = new Thread(() -> {
            try {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
                if (img == null) {
                    return;
                }
                int s = Math.min(img.getWidth(), img.getHeight());
                int ox = (img.getWidth() - s) / 2;
                int oy = (img.getHeight() - s) / 2;
                int size = Math.min(256, s);
                int[] out = new int[size * size + 1];
                out[0] = size;
                for (int y = 0; y < size; ++y) {
                    for (int x = 0; x < size; ++x) {
                        out[1 + y * size + x] = 0xFF000000 | img.getRGB(ox + x * s / size, oy + y * s / size) & 0xFFFFFF;
                    }
                }
                decodedSeq = seq;
                decoded = out;
            }
            catch (Throwable throwable) {
            }
        }, "SkyLyrics cover");
        th.setDaemon(true);
        th.start();
        return false;
    }

    private static void upload(int[] px) {
        try {
            int s;
            int size = px[0];
            class_1011 ni = new class_1011(size, size, false);
            for (int y = 0; y < size; ++y) {
                for (int x = 0; x < size; ++x) {
                    ni.method_61941(x, y, px[1 + y * size + x]);
                }
            }
            class_310.method_1551().method_1531().method_4616(ID, (class_1044)new class_1043(() -> "lyricsflow cover", ni));
            int rs = Math.min(size, 96);
            class_1011 rn = new class_1011(rs, rs, false);
            float c = (float)rs / 2.0f;
            for (int y = 0; y < rs; ++y) {
                for (int x = 0; x < rs; ++x) {
                    float d = (float)Math.hypot((float)x + 0.5f - c, (float)y + 0.5f - c);
                    float al = Math.max(0.0f, Math.min(1.0f, c - d));
                    int src = px[1 + y * size / rs * size + x * size / rs];
                    rn.method_61941(x, y, (int)(al * 255.0f) << 24 | src & 0xFFFFFF);
                }
            }
            class_310.method_1551().method_1531().method_4616(ID_ROUND, (class_1044)new class_1043(() -> "lyricsflow cover round", rn));
            rw = rs;
            vivid = CoverArt.vividOf(px);
            w = h = (s = size);
            has = true;
        }
        catch (Throwable t) {
            LyricsFlow.LOG.debug("Cover art failed", t);
            has = false;
        }
    }

    public static int vivid() {
        return has ? vivid : 0;
    }

    private static int vividOf(int[] px) {
        double r = 0.0;
        double g = 0.0;
        double b = 0.0;
        double wsum = 0.0;
        int size = px[0];
        for (int i = 1; i < 1 + size * size; i += 3) {
            int c = px[i];
            float[] hsv = Color.RGBtoHSB(c >> 16 & 0xFF, c >> 8 & 0xFF, c & 0xFF, null);
            double w = (double)(hsv[1] * hsv[1] * hsv[2] * hsv[2]) + 1.0E-4;
            r += (double)(c >> 16 & 0xFF) * w;
            g += (double)(c >> 8 & 0xFF) * w;
            b += (double)(c & 0xFF) * w;
            wsum += w;
        }
        float[] hsv = Color.RGBtoHSB((int)(r / wsum), (int)(g / wsum), (int)(b / wsum), null);
        return Color.HSBtoRGB(hsv[0], Math.max(0.45f, hsv[1]), Math.max(0.85f, hsv[2])) & 0xFFFFFF;
    }

    public static void drawRound(class_332 g, float cx, float cy, float r, float a) {
        if (!has) {
            return;
        }
        float sc = Gfx.scale();
        int px = Math.max(2, Math.round(r * 2.0f * sc));
        g.method_51448().pushMatrix();
        g.method_51448().translate(cx, cy);
        g.method_51448().scale(1.0f / sc, 1.0f / sc);
        g.method_25293(class_10799.field_56883, ID_ROUND, -px / 2, -px / 2, 0.0f, 0.0f, px, px, rw, rw, rw, rw, Col.alpha(-1, a));
        g.method_51448().popMatrix();
    }

    private static void release() {
        if (has) {
            try {
                class_310.method_1551().method_1531().method_4615(ID);
            }
            catch (Throwable throwable) {
            }
            try {
                class_310.method_1551().method_1531().method_4615(ID_ROUND);
            }
            catch (Throwable throwable) {
            }
        }
        has = false;
    }

    public static void draw(class_332 g, float x, float y, float size, float a) {
        if (!has) {
            return;
        }
        float sc = Gfx.scale();
        int px = Math.round(size * sc);
        g.method_51448().pushMatrix();
        g.method_51448().translate(x, y);
        g.method_51448().scale(1.0f / sc, 1.0f / sc);
        g.method_25293(class_10799.field_56883, ID, 0, 0, 0.0f, 0.0f, px, px, w, h, w, h, Col.alpha(-1, a));
        g.method_51448().popMatrix();
    }

    static {
        decodedSeq = -3L;
        rw = 1;
    }
}


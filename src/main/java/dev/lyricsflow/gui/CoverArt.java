/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.resources.Identifier
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package dev.lyricsflow.gui;

import dev.lyricsflow.SkyLyrics;
import dev.lyricsflow.media.MediaManager;
import dev.lyricsflow.render.Gfx;
import dev.lyricsflow.util.Col;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class CoverArt {
    private static final Identifier ID = SkyLyrics.id("dynamic/cover");
    private static final Identifier ID_ROUND = SkyLyrics.id("dynamic/cover_round");
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

    public static Identifier id() {
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
                // empty catch block
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
            NativeImage ni = new NativeImage(size, size, false);
            for (int y = 0; y < size; ++y) {
                for (int x = 0; x < size; ++x) {
                    ni.setPixel(x, y, px[1 + y * size + x]);
                }
            }
            Minecraft.getInstance().getTextureManager().register(ID, (AbstractTexture)new DynamicTexture(() -> "lyricsflow cover", ni));
            int rs = Math.min(size, 96);
            NativeImage rn = new NativeImage(rs, rs, false);
            float c = (float)rs / 2.0f;
            for (int y = 0; y < rs; ++y) {
                for (int x = 0; x < rs; ++x) {
                    float d = (float)Math.hypot((float)x + 0.5f - c, (float)y + 0.5f - c);
                    float al = Math.max(0.0f, Math.min(1.0f, c - d));
                    int src = px[1 + y * size / rs * size + x * size / rs];
                    rn.setPixel(x, y, (int)(al * 255.0f) << 24 | src & 0xFFFFFF);
                }
            }
            Minecraft.getInstance().getTextureManager().register(ID_ROUND, (AbstractTexture)new DynamicTexture(() -> "lyricsflow cover round", rn));
            rw = rs;
            vivid = CoverArt.vividOf(px);
            w = h = (s = size);
            has = true;
        }
        catch (Throwable t) {
            SkyLyrics.LOG.debug("Cover art failed", t);
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

    public static void drawRound(GuiGraphicsExtractor g, float cx, float cy, float r, float a) {
        if (!has) {
            return;
        }
        float sc = Gfx.scale();
        int px = Math.max(2, Math.round(r * 2.0f * sc));
        g.pose().pushMatrix();
        g.pose().translate(cx, cy);
        g.pose().scale(1.0f / sc, 1.0f / sc);
        g.blit(RenderPipelines.GUI_TEXTURED, ID_ROUND, -px / 2, -px / 2, 0.0f, 0.0f, px, px, rw, rw, rw, rw, Col.alpha(-1, a));
        g.pose().popMatrix();
    }

    private static void release() {
        if (has) {
            try {
                Minecraft.getInstance().getTextureManager().release(ID);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            try {
                Minecraft.getInstance().getTextureManager().release(ID_ROUND);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        has = false;
    }

    public static void draw(GuiGraphicsExtractor g, float x, float y, float size, float a) {
        if (!has) {
            return;
        }
        float sc = Gfx.scale();
        int px = Math.round(size * sc);
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(1.0f / sc, 1.0f / sc);
        g.blit(RenderPipelines.GUI_TEXTURED, ID, 0, 0, 0.0f, 0.0f, px, px, w, h, w, h, Col.alpha(-1, a));
        g.pose().popMatrix();
    }

    static {
        decodedSeq = -3L;
        rw = 1;
    }
}


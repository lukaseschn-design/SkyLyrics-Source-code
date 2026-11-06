/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.rendertype.RenderTypes
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  dev.lyricsflow.compat.LegacyBufferSource
 *  dev.lyricsflow.compat.LegacyBufferSource$BufferSource
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.network.chat.MutableComponent
 */
package dev.lyricsflow.render;

import dev.lyricsflow.SkyLyrics;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.config.LFConfig;
import dev.lyricsflow.render.Fonts;
import dev.lyricsflow.render.Proj;
import dev.lyricsflow.util.Col;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.lyricsflow.compat.LegacyBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.MutableComponent;

public final class Gfx {
    private static PoseStack wPose;
    private static LegacyBufferSource wBuf;
    public static float worldDepth;
    private static final float[] W0;
    private static final float[] W1;
    private static final float[] W2;
    private static final float[] W3;
    private static final Set<RenderType> wUsed;
    private static int wSeq;
    private static final Seg[] SEGS;
    private static int segNext;
    private static final Identifier ROUND;
    private static final Identifier RING;
    private static final Identifier SOFT;
    private static final Identifier DOT;
    private static final Identifier GLOW;
    private static final Identifier BAND;

    private Gfx() {
    }

    public static boolean inWorld() {
        return wPose != null;
    }

    public static void beginWorld(PoseStack pose, LegacyBufferSource buffers) {
        wPose = pose;
        wBuf = buffers;
        worldDepth = Gfx.skyDepth();
        wSeq = 0;
    }

    public static float skyDepth() {
        Minecraft mc = Minecraft.getInstance();
        return Math.max(20.0f, Math.min(96.0f, (float)mc.options.getEffectiveRenderDistance() * 16.0f * 0.5f));
    }

    public static void flushWorld() {
        Gfx.flushBands();
        LegacyBufferSource class_45972 = wBuf;
        if (class_45972 instanceof LegacyBufferSource.BufferSource) {
            LegacyBufferSource.BufferSource bs = (LegacyBufferSource.BufferSource)class_45972;
            for (RenderType t : wUsed) {
                bs.endBatch(t);
            }
        }
    }

    public static void endWorld() {
        Gfx.flushBands();
        LegacyBufferSource class_45972 = wBuf;
        if (class_45972 instanceof LegacyBufferSource.BufferSource) {
            LegacyBufferSource.BufferSource bs = (LegacyBufferSource.BufferSource)class_45972;
            for (RenderType t : wUsed) {
                bs.endBatch(t);
            }
        }
        wUsed.clear();
        wPose = null;
        wBuf = null;
    }

    private static void wQuad(Identifier tex, float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3, float u0, float v0, float u1, float v1, int color) {
        Gfx.wQuadC(tex, x0, y0, x1, y1, x2, y2, x3, y3, u0, v0, u1, v1, color, color, color, color);
    }

    private static void wQuadC(Identifier tex, float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3, float u0, float v0, float u1, float v1, int c0, int c1, int c2, int c3) {
        int color = c0;
        if (Col.a(c0) < 2 && Col.a(c1) < 2 && Col.a(c2) < 2 && Col.a(c3) < 2) {
            return;
        }
        float area = (x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0);
        float d = worldDepth * (1.0f - Math.min(0.03f, (float)wSeq++ * 3.0E-6f));
        Proj.unproject(x0, y0, d, W0);
        Proj.unproject(x1, y1, d, W1);
        Proj.unproject(x2, y2, d, W2);
        Proj.unproject(x3, y3, d, W3);
        RenderType type = RenderTypes.eyes((Identifier)tex);
        wUsed.add(type);
        VertexConsumer vc = wBuf.getBuffer(type);
        PoseStack.Pose pose = wPose.last();
        int ov = OverlayTexture.NO_OVERLAY;
        int light = 0xF000F0;
        if (area < 0.0f) {
            Gfx.vert(vc, pose, W0, u0, v0, c0, ov, light);
            Gfx.vert(vc, pose, W1, u1, v0, c1, ov, light);
            Gfx.vert(vc, pose, W2, u1, v1, c2, ov, light);
            Gfx.vert(vc, pose, W3, u0, v1, c3, ov, light);
        } else {
            Gfx.vert(vc, pose, W3, u0, v1, c3, ov, light);
            Gfx.vert(vc, pose, W2, u1, v1, c2, ov, light);
            Gfx.vert(vc, pose, W1, u1, v0, c1, ov, light);
            Gfx.vert(vc, pose, W0, u0, v0, c0, ov, light);
        }
    }

    private static void vert(VertexConsumer vc, PoseStack.Pose pose, float[] p, float u, float v, int color, int ov, int light) {
        vc.addVertex(pose, p[0], p[1], p[2]).setColor(color).setUv(u, v).setOverlay(ov).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    private static void wStrip(Identifier tex, float x0, float y0, float x1, float y1, float thickness, float u0, float v0, float u1, float v1, int color) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float len = (float)Math.sqrt(dx * dx + dy * dy);
        if (len < 0.001f) {
            return;
        }
        float nx = -dy / len * thickness / 2.0f;
        float ny = dx / len * thickness / 2.0f;
        Gfx.wQuad(tex, x0 - nx, y0 - ny, x1 - nx, y1 - ny, x1 + nx, y1 + ny, x0 + nx, y0 + ny, u0, v0, u1, v1, color);
    }

    public static void bandPath(GuiGraphicsExtractor g, float[] xs, float[] ys, int n, float thickness, int color) {
        int i;
        if (n < 2 || Col.a(color) == 0) {
            return;
        }
        if (!Gfx.inWorld()) {
            for (int i2 = 1; i2 < n; ++i2) {
                Gfx.band(g, xs[i2 - 1], ys[i2 - 1], xs[i2], ys[i2], thickness, color);
            }
            return;
        }
        float h = thickness / 2.0f;
        float[] ox = new float[n];
        float[] oy = new float[n];
        for (i = 0; i < n; ++i) {
            float dy;
            int a = Math.max(0, i - 1);
            int b = Math.min(n - 1, i + 1);
            float dx = xs[b] - xs[a];
            float len = (float)Math.sqrt(dx * dx + (dy = ys[b] - ys[a]) * dy);
            if (len < 1.0E-4f) {
                dx = 1.0f;
                dy = 0.0f;
                len = 1.0f;
            }
            ox[i] = -dy / len * h;
            oy[i] = dx / len * h;
        }
        for (i = 1; i < n; ++i) {
            float u0 = (float)(i - 1) / (float)(n - 1);
            float u1 = (float)i / (float)(n - 1);
            Gfx.wQuad(BAND, xs[i - 1] - ox[i - 1], ys[i - 1] - oy[i - 1], xs[i] - ox[i], ys[i] - oy[i], xs[i] + ox[i], ys[i] + oy[i], xs[i - 1] + ox[i - 1], ys[i - 1] + oy[i - 1], 0.5f, 0.0f, 0.5f, 1.0f, color);
        }
    }

    private static void joinBand(float x0, float y0, float x1, float y1, float thickness, int color) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float len = (float)Math.sqrt(dx * dx + dy * dy);
        if (len < 0.001f) {
            return;
        }
        float nx = -dy / len;
        float ny = dx / len;
        float h = thickness / 2.0f;
        float sx = nx * h;
        float sy = ny * h;
        for (Seg p : SEGS) {
            float r;
            if (!p.on || Math.abs(p.x1 - x0) > 0.001f || Math.abs(p.y1 - y0) > 0.001f || (r = h / Math.max(1.0E-4f, p.h)) < 0.7f || r > 1.43f) continue;
            float ax = p.nx + nx;
            float ay = p.ny + ny;
            float al = (float)Math.sqrt(ax * ax + ay * ay);
            if (al < 0.3f) break;
            float miter = Math.min(2.0f, 1.0f / Math.max(0.5f, (ax /= al) * nx + (ay /= al) * ny));
            float hj = (p.h + h) / 2.0f * miter;
            int jc = Col.lerp(p.color, color, 0.5f);
            Gfx.emitSeg(p, ax * hj, ay * hj, jc);
            Gfx.store(p, x0, y0, x1, y1, h, nx, ny, ax * hj, ay * hj, color);
            p.startColor = jc;
            return;
        }
        Seg slot = null;
        for (Seg p : SEGS) {
            if (p.on) continue;
            slot = p;
            break;
        }
        if (slot == null) {
            slot = SEGS[segNext];
            segNext = (segNext + 1) % SEGS.length;
            Gfx.emitSeg(slot, slot.nx * slot.h, slot.ny * slot.h, slot.color);
        }
        Gfx.store(slot, x0, y0, x1, y1, h, nx, ny, sx, sy, color);
        slot.startColor = color;
    }

    private static void store(Seg p, float x0, float y0, float x1, float y1, float h, float nx, float ny, float sx, float sy, int color) {
        p.on = true;
        p.x0 = x0;
        p.y0 = y0;
        p.x1 = x1;
        p.y1 = y1;
        p.h = h;
        p.nx = nx;
        p.ny = ny;
        p.sx = sx;
        p.sy = sy;
        p.color = color;
    }

    private static void emitSeg(Seg p, float ex, float ey, int endColor) {
        if (!p.on) {
            return;
        }
        p.on = false;
        Gfx.wQuadC(BAND, p.x0 - p.sx, p.y0 - p.sy, p.x1 - ex, p.y1 - ey, p.x1 + ex, p.y1 + ey, p.x0 + p.sx, p.y0 + p.sy, 0.0f, 0.0f, 1.0f, 1.0f, p.startColor, endColor, endColor, p.startColor);
    }

    private static void flushBands() {
        for (Seg p : SEGS) {
            Gfx.emitSeg(p, p.nx * p.h, p.ny * p.h, p.color);
        }
    }

    public static void bandPathC(GuiGraphicsExtractor g, float[] xs, float[] ys, float[] ws, int[] cs, int n) {
        int i;
        if (n < 2) {
            return;
        }
        if (Gfx.inWorld()) {
            Gfx.flushWorld();
        }
        if (!Gfx.inWorld()) {
            for (int i2 = 1; i2 < n; ++i2) {
                Gfx.band(g, xs[i2 - 1], ys[i2 - 1], xs[i2], ys[i2], (ws[i2 - 1] + ws[i2]) / 2.0f, cs[i2]);
            }
            return;
        }
        float[] ox = new float[n];
        float[] oy = new float[n];
        for (i = 0; i < n; ++i) {
            float dy;
            int a = Math.max(0, i - 1);
            int b = Math.min(n - 1, i + 1);
            float dx = xs[b] - xs[a];
            float len = (float)Math.sqrt(dx * dx + (dy = ys[b] - ys[a]) * dy);
            if (len < 1.0E-4f) {
                if (i <= 0) continue;
                ox[i] = ox[i - 1];
                oy[i] = oy[i - 1];
                continue;
            }
            float h = ws[i] / 2.0f;
            ox[i] = -dy / len * h;
            oy[i] = dx / len * h;
        }
        for (i = 1; i < n; ++i) {
            if (Col.a(cs[i - 1]) < 2 && Col.a(cs[i]) < 2) continue;
            Gfx.wQuadC(BAND, xs[i - 1] - ox[i - 1], ys[i - 1] - oy[i - 1], xs[i] - ox[i], ys[i] - oy[i], xs[i] + ox[i], ys[i] + oy[i], xs[i - 1] + ox[i - 1], ys[i - 1] + oy[i - 1], 0.0f, 0.0f, 1.0f, 1.0f, cs[i - 1], cs[i], cs[i], cs[i - 1]);
        }
    }

    public static float scale() {
        return Minecraft.getInstance().getWindow().getGuiScale();
    }

    public static Font font() {
        return Minecraft.getInstance().font;
    }

    public static void rect(GuiGraphicsExtractor g, float x, float y, float w, float h, int color) {
        if (Col.a(color) == 0 || w <= 0.0f || h <= 0.0f) {
            return;
        }
        if (Gfx.inWorld()) {
            Gfx.wQuad(DOT, x, y, x + w, y, x + w, y + h, x, y + h, 0.48f, 0.48f, 0.52f, 0.52f, color);
            return;
        }
        float s = Gfx.scale();
        g.pose().pushMatrix();
        g.pose().scale(1.0f / s, 1.0f / s);
        int x0 = Math.round(x * s);
        int y0 = Math.round(y * s);
        int x1 = Math.round((x + w) * s);
        int y1 = Math.round((y + h) * s);
        if (x1 > x0 && y1 > y0) {
            g.fill(x0, y0, x1, y1, color);
        }
        g.pose().popMatrix();
    }

    public static void gradientV(GuiGraphicsExtractor g, float x, float y, float w, float h, int top, int bottom) {
        float s = Gfx.scale();
        g.pose().pushMatrix();
        g.pose().scale(1.0f / s, 1.0f / s);
        g.fillGradient(Math.round(x * s), Math.round(y * s), Math.round((x + w) * s), Math.round((y + h) * s), top, bottom);
        g.pose().popMatrix();
    }

    public static void gradientH(GuiGraphicsExtractor g, float x, float y, float w, float h, int left, int right) {
        float s = Gfx.scale();
        int px0 = Math.round(x * s);
        int px1 = Math.round((x + w) * s);
        int py0 = Math.round(y * s);
        int py1 = Math.round((y + h) * s);
        int n = Math.max(1, Math.min(48, (px1 - px0) / 4));
        g.pose().pushMatrix();
        g.pose().scale(1.0f / s, 1.0f / s);
        for (int i = 0; i < n; ++i) {
            int a = px0 + (px1 - px0) * i / n;
            int b = px0 + (px1 - px0) * (i + 1) / n;
            g.fill(a, py0, b, py1, Col.lerp(left, right, ((float)i + 0.5f) / (float)n));
        }
        g.pose().popMatrix();
    }

    public static void band(GuiGraphicsExtractor g, float x0, float y0, float x1, float y1, float thickness, int color) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float len = (float)Math.sqrt(dx * dx + dy * dy);
        if (len < 0.5f || Col.a(color) == 0) {
            return;
        }
        if (Gfx.inWorld()) {
            Gfx.joinBand(x0, y0, x1, y1, thickness, color);
            return;
        }
        float s = Gfx.scale();
        g.pose().pushMatrix();
        g.pose().translate(x0, y0);
        g.pose().rotate((float)Math.atan2(dy, dx));
        g.pose().scale(1.0f / s, 1.0f / s);
        int L = Math.round(len * s) + 2;
        int T = Math.max(2, Math.round(thickness * s));
        g.blit(RenderPipelines.GUI_TEXTURED, BAND, -1, -T / 2, 0.0f, 0.0f, L, T, 64, 64, 64, 64, color);
        g.pose().popMatrix();
    }

    public static void worldPicture(Identifier tex, double[] tl, double[] tr, double[] br, double[] bl, int color) {
        Gfx.worldPicture(tex, tl, tr, br, bl, 0.0f, 0.0f, 1.0f, 1.0f, color);
    }

    public static void worldQuad(Identifier tex, double[][] p, float[][] uv, int[] col) {
        int[] order;
        if (!Gfx.inWorld()) {
            return;
        }
        if (Col.a(col[0]) < 2 && Col.a(col[1]) < 2 && Col.a(col[2]) < 2 && Col.a(col[3]) < 2) {
            return;
        }
        RenderType type = RenderTypes.eyes((Identifier)tex);
        wUsed.add(type);
        VertexConsumer vc = wBuf.getBuffer(type);
        PoseStack.Pose pose = wPose.last();
        int ov = OverlayTexture.NO_OVERLAY;
        int light = 0xF000F0;
        for (int k : order = new int[]{0, 1, 2, 3, 3, 2, 1, 0}) {
            Gfx.vert(vc, pose, Gfx.f(p[k]), uv[k][0], uv[k][1], col[k], ov, light);
        }
    }

    public static void worldPictureV(Identifier tex, double[] tl, double[] tr, double[] br, double[] bl, int top, int bottom) {
        if (!Gfx.inWorld() || Col.a(top) < 2 && Col.a(bottom) < 2) {
            return;
        }
        RenderType type = RenderTypes.eyes((Identifier)tex);
        wUsed.add(type);
        VertexConsumer vc = wBuf.getBuffer(type);
        PoseStack.Pose pose = wPose.last();
        int ov = OverlayTexture.NO_OVERLAY;
        int light = 0xF000F0;
        float[] a = Gfx.f(tl);
        float[] b = Gfx.f(tr);
        float[] c = Gfx.f(br);
        float[] d = Gfx.f(bl);
        Gfx.vert(vc, pose, a, 0.0f, 0.0f, top, ov, light);
        Gfx.vert(vc, pose, b, 1.0f, 0.0f, top, ov, light);
        Gfx.vert(vc, pose, c, 1.0f, 1.0f, bottom, ov, light);
        Gfx.vert(vc, pose, d, 0.0f, 1.0f, bottom, ov, light);
        Gfx.vert(vc, pose, d, 0.0f, 1.0f, bottom, ov, light);
        Gfx.vert(vc, pose, c, 1.0f, 1.0f, bottom, ov, light);
        Gfx.vert(vc, pose, b, 1.0f, 0.0f, top, ov, light);
        Gfx.vert(vc, pose, a, 0.0f, 0.0f, top, ov, light);
    }

    public static void worldPicture(Identifier tex, double[] tl, double[] tr, double[] br, double[] bl, float u0, float v0, float u1, float v1, int color) {
        if (!Gfx.inWorld() || Col.a(color) < 2) {
            return;
        }
        RenderType type = RenderTypes.eyes((Identifier)tex);
        wUsed.add(type);
        VertexConsumer vc = wBuf.getBuffer(type);
        PoseStack.Pose pose = wPose.last();
        int ov = OverlayTexture.NO_OVERLAY;
        int light = 0xF000F0;
        float[] a = Gfx.f(tl);
        float[] b = Gfx.f(tr);
        float[] c = Gfx.f(br);
        float[] d = Gfx.f(bl);
        Gfx.vert(vc, pose, a, u0, v0, color, ov, light);
        Gfx.vert(vc, pose, b, u1, v0, color, ov, light);
        Gfx.vert(vc, pose, c, u1, v1, color, ov, light);
        Gfx.vert(vc, pose, d, u0, v1, color, ov, light);
        Gfx.vert(vc, pose, d, u0, v1, color, ov, light);
        Gfx.vert(vc, pose, c, u1, v1, color, ov, light);
        Gfx.vert(vc, pose, b, u1, v0, color, ov, light);
        Gfx.vert(vc, pose, a, u0, v0, color, ov, light);
    }

    private static float[] f(double[] p) {
        return new float[]{(float)p[0], (float)p[1], (float)p[2]};
    }

    public static void image(GuiGraphicsExtractor g, Identifier tex, int texW, int texH, float x, float y, float w, float h, int color) {
        if (Col.a(color) == 0 || w <= 0.0f || h <= 0.0f) {
            return;
        }
        if (Gfx.inWorld()) {
            Gfx.wQuad(tex, x, y, x + w, y, x + w, y + h, x, y + h, 0.0f, 0.0f, 1.0f, 1.0f, color);
            return;
        }
        float s = Gfx.scale();
        int x0 = Math.round(x * s);
        int y0 = Math.round(y * s);
        int W = Math.max(1, Math.round(w * s));
        int H = Math.max(1, Math.round(h * s));
        g.pose().pushMatrix();
        g.pose().scale(1.0f / s, 1.0f / s);
        g.blit(RenderPipelines.GUI_TEXTURED, tex, x0, y0, 0.0f, 0.0f, W, H, texW, texH, texW, texH, color);
        g.pose().popMatrix();
    }

    private static void single(GuiGraphicsExtractor g, Identifier tex, float cx, float cy, float radius, int color) {
        if (Gfx.inWorld()) {
            Gfx.wQuad(tex, cx - radius, cy - radius, cx + radius, cy - radius, cx + radius, cy + radius, cx - radius, cy + radius, 0.0f, 0.0f, 1.0f, 1.0f, color);
            return;
        }
        float s = Gfx.scale();
        int d = Math.max(1, Math.round(radius * 2.0f * s));
        int x0 = Math.round(cx * s) - d / 2;
        int y0 = Math.round(cy * s) - d / 2;
        g.pose().pushMatrix();
        g.pose().scale(1.0f / s, 1.0f / s);
        g.blit(RenderPipelines.GUI_TEXTURED, tex, x0, y0, 0.0f, 0.0f, d, d, 128, 128, 128, 128, color);
        g.pose().popMatrix();
    }

    private static void nine(GuiGraphicsExtractor g, Identifier tex, int texSize, int slice, float x, float y, float w, float h, float corner, int top, int bottom) {
        float s = Gfx.scale();
        int x0 = Math.round(x * s);
        int y0 = Math.round(y * s);
        int x1 = Math.round((x + w) * s);
        int y1 = Math.round((y + h) * s);
        int W = x1 - x0;
        int H = y1 - y0;
        if (W <= 0 || H <= 0) {
            return;
        }
        int c = Math.max(1, Math.min(Math.round(corner * s), Math.min(W, H) / 2));
        int mid = texSize - 2 * slice;
        g.pose().pushMatrix();
        g.pose().scale(1.0f / s, 1.0f / s);
        int cw = W - 2 * c;
        int ch = H - 2 * c;
        g.blit(RenderPipelines.GUI_TEXTURED, tex, x0, y0, 0.0f, 0.0f, c, c, slice, slice, texSize, texSize, top);
        if (cw > 0) {
            g.blit(RenderPipelines.GUI_TEXTURED, tex, x0 + c, y0, (float)slice, 0.0f, cw, c, mid, slice, texSize, texSize, top);
        }
        g.blit(RenderPipelines.GUI_TEXTURED, tex, x1 - c, y0, (float)(slice + mid), 0.0f, c, c, slice, slice, texSize, texSize, top);
        if (ch > 0) {
            if (top == bottom || tex != ROUND) {
                int mc = top == bottom ? top : Col.lerp(top, bottom, 0.5f);
                g.blit(RenderPipelines.GUI_TEXTURED, tex, x0, y0 + c, 0.0f, (float)slice, c, ch, slice, mid, texSize, texSize, mc);
                if (cw > 0) {
                    g.blit(RenderPipelines.GUI_TEXTURED, tex, x0 + c, y0 + c, (float)slice, (float)slice, cw, ch, mid, mid, texSize, texSize, mc);
                }
                g.blit(RenderPipelines.GUI_TEXTURED, tex, x1 - c, y0 + c, (float)(slice + mid), (float)slice, c, ch, slice, mid, texSize, texSize, mc);
            } else {
                g.fillGradient(x0, y0 + c, x1, y1 - c, top, bottom);
            }
        }
        g.blit(RenderPipelines.GUI_TEXTURED, tex, x0, y1 - c, 0.0f, (float)(slice + mid), c, c, slice, slice, texSize, texSize, bottom);
        if (cw > 0) {
            g.blit(RenderPipelines.GUI_TEXTURED, tex, x0 + c, y1 - c, (float)slice, (float)(slice + mid), cw, c, mid, slice, texSize, texSize, bottom);
        }
        g.blit(RenderPipelines.GUI_TEXTURED, tex, x1 - c, y1 - c, (float)(slice + mid), (float)(slice + mid), c, c, slice, slice, texSize, texSize, bottom);
        g.pose().popMatrix();
    }

    public static void round(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, int color) {
        if (Col.a(color) == 0 || w <= 0.0f || h <= 0.0f) {
            return;
        }
        if (radius < 0.4f) {
            Gfx.rect(g, x, y, w, h, color);
            return;
        }
        Gfx.nine(g, ROUND, 136, 64, x, y, w, h, radius, color, color);
    }

    public static void roundGradient(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, int top, int bottom) {
        if (w <= 0.0f || h <= 0.0f) {
            return;
        }
        if (radius < 0.4f) {
            Gfx.gradientV(g, x, y, w, h, top, bottom);
            return;
        }
        Gfx.nine(g, ROUND, 136, 64, x, y, w, h, radius, top, bottom);
    }

    public static void roundOutline(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, float thickness, int color) {
        if (Col.a(color) == 0 || w <= 0.0f || h <= 0.0f) {
            return;
        }
        if (Gfx.inWorld()) {
            float cx = x + w / 2.0f;
            float cy = y + h / 2.0f;
            float rx = w / 2.0f;
            float ry = h / 2.0f;
            int n = 64;
            float ht = Math.max(1.2f, thickness * 2.4f) / 2.0f;
            for (int i = 0; i < n; ++i) {
                double a0 = (double)i * Math.PI * 2.0 / (double)n;
                double a1 = (double)(i + 1) * Math.PI * 2.0 / (double)n;
                float c0 = (float)Math.cos(a0);
                float s0 = (float)Math.sin(a0);
                float c1 = (float)Math.cos(a1);
                float s1 = (float)Math.sin(a1);
                Gfx.wQuad(BAND, cx + c0 * (rx - ht), cy + s0 * (ry - ht), cx + c1 * (rx - ht), cy + s1 * (ry - ht), cx + c1 * (rx + ht), cy + s1 * (ry + ht), cx + c0 * (rx + ht), cy + s0 * (ry + ht), 0.0f, 0.0f, 1.0f, 1.0f, color);
            }
            return;
        }
        float r = Math.max(radius, thickness * 3.0f);
        float corner = Math.max(r, thickness * 16.0f);
        corner = Math.min(corner, Math.min(w, h) / 2.0f);
        Gfx.nine(g, RING, 136, 64, x, y, w, h, corner, color, color);
    }

    public static void circle(GuiGraphicsExtractor g, float cx, float cy, float radius, int color) {
        if (radius <= 0.0f || Col.a(color) == 0) {
            return;
        }
        Gfx.single(g, DOT, cx, cy, radius, color);
    }

    public static void shadow(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, float size, int color) {
        if (Col.a(color) == 0) {
            return;
        }
        float e = size * 1.6f;
        Gfx.nine(g, SOFT, 128, 48, x - e, y - e + size * 0.4f, w + 2.0f * e, h + 2.0f * e, Math.max(radius, size) + e, color, color);
    }

    public static void blob(GuiGraphicsExtractor g, float cx, float cy, float radius, int color) {
        if (Col.a(color) == 0 || radius <= 0.5f) {
            return;
        }
        Gfx.single(g, GLOW, cx, cy, radius, color);
    }

    public static void glow(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, float size, int color) {
        if (Col.a(color) == 0) {
            return;
        }
        float e = size * 1.6f;
        Gfx.nine(g, SOFT, 128, 48, x - e, y - e, w + 2.0f * e, h + 2.0f * e, Math.max(radius, size) + e, color, color);
    }

    public static void line(GuiGraphicsExtractor g, float x0, float y0, float x1, float y1, float thickness, int color) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float len = (float)Math.sqrt(dx * dx + dy * dy);
        if (len < 0.01f || Col.a(color) == 0) {
            return;
        }
        if (Gfx.inWorld()) {
            Gfx.wStrip(DOT, x0, y0, x1, y1, Math.max(0.5f, thickness), 0.48f, 0.48f, 0.52f, 0.52f, color);
            return;
        }
        float s = Gfx.scale();
        g.pose().pushMatrix();
        g.pose().translate(x0, y0);
        g.pose().rotate((float)Math.atan2(dy, dx));
        g.pose().scale(1.0f / s, 1.0f / s);
        int L = Math.round(len * s);
        int T = Math.max(1, Math.round(thickness * s));
        g.fill(0, -T / 2, L, -T / 2 + T, color);
        g.pose().popMatrix();
    }

    public static void streak(GuiGraphicsExtractor g, float x0, float y0, float x1, float y1, float thickness, int color) {
        int n = 6;
        for (int i = 0; i < n; ++i) {
            float a = (float)i / (float)n;
            float b = (float)(i + 1) / (float)n;
            Gfx.line(g, x0 + (x1 - x0) * a, y0 + (y1 - y0) * a, x0 + (x1 - x0) * b, y0 + (y1 - y0) * b, thickness, Col.alpha(color, (float)(i + 1) / (float)n));
        }
    }

    public static float textWidth(String s, String fontId, float size) {
        return Fonts.unitWidth(s, fontId, Gfx.font()) * size;
    }

    public static float text(GuiGraphicsExtractor g, String s, String fontId, float x, float y, float size, int color) {
        if (s.isEmpty() || Col.a(color) < 4) {
            return 0.0f;
        }
        Font f = Gfx.font();
        MutableComponent c = Fonts.text(s, fontId);
        float k = size / Fonts.height(fontId);
        g.pose().pushMatrix();
        g.pose().translate(x, y + size * Fonts.yShift(fontId, Fonts.capsOnly(s)));
        g.pose().scale(k, k);
        g.text(f, (Component)c, 0, 0, color, false);
        g.pose().popMatrix();
        return Fonts.unitWidth(s, fontId, f) * size;
    }

    public static float textCentered(GuiGraphicsExtractor g, String s, String fontId, float cx, float y, float size, int color) {
        float w = Gfx.textWidth(s, fontId, size);
        Gfx.text(g, s, fontId, cx - w / 2.0f, y, size, color);
        return w;
    }

    public static String ellipsize(String s, String fontId, float size, float maxWidth) {
        if (Gfx.textWidth(s, fontId, size) <= maxWidth) {
            return s;
        }
        String e = "\u2026";
        int lo = 0;
        int hi = s.length();
        while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            if (Gfx.textWidth(s.substring(0, mid) + e, fontId, size) <= maxWidth) {
                lo = mid;
                continue;
            }
            hi = mid - 1;
        }
        return s.substring(0, lo).trim() + e;
    }

    private static void textShadow(GuiGraphicsExtractor g, String s, String fontId, float x, float y, float size, float o, int soft, float alpha) {
        int strong;
        int mode = ConfigManager.get().textShadow;
        int custom = ConfigManager.get().shadowColor;
        if (custom != 0) {
            soft = custom | 0xFF000000;
        }
        int n = strong = custom != 0 ? custom | 0xFF000000 : -16120302;
        if (mode == 1) {
            Gfx.text(g, s, fontId, x + o * 0.6f, y + o * 1.0f, size, Col.alpha(soft, alpha));
        } else if (mode == 2) {
            Gfx.text(g, s, fontId, x + o * 1.3f, y + o * 2.0f, size, Col.alpha(strong, alpha));
            Gfx.text(g, s, fontId, x + o * 0.7f, y + o * 1.1f, size, Col.alpha(strong, alpha));
        }
    }

    public static void neon(GuiGraphicsExtractor g, String s, String fontId, float cx, float y, float size, int main, int a, int b, float alpha, boolean twoColor, float glow) {
        if (alpha <= 0.01f) {
            return;
        }
        float w = Gfx.textWidth(s, fontId, size);
        float x = cx - w / 2.0f;
        float o = Math.max(0.6f, size * 0.055f);
        boolean clean = ConfigManager.get().textStyle == LFConfig.TextStyle.CLEAN;
        Gfx.textShadow(g, s, fontId, x, y, size, o, clean ? -15396054 : Col.darken(a, 0.75f), alpha);
        if (clean) {
            Gfx.text(g, s, fontId, x, y, size, Col.alpha(Col.lerp(main, -1, 0.2f), alpha));
            return;
        }
        float tcs = ConfigManager.get().twoColorStrength;
        if (twoColor && tcs > 0.01f) {
            int tc = ConfigManager.get().twoColorColor;
            int ca = tc != 0 ? tc | 0xFF000000 : a;
            int cb = tc != 0 ? tc | 0xFF000000 : b;
            float oo = o * (0.35f + 0.65f * tcs);
            float ta = alpha * Math.min(1.0f, 0.3f + 0.7f * tcs);
            Gfx.text(g, s, fontId, x - oo, y + oo * 0.5f, size, Col.alpha(ca, ta));
            Gfx.text(g, s, fontId, x + oo, y - oo * 0.5f, size, Col.alpha(cb, ta));
        }
        Gfx.text(g, s, fontId, x, y, size, Col.alpha(main, alpha));
    }

    static {
        worldDepth = 64.0f;
        W0 = new float[3];
        W1 = new float[3];
        W2 = new float[3];
        W3 = new float[3];
        wUsed = new HashSet<RenderType>();
        SEGS = new Seg[]{new Seg(), new Seg(), new Seg(), new Seg(), new Seg(), new Seg()};
        ROUND = SkyLyrics.id("textures/gui/shape/round.png");
        RING = SkyLyrics.id("textures/gui/shape/ring.png");
        SOFT = SkyLyrics.id("textures/gui/shape/soft.png");
        DOT = SkyLyrics.id("textures/gui/shape/dot.png");
        GLOW = SkyLyrics.id("textures/gui/shape/glow.png");
        BAND = SkyLyrics.id("textures/gui/shape/band.png");
    }

    private static final class Seg {
        boolean on;
        float x0;
        float y0;
        float x1;
        float y1;
        float h;
        float nx;
        float ny;
        float sx;
        float sy;
        int color;
        int startColor;

        private Seg() {
        }
    }
}


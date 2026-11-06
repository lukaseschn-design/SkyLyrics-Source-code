/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.rendertype.RenderTypes
 *  net.minecraft.resources.Identifier
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  dev.lyricsflow.compat.LegacyBufferSource
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package dev.lyricsflow.render;

import dev.lyricsflow.SkyLyrics;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.lyricsflow.compat.LegacyBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public final class Halo {
    private static final Identifier GLOW = SkyLyrics.id("textures/gui/shape/glow.png");
    private static final int LIGHT = 0xF000F0;

    private Halo() {
    }

    public static void draw(Matrix4f m, float x0, float y0, float x1, float y1, int argb, LegacyBufferSource buf) {
        if (argb >>> 24 < 3 || buf == null) {
            return;
        }
        VertexConsumer vc = buf.getBuffer(RenderTypes.eyes((Identifier)GLOW));
        int ov = OverlayTexture.NO_OVERLAY;
        Halo.v(vc, m, x0, y0, 0.0f, 0.0f, argb, ov);
        Halo.v(vc, m, x0, y1, 0.0f, 1.0f, argb, ov);
        Halo.v(vc, m, x1, y1, 1.0f, 1.0f, argb, ov);
        Halo.v(vc, m, x1, y0, 1.0f, 0.0f, argb, ov);
        Halo.v(vc, m, x1, y0, 1.0f, 0.0f, argb, ov);
        Halo.v(vc, m, x1, y1, 1.0f, 1.0f, argb, ov);
        Halo.v(vc, m, x0, y1, 0.0f, 1.0f, argb, ov);
        Halo.v(vc, m, x0, y0, 0.0f, 0.0f, argb, ov);
    }

    private static void v(VertexConsumer vc, Matrix4f m, float x, float y, float u, float vv, int argb, int ov) {
        vc.addVertex((Matrix4fc)m, x, y, 0.0f).setColor(argb).setUv(u, vv).setOverlay(ov).setLight(0xF000F0).setNormal(0.0f, 1.0f, 0.0f);
    }
}


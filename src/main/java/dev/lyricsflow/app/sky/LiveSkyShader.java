/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.renderpearl.api.pipeline.BlendFunction
 *  com.mojang.renderpearl.api.pipeline.RenderPipeline
 *  com.mojang.renderpearl.api.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.renderpearl.api.vertex.VertexFormat$Mode
 *  com.mojang.blaze3d.platform.NativeImage
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  com.mojang.renderpearl.api.pipeline.UniformType
 *  net.minecraft.client.renderer.rendertype.RenderSetup
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  net.minecraft.resources.Identifier
 *  net.minecraft.client.Minecraft
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package dev.lyricsflow.app.sky;

import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.lyricsflow.app.Core;
import dev.lyricsflow.app.config.Config;
import dev.lyricsflow.app.config.Storage;
import dev.lyricsflow.app.sky.LiveSky;
import dev.lyricsflow.app.sky.SkyPreset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.time.LocalDateTime;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.AbstractTexture;
import com.mojang.renderpearl.api.pipeline.UniformType;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public final class LiveSkyShader {
    private static final Identifier TEX = Identifier.fromNamespaceAndPath((String)"lyricsflow", (String)"live_sky_params");
    private static final RenderPipeline PIPE = RenderPipeline.builder()
        .withLocation(Identifier.fromNamespaceAndPath("lyricsflow", "pipeline/live_sky"))
        .withBindGroupLayout(BindGroupLayout.builder()
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .build())
        .withVertexShader(Identifier.fromNamespaceAndPath("lyricsflow", "core/live_sky"))
        .withFragmentShader(Identifier.fromNamespaceAndPath("lyricsflow", "core/live_sky"))
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
        .withCull(false)
        .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
        .withPrimitiveTopology(PrimitiveTopology.QUADS)
        .build();
    private static RenderType type;
    private static DynamicTexture tex;
    private static int state;
    private static final int[] last;
    private static final int[] next;

    private LiveSkyShader() {
    }

    public static boolean ready() {
        if (state != 0) {
            return state > 0;
        }
        try {
            NativeImage img = new NativeImage(16, 1, false);
            tex = new DynamicTexture(() -> "lyricsflow_live_sky", img);
            Minecraft.getInstance().getTextureManager().register(TEX, (AbstractTexture)tex);
            type = RenderType.create((String)"lyricsflow_live_sky", (RenderSetup)RenderSetup.builder((RenderPipeline)PIPE).withTexture("Sampler0", TEX).createRenderSetup());
            state = 1;
            Core.LOG.info("Himmel-Shader aktiv");
        }
        catch (Throwable var1) {
            Core.LOG.warn("Himmel-Shader nicht verfuegbar - nehme den alten Himmel", var1);
            state = -1;
        }
        LiveSkyShader.status(state > 0 ? "aktiv" : "aus (alter Himmel)");
        return state > 0;
    }

    private static void status(String s) {
        try {
            Object gpu = "";
            try {
                gpu = RenderSystem.getDevice().getDeviceInfo().name() + " / " + RenderSystem.getDevice().getDeviceInfo().backendName();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            Files.writeString(Storage.dir().resolve("sky-status.txt"), (CharSequence)(String.valueOf(LocalDateTime.now()) + "\nHimmel-Shader: " + s + "\nGrafik: " + (String)gpu + "\n"), new OpenOption[0]);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static void disable(Throwable t) {
        Core.LOG.warn("Himmel-Shader abgeschaltet", t);
        state = -1;
        LiveSkyShader.status("Fehler beim Zeichnen: " + String.valueOf(t));
    }

    public static RenderType type() {
        return type;
    }

    public static void params(SkyPreset p, int a1, int a2, double t, Config c, Matrix4f pose) {
        float pattern = 1.0f;
        float lively = 0.6f;
        float breath = 1.0f;
        float aurora = 1.0f;
        if (c != null) {
            pattern = Math.max(0.2f, Math.min(1.5f, c.skyPattern));
            lively = Math.max(0.0f, Math.min(1.0f, c.skyLively));
            breath = c.skyBreath <= 0.01f ? 1.0f : 0.85f + 0.15f * (float)Math.sin(t * 0.45 * (double)Math.max(0.2f, c.skyBreath));
            aurora = c.fxAurora ? 1.0f : 0.0f;
        }
        LiveSkyShader.next[0] = LiveSkyShader.rgba(p.top, 255);
        LiveSkyShader.next[1] = LiveSkyShader.rgba(p.horizon, 255);
        LiveSkyShader.next[2] = LiveSkyShader.rgba(a1, 255);
        LiveSkyShader.next[3] = LiveSkyShader.rgba(a2, 255);
        LiveSky.Body b = LiveSky.body(p);
        LiveSkyShader.next[4] = b == null ? 0 : LiveSkyShader.rgba(b.color(), b.kind() + 1);
        LiveSkyShader.next[5] = LiveSkyShader.bytes(LiveSkyShader.b8(pattern / 1.5f), LiveSkyShader.b8(lively), LiveSkyShader.b8(breath), LiveSkyShader.b8(aurora));
        LiveSkyShader.next[6] = b == null ? 0 : LiveSkyShader.pair(b.az() * 100.0, (b.el() + 90.0) * 100.0);
        LiveSkyShader.next[7] = (b == null ? 0 : LiveSkyShader.pair(b.radius() * 1000.0, 0.0) & 0xFFFF0000) | p.ordinal() << 8;
        Matrix3f inv = new Matrix3f().set((Matrix4fc)pose).invert();
        float[] m = new float[]{inv.m00, inv.m01, inv.m02, inv.m10, inv.m11, inv.m12, inv.m20, inv.m21, inv.m22, 0.0f};
        for (int i = 0; i < 5; ++i) {
            LiveSkyShader.next[8 + i] = LiveSkyShader.pair(LiveSkyShader.enc(m[i * 2]), LiveSkyShader.enc(m[i * 2 + 1]));
        }
        boolean changed = false;
        for (int i = 0; i < 16; ++i) {
            if (next[i] == last[i]) continue;
            LiveSkyShader.last[i] = next[i];
            changed = true;
            int v = next[i];
            tex.getPixels().setPixel(i, 0, (v & 0xFF) << 24 | v >>> 8);
        }
        if (changed) {
            tex.upload();
        }
    }

    private static int pair(double v1, double v2) {
        int a = (int)Math.max(0L, Math.min(65535L, Math.round(v1)));
        int b = (int)Math.max(0L, Math.min(65535L, Math.round(v2)));
        return a << 16 | b;
    }

    private static double enc(float v) {
        return ((double)Math.max(-2.0f, Math.min(2.0f, v)) + 2.0) / 4.0 * 65535.0;
    }

    private static int b8(float v) {
        return Math.max(0, Math.min(255, Math.round(v * 255.0f)));
    }

    private static int rgba(int rgb, int a) {
        return (rgb & 0xFFFFFF) << 8 | a & 0xFF;
    }

    private static int bytes(int r, int g, int b, int a) {
        return r << 24 | g << 16 | b << 8 | a;
    }

    static {
        state = 0;
        last = new int[16];
        next = new int[16];
    }
}

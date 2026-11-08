package dev.lyricsflow.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.lyricsflow.LyricsFlow;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.config.LFConfig;
import dev.lyricsflow.mixin.PostChainAccessor;
import dev.lyricsflow.mixin.PostPassAccessor;
import dev.lyricsflow.render.Hud;
import dev.lyricsflow.render.WorldLyrics;
import dev.lyricsflow.util.Anim;
import java.nio.ByteBuffer;
import java.util.Map;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_279;
import net.minecraft.class_283;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_9922;
import net.minecraft.class_9960;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;

public final class Chroma {
    public static final String[] COLORS = new String[]{"lilac", "black", "grey", "purple", "pink", "blue", "red", "gold"};
    private static final float[][] LOOK = new float[][]{{0.92f, 0.885f, 0.92f, 0.04f, 0.035f, 0.1f, 0.8f}, {0.28f, 0.28f, 0.31f, 0.0f, 0.0f, 0.0f, 1.0f}, {1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f}, {0.85f, 0.62f, 1.15f, 0.0f, 0.0f, 0.0f, 1.0f}, {1.15f, 0.66f, 0.95f, 0.0f, 0.0f, 0.0f, 1.0f}, {0.62f, 0.82f, 1.18f, 0.0f, 0.0f, 0.0f, 1.0f}, {1.15f, 0.55f, 0.55f, 0.0f, 0.0f, 0.0f, 1.0f}, {1.15f, 0.95f, 0.55f, 0.0f, 0.0f, 0.0f, 1.0f}};
    private static final float RADIUS = 6.0f;
    private static final float START = 70.0f;
    private static boolean failed;
    private static float radius;
    private static float strength;
    private static boolean wasOn;
    private static GpuBuffer ubo;
    private static class_279 hooked;
    private static final Matrix4f VIEW;
    private static final Matrix4f PROJ;
    private static final Matrix4f INV;
    private static double camX;
    private static double camY;
    private static double camZ;
    private static boolean haveMatrices;
    private static GpuBufferSlice FOG;
    public static boolean deferred;

    private Chroma() {
    }

    public static boolean drawsLate() {
        return strength > 0.0f && !failed;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void drawGhost() {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1724 == null || !haveMatrices) {
            return;
        }
        Matrix4fStack mv = RenderSystem.getModelViewStack();
        GpuBufferSlice oldFog = RenderSystem.getShaderFog();
        mv.pushMatrix();
        mv.set((Matrix4fc)VIEW);
        try {
            if (FOG != null) {
                RenderSystem.setShaderFog((GpuBufferSlice)FOG);
            }
            class_4597.class_4598 buffers = mc.method_22940().method_23000();
            WorldLyrics.renderGhost(new class_4587(), (class_4597)buffers);
            buffers.method_22993();
        }
        catch (Throwable t) {
            LyricsFlow.LOG.debug("ghost lyrics draw failed", t);
        }
        finally {
            mv.popMatrix();
            if (oldFog != null) {
                RenderSystem.setShaderFog((GpuBufferSlice)oldFog);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void drawLate() {
        deferred = false;
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1724 == null || !haveMatrices) {
            return;
        }
        Matrix4fStack mv = RenderSystem.getModelViewStack();
        GpuBufferSlice oldFog = RenderSystem.getShaderFog();
        mv.pushMatrix();
        mv.set((Matrix4fc)VIEW);
        try {
            if (FOG != null) {
                RenderSystem.setShaderFog((GpuBufferSlice)FOG);
            }
            class_4597.class_4598 buffers = mc.method_22940().method_23000();
            Hud.renderWorldSky(new class_4587(), (class_4597)buffers);
            WorldLyrics.renderWorld(new class_4587(), (class_4597)buffers);
            buffers.method_22993();
        }
        catch (Throwable t) {
            LyricsFlow.LOG.debug("late lyrics draw failed", t);
        }
        finally {
            mv.popMatrix();
            if (oldFog != null) {
                RenderSystem.setShaderFog((GpuBufferSlice)oldFog);
            }
        }
    }

    public static void capture(class_4184 camera, Matrix4f view, Matrix4f projection, GpuBufferSlice fog) {
        FOG = fog;
        if (camera == null || view == null || projection == null) {
            return;
        }
        VIEW.set((Matrix4fc)view);
        PROJ.set((Matrix4fc)projection);
        class_243 p = camera.method_71156();
        camX = p.field_1352;
        camY = p.field_1351;
        camZ = p.field_1350;
        haveMatrices = true;
    }

    public static void apply(class_9922 pool) {
        boolean on;
        LFConfig c = ConfigManager.get();
        class_310 mc = class_310.method_1551();
        boolean bl = on = c.enabled && c.chroma && !failed && mc.field_1687 != null && mc.field_1724 != null;
        if (on && !wasOn) {
            radius = 70.0f;
            strength = 1.0f;
        }
        wasOn = on;
        float dt = Anim.dt();
        if (on) {
            radius += (6.0f - radius) * (1.0f - (float)Math.exp(-dt * 2.2f));
            strength = 1.0f;
        } else if ((radius += (70.0f - radius) * (1.0f - (float)Math.exp(-dt * 2.5f))) > 68.0f) {
            strength = 0.0f;
        }
        if (strength <= 0.0f || failed || !haveMatrices || mc.field_1687 == null || mc.field_1724 == null) {
            return;
        }
        try {
            class_279 chain = mc.method_62887().method_62941(LyricsFlow.id("chroma"), class_9960.field_53902);
            if (chain == null) {
                return;
            }
            if (chain != hooked || ubo == null || ubo.isClosed()) {
                Chroma.hook(chain);
            }
            if (ubo == null) {
                return;
            }
            Chroma.writeUniforms(mc, c);
            chain.method_1258(mc.method_1522(), pool);
        }
        catch (Throwable t) {
            failed = true;
            LyricsFlow.LOG.warn("Chroma effect could not run, turning it off for this session", t);
        }
    }

    private static void hook(class_279 chain) {
        hooked = chain;
        for (class_283 pass : ((PostChainAccessor)chain).lyricsflow$passes()) {
            GpuBuffer old;
            Map<String, GpuBuffer> map = ((PostPassAccessor)pass).lyricsflow$uniforms();
            if (!map.containsKey("ChromaConfig") || (old = map.put("ChromaConfig", ubo = RenderSystem.getDevice().createBuffer(() -> "SkyLyrics Chroma", 136, 256L))) == null || old == ubo) continue;
            old.close();
        }
    }

    private static void writeUniforms(class_310 mc, LFConfig c) {
        float pt = mc.method_61966().method_60637(false);
        class_1297 e = mc.method_1560() != null ? mc.method_1560() : mc.field_1724;
        class_243 pp = e.method_30950(pt);
        double bx = Math.floor(camX);
        double by = Math.floor(camY);
        double bz = Math.floor(camZ);
        INV.set((Matrix4fc)PROJ).mul((Matrix4fc)VIEW).invert();
        float[] look = LOOK[Math.max(0, Math.min(LOOK.length - 1, c.chromaColor))];
        try (MemoryStack stack = MemoryStack.stackPush();){
            Std140Builder b = Std140Builder.onStack((MemoryStack)stack, (int)256);
            b.putMat4f((Matrix4fc)INV);
            b.putVec4((float)(camX - bx), (float)(camY - by), (float)(camZ - bz), 0.0f);
            b.putVec4((float)(pp.field_1352 - bx), (float)(pp.field_1351 - by), (float)(pp.field_1350 - bz), radius);
            b.putVec4(0.5f, look[6], strength, (float)Math.floorMod((long)bx, 256L));
            b.putVec4(look[0], look[1], look[2], (float)Math.floorMod((long)by, 256L));
            b.putVec4(look[3], look[4], look[5], (float)Math.floorMod((long)bz, 256L));
            ByteBuffer data = b.get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(ubo.slice(0L, (long)data.remaining()), data);
        }
    }

    static {
        radius = 70.0f;
        VIEW = new Matrix4f();
        PROJ = new Matrix4f();
        INV = new Matrix4f();
    }
}


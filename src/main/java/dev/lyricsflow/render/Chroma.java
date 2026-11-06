/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.renderpearl.api.buffers.GpuBuffer
 *  com.mojang.renderpearl.api.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.buffers.Std140Builder
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.client.renderer.PostChain
 *  net.minecraft.client.renderer.PostPass
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.Camera
 *  com.mojang.blaze3d.vertex.PoseStack
 *  dev.lyricsflow.compat.LegacyBufferSource
 *  dev.lyricsflow.compat.LegacyBufferSource$BufferSource
 *  com.mojang.blaze3d.resource.GraphicsResourceAllocator
 *  net.minecraft.client.renderer.LevelTargetBundle
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 *  org.lwjgl.system.MemoryStack
 */
package dev.lyricsflow.render;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.lyricsflow.SkyLyrics;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.config.LFConfig;
import dev.lyricsflow.mixin.PostChainAccessor;
import dev.lyricsflow.mixin.PostPassAccessor;
import dev.lyricsflow.render.Hud;
import dev.lyricsflow.render.WorldLyrics;
import dev.lyricsflow.util.Anim;
import java.nio.ByteBuffer;
import java.util.Map;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.lyricsflow.compat.LegacyBufferSource;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.renderer.LevelTargetBundle;
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
    private static PostChain hooked;
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
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || !haveMatrices) {
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
            LegacyBufferSource.BufferSource buffers = LegacyBufferSource.current();
            WorldLyrics.renderGhost(new PoseStack(), (LegacyBufferSource)buffers);
            buffers.endBatch();
        }
        catch (Throwable t) {
            SkyLyrics.LOG.debug("ghost lyrics draw failed", t);
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
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || !haveMatrices) {
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
            LegacyBufferSource.BufferSource buffers = LegacyBufferSource.current();
            Hud.renderWorldSky(new PoseStack(), (LegacyBufferSource)buffers);
            WorldLyrics.renderWorld(new PoseStack(), (LegacyBufferSource)buffers);
            buffers.endBatch();
        }
        catch (Throwable t) {
            SkyLyrics.LOG.debug("late lyrics draw failed", t);
        }
        finally {
            mv.popMatrix();
            if (oldFog != null) {
                RenderSystem.setShaderFog((GpuBufferSlice)oldFog);
            }
        }
    }

    public static void capture(Camera camera, Matrix4f view, Matrix4f projection, GpuBufferSlice fog) {
        FOG = fog;
        if (camera == null || view == null || projection == null) {
            return;
        }
        VIEW.set((Matrix4fc)view);
        PROJ.set((Matrix4fc)projection);
        Vec3 p = camera.position();
        camX = p.x;
        camY = p.y;
        camZ = p.z;
        haveMatrices = true;
    }

    public static void apply(GraphicsResourceAllocator pool) {
        boolean on;
        LFConfig c = ConfigManager.get();
        Minecraft mc = Minecraft.getInstance();
        boolean bl = on = c.enabled && c.chroma && !failed && mc.level != null && mc.player != null;
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
        if (strength <= 0.0f || failed || !haveMatrices || mc.level == null || mc.player == null) {
            return;
        }
        try {
            PostChain chain = mc.getShaderManager().getPostChain(SkyLyrics.id("chroma"), LevelTargetBundle.MAIN_TARGETS);
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
            chain.process(mc.gameRenderer.mainRenderTarget(), pool);
        }
        catch (Throwable t) {
            failed = true;
            SkyLyrics.LOG.warn("Chroma effect could not run, turning it off for this session", t);
        }
    }

    private static void hook(PostChain chain) {
        hooked = chain;
        for (PostPass pass : ((PostChainAccessor)chain).lyricsflow$passes()) {
            GpuBuffer old;
            Map<String, GpuBuffer> map = ((PostPassAccessor)pass).lyricsflow$uniforms();
            if (!map.containsKey("ChromaConfig") || (old = map.put("ChromaConfig", ubo = RenderSystem.getDevice().createBuffer(() -> "SkyLyrics Chroma", 136, 256L))) == null || old == ubo) continue;
            old.close();
        }
    }

    private static void writeUniforms(Minecraft mc, LFConfig c) {
        float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Entity e = mc.getCameraEntity() != null ? mc.getCameraEntity() : mc.player;
        Vec3 pp = e.getPosition(pt);
        double bx = Math.floor(camX);
        double by = Math.floor(camY);
        double bz = Math.floor(camZ);
        INV.set((Matrix4fc)PROJ).mul((Matrix4fc)VIEW).invert();
        float[] look = LOOK[Math.max(0, Math.min(LOOK.length - 1, c.chromaColor))];
        try (MemoryStack stack = MemoryStack.stackPush();){
            Std140Builder b = Std140Builder.onStack((MemoryStack)stack, (int)256);
            b.putMat4f((Matrix4fc)INV);
            b.putVec4((float)(camX - bx), (float)(camY - by), (float)(camZ - bz), 0.0f);
            b.putVec4((float)(pp.x - bx), (float)(pp.y - by), (float)(pp.z - bz), radius);
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

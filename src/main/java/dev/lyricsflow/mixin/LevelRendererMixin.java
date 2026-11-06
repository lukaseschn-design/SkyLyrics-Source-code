/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.renderpearl.api.buffers.GpuBufferSlice
 *  net.minecraft.client.Camera
 *  net.minecraft.client.renderer.LevelRenderer
 *  net.minecraft.client.DeltaTracker
 *  com.mojang.blaze3d.resource.GraphicsResourceAllocator
 *  org.joml.Matrix4f
 *  org.joml.Vector4f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.lyricsflow.mixin;

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.render.Chroma;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.DeltaTracker;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LevelRenderer.class})
public abstract class LevelRendererMixin {
    @Inject(method={"renderLevel"}, at={@At(value="HEAD")}, require=0)
    private void lyricsflow$matrices(GraphicsResourceAllocator allocator, DeltaTracker deltaTracker, boolean outline, Camera camera, Matrix4f view, Matrix4f projection, Matrix4f cullProjection, GpuBufferSlice fog, Vector4f fogColor, boolean sky, CallbackInfo ci) {
        if (ConfigManager.get().enabled) {
            Chroma.capture(camera, view, projection, fog);
        }
    }
}


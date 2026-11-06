/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Camera
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.client.DeltaTracker
 *  com.mojang.blaze3d.resource.CrossFrameResourcePool
 *  com.mojang.blaze3d.resource.GraphicsResourceAllocator
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.lyricsflow.mixin;

import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.render.Chroma;
import dev.lyricsflow.render.Proj;
import dev.lyricsflow.render.ScreenshotHelper;
import dev.lyricsflow.util.Anim;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.DeltaTracker;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={GameRenderer.class})
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private CrossFrameResourcePool resourcePool;

    @Inject(method={"renderLevel"}, at={@At(value="INVOKE", target="Lnet/minecraft/LevelRenderer;renderLevel(Lnet/minecraft/GraphicsResourceAllocator;Lnet/minecraft/DeltaTracker;ZLnet/minecraft/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V", shift=At.Shift.AFTER)}, require=0)
    private void lyricsflow$chroma(DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!ConfigManager.get().enabled) {
            return;
        }
        Chroma.apply((GraphicsResourceAllocator)this.resourcePool);
        if (Chroma.deferred) {
            Chroma.drawLate();
        }
        Chroma.drawGhost();
    }

    @Inject(method={"getFov"}, at={@At(value="RETURN")}, require=0)
    private void lyricsflow$captureFov(Camera camera, float partialTick, boolean useFovSetting, CallbackInfoReturnable<Float> cir) {
        if (useFovSetting) {
            Proj.fovFromRenderer = cir.getReturnValueF();
        }
    }

    @Inject(method={"render"}, at={@At(value="HEAD")}, require=0)
    private void lyricsflow$frameStart(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        Anim.beginFrame();
    }

    @Inject(method={"render"}, at={@At(value="TAIL")}, require=0)
    private void lyricsflow$afterRender(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        ScreenshotHelper.afterFrame();
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Camera
 *  net.minecraft.world.level.material.FogType
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.fog.FogRenderer
 *  org.joml.Vector4f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package dev.lyricsflow.mixin;

import dev.lyricsflow.app.sky.WorldSky;
import dev.lyricsflow.config.ConfigManager;
import net.minecraft.client.Camera;
import net.minecraft.world.level.material.FogType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={FogRenderer.class})
public abstract class FogRendererMixin {
    @Inject(method={"computeFogColor"}, at={@At(value="RETURN")}, cancellable=true, require=0)
    private void lyricsflow$skyFog(Camera camera, float pt, ClientLevel level, int renderDistance, float darken, CallbackInfoReturnable<Vector4f> cir) {
        Vector4f v;
        if (camera.getFluidInCamera() == FogType.NONE && ConfigManager.get().enabled && (v = (Vector4f)cir.getReturnValue()) != null && WorldSky.tintFog(v)) {
            cir.setReturnValue(v);
        }
    }
}

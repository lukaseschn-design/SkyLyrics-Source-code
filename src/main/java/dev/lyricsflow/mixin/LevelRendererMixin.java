package dev.lyricsflow.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.render.Chroma;
import net.minecraft.class_4184;
import net.minecraft.class_761;
import net.minecraft.class_9779;
import net.minecraft.class_9922;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_761.class})
public abstract class LevelRendererMixin {
    @Inject(method={"method_22710"}, at={@At(value="HEAD")}, require=0)
    private void lyricsflow$matrices(class_9922 allocator, class_9779 deltaTracker, boolean outline, class_4184 camera, Matrix4f view, Matrix4f projection, Matrix4f cullProjection, GpuBufferSlice fog, Vector4f fogColor, boolean sky, CallbackInfo ci) {
        if (ConfigManager.get().enabled) {
            Chroma.capture(camera, view, projection, fog);
        }
    }
}


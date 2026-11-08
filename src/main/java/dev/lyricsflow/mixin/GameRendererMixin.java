package dev.lyricsflow.mixin;

import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.render.Chroma;
import dev.lyricsflow.render.Proj;
import dev.lyricsflow.render.ScreenshotHelper;
import dev.lyricsflow.util.Anim;
import net.minecraft.class_4184;
import net.minecraft.class_757;
import net.minecraft.class_9779;
import net.minecraft.class_9920;
import net.minecraft.class_9922;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_757.class})
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private class_9920 field_53066;

    @Inject(method={"method_3188"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_761;method_22710(Lnet/minecraft/class_9922;Lnet/minecraft/class_9779;ZLnet/minecraft/class_4184;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V", shift=At.Shift.AFTER)}, require=0)
    private void lyricsflow$chroma(class_9779 deltaTracker, CallbackInfo ci) {
        if (!ConfigManager.get().enabled) {
            return;
        }
        Chroma.apply((class_9922)this.field_53066);
        if (Chroma.deferred) {
            Chroma.drawLate();
        }
        Chroma.drawGhost();
    }

    @Inject(method={"method_3196"}, at={@At(value="RETURN")}, require=0)
    private void lyricsflow$captureFov(class_4184 camera, float partialTick, boolean useFovSetting, CallbackInfoReturnable<Float> cir) {
        if (useFovSetting) {
            Proj.fovFromRenderer = cir.getReturnValueF();
        }
    }

    @Inject(method={"method_3192"}, at={@At(value="HEAD")}, require=0)
    private void lyricsflow$frameStart(class_9779 deltaTracker, boolean renderLevel, CallbackInfo ci) {
        Anim.beginFrame();
    }

    @Inject(method={"method_3192"}, at={@At(value="TAIL")}, require=0)
    private void lyricsflow$afterRender(class_9779 deltaTracker, boolean renderLevel, CallbackInfo ci) {
        ScreenshotHelper.afterFrame();
    }
}


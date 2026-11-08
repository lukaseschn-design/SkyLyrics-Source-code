package Yuki.lumiclient.mixin;

import Yuki.lumiclient.util.UiSounds;
import net.minecraft.class_1144;
import net.minecraft.class_339;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_339.class)
public abstract class ClickableWidgetMixin {
    @Inject(method = "method_25354(Lnet/minecraft/class_1144;)V", at = @At("HEAD"), cancellable = true)
    private void lotus$playCustomClickSound(class_1144 soundManager, CallbackInfo ci) {
        UiSounds.select();
        ci.cancel();
    }
}

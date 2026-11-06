package Yuki.lumiclient.mixin;

import Yuki.lumiclient.render.OverlayRenderer;
import net.minecraft.class_757;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_757.class)
public class GameRendererMixin {
    @Inject(method = "method_3192", at = @At("TAIL"))
    private void lumiclient$renderOverlay(class_9779 deltaTracker, boolean bl, CallbackInfo ci) {
        OverlayRenderer.render();
    }
}

package Yuki.lumiclient.mixin;

import Yuki.lumiclient.gui.LotusLoadingOverlay;
import net.minecraft.class_11653; // WorldLoadingTracker in 1.21.11
import net.minecraft.class_332; // DrawContext
import net.minecraft.class_3532; // MathHelper
import net.minecraft.class_3928; // LevelLoadingScreen
import net.minecraft.class_437; // Screen
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {class_3928.class})
public abstract class LevelLoadingScreenMixin extends class_437 {

    protected LevelLoadingScreenMixin() {
        super(null);
    }

    @Shadow private class_11653 field_61631;

    @Inject(method = "method_25394(Lnet/minecraft/class_332;IIF)V", at = @At("HEAD"), cancellable = true)
    private void lotus$renderCustomLevelLoading(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int screenW = context.method_51421();
        int screenH = context.method_51443();

        float progress = -1.0f;
        String status = "Loading terrain...";

        if (this.field_61631 != null && this.field_61631.method_72906()) {
            float p = this.field_61631.method_72905();
            progress = class_3532.method_15363(p > 1.0f ? p / 100.0f : p, 0.0f, 1.0f);
            int pct = Math.round(progress * 100.0f);
            status = "Loading world... (" + pct + "%)";
        }

        LotusLoadingOverlay.render(context, screenW, screenH, progress, status, 1.0f);
        ci.cancel();
    }
}

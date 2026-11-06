package Yuki.lumiclient.mixin;

import Yuki.lumiclient.gui.LotusLoadingOverlay;
import net.minecraft.client.multiplayer.LevelLoadTracker; // WorldLoadingTracker in 1.21.11
import net.minecraft.client.gui.GuiGraphicsExtractor; // DrawContext
import net.minecraft.util.Mth; // MathHelper
import net.minecraft.client.gui.screens.LevelLoadingScreen; // LevelLoadingScreen
import net.minecraft.client.gui.screens.Screen; // Screen
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * LevelLoadingScreenMixin - Ersetzt den Vanilla Singleplayer & Multiplayer Terrain-Ladebildschirm
 * durch den eleganten Lotus Ladebildschirm mit dem neuen Lotus-Emblem,
 * Glow-Effekt und animierter Fortschrittsanzeige.
 */
@Mixin(value = {LevelLoadingScreen.class})
public abstract class LevelLoadingScreenMixin extends Screen {

    protected LevelLoadingScreenMixin() {
        super(null);
    }

    @Shadow private LevelLoadTracker loadTracker;

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", at = @At("HEAD"), cancellable = true)
    private void lotus$renderCustomLevelLoading(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int screenW = context.guiWidth();
        int screenH = context.guiHeight();

        float progress = -1.0f;
        String status = "Loading terrain...";

        if (this.loadTracker != null && this.loadTracker.hasProgress()) {
            float p = this.loadTracker.serverProgress();
            progress = Mth.clamp(p > 1.0f ? p / 100.0f : p, 0.0f, 1.0f);
            int pct = Math.round(progress * 100.0f);
            status = "Loading world... (" + pct + "%)";
        }

        LotusLoadingOverlay.render(context, screenW, screenH, progress, status, 1.0f);
        ci.cancel();
    }
}

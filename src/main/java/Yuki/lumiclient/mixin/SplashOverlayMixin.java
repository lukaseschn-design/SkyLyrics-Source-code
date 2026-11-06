package Yuki.lumiclient.mixin;

import com.mojang.blaze3d.platform.NativeImage; // NativeImage
import net.minecraft.client.renderer.texture.DynamicTexture; // NativeImageBackedTexture
import net.minecraft.client.renderer.RenderPipelines; // RenderPipelines
import net.minecraft.util.Util; // Util
import net.minecraft.resources.Identifier; // Identifier
import net.minecraft.client.Minecraft; // MinecraftClient
import net.minecraft.client.gui.GuiGraphicsExtractor; // DrawContext
import net.minecraft.util.Mth; // MathHelper
import net.minecraft.server.packs.resources.ReloadInstance; // ResourceReload
import net.minecraft.client.gui.screens.LoadingOverlay; // SplashOverlay
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import Yuki.lumiclient.gui.LotusLoadingOverlay;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * SplashOverlayMixin - 1:1 Aurora-Style Minimalist Custom Loading Screen für Lotus Client.
 *
 * Zeigt das leuchtende Lotus-Logo zentriert auf edlem dunklem Hintergrund
 * mit einer animierten, farbverlaufenden Fortschrittsleiste (Violett -> Cyan).
 */
@Mixin(value = {LoadingOverlay.class})
public abstract class SplashOverlayMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private ReloadInstance reload;
    @Shadow @Final private Consumer<Optional<Throwable>> onFinish;
    @Shadow @Final private boolean fadeIn;
    @Shadow private float currentProgress;
    @Shadow private long fadeOutStart;
    @Shadow private long fadeInStart;

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", at = @At("HEAD"), cancellable = true)
    private void lotus$renderCustomSplash(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        try {
            int screenW = context.guiWidth();
            int screenH = context.guiHeight();
            long now = Util.getMillis();

            if (this.fadeIn && this.fadeInStart == -1L) {
                this.fadeInStart = now;
            }

            float reloadCompleteElapsed = this.fadeOutStart > -1L ? (float)(now - this.fadeOutStart) / 1000.0f : -1.0f;
            float reloadStartElapsed = this.fadeInStart > -1L ? (float)(now - this.fadeInStart) / 500.0f : -1.0f;

            float alpha;
            if (reloadCompleteElapsed >= 1.0f) {
                if (this.minecraft.gui.screen() != null) {
                    this.minecraft.gui.screen().extractRenderState(context, 0, 0, delta);
                }
                context.nextStratum();
                alpha = 1.0f - Mth.clamp(reloadCompleteElapsed - 1.0f, 0.0f, 1.0f);
            } else if (this.fadeIn) {
                if (this.minecraft.gui.screen() != null && reloadStartElapsed < 1.0f) {
                    this.minecraft.gui.screen().extractRenderState(context, mouseX, mouseY, delta);
                }
                context.nextStratum();
                alpha = Mth.clamp(reloadStartElapsed, 0.0f, 1.0f);
            } else {
                alpha = 1.0f;
            }

            float reloadProgress = this.reload.getActualProgress();
            this.currentProgress = Mth.clamp(this.currentProgress * 0.95f + reloadProgress * 0.05f, 0.0f, 1.0f);

            LotusLoadingOverlay.render(context, screenW, screenH, this.currentProgress, (String) null, alpha);

            if (reloadCompleteElapsed >= 2.0f) {
                this.minecraft.gui.setOverlay(null);
            }

            if (this.fadeOutStart == -1L && this.reload.isDone() && (!this.fadeIn || reloadStartElapsed >= 2.0f)) {
                try {
                    this.reload.checkExceptions();
                    this.onFinish.accept(Optional.empty());
                } catch (Throwable throwable) {
                    this.onFinish.accept(Optional.of(throwable));
                }
                this.fadeOutStart = Util.getMillis();
                if (this.minecraft.gui.screen() != null) {
                    this.minecraft.gui.screen().init(screenW, screenH);
                }
            }

            ci.cancel();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}

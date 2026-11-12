package Yuki.lumiclient.mixin;

import net.minecraft.class_1011; // NativeImage
import net.minecraft.class_1043; // NativeImageBackedTexture
import net.minecraft.class_10799; // RenderPipelines
import net.minecraft.class_156; // Util
import net.minecraft.class_2960; // Identifier
import net.minecraft.class_310; // MinecraftClient
import net.minecraft.class_332; // DrawContext
import net.minecraft.class_3532; // MathHelper
import net.minecraft.class_4011; // ResourceReload
import net.minecraft.class_425; // SplashOverlay
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

@Mixin(value = {class_425.class})
public abstract class SplashOverlayMixin {

    @Shadow @Final private class_310 field_18217;
    @Shadow @Final private class_4011 field_17767;
    @Shadow @Final private Consumer<Optional<Throwable>> field_18218;
    @Shadow @Final private boolean field_18219;
    @Shadow private float field_17770;
    @Shadow private long field_17771;
    @Shadow private long field_18220;

    @Inject(method = "method_25394(Lnet/minecraft/class_332;IIF)V", at = @At("HEAD"), cancellable = true)
    private void lotus$renderCustomSplash(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        try {
            int screenW = context.method_51421();
            int screenH = context.method_51443();
            long now = class_156.method_658();

            if (this.field_18219 && this.field_18220 == -1L) {
                this.field_18220 = now;
            }

            float reloadCompleteElapsed = this.field_17771 > -1L ? (float)(now - this.field_17771) / 1000.0f : -1.0f;
            float reloadStartElapsed = this.field_18220 > -1L ? (float)(now - this.field_18220) / 500.0f : -1.0f;

            float alpha;
            if (reloadCompleteElapsed >= 1.0f) {
                if (this.field_18217.field_1755 != null) {
                    this.field_18217.field_1755.method_25394(context, 0, 0, delta);
                } else if (this.field_18217.field_1705 != null) {
                    this.field_18217.field_1705.method_72738();
                }
                context.method_71048();
                alpha = 1.0f - class_3532.method_15363(reloadCompleteElapsed - 1.0f, 0.0f, 1.0f);
            } else if (this.field_18219) {
                if (this.field_18217.field_1755 != null && reloadStartElapsed < 1.0f) {
                    this.field_18217.field_1755.method_25394(context, mouseX, mouseY, delta);
                } else if (this.field_18217.field_1705 != null) {
                    this.field_18217.field_1705.method_72738();
                }
                context.method_71048();
                alpha = class_3532.method_15363(reloadStartElapsed, 0.0f, 1.0f);
            } else {
                alpha = 1.0f;
            }

            float reloadProgress = this.field_17767.method_18229();
            this.field_17770 = class_3532.method_15363(this.field_17770 * 0.95f + reloadProgress * 0.05f, 0.0f, 1.0f);

            LotusLoadingOverlay.render(context, screenW, screenH, this.field_17770, (String) null, alpha);

            if (reloadCompleteElapsed >= 2.0f) {
                this.field_18217.method_18502(null);
            }

            if (this.field_17771 == -1L && this.field_17767.method_18787() && (!this.field_18219 || reloadStartElapsed >= 2.0f)) {
                try {
                    this.field_17767.method_18849();
                    this.field_18218.accept(Optional.empty());
                } catch (Throwable throwable) {
                    this.field_18218.accept(Optional.of(throwable));
                }
                this.field_17771 = class_156.method_658();
                if (this.field_18217.field_1755 != null) {
                    this.field_18217.field_1755.method_25423(screenW, screenH);
                }
            }

            ci.cancel();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}

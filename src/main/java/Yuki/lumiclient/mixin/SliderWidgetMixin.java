package Yuki.lumiclient.mixin;

import Yuki.lumiclient.gui.LotusButtonRenderer;
import net.minecraft.class_332;
import net.minecraft.class_357;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * SliderWidgetMixin - Ersetzt das Standard Vanilla-Slider-Rendering (FOV, Lautstärke, Grafikoptionen etc.)
 * durch den modernen Lotus Aurora Frosted-Glass Pill Slider.
 */
@Mixin(class_357.class)
public abstract class SliderWidgetMixin {

    @Shadow
    protected double field_22753;

    @Inject(method = "method_48579(Lnet/minecraft/class_332;IIF)V", at = @At("HEAD"), cancellable = true)
    private void lotus$renderSliderWidget(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        LotusButtonRenderer.renderSlider((class_357) (Object) this, context, mouseX, mouseY, delta, this.field_22753);
        ci.cancel();
    }
}

package Yuki.lumiclient.mixin;

import Yuki.lumiclient.gui.LotusButtonRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * SliderWidgetMixin - Ersetzt das Standard Vanilla-Slider-Rendering (FOV, Lautstärke, Grafikoptionen etc.)
 * durch den modernen Lotus Aurora Frosted-Glass Pill Slider.
 */
@Mixin(AbstractSliderButton.class)
public abstract class SliderWidgetMixin {

    @Shadow
    protected double value;

    @Inject(method = "extractWidgetRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", at = @At("HEAD"), cancellable = true)
    private void lotus$renderSliderWidget(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        LotusButtonRenderer.renderSlider((AbstractSliderButton) (Object) this, context, mouseX, mouseY, delta, this.value);
        ci.cancel();
    }
}

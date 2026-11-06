package Yuki.lumiclient.mixin;

import Yuki.lumiclient.gui.LotusButtonRenderer;
import Yuki.lumiclient.keybinds.SmoothButton;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Checkbox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * PressableWidgetMixin - Ersetzt das Standard Vanilla-Button-Rendering aller Buttons
 * (ButtonWidget, CyclingButtonWidget, etc.) durch das Lotus Frosted-Glass Pill Design.
 */
@Mixin(AbstractButton.class)
public abstract class PressableWidgetMixin {

    @Inject(method = "extractWidgetRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", at = @At("HEAD"), cancellable = true)
    private void lotus$renderPressableWidget(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (((Object) this) instanceof Checkbox) {
            // CheckboxWidget: Checkboxen unberührt lassen
            return;
        }
        if (((Object) this) instanceof SmoothButton) {
            // SmoothButton behält sein Keybinds-Layout
            return;
        }

        LotusButtonRenderer.renderButton((AbstractButton) (Object) this, context, mouseX, mouseY, delta);
        ci.cancel();
    }
}

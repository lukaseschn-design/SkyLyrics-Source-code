package Yuki.lumiclient.mixin;

import Yuki.lumiclient.gui.LotusButtonRenderer;
import Yuki.lumiclient.keybinds.SmoothButton;
import net.minecraft.class_332;
import net.minecraft.class_4264;
import net.minecraft.class_4286;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * PressableWidgetMixin - Ersetzt das Standard Vanilla-Button-Rendering aller Buttons
 * (ButtonWidget, CyclingButtonWidget, etc.) durch das Lotus Frosted-Glass Pill Design.
 */
@Mixin(class_4264.class)
public abstract class PressableWidgetMixin {

    @Inject(method = "method_48579(Lnet/minecraft/class_332;IIF)V", at = @At("HEAD"), cancellable = true)
    private void lotus$renderPressableWidget(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (((Object) this) instanceof class_4286) {
            // CheckboxWidget: Checkboxen unberührt lassen
            return;
        }
        if (((Object) this) instanceof SmoothButton) {
            // SmoothButton behält sein Keybinds-Layout
            return;
        }

        LotusButtonRenderer.renderButton((class_4264) (Object) this, context, mouseX, mouseY, delta);
        ci.cancel();
    }
}

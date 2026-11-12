package Yuki.lumiclient.mixin;

import Yuki.lumiclient.gui.LotusBackdrop;
import Yuki.lumiclient.gui.LotusTitleScreen;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {class_437.class})
public abstract class ScreenMixin {

    @Shadow @Final protected class_310 field_22787;
    @Shadow public int field_22789;
    @Shadow public int field_22790;

    @Inject(method = "method_25420(Lnet/minecraft/class_332;IIF)V", at = @At("HEAD"), cancellable = true)
    private void lotus$renderMenuBackground(class_332 context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (this.field_22787 != null && this.field_22787.field_1687 == null) {
            
            if (((Object) this) instanceof LotusTitleScreen) {
                return;
            }
            LotusBackdrop.render(context, this.field_22789, this.field_22790, 1.0f);
            try {
                if (this.field_22787.field_1705 != null) {
                    this.field_22787.field_1705.method_72738();
                }
            } catch (Throwable ignored) {}
            ci.cancel();
        }
    }
}

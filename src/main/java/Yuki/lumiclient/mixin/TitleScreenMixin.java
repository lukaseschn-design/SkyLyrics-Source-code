package Yuki.lumiclient.mixin;

import Yuki.lumiclient.gui.LotusTitleScreen;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_442;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_310.class}, priority = 500)
public abstract class TitleScreenMixin {
    @Shadow
    public abstract void method_1507(class_437 screen);

    @Inject(method = "method_1507(Lnet/minecraft/class_437;)V", at = @At("HEAD"), cancellable = true)
    private void lotus$replaceTitleScreen(class_437 screen, CallbackInfo ci) {
        if (screen instanceof LotusTitleScreen) {
            return;
        }
        class_310 mc = (class_310) (Object) this;
        boolean isTitle = (screen instanceof class_442) 
            || (screen == null && mc.field_1687 == null)
            || (screen != null && screen.getClass().getName().toLowerCase().contains("title"));
        if (isTitle) {
            this.method_1507(new LotusTitleScreen());
            ci.cancel();
        }
    }
}

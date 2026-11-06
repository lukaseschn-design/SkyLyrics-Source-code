package Yuki.lumiclient.mixin;

import Yuki.lumiclient.gui.LotusTitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Gui.class, priority = 500)
public abstract class TitleScreenMixin {
    @Shadow @Final private Minecraft minecraft;

    @Shadow
    public abstract void setScreen(Screen screen);

    @Inject(method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"), cancellable = true)
    private void lotus$replaceTitleScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof LotusTitleScreen) {
            return;
        }
        boolean isTitle = (screen instanceof TitleScreen)
            || (screen == null && this.minecraft.level == null)
            || (screen != null && screen.getClass().getName().toLowerCase().contains("title"));
        if (isTitle) {
            this.setScreen(new LotusTitleScreen());
            ci.cancel();
        }
    }
}

package Yuki.lumiclient.mixin;

import Yuki.lumiclient.gui.LotusBackdrop;
import Yuki.lumiclient.gui.LotusTitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ScreenMixin - Ersetzt das Vanilla-Panorama und den Dirt-Hintergrund
 * in sämtlichen Screens (Singleplayer, Multiplayer, Options, Sub-Menüs)
 * durch den dynamisch animierten Lotus-Hintergrund, solange der Spieler
 * sich in den Menüs befindet (world == null).
 */
@Mixin(value = {Screen.class})
public abstract class ScreenMixin {

    @Shadow @Final protected Minecraft minecraft;
    @Shadow public int width;
    @Shadow public int height;

    @Inject(method = "extractMenuBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V", at = @At("HEAD"), cancellable = true)
    private void lotus$renderMenuBackground(GuiGraphicsExtractor context, CallbackInfo ci) {
        if (this.minecraft != null && this.minecraft.level == null) {
            // LotusTitleScreen übernimmt seine Hintergrund-Zeichnung selbst
            if (((Object) this) instanceof LotusTitleScreen) {
                return;
            }
            LotusBackdrop.render(context, this.width, this.height, 1.0f);
            ci.cancel();
        }
    }
}

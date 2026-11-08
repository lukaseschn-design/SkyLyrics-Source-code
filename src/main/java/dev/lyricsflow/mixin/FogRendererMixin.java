package dev.lyricsflow.mixin;

import dev.lyricsflow.app.sky.WorldSky;
import dev.lyricsflow.config.ConfigManager;
import net.minecraft.class_4184;
import net.minecraft.class_5636;
import net.minecraft.class_638;
import net.minecraft.class_758;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_758.class})
public abstract class FogRendererMixin {
    @Inject(method={"method_62185"}, at={@At(value="RETURN")}, cancellable=true, require=0)
    private void lyricsflow$skyFog(class_4184 camera, float pt, class_638 level, int renderDistance, float darken, CallbackInfoReturnable<Vector4f> cir) {
        Vector4f v;
        if (camera.method_19334() == class_5636.field_27888 && ConfigManager.get().enabled && (v = (Vector4f)cir.getReturnValue()) != null && WorldSky.tintFog(v)) {
            cir.setReturnValue(v);
        }
    }
}


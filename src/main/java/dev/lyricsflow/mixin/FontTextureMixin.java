package dev.lyricsflow.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import java.util.function.Supplier;
import net.minecraft.class_1011;
import net.minecraft.class_1044;
import net.minecraft.class_380;
import net.minecraft.class_8538;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_380.class})
public abstract class FontTextureMixin
extends class_1044 {
    @Inject(method={"<init>"}, at={@At(value="TAIL")}, require=0)
    private void lyricsflow$smooth(Supplier<String> name, class_8538 types, boolean colored, CallbackInfo ci) {
        block7: {
            try {
                String n = name.get();
                if (n == null || !n.startsWith("lyricsflow:") || n.contains("badge")) break block7;
                this.field_63613 = RenderSystem.getSamplerCache().method_75297(FilterMode.LINEAR);
                try (class_1011 blank = new class_1011(colored ? class_1011.class_1012.field_4997 : class_1011.class_1012.field_4998, 256, 256, true);){
                    RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.method_68004(), blank, 0, 0, 0, 0, 256, 256, 0, 0);
                }
            }
            catch (Throwable throwable) {
            }
        }
    }
}


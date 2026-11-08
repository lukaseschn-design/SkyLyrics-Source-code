package dev.lyricsflow.mixin;

import net.minecraft.class_757;
import net.minecraft.class_758;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_757.class})
public interface GameRendererAccessor {
    @Accessor(value="field_60793")
    public class_758 lyricsflow$fogRenderer();
}


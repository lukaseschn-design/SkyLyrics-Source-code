package dev.lyricsflow.mixin;

import net.minecraft.class_634;
import net.minecraft.class_8761;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_634.class})
public interface ClientPacketListenerAccessor {
    @Accessor(value="field_45946")
    public class_8761 lyricsflow$pingMonitor();
}


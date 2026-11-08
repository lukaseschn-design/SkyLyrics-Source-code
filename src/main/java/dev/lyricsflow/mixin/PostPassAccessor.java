package dev.lyricsflow.mixin;

import com.mojang.blaze3d.buffers.GpuBuffer;
import java.util.Map;
import net.minecraft.class_283;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_283.class})
public interface PostPassAccessor {
    @Accessor(value="field_60123")
    public Map<String, GpuBuffer> lyricsflow$uniforms();
}


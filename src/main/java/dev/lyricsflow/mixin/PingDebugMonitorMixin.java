package dev.lyricsflow.mixin;

import dev.lyricsflow.render.LivePing;
import net.minecraft.class_156;
import net.minecraft.class_2923;
import net.minecraft.class_8761;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_8761.class})
public abstract class PingDebugMonitorMixin {
    @Inject(method={"method_53487"}, at={@At(value="HEAD")}, require=0)
    private void lyricsflow$pong(class_2923 packet, CallbackInfo ci) {
        LivePing.record(class_156.method_658() - packet.comp_2201());
    }
}


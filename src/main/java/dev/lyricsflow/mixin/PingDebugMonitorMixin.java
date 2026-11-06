/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Util
 *  net.minecraft.network.protocol.ping.ClientboundPongResponsePacket
 *  net.minecraft.client.multiplayer.PingDebugMonitor
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.lyricsflow.mixin;

import dev.lyricsflow.render.LivePing;
import net.minecraft.util.Util;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.client.multiplayer.PingDebugMonitor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={PingDebugMonitor.class})
public abstract class PingDebugMonitorMixin {
    @Inject(method={"onPongReceived"}, at={@At(value="HEAD")}, require=0)
    private void lyricsflow$pong(ClientboundPongResponsePacket packet, CallbackInfo ci) {
        LivePing.record(Util.getMillis() - packet.time());
    }
}

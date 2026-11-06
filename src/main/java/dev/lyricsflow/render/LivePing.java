/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.PlayerInfo
 */
package dev.lyricsflow.render;

import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.mixin.ClientPacketListenerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;

public final class LivePing {
    private static volatile long lastMs = -1L;
    private static volatile long lastAt;
    private static float shown;
    private static long sentAt;

    private LivePing() {
    }

    public static void record(long ms) {
        if (ms < 0L || ms > 10000L) {
            return;
        }
        lastMs = ms;
        lastAt = System.currentTimeMillis();
    }

    public static void tick(Minecraft mc) {
        if (!ConfigManager.get().enabled) {
            return;
        }
        if (mc.getConnection() == null || mc.player == null) {
            shown = -1.0f;
            return;
        }
        long now = System.currentTimeMillis();
        if (now - sentAt < 1000L) {
            return;
        }
        sentAt = now;
        try {
            ((ClientPacketListenerAccessor)mc.getConnection()).lyricsflow$pingMonitor().tick();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static int get(Minecraft mc) {
        int target;
        long now = System.currentTimeMillis();
        if (lastMs >= 0L && now - lastAt < 5000L) {
            target = (int)lastMs;
        } else {
            PlayerInfo info = mc.getConnection() == null || mc.player == null ? null : mc.getConnection().getPlayerInfo(mc.player.getUUID());
            int n = target = info == null ? 0 : info.getLatency();
        }
        shown = shown < 0.0f || Math.abs((float)target - shown) > 150.0f ? (float)target : (shown += ((float)target - shown) * 0.08f);
        return Math.round(shown);
    }

    static {
        shown = -1.0f;
    }
}


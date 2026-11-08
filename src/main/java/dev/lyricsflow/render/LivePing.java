/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_640
 */
package dev.lyricsflow.render;

import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.mixin.ClientPacketListenerAccessor;
import net.minecraft.class_310;
import net.minecraft.class_640;

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

    public static void tick(class_310 mc) {
        if (!ConfigManager.get().enabled) {
            return;
        }
        if (mc.method_1562() == null || mc.field_1724 == null) {
            shown = -1.0f;
            return;
        }
        long now = System.currentTimeMillis();
        if (now - sentAt < 1000L) {
            return;
        }
        sentAt = now;
        try {
            ((ClientPacketListenerAccessor)mc.method_1562()).lyricsflow$pingMonitor().method_53486();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static int get(class_310 mc) {
        int target;
        long now = System.currentTimeMillis();
        if (lastMs >= 0L && now - lastAt < 5000L) {
            target = (int)lastMs;
        } else {
            class_640 info = mc.method_1562() == null || mc.field_1724 == null ? null : mc.method_1562().method_2871(mc.field_1724.method_5667());
            int n = target = info == null ? 0 : info.method_2959();
        }
        shown = shown < 0.0f || Math.abs((float)target - shown) > 150.0f ? (float)target : (shown += ((float)target - shown) * 0.08f);
        return Math.round(shown);
    }

    static {
        shown = -1.0f;
    }
}


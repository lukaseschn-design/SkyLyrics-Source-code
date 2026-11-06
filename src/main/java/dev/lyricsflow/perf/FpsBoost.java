/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.OptionInstance
 */
package dev.lyricsflow.perf;

import dev.lyricsflow.SkyLyrics;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.config.LFConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;

public final class FpsBoost {
    private FpsBoost() {
    }

    public static void apply(boolean on) {
        Minecraft mc = Minecraft.getInstance();
        LFConfig c = ConfigManager.get();
        try {
            if (on) {
                if (!c.fpsBoost || c.boostBefore == null || c.boostBefore.isEmpty()) {
                    c.boostBefore = FpsBoost.save(mc.options.entityShadows()) + ";" + FpsBoost.save(mc.options.particles()) + ";" + FpsBoost.save(mc.options.biomeBlendRadius()) + ";" + FpsBoost.save(mc.options.cloudStatus()) + ";" + FpsBoost.save(mc.options.enableVsync());
                }
                FpsBoost.set(mc.options.entityShadows(), "false");
                FpsBoost.set(mc.options.particles(), "MINIMAL");
                FpsBoost.set(mc.options.biomeBlendRadius(), "0");
                FpsBoost.set(mc.options.cloudStatus(), "OFF");
                FpsBoost.set(mc.options.enableVsync(), "false");
                c.quality = 0;
            } else if (c.boostBefore != null && !c.boostBefore.isEmpty()) {
                String[] v = c.boostBefore.split(";", -1);
                if (v.length >= 3) {
                    FpsBoost.set(mc.options.entityShadows(), v[0]);
                    FpsBoost.set(mc.options.particles(), v[1]);
                    FpsBoost.set(mc.options.biomeBlendRadius(), v[2]);
                }
                if (v.length >= 5) {
                    FpsBoost.set(mc.options.cloudStatus(), v[3]);
                    FpsBoost.set(mc.options.enableVsync(), v[4]);
                }
                c.boostBefore = "";
                if (c.quality == 0) {
                    c.quality = 1;
                }
            }
            mc.options.save();
        }
        catch (Exception e) {
            SkyLyrics.LOG.warn("[SkyLyrics] FPS boost could not change every setting", (Throwable)e);
        }
        c.fpsBoost = on;
        ConfigManager.save();
    }

    private static String save(OptionInstance<?> o) {
        String string;
        Object v = o.get();
        if (v instanceof Enum) {
            Enum e = (Enum)v;
            string = e.name();
        } else {
            string = String.valueOf(v);
        }
        return string;
    }

    private static void set(OptionInstance o, String value) {
        Object nv;
        Object cur = o.get();
        if (cur instanceof Boolean) {
            nv = Boolean.parseBoolean(value);
        } else if (cur instanceof Integer) {
            nv = Integer.parseInt(value);
        } else if (cur instanceof Enum) {
            Enum e = (Enum)cur;
            nv = Enum.valueOf(e.getDeclaringClass(), value);
        } else {
            return;
        }
        o.set((Object)nv);
    }
}

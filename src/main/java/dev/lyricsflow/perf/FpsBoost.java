/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_7172
 */
package dev.lyricsflow.perf;

import dev.lyricsflow.LyricsFlow;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.config.LFConfig;
import net.minecraft.class_310;
import net.minecraft.class_7172;

public final class FpsBoost {
    private FpsBoost() {
    }

    public static void apply(boolean on) {
        class_310 mc = class_310.method_1551();
        LFConfig c = ConfigManager.get();
        try {
            if (on) {
                if (!c.fpsBoost || c.boostBefore == null || c.boostBefore.isEmpty()) {
                    c.boostBefore = FpsBoost.save(mc.field_1690.method_42435()) + ";" + FpsBoost.save(mc.field_1690.method_42475()) + ";" + FpsBoost.save(mc.field_1690.method_41805()) + ";" + FpsBoost.save(mc.field_1690.method_42528()) + ";" + FpsBoost.save(mc.field_1690.method_42433());
                }
                FpsBoost.set(mc.field_1690.method_42435(), "false");
                FpsBoost.set(mc.field_1690.method_42475(), "MINIMAL");
                FpsBoost.set(mc.field_1690.method_41805(), "0");
                FpsBoost.set(mc.field_1690.method_42528(), "OFF");
                FpsBoost.set(mc.field_1690.method_42433(), "false");
                c.quality = 0;
            } else if (c.boostBefore != null && !c.boostBefore.isEmpty()) {
                String[] v = c.boostBefore.split(";", -1);
                if (v.length >= 3) {
                    FpsBoost.set(mc.field_1690.method_42435(), v[0]);
                    FpsBoost.set(mc.field_1690.method_42475(), v[1]);
                    FpsBoost.set(mc.field_1690.method_41805(), v[2]);
                }
                if (v.length >= 5) {
                    FpsBoost.set(mc.field_1690.method_42528(), v[3]);
                    FpsBoost.set(mc.field_1690.method_42433(), v[4]);
                }
                c.boostBefore = "";
                if (c.quality == 0) {
                    c.quality = 1;
                }
            }
            mc.field_1690.method_1640();
        }
        catch (Exception e) {
            LyricsFlow.LOG.warn("[SkyLyrics] FPS boost could not change every setting", (Throwable)e);
        }
        c.fpsBoost = on;
        ConfigManager.save();
    }

    private static String save(class_7172<?> o) {
        String string;
        Object v = o.method_41753();
        if (v instanceof Enum) {
            Enum e = (Enum)v;
            string = e.name();
        } else {
            string = String.valueOf(v);
        }
        return string;
    }

    private static void set(class_7172 o, String value) {
        Object nv;
        Object cur = o.method_41753();
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
        ((class_7172)o).method_41748(nv);
    }
}


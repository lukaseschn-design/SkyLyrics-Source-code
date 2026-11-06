/*
 * Decompiled with CFR 0.152.
 */
package dev.lyricsflow.perf;

import dev.lyricsflow.app.Core;
import dev.lyricsflow.app.config.Config;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.config.LFConfig;
import dev.lyricsflow.perf.AutoPerf;
import dev.lyricsflow.perf.FpsBoost;

public final class Profiles {
    private Profiles() {
    }

    public static void set(int profile) {
        LFConfig c = ConfigManager.get();
        boolean on = profile == 0;
        Profiles.setAll(c, on);
        c.profile = profile;
        ConfigManager.save();
    }

    public static void setAll(LFConfig c, boolean on) {
        if (!on && !"off".equals(c.skyPreset)) {
            c.lastSky = c.skyPreset;
        }
        if (on && "off".equals(c.skyPreset) && c.lastSky != null && !c.lastSky.isEmpty()) {
            c.skyPreset = c.lastSky;
        }
        if (!on) {
            c.skyPreset = "off";
        }
        c.fxSkyColor = on;
        c.fxWeather = on;
        c.fxAurora = on;
        c.fxStars = on;
        c.glow = on;
        c.quality = on ? 1 : 0;
        Config sc = Core.cfg();
        if (!on && sc.sky >= 0) {
            sc.lastSky = sc.sky;
        }
        sc.sky = on ? Math.max(0, sc.lastSky) : -1;
        sc.fxSkyColor = on;
        sc.fxWeather = on;
        sc.fxAurora = on;
        sc.fxStars = on;
        sc.fxConstellations = on;
        sc.fxSkyMusic = on;
        sc.quality = on ? 1 : 0;
        Core.saveConfig();
        if (!on && !c.fpsBoost) {
            FpsBoost.apply(true);
        }
        if (on && c.fpsBoost) {
            FpsBoost.apply(false);
        }
        AutoPerf.reset();
    }
}


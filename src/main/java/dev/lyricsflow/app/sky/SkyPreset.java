/*
 * Decompiled with CFR 0.152.
 */
package dev.lyricsflow.app.sky;

import dev.lyricsflow.app.Core;
import dev.lyricsflow.app.config.Config;
import dev.lyricsflow.app.hud.Gfx;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public enum SkyPreset {
    SYNTHWAVE(721956, 5905006, 0.9f, 0.8f, false, 16732120, 4973567, Weather.NONE, 0, true, 16759024),
    AURORA(133652, 731699, 0.85f, 1.0f, true, 4652976, 10124543, Weather.NONE, 0, true, 10289108),
    NEBULA(459546, 1968698, 0.85f, 1.0f, false, 16736184, 7240959, Weather.NONE, 0, true, 15780095),
    BLACK_HOLE(65541, 722456, 0.9f, 1.4f, false, 16757850, 16734766, Weather.NONE, 0, false, 16766632),
    COSMOS(197132, 1182252, 0.9f, 1.7f, false, 11824127, 6015231, Weather.NONE, 0, true, 14207231),
    NEON_CITY(329750, 2756170, 0.85f, 0.3f, false, 16727464, 3073791, Weather.RAIN, 0, false, 16751320),
    GOLDEN_HOUR(2761552, 16751196, 0.7f, 0.15f, false, 16761466, 16743052, Weather.FIREFLIES, 0, false, 16769208),
    COTTON_CANDY(5214453, 16763108, 0.85f, 0.0f, false, 0xFFFFFF, 16766700, Weather.NONE, 0, false, 0xFFFFFF),
    SAKURA(3807820, 16757721, 0.6f, 0.3f, false, 16762598, 13144319, Weather.PETALS, 0, false, 16762598),
    FROST(662064, 6264008, 0.75f, 0.8f, true, 12120319, 15137023, Weather.SNOW, 15398655, false, 0xDFF4FF),
    MIDNIGHT(131850, 1055290, 0.85f, 1.8f, false, 0, 0, Weather.NONE, 15920861, true, 13161215),
    DEEP_SEA(136734, 739930, 0.8f, 0.3f, false, 6025958, 12124150, Weather.NONE, 0, false, 11075572),
    BLOOD_MOON(1377030, 4852242, 0.85f, 0.6f, false, 16734794, 3803664, Weather.EMBERS, 0xFF3B3B, false, 0xFF8A8A),
    STORM(921880, 3028034, 0.8f, 0.0f, false, 0, 0, Weather.THUNDER, 0, false, 13161712),
    CUSTOM(656920, 2758216, 0.85f, 1.2f, true, 10719743, 0xFFFFFF, Weather.BLIZZARD, 0, true, 13219327),
    INFERNO(1704962, 8002566, 0.85f, 0.2f, false, 16747038, 16769658, Weather.EMBERS, 0, false, 16757867),
    MATRIX(2052, 138252, 0.9f, 0.0f, false, 4652922, 14221280, Weather.NONE, 0, false, 8191898),
    PRISM(459796, 1708086, 0.9f, 1.2f, false, 11824127, 4973567, Weather.NONE, 0, true, 15126783),
    PULSAR(131852, 660528, 0.9f, 1.6f, false, 4973567, 11824127, Weather.NONE, 0, true, 0xBFF4FF),
    AQUARELL(15124712, 16770258, 0.8f, 0.0f, false, 16752329, 10475775, Weather.PETALS, 0, false, 0xFFFFFF),
    POLAR(1551, 670280, 0.9f, 0.0f, false, 3076288, 4951295, Weather.NONE, 0, true, 10289128),
    GALAXY(131088, 1378360, 0.9f, 0.0f, false, 11824127, 6015231, Weather.NONE, 0, true, 14207231),
    OCEAN(340843, 2932966, 0.9f, 0.0f, false, 6025958, 12124150, Weather.NONE, 0, false, 0xB8FFFF),
    ICE(662096, 6261990, 0.9f, 0.0f, false, 12120319, 15137023, Weather.SNOW, 0, false, 0xDFF4FF),
    PURPLE(656160, 4199518, 0.9f, 0.0f, false, 11824127, 14725375, Weather.NONE, 0, true, 14729471),
    SUNSET(3021401, 16741158, 0.9f, 0.0f, false, 16757867, 0xFF6E6E, Weather.FIREFLIES, 0, false, 16765088),
    CHERRY(2034227, 9188224, 0.9f, 0.0f, false, 16762598, 13144319, Weather.PETALS, 0, false, 16762598),
    CRIMSON(655362, 4195077, 0.9f, 0.0f, false, 16726826, 16747114, Weather.EMBERS, 0, false, 0xFF8A8A),
    THUNDER(198676, 1320010, 0.9f, 0.0f, false, 9416959, 13161727, Weather.RAIN, 0, false, 13161712),
    RIFT(197388, 1706798, 0.9f, 1.2f, false, 11816191, 5042431, Weather.NONE, 0, true, 15255807),
    SUPERNOVA(263698, 1055290, 0.9f, 1.4f, false, 16742988, 5032191, Weather.NONE, 0, true, 16767168),
    WARP(132370, 662592, 0.9f, 0.6f, false, 0x8FB8FF, 0xFFFFFF, Weather.NONE, 0, false, 13623551),
    SILK(135182, 735802, 0.9f, 0.9f, false, 3997616, 8019199, Weather.FIREFLIES, 0, false, 13172712),
    ECLIPSE(330270, 8010268, 0.85f, 1.0f, false, 16769704, 16747069, Weather.NONE, 0, true, 16771272);

    public static final int PAINTED = 20;
    public int top;
    public int horizon;
    public float strength;
    public float stars;
    public boolean aurora;
    public int aurora1;
    public int aurora2;
    public Weather weather;
    public int moon;
    public boolean shootingStars;
    public int lyricColor;

    public boolean painted() {
        return this.ordinal() >= 20 && this.ordinal() <= THUNDER.ordinal();
    }

    private SkyPreset(int top, int horizon, float strength, float stars, boolean aurora, int aurora1, int aurora2, Weather weather, int moon, boolean shootingStars, int lyricColor) {
        this.top = top;
        this.horizon = horizon;
        this.strength = strength;
        this.stars = stars;
        this.aurora = aurora;
        this.aurora1 = aurora1;
        this.aurora2 = aurora2;
        this.weather = weather;
        this.moon = moon;
        this.shootingStars = shootingStars;
        this.lyricColor = lyricColor;
    }

    public boolean clouds() {
        return this == COTTON_CANDY;
    }

    public String key() {
        return "lyricsflow.sky." + this.name().toLowerCase(Locale.ROOT);
    }

    public String[] features() {
        ArrayList<Object> f = new ArrayList<Object>();
        switch (this.ordinal()) {
            case 0: {
                f.add("lyricsflow.feat.synth");
            }
            default: {
                break;
            }
            case 2: {
                f.add("lyricsflow.feat.nebula");
                break;
            }
            case 3: {
                f.add("lyricsflow.feat.blackhole");
                break;
            }
            case 4: {
                f.add("lyricsflow.feat.galaxy");
                break;
            }
            case 5: {
                f.add("lyricsflow.feat.beams");
                break;
            }
            case 6: 
            case 7: {
                f.add("lyricsflow.feat.clouds");
                break;
            }
            case 11: {
                f.add("lyricsflow.feat.lagoon");
                break;
            }
            case 15: {
                f.add("lyricsflow.feat.lava");
                break;
            }
            case 16: {
                f.add("lyricsflow.feat.code");
                break;
            }
            case 17: 
            case 18: {
                f.add("lyricsflow.feat.beams");
                break;
            }
            case 19: {
                f.add("lyricsflow.feat.paint");
                break;
            }
            case 29: {
                f.add("lyricsflow.feat.rift");
                break;
            }
            case 30: {
                f.add("lyricsflow.feat.supernova");
                break;
            }
            case 31: {
                f.add("lyricsflow.feat.warp");
                break;
            }
            case 32: {
                f.add("lyricsflow.feat.silk");
                break;
            }
            case 33: {
                f.add("lyricsflow.feat.eclipse");
            }
        }
        if (this.aurora) {
            f.add("lyricsflow.feat.aurora");
        }
        if (this.stars >= 1.0f) {
            f.add("lyricsflow.feat.stars");
        }
        if (this.shootingStars) {
            f.add("lyricsflow.feat.shooting");
        }
        if (this.moon != 0) {
            f.add("lyricsflow.feat.moon");
        }
        if (this.weather != Weather.NONE) {
            f.add("lyricsflow.feat." + this.weather.name().toLowerCase(Locale.ROOT));
        }
        return f.toArray(new String[0]);
    }

    public static SkyPreset[] order() {
        return new SkyPreset[]{RIFT, SUPERNOVA, WARP, SILK, ECLIPSE, INFERNO, MATRIX, PRISM, PULSAR, AQUARELL, SYNTHWAVE, AURORA, NEBULA, BLACK_HOLE, COSMOS, NEON_CITY, GOLDEN_HOUR, COTTON_CANDY, SAKURA, FROST, MIDNIGHT, DEEP_SEA, BLOOD_MOON, STORM, CUSTOM};
    }

    public static SkyPreset get(int i) {
        SkyPreset p;
        SkyPreset[] v = SkyPreset.values();
        SkyPreset skyPreset = p = i >= 0 && i < v.length ? v[i] : null;
        if (p == CUSTOM) {
            SkyPreset.applyCustom();
        }
        return p;
    }

    public static void shuffle() {
        int n;
        Config c = Core.cfg();
        SkyPreset[] all = SkyPreset.values();
        SkyPreset[] list = SkyPreset.order();
        while ((all[n = list[ThreadLocalRandom.current().nextInt(list.length)].ordinal()] == CUSTOM || n == c.sky) && list.length > 2) {
        }
        c.sky = n;
        c.lastSky = n;
        Core.saveConfig();
    }

    public static void applyCustom() {
        Config c = Core.cfg();
        SkyPreset.CUSTOM.top = c.customTop;
        SkyPreset.CUSTOM.horizon = c.customHorizon;
        SkyPreset.CUSTOM.aurora = c.customAurora;
        SkyPreset.CUSTOM.aurora1 = c.customAurora1;
        SkyPreset.CUSTOM.aurora2 = c.customAurora2;
        SkyPreset.CUSTOM.stars = c.customStars ? 1.3f : 0.0f;
        SkyPreset.CUSTOM.moon = c.customMoon ? 15920861 : 0;
        SkyPreset.CUSTOM.shootingStars = c.customShooting;
        Weather[] w = Weather.values();
        SkyPreset.CUSTOM.weather = w[Math.floorMod(c.customWeather, w.length)];
        SkyPreset.CUSTOM.lyricColor = Gfx.lerpColor(c.customAurora1, 0xFFFFFF, 0.35f);
    }

    public static enum Weather {
        NONE,
        SNOW,
        BLIZZARD,
        RAIN,
        THUNDER,
        PETALS,
        EMBERS,
        FIREFLIES;

    }
}


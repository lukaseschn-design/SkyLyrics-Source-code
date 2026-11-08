package dev.lyricsflow.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LFConfig {
    public int configVersion = 21;
    public boolean enabled = true;
    public boolean uiSounds = true;
    public float uiVolume = 0.6f;
    public int soundStyle = 0;
    public boolean songChime = false;
    public Source source = Source.SPOTIFY;
    public int syncOffsetMs = 0;
    public Map<String, Integer> songOffsets = new HashMap<String, Integer>();
    public boolean showLyrics = true;
    public boolean lyricsOn = true;
    public LyricsStyle lyricsStyle = LyricsStyle.FLOATING;
    public boolean skyWords = false;
    public int airMode = 0;
    public boolean followView = false;
    public boolean viewLock = false;
    public boolean showUnderground = false;
    public boolean musicNotes = true;
    public boolean randomPosition = true;
    public boolean sentenceMode = false;
    public boolean allCaps = true;
    public boolean skyFollow = true;
    public float floatHeight = 0.0f;
    public int skyColor = 0;
    public int plainSkyColor = 0;
    public int customTop = -16777216;
    public int customHorizon = -16777216;
    public int customAurora1 = -1;
    public int customAurora2 = -1;
    public boolean customAurora = true;
    public boolean customStars = false;
    public boolean customMoon = false;
    public boolean customShooting = true;
    public int customWeather = 0;
    public boolean weatherRain = false;
    public boolean weatherSnow = false;
    public boolean weatherEmbers = false;
    public float lyricsScale = 0.8f;
    public float lyricsDistance = 1.0f;
    public float skyDistance = 150.0f;
    public int textShadow = 1;
    public int shadowColor = 0;
    public boolean varySizes = true;
    public boolean arcLayout = true;
    public int maxLines = 2;
    public String font = "righteous";
    public TextStyle textStyle = TextStyle.NEON;
    public String theme = "lavender";
    public boolean customTheme = false;
    public int singleColor = 0;
    public int customMain = -7178;
    public int customA = -42296;
    public int customB = -9447169;
    public boolean twoColor = true;
    public float twoColorStrength = 0.6f;
    public int twoColorColor = 0;
    public boolean glow = true;
    public boolean autoPerf = true;
    public int targetFps = 60;
    public int quality = 1;
    public int profile = 0;
    public boolean fpsBoost = false;
    public String boostBefore = "";
    public boolean fxSkyColor = true;
    public boolean fxWeather = true;
    public boolean fxAurora = true;
    public boolean fxStars = true;
    public String lastSky = "";
    public float textOpacity = 1.0f;
    public EnterAnim enterAnim = EnterAnim.WORD;
    public ExitAnim exitAnim = ExitAnim.FLOAT_UP;
    public float animSpeed = 1.0f;
    public boolean beatGlow = false;
    public boolean stars = true;
    public float starDensity = 0.6f;
    public boolean motes = false;
    public float moteDensity = 0.5f;
    public boolean streaks = true;
    public String skyPreset = "off";
    public int accentColor = 0;
    public int barColor = 0;
    public int starColor = 0;
    public int moteColor = 0;
    public int streakColor = 0;
    public float streakDensity = 0.5f;
    public boolean showTopBar = true;
    public boolean chroma = false;
    public boolean skyAlways = true;
    public int chromaColor = 0;
    public boolean customHotbar = false;
    public int hotbarColor = 0;
    public int hotbarBarColor = 0;
    public boolean hudKeys = false;
    public boolean hudCps = false;
    public boolean hudPlayer = false;
    public boolean hudPlayerLyrics = true;
    public float keysX = -1.0f;
    public float keysY = -1.0f;
    public float cpsX = -1.0f;
    public float cpsY = -1.0f;
    public float playerX = -1.0f;
    public float playerY = -1.0f;
    public float keysScale = 1.0f;
    public float cpsScale = 1.0f;
    public float playerScale = 1.0f;
    public int hudColorMode = 0;
    public boolean hudRound = true;
    public String cosmetic = "none";
    public String[] cosmeticMore = new String[]{"none", "none", "none", "none"};
    public TopBarStyle topBarStyle = TopBarStyle.KARAOKE;
    public boolean clock = false;
    public boolean ping = false;
    public boolean fps = false;
    public String topBarFont = "outfit";
    public float topBarScale = 1.0f;
    public float topBarX = 0.5f;
    public float topBarY = 0.02f;
    public float screenLyricsX = 0.5f;
    public float screenLyricsY = 0.32f;
    public boolean showOnlyWhenPlaying = true;
    public boolean hideWhenStopped = false;
    public boolean dimWhenIdle = true;
    public boolean mediaKeys = true;
    public boolean hideInScreenshots = true;
    public Map<String, String> profiles = new LinkedHashMap<String, String>();

    public boolean lyricsShown() {
        return this.lyricsOn && this.showLyrics;
    }

    public String slot(int i) {
        String s;
        if (i == 0) {
            return this.cosmetic == null ? "none" : this.cosmetic;
        }
        if (this.cosmeticMore == null || this.cosmeticMore.length < 4) {
            this.cosmeticMore = new String[]{"none", "none", "none", "none"};
        }
        return (s = this.cosmeticMore[i - 1]) == null ? "none" : s;
    }

    public void setSlot(int i, String id) {
        if (id == null) {
            id = "none";
        }
        for (int k = 0; k < 5; ++k) {
            if (k == i || "none".equals(id) || !id.equals(this.slot(k))) continue;
            this.put(k, "none");
        }
        this.put(i, id);
    }

    private void put(int i, String id) {
        this.slot(0);
        this.slot(1);
        if (i == 0) {
            this.cosmetic = id;
        } else {
            this.cosmeticMore[i - 1] = id;
        }
    }

    public List<String> worn() {
        ArrayList<String> out = new ArrayList<String>();
        for (int i = 0; i < 5; ++i) {
            String s = this.slot(i);
            if ("none".equals(s) || out.contains(s)) continue;
            out.add(s);
        }
        return out;
    }

    public static enum Source {
        SPOTIFY,
        YOUTUBE,
        ANY,
        YTMUSIC,
        SOUNDCLOUD;

    }

    public static enum LyricsStyle {
        SKY,
        FLOATING,
        SCREEN,
        WALL;

    }

    public static enum TextStyle {
        NEON,
        CLEAN;

    }

    public static enum EnterAnim {
        WORD,
        FADE,
        RISE,
        NEON;

    }

    public static enum ExitAnim {
        FLOAT_UP,
        DRIFT,
        FADE;

    }

    public static enum TopBarStyle {
        KARAOKE,
        SIMPLE;

    }
}


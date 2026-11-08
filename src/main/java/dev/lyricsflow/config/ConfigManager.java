/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.google.gson.JsonPrimitive
 *  net.fabricmc.loader.api.FabricLoader
 */
package dev.lyricsflow.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import dev.lyricsflow.LyricsFlow;
import dev.lyricsflow.app.Core;
import dev.lyricsflow.config.LFConfig;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import net.fabricmc.loader.api.FabricLoader;

public final class ConfigManager {
    public static boolean moveKeys;
    private static final Gson GSON;
    private static LFConfig config;
    private static final Map<String, String[]> GROUPS;
    private static final Set<String> DROP;

    private ConfigManager() {
    }

    public static LFConfig get() {
        return config;
    }

    public static Path dir() {
        Path p = FabricLoader.getInstance().getConfigDir().resolve("lyricsflow");
        try {
            Files.createDirectories(p, new FileAttribute[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return p;
    }

    private static Path file() {
        return ConfigManager.dir().resolve("config.json");
    }

    private static boolean isColor(String k) {
        return k.endsWith("Color") && !k.equals("twoColor") || k.equals("customMain") || k.equals("customA") || k.equals("customB");
    }

    static JsonObject toPretty(LFConfig c) {
        JsonObject flat = GSON.toJsonTree((Object)c).getAsJsonObject();
        JsonObject out = new JsonObject();
        out.addProperty("_info", "SkyLyrics v1.0 - change these in game with the menu key. Colors: #RRGGBB or \"auto\".");
        out.add("configVersion", flat.remove("configVersion"));
        HashSet<String> used = new HashSet<String>();
        for (Map.Entry<String, String[]> g : GROUPS.entrySet()) {
            JsonObject o = new JsonObject();
            for (String k : g.getValue()) {
                JsonElement v = flat.get(k);
                if (v == null) continue;
                used.add(k);
                if (ConfigManager.isColor(k) && v.isJsonPrimitive() && v.getAsJsonPrimitive().isNumber()) {
                    int col = v.getAsInt();
                    v = new JsonPrimitive(col == 0 ? "auto" : (col >>> 24 == 255 ? String.format("#%06X", col & 0xFFFFFF) : String.format("#%08X", col)));
                }
                o.add(k, v);
            }
            out.add(g.getKey(), (JsonElement)o);
        }
        JsonObject other = new JsonObject();
        for (Map.Entry e : flat.entrySet()) {
            if (used.contains(e.getKey()) || DROP.contains(e.getKey()) || ((String)e.getKey()).equals("profiles")) continue;
            other.add((String)e.getKey(), (JsonElement)e.getValue());
        }
        if (other.size() > 0) {
            out.add("other", (JsonElement)other);
        }
        out.add("profiles", flat.get("profiles"));
        return out;
    }

    static JsonObject flatten(JsonObject in) {
        JsonObject flat = new JsonObject();
        for (Map.Entry e : in.entrySet()) {
            String k = (String)e.getKey();
            JsonElement v = (JsonElement)e.getValue();
            if (v.isJsonObject() && !k.equals("profiles")) {
                for (Map.Entry e2 : v.getAsJsonObject().entrySet()) {
                    flat.add((String)e2.getKey(), (JsonElement)e2.getValue());
                }
                continue;
            }
            if (k.startsWith("_")) continue;
            flat.add(k, v);
        }
        for (Map.Entry<String, JsonElement> e : new ArrayList<>(flat.entrySet())) {
            if (!ConfigManager.isColor((String)e.getKey()) || !((JsonElement)e.getValue()).isJsonPrimitive() || !((JsonElement)e.getValue()).getAsJsonPrimitive().isString()) continue;
            String s = ((JsonElement)e.getValue()).getAsString().trim();
            int col = 0;
            if (s.startsWith("#")) {
                try {
                    long l = Long.parseLong(s.substring(1), 16);
                    col = s.length() <= 7 ? (int)(0xFF000000L | l) : (int)l;
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
            }
            flat.addProperty((String)e.getKey(), (Number)col);
        }
        return flat;
    }

    public static void load() {
        Path f = ConfigManager.file();
        if (Files.exists(f, new LinkOption[0])) {
            try {
                JsonObject flat = ConfigManager.flatten(JsonParser.parseString((String)Files.readString(f, StandardCharsets.UTF_8)).getAsJsonObject());
                LFConfig c = (LFConfig)GSON.fromJson((JsonElement)flat, LFConfig.class);
                if (c != null) {
                    int ver;
                    if (c.profiles == null) {
                        c.profiles = new LinkedHashMap<String, String>();
                    }
                    if (c.source == null || c.source == LFConfig.Source.ANY || c.source == LFConfig.Source.SOUNDCLOUD) {
                        c.source = LFConfig.Source.SPOTIFY;
                    }
                    int n = ver = flat.has("configVersion") ? flat.get("configVersion").getAsInt() : 1;
                    if (ver < 4) {
                        c.lyricsStyle = LFConfig.LyricsStyle.FLOATING;
                    }
                    if (ver < 5) {
                        c.soundStyle = 0;
                    }
                    if (!flat.has("skyPreset")) {
                        c.skyPreset = "watercolor";
                    }
                    if ("candy".equals(c.skyPreset)) {
                        c.skyPreset = "lilac";
                    }
                    if (!flat.has("stars")) {
                        c.stars = true;
                    }
                    if (!flat.has("motes")) {
                        c.motes = true;
                    }
                    if (!flat.has("streaks")) {
                        c.streaks = true;
                    }
                    if (ver < 6) {
                        c.songChime = false;
                    }
                    if (ver < 7) {
                        moveKeys = true;
                    }
                    if (ver < 8) {
                        c.stars = true;
                        c.streaks = true;
                    }
                    if (ver < 9) {
                        c.beatGlow = false;
                    }
                    if (ver < 11) {
                        float f2 = c.twoColorStrength = c.twoColor ? 0.6f : 0.0f;
                    }
                    if (ver < 10) {
                        c.customHotbar = false;
                    }
                    c.viewLock = false;
                    if (ver < 13) {
                        c.customWeather = 0;
                    }
                    if (ver < 12) {
                        c.followView = false;
                    }
                    c.randomPosition = true;
                    if (ver < 14) {
                        c.syncOffsetMs = 0;
                    }
                    if (c.lyricsStyle == LFConfig.LyricsStyle.SCREEN || c.lyricsStyle == LFConfig.LyricsStyle.WALL || c.lyricsStyle == null) {
                        c.lyricsStyle = LFConfig.LyricsStyle.FLOATING;
                    }
                    if (ver < 15) {
                        c.font = "poppins";
                    }
                    if (ver < 16) {
                        c.theme = "lavender";
                        c.customTheme = false;
                        c.accentColor = 0;
                        c.barColor = 0;
                    }
                    if (ver < 17) {
                        c.allCaps = true;
                        c.twoColorStrength = 0.0f;
                    }
                    if (ver < 18) {
                        c.font = "poppins";
                        c.enterAnim = LFConfig.EnterAnim.NEON;
                    }
                    if (ver < 19) {
                        c.enterAnim = LFConfig.EnterAnim.WORD;
                        c.twoColorStrength = 0.6f;
                    }
                    if (ver < 20) {
                        if (Set.of("allura", "parisienne", "alexbrush", "sacramento", "greatvibes", "dancing").contains(c.font)) {
                            c.font = "poppins";
                        }
                        c.showLyrics = true;
                    }
                    if (ver < 21 && "poppins".equals(c.font)) {
                        c.font = "righteous";
                    }
                    c.configVersion = 21;
                    config = c;
                }
            }
            catch (Exception e) {
                LyricsFlow.LOG.warn("Could not read config, using defaults", (Throwable)e);
            }
        }
        ConfigManager.save();
    }

    public static void save() {
        String json = GSON.toJson((JsonElement)ConfigManager.toPretty(config));
        try {
            Path tmp = ConfigManager.file().resolveSibling("config.json.tmp");
            Files.writeString(tmp, (CharSequence)json, StandardCharsets.UTF_8, new OpenOption[0]);
            Files.move(tmp, ConfigManager.file(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        }
        catch (Exception e) {
            try {
                Files.writeString(ConfigManager.file(), (CharSequence)json, StandardCharsets.UTF_8, new OpenOption[0]);
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
    }

    public static String snapshot() {
        JsonObject o = GSON.toJsonTree((Object)config).getAsJsonObject();
        o.remove("profiles");
        for (String k : DROP) {
            o.remove(k);
        }
        try {
            o.add("_sky", new Gson().toJsonTree((Object)Core.cfg()));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return new Gson().toJson((JsonElement)o);
    }

    public static String exportCode(String snapshotJson) {
        return "LF1:" + Base64.getEncoder().encodeToString(ConfigManager.deflate(snapshotJson.getBytes(StandardCharsets.UTF_8)));
    }

    public static String importCode(String code) {
        if (code == null) {
            return null;
        }
        code = code.trim();
        try {
            String json;
            if (code.startsWith("LF1:")) {
                json = new String(ConfigManager.inflate(Base64.getDecoder().decode(code.substring(4).replaceAll("\\s", ""))), StandardCharsets.UTF_8);
            } else if (code.startsWith("{")) {
                json = code;
            } else {
                return null;
            }
            JsonObject flat = ConfigManager.flatten(JsonParser.parseString((String)json).getAsJsonObject());
            flat.remove("profiles");
            GSON.fromJson((JsonElement)flat, LFConfig.class);
            return new Gson().toJson((JsonElement)flat);
        }
        catch (Exception e) {
            return null;
        }
    }

    private static byte[] deflate(byte[] in) {
        Deflater d = new Deflater(9);
        d.setInput(in);
        d.finish();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        while (!d.finished()) {
            out.write(buf, 0, d.deflate(buf));
        }
        d.end();
        return out.toByteArray();
    }

    private static byte[] inflate(byte[] in) throws DataFormatException {
        int n;
        Inflater i = new Inflater();
        i.setInput(in);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        while (!(i.finished() || (n = i.inflate(buf)) == 0 && (i.needsInput() || i.needsDictionary()))) {
            out.write(buf, 0, n);
        }
        i.end();
        return out.toByteArray();
    }

    public static void applySnapshot(String json) {
        try {
            JsonObject root = JsonParser.parseString((String)json).getAsJsonObject();
            JsonElement sky = root.remove("_sky");
            if (sky != null && sky.isJsonObject()) {
                Core.applyJson(sky.getAsJsonObject());
            }
            LFConfig c = (LFConfig)GSON.fromJson((JsonElement)ConfigManager.flatten(root), LFConfig.class);
            c.profiles = ConfigManager.config.profiles;
            c.configVersion = 21;
            config = c;
            ConfigManager.save();
        }
        catch (Exception e) {
            LyricsFlow.LOG.warn("Could not apply profile", (Throwable)e);
        }
    }

    public static void reset() {
        config = new LFConfig();
        ConfigManager.save();
    }

    public static Gson gson() {
        return GSON;
    }

    static {
        GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        config = new LFConfig();
        GROUPS = new LinkedHashMap<String, String[]>();
        GROUPS.put("general", new String[]{"enabled", "uiSounds", "uiVolume", "soundStyle", "songChime", "mediaKeys", "hideInScreenshots"});
        GROUPS.put("music", new String[]{"source", "syncOffsetMs"});
        GROUPS.put("lyrics", new String[]{"lyricsOn", "showLyrics", "lyricsStyle", "sentenceMode", "allCaps", "floatHeight", "skyWords", "randomPosition", "followView", "viewLock", "showUnderground", "musicNotes", "lyricsScale", "lyricsDistance", "skyDistance", "textShadow", "shadowColor", "textOpacity", "maxLines", "varySizes", "arcLayout", "enterAnim", "exitAnim", "animSpeed"});
        GROUPS.put("style", new String[]{"font", "textStyle", "theme", "customTheme", "singleColor", "customMain", "customA", "customB", "twoColor", "twoColorStrength", "twoColorColor", "glow", "beatGlow", "accentColor"});
        GROUPS.put("sky", new String[]{"skyPreset", "skyColor", "plainSkyColor", "skyAlways", "chroma", "chromaColor", "stars", "starDensity", "starColor", "motes", "moteDensity", "moteColor", "streaks", "streakDensity", "streakColor", "customTop", "customHorizon", "customAurora1", "customAurora2", "customAurora", "customStars", "customMoon", "customShooting", "customWeather", "weatherRain", "weatherSnow", "weatherEmbers"});
        GROUPS.put("topBar", new String[]{"showTopBar", "customHotbar", "hotbarColor", "hotbarBarColor", "topBarStyle", "topBarFont", "topBarScale", "barColor", "clock", "ping", "fps", "showOnlyWhenPlaying", "hideWhenStopped", "dimWhenIdle"});
        GROUPS.put("performance", new String[]{"autoPerf", "targetFps", "quality", "profile", "fpsBoost", "boostBefore", "fxSkyColor", "fxWeather", "fxAurora", "fxStars", "lastSky"});
        GROUPS.put("positions", new String[]{"topBarX", "topBarY", "screenLyricsX", "screenLyricsY"});
        DROP = Set.of("connection", "spotifyClientId", "spotifyRefreshToken");
    }
}


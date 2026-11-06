/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.FontDescription
 *  net.minecraft.network.chat.FontDescription$Resource
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.Style
 *  net.minecraft.resources.Identifier
 *  net.minecraft.client.gui.Font
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.FormattedText
 */
package dev.lyricsflow.render;

import dev.lyricsflow.SkyLyrics;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.render.CustomFonts;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.FormattedText;

public final class Fonts {
    public static final float SIZE = 16.0f;
    public static final List<LyricFont> LYRICS = List.of(new LyricFont("righteous", "Righteous"), new LyricFont("poppins", "Poppins"), new LyricFont("lobster", "Lobster"), new LyricFont("caveatbrush", "Caveat Brush"), new LyricFont("bebas", "Bebas Neue"), new LyricFont("fredoka", "Fredoka"), new LyricFont("comfortaa", "Comfortaa"), new LyricFont("lyricsflow_script", "SkyLyrics Script"), new LyricFont("kaushan", "Kaushan Script"), new LyricFont("pacifico", "Pacifico"), new LyricFont("yellowtail", "Yellowtail"), new LyricFont("damion", "Damion"), new LyricFont("marck", "Marck Script"), new LyricFont("courgette", "Courgette"), new LyricFont("norican", "Norican"), new LyricFont("grandhotel", "Grand Hotel"), new LyricFont("yesteryear", "Yesteryear"), new LyricFont("quicksand", "Quicksand"), new LyricFont("outfit_bold", "Outfit Bold"), new LyricFont("minecraft", "Minecraft"));
    public static final List<LyricFont> UI = List.of(new LyricFont("outfit", "Outfit"), new LyricFont("syne", "Syne"), new LyricFont("quicksand", "Quicksand"), new LyricFont("minecraft", "Minecraft"));
    public static final String UI_REGULAR = "outfit";
    public static final String UI_BOLD = "outfit_bold";
    private static final Map<String, MutableComponent> TEXT_CACHE = new HashMap<String, MutableComponent>();
    private static final Map<String, Float> WIDTH_CACHE = new HashMap<String, Float>();
    private static final Map<String, Float> SHIFT = Map.ofEntries(Map.entry("alexbrush", Float.valueOf(0.35f)), Map.entry("allura", Float.valueOf(0.285f)), Map.entry("courgette", Float.valueOf(0.379f)), Map.entry("damion", Float.valueOf(0.326f)), Map.entry("dancing", Float.valueOf(0.326f)), Map.entry("grandhotel", Float.valueOf(0.348f)), Map.entry("greatvibes", Float.valueOf(0.354f)), Map.entry("kaushan", Float.valueOf(0.367f)), Map.entry("lyricsflow_script", Float.valueOf(0.367f)), Map.entry("marck", Float.valueOf(0.315f)), Map.entry("norican", Float.valueOf(0.35f)), Map.entry("outfit", Float.valueOf(0.357f)), Map.entry("outfit_bold", Float.valueOf(0.36f)), Map.entry("poppins", Float.valueOf(0.378f)), Map.entry("pacifico", Float.valueOf(0.399f)), Map.entry("parisienne", Float.valueOf(0.337f)), Map.entry("quicksand", Float.valueOf(0.37f)), Map.entry("sacramento", Float.valueOf(0.325f)), Map.entry("syne", Float.valueOf(0.349f)), Map.entry("yellowtail", Float.valueOf(0.354f)), Map.entry("yesteryear", Float.valueOf(0.329f)));
    private static final Map<String, float[]> CAPX = Map.ofEntries(Map.entry("alexbrush", new float[]{0.735f, 0.416f}), Map.entry("allura", new float[]{0.589f, 0.301f}), Map.entry("courgette", new float[]{0.759f, 0.505f}), Map.entry("damion", new float[]{0.705f, 0.35f}), Map.entry("dancing", new float[]{0.721f, 0.332f}), Map.entry("grandhotel", new float[]{0.75f, 0.393f}), Map.entry("greatvibes", new float[]{0.807f, 0.357f}), Map.entry("kaushan", new float[]{0.742f, 0.475f}), Map.entry("lyricsflow_script", new float[]{0.742f, 0.475f}), Map.entry("marck", new float[]{0.637f, 0.372f}), Map.entry("norican", new float[]{0.74f, 0.41f}), Map.entry("outfit", new float[]{0.7f, 0.48f}), Map.entry("outfit_bold", new float[]{0.706f, 0.486f}), Map.entry("pacifico", new float[]{0.877f, 0.47f}), Map.entry("parisienne", new float[]{0.745f, 0.353f}), Map.entry("poppins", new float[]{0.702f, 0.558f}), Map.entry("quicksand", new float[]{0.7f, 0.529f}), Map.entry("sacramento", new float[]{0.744f, 0.306f}), Map.entry("syne", new float[]{0.648f, 0.498f}), Map.entry("yellowtail", new float[]{0.721f, 0.444f}), Map.entry("yesteryear", new float[]{0.772f, 0.295f}), Map.entry("lobster", new float[]{0.75f, 0.557f}), Map.entry("caveatbrush", new float[]{0.66f, 0.412f}), Map.entry("righteous", new float[]{0.7f, 0.526f}), Map.entry("bebas", new float[]{0.7f, 0.7f}), Map.entry("fredoka", new float[]{0.69f, 0.499f}), Map.entry("comfortaa", new float[]{0.781f, 0.547f}));

    public static List<LyricFont> lyricsAll() {
        ArrayList<LyricFont> l = new ArrayList<LyricFont>(LYRICS);
        l.addAll(CustomFonts.list());
        return l;
    }

    private Fonts() {
    }

    public static LyricFont lyric(String id) {
        for (LyricFont f : Fonts.lyricsAll()) {
            if (!f.id.equals(id)) continue;
            return f;
        }
        return LYRICS.get(0);
    }

    public static Style style(String id) {
        if (id == null || id.equals("minecraft")) {
            return Style.EMPTY;
        }
        Identifier fid = SkyLyrics.id(id);
        return Style.EMPTY.withFont((FontDescription)new FontDescription.Resource(fid));
    }

    public static MutableComponent text(String s, String fontId) {
        String key = fontId + "\u0000" + s;
        MutableComponent c = TEXT_CACHE.get(key);
        if (c == null) {
            if (TEXT_CACHE.size() > 3000) {
                TEXT_CACHE.clear();
            }
            c = Component.literal((String)s).withStyle(Fonts.style(fontId));
            TEXT_CACHE.put(key, c);
        }
        return c;
    }

    public static float unitWidth(String s, String fontId, Font font) {
        String key = fontId + "\u0000" + s;
        Float w = WIDTH_CACHE.get(key);
        if (w == null) {
            if (WIDTH_CACHE.size() > 3000) {
                WIDTH_CACHE.clear();
            }
            w = Float.valueOf(font.getSplitter().stringWidth((FormattedText)Fonts.text(s, fontId)) / Fonts.height(fontId));
            WIDTH_CACHE.put(key, w);
        }
        return w.floatValue();
    }

    public static void clearCaches() {
        TEXT_CACHE.clear();
        WIDTH_CACHE.clear();
    }

    public static boolean isRtl(String s) {
        if (s == null) {
            return false;
        }
        for (int i = 0; i < s.length(); ++i) {
            char ch = s.charAt(i);
            if (!(ch >= '\u0590' && ch <= '\u08ff' || ch >= '\ufb1d' && ch <= '\ufdff') && (ch < '\ufe70' || ch > '\ufeff')) continue;
            return true;
        }
        return false;
    }

    public static float yShift(String id) {
        return Fonts.yShift(id, false);
    }

    public static float yShift(String id, boolean caps) {
        if (id == null || id.equals("minecraft")) {
            return 0.0f;
        }
        float[] cx = CAPX.get(id);
        if (cx != null) {
            return caps ? 0.0625f + cx[0] / 2.0f : 0.0625f + (cx[0] + cx[1]) / 4.0f;
        }
        Float f = SHIFT.get(id);
        if (f == null) {
            f = CustomFonts.shift(id);
        }
        return f == null ? 0.35f : f.floatValue();
    }

    public static boolean capsOnly(String s) {
        if (s == null || !ConfigManager.get().allCaps) {
            return false;
        }
        for (int i = 0; i < s.length(); ++i) {
            if (!Character.isLowerCase(s.charAt(i))) continue;
            return false;
        }
        return true;
    }

    public static float height(String id) {
        return id == null || id.equals("minecraft") ? 9.0f : 16.0f;
    }

    public record LyricFont(String id, String name) {
    }
}


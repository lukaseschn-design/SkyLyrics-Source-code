/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_3259$class_8619
 *  net.minecraft.class_3264
 *  net.minecraft.class_3285
 *  net.minecraft.class_3288
 *  net.minecraft.class_3288$class_3289
 *  net.minecraft.class_3288$class_7680
 *  net.minecraft.class_5352
 *  net.minecraft.class_9224
 *  net.minecraft.class_9225
 */
package dev.lyricsflow.render;

import dev.lyricsflow.SkyLyrics;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.render.Fonts;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.class_2561;
import net.minecraft.class_3259;
import net.minecraft.class_3264;
import net.minecraft.class_3285;
import net.minecraft.class_3288;
import net.minecraft.class_5352;
import net.minecraft.class_9224;
import net.minecraft.class_9225;

public final class CustomFonts {
    private static final List<Fonts.LyricFont> FONTS = new ArrayList<Fonts.LyricFont>();
    private static final Map<String, Float> SHIFT = new HashMap<String, Float>();

    private CustomFonts() {
    }

    public static Path folder() {
        Path p = ConfigManager.dir().resolve("fonts");
        try {
            Files.createDirectories(p, new FileAttribute[0]);
            Path readme = p.resolve("README.txt");
            if (!Files.exists(readme, new LinkOption[0])) {
                Files.writeString(readme, (CharSequence)"Put .ttf font files in this folder, then press \"Reload fonts\" in the SkyLyrics menu (Style tab).\nFree fonts: https://fonts.google.com  -  https://www.dafont.com\nOnly TrueType (.ttf) works. Some .otf fonts are TrueType inside: rename them to .ttf to try.\n", StandardCharsets.UTF_8, new OpenOption[0]);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return p;
    }

    private static Path packDir() {
        return ConfigManager.dir().resolve("fontpack");
    }

    public static synchronized List<Fonts.LyricFont> list() {
        return new ArrayList<Fonts.LyricFont>(FONTS);
    }

    public static synchronized Float shift(String id) {
        return SHIFT.get(id);
    }

    private static String slug(String name) {
        String s = name.toLowerCase(Locale.ROOT).replaceAll("\\.ttf$", "").replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        return s.isEmpty() ? "font" : s;
    }

    public static synchronized void rebuild() {
        FONTS.clear();
        SHIFT.clear();
        Path fonts = CustomFonts.folder();
        Path pack = CustomFonts.packDir();
        try {
            List<Path> files;
            CustomFonts.deleteTree(pack);
            Path fontDir = pack.resolve("assets/lyricsflow/font");
            Path ttfDir = fontDir.resolve("custom");
            Files.createDirectories(ttfDir, new FileAttribute[0]);
            Files.writeString(pack.resolve("pack.mcmeta"), (CharSequence)"{\"pack\": {\"description\": \"SkyLyrics custom fonts\", \"min_format\": 75, \"max_format\": 999}}\n", StandardCharsets.UTF_8, new OpenOption[0]);
            try (Stream<Path> s = Files.list(fonts);){
                files = s.filter(f -> f.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".ttf")).sorted(Comparator.comparing(f -> f.getFileName().toString().toLowerCase(Locale.ROOT))).toList();
            }
            for (Path f2 : files) {
                String display;
                float shift;
                String id;
                block13: {
                    String file = f2.getFileName().toString();
                    id = "custom_" + CustomFonts.slug(file);
                    if (SHIFT.containsKey(id)) continue;
                    shift = 0.35f;
                    try {
                        Font awt = Font.createFont(0, f2.toFile()).deriveFont(16.0f);
                        display = awt.getFamily(Locale.ROOT);
                        String style = awt.getFontName(Locale.ROOT);
                        if (style != null && !style.equals(display) && style.length() < 40) {
                            display = style;
                        }
                        FontRenderContext frc = new FontRenderContext(null, true, true);
                        Rectangle2D cap = awt.createGlyphVector(frc, "H").getVisualBounds();
                        Rectangle2D xh = awt.createGlyphVector(frc, "x").getVisualBounds();
                        double capH = -cap.getY();
                        double xH = -xh.getY();
                        if (!(capH > 1.0) || !(xH > 1.0)) break block13;
                        double center = 7.0 - (capH + xH) / 4.0;
                        shift = (float)((8.0 - center) / 16.0);
                    }
                    catch (Throwable t) {
                        SkyLyrics.LOG.warn("Skipping font {} (not a TrueType font?)", (Object)file);
                        continue;
                    }
                }
                Files.copy(f2, ttfDir.resolve(id + ".ttf"), new CopyOption[0]);
                Files.writeString(fontDir.resolve(id + ".json"), (CharSequence)"{\"providers\": [\n  {\"type\": \"ttf\", \"file\": \"lyricsflow:custom/%s.ttf\", \"size\": 16.0, \"oversample\": 4.0, \"shift\": [0.0, 0.0]},\n  {\"type\": \"reference\", \"id\": \"minecraft:include/default\"},\n  {\"type\": \"reference\", \"id\": \"minecraft:include/unifont\"}\n]}\n".formatted(id), StandardCharsets.UTF_8, new OpenOption[0]);
                FONTS.add(new Fonts.LyricFont(id, display));
                SHIFT.put(id, Float.valueOf(Math.max(0.1f, Math.min(0.6f, shift))));
            }
        }
        catch (Throwable t) {
            SkyLyrics.LOG.warn("Could not build the custom font pack", t);
        }
        Fonts.clearCaches();
    }

    private static void deleteTree(Path p) throws IOException {
        if (!Files.exists(p, new LinkOption[0])) {
            return;
        }
        try (Stream<Path> s = Files.walk(p, new FileVisitOption[0]);){
            for (Path q : s.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(q);
            }
        }
    }

    public static class_3285 source() {
        return consumer -> {
            CustomFonts.rebuild();
            class_9224 info = new class_9224("lyricsflow_fonts", (class_2561)class_2561.method_43470((String)"SkyLyrics fonts"), class_5352.field_25348, Optional.empty());
            class_3288 p = class_3288.method_45275((class_9224)info, (class_3288.class_7680)new class_3259.class_8619(CustomFonts.packDir()), (class_3264)class_3264.field_14188, (class_9225)new class_9225(true, class_3288.class_3289.field_14280, false));
            if (p != null) {
                consumer.accept(p);
            }
        };
    }
}


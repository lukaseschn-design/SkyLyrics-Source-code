/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1011
 *  net.minecraft.class_1043
 *  net.minecraft.class_1044
 *  net.minecraft.class_12249
 *  net.minecraft.class_156
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_4588
 *  net.minecraft.class_4597
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package dev.lyricsflow.app.sky;

import dev.lyricsflow.app.Core;
import dev.lyricsflow.app.config.Config;
import dev.lyricsflow.app.config.Storage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_12249;
import net.minecraft.class_156;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public final class SkyImage {
    public static final String RECOMMENDED = "4096 x 2048";
    private static final class_2960 ID = class_2960.method_60655((String)"lyricsflow", (String)"sky_picture");
    private static final int MAX_W = 4096;
    private static final int MAX_H = 2048;
    private static String loadedName;
    private static boolean ready;
    private static String error;
    private static int imgW;
    private static int imgH;
    private static final Map<String, class_2960> THUMBS;
    private static final Map<String, int[]> DECODED;
    private static final Set<String> LOADING;
    private static final ExecutorService THUMB_EXEC;
    private static final int TW = 160;
    private static final int TH = 80;
    private static List<String> cached;
    private static long cachedAt;

    private SkyImage() {
    }

    public static Path dir() {
        return Storage.dir().resolve("sky");
    }

    public static List<String> files() {
        try {
            Files.createDirectories(SkyImage.dir());
            try (Stream<Path> s = Files.list(SkyImage.dir())) {
                return s.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).map(p -> p.getFileName().toString()).filter(n -> {
                    String l = n.toLowerCase(Locale.ROOT);
                    return l.endsWith(".png") || l.endsWith(".jpg") || l.endsWith(".jpeg");
                }).sorted().toList();
            }
        } catch (Exception e) {
            return List.of();
        }
    }

    public static String error() {
        return error;
    }

    public static String size() {
        return ready ? imgW + " x " + imgH : "";
    }

    public static class_2960 thumb(String name) {
        class_2960 id = THUMBS.get(name);
        if (id != null) {
            return id;
        }
        int[] px = DECODED.remove(name);
        if (px != null) {
            class_1011 ni = new class_1011(160, 80, false);
            for (int y = 0; y < 80; ++y) {
                for (int x = 0; x < 160; ++x) {
                    ni.method_61941(x, y, px[y * 160 + x]);
                }
            }
            id = class_2960.method_60655((String)"lyricsflow", (String)("sky_thumb/" + Integer.toHexString(name.hashCode())));
            class_310.method_1551().method_1531().method_4616(id, (class_1044)new class_1043(() -> "lyricsflow sky preview", ni));
            THUMBS.put(name, id);
            return id;
        }
        if (LOADING.add(name)) {
            THUMB_EXEC.execute(() -> {
                try {
                    BufferedImage src = ImageIO.read(SkyImage.dir().resolve(name).toFile());
                    if (src == null) {
                        return;
                    }
                    BufferedImage img = new BufferedImage(160, 80, 2);
                    Graphics2D g = img.createGraphics();
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.drawImage(src, 0, 0, 160, 80, null);
                    g.dispose();
                    int[] out = new int[12800];
                    for (int y = 0; y < 80; ++y) {
                        for (int x = 0; x < 160; ++x) {
                            out[y * 160 + x] = img.getRGB(x, y) | 0xFF000000;
                        }
                    }
                    DECODED.put(name, out);
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            });
        }
        return null;
    }

    public static void reload() {
        for (class_2960 id : THUMBS.values()) {
            class_310.method_1551().method_1531().method_4615(id);
        }
        THUMBS.clear();
        LOADING.clear();
        cachedAt = 0L;
        loadedName = null;
        ready = false;
    }

    public static void openFolder() {
        try {
            Files.createDirectories(SkyImage.dir(), new FileAttribute[0]);
            class_156.method_668().method_60932(SkyImage.dir());
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static String chosen() {
        List<String> all;
        Config c = Core.cfg();
        long now = System.currentTimeMillis();
        if (now - cachedAt > 3000L) {
            cached = SkyImage.files();
            cachedAt = now;
        }
        if ((all = cached).isEmpty()) {
            return null;
        }
        if (c.skyImageFile != null && all.contains(c.skyImageFile)) {
            return c.skyImageFile;
        }
        return all.get(0);
    }

    public static boolean active() {
        Config c = Core.cfg();
        if (!c.skyImage) {
            return false;
        }
        String name = SkyImage.chosen();
        if (name == null) {
            error = "none";
            return false;
        }
        if (!name.equals(loadedName)) {
            SkyImage.load(name);
        }
        return ready;
    }

    private static void load(String name) {
        loadedName = name;
        ready = false;
        error = "";
        try {
            BufferedImage src = ImageIO.read(SkyImage.dir().resolve(name).toFile());
            if (src == null) {
                error = "format";
                return;
            }
            int w = src.getWidth();
            int h = src.getHeight();
            int tw = Math.max(2048, Math.min(4096, w));
            int th = tw / 2;
            BufferedImage img = src;
            if (tw != w || th != h) {
                img = new BufferedImage(tw, th, 2);
                Graphics2D g = img.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(src, 0, 0, tw, th, null);
                g.dispose();
            }
            class_1011 ni = new class_1011(tw, th, false);
            for (int y = 0; y < th; ++y) {
                for (int x = 0; x < tw; ++x) {
                    ni.method_61941(x, y, img.getRGB(x, y) | 0xFF000000);
                }
            }
            class_310.method_1551().method_1531().method_4616(ID, (class_1044)new class_1043(() -> "lyricsflow sky picture", ni));
            imgW = w;
            imgH = h;
            ready = true;
        }
        catch (Throwable e) {
            error = "read";
            Core.LOG.warn("[SkyLyrics] Could not load the sky picture {}", (Object)name, (Object)e);
        }
    }

    static void draw(class_4597 buffers, Matrix4f base, double dist, float alpha, float brightness) {
        if (!ready || alpha < 0.01f) {
            return;
        }
        class_4588 vc = buffers.method_73477(class_12249.method_76022((class_2960)ID));
        int b = Math.round(255.0f * Math.max(0.0f, Math.min(1.0f, brightness)));
        int col = Math.round(255.0f * Math.min(1.0f, alpha)) << 24 | b << 16 | b << 8 | b;
        int na = 64;
        int ne = 32;
        for (int j = 0; j < ne; ++j) {
            double e0 = -90.0 + 180.0 * (double)j / (double)ne;
            double e1 = -90.0 + 180.0 * (double)(j + 1) / (double)ne;
            float v0 = (float)(0.5 - e0 / 180.0);
            float v1 = (float)(0.5 - e1 / 180.0);
            for (int i = 0; i < na; ++i) {
                double a0 = 360.0 * (double)i / (double)na;
                double a1 = 360.0 * (double)(i + 1) / (double)na;
                float u0 = (float)i / (float)na;
                float u1 = (float)(i + 1) / (float)na;
                float[] p00 = SkyImage.p(a0, e0, dist);
                float[] p10 = SkyImage.p(a1, e0, dist);
                float[] p11 = SkyImage.p(a1, e1, dist);
                float[] p01 = SkyImage.p(a0, e1, dist);
                SkyImage.v(vc, base, p00, u0, v0, col);
                SkyImage.v(vc, base, p10, u1, v0, col);
                SkyImage.v(vc, base, p11, u1, v1, col);
                SkyImage.v(vc, base, p01, u0, v1, col);
                SkyImage.v(vc, base, p01, u0, v1, col);
                SkyImage.v(vc, base, p11, u1, v1, col);
                SkyImage.v(vc, base, p10, u1, v0, col);
                SkyImage.v(vc, base, p00, u0, v0, col);
            }
        }
    }

    private static float[] p(double azDeg, double elDeg, double d) {
        double a = Math.toRadians(azDeg);
        double e = Math.toRadians(elDeg);
        double ce = Math.cos(e);
        return new float[]{(float)(-Math.sin(a) * ce * d), (float)(Math.sin(e) * d), (float)(Math.cos(a) * ce * d)};
    }

    private static void v(class_4588 vc, Matrix4f m, float[] p, float u, float v, int col) {
        vc.method_22918((Matrix4fc)m, p[0], p[1], p[2]).method_39415(col).method_22913(u, v).method_60803(0xF000F0);
    }

    static {
        error = "";
        THUMBS = new ConcurrentHashMap<String, class_2960>();
        DECODED = new ConcurrentHashMap<String, int[]>();
        LOADING = ConcurrentHashMap.newKeySet();
        THUMB_EXEC = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "SkyLyrics sky previews");
            t.setDaemon(true);
            return t;
        });
        cached = List.of();
    }
}


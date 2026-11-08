package dev.lyricsflow.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.config.LFConfig;
import dev.lyricsflow.lyrics.Lyrics;
import dev.lyricsflow.media.MediaManager;
import dev.lyricsflow.mixin.GameRendererAccessor;
import dev.lyricsflow.perf.AutoPerf;
import dev.lyricsflow.render.AirLyrics;
import dev.lyricsflow.render.Fonts;
import dev.lyricsflow.render.Gfx;
import dev.lyricsflow.render.Hud;
import dev.lyricsflow.render.IrisCompat;
import dev.lyricsflow.render.Proj;
import dev.lyricsflow.render.Themes;
import dev.lyricsflow.util.Anim;
import dev.lyricsflow.util.Col;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.class_12206;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1922;
import net.minecraft.class_1944;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2374;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_4063;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_5250;
import net.minecraft.class_758;
import net.minecraft.class_7833;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public final class WorldLyrics {
    private static final float ENTER_MS = 300.0f;
    private static final float EXIT_MS = 650.0f;
    private static final double SKY_DIST = 48.0;
    private static final int LIGHT = 0xF000F0;
    private static final List<Placed> visible = new ArrayList<Placed>();
    private static final Set<Long> shown = new HashSet<Long>();
    private static long lastT = -1L;
    private static long songSeq = -1L;
    private static float pauseFade = 1.0f;
    private static float baseYaw;
    private static final double[] P;
    private static int layer;
    private static boolean ghostNow;
    private static float layerStep;
    private static final String[] NOTES;
    private static final long NOTE_EVERY = 1700L;
    private static Lyrics.Line noteLine;
    private static long noteSlot;
    private static long noteSong;
    private static Set<Placed> ghostOnly;

    private WorldLyrics() {
    }

    static void onEvent(MediaManager.Event e) {
        if (e.type() == MediaManager.EventType.SONG || e.type() == MediaManager.EventType.STOP) {
            visible.clear();
            shown.clear();
            AirLyrics.clear();
        }
    }

    public static void clear() {
        visible.clear();
        shown.clear();
        AirLyrics.clear();
    }

    static double skyDistance() {
        return WorldLyrics.skyDist();
    }

    private static boolean chunked(LFConfig c) {
        return c.lyricsStyle == LFConfig.LyricsStyle.FLOATING || WorldLyrics.skyWords(c);
    }

    private static boolean skyWords(LFConfig c) {
        return c.lyricsStyle == LFConfig.LyricsStyle.SKY && c.skyWords;
    }

    private static boolean inWorld(LFConfig c) {
        return c.lyricsStyle != LFConfig.LyricsStyle.SCREEN;
    }

    private static List<int[]> chunks(Lyrics.Line line, LFConfig c, long seed) {
        ArrayList<int[]> out = new ArrayList<int[]>();
        int n = line.words.size();
        if (!WorldLyrics.chunked(c)) {
            out.add(new int[]{0, n});
            return out;
        }
        int i = 0;
        int k = 0;
        while (i < n) {
            float r = WorldLyrics.rnd(seed, 300 + k);
            int len = 1;
            int b = Math.min(n, i + len);
            out.add(new int[]{i, b});
            i = b;
            ++k;
        }
        return out;
    }

    static void tick() {
        boolean skyPairs;
        boolean notesOk;
        boolean playing;
        MediaManager mm = MediaManager.get();
        LFConfig c = ConfigManager.get();
        if (AirLyrics.handles(c)) {
            visible.clear();
            return;
        }
        Lyrics lyrics = mm.lyrics();
        long t = mm.position();
        if (mm.songSeq() != songSeq) {
            songSeq = mm.songSeq();
            shown.clear();
            visible.clear();
            baseYaw = Proj.yaw;
        }
        pauseFade = Anim.approach(pauseFade, (playing = mm.isPlaying()) ? 1.0f : 0.0f, playing ? 4.0f : 1.1f);
        boolean noLyrics = lyrics == null || lyrics.lines.isEmpty();
        boolean bl = notesOk = c.musicNotes && (!noLyrics || mm.lyricsState() == MediaManager.LyricsState.NOT_FOUND);
        if (t < 0L || noLyrics && !notesOk) {
            visible.clear();
            return;
        }
        if (lastT >= 0L && t < lastT - 1500L) {
            visible.clear();
            shown.clear();
        }
        lastT = t;
        Lyrics.Line cur = noLyrics ? null : lyrics.lastStartedBefore(t);
        boolean bl2 = skyPairs = c.lyricsStyle == LFConfig.LyricsStyle.SKY && !c.skyWords;
        if (notesOk && playing && WorldLyrics.quiet(lyrics, cur, t)) {
            cur = WorldLyrics.noteLine(t);
        }
        if (cur != null && (float)t < (float)cur.end + 650.0f && t - cur.start < 6000L && !cur.words.isEmpty()) {
            long seed = (long)cur.index * 7919L + songSeq * 104729L;
            for (int[] ch : WorldLyrics.chunks(cur, c, seed)) {
                if (WorldLyrics.find(cur, ch[0]) != null || shown.contains((long)cur.index * 1000L + (long)ch[0])) continue;
                long st = cur.words.get((int)ch[0]).start;
                if (WorldLyrics.chunked(c) && t < st - 80L || WorldLyrics.chunked(c) && t > cur.words.get((int)(ch[1] - 1)).end + 1200L) continue;
                shown.add((long)cur.index * 1000L + (long)ch[0]);
                Placed p = new Placed(cur, ch[0], ch[1], seed + (long)ch[0] * 613L);
                WorldLyrics.place(p, c);
                if (ch[0] == 0) {
                    for (Placed o : visible) {
                        if (cur.index < 0 && o.line.index < 0 || skyPairs && cur.index % 2 == 1 && o.line.index == cur.index - 1 || o.exitAt >= 0L || o.line == cur) continue;
                        o.exitAt = WorldLyrics.chunked(c) ? Math.max(o.lastEnd(), Math.min(t, cur.start) + 250L) : Math.max(o.line.start, Math.min(t, cur.start));
                    }
                }
                visible.add(p);
            }
        }
        int limit = WorldLyrics.chunked(c) ? Math.max(2, c.maxLines * 3) : Math.max(skyPairs ? 2 : 1, c.maxLines);
        int alive = 0;
        for (int i = visible.size() - 1; i >= 0; --i) {
            long until;
            long lineEnd;
            long linger = 600L;
            Placed p = visible.get(i);
            if (p.exitAt >= 0L) continue;
            long l = WorldLyrics.chunked(c) ? (long)(c.lyricsStyle == LFConfig.LyricsStyle.FLOATING ? 900 : 1500) : linger;
            if (++alive > limit) {
                p.exitAt = t;
                continue;
            }
            long l2 = lineEnd = p.line.index >= 0 ? p.line.end : 0L;
            if (skyPairs && !noLyrics && p.line.index >= 0 && p.line.index % 2 == 0 && p.line.index + 1 < lyrics.lines.size()) {
                lineEnd = Math.max(lineEnd, lyrics.lines.get((int)(p.line.index + 1)).end);
            }
            if (t <= (until = Math.max(p.lastEnd() + linger, lineEnd))) continue;
            p.exitAt = until;
        }
        float speed = Math.max(0.3f, c.animSpeed);
        Iterator<Placed> it = visible.iterator();
        while (it.hasNext()) {
            boolean gone;
            Placed p = it.next();
            boolean bl3 = gone = p.exitAt >= 0L && (float)t > (float)p.exitAt + 650.0f / speed + (float)(60L * (long)p.n());
            if (!gone && (!p.lookedAway || !(p.viewVis < 0.02f))) continue;
            it.remove();
        }
        if (pauseFade < 0.01f) {
            return;
        }
        Themes.Theme th = Themes.current();
        if (WorldLyrics.inWorld(c)) {
            for (Placed p : visible) {
                WorldLyrics.update(p, t, c, th);
            }
        }
    }

    static void render(class_332 g) {
        LFConfig c = ConfigManager.get();
        if (pauseFade < 0.01f || visible.isEmpty()) {
            return;
        }
        if (WorldLyrics.inWorld(c)) {
            return;
        }
        long t = MediaManager.get().position();
        float speed = Math.max(0.3f, c.animSpeed);
        Themes.Theme th = Themes.current();
        for (Placed p : visible) {
            WorldLyrics.drawScreen(g, p, t, c, th, speed);
        }
    }

    private static void update(Placed p, long t, LFConfig c, Themes.Theme th) {
        class_310 mc = class_310.method_1551();
        if (c.lyricsStyle == LFConfig.LyricsStyle.SKY) {
            if (++p.occlTick % 10 == 1 && mc.field_1687 != null) {
                class_2338 at = class_2338.method_49637((double)Proj.cx, (double)Proj.cy, (double)Proj.cz);
                boolean under = !c.showUnderground && !mc.field_1687.method_8311(at.method_10084()) && mc.field_1687.method_8314(class_1944.field_9284, at) < 4;
                p.occl = under ? 0.0f : 1.0f;
            }
            p.viewVis = 1.0f;
        } else {
            boolean inView;
            double[] a = WorldLyrics.anchor(p, c);
            double dx = a[0] - Proj.cx;
            double dy = a[1] - Proj.cy;
            double dz = a[2] - Proj.cz;
            double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
            float yaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
            float pitch = d < 1.0E-4 ? 0.0f : (float)(-Math.toDegrees(Math.asin(dy / d)));
            float relYaw = Math.abs(WorldLyrics.wrap(yaw - Proj.yaw));
            float relPitch = Math.abs(pitch - Proj.pitch);
            boolean bl = inView = relYaw < 52.0f && relPitch < 42.0f && d < (double)(24.0f * Math.max(1.0f, c.lyricsDistance) + 4.0f);
            if (c.lyricsStyle == LFConfig.LyricsStyle.WALL) {
                boolean bl2 = inView = d < 40.0;
            }
            if (!inView && p.viewVis > 0.5f) {
                p.lookedAway = true;
            }
            p.viewVis = Anim.approach(p.viewVis, inView && !p.lookedAway ? 1.0f : 0.0f, p.lookedAway ? 9.0f : 12.0f);
            if (++p.occlTick % 6 == 1 && mc.field_1687 != null && mc.field_1724 != null) {
                class_243 from = new class_243(Proj.cx, Proj.cy, Proj.cz);
                class_243 to = new class_243(a[0], a[1], a[2]);
                class_3965 hit = mc.field_1687.method_17742(new class_3959(from, to, class_3959.class_3960.field_23142, class_3959.class_242.field_1348, (class_1297)mc.field_1724));
                p.occl = 1.0f;
            }
            if (c.lyricsStyle == LFConfig.LyricsStyle.WALL) {
                p.occl = 1.0f;
            }
        }
        p.occlVis = Anim.approach(p.occlVis, p.occl, 6.0f);
    }

    private static double[] anchor(Placed p, LFConfig c) {
        if (c.lyricsStyle == LFConfig.LyricsStyle.SKY && !c.viewLock) {
            return new double[]{p.ax, p.ay, p.az};
        }
        if (c.lyricsStyle == LFConfig.LyricsStyle.SKY) {
            float yaw = c.viewLock ? Proj.yaw + p.yaw : p.yaw;
            float pitch = c.viewLock ? Proj.pitch + p.pitch : p.pitch;
            double[] d = Proj.dir(yaw, pitch);
            return new double[]{Proj.cx + d[0] * 48.0, Proj.cy + d[1] * 48.0, Proj.cz + d[2] * 48.0};
        }
        if (c.viewLock) {
            double[] d = Proj.dir(Proj.yaw + p.yaw, Proj.pitch + p.pitch);
            return new double[]{Proj.cx + d[0] * p.dist, Proj.cy + d[1] * p.dist, Proj.cz + d[2] * p.dist};
        }
        return new double[]{p.ax, p.ay, p.az};
    }

    private static Quaternionf facing(Placed p, LFConfig c) {
        float pitch;
        float yaw;
        if (p.surf != null) {
            return new Quaternionf((Quaternionfc)p.surf);
        }
        if (c.viewLock) {
            yaw = Proj.yaw + p.yaw;
            pitch = Proj.pitch + p.pitch;
        } else {
            yaw = p.yaw;
            pitch = p.pitch;
        }
        if (c.lyricsStyle == LFConfig.LyricsStyle.SKY) {
            yaw += p.turn;
        }
        pitch = Math.max(-35.0f, Math.min(35.0f, pitch));
        return new Quaternionf().rotationYXZ((float)Math.PI - (float)Math.toRadians(yaw), -((float)Math.toRadians(pitch)), 0.0f);
    }

    private static float em(Placed p, LFConfig c) {
        if (c.lyricsStyle == LFConfig.LyricsStyle.SKY) {
            return (float)(150.0 * Math.tan(Math.toRadians(3.1))) * c.lyricsScale;
        }
        if (c.lyricsStyle == LFConfig.LyricsStyle.FLOATING) {
            return (float)(0.0765 * Math.max(2.5, (double)p.sizeDist)) * c.lyricsScale * p.fit;
        }
        return (float)(0.085 * Math.max(2.5, p.dist)) * c.lyricsScale * p.fit;
    }

    public static boolean lateForShaders() {
        LFConfig c = ConfigManager.get();
        return IrisCompat.shadersOn() && c.lyricsStyle != LFConfig.LyricsStyle.WALL;
    }

    public static void renderWorld(class_4587 ps, class_4597 buffers) {
        WorldLyrics.renderWorld(ps, buffers, false);
    }

    public static void renderGhost(class_4587 ps, class_4597 buffers) {
        LFConfig c = ConfigManager.get();
        if (WorldLyrics.lateForShaders()) {
            return;
        }
        if (AirLyrics.handles(c)) {
            return;
        }
        if (c.lyricsStyle == LFConfig.LyricsStyle.FLOATING) {
            ghostOnly = WorldLyrics.playerCovers();
            if (ghostOnly.isEmpty()) {
                return;
            }
        } else {
            if (c.lyricsStyle != LFConfig.LyricsStyle.SKY) {
                return;
            }
            ghostOnly = null;
        }
        try {
            WorldLyrics.renderWorld(ps, buffers, true);
        }
        finally {
            ghostOnly = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void renderWorld(class_4587 ps, class_4597 buffers, boolean ghost) {
        class_327.class_6415 mode;
        LFConfig c = ConfigManager.get();
        if (AirLyrics.handles(c)) {
            class_310 mc0 = class_310.method_1551();
            if (ghost || !c.enabled || !c.lyricsShown() || Hud.hideForScreenshot || mc0.field_1687 == null || mc0.field_1724 == null) {
                return;
            }
            if (buffers == null) {
                buffers = mc0.method_22940().method_23000();
            }
            AutoPerf.begin();
            try {
                AirLyrics.render(ps, buffers, WorldLyrics.lateForShaders() ? class_327.class_6415.field_33994 : class_327.class_6415.field_33993);
            }
            finally {
                AutoPerf.end();
            }
            return;
        }
        if (!c.enabled || !c.lyricsShown() || !WorldLyrics.inWorld(c) || Hud.hideForScreenshot || visible.isEmpty() || pauseFade < 0.01f) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1724 == null) {
            return;
        }
        if (buffers == null) {
            buffers = mc.method_22940().method_23000();
        }
        class_4184 cam = mc.field_1773.method_19418();
        class_243 cp = cam.method_71156();
        long t = MediaManager.get().position();
        float speed = Math.max(0.3f, c.animSpeed);
        Themes.Theme th = Themes.current();
        boolean sky = c.lyricsStyle == LFConfig.LyricsStyle.SKY;
        boolean wall = c.lyricsStyle == LFConfig.LyricsStyle.WALL;
        class_327.class_6415 class_64152 = mode = wall ? class_327.class_6415.field_33995 : class_327.class_6415.field_33993;
        if (WorldLyrics.lateForShaders() || ghost) {
            mode = class_327.class_6415.field_33994;
        }
        ghostNow = ghost;
        layerStep = wall ? 0.004f : 0.06f;
        float glowBase = c.glow ? 0.65f : 0.0f;
        float beat = c.beatGlow ? Hud.beat : 0.0f;
        GpuBufferSlice oldFog = null;
        if (sky) {
            try {
                oldFog = RenderSystem.getShaderFog();
                class_758 fr = ((GameRendererAccessor)mc.field_1773).lyricsflow$fogRenderer();
                RenderSystem.setShaderFog((GpuBufferSlice)fr.method_71109(class_758.class_4596.field_60101));
            }
            catch (Throwable ignored) {
                oldFog = null;
            }
        }
        for (Placed p : visible) {
            if (ghost && ghostOnly != null && !ghostOnly.contains(p)) continue;
            if (!ghost && c.lyricsStyle == LFConfig.LyricsStyle.FLOATING) {
                p.coverFade = Anim.approach(p.coverFade, WorldLyrics.overPlayer(p, c) ? 0.35f : 1.0f, 6.0f);
            }
            String font = WorldLyrics.fontOf(p, c);
            float globalA = (ghost ? 1.0f : p.coverFade) * pauseFade * Hud.focusA * c.textOpacity * p.occlVis * (sky ? 1.0f : Anim.easeInOut(p.viewVis)) * (ghost ? 0.3f : 1.0f);
            if (globalA < 0.02f) continue;
            double[] a = WorldLyrics.anchor(p, c);
            float em = WorldLyrics.em(p, c);
            ps.method_22903();
            ps.method_22904(a[0] - cp.field_1352, a[1] - cp.field_1351, a[2] - cp.field_1350);
            Quaternionf face = WorldLyrics.facing(p, c);
            ps.method_22907((Quaternionfc)face);
            if (!wall && p.surf == null) {
                Vector3f n = face.transform(new Vector3f(0.0f, 0.0f, 1.0f));
                double vx = cp.field_1352 - a[0];
                double vy = cp.field_1351 - a[1];
                double vz = cp.field_1350 - a[2];
                if ((double)n.x * vx + (double)n.y * vy + (double)n.z * vz < 0.0) {
                    ps.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees(180.0f));
                }
            }
            ps.method_22907((Quaternionfc)class_7833.field_40718.rotationDegrees(-p.tilt));
            for (int j = 0; j < p.n(); ++j) {
                Lyrics.Word w = p.line.words.get(p.w0 + j);
                float[] s = WorldLyrics.wordAnim(p, j, w, t, c, speed);
                if (s == null) continue;
                float alpha = s[0] * globalA;
                float dx = s[1];
                float dy = s[2];
                float sc = s[3];
                double bob = wall ? 0.0 : Math.sin(Anim.seconds() * 1.6 + (double)j * 0.9 + (double)p.seed) * (sky ? 0.02 : 0.04);
                float size = p.wsize[j] * sc;
                ps.method_22903();
                ps.method_46416((p.wx0[j] + dx) * em, -(p.wy0[j] + dy + (float)bob) * em, 0.0f);
                if (p.wrot[j] != 0.0f) {
                    ps.method_22907((Quaternionfc)class_7833.field_40718.rotationDegrees(-p.wrot[j]));
                }
                float k = em * size / Fonts.height(font);
                ps.method_22905(k, -k, k);
                if (wall) {
                    layerStep = 0.006f / Math.max(1.0E-4f, k);
                }
                boolean sung = t >= w.start && t < w.end + 80L;
                float glow = glowBase + (sung ? 0.35f : 0.0f) + beat * 0.5f;
                String word = Lyrics.show(w.text);
                Matrix4f wm = ps.method_23760().method_23761();
                if (s[4] < 1.0f && !word.isEmpty()) {
                    String part = word.substring(0, Math.max(1, (int)Math.ceil((float)word.length() * s[4])));
                    class_327 mf = class_310.method_1551().field_1772;
                    float fh = Fonts.height(font);
                    float shift = (Fonts.unitWidth(part, font, mf) - Fonts.unitWidth(word, font, mf)) * fh / 2.0f;
                    wm = new Matrix4f((Matrix4fc)wm).translate(shift, 0.0f, 0.0f);
                    word = part;
                }
                WorldLyrics.neon3d(wm, word, font, th, alpha, c.twoColor, glow, buffers, mode);
                ps.method_22909();
            }
            ps.method_22909();
        }
        if (buffers instanceof class_4597.class_4598) {
            class_4597.class_4598 bs = (class_4597.class_4598)buffers;
            bs.method_22993();
        }
        if (oldFog != null) {
            RenderSystem.setShaderFog((GpuBufferSlice)oldFog);
        }
    }

    private static void neon3d(Matrix4f m, String s, String font, Themes.Theme th, float alpha, boolean twoColor, float glow, class_4597 buf, class_327.class_6415 mode) {
        class_327 f = class_310.method_1551().field_1772;
        layer = 0;
        class_5250 comp = Fonts.text(s, font);
        float h = Fonts.height(font);
        float w = Fonts.unitWidth(s, font, f) * h;
        float x = -w / 2.0f;
        float y = -h / 2.0f + h * Fonts.yShift(font, ConfigManager.get().allCaps);
        float o = h * 0.055f;
        if (ghostNow) {
            WorldLyrics.draw(f, (class_2561)comp, x, y, Col.alpha(th.main(), alpha), m, buf, mode);
            return;
        }
        int shadow = ConfigManager.get().textShadow;
        boolean clean = ConfigManager.get().textStyle == LFConfig.TextStyle.CLEAN;
        int custom = ConfigManager.get().shadowColor;
        int sc;
        if (shadow == 1) {
            sc = custom != 0 ? custom | 0xFF000000 : (clean ? -15396054 : Col.darken(th.a(), 0.75f));
            WorldLyrics.draw(f, (class_2561)comp, x + o * 0.6f, y + o * 1.0f, Col.alpha(sc, alpha), m, buf, mode);
        } else if (shadow == 2) {
            sc = custom != 0 ? custom | 0xFF000000 : -16120302;
            WorldLyrics.draw(f, (class_2561)comp, x + o * 1.3f, y + o * 2.0f, Col.alpha(sc, alpha), m, buf, mode);
            WorldLyrics.draw(f, (class_2561)comp, x + o * 0.7f, y + o * 1.1f, Col.alpha(sc, alpha), m, buf, mode);
        }
        if (clean) {
            WorldLyrics.draw(f, (class_2561)comp, x, y, Col.alpha(Col.lerp(th.main(), -1, 0.2f), alpha), m, buf, mode);
            WorldLyrics.seeThrough(f, (class_2561)comp, x, y, Col.lerp(th.main(), -1, 0.2f), alpha, m, buf, mode);
            return;
        }
        float tcs = ConfigManager.get().twoColorStrength;
        if (tcs > 0.01f) {
            int tc = ConfigManager.get().twoColorColor;
            int ca = tc != 0 ? tc | 0xFF000000 : th.a();
            int cb = tc != 0 ? tc | 0xFF000000 : th.b();
            float oo = o * (0.35f + 0.65f * tcs);
            float ta = alpha * Math.min(1.0f, 0.3f + 0.7f * tcs);
            WorldLyrics.draw(f, (class_2561)comp, x - oo, y + oo * 0.5f, Col.alpha(ca, ta), m, buf, mode);
            WorldLyrics.draw(f, (class_2561)comp, x + oo, y - oo * 0.5f, Col.alpha(cb, ta), m, buf, mode);
        }
        WorldLyrics.draw(f, (class_2561)comp, x, y, Col.alpha(th.main(), alpha), m, buf, mode);
        WorldLyrics.seeThrough(f, (class_2561)comp, x, y, th.main(), alpha, m, buf, mode);
    }

    private static void seeThrough(class_327 f, class_2561 comp, float x, float y, int color, float alpha, Matrix4f m, class_4597 buf, class_327.class_6415 mode) {
        if (mode != class_327.class_6415.field_33993) {
            return;
        }
        WorldLyrics.draw(f, comp, x, y, Col.alpha(color, alpha * 0.32f), m, buf, class_327.class_6415.field_33994);
    }

    private static void draw(class_327 f, class_2561 c, float x, float y, int color, Matrix4f m, class_4597 buf, class_327.class_6415 mode) {
        Matrix4f mm = new Matrix4f((Matrix4fc)m).translate(0.0f, 0.0f, layerStep * (float)layer++);
        if (color >>> 24 < 26) {
            return;
        }
        f.method_27522(c, x, y, color, false, mm, buf, mode, 0, 0xF000F0);
    }

    private static float[] wordAnim(Placed p, int j, Lyrics.Word w, long t, LFConfig c, float speed) {
        long held;
        float alpha;
        float ein = Anim.progress(t, w.start - 40L, (float)(w.start - 40L) + 300.0f / speed);
        if (ein <= 0.0f) {
            return null;
        }
        float dx = 0.0f;
        float dy = 0.0f;
        float sc = 1.0f;
        float typed = 1.0f;
        switch (c.enterAnim) {
            case NEON: {
                float ms = (float)(t - (w.start - 40L)) * speed;
                alpha = Anim.clamp01(ms / 140.0f);
                sc = 1.0f + 0.22f * (1.0f - Anim.easeOutCubic(Anim.clamp01(ms / 240.0f)));
                typed = Anim.clamp01(ms / 230.0f);
                break;
            }
            case FADE: {
                alpha = Anim.easeInOut(ein);
                break;
            }
            case RISE: {
                float e;
                alpha = e = Anim.easeOutCubic(ein);
                dy = (1.0f - e) * 0.9f;
                break;
            }
            default: {
                float e = Anim.easeOutBack(ein);
                alpha = Anim.easeOutCubic(ein);
                dy = (1.0f - Anim.easeOutCubic(ein)) * 0.45f;
                sc = 0.82f + 0.18f * e;
            }
        }
        if (p.exitAt >= 0L) {
            long start = p.exitAt + 55L * (long)j;
            float eo = Anim.progress(t, start, (float)start + 650.0f / speed);
            if (eo >= 1.0f) {
                return null;
            }
            float e = Anim.easeInCubic(eo);
            alpha *= 1.0f - Anim.easeInOut(eo);
            switch (c.exitAnim) {
                case FLOAT_UP: {
                    dy -= e * 1.6f;
                    sc *= 1.0f + e * 0.08f;
                    break;
                }
                case DRIFT: {
                    dx += e * 1.8f * (float)(WorldLyrics.rnd(p.seed, 90) > 0.5f ? 1 : -1);
                    dy -= e * 0.35f;
                    break;
                }
            }
        }
        if (p.w0 + j == p.line.words.size() - 1 && t >= w.start && (held = Math.max(w.end, p.line.end) - w.start) >= 650L && t < w.start + held + 300L) {
            float ph = (float)((t - w.start) % 860L) / 860.0f;
            float beat = (float)(Math.exp(-Math.pow((ph - 0.08f) / 0.05f, 2.0)) + 0.55 * Math.exp(-Math.pow((ph - 0.27f) / 0.05f, 2.0)));
            float fadeIn = Anim.clamp01((float)(t - w.start) / 200.0f);
            sc *= 1.0f + 0.085f * beat * fadeIn;
        }
        return new float[]{alpha, dx, dy, sc, typed};
    }

    private static double clearance(float yaw, float pitch, double dist) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1724 == null) {
            return dist;
        }
        class_243 from = new class_243(Proj.cx, Proj.cy, Proj.cz);
        double[] d = Proj.dir(yaw, pitch);
        class_243 to = new class_243(Proj.cx + d[0] * dist, Proj.cy + d[1] * dist, Proj.cz + d[2] * dist);
        class_3965 hit = mc.field_1687.method_17742(new class_3959(from, to, class_3959.class_3960.field_23142, class_3959.class_242.field_1348, (class_1297)mc.field_1724));
        if (hit.method_17783() != class_239.class_240.field_1332) {
            return dist;
        }
        return hit.method_17784().method_1022(from);
    }

    private static Placed findIndex(int index) {
        for (Placed o : visible) {
            if (o.line.index != index) continue;
            return o;
        }
        return null;
    }

    private static double skyDist() {
        class_310 mc = class_310.method_1551();
        double far = (double)mc.field_1773.method_32796() * 0.85;
        return Math.max(20.0, Math.min(far, (double)ConfigManager.get().skyDistance));
    }

    private static double cloudY() {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1690.method_1632() == class_4063.field_18162 || !mc.field_1687.method_8597().comp_642()) {
            return Double.NaN;
        }
        try {
            Float h = (Float)mc.field_1687.method_75728().method_75695(class_12206.field_63748, new class_243(Proj.cx, Proj.cy, Proj.cz));
            return h == null ? Double.NaN : (double)h.floatValue();
        }
        catch (Throwable t) {
            return 192.33;
        }
    }

    private static int blocked(float yaw, float pitch, double dist, float halfWidthDeg) {
        int n = 0;
        if (WorldLyrics.clearance(yaw, pitch, dist) < dist - 0.3) {
            ++n;
        }
        if (WorldLyrics.clearance(yaw - halfWidthDeg, pitch, dist) < dist - 0.3) {
            ++n;
        }
        if (WorldLyrics.clearance(yaw + halfWidthDeg, pitch, dist) < dist - 0.3) {
            ++n;
        }
        return n;
    }

    private static boolean inClouds(float pitch, double dist, double cloud) {
        if (Double.isNaN(cloud)) {
            return false;
        }
        double y = Proj.cy + Proj.dir(0.0f, pitch)[1] * dist;
        return Math.abs(y - cloud) < 6.0;
    }

    static float wrap(float deg) {
        if ((deg %= 360.0f) > 180.0f) {
            deg -= 360.0f;
        }
        if (deg < -180.0f) {
            deg += 360.0f;
        }
        return deg;
    }

    private static boolean quiet(Lyrics lyrics, Lyrics.Line cur, long t) {
        if (cur != null && cur.index >= 0 && t <= cur.end + 1200L) {
            return false;
        }
        if (lyrics != null) {
            for (Lyrics.Line l : lyrics.lines) {
                if (l.start <= t) continue;
                return l.start - t > 2600L;
            }
        }
        return true;
    }

    private static Lyrics.Line noteLine(long t) {
        long slot = t / 1700L;
        if (slot != noteSlot || noteSong != songSeq || noteLine == null) {
            noteSlot = slot;
            noteSong = songSeq;
            long st = slot * 1700L;
            noteLine = Lyrics.Line.notes(-1 - (int)(slot % 100000L), NOTES[(int)(slot % (long)NOTES.length)], st, st + 2900L);
        }
        return noteLine;
    }

    private static String fontOf(Placed p, LFConfig c) {
        return p.line.index < 0 ? "minecraft" : c.font;
    }

    private static Placed find(Lyrics.Line l, int w0) {
        for (Placed p : visible) {
            if (p.line != l || p.w0 != w0) continue;
            return p;
        }
        return null;
    }

    private static float rnd(long seed, int k) {
        return Anim.hash01(seed * 31L + (long)k);
    }

    private static boolean overPlayer(Placed p, LFConfig c) {
        double ez;
        double ey;
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1724 == null) {
            return false;
        }
        boolean third = !mc.field_1690.method_31044().method_31034();
        double[] a = WorldLyrics.anchor(p, c);
        double ex = a[0] - Proj.cx;
        double len = Math.sqrt(ex * ex + (ey = a[1] - Proj.cy) * ey + (ez = a[2] - Proj.cz) * ez);
        if (len < 0.001) {
            return false;
        }
        float ly = (float)Math.toDegrees(Math.atan2(-ex, ez));
        float lp = (float)(-Math.toDegrees(Math.asin(ey / len)));
        float k = (float)(p.dist / len);
        for (class_1657 pl : mc.field_1687.method_18456()) {
            class_238 bb;
            class_243 d;
            double dist;
            if (pl == mc.field_1724 && !third || (dist = (d = (bb = pl.method_5829()).method_1005().method_1023(Proj.cx, Proj.cy, Proj.cz)).method_1033()) > 48.0 || dist < 0.3 || dist < len) continue;
            float yaw = (float)Math.toDegrees(Math.atan2(-d.field_1352, d.field_1350));
            float pitch = (float)(-Math.toDegrees(Math.asin(d.field_1351 / dist)));
            float hw = (float)Math.toDegrees(Math.atan(bb.method_17939() * 0.5 / dist));
            float hh = (float)Math.toDegrees(Math.atan(bb.method_17940() * 0.5 / dist));
            if (!(Math.abs(WorldLyrics.wrap(ly - yaw)) < hw + p.angW * k) || !(Math.abs(lp - pitch) < hh + p.angH * k)) continue;
            return true;
        }
        return false;
    }

    private static Set<Placed> playerCovers() {
        HashSet<Placed> out = new HashSet<Placed>();
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1724 == null) {
            return out;
        }
        LFConfig c = ConfigManager.get();
        class_243 eye = new class_243(Proj.cx, Proj.cy, Proj.cz);
        boolean third = !mc.field_1690.method_31044().method_31034();
        for (class_1657 pl : mc.field_1687.method_18456()) {
            class_238 bb;
            class_243 d;
            double dist;
            if (pl == mc.field_1724 && !third || (dist = (d = (bb = pl.method_5829()).method_1005().method_1020(eye)).method_1033()) > 48.0 || dist < 0.3) continue;
            float yaw = (float)Math.toDegrees(Math.atan2(-d.field_1352, d.field_1350));
            float pitch = (float)(-Math.toDegrees(Math.asin(d.field_1351 / dist)));
            float hw = (float)Math.toDegrees(Math.atan(bb.method_17939() * 0.5 / dist));
            float hh = (float)Math.toDegrees(Math.atan(bb.method_17940() * 0.5 / dist));
            for (Placed p : visible) {
                double ez;
                double ey;
                double[] a = WorldLyrics.anchor(p, c);
                double ex = a[0] - Proj.cx;
                double len = Math.sqrt(ex * ex + (ey = a[1] - Proj.cy) * ey + (ez = a[2] - Proj.cz) * ez);
                if (len < dist) continue;
                float ly = (float)Math.toDegrees(Math.atan2(-ex, ez));
                float lp = (float)(-Math.toDegrees(Math.asin(ey / len)));
                float lw = (float)Math.toDegrees(Math.atan(1.6 / len));
                float lh = (float)Math.toDegrees(Math.atan(0.5 / len));
                if (!(Math.abs(WorldLyrics.wrap(ly - yaw)) < hw + lw) || !(Math.abs(lp - pitch) < hh + lh)) continue;
                out.add(p);
            }
        }
        return out;
    }

    private static List<float[]> avoidBoxes() {
        class_1309 le;
        ArrayList<float[]> out = new ArrayList<float[]>();
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1724 == null) {
            return out;
        }
        class_243 eye = new class_243(Proj.cx, Proj.cy, Proj.cz);
        ArrayList<class_1297> list = new ArrayList<class_1297>();
        for (class_1657 class_16572 : mc.field_1687.method_18456()) {
            if (class_16572 == mc.field_1724) continue;
            list.add(class_16572);
        }
        class_1297 class_12972 = mc.field_1692;
        if (class_12972 instanceof class_1309 && !list.contains(le = (class_1309)class_12972)) {
            list.add(le);
        }
        for (class_1297 class_12973 : list) {
            class_238 bb = class_12973.method_5829();
            class_243 d = bb.method_1005().method_1020(eye);
            double dist = d.method_1033();
            if (dist > 40.0 || dist < 0.3) continue;
            float yaw = (float)Math.toDegrees(Math.atan2(-d.field_1352, d.field_1350));
            float pitch = (float)(-Math.toDegrees(Math.asin(d.field_1351 / dist)));
            float hw = (float)Math.toDegrees(Math.atan((bb.method_17939() * 0.5 + 0.9) / dist));
            float hh = (float)Math.toDegrees(Math.atan((bb.method_17940() * 0.5 + 1.0) / dist));
            out.add(new float[]{yaw, pitch, hw, hh});
        }
        return out;
    }

    private static float crowd(class_243 at) {
        float pen = 0.0f;
        for (Placed o : visible) {
            double d = Math.sqrt((o.ax - at.field_1352) * (o.ax - at.field_1352) + (o.ay - at.field_1351) * (o.ay - at.field_1351) + (o.az - at.field_1350) * (o.az - at.field_1350));
            if (!(d < 3.5)) continue;
            pen += (float)(3.5 - d) * 160.0f;
        }
        return pen;
    }

    private static boolean empty(class_310 mc, class_2338 pos) {
        return mc.field_1687.method_8320(pos).method_26220((class_1922)mc.field_1687, pos).method_1110() && mc.field_1687.method_8316(pos).method_15769();
    }

    private static boolean solid(class_310 mc, class_2338 pos) {
        return !mc.field_1687.method_8320(pos).method_26220((class_1922)mc.field_1687, pos).method_1110() || !mc.field_1687.method_8316(pos).method_15769();
    }

    private static void wallPlace(Placed p, long s) {
        class_310 mc = class_310.method_1551();
        class_243 eye = new class_243(Proj.cx, Proj.cy, Proj.cz);
        class_243 best = null;
        class_243 bestN = null;
        Quaternionf bestQ = null;
        float bestScore = -1.0E9f;
        if (mc.field_1687 != null && mc.field_1724 != null) {
            float score;
            for (int k = 0; k < 10; ++k) {
                float pitch;
                float yaw = Proj.yaw + (k == 0 ? 0.0f : (WorldLyrics.rnd(s, 200 + k * 2) * 2.0f - 1.0f) * 35.0f);
                double[] d = Proj.dir(yaw, pitch = Math.max(-30.0f, Math.min(25.0f, Proj.pitch * 0.5f + (k == 0 ? 0.0f : (WorldLyrics.rnd(s, 201 + k * 2) - 0.5f) * 24.0f))));
                class_3965 hit = mc.field_1687.method_17742(new class_3959(eye, eye.method_1031(d[0] * 16.0, d[1] * 16.0, d[2] * 16.0), class_3959.class_3960.field_17558, class_3959.class_242.field_1347, (class_1297)mc.field_1724));
                if (hit.method_17783() != class_239.class_240.field_1332) continue;
                class_2350 f = hit.method_17780();
                double dist = hit.method_17784().method_1022(eye);
                if (!f.method_10166().method_10179() || dist < 2.5) continue;
                class_243 n = new class_243((double)f.method_10148(), 0.0, (double)f.method_10165());
                class_243 t = new class_243(n.field_1350, 0.0, -n.field_1352);
                score = 1000.0f - (float)Math.abs(dist - 6.0) * 20.0f - Math.abs(WorldLyrics.wrap(yaw - Proj.yaw)) * 2.0f - WorldLyrics.crowd(hit.method_17784());
                for (int side = -2; side <= 2; ++side) {
                    boolean ok;
                    if (side == 0) continue;
                    class_243 q = hit.method_17784().method_1019(t.method_1021((double)side * 1.1));
                    class_2338 in = class_2338.method_49638((class_2374)q.method_1020(n.method_1021(0.5)));
                    class_2338 out = class_2338.method_49638((class_2374)q.method_1019(n.method_1021(0.5)));
                    boolean bl = ok = !mc.field_1687.method_8320(in).method_26215() && WorldLyrics.empty(mc, out);
                    if (ok) continue;
                    score -= Math.abs(side) == 1 ? 260.0f : 120.0f;
                }
                if (!(score > bestScore)) continue;
                bestScore = score;
                bestN = n;
                best = hit.method_17784().method_1019(n.method_1021(0.04));
                float yawIn = (float)Math.toDegrees(Math.atan2(n.field_1352, -n.field_1350));
                bestQ = new Quaternionf().rotationYXZ((float)Math.PI - (float)Math.toRadians(yawIn), 0.0f, 0.0f);
            }
            if (best == null) {
                float groundBest = -1.0E9f;
                for (int k = 0; k < 8; ++k) {
                    class_243 pos;
                    class_3965 see;
                    float yaw = Proj.yaw + (k == 0 ? 0.0f : (WorldLyrics.rnd(s, 230 + k * 2) * 2.0f - 1.0f) * 25.0f);
                    double dist = 4.5 + (double)WorldLyrics.rnd(s, 231 + k * 2) * 2.5;
                    double[] d = Proj.dir(yaw, 0.0f);
                    class_243 top = eye.method_1031(d[0] * dist, 1.0, d[2] * dist);
                    class_3965 hit = mc.field_1687.method_17742(new class_3959(top, top.method_1031(0.0, -8.0, 0.0), class_3959.class_3960.field_17558, class_3959.class_242.field_1347, (class_1297)mc.field_1724));
                    if (hit.method_17783() != class_239.class_240.field_1332 || hit.method_17780() != class_2350.field_11036 || (see = mc.field_1687.method_17742(new class_3959(eye, pos = hit.method_17784().method_1031(0.0, 0.04, 0.0), class_3959.class_3960.field_23142, class_3959.class_242.field_1348, (class_1297)mc.field_1724))).method_17783() == class_239.class_240.field_1332 && see.method_17784().method_1022(pos) > 0.3 || (score = (float)(k == 0 ? 50 : 0) - WorldLyrics.crowd(pos)) <= groundBest) continue;
                    groundBest = score;
                    best = pos;
                    bestN = new class_243(0.0, 1.0, 0.0);
                    float yawTo = (float)Math.toDegrees(Math.atan2(-(pos.field_1352 - eye.field_1352), pos.field_1350 - eye.field_1350));
                    bestQ = new Quaternionf().rotationYXZ((float)Math.PI - (float)Math.toRadians(yawTo), -((float)Math.toRadians(90.0)), 0.0f);
                }
            }
        }
        if (best == null) {
            double[] d = Proj.dir(Proj.yaw, Math.max(-20.0f, Math.min(20.0f, Proj.pitch)));
            best = eye.method_1031(d[0] * 6.0, d[1] * 6.0, d[2] * 6.0);
        }
        p.ax = best.field_1352;
        p.ay = best.field_1351;
        p.az = best.field_1350;
        class_243 rel = best.method_1020(eye);
        p.dist = Math.max(2.5, rel.method_1033());
        p.yaw = (float)Math.toDegrees(Math.atan2(-rel.field_1352, rel.field_1350));
        p.pitch = p.dist < 1.0E-4 ? 0.0f : (float)(-Math.toDegrees(Math.asin(rel.field_1351 / rel.method_1033())));
        p.surf = bestQ;
        p.surfN = bestQ == null ? null : bestN;
        p.fit = 1.0f;
    }

    private static void fitToSurface(Placed p, LFConfig c, float[] widths) {
        class_310 mc = class_310.method_1551();
        if (p.surf == null || p.surfN == null || mc.field_1687 == null) {
            return;
        }
        float x0 = Float.MAX_VALUE;
        float x1 = -3.4028235E38f;
        float y0 = Float.MAX_VALUE;
        float y1 = -3.4028235E38f;
        for (int j = 0; j < p.n(); ++j) {
            x0 = Math.min(x0, p.wx0[j] - widths[j] / 2.0f);
            x1 = Math.max(x1, p.wx0[j] + widths[j] / 2.0f);
            y0 = Math.min(y0, -p.wy0[j] - p.wsize[j] * 0.45f);
            y1 = Math.max(y1, -p.wy0[j] + p.wsize[j] * 0.45f);
        }
        if (x0 > x1) {
            return;
        }
        Vector3f r = p.surf.transform(new Vector3f(1.0f, 0.0f, 0.0f));
        Vector3f u = p.surf.transform(new Vector3f(0.0f, 1.0f, 0.0f));
        class_243 right = new class_243((double)r.x, (double)r.y, (double)r.z);
        class_243 up = new class_243((double)u.x, (double)u.y, (double)u.z);
        class_243 n = p.surfN;
        class_243 base = new class_243(p.ax, p.ay, p.az);
        float[] scales = new float[]{1.0f, 0.8f, 0.65f, 0.5f};
        double[] slide = new double[]{0.0, 0.5, -0.5, 1.0, -1.0, 1.5, -1.5, 2.0, -2.0, 3.0, -3.0};
        double[] lift = new double[]{0.0, 1.0, -1.0};
        float[] fArray = scales;
        int n2 = fArray.length;
        for (int i = 0; i < n2; ++i) {
            float sc;
            p.fit = sc = fArray[i];
            float em = WorldLyrics.em(p, c);
            for (double dv : lift) {
                for (double dh : slide) {
                    class_243 center = base.method_1019(right.method_1021(dh)).method_1019(up.method_1021(dv));
                    if (!WorldLyrics.fits(mc, center, right, up, n, x0 * em, x1 * em, y0 * em, y1 * em)) continue;
                    p.ax = center.field_1352;
                    p.ay = center.field_1351;
                    p.az = center.field_1350;
                    return;
                }
            }
        }
        p.fit = 0.5f;
    }

    private static boolean fits(class_310 mc, class_243 center, class_243 right, class_243 up, class_243 n, double x0, double x1, double y0, double y1) {
        int nx = Math.max(2, (int)Math.ceil((x1 - x0) / 0.45));
        int ny = Math.max(1, (int)Math.ceil((y1 - y0) / 0.45));
        for (int i = 0; i <= nx; ++i) {
            double x = x0 + (x1 - x0) * (double)i / (double)nx;
            for (int k = 0; k <= ny; ++k) {
                double y = y0 + (y1 - y0) * (double)k / (double)ny;
                class_243 q = center.method_1019(right.method_1021(x)).method_1019(up.method_1021(y));
                class_2338 behind = class_2338.method_49638((class_2374)q.method_1020(n.method_1021(0.3)));
                class_2338 front = class_2338.method_49638((class_2374)q.method_1019(n.method_1021(0.3)));
                if (!WorldLyrics.solid(mc, behind)) {
                    return false;
                }
                if (WorldLyrics.empty(mc, front)) continue;
                return false;
            }
        }
        return true;
    }

    /*
     * Enabled aggressive block sorting
     */
    private static void place(Placed p, LFConfig c) {
        boolean sky;
        boolean shaderMode;
        long s = p.seed;
        boolean random = c.randomPosition;
        float refYaw = c.followView || c.viewLock ? Proj.yaw : baseYaw;
        boolean skyStyle = c.lyricsStyle == LFConfig.LyricsStyle.SKY;
        boolean bl = shaderMode = c.lyricsStyle == LFConfig.LyricsStyle.FLOATING && IrisCompat.shadersOn();
        int tries = random ? (c.lyricsStyle == LFConfig.LyricsStyle.FLOATING ? 40 : 12) : (skyStyle || shaderMode ? 8 : 1);
        double cloud = c.lyricsStyle == LFConfig.LyricsStyle.SCREEN ? Double.NaN : WorldLyrics.cloudY();
        float bestScore = -1.0E9f;
        float bYaw = 0.0f;
        float bPitch = 0.0f;
        float bSx = 0.5f;
        float bSy = 0.3f;
        float bSize = 6.0f;
        float bPY = 0.0f;
        float bPP = 0.0f;
        double bDist = 6.0;
        List<float[]> avoid = c.lyricsStyle == LFConfig.LyricsStyle.FLOATING ? WorldLyrics.avoidBoxes() : List.of();
        block5: for (int k = 0; k < tries; ++k) {
            float r1 = random ? WorldLyrics.rnd(s, 100 + k * 3) * 2.0f - 1.0f : 0.0f;
            float r2 = random ? WorldLyrics.rnd(s, 101 + k * 3) : 0.5f;
            float r3 = random ? WorldLyrics.rnd(s, 102 + k * 3) : 0.5f;
            float yaw = 0.0f;
            float pitch = 0.0f;
            float sx = 0.0f;
            float sy = 0.0f;
            float pY = 0.0f;
            float pP = 0.0f;
            double dist = 8.0;
            float tSize = 6.0f;
            float score = 1000.0f;
            switch (c.lyricsStyle) {
                case SKY: {
                    float f;
                    Placed mate;
                    if (c.skyWords) {
                        dist = WorldLyrics.skyDist();
                        yaw = Proj.yaw + r1 * 36.0f;
                        pitch = Math.max(-55.0f, Math.min(-4.0f, Proj.pitch * 0.5f - 16.0f + (r2 - 0.5f) * 30.0f));
                        int b = WorldLyrics.blocked(yaw, pitch, dist, 6.0f);
                        score -= (float)(b * 450);
                        if (!WorldLyrics.inClouds(pitch, dist, cloud)) break;
                        score -= 500.0f;
                        break;
                    }
                    dist = WorldLyrics.skyDist();
                    float emDeg = (float)Math.toDegrees(Math.atan((double)WorldLyrics.em(p, c) / dist));
                    float h = emDeg * 2.0f;
                    float v = emDeg * 1.9f;
                    int side = p.line.index >= 0 ? (p.line.index % 2 == 0 ? -1 : 1) : 0;
                    Placed placed = mate = side > 0 ? WorldLyrics.findIndex(p.line.index - 1) : null;
                    if (mate != null) {
                        if (k > 0) continue block5;
                        pY = mate.pairYaw;
                        pP = mate.pairPitch;
                        score += 5000.0f;
                    } else {
                        float ry = k == 0 ? 0.0f : r1;
                        float f2 = f = k == 0 ? 0.5f : r2;
                        if (k == 0) {
                            score += 120.0f;
                        }
                        pY = Proj.yaw + ry * 14.0f;
                        pP = Math.min(Proj.pitch - (8.0f + (f - 0.5f) * 6.0f) - (k >= tries / 2 ? 4.0f : 0.0f), -3.0f - v);
                    }
                    yaw = pY + (float)side * h;
                    pitch = pP + (float)side * v;
                    float absYaw = yaw;
                    f = pitch;
                    int b = WorldLyrics.blocked(absYaw, f, dist, 8.0f);
                    score -= (float)(b * 450);
                    if (!WorldLyrics.inClouds(f, dist, cloud)) break;
                    score -= 500.0f;
                    break;
                }
                case FLOATING: {
                    if (shaderMode && !random) {
                        r1 = k == 0 ? 0.0f : WorldLyrics.rnd(s, 100 + k * 3) * 2.0f - 1.0f;
                        r2 = k == 0 ? 0.5f : WorldLyrics.rnd(s, 101 + k * 3);
                        float f = r3 = k == 0 ? 0.5f : WorldLyrics.rnd(s, 102 + k * 3);
                        if (k == 0) {
                            score += 120.0f;
                        }
                    }
                    yaw = c.followView || c.viewLock ? (c.viewLock ? 0.0f : Proj.yaw) + r1 * 13.0f : Proj.yaw + (r1 < 0.0f ? -1.0f : 1.0f) * (8.0f + Math.abs(r1) * 26.0f);
                    float rp = (r2 - 0.5f) * 26.0f;
                    pitch = Math.max(-40.0f, Math.min(28.0f, (c.viewLock ? 0.0f : Proj.pitch * 0.6f) + rp - 9.0f));
                    dist = (shaderMode ? 4.5f + r3 * 2.5f : 5.0f + r3 * 2.5f) * Math.max(0.4f, Math.min(2.0f, c.lyricsDistance));
                    float absYaw = c.viewLock ? Proj.yaw + yaw : yaw;
                    float absPitch = c.viewLock ? Proj.pitch + pitch : pitch;
                    float cy = Math.abs(WorldLyrics.wrap(absYaw - Proj.yaw));
                    float cp = Math.abs(absPitch - Proj.pitch);
                    if (cy < 4.0f && cp < 3.0f) {
                        score -= 250.0f;
                    }
                    for (float[] fArray : avoid) {
                        float dy = Math.abs(WorldLyrics.wrap(absYaw - fArray[0])) - fArray[2] - 12.0f;
                        float dp = Math.abs(absPitch - fArray[1]) - fArray[3] - 6.0f;
                        if (!(dy < 0.0f) || !(dp < 0.0f)) continue;
                        score -= 4000.0f;
                    }
                    if (!avoid.isEmpty() && cy < 14.0f && cp < 10.0f) {
                        score -= 1500.0f;
                    }
                    float hw = (float)Math.toDegrees(Math.atan(2.8 / dist));
                    int n = WorldLyrics.blocked(absYaw, absPitch, dist, hw);
                    score -= (float)(n * 800);
                    float hh = (float)Math.toDegrees(Math.atan(0.7 / dist));
                    int more = WorldLyrics.blocked(absYaw, absPitch - hh, dist, hw) + WorldLyrics.blocked(absYaw, absPitch + hh, dist, hw);
                    score -= (float)((n + more) * 700);
                    if (WorldLyrics.inClouds(absPitch, dist, cloud)) {
                        score -= 500.0f;
                    }
                    tSize = (float)(dist / (double)Math.max(0.4f, Math.min(2.0f, c.lyricsDistance)));
                    double free = WorldLyrics.clearance(absYaw, absPitch, dist + 4.0);
                    if (!(free < dist + 4.0)) break;
                    score -= 800.0f;
                    if (free < 2.2) {
                        score -= 700.0f;
                    }
                    dist = Math.max(1.8, free - 0.8);
                    score -= (float)(8.0 - free) * 10.0f;
                    break;
                }
                case SCREEN: {
                    sx = c.screenLyricsX + (random ? r1 * 0.26f : 0.0f);
                    sy = c.screenLyricsY + (random ? (r2 - 0.5f) * 0.24f : 0.0f);
                    if (!(Math.abs(sx - 0.5f) < 0.18f) || !(Math.abs(sy - 0.5f) < 0.12f)) break;
                    sy = 0.32f;
                }
            }
            float near = 1000.0f;
            for (Placed o : visible) {
                float d;
                if (o.exitAt >= 0L && c.lyricsStyle == LFConfig.LyricsStyle.SCREEN) continue;
                if (c.lyricsStyle == LFConfig.LyricsStyle.SCREEN) {
                    float dx = (o.sx - sx) * 1.6f;
                    float dy = o.sy - sy;
                    d = (float)Math.sqrt(dx * dx + dy * dy) * 100.0f;
                } else {
                    double ez;
                    double ey;
                    double d2;
                    double len;
                    float oy = o.yaw;
                    float op = o.pitch;
                    if (!c.viewLock && c.lyricsStyle != LFConfig.LyricsStyle.WALL && (len = Math.sqrt((d2 = o.ax - Proj.cx) * d2 + (ey = o.ay - Proj.cy) * ey + (ez = o.az - Proj.cz) * ez)) > 0.001) {
                        oy = (float)Math.toDegrees(Math.atan2(-d2, ez));
                        op = (float)(-Math.toDegrees(Math.asin(ey / len)));
                    }
                    float f = Math.abs(WorldLyrics.wrap(oy - yaw));
                    float dp = Math.abs(op - pitch) * 2.2f;
                    d = (float)Math.sqrt(f * f + dp * dp);
                }
                near = Math.min(near, d);
            }
            if ((c.lyricsStyle == LFConfig.LyricsStyle.FLOATING || WorldLyrics.skyWords(c)) && !c.viewLock) {
                float emB = WorldLyrics.skyWords(c) ? WorldLyrics.em(p, c) : (float)(0.0765 * Math.max(2.5, (double)tSize)) * c.lyricsScale;
                int chars = 0;
                for (int j = p.w0; j < p.w1; chars += p.line.words.get((int)j).text.length() + 1, ++j) {
                }
                float cw = (float)Math.toDegrees(Math.atan((double)((float)chars * 0.55f * emB / 2.0f) / dist));
                float ch = (float)Math.toDegrees(Math.atan((double)(0.8f * emB) / dist));
                for (Placed placed : visible) {
                    double ex = placed.ax - Proj.cx;
                    double ey = placed.ay - Proj.cy;
                    double ez = placed.az - Proj.cz;
                    double len = Math.sqrt(ex * ex + ey * ey + ez * ez);
                    if (len < 0.001) continue;
                    float ok = (float)(placed.dist / len);
                    float oy = (float)Math.toDegrees(Math.atan2(-ex, ez));
                    float op = (float)(-Math.toDegrees(Math.asin(ey / len)));
                    if (Math.abs(WorldLyrics.wrap(oy - yaw)) < cw + placed.angW * ok + 3.0f && Math.abs(op - pitch) < ch + placed.angH * ok + 2.5f) {
                        score -= 12000.0f;
                        continue;
                    }
                    if (!(Math.abs(WorldLyrics.wrap(oy - yaw)) < cw + placed.angW * ok + 6.0f) || !(Math.abs(op - pitch) < ch + placed.angH * ok + 4.0f)) continue;
                    score -= 600.0f;
                }
            }
            if (near < 22.0f) {
                score -= (22.0f - near) * 40.0f;
            }
            if (!((score += Math.min(near, 60.0f) * 4.0f) > bestScore)) continue;
            bestScore = score;
            bYaw = yaw;
            bPitch = pitch;
            bDist = dist;
            bSx = sx;
            bSy = sy;
            bSize = tSize;
            bPY = pY;
            bPP = pP;
        }
        p.yaw = bYaw;
        p.pitch = bPitch;
        p.sx = bSx;
        p.sy = bSy;
        p.dist = bDist;
        p.sizeDist = bSize;
        p.pairYaw = bPY;
        p.pairPitch = bPP;
        p.surf = null;
        if (c.lyricsStyle == LFConfig.LyricsStyle.WALL) {
            WorldLyrics.wallPlace(p, s);
        }
        if (!(c.lyricsStyle != LFConfig.LyricsStyle.FLOATING && c.lyricsStyle != LFConfig.LyricsStyle.SKY || c.viewLock)) {
            double[] d = Proj.dir(bYaw, bPitch);
            p.ax = Proj.cx + d[0] * bDist;
            p.ay = Proj.cy + d[1] * bDist;
            p.az = Proj.cz + d[2] * bDist;
        }
        if (c.lyricsStyle == LFConfig.LyricsStyle.FLOATING && !c.viewLock) {
            p.ay += (double)c.floatHeight;
        }
        boolean bl2 = sky = c.lyricsStyle == LFConfig.LyricsStyle.SKY;
        float f = sky && c.arcLayout ? (WorldLyrics.rnd(s, 5) > 0.5f ? 1.0f : -1.0f) * (c.skyWords ? 8.0f + WorldLyrics.rnd(s, 6) * 16.0f : 3.0f + WorldLyrics.rnd(s, 6) * 4.0f) : (p.turn = 0.0f);
        float f3 = c.arcLayout ? (sky ? (c.skyWords ? -22.0f + WorldLyrics.rnd(s, 4) * 44.0f : -1.5f + WorldLyrics.rnd(s, 4) * 3.0f) : -10.0f + WorldLyrics.rnd(s, 4) * 14.0f) : (p.tilt = 0.0f);
        if (c.lyricsStyle == LFConfig.LyricsStyle.WALL) {
            p.tilt = c.arcLayout ? -3.0f + WorldLyrics.rnd(s, 4) * 6.0f : 0.0f;
        }
        List<Lyrics.Word> words = p.line.words;
        int n = p.n();
        String font = WorldLyrics.fontOf(p, c);
        float space = Gfx.textWidth(" ", font, 1.0f);
        float[] widths = new float[n];
        for (int j = 0; j < n; ++j) {
            int i = p.w0 + j;
            float size = 1.0f;
            if (c.varySizes) {
                float h = WorldLyrics.rnd(s, 10 + i);
                float f4 = h > 0.82f ? 1.4f : (h > 0.55f ? 1.12f : (size = h < 0.2f ? 0.82f : 0.96f));
                if (j == 0 && words.get((int)i).text.length() <= 2 && !sky) {
                    size = 1.35f;
                }
            }
            p.wsize[j] = size;
            widths[j] = (Gfx.textWidth(Lyrics.show(words.get((int)i).text), font, 1.0f) + 0.08f) * size;
            p.wrot[j] = c.arcLayout && !sky ? (WorldLyrics.rnd(s, 40 + i) - 0.5f) * 8.0f : 0.0f;
        }
        float spaceEm = Math.max(0.26f, space);
        float maxRow = sky ? 18.0f : (WorldLyrics.chunked(c) ? 99.0f : 9.5f);
        ArrayList<int[]> rows = new ArrayList<int[]>();
        for (int pass = 0; pass < 20; maxRow *= 1.25f, ++pass) {
            rows.clear();
            int start = 0;
            float rowW = 0.0f;
            for (int j = 0; j < n; ++j) {
                float add = widths[j] + (j > start ? spaceEm : 0.0f);
                if (j > start && rowW + add > maxRow) {
                    rows.add(new int[]{start, j});
                    start = j;
                    rowW = widths[j];
                    continue;
                }
                rowW += add;
            }
            rows.add(new int[]{start, n});
            if (!sky || rows.size() <= 3) break;
        }
        float widest = 0.0f;
        Iterator start = rows.iterator();
        while (true) {
            if (!start.hasNext()) break;
            int[] rw = (int[])start.next();
            float w = 0.0f;
            for (int j = rw[0]; j < rw[1]; w += widths[j] + (j > rw[0] ? spaceEm : 0.0f), ++j) {
            }
            widest = Math.max(widest, w);
        }
        float rowGap = sky ? 1.4f : 1.5f;
        float totalH = (float)(rows.size() - 1) * rowGap;
        int r = 0;
        while (true) {
            float w;
            int b;
            int a;
            if (r < rows.size()) {
                a = ((int[])rows.get(r))[0];
                b = ((int[])rows.get(r))[1];
                w = 0.0f;
            } else {
                Placed mate;
                p.rowsN = rows.size();
                float emA = WorldLyrics.em(p, c);
                p.angW = (float)Math.toDegrees(Math.atan((double)(widest * 0.5f * emA) / Math.max(0.5, p.dist)));
                p.angH = (float)Math.toDegrees(Math.atan((double)((float)rows.size() * 0.7f * emA) / Math.max(0.5, p.dist)));
                if (sky && !c.viewLock && !c.skyWords && p.line.index >= 0 && p.line.index % 2 == 1 && (mate = WorldLyrics.findIndex(p.line.index - 1)) != null && mate.rowsN + p.rowsN > 2) {
                    float emDeg = (float)Math.toDegrees(Math.atan((double)WorldLyrics.em(p, c) / p.dist));
                    p.pitch += (float)(mate.rowsN - 1 + p.rowsN - 1) * rowGap * 0.5f * emDeg;
                    double[] d = Proj.dir(p.yaw, p.pitch);
                    p.ax = Proj.cx + d[0] * p.dist;
                    p.ay = Proj.cy + d[1] * p.dist;
                    p.az = Proj.cz + d[2] * p.dist;
                }
                if (Fonts.isRtl(p.line.text)) {
                    for (int j = 0; j < n; ++j) {
                        p.wx0[j] = -p.wx0[j];
                    }
                }
                if (c.lyricsStyle == LFConfig.LyricsStyle.WALL) {
                    WorldLyrics.fitToSurface(p, c, widths);
                }
                if (c.lyricsStyle == LFConfig.LyricsStyle.FLOATING && !c.viewLock) {
                    WorldLyrics.keepOutOfBlocks(p, c, widths);
                }
                return;
            }
            for (int j = a; j < b; w += widths[j] + (j > a ? spaceEm : 0.0f), ++j) {
            }
            float shift = sky ? 0.0f : (rows.size() > 1 ? (r == 0 ? -1.0f : 1.0f) * (sky ? 0.9f : 0.6f) * (random ? 0.5f + WorldLyrics.rnd(s, 60 + r) * 0.5f : 0.6f) : 0.0f);
            float x = sky ? -widest / 2.0f : -w / 2.0f + shift;
            for (int j = a; j < b; x += widths[j] + spaceEm, ++j) {
                float cxw;
                p.wx0[j] = cxw = x + widths[j] / 2.0f;
                float f5 = c.arcLayout ? (sky ? 0.008f : 0.03f) * (cxw * cxw) : 0.0f;
                p.wy0[j] = -totalH / 2.0f + (float)r * rowGap + (sky ? -f5 : f5) - (p.wsize[j] - 1.0f) * 0.35f;
            }
            ++r;
        }
    }

    private static void keepOutOfBlocks(Placed p, LFConfig c, float[] widths) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null) {
            return;
        }
        float em = WorldLyrics.em(p, c);
        Quaternionf q = WorldLyrics.facing(p, c).mul((Quaternionfc)class_7833.field_40718.rotationDegrees(-p.tilt));
        double ox = p.ax;
        double oy = p.ay;
        double oz = p.az;
        if (!WorldLyrics.wordsInBlocks(mc, p, q, em, widths)) {
            return;
        }
        double ex = ox - Proj.cx;
        double ey = oy - Proj.cy;
        double ez = oz - Proj.cz;
        double len = Math.sqrt(ex * ex + ey * ey + ez * ez);
        for (double d = len - 0.4; d >= 1.8; d -= 0.4) {
            double k = d / len;
            p.ax = Proj.cx + ex * k;
            p.ay = Proj.cy + ey * k;
            p.az = Proj.cz + ez * k;
            if (WorldLyrics.wordsInBlocks(mc, p, q, em, widths)) continue;
            p.dist = d;
            return;
        }
        for (double up = 0.5; up <= 3.01; up += 0.5) {
            p.ax = ox;
            p.ay = oy + up;
            p.az = oz;
            if (WorldLyrics.wordsInBlocks(mc, p, q, em, widths)) continue;
            return;
        }
        p.ax = ox;
        p.ay = oy;
        p.az = oz;
    }

    private static boolean wordsInBlocks(class_310 mc, Placed p, Quaternionf q, float em, float[] widths) {
        Vector3f v = new Vector3f();
        class_2338.class_2339 bp = new class_2338.class_2339();
        for (int j = 0; j < p.n(); ++j) {
            float hw = widths[j] * em * 0.5f;
            float hh = 0.42f * p.wsize[j] * em;
            float cx = p.wx0[j] * em;
            float cy = -p.wy0[j] * em;
            int nx = Math.max(3, (int)Math.ceil(hw * 2.0f / 0.2f) + 1);
            int ny = Math.max(2, (int)Math.ceil(hh * 2.0f / 0.2f) + 1);
            for (int ix = 0; ix < nx; ++ix) {
                for (int iy = 0; iy < ny; ++iy) {
                    class_265 shape;
                    v.set(cx - hw + 2.0f * hw * (float)ix / (float)(nx - 1), cy - hh + 2.0f * hh * (float)iy / (float)(ny - 1), 0.0f);
                    q.transform(v);
                    double wx = p.ax + (double)v.x;
                    double wy = p.ay + (double)v.y;
                    double wz = p.az + (double)v.z;
                    bp.method_10102(Math.floor(wx), Math.floor(wy), Math.floor(wz));
                    class_2680 st = mc.field_1687.method_8320((class_2338)bp);
                    if (st.method_26215() || (shape = st.method_26218((class_1922)mc.field_1687, (class_2338)bp)).method_1110()) continue;
                    double lx = wx - (double)bp.method_10263();
                    double ly = wy - (double)bp.method_10264();
                    double lz = wz - (double)bp.method_10260();
                    for (class_238 box : shape.method_1090()) {
                        if (!(lx >= box.field_1323 - 0.05) || !(lx <= box.field_1320 + 0.05) || !(ly >= box.field_1322 - 0.05) || !(ly <= box.field_1325 + 0.05) || !(lz >= box.field_1321 - 0.05) || !(lz <= box.field_1324 + 0.05)) continue;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void drawScreen(class_332 g, Placed p, long t, LFConfig c, Themes.Theme th, float speed) {
        float px = p.sx * Proj.width;
        float py = p.sy * Proj.height;
        float emPx = 15.0f * c.lyricsScale;
        float globalA = pauseFade * Hud.focusA * c.textOpacity;
        if (globalA < 0.01f) {
            return;
        }
        String font = WorldLyrics.fontOf(p, c);
        float glowBase = c.glow ? 0.65f : 0.0f;
        float beat = c.beatGlow ? Hud.beat : 0.0f;
        boolean single = false;
        g.method_51448().pushMatrix();
        g.method_51448().translate(px, py);
        g.method_51448().rotate((float)Math.toRadians(p.tilt));
        List<Lyrics.Word> words = p.line.words;
        for (int j = 0; j < p.n(); ++j) {
            Lyrics.Word w = words.get(p.w0 + j);
            float[] s = WorldLyrics.wordAnim(p, j, w, t, c, speed);
            if (s == null) continue;
            float alpha = s[0];
            float dx = s[1];
            float dy = s[2];
            float sc = s[3];
            double bob = Math.sin(Anim.seconds() * 1.6 + (double)j * 0.9 + (double)p.seed) * 0.04;
            float size = emPx * p.wsize[j] * sc;
            float x = (p.wx0[j] + dx) * emPx;
            float y = (float)(((double)(p.wy0[j] + dy) + bob) * (double)emPx) - size / 2.0f;
            if (single) {
                float out = Anim.progress(t, w.end + 120L, (float)(w.end + 120L) + 380.0f / speed);
                if (out >= 1.0f) continue;
                alpha *= 1.0f - Anim.easeInCubic(out);
                size = emPx * 1.7f * sc;
                x = dx * emPx;
                y = (float)(((double)(dy - out * 0.6f) + bob) * (double)emPx) - size / 2.0f;
            }
            boolean sung = t >= w.start && t < w.end + 80L;
            float glow = glowBase + (sung ? 0.35f : 0.0f) + beat * 0.5f;
            g.method_51448().pushMatrix();
            g.method_51448().translate(x, y + size / 2.0f);
            if (p.wrot[j] != 0.0f) {
                g.method_51448().rotate((float)Math.toRadians(p.wrot[j]));
            }
            Gfx.neon(g, Lyrics.show(w.text), font, 0.0f, -size / 2.0f, size, th.main(), th.a(), th.b(), alpha * globalA, c.twoColor, glow);
            g.method_51448().popMatrix();
        }
        g.method_51448().popMatrix();
    }

    static {
        P = new double[4];
        layerStep = 0.06f;
        NOTES = new String[]{"\u266a \u266b", "\u266b \u266a \u266c", "\u266a \u266b \u266a", "\u266c \u266a", "\u266b \u266a \u266b \u266a"};
        noteSlot = -1L;
        noteSong = -1L;
    }

    private static final class Placed {
        final Lyrics.Line line;
        final int w0;
        final int w1;
        final long seed;
        float yaw;
        float pitch;
        double dist = 6.0;
        double ax;
        double ay;
        double az;
        float turn;
        float sx;
        float sy;
        float tilt;
        Quaternionf surf;
        class_243 surfN;
        float fit = 1.0f;
        float pairYaw;
        float pairPitch;
        int rowsN = 1;
        float coverFade = 1.0f;
        float angW = 6.0f;
        float angH = 2.0f;
        float sizeDist = 6.0f;
        final float[] wx0;
        final float[] wy0;
        final float[] wsize;
        final float[] wrot;
        long exitAt = -1L;
        float occl = 1.0f;
        float occlVis = 1.0f;
        float viewVis = 0.0f;
        boolean lookedAway;
        int occlTick;

        Placed(Lyrics.Line line, int w0, int w1, long seed) {
            this.line = line;
            this.w0 = w0;
            this.w1 = w1;
            this.seed = seed;
            int n = w1 - w0;
            this.wx0 = new float[n];
            this.wy0 = new float[n];
            this.wsize = new float[n];
            this.wrot = new float[n];
        }

        int n() {
            return this.w1 - this.w0;
        }

        long lastEnd() {
            return this.line.words.get((int)(this.w1 - 1)).end;
        }
    }
}


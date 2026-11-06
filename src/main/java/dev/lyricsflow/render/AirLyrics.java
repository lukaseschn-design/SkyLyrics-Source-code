/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.renderpearl.api.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.network.chat.Component
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font$DisplayMode
 *  com.mojang.blaze3d.vertex.PoseStack
 *  dev.lyricsflow.compat.LegacyBufferSource
 *  dev.lyricsflow.compat.LegacyBufferSource$BufferSource
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.client.renderer.fog.FogRenderer
 *  net.minecraft.client.renderer.fog.FogRenderer$FogMode
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package dev.lyricsflow.render;

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.config.LFConfig;
import dev.lyricsflow.lyrics.Lyrics;
import dev.lyricsflow.media.MediaManager;
import dev.lyricsflow.mixin.GameRendererAccessor;
import dev.lyricsflow.perf.AutoPerf;
import dev.lyricsflow.render.Fonts;
import dev.lyricsflow.render.Halo;
import dev.lyricsflow.render.Hud;
import dev.lyricsflow.render.Proj;
import dev.lyricsflow.render.Themes;
import dev.lyricsflow.render.WorldLyrics;
import dev.lyricsflow.util.Col;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.lyricsflow.compat.LegacyBufferSource;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

final class AirLyrics {
    private static final float GLYPH = 9.25f;
    private static final int LIGHT = 0xF000F0;
    private static final Random RND = new Random();
    private static long lastNs;
    private static boolean pit;
    private static long pitSince;
    private static long pitCheck;
    private static long lastRender;
    private static long seq;
    private static SkyLine active;
    private static final List<SkyLine> exiting;
    private static Lyrics.Line activeLine;
    private static double lastSide;
    private static final float[][] SLOTS;
    private static final float BLOCKS_PER_UNIT = 0.062f;
    private static final List<Word> WORDS;
    private static Lyrics.Line fLine;
    private static int fChunk;
    private static int lastSlot;
    private static int prevSlot;
    private static String[] fChunks;
    private static int[] fChunkOf;

    private AirLyrics() {
    }

    static boolean handles(LFConfig c) {
        return c.lyricsStyle == LFConfig.LyricsStyle.SKY;
    }

    static boolean inPit() {
        boolean covered;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (now - pitCheck < 100L) {
            return pit;
        }
        pitCheck = now;
        boolean bl = covered = !mc.level.canSeeSky(BlockPos.containing((double)Proj.cx, (double)Proj.cy, (double)Proj.cz).above());
        if (covered != pit) {
            if (pitSince == 0L) {
                pitSince = now;
            }
            if (now - pitSince > 1000L) {
                pit = covered;
                pitSince = 0L;
            }
        } else {
            pitSince = 0L;
        }
        return pit;
    }

    static void clear() {
        active = null;
        exiting.clear();
        activeLine = null;
        WORDS.clear();
        fLine = null;
        fChunk = -1;
    }

    static void render(PoseStack ps, LegacyBufferSource buffers, Font.DisplayMode mode) {
        Minecraft mc = Minecraft.getInstance();
        if (System.currentTimeMillis() - lastRender > 500L) {
            AirLyrics.clear();
        }
        lastRender = System.currentTimeMillis();
        LFConfig c = ConfigManager.get();
        MediaManager mm = MediaManager.get();
        long ns = System.nanoTime();
        float dt = lastNs == 0L ? 0.016f : Math.min(0.1f, (float)(ns - lastNs) / 1.0E9f);
        lastNs = ns;
        if (mm.songSeq() != seq) {
            seq = mm.songSeq();
            AirLyrics.clear();
        }
        Matrix4f base = ps.last().pose();
        float alpha = Math.max(0.0f, Math.min(1.0f, c.textOpacity)) * Hud.focusA;
        if (c.lyricsStyle == LFConfig.LyricsStyle.SKY) {
            WORDS.clear();
            return;
        }
        active = null;
        exiting.clear();
        activeLine = null;
        AirLyrics.floating(mc, c, mm, buffers, base, mode, alpha);
    }

    private static String font(LFConfig c) {
        return c.font == null || c.font.isEmpty() ? "righteous" : c.font;
    }

    private static float widthU(String s, String font) {
        return Fonts.unitWidth(s, font, Minecraft.getInstance().font) * 9.25f;
    }

    private static void text(Matrix4f m, String s, String font, float x, float y, float z, int rgb, float a, LegacyBufferSource buf, Font.DisplayMode mode) {
        if (a <= 0.03f) {
            return;
        }
        float h = Fonts.height(font);
        float u = 9.25f / h;
        Matrix4f mm = new Matrix4f((Matrix4fc)m).translate(x, y, z).scale(u, u, u);
        MutableComponent comp = Fonts.text(s, font);
        float gy = 4.5f / u - h / 2.0f + h * Fonts.yShift(font, ConfigManager.get().allCaps);
        int col = Col.alpha(0xFF000000 | rgb & 0xFFFFFF, Math.min(1.0f, a));
        if (col >>> 24 < 8) {
            return;
        }
        buf.drawText((Component)comp, 0.0f, gy, col, false, mm, mode, 0, 0xF000F0);
    }

    private static boolean skyOpen(Minecraft mc) {
        return mc.level == null || mc.level.canSeeSky(BlockPos.containing((double)Proj.cx, (double)Proj.cy, (double)Proj.cz).above());
    }

    private static double skyEl(LFConfig c, boolean open) {
        double b = -Proj.pitch + 3.0f;
        return c.showUnderground && !open ? b : Math.max(-6.0, Math.min(60.0, b));
    }

    private static double degPerUnit(Minecraft mc, LFConfig c) {
        double guiH = Math.max(1, mc.getWindow().getGuiScaledHeight());
        double focalRef = guiH / 2.0 / Math.tan(Math.toRadians(35.0));
        return Math.toDegrees(1.6 * (double)c.lyricsScale / focalRef);
    }

    private static double angDiff(double a, double b) {
        return ((a - b) % 360.0 + 540.0) % 360.0 - 180.0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void sky(Minecraft mc, LFConfig c, MediaManager mm, LegacyBufferSource buf, Matrix4f base, Font.DisplayMode mode, float dt, float alpha) {
        Lyrics lyrics = mm.lyrics();
        long now = System.currentTimeMillis();
        long prog = mm.position() + 60L;
        Lyrics.Line cur = null;
        if (lyrics != null && mm.isPlaying() && mm.position() >= 0L && (cur = lyrics.lastStartedBefore(prog)) != null && (prog > cur.end + 400L || cur.words.isEmpty() || cur.text.isBlank())) {
            cur = null;
        }
        String font = AirLyrics.font(c);
        double dpp = AirLyrics.degPerUnit(mc, c);
        boolean open = AirLyrics.skyOpen(mc);
        if (cur != activeLine) {
            if (active != null) {
                AirLyrics.active.exitAt = now;
                exiting.add(active);
            }
            active = null;
            activeLine = cur;
            if (cur != null) {
                active = new SkyLine(cur, Proj.yaw, AirLyrics.skyEl(c, open), dpp, font);
            }
        }
        exiting.removeIf(s -> now - s.exitAt > 520L);
        while (exiting.size() > 1) {
            exiting.remove(0);
        }
        Font.DisplayMode m = c.showUnderground && !open ? Font.DisplayMode.SEE_THROUGH : mode;
        double d = WorldLyrics.skyDistance() * 0.9;
        GpuBufferSlice oldFog = null;
        try {
            oldFog = RenderSystem.getShaderFog();
            FogRenderer fr = ((GameRendererAccessor)mc.gameRenderer).lyricsflow$fogRenderer();
            RenderSystem.setShaderFog((GpuBufferSlice)fr.getBuffer(FogRenderer.FogMode.NONE));
        }
        catch (Throwable ignored) {
            oldFog = null;
        }
        try {
            int color = Themes.current().main() & 0xFFFFFF;
            for (SkyLine s2 : exiting) {
                AirLyrics.drawLine(c, buf, base, s2, color, -1, now, dt, dpp, d, font, m, alpha, open);
            }
            if (active != null) {
                AirLyrics.drawLine(c, buf, base, active, color, AirLyrics.active.line.wordAt(prog), now, dt, dpp, d, font, m, alpha, open);
            }
            if (buf instanceof LegacyBufferSource.BufferSource) {
                LegacyBufferSource.BufferSource bs = (LegacyBufferSource.BufferSource)buf;
                bs.endBatch();
            }
        }
        finally {
            if (oldFog != null) {
                RenderSystem.setShaderFog((GpuBufferSlice)oldFog);
            }
        }
    }

    private static void drawLine(LFConfig c, LegacyBufferSource buf, Matrix4f base, SkyLine s, int color, int cur, long now, float dt, double dpp, double d, String font, Font.DisplayMode mode, float alpha, boolean open) {
        float exit;
        float lineA;
        if (!c.viewLock && s.exitAt == 0L) {
            double dEl;
            double dAz = AirLyrics.angDiff(Proj.yaw, s.baseAz);
            double kh = 1.0 - Math.exp(-3.0 * (double)dt);
            if (Math.abs(dAz) > 26.0) {
                s.baseAz += (dAz - Math.signum(dAz) * 26.0) * kh;
            }
            double kv = 1.0 - Math.exp(-(Math.abs(dEl = AirLyrics.skyEl(c, open) - s.baseEl) > 6.0 ? 4.0 : 1.5) * (double)dt);
            s.baseEl += dEl * kv;
        }
        if ((lineA = (1.0f - AirLyrics.outCubic(exit = s.exitAt == 0L ? 0.0f : AirLyrics.clamp01((float)(now - s.exitAt) / 480.0f))) * alpha) <= 0.01f) {
            return;
        }
        int n = s.line.words.size();
        int upTo = s.exitAt != 0L ? n : cur;
        float k = (float)(Math.toRadians(dpp) * d);
        float space = AirLyrics.widthU(" ", font);
        int shadow = Col.lerp(0xFF000000 | color, -16120300, 0.75f) & 0xFFFFFF;
        long startDelay = !exiting.isEmpty() && s.exitAt == 0L ? 420L : 0L;
        float beat = c.beatGlow ? Hud.beat : 0.0f;
        for (Chunk ch : s.chunks) {
            double Nz;
            double Rx;
            double Ux;
            double Ny;
            double Uy;
            double Rz;
            double Uz;
            double sr;
            if (s.exitAt == 0L && ch.from > upTo) continue;
            float kk = k * ch.size;
            double az = Math.toRadians(s.baseAz + ch.offAz);
            double el = Math.toRadians(s.baseEl + ch.offEl + (double)(AirLyrics.outCubic(exit) * 3.0f));
            double dx = -Math.sin(az) * Math.cos(el);
            double dy = Math.sin(el);
            double dz = Math.cos(az) * Math.cos(el);
            double r0x = -Math.cos(az);
            double r0z = -Math.sin(az);
            double u0x = -r0z * dy;
            double u0y = r0z * dx - r0x * dz;
            double u0z = r0x * dy;
            double ct = Math.cos(ch.yawTilt);
            double st = Math.sin(ch.yawTilt);
            double rX = r0x * ct + dx * st;
            double rY = dy * st;
            double rZ = r0z * ct + dz * st;
            double cr = Math.cos(ch.roll);
            double Ry = rY * cr + u0y * (sr = Math.sin(ch.roll));
            double Nx = Ry * (Uz = u0z * cr - rZ * sr) - (Rz = rZ * cr + u0z * sr) * (Uy = u0y * cr - rY * sr);
            if (Nx * dx + (Ny = Rz * (Ux = u0x * cr - rX * sr) - (Rx = rX * cr + u0x * sr) * Uz) * dy + (Nz = Rx * Uy - Ry * Ux) * dz > 0.0) {
                Nx = -Nx;
                Ny = -Ny;
                Nz = -Nz;
            }
            Matrix4f m0 = new Matrix4f((Matrix4fc)base).mul((Matrix4fc)new Matrix4f((float)Rx * kk, (float)Ry * kk, (float)Rz * kk, 0.0f, (float)(-Ux) * kk, (float)(-Uy) * kk, (float)(-Uz) * kk, 0.0f, (float)Nx * kk, (float)Ny * kk, (float)Nz * kk, 0.0f, (float)(dx * d), (float)(dy * d), (float)(dz * d), 1.0f));
            float lx = -ch.widthU / 2.0f;
            for (int i = ch.from; i < ch.to; ++i) {
                boolean on;
                String word = Lyrics.show(s.line.words.get((int)i).text);
                float ww = AirLyrics.widthU(word, font);
                boolean bl = on = s.exitAt != 0L || i <= upTo;
                if (on && s.shown[i] == 0L) {
                    s.shown[i] = Math.max(now, s.born + startDelay);
                }
                if (!on) {
                    lx += ww + space;
                    continue;
                }
                float raw = AirLyrics.clamp01((float)(now - s.shown[i]) / 450.0f);
                float e = AirLyrics.outCubic(raw);
                float a = e * lineA;
                boolean current = s.exitAt == 0L && i == upTo;
                Matrix4f m = new Matrix4f((Matrix4fc)m0);
                if (current && beat > 0.01f) {
                    float bs = 1.0f + 0.07f * beat;
                    m.translate(lx + ww / 2.0f, 0.0f, 0.0f).scale(bs, bs, 1.0f).translate(-(lx + ww / 2.0f), 0.0f, 0.0f);
                }
                m.translate(lx, -4.5f + (1.0f - e) * 3.0f, 0.0f);
                AirLyrics.text(m, word, font, 0.0f, 0.9f, 0.0f, shadow, a * 0.55f, buf, mode);
                if (c.glow && AutoPerf.allow(3)) {
                    float pad = 5.5f;
                    Halo.draw(m, -pad, -pad * 0.8f, ww + pad, 9.0f + pad * 0.8f, Col.alpha(0xFF000000 | color, a * (current ? 0.42f : 0.26f)), buf);
                }
                int col = Col.lerp(0xFF000000 | color, -1, current ? 0.6f : 0.2f) & 0xFFFFFF;
                AirLyrics.text(m, word, font, 0.0f, 0.0f, 0.12f, col, a, buf, mode);
                lx += ww + space;
            }
        }
    }

    private static float sz(LFConfig c) {
        return 1.6f * c.lyricsScale / 1.7f;
    }

    private static void floating(Minecraft mc, LFConfig c, MediaManager mm, LegacyBufferSource buf, Matrix4f base, Font.DisplayMode mode, float alpha) {
        long progress;
        Lyrics.Line line;
        long now = System.currentTimeMillis();
        Vec3 cam = new Vec3(Proj.cx, Proj.cy, Proj.cz);
        boolean live = false;
        Lyrics lyrics = mm.lyrics();
        if (lyrics != null && mm.isPlaying() && mm.position() >= 0L && !lyrics.lines.isEmpty() && (line = lyrics.lastStartedBefore(progress = mm.position())) != null && progress <= line.end + 450L) {
            live = AirLyrics.update(c, line, progress, cam, now);
        }
        if (!live) {
            for (Word w2 : WORDS) {
                if (w2.gone != 0L) continue;
                w2.gone = now;
            }
        }
        float fadeMs = 2200.0f;
        WORDS.removeIf(w -> w.gone != 0L && (float)(now - w.gone) > fadeMs);
        AirLyrics.draw(c, buf, base, cam, now, fadeMs, mode, alpha);
    }

    private static boolean update(LFConfig c, Lyrics.Line line, long progress, Vec3 cam, long now) {
        if (line.text == null || line.text.isBlank() || line.words.isEmpty()) {
            return false;
        }
        int n = line.words.size();
        if (line != fLine) {
            fLine = line;
            fChunk = -1;
            AirLyrics.buildChunks(line);
        }
        if (fChunks.length == 0) {
            return false;
        }
        int aw = Math.max(0, Math.min(n - 1, line.wordAt(progress)));
        int ac = Math.max(0, Math.min(fChunks.length - 1, fChunkOf[Math.min(aw, fChunkOf.length - 1)]));
        if (ac > fChunk) {
            if (ac - fChunk > 2) {
                fChunk = ac - 1;
            }
            while (fChunk < ac) {
                AirLyrics.spawn(fChunks[++fChunk], c, cam, now);
            }
        }
        return true;
    }

    private static void buildChunks(Lyrics.Line line) {
        int n = line.words.size();
        ArrayList<String> list = new ArrayList<String>();
        int[] map = new int[n];
        int i = 0;
        while (i < n) {
            String b;
            String a = AirLyrics.clean(line.words.get((int)i).text);
            if (a.isEmpty()) {
                map[i] = list.isEmpty() ? 0 : list.size() - 1;
                ++i;
                continue;
            }
            int idx = list.size();
            int used = 1;
            Object chunk = a;
            if (a.length() <= 3 && i + 1 < n && !(b = AirLyrics.clean(line.words.get((int)(i + 1)).text)).isEmpty()) {
                chunk = a + " " + b;
                used = 2;
                map[i + 1] = idx;
            }
            map[i] = idx;
            list.add((String)chunk);
            i += used;
        }
        fChunks = list.toArray(new String[0]);
        fChunkOf = map;
    }

    private static String clean(String s) {
        int a;
        if (s == null) {
            return "";
        }
        int b = s.length();
        for (a = 0; a < b && !AirLyrics.keep(s.charAt(a)); ++a) {
        }
        while (b > a && !AirLyrics.keep(s.charAt(b - 1))) {
            --b;
        }
        return s.substring(a, b);
    }

    private static boolean keep(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '*' || ch == '\'';
    }

    private static void spawn(String text, LFConfig c, Vec3 cam, long now) {
        double nz;
        double nx;
        if (text == null || text.isEmpty()) {
            return;
        }
        String font = AirLyrics.font(c);
        float yaw = Proj.yaw;
        float el0 = -Proj.pitch + 12.0f;
        String txt = Lyrics.show(text);
        float widthU = AirLyrics.widthU(txt, font);
        double bestScore = Double.MAX_VALUE;
        double bAz = 0.0;
        double bEl = 0.0;
        double bDist = 8.0;
        double bSize = 1.0;
        double bHw = 1.0;
        double bHh = 1.0;
        int bestSlot = 0;
        for (int attempt = 0; attempt < 28; ++attempt) {
            double hh;
            int slot;
            int tries = 0;
            while (((slot = RND.nextInt(SLOTS.length)) == lastSlot || slot == prevSlot) && ++tries < 12) {
            }
            double az = Math.toRadians(yaw + SLOTS[slot][0] + (RND.nextFloat() - 0.5f) * 7.0f);
            double el = Math.toRadians(el0 + SLOTS[slot][1] + (RND.nextFloat() - 0.5f) * 6.0f);
            double dist = 6.5 + RND.nextDouble() * 8.5;
            float size = 0.82f + RND.nextFloat() * 0.5f;
            double hw = (double)(widthU * 0.062f * AirLyrics.sz(c) * size) * 0.5 * 0.95;
            double score = AirLyrics.overlap(az, el, dist, hw, hh = (double)(0.682f * AirLyrics.sz(c) * size) * 0.5, cam);
            if (score < bestScore) {
                bestScore = score;
                bAz = az;
                bEl = el;
                bDist = dist;
                bSize = size;
                bHw = hw;
                bHh = hh;
                bestSlot = slot;
            }
            if (score <= 0.0) break;
        }
        prevSlot = lastSlot;
        lastSlot = bestSlot;
        double dx = -Math.sin(bAz) * Math.cos(bEl);
        double dy = Math.sin(bEl);
        double dz = Math.cos(bAz) * Math.cos(bEl);
        double x = cam.x + dx * bDist;
        double y = cam.y + dy * bDist;
        double z = cam.z + dz * bDist;
        double h = Math.sqrt(dx * dx + dz * dz);
        if (h < 1.0E-4) {
            double yr = Math.toRadians(yaw);
            nx = Math.sin(yr);
            nz = -Math.cos(yr);
        } else {
            nx = -dx / h;
            nz = -dz / h;
        }
        float tilt = (float)Math.toRadians((float)(RND.nextBoolean() ? 1 : -1) * (10.0f + RND.nextFloat() * 22.0f));
        float pitch = (float)Math.toRadians((RND.nextFloat() - 0.5f) * 14.0f);
        float roll = (float)Math.toRadians((RND.nextFloat() - 0.5f) * 8.0f);
        for (Word o : WORDS) {
            if (o.gone != 0L) continue;
            o.gone = now;
        }
        WORDS.add(new Word(text, x, y, z, nx, nz, tilt, pitch, roll, (float)bSize, (float)bHw, (float)bHh, now));
        while (WORDS.size() > 7) {
            WORDS.remove(0);
        }
    }

    private static double overlap(double az, double el, double dist, double hw, double hh, Vec3 cam) {
        double score = 0.0;
        double angW = hw / dist;
        double angH = hh / dist;
        for (Word o : WORDS) {
            double ex = o.x - cam.x;
            double ey = o.y - cam.y;
            double ez = o.z - cam.z;
            double d = Math.sqrt(ex * ex + ey * ey + ez * ez);
            if (d < 0.5) continue;
            double oAz = Math.atan2(-ex, ez);
            double oEl = Math.asin(Math.max(-1.0, Math.min(1.0, ey / d)));
            double dAz = Math.abs(((az - oAz) % (Math.PI * 2) + Math.PI * 3) % (Math.PI * 2) - Math.PI) * Math.cos(el);
            double dEl = Math.abs(el - oEl);
            double needW = angW + (double)o.halfW / d + 0.035;
            double needH = angH + (double)o.halfH / d + 0.03;
            if (!(dAz < needW) || !(dEl < needH)) continue;
            score += 1.0 - dAz / needW + (1.0 - dEl / needH) + 0.5;
        }
        return score;
    }

    private static void draw(LFConfig c, LegacyBufferSource buf, Matrix4f base, Vec3 cam, long now, float fadeMs, Font.DisplayMode mode, float alpha) {
        if (WORDS.isEmpty()) {
            return;
        }
        String font = AirLyrics.font(c);
        int baseCol = Themes.current().main() & 0xFFFFFF;
        double yr = Math.toRadians(Proj.yaw);
        double pr = Math.toRadians(Proj.pitch);
        double vfx = -Math.sin(yr) * Math.cos(pr);
        double vfy = -Math.sin(pr);
        double vfz = Math.cos(yr) * Math.cos(pr);
        float beat = c.beatGlow ? Hud.beat : 0.0f;
        for (Word w : WORDS) {
            double rz;
            double drift;
            double ry;
            double rx;
            boolean current;
            long age = now - w.born;
            float a = AirLyrics.clamp01((float)age / 140.0f) * alpha;
            float bright = 1.0f;
            boolean bl = current = w.gone == 0L;
            if (!current) {
                float k = (float)(now - w.gone) / fadeMs;
                if (k >= 1.0f) continue;
                a *= 1.0f - k * k;
                bright = 1.0f - 0.5f * AirLyrics.easeOut(AirLyrics.clamp01((float)(now - w.gone) / 240.0f));
            }
            if (a < 0.03f || (rx = w.x - cam.x) * vfx + (ry = w.y + (drift = Math.min((double)age / 1000.0 * 0.25, 1.0)) - cam.y) * vfy + (rz = w.z - cam.z) * vfz < 0.3) continue;
            double ct = Math.cos(w.tilt);
            double st = Math.sin(w.tilt);
            double nx = w.nx * ct - w.nz * st;
            double nz = w.nx * st + w.nz * ct;
            double Ry = 0.0;
            double Rz = -nx;
            double Ux = 0.0;
            double Uy = 1.0;
            double Uz = 0.0;
            double Ny = 0.0;
            double cp = Math.cos(w.pitch);
            double sp = Math.sin(w.pitch);
            double U2x = Ux * cp + nx * sp;
            double U2y = Uy * cp + Ny * sp;
            double U2z = Uz * cp + nz * sp;
            double N2x = nx * cp - Ux * sp;
            double N2y = Ny * cp - Uy * sp;
            double N2z = nz * cp - Uz * sp;
            double cr = Math.cos(w.roll);
            double sr = Math.sin(w.roll);
            double R3x = nz * cr + U2x * sr;
            double R3y = Ry * cr + U2y * sr;
            double R3z = Rz * cr + U2z * sr;
            double U3x = U2x * cr - nz * sr;
            double U3y = U2y * cr - Ry * sr;
            double U3z = U2z * cr - Rz * sr;
            if (rx * N2x + ry * N2y + rz * N2z > 0.0) {
                R3x = -R3x;
                R3y = -R3y;
                R3z = -R3z;
                N2x = -N2x;
                N2y = -N2y;
                N2z = -N2z;
            }
            float pop = 1.0f + 0.22f * (1.0f - AirLyrics.easeOut(AirLyrics.clamp01((float)age / 240.0f)));
            float kk = 0.062f * AirLyrics.sz(c) * w.size * pop;
            Matrix4f m = new Matrix4f((Matrix4fc)base).mul((Matrix4fc)new Matrix4f((float)R3x * kk, (float)R3y * kk, (float)R3z * kk, 0.0f, (float)(-U3x) * kk, (float)(-U3y) * kk, (float)(-U3z) * kk, 0.0f, (float)N2x * kk, (float)N2y * kk, (float)N2z * kk, 0.0f, (float)rx, (float)ry, (float)rz, 1.0f));
            String txt = Lyrics.show(w.text);
            float width = AirLyrics.widthU(txt, font);
            int shownLen = Math.max(1, (int)Math.ceil((float)txt.length() * AirLyrics.clamp01((float)age / 230.0f)));
            String shown = txt.substring(0, Math.min(txt.length(), shownLen));
            if (current && beat > 0.01f) {
                float bs = 1.0f + 0.06f * beat;
                m.scale(bs, bs, 1.0f);
            }
            m.translate(-width / 2.0f, -4.5f, 0.0f);
            int col = Col.lerp(-7566176, 0xFF000000 | baseCol, bright) & 0xFFFFFF;
            AirLyrics.text(m, shown, font, 0.7f, 0.9f, -0.5f, 328716, a * 0.55f, buf, mode);
            AirLyrics.text(m, shown, font, 0.0f, 0.0f, 0.3f, col, a, buf, mode);
        }
        if (buf instanceof LegacyBufferSource.BufferSource) {
            LegacyBufferSource.BufferSource bs = (LegacyBufferSource.BufferSource)buf;
            bs.endBatch();
        }
    }

    private static float clamp01(float v) {
        return v < 0.0f ? 0.0f : Math.min(v, 1.0f);
    }

    private static float outCubic(float t) {
        float u = 1.0f - t;
        return 1.0f - u * u * u;
    }

    private static float easeOut(float t) {
        return AirLyrics.outCubic(t);
    }

    static {
        seq = -1L;
        exiting = new ArrayList<SkyLine>();
        SLOTS = new float[][]{{-22.0f, 7.0f}, {20.0f, -4.0f}, {-6.0f, -12.0f}, {16.0f, 12.0f}, {-26.0f, -8.0f}, {8.0f, 4.0f}, {-14.0f, 14.0f}, {26.0f, 6.0f}, {2.0f, -16.0f}, {-18.0f, 0.0f}, {12.0f, -10.0f}, {-8.0f, 9.0f}};
        WORDS = new ArrayList<Word>();
        fChunk = -1;
        lastSlot = -1;
        prevSlot = -1;
        fChunks = new String[0];
        fChunkOf = new int[0];
    }

    private static final class SkyLine {
        final Lyrics.Line line;
        final long[] shown;
        final List<Chunk> chunks = new ArrayList<Chunk>();
        double baseAz;
        double baseEl;
        final long born = System.currentTimeMillis();
        long exitAt;

        SkyLine(Lyrics.Line l, double az, double el, double dpp, String font) {
            this.line = l;
            int n = l.words.size();
            this.shown = new long[n];
            this.baseAz = az;
            this.baseEl = el;
            String[] w = new String[n];
            for (int i = 0; i < n; ++i) {
                w[i] = Lyrics.show(l.words.get((int)i).text);
            }
            float space = AirLyrics.widthU(" ", font);
            int i = 0;
            while (i < n) {
                int j;
                int chars = w[i].length();
                for (j = i + 1; j < n && j - i < 3 && chars + w[j].length() <= (j - i == 1 ? 12 : 9); ++j) {
                    chars += w[j].length();
                }
                Chunk ch = new Chunk();
                ch.from = i;
                ch.to = j;
                for (int k = i; k < j; ++k) {
                    ch.widthU = ch.widthU + (AirLyrics.widthU(w[k], font) + (k > i ? space : 0.0f));
                }
                this.chunks.add(ch);
                i = j;
            }
            double gap = (double)space * 2.4 * dpp;
            int cn = this.chunks.size();
            if (cn == 0) {
                return;
            }
            int rowsN = cn <= 2 ? 1 : (cn >= 6 && RND.nextFloat() < 0.35f ? 3 : 2);
            if (cn == 3 && RND.nextFloat() < 0.35f) {
                rowsN = 1;
            }
            ArrayList<ArrayList<Chunk>> rows = new ArrayList<ArrayList<Chunk>>();
            int idx = 0;
            for (int r = 0; r < rowsN; ++r) {
                int left = cn - idx;
                int rowsLeft = rowsN - r;
                int take = rowsLeft == 1 ? left : Math.max(1, Math.min(left - (rowsLeft - 1), Math.round((float)left / (float)rowsLeft + (RND.nextFloat() - 0.5f) * 1.2f)));
                rows.add(new ArrayList<Chunk>(this.chunks.subList(idx, idx + take)));
                idx += take;
            }
            int big = RND.nextInt(cn);
            for (int k = 0; k < cn; ++k) {
                this.chunks.get((int)k).size = (k == big ? 1.22f : 0.86f) + RND.nextFloat() * 0.2f;
            }
            double rowH = 17.0 * dpp;
            double side = (RND.nextDouble() * 2.0 - 1.0) * 20.0;
            if (Math.signum(side) == lastSide && RND.nextFloat() < 0.65f) {
                side = -side;
            }
            lastSide = Math.signum(side);
            double lift = 2.5 + RND.nextDouble() * 6.0;
            double stair = (double)(RND.nextBoolean() ? 1 : -1) * (5.0 + RND.nextDouble() * 8.0);
            for (int r = 0; r < rows.size(); ++r) {
                List<Chunk> rr = rows.get(r);
                double total = -gap;
                for (Chunk ch : rr) {
                    total += gap + (double)ch.widthU * dpp * (double)ch.size;
                }
                if (total > 58.0) {
                    float f = (float)(58.0 / total);
                    for (Chunk ch : rr) {
                        ch.size *= f;
                    }
                    total = 58.0;
                }
                double rowShift = side + (double)r * stair + (RND.nextDouble() - 0.5) * 4.0;
                double maxShift = Math.max(0.0, 46.0 - total / 2.0);
                rowShift = Math.max(-maxShift, Math.min(maxShift, rowShift));
                double y = lift + (double)(rows.size() - 1 - r) * rowH + (RND.nextDouble() - 0.5) * rowH * 0.15;
                double x = rowShift - total / 2.0;
                for (int k = 0; k < rr.size(); ++k) {
                    Chunk ch = (Chunk)rr.get(k);
                    double cw = (double)ch.widthU * dpp * (double)ch.size;
                    ch.offAz = x + cw / 2.0;
                    ch.offEl = y + (k % 2 == 0 ? 0.8 : -0.6) + (RND.nextDouble() - 0.5) * 0.8;
                    x += cw + gap;
                    ch.yawTilt = (float)Math.toRadians((float)(RND.nextFloat() < 0.5f ? -1 : 1) * (5.0f + RND.nextFloat() * 14.0f));
                    ch.roll = (float)Math.toRadians((RND.nextFloat() - 0.5f) * 9.0f);
                }
            }
        }
    }

    private static final class Chunk {
        int from;
        int to;
        float widthU;
        double offAz;
        double offEl;
        float yawTilt;
        float roll;
        float size = 1.0f;

        private Chunk() {
        }
    }

    private static final class Word {
        final String text;
        final double x;
        final double y;
        final double z;
        final double nx;
        final double nz;
        final float tilt;
        final float pitch;
        final float roll;
        final float size;
        final float halfW;
        final float halfH;
        final long born;
        long gone;

        Word(String text, double x, double y, double z, double nx, double nz, float tilt, float pitch, float roll, float size, float halfW, float halfH, long born) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.z = z;
            this.nx = nx;
            this.nz = nz;
            this.tilt = tilt;
            this.pitch = pitch;
            this.roll = roll;
            this.size = size;
            this.halfW = halfW;
            this.halfH = halfH;
            this.born = born;
        }
    }
}

/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1074
 *  net.minecraft.class_10799
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package dev.lyricsflow.render;

import dev.lyricsflow.SkyLyrics;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.config.LFConfig;
import dev.lyricsflow.gui.CoverArt;
import dev.lyricsflow.lyrics.Lyrics;
import dev.lyricsflow.media.MediaManager;
import dev.lyricsflow.media.Track;
import dev.lyricsflow.render.Fonts;
import dev.lyricsflow.render.Gfx;
import dev.lyricsflow.render.Hud;
import dev.lyricsflow.render.LivePing;
import dev.lyricsflow.render.Themes;
import dev.lyricsflow.util.Anim;
import dev.lyricsflow.util.Col;
import java.time.LocalTime;
import java.util.List;
import net.minecraft.class_1074;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class TopBar {
    private static final class_2960 SPOTIFY = SkyLyrics.id("textures/gui/logos/spotify.png");
    private static final class_2960 YOUTUBE = SkyLyrics.id("textures/gui/logos/youtube.png");
    private static final class_2960 YTMUSIC = SkyLyrics.id("textures/gui/logos/ytmusic.png");
    private static Phase phase = Phase.TITLE;
    private static long phaseStart = System.nanoTime();
    private static float vis;
    private static float width;
    private static float hlX;
    private static float hlW;
    private static float scroll;
    private static float swap;
    private static String contentKey;
    private static float spin;
    private static final float[] bars;
    public static float lastX;
    public static float lastY;
    public static float lastW;
    public static float lastH;

    private TopBar() {
    }

    static void onEvent(MediaManager.Event e) {
        switch (e.type()) {
            case SONG: {
                TopBar.setPhase(Phase.TITLE);
                break;
            }
            case PAUSE: {
                TopBar.setPhase(Phase.PAUSED);
                break;
            }
            case RESUME: {
                TopBar.setPhase(Phase.RESUMED);
                break;
            }
        }
    }

    private static void setPhase(Phase p) {
        phase = p;
        phaseStart = System.nanoTime();
    }

    private static float phaseAge() {
        return (float)(System.nanoTime() - phaseStart) / 1.0E9f;
    }

    public static void render(class_332 g, boolean preview) {
        int barBg;
        String key;
        boolean mcStyle;
        float dim;
        boolean want;
        Anim.frame();
        LFConfig c = ConfigManager.get();
        MediaManager mm = MediaManager.get();
        Track track = mm.track();
        Lyrics lyrics = mm.lyrics();
        long t = mm.position();
        if (phase == Phase.TITLE && TopBar.phaseAge() > 3.2f && lyrics != null) {
            phase = Phase.LYRICS;
        }
        if (phase == Phase.RESUMED && TopBar.phaseAge() > 1.3f) {
            TopBar.setPhase(lyrics != null ? Phase.LYRICS : Phase.TITLE);
        }
        if (track != null && track.playing && phase == Phase.PAUSED) {
            TopBar.setPhase(Phase.RESUMED);
        }
        boolean simple = c.topBarStyle == LFConfig.TopBarStyle.SIMPLE;
        boolean bl = want = track != null || preview;
        if (track != null && !track.playing && c.hideWhenStopped && phase == Phase.PAUSED && TopBar.phaseAge() > 3.0f) {
            want = false;
        }
        if (!c.showOnlyWhenPlaying && !preview) {
            want = true;
        }
        if ((vis = Anim.approach(vis, want ? 1.0f : 0.0f, want ? 7.0f : 5.0f)) < 0.01f) {
            return;
        }
        float f = dim = track != null && !track.playing && c.dimWhenIdle && phase == Phase.PAUSED && TopBar.phaseAge() > 2.0f ? 0.6f : 1.0f;
        if (!preview) {
            dim *= 0.25f + 0.75f * Hud.focusA;
        }
        float k = c.topBarScale;
        float h = 18.0f * k;
        boolean bl2 = mcStyle = !c.hudRound;
        String font = mcStyle ? "minecraft" : (c.font == null || c.font.isEmpty() ? c.topBarFont : c.font);
        float ts = (font.equals("minecraft") ? 9.0f : 8.6f) * k;
        float textY = (h - (font.equals("minecraft") ? ts * 0.78f : ts)) / 2.0f + (font.equals("minecraft") ? 0.5f * k : -0.3f * k);
        if (!c.lyricsOn && !preview) {
            vis = 1.0f;
            float cx = c.topBarX * (float)g.method_51421();
            float cy = Math.max(1.0f, Math.min((float)g.method_51443() - h - 1.0f, c.topBarY * (float)g.method_51443()));
            TopBar.drawChips(g, c, cx, cy, 0.0f, h, ts, font, Hud.focusA);
            return;
        }
        Lyrics.Line line = null;
        int word = -1;
        if (lyrics != null && t >= 0L) {
            Lyrics.Line last;
            line = lyrics.lineAt(t);
            if (line == null && (last = lyrics.lastStartedBefore(t)) != null && t - last.end < 2500L) {
                line = last;
            }
            if (line != null) {
                word = line.wordAt(t);
            }
        }
        String title = track != null ? track.title : class_1074.method_4662((String)"lyricsflow.topbar.preview_title", (Object[])new Object[0]);
        String artist = track != null ? track.artist : "SkyLyrics";
        boolean showLyrics = !simple && phase == Phase.LYRICS && line != null;
        String status = null;
        if (phase == Phase.PAUSED) {
            status = class_1074.method_4662((String)"lyricsflow.topbar.paused", (Object[])new Object[0]);
        } else if (phase == Phase.RESUMED) {
            status = class_1074.method_4662((String)"lyricsflow.topbar.resumed", (Object[])new Object[0]);
        }
        String string = showLyrics ? "L" + line.index : (key = status != null ? "S" + String.valueOf((Object)phase) : "T" + title);
        if (!key.equals(contentKey)) {
            contentKey = key;
            swap = 0.0f;
            if (showLyrics) {
                scroll = 0.0f;
            }
        }
        swap = Anim.approach(swap, 1.0f, 9.0f);
        String text = showLyrics ? line.text : (status != null ? status + "  \u00b7  " + title : title);
        float textW = Gfx.textWidth(text, font, ts);
        float iconW = h;
        float barsW = 14.0f * k;
        float pad = 7.0f * k;
        float maxText = Math.min(330.0f * k, (float)g.method_51421() * 0.62f);
        float targetW = iconW + Math.min(textW, maxText) + barsW + pad * 2.0f;
        targetW = Math.max(targetW, 150.0f * k);
        width = Anim.approach(width, targetW, 8.0f);
        float x = c.topBarX * (float)g.method_51421() - width / 2.0f;
        float y = c.topBarY * (float)g.method_51443() + (1.0f - Anim.easeOutCubic(vis)) * -10.0f;
        x = Math.max(2.0f, Math.min((float)g.method_51421() - width - 2.0f, x));
        y = Math.max(1.0f, Math.min((float)g.method_51443() - h - 1.0f, y));
        lastX = x;
        lastY = y;
        lastW = width;
        lastH = h;
        float a = Anim.easeOutCubic(vis) * dim;
        Themes.Theme th = Themes.current();
        int accent = Themes.accent();
        TopBar.drawChips(g, c, x, y, width, h, ts, font, a);
        float R = mcStyle ? 0.0f : h / 2.0f;
        int n = barBg = c.barColor != 0 ? Col.withAlpha(Col.darken(c.barColor, 0.78f), 240) : -266988767;
        if (mcStyle) {
            Gfx.rect(g, x, y, width, h, Col.alpha(-1879048192, a));
        } else {
            Gfx.shadow(g, x, y, width, h, R, 5.0f * k, Col.alpha(-16777216, a * 0.5f));
            Gfx.round(g, x, y, width, h, R, Col.alpha(barBg, a));
            Gfx.roundOutline(g, x, y, width, h, R, 0.5f, Col.alpha(0x40FFFFFF, a));
        }
        if (Hud.beat > 0.05f && c.beatGlow) {
            Gfx.glow(g, x, y, width, h, R, 3.0f * k, Col.alpha(accent, a * 0.35f * Hud.beat));
        }
        float icx = x + h / 2.0f + 1.0f * k;
        float icy = y + h / 2.0f;
        float ir = h * 0.34f;
        if (phase == Phase.PAUSED) {
            Gfx.circle(g, icx, icy, ir, Col.alpha(-1514251, a));
            Gfx.rect(g, icx - ir * 0.42f, icy - ir * 0.45f, ir * 0.28f, ir * 0.9f, Col.alpha(-15330527, a));
            Gfx.rect(g, icx + ir * 0.14f, icy - ir * 0.45f, ir * 0.28f, ir * 0.9f, Col.alpha(-15330527, a));
        } else if (showLyrics && CoverArt.ready()) {
            CoverArt.drawRound(g, icx, icy, ir * 1.15f, a);
        } else if (showLyrics) {
            float f2 = Anim.dt();
            float f3 = track != null && track.playing ? 2.4f : 0.0f;
            Gfx.circle(g, icx, icy, ir, Col.alpha(Col.lerp(accent, -1, 0.25f), a));
            Gfx.circle(g, icx, icy, ir * 0.72f, Col.alpha(-14015680, a));
            float sx = (float)Math.cos(spin += f2 * f3) * ir * 0.45f;
            float sy = (float)Math.sin(spin) * ir * 0.45f;
            Gfx.circle(g, icx + sx, icy + sy, ir * 0.16f, Col.alpha(-1862270977, a));
            Gfx.circle(g, icx, icy, ir * 0.22f, Col.alpha(accent, a));
        } else {
            float pop = Anim.easeOutBack(Math.min(1.0f, TopBar.phaseAge() * 3.0f));
            float rr = ir * (0.6f + 0.4f * pop);
            String src = track != null ? track.sourceApp : "Spotify";
            boolean yt = !"Spotify".equals(src);
            float s = Gfx.scale();
            int px = Math.max(1, Math.round(rr * 2.15f * s));
            g.method_51448().pushMatrix();
            g.method_51448().translate(icx, icy);
            g.method_51448().scale(1.0f / s, 1.0f / s);
            g.method_25293(class_10799.field_56883, "YT Music".equals(src) ? YTMUSIC : (yt ? YOUTUBE : SPOTIFY), -px / 2, -px / 2, 0.0f, 0.0f, px, px, 128, 128, 128, 128, Col.alpha(-1, a));
            g.method_51448().popMatrix();
        }
        float tx = x + iconW + 2.0f * k;
        float tw = width - iconW - barsW - pad - 2.0f * k;
        int sx0 = (int)Math.floor(tx);
        int sx1 = (int)Math.ceil(tx + tw);
        g.method_44379(sx0, (int)y, sx1, (int)Math.ceil(y + h));
        float slide = (1.0f - Anim.easeOutCubic(swap)) * 6.0f * k;
        float ta = a * Anim.easeOutCubic(swap);
        if (showLyrics) {
            TopBar.drawKaraoke(g, line, word, t, font, ts, tx, y + textY + slide, tw, ta, accent);
        } else {
            float yy = y + textY + slide;
            if (status != null) {
                Gfx.text(g, Gfx.ellipsize(text, font, ts, tw), font, tx, yy, ts, Col.alpha(-1514251, ta));
            } else {
                String full = title + (String)(artist.isEmpty() ? "" : "  \u00b7  " + artist);
                String shown = Gfx.ellipsize(full, font, ts, tw);
                float w1 = Gfx.text(g, Gfx.ellipsize(title, font, ts, tw), font, tx, yy, ts, Col.alpha(-1, ta));
                if (shown.length() > title.length() && w1 < tw) {
                    Gfx.text(g, Gfx.ellipsize("  \u00b7  " + artist, font, ts, tw - w1), font, tx + w1, yy, ts, Col.alpha(-6646859, ta));
                }
            }
        }
        g.method_44380();
        float bx = x + width - barsW - pad + 3.0f * k;
        float by = y + h / 2.0f;
        boolean playing = track != null && track.playing;
        double time = Anim.seconds();
        for (int i = 0; i < bars.length; ++i) {
            float target = playing ? 0.25f + 0.35f * (float)Math.abs(Math.sin(time * (3.1 + (double)i * 1.7) + (double)i)) + 0.45f * Hud.beat * (i % 2 == 0 ? 1.0f : 0.7f) : 0.18f;
            TopBar.bars[i] = Anim.approach(bars[i], Math.min(1.0f, target), 14.0f);
            float bh = Math.max(1.5f * k, bars[i] * h * 0.5f);
            Gfx.round(g, bx + (float)i * 3.0f * k, by - bh / 2.0f, 1.8f * k, bh, 0.9f * k, Col.alpha(Col.lerp(th.b(), accent, (float)i / 3.0f), a));
        }
    }

    private static void drawKaraoke(class_332 g, Lyrics.Line line, int word, long t, String font, float ts, float tx, float ty, float tw, float a, int accent) {
        float target;
        List<Lyrics.Word> words = line.words;
        float space = Gfx.textWidth(" ", font, ts);
        float[] xs = new float[words.size()];
        float[] ws = new float[words.size()];
        float cx = 0.0f;
        for (int i = 0; i < words.size(); ++i) {
            xs[i] = cx;
            ws[i] = Gfx.textWidth(Lyrics.show(words.get((int)i).text), font, ts);
            cx += ws[i] + space;
        }
        float total = cx - space;
        boolean rtl = Fonts.isRtl(line.text);
        if (rtl) {
            for (int i = 0; i < words.size(); ++i) {
                xs[i] = total - xs[i] - ws[i];
            }
        }
        float f = target = rtl && total > tw ? total - tw : 0.0f;
        if (word >= 0 && total > tw) {
            float wanted = xs[word] + ws[word] / 2.0f - tw * 0.55f;
            target = Math.max(0.0f, Math.min(total - tw, wanted));
        }
        scroll = Anim.approach(scroll, target, 6.0f);
        float ox = tx - scroll;
        if (word >= 0) {
            float pad = 2.2f;
            float targetX = xs[word] - pad;
            float targetW = ws[word] + pad * 2.0f;
            if (hlW <= 0.0f) {
                hlX = targetX;
                hlW = targetW;
            }
            hlX = Anim.approach(hlX, targetX, 16.0f);
            hlW = Anim.approach(hlW, targetW, 16.0f);
            float bh = ts + 2.6f;
            Gfx.round(g, ox + hlX, ty - 1.3f, hlW, bh, ConfigManager.get().hudRound ? 3.0f : 0.0f, Col.alpha(accent, a * 0.95f));
        }
        for (int i = 0; i < words.size(); ++i) {
            int col = i < word ? -1 : (i == word ? -1 : -4607018);
            Gfx.text(g, Lyrics.show(words.get((int)i).text), font, ox + xs[i], ty, ts, Col.alpha(col, a));
        }
    }

    private static void drawChips(class_332 g, LFConfig c, float x, float y, float w, float h, float ts, String font, float a) {
        Object s;
        class_310 mc = class_310.method_1551();
        float ch = h * 0.82f;
        float cy = y + (h - ch) / 2.0f;
        float textY = cy + (ch - ts) / 2.0f - 0.3f;
        if (c.clock) {
            LocalTime now = LocalTime.now();
            s = String.format("%02d:%02d", now.getHour(), now.getMinute());
            float cw = Gfx.textWidth((String)s, font, ts) + 10.0f;
            float cx = x - cw - 6.0f;
            Gfx.text(g, (String)s, font, cx + 5.6f, textY + 0.7f, ts, Col.alpha(-16777216, a * 0.55f));
            Gfx.text(g, (String)s, font, cx + 5.0f, textY, ts, Col.alpha(-1514251, a));
        }
        float rx = x + w + 10.0f;
        if (c.ping && mc.method_1562() != null && mc.field_1724 != null) {
            int i;
            int ms = LivePing.get(mc);
            int bars = ms <= 0 ? 0 : (ms < 60 ? 4 : (ms < 120 ? 3 : (ms < 250 ? 2 : 1)));
            float k = ts / 8.0f;
            float midY = textY + ts * 0.5f;
            float r = 3.0f * k;
            float gx = rx + 3.5f * k;
            float gy = midY + 0.5f * k;
            float[] xs = new float[9];
            float[] ys = new float[9];
            for (i = 0; i < 9; ++i) {
                double an = Math.PI + (double)i * Math.PI / 8.0;
                xs[i] = gx + (float)Math.cos(an) * r;
                ys[i] = gy + (float)Math.sin(an) * r;
            }
            for (i = 0; i < 8; ++i) {
                Gfx.line(g, xs[i], ys[i], xs[i + 1], ys[i + 1], 1.1f * k, Col.alpha(-6450248, a * 0.9f));
            }
            String s2 = Integer.toString(ms);
            float tx = rx + 9.0f * k;
            Gfx.text(g, s2, font, tx + 0.6f, textY + 0.7f, ts, Col.alpha(-16777216, a * 0.55f));
            Gfx.text(g, s2, font, tx, textY, ts, Col.alpha(-1, a * 0.95f));
            float bx = tx + Gfx.textWidth(s2, font, ts) + 5.0f * k;
            for (int i2 = 0; i2 < 4; ++i2) {
                float bh = (float)(2 + i2 * 2) * k;
                Gfx.rect(g, bx + (float)i2 * 3.0f * k, midY + 4.0f * k - bh, 2.0f * k, bh, Col.alpha(-1, a * (i2 < bars ? 0.95f : 0.22f)));
            }
            rx = bx + 16.0f * k;
        }
        if (c.fps) {
            s = mc.method_47599() + " fps";
            Gfx.text(g, (String)s, font, rx + 0.6f, textY + 0.7f, ts, Col.alpha(-16777216, a * 0.5f));
            Gfx.text(g, (String)s, font, rx, textY, ts, Col.alpha(-6450248, a * 0.85f));
        }
    }

    private static void drawArc(class_332 g, float cx, float cy, float r, float thick, int color, float part) {
        int n = 18;
        int m = Math.max(2, Math.round((float)n * part) + 1);
        float[] xs = new float[m];
        float[] ys = new float[m];
        double from = Math.toRadians(200.0);
        double span = Math.toRadians(140.0) * (double)part;
        for (int i = 0; i < m; ++i) {
            double ang = from + span * (double)i / (double)(m - 1);
            xs[i] = cx + (float)Math.cos(ang) * r;
            ys[i] = cy + (float)Math.sin(ang) * r;
        }
        Gfx.bandPath(g, xs, ys, m, thick * 1.4f, color);
    }

    static {
        width = 150.0f;
        swap = 1.0f;
        contentKey = "";
        bars = new float[4];
    }

    private static enum Phase {
        TITLE,
        LYRICS,
        PAUSED,
        RESUMED;

    }
}


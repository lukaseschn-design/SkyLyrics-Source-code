/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package dev.lyricsflow.gui;

import dev.lyricsflow.SkyLyrics;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.config.LFConfig;
import dev.lyricsflow.gui.CoverArt;
import dev.lyricsflow.gui.Icons;
import dev.lyricsflow.gui.Ui;
import dev.lyricsflow.gui.UiSounds;
import dev.lyricsflow.lyrics.Lyrics;
import dev.lyricsflow.media.AppVolume;
import dev.lyricsflow.media.MediaManager;
import dev.lyricsflow.media.Track;
import dev.lyricsflow.render.Fonts;
import dev.lyricsflow.render.Gfx;
import dev.lyricsflow.render.Themes;
import dev.lyricsflow.util.Anim;
import dev.lyricsflow.util.Col;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class NowPlaying {
    private static final float[] btnHover = new float[5];
    private static final float[] btnPress = new float[5];
    private static final float[][] btn = new float[5][4];
    private static float playMorph = -1.0f;
    private static float barX;
    private static float barY;
    private static float barW;
    private static float barHover;
    private static float dragFrac;
    private static boolean barOn;
    private static boolean barDrag;
    private static long dragSentAt;
    private static float volX;
    private static float volY;
    private static float volW;
    private static float volIconX;
    private static float volHover;
    private static float volFrac;
    private static boolean volOn;
    private static boolean volDrag;

    public static boolean pressBar(double mx, double my) {
        if (Ui.inside(mx, my, volIconX - 6.0f, volY - 6.0f, 12.0f, 12.0f)) {
            AppVolume.toggleMute();
            UiSounds.play(UiSounds.CLICK, 1.1f);
            return true;
        }
        if (volOn && Ui.inside(mx, my, volX - 3.0f, volY - 5.0f, volW + 6.0f, 10.0f)) {
            volDrag = true;
            volFrac = (float)Math.max(0.0, Math.min(1.0, (mx - (double)volX) / (double)volW));
            AppVolume.set(volFrac);
            UiSounds.play(UiSounds.SLIDER, 1.0f);
            return true;
        }
        if (!barOn || !Ui.inside(mx, my, barX - 3.0f, barY - 5.0f, barW + 6.0f, 13.0f)) {
            return false;
        }
        barDrag = true;
        dragFrac = (float)Math.max(0.0, Math.min(1.0, (mx - (double)barX) / (double)barW));
        UiSounds.play(UiSounds.CLICK, 1.2f);
        return true;
    }

    public static boolean dragging() {
        return barDrag || volDrag;
    }

    public static void dragBar(double mx) {
        if (volDrag) {
            float nf = (float)Math.max(0.0, Math.min(1.0, (mx - (double)volX) / (double)volW));
            if (Math.abs(nf - volFrac) > 0.01f) {
                volFrac = nf;
                AppVolume.set(nf);
            }
            return;
        }
        if (!barDrag) {
            return;
        }
        float nf = (float)Math.max(0.0, Math.min(1.0, (mx - (double)barX) / (double)barW));
        if (Math.abs(nf - dragFrac) > 0.004f) {
            UiSounds.play(UiSounds.SLIDER, 0.9f + nf * 0.4f);
        }
        dragFrac = nf;
    }

    public static void releaseBar() {
        volDrag = false;
        if (!barDrag) {
            return;
        }
        barDrag = false;
        Track t = MediaManager.get().track();
        if (t != null && t.durationMs > 0L) {
            MediaManager.get().seek((long)(dragFrac * (float)t.durationMs));
            dragSentAt = System.currentTimeMillis();
        }
    }

    private NowPlaying() {
    }

    private static float coverH(float w) {
        return w - 24.0f;
    }

    public static float height(float w) {
        return 36.0f + NowPlaying.coverH(w) + 10.0f + 13.0f + 11.0f + 10.0f + 18.0f + 16.0f + 30.0f + 10.0f + 13.0f + 16.0f;
    }

    public static void renderHero(GuiGraphicsExtractor g, float x, float y, float w, float h, double mx, double my, float a) {
        int stc;
        String st;
        boolean playing;
        float frac;
        boolean live;
        boolean ytm;
        String src;
        boolean spotify;
        Themes.Theme th = Themes.current();
        MediaManager mm = MediaManager.get();
        Track t = mm.track();
        LFConfig c = ConfigManager.get();
        Gfx.shadow(g, x, y, w, h, 14.0f, 12.0f, Col.alpha(-16777216, a * 0.5f));
        Gfx.roundGradient(g, x, y, w, h, 14.0f, Col.alpha(Col.lerp(th.skyTop(), -15593447, 0.6f), a), Col.alpha(-15593447, a));
        Gfx.roundOutline(g, x, y, w, h, 14.0f, 0.6f, Col.alpha(0x22FFFFFF, a));
        float pad = 12.0f;
        float cx = x + pad;
        float cw = w - pad * 2.0f;
        float yy = y + 12.0f;
        boolean bl = spotify = t == null || t.sourceApp.equals("Spotify");
        src = t == null ? "Spotify" : (t.sourceApp.equals("Browser") ? "YouTube" : t.sourceApp);
        int srcCol = t == null ? -14756000 : (spotify ? -14756000 : (t.sourceApp.equals("Browser") ? -45499 : (t.sourceApp.equals("YT Music") ? -65485 : Themes.accent())));
        boolean yt = t != null && t.sourceApp.equals("Browser");
        boolean bl2 = ytm = t != null && t.sourceApp.equals("YT Music");
        if (spotify || yt || ytm) {
            Gfx.image(g, SkyLyrics.id(spotify ? "textures/gui/logos/spotify.png" : (ytm ? "textures/gui/logos/ytmusic.png" : "textures/gui/logos/youtube.png")), 128, 128, cx, yy, 14.0f, 14.0f, Col.alpha(-1, a));
        } else {
            Gfx.circle(g, cx + 7.0f, yy + 7.0f, 7.0f, Col.alpha(srcCol, a));
            Icons.draw(g, "play", cx + 7.6f, yy + 7.0f, 7.0f, Col.alpha(-1, a));
        }
        Gfx.text(g, src, Ui.FB, cx + 18.0f, yy + 0.5f, 7.6f, Col.alpha(-856070, a));
        Gfx.text(g, Ui.tr("lyricsflow.hero.now_playing", new Object[0]), Ui.F, cx + 18.0f, yy + 9.4f, 5.6f, Col.alpha(-6515020, a));
        boolean bl3 = live = t != null && t.playing;
        if (live) {
            double time = Anim.seconds();
            for (int i = 0; i < 3; ++i) {
                float bh = 2.5f + 5.0f * (float)(0.5 + 0.5 * Math.sin(time * (7.0 + (double)i * 2.3) + (double)i));
                Gfx.round(g, cx + cw - 12.0f + (float)(i * 4), yy + 11.0f - bh, 2.4f, bh, 1.2f, Col.alpha(srcCol, a));
            }
        }
        yy += 24.0f;
        float bh = NowPlaying.coverH(w);
        boolean art = CoverArt.ready();
        if (art) {
            Gfx.shadow(g, cx, yy, cw, bh, 6.0f, 6.0f, Col.alpha(-16777216, a * 0.5f));
            CoverArt.draw(g, cx, yy, cw, a);
            Gfx.roundOutline(g, cx, yy, cw, bh, 4.0f, 0.5f, Col.alpha(0x30FFFFFF, a));
            Gfx.gradientV(g, cx, yy + bh * 0.55f, cw, bh * 0.45f, Col.alpha(0, a), Col.alpha(-1073741824, a));
            g.enableScissor((int)cx, (int)(yy + bh * 0.5f), (int)Math.ceil(cx + cw), (int)Math.ceil(yy + bh));
            NowPlaying.previewLyrics(g, cx, yy + bh * 0.5f, cw, bh * 0.5f, a, th, c);
            g.disableScissor();
        } else {
            Gfx.roundGradient(g, cx, yy, cw, bh, 10.0f, Col.alpha(th.skyTop(), a), Col.alpha(th.skyBottom(), a));
            double time = Anim.seconds();
            for (int i = 0; i < 20; ++i) {
                float sx2 = cx + 6.0f + Anim.hash01((long)i * 13L) * (cw - 12.0f);
                float sy2 = yy + 5.0f + Anim.hash01((long)i * 29L + 7L) * (bh * 0.55f);
                float tw = 0.35f + 0.65f * (float)(0.5 + 0.5 * Math.sin(time * (double)(1.0f + Anim.hash01(i) * 2.0f) + (double)i));
                Gfx.circle(g, sx2, sy2, 0.5f + Anim.hash01((long)i * 7L) * 0.5f, Col.alpha(-1, a * tw * 0.8f));
            }
            g.enableScissor((int)cx, (int)yy, (int)Math.ceil(cx + cw), (int)Math.ceil(yy + bh));
            NowPlaying.previewLyrics(g, cx, yy, cw, bh, a, th, c);
            g.disableScissor();
        }
        String title = t == null ? Ui.tr("lyricsflow.hero.nothing_title", new Object[0]) : t.title;
        String artist = t == null ? Ui.tr("lyricsflow.hero.nothing_sub", new Object[0]) : t.artist;
        Gfx.text(g, Gfx.ellipsize(title, Ui.FB, 10.5f, cw), Ui.FB, cx, yy += bh + 10.0f, 10.5f, Col.alpha(-856070, a));
        Gfx.text(g, Gfx.ellipsize(artist, Ui.F, 7.4f, cw), Ui.F, cx, yy += 13.0f, 7.4f, Col.alpha(-6515020, a));
        yy += 21.0f;
        long pos = Math.max(0L, mm.positionRaw());
        long dur = t == null ? 0L : t.durationMs;
        float f = frac = dur > 0L ? Math.min(1.0f, (float)pos / (float)dur) : 0.0f;
        if (barDrag) {
            frac = dragFrac;
        } else if (dragFrac >= 0.0f && System.currentTimeMillis() - dragSentAt < 900L) {
            frac = dragFrac;
        } else {
            dragFrac = -1.0f;
        }
        barX = cx;
        barY = yy;
        barW = cw;
        barOn = dur > 0L;
        boolean overBar = barOn && Ui.inside(mx, my, cx - 3.0f, yy - 5.0f, cw + 6.0f, 13.0f);
        barHover = Anim.approach(barHover, overBar || barDrag ? 1.0f : 0.0f, 14.0f);
        float bt = 3.0f + barHover * 1.2f;
        int green = spotify ? -14756000 : Themes.accent();
        Gfx.round(g, cx, yy + 1.5f - bt / 2.0f, cw, bt, bt / 2.0f, Col.alpha(0x30FFFFFF, a));
        if (frac > 0.0f) {
            Gfx.round(g, cx, yy + 1.5f - bt / 2.0f, Math.max(bt, cw * frac), bt, bt / 2.0f, Col.alpha(barHover > 0.05f ? green : -1, a));
        }
        Gfx.circle(g, cx + cw * frac, yy + 1.5f, 2.6f + barHover * 1.4f, Col.alpha(-1, a));
        Gfx.text(g, NowPlaying.fmt(t == null ? 0L : (dur > 0L ? (long)(frac * (float)dur) : pos)), Ui.F, cx, yy + 6.0f, 5.6f, Col.alpha(barDrag ? -856070 : -9541498, a));
        String right = dur > 0L ? NowPlaying.fmt(dur) : "--:--";
        Gfx.text(g, right, Ui.F, cx + cw - Gfx.textWidth(right, Ui.F, 5.6f), yy + 6.0f, 5.6f, Col.alpha(-9541498, a));
        yy += 18.0f;
        float vol = AppVolume.get();
        if (volDrag) {
            vol = volFrac;
        }
        boolean mute = AppVolume.muted();
        volIconX = cx + 4.0f;
        volX = cx + 14.0f;
        volY = yy + 4.0f;
        volW = cw - 40.0f;
        volOn = vol >= 0.0f;
        boolean overVol = volOn && Ui.inside(mx, my, volX - 3.0f, volY - 5.0f, volW + 6.0f, 10.0f);
        volHover = Anim.approach(volHover, overVol || volDrag ? 1.0f : 0.0f, 14.0f);
        Icons.draw(g, mute || volOn && vol < 0.01f ? "mute" : "volume", volIconX, volY, 8.0f, Col.alpha(volOn ? -6515020 : -9541498, a));
        if (volOn) {
            float f2 = mute ? 0.0f : vol;
            float vt = 2.5f + volHover;
            Gfx.round(g, volX, volY - vt / 2.0f, volW, vt, vt / 2.0f, Col.alpha(0x30FFFFFF, a));
            if (f2 > 0.0f) {
                Gfx.round(g, volX, volY - vt / 2.0f, Math.max(vt, volW * f2), vt, vt / 2.0f, Col.alpha(volHover > 0.05f ? green : -1, a));
            }
            Gfx.circle(g, volX + volW * f2, volY, 2.4f + volHover * 1.2f, Col.alpha(-1, a));
            Gfx.text(g, Math.round(f2 * 100.0f) + "%", Ui.F, volX + volW + 5.0f, volY - 2.8f, 5.6f, Col.alpha(-9541498, a));
        } else {
            Gfx.text(g, Gfx.ellipsize(Ui.tr("lyricsflow.player.volume_none", new Object[0]), Ui.F, 5.6f, cw - 18.0f), Ui.F, volX, volY - 2.8f, 5.6f, Col.alpha(-9541498, a));
        }
        float by = (yy += 16.0f) + 13.0f;
        float mid = x + w / 2.0f;
        NowPlaying.drawButton(g, 0, mid - 34.0f, by, 10.0f, "prev", mx, my, a, false);
        boolean bl4 = playing = t != null && t.playing;
        if (playMorph < 0.0f) {
            playMorph = playing ? 1.0f : 0.0f;
        }
        playMorph = Anim.approach(playMorph, playing ? 1.0f : 0.0f, 12.0f);
        NowPlaying.drawButton(g, 1, mid, by, 13.0f, playing ? "pause" : "play", mx, my, a, true);
        NowPlaying.drawButton(g, 2, mid + 34.0f, by, 10.0f, "next", mx, my, a, false);
        NowPlaying.drawRepeat(g, 4, mid + 62.0f, by, 7.5f, mx, my, a, -14756000);
        yy += 40.0f;
        MediaManager.LyricsState ls = mm.lyricsState();
        Lyrics l = mm.lyrics();
        if (t == null) {
            st = Ui.tr(mm.statusKey(), new Object[0]);
            stc = -9541498;
        } else if (ls == MediaManager.LyricsState.SEARCHING) {
            st = Ui.tr("lyricsflow.lyrics.searching", new Object[0]);
            stc = -11414;
        } else if (ls == MediaManager.LyricsState.NOT_FOUND) {
            st = Ui.tr("lyricsflow.lyrics.not_found", new Object[0]);
            stc = -34182;
        } else if (l != null && !l.synced) {
            st = Ui.tr("lyricsflow.lyrics.estimated", new Object[0]);
            stc = -18326;
        } else {
            st = Ui.tr("lyricsflow.lyrics.synced", new Object[0]);
            stc = -11870592;
        }
        float chipY = yy;
        st = Gfx.ellipsize(st, Ui.F, 6.4f, cw - 36.0f);
        float chw = Gfx.textWidth(st, Ui.F, 6.4f) + 18.0f + (float)(t != null ? 14 : 0);
        float chx = mid - chw / 2.0f;
        Gfx.round(g, chx, chipY, chw, 13.0f, 6.5f, Col.alpha(0x30000000, a));
        Gfx.circle(g, chx + 7.0f, chipY + 6.5f, 2.4f, Col.alpha(stc, a));
        Gfx.text(g, st, Ui.F, chx + 12.0f, chipY + 3.3f, 6.4f, Col.alpha(-856070, a));
        if (t != null) {
            boolean over = Ui.inside(mx, my, chx + chw - 15.0f, chipY, 15.0f, 13.0f);
            NowPlaying.btnHover[3] = Anim.approach(btnHover[3], over ? 1.0f : 0.0f, 14.0f);
            NowPlaying.btn[3][0] = chx + chw - 8.0f;
            NowPlaying.btn[3][1] = chipY + 6.5f;
            NowPlaying.btn[3][2] = 7.0f;
            g.pose().pushMatrix();
            g.pose().translate(btn[3][0], btn[3][1]);
            g.pose().rotate(btnHover[3] * 1.2f);
            Icons.draw(g, "refresh", 0.0f, 0.0f, 7.0f, Col.alpha(Col.lerp(-6515020, -1, btnHover[3]), a));
            g.pose().popMatrix();
        } else {
            NowPlaying.btn[3][2] = 0.0f;
        }
        if (t != null && l != null) {
            Gfx.textCentered(g, Ui.tr("lyricsflow.hero.source", NowPlaying.sourceName(l.source)), Ui.F, mid, y + h - 9.5f, 5.4f, Col.alpha(-9541498, a));
        }
    }

    static String sourceName(String src) {
        String s;
        String string = s = src == null ? "" : src.toLowerCase(Locale.ROOT);
        String base = s.contains("musixmatch") ? "Musixmatch" : (s.contains("netease") ? "NetEase" : (s.contains("lrclib") ? "LRCLIB" : (src == null || src.isBlank() ? "?" : src)));
        if (s.contains("word")) {
            base += " \u00b7 " + Ui.tr("lyricsflow.hero.word_by_word", new Object[0]);
        }
        return base;
    }

    private static String fmt(long ms) {
        long s = Math.max(0L, ms / 1000L);
        return s / 60L + ":" + String.format("%02d", s % 60L);
    }

    private static void drawButton(GuiGraphicsExtractor g, int i, float cx, float cy, float r, String icon, double mx, double my, float a, boolean primary) {
        boolean over = Ui.inside(mx, my, cx - r, cy - r, r * 2.0f, r * 2.0f);
        if (over && btnHover[i] < 0.05f) {
            UiSounds.play(UiSounds.HOVER, 1.25f);
        }
        NowPlaying.btnHover[i] = Anim.approach(btnHover[i], over ? 1.0f : 0.0f, 14.0f);
        NowPlaying.btnPress[i] = Anim.approach(btnPress[i], 0.0f, 9.0f);
        NowPlaying.btn[i][0] = cx;
        NowPlaying.btn[i][1] = cy;
        NowPlaying.btn[i][2] = r;
        float rr = r * (1.0f + btnHover[i] * 0.07f - btnPress[i] * 0.12f);
        if (primary) {
            Gfx.glow(g, cx - rr, cy - rr, rr * 2.0f, rr * 2.0f, rr, 5.0f, Col.alpha(Themes.accent(), a * (0.25f + 0.3f * btnHover[i])));
            Gfx.circle(g, cx, cy, rr, Col.alpha(-1, a));
            Icons.draw(g, icon, cx + (icon.equals("play") ? 1.0f : 0.0f), cy, rr * 0.95f, Col.alpha(-15330529, a));
        } else {
            Gfx.circle(g, cx, cy, rr, Col.alpha(Col.lerp(0x20FFFFFF, 0x38FFFFFF, btnHover[i]), a));
            Icons.draw(g, icon, cx, cy, rr * 0.95f, Col.alpha(-1, a));
        }
    }

    static void drawRepeat(GuiGraphicsExtractor g, int i, float cx, float cy, float r, double mx, double my, float a, int on) {
        int mode = MediaManager.get().repeat();
        boolean over = Ui.inside(mx, my, cx - r, cy - r, r * 2.0f, r * 2.0f);
        if (over && btnHover[i] < 0.05f) {
            UiSounds.play(UiSounds.HOVER, 1.25f);
        }
        NowPlaying.btnHover[i] = Anim.approach(btnHover[i], over ? 1.0f : 0.0f, 14.0f);
        NowPlaying.btnPress[i] = Anim.approach(btnPress[i], 0.0f, 9.0f);
        NowPlaying.btn[i][0] = cx;
        NowPlaying.btn[i][1] = cy;
        NowPlaying.btn[i][2] = r;
        float rr = r * (1.0f + btnHover[i] * 0.07f - btnPress[i] * 0.12f);
        int col = mode == 0 ? Col.lerp(-2434342, -1, btnHover[i]) : on;
        Icons.draw(g, mode == 1 ? "repeat_one" : "repeat", cx, cy - 0.5f, rr * 1.35f, Col.alpha(col, a));
        if (mode != 0) {
            Gfx.circle(g, cx, cy + rr + 1.6f, 1.3f, Col.alpha(on, a));
        }
    }

    public static void click(double mx, double my) {
        MediaManager mm = MediaManager.get();
        if (NowPlaying.pressBar(mx, my)) {
            if (!volDrag) {
                NowPlaying.releaseBar();
            } else {
                volDrag = false;
            }
            return;
        }
        for (int i = 0; i < 5; ++i) {
            double dy;
            double dx;
            float[] b = btn[i];
            if (b[2] <= 0.0f || !((dx = mx - (double)b[0]) * dx + (dy = my - (double)b[1]) * dy <= (double)((b[2] + 2.0f) * (b[2] + 2.0f)))) continue;
            NowPlaying.btnPress[i] = 1.0f;
            UiSounds.play(UiSounds.CLICK, i == 1 ? 1.0f : 1.15f);
            switch (i) {
                case 0: {
                    mm.previous();
                    break;
                }
                case 1: {
                    mm.playPause();
                    break;
                }
                case 2: {
                    mm.next();
                    break;
                }
                case 3: {
                    mm.refetch();
                    break;
                }
                case 4: {
                    mm.cycleRepeat();
                }
            }
            return;
        }
    }

    private static void previewLyrics(GuiGraphicsExtractor g, float x, float y, float w, float h, float a, Themes.Theme th, LFConfig c) {
        MediaManager mm = MediaManager.get();
        Lyrics l = mm.lyrics();
        long t = mm.position();
        String font = c.font;
        if (l == null || t < 0L) {
            String s = mm.track() == null ? Ui.tr("lyricsflow.hero.preview_idle", new Object[0]) : Ui.tr("lyricsflow.hero.preview_wait", new Object[0]);
            float size = 12.0f;
            Gfx.neon(g, Gfx.ellipsize(s, font, size, w - 16.0f), font, x + w / 2.0f, y + h / 2.0f - size / 2.0f - 4.0f, size, th.main(), th.a(), th.b(), a * 0.85f, false, c.glow ? 0.6f : 0.0f);
            return;
        }
        Lyrics.Line line = l.lineAt(t);
        if (line == null) {
            line = l.lastStartedBefore(t);
        }
        if (line == null) {
            return;
        }
        float size = 13.0f;
        ArrayList<List<Lyrics.Word>> rows = new ArrayList<>();
        ArrayList<Lyrics.Word> row = new ArrayList<Lyrics.Word>();
        float rw = 0.0f;
        float space = Gfx.textWidth(" ", font, size);
        for (Lyrics.Word word : line.words) {
            float ww = Gfx.textWidth(Lyrics.show(word.text), font, size);
            if (!row.isEmpty() && rw + space + ww > w - 18.0f) {
                rows.add(row);
                row = new ArrayList();
                rw = 0.0f;
            }
            rw += (row.isEmpty() ? 0.0f : space) + ww;
            row.add(word);
        }
        if (!row.isEmpty()) {
            rows.add(row);
        }
        if (Fonts.isRtl(line.text)) {
            for (List<Lyrics.Word> list : rows) {
                Collections.reverse(list);
            }
        }
        float lineH = size * 1.25f;
        float f = y + h * 0.5f - (float)rows.size() * lineH / 2.0f;
        g.pose().pushMatrix();
        g.pose().translate(x + w / 2.0f, y + h / 2.0f);
        g.pose().rotate((float)Math.toRadians(-4.0));
        g.pose().translate(-(x + w / 2.0f), -(y + h / 2.0f));
        for (int r = 0; r < rows.size(); ++r) {
            List<Lyrics.Word> rr = rows.get(r);
            float total = 0.0f;
            for (int i = 0; i < rr.size(); ++i) {
                total += Gfx.textWidth(Lyrics.show(((Lyrics.Word)rr.get((int)i)).text), font, size) + (i > 0 ? space : 0.0f);
            }
            float wx = x + w / 2.0f - total / 2.0f;
            for (Lyrics.Word wd : rr) {
                float ww = Gfx.textWidth(Lyrics.show(wd.text), font, size);
                float p = Anim.progress(t, wd.start - 40L, wd.start + 260L);
                if (p > 0.0f) {
                    float e = Anim.easeOutCubic(p);
                    boolean sung = t >= wd.start && t < wd.end + 80L;
                    Gfx.neon(g, Lyrics.show(wd.text), font, wx + ww / 2.0f, f + (float)r * lineH + (1.0f - e) * 4.0f, size, th.main(), th.a(), th.b(), a * e, false, (c.glow ? 0.55f : 0.0f) + (sung ? 0.4f : 0.0f));
                }
                wx += ww + space;
            }
        }
        g.pose().popMatrix();
    }

    static {
        dragFrac = -1.0f;
    }
}

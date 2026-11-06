/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.network.chat.Component
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 */
package dev.lyricsflow.gui;

import dev.lyricsflow.Keys;
import dev.lyricsflow.gui.CoverArt;
import dev.lyricsflow.gui.Icons;
import dev.lyricsflow.gui.Ui;
import dev.lyricsflow.gui.UiSounds;
import dev.lyricsflow.lyrics.Lyrics;
import dev.lyricsflow.media.AppVolume;
import dev.lyricsflow.media.MediaManager;
import dev.lyricsflow.media.Track;
import dev.lyricsflow.render.Gfx;
import dev.lyricsflow.render.Hud;
import dev.lyricsflow.render.Themes;
import dev.lyricsflow.util.Anim;
import dev.lyricsflow.util.Col;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

public class PlayerScreen
extends Screen {
    private float openT;
    private boolean closing;
    private float scroll = -1.0f;
    private float userScroll;
    private double lastUserScroll = -10.0;
    private final float[] btnHover = new float[4];
    private final float[] btnPress = new float[4];
    private float[] lineY = new float[0];
    private float px;
    private float py;
    private float pw;
    private float ph;
    private float lx;
    private float lw;
    private float ly;
    private float lh;
    private final Screen parent;
    private final boolean blur;
    private float barX;
    private float barY;
    private float barW;
    private boolean seeking;
    private float seekFrac = -1.0f;
    private float barHover;
    private long seekSentAt;
    private boolean editing;
    private String timeText = "";
    private float tbX;
    private float tbY;
    private float tbW;
    private float tbH;
    private float mX;
    private float pX;
    private float jW;
    private float timeHover;
    private float vX;
    private float vY;
    private float vW;
    private float iconX;
    private boolean volDrag;
    private float volHover;
    private float volShown = -1.0f;
    private float ctlMid;
    private float ctlY;

    public PlayerScreen() {
        this(null, false);
    }

    public PlayerScreen(Screen parent, boolean blur) {
        super((Component)Component.translatable((String)"lyricsflow.player.title"));
        this.parent = parent;
        this.blur = blur;
    }

    protected void init() {
        UiSounds.play(UiSounds.OPEN, 0.9f);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float pt) {
        if (this.blur) {
            this.extractBlurredBackground(g);
        }
    }

    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        float frac;
        Anim.frame();
        this.openT = Anim.approach(this.openT, this.closing ? 0.0f : 1.0f, this.closing ? 16.0f : 9.0f);
        if (this.closing && this.openT < 0.02f) {
            this.finish();
            return;
        }
        float a = Anim.easeOutCubic(this.openT);
        Themes.Theme th = Themes.current();
        MediaManager mm = MediaManager.get();
        Track t = mm.track();
        Lyrics l = mm.lyrics();
        long pos = mm.positionRaw();
        Gfx.gradientV(g, 0.0f, 0.0f, this.width, this.height, Col.alpha(Col.withAlpha(Col.darken(th.skyTop(), 0.3f), 224), a), Col.alpha(-267778032, a));
        this.pw = Math.min(680, this.width - 30);
        this.ph = Math.min(380, this.height - 30);
        this.px = ((float)this.width - this.pw) / 2.0f;
        this.py = ((float)this.height - this.ph) / 2.0f + (1.0f - a) * 12.0f;
        Gfx.shadow(g, this.px, this.py, this.pw, this.ph, 18.0f, 16.0f, Col.alpha(-16777216, a * 0.6f));
        Gfx.roundGradient(g, this.px, this.py, this.pw, this.ph, 18.0f, Col.alpha(Col.lerp(th.skyTop(), -15593447, 0.4f), a), Col.alpha(-15856620, a));
        float leftW = Math.min(230.0f, this.pw * 0.38f);
        float cs = Math.min(leftW - 40.0f, this.ph - 214.0f);
        float cx = this.px + (leftW - cs) / 2.0f + 6.0f;
        float cy = this.py + 22.0f;
        Gfx.glow(g, cx, cy, cs, cs, 14.0f, 8.0f, Col.alpha(th.a(), a * (0.25f + 0.25f * Hud.beat)));
        if (CoverArt.ready()) {
            CoverArt.draw(g, cx, cy, cs, a);
            Gfx.roundOutline(g, cx, cy, cs, cs, 6.0f, 0.6f, Col.alpha(0x30FFFFFF, a));
        } else {
            Gfx.roundGradient(g, cx, cy, cs, cs, 14.0f, Col.alpha(th.skyTop(), a), Col.alpha(th.skyBottom(), a));
            float dr = cs * 0.33f;
            double spin = Anim.seconds() * (t != null && t.playing ? 1.4 : 0.0);
            Gfx.circle(g, cx + cs / 2.0f, cy + cs / 2.0f, dr, Col.alpha(-15396322, a));
            Gfx.circle(g, cx + cs / 2.0f, cy + cs / 2.0f, dr * 0.72f, Col.alpha(-14541264, a));
            Gfx.circle(g, cx + cs / 2.0f + (float)Math.cos(spin) * dr * 0.5f, cy + cs / 2.0f + (float)Math.sin(spin) * dr * 0.5f, dr * 0.08f, Col.alpha(-2130706433, a));
            Gfx.circle(g, cx + cs / 2.0f, cy + cs / 2.0f, dr * 0.25f, Col.alpha(th.a(), a));
        }
        float ty = cy + cs + 12.0f;
        String title = t == null ? Ui.tr("lyricsflow.hero.nothing_title", new Object[0]) : t.title;
        String artist = t == null ? Ui.tr("lyricsflow.hero.nothing_sub", new Object[0]) : t.artist;
        Gfx.textCentered(g, Gfx.ellipsize(title, Ui.FB, 12.0f, leftW - 20.0f), Ui.FB, this.px + leftW / 2.0f + 6.0f, ty, 12.0f, Col.alpha(-856070, a));
        Gfx.textCentered(g, Gfx.ellipsize(artist, Ui.F, 8.0f, leftW - 20.0f), Ui.F, this.px + leftW / 2.0f + 6.0f, ty + 15.0f, 8.0f, Col.alpha(-6515020, a));
        this.barW = leftW - 36.0f;
        this.barX = this.px + leftW / 2.0f + 6.0f - this.barW / 2.0f;
        this.barY = ty + 31.0f;
        long dur = t == null ? 0L : t.durationMs;
        boolean overBar = dur > 0L && Ui.inside(mouseX, mouseY, this.barX - 4.0f, this.barY - 6.0f, this.barW + 8.0f, 12.0f);
        this.barHover = Anim.approach(this.barHover, overBar || this.seeking ? 1.0f : 0.0f, 14.0f);
        float f = frac = dur > 0L ? Math.max(0.0f, Math.min(1.0f, (float)pos / (float)dur)) : 0.0f;
        if (this.seeking) {
            frac = this.seekFrac;
        } else if (this.seekFrac >= 0.0f && System.currentTimeMillis() - this.seekSentAt < 900L) {
            frac = this.seekFrac;
        } else {
            this.seekFrac = -1.0f;
        }
        float bh = 3.0f + this.barHover * 1.2f;
        Gfx.round(g, this.barX, this.barY - bh / 2.0f, this.barW, bh, bh / 2.0f, Col.alpha(0x30FFFFFF, a));
        if (frac > 0.0f) {
            Gfx.roundGradient(g, this.barX, this.barY - bh / 2.0f, Math.max(bh, this.barW * frac), bh, bh / 2.0f, Col.alpha(th.a(), a), Col.alpha(th.b(), a));
        }
        float knob = 3.2f + this.barHover * 1.6f;
        Gfx.glow(g, this.barX + this.barW * frac - knob, this.barY - knob, knob * 2.0f, knob * 2.0f, knob, 3.0f, Col.alpha(th.a(), a * 0.4f * this.barHover));
        Gfx.circle(g, this.barX + this.barW * frac, this.barY, knob, Col.alpha(-1, a * (0.6f + 0.4f * this.barHover)));
        String left = PlayerScreen.fmt(dur > 0L ? (long)(frac * (float)dur) : Math.max(0L, pos));
        String right = dur > 0L ? PlayerScreen.fmt(dur) : "--:--";
        Gfx.text(g, left, Ui.F, this.barX, this.barY + 5.0f, 6.2f, Col.alpha(this.seeking ? -856070 : -9541498, a));
        Gfx.text(g, right, Ui.F, this.barX + this.barW - Gfx.textWidth(right, Ui.F, 6.2f), this.barY + 5.0f, 6.2f, Col.alpha(-9541498, a));
        float mid = this.px + leftW / 2.0f + 6.0f;
        this.tbH = 13.0f;
        this.tbY = this.barY + 15.0f;
        String shown = this.editing ? this.timeText + (System.currentTimeMillis() / 450L % 2L == 0L ? "|" : " ") : left + " / " + right;
        this.tbW = Math.max(62.0f, Gfx.textWidth(shown, Ui.FB, 6.8f) + 16.0f);
        this.tbX = mid - this.tbW / 2.0f;
        this.jW = 24.0f;
        this.mX = this.tbX - this.jW - 5.0f;
        this.pX = this.tbX + this.tbW + 5.0f;
        boolean overTb = Ui.inside(mouseX, mouseY, this.tbX, this.tbY, this.tbW, this.tbH);
        this.timeHover = Anim.approach(this.timeHover, overTb || this.editing ? 1.0f : 0.0f, 14.0f);
        Gfx.round(g, this.tbX, this.tbY, this.tbW, this.tbH, 6.5f, Col.alpha(Col.lerp(0x24FFFFFF, 0x38FFFFFF, this.timeHover), a));
        Gfx.roundOutline(g, this.tbX, this.tbY, this.tbW, this.tbH, 6.5f, this.editing ? 1.0f : 0.5f, Col.alpha(this.editing ? Themes.accent() : 0x20FFFFFF, a));
        Gfx.textCentered(g, shown, Ui.FB, mid, this.tbY + (this.tbH - 6.8f) / 2.0f, 6.8f, Col.alpha(this.editing ? -1 : -6515020, a));
        for (int k = 0; k < 2; ++k) {
            float jx = k == 0 ? this.mX : this.pX;
            boolean ov = Ui.inside(mouseX, mouseY, jx, this.tbY, this.jW, this.tbH);
            Gfx.round(g, jx, this.tbY, this.jW, this.tbH, 6.5f, Col.alpha(ov ? 0x38FFFFFF : 0x20FFFFFF, a));
            Gfx.textCentered(g, k == 0 ? "-10" : "+10", Ui.FB, jx + this.jW / 2.0f, this.tbY + (this.tbH - 6.4f) / 2.0f, 6.4f, Col.alpha(-6515020, a));
        }
        float by = Math.min(this.py + this.ph - 52.0f, this.tbY + this.tbH + 24.0f);
        this.ctlMid = mid;
        this.ctlY = by;
        this.button(g, 0, mid - 40.0f, by, 12.0f, "prev", false, mouseX, mouseY, a);
        this.button(g, 1, mid, by, 17.0f, t != null && t.playing ? "pause" : "play", true, mouseX, mouseY, a);
        this.button(g, 2, mid + 40.0f, by, 12.0f, "next", false, mouseX, mouseY, a);
        int mode = MediaManager.get().repeat();
        int on = -14756000;
        boolean ov = Ui.inside(mouseX, mouseY, mid + 72.0f - 9.0f, by - 9.0f, 18.0f, 18.0f);
        if (ov && this.btnHover[3] < 0.05f) {
            UiSounds.play(UiSounds.HOVER, 1.2f);
        }
        this.btnHover[3] = Anim.approach(this.btnHover[3], ov ? 1.0f : 0.0f, 14.0f);
        this.btnPress[3] = Anim.approach(this.btnPress[3], 0.0f, 9.0f);
        float rr = 9.0f * (1.0f + this.btnHover[3] * 0.08f - this.btnPress[3] * 0.14f);
        int col = mode == 0 ? Col.lerp(-2434342, -1, this.btnHover[3]) : on;
        Icons.draw(g, mode == 1 ? "repeat_one" : "repeat", mid + 72.0f, by - 0.5f, rr * 1.3f, Col.alpha(col, a));
        if (mode != 0) {
            Gfx.circle(g, mid + 72.0f, by + rr + 1.5f, 1.2f, Col.alpha(on, a));
        }
        this.vY = by + 30.0f;
        this.vW = leftW - 70.0f;
        this.vX = mid - this.vW / 2.0f + 8.0f;
        this.iconX = this.vX - 14.0f;
        float vol = AppVolume.get();
        if (!this.volDrag) {
            this.volShown = vol;
        }
        boolean have = this.volShown >= 0.0f;
        boolean overVol = have && Ui.inside(mouseX, mouseY, this.vX - 4.0f, this.vY - 6.0f, this.vW + 8.0f, 12.0f);
        this.volHover = Anim.approach(this.volHover, overVol || this.volDrag ? 1.0f : 0.0f, 14.0f);
        boolean mute = AppVolume.muted();
        Icons.draw(g, mute || have && this.volShown < 0.01f ? "mute" : "volume", this.iconX, this.vY, 9.0f, Col.alpha(have ? -856070 : -9541498, a));
        if (have) {
            float vb = 3.0f + this.volHover * 1.2f;
            float f2 = mute ? 0.0f : this.volShown;
            Gfx.round(g, this.vX, this.vY - vb / 2.0f, this.vW, vb, vb / 2.0f, Col.alpha(0x30FFFFFF, a));
            if (f2 > 0.0f) {
                Gfx.round(g, this.vX, this.vY - vb / 2.0f, Math.max(vb, this.vW * f2), vb, vb / 2.0f, Col.alpha(-1, a * 0.85f));
            }
            Gfx.circle(g, this.vX + this.vW * f2, this.vY, 3.0f + this.volHover * 1.4f, Col.alpha(-1, a));
            Gfx.text(g, Math.round(f2 * 100.0f) + "%", Ui.F, this.vX + this.vW + 6.0f, this.vY - 3.1f, 6.2f, Col.alpha(-9541498, a));
        } else {
            Gfx.text(g, Ui.tr("lyricsflow.player.volume_none", new Object[0]), Ui.F, this.vX, this.vY - 3.1f, 6.2f, Col.alpha(-9541498, a));
        }
        this.lx = this.px + leftW + 16.0f;
        this.lw = this.pw - leftW - 32.0f;
        this.ly = this.py + 16.0f;
        this.lh = this.ph - 32.0f;
        Gfx.round(g, this.lx, this.ly, this.lw, this.lh, 12.0f, Col.alpha(0x40000000, a));
        g.enableScissor((int)this.lx, (int)this.ly, (int)Math.ceil(this.lx + this.lw), (int)Math.ceil(this.ly + this.lh));
        if (l == null || l.lines.isEmpty()) {
            String s = t == null ? Ui.tr("lyricsflow.hero.preview_idle", new Object[0]) : Ui.tr(mm.lyricsState() == MediaManager.LyricsState.SEARCHING ? "lyricsflow.lyrics.searching" : "lyricsflow.lyrics.not_found", new Object[0]);
            Gfx.textCentered(g, s, Ui.FB, this.lx + this.lw / 2.0f, this.ly + this.lh / 2.0f - 5.0f, 10.0f, Col.alpha(-6515020, a));
        } else {
            this.drawLyrics(g, l, pos, mouseX, mouseY, a, th);
        }
        g.disableScissor();
        Gfx.gradientV(g, this.lx, this.ly, this.lw, 14.0f, Col.alpha(0x60000000, a), 0);
        Gfx.gradientV(g, this.lx, this.ly + this.lh - 14.0f, this.lw, 14.0f, 0, Col.alpha(0x60000000, a));
        Gfx.text(g, Ui.tr("lyricsflow.player.hint", new Object[0]), Ui.F, this.px + 14.0f, this.py + this.ph - 11.0f, 5.6f, Col.alpha(-9541498, a));
    }

    private void drawLyrics(GuiGraphicsExtractor g, Lyrics l, long pos, double mx, double my, float a, Themes.Theme th) {
        float target;
        float size = 11.0f;
        float gap = 7.0f;
        List<Lyrics.Line> lines = l.lines;
        if (this.lineY.length != lines.size() + 1) {
            this.lineY = new float[lines.size() + 1];
        }
        float y = 0.0f;
        ArrayList<List<String>> wrapped = new ArrayList<List<String>>();
        for (int i = 0; i < lines.size(); ++i) {
            this.lineY[i] = y;
            List<String> w = Ui.wrap(lines.get((int)i).text, Ui.FB, size, this.lw - 28.0f);
            wrapped.add(w);
            y += (float)w.size() * (size + 3.0f) + gap;
        }
        this.lineY[lines.size()] = y;
        Lyrics.Line cur = l.lastStartedBefore(pos);
        int ci = cur == null ? -1 : cur.index;
        float f = target = ci < 0 ? 0.0f : this.lineY[ci] - this.lh * 0.38f;
        if (Anim.seconds() - this.lastUserScroll < 3.0) {
            target = this.userScroll;
        }
        target = Math.max(0.0f, Math.min(Math.max(0.0f, y - this.lh + 20.0f), target));
        if (this.scroll < 0.0f) {
            this.scroll = target;
        }
        this.scroll = Anim.approach(this.scroll, target, 5.0f);
        for (int i = 0; i < lines.size(); ++i) {
            int col = i == ci ? -1 : (i < ci ? -7567706 : -11054484);
            float yy = this.ly + 16.0f + this.lineY[i] - this.scroll;
            if (yy > this.ly + this.lh || yy + (this.lineY[i + 1] - this.lineY[i]) < this.ly) continue;
            boolean over = Ui.inside(mx, my, this.lx, yy - 2.0f, this.lw, this.lineY[i + 1] - this.lineY[i] - gap + 4.0f);
            if (over && i != ci) {
                col = Col.lerp(col, -1, 0.5f);
            }
            List w = (List)wrapped.get(i);
            for (int k = 0; k < w.size(); ++k) {
                float lineYPos = yy + (float)k * (size + 3.0f);
                if (i == ci) {
                    Gfx.glow(g, this.lx + 12.0f, lineYPos, Gfx.textWidth((String)w.get(k), Ui.FB, size), size, 4.0f, 4.0f, Col.alpha(th.a(), a * 0.18f));
                }
                Gfx.text(g, (String)w.get(k), Ui.FB, this.lx + 14.0f, lineYPos, size, Col.alpha(col, a));
            }
        }
    }

    private static String fmt(long ms) {
        long s = ms / 1000L;
        return String.format("%d:%02d", s / 60L, s % 60L);
    }

    private void button(GuiGraphicsExtractor g, int i, float x, float y, float r, String icon, boolean primary, double mx, double my, float a) {
        boolean over = Ui.inside(mx, my, x - r, y - r, r * 2.0f, r * 2.0f);
        if (over && this.btnHover[i] < 0.05f) {
            UiSounds.play(UiSounds.HOVER, 1.2f);
        }
        this.btnHover[i] = Anim.approach(this.btnHover[i], over ? 1.0f : 0.0f, 14.0f);
        this.btnPress[i] = Anim.approach(this.btnPress[i], 0.0f, 9.0f);
        float rr = r * (1.0f + this.btnHover[i] * 0.08f - this.btnPress[i] * 0.14f);
        if (primary) {
            Gfx.glow(g, x - rr, y - rr, rr * 2.0f, rr * 2.0f, rr, 5.0f, Col.alpha(Themes.accent(), a * (0.3f + 0.3f * this.btnHover[i])));
            Gfx.circle(g, x, y, rr, Col.alpha(-1, a));
            Icons.draw(g, icon, x + (icon.equals("play") ? 1.0f : 0.0f), y, rr * 0.95f, Col.alpha(-15330529, a));
        } else {
            Gfx.circle(g, x, y, rr, Col.alpha(Col.lerp(0x20FFFFFF, 0x38FFFFFF, this.btnHover[i]), a));
            Icons.draw(g, icon, x, y, rr * 0.95f, Col.alpha(-1, a));
        }
    }

    public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
        double mx = e.x();
        double my = e.y();
        MediaManager mm = MediaManager.get();
        float by = this.ctlY;
        float mid = this.ctlMid;
        Track tr = mm.track();
        if (this.editing && !Ui.inside(mx, my, this.tbX, this.tbY, this.tbW, this.tbH)) {
            this.editing = false;
        }
        if (tr != null && tr.durationMs > 0L && Ui.inside(mx, my, this.barX - 4.0f, this.barY - 7.0f, this.barW + 8.0f, 14.0f)) {
            this.seeking = true;
            this.seekFrac = (float)Math.max(0.0, Math.min(1.0, (mx - (double)this.barX) / (double)this.barW));
            UiSounds.play(UiSounds.CLICK, 1.2f);
            return true;
        }
        if (tr != null && Ui.inside(mx, my, this.tbX, this.tbY, this.tbW, this.tbH)) {
            this.editing = true;
            this.timeText = "";
            UiSounds.play(UiSounds.CLICK, 1.2f);
            return true;
        }
        for (int k = 0; k < 2; ++k) {
            if (tr == null || !Ui.inside(mx, my, k == 0 ? this.mX : this.pX, this.tbY, this.jW, this.tbH)) continue;
            long target = Math.max(0L, mm.positionRaw() + (long)(k == 0 ? -10000 : 10000));
            if (tr.durationMs > 0L) {
                target = Math.min(target, tr.durationMs - 500L);
            }
            this.jump(target);
            UiSounds.play(UiSounds.CLICK, k == 0 ? 1.05f : 1.2f);
            return true;
        }
        if (Ui.inside(mx, my, this.iconX - 7.0f, this.vY - 7.0f, 14.0f, 14.0f)) {
            AppVolume.toggleMute();
            UiSounds.play(UiSounds.CLICK, 1.1f);
            return true;
        }
        if (this.volShown >= 0.0f && Ui.inside(mx, my, this.vX - 4.0f, this.vY - 7.0f, this.vW + 34.0f, 14.0f)) {
            this.volDrag = true;
            this.volShown = (float)Math.max(0.0, Math.min(1.0, (mx - (double)this.vX) / (double)this.vW));
            AppVolume.set(this.volShown);
            UiSounds.play(UiSounds.SLIDER, 1.0f);
            return true;
        }
        if (Ui.inside(mx, my, mid + 72.0f - 10.0f, by - 10.0f, 20.0f, 20.0f)) {
            this.btnPress[3] = 1.0f;
            UiSounds.play(UiSounds.CLICK, 1.15f);
            mm.cycleRepeat();
            return true;
        }
        float[][] b = new float[][]{{mid - 40.0f, 12.0f}, {mid, 17.0f}, {mid + 40.0f, 12.0f}};
        for (int i = 0; i < 3; ++i) {
            double dx = mx - (double)b[i][0];
            double dy = my - (double)by;
            if (!(dx * dx + dy * dy <= (double)((b[i][1] + 2.0f) * (b[i][1] + 2.0f)))) continue;
            this.btnPress[i] = 1.0f;
            UiSounds.play(UiSounds.CLICK, i == 1 ? 1.0f : 1.15f);
            if (i == 0) {
                mm.previous();
            } else if (i == 1) {
                mm.playPause();
            } else {
                mm.next();
            }
            return true;
        }
        Lyrics l = mm.lyrics();
        if (l != null && Ui.inside(mx, my, this.lx, this.ly, this.lw, this.lh) && this.lineY.length == l.lines.size() + 1) {
            for (int i = 0; i < l.lines.size(); ++i) {
                float yy = this.ly + 16.0f + this.lineY[i] - this.scroll;
                if (!(my >= (double)(yy - 2.0f)) || !(my < (double)(yy + this.lineY[i + 1] - this.lineY[i]))) continue;
                mm.seek(Math.max(0L, l.lines.get((int)i).start));
                this.lastUserScroll = -10.0;
                UiSounds.play(UiSounds.TOGGLE_ON, 1.1f);
                return true;
            }
        }
        return super.mouseClicked(e, dbl);
    }

    private void jump(long ms) {
        Track tr = MediaManager.get().track();
        if (tr == null) {
            return;
        }
        MediaManager.get().seek(ms);
        if (tr.durationMs > 0L) {
            this.seekFrac = Math.max(0.0f, Math.min(1.0f, (float)ms / (float)tr.durationMs));
            this.seekSentAt = System.currentTimeMillis();
        }
        this.lastUserScroll = -10.0;
    }

    private static long parseTime(String s) {
        if ((s = s.trim().replace('.', ':').replace(' ', ':')).isEmpty()) {
            return -1L;
        }
        try {
            String[] p = s.split(":");
            long sec = 0L;
            for (String part : p) {
                sec = sec * 60L + (part.isEmpty() ? 0L : Long.parseLong(part));
            }
            return sec * 1000L;
        }
        catch (NumberFormatException e) {
            return -1L;
        }
    }

    public boolean charTyped(CharacterEvent e) {
        if (!this.editing) {
            return super.charTyped(e);
        }
        String c = e.codepointAsString();
        if (c.length() == 1 && (Character.isDigit(c.charAt(0)) || c.charAt(0) == ':' || c.charAt(0) == '.') && this.timeText.length() < 8) {
            this.timeText = this.timeText + c;
            UiSounds.play(UiSounds.SLIDER, 1.3f);
        }
        return true;
    }

    public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        if (this.volDrag) {
            float nf = (float)Math.max(0.0, Math.min(1.0, (e.x() - (double)this.vX) / (double)this.vW));
            if (Math.abs(nf - this.volShown) > 0.01f) {
                this.volShown = nf;
                AppVolume.set(nf);
                UiSounds.play(UiSounds.SLIDER, 0.8f + nf * 0.5f);
            }
            return true;
        }
        if (this.seeking) {
            float nf = (float)Math.max(0.0, Math.min(1.0, (e.x() - (double)this.barX) / (double)this.barW));
            if (Math.abs(nf - this.seekFrac) > 0.004f) {
                UiSounds.play(UiSounds.SLIDER, 0.9f + nf * 0.4f);
            }
            this.seekFrac = nf;
            return true;
        }
        return super.mouseDragged(e, dx, dy);
    }

    public boolean mouseReleased(MouseButtonEvent e) {
        if (this.volDrag) {
            this.volDrag = false;
            return true;
        }
        if (this.seeking) {
            this.seeking = false;
            Track tr = MediaManager.get().track();
            if (tr != null && tr.durationMs > 0L) {
                MediaManager.get().seek((long)(this.seekFrac * (float)tr.durationMs));
                this.seekSentAt = System.currentTimeMillis();
                this.lastUserScroll = -10.0;
            }
            return true;
        }
        return super.mouseReleased(e);
    }

    public boolean mouseScrolled(double x, double y, double h, double v) {
        if (Anim.seconds() - this.lastUserScroll >= 3.0) {
            this.userScroll = this.scroll;
        }
        this.userScroll -= (float)v * 30.0f;
        this.lastUserScroll = Anim.seconds();
        return true;
    }

    public boolean keyPressed(KeyEvent e) {
        if (this.editing) {
            if (e.key() == 257 || e.key() == 335) {
                long ms = PlayerScreen.parseTime(this.timeText);
                Track tr = MediaManager.get().track();
                if (ms >= 0L && tr != null) {
                    if (tr.durationMs > 0L) {
                        ms = Math.min(ms, tr.durationMs - 500L);
                    }
                    this.jump(Math.max(0L, ms));
                    UiSounds.play(UiSounds.TOGGLE_ON, 1.1f);
                }
                this.editing = false;
            } else if (e.key() == 259) {
                if (!this.timeText.isEmpty()) {
                    this.timeText = this.timeText.substring(0, this.timeText.length() - 1);
                }
            } else if (e.key() == 256) {
                this.editing = false;
            }
            return true;
        }
        if (e.key() == 265 || e.key() == 264) {
            float v = AppVolume.get();
            if (v >= 0.0f) {
                AppVolume.set(v + (e.key() == 265 ? 0.05f : -0.05f));
                UiSounds.play(UiSounds.SLIDER, 1.0f);
            }
            return true;
        }
        if (e.key() == 256 || Keys.openPlayer != null && Keys.openPlayer.matches(e)) {
            this.onClose();
            return true;
        }
        if (e.key() == 32) {
            MediaManager.get().playPause();
            UiSounds.play(UiSounds.CLICK);
            return true;
        }
        if (e.key() == 262) {
            MediaManager.get().next();
            return true;
        }
        if (e.key() == 263) {
            MediaManager.get().previous();
            return true;
        }
        return super.keyPressed(e);
    }

    public void onClose() {
        if (this.closing) {
            return;
        }
        this.closing = true;
        UiSounds.play(UiSounds.CLOSE, 0.95f);
    }

    public void removed() {
        super.removed();
    }

    private void finish() {
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.parent);
        }
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }
}

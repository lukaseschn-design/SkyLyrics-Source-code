package dev.lyricsflow.gui;

import dev.lyricsflow.Keys;
import dev.lyricsflow.app.Core;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.gui.Icons;
import dev.lyricsflow.gui.NowPlaying;
import dev.lyricsflow.gui.Pages;
import dev.lyricsflow.gui.Ui;
import dev.lyricsflow.gui.UiSounds;
import dev.lyricsflow.render.Gfx;
import dev.lyricsflow.render.Themes;
import dev.lyricsflow.util.Anim;
import dev.lyricsflow.util.Col;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

public class LyricsFlowScreen
extends class_437 {
    private static Tab lastTab = Tab.LYRICS;
    private Tab tab = lastTab;
    private final List<Ui.Widget> widgets = new ArrayList<Ui.Widget>();
    private float openT;
    private boolean closing;
    private final long openedAt = System.currentTimeMillis();
    private long closedAt = -1L;
    private float tabT = 1.0f;
    private float railY = -1.0f;
    private final float[] railHover = new float[Tab.values().length];
    private float scroll;
    private float scrollTarget;
    private float contentH;
    private float barX;
    private float barY;
    private float barH;
    private float barMax;
    private float barHover;
    private boolean barDrag;
    private float barGrab;
    private float closeHover;
    private Ui.Widget dragging;
    private boolean rebuild = true;
    private boolean heroVisible;
    private float px;
    private float py;
    private float pw;
    private float ph;
    private float hx;
    private float hy;
    private float hw;
    private float hh;
    private float railW;
    private float contentX;
    private float contentY;
    private float contentW;
    private float contentHgt;
    private final class_437 parent;
    private float fit = 1.0f;
    private double lastMx;
    private double lastMy;
    private final float[] modeHover = new float[2];
    private float modePill = -1.0f;

    public LyricsFlowScreen() {
        this(null);
    }

    public LyricsFlowScreen(class_437 parent) {
        super((class_2561)class_2561.method_43470((String)"SkyLyrics"));
        this.parent = parent;
    }

    public void rebuildSoon() {
        this.rebuild = true;
    }

    protected void method_25426() {
        UiSounds.play(UiSounds.OPEN, 1.0f);
        this.rebuild = true;
    }

    public boolean method_25421() {
        return false;
    }

    private static float easeOutQuint(float x) {
        float u = 1.0f - x;
        return 1.0f - u * u * u * u * u;
    }

    private void computeFit() {
        float f = Math.min((float)this.field_22789 / 812.0f, (float)this.field_22790 / 424.0f);
        this.fit = Math.max(0.75f, Math.min(1.5f, f * 0.98f));
    }

    private void layout() {
        float W = (float)this.field_22789 / this.fit;
        float H = (float)this.field_22790 / this.fit;
        this.heroVisible = W >= 640.0f;
        this.hw = this.heroVisible ? Math.min(158.0f, W * 0.22f) : 0.0f;
        float gap = this.heroVisible ? 10.0f : 0.0f;
        this.pw = Math.min(520.0f, W - 24.0f - this.hw - gap);
        this.ph = Math.min(350.0f, H - 24.0f);
        float total = this.hw + gap + this.pw;
        this.hx = (W - total) / 2.0f;
        this.px = this.hx + this.hw + gap;
        this.py = (H - this.ph) / 2.0f;
        this.hh = Math.min(this.ph, NowPlaying.height(this.hw));
        this.hy = this.py;
        this.railW = 66.0f;
        this.contentX = this.px + this.railW + 14.0f;
        this.contentY = this.py + 46.0f;
        this.contentW = this.pw - this.railW - 28.0f;
        this.contentHgt = this.ph - 46.0f - 10.0f;
    }

    private void build() {
        this.widgets.clear();
        this.widgets.addAll(Pages.build(this.tab, this, this.heroVisible));
        float y = 0.0f;
        for (Ui.Widget w : this.widgets) {
            y += w.layout(0.0f, y, this.contentW);
        }
        this.contentH = y;
        this.rebuild = false;
    }

    private void switchTab(Tab t) {
        if (t == this.tab) {
            return;
        }
        this.tab = t;
        lastTab = t;
        this.tabT = 0.0f;
        this.scrollTarget = 0.0f;
        this.scroll = 0.0f;
        this.rebuild = true;
        UiSounds.play(UiSounds.TAB, 0.95f + (float)t.ordinal() * 0.03f);
    }

    public void method_25420(class_332 g, int mouseX, int mouseY, float partialTick) {
        if (this.parent != null || this.field_22787 == null || this.field_22787.field_1687 == null) {
            super.method_25420(g, mouseX, mouseY, partialTick);
        }
    }

    public void method_25394(class_332 g, int mouseX, int mouseY, float partialTick) {
        float ck;
        Anim.frame();
        String oldF = Ui.F;
        Ui.syncFont();
        if (!oldF.equals(Ui.F)) {
            this.rebuild = true;
        }
        float oldFit = this.fit;
        this.computeFit();
        if (Math.abs(oldFit - this.fit) > 0.001f) {
            this.rebuild = true;
        }
        this.layout();
        if (this.rebuild) {
            this.build();
        }
        long nowMs = System.currentTimeMillis();
        float t = (float)(nowMs - this.openedAt) / 1000.0f;
        if (this.closing && this.closedAt < 0L) {
            this.closedAt = nowMs;
        }
        this.openT = ck = this.closing ? 1.0f - Anim.easeOutCubic(Math.min(1.0f, (float)(nowMs - this.closedAt) / 160.0f)) : 1.0f;
        if (this.closing && ck < 0.02f) {
            if (this.parent != null && this.field_22787 != null) {
                this.field_22787.method_1507(this.parent);
            } else {
                super.method_25419();
            }
            return;
        }
        this.tabT = Anim.approach(this.tabT, 1.0f, 10.0f);
        float bgA = Anim.easeOutCubic(Math.min(1.0f, t / 0.25f)) * ck;
        float eh = LyricsFlowScreen.easeOutQuint(Math.max(0.0f, Math.min(1.0f, (t - 0.02f) / 0.55f)));
        float ep = LyricsFlowScreen.easeOutQuint(Math.max(0.0f, Math.min(1.0f, (t - 0.1f) / 0.6f)));
        float out = 1.0f - ck;
        float a = Math.min(1.0f, Math.max(0.0f, t / 0.2f)) * ck;
        float aH = Math.min(1.0f, eh * 1.6f) * ck;
        float aP = Math.min(1.0f, ep * 1.6f) * ck;
        Gfx.gradientV(g, 0.0f, 0.0f, this.field_22789, this.field_22790, Col.alpha(-1341585646, bgA), Col.alpha(-804714734, bgA));
        float fl = Math.max(0.0f, 1.0f - Math.abs(t - 0.1f) / 0.1f);
        if (fl > 0.01f) {
            Gfx.rect(g, 0.0f, 0.0f, this.field_22789, this.field_22790, Col.alpha(-1, 0.06f * fl));
        }
        g.method_51448().pushMatrix();
        g.method_51448().scale(this.fit, this.fit);
        float cx = (float)this.field_22789 / this.fit / 2.0f;
        float cy = (float)this.field_22790 / this.fit / 2.0f;
        float sc = 1.0f - 0.03f * out;
        g.method_51448().translate(cx, cy + out * 8.0f);
        g.method_51448().scale(sc, sc);
        g.method_51448().translate(-cx, -cy);
        double vmx = (float)mouseX / this.fit;
        double vmy = (float)mouseY / this.fit;
        double mx = (vmx - (double)cx) / (double)sc + (double)cx;
        double my = (vmy - (double)cy - (double)(out * 8.0f)) / (double)sc + (double)cy;
        this.lastMx = mx;
        this.lastMy = my;
        if (this.heroVisible) {
            float hs = 0.96f + 0.04f * eh;
            g.method_51448().pushMatrix();
            g.method_51448().translate(this.hx + this.hw / 2.0f - (1.0f - eh) * 46.0f, this.hy + this.hh / 2.0f);
            g.method_51448().scale(hs, hs);
            g.method_51448().translate(-(this.hx + this.hw / 2.0f), -(this.hy + this.hh / 2.0f));
            NowPlaying.renderHero(g, this.hx, this.hy, this.hw, this.hh, mx, my, aH);
            g.method_51448().popMatrix();
        }
        float ps = 0.96f + 0.04f * ep;
        g.method_51448().pushMatrix();
        g.method_51448().translate(this.px + this.pw / 2.0f + (1.0f - ep) * 46.0f, this.py + this.ph / 2.0f);
        g.method_51448().scale(ps, ps);
        g.method_51448().translate(-(this.px + this.pw / 2.0f), -(this.py + this.ph / 2.0f));
        a = aP;
        this.renderPanel(g, mx, my, a);
        g.method_51448().popMatrix();
        g.method_51448().popMatrix();
    }

    private void renderPanel(class_332 g, double mx, double my, float a) {
        int accent = Themes.accent();
        Gfx.shadow(g, this.px, this.py, this.pw, this.ph, 16.0f, 14.0f, Col.alpha(-16777216, a * 0.55f));
        Gfx.round(g, this.px, this.py, this.pw, this.ph, 16.0f, Col.alpha(-233697255, a));
        Gfx.roundOutline(g, this.px, this.py, this.pw, this.ph, 16.0f, 0.6f, Col.alpha(0x22FFFFFF, a));
        Tab[] tabs = Tab.values();
        float tabH = Math.min(36.0f, (this.ph - 16.0f) / (float)tabs.length);
        boolean labels = tabH >= 30.0f;
        float ry0 = this.py + (this.ph - tabH * (float)tabs.length) / 2.0f;
        float target = ry0 + (float)this.tab.ordinal() * tabH;
        if (this.railY < 0.0f) {
            this.railY = target;
        }
        this.railY = Anim.approach(this.railY, target, 14.0f);
        Gfx.rect(g, this.px + this.railW + 2.0f, this.py + 12.0f, 0.5f, this.ph - 24.0f, Col.alpha(0x14FFFFFF, a));
        float pillX = this.px + 7.0f;
        float pillW = this.railW - 10.0f;
        Gfx.glow(g, pillX, this.railY + 2.0f, pillW, tabH - 4.0f, 9.0f, 4.0f, Col.alpha(accent, a * 0.25f));
        Gfx.round(g, pillX, this.railY + 2.0f, pillW, tabH - 4.0f, 9.0f, Col.alpha(-724481, a));
        for (int i = 0; i < tabs.length; ++i) {
            Tab t = tabs[i];
            float ty = ry0 + (float)i * tabH;
            boolean over = Ui.inside(mx, my, pillX, ty + 2.0f, pillW, tabH - 4.0f);
            if (over && this.railHover[i] < 0.05f && t != this.tab) {
                UiSounds.play(UiSounds.HOVER, 1.3f);
            }
            this.railHover[i] = Anim.approach(this.railHover[i], over ? 1.0f : 0.0f, 14.0f);
            float sel = 1.0f - Math.min(1.0f, Math.abs(this.railY - ty) / tabH);
            if (this.railHover[i] > 0.01f && t != this.tab) {
                Gfx.round(g, pillX, ty + 2.0f, pillW, tabH - 4.0f, 9.0f, Col.alpha(0x10FFFFFF, a * this.railHover[i]));
            }
            int col = Col.lerp(Col.lerp(-6515020, -856070, this.railHover[i]), -15330529, sel);
            float lift = this.railHover[i] * -0.8f;
            if (labels) {
                Icons.draw(g, t.icon, pillX + pillW / 2.0f, ty + tabH * 0.38f + lift, 10.5f, Col.alpha(col, a));
                Gfx.textCentered(g, Gfx.ellipsize(t.title(), Ui.FB, 6.4f, pillW - 4.0f), Ui.FB, pillX + pillW / 2.0f, ty + tabH * 0.62f + lift, 6.4f, Col.alpha(col, a));
                continue;
            }
            Icons.draw(g, t.icon, pillX + pillW / 2.0f, ty + tabH / 2.0f + lift, 11.0f, Col.alpha(col, a));
        }
        float ta = a * Anim.easeOutCubic(this.tabT);
        float tOff = (1.0f - Anim.easeOutCubic(this.tabT)) * 8.0f;
        Gfx.text(g, this.tab.title(), Ui.FB, this.contentX + 2.0f + tOff, this.py + 14.0f, 14.5f, Col.alpha(-856070, ta));
        Gfx.text(g, this.tab.subtitle(), Ui.F, this.contentX + 2.0f + tOff, this.py + 31.0f, 7.2f, Col.alpha(-6515020, ta));
        float cr = 8.5f;
        float ccx = this.px + this.pw - 18.0f;
        float ccy = this.py + 18.0f;
        boolean overClose = Ui.inside(mx, my, ccx - cr, ccy - cr, cr * 2.0f, cr * 2.0f);
        this.closeHover = Anim.approach(this.closeHover, overClose ? 1.0f : 0.0f, 14.0f);
        Gfx.circle(g, ccx, ccy, cr + this.closeHover * 1.2f, Col.alpha(Col.lerp(-14541264, -42374, this.closeHover * 0.85f), a));
        g.method_51448().pushMatrix();
        g.method_51448().translate(ccx, ccy);
        g.method_51448().rotate(this.closeHover * (float)Math.PI / 2.0f);
        Icons.draw(g, "close", 0.0f, 0.0f, 8.0f, Col.alpha(-1, a));
        g.method_51448().popMatrix();
        this.drawModeSwitch(g, mx, my, a);
        float maxScroll = Math.max(0.0f, this.contentH - this.contentHgt);
        this.scrollTarget = Math.max(0.0f, Math.min(maxScroll, this.scrollTarget));
        this.scroll = Anim.approach(this.scroll, this.scrollTarget, 16.0f);
        g.method_44379((int)this.contentX - 10, (int)this.contentY, (int)Math.ceil(this.contentX + this.contentW + 10.0f), (int)Math.ceil(this.contentY + this.contentHgt));
        float ox = this.contentX;
        float oy = this.contentY - this.scroll + tOff * 1.4f;
        for (Ui.Widget w : this.widgets) {
            if (oy + w.y + w.h < this.contentY - 20.0f || oy + w.y > this.contentY + this.contentHgt + 20.0f) continue;
            boolean inView = my >= (double)this.contentY && my <= (double)(this.contentY + this.contentHgt);
            w.render(g, ox, oy, inView ? mx : -9999.0, inView ? my : -9999.0, ta);
        }
        g.method_44380();
        if (this.scroll > 1.0f) {
            Gfx.gradientV(g, this.contentX - 10.0f, this.contentY, this.contentW + 20.0f, 10.0f, Col.alpha(-233697255, a), Col.alpha(-233697255, 0.0f));
        }
        if (this.scroll < maxScroll - 1.0f) {
            Gfx.gradientV(g, this.contentX - 10.0f, this.contentY + this.contentHgt - 10.0f, this.contentW + 20.0f, 10.0f, Col.alpha(-233697255, 0.0f), Col.alpha(-233697255, a));
        }
        this.barMax = maxScroll;
        if (maxScroll > 0.0f) {
            this.barH = Math.max(18.0f, this.contentHgt * this.contentHgt / this.contentH);
            this.barY = this.contentY + (this.contentHgt - this.barH) * (this.scroll / maxScroll);
            this.barX = this.px + this.pw - 6.0f;
            boolean over = this.barDrag || Ui.inside(mx, my, this.barX - 6.0f, this.barY, 12.0f, this.barH);
            this.barHover = Anim.approach(this.barHover, over ? 1.0f : 0.0f, 14.0f);
            float bw = 2.5f + this.barHover * 1.5f;
            Gfx.round(g, this.barX + 1.25f - bw / 2.0f, this.barY, bw, this.barH, bw / 2.0f, Col.alpha(Col.lerp(0x40FFFFFF, -1862270977, this.barHover), a));
        }
        Gfx.text(g, "v1.0", Ui.F, this.px + 10.0f, this.py + this.ph - 10.0f, 5.6f, Col.alpha(-9541498, a));
    }

    private float[] modeBox() {
        float h = 14.0f;
        float w = 0.0f;
        String[] l = LyricsFlowScreen.modeLabels();
        float[] ws = new float[2];
        for (int i = 0; i < 2; ++i) {
            ws[i] = Gfx.textWidth(l[i], Ui.FB, 6.6f) + 14.0f;
            w += ws[i];
        }
        float x = this.px + this.pw - 36.0f - w;
        float y = this.py + 11.0f;
        return new float[]{x, y, w, h, ws[0], ws[1]};
    }

    private static String[] modeLabels() {
        return new String[]{Ui.tr("lyricsflow.ui.menu.normal", new Object[0]), Ui.tr("lyricsflow.ui.menu.world", new Object[0])};
    }

    private void drawModeSwitch(class_332 g, double mx, double my, float a) {
        float target;
        float[] b = this.modeBox();
        int sel = Core.cfg().menuMode == 1 ? 1 : 0;
        Gfx.round(g, b[0], b[1], b[2], b[3], b[3] / 2.0f, Col.alpha(-14541264, a));
        float f = target = sel == 0 ? b[0] : b[0] + b[4];
        if (this.modePill < 0.0f) {
            this.modePill = target;
        }
        this.modePill = Anim.approach(this.modePill, target, 16.0f);
        float pwid = sel == 0 ? b[4] : b[5];
        Gfx.round(g, this.modePill + 1.0f, b[1] + 1.0f, pwid - 2.0f, b[3] - 2.0f, (b[3] - 2.0f) / 2.0f, Col.alpha(-724481, a));
        String[] l = LyricsFlowScreen.modeLabels();
        float x = b[0];
        for (int i = 0; i < 2; ++i) {
            boolean over = Ui.inside(mx, my, x, b[1], b[4 + i], b[3]);
            this.modeHover[i] = Anim.approach(this.modeHover[i], over ? 1.0f : 0.0f, 14.0f);
            int col = i == sel ? -15330529 : Col.lerp(-6515020, -856070, this.modeHover[i]);
            Gfx.textCentered(g, l[i], Ui.FB, x + b[4 + i] / 2.0f, b[1] + 3.6f, 6.6f, Col.alpha(col, a));
            x += b[4 + i];
        }
    }

    private boolean clickModeSwitch(double mx, double my) {
        int i;
        float[] b = this.modeBox();
        if (!Ui.inside(mx, my, b[0], b[1], b[2], b[3])) {
            return false;
        }
        Core.cfg().menuMode = i = mx < (double)(b[0] + b[4]) ? 0 : 1;
        Core.saveConfig();
        UiSounds.play(UiSounds.CLICK);
        this.rebuild = true;
        return true;
    }

    private float ox() {
        return this.contentX;
    }

    private float oy() {
        return this.contentY - this.scroll;
    }

    public boolean method_25402(class_11909 e, boolean doubleClick) {
        if (this.closing) {
            return true;
        }
        double mx = this.lastMx;
        double my = this.lastMy;
        if (e.method_74245() != 0) {
            return super.method_25402(e, doubleClick);
        }
        if (Ui.inside(mx, my, this.px + this.pw - 27.0f, this.py + 9.0f, 18.0f, 18.0f)) {
            this.method_25419();
            return true;
        }
        if (this.clickModeSwitch(mx, my)) {
            return true;
        }
        if (this.heroVisible && Ui.inside(mx, my, this.hx, this.hy, this.hw, this.hh)) {
            if (!NowPlaying.pressBar(mx, my)) {
                NowPlaying.click(mx, my);
            }
            return true;
        }
        Tab[] tabs = Tab.values();
        float tabH = Math.min(36.0f, (this.ph - 16.0f) / (float)tabs.length);
        float ry0 = this.py + (this.ph - tabH * (float)tabs.length) / 2.0f;
        for (int i = 0; i < tabs.length; ++i) {
            if (!Ui.inside(mx, my, this.px + 7.0f, ry0 + (float)i * tabH, this.railW - 10.0f, tabH)) continue;
            this.switchTab(tabs[i]);
            return true;
        }
        if (this.barMax > 0.0f && Ui.inside(mx, my, this.barX - 6.0f, this.barY, 12.0f, this.barH)) {
            this.barGrab = (float)(my - (double)this.barY);
            this.barDrag = true;
            return true;
        }
        if (Ui.inside(mx, my, this.contentX - 10.0f, this.contentY, this.contentW + 20.0f, this.contentHgt)) {
            for (Ui.Widget w : this.widgets) {
                if (!w.click(mx, my, this.ox(), this.oy())) continue;
                this.dragging = w;
                return true;
            }
        }
        return super.method_25402(e, doubleClick);
    }

    public boolean method_25403(class_11909 e, double dx, double dy) {
        if (this.barDrag) {
            this.dragBar(this.lastMy);
            return true;
        }
        if (NowPlaying.dragging()) {
            NowPlaying.dragBar(this.lastMx);
            return true;
        }
        if (this.dragging != null) {
            this.dragging.drag(this.lastMx, this.lastMy, this.ox(), this.oy());
        }
        return true;
    }

    public void method_16014(double x, double y) {
        if (this.barDrag) {
            this.dragBar(this.lastMy);
            return;
        }
        if (NowPlaying.dragging()) {
            NowPlaying.dragBar(this.lastMx);
        }
        if (this.dragging != null) {
            this.dragging.drag(this.lastMx, this.lastMy, this.ox(), this.oy());
        }
    }

    public boolean method_25406(class_11909 e) {
        this.barDrag = false;
        NowPlaying.releaseBar();
        if (this.dragging != null) {
            this.dragging.release();
            this.dragging = null;
        }
        return true;
    }

    private void dragBar(double my) {
        float track = this.contentHgt - this.barH;
        if (track <= 0.0f) {
            return;
        }
        float k = (float)Math.max(0.0, Math.min(1.0, (my - (double)this.barGrab - (double)this.contentY) / (double)track));
        this.scroll = this.scrollTarget = k * this.barMax;
    }

    public boolean method_25401(double x, double y, double h, double v) {
        this.scrollTarget -= (float)v * 28.0f;
        return true;
    }

    private boolean anyCapturing() {
        for (Ui.Widget w : this.widgets) {
            if (!w.capturesKeys()) continue;
            return true;
        }
        return false;
    }

    public boolean method_25404(class_11908 e) {
        for (Ui.Widget w : this.widgets) {
            if (!w.capturesKeys() || !w.key(e)) continue;
            return true;
        }
        if (!this.anyCapturing() && Keys.openMenu != null && Keys.openMenu.method_1417(e)) {
            this.method_25419();
            return true;
        }
        if (e.comp_4795() == 256) {
            this.method_25419();
            return true;
        }
        if (e.comp_4795() == 264 || e.comp_4795() == 265) {
            int d = e.comp_4795() == 264 ? 1 : -1;
            Tab[] t = Tab.values();
            this.switchTab(t[(this.tab.ordinal() + d + t.length) % t.length]);
            return true;
        }
        return super.method_25404(e);
    }

    public boolean method_25400(class_11905 e) {
        for (Ui.Widget w : this.widgets) {
            if (!w.chr(e)) continue;
            return true;
        }
        return super.method_25400(e);
    }

    public void method_25419() {
        if (this.closing) {
            return;
        }
        this.closing = true;
        ConfigManager.save();
        UiSounds.play(UiSounds.CLOSE, 1.0f);
    }

    public boolean method_25422() {
        return false;
    }

    public static enum Tab {
        NOW("now", "music"),
        LYRICS("lyrics", "lyrics"),
        STYLE("style", "style"),
        SKY("sky", "sky"),
        COSMETICS("cosmetics", "cosmetic"),
        DISPLAY("display", "display"),
        PERF("perf", "perf"),
        EXTRA("extra", "move"),
        KEYS("keys", "keys"),
        SAVED("saved", "saved");

        final String id;
        final String icon;

        private Tab(String id, String icon) {
            this.id = id;
            this.icon = icon;
        }

        String title() {
            return Ui.tr("lyricsflow.tab." + this.id, new Object[0]);
        }

        String subtitle() {
            return Ui.tr("lyricsflow.tab." + this.id + ".desc", new Object[0]);
        }
    }
}


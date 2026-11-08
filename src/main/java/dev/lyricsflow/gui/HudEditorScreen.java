package dev.lyricsflow.gui;

import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.config.LFConfig;
import dev.lyricsflow.gui.LyricsFlowScreen;
import dev.lyricsflow.gui.Ui;
import dev.lyricsflow.gui.UiSounds;
import dev.lyricsflow.render.ExtraHud;
import dev.lyricsflow.render.Gfx;
import dev.lyricsflow.render.Themes;
import dev.lyricsflow.render.TopBar;
import dev.lyricsflow.util.Anim;
import dev.lyricsflow.util.Col;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

public class HudEditorScreen
extends class_437 {
    private final class_437 parent;
    private int dragging;
    private double grabDx;
    private double grabDy;
    private float barHover;
    private float lyrHover;
    private float doneHover;
    private float resetHover;
    private float guideX;
    private float guideY;
    private float openT;

    public HudEditorScreen(class_437 parent) {
        super((class_2561)class_2561.method_43471((String)"lyricsflow.editor.title"));
        this.parent = parent;
    }

    protected void method_25426() {
        UiSounds.play(UiSounds.OPEN, 1.15f);
    }

    public boolean method_25421() {
        return false;
    }

    public void method_25420(class_332 g, int mouseX, int mouseY, float partialTick) {
    }

    private float lyrW() {
        return 150.0f * ConfigManager.get().lyricsScale;
    }

    private float lyrH() {
        return 34.0f * ConfigManager.get().lyricsScale;
    }

    public void method_25394(class_332 g, int mx, int my, float pt) {
        Anim.frame();
        this.openT = Anim.approach(this.openT, 1.0f, 8.0f);
        float a = Anim.easeOutCubic(this.openT);
        LFConfig c = ConfigManager.get();
        Gfx.rect(g, 0.0f, 0.0f, this.field_22789, this.field_22790, Col.alpha(0x70000000, a));
        for (int x = 0; x < this.field_22789; x += 20) {
            Gfx.rect(g, x, 0.0f, 0.5f, this.field_22790, Col.alpha(0xCFFFFFF, a));
        }
        for (int y = 0; y < this.field_22790; y += 20) {
            Gfx.rect(g, 0.0f, y, this.field_22789, 0.5f, Col.alpha(0xCFFFFFF, a));
        }
        this.guideX = Anim.approach(this.guideX, this.dragging != 0 && Math.abs(this.centerOf(this.dragging)[0] - (float)this.field_22789 / 2.0f) < 1.0f ? 1.0f : 0.0f, 12.0f);
        this.guideY = Anim.approach(this.guideY, this.dragging != 0 && Math.abs(this.centerOf(this.dragging)[1] - (float)this.field_22790 / 2.0f) < 1.0f ? 1.0f : 0.0f, 12.0f);
        if (this.guideX > 0.01f) {
            Gfx.rect(g, (float)this.field_22789 / 2.0f - 0.5f, 0.0f, 1.0f, this.field_22790, Col.alpha(Themes.accent(), a * this.guideX));
        }
        if (this.guideY > 0.01f) {
            Gfx.rect(g, 0.0f, (float)this.field_22790 / 2.0f - 0.5f, this.field_22789, 1.0f, Col.alpha(Themes.accent(), a * this.guideY));
        }
        ExtraHud.renderPreview(g, mx, my);
        TopBar.render(g, true);
        boolean overBar = Ui.inside(mx, my, TopBar.lastX, TopBar.lastY, TopBar.lastW, TopBar.lastH);
        this.barHover = Anim.approach(this.barHover, overBar || this.dragging == 1 ? 1.0f : 0.0f, 14.0f);
        Gfx.roundOutline(g, TopBar.lastX - 3.0f, TopBar.lastY - 3.0f, TopBar.lastW + 6.0f, TopBar.lastH + 6.0f, TopBar.lastH / 2.0f + 3.0f, 1.0f, Col.alpha(Col.lerp(0x60FFFFFF, Themes.accent(), this.barHover), a));
        this.label(g, Ui.tr("lyricsflow.editor.top_bar", new Object[0]), TopBar.lastX + TopBar.lastW / 2.0f, TopBar.lastY + TopBar.lastH + 6.0f, a * (0.6f + 0.4f * this.barHover));
        Themes.Theme th = Themes.current();
        float lx = c.screenLyricsX * (float)this.field_22789;
        float ly = c.screenLyricsY * (float)this.field_22790;
        float lw = this.lyrW();
        float lh = this.lyrH();
        boolean overLyr = Ui.inside(mx, my, lx - lw / 2.0f, ly - lh / 2.0f, lw, lh);
        this.lyrHover = Anim.approach(this.lyrHover, overLyr || this.dragging == 2 ? 1.0f : 0.0f, 14.0f);
        Gfx.round(g, lx - lw / 2.0f, ly - lh / 2.0f, lw, lh, 10.0f, Col.alpha(0x30000000, a));
        Gfx.roundOutline(g, lx - lw / 2.0f, ly - lh / 2.0f, lw, lh, 10.0f, 1.0f, Col.alpha(Col.lerp(0x60FFFFFF, Themes.accent(), this.lyrHover), a));
        float size = 18.0f * c.lyricsScale;
        Gfx.neon(g, Ui.tr("lyricsflow.editor.sample", new Object[0]), c.font, lx, ly - size / 2.0f, size, th.main(), th.a(), th.b(), a, c.twoColor, 0.6f);
        this.label(g, Ui.tr("lyricsflow.editor.screen_lyrics", new Object[0]), lx, ly + lh / 2.0f + 5.0f, a * (0.6f + 0.4f * this.lyrHover));
        Gfx.textCentered(g, Ui.tr("lyricsflow.editor.help", new Object[0]), Ui.F, (float)this.field_22789 / 2.0f, this.field_22790 - 46, 7.2f, Col.alpha(-856070, a));
        float bw = 70.0f;
        float bh = 18.0f;
        float by = this.field_22790 - 30;
        this.doneHover = this.button(g, (float)this.field_22789 / 2.0f + 4.0f, by, bw, bh, Ui.tr("lyricsflow.button.done", new Object[0]), true, mx, my, this.doneHover, a);
        this.resetHover = this.button(g, (float)this.field_22789 / 2.0f - 4.0f - bw, by, bw, bh, Ui.tr("lyricsflow.button.reset", new Object[0]), false, mx, my, this.resetHover, a);
    }

    private float[] centerOf(int which) {
        LFConfig c = ConfigManager.get();
        if (which == 1) {
            return new float[]{TopBar.lastX + TopBar.lastW / 2.0f, TopBar.lastY + TopBar.lastH / 2.0f};
        }
        return new float[]{c.screenLyricsX * (float)this.field_22789, c.screenLyricsY * (float)this.field_22790};
    }

    private void label(class_332 g, String s, float cx, float y, float a) {
        float w = Gfx.textWidth(s, Ui.FB, 6.4f) + 10.0f;
        Gfx.round(g, cx - w / 2.0f, y, w, 11.0f, 5.5f, Col.alpha(-1072295135, a));
        Gfx.textCentered(g, s, Ui.FB, cx, y + 2.6f, 6.4f, Col.alpha(-856070, a));
    }

    private float button(class_332 g, float x, float y, float w, float h, String text, boolean primary, double mx, double my, float hov, float a) {
        boolean over = Ui.inside(mx, my, x, y, w, h);
        hov = Anim.approach(hov, over ? 1.0f : 0.0f, 14.0f);
        int base = primary ? Themes.accent() : -13883584;
        Gfx.round(g, x, y, w, h, h / 2.0f, Col.alpha(Col.brighten(base, hov * 0.12f), a));
        Gfx.textCentered(g, text, Ui.FB, x + w / 2.0f, y + (h - 7.6f) / 2.0f - 0.3f, 7.6f, Col.alpha(-1, a));
        return hov;
    }

    public boolean method_25402(class_11909 e, boolean dbl) {
        double mx = e.comp_4798();
        double my = e.comp_4799();
        LFConfig c = ConfigManager.get();
        float by = this.field_22790 - 30;
        float bw = 70.0f;
        float bh = 18.0f;
        if (Ui.inside(mx, my, (float)this.field_22789 / 2.0f + 4.0f, by, bw, bh)) {
            this.method_25419();
            return true;
        }
        if (Ui.inside(mx, my, (float)this.field_22789 / 2.0f - 4.0f - bw, by, bw, bh)) {
            LFConfig d = new LFConfig();
            c.topBarX = d.topBarX;
            c.topBarY = d.topBarY;
            c.screenLyricsX = d.screenLyricsX;
            c.screenLyricsY = d.screenLyricsY;
            c.topBarScale = d.topBarScale;
            ExtraHud.resetPositions();
            ConfigManager.save();
            UiSounds.play(UiSounds.CLICK, 0.9f);
            return true;
        }
        if (ExtraHud.press(mx, my, e.method_74245())) {
            return true;
        }
        if (Ui.inside(mx, my, TopBar.lastX, TopBar.lastY, TopBar.lastW, TopBar.lastH)) {
            this.dragging = 1;
            this.grabDx = mx - (double)(c.topBarX * (float)this.field_22789);
            this.grabDy = my - (double)(c.topBarY * (float)this.field_22790);
            UiSounds.play(UiSounds.CLICK, 1.2f);
            return true;
        }
        float lx = c.screenLyricsX * (float)this.field_22789;
        float ly = c.screenLyricsY * (float)this.field_22790;
        if (Ui.inside(mx, my, lx - this.lyrW() / 2.0f, ly - this.lyrH() / 2.0f, this.lyrW(), this.lyrH())) {
            this.dragging = 2;
            this.grabDx = mx - (double)lx;
            this.grabDy = my - (double)ly;
            UiSounds.play(UiSounds.CLICK, 1.2f);
            return true;
        }
        return super.method_25402(e, dbl);
    }

    public boolean method_25403(class_11909 e, double dx, double dy) {
        if (ExtraHud.drag(e.comp_4798(), e.comp_4799())) {
            return true;
        }
        LFConfig c = ConfigManager.get();
        double nx = e.comp_4798() - this.grabDx;
        double ny = e.comp_4799() - this.grabDy;
        if (this.dragging == 1) {
            if (Math.abs(nx - (double)this.field_22789 / 2.0) < 6.0) {
                nx = (double)this.field_22789 / 2.0;
            }
            c.topBarX = (float)Math.max(0.0, Math.min(1.0, nx / (double)this.field_22789));
            c.topBarY = (float)Math.max(0.0, Math.min(0.95, ny / (double)this.field_22790));
        } else if (this.dragging == 2) {
            if (Math.abs(nx - (double)this.field_22789 / 2.0) < 6.0) {
                nx = (double)this.field_22789 / 2.0;
            }
            if (Math.abs(ny - (double)this.field_22790 / 2.0) < 6.0) {
                ny = (double)this.field_22790 / 2.0;
            }
            c.screenLyricsX = (float)Math.max(0.05, Math.min(0.95, nx / (double)this.field_22789));
            c.screenLyricsY = (float)Math.max(0.05, Math.min(0.95, ny / (double)this.field_22790));
        }
        return true;
    }

    public boolean method_25406(class_11909 e) {
        if (ExtraHud.release()) {
            return true;
        }
        if (this.dragging != 0) {
            this.dragging = 0;
            ConfigManager.save();
            UiSounds.play(UiSounds.TOGGLE_ON, 1.0f);
        }
        return true;
    }

    public boolean method_25401(double mx, double my, double h, double v) {
        if (ExtraHud.scroll(mx, my, v)) {
            UiSounds.play(UiSounds.SLIDER, 1.0f);
            return true;
        }
        LFConfig c = ConfigManager.get();
        if (Ui.inside(mx, my, TopBar.lastX, TopBar.lastY, TopBar.lastW, TopBar.lastH)) {
            c.topBarScale = (float)Math.max(0.6, Math.min(1.8, (double)c.topBarScale + v * 0.05));
        } else {
            c.lyricsScale = (float)Math.max(0.4, Math.min(2.5, (double)c.lyricsScale + v * 0.05));
        }
        UiSounds.play(UiSounds.SLIDER, 1.0f);
        ConfigManager.save();
        return true;
    }

    public boolean method_25404(class_11908 e) {
        if (e.comp_4795() == 256 || e.comp_4795() == 257) {
            this.method_25419();
            return true;
        }
        return super.method_25404(e);
    }

    public void method_25419() {
        ConfigManager.save();
        UiSounds.play(UiSounds.CLOSE, 1.1f);
        this.field_22787.method_1507((class_437)(this.parent instanceof LyricsFlowScreen ? new LyricsFlowScreen() : this.parent));
    }
}


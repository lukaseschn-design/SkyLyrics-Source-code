package dev.lyricsflow.gui;

import dev.lyricsflow.LyricsFlow;
import dev.lyricsflow.app.Core;
import dev.lyricsflow.app.hud.SkyThumbs;
import dev.lyricsflow.app.sky.SkyPreset;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.gui.Icons;
import dev.lyricsflow.gui.UiSounds;
import dev.lyricsflow.render.Gfx;
import dev.lyricsflow.render.Themes;
import dev.lyricsflow.util.Anim;
import dev.lyricsflow.util.Col;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import net.minecraft.class_1074;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3675;

public final class Ui {
    public static final int BG = -233697255;
    public static final int CARD = -15001562;
    public static final int CARD_HOVER = -14541264;
    public static final int LINE = 0x14FFFFFF;
    public static final int TEXT = -856070;
    public static final int TEXT2 = -6515020;
    public static final int TEXT3 = -9541498;
    public static final int OFF = -12962480;
    public static String F = "outfit";
    public static String FB = "outfit_bold";

    public static void syncFont() {
        F = "outfit";
        FB = "outfit_bold";
    }

    private Ui() {
    }

    public static String tr(String key, Object ... args) {
        return class_1074.method_4662((String)key, (Object[])args);
    }

    public static boolean inside(double mx, double my, float x, float y, float w, float h) {
        return mx >= (double)x && my >= (double)y && mx < (double)(x + w) && my < (double)(y + h);
    }

    static void save() {
        ConfigManager.save();
        Core.saveConfig();
    }

    public static List<String> wrap(String s, String font, float size, float maxW) {
        ArrayList<String> out = new ArrayList<String>();
        for (String para : s.split("\n")) {
            StringBuilder line = new StringBuilder();
            for (String word : para.split(" ")) {
                String test;
                String string = test = line.isEmpty() ? word : String.valueOf(line) + " " + word;
                if (Gfx.textWidth(test, font, size) > maxW && !line.isEmpty()) {
                    out.add(line.toString());
                    line = new StringBuilder(word);
                    continue;
                }
                line = new StringBuilder(test);
            }
            out.add(line.toString());
        }
        return out;
    }

    public static class Custom
    extends Widget {
        private final Function<Float, Float> height;
        private final Painter painter;
        private final Clicker clicker;

        public Custom(Function<Float, Float> height, Painter painter, Clicker clicker) {
            this.height = height;
            this.painter = painter;
            this.clicker = clicker;
        }

        @Override
        protected float measure(float w) {
            return this.height.apply(Float.valueOf(w)).floatValue();
        }

        @Override
        public void render(class_332 g, float ox, float oy, double mx, double my, float a) {
            this.painter.paint(g, ox + this.x, oy + this.y, this.w, this.h, mx, my, a);
        }

        @Override
        public boolean click(double mx, double my, float ox, float oy) {
            return this.clicker != null && this.clicker.click(mx, my, ox + this.x, oy + this.y, this.w, this.h);
        }

        public static interface Painter {
            public void paint(class_332 var1, float var2, float var3, float var4, float var5, double var6, double var8, float var10);
        }

        public static interface Clicker {
            public boolean click(double var1, double var3, float var5, float var6, float var7, float var8);
        }
    }

    public static class TextField
    extends Row {
        private final Supplier<String> get;
        private final Consumer<String> set;
        private final String placeholder;
        private boolean focused;
        private boolean all;

        public TextField(String label, String desc, String placeholderKey, Supplier<String> get, Consumer<String> set) {
            super(label, desc);
            this.get = get;
            this.set = set;
            this.placeholder = placeholderKey;
        }

        @Override
        protected float measure(float w) {
            return 44.0f;
        }

        @Override
        public void render(class_332 g, float ox, float oy, double mx, double my, float a) {
            this.drawHover(g, ox, oy, mx, my, a);
            Gfx.text(g, Ui.tr(this.label, new Object[0]), FB, ox + this.x + 2.0f, oy + this.y + 4.0f, 8.4f, Col.alpha(-856070, a));
            if (this.desc != null) {
                Gfx.text(g, Gfx.ellipsize(Ui.tr(this.desc, new Object[0]), F, 6.8f, this.w), F, ox + this.x + 2.0f, oy + this.y + 14.5f, 6.8f, Col.alpha(-6515020, a));
            }
            float fx = ox + this.x + 2.0f;
            float fy = oy + this.y + 25.0f;
            float fw = this.w - 4.0f;
            float fh = 15.0f;
            Gfx.round(g, fx, fy, fw, fh, 5.0f, Col.alpha(-15659240, a));
            Gfx.roundOutline(g, fx, fy, fw, fh, 5.0f, this.focused ? 1.0f : 0.5f, Col.alpha(this.focused ? Themes.accent() : 0x30FFFFFF, a));
            String v = this.get.get();
            boolean empty = v.isEmpty();
            String shown = empty ? Ui.tr(this.placeholder, new Object[0]) : v;
            float tw = Gfx.textWidth(shown, F, 7.4f);
            float off = Math.max(0.0f, tw - (fw - 12.0f));
            g.method_44379((int)fx + 1, (int)fy, (int)(fx + fw - 1.0f), (int)(fy + fh));
            if (this.focused && this.all && !empty) {
                Gfx.round(g, fx + 5.0f - off, fy + 2.5f, tw + 2.0f, fh - 5.0f, 2.0f, Col.alpha(Themes.accent(), a * 0.55f));
            }
            Gfx.text(g, shown, F, fx + 6.0f - off, fy + 4.0f, 7.4f, Col.alpha(empty ? -9541498 : -856070, a));
            if (this.focused && !this.all && System.currentTimeMillis() / 500L % 2L == 0L) {
                float cx = fx + 6.0f - off + (empty ? 0.0f : tw) + 0.5f;
                Gfx.rect(g, cx, fy + 3.5f, 0.8f, 8.0f, Col.alpha(-856070, a));
            }
            g.method_44380();
        }

        @Override
        public boolean click(double mx, double my, float ox, float oy) {
            boolean over = Ui.inside(mx, my, ox + this.x, oy + this.y + 24.0f, this.w, 17.0f);
            if (over && !this.focused) {
                UiSounds.play(UiSounds.CLICK, 1.2f);
            }
            this.focused = over;
            this.all = false;
            return over;
        }

        @Override
        public boolean capturesKeys() {
            return this.focused;
        }

        @Override
        public boolean key(class_11908 e) {
            boolean ctrl;
            if (!this.focused) {
                return false;
            }
            String v = this.get.get();
            boolean bl = ctrl = (e.comp_4797() & 0xA) != 0;
            if (ctrl && e.comp_4795() == 65) {
                this.all = true;
                return true;
            }
            if (ctrl && (e.comp_4795() == 67 || e.comp_4795() == 88)) {
                class_310.method_1551().field_1774.method_1455(v);
                if (e.comp_4795() == 88) {
                    this.set.accept("");
                    this.all = false;
                }
            } else if (e.comp_4795() == 259 || e.comp_4795() == 261) {
                if (this.all || ctrl) {
                    this.set.accept("");
                } else if (!v.isEmpty()) {
                    this.set.accept(v.substring(0, v.length() - 1));
                }
                this.all = false;
            } else if (e.comp_4795() == 86 && ctrl) {
                String clip = class_310.method_1551().field_1774.method_1460();
                this.set.accept(((this.all ? "" : v) + clip.trim()).replaceAll("[\\r\\n]", ""));
                this.all = false;
            } else {
                if (e.comp_4795() == 263 || e.comp_4795() == 262 || e.comp_4795() == 269 || e.comp_4795() == 268) {
                    this.all = false;
                    return true;
                }
                if (e.comp_4795() == 257 || e.comp_4795() == 256 || e.comp_4795() == 258) {
                    this.focused = false;
                } else {
                    return e.comp_4795() != 256;
                }
            }
            Ui.save();
            return true;
        }

        @Override
        public boolean chr(class_11905 e) {
            if (!this.focused) {
                return false;
            }
            String s = e.method_74226();
            if (s.isEmpty() || Character.isISOControl(s.charAt(0))) {
                return true;
            }
            if (this.all) {
                this.set.accept("");
                this.all = false;
            }
            if (this.get.get().length() < 120) {
                this.set.accept(this.get.get() + s);
            }
            Ui.save();
            return true;
        }
    }

    public static class KeyBind
    extends Row {
        private final class_304 mapping;
        private boolean listening;

        public KeyBind(String label, String desc, class_304 mapping) {
            super(label, desc);
            this.mapping = mapping;
        }

        @Override
        public void render(class_332 g, float ox, float oy, double mx, double my, float a) {
            String s;
            this.drawHover(g, ox, oy, mx, my, a);
            String string = s = this.listening ? Ui.tr("lyricsflow.menu.press_key", new Object[0]) : this.mapping.method_16007().getString();
            if (this.mapping.method_1415() && !this.listening) {
                s = Ui.tr("lyricsflow.menu.unbound", new Object[0]);
            }
            float kw = Math.max(26.0f, Gfx.textWidth(s, FB, 7.6f) + 14.0f);
            float kh = 15.0f;
            float kx = ox + this.x + this.w - kw - 2.0f;
            float ky = oy + this.y + (this.h - kh) / 2.0f;
            boolean custom = !this.mapping.method_1427();
            float rx = kx - 18.0f;
            float ry = oy + this.y + this.h / 2.0f;
            this.drawLabel(g, ox, oy, a, this.w - kw - (float)(custom ? 30 : 12));
            int bg = this.listening ? Col.lerp(-13883584, Themes.accent(), (float)(0.5 + 0.5 * Math.sin(Anim.seconds() * 6.0))) : -13883584;
            Gfx.round(g, kx, ky, kw, kh, 5.0f, Col.alpha(bg, a));
            Gfx.roundOutline(g, kx, ky, kw, kh, 5.0f, 0.5f, Col.alpha(0x30FFFFFF, a));
            Gfx.textCentered(g, s, FB, kx + kw / 2.0f, ky + (kh - 7.6f) / 2.0f - 0.3f, 7.6f, Col.alpha(this.mapping.method_1415() && !this.listening ? -6515020 : -856070, a));
            if (custom) {
                boolean over = Ui.inside(mx, my, rx - 7.0f, ry - 7.0f, 14.0f, 14.0f);
                Gfx.circle(g, rx, ry, 7.0f, Col.alpha(over ? 0x38FFFFFF : 0x1CFFFFFF, a));
                Icons.draw(g, "refresh", rx, ry, 6.5f, Col.alpha(over ? -856070 : -6515020, a));
            }
            if (this.listening) {
                Gfx.text(g, Ui.tr("lyricsflow.menu.esc_unbind", new Object[0]), F, ox + this.x + 10.0f, oy + this.y + this.h - 7.0f, 5.6f, Col.alpha(-9541498, a));
            }
        }

        private float keyBoxX(float ox) {
            String s;
            String string = s = this.listening ? Ui.tr("lyricsflow.menu.press_key", new Object[0]) : this.mapping.method_16007().getString();
            if (this.mapping.method_1415() && !this.listening) {
                s = Ui.tr("lyricsflow.menu.unbound", new Object[0]);
            }
            float kw = Math.max(26.0f, Gfx.textWidth(s, FB, 7.6f) + 14.0f);
            return ox + this.x + this.w - kw - 2.0f;
        }

        @Override
        public boolean click(double mx, double my, float ox, float oy) {
            float ry;
            float rx;
            if (!this.hit(mx, my, ox, oy)) {
                this.listening = false;
                return false;
            }
            if (!this.mapping.method_1427() && !this.listening && Ui.inside(mx, my, (rx = this.keyBoxX(ox) - 18.0f) - 7.0f, (ry = oy + this.y + this.h / 2.0f) - 7.0f, 14.0f, 14.0f)) {
                this.mapping.method_1422(this.mapping.method_1429());
                class_304.method_1426();
                class_310.method_1551().field_1690.method_1640();
                UiSounds.play(UiSounds.TOGGLE_ON, 1.1f);
                return true;
            }
            this.listening = !this.listening;
            UiSounds.play(UiSounds.CLICK, this.listening ? 1.2f : 0.9f);
            this.ripple(mx, my);
            return true;
        }

        @Override
        public boolean capturesKeys() {
            return this.listening;
        }

        @Override
        public boolean key(class_11908 e) {
            if (!this.listening) {
                return false;
            }
            class_310 mc = class_310.method_1551();
            boolean clear = e.comp_4795() == 256 || e.comp_4795() == 259;
            class_3675.class_306 k = clear ? class_3675.field_16237 : class_3675.method_15985((class_11908)e);
            this.mapping.method_1422(k);
            class_304.method_1426();
            mc.field_1690.method_1640();
            this.listening = false;
            UiSounds.play(UiSounds.TOGGLE_ON, 1.1f);
            return true;
        }
    }

    public static class Colors
    extends Row {
        private final IntSupplier get;
        private final IntConsumer set;
        private final int[] pal;
        private final int first;
        private final float[] pop;
        private boolean none;

        public Colors(String label, String desc, IntSupplier get, IntConsumer set) {
            this(label, desc, get, set, Themes.SINGLE, true);
        }

        public Colors(String label, String desc, IntSupplier get, IntConsumer set, int[] palette, boolean theme) {
            super(label, desc);
            this.get = get;
            this.set = set;
            this.pal = palette;
            this.first = theme ? 1 : 0;
            this.pop = new float[palette.length + 1];
        }

        public Colors noneDot() {
            this.none = true;
            return this;
        }

        private float dotsW() {
            return (float)(this.pal.length + this.first) * 12.5f;
        }

        @Override
        public void render(class_332 g, float ox, float oy, double mx, double my, float a) {
            this.drawHover(g, ox, oy, mx, my, a);
            float dw = this.dotsW();
            this.drawLabel(g, ox, oy, a, this.w - dw - 10.0f);
            float sx = ox + this.x + this.w - dw;
            float cy = oy + this.y + this.h / 2.0f;
            int cur = this.get.getAsInt();
            for (int i = 1 - this.first; i <= this.pal.length; ++i) {
                float cx = sx + (float)(i - 1 + this.first) * 12.5f + 6.0f;
                boolean sel = i == 0 ? cur == 0 : (cur | 0xFF000000) == (this.pal[i - 1] | 0xFF000000);
                boolean over = Ui.inside(mx, my, cx - 6.0f, cy - 6.0f, 12.0f, 12.0f);
                this.pop[i] = Anim.approach(this.pop[i], sel ? 1.0f : (over ? 0.5f : 0.0f), 16.0f);
                float r = 4.2f + this.pop[i] * 0.9f;
                if (sel) {
                    Gfx.circle(g, cx, cy, r + 2.2f, Col.alpha(-1, a));
                }
                if (sel) {
                    Gfx.circle(g, cx, cy, r + 1.1f, Col.alpha(-15001562, a));
                }
                if (i == 0) {
                    Gfx.circle(g, cx, cy, r, Col.alpha(-12962480, a));
                    Gfx.textCentered(g, this.none ? "/" : "A", FB, cx, cy - 3.2f, 6.4f, Col.alpha(this.none ? -2076592 : -856070, a));
                    continue;
                }
                Gfx.circle(g, cx, cy, r, Col.alpha(this.pal[i - 1] | 0xFF000000, a));
                if ((this.pal[i - 1] & 0xFFFFFF) >= 0x202020) continue;
                Gfx.roundOutline(g, cx - r, cy - r, r * 2.0f, r * 2.0f, r, 0.6f, Col.alpha(0x60FFFFFF, a));
            }
        }

        @Override
        public boolean click(double mx, double my, float ox, float oy) {
            if (!this.hit(mx, my, ox, oy)) {
                return false;
            }
            float sx = ox + this.x + this.w - this.dotsW();
            float cy = oy + this.y + this.h / 2.0f;
            for (int i = 1 - this.first; i <= this.pal.length; ++i) {
                float cx = sx + (float)(i - 1 + this.first) * 12.5f + 6.0f;
                if (!Ui.inside(mx, my, cx - 6.25f, cy - 8.0f, 12.5f, 16.0f)) continue;
                this.set.accept(i == 0 ? 0 : this.pal[i - 1]);
                Ui.save();
                UiSounds.play(UiSounds.CLICK, 0.9f + (float)i * 0.03f);
                this.pop[i] = 1.6f;
                return true;
            }
            return true;
        }
    }

    public static class Info
    extends Widget {
        private final Supplier<String> text;
        private List<String> lines = List.of();

        public Info(Supplier<String> text) {
            this.text = text;
        }

        @Override
        protected float measure(float w) {
            this.lines = Ui.wrap(this.text.get(), F, 7.2f, w - 4.0f);
            return (float)this.lines.size() * 10.5f + 6.0f;
        }

        @Override
        public void render(class_332 g, float ox, float oy, double mx, double my, float a) {
            for (int i = 0; i < this.lines.size(); ++i) {
                Gfx.text(g, this.lines.get(i), F, ox + this.x + 2.0f, oy + this.y + 3.0f + (float)i * 10.5f, 7.2f, Col.alpha(-6515020, a));
            }
        }
    }

    public static class Status
    extends Row {
        private final Supplier<String> text;
        private final Supplier<String> sub;
        private final IntSupplier color;

        public Status(Supplier<String> text, Supplier<String> sub, IntSupplier color) {
            super("", null);
            this.text = text;
            this.sub = sub;
            this.color = color;
        }

        @Override
        protected boolean interactive() {
            return false;
        }

        @Override
        protected float measure(float w) {
            return 32.0f;
        }

        @Override
        public void render(class_332 g, float ox, float oy, double mx, double my, float a) {
            Gfx.text(g, Gfx.ellipsize(this.text.get(), FB, 8.6f, this.w - 30.0f), FB, ox + this.x + 2.0f, oy + this.y + 6.0f, 8.6f, Col.alpha(-856070, a));
            String s = this.sub.get();
            if (s != null) {
                Gfx.text(g, Gfx.ellipsize(s, F, 7.0f, this.w - 30.0f), F, ox + this.x + 2.0f, oy + this.y + 18.0f, 7.0f, Col.alpha(-6515020, a));
            }
            float cx = ox + this.x + this.w - 8.0f;
            float cy = oy + this.y + this.h / 2.0f;
            double pulse = 0.5 + 0.5 * Math.sin(Anim.seconds() * 3.0);
            Gfx.circle(g, cx, cy, 6.0f + (float)pulse * 2.0f, Col.alpha(this.color.getAsInt(), a * 0.18f));
            Gfx.circle(g, cx, cy, 3.6f, Col.alpha(this.color.getAsInt(), a));
        }
    }

    public static class Button
    extends Row {
        private final Supplier<String> button;
        private final Runnable action;
        private final String icon;
        private final boolean primary;

        public Button(String label, String desc, Supplier<String> buttonText, String icon, boolean primary, Runnable action) {
            super(label, desc);
            this.button = buttonText;
            this.icon = icon;
            this.primary = primary;
            this.action = action;
        }

        private float bw() {
            return Gfx.textWidth(this.button.get(), FB, 7.4f) + (float)(this.icon != null ? 26 : 16);
        }

        @Override
        public void render(class_332 g, float ox, float oy, double mx, double my, float a) {
            int col;
            this.drawHover(g, ox, oy, mx, my, a);
            float bw = this.bw();
            float bh = 15.0f;
            float bx = ox + this.x + this.w - bw - 2.0f;
            float by = oy + this.y + (this.h - bh) / 2.0f;
            this.drawLabel(g, ox, oy, a, this.w - bw - 12.0f);
            boolean over = Ui.inside(mx, my, bx, by, bw, bh);
            float sc = 1.0f - this.pressT * 0.06f + (over ? 0.02f : 0.0f);
            float cw = bw * sc;
            float ch = bh * sc;
            float cx = bx + (bw - cw) / 2.0f;
            float cy = by + (bh - ch) / 2.0f;
            int base = this.primary ? Themes.accent() : -13883584;
            int n = col = over ? Col.brighten(base, 0.12f) : base;
            if (this.primary && over) {
                Gfx.glow(g, cx, cy, cw, ch, ch / 2.0f, 3.0f, Col.alpha(base, a * 0.4f));
            }
            Gfx.round(g, cx, cy, cw, ch, ch / 2.0f, Col.alpha(col, a));
            float tx = cx + 8.0f;
            if (this.icon != null) {
                Icons.draw(g, this.icon, cx + 10.0f, cy + ch / 2.0f, 8.0f, Col.alpha(-1, a));
                tx += 10.0f;
            }
            Gfx.text(g, this.button.get(), FB, tx, cy + (ch - 7.4f) / 2.0f - 0.3f, 7.4f, Col.alpha(-1, a));
        }

        @Override
        public boolean click(double mx, double my, float ox, float oy) {
            if (!this.hit(mx, my, ox, oy)) {
                return false;
            }
            UiSounds.play(UiSounds.CLICK, 1.1f);
            this.ripple(mx, my);
            this.action.run();
            return true;
        }
    }

    public static class Slider
    extends Row {
        private final double min;
        private final double max;
        private final double step;
        private final DoubleSupplier get;
        private final DoubleConsumer set;
        private final DoubleFunction<String> fmt;
        private boolean dragging;
        private float thumb;
        private float shown = Float.NaN;
        private double lastStepValue = Double.NaN;

        public Slider(String label, double min, double max, double step, DoubleSupplier get, DoubleConsumer set, DoubleFunction<String> fmt) {
            super(label, null);
            this.min = min;
            this.max = max;
            this.step = step;
            this.get = get;
            this.set = set;
            this.fmt = fmt;
        }

        @Override
        protected float measure(float w) {
            return 32.0f;
        }

        private float trackX(float ox) {
            return ox + this.x + 2.0f;
        }

        private float trackW() {
            return this.w - 4.0f;
        }

        private float trackY(float oy) {
            return oy + this.y + 22.0f;
        }

        @Override
        public void render(class_332 g, float ox, float oy, double mx, double my, float a) {
            this.drawHover(g, ox, oy, mx, my, a);
            double v = this.get.getAsDouble();
            float p = (float)((v - this.min) / (this.max - this.min));
            if (Float.isNaN(this.shown)) {
                this.shown = p;
            }
            this.shown = Anim.approach(this.shown, p, this.dragging ? 30.0f : 14.0f);
            String val = this.fmt.apply(v);
            Gfx.text(g, Ui.tr(this.label, new Object[0]), FB, ox + this.x + 2.0f, oy + this.y + 5.0f, 8.4f, Col.alpha(-856070, a));
            float vw = Gfx.textWidth(val, F, 7.6f);
            Gfx.text(g, val, F, ox + this.x + this.w - vw - 2.0f, oy + this.y + 5.6f, 7.6f, Col.alpha(-6515020, a));
            float tx = this.trackX(ox);
            float tw = this.trackW();
            float ty = this.trackY(oy);
            Gfx.round(g, tx, ty - 1.5f, tw, 3.0f, 1.5f, Col.alpha(-12962480, a));
            Gfx.round(g, tx, ty - 1.5f, Math.max(3.0f, tw * this.shown), 3.0f, 1.5f, Col.alpha(Themes.accent(), a));
            boolean over = Ui.inside(mx, my, tx - 4.0f, ty - 7.0f, tw + 8.0f, 14.0f);
            this.thumb = Anim.approach(this.thumb, this.dragging ? 1.4f : (over ? 1.18f : 1.0f), 18.0f);
            float r = 4.6f * this.thumb;
            float kx = tx + tw * this.shown;
            if (this.dragging) {
                Gfx.circle(g, kx, ty, r + 4.0f, Col.alpha(Themes.accent(), a * 0.22f));
            }
            Gfx.circle(g, kx, ty + 0.6f, r + 0.6f, Col.alpha(0x55000000, a));
            Gfx.circle(g, kx, ty, r, Col.alpha(-1, a));
        }

        private void apply(double mx, float ox) {
            double p = (mx - (double)this.trackX(ox)) / (double)this.trackW();
            p = Math.max(0.0, Math.min(1.0, p));
            double v = this.min + (this.max - this.min) * p;
            if (this.step > 0.0) {
                v = (double)Math.round(v / this.step) * this.step;
            }
            v = Math.max(this.min, Math.min(this.max, v));
            if (Double.isNaN(this.lastStepValue) || Math.abs(v - this.lastStepValue) > 1.0E-9) {
                if (!Double.isNaN(this.lastStepValue)) {
                    UiSounds.play(UiSounds.SLIDER, 0.8f + (float)p * 0.6f);
                }
                this.lastStepValue = v;
                this.set.accept(v);
            }
        }

        @Override
        public boolean click(double mx, double my, float ox, float oy) {
            if (!Ui.inside(mx, my, ox + this.x - 4.0f, oy + this.y + 12.0f, this.w + 8.0f, 20.0f)) {
                return false;
            }
            this.dragging = true;
            this.lastStepValue = this.get.getAsDouble();
            this.apply(mx, ox);
            return true;
        }

        @Override
        public void drag(double mx, double my, float ox, float oy) {
            if (this.dragging) {
                this.apply(mx, ox);
            }
        }

        @Override
        public void release() {
            if (this.dragging) {
                this.dragging = false;
                Ui.save();
            }
        }
    }

    public static class CosmeticSlots
    extends Widget {
        private final IntFunction<String> idOf;
        private final IntSupplier sel;
        private final IntConsumer pick;
        private final IntConsumer clear;
        private final float[] hov = new float[5];
        private static final float GAP = 6.0f;
        private static final float H = 50.0f;

        public CosmeticSlots(IntFunction<String> idOf, IntSupplier sel, IntConsumer pick, IntConsumer clear) {
            this.idOf = idOf;
            this.sel = sel;
            this.pick = pick;
            this.clear = clear;
        }

        @Override
        protected boolean interactive() {
            return true;
        }

        @Override
        protected float measure(float w) {
            return 54.0f;
        }

        private float cw() {
            return (this.w - 24.0f) / 5.0f;
        }

        @Override
        public void render(class_332 g, float ox, float oy, double mx, double my, float a) {
            float cw = this.cw();
            int s = this.sel.getAsInt();
            for (int i = 0; i < 5; ++i) {
                float x = ox + this.x + (float)i * (cw + 6.0f);
                float y = oy + this.y;
                boolean over = Ui.inside(mx, my, x, y, cw, 50.0f);
                boolean on = i == s;
                this.hov[i] = Anim.approach(this.hov[i], over ? 1.0f : 0.0f, 14.0f);
                Gfx.round(g, x, y, cw, 50.0f, 8.0f, Col.alpha(Col.lerp(on ? -13883584 : -15067356, -13423286, this.hov[i]), a));
                if (on) {
                    Gfx.roundOutline(g, x, y, cw, 50.0f, 8.0f, 1.4f, Col.alpha(Themes.accent(), a));
                }
                String id = this.idOf.apply(i);
                Gfx.text(g, String.valueOf(i + 1), FB, x + 5.0f, y + 4.0f, 6.4f, Col.alpha(on ? Themes.accent() : -9541498, a));
                float im = Math.min(cw - 18.0f, 34.0f);
                float ix = x + (cw - im) / 2.0f;
                float iy = y + 7.0f;
                if ("none".equals(id)) {
                    Gfx.textCentered(g, "+", FB, x + cw / 2.0f, y + 25.0f - 7.0f, 13.0f, Col.alpha(on ? -6515020 : -9541498, a));
                    continue;
                }
                Gfx.image(g, LyricsFlow.id("textures/gui/cosmetics/" + id + ".png"), 128, 128, ix, iy, im, im, Col.alpha(-1, a));
                boolean xo = Ui.inside(mx, my, x + cw - 13.0f, y + 2.0f, 11.0f, 11.0f);
                Gfx.circle(g, x + cw - 7.5f, y + 7.5f, 5.0f, Col.alpha(xo ? -2076566 : -12962736, a * (0.4f + 0.6f * Math.max(this.hov[i], on ? 1.0f : 0.0f))));
                Icons.draw(g, "close", x + cw - 7.5f, y + 7.5f, 5.0f, Col.alpha(-856070, a * (0.4f + 0.6f * Math.max(this.hov[i], on ? 1.0f : 0.0f))));
            }
        }

        @Override
        public boolean click(double mx, double my, float ox, float oy) {
            float cw = this.cw();
            for (int i = 0; i < 5; ++i) {
                float x = ox + this.x + (float)i * (cw + 6.0f);
                float y = oy + this.y;
                if (!Ui.inside(mx, my, x, y, cw, 50.0f)) continue;
                if (!"none".equals(this.idOf.apply(i)) && Ui.inside(mx, my, x + cw - 13.0f, y + 2.0f, 11.0f, 11.0f)) {
                    this.clear.accept(i);
                }
                this.pick.accept(i);
                Ui.save();
                UiSounds.play(UiSounds.CLICK, 1.0f + (float)i * 0.03f);
                return true;
            }
            return false;
        }
    }

    public static class CosmeticGrid
    extends Widget {
        private final String[] ids;
        private final IntSupplier get;
        private final IntConsumer set;
        private static final float CW = 66.0f;
        private static final float CH = 80.0f;
        private static final float GAP = 6.0f;
        private int cols = 1;
        private final float[] hov;
        private IntUnaryOperator badge;

        public CosmeticGrid(String[] ids, IntSupplier get, IntConsumer set) {
            this.ids = ids;
            this.get = get;
            this.set = set;
            this.hov = new float[ids.length];
        }

        public CosmeticGrid badge(IntUnaryOperator b) {
            this.badge = b;
            return this;
        }

        @Override
        protected boolean interactive() {
            return true;
        }

        @Override
        protected float measure(float w) {
            this.cols = Math.max(1, (int)((w + 6.0f) / 72.0f));
            int rows = (this.ids.length + this.cols - 1) / this.cols;
            return (float)rows * 86.0f + 4.0f;
        }

        private float cellW() {
            return (this.w - 6.0f * (float)(this.cols - 1)) / (float)this.cols;
        }

        @Override
        public void render(class_332 g, float ox, float oy, double mx, double my, float a) {
            float cw = this.cellW();
            int sel = this.get.getAsInt();
            for (int i = 0; i < this.ids.length; ++i) {
                int bn;
                float x = ox + this.x + (float)(i % this.cols) * (cw + 6.0f);
                float y = oy + this.y + (float)(i / this.cols) * 86.0f;
                boolean over = Ui.inside(mx, my, x, y, cw, 80.0f);
                boolean on = i == sel;
                this.hov[i] = Anim.approach(this.hov[i], over ? 1.0f : 0.0f, 14.0f);
                int bg = Col.lerp(-14541264, -13883584, this.hov[i]);
                Gfx.round(g, x, y, cw, 80.0f, 8.0f, Col.alpha(bg, a));
                if (on) {
                    Gfx.roundOutline(g, x, y, cw, 80.0f, 8.0f, 1.4f, Col.alpha(Themes.accent(), a));
                }
                float im = Math.min(cw - 12.0f, 56.0f);
                float ix = x + (cw - im) / 2.0f;
                float iy = y + 5.0f - this.hov[i] * 1.5f;
                if ("none".equals(this.ids[i])) {
                    Icons.draw(g, "close", ix + im / 2.0f, iy + im / 2.0f, im * 0.45f, Col.alpha(-9541498, a));
                } else {
                    Gfx.image(g, LyricsFlow.id("textures/gui/cosmetics/" + this.ids[i] + ".png"), 128, 128, ix, iy, im, im, Col.alpha(-1, a));
                }
                int n = bn = this.badge != null && !on ? this.badge.applyAsInt(i) : 0;
                if (bn > 0) {
                    Gfx.circle(g, x + cw - 9.0f, y + 9.0f, 6.0f, Col.alpha(Col.darken(Themes.accent(), 0.35f), a));
                    Gfx.textCentered(g, String.valueOf(bn), FB, x + cw - 9.0f, y + 5.6f, 6.4f, Col.alpha(-856070, a));
                }
                String name = Gfx.ellipsize(Ui.tr("lyricsflow.cosmetic." + this.ids[i], new Object[0]), FB, 6.6f, cw - 8.0f);
                Gfx.textCentered(g, name, FB, x + cw / 2.0f, y + 80.0f - 14.0f, 6.6f, Col.alpha(on ? -856070 : -6515020, a));
            }
        }

        @Override
        public boolean click(double mx, double my, float ox, float oy) {
            float cw = this.cellW();
            for (int i = 0; i < this.ids.length; ++i) {
                float x = ox + this.x + (float)(i % this.cols) * (cw + 6.0f);
                float y = oy + this.y + (float)(i / this.cols) * 86.0f;
                if (!Ui.inside(mx, my, x, y, cw, 80.0f)) continue;
                this.set.accept(i);
                Ui.save();
                UiSounds.play(UiSounds.CLICK, 1.0f + (float)(i % 5) * 0.02f);
                return true;
            }
            return false;
        }
    }

    public static class SkyGrid
    extends Widget {
        private final SkyPreset[] skies;
        private final Supplier<SkyPreset> get;
        private final Consumer<SkyPreset> set;
        private static final float MINW = 92.0f;
        private static final float GAP = 6.0f;
        private int cols = 1;
        private final float[] hov;

        public SkyGrid(SkyPreset[] skies, Supplier<SkyPreset> get, Consumer<SkyPreset> set) {
            this.skies = skies;
            this.get = get;
            this.set = set;
            this.hov = new float[skies.length];
        }

        @Override
        protected boolean interactive() {
            return true;
        }

        private float cellW() {
            return (this.w - 6.0f * (float)(this.cols - 1)) / (float)this.cols;
        }

        private float picH() {
            return this.cellW() * 0.5625f;
        }

        private float cellH() {
            return this.picH() + 14.0f;
        }

        @Override
        protected float measure(float w) {
            this.cols = Math.max(2, (int)((w + 6.0f) / 98.0f));
            this.w = w;
            int rows = (this.skies.length + this.cols - 1) / this.cols;
            return (float)rows * (this.cellH() + 6.0f) + 2.0f;
        }

        @Override
        public void render(class_332 g, float ox, float oy, double mx, double my, float a) {
            float cw = this.cellW();
            float ph = this.picH();
            float ch = this.cellH();
            SkyPreset cur = this.get.get();
            for (int i = 0; i < this.skies.length; ++i) {
                float x = ox + this.x + (float)(i % this.cols) * (cw + 6.0f);
                float y = oy + this.y + (float)(i / this.cols) * (ch + 6.0f);
                boolean over = Ui.inside(mx, my, x, y, cw, ch);
                boolean on = this.skies[i] == cur;
                this.hov[i] = Anim.approach(this.hov[i], over ? 1.0f : 0.0f, 14.0f);
                float lift = -this.hov[i] * 1.2f;
                if (on) {
                    Gfx.glow(g, x, y + lift, cw, ph, 6.0f, 4.0f, Col.alpha(Themes.accent(), a * 0.45f));
                }
                Gfx.round(g, x, y + lift, cw, ph, 6.0f, Col.alpha(-14541264, a));
                SkyPreset p = this.skies[i];
                float fx = x;
                float fy = y + lift;
                int ix = Math.round(fx);
                int iy = Math.round(fy);
                int iw = Math.round(cw);
                int ih = Math.round(ph);
                SkyThumbs.draw(g, p, ix, iy, iw, ih, a);
                Gfx.roundOutline(g, x, y + lift, cw, ph, 6.0f, on ? 1.3f : 0.5f, Col.alpha(on ? Themes.accent() : 0x22FFFFFF, a));
                String name = Gfx.ellipsize(Ui.tr("lyricsflow.sky." + p.name().toLowerCase(Locale.ROOT), new Object[0]), FB, 6.6f, cw - 4.0f);
                Gfx.text(g, name, FB, x + 1.0f, y + lift + ph + 3.5f, 6.6f, Col.alpha(on ? -856070 : Col.lerp(-6515020, -856070, this.hov[i]), a));
            }
        }

        @Override
        public boolean click(double mx, double my, float ox, float oy) {
            float cw = this.cellW();
            float ch = this.cellH();
            for (int i = 0; i < this.skies.length; ++i) {
                float x = ox + this.x + (float)(i % this.cols) * (cw + 6.0f);
                float y = oy + this.y + (float)(i / this.cols) * (ch + 6.0f);
                if (!Ui.inside(mx, my, x, y, cw, ch)) continue;
                this.set.accept(this.skies[i]);
                Ui.save();
                UiSounds.play(UiSounds.CLICK, 1.0f + (float)(i % 5) * 0.02f);
                return true;
            }
            return false;
        }
    }

    public static class Chips
    extends Row {
        private final String[] options;
        private final IntSupplier get;
        private final IntConsumer set;
        private float[] cx;
        private float[] cy;
        private float[] cw;
        private static final float CH = 15.0f;
        private static final float GAP = 4.0f;
        private String[] fonts;
        private int[] dots;
        private Runnable reset;
        private float spin;

        public Chips fonts(String[] f) {
            this.fonts = f;
            return this;
        }

        public Chips dots(int[] d) {
            this.dots = d;
            return this;
        }

        public Chips reset(Runnable r) {
            this.reset = r;
            return this;
        }

        private String fontOf(int i) {
            return this.fonts != null && i < this.fonts.length && this.fonts[i] != null ? this.fonts[i] : FB;
        }

        private float dotW(int i) {
            return this.dots != null && i < this.dots.length ? 9.0f : 0.0f;
        }

        public Chips(String label, String desc, String[] optionKeys, IntSupplier get, IntConsumer set) {
            super(label, desc);
            this.options = optionKeys;
            this.get = get;
            this.set = set;
        }

        private float head() {
            return this.desc != null ? 30.0f : 22.0f;
        }

        private int place(float w) {
            int n = this.options.length;
            this.cx = new float[n];
            this.cy = new float[n];
            this.cw = new float[n];
            float x = 2.0f;
            float y = 0.0f;
            int lines = 1;
            for (int i = 0; i < n; ++i) {
                float ww = Gfx.textWidth(Ui.tr(this.options[i], new Object[0]), this.fontOf(i), 7.2f) + 16.0f + this.dotW(i);
                if (x + ww > w - 2.0f && x > 2.0f) {
                    x = 2.0f;
                    y += 19.0f;
                    ++lines;
                }
                this.cx[i] = x;
                this.cy[i] = y;
                this.cw[i] = ww;
                x += ww + 4.0f;
            }
            return lines;
        }

        @Override
        protected float measure(float w) {
            int lines = this.place(w);
            return this.head() + (float)lines * 19.0f + 4.0f;
        }

        @Override
        protected void drawLabel(class_332 g, float ox, float oy, float a, float maxW) {
            float ly = oy + this.y + 5.0f;
            Gfx.text(g, Gfx.ellipsize(Ui.tr(this.label, new Object[0]), FB, 8.4f, this.w - 4.0f), FB, ox + this.x + 2.0f, ly, 8.4f, Col.alpha(-856070, a));
            if (this.desc != null) {
                Gfx.text(g, Gfx.ellipsize(Ui.tr(this.desc, new Object[0]), F, 6.8f, this.w - 4.0f), F, ox + this.x + 2.0f, ly + 11.5f, 6.8f, Col.alpha(-6515020, a));
            }
        }

        @Override
        public void render(class_332 g, float ox, float oy, double mx, double my, float a) {
            this.drawHover(g, ox, oy, mx, my, a);
            this.drawLabel(g, ox, oy, a, this.w);
            if (this.cx == null) {
                this.place(this.w);
            }
            int sel = this.get.getAsInt();
            float by = oy + this.y + this.head();
            for (int i = 0; i < this.options.length; ++i) {
                float px = ox + this.x + this.cx[i];
                float py = by + this.cy[i];
                boolean on = i == sel;
                boolean over = Ui.inside(mx, my, px, py, this.cw[i], 15.0f);
                int bg = on ? -1 : (over ? -14015176 : -15659240);
                Gfx.round(g, px, py, this.cw[i], 15.0f, 7.5f, Col.alpha(bg, a));
                int col = on ? -15330529 : (over ? -856070 : -6515020);
                float dw = this.dotW(i);
                if (dw > 0.0f) {
                    Gfx.circle(g, px + 9.0f, py + 7.5f, 3.2f, Col.alpha(this.dots[i] | 0xFF000000, a));
                }
                Gfx.textCentered(g, Ui.tr(this.options[i], new Object[0]), this.fontOf(i), px + dw + (this.cw[i] - dw) / 2.0f, py + 3.9f - 0.4f, 7.2f, Col.alpha(col, a));
            }
            if (this.reset != null) {
                float rx = ox + this.x + this.w - 16.0f;
                float ry = oy + this.y + 3.0f;
                boolean ro = Ui.inside(mx, my, rx, ry, 16.0f, 16.0f);
                this.spin = Anim.approach(this.spin, 0.0f, 6.0f);
                Gfx.round(g, rx, ry, 16.0f, 16.0f, 8.0f, Col.alpha(ro ? -13883584 : -15659240, a));
                g.method_51448().pushMatrix();
                g.method_51448().translate(rx + 8.0f, ry + 8.0f);
                g.method_51448().rotate(-this.spin * 6.28f);
                Icons.draw(g, "refresh", 0.0f, 0.0f, 8.0f, Col.alpha(ro ? -856070 : -9541498, a));
                g.method_51448().popMatrix();
            }
        }

        @Override
        public boolean click(double mx, double my, float ox, float oy) {
            if (!this.hit(mx, my, ox, oy) || this.cx == null) {
                return false;
            }
            if (this.reset != null && Ui.inside(mx, my, ox + this.x + this.w - 16.0f, oy + this.y + 3.0f, 16.0f, 16.0f)) {
                this.reset.run();
                this.spin = 1.0f;
                Ui.save();
                UiSounds.play(UiSounds.CLICK, 0.9f);
                this.ripple(mx, my);
                return true;
            }
            float by = oy + this.y + this.head();
            for (int i = 0; i < this.options.length; ++i) {
                if (!Ui.inside(mx, my, ox + this.x + this.cx[i], by + this.cy[i], this.cw[i], 15.0f)) continue;
                this.set.accept(i);
                Ui.save();
                UiSounds.play(UiSounds.CLICK, 1.0f);
                this.ripple(mx, my);
                return true;
            }
            return true;
        }
    }

    public static class Choice
    extends Row {
        private final String[] options;
        private final IntSupplier get;
        private final IntConsumer set;
        private float pillX = -1.0f;
        private float pillW;
        private float[] ox0;
        private float[] ow;
        private boolean stacked;

        public Choice(String label, String desc, String[] optionKeys, IntSupplier get, IntConsumer set) {
            super(label, desc);
            this.options = optionKeys;
            this.get = get;
            this.set = set;
        }

        private float segH() {
            return 15.0f;
        }

        private float totalWidth() {
            float total = 6.0f;
            for (String o : this.options) {
                total += Gfx.textWidth(Ui.tr(o, new Object[0]), FB, 7.2f) + 14.0f;
            }
            return total;
        }

        @Override
        protected float measure(float w) {
            boolean bl = this.stacked = this.totalWidth() > w * 0.56f;
            if (!this.stacked) {
                return super.measure(w);
            }
            return (this.desc != null ? 30 : 22) + 18;
        }

        private float segY() {
            return this.stacked ? this.h - this.segH() - 5.0f : (this.h - this.segH()) / 2.0f;
        }

        @Override
        protected void drawLabel(class_332 g, float ox, float oy, float a, float maxW) {
            if (!this.stacked) {
                super.drawLabel(g, ox, oy, a, maxW);
                return;
            }
            float ly = oy + this.y + 5.0f;
            Gfx.text(g, Gfx.ellipsize(Ui.tr(this.label, new Object[0]), FB, 8.4f, this.w - 4.0f), FB, ox + this.x + 2.0f, ly, 8.4f, Col.alpha(-856070, a));
            if (this.desc != null) {
                Gfx.text(g, Gfx.ellipsize(Ui.tr(this.desc, new Object[0]), F, 6.8f, this.w - 4.0f), F, ox + this.x + 2.0f, ly + 11.5f, 6.8f, Col.alpha(-6515020, a));
            }
        }

        @Override
        public void render(class_332 g, float ox, float oy, double mx, double my, float a) {
            this.drawHover(g, ox, oy, mx, my, a);
            int n = this.options.length;
            this.ox0 = new float[n];
            this.ow = new float[n];
            float total = 3.0f;
            for (int i = 0; i < n; ++i) {
                this.ow[i] = Gfx.textWidth(Ui.tr(this.options[i], new Object[0]), FB, 7.2f) + 14.0f;
                total += this.ow[i];
            }
            total += 3.0f;
            float sh = this.segH();
            if (this.stacked && total > this.w - 4.0f) {
                float k = (this.w - 4.0f - 6.0f) / (total - 6.0f);
                int i = 0;
                while (i < n) {
                    int n2 = i++;
                    this.ow[n2] = this.ow[n2] * k;
                }
                total = this.w - 4.0f;
            }
            float bx = this.stacked ? ox + this.x + 2.0f : ox + this.x + this.w - total - 2.0f;
            float by = oy + this.y + this.segY();
            float cx = bx + 3.0f;
            for (int i = 0; i < n; ++i) {
                this.ox0[i] = cx;
                cx += this.ow[i];
            }
            this.drawLabel(g, ox, oy, a, this.w - total - 12.0f);
            Gfx.round(g, bx, by, total, sh, sh / 2.0f, Col.alpha(-15659240, a));
            int sel = Math.max(0, Math.min(n - 1, this.get.getAsInt()));
            float tx = this.ox0[sel];
            float twd = this.ow[sel];
            if (this.pillX < 0.0f) {
                this.pillX = tx - bx;
                this.pillW = twd;
            }
            this.pillX = Anim.approach(this.pillX, tx - bx, 15.0f);
            this.pillW = Anim.approach(this.pillW, twd, 15.0f);
            Gfx.round(g, bx + this.pillX, by + 2.0f, this.pillW, sh - 4.0f, (sh - 4.0f) / 2.0f, Col.alpha(-1, a));
            for (int i = 0; i < n; ++i) {
                float center = this.ox0[i] + this.ow[i] / 2.0f;
                float dist = Math.abs(bx + this.pillX + this.pillW / 2.0f - center) / Math.max(1.0f, this.ow[i]);
                int col = Col.lerp(-15330529, -6515020, Math.min(1.0f, dist * 1.6f));
                boolean over = Ui.inside(mx, my, this.ox0[i], by, this.ow[i], sh);
                if (over && i != sel) {
                    col = Col.lerp(col, -856070, 0.6f);
                }
                String lbl = Gfx.ellipsize(Ui.tr(this.options[i], new Object[0]), FB, 7.2f, this.ow[i] - 6.0f);
                Gfx.textCentered(g, lbl, FB, center, by + (sh - 7.2f) / 2.0f - 0.4f, 7.2f, Col.alpha(col, a));
            }
        }

        @Override
        public boolean click(double mx, double my, float ox, float oy) {
            if (this.ox0 == null || !this.hit(mx, my, ox, oy)) {
                return false;
            }
            int n = this.options.length;
            float sh = this.segH();
            float by = oy + this.y + this.segY();
            for (int i = 0; i < n; ++i) {
                if (!Ui.inside(mx, my, this.ox0[i], by - 3.0f, this.ow[i], sh + 6.0f)) continue;
                if (i != this.get.getAsInt()) {
                    this.set.accept(i);
                    Ui.save();
                    UiSounds.play(UiSounds.CLICK, 1.05f + (float)i * 0.04f);
                }
                this.ripple(mx, my);
                return true;
            }
            this.set.accept((this.get.getAsInt() + 1) % n);
            Ui.save();
            UiSounds.play(UiSounds.CLICK, 1.0f);
            this.ripple(mx, my);
            return true;
        }
    }

    public static class Picker
    extends Row {
        private final Supplier<String> value;
        private final Supplier<String> valueFont;
        private final IntSupplier dot;
        private final Runnable prev;
        private final Runnable next;
        private Runnable reset;
        private float bx;
        private float bw;
        private float flash;
        private float rx = -1.0f;
        private float spin;

        public Picker(String label, String desc, Supplier<String> value, Supplier<String> valueFont, IntSupplier dot, Runnable prev, Runnable next) {
            super(label, desc);
            this.value = value;
            this.valueFont = valueFont;
            this.dot = dot;
            this.prev = prev;
            this.next = next;
        }

        public Picker reset(Runnable r) {
            this.reset = r;
            return this;
        }

        @Override
        public void render(class_332 g, float ox, float oy, double mx, double my, float a) {
            this.drawHover(g, ox, oy, mx, my, a);
            String v = this.value.get();
            String vf = this.valueFont != null && this.valueFont.get() != null ? this.valueFont.get() : FB;
            float size = 8.6f;
            int dc = this.dot != null ? this.dot.getAsInt() : 0;
            float vw = Math.min(110.0f, Gfx.textWidth(v, vf, size)) + (float)(dc != 0 ? 11 : 0);
            this.bw = vw + 34.0f;
            this.bx = ox + this.x + this.w - this.bw - 2.0f;
            float by = oy + this.y + (this.h - 16.0f) / 2.0f;
            float extra = 0.0f;
            if (this.reset != null) {
                this.rx = this.bx - 18.0f;
                extra = 20.0f;
                boolean ro = Ui.inside(mx, my, this.rx, by, 16.0f, 16.0f);
                this.spin = Anim.approach(this.spin, 0.0f, 6.0f);
                Gfx.round(g, this.rx, by, 16.0f, 16.0f, 8.0f, Col.alpha(ro ? -13883584 : -15659240, a));
                g.method_51448().pushMatrix();
                g.method_51448().translate(this.rx + 8.0f, by + 8.0f);
                g.method_51448().rotate(-this.spin * 6.28f);
                Icons.draw(g, "refresh", 0.0f, 0.0f, 8.0f, Col.alpha(ro ? -856070 : -9541498, a));
                g.method_51448().popMatrix();
            }
            this.drawLabel(g, ox, oy, a, this.w - this.bw - 12.0f - extra);
            this.flash = Anim.approach(this.flash, 0.0f, 8.0f);
            Gfx.round(g, this.bx, by, this.bw, 16.0f, 8.0f, Col.alpha(Col.lerp(-15659240, -13883584, this.flash), a));
            boolean ol = Ui.inside(mx, my, this.bx, by, this.bw / 2.0f, 16.0f);
            boolean or = Ui.inside(mx, my, this.bx + this.bw / 2.0f, by, this.bw / 2.0f, 16.0f);
            Gfx.text(g, "\u2039", FB, this.bx + 6.0f, by + 3.5f, 9.0f, Col.alpha(ol ? -856070 : -9541498, a));
            Gfx.text(g, "\u203a", FB, this.bx + this.bw - 10.0f, by + 3.5f, 9.0f, Col.alpha(or ? -856070 : -9541498, a));
            float tx = this.bx + 17.0f;
            if (dc != 0) {
                Gfx.circle(g, tx + 3.5f, by + 8.0f, 3.6f, Col.alpha(dc | 0xFF000000, a));
                tx += 11.0f;
            }
            Gfx.text(g, Gfx.ellipsize(v, vf, size, 110.0f), vf, tx, by + 3.6f, size, Col.alpha(-856070, a));
        }

        @Override
        public boolean click(double mx, double my, float ox, float oy) {
            if (!this.hit(mx, my, ox, oy)) {
                return false;
            }
            if (this.reset != null && mx >= (double)this.rx && mx < (double)(this.rx + 16.0f)) {
                this.reset.run();
                this.spin = 1.0f;
                Ui.save();
                UiSounds.play(UiSounds.CLICK, 0.9f);
                this.ripple(mx, my);
                return true;
            }
            if (mx < (double)this.bx) {
                return false;
            }
            if (mx < (double)(this.bx + this.bw / 2.0f)) {
                this.prev.run();
            } else {
                this.next.run();
            }
            this.flash = 1.0f;
            Ui.save();
            UiSounds.play(UiSounds.CLICK, mx < (double)(this.bx + this.bw / 2.0f) ? 0.95f : 1.1f);
            this.ripple(mx, my);
            return true;
        }
    }

    public static class Toggle
    extends Row {
        private final BooleanSupplier get;
        private final Consumer<Boolean> set;
        private float knob = -1.0f;

        public Toggle(String label, String desc, BooleanSupplier get, Consumer<Boolean> set) {
            super(label, desc);
            this.get = get;
            this.set = set;
        }

        @Override
        public void render(class_332 g, float ox, float oy, double mx, double my, float a) {
            this.drawHover(g, ox, oy, mx, my, a);
            boolean on = this.get.getAsBoolean();
            if (this.knob < 0.0f) {
                this.knob = on ? 1.0f : 0.0f;
            }
            this.knob = Anim.approach(this.knob, on ? 1.0f : 0.0f, 16.0f);
            float tw = 24.0f;
            float th = 13.0f;
            float tx = ox + this.x + this.w - tw - 2.0f;
            float ty = oy + this.y + (this.h - th) / 2.0f;
            this.drawLabel(g, ox, oy, a, this.w - tw - 14.0f);
            int bg = Col.lerp(-12962480, Themes.accent(), this.knob);
            if (this.knob > 0.5f) {
                Gfx.glow(g, tx, ty, tw, th, th / 2.0f, 3.0f, Col.alpha(Themes.accent(), a * 0.35f * this.knob));
            }
            Gfx.round(g, tx, ty, tw, th, th / 2.0f, Col.alpha(bg, a));
            float squash = 1.0f + this.pressT * 0.25f;
            float kd = th - 3.4f;
            float kw = kd * squash;
            float kx = tx + 1.7f + (tw - 3.4f - kw) * Anim.easeInOut(this.knob);
            Gfx.shadow(g, kx, ty + 1.7f, kw, kd, kd / 2.0f, 1.5f, Col.alpha(-16777216, a * 0.4f));
            Gfx.round(g, kx, ty + 1.7f, kw, kd, kd / 2.0f, Col.alpha(-1, a));
        }

        @Override
        public boolean click(double mx, double my, float ox, float oy) {
            if (!this.hit(mx, my, ox, oy)) {
                return false;
            }
            boolean v = !this.get.getAsBoolean();
            this.set.accept(v);
            Ui.save();
            UiSounds.play(v ? UiSounds.TOGGLE_ON : UiSounds.TOGGLE_OFF, 1.0f);
            this.ripple(mx, my);
            return true;
        }
    }

    public static abstract class Row
    extends Widget {
        protected final String label;
        protected final String desc;
        protected float pressT;
        protected double rippleX;
        protected double rippleY;
        protected double rippleAt = -10.0;

        protected Row(String label, String desc) {
            this.label = label;
            this.desc = desc;
        }

        @Override
        protected float measure(float w) {
            return this.desc != null ? 30.0f : 24.0f;
        }

        @Override
        protected boolean interactive() {
            return true;
        }

        protected void drawLabel(class_332 g, float ox, float oy, float a, float maxW) {
            float ly = oy + this.y + (this.desc != null ? 6.0f : 7.5f);
            Gfx.text(g, Gfx.ellipsize(Ui.tr(this.label, new Object[0]), FB, 8.4f, maxW), FB, ox + this.x + 2.0f, ly, 8.4f, Col.alpha(-856070, a));
            if (this.desc != null) {
                Gfx.text(g, Gfx.ellipsize(Ui.tr(this.desc, new Object[0]), F, 6.8f, maxW), F, ox + this.x + 2.0f, ly + 11.5f, 6.8f, Col.alpha(-6515020, a));
            }
        }

        protected void drawHover(class_332 g, float ox, float oy, double mx, double my, float a) {
            double age;
            boolean over = Ui.inside(mx, my, ox + this.x - 6.0f, oy + this.y, this.w + 12.0f, this.h);
            this.updateHover(over);
            if (this.hover > 0.01f) {
                Gfx.round(g, ox + this.x - 6.0f, oy + this.y + 1.0f, this.w + 12.0f, this.h - 2.0f, 6.0f, Col.alpha(0xCFFFFFF, a * this.hover));
            }
            if ((age = Anim.seconds() - this.rippleAt) < 0.45) {
                float p = (float)(age / 0.45);
                float r = 8.0f + p * 60.0f;
                g.method_44379((int)(ox + this.x - 6.0f), (int)(oy + this.y), (int)Math.ceil(ox + this.x + this.w + 6.0f), (int)Math.ceil(oy + this.y + this.h));
                Gfx.circle(g, (float)this.rippleX, (float)this.rippleY, r, Col.alpha(Themes.accent(), a * 0.16f * (1.0f - p)));
                g.method_44380();
            }
            this.pressT = Anim.approach(this.pressT, 0.0f, 10.0f);
        }

        protected void ripple(double mx, double my) {
            this.rippleX = mx;
            this.rippleY = my;
            this.rippleAt = Anim.seconds();
            this.pressT = 1.0f;
        }

        protected boolean hit(double mx, double my, float ox, float oy) {
            return Ui.inside(mx, my, ox + this.x - 6.0f, oy + this.y, this.w + 12.0f, this.h);
        }
    }

    public static class Card
    extends Widget {
        final List<Widget> rows = new ArrayList<Widget>();

        public Card add(Widget w) {
            this.rows.add(w);
            return this;
        }

        @Override
        public float layout(float x, float y, float w) {
            this.x = x;
            this.y = y;
            this.w = w;
            float cy = y + 2.0f;
            for (Widget r : this.rows) {
                cy += r.layout(x + 10.0f, cy, w - 20.0f);
            }
            this.h = cy - y + 2.0f;
            return this.h + 6.0f;
        }

        @Override
        protected float measure(float w) {
            return this.h;
        }

        @Override
        public void render(class_332 g, float ox, float oy, double mx, double my, float a) {
            Gfx.round(g, ox + this.x, oy + this.y, this.w, this.h, 9.0f, Col.alpha(-15001562, a));
            Gfx.roundOutline(g, ox + this.x, oy + this.y, this.w, this.h, 9.0f, 0.5f, Col.alpha(0x10FFFFFF, a));
            for (int i = 0; i < this.rows.size(); ++i) {
                Widget r = this.rows.get(i);
                r.render(g, ox, oy, mx, my, a);
                if (i >= this.rows.size() - 1) continue;
                Gfx.rect(g, ox + r.x, oy + r.y + r.h, r.w, 0.5f, Col.alpha(0x14FFFFFF, a));
            }
        }

        @Override
        public boolean click(double mx, double my, float ox, float oy) {
            for (Widget r : this.rows) {
                if (!r.click(mx, my, ox, oy)) continue;
                return true;
            }
            return false;
        }

        @Override
        public void drag(double mx, double my, float ox, float oy) {
            for (Widget r : this.rows) {
                r.drag(mx, my, ox, oy);
            }
        }

        @Override
        public void release() {
            for (Widget r : this.rows) {
                r.release();
            }
        }

        @Override
        public boolean key(class_11908 e) {
            for (Widget r : this.rows) {
                if (!r.key(e)) continue;
                return true;
            }
            return false;
        }

        @Override
        public boolean chr(class_11905 e) {
            for (Widget r : this.rows) {
                if (!r.chr(e)) continue;
                return true;
            }
            return false;
        }

        @Override
        public boolean capturesKeys() {
            for (Widget r : this.rows) {
                if (!r.capturesKeys()) continue;
                return true;
            }
            return false;
        }
    }

    public static class Spacer
    extends Widget {
        private final float size;

        public Spacer(float size) {
            this.size = size;
        }

        @Override
        protected float measure(float w) {
            return this.size;
        }

        @Override
        public void render(class_332 g, float ox, float oy, double mx, double my, float a) {
        }
    }

    public static class Section
    extends Widget {
        private final String key;

        public Section(String key) {
            this.key = key;
        }

        @Override
        protected float measure(float w) {
            return 18.0f;
        }

        @Override
        public void render(class_332 g, float ox, float oy, double mx, double my, float a) {
            String s = Ui.tr(this.key, new Object[0]).toUpperCase(Locale.ROOT);
            Gfx.text(g, s, FB, ox + this.x + 2.0f, oy + this.y + 8.0f, 6.4f, Col.alpha(-6515020, a));
        }
    }

    public static abstract class Widget {
        public float x;
        public float y;
        public float w;
        public float h;
        protected float hover;
        protected boolean wasHovered;

        public float layout(float x, float y, float w) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = this.measure(w);
            return this.h;
        }

        protected abstract float measure(float var1);

        public abstract void render(class_332 var1, float var2, float var3, double var4, double var6, float var8);

        protected boolean interactive() {
            return false;
        }

        protected void updateHover(boolean over) {
            if (this.interactive() && over && !this.wasHovered) {
                UiSounds.play(UiSounds.HOVER, 1.2f);
            }
            this.wasHovered = over;
            this.hover = Anim.approach(this.hover, over ? 1.0f : 0.0f, 14.0f);
        }

        public boolean click(double mx, double my, float ox, float oy) {
            return false;
        }

        public void drag(double mx, double my, float ox, float oy) {
        }

        public void release() {
        }

        public boolean key(class_11908 e) {
            return false;
        }

        public boolean chr(class_11905 e) {
            return false;
        }

        public boolean capturesKeys() {
            return false;
        }
    }
}


package dev.lyricsflow.render;
import net.minecraft.class_11909;

import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.config.LFConfig;
import dev.lyricsflow.gui.CoverArt;
import dev.lyricsflow.gui.HudEditorScreen;
import dev.lyricsflow.gui.Icons;
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
import java.util.ArrayDeque;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.class_1041;
import net.minecraft.class_1074;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3675;
import net.minecraft.class_408;
import net.minecraft.class_437;
import net.minecraft.class_9779;
import org.lwjgl.glfw.GLFW;

public final class ExtraHud {
    public static final int KEYS = 0;
    public static final int CPS = 1;
    public static final int PLAYER = 2;
    static final float[][] BOX = new float[3][4];
    private static final ArrayDeque<Long> LEFT = new ArrayDeque();
    private static final ArrayDeque<Long> RIGHT = new ArrayDeque();
    private static boolean wasL;
    private static boolean wasR;
    private static final Key[][] ROWS;
    private static final float[] KEY_ANIM;
    private static float mouseL;
    private static float mouseR;
    private static float barAnim;
    private static final float[] EQ;
    private static final float[] SEEK;
    private static final float[] VOL;
    private static final float[][] BTN;
    static int dragging;
    private static double grabX;
    private static double grabY;
    private static boolean volDrag;

    private ExtraHud() {
    }

    static boolean round() {
        return ConfigManager.get().hudRound;
    }

    static String font() {
        LFConfig c = ConfigManager.get();
        if (!c.hudRound) {
            return "minecraft";
        }
        return c.font == null || c.font.isEmpty() ? "righteous" : c.font;
    }

    static int color() {
        int v;
        LFConfig c = ConfigManager.get();
        int main = Themes.current().main() | 0xFF000000;
        if (c.hudColorMode == 1 && (v = CoverArt.vivid()) != 0) {
            return v | 0xFF000000;
        }
        return main;
    }

    static int bg() {
        return ExtraHud.round() ? -434760927 : -1879048192;
    }

    static float rad(float r) {
        return ExtraHud.round() ? r : 0.0f;
    }

    static int onColor(int c) {
        float l = (0.299f * (float)(c >> 16 & 0xFF) + 0.587f * (float)(c >> 8 & 0xFF) + 0.114f * (float)(c & 0xFF)) / 255.0f;
        return l > 0.6f ? -15593448 : -1;
    }

    static boolean chatOpen() {
        return class_310.method_1551().field_1755 instanceof class_408;
    }

    static void countClicks() {
        class_310 mc = class_310.method_1551();
        long w = mc.method_22683().method_4490();
        boolean l = mc.field_1755 == null && GLFW.glfwGetMouseButton((long)w, (int)0) == 1;
        boolean r = mc.field_1755 == null && GLFW.glfwGetMouseButton((long)w, (int)1) == 1;
        long now = System.currentTimeMillis();
        if (l && !wasL) {
            LEFT.addLast(now);
        }
        if (r && !wasR) {
            RIGHT.addLast(now);
        }
        wasL = l;
        wasR = r;
        while (!LEFT.isEmpty() && now - LEFT.peekFirst() > 1000L) {
            LEFT.pollFirst();
        }
        while (!RIGHT.isEmpty() && now - RIGHT.peekFirst() > 1000L) {
            RIGHT.pollFirst();
        }
    }

    public static void render(class_332 g, class_9779 tick) {
        class_310 mc = class_310.method_1551();
        LFConfig c = ConfigManager.get();
        if (!c.enabled || mc.field_1687 == null || mc.field_1724 == null || mc.field_1690.field_1842 || Hud.hideForScreenshot) {
            return;
        }
        if (!(c.hudKeys || c.hudCps || c.hudPlayer)) {
            return;
        }
        Anim.frame();
        ExtraHud.countClicks();
        double mx = mc.field_1729.method_68879(mc.method_22683());
        double my = mc.field_1729.method_68883(mc.method_22683());
        boolean edit = ExtraHud.chatOpen();
        if (c.hudKeys) {
            ExtraHud.keys(g, c, edit);
        }
        if (c.hudCps) {
            ExtraHud.cps(g, c);
        }
        if (c.hudPlayer) {
            ExtraHud.player(g, c, edit, mx, my);
        }
        if (edit) {
            for (int i = 0; i < 3; ++i) {
                if (!ExtraHud.on(c, i)) continue;
                float[] b = BOX[i];
                boolean over = ExtraHud.Gfx_inside(mx, my, b);
                Gfx.roundOutline(g, b[0] - 2.0f, b[1] - 2.0f, b[2] + 4.0f, b[3] + 4.0f, ExtraHud.rad(6.0f), 0.8f, Col.alpha(over || dragging == i ? ExtraHud.color() : 0x55FFFFFF, 1.0f));
            }
        }
    }

    public static void renderPreview(class_332 g, double mx, double my) {
        LFConfig c = ConfigManager.get();
        ExtraHud.countClicks();
        ExtraHud.keys(g, c, true);
        ExtraHud.cps(g, c);
        ExtraHud.player(g, c, true, mx, my);
    }

    static boolean on(LFConfig c, int i) {
        return i == 0 ? c.hudKeys : (i == 1 ? c.hudCps : c.hudPlayer);
    }

    static boolean Gfx_inside(double mx, double my, float[] b) {
        return mx >= (double)b[0] && mx < (double)(b[0] + b[2]) && my >= (double)b[1] && my < (double)(b[1] + b[3]);
    }

    private static float place(float frac, float def, float size, float screen) {
        float v = frac < 0.0f ? def : frac * screen;
        return Math.max(1.0f, Math.min(screen - size - 1.0f, v));
    }

    static void keys(class_332 g, LFConfig c, boolean preview) {
        class_310 mc = class_310.method_1551();
        float s = c.keysScale;
        float u = 13.0f * s;
        float gap = 2.0f * s;
        float kbW = 0.0f;
        for (Key[] row : ROWS) {
            float w = 0.0f;
            for (Key k : row) {
                w += k.w * u + gap;
            }
            kbW = Math.max(kbW, w - gap);
        }
        float H = (float)ROWS.length * (u + gap) - gap;
        float mouseW = 19.0f * s;
        float mouseH = 3.0f * (u + gap);
        float W = kbW + 6.0f * s + mouseW;
        float x0 = ExtraHud.place(c.keysX, 4.0f, W, g.method_51421());
        float y0 = ExtraHud.place(c.keysY, 16.0f, H, g.method_51443());
        ExtraHud.BOX[0] = new float[]{x0, y0, W, H};
        long win = mc.method_22683().method_4490();
        boolean playing = mc.field_1755 == null || preview;
        int col = ExtraHud.color();
        String f = ExtraHud.font();
        int idx = 0;
        float y = y0;
        for (Key[] row : ROWS) {
            float x = x0;
            for (Key k : row) {
                float w = k.w * u + (k.w - 1.0f) * 0.0f;
                boolean down = playing && class_3675.method_15987((class_1041)mc.method_22683(), (int)k.code);
                ExtraHud.KEY_ANIM[idx] = Anim.approach(KEY_ANIM[idx], down ? 1.0f : 0.0f, down ? 30.0f : 10.0f);
                float p = KEY_ANIM[idx];
                int fill = Col.lerp(ExtraHud.bg(), col, p);
                Gfx.round(g, x, y, w, u, ExtraHud.rad(3.0f * s), Col.alpha(fill, 1.0f));
                if (ExtraHud.round()) {
                    Gfx.roundOutline(g, x, y, w, u, ExtraHud.rad(3.0f * s), 0.5f, Col.alpha(0x30FFFFFF, 1.0f - p));
                }
                if (!k.label.isEmpty()) {
                    float ts = (k.label.length() > 1 ? 5.4f : 7.0f) * s;
                    int tc = Col.lerp(-1514251, ExtraHud.onColor(col), p);
                    Gfx.textCentered(g, k.label, f, x + w / 2.0f, y + (u - ts) / 2.0f + (f.equals("minecraft") ? 0.6f * s : 0.0f), ts, tc);
                }
                x += w + gap;
                ++idx;
            }
            y += u + gap;
        }
        float mx0 = x0 + kbW + 6.0f * s;
        float my0 = y0 + (H - mouseH) / 2.0f;
        boolean l = playing && GLFW.glfwGetMouseButton((long)win, (int)0) == 1;
        boolean r = playing && GLFW.glfwGetMouseButton((long)win, (int)1) == 1;
        mouseL = Anim.approach(mouseL, l ? 1.0f : 0.0f, l ? 30.0f : 10.0f);
        mouseR = Anim.approach(mouseR, r ? 1.0f : 0.0f, r ? 30.0f : 10.0f);
        float mr = ExtraHud.rad(mouseW / 2.0f);
        Gfx.round(g, mx0, my0, mouseW, mouseH, mr, ExtraHud.bg());
        float bh = mouseH * 0.42f;
        int sx0 = (int)Math.floor(mx0);
        int sy0 = (int)Math.floor(my0);
        g.method_44379(sx0, sy0, (int)Math.ceil(mx0 + mouseW / 2.0f - 0.6f * s), (int)Math.ceil(my0 + bh));
        Gfx.round(g, mx0, my0, mouseW, mouseH, mr, Col.alpha(col, mouseL));
        g.method_44380();
        g.method_44379((int)Math.floor(mx0 + mouseW / 2.0f + 0.6f * s), sy0, (int)Math.ceil(mx0 + mouseW), (int)Math.ceil(my0 + bh));
        Gfx.round(g, mx0, my0, mouseW, mouseH, mr, Col.alpha(col, mouseR));
        g.method_44380();
        Gfx.rect(g, mx0 + mouseW / 2.0f - 0.5f * s, my0 + 2.0f * s, 1.0f * s, bh - 2.0f * s, 0x40FFFFFF);
        Gfx.rect(g, mx0 + 3.0f * s, my0 + bh, mouseW - 6.0f * s, 0.8f * s, 0x40FFFFFF);
        Gfx.round(g, mx0 + mouseW / 2.0f - 1.8f * s, my0 + bh * 0.3f, 3.6f * s, bh * 0.4f, ExtraHud.rad(1.8f * s), -13883584);
        if (ExtraHud.round()) {
            Gfx.roundOutline(g, mx0, my0, mouseW, mouseH, mr, 0.6f, 0x40FFFFFF);
        }
    }

    static void cps(class_332 g, LFConfig c) {
        float s = c.cpsScale;
        String f = ExtraHud.font();
        float ts = 8.0f * s;
        String ls = Integer.toString(LEFT.size());
        String rs = Integer.toString(RIGHT.size());
        String mid = "  |  ";
        String tail = "  CPS";
        float w = Gfx.textWidth(ls + mid + rs + tail, f, ts) + 14.0f * s;
        float h = 15.0f * s;
        float x = ExtraHud.place(c.cpsX, ((float)g.method_51421() - w) / 2.0f, w, g.method_51421());
        float y = ExtraHud.place(c.cpsY, (float)g.method_51443() - 74.0f, h, g.method_51443());
        ExtraHud.BOX[1] = new float[]{x, y, w, h};
        Gfx.round(g, x, y, w, h, ExtraHud.rad(h / 2.0f), ExtraHud.bg());
        if (ExtraHud.round()) {
            Gfx.roundOutline(g, x, y, w, h, ExtraHud.rad(h / 2.0f), 0.5f, 0x40FFFFFF);
        }
        int col = ExtraHud.color();
        float tx = x + 7.0f * s;
        float ty = y + (h - ts) / 2.0f + (f.equals("minecraft") ? 0.8f * s : 0.0f);
        tx += Gfx.text(g, ls, f, tx, ty, ts, -1);
        tx += Gfx.text(g, mid, f, tx, ty, ts, -7699544);
        tx += Gfx.text(g, rs, f, tx, ty, ts, -1);
        Gfx.text(g, tail, f, tx, ty, ts, col);
    }

    static void player(class_332 g, LFConfig c, boolean edit, double mx, double my) {
        MediaManager mm = MediaManager.get();
        Track t = mm.track();
        float s = c.playerScale;
        String f = ExtraHud.font();
        boolean mcf = f.equals("minecraft");
        float W = 190.0f * s;
        float H1 = 56.0f * s;
        Lyrics lyr = mm.lyrics();
        long pos = mm.position();
        boolean lines = c.hudPlayerLyrics && lyr != null && c.lyricsOn;
        float lineH = 11.0f * s;
        float H2 = lines ? lineH * 3.0f + 10.0f * s : 0.0f;
        float H3 = edit ? 20.0f * s : 0.0f;
        float H = H1 + (lines ? 4.0f * s + H2 : 0.0f) + (edit ? 4.0f * s + H3 : 0.0f);
        float x = ExtraHud.place(c.playerX, (float)g.method_51421() - W - 4.0f, W, g.method_51421());
        float y = ExtraHud.place(c.playerY, 4.0f, H, g.method_51443());
        ExtraHud.BOX[2] = new float[]{x, y, W, H};
        int col = ExtraHud.color();
        float r = ExtraHud.rad(6.0f * s);
        Gfx.round(g, x, y, W, H1, r, ExtraHud.bg());
        if (ExtraHud.round()) {
            Gfx.roundOutline(g, x, y, W, H1, r, 0.5f, 0x40FFFFFF);
        }
        Gfx.rect(g, x, y + (ExtraHud.round() ? r * 0.6f : 0.0f), 2.0f * s, H1 - (ExtraHud.round() ? r * 1.2f : 0.0f), col);
        float cs = 42.0f * s;
        float cx = x + 8.0f * s;
        float cy = y + (H1 - cs) / 2.0f;
        if (t != null && CoverArt.ready()) {
            CoverArt.draw(g, cx, cy, cs, 1.0f);
        } else {
            Gfx.round(g, cx, cy, cs, cs, ExtraHud.rad(4.0f * s), -13883584);
            Icons.draw(g, "music", cx + cs / 2.0f, cy + cs / 2.0f, cs * 0.4f, -7699544);
        }
        float tx = cx + cs + 8.0f * s;
        float tw = x + W - 8.0f * s - tx;
        String title = t != null ? t.title : ExtraHud.Ui_tr("lyricsflow.hud.nothing");
        String artist = t != null ? t.artist : "SkyLyrics";
        Gfx.text(g, Gfx.ellipsize(title, f, 8.4f * s, tw - 16.0f * s), f, tx, cy + 1.0f * s + (mcf ? 0.6f * s : 0.0f), 8.4f * s, -1);
        Gfx.text(g, Gfx.ellipsize(artist, f, 7.0f * s, tw * 0.6f), f, tx, cy + 14.0f * s + (mcf ? 0.6f * s : 0.0f), 7.0f * s, -6646859);
        long dur = t != null ? t.durationMs : 0L;
        long p = Math.max(0L, Math.min(dur, pos));
        String time = ExtraHud.fmt(p) + " / " + (dur > 0L ? ExtraHud.fmt(dur) : "--:--");
        float timeW = Gfx.textWidth(time, f, 6.6f * s);
        Gfx.text(g, time, f, x + W - 8.0f * s - timeW, cy + 14.4f * s + (mcf ? 0.6f * s : 0.0f), 6.6f * s, -7699544);
        boolean playing = t != null && t.playing;
        double now = Anim.seconds();
        for (int i = 0; i < 3; ++i) {
            float target = playing ? 0.3f + 0.7f * (float)Math.abs(Math.sin(now * (3.3 + (double)i * 1.9) + (double)i)) : 0.2f;
            ExtraHud.EQ[i] = Anim.approach(EQ[i], target, 12.0f);
            float bh = Math.max(1.5f * s, EQ[i] * 7.0f * s);
            Gfx.rect(g, x + W - 15.0f * s + (float)i * 3.0f * s, cy + 8.0f * s - bh, 2.0f * s, bh, col);
        }
        float px0 = tx;
        float pw = x + W - 8.0f * s - tx;
        float py = cy + cs - 5.0f * s;
        float frac = dur > 0L ? (float)p / (float)dur : 0.0f;
        barAnim = Anim.approach(barAnim, frac, 10.0f);
        Gfx.round(g, px0, py, pw, 2.4f * s, ExtraHud.rad(1.2f * s), 0x40FFFFFF);
        Gfx.round(g, px0, py, pw * barAnim, 2.4f * s, ExtraHud.rad(1.2f * s), col);
        Gfx.round(g, px0 + pw * barAnim - 2.0f * s, py - 0.8f * s, 4.0f * s, 4.0f * s, ExtraHud.rad(2.0f * s), col);
        ExtraHud.SEEK[0] = px0;
        ExtraHud.SEEK[1] = py - 4.0f * s;
        ExtraHud.SEEK[2] = pw;
        ExtraHud.SEEK[3] = 10.0f * s;
        float yy = y + H1 + 4.0f * s;
        if (lines) {
            Gfx.round(g, x, yy, W, H2, r, ExtraHud.bg());
            if (ExtraHud.round()) {
                Gfx.roundOutline(g, x, yy, W, H2, r, 0.5f, 0x40FFFFFF);
            }
            Gfx.rect(g, x, yy + (ExtraHud.round() ? r * 0.6f : 0.0f), 2.0f * s, H2 - (ExtraHud.round() ? r * 1.2f : 0.0f), col);
            Lyrics.Line cur = lyr.lineAt(pos);
            if (cur == null) {
                cur = lyr.lastStartedBefore(pos);
            }
            int ci = cur != null ? cur.index : -1;
            for (int k = 0; k < 3; ++k) {
                Lyrics.Line ln = ExtraHud.lineAt(lyr, ci + k);
                if (ln == null) continue;
                String text = Lyrics.show(ln.text);
                float ts = 7.2f * s;
                Gfx.text(g, Gfx.ellipsize(text, f, ts, W - 16.0f * s), f, x + 8.0f * s, yy + 5.0f * s + (float)k * lineH + (mcf ? 0.6f * s : 0.0f), ts, k == 0 ? col : -7699544);
            }
            yy += H2 + 4.0f * s;
        }
        if (edit) {
            Gfx.round(g, x, yy, W, H3, r, ExtraHud.bg());
            if (ExtraHud.round()) {
                Gfx.roundOutline(g, x, yy, W, H3, r, 0.5f, 0x40FFFFFF);
            }
            String[] icons = new String[]{"prev", playing ? "pause" : "play", "next", mm.repeat() == 1 ? "repeat_one" : "repeat"};
            for (int i = 0; i < 4; ++i) {
                boolean hot;
                float bx = x + 8.0f * s + (float)i * 18.0f * s;
                float by = yy + 2.0f * s;
                float bw = 16.0f * s;
                boolean over = mx >= (double)bx && mx < (double)(bx + bw) && my >= (double)by && my < (double)(by + bw);
                boolean bl = hot = i == 1 || i == 3 && mm.repeat() != 0;
                if (over || i == 1) {
                    Gfx.round(g, bx, by, bw, bw, ExtraHud.rad(8.0f * s), i == 1 ? col : 0x30FFFFFF);
                }
                int ic = i == 1 ? ExtraHud.onColor(col) : (hot ? -14756000 : -1514251);
                Icons.draw(g, icons[i], bx + bw / 2.0f, by + bw / 2.0f, 7.0f * s, ic);
                ExtraHud.BTN[i][0] = bx;
                ExtraHud.BTN[i][1] = by;
                ExtraHud.BTN[i][2] = bw;
                ExtraHud.BTN[i][3] = bw;
            }
            float vx = x + 8.0f * s + 72.0f * s + 6.0f * s;
            float vw = x + W - 10.0f * s - vx;
            float vy = yy + H3 / 2.0f - 1.2f * s;
            Icons.draw(g, "volume", vx + 4.0f * s, vy + 1.2f * s, 6.0f * s, -7699544);
            float vol = AppVolume.get();
            Gfx.round(g, vx += 11.0f * s, vy, vw -= 11.0f * s, 2.4f * s, ExtraHud.rad(1.2f * s), 0x40FFFFFF);
            if (vol >= 0.0f) {
                Gfx.round(g, vx, vy, vw * vol, 2.4f * s, ExtraHud.rad(1.2f * s), col);
            }
            ExtraHud.VOL[0] = vx;
            ExtraHud.VOL[1] = vy - 5.0f * s;
            ExtraHud.VOL[2] = vw;
            ExtraHud.VOL[3] = 12.0f * s;
        }
    }

    private static Lyrics.Line lineAt(Lyrics l, int i) {
        if (i < 0 || l.lines == null || i >= l.lines.size()) {
            return null;
        }
        return l.lines.get(i);
    }

    private static String fmt(long ms) {
        long sec = Math.max(0L, ms / 1000L);
        return sec / 60L + ":" + String.format("%02d", sec % 60L);
    }

    private static String Ui_tr(String k) {
        return class_1074.method_4662((String)k, (Object[])new Object[0]);
    }

    public static boolean press(double mx, double my, int button) {
        LFConfig c = ConfigManager.get();
        if (!c.enabled || button != 0) {
            return false;
        }
        MediaManager mm = MediaManager.get();
        if (c.hudPlayer) {
            for (int i = 0; i < 4; ++i) {
                if (!ExtraHud.Gfx_inside(mx, my, BTN[i])) continue;
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
                    default: {
                        mm.cycleRepeat();
                    }
                }
                UiSounds.play(UiSounds.CLICK, 1.0f);
                return true;
            }
            Track t = mm.track();
            if (t != null && t.durationMs > 0L && ExtraHud.Gfx_inside(mx, my, SEEK)) {
                mm.seek((long)(Math.max(0.0, Math.min(1.0, (mx - (double)SEEK[0]) / (double)SEEK[2])) * (double)t.durationMs));
                return true;
            }
            if (ExtraHud.Gfx_inside(mx, my, VOL)) {
                volDrag = true;
                AppVolume.set((float)Math.max(0.0, Math.min(1.0, (mx - (double)VOL[0]) / (double)VOL[2])));
                return true;
            }
        }
        for (int i = 2; i >= 0; --i) {
            if (!ExtraHud.on(c, i) && !(class_310.method_1551().field_1755 instanceof HudEditorScreen) || !ExtraHud.Gfx_inside(mx, my, BOX[i])) continue;
            dragging = i;
            grabX = mx - (double)BOX[i][0];
            grabY = my - (double)BOX[i][1];
            return true;
        }
        return false;
    }

    public static boolean drag(double mx, double my) {
        if (volDrag) {
            AppVolume.set((float)Math.max(0.0, Math.min(1.0, (mx - (double)VOL[0]) / (double)VOL[2])));
            return true;
        }
        if (dragging < 0) {
            return false;
        }
        class_310 mc = class_310.method_1551();
        float sw = mc.method_22683().method_4486();
        float sh = mc.method_22683().method_4502();
        float[] b = BOX[dragging];
        double nx = mx - grabX;
        double ny = my - grabY;
        if (Math.abs(nx + (double)(b[2] / 2.0f) - (double)(sw / 2.0f)) < 5.0) {
            nx = sw / 2.0f - b[2] / 2.0f;
        }
        float fx = (float)Math.max(0.0, Math.min(1.0, nx / (double)sw));
        float fy = (float)Math.max(0.0, Math.min(1.0, ny / (double)sh));
        LFConfig c = ConfigManager.get();
        switch (dragging) {
            case 0: {
                c.keysX = fx;
                c.keysY = fy;
                break;
            }
            case 1: {
                c.cpsX = fx;
                c.cpsY = fy;
                break;
            }
            default: {
                c.playerX = fx;
                c.playerY = fy;
            }
        }
        return true;
    }

    public static boolean release() {
        boolean was = dragging >= 0 || volDrag;
        dragging = -1;
        volDrag = false;
        if (was) {
            ConfigManager.save();
        }
        return was;
    }

    public static boolean scroll(double mx, double my, double v) {
        LFConfig c = ConfigManager.get();
        for (int i = 2; i >= 0; --i) {
            if (!ExtraHud.on(c, i) && !(class_310.method_1551().field_1755 instanceof HudEditorScreen) || !ExtraHud.Gfx_inside(mx, my, BOX[i])) continue;
            float d = (float)v * 0.05f;
            switch (i) {
                case 0: {
                    c.keysScale = Math.max(0.5f, Math.min(2.5f, c.keysScale + d));
                    break;
                }
                case 1: {
                    c.cpsScale = Math.max(0.5f, Math.min(2.5f, c.cpsScale + d));
                    break;
                }
                default: {
                    c.playerScale = Math.max(0.5f, Math.min(2.0f, c.playerScale + d));
                }
            }
            ConfigManager.save();
            return true;
        }
        return false;
    }

    public static void resetPositions() {
        LFConfig c = ConfigManager.get();
        LFConfig d = new LFConfig();
        c.keysX = d.keysX;
        c.keysY = d.keysY;
        c.cpsX = d.cpsX;
        c.cpsY = d.cpsY;
        c.playerX = d.playerX;
        c.playerY = d.playerY;
        c.keysScale = d.keysScale;
        c.cpsScale = d.cpsScale;
        c.playerScale = d.playerScale;
    }

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (!(screen instanceof class_408)) {
                return;
            }
            ScreenMouseEvents.allowMouseClick((class_437)screen).register((sc, e) -> !ExtraHud.press(e.comp_4798(), e.comp_4799(), e.method_74245()));
            ScreenMouseEvents.allowMouseDrag((class_437)screen).register((sc, e, dx, dy) -> !ExtraHud.drag(e.comp_4798(), e.comp_4799()));
            ScreenMouseEvents.allowMouseRelease((class_437)screen).register((sc, e) -> !ExtraHud.release());
            ScreenMouseEvents.allowMouseScroll((class_437)screen).register((sc, x, y, hh, v) -> !ExtraHud.scroll(x, y, v));
        });
    }

    static {
        ROWS = new Key[][]{{new Key("Esc", 256, 1.0f), new Key("1", 49, 1.0f), new Key("2", 50, 1.0f), new Key("3", 51, 1.0f), new Key("4", 52, 1.0f), new Key("5", 53, 1.0f)}, {new Key("Tab", 258, 1.45f), new Key("Q", 81, 1.0f), new Key("W", 87, 1.0f), new Key("E", 69, 1.0f), new Key("R", 82, 1.0f), new Key("T", 84, 1.0f)}, {new Key("Caps", 280, 1.7f), new Key("A", 65, 1.0f), new Key("S", 83, 1.0f), new Key("D", 68, 1.0f), new Key("F", 70, 1.0f), new Key("G", 71, 1.0f)}, {new Key("Shift", 340, 2.2f), new Key("Z", 90, 1.0f), new Key("X", 88, 1.0f), new Key("C", 67, 1.0f), new Key("V", 86, 1.0f)}, {new Key("Ctrl", 341, 1.45f), new Key("Alt", 342, 1.45f), new Key("", 32, 3.85f)}};
        KEY_ANIM = new float[64];
        EQ = new float[3];
        SEEK = new float[4];
        VOL = new float[4];
        BTN = new float[4][4];
        dragging = -1;
    }

    private record Key(String label, int code, float w) {
    }
}


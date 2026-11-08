package dev.lyricsflow.app.cosmetic;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.lyricsflow.LyricsFlow;
import dev.lyricsflow.app.Core;
import dev.lyricsflow.app.config.Config;
import dev.lyricsflow.app.cosmetic.PhoneMenu;
import dev.lyricsflow.app.hud.Gfx;
import dev.lyricsflow.app.hud.SkyThumbs;
import dev.lyricsflow.app.lyrics.NowPlaying;
import dev.lyricsflow.app.lyrics.SpotifyClient;
import dev.lyricsflow.app.sky.SkyImage;
import dev.lyricsflow.app.sky.SkyPreset;
import dev.lyricsflow.app.util.Fx;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.config.LFConfig;
import dev.lyricsflow.gui.CoverArt;
import dev.lyricsflow.gui.LyricsFlowScreen;
import dev.lyricsflow.media.MediaManager;
import dev.lyricsflow.media.Track;
import dev.lyricsflow.render.Cosmetics;
import dev.lyricsflow.render.Fonts;
import dev.lyricsflow.render.Themes;
import dev.lyricsflow.render.WorldLyrics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import net.minecraft.class_10799;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_12247;
import net.minecraft.class_12249;
import net.minecraft.class_1921;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_4184;
import net.minecraft.class_437;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

final class WorldPanel {
    static final int W = 380;
    static final int H = 222;
    static final float K = 0.0105f;
    private static final float RCX = 98.0f;
    private static final float RCY = 104.0f;
    private static final float RR = 78.0f;
    private static final float BX = 62.0f;
    private static final float CX0 = 206.0f;
    private static final float CX1 = 366.0f;
    private static final int ACCENT = 10321407;
    private static final int ACCENT2 = 0xDCCFFF;
    private static final int DEEP = 5914576;
    private static final int TOP = 1709620;
    private static final int BOTTOM = 723479;
    private static final int LIGHT = 0xF000F0;
    private static class_1921 flat;
    private final class_243 pos;
    private final float rx;
    private final float rz;
    private final float nx;
    private final float nz;
    private final List<Hit> hits = new ArrayList<Hit>();
    private final Map<String, Float> hov = new HashMap<String, Float>();
    private int page;
    private int skyPage;
    private int hatPage;
    private int deathPage;
    private final long born = System.currentTimeMillis();
    private long closing = -1L;
    private long pageTime = 0L;
    private long lastNs = System.nanoTime();
    private float dt;
    private float spin;
    private boolean hovered;
    private boolean sndLand;
    private boolean sndClose;
    private float hx;
    private float hy;
    private static final String[] TABS;
    private Matrix4f m;
    private float ox;
    private float oy;
    private class_4588 q;
    private final List<Runnable> late = new ArrayList<Runnable>();
    private int fontPage;

    private static void wear(String id) {
        int i;
        LFConfig c = WorldPanel.lf();
        if ("none".equals(id)) {
            for (int i2 = 0; i2 < 5; ++i2) {
                c.setSlot(i2, "none");
            }
            return;
        }
        for (i = 0; i < 5; ++i) {
            if (!id.equals(c.slot(i))) continue;
            c.setSlot(i, "none");
            return;
        }
        for (i = 0; i < 5; ++i) {
            if (!"none".equals(c.slot(i))) continue;
            c.setSlot(i, id);
            return;
        }
        c.setSlot(4, id);
    }

    private static class_1921 flat() {
        if (flat == null) {
            flat = dev.lyricsflow.mixin.RenderLayerInvoker.invokeCreate("lyricsflow_panel", (class_12247)class_12247.method_75927((RenderPipeline)class_10799.field_56865).method_75938());
        }
        return flat;
    }

    private static String tr(String key) {
        return class_2561.method_43471((String)("lyricsflow.wp." + key)).getString();
    }

    WorldPanel(class_310 mc) {
        class_4184 cam = mc.field_1773.method_19418();
        Vector3fc f = cam.method_19335();
        double fl = Math.sqrt(f.x() * f.x() + f.z() * f.z());
        float fx = fl < 1.0E-4 ? 0.0f : (float)((double)f.x() / fl);
        float fz = fl < 1.0E-4 ? 1.0f : (float)((double)f.z() / fl);
        class_243 c = cam.method_71156();
        this.pos = new class_243(c.field_1352 + (double)fx * 3.2, c.field_1351 - 0.05, c.field_1350 + (double)fz * 3.2);
        this.nx = -fx;
        this.nz = -fz;
        this.rx = -fz;
        this.rz = fx;
        WorldPanel.play(mc, class_3417.field_26980, 1.3f, 0.8f);
    }

    class_243 pos() {
        return this.pos;
    }

    boolean hovered() {
        return this.hovered;
    }

    boolean closing() {
        return this.closing >= 0L;
    }

    void close() {
        if (this.closing < 0L) {
            this.closing = System.currentTimeMillis();
        }
    }

    boolean finished() {
        return this.closing >= 0L && System.currentTimeMillis() - this.closing > 900L;
    }

    private float age() {
        return System.currentTimeMillis() - this.born;
    }

    private float closeAge() {
        return this.closing < 0L ? -1.0f : (float)(System.currentTimeMillis() - this.closing);
    }

    void tick(class_310 mc) {
        if (!this.sndLand && this.age() > 860.0f) {
            this.sndLand = true;
            WorldPanel.play(mc, class_3417.field_14891, 1.8f, 0.3f);
        }
        if (!this.sndClose && this.closing >= 0L && this.closeAge() > 360.0f) {
            this.sndClose = true;
            WorldPanel.play(mc, class_3417.field_26980, 1.0f, 0.7f);
        }
    }

    private static void play(class_310 mc, class_3414 ev, float pitch, float vol) {
        try {
            mc.method_1483().method_4873((class_1113)class_1109.method_4757((class_3414)ev, (float)pitch, (float)vol));
        }
        catch (Throwable throwable) {
        }
    }

    private void aim(class_4184 cam) {
        this.hovered = false;
        if (!(this.age() < 1200.0f) && this.closing < 0L) {
            double t;
            class_243 o = cam.method_71156();
            Vector3fc d = cam.method_19335();
            double denom = d.x() * this.nx + d.z() * this.nz;
            if (!(denom > -1.0E-4 || (t = ((this.pos.field_1352 - o.field_1352) * (double)this.nx + (this.pos.field_1350 - o.field_1350) * (double)this.nz) / denom) <= 0.0 || t > 14.0)) {
                double px = o.field_1352 + (double)d.x() * t - this.pos.field_1352;
                double py = o.field_1351 + (double)d.y() * t - this.pos.field_1351;
                double pz = o.field_1350 + (double)d.z() * t - this.pos.field_1350;
                float u = (float)((px * (double)this.rx + pz * (double)this.rz) / (double)0.0105f) + 190.0f;
                float v = 111.0f - (float)(py / (double)0.0105f);
                if (!(u < 14.0f || v < 0.0f || u > 380.0f || v > 222.0f)) {
                    this.hovered = true;
                    this.hx = u;
                    this.hy = v;
                }
            }
        }
    }

    private boolean over(float x, float y, float w, float h) {
        return this.hovered && this.hx >= x && this.hy >= y && this.hx < x + w && this.hy < y + h;
    }

    private float hv(String id, boolean on) {
        float v = this.hov.getOrDefault(id, Float.valueOf(0.0f)).floatValue();
        v += ((on ? 1.0f : 0.0f) - v) * Math.min(1.0f, this.dt * 14.0f);
        this.hov.put(id, Float.valueOf(v));
        return v;
    }

    void click() {
        if (this.hovered && this.closing < 0L) {
            for (int i = this.hits.size() - 1; i >= 0; --i) {
                Hit h = this.hits.get(i);
                if (!(this.hx >= h.x()) || !(this.hy >= h.y()) || !(this.hx < h.x() + h.w()) || !(this.hy < h.y() + h.h())) continue;
                Fx.sound(Fx.Sfx.CLICK);
                h.run().run();
                Core.saveConfig();
                ConfigManager.save();
                return;
            }
        }
    }

    private void go(int p) {
        if (p != this.page) {
            this.page = p;
            this.pageTime = System.currentTimeMillis();
        }
    }

    void render(class_310 mc, class_4597 buffers, Matrix4f base) {
        float arm;
        float content;
        float slide;
        float fly;
        class_4184 cam = mc.field_1773.method_19418();
        this.aim(cam);
        long ns = System.nanoTime();
        this.dt = Math.min(0.1f, (float)(ns - this.lastNs) / 1.0E9f);
        this.lastNs = ns;
        float t = this.age();
        float c = this.closeAge();
        if (c < 0.0f) {
            fly = WorldPanel.inOut(WorldPanel.clamp(t / 900.0f));
            slide = WorldPanel.outCubic(WorldPanel.clamp((t - 820.0f) / 460.0f));
            content = WorldPanel.outCubic(WorldPanel.clamp((t - 1100.0f) / 300.0f));
            arm = WorldPanel.inOut(WorldPanel.clamp((t - 1150.0f) / 400.0f));
        } else {
            arm = 1.0f - WorldPanel.inOut(WorldPanel.clamp(c / 220.0f));
            content = 1.0f - WorldPanel.clamp(c / 160.0f);
            slide = 1.0f - WorldPanel.inOut(WorldPanel.clamp((c - 120.0f) / 300.0f));
            fly = 1.0f - WorldPanel.inOut(WorldPanel.clamp((c - 380.0f) / 480.0f));
        }
        float w = 3.5f + 16.5f * (float)Math.exp(-t / 420.0f) + (c >= 0.0f ? 10.0f * WorldPanel.clamp(c / 600.0f) : 0.0f);
        this.spin += w * this.dt;
        class_243 cp = cam.method_71156();
        float bob = (float)Math.sin((double)System.currentTimeMillis() / 1200.0) * 0.012f * slide;
        Matrix4f rot = new Matrix4f(this.rx, 0.0f, this.rz, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, this.nx, 0.0f, this.nz, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
        Matrix4f panel = new Matrix4f((Matrix4fc)base).translate((float)(this.pos.field_1352 - cp.field_1352), (float)(this.pos.field_1351 - cp.field_1351) + bob, (float)(this.pos.field_1350 - cp.field_1350)).mul((Matrix4fc)rot);
        this.q = buffers.method_73477(WorldPanel.flat());
        this.hits.clear();
        this.late.clear();
        Config cf = Core.cfg();
        NowPlaying np = SpotifyClient.now();
        if (slide > 0.01f) {
            float anchor = -0.96599996f;
            this.m = new Matrix4f((Matrix4fc)panel).translate(anchor, 0.0f, 0.0f).scale(slide, 1.0f, 1.0f).translate(-anchor, 0.0f, 0.0f);
            this.ox = 190.0f;
            this.oy = 111.0f;
            float a = Math.min(1.0f, slide * 1.5f);
            for (int i = 4; i >= 1; --i) {
                this.round(62.0f - (float)i * 2.5f, (float)(-i) * 2.5f, 318.0f + (float)i * 5.0f, 222.0f + (float)i * 5.0f, 19.0f + (float)i * 2.5f, 10321407, 10321407, 0.05f * a, 0.0f);
            }
            this.round(61.2f, -0.8f, 319.6f, 223.6f, 19.8f, 12167664, 7231688, 0.35f * a, 0.0f);
            this.round(62.0f, 0.0f, 318.0f, 222.0f, 19.0f, 1709620, 723479, 0.96f * a, 0.0f);
            this.round(150.0f, 0.6f, 144.0f, 1.6f, 0.8f, 10321407, 0xDCCFFF, 0.7f * a, 0.0f);
            if (content > 0.01f) {
                this.body(mc, cf, np, content);
            }
        }
        float recX = -0.96599996f;
        float recY = 0.0735f;
        Vector3f end = new Vector3f(this.rx * recX, recY + bob, this.rz * recX).add((float)(this.pos.field_1352 - cp.field_1352), (float)(this.pos.field_1351 - cp.field_1351), (float)(this.pos.field_1350 - cp.field_1350));
        Vector3fc f = cam.method_19335();
        Vector3fc u = cam.method_19336();
        Vector3f start = new Vector3f(f).mul(0.9f).add((Vector3fc)new Vector3f(u).mul(-0.3f));
        Vector3f p = new Vector3f((Vector3fc)start).lerp((Vector3fc)end, fly).add(0.0f, (float)Math.sin(Math.PI * (double)fly) * 0.45f, 0.0f);
        float scale = 0.12f + 0.88f * WorldPanel.outCubic(fly);
        float tilt = (1.0f - fly) * 70.0f;
        float ra = Math.min(1.0f, (c < 0.0f ? t : 900.0f - c) / 150.0f);
        if (ra > 0.01f) {
            this.m = new Matrix4f((Matrix4fc)base).translate((Vector3fc)p).mul((Matrix4fc)rot).rotateX((float)Math.toRadians(-tilt)).scale(scale).translate(0.0f, 0.0f, 0.006f);
            this.ox = 0.0f;
            this.oy = 0.0f;
            this.record(np, ra, arm);
        }
        if (this.hovered) {
            this.m = new Matrix4f((Matrix4fc)panel);
            this.ox = 190.0f;
            this.oy = 111.0f;
            this.ring(this.hx, this.hy, 0.0f, 2.2f, 0.0f, 360.0f, 0xFFFFFF, 0.95f, 0xFFFFFF, 0.95f, 0.02f, 12);
            this.ring(this.hx, this.hy, 2.2f, 5.5f, 0.0f, 360.0f, 10321407, 0.35f, 10321407, 0.0f, 0.02f, 12);
        }
        if (buffers instanceof class_4597.class_4598) {
            class_4597.class_4598 bs = (class_4597.class_4598)buffers;
            bs.method_22994(WorldPanel.flat());
        }
        for (Runnable r : this.late) {
            r.run();
        }
    }

    private void record(NowPlaying np, float a, float arm) {
        Track tr;
        this.round(-84.8f, -104.8f, 169.6f, 223.6f, 19.8f, 12167664, 7231688, 0.35f * a, 5.0E-4f);
        this.round(-84.0f, -104.0f, 168.0f, 222.0f, 19.0f, 1709620, 723479, 0.97f * a, 0.001f);
        float cs = 124.0f;
        float cx0 = -cs / 2.0f;
        float cy0 = -86.0f;
        if (np.hasTrack() && CoverArt.ready()) {
            this.round(cx0 - 1.5f, cy0 - 1.5f, cs + 3.0f, cs + 3.0f, 9.0f, 10321407, 7231688, 0.35f * a, 0.002f);
            this.image(CoverArt.id(), cx0, cy0, cs, cs, a);
        } else {
            this.round(cx0, cy0, cs, cs, 10.0f, 7231688, 2894902, a, 0.002f);
            this.textAt(this.m, "\u266a", 0.0f, cy0 + cs / 2.0f - 14.0f, 3.0f, 0xFFFFFF, 0.8f * a, true);
        }
        String title = np.hasTrack() ? np.title : WorldPanel.tr("nomusic");
        String artist = np.hasTrack() ? np.artist : "";
        this.textAt(this.m, WorldPanel.ellip(title, 150.0f, 0.85f), 0.0f, 46.0f, 0.85f, 0xFFFFFF, a, true);
        if (!artist.isEmpty()) {
            this.textAt(this.m, WorldPanel.ellip(artist, 150.0f, 0.62f), 0.0f, 59.0f, 0.62f, 0xC5C5C5, 0.85f * a, true);
        }
        float bw = 136.0f;
        float bx = -bw / 2.0f;
        float byy = 70.0f;
        float frac = 0.0f;
        long pos = 0L;
        if (np.hasTrack() && np.durationMs > 0L) {
            pos = np.progressNow(0);
            frac = WorldPanel.clamp((float)pos / (float)np.durationMs);
        }
        this.round(bx, byy, bw, 3.0f, 1.5f, 0xFFFFFF, 0xFFFFFF, 0.18f * a, 0.003f);
        if (frac > 0.0f) {
            this.round(bx, byy, Math.max(3.0f, bw * frac), 3.0f, 1.5f, 2021216, 2021216, a, 0.004f);
        }
        if (np.hasTrack()) {
            this.textAt(this.m, WorldPanel.fmt(pos), bx, 75.5f, 0.5f, 0xC5C5C5, 0.8f * a, false);
            String dur = WorldPanel.fmt(np.durationMs);
            this.textAt(this.m, dur, bx + bw - (float)class_310.method_1551().field_1772.method_1727(dur) * 0.5f, 75.5f, 0.5f, 0xC5C5C5, 0.8f * a, false);
        }
        if ((tr = MediaManager.get().track()) != null && !tr.isEmpty()) {
            boolean spotify = "Spotify".equals(tr.sourceApp);
            String src = spotify ? "Spotify" : ("Browser".equals(tr.sourceApp) ? "YouTube" : tr.sourceApp);
            boolean ytm = "YT Music".equals(tr.sourceApp);
            boolean logo = spotify || ytm || "Browser".equals(tr.sourceApp);
            float tw = (float)class_310.method_1551().field_1772.method_1727(src) * 0.6f;
            float sx = -(tw + (logo ? 15.0f : 0.0f)) / 2.0f;
            if (logo) {
                this.image(LyricsFlow.id(spotify ? "textures/gui/logos/spotify.png" : (ytm ? "textures/gui/logos/ytmusic.png" : "textures/gui/logos/youtube.png")), sx - 2.0f, 82.0f, 10.0f, 10.0f, a);
            }
            int srcCol = 0xFFFFFF;
            this.textAt(this.m, src, sx + (logo ? 13.0f : 0.0f), 84.0f, 0.6f, srcCol, a, false);
        }
    }

    private static String fmt(long ms) {
        long s = Math.max(0L, ms / 1000L);
        return s / 60L + ":" + String.format("%02d", s % 60L);
    }

    private void body(class_310 mc, Config cf, NowPlaying np, float a) {
        float by = 207.0f;
        this.roundBtn("prev", 70.0f, by, 8.0f, "\u23ee", a, () -> SpotifyClient.control(SpotifyClient.Cmd.PREVIOUS));
        this.roundBtn("play", 98.0f, by, 10.0f, np.playing ? "\u23f8" : "\u25b6", a, () -> SpotifyClient.control(SpotifyClient.Cmd.PLAYPAUSE));
        this.roundBtn("next", 126.0f, by, 8.0f, "\u23ed", a, () -> SpotifyClient.control(SpotifyClient.Cmd.NEXT));
        int rep = MediaManager.get().repeat();
        this.roundBtn("repeat", 150.0f, by, 6.5f, rep == 1 ? "\u21bb1" : (rep == 2 ? "\u21bb\u2022" : "\u21bb"), a, () -> MediaManager.get().cycleRepeat(), rep > 0 ? 2021216 : 0xFFFFFF);
        this.hits.add(new Hit(36.0f, 18.0f, 124.0f, 124.0f, () -> SpotifyClient.control(SpotifyClient.Cmd.PLAYPAUSE)));
        this.hits.add(new Hit(30.0f, 169.0f, 136.0f, 10.0f, () -> {
            Track tk = MediaManager.get().track();
            if (tk != null && tk.durationMs > 0L) {
                float f = WorldPanel.clamp((this.hx - 30.0f) / 136.0f);
                MediaManager.get().seek((long)(f * (float)tk.durationMs));
            }
        }));
        class_327 font = class_310.method_1551().field_1772;
        this.text("SkyLyrics", 206.0f, 10.0f, 0.85f, 0xFFFFFF, a);
        float xh = this.hv("x", this.over(351.0f, 6.0f, 15.0f, 15.0f));
        this.ring(358.5f, 13.5f, 0.0f, 7.5f, 0.0f, 360.0f, Gfx.lerpColor(0xFFFFFF, 16734842, xh), (0.08f + 0.55f * xh) * a, Gfx.lerpColor(0xFFFFFF, 16734842, xh), (0.08f + 0.55f * xh) * a, 0.002f, 16);
        this.text("\u2715", 355.5f, 10.5f, 0.7f, 0xFFFFFF, a);
        this.hits.add(new Hit(351.0f, 6.0f, 15.0f, 15.0f, this::close));
        String nl = WorldPanel.tr("normal");
        float nw = (float)font.method_1727(nl) * 0.62f + 12.0f;
        float nhv = this.hv("nrm", this.over(346.0f - nw, 6.0f, nw, 15.0f));
        this.round(346.0f - nw, 6.0f, nw, 15.0f, 7.5f, 0xFFFFFF, 0xFFFFFF, (0.07f + 0.12f * nhv) * a, 0.002f);
        this.text(nl, 352.0f - nw, 10.5f, 0.62f, 0xFFFFFF, (0.8f + 0.2f * nhv) * a);
        this.hits.add(new Hit(346.0f - nw, 6.0f, nw, 15.0f, () -> {
            cf.menuMode = 0;
            PhoneMenu.closeNow();
            mc.method_1507((class_437)new LyricsFlowScreen());
        }));
        float tabW = (160.0f - 3.0f * (float)(TABS.length - 1)) / (float)TABS.length;
        for (int i = 0; i < TABS.length; ++i) {
            float x = 206.0f + (float)i * (tabW + 3.0f);
            boolean on = this.page == i;
            float h = this.hv("tab" + i, this.over(x, 203.0f, tabW, 14.0f));
            int k = i;
            if (on) {
                this.round(x, 203.0f, tabW, 14.0f, 7.0f, 10321407, 8018664, 0.9f * a, 0.002f);
            } else {
                this.round(x, 203.0f, tabW, 14.0f, 7.0f, 0xFFFFFF, 0xFFFFFF, (0.05f + 0.08f * h) * a, 0.002f);
            }
            String lbl = WorldPanel.ellip(WorldPanel.tr(TABS[i]), tabW - 6.0f, 0.62f);
            this.text(lbl, x + (tabW - (float)font.method_1727(lbl) * 0.62f) / 2.0f, 207.0f, 0.62f, 0xFFFFFF, (on ? 1.0f : 0.7f + 0.3f * h) * a);
            this.hits.add(new Hit(x, 203.0f, tabW, 14.0f, () -> this.go(k)));
        }
        String song = np.hasTrack() ? "\u266a  " + np.title + "  \u00b7  " + np.artist : "\u266a  " + WorldPanel.tr("nomusic");
        this.text(WorldPanel.ellip(song, 160.0f, 0.62f), 206.0f, 28.0f, 0.62f, 0xDCCFFF, 0.75f * a);
        float pa = a * WorldPanel.outCubic(WorldPanel.clamp((float)(System.currentTimeMillis() - this.pageTime) / 220.0f));
        switch (this.page) {
            case 0: {
                this.skies(cf, pa);
                break;
            }
            case 1: {
                this.lyrics(cf, pa);
                break;
            }
            case 2: {
                this.look(pa);
                break;
            }
            case 3: {
                this.style(cf, pa);
                break;
            }
            default: {
                this.more(mc, cf, pa);
            }
        }
    }

    private void skies(Config cf, float a) {
        int idx;
        SkyPreset[] all = SkyPreset.order();
        this.toggle2("skon", 206.0f, 45.0f, 78.0f, WorldPanel.tr("skyon"), cf.sky >= 0, () -> {
            cf.sky = cf.sky >= 0 ? -1 : Math.max(0, cf.lastSky);
        }, a);
        this.toggle2("skpic", 288.0f, 45.0f, 78.0f, WorldPanel.tr("picture"), cf.skyImage, () -> {
            cf.skyImage = !cf.skyImage;
            SkyImage.reload();
        }, a);
        int per = 6;
        int pages = (all.length + per - 1) / per;
        this.skyPage = Math.floorMod(this.skyPage, pages);
        float tw = 48.0f;
        float th = 27.0f;
        for (int i = 0; i < per && (idx = this.skyPage * per + i) < all.length; ++i) {
            SkyPreset p = all[idx];
            float x = 206.0f + (float)(i % 3) * (tw + 8.0f);
            float y = 66.0f + (float)(i / 3) * (th + 13.0f);
            boolean on = cf.sky == p.ordinal();
            float h = this.hv("sky" + idx, this.over(x - 1.0f, y - 1.0f, tw + 2.0f, th + 12.0f));
            float lift = h * 1.2f;
            if (on) {
                this.round(x - 2.0f, y - 2.0f - lift, tw + 4.0f, th + 4.0f, 6.0f, 10321407, 0xDCCFFF, a, 0.002f);
            } else if (h > 0.01f) {
                this.round(x - 1.5f, y - 1.5f - lift, tw + 3.0f, th + 3.0f, 5.5f, 0xFFFFFF, 0xFFFFFF, 0.5f * h * a, 0.002f);
            }
            class_2960 img = SkyThumbs.texture(p);
            if (img != null) {
                this.image(img, x, y - lift, tw, th, a);
            } else {
                this.round(x, y - lift, tw, th, 4.0f, p.top, p.horizon, a, 0.003f);
            }
            this.text(WorldPanel.ellip(class_2561.method_43471((String)p.key()).getString(), tw, 0.56f), x + 1.0f, y + th + 3.0f, 0.56f, 0xFFFFFF, a * (on ? 1.0f : 0.75f + 0.25f * h));
            this.hits.add(new Hit(x - 1.0f, y - 1.0f, tw + 2.0f, th + 12.0f, () -> {
                cf.lastSky = cf.sky = p.ordinal();
            }));
        }
        this.pager("sk", 146.0f, this.skyPage, pages, d -> this.skyPage += d, a);
        this.slider("str", WorldPanel.tr("strength"), 162.0f, (cf.skyStrength - 0.1f) / 0.9f, v -> {
            cf.skyStrength = 0.1f + 0.9f * v.floatValue();
        }, a);
        this.slider("spd", WorldPanel.tr("speed"), 178.0f, (cf.skySpeed - 0.25f) / 1.75f, v -> {
            cf.skySpeed = 0.25f + 1.75f * v.floatValue();
        }, a);
        this.slider("pat", WorldPanel.tr("pattern"), 194.0f, (cf.skyPattern - 0.2f) / 1.3f, v -> {
            cf.skyPattern = 0.2f + 1.3f * v.floatValue();
        }, a);
    }

    private static LFConfig lf() {
        return ConfigManager.get();
    }

    private void lyrics(Config cf, float a) {
        LFConfig c = WorldPanel.lf();
        this.toggle(0, WorldPanel.tr("lyricsmode"), c.lyricsOn, () -> {
            c.lyricsOn = !c.lyricsOn;
            WorldLyrics.clear();
        }, a);
        this.toggle(1, WorldPanel.tr("floating"), c.lyricsStyle == LFConfig.LyricsStyle.FLOATING, () -> {
            c.lyricsStyle = c.lyricsStyle == LFConfig.LyricsStyle.FLOATING ? LFConfig.LyricsStyle.SKY : LFConfig.LyricsStyle.FLOATING;
            WorldLyrics.clear();
        }, a);
        this.toggle2("beat", 206.0f, 87.0f, 78.0f, WorldPanel.tr("beat"), c.beatGlow, () -> {
            c.beatGlow = !c.beatGlow;
        }, a);
        this.toggle2("notes", 288.0f, 87.0f, 78.0f, WorldPanel.tr("notes"), c.musicNotes, () -> {
            c.musicNotes = !c.musicNotes;
        }, a);
        this.toggle2("smus", 206.0f, 107.0f, 78.0f, WorldPanel.tr("skymusic"), cf.fxSkyMusic, () -> {
            cf.fxSkyMusic = !cf.fxSkyMusic;
        }, a);
        this.toggle2("ugr", 288.0f, 107.0f, 78.0f, WorldPanel.tr("underground"), c.showUnderground, () -> {
            c.showUnderground = !c.showUnderground;
        }, a);
        this.slider("lop", WorldPanel.tr("opacity"), 142.0f, (c.textOpacity - 0.2f) / 0.8f, v -> {
            c.textOpacity = 0.2f + 0.8f * v.floatValue();
        }, a);
        this.slider("lsz", WorldPanel.tr("size"), 160.0f, (c.lyricsScale - 0.4f) / 2.1f, v -> {
            c.lyricsScale = 0.4f + 2.1f * v.floatValue();
        }, a);
        this.offset(178.0f, a);
    }

    private void offset(float y, float a) {
        LFConfig c = WorldPanel.lf();
        class_327 font = class_310.method_1551().field_1772;
        this.text(WorldPanel.tr("offset"), 206.0f, y, 0.66f, 0xFFFFFF, 0.85f * a);
        String v = (c.syncOffsetMs > 0 ? "+" : "") + c.syncOffsetMs + " ms";
        float vw = (float)font.method_1727(v) * 0.66f;
        this.text(v, 330.0f - vw / 2.0f, y, 0.66f, 0xFFFFFF, a);
        float lh = this.hv("offl", this.over(294.0f, y - 4.0f, 14.0f, 14.0f));
        float rh = this.hv("offr", this.over(352.0f, y - 4.0f, 14.0f, 14.0f));
        this.round(294.0f, y - 4.0f, 14.0f, 14.0f, 7.0f, 0xFFFFFF, 0xFFFFFF, (0.06f + 0.12f * lh) * a, 0.002f);
        this.round(352.0f, y - 4.0f, 14.0f, 14.0f, 7.0f, 0xFFFFFF, 0xFFFFFF, (0.06f + 0.12f * rh) * a, 0.002f);
        this.text("\u2039", 299.0f, y, 0.66f, 0xFFFFFF, a);
        this.text("\u203a", 357.0f, y, 0.66f, 0xFFFFFF, a);
        this.hits.add(new Hit(294.0f, y - 4.0f, 14.0f, 14.0f, () -> {
            c.syncOffsetMs = Math.max(-2000, c.syncOffsetMs - 100);
        }));
        this.hits.add(new Hit(352.0f, y - 4.0f, 14.0f, 14.0f, () -> {
            c.syncOffsetMs = Math.min(2000, c.syncOffsetMs + 100);
        }));
    }

    private void look(float a) {
        int idx;
        LFConfig c = WorldPanel.lf();
        List<Themes.Theme> th = Themes.ALL;
        this.text(WorldPanel.tr("color"), 206.0f, 45.0f, 0.62f, 0xFFFFFF, 0.55f * a);
        int n = Math.min(th.size(), 20);
        for (int i = 0; i < n; ++i) {
            Themes.Theme t = th.get(i);
            float cx = 212.0f + (float)(i % 10) * 16.0f;
            float cy = 60.0f + (float)(i / 10) * 15.0f;
            boolean on = !c.customTheme && c.singleColor == 0 && c.theme.equals(t.id());
            float h = this.hv("col" + i, this.over(cx - 6.0f, cy - 6.0f, 12.0f, 12.0f));
            if (on) {
                this.ring(cx, cy, 5.2f, 6.6f, 0.0f, 360.0f, 0xFFFFFF, a, 0xFFFFFF, a, 0.003f, 20);
            }
            this.ring(cx, cy, 0.0f, 4.6f + h * 0.6f, 0.0f, 360.0f, t.main() & 0xFFFFFF, a, t.a() & 0xFFFFFF, a, 0.004f, 18);
            this.hits.add(new Hit(cx - 6.0f, cy - 6.0f, 12.0f, 12.0f, () -> {
                c.theme = t.id();
                c.customTheme = false;
                c.singleColor = 0;
            }));
        }
        List<Fonts.LyricFont> fonts = Fonts.lyricsAll();
        int per = 6;
        int pages = (fonts.size() + per - 1) / per;
        this.fontPage = Math.floorMod(this.fontPage, pages);
        this.text(WorldPanel.tr("font"), 206.0f, 86.0f, 0.62f, 0xFFFFFF, 0.55f * a);
        for (int i = 0; i < per && (idx = this.fontPage * per + i) < fonts.size(); ++i) {
            Fonts.LyricFont f = fonts.get(idx);
            this.option("fnt" + idx, 206.0f + (float)(i % 2 * 82), 95 + i / 2 * 17, 78.0f, 14.0f, f.name(), f.id().equals(c.font), () -> {
                c.font = f.id();
                WorldLyrics.clear();
            }, a);
        }
        this.pager("fp", 84.0f, this.fontPage, pages, d -> this.fontPage += d, a);
        this.toggle2("caps", 206.0f, 148.0f, 78.0f, WorldPanel.tr("caps"), c.allCaps, () -> {
            c.allCaps = !c.allCaps;
            WorldLyrics.clear();
        }, a);
        this.toggle2("glow", 288.0f, 148.0f, 78.0f, WorldPanel.tr("glow"), c.glow, () -> {
            c.glow = !c.glow;
        }, a);
        boolean typed = c.enterAnim == LFConfig.EnterAnim.NEON;
        this.toggle2("typed", 206.0f, 167.0f, 160.0f, WorldPanel.tr("typed"), typed, () -> {
            c.enterAnim = typed ? LFConfig.EnterAnim.WORD : LFConfig.EnterAnim.NEON;
        }, a);
        String[] sh = new String[]{WorldPanel.tr("shadow0"), WorldPanel.tr("shadow1"), WorldPanel.tr("shadow2")};
        for (int i = 0; i < 3; ++i) {
            int k = i;
            this.option("sh" + i, 206.0f + (float)(i * 54), 186.0f, 50.0f, 13.0f, sh[i], c.textShadow == i, () -> {
                c.textShadow = k;
            }, a);
        }
    }

    private void style(Config cf, float a) {
        int i;
        int idx;
        String[] ids = Cosmetics.IDS;
        int per = 6;
        int pages = (ids.length + per - 1) / per;
        this.hatPage = Math.floorMod(this.hatPage, pages);
        this.text(WorldPanel.tr("hat"), 206.0f, 45.0f, 0.62f, 0xFFFFFF, 0.55f * a);
        for (int i2 = 0; i2 < per && (idx = this.hatPage * per + i2) < ids.length; ++i2) {
            int curIdx = idx;
            this.option("hat" + idx, 206.0f + (float)(i2 % 2 * 82), 54 + i2 / 2 * 18, 78.0f, 15.0f, class_2561.method_43471((String)("lyricsflow.cosmetic." + ids[idx])).getString(), WorldPanel.lf().worn().contains(ids[idx]) || "none".equals(ids[idx]) && WorldPanel.lf().worn().isEmpty(), () -> WorldPanel.wear(ids[curIdx]), a);
        }
        this.pager("ht", 108.0f, this.hatPage, pages, d -> this.hatPage += d, a);
        this.text(WorldPanel.tr("death"), 206.0f, 125.0f, 0.62f, 0xFFFFFF, 0.55f * a);
        int dper = 4;
        int dpages = (7 + dper - 1) / dper;
        this.deathPage = Math.floorMod(this.deathPage, dpages);
        this.pager("dp", 123.0f, this.deathPage, dpages, d -> this.deathPage += d, a);
        for (int j = 0; j < dper && (i = this.deathPage * dper + j) < 7; ++j) {
            int k = i;
            this.option("de" + i, 206.0f + (float)(j % 2 * 82), 134 + j / 2 * 18, 78.0f, 15.0f, class_2561.method_43471((String)("lyricsflow.ui.death." + i)).getString(), cf.deathFx == i, () -> {
                cf.deathFx = k;
            }, a);
        }
        this.toggle2("dot", 206.0f, 174.0f, 160.0f, class_2561.method_43471((String)"lyricsflow.ui.style.deathothers").getString(), cf.deathOthers, () -> {
            cf.deathOthers = !cf.deathOthers;
        }, a);
    }

    private void more(class_310 mc, Config cf, float a) {
        LFConfig c = WorldPanel.lf();
        this.toggle(0, WorldPanel.tr("island"), c.showTopBar, () -> {
            c.showTopBar = !c.showTopBar;
        }, a);
        this.toggle(1, WorldPanel.tr("clock"), c.clock, () -> {
            c.clock = !c.clock;
        }, a);
        this.toggle2("fps", 206.0f, 87.0f, 78.0f, "FPS", c.fps, () -> {
            c.fps = !c.fps;
        }, a);
        this.toggle2("png", 288.0f, 87.0f, 78.0f, "Ping", c.ping, () -> {
            c.ping = !c.ping;
        }, a);
        this.toggle2("hb", 206.0f, 107.0f, 78.0f, WorldPanel.tr("hotbar"), c.customHotbar, () -> {
            c.customHotbar = !c.customHotbar;
        }, a);
        this.toggle2("snd", 288.0f, 107.0f, 78.0f, WorldPanel.tr("sounds"), c.uiSounds, () -> {
            c.uiSounds = !c.uiSounds;
        }, a);
        this.option("m0", 206.0f, 128.0f, 160.0f, 15.0f, "\u2197  " + WorldPanel.tr("bigmenu"), false, () -> {
            PhoneMenu.closeNow();
            mc.method_1507((class_437)new LyricsFlowScreen());
        }, a);
        this.toggle2("m1", 206.0f, 145.0f, 160.0f, WorldPanel.tr("random"), cf.skyShuffle, () -> {
            boolean bl = cf.skyShuffle = !cf.skyShuffle;
            if (cf.skyShuffle) {
                SkyPreset.shuffle();
            }
        }, a);
        this.option("m2", 206.0f, 162.0f, 160.0f, 15.0f, "\u21ba  " + WorldPanel.tr("backnormal"), false, () -> {
            cf.menuMode = 0;
            this.close();
        }, a);
        this.option("m3", 206.0f, 179.0f, 160.0f, 15.0f, "\u2715  " + WorldPanel.tr("close"), false, this::close, a);
    }

    private void toggle(int row, String label, boolean on, Runnable flip, float a) {
        this.toggle2("tg" + row, 206.0f, 47 + row * 20, 160.0f, label, on, flip, a);
    }

    private void toggle2(String id, float x, float y, float w, String label, boolean on, Runnable flip, float a) {
        float h = 16.0f;
        float hh = this.hv(id, this.over(x, y, w, h));
        this.round(x, y, w, h, 6.0f, 0xFFFFFF, 0xFFFFFF, (0.035f + 0.05f * hh) * a, 0.002f);
        this.text(WorldPanel.ellip(label, w - 34.0f, 0.68f), x + 7.0f, y + 5.0f, 0.68f, 0xFFFFFF, 0.92f * a);
        float tx = x + w - 25.0f;
        float ty = y + 4.0f;
        this.round(tx, ty, 19.0f, 8.0f, 4.0f, on ? 10321407 : 0xFFFFFF, on ? 8018664 : 0xFFFFFF, (on ? 0.95f : 0.16f) * a, 0.003f);
        this.ring(on ? tx + 15.0f : tx + 4.0f, ty + 4.0f, 0.0f, 3.2f, 0.0f, 360.0f, 0xFFFFFF, a, 15262966, a, 0.004f, 12);
        this.hits.add(new Hit(x, y, w, h, flip));
    }

    private void option(String id, float x, float y, float w, float h, String label, boolean on, Runnable run, float a) {
        float hh = this.hv(id, this.over(x, y, w, h));
        if (on) {
            this.round(x, y, w, h, 6.0f, 10321407, 8018664, 0.88f * a, 0.002f);
        } else {
            this.round(x, y, w, h, 6.0f, 0xFFFFFF, 0xFFFFFF, (0.035f + 0.06f * hh) * a, 0.002f);
        }
        this.text(WorldPanel.ellip(label, w - 12.0f, 0.66f), x + 7.0f, y + (h - 6.0f) / 2.0f, 0.66f, 0xFFFFFF, (on ? 1.0f : 0.8f + 0.2f * hh) * a);
        this.hits.add(new Hit(x, y, w, h, run));
    }

    private void slider(String id, String label, float y, float v, Consumer<Float> set, float a) {
        v = WorldPanel.clamp(v);
        float tx = 258.0f;
        float tw = 366.0f - tx;
        float hh = this.hv(id, this.over(206.0f, y - 4.0f, 160.0f, 14.0f));
        this.text(label, 206.0f, y, 0.66f, 0xFFFFFF, (0.7f + 0.3f * hh) * a);
        this.round(tx, y + 1.5f, tw, 4.0f, 2.0f, 0xFFFFFF, 0xFFFFFF, (0.12f + 0.06f * hh) * a, 0.002f);
        this.round(tx, y + 1.5f, Math.max(4.0f, tw * v), 4.0f, 2.0f, 0xDCCFFF, 10321407, a, 0.003f);
        this.ring(tx + tw * v, y + 3.5f, 0.0f, 4.5f + hh, 0.0f, 360.0f, 0xFFFFFF, a, 15262966, a, 0.004f, 14);
        this.hits.add(new Hit(tx - 4.0f, y - 4.0f, tw + 8.0f, 14.0f, () -> set.accept(Float.valueOf(WorldPanel.clamp((this.hx - tx) / tw)))));
    }

    private void roundBtn(String id, float cx, float cy, float r, String icon, float a, Runnable run) {
        this.roundBtn(id, cx, cy, r, icon, a, run, 0xFFFFFF);
    }

    private void roundBtn(String id, float cx, float cy, float r, String icon, float a, Runnable run, int textCol) {
        float hh = this.hv(id, this.over(cx - r, cy - r, 2.0f * r, 2.0f * r));
        this.ring(cx, cy, 0.0f, r, 0.0f, 360.0f, Gfx.lerpColor(2762309, 10321407, hh), a, Gfx.lerpColor(1841460, 8018664, hh), a, 0.002f, 20);
        float tw = (float)class_310.method_1551().field_1772.method_1727(icon) * 0.75f;
        this.text(icon, cx - tw / 2.0f, cy - 3.0f, 0.75f, textCol, a);
        this.hits.add(new Hit(cx - r, cy - r, 2.0f * r, 2.0f * r, run));
    }

    private void pager(String id, float y, int cur, int pages, IntConsumer move, float a) {
        if (pages > 1) {
            float h1 = this.hv(id + "l", this.over(320.0f, y, 14.0f, 12.0f));
            float h2 = this.hv(id + "r", this.over(352.0f, y, 14.0f, 12.0f));
            this.round(320.0f, y, 14.0f, 12.0f, 6.0f, 0xFFFFFF, 0xFFFFFF, (0.08f + 0.2f * h1) * a, 0.002f);
            this.round(352.0f, y, 14.0f, 12.0f, 6.0f, 0xFFFFFF, 0xFFFFFF, (0.08f + 0.2f * h2) * a, 0.002f);
            this.text("\u2039", 325.0f, y + 2.5f, 0.75f, 0xFFFFFF, a);
            this.text("\u203a", 357.0f, y + 2.5f, 0.75f, 0xFFFFFF, a);
            this.text(cur + 1 + "/" + pages, 336.0f, y + 3.0f, 0.6f, 0xFFFFFF, 0.7f * a);
            this.hits.add(new Hit(320.0f, y, 14.0f, 12.0f, () -> move.accept(-1)));
            this.hits.add(new Hit(352.0f, y, 14.0f, 12.0f, () -> move.accept(1)));
        }
    }

    private float lx(float x) {
        return (x - this.ox) * 0.0105f;
    }

    private float ly(float y) {
        return (this.oy - y) * 0.0105f;
    }

    private void vert(float x, float y, float z, int col) {
        this.q.method_22918((Matrix4fc)this.m, this.lx(x), this.ly(y), z).method_39415(col);
    }

    private void quad(float x, float y, float w, float h, int cTop, float aTop, int cBot, float aBot, float z) {
        int t = Gfx.argbRaw(cTop, aTop);
        int b = Gfx.argbRaw(cBot, aBot);
        this.vert(x, y, z, t);
        this.vert(x + w, y, z, t);
        this.vert(x + w, y + h, z, b);
        this.vert(x, y + h, z, b);
    }

    private void poly(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3, int rgb, float a, float z) {
        int col = Gfx.argbRaw(rgb, a);
        this.vert(x0, y0, z, col);
        this.vert(x1, y1, z, col);
        this.vert(x2, y2, z, col);
        this.vert(x3, y3, z, col);
    }

    private void ring(float cx, float cy, float r0, float r1, float a0, float a1, int cIn, float aIn, int cOut, float aOut, float z, int segs) {
        if (!(aIn <= 0.003f) || !(aOut <= 0.003f)) {
            int ci = Gfx.argbRaw(cIn, aIn);
            int co = Gfx.argbRaw(cOut, aOut);
            for (int i = 0; i < segs; ++i) {
                double t0 = Math.toRadians(a0 + (a1 - a0) * (float)i / (float)segs);
                double t1 = Math.toRadians(a0 + (a1 - a0) * (float)(i + 1) / (float)segs);
                float c0 = (float)Math.cos(t0);
                float s0 = (float)Math.sin(t0);
                float c1 = (float)Math.cos(t1);
                float s1 = (float)Math.sin(t1);
                this.vert(cx + c0 * r0, cy + s0 * r0, z, ci);
                this.vert(cx + c0 * r1, cy + s0 * r1, z, co);
                this.vert(cx + c1 * r1, cy + s1 * r1, z, co);
                this.vert(cx + c1 * r0, cy + s1 * r0, z, ci);
            }
        }
    }

    private void round(float x, float y, float w, float h, float r, int top, int bot, float a, float z) {
        if (!(a <= 0.003f)) {
            int n = (r = Math.min(r, Math.min(w, h) / 2.0f)) > 3.0f ? 5 : 2;
            float step = r / (float)n;
            for (int j = 0; j < n; ++j) {
                float yc = r - ((float)j + 0.5f) * step;
                float in = r - (float)Math.sqrt(Math.max(0.0f, r * r - yc * yc));
                this.band(x + in, y + (float)j * step, w - 2.0f * in, step, y, h, top, bot, a, z);
                this.band(x + in, y + h - (float)(j + 1) * step, w - 2.0f * in, step, y, h, top, bot, a, z);
            }
            this.band(x, y + r, w, h - 2.0f * r, y, h, top, bot, a, z);
        }
    }

    private void band(float x, float y, float w, float h, float oy0, float oh, int top, int bot, float a, float z) {
        if (!(h <= 0.0f) && !(w <= 0.0f)) {
            this.quad(x, y, w, h, Gfx.lerpColor(top, bot, (y - oy0) / oh), a, Gfx.lerpColor(top, bot, (y + h - oy0) / oh), a, z);
        }
    }

    private void image(class_2960 tex, float x, float y, float w, float h, float a) {
        Matrix4f mm = new Matrix4f((Matrix4fc)this.m);
        float x0 = this.lx(x);
        float x1 = this.lx(x + w);
        float y0 = this.ly(y);
        float y1 = this.ly(y + h);
        float z = 0.005f;
        int col = Gfx.argbRaw(0xFFFFFF, a);
        this.late.add(() -> {
            class_4588 v = class_310.method_1551().method_22940().method_23000().method_73477(class_12249.method_76022((class_2960)tex));
            v.method_22918((Matrix4fc)mm, x0, y0, z).method_39415(col).method_22913(0.0f, 0.0f).method_60803(0xF000F0);
            v.method_22918((Matrix4fc)mm, x0, y1, z).method_39415(col).method_22913(0.0f, 1.0f).method_60803(0xF000F0);
            v.method_22918((Matrix4fc)mm, x1, y1, z).method_39415(col).method_22913(1.0f, 1.0f).method_60803(0xF000F0);
            v.method_22918((Matrix4fc)mm, x1, y0, z).method_39415(col).method_22913(1.0f, 0.0f).method_60803(0xF000F0);
        });
    }

    private void text(String s, float x, float y, float scale, int rgb, float a) {
        this.textAt(this.m, s, this.lx(x) / 0.0105f, -this.ly(y) / 0.0105f, scale, rgb, a, false);
    }

    private void textAt(Matrix4f mat, String s, float px, float py, float scale, int rgb, float a, boolean centered) {
        if (!(a <= 0.01f) && !s.isEmpty()) {
            class_327 f = class_310.method_1551().field_1772;
            float x = centered ? px - (float)f.method_1727(s) * scale / 2.0f : px;
            Matrix4f tm = new Matrix4f((Matrix4fc)mat).translate(x * 0.0105f, -py * 0.0105f, 0.012f).scale(0.0105f * scale, -0.0105f * scale, 0.0105f);
            int col = Gfx.argbRaw(rgb, a);
            this.late.add(() -> {
                class_310 mc = class_310.method_1551();
                mc.field_1772.method_27522((class_2561)class_2561.method_43470((String)s), 0.0f, 0.0f, col, false, tm, (class_4597)mc.method_22940().method_23000(), class_327.class_6415.field_33993, 0, 0xF000F0);
            });
        }
    }

    private static String ellip(String s, float maxPx, float scale) {
        class_327 f = class_310.method_1551().field_1772;
        if ((float)f.method_1727(s) * scale <= maxPx) {
            return s;
        }
        String t = s;
        while (t.length() > 1 && (float)f.method_1727(t + "...") * scale > maxPx) {
            t = t.substring(0, t.length() - 1);
        }
        return t + "...";
    }

    private static float clamp(float x) {
        return Math.max(0.0f, Math.min(1.0f, x));
    }

    private static float outCubic(float t) {
        float u = 1.0f - t;
        return 1.0f - u * u * u;
    }

    private static float inOut(float t) {
        return t < 0.5f ? 4.0f * t * t * t : 1.0f - (float)Math.pow(-2.0f * t + 2.0f, 3.0) / 2.0f;
    }

    static {
        TABS = new String[]{"sky", "lyrics", "look", "cosm", "more"};
    }

    private record Hit(float x, float y, float w, float h, Runnable run) {
    }
}


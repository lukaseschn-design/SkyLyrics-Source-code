package dev.lyricsflow.render;

import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.config.LFConfig;
import dev.lyricsflow.gui.UiSounds;
import dev.lyricsflow.lyrics.Lyrics;
import dev.lyricsflow.media.MediaManager;
import dev.lyricsflow.perf.AutoPerf;
import dev.lyricsflow.render.Proj;
import dev.lyricsflow.render.TopBar;
import dev.lyricsflow.render.WorldLyrics;
import dev.lyricsflow.util.Anim;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_9779;

public final class Hud {
    public static float beat;
    public static float intensity;
    public static float skyIntensity;
    public static float focusA;
    private static boolean wasActive;
    private static long backAt;
    public static volatile boolean hideForScreenshot;
    private static int lastWordKey;
    private static long lastFrame;

    private Hud() {
    }

    public static void frame() {
        Lyrics.Line line;
        boolean waiting;
        MediaManager.Event e;
        Anim.frame();
        class_310 mcw = class_310.method_1551();
        Proj.update(mcw.method_22683().method_4486(), mcw.method_22683().method_4502());
        if (Anim.frameId() == lastFrame) {
            return;
        }
        lastFrame = Anim.frameId();
        AutoPerf.frame();
        MediaManager mm = MediaManager.get();
        while ((e = mm.pollEvent()) != null) {
            TopBar.onEvent(e);
            WorldLyrics.onEvent(e);
            if (e.type() != MediaManager.EventType.SONG || !ConfigManager.get().enabled || !ConfigManager.get().lyricsOn || !ConfigManager.get().songChime) continue;
            UiSounds.play(UiSounds.SONG, 1.0f);
        }
        boolean active = mcw.method_1569();
        if (active && !wasActive) {
            mm.resync();
            backAt = System.nanoTime();
        }
        wasActive = active;
        boolean bl = waiting = backAt > 0L && System.nanoTime() - backAt < 450000000L;
        if (!waiting) {
            backAt = -1L;
        }
        focusA = 1.0f;
        LFConfig c = ConfigManager.get();
        boolean on = c.enabled && mm.isPlaying();
        intensity = Anim.approach(intensity, on ? 1.0f : 0.0f, on ? 2.2f : 1.2f);
        boolean skyOn = c.enabled && (mm.isPlaying() || c.skyAlways);
        skyIntensity = Anim.approach(skyIntensity, skyOn ? 1.0f : 0.0f, skyOn ? 2.2f : 1.2f);
        Lyrics l = mm.lyrics();
        long t = mm.position();
        if (l != null && t >= 0L && mm.isPlaying() && (line = l.lineAt(t)) != null) {
            int w = line.wordAt(t);
            int key = line.index * 1000 + w;
            if (w >= 0 && key != lastWordKey) {
                lastWordKey = key;
                beat = 1.0f;
            }
        }
        beat = Math.max(0.0f, beat - Anim.dt() * 3.2f);
    }

    public static void renderWorldSky(class_4587 pose, class_4597 buffers) {
        Hud.renderWorldFx(pose, buffers, false);
    }

    public static void renderFarSky() {
        Hud.renderWorldFx(new class_4587(), null, true);
    }

    private static void renderWorldFx(class_4587 pose, class_4597 buffers, boolean sky) {
        AutoPerf.begin();
        try {
            Hud.renderWorldFx0(pose, buffers, sky);
        }
        finally {
            AutoPerf.end();
        }
    }

    private static void renderWorldFx0(class_4587 pose, class_4597 buffers, boolean sky) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1724 == null || !ConfigManager.get().enabled) {
            return;
        }
        Hud.frame();
        LFConfig c = ConfigManager.get();
        if (!sky && c.enabled && c.lyricsShown()) {
            WorldLyrics.tick();
        }
        if (hideForScreenshot) {
            return;
        }
        if (!c.enabled) {
            return;
        }
        if (buffers == null) {
            buffers = mc.method_22940().method_23000();
        }
    }

    public static void renderWorldLayer(class_332 g, class_9779 tick) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1724 == null || !ConfigManager.get().enabled) {
            return;
        }
        Hud.frame();
        if (hideForScreenshot) {
            return;
        }
        LFConfig c = ConfigManager.get();
        if (!c.enabled) {
            return;
        }
        if (c.lyricsShown()) {
            WorldLyrics.render(g);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void renderTopLayer(class_332 g, class_9779 tick) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1724 == null || hideForScreenshot) {
            return;
        }
        LFConfig c = ConfigManager.get();
        if (!c.enabled || !c.showTopBar) {
            return;
        }
        AutoPerf.begin();
        try {
            TopBar.render(g, false);
        }
        finally {
            AutoPerf.end();
        }
    }

    static {
        focusA = 1.0f;
        wasActive = true;
        backAt = -1L;
        lastWordKey = Integer.MIN_VALUE;
        lastFrame = -1L;
    }
}


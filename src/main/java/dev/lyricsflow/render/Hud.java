/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  com.mojang.blaze3d.vertex.PoseStack
 *  dev.lyricsflow.compat.LegacyBufferSource
 *  net.minecraft.client.DeltaTracker
 */
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.lyricsflow.compat.LegacyBufferSource;
import net.minecraft.client.DeltaTracker;

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
        Minecraft mcw = Minecraft.getInstance();
        Proj.update(mcw.getWindow().getGuiScaledWidth(), mcw.getWindow().getGuiScaledHeight());
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
        boolean active = mcw.isWindowActive();
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

    public static void renderWorldSky(PoseStack pose, LegacyBufferSource buffers) {
        Hud.renderWorldFx(pose, buffers, false);
    }

    public static void renderFarSky() {
        Hud.renderWorldFx(new PoseStack(), null, true);
    }

    private static void renderWorldFx(PoseStack pose, LegacyBufferSource buffers, boolean sky) {
        AutoPerf.begin();
        try {
            Hud.renderWorldFx0(pose, buffers, sky);
        }
        finally {
            AutoPerf.end();
        }
    }

    private static void renderWorldFx0(PoseStack pose, LegacyBufferSource buffers, boolean sky) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || !ConfigManager.get().enabled) {
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
            buffers = LegacyBufferSource.current();
        }
    }

    public static void renderWorldLayer(GuiGraphicsExtractor g, DeltaTracker tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || !ConfigManager.get().enabled) {
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
    public static void renderTopLayer(GuiGraphicsExtractor g, DeltaTracker tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || hideForScreenshot) {
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


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 */
package dev.lyricsflow.app.util;

import net.minecraft.client.Minecraft;

public final class AutoPerf {
    public static final int MAX = 6;

    private AutoPerf() {
    }

    public static int level() {
        return dev.lyricsflow.perf.AutoPerf.level();
    }

    public static int fps() {
        return dev.lyricsflow.perf.AutoPerf.fps();
    }

    public static double modMs() {
        return dev.lyricsflow.perf.AutoPerf.modMs();
    }

    public static int quality() {
        return dev.lyricsflow.perf.AutoPerf.quality();
    }

    public static boolean allow(int cut) {
        return dev.lyricsflow.perf.AutoPerf.allow(cut);
    }

    public static int target() {
        return dev.lyricsflow.perf.AutoPerf.target();
    }

    public static void begin() {
        dev.lyricsflow.perf.AutoPerf.begin();
    }

    public static void end() {
        dev.lyricsflow.perf.AutoPerf.end();
    }

    public static void frame() {
        dev.lyricsflow.perf.AutoPerf.frame();
    }

    public static void tick(Minecraft mc) {
    }

    public static void reset() {
        dev.lyricsflow.perf.AutoPerf.reset();
    }
}


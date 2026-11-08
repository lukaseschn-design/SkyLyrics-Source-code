/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10799
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 */
package dev.lyricsflow.gui;

import dev.lyricsflow.LyricsFlow;
import dev.lyricsflow.render.Gfx;
import dev.lyricsflow.util.Col;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_332;

public final class Icons {
    private static final Map<String, class_2960> CACHE = new HashMap<String, class_2960>();

    private Icons() {
    }

    private static class_2960 tex(String name) {
        return CACHE.computeIfAbsent(name, n -> LyricsFlow.id("textures/gui/icons/" + n + ".png"));
    }

    public static void draw(class_332 g, String name, float cx, float cy, float size, int color) {
        if (Col.a(color) < 4) {
            return;
        }
        float s = Gfx.scale();
        int px = Math.max(1, Math.round(size * s));
        g.method_51448().pushMatrix();
        g.method_51448().translate(cx, cy);
        g.method_51448().scale(1.0f / s, 1.0f / s);
        g.method_25293(class_10799.field_56883, Icons.tex(name), -px / 2, -px / 2, 0.0f, 0.0f, px, px, 64, 64, 64, 64, color);
        g.method_51448().popMatrix();
    }
}


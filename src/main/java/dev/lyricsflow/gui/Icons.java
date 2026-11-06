/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.resources.Identifier
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package dev.lyricsflow.gui;

import dev.lyricsflow.SkyLyrics;
import dev.lyricsflow.render.Gfx;
import dev.lyricsflow.util.Col;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class Icons {
    private static final Map<String, Identifier> CACHE = new HashMap<String, Identifier>();

    private Icons() {
    }

    private static Identifier tex(String name) {
        return CACHE.computeIfAbsent(name, n -> SkyLyrics.id("textures/gui/icons/" + n + ".png"));
    }

    public static void draw(GuiGraphicsExtractor g, String name, float cx, float cy, float size, int color) {
        if (Col.a(color) < 4) {
            return;
        }
        float s = Gfx.scale();
        int px = Math.max(1, Math.round(size * s));
        g.pose().pushMatrix();
        g.pose().translate(cx, cy);
        g.pose().scale(1.0f / s, 1.0f / s);
        g.blit(RenderPipelines.GUI_TEXTURED, Icons.tex(name), -px / 2, -px / 2, 0.0f, 0.0f, px, px, 64, 64, 64, 64, color);
        g.pose().popMatrix();
    }
}


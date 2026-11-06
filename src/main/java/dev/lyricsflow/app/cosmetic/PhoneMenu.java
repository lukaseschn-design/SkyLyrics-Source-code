/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  dev.lyricsflow.compat.LegacyBufferSource
 *  org.joml.Matrix4f
 */
package dev.lyricsflow.app.cosmetic;

import dev.lyricsflow.app.Core;
import dev.lyricsflow.app.cosmetic.WorldPanel;
import dev.lyricsflow.gui.SkyLyricsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import dev.lyricsflow.compat.LegacyBufferSource;
import org.joml.Matrix4f;

public final class PhoneMenu {
    private static WorldPanel panel;

    private PhoneMenu() {
    }

    public static boolean interactive() {
        return Core.cfg().menuMode == 1;
    }

    public static void open(Minecraft mc) {
        if (panel != null) {
            if (!panel.closing()) {
                panel.close();
            }
        } else if (PhoneMenu.interactive() && mc.level != null && mc.player != null) {
            panel = new WorldPanel(mc);
        } else {
            mc.gui.setScreen((Screen)new SkyLyricsScreen());
        }
    }

    public static void closeNow() {
        panel = null;
    }

    public static void menuClosed() {
    }

    public static boolean pointerShown() {
        return panel != null && !panel.closing() && panel.hovered();
    }

    public static boolean hidesHand() {
        return false;
    }

    public static void tick(Minecraft mc) {
        if (panel != null) {
            if (mc.level != null && mc.player != null && !panel.finished() && !(mc.player.position().distanceTo(panel.pos()) > 32.0)) {
                panel.tick(mc);
            } else {
                panel = null;
            }
        }
    }

    public static void startTick(Minecraft mc) {
        if (panel != null && mc.gui.screen() == null && panel.hovered()) {
            while (mc.options.keyAttack.consumeClick()) {
                panel.click();
            }
            while (mc.options.keyUse.consumeClick()) {
                panel.click();
            }
        }
    }

    public static void render(Minecraft mc, LegacyBufferSource buffers, Matrix4f base, float pt) {
        if (panel != null && mc.player != null) {
            panel.render(mc, buffers, base);
        }
    }
}


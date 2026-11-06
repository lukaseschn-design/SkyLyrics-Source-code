/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_437
 *  net.minecraft.class_4597
 *  org.joml.Matrix4f
 */
package dev.lyricsflow.app.cosmetic;

import dev.lyricsflow.app.Core;
import dev.lyricsflow.app.cosmetic.WorldPanel;
import dev.lyricsflow.gui.SkyLyricsScreen;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_4597;
import org.joml.Matrix4f;

public final class PhoneMenu {
    private static WorldPanel panel;

    private PhoneMenu() {
    }

    public static boolean interactive() {
        return Core.cfg().menuMode == 1;
    }

    public static void open(class_310 mc) {
        if (panel != null) {
            if (!panel.closing()) {
                panel.close();
            }
        } else if (PhoneMenu.interactive() && mc.field_1687 != null && mc.field_1724 != null) {
            panel = new WorldPanel(mc);
        } else {
            mc.method_1507((class_437)new SkyLyricsScreen());
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

    public static void tick(class_310 mc) {
        if (panel != null) {
            if (mc.field_1687 != null && mc.field_1724 != null && !panel.finished() && !(mc.field_1724.method_73189().method_1022(panel.pos()) > 32.0)) {
                panel.tick(mc);
            } else {
                panel = null;
            }
        }
    }

    public static void startTick(class_310 mc) {
        if (panel != null && mc.field_1755 == null && panel.hovered()) {
            while (mc.field_1690.field_1886.method_1436()) {
                panel.click();
            }
            while (mc.field_1690.field_1904.method_1436()) {
                panel.click();
            }
        }
    }

    public static void render(class_310 mc, class_4597 buffers, Matrix4f base, float pt) {
        if (panel != null && mc.field_1724 != null) {
            panel.render(mc, buffers, base);
        }
    }
}


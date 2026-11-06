package Yuki.lumiclient.util;

import net.minecraft.class_1109;
import net.minecraft.class_310;
import net.minecraft.class_3417;

public final class UiSounds {
    private static long lastHover = 0;

    /**
     * Sanfter, dezenter Tastatur-/Hover-Tick (Note Block Hat).
     * Klingt wie ein edler mechanischer Switch, kein schrilles Klicken.
     */
    public static void hover() {
        long now = System.currentTimeMillis();
        if (now - lastHover < 75) return;
        lastHover = now;
        try {
            class_310 mc = class_310.method_1551();
            if (mc != null && mc.method_1483() != null) {
                // field_15204 ist block.note_block.hat -> weicher, knackiger Tick
                mc.method_1483().method_4873(class_1109.method_4757(class_3417.field_15204.comp_349(), 1.30f, 0.10f));
            }
        } catch (Throwable ignored) {}
    }

    /**
     * Knackiger, satter Button-Klick (echter UI Button Click).
     */
    public static void select() {
        try {
            class_310 mc = class_310.method_1551();
            if (mc != null && mc.method_1483() != null) {
                // field_15015 ist ui.button.click -> satter, klassischer Minecraft Klick
                mc.method_1483().method_4873(class_1109.method_4757(class_3417.field_15015.comp_349(), 1.0f, 0.35f));
            }
        } catch (Throwable ignored) {}
    }
}

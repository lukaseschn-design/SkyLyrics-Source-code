package Yuki.lumiclient.util;

import net.minecraft.class_1109;
import net.minecraft.class_310;
import net.minecraft.class_3417;

public final class UiSounds {
    private static long lastHover = 0;

    public static void hover() {
        long now = System.currentTimeMillis();
        if (now - lastHover < 60) return;
        lastHover = now;
        try {
            class_310 mc = class_310.method_1551();
            if (mc != null && mc.method_1483() != null) {
                mc.method_1483().method_4873(class_1109.method_4757(class_3417.field_15239.comp_349(), 1.6f, 0.15f));
            }
        } catch (Throwable ignored) {}
    }

    public static void select() {
        try {
            class_310 mc = class_310.method_1551();
            if (mc != null && mc.method_1483() != null) {
                mc.method_1483().method_4873(class_1109.method_4757(class_3417.field_15239.comp_349(), 1.0f, 0.35f));
            }
        } catch (Throwable ignored) {}
    }
}

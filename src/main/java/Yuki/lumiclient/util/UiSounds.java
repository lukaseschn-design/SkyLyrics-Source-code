package Yuki.lumiclient.util;

import net.minecraft.class_1109;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3414;

public final class UiSounds {
    private static long lastHover = 0;

    public static final class_3414 SOUND_SELECT = class_3414.method_47908(class_2960.method_60655("lumiclient", "ui.select"));
    public static final class_3414 SOUND_HOVER = class_3414.method_47908(class_2960.method_60655("lumiclient", "ui.hover"));

    public static void hover() {
        long now = System.currentTimeMillis();
        if (now - lastHover < 65) return;
        lastHover = now;
        try {
            class_310 mc = class_310.method_1551();
            if (mc != null && mc.method_1483() != null) {
                mc.method_1483().method_4873(class_1109.method_4757(SOUND_HOVER, 1.0f, 0.40f));
            }
        } catch (Throwable ignored) {}
    }

    public static void select() {
        try {
            class_310 mc = class_310.method_1551();
            if (mc != null && mc.method_1483() != null) {
                mc.method_1483().method_4873(class_1109.method_4757(SOUND_SELECT, 1.0f, 0.65f));
            }
        } catch (Throwable ignored) {}
    }
}

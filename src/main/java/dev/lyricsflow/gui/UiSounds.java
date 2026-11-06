/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1109
 *  net.minecraft.class_1113
 *  net.minecraft.class_1113$class_1114
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3414
 *  net.minecraft.class_3417
 *  net.minecraft.class_3419
 *  net.minecraft.class_5819
 */
package dev.lyricsflow.gui;

import dev.lyricsflow.SkyLyrics;
import dev.lyricsflow.config.ConfigManager;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_5819;

public final class UiSounds {
    public static final class_3414 CLICK = UiSounds.event("ui.click");
    public static final class_3414 HOVER = UiSounds.event("ui.hover");
    public static final class_3414 OPEN = UiSounds.event("ui.open");
    public static final class_3414 CLOSE = UiSounds.event("ui.close");
    public static final class_3414 TAB = UiSounds.event("ui.tab");
    public static final class_3414 TOGGLE_ON = UiSounds.event("ui.toggle_on");
    public static final class_3414 TOGGLE_OFF = UiSounds.event("ui.toggle_off");
    public static final class_3414 SLIDER = UiSounds.event("ui.slider");
    public static final class_3414 SONG = UiSounds.event("ui.song");
    private static final class_3414 S_CLICK = UiSounds.event("ui.serious.click");
    private static final class_3414 S_TAB = UiSounds.event("ui.serious.tab");
    private static final class_3414 S_ON = UiSounds.event("ui.serious.toggle_on");
    private static final class_3414 S_OFF = UiSounds.event("ui.serious.toggle_off");
    private static final class_3414 S_SONG = UiSounds.event("ui.serious.song");
    private static final class_5819 RANDOM = class_5819.method_43047();
    private static long lastHover;
    private static long lastSlider;

    private UiSounds() {
    }

    private static class_3414 event(String name) {
        return class_3414.method_47908((class_2960)SkyLyrics.id(name));
    }

    public static void play(class_3414 e, float pitch) {
        boolean clicky;
        if (!ConfigManager.get().uiSounds && e != SONG) {
            return;
        }
        float vol = 1.0f;
        class_310 mc = class_310.method_1551();
        if (mc.method_1483() == null) {
            return;
        }
        if (e == SONG) {
            mc.method_1483().method_4873((class_1113)new class_1109(e.comp_3319(), class_3419.field_15250, vol, pitch, RANDOM, false, 0, class_1113.class_1114.field_5478, 0.0, 0.0, 0.0, true));
            return;
        }
        boolean bl = clicky = e == CLICK || e == TAB || e == TOGGLE_ON || e == TOGGLE_OFF;
        if (!clicky) {
            return;
        }
        mc.method_1483().method_4873((class_1113)class_1109.method_4757((class_3414)((class_3414)class_3417.field_15015.comp_349()), (float)1.4f, (float)(0.35f * vol)));
    }

    public static void play(class_3414 e) {
        UiSounds.play(e, 1.0f);
    }
}


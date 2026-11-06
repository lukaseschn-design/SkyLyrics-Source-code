package Yuki.lumiclient.util;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;

public final class UiSounds {
    private static long lastHover = 0;

    public static void hover() {
        long now = System.currentTimeMillis();
        if (now - lastHover < 60) return;
        lastHover = now;
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getSoundManager() != null) {
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.SHIELD_BREAK.value(), 1.6f, 0.15f));
            }
        } catch (Throwable ignored) {}
    }

    public static void select() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getSoundManager() != null) {
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.SHIELD_BREAK.value(), 1.0f, 0.35f));
            }
        } catch (Throwable ignored) {}
    }
}

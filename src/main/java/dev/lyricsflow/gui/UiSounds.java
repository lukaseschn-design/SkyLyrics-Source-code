/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance$Attenuation
 *  net.minecraft.resources.Identifier
 *  net.minecraft.client.Minecraft
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.RandomSource
 */
package dev.lyricsflow.gui;

import dev.lyricsflow.SkyLyrics;
import dev.lyricsflow.config.ConfigManager;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public final class UiSounds {
    public static final SoundEvent CLICK = UiSounds.event("ui.click");
    public static final SoundEvent HOVER = UiSounds.event("ui.hover");
    public static final SoundEvent OPEN = UiSounds.event("ui.open");
    public static final SoundEvent CLOSE = UiSounds.event("ui.close");
    public static final SoundEvent TAB = UiSounds.event("ui.tab");
    public static final SoundEvent TOGGLE_ON = UiSounds.event("ui.toggle_on");
    public static final SoundEvent TOGGLE_OFF = UiSounds.event("ui.toggle_off");
    public static final SoundEvent SLIDER = UiSounds.event("ui.slider");
    public static final SoundEvent SONG = UiSounds.event("ui.song");
    private static final SoundEvent S_CLICK = UiSounds.event("ui.serious.click");
    private static final SoundEvent S_TAB = UiSounds.event("ui.serious.tab");
    private static final SoundEvent S_ON = UiSounds.event("ui.serious.toggle_on");
    private static final SoundEvent S_OFF = UiSounds.event("ui.serious.toggle_off");
    private static final SoundEvent S_SONG = UiSounds.event("ui.serious.song");
    private static final RandomSource RANDOM = RandomSource.create();
    private static long lastHover;
    private static long lastSlider;

    private UiSounds() {
    }

    private static SoundEvent event(String name) {
        return SoundEvent.createVariableRangeEvent((Identifier)SkyLyrics.id(name));
    }

    public static void play(SoundEvent e, float pitch) {
        boolean clicky;
        if (!ConfigManager.get().uiSounds && e != SONG) {
            return;
        }
        float vol = 1.0f;
        Minecraft mc = Minecraft.getInstance();
        if (mc.getSoundManager() == null) {
            return;
        }
        if (e == SONG) {
            mc.getSoundManager().play((SoundInstance)new SimpleSoundInstance(e.location(), SoundSource.MASTER, vol, pitch, RANDOM, false, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true));
            return;
        }
        boolean bl = clicky = e == CLICK || e == TAB || e == TOGGLE_ON || e == TOGGLE_OFF;
        if (!clicky) {
            return;
        }
        mc.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value()), (float)1.4f, (float)(0.35f * vol)));
    }

    public static void play(SoundEvent e) {
        UiSounds.play(e, 1.0f);
    }
}

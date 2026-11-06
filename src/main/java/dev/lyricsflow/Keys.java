/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.KeyMapping$Category
 *  net.minecraft.client.Minecraft
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.minecraft.client.gui.screens.Screen
 */
package dev.lyricsflow;

import dev.lyricsflow.SkyLyrics;
import dev.lyricsflow.app.cosmetic.PhoneMenu;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.config.LFConfig;
import dev.lyricsflow.gui.PlayerScreen;
import dev.lyricsflow.gui.UiSounds;
import dev.lyricsflow.media.MediaManager;
import dev.lyricsflow.media.Track;
import dev.lyricsflow.render.WorldLyrics;
import java.util.HashMap;
import java.util.Locale;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.Screen;

public final class Keys {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register((Identifier)SkyLyrics.id("main"));
    public static KeyMapping openMenu;
    public static KeyMapping playPause;
    public static KeyMapping next;
    public static KeyMapping previous;
    public static KeyMapping openPlayer;
    public static KeyMapping toggle;
    public static KeyMapping lyricsEarlier;
    public static KeyMapping lyricsLater;
    private static boolean movedKeys;

    private Keys() {
    }

    public static void register() {
        openMenu = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.lyricsflow.menu", InputConstants.Type.KEYBOARD, 75, CATEGORY));
        playPause = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.lyricsflow.play_pause", InputConstants.Type.KEYBOARD, 264, CATEGORY));
        next = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.lyricsflow.next", InputConstants.Type.KEYBOARD, 262, CATEGORY));
        openPlayer = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.lyricsflow.player", InputConstants.Type.KEYBOARD, 74, CATEGORY));
        toggle = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.lyricsflow.onoff", InputConstants.Type.KEYBOARD, 79, CATEGORY));
        previous = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.lyricsflow.previous", InputConstants.Type.KEYBOARD, 263, CATEGORY));
        lyricsEarlier = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.lyricsflow.earlier", InputConstants.Type.KEYBOARD, 93, CATEGORY));
        lyricsLater = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.lyricsflow.later", InputConstants.Type.KEYBOARD, 91, CATEGORY));
    }

    private static void moveOldKeys(Minecraft mc) {
        movedKeys = true;
        if (!ConfigManager.moveKeys || mc.options == null) {
            return;
        }
        ConfigManager.moveKeys = false;
        boolean changed = false;
        changed |= Keys.move(openMenu, "key.keyboard.period");
        changed |= Keys.move(openPlayer, "key.keyboard.semicolon");
        if (changed |= Keys.move(toggle, "key.keyboard.right.bracket")) {
            KeyMapping.resetMapping();
            mc.options.save();
        }
        ConfigManager.save();
    }

    private static boolean move(KeyMapping k, String old) {
        if (!old.equals(k.saveString())) {
            return false;
        }
        k.setKey(k.getDefaultKey());
        return true;
    }

    public static void tick(Minecraft mc) {
        if (!movedKeys) {
            Keys.moveOldKeys(mc);
        }
        while (openMenu.consumeClick()) {
            if (mc.gui.screen() != null) continue;
            PhoneMenu.open(mc);
        }
        while (openPlayer.consumeClick()) {
            if (mc.gui.screen() != null) continue;
            mc.gui.setScreen((Screen)new PlayerScreen());
        }
        while (toggle.consumeClick()) {
            LFConfig c = ConfigManager.get();
            c.enabled = !c.enabled;
            ConfigManager.save();
            if (!c.enabled) {
                WorldLyrics.clear();
            }
            mc.gui.hud.setOverlayMessage((Component)Component.literal((String)"SkyLyrics: ").append((Component)Component.translatable((String)(c.enabled ? "lyricsflow.toggle.on" : "lyricsflow.toggle.off")).withStyle(c.enabled ? ChatFormatting.GREEN : ChatFormatting.RED)), false);
            UiSounds.play(c.enabled ? UiSounds.TOGGLE_ON : UiSounds.TOGGLE_OFF, 1.0f);
        }
        while (lyricsEarlier.consumeClick()) {
            Keys.nudge(mc, 100);
        }
        while (lyricsLater.consumeClick()) {
            Keys.nudge(mc, -100);
        }
        boolean media = ConfigManager.get().mediaKeys;
        while (playPause.consumeClick()) {
            if (!media) continue;
            MediaManager.get().playPause();
        }
        while (next.consumeClick()) {
            if (!media) continue;
            MediaManager.get().next();
        }
        while (previous.consumeClick()) {
            if (!media) continue;
            MediaManager.get().previous();
        }
    }

    public static KeyMapping[] all() {
        return new KeyMapping[]{openMenu, openPlayer, toggle, playPause, next, previous};
    }

    private static void nudge(Minecraft mc, int ms) {
        int v;
        Track t = MediaManager.get().track();
        if (t == null) {
            return;
        }
        LFConfig c = ConfigManager.get();
        if (c.songOffsets == null) {
            c.songOffsets = new HashMap<String, Integer>();
        }
        if ((v = Math.max(-5000, Math.min(5000, c.songOffsets.getOrDefault(t.key(), 0) + ms))) == 0) {
            c.songOffsets.remove(t.key());
        } else {
            c.songOffsets.put(t.key(), v);
        }
        ConfigManager.save();
        String shown = v == 0 ? "0 s" : (v > 0 ? "+" : "") + String.format(Locale.ROOT, "%.1f s", (double)v / 1000.0);
        mc.gui.hud.setOverlayMessage((Component)Component.literal((String)"SkyLyrics: ").append((Component)Component.translatable((String)"lyricsflow.toast.song_offset", (Object[])new Object[]{shown})), false);
        UiSounds.play(UiSounds.CLICK, ms > 0 ? 1.15f : 0.9f);
    }
}

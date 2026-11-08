/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
 *  net.minecraft.class_124
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_304
 *  net.minecraft.class_304$class_11900
 *  net.minecraft.class_310
 *  net.minecraft.class_3675$class_307
 *  net.minecraft.class_437
 */
package dev.lyricsflow;

import dev.lyricsflow.LyricsFlow;
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
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_437;

public final class Keys {
    public static final class_304.class_11900 CATEGORY = class_304.class_11900.method_74698((class_2960)LyricsFlow.id("main"));
    public static class_304 openMenu;
    public static class_304 playPause;
    public static class_304 next;
    public static class_304 previous;
    public static class_304 openPlayer;
    public static class_304 toggle;
    public static class_304 lyricsEarlier;
    public static class_304 lyricsLater;
    private static boolean movedKeys;

    private Keys() {
    }

    public static void register() {
        openMenu = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.lyricsflow.menu", class_3675.class_307.field_1668, 75, CATEGORY));
        playPause = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.lyricsflow.play_pause", class_3675.class_307.field_1668, 264, CATEGORY));
        next = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.lyricsflow.next", class_3675.class_307.field_1668, 262, CATEGORY));
        openPlayer = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.lyricsflow.player", class_3675.class_307.field_1668, 74, CATEGORY));
        toggle = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.lyricsflow.onoff", class_3675.class_307.field_1668, 79, CATEGORY));
        previous = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.lyricsflow.previous", class_3675.class_307.field_1668, 263, CATEGORY));
        lyricsEarlier = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.lyricsflow.earlier", class_3675.class_307.field_1668, 93, CATEGORY));
        lyricsLater = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.lyricsflow.later", class_3675.class_307.field_1668, 91, CATEGORY));
    }

    private static void moveOldKeys(class_310 mc) {
        movedKeys = true;
        if (!ConfigManager.moveKeys || mc.field_1690 == null) {
            return;
        }
        ConfigManager.moveKeys = false;
        boolean changed = false;
        changed |= Keys.move(openMenu, "key.keyboard.period");
        changed |= Keys.move(openPlayer, "key.keyboard.semicolon");
        if (changed |= Keys.move(toggle, "key.keyboard.right.bracket")) {
            class_304.method_1426();
            mc.field_1690.method_1640();
        }
        ConfigManager.save();
    }

    private static boolean move(class_304 k, String old) {
        if (!old.equals(k.method_1428())) {
            return false;
        }
        k.method_1422(k.method_1429());
        return true;
    }

    public static void tick(class_310 mc) {
        if (!movedKeys) {
            Keys.moveOldKeys(mc);
        }
        while (openMenu.method_1436()) {
            if (mc.field_1755 != null) continue;
            PhoneMenu.open(mc);
        }
        while (openPlayer.method_1436()) {
            if (mc.field_1755 != null) continue;
            mc.method_1507((class_437)new PlayerScreen());
        }
        while (toggle.method_1436()) {
            LFConfig c = ConfigManager.get();
            c.enabled = !c.enabled;
            ConfigManager.save();
            if (!c.enabled) {
                WorldLyrics.clear();
            }
            mc.field_1705.method_1758((class_2561)class_2561.method_43470((String)"SkyLyrics: ").method_10852((class_2561)class_2561.method_43471((String)(c.enabled ? "lyricsflow.toggle.on" : "lyricsflow.toggle.off")).method_27692(c.enabled ? class_124.field_1060 : class_124.field_1061)), false);
            UiSounds.play(c.enabled ? UiSounds.TOGGLE_ON : UiSounds.TOGGLE_OFF, 1.0f);
        }
        while (lyricsEarlier.method_1436()) {
            Keys.nudge(mc, 100);
        }
        while (lyricsLater.method_1436()) {
            Keys.nudge(mc, -100);
        }
        boolean media = ConfigManager.get().mediaKeys;
        while (playPause.method_1436()) {
            if (!media) continue;
            MediaManager.get().playPause();
        }
        while (next.method_1436()) {
            if (!media) continue;
            MediaManager.get().next();
        }
        while (previous.method_1436()) {
            if (!media) continue;
            MediaManager.get().previous();
        }
    }

    public static class_304[] all() {
        return new class_304[]{openMenu, openPlayer, toggle, playPause, next, previous};
    }

    private static void nudge(class_310 mc, int ms) {
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
        mc.field_1705.method_1758((class_2561)class_2561.method_43470((String)"SkyLyrics: ").method_10852((class_2561)class_2561.method_43469((String)"lyricsflow.toast.song_offset", (Object[])new Object[]{shown})), false);
        UiSounds.play(UiSounds.CLICK, ms > 0 ? 1.15f : 0.9f);
    }
}


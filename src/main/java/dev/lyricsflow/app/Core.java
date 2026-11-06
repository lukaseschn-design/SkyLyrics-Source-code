/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
 *  net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
 *  net.fabricmc.fabric.api.event.player.AttackEntityCallback
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.dimension.DimensionType
 *  net.minecraft.resources.Identifier
 *  net.minecraft.client.Minecraft
 *  dev.lyricsflow.compat.LegacyBufferSource
 *  dev.lyricsflow.compat.LegacyBufferSource$BufferSource
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package dev.lyricsflow.app;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.lyricsflow.app.config.Config;
import dev.lyricsflow.app.config.Storage;
import dev.lyricsflow.app.cosmetic.DeathFx;
import dev.lyricsflow.app.cosmetic.PhoneMenu;
import dev.lyricsflow.app.hud.Gfx;
import dev.lyricsflow.app.hud.GlassHotbar;
import dev.lyricsflow.app.lyrics.SpotifyClient;
import dev.lyricsflow.app.sky.SkyImage;
import dev.lyricsflow.app.sky.SkyRenderer;
import dev.lyricsflow.app.sky.WorldSky;
import dev.lyricsflow.app.util.AutoPerf;
import dev.lyricsflow.config.ConfigManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import dev.lyricsflow.compat.LegacyBufferSource;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Core {
    public static final Logger LOG = LoggerFactory.getLogger((String)"SkyLyrics");
    private static Config config = new Config();

    private Core() {
    }

    public static Config cfg() {
        return config;
    }

    public static boolean skyWorld(Level level) {
        if (level == null) {
            return false;
        }
        if (level.dimension() == Level.OVERWORLD) {
            return true;
        }
        if (level.dimension() == Level.NETHER || level.dimension() == Level.END) {
            return false;
        }
        try {
            DimensionType t = level.dimensionType();
            return t.hasSkyLight() && !t.hasCeiling();
        }
        catch (Throwable e) {
            return true;
        }
    }

    public static void applyJson(JsonObject o) {
        try {
            Config c = (Config)new Gson().fromJson((JsonElement)o, Config.class);
            if (c == null) {
                return;
            }
            c.clamp();
            c.uiFont = Gfx.FONT_IDS.length;
            config = c;
            Core.saveConfig();
            SkyImage.reload();
        }
        catch (Throwable e) {
            LOG.warn("[SkyLyrics] Could not load the sky part of the profile", e);
        }
    }

    public static void resetConfig() {
        Config fresh = new Config();
        fresh.clamp();
        fresh.uiFont = Gfx.FONT_IDS.length;
        config = fresh;
        Core.saveConfig();
    }

    public static void saveConfig() {
        config.clamp();
        Storage.save("settings.json", config);
    }

    public static void init() {
        config = Storage.load("settings.json", Config.class, Config::new);
        config.clamp();
        Core.config.uiFont = Gfx.FONT_IDS.length;
        Core.saveConfig();
        HudElementRegistry.addFirst((Identifier)Identifier.fromNamespaceAndPath((String)"lyricsflow", (String)"sky_flash"), SkyRenderer::render);
        HudElementRegistry.replaceElement((Identifier)VanillaHudElements.CROSSHAIR, original -> (g, delta) -> {
            if (!PhoneMenu.pointerShown()) {
                original.extractRenderState(g, delta);
            }
        });
        HudElementRegistry.replaceElement((Identifier)VanillaHudElements.HOTBAR, GlassHotbar::wrap);
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(WorldSky::render);
        LevelRenderEvents.END_MAIN.register(ctx -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || ctx.poseStack() == null) {
                return;
            }
            float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            try (LegacyBufferSource.BufferSource buf = new LegacyBufferSource.BufferSource(ctx.submitNodeCollector())) {
                try {
                    PhoneMenu.render(mc, buf, new Matrix4f((Matrix4fc)ctx.poseStack().last().pose()), pt);
                }
                catch (Throwable e) {
                    LOG.debug("world menu draw failed", e);
                }
            }
        });
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            DeathFx.onAttack(entity);
            return InteractionResult.PASS;
        });
        ClientTickEvents.END_CLIENT_TICK.register(Core::tick);
        ClientTickEvents.START_CLIENT_TICK.register(PhoneMenu::startTick);
    }

    private static void tick(Minecraft mc) {
        if (!ConfigManager.get().enabled) {
            PhoneMenu.tick(mc);
            DeathFx.tick(mc);
            return;
        }
        SpotifyClient.tick();
        AutoPerf.tick(mc);
        PhoneMenu.tick(mc);
        DeathFx.tick(mc);
        SkyRenderer.tick(mc);
    }
}

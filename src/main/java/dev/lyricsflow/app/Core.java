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
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.class_1269;
import net.minecraft.class_1937;
import net.minecraft.class_2874;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4597;
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

    public static boolean skyWorld(class_1937 level) {
        if (level == null) {
            return false;
        }
        if (level.method_27983() == class_1937.field_25179) {
            return true;
        }
        if (level.method_27983() == class_1937.field_25180 || level.method_27983() == class_1937.field_25181) {
            return false;
        }
        try {
            class_2874 t = level.method_8597();
            return t.comp_642() && !t.comp_643();
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
        HudElementRegistry.addFirst((class_2960)class_2960.method_60655((String)"lyricsflow", (String)"sky_flash"), SkyRenderer::render);
        HudElementRegistry.replaceElement((class_2960)VanillaHudElements.CROSSHAIR, original -> (g, delta) -> {
            if (!PhoneMenu.pointerShown()) {
                original.render(g, delta);
            }
        });
        HudElementRegistry.replaceElement((class_2960)VanillaHudElements.HOTBAR, GlassHotbar::wrap);
        WorldRenderEvents.AFTER_ENTITIES.register(WorldSky::render);
        WorldRenderEvents.END_MAIN.register(ctx -> {
            class_310 mc = class_310.method_1551();
            if (mc.field_1724 == null || ctx.matrices() == null) {
                return;
            }
            class_4597.class_4598 buf = mc.method_22940().method_23000();
            float pt = mc.method_61966().method_60637(false);
            try {
                PhoneMenu.render(mc, (class_4597)buf, new Matrix4f((Matrix4fc)ctx.matrices().method_23760().method_23761()), pt);
            }
            catch (Throwable e) {
                LOG.debug("world menu draw failed", e);
            }
            buf.method_22993();
        });
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            DeathFx.onAttack(entity);
            return class_1269.field_5811;
        });
        ClientTickEvents.END_CLIENT_TICK.register(Core::tick);
        ClientTickEvents.START_CLIENT_TICK.register(PhoneMenu::startTick);
    }

    private static void tick(class_310 mc) {
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


package dev.lyricsflow;

import dev.lyricsflow.Keys;
import dev.lyricsflow.LyricsFlow;
import dev.lyricsflow.app.Core;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.media.CosmeticSync;
import dev.lyricsflow.media.MediaManager;
import dev.lyricsflow.perf.AutoPerf;
import dev.lyricsflow.render.Chroma;
import dev.lyricsflow.render.CosmeticLayer;
import dev.lyricsflow.render.ExtraHud;
import dev.lyricsflow.render.HelmetLayer;
import dev.lyricsflow.render.Hud;
import dev.lyricsflow.render.LivePing;
import dev.lyricsflow.render.WorldLyrics;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.class_10055;
import net.minecraft.class_1007;
import net.minecraft.class_1269;
import net.minecraft.class_2960;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_591;

public class SkyLyricsClient
implements ClientModInitializer {
    public void onInitializeClient() {
        ConfigManager.load();
        Keys.register();
        Core.init();
        HudElementRegistry.attachElementBefore((class_2960)VanillaHudElements.MISC_OVERLAYS, (class_2960)LyricsFlow.id("world_lyrics"), Hud::renderWorldLayer);
        HudElementRegistry.attachElementBefore((class_2960)VanillaHudElements.CHAT, (class_2960)LyricsFlow.id("top_bar"), Hud::renderTopLayer);
        HudElementRegistry.attachElementBefore((class_2960)VanillaHudElements.CHAT, (class_2960)LyricsFlow.id("extra_hud"), ExtraHud::render);
        ExtraHud.init();
        ClientTickEvents.END_CLIENT_TICK.register(Keys::tick);
        ClientTickEvents.END_CLIENT_TICK.register(LivePing::tick);
        ClientTickEvents.END_CLIENT_TICK.register(CosmeticSync::tick);
        ClientTickEvents.END_CLIENT_TICK.register(AutoPerf::tick);
        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> {
            if (Chroma.drawsLate() || WorldLyrics.lateForShaders()) {
                Chroma.deferred = true;
                return;
            }
            Hud.renderWorldSky(ctx.matrices(), ctx.consumers());
            WorldLyrics.renderWorld(ctx.matrices(), ctx.consumers());
        });
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, ctx) -> {
            if (renderer instanceof class_1007) {
                class_1007 r = (class_1007)renderer;
                SkyLyricsClient.registerLayers(helper, r);
            }
        });
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> class_1269.field_5811);
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> MediaManager.get().start());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> MediaManager.get().stop());
        LyricsFlow.LOG.info("SkyLyrics {} ready", (Object)"v1.0");
    }

    private static void registerLayers(LivingEntityFeatureRendererRegistrationCallback.RegistrationHelper helper, class_1007<?> r) {
        helper.register((class_3887)new HelmetLayer((class_3883<class_10055, class_591>)r));
        helper.register((class_3887)new CosmeticLayer((class_3883<class_10055, class_591>)r));
    }
}


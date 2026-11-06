/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback
 *  net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback$RegistrationHelper
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
 *  net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
 *  net.fabricmc.fabric.api.event.player.AttackEntityCallback
 *  net.minecraft.client.renderer.entity.state.AvatarRenderState
 *  net.minecraft.client.renderer.entity.player.AvatarRenderer
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.resources.Identifier
 *  net.minecraft.client.renderer.entity.RenderLayerParent
 *  net.minecraft.client.renderer.entity.layers.RenderLayer
 *  net.minecraft.client.model.player.PlayerModel
 */
package dev.lyricsflow;

import dev.lyricsflow.Keys;
import dev.lyricsflow.SkyLyrics;
import dev.lyricsflow.app.Core;
import dev.lyricsflow.compat.LegacyBufferSource;
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
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.InteractionResult;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.player.PlayerModel;

public class SkyLyricsClient
implements ClientModInitializer {
    public void onInitializeClient() {
        ConfigManager.load();
        Keys.register();
        Core.init();
        HudElementRegistry.attachElementBefore((Identifier)VanillaHudElements.MISC_OVERLAYS, (Identifier)SkyLyrics.id("world_lyrics"), Hud::renderWorldLayer);
        HudElementRegistry.attachElementBefore((Identifier)VanillaHudElements.CHAT, (Identifier)SkyLyrics.id("top_bar"), Hud::renderTopLayer);
        HudElementRegistry.attachElementBefore((Identifier)VanillaHudElements.CHAT, (Identifier)SkyLyrics.id("extra_hud"), ExtraHud::render);
        ExtraHud.init();
        ClientTickEvents.END_CLIENT_TICK.register(Keys::tick);
        ClientTickEvents.END_CLIENT_TICK.register(LivePing::tick);
        ClientTickEvents.END_CLIENT_TICK.register(CosmeticSync::tick);
        ClientTickEvents.END_CLIENT_TICK.register(AutoPerf::tick);
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(ctx -> {
            if (Chroma.drawsLate() || WorldLyrics.lateForShaders()) {
                Chroma.deferred = true;
                return;
            }
            try (LegacyBufferSource.BufferSource buffers = new LegacyBufferSource.BufferSource(ctx.submitNodeCollector())) {
                Hud.renderWorldSky(ctx.poseStack(), buffers);
                WorldLyrics.renderWorld(ctx.poseStack(), buffers);
            }
        });
        LivingEntityRenderLayerRegistrationCallback.EVENT.register((type, renderer, helper, ctx) -> {
            if (renderer instanceof AvatarRenderer) {
                AvatarRenderer r = (AvatarRenderer)renderer;
                SkyLyricsClient.registerLayers(helper, r);
            }
        });
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> InteractionResult.PASS);
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> MediaManager.get().start());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> MediaManager.get().stop());
        SkyLyrics.LOG.info("SkyLyrics {} ready", (Object)"v1.0");
    }

    private static void registerLayers(LivingEntityRenderLayerRegistrationCallback.RegistrationHelper helper, AvatarRenderer<?> r) {
        helper.register((RenderLayer)new HelmetLayer((RenderLayerParent<AvatarRenderState, PlayerModel>)r));
        helper.register((RenderLayer)new CosmeticLayer((RenderLayerParent<AvatarRenderState, PlayerModel>)r));
    }
}

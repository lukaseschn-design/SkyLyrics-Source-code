package Yuki.lumiclient.gui;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

import java.io.InputStream;

public class LotusLoadingOverlay {

    public static final Identifier BRAND_LOGO = Identifier.fromNamespaceAndPath("lyricsflow", "textures/gui/skylyrics_brand.png");
    private static boolean brandLoaded = false;

    public static void ensureBrandLoaded(Minecraft client) {
        if (brandLoaded) return;
        try (InputStream in = LotusLoadingOverlay.class.getResourceAsStream("/assets/lyricsflow/textures/gui/skylyrics_brand.png")) {
            if (in != null) {
                NativeImage img = NativeImage.read(in);
                DynamicTexture tex = new DynamicTexture(() -> "skylyrics_brand", img);
                tex.upload();
                client.getTextureManager().register(BRAND_LOGO, tex);
                brandLoaded = true;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void render(GuiGraphicsExtractor context, int screenW, int screenH, float progress, String statusText, float alpha) {
        if (alpha <= 0.002f) return;

        Minecraft client = Minecraft.getInstance();
        ensureBrandLoaded(client);

        int a = Math.round(alpha * 255.0f);

        // 1. Edler dunkler Hintergrund (#0A0C13)
        int bgColor = (a << 24) | 0x0A0C13;
        context.fill(0, 0, screenW, screenH, bgColor);

        // 2. SkyLyrics Branding (512x192 Textur)
        int brandH = Math.max(52, Math.min(84, Math.round(screenH * 0.135f)));
        int brandW = Math.round((float) brandH * (512.0f / 192.0f));

        int barW = Math.max(brandW - 20, Math.min(260, Math.round(screenW * 0.22f)));
        int barH = 3;

        boolean hasStatus = statusText != null && !statusText.isEmpty();
        int totalH = brandH + 24 + barH + (hasStatus ? 20 : 0);
        int brandX = (screenW - brandW) / 2;
        int brandY = (screenH - totalH) / 2 - 8;

        if (brandLoaded) {
            int logoColor = (a << 24) | 0x00FFFFFF;
            context.blit(RenderPipelines.MOJANG_LOGO, BRAND_LOGO, brandX, brandY, 0.0f, 0.0f, brandW, brandH, 512, 192, 512, 192, logoColor);
        }

        // 3. Fortschrittsleiste
        int barX = (screenW - barW) / 2;
        int barY = brandY + brandH + 24;

        int trackAlpha = Math.round(alpha * 40.0f);
        context.fill(barX, barY, barX + barW, barY + barH, (trackAlpha << 24) | 0x00FFFFFF);

        float clampedProgress;
        if (progress >= 0.0f) {
            clampedProgress = Mth.clamp(progress, 0.0f, 1.0f);
        } else {
            long time = System.currentTimeMillis();
            clampedProgress = (float) (0.5 + 0.5 * Math.sin(time * 0.0035));
        }

        int filledW = Math.round((float) barW * clampedProgress);
        if (filledW > 0) {
            for (int i = 0; i < filledW; i++) {
                float t = (float) i / (float) Math.max(1, barW - 1);
                int r = Math.round((1.0f - t) * 182f + t * 127f);
                int g = Math.round((1.0f - t) * 136f + t * 230f);
                int b = Math.round((1.0f - t) * 255f + t * 255f);
                int col = (a << 24) | (r << 16) | (g << 8) | b;
                context.fill(barX + i, barY, barX + i + 1, barY + barH, col);
            }

            int glowAlpha = Math.round(alpha * 35.0f);
            if (glowAlpha > 0) {
                int glowCol = (glowAlpha << 24) | 0x00B688FF;
                context.fill(barX, barY - 1, barX + filledW, barY, glowCol);
                context.fill(barX, barY + barH, barX + filledW, barY + barH + 1, glowCol);
            }
        }

        // 4. Status-Text
        if (hasStatus && client.font != null) {
            int textAlpha = Math.round(alpha * 190.0f);
            int textCol = (textAlpha << 24) | 0xCAD4E0;
            int textY = barY + barH + 12;
            context.centeredText(client.font, Component.literal(statusText), screenW / 2, textY, textCol);
        }
    }
}

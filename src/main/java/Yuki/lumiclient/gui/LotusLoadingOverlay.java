package Yuki.lumiclient.gui;

import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_10799;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3532;

import java.io.InputStream;

public class LotusLoadingOverlay {

    public static final class_2960 BRAND_LOGO = class_2960.method_60655("lyricsflow", "textures/gui/skylyrics_brand.png");
    private static boolean brandLoaded = false;

    public static void ensureBrandLoaded(class_310 client) {
        if (brandLoaded) return;
        try (InputStream in = LotusLoadingOverlay.class.getResourceAsStream("/assets/lyricsflow/textures/gui/skylyrics_brand.png")) {
            if (in != null) {
                class_1011 img = class_1011.method_4309(in);
                class_1043 tex = new class_1043(() -> "skylyrics_brand", img);
                tex.method_4524();
                client.method_1531().method_4616(BRAND_LOGO, tex);
                brandLoaded = true;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void render(class_332 context, int screenW, int screenH, float progress, String statusText, float alpha) {
        if (alpha <= 0.002f) return;

        class_310 client = class_310.method_1551();
        ensureBrandLoaded(client);

        int a = Math.round(alpha * 255.0f);

        // 1. Edler dunkler Hintergrund (#0A0C13)
        int bgColor = (a << 24) | 0x0A0C13;
        context.method_25294(0, 0, screenW, screenH, bgColor);

        // 2. SkyLyrics Branding (512x192 Textur, knackig scharf & prominent)
        int brandH = Math.max(68, Math.min(108, Math.round(screenH * 0.165f)));
        int brandW = Math.round((float) brandH * (512.0f / 192.0f));

        int barW = Math.max(brandW - 10, Math.min(320, Math.round(screenW * 0.25f)));
        int barH = 3;

        boolean hasStatus = statusText != null && !statusText.isEmpty();
        int totalH = brandH + 28 + barH + (hasStatus ? 20 : 0);
        int brandX = (screenW - brandW) / 2;
        int brandY = (screenH - totalH) / 2 - 10;

        if (brandLoaded) {
            int logoColor = (a << 24) | 0x00FFFFFF;
            context.method_25293(class_10799.field_56839, BRAND_LOGO, brandX, brandY, 0.0f, 0.0f, brandW, brandH, 512, 192, 512, 192, logoColor);
        } else if (client.field_1772 != null) {
            // Eleganter Fallback Text
            int titleCol = (a << 24) | 0x45D6F5;
            context.method_27534(client.field_1772, class_2561.method_43470("SkyLyrics"), screenW / 2, brandY + brandH / 2 - 6, titleCol);
        }

        // 3. Fortschrittsleiste
        int barX = (screenW - barW) / 2;
        int barY = brandY + brandH + 24;

        int trackAlpha = Math.round(alpha * 40.0f);
        context.method_25294(barX, barY, barX + barW, barY + barH, (trackAlpha << 24) | 0x00FFFFFF);

        float clampedProgress;
        if (progress >= 0.0f) {
            clampedProgress = class_3532.method_15363(progress, 0.0f, 1.0f);
        } else {
            long time = System.currentTimeMillis();
            clampedProgress = (float) (0.5 + 0.5 * Math.sin(time * 0.0035));
        }

        int filledW = Math.round((float) barW * clampedProgress);
        if (filledW > 0) {
            for (int i = 0; i < filledW; i++) {
                float t = (float) i / (float) Math.max(1, barW - 1);
                int r = Math.round((1.0f - t) * 182f + t * 69f);
                int g = Math.round((1.0f - t) * 136f + t * 214f);
                int b = Math.round((1.0f - t) * 255f + t * 245f);
                int col = (a << 24) | (r << 16) | (g << 8) | b;
                context.method_25294(barX + i, barY, barX + i + 1, barY + barH, col);
            }

            int glowAlpha = Math.round(alpha * 38.0f);
            if (glowAlpha > 0) {
                int glowCol = (glowAlpha << 24) | 0x0045D6F5;
                context.method_25294(barX, barY - 1, barX + filledW, barY, glowCol);
                context.method_25294(barX, barY + barH, barX + filledW, barY + barH + 1, glowCol);
            }
        }

        // 4. Status-Text
        if (hasStatus && client.field_1772 != null) {
            int textAlpha = Math.round(alpha * 190.0f);
            int textCol = (textAlpha << 24) | 0xCAD4E0;
            int textY = barY + barH + 12;
            context.method_27534(client.field_1772, class_2561.method_43470(statusText), screenW / 2, textY, textCol);
        }
    }
}

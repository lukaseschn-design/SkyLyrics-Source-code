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

        int bgColor = (a << 24) | 0x0A0C13;
        context.method_25294(0, 0, screenW, screenH, bgColor);

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
            int titleCol = (a << 24) | 0x45D6F5;
            context.method_27534(client.field_1772, class_2561.method_43470("SkyLyrics"), screenW / 2, brandY + brandH / 2 - 6, titleCol);
        }

        int barX = (screenW - barW) / 2;
        int barY = brandY + brandH + 24;

        int trackAlpha = Math.round(alpha * 40.0f);
        context.method_25294(barX, barY, barX + barW, barY + barH, (trackAlpha << 24) | 0xFFFFFF);

        float p = class_3532.method_15363(progress, 0.0f, 1.0f);
        int fillW = Math.round(barW * p);
        if (fillW > 0) {
            int glowAlpha = Math.round(alpha * 70.0f);
            if (glowAlpha > 0) {
                context.method_25294(barX - 1, barY - 1, barX + fillW + 1, barY + barH + 1, (glowAlpha << 24) | 0x45D6F5);
            }
            context.method_25296(barX, barY, barX + fillW, barY + barH, (a << 24) | 0x45D6F5, (a << 24) | 0x8A2BE2);
        }

        if (hasStatus && client.field_1772 != null) {
            int textAlpha = Math.round(alpha * 190.0f);
            int textColor = (textAlpha << 24) | 0xB0C4DE;
            int textY = barY + barH + 8;
            context.method_27534(client.field_1772, class_2561.method_43470(statusText), screenW / 2, textY, textColor);
        }
    }
}

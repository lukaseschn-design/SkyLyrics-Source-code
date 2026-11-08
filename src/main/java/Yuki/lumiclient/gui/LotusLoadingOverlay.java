package Yuki.lumiclient.gui;

import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_10799;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import org.joml.Matrix3x2fStack;

import java.io.InputStream;

public class LotusLoadingOverlay {

    public static final class_2960 BRAND_LOGO = class_2960.method_60655("lyricsflow", "textures/gui/skylyrics_human_icon.png");
    private static boolean brandLoaded = false;

    public static void ensureBrandLoaded(class_310 client) {
        if (brandLoaded) return;
        try (InputStream in = LotusLoadingOverlay.class.getResourceAsStream("/assets/lyricsflow/icon.png")) {
            if (in != null) {
                class_1011 img = class_1011.method_4309(in);
                class_1043 tex = new class_1043(() -> "skylyrics_human_icon", img);
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

        // Dunkler Hintergrund
        int bgColor = (a << 24) | 0x0A0C13;
        context.method_25294(0, 0, screenW, screenH, bgColor);

        class_327 font = client.field_1772;
        class_2561 title = class_2561.method_43470("§b§lSky§f§lLyrics");

        int iconSize = 42;
        float textScale = 2.4f;
        int gap = 12;

        int textW = font != null ? font.method_27525(title) : 60;
        int scaledTextW = Math.round(textW * textScale);
        int totalHeaderW = iconSize + gap + scaledTextW;

        int centerY = screenH / 2 - 20;
        int startX = (screenW - totalHeaderW) / 2;

        // 1. Menschliches Pixel-Art-Logo (links)
        if (brandLoaded) {
            int logoColor = (a << 24) | 0x00FFFFFF;
            int iconX = startX;
            int iconY = centerY - iconSize / 2;
            context.method_25293(class_10799.field_56883, BRAND_LOGO, iconX, iconY, 0.0f, 0.0f, iconSize, iconSize, 512, 512, 512, 512, logoColor);
        }

        // 2. Identischer SkyLyrics-Text wie im Hauptmenü (rechts neben dem Logo)
        if (font != null) {
            Matrix3x2fStack matrices = context.method_51448();
            matrices.pushMatrix();
            matrices.translate((float) (startX + iconSize + gap), (float) centerY);
            matrices.scale(textScale, textScale);
            context.method_51439(font, title, 0, -4, (a << 24) | 0xFFFFFF, true);
            matrices.popMatrix();
        }

        // 3. Fortschrittsbalken
        int barW = Math.max(totalHeaderW, Math.min(280, Math.round(screenW * 0.25f)));
        int barH = 3;
        int barX = (screenW - barW) / 2;
        int barY = centerY + iconSize / 2 + 18;

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

        // 4. Status-Text
        if (statusText != null && !statusText.isEmpty() && font != null) {
            int textAlpha = Math.round(alpha * 190.0f);
            int textColor = (textAlpha << 24) | 0xB0C4DE;
            int textY = barY + barH + 8;
            context.method_27534(font, class_2561.method_43470(statusText), screenW / 2, textY, textColor);
        }
    }
}

package Yuki.lumiclient.gui;

import Yuki.lumiclient.util.Colors;
import Yuki.lumiclient.util.UiSounds;
import net.minecraft.class_11876;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_357;
import net.minecraft.class_4264;
import org.joml.Matrix3x2fStack;

import java.util.WeakHashMap;

public final class LotusButtonRenderer {

    public static final int GLASS_BG = 0x55101824;
    public static final int GLASS_BG_HOVER = 0x801A283C;
    public static final int GLASS_BG_DISABLED = 0x220B111A;

    public static final int GLASS_BORDER = 0x458098B0;
    public static final int GLASS_BORDER_HOVER = 0xEE9FE2FF;
    public static final int GLASS_BORDER_DISABLED = 0x20506070;

    public static final int ACCENT_CYAN = 0xFF7FE6FF;

    public static final int TEXT_WHITE = 0xFFFFFFFF;
    public static final int TEXT_IDLE = 0xFFD8E4F0;
    public static final int TEXT_DISABLED = 0xFF6A7B8C;

    private static final WeakHashMap<class_339, Float> HOVER_MAP = new WeakHashMap<>();
    private static final WeakHashMap<class_339, Boolean> SOUND_STATE_MAP = new WeakHashMap<>();

    private LotusButtonRenderer() {}

    public static void renderButton(class_4264 button, class_332 g, int mouseX, int mouseY, float delta) {
        int x = button.method_46426();
        int y = button.method_46427();
        int w = button.method_25368();
        int h = button.method_25364();
        boolean active = button.field_22763;
        boolean hovered = button.method_25367(); // isHoveredOrFocused

        float hover = updateHover(button, active && hovered);

        if (active && hovered) {
            try {
                g.method_74037(class_11876.field_62455);
            } catch (Throwable ignored) {}
        }

        if (active && hover > 0.02f) {
            drawOuterGlow(g, x, y, w, h, hover);
        }

        int bg;
        int border;
        if (!active) {
            bg = GLASS_BG_DISABLED;
            border = GLASS_BORDER_DISABLED;
        } else {
            bg = Colors.lerp(GLASS_BG, GLASS_BG_HOVER, hover);
            border = Colors.lerp(GLASS_BORDER, GLASS_BORDER_HOVER, hover);
        }

        drawRoundedFill(g, x, y, w, h, bg);

        if (active) {
            drawGlassSheen(g, x, y, w, h, hover);
        }

        drawRoundedBorder(g, x, y, w, h, border);

        class_310 client = class_310.method_1551();
        class_327 font = client != null ? client.field_1772 : null;
        if (font != null) {
            class_2561 msg = button.method_25369();
            int textColor = !active ? TEXT_DISABLED : Colors.lerp(TEXT_IDLE, TEXT_WHITE, hover);
            drawCenteredButtonText(g, font, msg, x, y, w, h, textColor);
        }
    }

    public static void renderSlider(class_357 slider, class_332 g, int mouseX, int mouseY, float delta, double value) {
        int x = slider.method_46426();
        int y = slider.method_46427();
        int w = slider.method_25368();
        int h = slider.method_25364();
        boolean active = slider.field_22763;
        boolean hovered = slider.method_25367(); // isHoveredOrFocused

        float hover = updateHover(slider, active && hovered);
        double clampedVal = Math.clamp(value, 0.0, 1.0);

        if (active && hovered) {
            try {
                g.method_74037(class_11876.field_62455);
            } catch (Throwable ignored) {}
        }

        if (active && hover > 0.02f) {
            drawOuterGlow(g, x, y, w, h, hover * 0.75f);
        }

        int trackBg = !active ? GLASS_BG_DISABLED : Colors.lerp(GLASS_BG, 0x65142232, hover);
        int trackBorder = !active ? GLASS_BORDER_DISABLED : Colors.lerp(GLASS_BORDER, GLASS_BORDER_HOVER, hover);

        drawRoundedFill(g, x, y, w, h, trackBg);

        int handleOffset = (int) (clampedVal * (w - 8));
        int progressW = handleOffset + 4;
        if (progressW > 3 && active) {
            int progressCol = Colors.withAlpha(ACCENT_CYAN, 0.20f + 0.15f * hover);
            drawProgressFill(g, x, y, progressW, h, progressCol);
        }

        if (active) {
            drawGlassSheen(g, x, y, w, h, hover);
        }

        drawRoundedBorder(g, x, y, w, h, trackBorder);

        int hx = x + handleOffset;
        int hw = 8;
        int hy = y + 1;
        int hh = h - 2;

        int handleBg = !active ? 0x60303840 : (hovered ? 0xDD7FE6FF : 0xBB1E3246);
        int handleBorder = !active ? 0x40607080 : (hovered ? 0xFFFFFFFF : 0xFF7FE6FF);

        drawHandle(g, hx, hy, hw, hh, handleBg, handleBorder, hovered && active);

        class_310 client = class_310.method_1551();
        class_327 font = client != null ? client.field_1772 : null;
        if (font != null) {
            class_2561 msg = slider.method_25369();
            int textColor = !active ? TEXT_DISABLED : Colors.lerp(TEXT_IDLE, TEXT_WHITE, hover);
            drawCenteredButtonText(g, font, msg, x, y, w, h, textColor);
        }
    }

    private static float updateHover(class_339 widget, boolean isHovered) {
        float target = isHovered ? 1.0f : 0.0f;
        Float currentObj = HOVER_MAP.get(widget);
        float current = currentObj != null ? currentObj : target;
        current += (target - current) * 0.30f;
        if (Math.abs(target - current) < 0.005f) {
            current = target;
        }
        HOVER_MAP.put(widget, current);

        Boolean prevSound = SOUND_STATE_MAP.get(widget);
        if (prevSound == null) prevSound = false;
        if (isHovered && !prevSound) {
            try {
                UiSounds.hover();
            } catch (Throwable ignored) {}
        }
        SOUND_STATE_MAP.put(widget, isHovered);

        return current;
    }

    private static void drawOuterGlow(class_332 g, int x, int y, int w, int h, float hover) {
        int alpha = (int) (0x35 * hover);
        if (alpha <= 0) return;
        int glowColor = (alpha << 24) | 0x7FE6FF;

        g.method_25294(x + 2, y - 1, x + w - 2, y, glowColor);
        g.method_25294(x + 2, y + h, x + w - 2, y + h + 1, glowColor);

        g.method_25294(x - 1, y + 2, x, y + h - 2, glowColor);
        g.method_25294(x + w, y + 2, x + w + 1, y + h - 2, glowColor);

        g.method_25294(x, y - 1, x + 2, y, glowColor);
        g.method_25294(x - 1, y, x, y + 2, glowColor);

        g.method_25294(x + w - 2, y - 1, x + w, y, glowColor);
        g.method_25294(x + w, y, x + w + 1, y + 2, glowColor);

        g.method_25294(x, y + h, x + 2, y + h + 1, glowColor);
        g.method_25294(x - 1, y + h - 2, x, y + h, glowColor);

        g.method_25294(x + w - 2, y + h, x + w, y + h + 1, glowColor);
        g.method_25294(x + w, y + h - 2, x + w + 1, y + h, glowColor);
    }

    private static void drawGlassSheen(class_332 g, int x, int y, int w, int h, float hover) {
        int sheenTop = Colors.withAlpha(0xFFFFFF, 0.12f + 0.08f * hover);
        int sheenBot = Colors.withAlpha(0xFFFFFF, 0.01f);
        int halfH = Math.max(2, (h - 2) / 2);
        g.method_25296(x + 2, y + 1, x + w - 2, y + 1 + halfH, sheenTop, sheenBot);

        int specColor = Colors.withAlpha(0xFFFFFF, 0.20f + 0.18f * hover);
        g.method_25294(x + 3, y + 1, x + w - 3, y + 2, specColor);
    }

    public static void drawRoundedFill(class_332 g, int x, int y, int w, int h, int color) {
        if (w <= 0 || h <= 0) return;
        if (w < 8 || h < 8) {
            g.method_25294(x, y, x + w, y + h, color);
            return;
        }

        g.method_25294(x + 2, y + 1, x + w - 2, y + 2, color);
        g.method_25294(x + 1, y + 2, x + w - 1, y + h - 2, color);
        g.method_25294(x + 2, y + h - 2, x + w - 2, y + h - 1, color);
    }

    public static void drawRoundedBorder(class_332 g, int x, int y, int w, int h, int color) {
        if (w <= 0 || h <= 0) return;
        if (w < 8 || h < 8) {
            g.method_73198(x, y, w, h, color);
            return;
        }

        g.method_25294(x + 2, y, x + w - 2, y + 1, color);
        g.method_25294(x + 2, y + h - 1, x + w - 2, y + h, color);

        g.method_25294(x, y + 2, x + 1, y + h - 2, color);
        g.method_25294(x + w - 1, y + 2, x + w, y + h - 2, color);

        g.method_25294(x + 1, y + 1, x + 2, y + 2, color);
        g.method_25294(x + w - 2, y + 1, x + w - 1, y + 2, color);
        g.method_25294(x + 1, y + h - 2, x + 2, y + h - 1, color);
        g.method_25294(x + w - 2, y + h - 2, x + w - 1, y + h - 1, color);
    }

    private static void drawProgressFill(class_332 g, int x, int y, int pw, int h, int color) {
        if (pw <= 2) return;
        g.method_25294(x + 2, y + 1, x + pw, y + 2, color);
        g.method_25294(x + 1, y + 2, x + pw, y + h - 2, color);
        g.method_25294(x + 2, y + h - 2, x + pw, y + h - 1, color);
    }

    private static void drawHandle(class_332 g, int x, int y, int w, int h, int fill, int border, boolean hovered) {
        if (hovered) {
            g.method_25294(x - 1, y, x + w + 1, y + h, Colors.withAlpha(ACCENT_CYAN, 0.30f));
        }
        g.method_25294(x + 1, y, x + w - 1, y + 1, border);
        g.method_25294(x + 1, y + h - 1, x + w - 1, y + h, border);
        g.method_25294(x, y + 1, x + 1, y + h - 1, border);
        g.method_25294(x + w - 1, y + 1, x + w, y + h - 1, border);

        g.method_25294(x + 1, y + 1, x + w - 1, y + h - 1, fill);
        g.method_25294(x + 2, y + 2, x + w - 2, y + 3, Colors.withAlpha(0xFFFFFF, 0.45f));
    }

    public static void drawCenteredButtonText(class_332 g, class_327 font, class_2561 text, int x, int y, int w, int h, int color) {
        if (text == null) return;
        int textW = font.method_27525(text);
        int cx = x + w / 2;
        int cy = y + (h - 8) / 2; // Exakte Minecraft Text-Zentrierung
        if (textW <= w - 6) {
            g.method_27534(font, text, cx, cy, color);
        } else {
            float scale = (float) (w - 6) / (float) textW;
            Matrix3x2fStack matrices = g.method_51448();
            matrices.pushMatrix();
            matrices.translate((float) cx, (float) (y + h / 2));
            matrices.scale(scale, scale);
            g.method_51439(font, text, -textW / 2, -4, color, true);
            matrices.popMatrix();
        }
    }
}
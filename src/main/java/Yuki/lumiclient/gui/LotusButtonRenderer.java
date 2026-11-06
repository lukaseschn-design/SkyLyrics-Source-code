package Yuki.lumiclient.gui;

import Yuki.lumiclient.util.Colors;
import Yuki.lumiclient.util.UiSounds;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractButton;
import org.joml.Matrix3x2fStack;

import java.util.WeakHashMap;

/**
 * LotusButtonRenderer - Überträgt das moderne Frosted-Glass Pill Design des Startmenüs
 * (LotusTitleScreen / Aurora Aesthetic) auf alle Menü-Buttons und Regler im gesamten Client.
 */
public final class LotusButtonRenderer {

    // ── Design Tokens (Exakt abgestimmt auf LotusTitleScreen) ─────────────
    public static final int GLASS_BG = 0x55101824;
    public static final int GLASS_BG_HOVER = 0x801A283C;
    public static final int GLASS_BG_DISABLED = 0x220B111A;

    public static final int GLASS_BORDER = 0x458098B0;
    public static final int GLASS_BORDER_HOVER = 0xEE9FE2FF;
    public static final int GLASS_BORDER_DISABLED = 0x20506070;

    public static final int ACCENT_CYAN = 0xFF7FE6FF;

    // Vollständige 32-Bit ARGB Textfarben (Alpha immer 0xFF für permanente Sichtbarkeit)
    public static final int TEXT_WHITE = 0xFFFFFFFF;
    public static final int TEXT_IDLE = 0xFFD8E4F0;
    public static final int TEXT_DISABLED = 0xFF6A7B8C;

    // Animations- und Sound-Status pro Widget
    private static final WeakHashMap<AbstractWidget, Float> HOVER_MAP = new WeakHashMap<>();
    private static final WeakHashMap<AbstractWidget, Boolean> SOUND_STATE_MAP = new WeakHashMap<>();

    private LotusButtonRenderer() {}

    // ── Button Rendering ──────────────────────────────────────────────────
    public static void renderButton(AbstractButton button, GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        int x = button.getX();
        int y = button.getY();
        int w = button.getWidth();
        int h = button.getHeight();
        boolean active = button.active;
        boolean hovered = button.isHoveredOrFocused(); // isHoveredOrFocused

        float hover = updateHover(button, active && hovered);

        if (active && hovered) {
            try {
                g.requestCursor(CursorTypes.POINTING_HAND);
            } catch (Throwable ignored) {}
        }

        // 1. Weicher Cyan-Glow bei Hover (flackerfrei & sauber außenliegend)
        if (active && hover > 0.02f) {
            drawOuterGlow(g, x, y, w, h, hover);
        }

        // 2. Button-Hintergrund (Frosted Glass ohne Überlappungen)
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

        // 3. Feiner Glas-Lichtschein & Reflektion (obere Hälfte)
        if (active) {
            drawGlassSheen(g, x, y, w, h, hover);
        }

        // 4. Nahtlose abgerundete Kontur
        drawRoundedBorder(g, x, y, w, h, border);

        // 5. Zentrierter Text (IMMER sichtbar, sanftes Aufleuchten bei Hover)
        Minecraft client = Minecraft.getInstance();
        Font font = client != null ? client.font : null;
        if (font != null) {
            Component msg = button.getMessage();
            int textColor = !active ? TEXT_DISABLED : Colors.lerp(TEXT_IDLE, TEXT_WHITE, hover);
            drawCenteredButtonText(g, font, msg, x, y, w, h, textColor);
        }
    }

    // ── Slider Rendering ──────────────────────────────────────────────────
    public static void renderSlider(AbstractSliderButton slider, GuiGraphicsExtractor g, int mouseX, int mouseY, float delta, double value) {
        int x = slider.getX();
        int y = slider.getY();
        int w = slider.getWidth();
        int h = slider.getHeight();
        boolean active = slider.active;
        boolean hovered = slider.isHoveredOrFocused(); // isHoveredOrFocused

        float hover = updateHover(slider, active && hovered);
        double clampedVal = Math.clamp(value, 0.0, 1.0);

        if (active && hovered) {
            try {
                g.requestCursor(CursorTypes.POINTING_HAND);
            } catch (Throwable ignored) {}
        }

        // 1. Sanfter Track Glow
        if (active && hover > 0.02f) {
            drawOuterGlow(g, x, y, w, h, hover * 0.75f);
        }

        int trackBg = !active ? GLASS_BG_DISABLED : Colors.lerp(GLASS_BG, 0x65142232, hover);
        int trackBorder = !active ? GLASS_BORDER_DISABLED : Colors.lerp(GLASS_BORDER, GLASS_BORDER_HOVER, hover);

        drawRoundedFill(g, x, y, w, h, trackBg);

        // 2. Fortschrittsbalken im Inneren des Reglers
        int handleOffset = (int) (clampedVal * (w - 8));
        int progressW = handleOffset + 4;
        if (progressW > 3 && active) {
            int progressCol = Colors.withAlpha(ACCENT_CYAN, 0.20f + 0.15f * hover);
            drawProgressFill(g, x, y, progressW, h, progressCol);
        }

        // 3. Glas-Lichtschein
        if (active) {
            drawGlassSheen(g, x, y, w, h, hover);
        }

        // 4. Regler-Schiene Kontur
        drawRoundedBorder(g, x, y, w, h, trackBorder);

        // 5. Regler-Griff (Handle Knob)
        int hx = x + handleOffset;
        int hw = 8;
        int hy = y + 1;
        int hh = h - 2;

        int handleBg = !active ? 0x60303840 : (hovered ? 0xDD7FE6FF : 0xBB1E3246);
        int handleBorder = !active ? 0x40607080 : (hovered ? 0xFFFFFFFF : 0xFF7FE6FF);

        drawHandle(g, hx, hy, hw, hh, handleBg, handleBorder, hovered && active);

        // 6. Zentrierter Text (IMMER sichtbar)
        Minecraft client = Minecraft.getInstance();
        Font font = client != null ? client.font : null;
        if (font != null) {
            Component msg = slider.getMessage();
            int textColor = !active ? TEXT_DISABLED : Colors.lerp(TEXT_IDLE, TEXT_WHITE, hover);
            drawCenteredButtonText(g, font, msg, x, y, w, h, textColor);
        }
    }

    // ── Animations-Helper ─────────────────────────────────────────────────
    private static float updateHover(AbstractWidget widget, boolean isHovered) {
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

    // ── Glow & Sheen Rendering ────────────────────────────────────────────
    private static void drawOuterGlow(GuiGraphicsExtractor g, int x, int y, int w, int h, float hover) {
        int alpha = (int) (0x35 * hover);
        if (alpha <= 0) return;
        int glowColor = (alpha << 24) | 0x7FE6FF;

        // Horizontale Außenränder (vollständig außerhalb des Buttons)
        g.fill(x + 2, y - 1, x + w - 2, y, glowColor);
        g.fill(x + 2, y + h, x + w - 2, y + h + 1, glowColor);

        // Vertikale Außenränder
        g.fill(x - 1, y + 2, x, y + h - 2, glowColor);
        g.fill(x + w, y + 2, x + w + 1, y + h - 2, glowColor);

        // Abgeschlossene äußere Ecken
        g.fill(x, y - 1, x + 2, y, glowColor);
        g.fill(x - 1, y, x, y + 2, glowColor);

        g.fill(x + w - 2, y - 1, x + w, y, glowColor);
        g.fill(x + w, y, x + w + 1, y + 2, glowColor);

        g.fill(x, y + h, x + 2, y + h + 1, glowColor);
        g.fill(x - 1, y + h - 2, x, y + h, glowColor);

        g.fill(x + w - 2, y + h, x + w, y + h + 1, glowColor);
        g.fill(x + w, y + h - 2, x + w + 1, y + h, glowColor);
    }

    private static void drawGlassSheen(GuiGraphicsExtractor g, int x, int y, int w, int h, float hover) {
        int sheenTop = Colors.withAlpha(0xFFFFFF, 0.12f + 0.08f * hover);
        int sheenBot = Colors.withAlpha(0xFFFFFF, 0.01f);
        int halfH = Math.max(2, (h - 2) / 2);
        g.fillGradient(x + 2, y + 1, x + w - 2, y + 1 + halfH, sheenTop, sheenBot);

        int specColor = Colors.withAlpha(0xFFFFFF, 0.20f + 0.18f * hover);
        g.fill(x + 3, y + 1, x + w - 3, y + 2, specColor);
    }

    // ── Pixel-Perfekte Abgerundete Geometrie (Absolut Nahtlos & Ohne Überlappung) ──
    public static void drawRoundedFill(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        if (w <= 0 || h <= 0) return;
        if (w < 8 || h < 8) {
            g.fill(x, y, x + w, y + h, color);
            return;
        }

        // Komplett disjunkte horizontale Zeilen: Kein einziger Pixel wird doppelt gezeichnet!
        g.fill(x + 2, y + 1, x + w - 2, y + 2, color);
        g.fill(x + 1, y + 2, x + w - 1, y + h - 2, color);
        g.fill(x + 2, y + h - 2, x + w - 2, y + h - 1, color);
    }

    public static void drawRoundedBorder(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        if (w <= 0 || h <= 0) return;
        if (w < 8 || h < 8) {
            g.outline(x, y, w, h, color);
            return;
        }

        // Horizontale Außenkanten
        g.fill(x + 2, y, x + w - 2, y + 1, color);
        g.fill(x + 2, y + h - 1, x + w - 2, y + h, color);

        // Vertikale Außenkanten
        g.fill(x, y + 2, x + 1, y + h - 2, color);
        g.fill(x + w - 1, y + 2, x + w, y + h - 2, color);

        // Verbindende Eck-Pixel (perfekt geschlossener 1px Radius)
        g.fill(x + 1, y + 1, x + 2, y + 2, color);
        g.fill(x + w - 2, y + 1, x + w - 1, y + 2, color);
        g.fill(x + 1, y + h - 2, x + 2, y + h - 1, color);
        g.fill(x + w - 2, y + h - 2, x + w - 1, y + h - 1, color);
    }

    private static void drawProgressFill(GuiGraphicsExtractor g, int x, int y, int pw, int h, int color) {
        if (pw <= 2) return;
        g.fill(x + 2, y + 1, x + pw, y + 2, color);
        g.fill(x + 1, y + 2, x + pw, y + h - 2, color);
        g.fill(x + 2, y + h - 2, x + pw, y + h - 1, color);
    }

    private static void drawHandle(GuiGraphicsExtractor g, int x, int y, int w, int h, int fill, int border, boolean hovered) {
        if (hovered) {
            g.fill(x - 1, y, x + w + 1, y + h, Colors.withAlpha(ACCENT_CYAN, 0.30f));
        }
        g.fill(x + 1, y, x + w - 1, y + 1, border);
        g.fill(x + 1, y + h - 1, x + w - 1, y + h, border);
        g.fill(x, y + 1, x + 1, y + h - 1, border);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, border);

        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);
        g.fill(x + 2, y + 2, x + w - 2, y + 3, Colors.withAlpha(0xFFFFFF, 0.45f));
    }

    // ── Zentrierter & Garantiert Sichtbarer Button-Text ───────────────────
    public static void drawCenteredButtonText(GuiGraphicsExtractor g, Font font, Component text, int x, int y, int w, int h, int color) {
        if (text == null) return;
        int textW = font.width(text);
        int cx = x + w / 2;
        int cy = y + (h - 8) / 2; // Exakte Minecraft Text-Zentrierung
        if (textW <= w - 6) {
            g.centeredText(font, text, cx, cy, color);
        } else {
            float scale = (float) (w - 6) / (float) textW;
            Matrix3x2fStack matrices = g.pose();
            matrices.pushMatrix();
            matrices.translate((float) cx, (float) (y + h / 2));
            matrices.scale(scale, scale);
            g.text(font, text, -textW / 2, -4, color, true);
            matrices.popMatrix();
        }
    }
}

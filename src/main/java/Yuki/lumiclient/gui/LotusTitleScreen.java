package Yuki.lumiclient.gui;

import Yuki.lumiclient.keybinds.KeyboardScreen;
import Yuki.lumiclient.render.NvgDrawable;
import Yuki.lumiclient.render.OverlayRenderer;
import Yuki.lumiclient.render.nanovg.NVGRenderer;
import Yuki.lumiclient.util.Colors;
import Yuki.lumiclient.util.UiSounds;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.options.SkinCustomizationScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.client.gui.components.PlayerSkinWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class LotusTitleScreen extends TitleScreen implements NvgDrawable {

    public static final int GLASS_BG             = 0x50101824;
    public static final int GLASS_BG_HOVER       = 0x801A2638;
    public static final int GLASS_BORDER         = 0x408098B0;
    public static final int GLASS_BORDER_HOVER   = 0xDD9FE2FF;
    public static final int ACCENT_CYAN          = 0xFF7FE6FF;
    public static final int ACCENT_LOTUS         = 0xFFFF78B5;
    public static final int TEXT_WHITE           = 0xFFFFFFFF;
    public static final int TEXT_MUTED           = 0x90C8D6E5;

    private static class GlassButton {
        String label;
        final Runnable action;
        float hoverAnim = 0f;

        GlassButton(String label, Runnable action) {
            this.label = label;
            this.action = action;
        }
    }

    private final List<GlassButton> centerButtons = new ArrayList<>();
    private GlassButton optionsButton;
    private GlassButton quitButton;
    private GlassButton wardrobeButton;
    private GlassButton skyButton;
    private GlassButton keybindsButton;
    private GlassButton spotifyButton;

    private PlayerSkinWidget skinWidget;
    private long lastFrameTime = System.currentTimeMillis();
    private int hoveredBtnId = -1;

    public LotusTitleScreen() {
        super();
        initMenuButtons();
    }

    private void initMenuButtons() {
        centerButtons.clear();

        // 1. Singleplayer
        centerButtons.add(new GlassButton("Singleplayer", () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) mc.gui.setScreen(new SelectWorldScreen(this));
        }));

        // 2. Multiplayer
        centerButtons.add(new GlassButton("Multiplayer", () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) mc.gui.setScreen(new JoinMultiplayerScreen(this));
        }));

        // Lotus Button WEG GELASSEN wie gewünscht!

        // Split-Row: Options... & Quit Game
        optionsButton = new GlassButton("Options...", () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) mc.gui.setScreen(new OptionsScreen(this, mc.options));
        });

        quitButton = new GlassButton("Quit Game", () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) mc.stop();
        });

        // Wardrobe Button
        wardrobeButton = new GlassButton("Wardrobe", () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) mc.gui.setScreen(new SkinCustomizationScreen(this, mc.options));
        });

        // Sky-Theme Switcher Button (Top-Right)
        skyButton = new GlassButton("Sky: " + LotusBackdrop.name(), () -> {
            LotusBackdrop.cycle();
        });

        // Keybinds Button (Top-Right)
        keybindsButton = new GlassButton("Keybinds", () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) mc.gui.setScreen(new KeyboardScreen(this));
        });

        // Music / SkyLyrics Dashboard Button (Top-Right)
        spotifyButton = new GlassButton("🎵 Music", () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) mc.gui.setScreen(new dev.lyricsflow.gui.SkyLyricsScreen(this));
        });
    }

    private void initSkinWidget() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.getEntityModels() == null) return;
            Supplier<PlayerSkin> skinSupplier = () -> {
                try {
                    GameProfile profile = mc.getGameProfile();
                    if (profile != null && mc.getSkinManager() != null) {
                        Supplier<PlayerSkin> sup = mc.getSkinManager().createLookup(profile, false);
                        if (sup != null && sup.get() != null) {
                            return sup.get();
                        }
                    }
                } catch (Throwable ignored) {}
                return DefaultPlayerSkin.getDefaultSkin();
            };
            this.skinWidget = new PlayerSkinWidget(120, 190, mc.getEntityModels(), skinSupplier);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    protected void init() {
        try {
            this.afterKeyboardAction();
        } catch (Throwable ignored) {}
        initSkinWidget();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        int w = this.width;
        int h = this.height;

        LotusBackdrop.render(context, w, h, 1.0f);
        drawMannequin(context, mouseX, mouseY, deltaTicks);
        context.blurBeforeThisStratum();
    }

    private void drawMannequin(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        try {
            if (this.skinWidget == null) {
                initSkinWidget();
            }
            if (this.skinWidget == null) return;

            float sw = OverlayRenderer.uiWidth();
            float sh = OverlayRenderer.uiHeight();
            if (sw <= 0) sw = this.width;
            if (sh <= 0) sh = this.height;

            float s = Math.max(0.65f, Math.min(sw / 960.0f, sh / 540.0f));
            float cx = sw / 2f;
            float totalBlockH = (centerButtons.size() + 1) * (30f * s) + centerButtons.size() * (6f * s);
            float startY = Math.max(80f * s, (sh - totalBlockH) * 0.46f);

            float figX = cx + (153f + 40f) * s;
            float figY = startY - 40f * s;
            float figW = 240f * s;

            float skinUiY = figY + 28f * s;
            float skinUiH = (225f - 28f) * s;
            float skinUiW = 130f * s;
            float skinUiX = figX + (figW - skinUiW) / 2f;

            float scaleX = (float) this.width / sw;
            float scaleY = (float) this.height / sh;

            int gx = Math.round(skinUiX * scaleX);
            int gy = Math.round(skinUiY * scaleY);
            int gw = Math.round(skinUiW * scaleX);
            int gh = Math.round(skinUiH * scaleY);

            this.skinWidget.setX(gx);
            this.skinWidget.setY(gy);
            this.skinWidget.setWidth(gw);
            this.skinWidget.setHeight(gh);
            this.skinWidget.extractRenderState(context, mouseX, mouseY, deltaTicks);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void renderNvg(NVGRenderer nvg, float mx, float my, float sw, float sh) {
        long now = System.currentTimeMillis();
        float dt = Math.min(2.0f, Math.max(0.2f, (now - lastFrameTime) / 16.666f));
        lastFrameTime = now;

        float s = Math.max(0.65f, Math.min(sw / 960.0f, sh / 540.0f));

        nvg.rectGradient(0f, 0f, sw, sh * 0.28f, 0f, 0x50000000, 0x00000000, true);
        nvg.rectGradient(0f, sh * 0.72f, sw, sh * 0.28f, 0f, 0x00000000, 0x65020408, true);

        float cx = sw / 2f;
        float menuW = 306f * s;
        float btnH = 30f * s;
        float btnRadius = 10f * s;
        float btnGap = 6f * s;
        float fontSize = 13.5f * s;

        float totalBlockH = (centerButtons.size() + 1) * btnH + centerButtons.size() * btnGap;
        float startY = Math.max(80f * s, (sh - totalBlockH) * 0.46f);

        // ── Zentrales Branding: SkyLyrics ─────────────────────────────────────
        float titleCenterY = startY - 48f * s;
        float emblemRadius = 24f * s;
        float pulse = (float) (Math.sin(now * 0.0035) * (1.2f * s));
        float titleFontSize = 32f * s;

        float textW_Sky = nvg.textWidth("Sky", titleFontSize);
        float textW_Lyrics = nvg.textWidth("Lyrics", titleFontSize);
        float emblemToTextGap = 14f * s;
        float totalBrandingW = (emblemRadius * 2f) + emblemToTextGap + textW_Sky + textW_Lyrics;

        float emblemCenterX = cx - totalBrandingW / 2f + emblemRadius;
        float textLeftX = emblemCenterX + emblemRadius + emblemToTextGap;

        drawSkyLyricsEmblem(nvg, emblemCenterX, titleCenterY, emblemRadius, pulse);
        nvg.text("Sky", textLeftX, titleCenterY, titleFontSize, ACCENT_CYAN);
        nvg.text("Lyrics", textLeftX + textW_Sky, titleCenterY, titleFontSize, TEXT_WHITE);

        // ── Menü-Buttons (Singleplayer, Multiplayer) ───────────────────────────
        for (int i = 0; i < centerButtons.size(); i++) {
            GlassButton btn = centerButtons.get(i);
            float bx = cx - menuW / 2f;
            float by = startY + i * (btnH + btnGap);

            boolean hovered = mx >= bx && mx <= bx + menuW && my >= by && my <= by + btnH;
            renderGlassPill(nvg, btn, bx, by, menuW, btnH, btnRadius, fontSize, hovered, dt, i, s);
        }

        // ── Split Row: Options... & Quit Game ────────────────────────────────
        float splitY = startY + centerButtons.size() * (btnH + btnGap);
        float splitW = (menuW - btnGap) / 2f;
        float optX = cx - menuW / 2f;
        float quitX = optX + splitW + btnGap;

        boolean optHover = mx >= optX && mx <= optX + splitW && my >= splitY && my <= splitY + btnH;
        renderGlassPill(nvg, optionsButton, optX, splitY, splitW, btnH, btnRadius, fontSize, optHover, dt, 100, s);

        boolean quitHover = mx >= quitX && mx <= quitX + splitW && my >= splitY && my <= splitY + btnH;
        renderGlassPill(nvg, quitButton, quitX, splitY, splitW, btnH, btnRadius, fontSize, quitHover, dt, 101, s);

        // ── Oben Rechts: Sky Theme, Keybinds & Music Buttons ─────────────────
        skyButton.label = "Sky: " + LotusBackdrop.name();
        float skyW = 92f * s;
        float skyH = 22f * s;
        float skyX = sw - skyW - 14f * s;
        float skyY = 12f * s;

        boolean skyHover = mx >= skyX && mx <= skyX + skyW && my >= skyY && my <= skyY + skyH;
        renderGlassPill(nvg, skyButton, skyX, skyY, skyW, skyH, 6f * s, 10.5f * s, skyHover, dt, 102, s);

        float keybindsW = 86f * s;
        float keybindsH = 22f * s;
        float keybindsX = skyX - keybindsW - 8f * s;
        float keybindsY = 12f * s;

        boolean keybindsHover = mx >= keybindsX && mx <= keybindsX + keybindsW && my >= keybindsY && my <= keybindsY + keybindsH;
        renderGlassPill(nvg, keybindsButton, keybindsX, keybindsY, keybindsW, keybindsH, 6f * s, 10.5f * s, keybindsHover, dt, 104, s);

        float spotifyW = 86f * s;
        float spotifyH = 22f * s;
        float spotifyX = keybindsX - spotifyW - 8f * s;
        float spotifyY = 12f * s;

        boolean spotifyHover = mx >= spotifyX && mx <= spotifyX + spotifyW && my >= spotifyY && my <= spotifyY + spotifyH;
        renderGlassPill(nvg, spotifyButton, spotifyX, spotifyY, spotifyW, spotifyH, 6f * s, 10.5f * s, spotifyHover, dt, 105, s);

        // ── Rechte Seite: Badge & Wardrobe Button ────────────────────────────
        float figX = cx + (153f + 40f) * s;
        float figY = startY - 40f * s;
        float figW = 240f * s;

        String currentAccount = getUsername();
        float badgeFontSize = 11f * s;
        float badgeW = 20f * s + nvg.textWidth("SkyLyrics  " + currentAccount, badgeFontSize) + 20f * s;
        float badgeH = 20f * s;
        float badgeX = figX + (figW - badgeW) / 2f;
        float badgeY = figY + 8f * s;

        nvg.rect(badgeX, badgeY, badgeW, badgeH, 10f * s, 0x650D141F);
        nvg.rectOutline(badgeX, badgeY, badgeW, badgeH, 10f * s, 1.0f, 0x458098B0);

        float sqSize = 6.5f * s;
        nvg.glow(badgeX + 7f * s, badgeY + (badgeH - sqSize) / 2f, sqSize, sqSize, 2f * s, 4f * s, Colors.withAlpha(ACCENT_CYAN, 0.6f));
        nvg.rect(badgeX + 7f * s, badgeY + (badgeH - sqSize) / 2f, sqSize, sqSize, 1.5f * s, ACCENT_CYAN);

        float labelW = nvg.textWidth("SkyLyrics", badgeFontSize);
        nvg.text("SkyLyrics", badgeX + 17f * s, badgeY + badgeH / 2f, badgeFontSize, 0xFFCAD4E0);
        float sepX = badgeX + 17f * s + labelW + 6f * s;
        nvg.line(sepX, badgeY + 4f * s, sepX, badgeY + badgeH - 4f * s, 1f, 0x35FFFFFF);
        nvg.text(currentAccount, sepX + 6f * s, badgeY + badgeH / 2f, badgeFontSize, TEXT_WHITE);

        float wardrobeW = 120f * s;
        float wardrobeH = 30f * s;
        float wardrobeX = figX + (figW - wardrobeW) / 2f;
        float wardrobeY = figY + 225f * s;

        boolean wardHover = mx >= wardrobeX && mx <= wardrobeX + wardrobeW && my >= wardrobeY && my <= wardrobeY + wardrobeH;
        renderGlassPill(nvg, wardrobeButton, wardrobeX, wardrobeY, wardrobeW, wardrobeH, 9f * s, fontSize, wardHover, dt, 103, s);

        // ── Linke Seite: "What's new" Changelog ──────────────────────────────
        float newsX = Math.max(18f * s, cx - menuW / 2f - 240f * s);
        float newsY = startY + 6f * s;

        nvg.text("What's new", newsX, newsY, 13f * s, TEXT_WHITE);
        nvg.text("1.0", newsX, newsY + 16f * s, 11f * s, ACCENT_CYAN);
        nvg.text("Features", newsX, newsY + 34f * s, 10.5f * s, 0xFF8AA4BE);
        nvg.text("• Synced song lyrics from Spotify & YouTube", newsX, newsY + 48f * s, 10.5f * s, TEXT_MUTED);
        nvg.text("• 3D Holographic in-air & celestial sky lyrics", newsX, newsY + 62f * s, 10.5f * s, TEXT_MUTED);
        nvg.text("• Dynamic animated Aurora start menu", newsX, newsY + 76f * s, 10.5f * s, TEXT_MUTED);
        nvg.text("• Interactive visual keybind manager", newsX, newsY + 90f * s, 10.5f * s, TEXT_MUTED);

        // ── Unten Links: SkyLyrics Branding ──────────────────────────────────
        nvg.text("SkyLyrics 1.0  Minecraft 1.21.11", 16f * s, sh - 14f * s, 10.5f * s, 0x80859CAE);
    }

    private void renderGlassPill(NVGRenderer nvg, GlassButton btn, float x, float y, float w, float h, float r, float fontSz, boolean hovered, float dt, int btnId, float s) {
        if (hovered) {
            if (hoveredBtnId != btnId) {
                UiSounds.hover();
                hoveredBtnId = btnId;
            }
        } else if (hoveredBtnId == btnId) {
            hoveredBtnId = -1;
        }

        float target = hovered ? 1.0f : 0.0f;
        btn.hoverAnim += (target - btn.hoverAnim) * Math.min(1.0f, 0.26f * dt);

        float lift = 1.0f * s * btn.hoverAnim;
        float expand = 1.5f * s * btn.hoverAnim;
        float drawX = x - expand;
        float drawY = y - lift;
        float drawW = w + expand * 2.0f;
        float drawH = h;
        float drawR = r + expand * 0.25f;

        if (btn.hoverAnim > 0.015f) {
            int argb = Colors.withAlpha(ACCENT_CYAN, 0.52f * btn.hoverAnim);
            drawBoxGlow(nvg, drawX, drawY, drawW, drawH, drawR, 14f * s, argb);
        }

        int hoverBg = 0x75182C40;
        int bg = Colors.lerp(GLASS_BG, hoverBg, btn.hoverAnim);
        nvg.rect(drawX, drawY, drawW, drawH, drawR, bg);

        if (btn.hoverAnim > 0.02f) {
            int sheenTop = Colors.withAlpha(0xFFFFFF, 0.16f * btn.hoverAnim);
            int sheenBot = Colors.withAlpha(0xFFFFFF, 0.01f * btn.hoverAnim);
            nvg.rectGradient(drawX, drawY, drawW, drawH * 0.52f, drawR, sheenTop, sheenBot, true);

            float innerPad = drawR * 0.65f;
            nvg.line(drawX + innerPad, drawY + 1.0f, drawX + drawW - innerPad, drawY + 1.0f, 1.0f, Colors.withAlpha(0xFFFFFF, 0.35f * btn.hoverAnim));
        }

        int hoverBorder = 0xFF9FE2FF;
        int border = Colors.lerp(GLASS_BORDER, hoverBorder, btn.hoverAnim);
        float borderWidth = 1.0f + 0.45f * btn.hoverAnim;
        nvg.rectOutline(drawX, drawY, drawW, drawH, drawR, borderWidth, border);

        int textCol = Colors.lerp(0xD5D8E4F0, 0xFFFFFFFF, btn.hoverAnim);
        float textW = nvg.textWidth(btn.label, fontSz);
        nvg.text(btn.label, drawX + (drawW - textW) / 2f, drawY + drawH / 2f, fontSz, textCol);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        float mx = OverlayRenderer.guiToUi(event.x());
        float my = OverlayRenderer.guiToUi(event.y());
        int button = event.button();

        if (button == 0) {
            float sw = OverlayRenderer.uiWidth();
            float sh = OverlayRenderer.uiHeight();
            float s = Math.max(0.65f, Math.min(sw / 960.0f, sh / 540.0f));

            float cx = sw / 2f;
            float menuW = 306f * s;
            float btnH = 30f * s;
            float btnGap = 6f * s;
            float totalBlockH = (centerButtons.size() + 1) * btnH + centerButtons.size() * btnGap;
            float startY = Math.max(80f * s, (sh - totalBlockH) * 0.46f);
            float pad = 2f * s;

            // 1. Top-Right Buttons
            float skyW = 92f * s;
            float skyH = 22f * s;
            float skyX = sw - skyW - 14f * s;
            float skyY = 12f * s;
            if (mx >= skyX - pad && mx <= skyX + skyW + pad && my >= skyY - pad && my <= skyY + skyH + pad) {
                UiSounds.select();
                skyButton.action.run();
                return true;
            }

            float keybindsW = 86f * s;
            float keybindsH = 22f * s;
            float keybindsX = skyX - keybindsW - 8f * s;
            float keybindsY = 12f * s;
            if (mx >= keybindsX - pad && mx <= keybindsX + keybindsW + pad && my >= keybindsY - pad && my <= keybindsY + keybindsH + pad) {
                UiSounds.select();
                if (keybindsButton != null && keybindsButton.action != null) {
                    keybindsButton.action.run();
                }
                return true;
            }

            float spotifyW = 86f * s;
            float spotifyH = 22f * s;
            float spotifyX = keybindsX - spotifyW - 8f * s;
            float spotifyY = 12f * s;
            if (mx >= spotifyX - pad && mx <= spotifyX + spotifyW + pad && my >= spotifyY - pad && my <= spotifyY + spotifyH + pad) {
                UiSounds.select();
                if (spotifyButton != null && spotifyButton.action != null) {
                    spotifyButton.action.run();
                }
                return true;
            }

            // 2. Center Buttons
            for (int i = 0; i < centerButtons.size(); i++) {
                float bx = cx - menuW / 2f;
                float by = startY + i * (btnH + btnGap);
                if (mx >= bx - pad && mx <= bx + menuW + pad && my >= by - pad && my <= by + btnH + pad) {
                    UiSounds.select();
                    centerButtons.get(i).action.run();
                    return true;
                }
            }

            // 3. Options & Quit Game
            float splitY = startY + centerButtons.size() * (btnH + btnGap);
            float splitW = (menuW - btnGap) / 2f;
            float optX = cx - menuW / 2f;
            float quitX = optX + splitW + btnGap;

            if (mx >= optX - pad && mx <= optX + splitW + pad && my >= splitY - pad && my <= splitY + btnH + pad) {
                UiSounds.select();
                optionsButton.action.run();
                return true;
            }
            if (mx >= quitX - pad && mx <= quitX + splitW + pad && my >= splitY - pad && my <= splitY + btnH + pad) {
                UiSounds.select();
                quitButton.action.run();
                return true;
            }

            // 4. Wardrobe Button
            float figX = cx + (153f + 40f) * s;
            float figY = startY - 40f * s;
            float figW = 240f * s;
            float wardrobeW = 120f * s;
            float wardrobeH = 30f * s;
            float wardrobeX = figX + (figW - wardrobeW) / 2f;
            float wardrobeY = figY + 225f * s;

            if (mx >= wardrobeX - pad && mx <= wardrobeX + wardrobeW + pad && my >= wardrobeY - pad && my <= wardrobeY + wardrobeH + pad) {
                UiSounds.select();
                wardrobeButton.action.run();
                return true;
            }

            if (this.skinWidget != null && this.skinWidget.isMouseOver(event.x(), event.y())) {
                this.skinWidget.mouseClicked(event, doubleClick);
                return true;
            }

            return true;
        }
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.skinWidget != null) {
            this.skinWidget.mouseReleased(event);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (this.skinWidget != null) {
            double mx = event.x();
            double my = event.y();
            if (this.skinWidget.isMouseOver(mx, my)) {
                this.skinWidget.mouseDragged(event, deltaX, deltaY);
                return true;
            }
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256) {
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return super.charTyped(event);
    }

    private static String getUsername() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getGameProfile() != null) {
                Object profile = mc.getGameProfile();
                try {
                    return (String) profile.getClass().getMethod("name").invoke(profile);
                } catch (Throwable t) {
                    return (String) profile.getClass().getMethod("getName").invoke(profile);
                }
            }
        } catch (Throwable ignored) {}
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getUser() != null) {
                return mc.getUser().getName();
            }
        } catch (Throwable ignored) {}
        return "Player";
    }

    private static void drawSkyLyricsEmblem(NVGRenderer nvg, float cx, float cy, float radius, float pulse) {
        // Multi-Layer Outer Bloom Glow
        nvg.circleGlow(cx, cy, radius + 4f + pulse, 18f, Colors.withAlpha(ACCENT_CYAN, 0.45f));
        nvg.circleGlow(cx, cy, radius, 8f, Colors.withAlpha(0xFFE0F7FF, 0.60f));

        // Frosted Glass Disk
        nvg.circle(cx, cy, radius, 0x750E1826);
        nvg.circleOutline(cx, cy, radius, 1.4f, ACCENT_CYAN);

        // Inner glowing ring
        nvg.circle(cx, cy, radius * 0.78f, 0x301D88E5);
        nvg.circleOutline(cx, cy, radius * 0.78f, 1.0f, 0x807FE6FF);

        // Vector Beamed Musical Note (♫) - 100% gestochen scharf, kein Font-Glyph-Fehler
        float r = radius;
        // Left notehead
        nvg.circle(cx - r * 0.28f, cy + r * 0.22f, r * 0.20f, 0xFFFFFFFF);
        nvg.circle(cx - r * 0.23f, cy + r * 0.16f, r * 0.15f, 0xFF7FE6FF);

        // Right notehead
        nvg.circle(cx + r * 0.22f, cy + r * 0.10f, r * 0.20f, 0xFFFFFFFF);
        nvg.circle(cx + r * 0.27f, cy + r * 0.04f, r * 0.15f, 0xFF7FE6FF);

        // Left stem
        nvg.rect(cx - r * 0.14f, cy - r * 0.38f, r * 0.09f, r * 0.60f, r * 0.04f, 0xFFFFFFFF);

        // Right stem
        nvg.rect(cx + r * 0.36f, cy - r * 0.50f, r * 0.09f, r * 0.60f, r * 0.04f, 0xFFFFFFFF);

        // Connecting Beam
        nvg.rect(cx - r * 0.14f, cy - r * 0.48f, r * 0.59f, r * 0.16f, r * 0.05f, 0xFFFFFFFF);
        nvg.rect(cx - r * 0.14f, cy - r * 0.48f, r * 0.59f, r * 0.06f, 0.5f, 0xFF7FE6FF);

        // Sparkle Star (top right)
        float sx = cx + r * 0.46f, sy = cy - r * 0.22f;
        nvg.glow(sx - 1.5f, sy - 1.5f, 3f, 3f, 1f, 4f, 0xCC7FE6FF);
        nvg.rect(sx - 0.7f, sy - 3f, 1.4f, 6f, 0.5f, 0xFFFFFFFF);
        nvg.rect(sx - 3f, sy - 0.7f, 6f, 1.4f, 0.5f, 0xFFFFFFFF);
    }

    private static void drawBoxGlow(NVGRenderer nvg, float x, float y, float w, float h, float radius, float blur, int argb) {
        if ((argb >>> 24) == 0 || blur <= 0.5f) return;
        int ambient = Colors.withAlpha(argb, 0.45f * ((argb >>> 24) / 255.0f));
        nvg.glow(x, y, w, h, radius, blur * 1.35f, ambient);

        int core = Colors.withAlpha(argb, 0.90f * ((argb >>> 24) / 255.0f));
        nvg.glow(x, y, w, h, radius, blur * 0.50f, core);
    }
}

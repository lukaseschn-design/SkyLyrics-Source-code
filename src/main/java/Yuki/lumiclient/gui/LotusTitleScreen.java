package Yuki.lumiclient.gui;

import Yuki.lumiclient.keybinds.KeyboardScreen;
import com.mojang.authlib.GameProfile;
import net.minecraft.class_1068;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_429;
import net.minecraft.class_440;
import net.minecraft.class_442;
import net.minecraft.class_500;
import net.minecraft.class_526;
import net.minecraft.class_8685;
import net.minecraft.class_8765;
import org.joml.Matrix3x2fStack;

import java.util.function.Supplier;

public class LotusTitleScreen extends class_442 {

    private class_8765 skinWidget;

    public LotusTitleScreen() {
        super();
    }

    private void initSkinWidget() {
        try {
            class_310 mc = class_310.method_1551();
            if (mc == null || mc.method_31974() == null) return;
            Supplier<class_8685> skinSupplier = () -> {
                try {
                    GameProfile profile = mc.method_53462();
                    if (profile != null && mc.method_1582() != null) {
                        Supplier<class_8685> sup = mc.method_1582().method_73544(profile, false);
                        if (sup != null && sup.get() != null) {
                            return sup.get();
                        }
                    }
                } catch (Throwable ignored) {}
                return class_1068.method_62620();
            };
            this.skinWidget = new class_8765(120, 190, mc.method_31974(), skinSupplier);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    protected void method_25426() {
        try {
            this.method_37070();
        } catch (Throwable ignored) {}
        initSkinWidget();

        int sw = this.field_22789;
        int sh = this.field_22790;
        int cx = sw / 2;

        int menuW = 204;
        int btnH = 20;
        int btnGap = 4;
        int startY = sh / 2 - 10;

        // 1. Singleplayer Button
        this.method_37063(class_4185.method_46430(
            class_2561.method_43470("Singleplayer"),
            b -> {
                class_310 mc = class_310.method_1551();
                if (mc != null) mc.method_1507(new class_526(this));
            }
        ).method_46434(cx - menuW / 2, startY, menuW, btnH).method_46431());

        // 2. Multiplayer Button
        this.method_37063(class_4185.method_46430(
            class_2561.method_43470("Multiplayer"),
            b -> {
                class_310 mc = class_310.method_1551();
                if (mc != null) mc.method_1507(new class_500(this));
            }
        ).method_46434(cx - menuW / 2, startY + (btnH + btnGap), menuW, btnH).method_46431());

        // 3. Options... & Quit Game (Split Row)
        int splitW = (menuW - btnGap) / 2;
        int splitY = startY + 2 * (btnH + btnGap);

        this.method_37063(class_4185.method_46430(
            class_2561.method_43470("Options..."),
            b -> {
                class_310 mc = class_310.method_1551();
                if (mc != null) mc.method_1507(new class_429(this, mc.field_1690));
            }
        ).method_46434(cx - menuW / 2, splitY, splitW, btnH).method_46431());

        this.method_37063(class_4185.method_46430(
            class_2561.method_43470("Quit Game"),
            b -> {
                class_310 mc = class_310.method_1551();
                if (mc != null) mc.method_1592();
            }
        ).method_46434(cx - menuW / 2 + splitW + btnGap, splitY, splitW, btnH).method_46431());

        // 4. Oben Rechts: Sky Theme, Keybinds & Music
        int topBtnH = 20;
        int topY = 10;

        // Sky: <Theme>
        class_4185 skyBtn = class_4185.method_46430(
            class_2561.method_43470("Sky: " + LotusBackdrop.name()),
            b -> {
                LotusBackdrop.cycle();
                b.method_25355(class_2561.method_43470("Sky: " + LotusBackdrop.name()));
            }
        ).method_46434(sw - 85 - 10, topY, 85, topBtnH).method_46431();
        this.method_37063(skyBtn);

        // Keybinds
        this.method_37063(class_4185.method_46430(
            class_2561.method_43470("Keybinds"),
            b -> {
                class_310 mc = class_310.method_1551();
                if (mc != null) mc.method_1507(new KeyboardScreen(this));
            }
        ).method_46434(sw - 85 - 10 - 75 - 6, topY, 75, topBtnH).method_46431());

        // 🎵 Music
        this.method_37063(class_4185.method_46430(
            class_2561.method_43470("🎵 Music"),
            b -> {
                class_310 mc = class_310.method_1551();
                if (mc != null) mc.method_1507(new dev.lyricsflow.gui.LyricsFlowScreen(this));
            }
        ).method_46434(sw - 85 - 10 - 75 - 6 - 72 - 6, topY, 72, topBtnH).method_46431());

        // 5. Wardrobe Button unter dem Skin
        int wardW = 80;
        int wardX = cx + 160 + (120 - wardW) / 2;
        int wardY = startY + 145;
        this.method_37063(class_4185.method_46430(
            class_2561.method_43470("Wardrobe"),
            b -> {
                class_310 mc = class_310.method_1551();
                if (mc != null) mc.method_1507(new class_440(this, mc.field_1690));
            }
        ).method_46434(wardX, wardY, wardW, btnH).method_46431());
    }

    @Override
    public void method_25420(class_332 context, int mouseX, int mouseY, float deltaTicks) {
    }

    @Override
    public boolean method_25422() {
        return false;
    }

    @Override
    public void method_25394(class_332 context, int mouseX, int mouseY, float deltaTicks) {
        int w = this.field_22789;
        int h = this.field_22790;
        int cx = w / 2;

        // 1. Aurora Dynamic Animated Background
        LotusBackdrop.render(context, w, h, 1.0f);

        // 2. 3D Mannequin
        drawMannequin(context, mouseX, mouseY, deltaTicks);

        // 3. Zentrierter SkyLyrics Titel (Ohne KI-Noten-Logo, clean und zentriert!)
        class_327 font = this.field_22787 != null ? this.field_22787.field_1772 : null;
        if (font != null) {
            int startY = h / 2 - 10;
            Matrix3x2fStack matrices = context.method_51448();
            matrices.pushMatrix();
            float scale = 2.4f;
            matrices.translate((float) cx, (float) (startY - 38));
            matrices.scale(scale, scale);

            class_2561 title = class_2561.method_43470("§b§lSky§f§lLyrics");
            int tw = font.method_27525(title);
            context.method_51439(font, title, -tw / 2, -4, 0xFFFFFFFF, true);
            matrices.popMatrix();

            // Unten links: Subtiles Versions-Branding
            context.method_51439(font, class_2561.method_43470("§7SkyLyrics 1.0  §8Minecraft 1.21.11"), 12, h - 14, 0xFFCAD4E0, true);
        }

        // 4. Alle Buttons zeichnen (mittels LotusButtonRenderer über PressableWidgetMixin!)
        super.method_25394(context, mouseX, mouseY, deltaTicks);

        context.method_71278();
    }

    private void drawMannequin(class_332 context, int mouseX, int mouseY, float deltaTicks) {
        try {
            if (this.skinWidget == null) {
                initSkinWidget();
            }
            if (this.skinWidget == null) return;

            int cx = this.field_22789 / 2;
            int startY = this.field_22790 / 2 - 10;

            int skinX = cx + 160;
            int skinY = startY - 45;
            int skinW = 120;
            int skinH = 180;

            this.skinWidget.method_46421(skinX);
            this.skinWidget.method_46419(skinY);
            this.skinWidget.method_25358(skinW);
            this.skinWidget.method_53533(skinH);
            this.skinWidget.method_25394(context, mouseX, mouseY, deltaTicks);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}

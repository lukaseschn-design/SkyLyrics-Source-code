package dev.lyricsflow.render;

import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.render.Gfx;
import dev.lyricsflow.util.Col;
import net.minecraft.class_2960;
import net.minecraft.class_332;

public final class HotbarStyle {
    private static float selX = Float.NaN;

    private HotbarStyle() {
    }

    public static boolean draw(class_332 g, class_2960 id, int x, int y, int w, int h) {
        if (!(ConfigManager.get().enabled && ConfigManager.get().customHotbar && "minecraft".equals(id.method_12836()))) {
            return false;
        }
        String path = id.method_12832();
        int custom = ConfigManager.get().hotbarColor;
        int accent = custom != 0 ? custom | 0xFF000000 : -6325530;
        int barCol = ConfigManager.get().hotbarBarColor;
        int bar = barCol != 0 ? 0xD8000000 | barCol & 0xFFFFFF : -668851164;
        switch (path) {
            case "hud/hotbar": {
                Gfx.round(g, x, y, w, h, 5.5f, bar);
                return true;
            }
            case "hud/hotbar_selection": {
                float sx = (float)x + 3.0f;
                float sy = (float)y + 3.0f;
                float s = 18.0f;
                Gfx.round(g, sx, sy, s, s, 3.0f, Col.alpha(accent, 0.22f));
                Gfx.roundOutline(g, sx, sy, s, s, 3.0f, 1.0f, Col.alpha(accent, 0.95f));
                return true;
            }
            case "hud/hotbar_offhand_left": 
            case "hud/hotbar_offhand_right": {
                float bx = path.endsWith("left") ? (float)x : (float)(x + w - 22);
                float by = y + 1;
                Gfx.round(g, bx, by - 1.0f, 22.0f, 22.0f, 5.5f, bar);
                return true;
            }
        }
        return false;
    }
}


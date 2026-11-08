package dev.lyricsflow.app.hud;

import net.minecraft.class_2561;
import net.minecraft.class_310;

public final class Island {
    private Island() {
    }

    public static void notify(String text, boolean ok) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1705 != null) {
            mc.field_1705.method_1758((class_2561)class_2561.method_43470((String)text), false);
        }
    }
}


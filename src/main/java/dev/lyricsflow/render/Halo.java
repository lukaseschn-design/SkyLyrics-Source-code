package dev.lyricsflow.render;

import dev.lyricsflow.LyricsFlow;
import net.minecraft.class_12249;
import net.minecraft.class_2960;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public final class Halo {
    private static final class_2960 GLOW = LyricsFlow.id("textures/gui/shape/glow.png");
    private static final int LIGHT = 0xF000F0;

    private Halo() {
    }

    public static void draw(Matrix4f m, float x0, float y0, float x1, float y1, int argb, class_4597 buf) {
        if (argb >>> 24 < 3 || buf == null) {
            return;
        }
        class_4588 vc = buf.method_73477(class_12249.method_76014((class_2960)GLOW));
        int ov = class_4608.field_21444;
        Halo.v(vc, m, x0, y0, 0.0f, 0.0f, argb, ov);
        Halo.v(vc, m, x0, y1, 0.0f, 1.0f, argb, ov);
        Halo.v(vc, m, x1, y1, 1.0f, 1.0f, argb, ov);
        Halo.v(vc, m, x1, y0, 1.0f, 0.0f, argb, ov);
        Halo.v(vc, m, x1, y0, 1.0f, 0.0f, argb, ov);
        Halo.v(vc, m, x1, y1, 1.0f, 1.0f, argb, ov);
        Halo.v(vc, m, x0, y1, 0.0f, 1.0f, argb, ov);
        Halo.v(vc, m, x0, y0, 0.0f, 0.0f, argb, ov);
    }

    private static void v(class_4588 vc, Matrix4f m, float x, float y, float u, float vv, int argb, int ov) {
        vc.method_22918((Matrix4fc)m, x, y, 0.0f).method_39415(argb).method_22913(u, vv).method_22922(ov).method_60803(0xF000F0).method_22914(0.0f, 1.0f, 0.0f);
    }
}


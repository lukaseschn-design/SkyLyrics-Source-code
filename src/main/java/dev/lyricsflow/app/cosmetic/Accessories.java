package dev.lyricsflow.app.cosmetic;

import dev.lyricsflow.app.Core;
import dev.lyricsflow.render.Cosmetics;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.imageio.ImageIO;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4597;
import org.joml.Matrix4f;

public final class Accessories {
    public static final String[] HATS = Cosmetics.IDS;
    public static final class_2960 WHITE = class_2960.method_60655((String)"lyricsflow", (String)"cosmetic/white");
    private static boolean whiteReady;
    private static boolean whiteFailed;
    private static final float[][] MESH;
    private static final boolean[] TRIED;
    private static final int STRIDE = 14;

    private Accessories() {
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static float[] mesh(int i) {
        if (TRIED[i]) {
            return MESH[i];
        }
        Accessories.TRIED[i] = true;
        try {
            ByteBuffer b;
            try (InputStream in = Accessories.class.getResourceAsStream("/assets/lyricsflow/models/" + HATS[i] + ".bin")) {
                if (in == null) {
                    return null;
                }
                b = ByteBuffer.wrap(in.readAllBytes()).order(ByteOrder.LITTLE_ENDIAN);
                if (b.getInt() != 827149390) {
                    return null;
                }
            }
            int n = b.getInt();
            float[] m = new float[n * 14];
            int t = 0;
            while (true) {
                if (t >= n) {
                    Accessories.MESH[i] = m;
                    float[] fArray = MESH[i];
                    return fArray;
                }
                for (int k = 0; k < 12; ++k) {
                    m[t * 14 + k] = b.getFloat();
                }
                m[t * 14 + 12] = Float.intBitsToFloat(b.getInt());
                m[t * 14 + 13] = b.getInt();
                ++t;
            }
        }
        catch (Throwable var9) {
            Core.LOG.warn("Hut-Modell fehlt: " + HATS[i], var9);
            return MESH[i];
        }
    }

    public static boolean whiteTex() {
        return Accessories.white();
    }

    private static boolean white() {
        if (whiteReady) {
            return true;
        }
        if (whiteFailed) {
            return false;
        }
        try {
            BufferedImage img = new BufferedImage(4, 4, 2);
            for (int y = 0; y < 4; ++y) {
                for (int x = 0; x < 4; ++x) {
                    img.setRGB(x, y, -1);
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write((RenderedImage)img, "png", out);
            class_310.method_1551().method_1531().method_4616(WHITE, (class_1044)new class_1043(() -> "lyricsflow_white", class_1011.method_49277((byte[])out.toByteArray())));
            whiteReady = true;
        }
        catch (Throwable var3) {
            whiteFailed = true;
        }
        return whiteReady;
    }

    public static void render(class_310 mc, class_4597 buffers, Matrix4f base, class_243 cam, float pt) {
    }

    static {
        MESH = new float[HATS.length][];
        TRIED = new boolean[HATS.length];
    }
}


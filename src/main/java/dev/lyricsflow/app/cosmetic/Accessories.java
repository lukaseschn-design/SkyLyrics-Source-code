/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.resources.Identifier
 *  net.minecraft.client.Minecraft
 *  dev.lyricsflow.compat.LegacyBufferSource
 *  org.joml.Matrix4f
 */
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
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import dev.lyricsflow.compat.LegacyBufferSource;
import org.joml.Matrix4f;

public final class Accessories {
    public static final String[] HATS = Cosmetics.IDS;
    public static final Identifier WHITE = Identifier.fromNamespaceAndPath((String)"lyricsflow", (String)"cosmetic/white");
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
            Minecraft.getInstance().getTextureManager().register(WHITE, (AbstractTexture)new DynamicTexture(() -> "lyricsflow_white", NativeImage.read((byte[])out.toByteArray())));
            whiteReady = true;
        }
        catch (Throwable var3) {
            whiteFailed = true;
        }
        return whiteReady;
    }

    public static void render(Minecraft mc, LegacyBufferSource buffers, Matrix4f base, Vec3 cam, float pt) {
    }

    static {
        MESH = new float[HATS.length][];
        TRIED = new boolean[HATS.length];
    }
}

package Yuki.lumiclient.gui;

import net.minecraft.class_1011; // NativeImage
import net.minecraft.class_1043; // NativeImageBackedTexture
import net.minecraft.class_1044; // AbstractTexture
import net.minecraft.class_10799; // RenderPipelines
import net.minecraft.class_2960; // Identifier
import net.minecraft.class_310; // MinecraftClient
import net.minecraft.class_332; // DrawContext
import org.joml.Matrix3x2fStack;

import java.util.Random;

public final class LotusBackdrop {

    public record Curtain(float base, float amp, float height, int waves, int ripples, int bands, float phase, int rgb, float alpha, float drift, float rays) {
        public float top() {
            return this.base - this.amp * 1.3f - this.height;
        }

        public float bottom() {
            return this.base + this.amp * 1.3f + 0.05f;
        }
    }

    public record Style(String name, int skyTop, int skyMid, int skyBot, int ridge, int glow, Curtain[] curtains) {
    }

    private static final int CURTAIN_W = 512;
    private static final int CURTAIN_H = 256;

    public static final Style[] STYLES = new Style[]{
            // 0: Polar (Cyan / Mint / Magenta Aurora)
            new Style("Polar", 0xFF030A0A, 0xFF071019, 0xFF0B1622, 0xFF04070A, 0x3343EFB0, new Curtain[]{
                    new Curtain(0.44f, 0.07f, 0.42f, 1, 3, 5, 0.4f, 0x4DF5A6, 0.42f, 0.014285714f, 0.55f),
                    new Curtain(0.30f, 0.08f, 0.30f, 1, 4, 4, 2.1f, 0xE06AD9, 0.20f, -0.010526316f, 0.40f),
                    new Curtain(0.52f, 0.05f, 0.26f, 2, 5, 6, 4.0f, 0x3DDDCE, 0.26f, 0.020f, 0.60f)
            }),
            // 1: Glacier (Ice Blue / Deep Sky Sapphire)
            new Style("Glacier", 0xFF03070D, 0xFF07121E, 0xFF0D1C2A, 0xFF05090F, 0x2C7FDBFF, new Curtain[]{
                    new Curtain(0.42f, 0.065f, 0.40f, 1, 3, 5, 0.9f, 0x7FE6FF, 0.36f, 0.0125f, 0.50f),
                    new Curtain(0.36f, 0.085f, 0.46f, 1, 3, 4, 2.6f, 0xBFD8FF, 0.22f, -0.009090909f, 0.35f),
                    new Curtain(0.54f, 0.045f, 0.24f, 2, 5, 6, 4.4f, 0x4FB3FF, 0.24f, 0.018181818f, 0.60f)
            }),
            // 2: Dusk (Violet / Amber / Magenta Sunset)
            new Style("Dusk", 0xFF06050E, 0xFF110B1E, 0xFF1B1026, 0xFF07050C, 0x33FE67B5, new Curtain[]{
                    new Curtain(0.44f, 0.07f, 0.40f, 1, 3, 5, 0.4f, 0xB57CFF, 0.36f, 0.013333334f, 0.45f),
                    new Curtain(0.38f, 0.09f, 0.46f, 1, 3, 4, 2.1f, 0xFF7AC6, 0.24f, -0.010526316f, 0.35f),
                    new Curtain(0.54f, 0.05f, 0.26f, 2, 5, 6, 4.0f, 0x7A9BFF, 0.22f, 0.020f, 0.55f)
            }),
            // 3: Mono (Moonlight Silver / Dark Velvet)
            new Style("Mono", 0xFF030507, 0xFF090C10, 0xFF0F1318, 0xFF050608, 0x22FFFFFF, new Curtain[]{
                    new Curtain(0.44f, 0.07f, 0.40f, 1, 3, 5, 0.4f, 0xE7F0F5, 0.24f, 0.014285714f, 0.50f),
                    new Curtain(0.38f, 0.085f, 0.44f, 1, 3, 4, 2.1f, 0xAFC4E0, 0.16f, -0.010526316f, 0.40f),
                    new Curtain(0.54f, 0.05f, 0.26f, 2, 5, 6, 4.0f, 0xFFFFFF, 0.14f, 0.020f, 0.60f)
            }),
            // 4: Blossom (Lotus Special - Sakura Pink & Orchid Lights)
            new Style("Blossom", 0xFF0A040A, 0xFF160815, 0xFF220C1F, 0xFF0C030A, 0x35FF78B5, new Curtain[]{
                    new Curtain(0.44f, 0.07f, 0.42f, 1, 3, 5, 0.4f, 0xFFFF78B5, 0.40f, 0.013f, 0.55f),
                    new Curtain(0.32f, 0.08f, 0.34f, 1, 4, 4, 2.1f, 0xFFFFC2E2, 0.25f, -0.010f, 0.40f),
                    new Curtain(0.52f, 0.05f, 0.26f, 2, 5, 6, 4.0f, 0xFFB359D6, 0.26f, 0.018f, 0.60f)
            })
    };

    private static final class_2960[][] CURTAIN_TEX = new class_2960[STYLES.length][];
    private static int current = 0;

    private static class_2960 ridgeId;
    private static int ridgeW = 0;
    private static int ridgeH = 0;
    private static int ridgeStyle = -1;

    private static final int HERO_STARS = 8;
    private static final float[] HERO_X = new float[HERO_STARS];
    private static final float[] HERO_Y = new float[HERO_STARS];
    private static final float[] HERO_FREQ = new float[HERO_STARS];
    private static final float[] HERO_PHASE = new float[HERO_STARS];
    private static class_2960 starsId;
    private static int starsW = 0;
    private static int starsH = 0;

    static {
        Random rand = new Random(0x4C6F747573L); // "Lotus"
        for (int i = 0; i < HERO_STARS; i++) {
            HERO_X[i] = 0.06f + rand.nextFloat() * 0.88f;
            HERO_Y[i] = 0.04f + rand.nextFloat() * 0.40f;
            HERO_FREQ[i] = 0.40f + rand.nextFloat() * 0.90f;
            HERO_PHASE[i] = rand.nextFloat() * 6.28f;
        }
    }

    private LotusBackdrop() {}

    public static int current() {
        return current;
    }

    public static void setCurrent(int idx) {
        current = Math.floorMod(idx, STYLES.length);
    }

    public static void cycle() {
        setCurrent(current + 1);
    }

    public static String name() {
        return STYLES[current].name;
    }

    public static Style style() {
        return STYLES[current];
    }

    public static void render(class_332 context, int width, int height, float alpha) {
        if (width <= 0 || height <= 0 || alpha <= 0.0f) {
            return;
        }

        Style s = style();
        float timeSec = (float) (System.currentTimeMillis() % 10000000L) / 1000.0f;
        int a255 = Math.round(255.0f * Math.min(1.0f, alpha));

        int hMid = Math.round((float) height * 0.60f);
        int topCol = withAlpha(s.skyTop, a255);
        int midCol = withAlpha(s.skyMid, a255);
        int botCol = withAlpha(s.skyBot, a255);

        context.method_25296(0, 0, width, hMid, topCol, midCol);
        context.method_25296(0, hMid, width, height, midCol, botCol);

        stars(context, width, height, timeSec, alpha);

        int styleIdx = current;
        for (int i = 0; i < s.curtains.length; i++) {
            curtain(context, styleIdx, i, s.curtains[i], width, height, timeSec, alpha);
        }

        ridge(context, s, width, height, alpha);
    }

    private static void stars(class_332 context, int width, int height, float timeSec, float alpha) {
        class_2960 fieldId = starField(width, height);
        if (fieldId != null) {
            int col = withAlpha(0xFFFFFF, Math.round(255.0f * alpha));
            context.method_25293(class_10799.field_56883, fieldId, 0, 0, 0.0f, 0.0f, width, height, starsW, starsH, starsW, starsH, col);
        }

        // Hero Twinkling Stars
        for (int i = 0; i < HERO_STARS; i++) {
            int sx = (int) (HERO_X[i] * (float) width);
            int sy = (int) (HERO_Y[i] * (float) height);
            float twinkle = Math.abs((float) Math.sin(timeSec * HERO_FREQ[i] + HERO_PHASE[i]));
            int starAlpha = (int) ((70.0f + twinkle * 185.0f) * alpha);
            context.method_25294(sx, sy, sx + 1, sy + 1, withAlpha(0xFFFFFF, starAlpha));
        }
    }

    private static class_2960 starField(int width, int height) {
        class_310 mc = class_310.method_1551();
        int scale = Math.max(1, mc.method_22683().method_4495());
        int pxW = width * scale;
        int pxH = height * scale;

        if (starsId != null && starsW == pxW && starsH == pxH) {
            return starsId;
        }
        if (pxW <= 0 || pxH <= 0 || (long) pxW * (long) pxH > 16777216L) {
            return null;
        }

        class_1011 img = new class_1011(class_1011.class_1012.field_4997, pxW, pxH, true);
        Random rand = new Random(0x1337BEEFL);
        for (int i = 0; i < 90; i++) {
            int x = rand.nextInt(pxW);
            int y = (int) (rand.nextFloat() * (float) pxH * 0.70f);
            int starSize = rand.nextFloat() < 0.82f ? 1 : Math.max(1, scale / 2);
            int a = 80 + rand.nextInt(150);
            int argb = (a << 24) | 0x00FFFFFF;

            for (int dy = y; dy < Math.min(pxH, y + starSize); dy++) {
                for (int dx = x; dx < Math.min(pxW, x + starSize); dx++) {
                    img.method_61941(dx, dy, argb);
                }
            }
        }

        if (starsId != null) {
            mc.method_1531().method_4615(starsId);
        }
        starsId = upload("stars", img);
        starsW = pxW;
        starsH = pxH;
        return starsId;
    }

    private static void curtain(class_332 context, int styleIdx, int curtainIdx, Curtain c, int width, int height, float timeSec, float alpha) {
        class_2960 tex = texture(styleIdx, curtainIdx, c);
        int topY = Math.round((float) height * c.top());
        int curtainH = Math.max(1, Math.round((float) height * (c.bottom() - c.top())));

        float driftShift = (timeSec * c.drift % 1.0f + 1.0f) % 1.0f;
        float pulse = 0.8f + 0.2f * (float) Math.sin(timeSec * 0.6f + c.phase * 2.0f);
        float breatheY = (float) height * 0.018f * (float) Math.sin(timeSec * 0.45f + c.phase);

        int curtainAlpha = Math.round(255.0f * c.alpha * pulse * alpha);
        int tint = (curtainAlpha << 24) | (c.rgb & 0x00FFFFFF);

        Matrix3x2fStack stack = context.method_51448();
        stack.pushMatrix();
        stack.translate(-driftShift * (float) width, breatheY);

        // Dual tiling so scrolling is seamless across width
        context.method_25293(class_10799.field_56883, tex, 0, topY, 0.0f, 0.0f, width, curtainH, CURTAIN_W, CURTAIN_H, CURTAIN_W, CURTAIN_H, tint);
        context.method_25293(class_10799.field_56883, tex, width, topY, 0.0f, 0.0f, width, curtainH, CURTAIN_W, CURTAIN_H, CURTAIN_W, CURTAIN_H, tint);

        stack.popMatrix();
    }

    private static class_2960 texture(int styleIdx, int curtainIdx, Curtain c) {
        if (CURTAIN_TEX[styleIdx] == null) {
            CURTAIN_TEX[styleIdx] = new class_2960[STYLES[styleIdx].curtains.length];
        }
        if (CURTAIN_TEX[styleIdx][curtainIdx] != null) {
            return CURTAIN_TEX[styleIdx][curtainIdx];
        }

        float[] rayMap = new float[CURTAIN_W];
        Random rand = new Random(0x4175726FL * (long) styleIdx + (long) curtainIdx);
        for (int i = 0; i < 6; i++) {
            int freq = 9 + rand.nextInt(40);
            double phase = rand.nextDouble() * Math.PI * 2.0;
            for (int x = 0; x < CURTAIN_W; x++) {
                rayMap[x] += (float) Math.sin(Math.PI * 2.0 * (double) freq * (double) x / (double) CURTAIN_W + phase) / 6.0f;
            }
        }

        float[] grid = new float[CURTAIN_W * CURTAIN_H];
        for (int x = 0; x < CURTAIN_W; x++) {
            double angle = Math.PI * 2.0 * ((double) x + 0.5) / (double) CURTAIN_W;
            float waveCurve = c.base + c.amp * (float) (Math.sin((double) c.waves * angle + (double) c.phase)
                    + 0.3 * Math.sin((double) c.ripples * angle + (double) c.phase));
            float bandFactor = 0.55f + 0.45f * (float) Math.sin((double) c.bands * angle + (double) (c.phase * 3.0f));
            bandFactor *= (1.0f - c.rays * (0.5f - 0.5f * rayMap[x]));

            for (int y = 0; y < CURTAIN_H; y++) {
                float sampleY = c.top() + ((float) y + 0.5f) / (float) CURTAIN_H * (c.bottom() - c.top());
                float diff = waveCurve - sampleY;
                float intensity;
                if (diff < 0.0f) {
                    intensity = (float) Math.exp(diff / 0.012f);
                } else {
                    float norm = diff / c.height;
                    if (norm <= 0.25f) {
                        intensity = 1.0f - 0.45f * (norm / 0.25f);
                    } else {
                        intensity = Math.max(0.0f, 0.55f * (1.0f - (norm - 0.25f) / 0.75f));
                    }
                }
                grid[y * CURTAIN_W + x] = intensity * bandFactor;
            }
        }

        blur(grid, 1, 4);
        blur(grid, 1, 4);

        class_1011 img = new class_1011(class_1011.class_1012.field_4997, CURTAIN_W, CURTAIN_H, true);
        for (int i = 0; i < grid.length; i++) {
            int a = Math.round(255.0f * Math.min(1.0f, Math.max(0.0f, grid[i])));
            int argb = a == 0 ? 0 : (a << 24) | 0x00FFFFFF;
            img.method_61941(i % CURTAIN_W, i / CURTAIN_W, argb);
        }

        CURTAIN_TEX[styleIdx][curtainIdx] = upload("curtain_" + styleIdx + "_" + curtainIdx, img);
        return CURTAIN_TEX[styleIdx][curtainIdx];
    }

    private static void blur(float[] data, int radX, int radY) {
        float[] temp = new float[data.length];
        for (int y = 0; y < CURTAIN_H; y++) {
            for (int x = 0; x < CURTAIN_W; x++) {
                float sum = 0.0f;
                for (int dx = -radX; dx <= radX; dx++) {
                    sum += data[y * CURTAIN_W + Math.floorMod(x + dx, CURTAIN_W)];
                }
                temp[y * CURTAIN_W + x] = sum / (float) (2 * radX + 1);
            }
        }
        for (int x = 0; x < CURTAIN_W; x++) {
            for (int y = 0; y < CURTAIN_H; y++) {
                float sum = 0.0f;
                for (int dy = -radY; dy <= radY; dy++) {
                    int sy = Math.max(0, Math.min(CURTAIN_H - 1, y + dy));
                    sum += temp[sy * CURTAIN_W + x];
                }
                data[y * CURTAIN_W + x] = sum / (float) (2 * radY + 1);
            }
        }
    }

    private static void ridge(class_332 context, Style style, int width, int height, float alpha) {
        int glowStartY = Math.round((float) height * 0.70f);
        int glowAlpha = Math.round((float) (style.glow >>> 24 & 0xFF) * alpha);
        context.method_25296(0, glowStartY, width, height, withAlpha(style.glow, 0), withAlpha(style.glow, glowAlpha));

        class_2960 rId = ridgeTexture(style, width, height);
        if (rId != null) {
            context.method_25293(class_10799.field_56883, rId, 0, 0, 0.0f, 0.0f, width, height, ridgeW, ridgeH, ridgeW, ridgeH, withAlpha(0xFFFFFF, Math.round(255.0f * alpha)));
        }
    }

    private static class_2960 ridgeTexture(Style style, int width, int height) {
        class_310 mc = class_310.method_1551();
        int scale = Math.max(1, mc.method_22683().method_4495());
        int pxW = width * scale;
        int pxH = height * scale;

        if (ridgeId != null && ridgeW == pxW && ridgeH == pxH && ridgeStyle == current) {
            return ridgeId;
        }
        if (pxW <= 0 || pxH <= 0 || (long) pxW * (long) pxH > 16777216L) {
            return null;
        }

        class_1011 img = new class_1011(class_1011.class_1012.field_4997, pxW, pxH, true);
        int baseRgb = style.ridge & 0x00FFFFFF;

        for (int x = 0; x < pxW; x++) {
            double normX = (double) x / (double) pxW;
            double r1 = 0.80 - 0.05 * Math.sin(normX * 7.1 + 1.3) - 0.03 * Math.sin(normX * 17.3 + 0.4) - 0.012 * Math.sin(normX * 53.0);
            double r2 = 0.87 - 0.04 * Math.sin(normX * 4.3 + 2.2) - 0.025 * Math.sin(normX * 13.7 + 1.1) - 0.01 * Math.sin(normX * 41.0 + 0.7);

            int y1 = (int) (r1 * (double) pxH);
            int y2 = (int) (r2 * (double) pxH);

            for (int y = Math.max(0, y1); y < pxH; y++) {
                int a = (y < y2) ? 170 : 255;
                img.method_61941(x, y, (a << 24) | baseRgb);
            }
        }

        if (ridgeId != null) {
            mc.method_1531().method_4615(ridgeId);
        }
        ridgeId = upload("ridge", img);
        ridgeW = pxW;
        ridgeH = pxH;
        ridgeStyle = current;
        return ridgeId;
    }

    private static class_2960 upload(String name, class_1011 img) {
        class_2960 id = class_2960.method_60655("lumiclient", name);
        class_1043 tex = new class_1043(() -> "lotus_" + name, img);
        tex.method_4524();
        class_310.method_1551().method_1531().method_4616(id, (class_1044) tex);
        return id;
    }

    private static int withAlpha(int argb, int alpha) {
        return (alpha << 24) | (argb & 0x00FFFFFF);
    }
}
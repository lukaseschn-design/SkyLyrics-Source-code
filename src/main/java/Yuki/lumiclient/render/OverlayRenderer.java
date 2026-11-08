package Yuki.lumiclient.render;

import Yuki.lumiclient.render.nanovg.GlStateSnapshot;
import Yuki.lumiclient.render.nanovg.NVGRenderer;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.class_10868;
import net.minecraft.class_276;
import net.minecraft.class_310;
import org.lwjgl.opengl.GL33C;

public final class OverlayRenderer {
    private static int fbo = -1;
    private static boolean crashed = false;

    private OverlayRenderer() {
    }

    public static float uiScale() {
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.method_22683() == null) return 1.0f;
        return Math.max(1.0f, (float) mc.method_22683().method_4506() / 1080.0f);
    }

    public static void render() {
        if (crashed) return;
        class_310 mc = class_310.method_1551();
        if (mc == null) return;
        if (!(mc.field_1755 instanceof NvgDrawable nvgDrawable)) return;

        class_276 target = mc.method_1522();
        if (target == null) return;
        GpuTexture tex = target.method_30277();
        if (!(tex instanceof class_10868 colorTexture)) return;

        try {
            GlStateSnapshot state = GlStateSnapshot.capture();
            try {
                if (!bindOverlayFbo(colorTexture, target.field_1482, target.field_1481)) {
                    return;
                }
                NVGRenderer vg = NVGRenderer.get();
                if (vg.hasFont()) {
                    float scale = uiScale();
                    float uiWidth = (float) target.field_1482 / scale;
                    float uiHeight = (float) target.field_1481 / scale;
                    vg.beginFrame(target.field_1482, target.field_1481, 1.0f);
                    vg.save();
                    vg.scale(scale);
                    nvgDrawable.renderNvg(vg, uiMouseX(), uiMouseY(), uiWidth, uiHeight);
                    vg.restore();
                    vg.endFrame();
                }
            } finally {
                state.restore();
            }
        } catch (Throwable t) {
            crashed = true;
        }
    }

    private static boolean bindOverlayFbo(class_10868 colorTexture, int width, int height) {
        if (fbo == -1) {
            fbo = GL33C.glGenFramebuffers();
        }
        GL33C.glBindFramebuffer(36160, fbo);
        GL33C.glFramebufferTexture2D(36160, 36064, 3553, colorTexture.method_68427(), 0);
        if (GL33C.glCheckFramebufferStatus(36160) != 36053) {
            return false;
        }
        GL33C.glViewport(0, 0, width, height);
        GL33C.glDisable(3089);
        return true;
    }

    public static float uiMouseX() {
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1729 == null || mc.method_22683() == null) return 0f;
        return (float) (mc.field_1729.method_1603() * (double) mc.method_22683().method_4489() / (double) Math.max(1, mc.method_22683().method_4480())) / uiScale();
    }

    public static float uiMouseY() {
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.field_1729 == null || mc.method_22683() == null) return 0f;
        return (float) (mc.field_1729.method_1604() * (double) mc.method_22683().method_4506() / (double) Math.max(1, mc.method_22683().method_4507())) / uiScale();
    }

    public static float guiToUi(double guiCoord) {
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.method_22683() == null) return (float) guiCoord;
        float pixels = (float) (guiCoord * (double) mc.method_22683().method_4495());
        return pixels / uiScale();
    }

    public static float uiWidth() {
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.method_22683() == null) return 960f;
        return (float) mc.method_22683().method_4489() / uiScale();
    }

    public static float uiHeight() {
        class_310 mc = class_310.method_1551();
        if (mc == null || mc.method_22683() == null) return 540f;
        return (float) mc.method_22683().method_4506() / uiScale();
    }
}

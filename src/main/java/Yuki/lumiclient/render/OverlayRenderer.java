package Yuki.lumiclient.render;

import Yuki.lumiclient.render.nanovg.GlStateSnapshot;
import Yuki.lumiclient.render.nanovg.NVGRenderer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.backend.opengl.GlTexture;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL33C;

public final class OverlayRenderer {
    private static int fbo = -1;
    private static boolean crashed = false;

    private OverlayRenderer() {
    }

    public static float uiScale() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) return 1.0f;
        return Math.max(1.0f, (float) mc.getWindow().getHeight() / 1080.0f);
    }

    public static void render() {
        if (crashed) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        if (!(mc.gui.screen() instanceof NvgDrawable nvgDrawable)) return;

        RenderTarget target = mc.gameRenderer.mainRenderTarget();
        if (target == null) return;
        GpuTexture tex = target.getColorTexture();
        if (!(tex instanceof GlTexture colorTexture)) return;

        try {
            GlStateSnapshot state = GlStateSnapshot.capture();
            try {
                if (!bindOverlayFbo(colorTexture, target.width, target.height)) {
                    return;
                }
                NVGRenderer vg = NVGRenderer.get();
                if (vg.hasFont()) {
                    float scale = uiScale();
                    float uiWidth = (float) target.width / scale;
                    float uiHeight = (float) target.height / scale;
                    vg.beginFrame(target.width, target.height, 1.0f);
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

    private static boolean bindOverlayFbo(GlTexture colorTexture, int width, int height) {
        if (fbo == -1) {
            fbo = GL33C.glGenFramebuffers();
        }
        GL33C.glBindFramebuffer(36160, fbo);
        GL33C.glFramebufferTexture2D(36160, 36064, 3553, colorTexture.glId(), 0);
        if (GL33C.glCheckFramebufferStatus(36160) != 36053) {
            return false;
        }
        GL33C.glViewport(0, 0, width, height);
        GL33C.glDisable(3089);
        return true;
    }

    public static float uiMouseX() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.mouseHandler == null || mc.getWindow() == null) return 0f;
        return (float) (mc.mouseHandler.xpos() * (double) mc.getWindow().getWidth() / (double) Math.max(1, mc.getWindow().getScreenWidth())) / uiScale();
    }

    public static float uiMouseY() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.mouseHandler == null || mc.getWindow() == null) return 0f;
        return (float) (mc.mouseHandler.ypos() * (double) mc.getWindow().getHeight() / (double) Math.max(1, mc.getWindow().getScreenHeight())) / uiScale();
    }

    public static float guiToUi(double guiCoord) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) return (float) guiCoord;
        float pixels = (float) (guiCoord * (double) mc.getWindow().getGuiScale());
        return pixels / uiScale();
    }

    public static float uiWidth() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) return 960f;
        return (float) mc.getWindow().getWidth() / uiScale();
    }

    public static float uiHeight() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) return 540f;
        return (float) mc.getWindow().getHeight() / uiScale();
    }
}

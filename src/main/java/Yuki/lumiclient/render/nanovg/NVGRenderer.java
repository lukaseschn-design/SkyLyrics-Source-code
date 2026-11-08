package Yuki.lumiclient.render.nanovg;

import Yuki.lumiclient.util.Colors;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public final class NVGRenderer {
    public static final String FONT_INTER = "inter";
    private static NVGRenderer instance;
    private final long ctx = NanoVGGL3.nvgCreate((int)1);
    private boolean interLoaded;
    private final List<ByteBuffer> retainedFontData = new ArrayList<ByteBuffer>();
    private String activeFont = "inter";
    private final ArrayDeque<Float> alphaStack = new ArrayDeque();
    private float appliedAlpha = 1.0f;

    private NVGRenderer() {
        if (this.ctx == 0L) {
            throw new IllegalStateException("Failed to create NanoVG context");
        }
        this.interLoaded = this.loadFont(FONT_INTER, "assets/lumiclient/fonts/Inter-Bold.ttf");
    }

    public static NVGRenderer get() {
        if (instance == null) {
            instance = new NVGRenderer();
        }
        return instance;
    }

    public long ctx() {
        return this.ctx;
    }

    private boolean loadFont(String name, String resourcePath) {
        try {
            boolean var11;
            block10: {
                boolean var12;
                block11: {
                    boolean var122;
                    try (InputStream in = NVGRenderer.class.getClassLoader().getResourceAsStream(resourcePath);){
                        if (in == null) {
                            var11 = false;
                            break block10;
                        }
                        byte[] bytes = in.readAllBytes();
                        ByteBuffer buffer = MemoryUtil.memAlloc((int)bytes.length);
                        buffer.put(bytes).flip();
                        int handle = NanoVG.nvgCreateFontMem((long)this.ctx, (CharSequence)name, (ByteBuffer)buffer, (boolean)false);
                        if (handle == -1) {
                            MemoryUtil.memFree((Buffer)buffer);
                            var12 = false;
                            break block11;
                        }
                        this.retainedFontData.add(buffer);
                        var122 = true;
                    }
                    return var122;
                }
                return var12;
            }
            return var11;
        }
        catch (IOException var111) {
            return false;
        }
    }

    public void setFontMode(String mode) {
        this.activeFont = this.interLoaded ? FONT_INTER : null;
    }

    public boolean hasFont() {
        return this.activeFont != null;
    }

    public void beginFrame(float width, float height, float pixelRatio) {
        this.alphaStack.clear();
        this.appliedAlpha = 1.0f;
        NanoVG.nvgBeginFrame((long)this.ctx, (float)width, (float)height, (float)pixelRatio);
    }

    public void endFrame() {
        NanoVG.nvgEndFrame((long)this.ctx);
    }

    public void save() {
        this.alphaStack.push(Float.valueOf(this.appliedAlpha));
        NanoVG.nvgSave((long)this.ctx);
    }

    public void restore() {
        if (!this.alphaStack.isEmpty()) {
            this.appliedAlpha = this.alphaStack.pop().floatValue();
        }
        NanoVG.nvgRestore((long)this.ctx);
    }

    public void scale(float s) {
        NanoVG.nvgScale((long)this.ctx, (float)s, (float)s);
    }

    public void translate(float x, float y) {
        NanoVG.nvgTranslate((long)this.ctx, (float)x, (float)y);
    }

    public void alpha(float a) {
        this.appliedAlpha *= Math.clamp(a, 0.0f, 1.0f);
        NanoVG.nvgGlobalAlpha((long)this.ctx, (float)this.appliedAlpha);
    }

    public void scissor(float x, float y, float w, float h) {
        NanoVG.nvgIntersectScissor((long)this.ctx, (float)x, (float)y, (float)w, (float)h);
    }

    public void rect(float x, float y, float w, float h, float radius, int argb) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            NanoVG.nvgBeginPath((long)this.ctx);
            NanoVG.nvgRoundedRect((long)this.ctx, (float)x, (float)y, (float)w, (float)h, (float)radius);
            NanoVG.nvgFillColor((long)this.ctx, (NVGColor)NVGRenderer.color(stack, argb));
            NanoVG.nvgFill((long)this.ctx);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void rectVarying(float x, float y, float w, float h, float rtl, float rtr, float rbr, float rbl, int argb) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            NanoVG.nvgBeginPath((long)this.ctx);
            NanoVG.nvgRoundedRectVarying((long)this.ctx, (float)x, (float)y, (float)w, (float)h, (float)rtl, (float)rtr, (float)rbr, (float)rbl);
            NanoVG.nvgFillColor((long)this.ctx, (NVGColor)NVGRenderer.color(stack, argb));
            NanoVG.nvgFill((long)this.ctx);
        }
    }

    public void rectGradient(float x, float y, float w, float h, float radius, int from, int to, boolean vertical) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            NVGPaint paint = NVGPaint.malloc((MemoryStack)stack);
            float ex = vertical ? x : x + w;
            float ey = vertical ? y + h : y;
            NanoVG.nvgLinearGradient((long)this.ctx, (float)x, (float)y, (float)ex, (float)ey, (NVGColor)NVGRenderer.color(stack, from), (NVGColor)NVGRenderer.color(stack, to), (NVGPaint)paint);
            NanoVG.nvgBeginPath((long)this.ctx);
            NanoVG.nvgRoundedRect((long)this.ctx, (float)x, (float)y, (float)w, (float)h, (float)radius);
            NanoVG.nvgFillPaint((long)this.ctx, (NVGPaint)paint);
            NanoVG.nvgFill((long)this.ctx);
        }
    }

    public void rectVaryingGradient(float x, float y, float w, float h, float rtl, float rtr, float rbr, float rbl, int from, int to) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            NVGPaint paint = NVGPaint.malloc((MemoryStack)stack);
            NanoVG.nvgLinearGradient((long)this.ctx, (float)x, (float)y, (float)x, (float)(y + h), (NVGColor)NVGRenderer.color(stack, from), (NVGColor)NVGRenderer.color(stack, to), (NVGPaint)paint);
            NanoVG.nvgBeginPath((long)this.ctx);
            NanoVG.nvgRoundedRectVarying((long)this.ctx, (float)x, (float)y, (float)w, (float)h, (float)rtl, (float)rtr, (float)rbr, (float)rbl);
            NanoVG.nvgFillPaint((long)this.ctx, (NVGPaint)paint);
            NanoVG.nvgFill((long)this.ctx);
        }
    }

    public void chevron(float cx, float cy, float size, float stroke, int argb, boolean pointDown) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            NanoVG.nvgBeginPath((long)this.ctx);
            if (pointDown) {
                NanoVG.nvgMoveTo((long)this.ctx, (float)(cx - size), (float)(cy - size / 2.0f));
                NanoVG.nvgLineTo((long)this.ctx, (float)cx, (float)(cy + size / 2.0f));
                NanoVG.nvgLineTo((long)this.ctx, (float)(cx + size), (float)(cy - size / 2.0f));
            } else {
                NanoVG.nvgMoveTo((long)this.ctx, (float)(cx - size / 2.0f), (float)(cy - size));
                NanoVG.nvgLineTo((long)this.ctx, (float)(cx + size / 2.0f), (float)cy);
                NanoVG.nvgLineTo((long)this.ctx, (float)(cx - size / 2.0f), (float)(cy + size));
            }
            NanoVG.nvgStrokeColor((long)this.ctx, (NVGColor)NVGRenderer.color(stack, argb));
            NanoVG.nvgStrokeWidth((long)this.ctx, (float)stroke);
            NanoVG.nvgLineCap((long)this.ctx, (int)1);
            NanoVG.nvgLineJoin((long)this.ctx, (int)1);
            NanoVG.nvgStroke((long)this.ctx);
        }
    }

    public void triangle(float x0, float y0, float x1, float y1, float x2, float y2, int argb) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            NanoVG.nvgBeginPath((long)this.ctx);
            NanoVG.nvgMoveTo((long)this.ctx, (float)x0, (float)y0);
            NanoVG.nvgLineTo((long)this.ctx, (float)x1, (float)y1);
            NanoVG.nvgLineTo((long)this.ctx, (float)x2, (float)y2);
            NanoVG.nvgClosePath((long)this.ctx);
            NanoVG.nvgFillColor((long)this.ctx, (NVGColor)NVGRenderer.color(stack, argb));
            NanoVG.nvgFill((long)this.ctx);
        }
    }

    public void rectOutline(float x, float y, float w, float h, float radius, float stroke, int argb) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            NanoVG.nvgBeginPath((long)this.ctx);
            NanoVG.nvgRoundedRect((long)this.ctx, (float)(x + stroke / 2.0f), (float)(y + stroke / 2.0f), (float)(w - stroke), (float)(h - stroke), (float)radius);
            NanoVG.nvgStrokeColor((long)this.ctx, (NVGColor)NVGRenderer.color(stack, argb));
            NanoVG.nvgStrokeWidth((long)this.ctx, (float)stroke);
            NanoVG.nvgStroke((long)this.ctx);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void rectVaryingOutline(float x, float y, float w, float h, float rtl, float rtr, float rbr, float rbl, float stroke, int argb) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            NanoVG.nvgBeginPath((long)this.ctx);
            NanoVG.nvgRoundedRectVarying((long)this.ctx, (float)(x + stroke / 2.0f), (float)(y + stroke / 2.0f), (float)(w - stroke), (float)(h - stroke), (float)rtl, (float)rtr, (float)rbr, (float)rbl);
            NanoVG.nvgStrokeColor((long)this.ctx, (NVGColor)NVGRenderer.color(stack, argb));
            NanoVG.nvgStrokeWidth((long)this.ctx, (float)stroke);
            NanoVG.nvgStroke((long)this.ctx);
        }
    }

    public void glow(float x, float y, float w, float h, float radius, float spread, int argb) {
        if ((argb >>> 24) == 0 || spread <= 0.1f) return;
        try (MemoryStack stack = MemoryStack.stackPush();){
            NVGPaint paint = NVGPaint.malloc((MemoryStack)stack);
            NanoVG.nvgBoxGradient((long)this.ctx, (float)x, (float)y, (float)w, (float)h, (float)radius, (float)(spread * 2.0f), (NVGColor)NVGRenderer.color(stack, argb), (NVGColor)NVGRenderer.color(stack, Colors.withAlpha(argb, 0)), (NVGPaint)paint);
            NanoVG.nvgBeginPath((long)this.ctx);
            NanoVG.nvgRoundedRect((long)this.ctx, (float)(x - spread), (float)(y - spread), (float)(w + spread * 2.0f), (float)(h + spread * 2.0f), (float)(radius + spread));
            NanoVG.nvgFillPaint((long)this.ctx, (NVGPaint)paint);
            NanoVG.nvgFill((long)this.ctx);
        }
    }

    public void circle(float cx, float cy, float r, int argb) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            NanoVG.nvgBeginPath((long)this.ctx);
            NanoVG.nvgCircle((long)this.ctx, (float)cx, (float)cy, (float)r);
            NanoVG.nvgFillColor((long)this.ctx, (NVGColor)NVGRenderer.color(stack, argb));
            NanoVG.nvgFill((long)this.ctx);
        }
    }

    public void circleGlow(float cx, float cy, float r, float spread, int argb) {
        if ((argb >>> 24) == 0 || spread <= 0.1f) return;
        try (MemoryStack stack = MemoryStack.stackPush();){
            NVGPaint paint = NVGPaint.malloc((MemoryStack)stack);
            NanoVG.nvgRadialGradient((long)this.ctx, (float)cx, (float)cy, (float)(r * 0.25f), (float)(r + spread), (NVGColor)NVGRenderer.color(stack, argb), (NVGColor)NVGRenderer.color(stack, Colors.withAlpha(argb, 0)), (NVGPaint)paint);
            NanoVG.nvgBeginPath((long)this.ctx);
            NanoVG.nvgCircle((long)this.ctx, (float)cx, (float)cy, (float)(r + spread));
            NanoVG.nvgFillPaint((long)this.ctx, (NVGPaint)paint);
            NanoVG.nvgFill((long)this.ctx);
        }
    }

    public void line(float x1, float y1, float x2, float y2, float width, int argb) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            NanoVG.nvgBeginPath((long)this.ctx);
            NanoVG.nvgMoveTo((long)this.ctx, (float)x1, (float)y1);
            NanoVG.nvgLineTo((long)this.ctx, (float)x2, (float)y2);
            NanoVG.nvgStrokeColor((long)this.ctx, (NVGColor)NVGRenderer.color(stack, argb));
            NanoVG.nvgStrokeWidth((long)this.ctx, (float)width);
            NanoVG.nvgLineCap((long)this.ctx, (int)1);
            NanoVG.nvgStroke((long)this.ctx);
        }
    }

    public void checkmark(float x, float y, float size, float stroke, int argb) {
        float x1 = x + size * 0.22f;
        float y1 = y + size * 0.55f;
        float x2 = x + size * 0.42f;
        float y2 = y + size * 0.74f;
        float x3 = x + size * 0.78f;
        float y3 = y + size * 0.3f;
        try (MemoryStack stack = MemoryStack.stackPush();){
            NanoVG.nvgBeginPath((long)this.ctx);
            NanoVG.nvgMoveTo((long)this.ctx, (float)x1, (float)y1);
            NanoVG.nvgLineTo((long)this.ctx, (float)x2, (float)y2);
            NanoVG.nvgLineTo((long)this.ctx, (float)x3, (float)y3);
            NanoVG.nvgStrokeColor((long)this.ctx, (NVGColor)NVGRenderer.color(stack, argb));
            NanoVG.nvgStrokeWidth((long)this.ctx, (float)stroke);
            NanoVG.nvgLineCap((long)this.ctx, (int)1);
            NanoVG.nvgLineJoin((long)this.ctx, (int)1);
            NanoVG.nvgStroke((long)this.ctx);
        }
    }

    public void cross(float x, float y, float size, float stroke, int argb) {
        float pad = size * 0.3f;
        this.line(x + pad, y + pad, x + size - pad, y + size - pad, stroke, argb);
        this.line(x + size - pad, y + pad, x + pad, y + size - pad, stroke, argb);
    }

    public float text(String str, float x, float y, float size, int argb) {
        return this.text(str, x, y, size, argb, this.activeFont);
    }

    public float text(String str, float x, float y, float size, int argb, String font) {
        float var8;
        if (font == null) {
            return 0.0f;
        }
        try (MemoryStack stack = MemoryStack.stackPush();){
            NanoVG.nvgFontFace((long)this.ctx, (CharSequence)font);
            NanoVG.nvgFontSize((long)this.ctx, (float)size);
            NanoVG.nvgTextAlign((long)this.ctx, (int)17);
            NanoVG.nvgFillColor((long)this.ctx, (NVGColor)NVGRenderer.color(stack, argb));
            var8 = NanoVG.nvgText((long)this.ctx, (float)x, (float)y, (CharSequence)str) - x;
        }
        return var8;
    }

    public float textGradient(String str, float x, float y, float size, int top, int bottom) {
        float var9;
        if (this.activeFont == null) {
            return 0.0f;
        }
        try (MemoryStack stack = MemoryStack.stackPush();){
            NVGPaint paint = NVGPaint.malloc((MemoryStack)stack);
            NanoVG.nvgLinearGradient((long)this.ctx, (float)x, (float)(y - size / 2.0f), (float)x, (float)(y + size / 2.0f), (NVGColor)NVGRenderer.color(stack, top), (NVGColor)NVGRenderer.color(stack, bottom), (NVGPaint)paint);
            NanoVG.nvgFontFace((long)this.ctx, (CharSequence)this.activeFont);
            NanoVG.nvgFontSize((long)this.ctx, (float)size);
            NanoVG.nvgTextAlign((long)this.ctx, (int)17);
            NanoVG.nvgFillPaint((long)this.ctx, (NVGPaint)paint);
            var9 = NanoVG.nvgText((long)this.ctx, (float)x, (float)y, (CharSequence)str) - x;
        }
        return var9;
    }

    public void textGlow(String str, float x, float y, float size, int argb) {
    }

    public float textTruncated(String str, float x, float y, float size, int argb, float maxWidth) {
        if (this.textWidth(str, size) <= maxWidth) {
            return this.text(str, x, y, size, argb);
        }
        String cut = str;
        while (cut.length() > 1 && this.textWidth(cut + "\u2026", size) > maxWidth) {
            cut = cut.substring(0, cut.length() - 1);
        }
        return this.text(cut + "\u2026", x, y, size, argb);
    }

    public float textWidth(String str, float size) {
        if (this.activeFont == null) {
            return 0.0f;
        }
        NanoVG.nvgFontFace((long)this.ctx, (CharSequence)this.activeFont);
        NanoVG.nvgFontSize((long)this.ctx, (float)size);
        NanoVG.nvgTextAlign((long)this.ctx, (int)17);
        return NanoVG.nvgTextBounds((long)this.ctx, (float)0.0f, (float)0.0f, (CharSequence)str, (FloatBuffer)null);
    }

    public void rotate(float radians) {
        NanoVG.nvgRotate((long)this.ctx, (float)radians);
    }

    public void imagePattern(int image, float patternX, float patternY, float patternW, float patternH, float x, float y, float w, float h, float alphaMul) {
        if (image > 0) {
            try (MemoryStack stack = MemoryStack.stackPush();){
                NVGPaint paint = NVGPaint.malloc((MemoryStack)stack);
                NanoVG.nvgImagePattern((long)this.ctx, (float)patternX, (float)patternY, (float)patternW, (float)patternH, (float)0.0f, (int)image, (float)alphaMul, (NVGPaint)paint);
                NanoVG.nvgBeginPath((long)this.ctx);
                NanoVG.nvgRect((long)this.ctx, (float)x, (float)y, (float)w, (float)h);
                NanoVG.nvgFillPaint((long)this.ctx, (NVGPaint)paint);
                NanoVG.nvgFill((long)this.ctx);
            }
        }
    }

    public void circleOutline(float cx, float cy, float r, float stroke, int argb) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            NanoVG.nvgBeginPath((long)this.ctx);
            NanoVG.nvgCircle((long)this.ctx, (float)cx, (float)cy, (float)r);
            NanoVG.nvgStrokeColor((long)this.ctx, (NVGColor)NVGRenderer.color(stack, argb));
            NanoVG.nvgStrokeWidth((long)this.ctx, (float)stroke);
            NanoVG.nvgStroke((long)this.ctx);
        }
    }

    public void image(int image, float x, float y, float w, float h, int tint) {
        if (image > 0) {
            try (MemoryStack stack = MemoryStack.stackPush();){
                NVGPaint paint = NVGPaint.malloc((MemoryStack)stack);
                NanoVG.nvgImagePattern((long)this.ctx, (float)x, (float)y, (float)w, (float)h, (float)0.0f, (int)image, (float)1.0f, (NVGPaint)paint);
                paint.innerColor(NVGRenderer.color(stack, tint));
                NanoVG.nvgBeginPath((long)this.ctx);
                NanoVG.nvgRect((long)this.ctx, (float)x, (float)y, (float)w, (float)h);
                NanoVG.nvgFillPaint((long)this.ctx, (NVGPaint)paint);
                NanoVG.nvgFill((long)this.ctx);
            }
        }
    }

    private static NVGColor color(MemoryStack stack, int argb) {
        return NanoVG.nvgRGBA((byte)((byte)Colors.red(argb)), (byte)((byte)Colors.green(argb)), (byte)((byte)Colors.blue(argb)), (byte)((byte)Colors.alpha(argb)), (NVGColor)NVGColor.malloc((MemoryStack)stack));
    }
}

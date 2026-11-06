/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL33C
 */
package Yuki.lumiclient.render.nanovg;

import org.lwjgl.opengl.GL33C;

public final class GlStateSnapshot {
    private int program;
    private int vao;
    private int arrayBuffer;
    private int uniformBuffer;
    private int uniformBufferBase0;
    private int activeTexture;
    private int texture2D;
    private int sampler0;
    private int blendSrcRgb;
    private int blendDstRgb;
    private int blendSrcAlpha;
    private int blendDstAlpha;
    private int blendEqRgb;
    private int blendEqAlpha;
    private int cullFaceMode;
    private int frontFace;
    private int depthFunc;
    private int stencilFunc;
    private int stencilRef;
    private int stencilValueMask;
    private int stencilWriteMask;
    private int stencilFail;
    private int stencilPassDepthFail;
    private int stencilPassDepthPass;
    private boolean blend;
    private boolean cullFace;
    private boolean depthTest;
    private boolean scissorTest;
    private boolean stencilTest;
    private boolean depthMask;
    private final int[] viewport = new int[4];
    private final int[] scissorBox = new int[4];
    private final boolean[] colorMask = new boolean[4];
    private int unpackAlignment;
    private int unpackRowLength;
    private int unpackSkipPixels;
    private int unpackSkipRows;

    private GlStateSnapshot() {
    }

    public static GlStateSnapshot capture() {
        GlStateSnapshot s = new GlStateSnapshot();
        s.program = GL33C.glGetInteger((int)35725);
        s.vao = GL33C.glGetInteger((int)34229);
        s.arrayBuffer = GL33C.glGetInteger((int)34964);
        s.uniformBuffer = GL33C.glGetInteger((int)35368);
        s.uniformBufferBase0 = GL33C.glGetIntegeri((int)35368, (int)0);
        s.activeTexture = GL33C.glGetInteger((int)34016);
        GL33C.glActiveTexture((int)33984);
        s.texture2D = GL33C.glGetInteger((int)32873);
        s.sampler0 = GL33C.glGetInteger((int)35097);
        GL33C.glBindSampler((int)0, (int)0);
        GL33C.glActiveTexture((int)s.activeTexture);
        s.blend = GL33C.glIsEnabled((int)3042);
        s.blendSrcRgb = GL33C.glGetInteger((int)32969);
        s.blendDstRgb = GL33C.glGetInteger((int)32968);
        s.blendSrcAlpha = GL33C.glGetInteger((int)32971);
        s.blendDstAlpha = GL33C.glGetInteger((int)32970);
        s.blendEqRgb = GL33C.glGetInteger((int)32777);
        s.blendEqAlpha = GL33C.glGetInteger((int)34877);
        s.cullFace = GL33C.glIsEnabled((int)2884);
        s.cullFaceMode = GL33C.glGetInteger((int)2885);
        s.frontFace = GL33C.glGetInteger((int)2886);
        s.depthTest = GL33C.glIsEnabled((int)2929);
        s.depthFunc = GL33C.glGetInteger((int)2932);
        s.depthMask = GL33C.glGetBoolean((int)2930);
        s.scissorTest = GL33C.glIsEnabled((int)3089);
        s.stencilTest = GL33C.glIsEnabled((int)2960);
        s.stencilFunc = GL33C.glGetInteger((int)2962);
        s.stencilRef = GL33C.glGetInteger((int)2967);
        s.stencilValueMask = GL33C.glGetInteger((int)2963);
        s.stencilWriteMask = GL33C.glGetInteger((int)2968);
        s.stencilFail = GL33C.glGetInteger((int)2964);
        s.stencilPassDepthFail = GL33C.glGetInteger((int)2965);
        s.stencilPassDepthPass = GL33C.glGetInteger((int)2966);
        GL33C.glGetIntegerv((int)2978, (int[])s.viewport);
        GL33C.glGetIntegerv((int)3088, (int[])s.scissorBox);
        int[] mask = new int[4];
        GL33C.glGetIntegerv((int)3107, (int[])mask);
        for (int i = 0; i < 4; ++i) {
            s.colorMask[i] = mask[i] != 0;
        }
        s.unpackAlignment = GL33C.glGetInteger((int)3317);
        s.unpackRowLength = GL33C.glGetInteger((int)3314);
        s.unpackSkipPixels = GL33C.glGetInteger((int)3316);
        s.unpackSkipRows = GL33C.glGetInteger((int)3315);
        return s;
    }

    public void restore() {
        GL33C.glUseProgram((int)this.program);
        GL33C.glBindVertexArray((int)this.vao);
        GL33C.glBindBuffer((int)34962, (int)this.arrayBuffer);
        GL33C.glBindBufferBase((int)35345, (int)0, (int)this.uniformBufferBase0);
        GL33C.glBindBuffer((int)35345, (int)this.uniformBuffer);
        GL33C.glActiveTexture((int)33984);
        GL33C.glBindTexture((int)3553, (int)this.texture2D);
        GL33C.glBindSampler((int)0, (int)this.sampler0);
        GL33C.glActiveTexture((int)this.activeTexture);
        GlStateSnapshot.setEnabled(3042, this.blend);
        GL33C.glBlendFuncSeparate((int)this.blendSrcRgb, (int)this.blendDstRgb, (int)this.blendSrcAlpha, (int)this.blendDstAlpha);
        GL33C.glBlendEquationSeparate((int)this.blendEqRgb, (int)this.blendEqAlpha);
        GlStateSnapshot.setEnabled(2884, this.cullFace);
        GL33C.glCullFace((int)this.cullFaceMode);
        GL33C.glFrontFace((int)this.frontFace);
        GlStateSnapshot.setEnabled(2929, this.depthTest);
        GL33C.glDepthFunc((int)this.depthFunc);
        GL33C.glDepthMask((boolean)this.depthMask);
        GlStateSnapshot.setEnabled(3089, this.scissorTest);
        GlStateSnapshot.setEnabled(2960, this.stencilTest);
        GL33C.glStencilFunc((int)this.stencilFunc, (int)this.stencilRef, (int)this.stencilValueMask);
        GL33C.glStencilMask((int)this.stencilWriteMask);
        GL33C.glStencilOp((int)this.stencilFail, (int)this.stencilPassDepthFail, (int)this.stencilPassDepthPass);
        GL33C.glViewport((int)this.viewport[0], (int)this.viewport[1], (int)this.viewport[2], (int)this.viewport[3]);
        GL33C.glScissor((int)this.scissorBox[0], (int)this.scissorBox[1], (int)this.scissorBox[2], (int)this.scissorBox[3]);
        GL33C.glColorMask((boolean)this.colorMask[0], (boolean)this.colorMask[1], (boolean)this.colorMask[2], (boolean)this.colorMask[3]);
        GL33C.glPixelStorei((int)3317, (int)this.unpackAlignment);
        GL33C.glPixelStorei((int)3314, (int)this.unpackRowLength);
        GL33C.glPixelStorei((int)3316, (int)this.unpackSkipPixels);
        GL33C.glPixelStorei((int)3315, (int)this.unpackSkipRows);
    }

    private static void setEnabled(int cap, boolean enabled) {
        if (enabled) {
            GL33C.glEnable((int)cap);
        } else {
            GL33C.glDisable((int)cap);
        }
    }
}


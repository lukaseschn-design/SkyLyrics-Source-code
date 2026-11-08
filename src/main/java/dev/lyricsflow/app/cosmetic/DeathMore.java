/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_12249
 *  net.minecraft.class_1297
 *  net.minecraft.class_1299
 *  net.minecraft.class_1538
 *  net.minecraft.class_1937
 *  net.minecraft.class_243
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3414
 *  net.minecraft.class_3417
 *  net.minecraft.class_3419
 *  net.minecraft.class_3730
 *  net.minecraft.class_4587
 *  net.minecraft.class_4588
 *  net.minecraft.class_4597
 *  net.minecraft.class_4608
 *  net.minecraft.class_9944
 *  org.joml.Matrix3f
 *  org.joml.Matrix3fc
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package dev.lyricsflow.app.cosmetic;

import dev.lyricsflow.app.cosmetic.Accessories;
import dev.lyricsflow.app.cosmetic.DeathFx;
import dev.lyricsflow.app.cosmetic.DeathHard;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.class_12249;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1538;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_3730;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_9944;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

final class DeathMore {
    static final int FIRST = 4;
    static final float[] END = new float[]{5.6f, 6.2f, 5.4f};
    static final int FULL = 0xF000F0;
    static final int[] BLOOD = new int[]{9044739, 10815496, 7209474, 11866642, 0x5A0000};
    static final int[] ASH = new int[]{2762790, 0x3A3633, 4933187, 6182995, 1973275};
    static final int[][] PT = new int[][]{{8, 8, 8, 0, 0, 32, 0}, {8, 12, 4, 16, 16, 16, 32}, {4, 12, 4, 40, 16, 40, 32}, {4, 12, 4, 32, 48, 48, 48}, {4, 12, 4, 0, 16, 0, 32}, {4, 12, 4, 16, 48, 0, 48}};
    static final float[] INFL = new float[]{0.5f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f};
    static final int HEAD = 0;
    static final int BODY = 1;
    static final int RARM = 2;
    static final int LARM = 3;
    static final int RLEG = 4;
    static final int LLEG = 5;
    static final float PX = 0.05859375f;
    static final float[][] STAND = new float[][]{{0.0f, 1.64f, 0.0f}, {0.0f, 1.055f, 0.0f}, {-0.3516f, 1.055f, 0.0f}, {0.3516f, 1.055f, 0.0f}, {-0.1172f, 0.3516f, 0.0f}, {0.1172f, 0.3516f, 0.0f}};
    static final int SKIN = 14920066;
    static final int SKIN_D = 13209194;
    static final int SKIN_L = 15778724;
    static final int KNUCK = 14260344;
    static final int NAIL_C = 16047311;
    static final int NAIL_TIP = 0xFFF4EE;
    static final int VEIN = 12095615;
    static final int SLEEVE = 2828339;
    static final int CUFF = 1841698;
    static final class_2960 CHEST_TEX = class_2960.method_60656((String)"textures/entity/chest/normal.png");
    private static class_9944 chestModel;

    private DeathMore() {
    }

    static float end(int kind) {
        return END[Math.max(0, Math.min(END.length - 1, kind - 4))];
    }

    static Object create(DeathFx.Fx f, Random r) {
        return new S(f.kind, r);
    }

    static void draw(class_310 mc, class_4597 buffers, Matrix4f base, class_243 cam, DeathFx.Fx f, float t) {
        float dt;
        S s;
        block9: {
            block8: {
                Object object = f.extra;
                if (!(object instanceof S)) break block8;
                s = (S)object;
                if (Accessories.whiteTex()) break block9;
            }
            return;
        }
        C c = new C();
        c.mc = mc;
        c.buf = buffers;
        c.f = f;
        c.s = s;
        c.t = t;
        c.m = new Matrix4f((Matrix4fc)base).translate((float)(f.ox - cam.field_1352), (float)(f.oy - cam.field_1351), (float)(f.oz - cam.field_1350)).rotateY(-f.yaw);
        c.light = DeathFx.light(mc, f.ox, f.oy + 1.0, f.oz);
        c.fp = f.self && mc.field_1690.method_31044().method_31034();
        float f2 = dt = s.lastT < 0.0f ? 0.0f : t - s.lastT;
        if (dt < 0.0f || dt > 0.25f) {
            dt = 0.0f;
        }
        s.lastT = t;
        c.dt = dt;
        switch (f.kind) {
            case 4: {
                DeathMore.ash(c);
                break;
            }
            case 5: {
                DeathMore.hand(c);
                break;
            }
            default: {
                DeathMore.chest(c);
            }
        }
        DeathMore.particles(c, DeathMore.end(f.kind) - 1.5f);
    }

    static void ash(C c) {
        float heap;
        S s = c.s;
        float t = c.t;
        if (s.once(0, true)) {
            try {
                class_1538 lb = (class_1538)class_1299.field_6112.method_5883((class_1937)c.mc.field_1687, class_3730.field_16461);
                if (lb != null) {
                    lb.method_5838(-1000000 - s.r.nextInt(1000000));
                    lb.method_29498(true);
                    lb.method_5814(c.f.ox, c.f.oy, c.f.oz);
                    c.mc.field_1687.method_53875((class_1297)lb);
                }
            }
            catch (Throwable lb) {
                // empty catch block
            }
            c.sound(class_3417.field_14865, 3.0f, 0.9f);
            c.sound(class_3417.field_14956, 2.0f, 0.8f);
            c.sound((class_3414)class_3417.field_15152.comp_349(), 1.0f, 1.2f);
            for (int i = 0; i < 300; ++i) {
                int part = DeathMore.pickPart(s.r);
                P p = DeathMore.inPart(s.r, part);
                p.s = 0.035f + s.r.nextFloat() * 0.05f;
                p.col = ASH[s.r.nextInt(ASH.length)];
                p.rel = 1.15f + (2.0f - p.y) / 2.1f * 1.4f + s.r.nextFloat() * 0.12f;
                p.vx = (s.r.nextFloat() - 0.5f) * 0.9f + 0.25f;
                p.vy = 0.2f + s.r.nextFloat() * 0.7f;
                p.vz = (s.r.nextFloat() - 0.5f) * 0.9f;
                p.grav = 3.2f;
                p.drag = 2.2f;
                p.glow = s.r.nextFloat() < 0.35f ? 1.0f : 0.0f;
                s.ps.add(p);
            }
        }
        if (s.once(5, t > 0.35f)) {
            c.sound(class_3417.field_14956, 2.0f, 1.1f);
        }
        if (s.once(1, t > 1.15f)) {
            c.sound(class_3417.field_15102, 1.0f, 0.6f);
        }
        if (s.once(2, t > 1.6f)) {
            c.sound(class_3417.field_15074, 1.0f, 0.6f);
            c.sound(class_3417.field_21949, 1.0f, 0.5f);
        }
        float fade = 1.0f - DeathFx.sm(DeathMore.end(4) - 1.5f, DeathMore.end(4) - 0.2f, t);
        DeathFx.disc(c.glow(), c.m, 0.0f, 0.012f, 0.0f, 0.0f, 1.05f, 1314828, 0.8f * DeathFx.sm(0.0f, 0.3f, t) * fade, 1314828, 0.0f, 26);
        float flash = (float)Math.exp(-t * 5.0f);
        if (flash > 0.02f) {
            DeathFx.disc(c.glow(), c.m, 0.0f, 0.02f, 0.0f, 0.0f, 2.4f, 14543103, 0.85f * flash, 10467583, 0.0f, 28);
        }
        if ((heap = DeathFx.sm(1.6f, 2.8f, t) * fade) > 0.01f) {
            DeathFx.disc(c.glow(), c.m, 0.05f, 0.018f, 0.0f, 0.0f, 0.55f, 2762533, 0.9f * heap, 2762533, 0.0f, 22);
        }
        if (t < 0.85f && (t < 0.16f || t > 0.22f && t < 0.4f || t > 0.5f && t < 0.62f || t > 0.7f && t < 0.78f)) {
            if (s.bolt == null || t - s.boltAt > 0.06f) {
                s.bolt = DeathMore.bolt(s.r);
                s.boltAt = t;
            }
            DeathMore.drawBolt(c, s.bolt);
        }
        if (!c.fp && t < 2.75f) {
            float shock = DeathFx.sm(0.0f, 0.08f, t) * (1.0f - DeathFx.sm(0.9f, 1.2f, t));
            float jit = (float)Math.sin(t * 90.0f) * 0.06f * shock;
            float burn = DeathFx.sm(0.05f, 1.0f, t);
            int tint = DeathFx.mix(0xFFFFFF, 3025447, burn);
            float front = 2.05f - DeathFx.sm(1.15f, 2.55f, t) * 2.15f;
            Fig g = new Fig();
            g.armRZ = 0.45f * shock + jit;
            g.armLZ = 0.45f * shock - jit;
            g.headX = -0.35f * shock;
            class_4588 v = c.skin();
            Matrix4f[] pm = g.mats(new Matrix4f((Matrix4fc)c.m).translate(0.0f, 0.703f, 0.0f));
            for (int i = 0; i < 6; ++i) {
                float cy = STAND[i][1];
                float h = (float)PT[i][1] * 0.05859375f;
                float k = DeathFx.cl((front - (cy - h / 2.0f)) / h);
                if (k <= 0.01f) continue;
                Matrix4f mm = new Matrix4f((Matrix4fc)pm[i]).translate(0.0f, -(1.0f - k) * h / 2.0f, 0.0f).scale(1.0f, k, 1.0f);
                DeathMore.part(v, mm, i, c.skinTint(tint), c.light);
            }
            if (burn > 0.3f) {
                class_4588 w = c.white();
                for (int i = 0; i < 14; ++i) {
                    float hy = 0.15f + DeathFx.hashF(i * 7 + 1) * 1.7f;
                    if (hy > front) continue;
                    float hx = (DeathFx.hashF(i * 7 + 2) - 0.5f) * 0.42f;
                    float pulse = 0.6f + 0.4f * (float)Math.sin(t * 9.0f + (float)i);
                    int col = 0xFF000000 | DeathFx.mix(16734740, 16760896, pulse);
                    DeathFx.cube(w, c.m, hx - 0.012f, hy - 0.06f, 0.118f, hx + 0.012f, hy + 0.06f, 0.128f, col, 0xF000F0);
                }
            }
        }
    }

    static float[][] bolt(Random r) {
        int n = 16;
        float[][] p = new float[n + 1][3];
        float x = (r.nextFloat() - 0.5f) * 6.0f;
        float z = (r.nextFloat() - 0.5f) * 6.0f;
        for (int i = 0; i <= n; ++i) {
            float k = (float)i / (float)n;
            float jx = i == n ? 0.0f : (r.nextFloat() - 0.5f) * 1.3f * (1.0f - k * 0.6f);
            float jz = i == n ? 0.0f : (r.nextFloat() - 0.5f) * 1.3f * (1.0f - k * 0.6f);
            p[i][0] = x * (1.0f - k) + jx;
            p[i][1] = 34.0f * (1.0f - k) + 1.2f * k;
            p[i][2] = z * (1.0f - k) + jz;
        }
        return p;
    }

    static void drawBolt(C c, float[][] p) {
        class_4588 g = c.glow();
        int i = 0;
        while (i + 1 < p.length) {
            DeathHard.gline(g, c.m, p[i][0], p[i][1], p[i][2], p[i + 1][0], p[i + 1][1], p[i + 1][2], 1.1f, 8363263, 0.25f);
            DeathHard.gline(g, c.m, p[i][0], p[i][1], p[i][2], p[i + 1][0], p[i + 1][1], p[i + 1][2], 0.32f, 14542591, 0.85f);
            DeathHard.gline(g, c.m, p[i][0], p[i][1], p[i][2], p[i + 1][0], p[i + 1][1], p[i + 1][2], 0.1f, 0xFFFFFF, 1.0f);
            ++i;
        }
        class_4588 v = c.buf.method_73477(class_12249.method_76003());
        int i2 = 0;
        while (i2 + 1 < p.length) {
            for (int layer = 0; layer < 3; ++layer) {
                float w = (new float[]{0.42f, 0.2f, 0.07f})[layer];
                int col = (new int[]{1077570815, 1887414527, -252380929})[layer];
                DeathMore.beam(v, c.m, p[i2], p[i2 + 1], w, col);
            }
            ++i2;
        }
    }

    static void beam(class_4588 v, Matrix4f m, float[] a, float[] b, float w, int argb) {
        float h = w / 2.0f;
        v.method_22918((Matrix4fc)m, a[0] - h, a[1], a[2]).method_39415(argb);
        v.method_22918((Matrix4fc)m, a[0] + h, a[1], a[2]).method_39415(argb);
        v.method_22918((Matrix4fc)m, b[0] + h, b[1], b[2]).method_39415(argb);
        v.method_22918((Matrix4fc)m, b[0] - h, b[1], b[2]).method_39415(argb);
        v.method_22918((Matrix4fc)m, a[0], a[1], a[2] - h).method_39415(argb);
        v.method_22918((Matrix4fc)m, a[0], a[1], a[2] + h).method_39415(argb);
        v.method_22918((Matrix4fc)m, b[0], b[1], b[2] + h).method_39415(argb);
        v.method_22918((Matrix4fc)m, b[0], b[1], b[2] - h).method_39415(argb);
    }

    static float handY(float t) {
        if (t < 1.8f) {
            return DeathFx.lerp(26.0f, 4.0f, DeathFx.eo(DeathFx.cl(t / 1.8f)));
        }
        if (t < 2.1f) {
            return DeathFx.lerp(4.0f, 4.9f, DeathFx.sm(1.8f, 2.1f, t));
        }
        if (t < 2.24f) {
            return DeathFx.lerp(4.9f, 0.12f, DeathFx.ei((t - 2.1f) / 0.14f));
        }
        if (t < 3.4f) {
            return 0.12f - 0.035f * DeathFx.sm(2.24f, 2.6f, t);
        }
        if (t < 4.4f) {
            return DeathFx.lerp(0.085f, 2.4f, DeathFx.sm(3.4f, 4.4f, t));
        }
        return 2.4f + 30.0f * DeathFx.ei(DeathFx.cl((t - 4.4f) / 1.3f));
    }

    static void hand(C c) {
        int tint;
        S s = c.s;
        float t = c.t;
        float hy = DeathMore.handY(t);
        boolean hit = t >= 2.24f;
        float press = hit ? 1.0f - DeathFx.sm(3.4f, 3.9f, t) : 0.0f;
        if (s.once(0, t > 0.05f)) {
            c.sound(class_3417.field_38831, 1.0f, 0.45f);
        }
        if (s.once(1, t > 1.8f)) {
            c.sound(class_3417.field_38068, 1.0f, 0.6f);
        }
        if (s.once(2, hit)) {
            c.sound(class_3417.field_14833, 1.0f, 0.45f);
            c.sound((class_3414)class_3417.field_15152.comp_349(), 0.7f, 1.5f);
            c.sound(class_3417.field_21872, 1.0f, 0.5f);
            c.sound(class_3417.field_15194, 1.0f, 0.5f);
            c.sound(class_3417.field_15115, 1.0f, 0.6f);
            for (int i = 0; i < 230; ++i) {
                double a = s.r.nextDouble() * Math.PI * 2.0;
                P p = DeathMore.blood(s, (float)Math.cos(a) * 0.25f, 0.12f, 0.2f + (float)Math.sin(a) * 0.25f);
                float sp = 3.0f + s.r.nextFloat() * 5.0f;
                p.vx = (float)Math.cos(a) * sp;
                p.vz = (float)Math.sin(a) * sp;
                p.vy = 0.6f + s.r.nextFloat() * 2.6f;
                p.s *= 1.15f;
                s.ps.add(p);
            }
            DeathMore.dirt(s, 0.0f, 0.2f, 46, 1.9f);
            DeathMore.pool(s, 0.0f, 0.2f, 1.75f, 2.25f);
        }
        if (hit && t < 3.4f && s.r.nextFloat() < c.dt * 14.0f) {
            double a = s.r.nextDouble() * Math.PI * 2.0;
            DeathMore.squirt(s, (float)Math.cos(a) * 0.85f, 0.1f, (float)Math.sin(a) * 0.85f, (float)Math.cos(a) * 1.4f, 0.0f, (float)Math.sin(a) * 1.4f, 5);
        }
        if (s.once(3, t > 3.4f)) {
            c.sound(class_3417.field_21070, 1.0f, 0.5f);
        }
        if (s.once(4, t > 4.4f)) {
            c.sound(class_3417.field_38831, 0.6f, 0.7f);
        }
        float fade = 1.0f - DeathFx.sm(DeathMore.end(5) - 1.2f, DeathMore.end(5) - 0.1f, t);
        class_4588 g = c.glow();
        float near = DeathFx.cl(1.0f - hy / 22.0f);
        DeathFx.disc(g, c.m, 0.0f, 0.014f, 0.2f, 0.0f, 1.0f + 1.3f * near, 0, 0.6f * near * near, 0, 0.0f, 28);
        if (hit) {
            float k = DeathFx.eo(DeathFx.cl((t - 2.24f) / 0.25f)) * fade;
            DeathFx.disc(g, c.m, 0.0f, 0.013f, 0.2f, 0.0f, 2.2f * k, 1708556, 0.55f * k, 1708556, 0.0f, 30);
        }
        if (!c.fp) {
            Fig fg = new Fig();
            float sq = DeathFx.cl((hy - 0.02f) / 1.95f);
            float fear = DeathFx.sm(0.4f, 1.2f, t);
            fg.headX = -0.9f * fear * (sq > 0.9f ? 1.0f : sq);
            float push = DeathFx.sm(1.4f, 1.9f, t);
            fg.armLX = fg.armRX = DeathFx.lerp(-0.6f * fear, -3.0f, push);
            fg.armLZ = fg.armRZ = 0.25f * (1.0f - push);
            float shake = (float)Math.sin(t * 40.0f) * 0.04f * push * (hit ? 0.0f : 1.0f);
            fg.armRX += shake;
            fg.armLX -= shake;
            float back = -0.25f * DeathFx.sm(0.6f, 1.4f, t);
            float sy = hit ? 0.07f : Math.min(1.0f, sq);
            float sp = 1.0f + (1.0f - sy) * 0.95f;
            Matrix4f feet = new Matrix4f((Matrix4fc)c.m).translate(0.0f, 0.0f, back).scale(sp, Math.max(0.06f, sy), sp);
            tint = c.skinTint(DeathFx.mix(0xFFFFFF, 0xC03030, hit ? 0.55f : (1.0f - sy) * 0.6f));
            fg.draw(c.skin(), new Matrix4f((Matrix4fc)feet).translate(0.0f, 0.703f, 0.0f), tint, c.light);
        }
        if (hy < 31.0f) {
            float spread;
            float tremble = hit ? press * 0.018f * (float)Math.sin(t * 57.0f) : 0.0f;
            float grind = hit ? press * 0.06f * (float)Math.sin(t * 9.0f) : 0.0f;
            float f = spread = t < 2.1f ? DeathFx.sm(1.8f, 2.1f, t) : 1.0f - 0.6f * press;
            float curl = t < 2.1f ? 0.55f - 0.4f * DeathFx.sm(1.8f, 2.1f, t) : (hit ? 0.12f + 0.25f * press : 0.15f);
            int hl = DeathFx.light(c.mc, c.f.ox, c.f.oy + 1.0 + (double)Math.min(hy, 6.0f), c.f.oz);
            float K = 7.0f;
            float len = 12.0f * K * 0.05859375f;
            Matrix4f am = new Matrix4f((Matrix4fc)c.m).translate(tremble, hy + len / 2.0f, 0.0f).rotateY(grind).scale(K);
            class_4588 sk = c.skin();
            tint = c.skinTint(0xFFFFFF);
            DeathFx.skinBox(sk, am, 4.0f, 12.0f, 4.0f, 0.0f, 40, 16, tint, hl);
            DeathFx.skinBox(sk, am, 4.0f, 12.0f, 4.0f, 0.25f, 40, 32, tint, hl);
            if (hit) {
                class_4588 w = c.white();
                float hw = 2.0f * K * 0.05859375f;
                for (int i = 0; i < 12; ++i) {
                    float bx = (DeathFx.hashF(i * 5 + 1) - 0.5f) * hw * 1.6f;
                    float bz = (DeathFx.hashF(i * 5 + 2) - 0.5f) * hw * 1.6f;
                    float r = 0.07f + DeathFx.hashF(i * 5 + 3) * 0.16f;
                    DeathFx.cube(w, c.m, tremble + bx - r, hy - 0.035f, bz - r, tremble + bx + r, hy - 0.015f, bz + r, 0xFF000000 | BLOOD[i % BLOOD.length], hl);
                    if (i >= 6) continue;
                    float sy = hy + DeathFx.hashF(i * 7 + 4) * 0.8f;
                    DeathFx.cube(w, c.m, tremble + hw + 0.005f, sy, bz - r * 0.5f, tremble + hw + 0.02f, sy + r * 1.5f, bz + r * 0.5f, 0xFF000000 | BLOOD[i % BLOOD.length], hl);
                }
            }
        }
    }

    static void drawHand(class_4588 w, Matrix4f m, float spread, float curl, float t, int li, boolean bloody) {
        int sk = -1857150;
        int sd = -3568022;
        int sl = -998492;
        int kn = -2516872;
        DeathFx.cube(w, m, -0.85f, 0.03f, -0.92f, 0.85f, 0.4f, 0.9f, sk, li);
        DeathFx.cube(w, m, -0.8f, 0.0f, -0.96f, 0.8f, 0.43f, 0.94f, sk, li);
        DeathFx.cube(w, m, -0.96f, -0.03f, -0.86f, -0.44f, 0.3f, 0.12f, sl, li);
        DeathFx.cube(w, m, 0.46f, -0.025f, -0.82f, 0.88f, 0.26f, 0.34f, sl, li);
        DeathFx.cube(w, m, -0.7f, -0.02f, 0.5f, 0.74f, 0.12f, 0.92f, sl, li);
        float[] fx = new float[]{-0.58f, -0.2f, 0.2f, 0.58f};
        for (int i = 0; i < 4; ++i) {
            DeathFx.cube(w, m, fx[i] - 0.05f, 0.42f, -0.7f + (float)i * 0.05f, fx[i] + 0.05f, 0.455f, 0.86f, sd, li);
        }
        int vc = -4681601;
        DeathFx.cube(w, m, -0.36f, 0.43f, -0.9f, -0.31f, 0.448f, -0.2f, vc, li);
        DeathFx.cube(w, m, -0.31f, 0.43f, -0.25f, -0.08f, 0.448f, -0.2f, vc, li);
        DeathFx.cube(w, m, 0.3f, 0.43f, -0.9f, 0.35f, 0.448f, -0.1f, vc, li);
        DeathFx.cube(w, m, -0.66f, 0.06f, -1.42f, 0.66f, 0.48f, -0.85f, sk, li);
        DeathFx.cube(w, m, -0.72f, 0.12f, -1.12f, -0.6f, 0.36f, -0.94f, sd, li);
        DeathFx.cube(w, m, -0.62f, 0.3f, -1.48f, 0.62f, 2.3f, -0.78f, sk, li);
        DeathFx.cube(w, m, -0.78f, 2.2f, -1.64f, 0.78f, 2.55f, -0.62f, -14935518, li);
        DeathFx.cube(w, m, -0.74f, 2.5f, -1.6f, 0.74f, 42.0f, -0.66f, -13948877, li);
        float[] len = new float[]{0.92f, 1.0f, 0.95f, 0.78f};
        float[] wid = new float[]{0.33f, 0.35f, 0.32f, 0.27f};
        for (int i = 0; i < 4; ++i) {
            float twitch = 0.04f * (float)Math.sin(t * 2.3f + (float)i * 1.7f);
            Matrix4f fm = new Matrix4f((Matrix4fc)m).translate(fx[i], 0.2f, 0.9f).rotateY(((float)i - 1.5f) * 0.1f * spread).rotateX(curl * 0.4f + twitch);
            float[] seg = new float[]{0.6f * len[i], 0.42f * len[i], 0.33f * len[i]};
            float fw = wid[i];
            float fh = 0.32f;
            for (int j = 0; j < 3; ++j) {
                float hw = fw / 2.0f;
                float hh = fh / 2.0f;
                DeathFx.cube(w, fm, -hw * 1.07f, -hh * 1.05f, -0.04f, hw * 1.07f, hh * 1.1f, 0.07f, kn, li);
                DeathFx.cube(w, fm, -hw, -hh, 0.0f, hw, hh, seg[j], sk, li);
                DeathFx.cube(w, fm, -hw * 0.8f, -hh - 0.008f, seg[j] - 0.03f, hw * 0.8f, -hh, seg[j], sd, li);
                if (j == 2) {
                    DeathFx.cube(w, fm, -hw * 0.72f, hh - 0.01f, seg[j] * 0.42f, hw * 0.72f, hh + 0.025f, seg[j] + 0.012f, -729905, li);
                    DeathFx.cube(w, fm, -hw * 0.66f, hh, seg[j] - 0.01f, hw * 0.66f, hh + 0.027f, seg[j] + 0.03f, -2834, li);
                    DeathFx.cube(w, fm, -hw * 0.82f, -hh * 0.82f, seg[j], hw * 0.82f, hh * 0.82f, seg[j] + 0.05f, sk, li);
                }
                fm.translate(0.0f, 0.0f, seg[j]).rotateX(curl * (j == 0 ? 0.55f : 0.45f) + twitch * 0.5f);
                fw *= 0.93f;
                fh *= 0.92f;
            }
        }
        Matrix4f tm = new Matrix4f((Matrix4fc)m).translate(-0.8f, 0.16f, -0.25f).rotateY(-0.85f + 0.25f * (1.0f - spread)).rotateX(curl * 0.3f + 0.12f);
        float[] tl = new float[]{0.48f, 0.38f, 0.32f};
        float tw = 0.42f;
        float th = 0.36f;
        for (int j = 0; j < 3; ++j) {
            float hw = tw / 2.0f;
            float hh = th / 2.0f;
            DeathFx.cube(w, tm, -hw * 1.06f, -hh * 1.05f, -0.04f, hw * 1.06f, hh * 1.08f, 0.07f, kn, li);
            DeathFx.cube(w, tm, -hw, -hh, 0.0f, hw, hh, tl[j], j == 0 ? sl : sk, li);
            if (j == 2) {
                DeathFx.cube(w, tm, -hw * 0.7f, hh - 0.01f, tl[j] * 0.4f, hw * 0.7f, hh + 0.025f, tl[j] + 0.012f, -729905, li);
                DeathFx.cube(w, tm, -hw * 0.8f, -hh * 0.8f, tl[j], hw * 0.8f, hh * 0.8f, tl[j] + 0.05f, sk, li);
            }
            tm.translate(0.0f, 0.0f, tl[j]).rotateX(curl * 0.35f).rotateY(0.25f);
            tw *= 0.92f;
            th *= 0.92f;
        }
        if (bloody) {
            for (int i = 0; i < 16; ++i) {
                float bx = (DeathFx.hashF(i * 5 + 1) - 0.5f) * 1.4f;
                float bz = (DeathFx.hashF(i * 5 + 2) - 0.5f) * 1.5f;
                float r = 0.08f + DeathFx.hashF(i * 5 + 3) * 0.2f;
                DeathFx.cube(w, m, bx - r, -0.04f, bz - r * 0.8f, bx + r, -0.02f, bz + r * 0.8f, 0xFF000000 | BLOOD[i % BLOOD.length], li);
            }
        }
    }

    static void chest(C c) {
        boolean cutOff;
        S s = c.s;
        float t = c.t;
        float rise = DeathFx.eo(DeathFx.cl(t / 0.6f));
        float sink = DeathFx.ei(DeathFx.cl((t - 3.0f) / 0.85f));
        float cy = -0.95f * (1.0f - rise) - 1.05f * sink;
        float cz = 1.3f;
        float open = t < 1.25f ? 0.0f : (t < 1.37f ? DeathFx.eo((t - 1.25f) / 0.12f) : (t < 2.1f ? 1.0f : (t < 2.18f ? 1.0f - DeathFx.ei((t - 2.1f) / 0.08f) : (t < 2.9f ? 0.18f * Math.max(0.0f, (float)Math.sin((t - 2.18f) * 9.0f)) * (1.0f - DeathFx.sm(2.5f, 2.9f, t)) : 0.0f))));
        boolean bl = cutOff = t >= 2.18f;
        if (s.once(0, true)) {
            c.sound(class_3417.field_28601, 1.0f, 0.8f);
            DeathMore.dirt(s, 0.0f, cz, 26, 0.9f);
        }
        if (s.once(1, t > 1.25f)) {
            c.sound(class_3417.field_14982, 1.0f, 0.7f);
            c.sound(class_3417.field_38831, 0.5f, 1.6f);
        }
        if (s.once(2, t > 1.6f)) {
            c.sound(class_3417.field_14879, 1.0f, 0.5f);
        }
        if (s.once(3, t > 2.1f)) {
            c.sound(class_3417.field_14823, 1.0f, 0.55f);
        }
        if (s.once(4, cutOff)) {
            c.sound(class_3417.field_14742, 0.6f, 1.2f);
            c.sound(class_3417.field_15194, 1.0f, 0.6f);
            c.sound(class_3417.field_15115, 1.0f, 0.7f);
            for (int i = 0; i < 2; ++i) {
                P p = new P();
                p.part = i == 0 ? 4 : 5;
                p.x = i == 0 ? -0.12f : 0.12f;
                p.y = 1.0f;
                p.z = cz;
                p.rx = (float)Math.PI;
                p.vx = (i == 0 ? -1.0f : 1.0f) * (0.8f + s.r.nextFloat() * 0.5f);
                p.vy = 3.0f + s.r.nextFloat() * 0.8f;
                p.vz = -1.6f - s.r.nextFloat() * 0.6f;
                p.wx = -6.0f + s.r.nextFloat() * 3.0f;
                p.wz = (i == 0 ? 1.0f : -1.0f) * (4.0f + s.r.nextFloat() * 4.0f);
                p.col = 0xFFFFFF;
                s.ps.add(p);
            }
            DeathMore.burst(s, 0.0f, 0.7f, cz - 0.3f, 110, 3.0f);
            DeathMore.pool(s, 0.0f, cz - 0.7f, 0.7f, 2.2f);
        }
        if (s.once(5, t > 3.0f)) {
            c.sound(class_3417.field_28601, 1.0f, 0.6f);
            DeathMore.dirt(s, 0.0f, cz, 30, 0.8f);
        }
        if (t > 2.2f && t < 3.0f && s.r.nextFloat() < c.dt * 30.0f) {
            DeathMore.squirt(s, (s.r.nextFloat() - 0.5f) * 0.8f, 0.62f + cy, cz - 0.45f, 0.0f, 0.0f, -0.5f, 2);
        }
        if (!c.fp && !cutOff) {
            float stride;
            Fig g = new Fig();
            float walk = DeathFx.sm(0.6f, 1.2f, t);
            float pz = 0.42f * walk;
            boolean walking = t > 0.6f && t < 1.2f;
            g.legRX = stride = walking ? (float)Math.sin(t * 11.0f) * 0.55f : 0.0f;
            g.legLX = -stride;
            float reach = DeathFx.sm(1.05f, 1.3f, t);
            g.armRX = -stride * 0.6f - 1.3f * reach;
            g.armLX = stride * 0.6f - 1.3f * reach;
            g.headX = 0.4f * DeathFx.sm(0.15f, 0.5f, t);
            float lean = DeathFx.sm(1.4f, 1.6f, t) * 0.35f;
            float pull = DeathFx.ei(DeathFx.cl((t - 1.6f) / 0.45f));
            float hyy = 0.703f;
            float hz = pz;
            float pitch = lean;
            if (pull > 0.0f) {
                float u = 1.0f - pull;
                float[] p0 = new float[]{0.703f, pz};
                float[] p1 = new float[]{1.7f, cz - 0.2f};
                float[] p2 = new float[]{0.62f + cy, cz};
                hyy = u * u * p0[0] + 2.0f * u * pull * p1[0] + pull * pull * p2[0];
                hz = u * u * p0[1] + 2.0f * u * pull * p1[1] + pull * pull * p2[1];
                pitch = lean + ((float)Math.PI - lean) * DeathFx.eo(pull);
                g.armLX = g.armRX = -2.9f * pull - 1.3f * (1.0f - pull);
                g.legRX = 0.35f * (float)Math.sin(t * 30.0f) * pull;
                g.legLX = -g.legRX;
            }
            Matrix4f hip = new Matrix4f((Matrix4fc)c.m).translate(0.0f, hyy, hz).rotateX(pitch);
            g.draw(c.skin(), hip, c.skinTint(0xFFFFFF), c.light);
        }
        if (sink < 0.999f) {
            float shake = t > 2.18f && t < 2.9f ? 0.05f * (float)Math.sin(t * 46.0f) * (1.0f - DeathFx.sm(2.2f, 2.9f, t)) : 0.0f;
            Matrix4f cm = new Matrix4f((Matrix4fc)c.m).translate(0.0f, cy, cz).rotateY((float)Math.PI + shake);
            int li = DeathFx.light(c.mc, c.f.ox, c.f.oy + 0.8, c.f.oz);
            try {
                if (chestModel == null) {
                    chestModel = new class_9944(class_9944.method_62070().method_32109());
                }
                chestModel.method_62069(Float.valueOf(open));
                class_4587 ps = new class_4587();
                ps.method_23760().method_23761().set((Matrix4fc)new Matrix4f((Matrix4fc)cm).translate(-0.5f, 0.0f, -0.5f));
                ps.method_23760().method_23762().set((Matrix3fc)new Matrix3f((Matrix4fc)cm).normal());
                chestModel.method_60879(ps, c.buf.method_73477(class_12249.method_75994((class_2960)CHEST_TEX)), li, class_4608.field_21444);
            }
            catch (Throwable e) {
                class_4588 w = c.white();
                DeathFx.cube(w, cm, -0.44f, 0.0f, -0.44f, 0.44f, 0.62f, 0.44f, -6658766, li);
            }
            if (open > 0.02f || t > 1.25f && t < 2.2f) {
                class_4588 w = c.white();
                DeathFx.cube(w, cm, -0.375f, 0.627f, -0.375f, 0.375f, 0.633f, 0.375f, -16777216, 0);
                DeathFx.disc(c.glow(), cm, 0.0f, 0.64f, 0.0f, 0.0f, 0.4f, 2755130, 0.5f * open, 0, 0.0f, 16);
            }
        }
    }

    static void part(class_4588 v, Matrix4f m, int i, int tint, int light) {
        int[] p = PT[i];
        DeathFx.skinBox(v, m, p[0], p[1], p[2], 0.0f, p[3], p[4], tint, light);
        DeathFx.skinBox(v, m, p[0], p[1], p[2], INFL[i], p[5], p[6], tint, light);
    }

    static int pickPart(Random r) {
        float k = r.nextFloat();
        return k < 0.2f ? 0 : (k < 0.5f ? 1 : (k < 0.62f ? 2 : (k < 0.74f ? 3 : (k < 0.87f ? 4 : 5))));
    }

    static P inPart(Random r, int i) {
        P p = new P();
        p.x = STAND[i][0] + (r.nextFloat() - 0.5f) * (float)PT[i][0] * 0.05859375f;
        p.y = STAND[i][1] + (r.nextFloat() - 0.5f) * (float)PT[i][1] * 0.05859375f;
        p.z = STAND[i][2] + (r.nextFloat() - 0.5f) * (float)PT[i][2] * 0.05859375f;
        p.rx = r.nextFloat() * 6.0f;
        p.ry = r.nextFloat() * 6.0f;
        p.wx = (r.nextFloat() - 0.5f) * 6.0f;
        p.wy = (r.nextFloat() - 0.5f) * 6.0f;
        p.seed = r.nextFloat();
        return p;
    }

    static P blood(S s, float x, float y, float z) {
        P p = new P();
        p.x = x;
        p.y = y;
        p.z = z;
        p.s = 0.03f + s.r.nextFloat() * 0.06f;
        p.col = BLOOD[s.r.nextInt(BLOOD.length)];
        p.blood = true;
        p.grav = 12.0f;
        p.rel = s.lastT;
        p.seed = s.r.nextFloat();
        p.wx = (s.r.nextFloat() - 0.5f) * 10.0f;
        p.wz = (s.r.nextFloat() - 0.5f) * 10.0f;
        return p;
    }

    static void burst(S s, float x, float y, float z, int n, float speed) {
        for (int i = 0; i < n; ++i) {
            P p = DeathMore.blood(s, x + (s.r.nextFloat() - 0.5f) * 0.3f, y + (s.r.nextFloat() - 0.5f) * 0.3f, z + (s.r.nextFloat() - 0.5f) * 0.3f);
            double a = s.r.nextDouble() * Math.PI * 2.0;
            float sp = speed * (0.3f + s.r.nextFloat());
            p.vx = (float)Math.cos(a) * sp;
            p.vz = (float)Math.sin(a) * sp;
            p.vy = 1.0f + s.r.nextFloat() * speed * 1.2f;
            s.ps.add(p);
        }
    }

    static void squirt(S s, float x, float y, float z, float dx, float dy, float dz, int n) {
        for (int i = 0; i < n; ++i) {
            P p = DeathMore.blood(s, x, y, z);
            p.vx = dx * (1.0f + s.r.nextFloat() * 2.0f) + (s.r.nextFloat() - 0.5f) * 1.2f;
            p.vy = dy + 0.8f + s.r.nextFloat() * 2.2f;
            p.vz = dz * (1.0f + s.r.nextFloat() * 2.0f) + (s.r.nextFloat() - 0.5f) * 1.2f;
            s.ps.add(p);
        }
    }

    static void dirt(S s, float x, float z, int n, float spread) {
        for (int i = 0; i < n; ++i) {
            P p = new P();
            double a = s.r.nextDouble() * Math.PI * 2.0;
            float rr = spread * (0.4f + s.r.nextFloat() * 0.6f);
            p.x = x + (float)Math.cos(a) * rr * 0.6f;
            p.z = z + (float)Math.sin(a) * rr * 0.6f;
            p.y = 0.05f;
            p.vx = (float)Math.cos(a) * (1.0f + s.r.nextFloat() * 1.5f);
            p.vz = (float)Math.sin(a) * (1.0f + s.r.nextFloat() * 1.5f);
            p.vy = 2.0f + s.r.nextFloat() * 2.5f;
            p.s = 0.06f + s.r.nextFloat() * 0.08f;
            p.col = s.r.nextBoolean() ? 7031342 : 8806460;
            p.wx = (s.r.nextFloat() - 0.5f) * 12.0f;
            p.wz = (s.r.nextFloat() - 0.5f) * 12.0f;
            p.rel = s.lastT;
            p.seed = s.r.nextFloat();
            s.ps.add(p);
        }
    }

    static void pool(S s, float x, float z, float rad, float at) {
        s.pools.add(new float[]{x, z, rad, at, s.r.nextInt(1000)});
    }

    static void particles(C c, float fadeAt) {
        Matrix4f pm;
        S s = c.s;
        float t = c.t;
        float dt = c.dt;
        if (!s.pools.isEmpty()) {
            class_4588 g = c.glow();
            float a = 1.0f - DeathFx.sm(fadeAt, fadeAt + 1.3f, t);
            for (float[] fArray : s.pools) {
                float k = DeathFx.eo(DeathFx.cl((t - fArray[3]) / 1.4f));
                DeathHard.poolDisc(g, c.m, fArray[0], fArray[1], fArray[2], k, a, (int)fArray[4]);
            }
        }
        if (s.ps.isEmpty()) {
            return;
        }
        class_4588 w = c.white();
        int extraPools = 0;
        for (P p : s.ps) {
            float h;
            float hot;
            float shrink;
            float sz;
            if (!p.alive || p.part >= 0 || t < p.rel) continue;
            DeathMore.step(p, dt);
            if (p.landed && p.blood && !p.splat) {
                p.splat = true;
                if (s.pools.size() < 26 && s.r.nextFloat() < 0.18f) {
                    DeathMore.pool(s, p.x, p.z, 0.08f + s.r.nextFloat() * 0.14f, t);
                    ++extraPools;
                }
            }
            if ((sz = p.s * (shrink = 1.0f - DeathFx.sm(fadeAt + p.seed * 0.7f, fadeAt + p.seed * 0.7f + 0.8f, t))) < 0.004f) continue;
            int col = p.col;
            int li = c.light;
            if (p.glow > 0.0f && (hot = 1.0f - DeathFx.sm(0.0f, 0.6f, t - p.rel)) > 0.0f) {
                col = DeathFx.mix(col, 16738842, hot);
                li = 0xF000F0;
            }
            pm = new Matrix4f((Matrix4fc)c.m).translate(p.x, p.y, p.z);
            if (p.landed && p.blood) {
                h = sz * 0.8f;
                DeathFx.cube(w, pm, -h, -p.y + 0.004f, -h, h, -p.y + 0.012f, h, 0xFF000000 | col, li);
                continue;
            }
            pm.rotateXYZ(p.rx, p.ry, p.rz);
            h = sz / 2.0f;
            DeathFx.cube(w, pm, -h, -h, -h, h, h, h, 0xFF000000 | col, li);
        }
        boolean any = false;
        for (P p : s.ps) {
            if (p.part < 0 || !p.alive) continue;
            any = true;
            break;
        }
        if (any && !c.fp) {
            class_4588 class_45882 = c.skin();
            for (P p : s.ps) {
                if (p.part < 0 || !p.alive) continue;
                float half = p.part == 0 ? 0.24f : 0.12f;
                DeathMore.stepPart(p, dt, half);
                float sink = DeathFx.sm(fadeAt + 0.2f, fadeAt + 1.4f, t) * 0.5f;
                pm = new Matrix4f((Matrix4fc)c.m).translate(p.x, p.y - sink, p.z).rotateXYZ(p.rx, p.ry, p.rz);
                DeathMore.part(class_45882, pm, p.part, c.skinTint(DeathFx.mix(p.col, 0xFFFFFF, 0.0f)), c.light);
            }
            class_4588 w2 = c.white();
            for (P p : s.ps) {
                if (p.part < 0 || !p.alive || p.part == 0) continue;
                float sink = DeathFx.sm(fadeAt + 0.2f, fadeAt + 1.4f, t) * 0.5f;
                pm = new Matrix4f((Matrix4fc)c.m).translate(p.x, p.y - sink, p.z).rotateXYZ(p.rx, p.ry, p.rz);
                float hy = (float)PT[p.part][1] * 0.05859375f / 2.0f;
                DeathFx.cube(w2, pm, -0.115f, hy, -0.115f, 0.115f, hy + 0.02f, 0.115f, -8780539, c.light);
            }
        }
    }

    static void step(P p, float dt) {
        if (p.landed || dt <= 0.0f) {
            return;
        }
        p.vy -= p.grav * dt;
        float d = (float)Math.exp(-p.drag * dt);
        p.vx *= d;
        p.vz *= d;
        if (p.drag > 1.0f) {
            p.vy = Math.max(p.vy, -1.4f);
        }
        p.x += p.vx * dt;
        p.y += p.vy * dt;
        p.z += p.vz * dt;
        p.rx += p.wx * dt;
        p.ry += p.wy * dt;
        p.rz += p.wz * dt;
        float floor = p.s / 2.0f;
        if (p.y <= floor && p.vy < 0.0f) {
            p.y = floor;
            p.landed = true;
            p.vz = 0.0f;
            p.vy = 0.0f;
            p.vx = 0.0f;
        }
    }

    static void stepPart(P p, float dt, float half) {
        if (p.landed || dt <= 0.0f) {
            return;
        }
        p.vy -= 11.0f * dt;
        p.x += p.vx * dt;
        p.y += p.vy * dt;
        p.z += p.vz * dt;
        p.rx += p.wx * dt;
        p.ry += p.wy * dt;
        p.rz += p.wz * dt;
        if (p.y <= half && p.vy < 0.0f) {
            if (p.vy < -3.0f) {
                p.vy = -p.vy * 0.25f;
                p.vx *= 0.4f;
                p.vz *= 0.4f;
                p.wx *= 0.3f;
                p.wz *= 0.3f;
                p.y = half;
            } else {
                p.y = half;
                p.landed = true;
                p.rx = 1.5707964f;
                p.rz = 0.0f;
            }
        }
    }

    static int mul(int a, int b) {
        int r = (a >> 16 & 0xFF) * (b >> 16 & 0xFF) / 255;
        int g = (a >> 8 & 0xFF) * (b >> 8 & 0xFF) / 255;
        int bb = (a & 0xFF) * (b & 0xFF) / 255;
        return r << 16 | g << 8 | bb;
    }

    static final class S {
        final int kind;
        final Random r;
        float lastT = -1.0f;
        long once;
        final List<P> ps = new ArrayList<P>();
        final List<float[]> pools = new ArrayList<float[]>();
        float[][] bolt;
        float boltAt = -1.0f;
        float[] trail = new float[0];

        S(int kind, Random r) {
            this.kind = kind;
            this.r = r;
        }

        boolean once(int bit, boolean when) {
            if (!when || (this.once & 1L << bit) != 0L) {
                return false;
            }
            this.once |= 1L << bit;
            return true;
        }
    }

    static final class C {
        class_310 mc;
        class_4597 buf;
        Matrix4f m;
        DeathFx.Fx f;
        S s;
        float t;
        float dt;
        int light;
        boolean fp;

        C() {
        }

        class_4588 white() {
            return this.buf.method_73477(class_12249.method_75994((class_2960)Accessories.WHITE));
        }

        class_4588 skin() {
            return this.buf.method_73477(class_12249.method_75994((class_2960)(this.f.skin != null ? this.f.skin : Accessories.WHITE)));
        }

        int skinTint(int rgb) {
            return this.f.skin != null ? 0xFF000000 | rgb : 0xFF000000 | DeathMore.mul(rgb, 0x8A8A8A);
        }

        class_4588 glow() {
            return this.buf.method_73477(class_12249.method_76023());
        }

        void sound(class_3414 e, float vol, float pitch) {
            try {
                this.mc.field_1687.method_8486(this.f.ox, this.f.oy + 1.0, this.f.oz, e, class_3419.field_15248, vol, pitch, false);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    static final class P {
        float x;
        float y;
        float z;
        float vx;
        float vy;
        float vz;
        float s;
        float rx;
        float ry;
        float rz;
        float wx;
        float wy;
        float wz;
        int col;
        int part = -1;
        float rel;
        float grav = 11.0f;
        float drag = 0.2f;
        float seed;
        float glow;
        boolean landed;
        boolean blood;
        boolean splat;
        boolean alive = true;

        P() {
        }
    }

    static final class Fig {
        float headX;
        float armRX;
        float armRZ;
        float armLX;
        float armLZ;
        float legRX;
        float legLX;
        float sx = 1.0f;
        float sz = 1.0f;
        int only = 63;

        Fig() {
        }

        Matrix4f[] mats(Matrix4f hip) {
            Matrix4f[] o = new Matrix4f[]{new Matrix4f((Matrix4fc)hip).translate(0.0f, 0.703f, 0.0f).rotateX(this.headX).translate(0.0f, 0.234f, 0.0f), new Matrix4f((Matrix4fc)hip).translate(0.0f, 0.3516f, 0.0f).scale(this.sx, 1.0f, this.sz), new Matrix4f((Matrix4fc)hip).translate(-0.3516f * this.sx, 0.586f, 0.0f).rotateX(this.armRX).rotateZ(-this.armRZ).translate(0.0f, -0.234f, 0.0f), new Matrix4f((Matrix4fc)hip).translate(0.3516f * this.sx, 0.586f, 0.0f).rotateX(this.armLX).rotateZ(this.armLZ).translate(0.0f, -0.234f, 0.0f), new Matrix4f((Matrix4fc)hip).translate(-0.1172f, 0.0f, 0.0f).rotateX(this.legRX).translate(0.0f, -0.3516f, 0.0f), new Matrix4f((Matrix4fc)hip).translate(0.1172f, 0.0f, 0.0f).rotateX(this.legLX).translate(0.0f, -0.3516f, 0.0f)};
            return o;
        }

        void draw(class_4588 v, Matrix4f hip, int tint, int light) {
            Matrix4f[] o = this.mats(hip);
            for (int i = 0; i < 6; ++i) {
                if ((this.only & 1 << i) == 0) continue;
                DeathMore.part(v, o[i], i, tint, light);
            }
        }
    }
}


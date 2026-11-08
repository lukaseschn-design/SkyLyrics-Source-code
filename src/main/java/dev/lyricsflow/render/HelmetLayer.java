/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10055
 *  net.minecraft.class_11659
 *  net.minecraft.class_12249
 *  net.minecraft.class_1304
 *  net.minecraft.class_1657
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3883
 *  net.minecraft.class_3887
 *  net.minecraft.class_4587
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_4588
 *  net.minecraft.class_4608
 *  net.minecraft.class_591
 */
package dev.lyricsflow.render;
import net.minecraft.class_10017;

import dev.lyricsflow.LyricsFlow;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.config.LFConfig;
import dev.lyricsflow.render.Cosmetics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_10055;
import net.minecraft.class_11659;
import net.minecraft.class_12249;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4608;
import net.minecraft.class_591;

public final class HelmetLayer
extends class_3887<class_10055, class_591> {
    public static final String ID = "astronaut_helmet";
    private static final class_2960 WHITE = LyricsFlow.id("textures/misc/white.png");
    private static final float PX = 0.0625f;
    private static final float V = 0.5f;
    private static List<Vox> solid;
    private static List<Vox> glow;
    private static final int WHITE_SHELL = 15922423;
    private static final int SHADE = 14278116;
    private static final int RIM = 12173259;
    private static final int COLLAR = 1843760;
    private static final int COLLAR_HI = 3029072;
    private static final int RED = 14879255;
    private static final int FLAG_W = 0xFFFFFF;
    private static final int ANT = 10134192;
    private static final float VX0 = -3.5f;
    private static final float VX1 = 3.5f;
    private static final float VY0 = -6.5f;
    private static final float VY1 = -1.5f;
    private static final String[] FLAG;

    public HelmetLayer(class_3883<class_10055, class_591> parent) {
        super(parent);
    }

    public static boolean on() {
        LFConfig c = ConfigManager.get();
        return c.worn().contains(ID);
    }

    @Override
    public void method_4199(class_4587 pose, class_11659 queue, int light, class_10055 state, float yRot, float xRot) {
        double t;
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || state.field_53333 || state.field_53542) {
            return;
        }
        if (!Cosmetics.forEntity(state.field_53528).contains(ID)) {
            return;
        }
        class_1657 owner = Cosmetics.playerOf(state.field_53528);
        if (owner == null) {
            return;
        }
        if (!((class_591)this.method_17165()).field_3398.field_3665) {
            return;
        }
        if (solid == null) {
            HelmetLayer.build();
        }
        boolean blink = (t = (double)System.nanoTime() / 1.0E9) % 1.4 < 0.9;
        pose.method_22903();
        ((class_591)this.method_17165()).field_3398.method_22703(pose);
        if (!owner.method_6118(class_1304.field_6169).method_7960()) {
            pose.method_46416(0.0f, -0.25f, 0.0f);
            pose.method_22905(1.17f, 1.17f, 1.17f);
            pose.method_46416(0.0f, 0.25f, 0.0f);
        }
        queue.method_73483(pose, class_12249.method_75994((class_2960)WHITE), (p, vc) -> {
            for (Vox v : solid) {
                HelmetLayer.cube(vc, p, v, light);
            }
        });
        queue.method_73483(pose, class_12249.method_76000((class_2960)WHITE), (p, vc) -> HelmetLayer.visor(vc, p, light));
        queue.method_73483(pose, class_12249.method_75994((class_2960)WHITE), (p, vc) -> {
            for (Vox v : glow) {
                if (v.rgb() == 0xFF3030 && !blink) continue;
                HelmetLayer.cube(vc, p, v, 0xF000F0);
            }
        });
        pose.method_22909();
    }

    private static void build() {
        int iy;
        float cx;
        int iz;
        int iy2;
        int ix;
        HashMap<Long, int[]> m = new HashMap<Long, int[]>();
        ArrayList<Vox> g = new ArrayList<Vox>();
        for (ix = -11; ix < 11; ++ix) {
            for (iy2 = -19; iy2 < 1; ++iy2) {
                for (iz = -11; iz < 11; ++iz) {
                    boolean front;
                    int edges;
                    boolean inner;
                    cx = ((float)ix + 0.5f) * 0.5f;
                    float cy = ((float)iy2 + 0.5f) * 0.5f;
                    float cz = ((float)iz + 0.5f) * 0.5f;
                    boolean bl = inner = Math.abs(cx) < 4.5f && Math.abs(cz) < 4.5f && cy > -8.5f;
                    if (inner || (edges = (Math.abs(cx) > 5.0f ? 1 : 0) + (Math.abs(cz) > 5.0f ? 1 : 0) + (cy < -9.0f ? 1 : 0)) >= 2 || cz < -4.5f && cx > -3.5f && cx < 3.5f && cy > -6.5f && cy < -1.5f) continue;
                    int col = 15922423;
                    boolean bl2 = front = cz < -4.5f;
                    if (front && cx > -4.5f && cx < 4.5f && cy > -7.5f && cy < -0.5f) {
                        col = 12173259;
                    }
                    if (cy > -0.5f) {
                        col = 14278116;
                    }
                    if (Math.abs(cy + 8.75f) < 0.3f && !front) {
                        col = 14278116;
                    }
                    if (cx > 5.0f && cz > -2.0f && cz < 3.0f && cy > -7.0f && cy < -3.0f) {
                        int pz = (int)Math.floor((cz + 2.0f) / 0.5f);
                        int py = (int)Math.floor((cy + 7.0f) / 0.5f);
                        col = FLAG[py].charAt(pz) == 'W' ? 0xFFFFFF : 14879255;
                    }
                    HelmetLayer.put(m, ix, iy2, iz, col);
                }
            }
        }
        for (ix = -12; ix < 12; ++ix) {
            for (iy2 = 0; iy2 < 3; ++iy2) {
                for (iz = -12; iz < 12; ++iz) {
                    cx = ((float)ix + 0.5f) * 0.5f;
                    float cz = ((float)iz + 0.5f) * 0.5f;
                    float r = Math.max(Math.abs(cx), Math.abs(cz));
                    if (r < 4.5f || r > 6.0f || Math.abs(cx) > 5.5f && Math.abs(cz) > 5.5f) continue;
                    HelmetLayer.put(m, ix, iy2, iz, iy2 == 0 ? 3029072 : 1843760);
                }
            }
        }
        for (iy = -23; iy < -19; ++iy) {
            HelmetLayer.put(m, -7, iy, 6, 10134192);
        }
        g.add(new Vox(-7, -24, 6, 0xFF3030, 63, true));
        for (iy = -11; iy < -9; ++iy) {
            for (int iz2 = -1; iz2 < 1; ++iz2) {
                g.add(new Vox(-12, iy, iz2, 6285567, 63, true));
            }
        }
        ArrayList<Vox> s = new ArrayList<Vox>();
        for (Map.Entry e : m.entrySet()) {
            long k = (Long)e.getKey();
            int ix2 = (int)(k >> 40) - 1000;
            int iy3 = (int)(k >> 20 & 0xFFFFFL) - 1000;
            int iz3 = (int)(k & 0xFFFFFL) - 1000;
            int faces = 0;
            if (!m.containsKey(HelmetLayer.key(ix2, iy3 - 1, iz3))) {
                faces |= 1;
            }
            if (!m.containsKey(HelmetLayer.key(ix2, iy3 + 1, iz3))) {
                faces |= 2;
            }
            if (!m.containsKey(HelmetLayer.key(ix2, iy3, iz3 - 1))) {
                faces |= 4;
            }
            if (!m.containsKey(HelmetLayer.key(ix2, iy3, iz3 + 1))) {
                faces |= 8;
            }
            if (!m.containsKey(HelmetLayer.key(ix2 - 1, iy3, iz3))) {
                faces |= 0x10;
            }
            if (!m.containsKey(HelmetLayer.key(ix2 + 1, iy3, iz3))) {
                faces |= 0x20;
            }
            if (faces == 0) continue;
            s.add(new Vox(ix2, iy3, iz3, ((int[])e.getValue())[0], faces, false));
        }
        solid = s;
        glow = g;
    }

    private static long key(int x, int y, int z) {
        return (long)(x + 1000) << 40 | (long)(y + 1000) << 20 | (long)(z + 1000);
    }

    private static void put(Map<Long, int[]> m, int x, int y, int z, int col) {
        m.put(HelmetLayer.key(x, y, z), new int[]{col});
    }

    private static void visor(class_4588 vc, class_4587.class_4665 p, int light) {
        float z = -0.3125f;
        float x0 = -0.21875f;
        float x1 = 0.21875f;
        float y0 = -0.40625f;
        float y1 = -0.09375f;
        int top = -2131508128;
        int bot = 1892192302;
        HelmetLayer.v(vc, p, x0, y0, z, top, light, 0.0f, 0.0f, -1.0f);
        HelmetLayer.v(vc, p, x1, y0, z, top, light, 0.0f, 0.0f, -1.0f);
        HelmetLayer.v(vc, p, x1, y1, z, bot, light, 0.0f, 0.0f, -1.0f);
        HelmetLayer.v(vc, p, x0, y1, z, bot, light, 0.0f, 0.0f, -1.0f);
        float sx0 = -0.1625f;
        float sx1 = -0.10625f;
        float sy0 = -0.38125f;
        float sy1 = -0.225f;
        float zz = z - 6.25E-4f;
        int sh = 0x70FFFFFF;
        HelmetLayer.v(vc, p, sx0, sy0, zz, sh, light, 0.0f, 0.0f, -1.0f);
        HelmetLayer.v(vc, p, sx1, sy0, zz, sh, light, 0.0f, 0.0f, -1.0f);
        HelmetLayer.v(vc, p, sx1 - 0.0375f, sy1, zz, sh, light, 0.0f, 0.0f, -1.0f);
        HelmetLayer.v(vc, p, sx0 - 0.0375f, sy1, zz, sh, light, 0.0f, 0.0f, -1.0f);
    }

    private static void cube(class_4588 vc, class_4587.class_4665 p, Vox v, int light) {
        float x0 = (float)v.x() * 0.5f * 0.0625f;
        float x1 = (float)(v.x() + 1) * 0.5f * 0.0625f;
        float y0 = (float)v.y() * 0.5f * 0.0625f;
        float y1 = (float)(v.y() + 1) * 0.5f * 0.0625f;
        float z0 = (float)v.z() * 0.5f * 0.0625f;
        float z1 = (float)(v.z() + 1) * 0.5f * 0.0625f;
        int f = v.faces();
        int rgb = v.rgb();
        boolean both = v.glow();
        if ((f & 1) != 0) {
            HelmetLayer.quad(vc, p, rgb, 1.0f, light, both, 0.0f, -1.0f, 0.0f, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        }
        if ((f & 2) != 0) {
            HelmetLayer.quad(vc, p, rgb, 0.55f, light, both, 0.0f, 1.0f, 0.0f, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0);
        }
        if ((f & 4) != 0) {
            HelmetLayer.quad(vc, p, rgb, 0.9f, light, both, 0.0f, 0.0f, -1.0f, x0, y1, z0, x1, y1, z0, x1, y0, z0, x0, y0, z0);
        }
        if ((f & 8) != 0) {
            HelmetLayer.quad(vc, p, rgb, 0.8f, light, both, 0.0f, 0.0f, 1.0f, x1, y1, z1, x0, y1, z1, x0, y0, z1, x1, y0, z1);
        }
        if ((f & 0x10) != 0) {
            HelmetLayer.quad(vc, p, rgb, 0.72f, light, both, -1.0f, 0.0f, 0.0f, x0, y1, z1, x0, y1, z0, x0, y0, z0, x0, y0, z1);
        }
        if ((f & 0x20) != 0) {
            HelmetLayer.quad(vc, p, rgb, 0.72f, light, both, 1.0f, 0.0f, 0.0f, x1, y1, z0, x1, y1, z1, x1, y0, z1, x1, y0, z0);
        }
    }

    private static void quad(class_4588 vc, class_4587.class_4665 p, int rgb, float k, int light, boolean both, float nx, float ny, float nz, float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz, float dx, float dy, float dz) {
        if (both) {
            k = 1.0f;
        }
        int r = Math.min(255, (int)((float)(rgb >> 16 & 0xFF) * k));
        int g = Math.min(255, (int)((float)(rgb >> 8 & 0xFF) * k));
        int b = Math.min(255, (int)((float)(rgb & 0xFF) * k));
        int col = 0xFF000000 | r << 16 | g << 8 | b;
        HelmetLayer.v(vc, p, ax, ay, az, col, light, nx, ny, nz);
        HelmetLayer.v(vc, p, bx, by, bz, col, light, nx, ny, nz);
        HelmetLayer.v(vc, p, cx, cy, cz, col, light, nx, ny, nz);
        HelmetLayer.v(vc, p, dx, dy, dz, col, light, nx, ny, nz);
    }

    private static void v(class_4588 vc, class_4587.class_4665 p, float x, float y, float z, int col, int light, float nx, float ny, float nz) {
        vc.method_56824(p, x, y, z).method_39415(col).method_22913(0.5f, 0.5f).method_22922(class_4608.field_21444).method_60803(light).method_60831(p, nx, ny, nz);
    }

    static {
        FLAG = new String[]{"RRRRRRRRRR", "RRRWWWRRRR", "RRWWRRRRRR", "RRWRRRRWRR", "RRWRRRWWWR", "RRWWRRRWRR", "RRRWWWRRRR", "RRRRRRRRRR"};
    }

    private record Vox(int x, int y, int z, int rgb, int faces, boolean glow) {
    }
}


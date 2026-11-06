/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.state.AvatarRenderState
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.rendertype.RenderTypes
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.resources.Identifier
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.entity.RenderLayerParent
 *  net.minecraft.client.renderer.entity.layers.RenderLayer
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.model.player.PlayerModel
 */
package dev.lyricsflow.render;

import dev.lyricsflow.SkyLyrics;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.config.LFConfig;
import dev.lyricsflow.render.Cosmetics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.model.player.PlayerModel;

public final class HelmetLayer
extends RenderLayer<AvatarRenderState, PlayerModel> {
    public static final String ID = "astronaut_helmet";
    private static final Identifier WHITE = SkyLyrics.id("textures/misc/white.png");
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

    public HelmetLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    public static boolean on() {
        LFConfig c = ConfigManager.get();
        return c.worn().contains(ID);
    }

    public void submit(PoseStack pose, SubmitNodeCollector queue, int light, AvatarRenderState state, float yRot, float xRot) {
        double t;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || state.isInvisible || state.isSpectator) {
            return;
        }
        if (!Cosmetics.forEntity(state.id).contains(ID)) {
            return;
        }
        Player owner = Cosmetics.playerOf(state.id);
        if (owner == null) {
            return;
        }
        if (!((PlayerModel)this.getParentModel()).head.visible) {
            return;
        }
        if (solid == null) {
            HelmetLayer.build();
        }
        boolean blink = (t = (double)System.nanoTime() / 1.0E9) % 1.4 < 0.9;
        pose.pushPose();
        ((PlayerModel)this.getParentModel()).head.translateAndRotate(pose);
        if (!owner.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            pose.translate(0.0f, -0.25f, 0.0f);
            pose.scale(1.17f, 1.17f, 1.17f);
            pose.translate(0.0f, 0.25f, 0.0f);
        }
        queue.submitCustomGeometry(pose, RenderTypes.entityCutout((Identifier)WHITE), (p, vc) -> {
            for (Vox v : solid) {
                HelmetLayer.cube(vc, p, v, light);
            }
        });
        queue.submitCustomGeometry(pose, RenderTypes.entityTranslucent((Identifier)WHITE), (p, vc) -> HelmetLayer.visor(vc, p, light));
        queue.submitCustomGeometry(pose, RenderTypes.entityCutout((Identifier)WHITE), (p, vc) -> {
            for (Vox v : glow) {
                if (v.rgb() == 0xFF3030 && !blink) continue;
                HelmetLayer.cube(vc, p, v, 0xF000F0);
            }
        });
        pose.popPose();
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

    private static void visor(VertexConsumer vc, PoseStack.Pose p, int light) {
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

    private static void cube(VertexConsumer vc, PoseStack.Pose p, Vox v, int light) {
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

    private static void quad(VertexConsumer vc, PoseStack.Pose p, int rgb, float k, int light, boolean both, float nx, float ny, float nz, float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz, float dx, float dy, float dz) {
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

    private static void v(VertexConsumer vc, PoseStack.Pose p, float x, float y, float z, int col, int light, float nx, float ny, float nz) {
        vc.addVertex(p, x, y, z).setColor(col).setUv(0.5f, 0.5f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, nx, ny, nz);
    }

    static {
        FLAG = new String[]{"RRRRRRRRRR", "RRRWWWRRRR", "RRWWRRRRRR", "RRWRRRRWRR", "RRWRRRWWWR", "RRWWRRRWRR", "RRRWWWRRRR", "RRRRRRRRRR"};
    }

    private record Vox(int x, int y, int z, int rgb, int faces, boolean glow) {
    }
}


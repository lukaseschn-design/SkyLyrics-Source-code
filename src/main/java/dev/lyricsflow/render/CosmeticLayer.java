/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.client.renderer.entity.state.AxolotlRenderState
 *  net.minecraft.client.renderer.entity.state.AvatarRenderState
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.rendertype.RenderTypes
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.resources.Identifier
 *  net.minecraft.client.Minecraft
 *  net.minecraft.util.Mth
 *  net.minecraft.client.renderer.entity.RenderLayerParent
 *  net.minecraft.client.renderer.entity.layers.RenderLayer
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.model.geom.ModelLayers
 *  net.minecraft.world.entity.animal.axolotl.Axolotl$Variant
 *  net.minecraft.client.model.animal.axolotl.AxolotlModel
 *  net.minecraft.client.model.player.PlayerModel
 *  net.minecraft.client.model.geom.ModelPart
 *  com.mojang.math.Axis
 *  org.joml.Quaternionfc
 */
package dev.lyricsflow.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.lyricsflow.SkyLyrics;
import dev.lyricsflow.render.Cosmetics;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.entity.state.AxolotlRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.client.model.animal.axolotl.AdultAxolotlModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import com.mojang.math.Axis;
import org.joml.Quaternionfc;

public final class CosmeticLayer
extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final Identifier WHITE = SkyLyrics.id("textures/misc/white.png");
    private static final float PX = 0.0625f;
    static final float HELMET_FIT = 1.17f;
    private static Map<String, List<Group>> all;
    private static final Identifier RING;
    private static final Identifier GLOW;
    private static final Identifier AXOLOTL_BLUE;
    private static AdultAxolotlModel axolotlModel;
    private static final AxolotlRenderState AXO_STATE;
    private static final float AXOLOTL_SCALE = 0.5f;

    public CosmeticLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    public void submit(PoseStack pose, SubmitNodeCollector queue, int light, AvatarRenderState state, float yRot, float xRot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || state.isInvisible || state.isSpectator) {
            return;
        }
        Player owner = Cosmetics.playerOf(state.id);
        if (owner == null) {
            return;
        }
        for (String cos : Cosmetics.forEntity(state.id)) {
            this.one(pose, queue, light, state, yRot, xRot, owner, cos);
        }
    }

    private void one(PoseStack pose, SubmitNodeCollector queue, int light, AvatarRenderState state, float yRot, float xRot, Player owner, String cos) {
        List<Group> groups;
        if ("axolotl".equals(cos)) {
            this.axolotl(pose, queue, light, owner);
            return;
        }
        if ("kuz3y_wqq".equals(cos)) {
            this.buddy(pose, queue, light, owner, state.id, yRot, xRot);
            return;
        }
        if (all == null) {
            CosmeticLayer.load();
        }
        if ((groups = all.get(cos)) == null) {
            return;
        }
        double t = (double)System.nanoTime() / 1.0E9 % 3600.0;
        PlayerModel model = (PlayerModel)this.getParentModel();
        boolean helmetOn = !owner.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
        for (Group g : groups) {
            String a;
            ModelPart part;
            ModelPart class_6302 = part = "body".equals(g.part()) ? model.body : model.head;
            if (!part.visible) continue;
            pose.pushPose();
            part.translateAndRotate(pose);
            float lift = g.lift();
            if ("head".equals(g.part()) && helmetOn) {
                lift *= 1.17f;
            }
            if (lift != 1.0f) {
                pose.translate(0.0f, -0.25f, 0.0f);
                pose.scale(lift, lift, lift);
                pose.translate(0.0f, 0.25f, 0.0f);
            }
            if ((a = g.anim()).equals("bob")) {
                pose.translate(0.0f, -((float)Math.sin(t * 2.2)) * 0.6f * 0.0625f, 0.0f);
            } else if (a.startsWith("sway:")) {
                String[] q = a.split(":");
                float deg = Float.parseFloat(q[1]) * (float)Math.sin(t * 2.4);
                float py = Float.parseFloat(q[3]) * 0.0625f;
                float pz = Float.parseFloat(q[4]) * 0.0625f;
                pose.translate(0.0f, py, pz);
                pose.rotate((Quaternionfc)(switch (q[2]) {
                    case "x" -> Axis.XP.rotationDegrees(deg);
                    case "y" -> Axis.YP.rotationDegrees(deg);
                    default -> Axis.ZP.rotationDegrees(deg);
                }));
                pose.translate(0.0f, -py, -pz);
            } else if (a.startsWith("flap:")) {
                int side = Integer.parseInt(a.substring(5));
                float beat = (float)(0.5 + 0.5 * Math.sin(t * 2.6));
                float deg = (float)(-side) * (6.0f + 30.0f * beat);
                pose.translate(0.0f, 0.0f, 0.21875f);
                pose.rotate((Quaternionfc)Axis.YP.rotationDegrees(deg));
                pose.rotate((Quaternionfc)Axis.ZP.rotationDegrees((float)side * 6.0f * (beat - 0.5f)));
                pose.translate(0.0f, 0.0f, -0.21875f);
            }
            RenderType type = g.alpha() < 255 && !g.glow() ? RenderTypes.entityTranslucent((Identifier)WHITE) : RenderTypes.entityCutout((Identifier)WHITE);
            int lt = g.glow() ? 0xF000F0 : light;
            queue.submitCustomGeometry(pose, type, (p, vc) -> CosmeticLayer.emit(vc, p, g, lt));
            pose.popPose();
        }
        if ("halo".equals(cos)) {
            this.haloGlow(pose, queue, helmetOn, t);
        }
    }

    private void haloGlow(PoseStack pose, SubmitNodeCollector queue, boolean helmetOn, double t) {
        ModelPart head = ((PlayerModel)this.getParentModel()).head;
        if (!head.visible) {
            return;
        }
        pose.pushPose();
        head.translateAndRotate(pose);
        float lift = helmetOn ? 1.2636f : 1.08f;
        pose.translate(0.0f, -0.25f, 0.0f);
        pose.scale(lift, lift, lift);
        pose.translate(0.0f, 0.25f, 0.0f);
        pose.translate(0.015625f, -0.6875f - (float)Math.sin(t * 2.2) * 0.6f * 0.0625f, 0.015625f);
        float pulse = 0.82f + 0.18f * (float)Math.sin(t * 3.1);
        int ring = (int)(235.0f * pulse) << 24 | 0xFFD54A;
        int soft = (int)(120.0f * pulse) << 24 | 0xFFC23A;
        queue.submitCustomGeometry(pose, RenderTypes.eyes((Identifier)RING), (p, vc) -> CosmeticLayer.flat(vc, p, 0.45f, ring));
        queue.submitCustomGeometry(pose, RenderTypes.eyes((Identifier)GLOW), (p, vc) -> CosmeticLayer.flat(vc, p, 0.6875f, soft));
        pose.popPose();
    }

    private static void flat(VertexConsumer vc, PoseStack.Pose p, float r, int col) {
        int k;
        float[][] c = new float[][]{{-r, -r, 0.0f, 0.0f}, {r, -r, 1.0f, 0.0f}, {r, r, 1.0f, 1.0f}, {-r, r, 0.0f, 1.0f}};
        for (k = 0; k < 4; ++k) {
            CosmeticLayer.fv(vc, p, c[k][0], c[k][1], c[k][2], c[k][3], col);
        }
        for (k = 3; k >= 0; --k) {
            CosmeticLayer.fv(vc, p, c[k][0], c[k][1], c[k][2], c[k][3], col);
        }
    }

    private static void fv(VertexConsumer vc, PoseStack.Pose p, float x, float z, float u, float v, int col) {
        vc.addVertex(p, x, 0.0f, z).setColor(col).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(p, 0.0f, -1.0f, 0.0f);
    }

    private void axolotl(PoseStack pose, SubmitNodeCollector queue, int light, Player owner) {
        ModelPart head = ((PlayerModel)this.getParentModel()).head;
        if (!head.visible) {
            return;
        }
        try {
            if (axolotlModel == null) {
                axolotlModel = new AdultAxolotlModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.AXOLOTL));
            }
            AxolotlRenderStateSetup.apply(AXO_STATE, (float)((double)System.nanoTime() / 1.0E9 % 3600.0 * 20.0));
            axolotlModel.setupAnim(AXO_STATE);
            pose.pushPose();
            head.translateAndRotate(pose);
            boolean helm = !owner.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
            pose.translate(0.0f, (helm ? -9.2f : -8.0f) * 0.0625f, 0.0f);
            pose.scale(0.5f, 0.5f, 0.5f);
            pose.translate(0.0f, -1.375f, 0.0f);
            queue.submitModelPart(axolotlModel.root(), pose, RenderTypes.entityCutout((Identifier)AXOLOTL_BLUE), light, OverlayTexture.NO_OVERLAY, null);
            pose.popPose();
        }
        catch (Throwable e) {
            SkyLyrics.LOG.warn("[SkyLyrics] Axolotl cosmetic failed: {}", (Object)e.toString());
        }
    }

    private void buddy(PoseStack pose, SubmitNodeCollector queue, int light, Player owner, int id, float headYaw, float headPitch) {
        ModelPart body = ((PlayerModel)this.getParentModel()).body;
        if (!body.visible) {
            return;
        }
        PlayerModel b = Cosmetics.buddyFor(id, (double)System.nanoTime() / 1.0E9 % 3600.0, headYaw, headPitch);
        if (b == null) {
            return;
        }
        boolean chest = !owner.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
        pose.pushPose();
        body.translateAndRotate(pose);
        pose.translate((chest ? -6.9f : -6.2f) * 0.0625f, (chest ? -1.2f : -0.2f) * 0.0625f, 0.0f);
        float k = 0.32f;
        pose.scale(k, k, k);
        pose.translate(0.0f, -0.75f, 0.0f);
        pose.rotate((Quaternionfc)Axis.YP.rotation((float)Math.toRadians(Mth.clamp((float)headYaw, (float)-80.0f, (float)80.0f))));
        queue.submitModelPart(b.root(), pose, RenderTypes.entityTranslucent((Identifier)Cosmetics.KUZ3Y), light, OverlayTexture.NO_OVERLAY, null);
        pose.popPose();
    }

    private static void emit(VertexConsumer vc, PoseStack.Pose p, Group g, int light) {
        float[] q = g.quads();
        int alpha = g.alpha() << 24;
        boolean both = false;
        for (int i = 0; i < q.length; i += 16) {
            int k;
            int col = alpha | Float.floatToRawIntBits(q[i]) & 0xFFFFFF;
            float nx = g.glow() ? 0.0f : q[i + 1];
            float ny = g.glow() ? 1.0f : q[i + 2];
            float nz = g.glow() ? 0.0f : q[i + 3];
            for (k = 0; k < 4; ++k) {
                CosmeticLayer.v(vc, p, q[i + 4 + k * 3], q[i + 5 + k * 3], q[i + 6 + k * 3], col, light, nx, ny, nz);
            }
            if (!both) continue;
            for (k = 3; k >= 0; --k) {
                CosmeticLayer.v(vc, p, q[i + 4 + k * 3], q[i + 5 + k * 3], q[i + 6 + k * 3], col, light, -nx, -ny, -nz);
            }
        }
    }

    private static void v(VertexConsumer vc, PoseStack.Pose p, float x, float y, float z, int col, int light, float nx, float ny, float nz) {
        vc.addVertex(p, x, y, z).setColor(col).setUv(0.5f, 0.5f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, nx, ny, nz);
    }

    private static synchronized void load() {
        if (all != null) {
            return;
        }
        HashMap<String, List<Group>> out = new HashMap<String, List<Group>>();
        try (InputStream in = CosmeticLayer.class.getResourceAsStream("/assets/lyricsflow/cosmetics.json");){
            if (in != null) {
                JsonArray arr = JsonParser.parseReader((Reader)new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonArray();
                for (JsonElement e : arr) {
                    JsonObject o = e.getAsJsonObject();
                    ArrayList<Group> gs = new ArrayList<Group>();
                    for (JsonElement ge : o.getAsJsonArray("groups")) {
                        gs.add(CosmeticLayer.bake(ge.getAsJsonObject()));
                    }
                    out.put(o.get("id").getAsString(), gs);
                }
            }
        }
        catch (Exception ex) {
            SkyLyrics.LOG.warn("[SkyLyrics] Could not load cosmetics: {}", (Object)ex.toString());
        }
        all = out;
    }

    private static long key(int x, int y, int z) {
        return (long)(x + 4096) << 26 | (long)(y + 4096) << 13 | (long)(z + 4096);
    }

    private static Group bake(JsonObject o) {
        float s = o.get("size").getAsFloat() * 0.0625f;
        float oy = o.has("oy") ? o.get("oy").getAsFloat() * 0.0625f : 0.0f;
        JsonArray v = o.getAsJsonArray("v");
        int n = v.size() / 4;
        int[] xs = new int[n];
        int[] ys = new int[n];
        int[] zs = new int[n];
        int[] cs = new int[n];
        HashSet<Long> filled = new HashSet<Long>();
        for (int i = 0; i < n; ++i) {
            xs[i] = v.get(i * 4).getAsInt();
            ys[i] = v.get(i * 4 + 1).getAsInt();
            zs[i] = v.get(i * 4 + 2).getAsInt();
            cs[i] = v.get(i * 4 + 3).getAsInt();
            filled.add(CosmeticLayer.key(xs[i], ys[i], zs[i]));
        }
        boolean glow = o.get("glow").getAsBoolean();
        ArrayList<Float> q = new ArrayList<Float>();
        for (int i = 0; i < n; ++i) {
            int x = xs[i];
            int y = ys[i];
            int z = zs[i];
            int rgb = cs[i];
            float x0 = (float)x * s;
            float x1 = (float)(x + 1) * s;
            float y0 = (float)y * s + oy;
            float y1 = (float)(y + 1) * s + oy;
            float z0 = (float)z * s;
            float z1 = (float)(z + 1) * s;
            if (!filled.contains(CosmeticLayer.key(x, y - 1, z))) {
                CosmeticLayer.face(q, rgb, glow ? 1.0f : 1.0f, 0.0f, -1.0f, 0.0f, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
            }
            if (!filled.contains(CosmeticLayer.key(x, y + 1, z))) {
                CosmeticLayer.face(q, rgb, glow ? 1.0f : 0.55f, 0.0f, 1.0f, 0.0f, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0);
            }
            if (!filled.contains(CosmeticLayer.key(x, y, z - 1))) {
                CosmeticLayer.face(q, rgb, glow ? 1.0f : 0.9f, 0.0f, 0.0f, -1.0f, x0, y1, z0, x1, y1, z0, x1, y0, z0, x0, y0, z0);
            }
            if (!filled.contains(CosmeticLayer.key(x, y, z + 1))) {
                CosmeticLayer.face(q, rgb, glow ? 1.0f : 0.8f, 0.0f, 0.0f, 1.0f, x1, y1, z1, x0, y1, z1, x0, y0, z1, x1, y0, z1);
            }
            if (!filled.contains(CosmeticLayer.key(x - 1, y, z))) {
                CosmeticLayer.face(q, rgb, glow ? 1.0f : 0.72f, -1.0f, 0.0f, 0.0f, x0, y1, z1, x0, y1, z0, x0, y0, z0, x0, y0, z1);
            }
            if (filled.contains(CosmeticLayer.key(x + 1, y, z))) continue;
            CosmeticLayer.face(q, rgb, glow ? 1.0f : 0.72f, 1.0f, 0.0f, 0.0f, x1, y1, z0, x1, y1, z1, x1, y0, z1, x1, y0, z0);
        }
        float[] arr = new float[q.size()];
        for (int i = 0; i < arr.length; ++i) {
            arr[i] = ((Float)q.get(i)).floatValue();
        }
        String part = o.get("part").getAsString();
        return new Group(part, o.get("anim").getAsString(), glow, o.get("alpha").getAsInt(), arr, !"head".equals(part) ? 1.0f : (o.has("oy") ? 1.01f : 1.08f));
    }

    private static void face(List<Float> q, int rgb, float k, float nx, float ny, float nz, float ... c) {
        int r = Math.min(255, (int)((float)(rgb >> 16 & 0xFF) * k));
        int g = Math.min(255, (int)((float)(rgb >> 8 & 0xFF) * k));
        int b = Math.min(255, (int)((float)(rgb & 0xFF) * k));
        q.add(Float.valueOf(Float.intBitsToFloat(r << 16 | g << 8 | b)));
        q.add(Float.valueOf(nx));
        q.add(Float.valueOf(ny));
        q.add(Float.valueOf(nz));
        for (float f : c) {
            q.add(Float.valueOf(f));
        }
    }

    static {
        RING = SkyLyrics.id("textures/gui/shape/ring.png");
        GLOW = SkyLyrics.id("textures/gui/shape/glow.png");
        AXOLOTL_BLUE = Identifier.withDefaultNamespace((String)"textures/entity/axolotl/axolotl_blue.png");
        AXO_STATE = new AxolotlRenderState();
    }

    private record Group(String part, String anim, boolean glow, int alpha, float[] quads, float lift) {
    }

    private static final class AxolotlRenderStateSetup {
        private AxolotlRenderStateSetup() {
        }

        static void apply(AxolotlRenderState s, float age) {
            s.variant = Axolotl.Variant.BLUE;
            s.onGroundFactor = 1.0f;
            s.inWaterFactor = 0.0f;
            s.movingFactor = 0.0f;
            s.playingDeadFactor = 0.0f;
            s.ageInTicks = age;
            s.xRot = 0.0f;
            s.yRot = 0.0f;
            s.walkAnimationPos = 0.0f;
            s.walkAnimationSpeed = 0.0f;
        }
    }
}

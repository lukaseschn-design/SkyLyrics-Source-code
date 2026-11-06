/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.mojang.blaze3d.platform.NativeImage
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.rendertype.RenderTypes
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.resources.Identifier
 *  net.minecraft.client.Minecraft
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Pose
 *  com.mojang.blaze3d.vertex.PoseStack
 *  dev.lyricsflow.compat.LegacyBufferSource
 *  dev.lyricsflow.compat.LegacyBufferSource$BufferSource
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.model.geom.ModelLayers
 *  net.minecraft.client.model.player.PlayerModel
 *  com.mojang.math.Axis
 *  org.joml.Quaternionfc
 */
package dev.lyricsflow.render;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.lyricsflow.SkyLyrics;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.media.CosmeticSync;
import dev.lyricsflow.util.Anim;
import dev.lyricsflow.util.Http;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.lyricsflow.compat.LegacyBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import com.mojang.math.Axis;
import org.joml.Quaternionfc;

public final class Cosmetics {
    public static final String[] IDS = new String[]{"none", "kuz3y_wqq", "astronaut_helmet", "axolotl", "snorkel", "cat_ears", "pixel_glasses", "halo", "hair_bow", "cap_back", "devil_horns", "viking", "cat_headphones", "straw_hat", "pirate_hat", "spartan", "dragon_wings", "red_dragon_wings", "bat_wings", "captain_hat", "knight_helmet", "top_hat", "witch_hat", "jack_o_lantern", "cowboy_hat", "snapback", "ninja_band", "traffic_cone"};
    private static final float SCALE = 0.3f;
    private static final Identifier SKIN = SkyLyrics.id("cosmetic/buddy_skin");
    static final Identifier KUZ3Y = SkyLyrics.id("textures/cosmetic/kuz3y_wqq.png");
    private static final Map<Integer, PlayerModel> BUDDIES = new HashMap<Integer, PlayerModel>();
    private static PlayerModel wide;
    private static PlayerModel slim;
    private static String loadedName;
    private static String loadingName;
    private static boolean slimSkin;
    private static boolean ready;
    private static long nextTry;
    private static int tries;
    private static String lastWanted;

    static PlayerModel buddyFor(int entityId, double t, float headYaw, float headPitch) {
        PlayerModel model;
        try {
            if (BUDDIES.size() > 32) {
                BUDDIES.clear();
            }
            model = BUDDIES.computeIfAbsent(entityId, k -> new PlayerModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_SLIM), true));
        }
        catch (Throwable e) {
            return null;
        }
        Cosmetics.pose(model, t);
        model.head.yRot = 0.0f;
        model.head.xRot = (float)Math.toRadians(Mth.clamp((float)headPitch, (float)-60.0f, (float)60.0f));
        model.head.zRot = -0.04f * (float)Math.sin(t * 7.0);
        return model;
    }

    static PlayerModel buddy(double t) {
        PlayerModel model = Cosmetics.model(true);
        if (model == null) {
            return null;
        }
        Cosmetics.pose(model, t);
        return model;
    }

    private static void pose(PlayerModel model, double t) {
        model.resetPose();
        model.allParts().forEach(part -> part.visible = true);
        model.rightLeg.xRot = -1.35f;
        model.rightLeg.yRot = 0.18f;
        model.leftLeg.xRot = -1.35f;
        model.leftLeg.yRot = -0.18f;
        model.rightArm.xRot = -0.15f;
        model.rightArm.zRot = 2.55f + 0.38f * (float)Math.sin(t * 7.0);
        model.leftArm.xRot = -0.55f;
        model.leftArm.zRot = -0.12f;
        model.head.yRot = 0.25f * (float)Math.sin(t * 0.9);
        model.head.xRot = 0.08f + 0.05f * (float)Math.sin(t * 1.3);
        model.head.zRot = -0.06f * (float)Math.sin(t * 7.0);
    }

    private Cosmetics() {
    }

    public static void render(PoseStack ps, LegacyBufferSource buffers) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || buffers == null) {
            return;
        }
        for (Player pl : mc.level.players()) {
            if (!Cosmetics.wornBy(pl).contains("kuz3y_wqq") || pl == mc.player && mc.options.getCameraType().isFirstPerson() || pl.distanceToSqr(mc.gameRenderer.mainCamera().position()) > 4096.0) continue;
            Cosmetics.renderOne(ps, buffers, pl, "kuz3y_wqq");
        }
    }

    public static List<String> wornBy(Player p) {
        Minecraft mc = Minecraft.getInstance();
        if (!ConfigManager.get().enabled) {
            return List.of();
        }
        if (p == mc.player) {
            return ConfigManager.get().worn();
        }
        String s = CosmeticSync.of(p.getUUID());
        if (s == null || s.isEmpty() || "none".equals(s)) {
            return List.of();
        }
        ArrayList<String> out = new ArrayList<String>();
        for (String x : s.split(",")) {
            if (x.isEmpty() || "none".equals(x) || out.contains(x) || out.size() >= 5) continue;
            out.add(x);
        }
        return out;
    }

    public static List<String> forEntity(int entityId) {
        List<String> list;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return List.of();
        }
        if (mc.player != null && entityId == mc.player.getId()) {
            return Cosmetics.wornBy((Player)mc.player);
        }
        Entity class_12972 = mc.level.getEntity(entityId);
        if (class_12972 instanceof Player) {
            Player p = (Player)class_12972;
            list = Cosmetics.wornBy(p);
        } else {
            list = List.of();
        }
        return list;
    }

    public static Player playerOf(int entityId) {
        Player p;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return null;
        }
        Entity class_12972 = mc.level.getEntity(entityId);
        return class_12972 instanceof Player ? (p = (Player)class_12972) : null;
    }

    private static void renderOne(PoseStack ps, LegacyBufferSource buffers, Player pl, String name) {
        double top;
        boolean isSlim;
        Identifier skinTex;
        Minecraft mc = Minecraft.getInstance();
        if ("kuz3y_wqq".equals(name)) {
            skinTex = KUZ3Y;
            isSlim = true;
        } else {
            Cosmetics.ensureSkin(name);
            if (!ready || !name.equals(loadedName)) {
                return;
            }
            skinTex = SKIN;
            isSlim = slimSkin;
        }
        if (pl.isInvisible()) {
            return;
        }
        Pose pose = pl.getPose();
        if (pose != Pose.STANDING && pose != Pose.CROUCHING) {
            return;
        }
        PlayerModel model = Cosmetics.model(isSlim);
        if (model == null) {
            return;
        }
        float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Vec3 cam = mc.gameRenderer.mainCamera().position();
        Vec3 p = pl.getPosition(pt);
        float bodyDeg = Mth.rotLerp((float)pt, (float)pl.yBodyRotO, (float)pl.yBodyRot);
        double body = Math.toRadians(bodyDeg);
        double t = Anim.seconds();
        double side = 0.38;
        double d = top = pl.isCrouching() ? 1.12 : 1.4;
        if (!pl.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
            side += 0.06;
            top += 0.09;
        }
        double x = p.x - cam.x - Math.cos(body) * side;
        double y = p.y - cam.y + top;
        double z = p.z - cam.z - Math.sin(body) * side;
        model.resetPose();
        model.allParts().forEach(part -> part.visible = true);
        model.rightLeg.xRot = -1.35f;
        model.rightLeg.yRot = 0.18f;
        model.leftLeg.xRot = -1.35f;
        model.leftLeg.yRot = -0.18f;
        model.rightArm.xRot = -0.15f;
        model.rightArm.zRot = 2.55f + 0.38f * (float)Math.sin(t * 7.0);
        model.leftArm.xRot = -0.55f;
        model.leftArm.zRot = -0.12f;
        float headYaw = Mth.wrapDegrees((float)(Mth.rotLerp((float)pt, (float)pl.yHeadRotO, (float)pl.yHeadRot) - bodyDeg));
        float headPitch = Mth.lerp((float)pt, (float)pl.xRotO, (float)pl.getXRot());
        model.head.yRot = (float)Math.toRadians(Mth.clamp((float)headYaw, (float)-75.0f, (float)75.0f));
        model.head.xRot = (float)Math.toRadians(Mth.clamp((float)headPitch, (float)-60.0f, (float)60.0f));
        model.head.zRot = -0.04f * (float)Math.sin(t * 7.0);
        int light = mc.getEntityRenderDispatcher().getPackedLightCoords((Entity)pl, pt);
        ps.pushPose();
        ps.translate(x, y, z);
        ps.rotate((Quaternionfc)Axis.YP.rotationDegrees(180.0f - bodyDeg));
        ps.scale(-0.3f, -0.3f, 0.3f);
        ps.translate(0.0f, -0.75f, 0.0f);
        RenderType type = RenderTypes.entityTranslucent((Identifier)skinTex);
        model.renderToBuffer(ps, buffers.getBuffer(type), light, OverlayTexture.NO_OVERLAY, -1);
        ps.popPose();
        if (buffers instanceof LegacyBufferSource.BufferSource) {
            LegacyBufferSource.BufferSource bs = (LegacyBufferSource.BufferSource)buffers;
            bs.endBatch(type);
        }
    }

    private static PlayerModel model(boolean isSlim) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (isSlim) {
                if (slim == null) {
                    slim = new PlayerModel(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER_SLIM), true);
                }
                return slim;
            }
            if (wide == null) {
                wide = new PlayerModel(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER), false);
            }
            return wide;
        }
        catch (Throwable e) {
            return null;
        }
    }

    private static void ensureSkin(String name) {
        long now;
        if (name.equals(loadedName) || name.equals(loadingName)) {
            return;
        }
        if (!name.equals(lastWanted)) {
            lastWanted = name;
            tries = 0;
            nextTry = 0L;
            ready = false;
        }
        if ((now = System.currentTimeMillis()) < nextTry || tries >= 5) {
            return;
        }
        ++tries;
        loadingName = name;
        Thread th = new Thread(() -> {
            block5: {
                try {
                    Http.Response r = Http.get("https://api.mojang.com/users/profiles/minecraft/" + Http.enc(name), null);
                    if (r.code() != 200) {
                        throw new IllegalStateException("profile " + r.code());
                    }
                    String id = JsonParser.parseString((String)r.body()).getAsJsonObject().get("id").getAsString();
                    Http.Response s = Http.get("https://sessionserver.mojang.com/session/minecraft/profile/" + id, null);
                    if (s.code() != 200) {
                        throw new IllegalStateException("session " + s.code());
                    }
                    String b64 = JsonParser.parseString((String)s.body()).getAsJsonObject().getAsJsonArray("properties").get(0).getAsJsonObject().get("value").getAsString();
                    JsonObject skin = JsonParser.parseString((String)new String(Base64.getDecoder().decode(b64), StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("textures").getAsJsonObject("SKIN");
                    String url = skin.get("url").getAsString().replace("http://", "https://");
                    boolean isSlim = skin.has("metadata") && "slim".equals(skin.getAsJsonObject("metadata").get("model").getAsString());
                    byte[] png = Http.getBytes(url);
                    if (png == null) {
                        throw new IllegalStateException("skin download");
                    }
                    NativeImage img = Cosmetics.legacy(NativeImage.read((byte[])png));
                    Minecraft.getInstance().execute(() -> {
                        Minecraft.getInstance().getTextureManager().register(SKIN, (AbstractTexture)new DynamicTexture(() -> "lyricsflow buddy skin", img));
                        slimSkin = isSlim;
                        loadedName = name;
                        ready = true;
                        if (name.equals(loadingName)) {
                            loadingName = null;
                        }
                    });
                }
                catch (Throwable e) {
                    SkyLyrics.LOG.warn("[SkyLyrics] Could not load the skin of {}: {}", (Object)name, (Object)e.toString());
                    nextTry = System.currentTimeMillis() + 20000L;
                    if (!name.equals(loadingName)) break block5;
                    loadingName = null;
                }
            }
        }, "SkyLyrics skin");
        th.setDaemon(true);
        th.start();
    }

    private static NativeImage legacy(NativeImage in) {
        int xx;
        int yy;
        if (in.getHeight() != 32) {
            return in;
        }
        NativeImage out = new NativeImage(64, 64, true);
        for (yy = 0; yy < 32; ++yy) {
            for (xx = 0; xx < 64; ++xx) {
                out.setPixel(xx, yy, in.getPixel(xx, yy));
            }
        }
        for (yy = 0; yy < 16; ++yy) {
            for (xx = 0; xx < 16; ++xx) {
                out.setPixel(16 + xx, 48 + yy, in.getPixel(xx, 16 + yy));
                out.setPixel(32 + xx, 48 + yy, in.getPixel(40 + xx, 16 + yy));
            }
        }
        in.close();
        return out;
    }
}

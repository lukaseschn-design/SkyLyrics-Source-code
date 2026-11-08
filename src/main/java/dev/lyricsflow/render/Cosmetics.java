package dev.lyricsflow.render;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.lyricsflow.LyricsFlow;
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
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_12249;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1921;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4050;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_5602;
import net.minecraft.class_591;
import net.minecraft.class_7833;
import org.joml.Quaternionfc;

public final class Cosmetics {
    public static final String[] IDS = new String[]{"none", "kuz3y_wqq", "astronaut_helmet", "axolotl", "snorkel", "cat_ears", "pixel_glasses", "halo", "hair_bow", "cap_back", "devil_horns", "viking", "cat_headphones", "straw_hat", "pirate_hat", "spartan", "dragon_wings", "red_dragon_wings", "bat_wings", "captain_hat", "knight_helmet", "top_hat", "witch_hat", "jack_o_lantern", "cowboy_hat", "snapback", "ninja_band", "traffic_cone"};
    private static final float SCALE = 0.3f;
    private static final class_2960 SKIN = LyricsFlow.id("cosmetic/buddy_skin");
    static final class_2960 KUZ3Y = LyricsFlow.id("textures/cosmetic/kuz3y_wqq.png");
    private static final Map<Integer, class_591> BUDDIES = new HashMap<Integer, class_591>();
    private static class_591 wide;
    private static class_591 slim;
    private static String loadedName;
    private static String loadingName;
    private static boolean slimSkin;
    private static boolean ready;
    private static long nextTry;
    private static int tries;
    private static String lastWanted;

    static class_591 buddyFor(int entityId, double t, float headYaw, float headPitch) {
        class_591 model;
        try {
            if (BUDDIES.size() > 32) {
                BUDDIES.clear();
            }
            model = BUDDIES.computeIfAbsent(entityId, k -> new class_591(class_310.method_1551().method_31974().method_32072(class_5602.field_27581), true));
        }
        catch (Throwable e) {
            return null;
        }
        Cosmetics.pose(model, t);
        model.field_3398.field_3675 = 0.0f;
        model.field_3398.field_3654 = (float)Math.toRadians(class_3532.method_15363((float)headPitch, (float)-60.0f, (float)60.0f));
        model.field_3398.field_3674 = -0.04f * (float)Math.sin(t * 7.0);
        return model;
    }

    static class_591 buddy(double t) {
        class_591 model = Cosmetics.model(true);
        if (model == null) {
            return null;
        }
        Cosmetics.pose(model, t);
        return model;
    }

    private static void pose(class_591 model, double t) {
        model.method_63514();
        model.method_2805(true);
        model.field_3392.field_3654 = -1.35f;
        model.field_3392.field_3675 = 0.18f;
        model.field_3397.field_3654 = -1.35f;
        model.field_3397.field_3675 = -0.18f;
        model.field_3401.field_3654 = -0.15f;
        model.field_3401.field_3674 = 2.55f + 0.38f * (float)Math.sin(t * 7.0);
        model.field_27433.field_3654 = -0.55f;
        model.field_27433.field_3674 = -0.12f;
        model.field_3398.field_3675 = 0.25f * (float)Math.sin(t * 0.9);
        model.field_3398.field_3654 = 0.08f + 0.05f * (float)Math.sin(t * 1.3);
        model.field_3398.field_3674 = -0.06f * (float)Math.sin(t * 7.0);
    }

    private Cosmetics() {
    }

    public static void render(class_4587 ps, class_4597 buffers) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || buffers == null) {
            return;
        }
        for (class_1657 pl : mc.field_1687.method_18456()) {
            if (!Cosmetics.wornBy(pl).contains("kuz3y_wqq") || pl == mc.field_1724 && mc.field_1690.method_31044().method_31034() || pl.method_5707(mc.field_1773.method_19418().method_71156()) > 4096.0) continue;
            Cosmetics.renderOne(ps, buffers, pl, "kuz3y_wqq");
        }
    }

    public static List<String> wornBy(class_1657 p) {
        class_310 mc = class_310.method_1551();
        if (!ConfigManager.get().enabled) {
            return List.of();
        }
        if (p == mc.field_1724) {
            return ConfigManager.get().worn();
        }
        String s = CosmeticSync.of(p.method_5667());
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
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null) {
            return List.of();
        }
        if (mc.field_1724 != null && entityId == mc.field_1724.method_5628()) {
            return Cosmetics.wornBy((class_1657)mc.field_1724);
        }
        class_1297 class_12972 = mc.field_1687.method_8469(entityId);
        if (class_12972 instanceof class_1657) {
            class_1657 p = (class_1657)class_12972;
            list = Cosmetics.wornBy(p);
        } else {
            list = List.of();
        }
        return list;
    }

    public static class_1657 playerOf(int entityId) {
        class_1657 p;
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null) {
            return null;
        }
        class_1297 class_12972 = mc.field_1687.method_8469(entityId);
        return class_12972 instanceof class_1657 ? (p = (class_1657)class_12972) : null;
    }

    private static void renderOne(class_4587 ps, class_4597 buffers, class_1657 pl, String name) {
        double top;
        boolean isSlim;
        class_2960 skinTex;
        class_310 mc = class_310.method_1551();
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
        if (pl.method_5767()) {
            return;
        }
        class_4050 pose = pl.method_18376();
        if (pose != class_4050.field_18076 && pose != class_4050.field_18081) {
            return;
        }
        class_591 model = Cosmetics.model(isSlim);
        if (model == null) {
            return;
        }
        float pt = mc.method_61966().method_60637(false);
        class_243 cam = mc.field_1773.method_19418().method_71156();
        class_243 p = pl.method_30950(pt);
        float bodyDeg = class_3532.method_17821((float)pt, (float)pl.field_6220, (float)pl.field_6283);
        double body = Math.toRadians(bodyDeg);
        double t = Anim.seconds();
        double side = 0.38;
        double d = top = pl.method_18276() ? 1.12 : 1.4;
        if (!pl.method_6118(class_1304.field_6174).method_7960()) {
            side += 0.06;
            top += 0.09;
        }
        double x = p.field_1352 - cam.field_1352 - Math.cos(body) * side;
        double y = p.field_1351 - cam.field_1351 + top;
        double z = p.field_1350 - cam.field_1350 - Math.sin(body) * side;
        model.method_63514();
        model.method_2805(true);
        model.field_3392.field_3654 = -1.35f;
        model.field_3392.field_3675 = 0.18f;
        model.field_3397.field_3654 = -1.35f;
        model.field_3397.field_3675 = -0.18f;
        model.field_3401.field_3654 = -0.15f;
        model.field_3401.field_3674 = 2.55f + 0.38f * (float)Math.sin(t * 7.0);
        model.field_27433.field_3654 = -0.55f;
        model.field_27433.field_3674 = -0.12f;
        float headYaw = class_3532.method_15393((float)(class_3532.method_17821((float)pt, (float)pl.field_6259, (float)pl.field_6241) - bodyDeg));
        float headPitch = class_3532.method_16439((float)pt, (float)pl.field_6004, (float)pl.method_36455());
        model.field_3398.field_3675 = (float)Math.toRadians(class_3532.method_15363((float)headYaw, (float)-75.0f, (float)75.0f));
        model.field_3398.field_3654 = (float)Math.toRadians(class_3532.method_15363((float)headPitch, (float)-60.0f, (float)60.0f));
        model.field_3398.field_3674 = -0.04f * (float)Math.sin(t * 7.0);
        int light = mc.method_1561().method_23839((class_1297)pl, pt);
        ps.method_22903();
        ps.method_22904(x, y, z);
        ps.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees(180.0f - bodyDeg));
        ps.method_22905(-0.3f, -0.3f, 0.3f);
        ps.method_46416(0.0f, -0.75f, 0.0f);
        class_1921 type = class_12249.method_76000((class_2960)skinTex);
        model.method_60879(ps, buffers.method_73477(type), light, class_4608.field_21444);
        ps.method_22909();
        if (buffers instanceof class_4597.class_4598) {
            class_4597.class_4598 bs = (class_4597.class_4598)buffers;
            bs.method_22994(type);
        }
    }

    private static class_591 model(boolean isSlim) {
        try {
            class_310 mc = class_310.method_1551();
            if (isSlim) {
                if (slim == null) {
                    slim = new class_591(mc.method_31974().method_32072(class_5602.field_27581), true);
                }
                return slim;
            }
            if (wide == null) {
                wide = new class_591(mc.method_31974().method_32072(class_5602.field_27577), false);
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
                    class_1011 img = Cosmetics.legacy(class_1011.method_49277((byte[])png));
                    class_310.method_1551().execute(() -> {
                        class_310.method_1551().method_1531().method_4616(SKIN, (class_1044)new class_1043(() -> "lyricsflow buddy skin", img));
                        slimSkin = isSlim;
                        loadedName = name;
                        ready = true;
                        if (name.equals(loadingName)) {
                            loadingName = null;
                        }
                    });
                }
                catch (Throwable e) {
                    LyricsFlow.LOG.warn("[SkyLyrics] Could not load the skin of {}: {}", (Object)name, (Object)e.toString());
                    nextTry = System.currentTimeMillis() + 20000L;
                    if (!name.equals(loadingName)) break block5;
                    loadingName = null;
                }
            }
        }, "SkyLyrics skin");
        th.setDaemon(true);
        th.start();
    }

    private static class_1011 legacy(class_1011 in) {
        int xx;
        int yy;
        if (in.method_4323() != 32) {
            return in;
        }
        class_1011 out = new class_1011(64, 64, true);
        for (yy = 0; yy < 32; ++yy) {
            for (xx = 0; xx < 64; ++xx) {
                out.method_61941(xx, yy, in.method_61940(xx, yy));
            }
        }
        for (yy = 0; yy < 16; ++yy) {
            for (xx = 0; xx < 16; ++xx) {
                out.method_61941(16 + xx, 48 + yy, in.method_61940(xx, 16 + yy));
                out.method_61941(32 + xx, 48 + yy, in.method_61940(40 + xx, 16 + yy));
            }
        }
        in.close();
        return out;
    }
}


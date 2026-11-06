/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.class_10000
 *  net.minecraft.class_10055
 *  net.minecraft.class_11659
 *  net.minecraft.class_12249
 *  net.minecraft.class_1304
 *  net.minecraft.class_1657
 *  net.minecraft.class_1921
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_3883
 *  net.minecraft.class_3887
 *  net.minecraft.class_4587
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_4588
 *  net.minecraft.class_4608
 *  net.minecraft.class_5602
 *  net.minecraft.class_5762$class_5767
 *  net.minecraft.class_5772
 *  net.minecraft.class_591
 *  net.minecraft.class_630
 *  net.minecraft.class_7833
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
import net.minecraft.class_10000;
import net.minecraft.class_10055;
import net.minecraft.class_11659;
import net.minecraft.class_12249;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4608;
import net.minecraft.class_5602;
import net.minecraft.class_5762;
import net.minecraft.class_5772;
import net.minecraft.class_591;
import net.minecraft.class_630;
import net.minecraft.class_7833;
import org.joml.Quaternionfc;

public final class CosmeticLayer
extends class_3887<class_10055, class_591> {
    private static final class_2960 WHITE = SkyLyrics.id("textures/misc/white.png");
    private static final float PX = 0.0625f;
    static final float HELMET_FIT = 1.17f;
    private static Map<String, List<Group>> all;
    private static final class_2960 RING;
    private static final class_2960 GLOW;
    private static final class_2960 AXOLOTL_BLUE;
    private static class_5772 axolotlModel;
    private static final class_10000 AXO_STATE;
    private static final float AXOLOTL_SCALE = 0.5f;

    public CosmeticLayer(class_3883<class_10055, class_591> parent) {
        super(parent);
    }

    public void submit(class_4587 pose, class_11659 queue, int light, class_10055 state, float yRot, float xRot) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || state.field_53333 || state.field_53542) {
            return;
        }
        class_1657 owner = Cosmetics.playerOf(state.field_53528);
        if (owner == null) {
            return;
        }
        for (String cos : Cosmetics.forEntity(state.field_53528)) {
            this.one(pose, queue, light, state, yRot, xRot, owner, cos);
        }
    }

    private void one(class_4587 pose, class_11659 queue, int light, class_10055 state, float yRot, float xRot, class_1657 owner, String cos) {
        List<Group> groups;
        if ("axolotl".equals(cos)) {
            this.axolotl(pose, queue, light, owner);
            return;
        }
        if ("kuz3y_wqq".equals(cos)) {
            this.buddy(pose, queue, light, owner, state.field_53528, yRot, xRot);
            return;
        }
        if (all == null) {
            CosmeticLayer.load();
        }
        if ((groups = all.get(cos)) == null) {
            return;
        }
        double t = (double)System.nanoTime() / 1.0E9 % 3600.0;
        class_591 model = (class_591)this.method_17165();
        boolean helmetOn = !owner.method_6118(class_1304.field_6169).method_7960();
        for (Group g : groups) {
            String a;
            class_630 part;
            class_630 class_6302 = part = "body".equals(g.part()) ? model.field_3391 : model.field_3398;
            if (!part.field_3665) continue;
            pose.method_22903();
            part.method_22703(pose);
            float lift = g.lift();
            if ("head".equals(g.part()) && helmetOn) {
                lift *= 1.17f;
            }
            if (lift != 1.0f) {
                pose.method_46416(0.0f, -0.25f, 0.0f);
                pose.method_22905(lift, lift, lift);
                pose.method_46416(0.0f, 0.25f, 0.0f);
            }
            if ((a = g.anim()).equals("bob")) {
                pose.method_46416(0.0f, -((float)Math.sin(t * 2.2)) * 0.6f * 0.0625f, 0.0f);
            } else if (a.startsWith("sway:")) {
                String[] q = a.split(":");
                float deg = Float.parseFloat(q[1]) * (float)Math.sin(t * 2.4);
                float py = Float.parseFloat(q[3]) * 0.0625f;
                float pz = Float.parseFloat(q[4]) * 0.0625f;
                pose.method_46416(0.0f, py, pz);
                pose.method_22907((Quaternionfc)(switch (q[2]) {
                    case "x" -> class_7833.field_40714.rotationDegrees(deg);
                    case "y" -> class_7833.field_40716.rotationDegrees(deg);
                    default -> class_7833.field_40718.rotationDegrees(deg);
                }));
                pose.method_46416(0.0f, -py, -pz);
            } else if (a.startsWith("flap:")) {
                int side = Integer.parseInt(a.substring(5));
                float beat = (float)(0.5 + 0.5 * Math.sin(t * 2.6));
                float deg = (float)(-side) * (6.0f + 30.0f * beat);
                pose.method_46416(0.0f, 0.0f, 0.21875f);
                pose.method_22907((Quaternionfc)class_7833.field_40716.rotationDegrees(deg));
                pose.method_22907((Quaternionfc)class_7833.field_40718.rotationDegrees((float)side * 6.0f * (beat - 0.5f)));
                pose.method_46416(0.0f, 0.0f, -0.21875f);
            }
            class_1921 type = g.alpha() < 255 && !g.glow() ? class_12249.method_76000((class_2960)WHITE) : class_12249.method_75994((class_2960)WHITE);
            int lt = g.glow() ? 0xF000F0 : light;
            queue.method_73483(pose, type, (p, vc) -> CosmeticLayer.emit(vc, p, g, lt));
            pose.method_22909();
        }
        if ("halo".equals(cos)) {
            this.haloGlow(pose, queue, helmetOn, t);
        }
    }

    private void haloGlow(class_4587 pose, class_11659 queue, boolean helmetOn, double t) {
        class_630 head = ((class_591)this.method_17165()).field_3398;
        if (!head.field_3665) {
            return;
        }
        pose.method_22903();
        head.method_22703(pose);
        float lift = helmetOn ? 1.2636f : 1.08f;
        pose.method_46416(0.0f, -0.25f, 0.0f);
        pose.method_22905(lift, lift, lift);
        pose.method_46416(0.0f, 0.25f, 0.0f);
        pose.method_46416(0.015625f, -0.6875f - (float)Math.sin(t * 2.2) * 0.6f * 0.0625f, 0.015625f);
        float pulse = 0.82f + 0.18f * (float)Math.sin(t * 3.1);
        int ring = (int)(235.0f * pulse) << 24 | 0xFFD54A;
        int soft = (int)(120.0f * pulse) << 24 | 0xFFC23A;
        queue.method_73483(pose, class_12249.method_76014((class_2960)RING), (p, vc) -> CosmeticLayer.flat(vc, p, 0.45f, ring));
        queue.method_73483(pose, class_12249.method_76014((class_2960)GLOW), (p, vc) -> CosmeticLayer.flat(vc, p, 0.6875f, soft));
        pose.method_22909();
    }

    private static void flat(class_4588 vc, class_4587.class_4665 p, float r, int col) {
        int k;
        float[][] c = new float[][]{{-r, -r, 0.0f, 0.0f}, {r, -r, 1.0f, 0.0f}, {r, r, 1.0f, 1.0f}, {-r, r, 0.0f, 1.0f}};
        for (k = 0; k < 4; ++k) {
            CosmeticLayer.fv(vc, p, c[k][0], c[k][1], c[k][2], c[k][3], col);
        }
        for (k = 3; k >= 0; --k) {
            CosmeticLayer.fv(vc, p, c[k][0], c[k][1], c[k][2], c[k][3], col);
        }
    }

    private static void fv(class_4588 vc, class_4587.class_4665 p, float x, float z, float u, float v, int col) {
        vc.method_56824(p, x, 0.0f, z).method_39415(col).method_22913(u, v).method_22922(class_4608.field_21444).method_60803(0xF000F0).method_60831(p, 0.0f, -1.0f, 0.0f);
    }

    private void axolotl(class_4587 pose, class_11659 queue, int light, class_1657 owner) {
        class_630 head = ((class_591)this.method_17165()).field_3398;
        if (!head.field_3665) {
            return;
        }
        try {
            if (axolotlModel == null) {
                axolotlModel = new class_5772(class_310.method_1551().method_31974().method_32072(class_5602.field_28383));
            }
            AxolotlRenderStateSetup.apply(AXO_STATE, (float)((double)System.nanoTime() / 1.0E9 % 3600.0 * 20.0));
            axolotlModel.method_33293(AXO_STATE);
            pose.method_22903();
            head.method_22703(pose);
            boolean helm = !owner.method_6118(class_1304.field_6169).method_7960();
            pose.method_46416(0.0f, (helm ? -9.2f : -8.0f) * 0.0625f, 0.0f);
            pose.method_22905(0.5f, 0.5f, 0.5f);
            pose.method_46416(0.0f, -1.375f, 0.0f);
            queue.method_73491(axolotlModel.method_63512(), pose, class_12249.method_75994((class_2960)AXOLOTL_BLUE), light, class_4608.field_21444, null);
            pose.method_22909();
        }
        catch (Throwable e) {
            SkyLyrics.LOG.warn("[SkyLyrics] Axolotl cosmetic failed: {}", (Object)e.toString());
        }
    }

    private void buddy(class_4587 pose, class_11659 queue, int light, class_1657 owner, int id, float headYaw, float headPitch) {
        class_630 body = ((class_591)this.method_17165()).field_3391;
        if (!body.field_3665) {
            return;
        }
        class_591 b = Cosmetics.buddyFor(id, (double)System.nanoTime() / 1.0E9 % 3600.0, headYaw, headPitch);
        if (b == null) {
            return;
        }
        boolean chest = !owner.method_6118(class_1304.field_6174).method_7960();
        pose.method_22903();
        body.method_22703(pose);
        pose.method_46416((chest ? -6.9f : -6.2f) * 0.0625f, (chest ? -1.2f : -0.2f) * 0.0625f, 0.0f);
        float k = 0.32f;
        pose.method_22905(k, k, k);
        pose.method_46416(0.0f, -0.75f, 0.0f);
        pose.method_22907((Quaternionfc)class_7833.field_40716.rotation((float)Math.toRadians(class_3532.method_15363((float)headYaw, (float)-80.0f, (float)80.0f))));
        queue.method_73491(b.method_63512(), pose, class_12249.method_76000((class_2960)Cosmetics.KUZ3Y), light, class_4608.field_21444, null);
        pose.method_22909();
    }

    private static void emit(class_4588 vc, class_4587.class_4665 p, Group g, int light) {
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

    private static void v(class_4588 vc, class_4587.class_4665 p, float x, float y, float z, int col, int light, float nx, float ny, float nz) {
        vc.method_56824(p, x, y, z).method_39415(col).method_22913(0.5f, 0.5f).method_22922(class_4608.field_21444).method_60803(light).method_60831(p, nx, ny, nz);
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
        AXOLOTL_BLUE = class_2960.method_60656((String)"textures/entity/axolotl/axolotl_blue.png");
        AXO_STATE = new class_10000();
    }

    private record Group(String part, String anim, boolean glow, int alpha, float[] quads, float lift) {
    }

    private static final class AxolotlRenderStateSetup {
        private AxolotlRenderStateSetup() {
        }

        static void apply(class_10000 s, float age) {
            s.field_53260 = class_5762.class_5767.field_28345;
            s.field_53264 = 1.0f;
            s.field_53263 = 0.0f;
            s.field_53262 = 0.0f;
            s.field_53261 = 0.0f;
            s.field_53328 = age;
            s.field_53448 = 0.0f;
            s.field_53447 = 0.0f;
            s.field_53450 = 0.0f;
            s.field_53451 = 0.0f;
        }
    }
}


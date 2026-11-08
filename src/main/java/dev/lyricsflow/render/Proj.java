/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_4184
 *  org.joml.Vector3fc
 */
package dev.lyricsflow.render;

import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import org.joml.Vector3fc;

public final class Proj {
    public static volatile float fovFromRenderer = Float.NaN;
    public static double cx;
    public static double cy;
    public static double cz;
    public static float yaw;
    public static float pitch;
    public static float width;
    public static float height;
    private static double fx;
    private static double fy;
    private static double fz;
    private static double rx;
    private static double ry;
    private static double rz;
    private static double ux;
    private static double uy;
    private static double uz;
    private static double focal;
    private static double focalBase;

    private Proj() {
    }

    public static void update(float guiWidth, float guiHeight) {
        class_310 mc = class_310.method_1551();
        class_4184 cam = mc.field_1773.method_19418();
        class_243 p = cam.method_71156();
        cx = p.field_1352;
        cy = p.field_1351;
        cz = p.field_1350;
        yaw = cam.method_19330();
        pitch = cam.method_19329();
        width = guiWidth;
        height = guiHeight;
        Vector3fc f = cam.method_19335();
        Vector3fc u = cam.method_19336();
        Vector3fc l = cam.method_35689();
        fx = f.x();
        fy = f.y();
        fz = f.z();
        ux = u.x();
        uy = u.y();
        uz = u.z();
        rx = -l.x();
        ry = -l.y();
        rz = -l.z();
        float fov = fovFromRenderer;
        if (Float.isNaN(fov) || fov < 1.0f || fov > 179.0f) {
            fov = ((Integer)mc.field_1690.method_41808().method_41753()).intValue();
        }
        focal = (double)guiHeight / 2.0 / Math.tan(Math.toRadians(fov) / 2.0);
        focalBase = (double)guiHeight / 2.0 / Math.tan(Math.toRadians(((Integer)mc.field_1690.method_41808().method_41753()).intValue()) / 2.0);
    }

    public static double focal() {
        return focal;
    }

    public static double zoomComp() {
        return focal <= 0.0 ? 1.0 : Math.pow(focalBase / focal, 0.85);
    }

    public static double[] dir(float yawDeg, float pitchDeg) {
        double yr = Math.toRadians(yawDeg);
        double pr = Math.toRadians(pitchDeg);
        return new double[]{-Math.sin(yr) * Math.cos(pr), -Math.sin(pr), Math.cos(yr) * Math.cos(pr)};
    }

    public static boolean projectRel(double dx, double dy, double dz, double[] out) {
        double z = dx * fx + dy * fy + dz * fz;
        if (z < 0.05) {
            return false;
        }
        double x = dx * rx + dy * ry + dz * rz;
        double y = dx * ux + dy * uy + dz * uz;
        double k = focal / z;
        out[0] = (double)width / 2.0 + x * k;
        out[1] = (double)height / 2.0 - y * k;
        out[2] = z;
        out[3] = k;
        return true;
    }

    public static void unproject(float gx, float gy, float depth, float[] out) {
        double a = ((double)gx - (double)width / 2.0) / focal;
        double b = -((double)gy - (double)height / 2.0) / focal;
        out[0] = (float)((fx + a * rx + b * ux) * (double)depth);
        out[1] = (float)((fy + a * ry + b * uy) * (double)depth);
        out[2] = (float)((fz + a * rz + b * uz) * (double)depth);
    }

    public static boolean project(double wx, double wy, double wz, double[] out) {
        return Proj.projectRel(wx - cx, wy - cy, wz - cz, out);
    }

    public static boolean onScreen(double[] p, double margin) {
        return p[0] > -margin && p[0] < (double)width + margin && p[1] > -margin && p[1] < (double)height + margin;
    }

    static {
        focalBase = 1.0;
    }
}


package dev.lyricsflow.util;

public final class Anim {
    private static long lastFrame = System.nanoTime();
    private static float dt = 0.016f;
    private static long frameId;
    private static boolean driven;

    private Anim() {
    }

    public static void frame() {
        if (driven) {
            return;
        }
        Anim.tick();
    }

    public static void beginFrame() {
        driven = true;
        Anim.tick();
    }

    private static void tick() {
        long now = System.nanoTime();
        if (now - lastFrame < 1000000L) {
            return;
        }
        dt = Math.min(0.1f, (float)(now - lastFrame) / 1.0E9f);
        lastFrame = now;
        ++frameId;
    }

    public static float dt() {
        return dt;
    }

    public static long frameId() {
        return frameId;
    }

    public static double seconds() {
        return (double)System.nanoTime() / 1.0E9;
    }

    public static float approach(float current, float target, float speed) {
        float k = 1.0f - (float)Math.exp(-speed * dt);
        float v = current + (target - current) * k;
        return Math.abs(target - v) < 5.0E-4f ? target : v;
    }

    public static float clamp01(float t) {
        return t < 0.0f ? 0.0f : (t > 1.0f ? 1.0f : t);
    }

    public static float easeOutCubic(float t) {
        t = Anim.clamp01(t);
        float f = 1.0f - t;
        return 1.0f - f * f * f;
    }

    public static float easeInCubic(float t) {
        t = Anim.clamp01(t);
        return t * t * t;
    }

    public static float easeInOut(float t) {
        return (t = Anim.clamp01(t)) < 0.5f ? 4.0f * t * t * t : 1.0f - (float)Math.pow(-2.0f * t + 2.0f, 3.0) / 2.0f;
    }

    public static float easeOutBack(float t) {
        t = Anim.clamp01(t);
        float c1 = 1.70158f;
        float c3 = c1 + 1.0f;
        return 1.0f + c3 * (float)Math.pow(t - 1.0f, 3.0) + c1 * (float)Math.pow(t - 1.0f, 2.0);
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static float progress(double t, double a, double b) {
        if (b <= a) {
            return t >= b ? 1.0f : 0.0f;
        }
        return Anim.clamp01((float)((t - a) / (b - a)));
    }

    public static float hash01(long seed) {
        long x = seed * -7046029254386353131L;
        x ^= x >>> 31;
        x *= -4658895280553007687L;
        x ^= x >>> 27;
        return (float)(x >>> 40) / 1.6777216E7f;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 */
package dev.lyricsflow.render;

import java.lang.reflect.Method;
import net.fabricmc.loader.api.FabricLoader;

public final class IrisCompat {
    private static final boolean PRESENT = FabricLoader.getInstance().isModLoaded("iris");
    private static Object api;
    private static Method inUse;
    private static boolean broken;
    private static long nextCheck;
    private static boolean cached;

    private IrisCompat() {
    }

    public static boolean shadersOn() {
        if (!PRESENT || broken) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (now < nextCheck) {
            return cached;
        }
        nextCheck = now + 1000L;
        try {
            if (inUse == null) {
                Class<?> c = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                api = c.getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
                inUse = c.getMethod("isShaderPackInUse", new Class[0]);
            }
            cached = (Boolean)inUse.invoke(api, new Object[0]);
        }
        catch (Throwable t) {
            broken = true;
            cached = false;
        }
        return cached;
    }
}


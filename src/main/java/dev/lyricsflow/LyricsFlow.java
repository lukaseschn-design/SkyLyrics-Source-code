/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2960
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package dev.lyricsflow;

import net.minecraft.class_2960;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LyricsFlow {
    public static final String MOD_ID = "lyricsflow";
    public static final String NAME = "SkyLyrics";
    public static final String VERSION_LABEL = "v1.0";
    public static final String CREDIT_DC = "dc: i.am.maximus";
    public static final String SERVER_URL = "";
    public static final String CREDIT_SERVER = "";
    public static final Logger LOG = LoggerFactory.getLogger((String)"SkyLyrics");

    private LyricsFlow() {
    }

    public static class_2960 id(String path) {
        return class_2960.method_60655((String)MOD_ID, (String)path);
    }
}


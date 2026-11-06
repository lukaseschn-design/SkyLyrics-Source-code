/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.client.Minecraft
 */
package dev.lyricsflow.app.hud;

import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;

public final class Island {
    private Island() {
    }

    public static void notify(String text, boolean ok) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui != null) {
            mc.gui.hud.setOverlayMessage((Component)Component.literal((String)text), false);
        }
    }
}

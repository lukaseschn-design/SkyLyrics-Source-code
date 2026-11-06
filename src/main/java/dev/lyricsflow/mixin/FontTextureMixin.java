/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.renderpearl.api.textures.FilterMode
 *  com.mojang.blaze3d.platform.NativeImage
 *  com.mojang.blaze3d.platform.NativeImage$Format
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.gui.font.FontTexture
 *  net.minecraft.client.gui.font.GlyphRenderTypes
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.lyricsflow.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import java.util.function.Supplier;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.gui.font.FontTexture;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={FontTexture.class})
public abstract class FontTextureMixin
extends AbstractTexture {
    @Inject(method={"<init>"}, at={@At(value="TAIL")}, require=0)
    private void lyricsflow$smooth(Supplier<String> name, GlyphRenderTypes types, boolean colored, CallbackInfo ci) {
        block7: {
            try {
                String n = name.get();
                if (n == null || !n.startsWith("lyricsflow:") || n.contains("badge")) break block7;
                this.sampler = RenderSystem.getSamplerCache().getRepeat(FilterMode.LINEAR);
                try (NativeImage blank = new NativeImage(colored ? NativeImage.Format.RGBA : NativeImage.Format.LUMINANCE, 256, 256, true);){
                    RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.getTexture(), blank);
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }
}

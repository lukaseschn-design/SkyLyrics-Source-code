/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  com.mojang.renderpearl.api.pipeline.RenderPipeline
 *  net.minecraft.resources.Identifier
 *  net.minecraft.client.gui.Gui
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package dev.lyricsflow.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.lyricsflow.render.HotbarStyle;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={Gui.class})
public abstract class GuiHotbarMixin {
    @WrapOperation(method={"renderItemHotbar"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V")})
    private void lyricsflow$hotbar(GuiGraphicsExtractor g, RenderPipeline pipeline, Identifier id, int x, int y, int w, int h, Operation<Void> original) {
        if (!HotbarStyle.draw(g, id, x, y, w, h)) {
            original.call(new Object[]{g, pipeline, id, x, y, w, h});
        }
    }
}

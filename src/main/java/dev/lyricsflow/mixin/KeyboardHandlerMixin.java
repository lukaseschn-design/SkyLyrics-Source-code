/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.network.chat.Component
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  net.minecraft.client.KeyboardHandler
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package dev.lyricsflow.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.lyricsflow.render.ScreenshotHelper;
import java.io.File;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={KeyboardHandler.class})
public abstract class KeyboardHandlerMixin {
    @WrapOperation(method={"keyPress"}, require=0, at={@At(value="INVOKE", target="Lnet/minecraft/Screenshot;grab(Ljava/io/File;Lnet/minecraft/RenderTarget;Ljava/util/function/Consumer;)V")})
    private void lyricsflow$delayScreenshot(File dir, RenderTarget target, Consumer<Component> done, Operation<Void> original) {
        if (!ScreenshotHelper.defer(() -> original.call(new Object[]{dir, target, done}))) {
            original.call(new Object[]{dir, target, done});
        }
    }
}


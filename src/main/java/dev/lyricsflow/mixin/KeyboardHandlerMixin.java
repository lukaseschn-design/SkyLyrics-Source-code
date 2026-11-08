/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.class_2561
 *  net.minecraft.class_276
 *  net.minecraft.class_309
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package dev.lyricsflow.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.lyricsflow.render.ScreenshotHelper;
import java.io.File;
import java.util.function.Consumer;
import net.minecraft.class_2561;
import net.minecraft.class_276;
import net.minecraft.class_309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={class_309.class})
public abstract class KeyboardHandlerMixin {
    @WrapOperation(method={"method_1466"}, require=0, at={@At(value="INVOKE", target="Lnet/minecraft/class_318;method_1659(Ljava/io/File;Lnet/minecraft/class_276;Ljava/util/function/Consumer;)V")})
    private void lyricsflow$delayScreenshot(File dir, class_276 target, Consumer<class_2561> done, Operation<Void> original) {
        if (!ScreenshotHelper.defer(() -> original.call(new Object[]{dir, target, done}))) {
            original.call(new Object[]{dir, target, done});
        }
    }
}


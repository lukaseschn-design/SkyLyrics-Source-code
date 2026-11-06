/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1065
 *  net.minecraft.class_3283
 *  net.minecraft.class_3285
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.lyricsflow.mixin;

import dev.lyricsflow.render.CustomFonts;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.class_1065;
import net.minecraft.class_3283;
import net.minecraft.class_3285;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_3283.class})
public abstract class PackRepositoryMixin {
    @Shadow
    @Final
    @Mutable
    private Set<class_3285> field_14227;

    @Inject(method={"<init>"}, at={@At(value="RETURN")}, require=0)
    private void lyricsflow$addFonts(class_3285[] given, CallbackInfo ci) {
        boolean client = false;
        for (class_3285 s : this.field_14227) {
            if (!(s instanceof class_1065)) continue;
            client = true;
        }
        if (!client) {
            return;
        }
        LinkedHashSet<class_3285> set = new LinkedHashSet<class_3285>(this.field_14227);
        set.add(CustomFonts.source());
        this.field_14227 = set;
    }
}


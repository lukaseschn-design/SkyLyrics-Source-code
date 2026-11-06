/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.resources.ClientPackSource
 *  net.minecraft.server.packs.repository.PackRepository
 *  net.minecraft.server.packs.repository.RepositorySource
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
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={PackRepository.class})
public abstract class PackRepositoryMixin {
    @Shadow
    @Final
    @Mutable
    private Set<RepositorySource> sources;

    @Inject(method={"<init>"}, at={@At(value="RETURN")}, require=0)
    private void lyricsflow$addFonts(RepositorySource[] given, CallbackInfo ci) {
        boolean client = false;
        for (RepositorySource s : this.sources) {
            if (!(s instanceof ClientPackSource)) continue;
            client = true;
        }
        if (!client) {
            return;
        }
        LinkedHashSet<RepositorySource> set = new LinkedHashSet<RepositorySource>(this.sources);
        set.add(CustomFonts.source());
        this.sources = set;
    }
}


package Yuki.lumiclient.keybinds;

import Yuki.lumiclient.mixin.KeyMappingAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class BindingCatalog {
    private final List<KeyMapping> mappings;

    public BindingCatalog(KeyMapping[] mappings) {
        this.mappings = List.copyOf(Arrays.asList(mappings));
    }

    public static InputConstants.Key getKey(KeyMapping mapping) {
        return ((KeyMappingAccessor) mapping).lumiclient$getKey();
    }

    public List<KeyMapping> onKey(InputConstants.Key key) {
        return this.mappings.stream()
                .filter(mapping -> !mapping.consumeClick() && getKey(mapping).equals(key))
                .toList();
    }

    public boolean conflict(KeyMapping mapping) {
        return !mapping.consumeClick() && this.mappings.stream().anyMatch(other -> other != mapping && !other.consumeClick() && mapping.same(other));
    }

    public long conflictKeys() {
        return this.mappings.stream()
                .filter(this::conflict)
                .map(mapping -> getKey(mapping).getName())
                .distinct()
                .count();
    }

    public List<KeyMapping> search(InputConstants.Key key, boolean onlyThisKey, String query) {
        String needle = query.strip().toLowerCase(Locale.ROOT);
        return this.mappings.stream()
                .filter(mapping -> !onlyThisKey || (!mapping.consumeClick() && getKey(mapping).equals(key)))
                .filter(mapping -> (name(mapping) + " " + category(mapping) + " " + mapping.getTranslatedKeyMessage().getString() + " " + mapping.getName()).toLowerCase(Locale.ROOT).contains(needle))
                .sorted(Comparator.comparing(BindingCatalog::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public static String name(KeyMapping mapping) {
        return Component.translatable(mapping.getName()).getString();
    }

    public static String category(KeyMapping mapping) {
        return Component.translatable(mapping.saveString()).getString();
    }

    public static void rebind(KeyMapping mapping, InputConstants.Key key) {
        mapping.setKey(key);
        refreshMappings();
    }

    public static void reset(KeyMapping mapping) {
        mapping.setKey(mapping.getDefaultKey());
        refreshMappings();
    }

    private static void refreshMappings() {
        KeyMapping.releaseAll();
        KeyMapping.setAll();
    }

    public static void save(KeyMapping mapping, InputConstants.Key key) {
        rebind(mapping, key);
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.options != null) {
            mc.options.save();
        }
    }

    public static void saveDefault(KeyMapping mapping) {
        reset(mapping);
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.options != null) {
            mc.options.save();
        }
    }
}

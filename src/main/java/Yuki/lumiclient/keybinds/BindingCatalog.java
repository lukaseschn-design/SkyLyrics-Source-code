package Yuki.lumiclient.keybinds;

import Yuki.lumiclient.mixin.KeyMappingAccessor;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class BindingCatalog {
    private final List<class_304> mappings;

    public BindingCatalog(class_304[] mappings) {
        this.mappings = List.copyOf(Arrays.asList(mappings));
    }

    public static class_3675.class_306 getKey(class_304 mapping) {
        return ((KeyMappingAccessor) mapping).lumiclient$getKey();
    }

    public List<class_304> onKey(class_3675.class_306 key) {
        return this.mappings.stream()
                .filter(mapping -> !mapping.method_1436() && getKey(mapping).equals(key))
                .toList();
    }

    public boolean conflict(class_304 mapping) {
        return !mapping.method_1436() && this.mappings.stream().anyMatch(other -> other != mapping && !other.method_1436() && mapping.method_1435(other));
    }

    public long conflictKeys() {
        return this.mappings.stream()
                .filter(this::conflict)
                .map(mapping -> getKey(mapping).method_1441())
                .distinct()
                .count();
    }

    public List<class_304> search(class_3675.class_306 key, boolean onlyThisKey, String query) {
        String needle = query.strip().toLowerCase(Locale.ROOT);
        return this.mappings.stream()
                .filter(mapping -> !onlyThisKey || (!mapping.method_1436() && getKey(mapping).equals(key)))
                .filter(mapping -> (name(mapping) + " " + category(mapping) + " " + mapping.method_16007().getString() + " " + mapping.method_1431()).toLowerCase(Locale.ROOT).contains(needle))
                .sorted(Comparator.comparing(BindingCatalog::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public static String name(class_304 mapping) {
        return class_2561.method_43469(mapping.method_1431()).getString();
    }

    public static String category(class_304 mapping) {
        return class_2561.method_43469(mapping.method_1428()).getString();
    }

    public static void rebind(class_304 mapping, class_3675.class_306 key) {
        mapping.method_1422(key);
        refreshMappings();
    }

    public static void reset(class_304 mapping) {
        mapping.method_1422(mapping.method_1429());
        refreshMappings();
    }

    private static void refreshMappings() {
        class_304.method_1437();
        class_304.method_1424();
    }

    public static void save(class_304 mapping, class_3675.class_306 key) {
        rebind(mapping, key);
        class_310 mc = class_310.method_1551();
        if (mc != null && mc.field_1690 != null) {
            mc.field_1690.method_1640();
        }
    }

    public static void saveDefault(class_304 mapping) {
        reset(mapping);
        class_310 mc = class_310.method_1551();
        if (mc != null && mc.field_1690 != null) {
            mc.field_1690.method_1640();
        }
    }
}

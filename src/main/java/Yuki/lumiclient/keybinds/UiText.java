package Yuki.lumiclient.keybinds;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.class_2561;
import net.minecraft.class_5250;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class UiText {
    private static final Map<String, String> ENGLISH = UiText.loadEnglish();

    private UiText() {
    }

    public static class_5250 tr(String key, Object... args) {
        String translationKey = "studiouskeybinds." + key;
        String fallback = ENGLISH.getOrDefault(translationKey, key.replace('_', ' '));
        if (args == null || args.length == 0) {
            return class_2561.method_48321(translationKey, fallback);
        }
        return class_2561.method_48322(translationKey, fallback, args);
    }

    private static Map<String, String> loadEnglish() {
        try (InputStream stream = UiText.class.getResourceAsStream("/assets/studiouskeybinds/lang/en_us.json")) {
            if (stream == null) {
                return Map.of();
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                HashMap<String, String> labels = new HashMap<>();
                JsonParser.parseReader((Reader) reader).getAsJsonObject().entrySet().forEach(entry -> {
                    labels.put(entry.getKey(), ((JsonElement) entry.getValue()).getAsString());
                });
                return Map.copyOf(labels);
            }
        } catch (Throwable t) {
            return Map.of();
        }
    }
}

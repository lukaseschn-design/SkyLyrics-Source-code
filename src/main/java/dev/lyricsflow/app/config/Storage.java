package dev.lyricsflow.app.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.lyricsflow.app.Core;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;

public final class Storage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private Storage() {
    }

    public static Path dir() {
        return FabricLoader.getInstance().getConfigDir().resolve("lyricsflow");
    }

    public static void migrateFromAuction() {
    }

    public static <T> T load(String file, Type type, Supplier<T> fallback) {
        Path p = Storage.dir().resolve(file);
        if (!Files.exists(p, new LinkOption[0])) {
            return fallback.get();
        }
        try {
            Object var6;
            try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8);){
                Object t = GSON.fromJson((Reader)r, type);
                var6 = t != null ? t : fallback.get();
            }
            return (T)var6;
        }
        catch (Exception var9) {
            Core.LOG.warn("Konnte {} nicht laden, nutze Standardwerte", (Object)file, (Object)var9);
            return fallback.get();
        }
    }

    public static void save(String file, Object data) {
        try {
            Files.createDirectories(Storage.dir(), new FileAttribute[0]);
            Path p = Storage.dir().resolve(file);
            Path tmp = Storage.dir().resolve(file + ".tmp");
            try (BufferedWriter w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8, new OpenOption[0]);){
                GSON.toJson(data, (Appendable)w);
            }
            Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (Exception var9) {
            Core.LOG.warn("Konnte {} nicht speichern", (Object)file, (Object)var9);
        }
    }
}


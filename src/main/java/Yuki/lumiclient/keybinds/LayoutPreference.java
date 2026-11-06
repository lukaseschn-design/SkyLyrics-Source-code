package Yuki.lumiclient.keybinds;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Properties;

public final class LayoutPreference {
    private LayoutPreference() {
    }

    public static KeyboardLayout.Size load(Path path) throws IOException {
        if (!Files.exists(path, new LinkOption[0])) {
            return KeyboardLayout.Size.FULL;
        }
        Properties properties = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IllegalArgumentException invalidProperties) {
            return KeyboardLayout.Size.FULL;
        }
        return KeyboardLayout.Size.fromId(properties.getProperty("keyboardLayout", "100").strip());
    }

    public static void save(Path path, KeyboardLayout.Size size) throws IOException {
        Path absolute = path.toAbsolutePath();
        if (absolute.getParent() != null) {
            Files.createDirectories(absolute.getParent(), new FileAttribute[0]);
        }
        Path temporary = Files.createTempFile(absolute.getParent(), "studiouskeybinds-", ".tmp", new FileAttribute[0]);
        try {
            Files.writeString(temporary, "# Studious Keybinds display preferences\nkeyboardLayout=" + size.id() + "\n", StandardCharsets.UTF_8, new OpenOption[0]);
            try {
                Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}

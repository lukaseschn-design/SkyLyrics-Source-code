package dev.lyricsflow.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.lyricsflow.gui.LyricsFlowScreen;

public class ModMenuEntry
implements ModMenuApi {
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return LyricsFlowScreen::new;
    }
}


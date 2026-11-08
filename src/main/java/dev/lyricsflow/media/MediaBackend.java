package dev.lyricsflow.media;

import dev.lyricsflow.config.LFConfig;
import dev.lyricsflow.media.Track;

public interface MediaBackend {
    public Track poll(LFConfig.Source var1) throws Exception;

    public byte[] cover();

    public void playPause();

    public void next();

    public void previous();

    default public int repeatMode() {
        return -1;
    }

    default public void setRepeat(int mode) {
    }

    default public void seek(long ms) {
    }

    public String statusKey();

    public boolean available();

    public void close();
}


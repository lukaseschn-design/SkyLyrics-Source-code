/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.jna.Memory
 *  com.sun.jna.Platform
 *  com.sun.jna.Pointer
 *  com.sun.jna.ptr.PointerByReference
 */
package dev.lyricsflow.media;

import com.sun.jna.Memory;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import dev.lyricsflow.SkyLyrics;
import dev.lyricsflow.config.LFConfig;
import dev.lyricsflow.media.Com;
import dev.lyricsflow.media.MediaBackend;
import dev.lyricsflow.media.TitleCleaner;
import dev.lyricsflow.media.Track;
import java.util.Locale;

public final class SmtcBackend
implements MediaBackend {
    private static final String MANAGER_CLASS = "Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager";
    private static final Memory IID_MANAGER_STATICS = Com.guid("2050c4ee-11a0-57de-aed7-c97c70338245");
    private static final long FILETIME_EPOCH_DIFF_MS = 11644473600000L;
    private Pointer manager;
    private Pointer session;
    private boolean failed;
    private String status = "lyricsflow.status.searching";
    private int repeat = -1;

    @Override
    public boolean available() {
        return Platform.isWindows() && !this.failed;
    }

    private boolean ensureManager() {
        if (this.manager != null) {
            return true;
        }
        if (!Platform.isWindows() || this.failed) {
            return false;
        }
        try {
            Com.init();
            Pointer statics = Com.activationFactory(MANAGER_CLASS, IID_MANAGER_STATICS);
            if (statics == null) {
                this.failed = true;
                return false;
            }
            Pointer op = Com.outPtr(statics, 6, new Object[0]);
            Com.release(statics);
            this.manager = Com.await(op, 5000L);
            if (this.manager == null) {
                this.failed = true;
                return false;
            }
            return true;
        }
        catch (Throwable t) {
            SkyLyrics.LOG.warn("Windows media controls unavailable", t);
            this.failed = true;
            return false;
        }
    }

    private static boolean isSpotify(String aumid) {
        return aumid.toLowerCase(Locale.ROOT).contains("spotify");
    }

    private static boolean isBrowser(String aumid) {
        String a = aumid.toLowerCase(Locale.ROOT);
        return a.contains("chrome") || a.contains("msedge") || a.contains("edge") || a.contains("firefox") || a.contains("308046b0af4a39cb") || a.contains("opera") || a.contains("brave") || a.contains("vivaldi") || a.contains("yandex") || a.contains("arc") || a.contains("zen");
    }

    private static boolean isYtMusic(String aumid) {
        String a = aumid.toLowerCase(Locale.ROOT);
        return a.contains("youtube-music") || a.contains("youtubemusic") || a.contains("youtube music") || a.contains("ytmusic") || a.contains("music.youtube");
    }

    private boolean matches(LFConfig.Source src, String aumid) {
        return switch (src) {
            default -> throw new MatchException(null, null);
            case LFConfig.Source.SPOTIFY -> SmtcBackend.isSpotify(aumid);
            case LFConfig.Source.YOUTUBE -> {
                if (SmtcBackend.isBrowser(aumid) || aumid.toLowerCase(Locale.ROOT).contains("youtube") && !SmtcBackend.isYtMusic(aumid)) {
                    yield true;
                }
                yield false;
            }
            case LFConfig.Source.YTMUSIC -> {
                if (SmtcBackend.isYtMusic(aumid) || SmtcBackend.isBrowser(aumid)) {
                    yield true;
                }
                yield false;
            }
            case LFConfig.Source.SOUNDCLOUD -> SmtcBackend.isBrowser(aumid);
            case LFConfig.Source.ANY -> true;
        };
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Track poll(LFConfig.Source source) {
        String album;
        String artist;
        String title;
        if (!this.ensureManager()) {
            this.status = "lyricsflow.status.unsupported";
            return null;
        }
        Pointer chosen = null;
        int chosenStatus = -1;
        String chosenApp = "";
        Pointer sessions = Com.outPtr(this.manager, 7, new Object[0]);
        if (sessions == null) {
            return null;
        }
        try {
            int size = Com.outInt(sessions, 7);
            for (int i = 0; i < size; ++i) {
                PointerByReference r = new PointerByReference();
                if (Com.call(sessions, 6, i, r) < 0 || r.getValue() == null) continue;
                Pointer s = r.getValue();
                String app = Com.hstringProp(s, 6);
                if (!this.matches(source, app)) {
                    Com.release(s);
                    continue;
                }
                int st = SmtcBackend.playbackStatus(s);
                if (chosen == null || st == 4 && chosenStatus != 4) {
                    Com.release(chosen);
                    chosen = s;
                    chosenStatus = st;
                    chosenApp = app;
                    continue;
                }
                Com.release(s);
            }
        }
        finally {
            Com.release(sessions);
        }
        Com.release(this.session);
        this.session = chosen;
        if (chosen == null) {
            this.status = source == LFConfig.Source.SPOTIFY ? "lyricsflow.status.open_spotify" : "lyricsflow.status.nothing";
            return null;
        }
        this.repeat = SmtcBackend.repeatOf(chosen);
        Pointer props = Com.await(Com.outPtr(chosen, 7, new Object[0]), 1500L);
        if (props == null) {
            return null;
        }
        try {
            title = Com.hstringProp(props, 6);
            artist = Com.hstringProp(props, 9);
            album = Com.hstringProp(props, 10);
            if (artist.isEmpty()) {
                artist = Com.hstringProp(props, 8);
            }
        }
        finally {
            Com.release(props);
        }
        if (title.isEmpty()) {
            this.status = "lyricsflow.status.nothing";
            return null;
        }
        long durationMs = 0L;
        long positionMs = 0L;
        long rawMs = -1L;
        long updatedFt = -1L;
        Pointer tl = Com.outPtr(chosen, 8, new Object[0]);
        if (tl != null) {
            try {
                long nowFt;
                long delta;
                long start = Com.outLong(tl, 6);
                long end = Com.outLong(tl, 7);
                long pos = Com.outLong(tl, 10);
                long updated = Com.outLong(tl, 11);
                if (end > 0L && end != Long.MIN_VALUE) {
                    durationMs = (end - Math.max(0L, start)) / 10000L;
                }
                if (pos != Long.MIN_VALUE) {
                    rawMs = positionMs = pos / 10000L;
                }
                if (updated > 0L && updated != Long.MIN_VALUE) {
                    updatedFt = updated;
                }
                if (chosenStatus == 4 && updated > 0L && updated != Long.MIN_VALUE && (delta = ((nowFt = (System.currentTimeMillis() + 11644473600000L) * 10000L) - updated) / 10000L) > 0L && delta < 21600000L) {
                    positionMs += delta;
                }
            }
            finally {
                Com.release(tl);
            }
        }
        boolean playing = chosenStatus == 4;
        String string = this.status = playing ? "lyricsflow.status.playing" : "lyricsflow.status.paused";
        String app = SmtcBackend.isSpotify(chosenApp) ? "Spotify" : (SmtcBackend.isYtMusic(chosenApp) || source == LFConfig.Source.YTMUSIC ? "YT Music" : (SmtcBackend.isBrowser(chosenApp) ? "Browser" : chosenApp));
        if (!SmtcBackend.isSpotify(chosenApp)) {
            String[] split = TitleCleaner.splitYoutube(title, artist);
            artist = split[0];
            title = split[1];
        }
        return new Track(title, artist, album, durationMs, positionMs, playing, app, rawMs, updatedFt, System.nanoTime());
    }

    private static int playbackStatus(Pointer s) {
        Pointer info = Com.outPtr(s, 9, new Object[0]);
        if (info == null) {
            return -1;
        }
        try {
            int n = Com.outInt(info, 7);
            return n;
        }
        finally {
            Com.release(info);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static int repeatOf(Pointer s) {
        Pointer info = Com.outPtr(s, 9, new Object[0]);
        if (info == null) {
            return -1;
        }
        try {
            int n;
            Pointer ref = Com.outPtr(info, 9, new Object[0]);
            if (ref == null) {
                int n2 = -1;
                return n2;
            }
            try {
                n = Com.outInt(ref, 6);
            }
            catch (Throwable throwable) {
                try {
                    Com.release(ref);
                    throw throwable;
                }
                catch (Throwable t) {
                    int n3 = -1;
                    return n3;
                }
            }
            Com.release(ref);
            return n;
        }
        finally {
            Com.release(info);
        }
    }

    @Override
    public int repeatMode() {
        return this.repeat;
    }

    @Override
    public void setRepeat(int mode) {
        if (this.session == null) {
            return;
        }
        try {
            Com.awaitAction(Com.outPtr(this.session, 21, mode));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public byte[] cover() {
        byte[] byArray;
        Pointer stream;
        Pointer ref;
        if (this.session == null) {
            return null;
        }
        Pointer props = Com.await(Com.outPtr(this.session, 7, new Object[0]), 1500L);
        if (props == null) {
            return null;
        }
        try {
            ref = Com.outPtr(props, 15, new Object[0]);
        }
        finally {
            Com.release(props);
        }
        if (ref == null) {
            return null;
        }
        try {
            stream = Com.await(Com.outPtr(ref, 6, new Object[0]), 3000L);
        }
        finally {
            Com.release(ref);
        }
        if (stream == null) {
            return null;
        }
        try {
            byArray = Com.readRandomAccessStream(stream, 0x800000);
        }
        catch (Throwable throwable) {
            try {
                Com.release(stream);
                throw throwable;
            }
            catch (Throwable t) {
                return null;
            }
        }
        Com.release(stream);
        return byArray;
    }

    private void action(int index) {
        if (this.session == null) {
            return;
        }
        try {
            Com.awaitAction(Com.outPtr(this.session, index, new Object[0]));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Override
    public void playPause() {
        this.action(20);
    }

    @Override
    public void next() {
        this.action(16);
    }

    @Override
    public void previous() {
        this.action(17);
    }

    @Override
    public void seek(long ms) {
        if (this.session == null) {
            return;
        }
        try {
            Com.awaitAction(Com.outPtr(this.session, 24, Math.max(0L, ms) * 10000L));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Override
    public String statusKey() {
        return this.status;
    }

    @Override
    public void close() {
        Com.release(this.session);
        this.session = null;
        Com.release(this.manager);
        this.manager = null;
    }
}


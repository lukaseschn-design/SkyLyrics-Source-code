package dev.lyricsflow.app.lyrics;

import dev.lyricsflow.app.Core;
import dev.lyricsflow.app.config.Config;
import dev.lyricsflow.app.hud.Island;
import dev.lyricsflow.app.lyrics.NowPlaying;
import dev.lyricsflow.app.sky.SkyPreset;
import dev.lyricsflow.app.util.Fx;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.lyrics.Lyrics;
import dev.lyricsflow.media.MediaManager;
import dev.lyricsflow.media.SpotifyWeb;
import dev.lyricsflow.media.Track;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public final class SpotifyClient {
    public static final int PORT = 8789;
    public static final String REDIRECT = "http://127.0.0.1:8789/callback";
    public static final String DASHBOARD = "https://developer.spotify.com/dashboard";
    private static volatile NowPlaying now = NowPlaying.EMPTY;
    private static final Deque<Played> HISTORY = new ArrayDeque<Played>();
    private static Lyrics shownLyrics;
    private static boolean shownCaps;
    private static List<NowPlaying.Line> lines;
    private static long coverSeq;
    private static String lastKey;

    private SpotifyClient() {
    }

    public static void init() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> MediaManager.get().start());
        Config c = Core.cfg();
        if (c.spotifyClientId != null && !c.spotifyClientId.isBlank() && SpotifyWeb.clientId().isEmpty()) {
            SpotifyWeb.setClientId(c.spotifyClientId);
        }
        if ((c.spotifyClientId == null || c.spotifyClientId.isBlank()) && !SpotifyWeb.clientId().isEmpty()) {
            c.spotifyClientId = SpotifyWeb.clientId();
            if (SpotifyWeb.connected()) {
                c.spotifyMode = 1;
            }
            Core.saveConfig();
        }
    }

    public static void tick() {
        MediaManager mm = MediaManager.get();
        Track t = mm.track();
        if (t == null || t.isEmpty()) {
            now = NowPlaying.EMPTY;
            lastKey = "";
            return;
        }
        Lyrics l = mm.lyrics();
        boolean caps = ConfigManager.get().allCaps;
        if (l != shownLyrics || caps != shownCaps) {
            shownLyrics = l;
            shownCaps = caps;
            lines = SpotifyClient.lines(l);
        }
        NowPlaying.LyricsState st = switch (mm.lyricsState()) {
            case MediaManager.LyricsState.SEARCHING -> NowPlaying.LyricsState.LOADING;
            case MediaManager.LyricsState.FOUND -> {
                if (lines.isEmpty()) {
                    yield NowPlaying.LyricsState.NOT_FOUND;
                }
                yield NowPlaying.LyricsState.SYNCED;
            }
            case MediaManager.LyricsState.NOT_FOUND -> NowPlaying.LyricsState.NOT_FOUND;
            default -> NowPlaying.LyricsState.NONE;
        };
        String key = t.key();
        long pos = Math.max(0L, mm.position());
        now = new NowPlaying(key, t.title, t.artist, t.durationMs, t.playing, pos, System.currentTimeMillis(), lines, st);
        if (!key.equals(lastKey)) {
            lastKey = key;
            SpotifyClient.remember(t.title, t.artist);
            Config cf = Core.cfg();
            if (cf.skyShuffle && cf.sky >= 0) {
                SkyPreset.shuffle();
            }
        }
    }

    public static List<NowPlaying.Line> lines(Lyrics l) {
        if (l == null || l.lines.isEmpty()) {
            return List.of();
        }
        ArrayList<NowPlaying.Line> out = new ArrayList<NowPlaying.Line>();
        for (Lyrics.Line line : l.lines) {
            int n = line.words.size();
            String[] words = new String[n];
            long[] times = new long[n];
            for (int i = 0; i < n; ++i) {
                words[i] = Lyrics.show(line.words.get((int)i).text);
                times[i] = line.words.get((int)i).start;
            }
            out.add(new NowPlaying.Line(line.start, Lyrics.show(line.text), words, (long[])(n > 0 ? times : null)));
        }
        return Collections.unmodifiableList(out);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void remember(String title, String artist) {
        Deque<Played> deque = HISTORY;
        synchronized (deque) {
            HISTORY.removeIf(p -> p.title().equals(title) && p.artist().equals(artist));
            HISTORY.addFirst(new Played(title, artist, System.currentTimeMillis()));
            while (HISTORY.size() > 30) {
                HISTORY.removeLast();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static List<Played> history() {
        Deque<Played> deque = HISTORY;
        synchronized (deque) {
            return new ArrayList<Played>(HISTORY);
        }
    }

    public static NowPlaying now() {
        return now;
    }

    public static Status status() {
        return switch (SpotifyWeb.state()) {
            case "on" -> Status.CONNECTED;
            case "waiting" -> Status.WAITING_LOGIN;
            case "error" -> Status.ERROR;
            default -> SpotifyWeb.clientId().isBlank() ? Status.NOT_CONFIGURED : Status.DISCONNECTED;
        };
    }

    public static String lastError() {
        return SpotifyWeb.error();
    }

    public static void login() {
        SpotifyWeb.setClientId(Core.cfg().spotifyClientId);
        SpotifyWeb.login();
    }

    public static void logout() {
        SpotifyWeb.disconnect();
    }

    public static boolean autoMode() {
        return Core.cfg().spotifyMode == 0;
    }

    public static void setMode(int mode) {
        Core.cfg().spotifyMode = mode == 1 ? 1 : 0;
        Core.saveConfig();
    }

    public static void restartDemo() {
    }

    public static void syncHere() {
        NowPlaying np = SpotifyClient.now();
        Config c = Core.cfg();
        if (np.hasTrack() && !np.lines.isEmpty()) {
            long raw = np.progressNow(0);
            long cur = raw + (long)c.lyricsOffsetMs;
            long best = Long.MAX_VALUE;
            long bestT = 0L;
            for (NowPlaying.Line l : np.lines) {
                long d;
                if (l.text().isBlank() || (d = Math.abs(l.timeMs() - cur)) >= best) continue;
                best = d;
                bestT = l.timeMs();
            }
            c.lyricsOffsetMs = (int)Math.max(-5000L, Math.min(5000L, bestT - raw));
            Core.saveConfig();
            Island.notify(Fx.t("lyricsflow.media.synced", (c.lyricsOffsetMs > 0 ? "+" : "") + c.lyricsOffsetMs), true);
        } else {
            Island.notify(Fx.t("lyricsflow.media.nolyrics", new Object[0]), false);
        }
    }

    public static void control(Cmd cmd) {
        MediaManager mm = MediaManager.get();
        switch (cmd.ordinal()) {
            case 0: {
                mm.next();
                break;
            }
            case 1: {
                mm.previous();
                break;
            }
            case 2: {
                mm.playPause();
            }
        }
    }

    static {
        lines = List.of();
        coverSeq = -1L;
        lastKey = "";
    }

    public record Played(String title, String artist, long at) {
    }

    public static enum Status {
        NOT_CONFIGURED,
        DISCONNECTED,
        WAITING_LOGIN,
        CONNECTED,
        ERROR;

    }

    public static enum Cmd {
        NEXT,
        PREVIOUS,
        PLAYPAUSE;

    }
}


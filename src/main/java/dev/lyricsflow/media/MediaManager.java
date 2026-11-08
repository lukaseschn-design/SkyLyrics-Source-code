package dev.lyricsflow.media;

import com.google.gson.Gson;
import dev.lyricsflow.LyricsFlow;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.config.LFConfig;
import dev.lyricsflow.lyrics.Lyrics;
import dev.lyricsflow.lyrics.LyricsFetcher;
import dev.lyricsflow.media.MediaBackend;
import dev.lyricsflow.media.SmtcBackend;
import dev.lyricsflow.media.SpotifyWeb;
import dev.lyricsflow.media.Track;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MediaManager {
    private static final MediaManager INSTANCE = new MediaManager();
    private final SmtcBackend smtc = new SmtcBackend();
    private final ConcurrentLinkedQueue<Runnable> commands = new ConcurrentLinkedQueue();
    private final ConcurrentLinkedQueue<Event> events = new ConcurrentLinkedQueue();
    private final ExecutorService lyricsPool = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "SkyLyrics-Lyrics");
        t.setDaemon(true);
        return t;
    });
    private volatile Track track;
    private volatile Lyrics lyrics;
    private volatile LyricsState lyricsState = LyricsState.NONE;
    private volatile String statusKey = "lyricsflow.status.searching";
    private volatile boolean running;
    private volatile long songSeq;
    private Thread thread;
    private String lastKey = "";
    private boolean lastPlaying;
    private long missingSince = -1L;
    private long pausedUpdatedFt = -2L;
    private long lastOffset = Long.MIN_VALUE;
    private long lastPollNs = -1L;
    private int stable;
    private volatile byte[] cover;
    private volatile long coverSeq = -1L;
    private volatile int repeat;
    private volatile long repeatHold;
    private int lastReportedRepeat = -1;
    private volatile boolean pollSoon;
    private volatile boolean forceSync;
    private long lastApiAt;
    private final long[] apiOffs = new long[5];
    private int apiN;
    private static final Gson GSON = new Gson();

    public static MediaManager get() {
        return INSTANCE;
    }

    private MediaManager() {
    }

    public void start() {
        if (this.running) {
            return;
        }
        this.running = true;
        this.thread = new Thread(this::loop, "SkyLyrics-Media");
        this.thread.setDaemon(true);
        this.thread.start();
    }

    public void stop() {
        this.running = false;
        if (this.thread != null) {
            this.thread.interrupt();
        }
        this.lyricsPool.shutdownNow();
    }

    public Track track() {
        return this.track;
    }

    public Lyrics lyrics() {
        return this.lyrics;
    }

    public LyricsState lyricsState() {
        return this.lyricsState;
    }

    public String statusKey() {
        return this.statusKey;
    }

    public long songSeq() {
        return this.songSeq;
    }

    public byte[] cover() {
        return this.cover;
    }

    public long coverSeq() {
        return this.coverSeq;
    }

    public boolean isPlaying() {
        Track t = this.track;
        return t != null && t.playing;
    }

    public long positionRaw() {
        Track t = this.track;
        return t == null ? -1L : t.positionNow();
    }

    public long position() {
        Track t = this.track;
        if (t == null) {
            return -1L;
        }
        LFConfig c = ConfigManager.get();
        Integer own = c.songOffsets == null ? null : c.songOffsets.get(t.key());
        return Math.max(0L, t.positionNow() + (long)c.syncOffsetMs + (long)(own == null ? 0 : own));
    }

    public Event pollEvent() {
        return this.events.poll();
    }

    public void playPause() {
        this.commands.add(() -> {
            this.backend().playPause();
            this.pollSoon = true;
        });
    }

    public void next() {
        this.commands.add(() -> {
            this.backend().next();
            this.pollSoon = true;
        });
    }

    public void previous() {
        this.commands.add(() -> {
            this.backend().previous();
            this.pollSoon = true;
        });
    }

    public int repeat() {
        return this.repeat;
    }

    public void cycleRepeat() {
        int next;
        this.repeat = next = this.repeat == 0 ? 2 : (this.repeat == 2 ? 1 : 0);
        this.repeatHold = System.currentTimeMillis() + 4000L;
        this.commands.add(() -> {
            this.backend().setRepeat(next);
            Track t = this.track;
            if (t != null && "Spotify".equals(t.sourceApp)) {
                Thread th = new Thread(() -> SpotifyWeb.setRepeat(next), "SkyLyrics repeat");
                th.setDaemon(true);
                th.start();
            }
            this.pollSoon = true;
        });
    }

    public void seek(long ms) {
        this.commands.add(() -> {
            this.backend().seek(ms);
            Track t = this.track;
            if (t != null) {
                this.track = new Track(t.title, t.artist, t.album, t.durationMs, ms, t.playing, t.sourceApp, ms);
            }
            this.pollSoon = true;
        });
    }

    public void reconnect() {
        this.commands.add(() -> {
            this.smtc.close();
            this.lastKey = "";
            this.track = null;
            this.lyrics = null;
            this.lyricsState = LyricsState.NONE;
        });
    }

    public void refetch() {
        Track t = this.track;
        if (t == null) {
            return;
        }
        try {
            Files.deleteIfExists(MediaManager.cacheFile(t));
        }
        catch (Exception exception) {
        }
        this.fetchLyrics(t, this.songSeq);
    }

    public void resync() {
        this.forceSync = true;
        this.pollSoon = true;
    }

    private MediaBackend backend() {
        return this.smtc;
    }

    private void loop() {
        while (this.running) {
            long sleep;
            long started = System.currentTimeMillis();
            try {
                Runnable r;
                while ((r = this.commands.poll()) != null) {
                    try {
                        r.run();
                    }
                    catch (Throwable t) {
                        LyricsFlow.LOG.debug("media command failed", t);
                    }
                }
                if (!ConfigManager.get().enabled) {
                    try {
                        Thread.sleep(500L);
                        continue;
                    }
                    catch (InterruptedException e) {
                        if (this.running) continue;
                        return;
                    }
                }
                this.tick();
            }
            catch (Throwable t) {
                LyricsFlow.LOG.debug("media poll failed", t);
            }
            long interval = 250L;
            if (this.pollSoon) {
                this.pollSoon = false;
                interval = 120L;
            }
            if ((sleep = interval - (System.currentTimeMillis() - started)) <= 0L) continue;
            try {
                Thread.sleep(sleep);
            }
            catch (InterruptedException e) {
                if (this.running) continue;
                return;
            }
        }
        this.smtc.close();
    }

    private void tick() throws Exception {
        LFConfig c = ConfigManager.get();
        Track t = null;
        if (c.enabled) {
            MediaBackend b = this.backend();
            t = b.poll(c.source);
            int rp = b.repeatMode();
            if (rp >= 0 && rp <= 2 && rp != this.lastReportedRepeat) {
                if (System.currentTimeMillis() > this.repeatHold || this.lastReportedRepeat < 0) {
                    this.repeat = rp;
                }
                this.lastReportedRepeat = rp;
            }
            if (this.repeat < 0) {
                this.repeat = 0;
            }
            this.statusKey = b.statusKey();
        }
        if (t == null || t.isEmpty()) {
            if (this.track != null) {
                if (this.missingSince < 0L) {
                    this.missingSince = System.currentTimeMillis();
                }
                if (System.currentTimeMillis() - this.missingSince > 2500L) {
                    this.events.add(new Event(EventType.STOP, this.track, System.nanoTime()));
                    this.track = null;
                    this.lastKey = "";
                }
            }
            return;
        }
        this.missingSince = -1L;
        String key = t.key();
        if (!key.equals(this.lastKey)) {
            this.lastKey = key;
            this.lastPlaying = t.playing;
            this.pausedUpdatedFt = t.playing ? -2L : t.updatedFt;
            this.stable = 0;
            this.lastOffset = Long.MIN_VALUE;
            this.apiN = 0;
            this.track = t;
            long seq = ++this.songSeq;
            this.lyrics = null;
            this.events.add(new Event(EventType.SONG, t, System.nanoTime()));
            this.cover = null;
            this.commands.add(() -> {
                byte[] b = null;
                try {
                    b = this.backend().cover();
                }
                catch (Throwable throwable) {
                }
                if (seq == this.songSeq) {
                    this.cover = b;
                    this.coverSeq = seq;
                }
            });
            this.fetchLyrics(t, seq);
            return;
        }
        Track prev = this.track;
        boolean fresh = t.updatedFt > 0L && t.updatedFt != this.pausedUpdatedFt;
        long nowNs = System.nanoTime();
        if (prev != null && t.playing) {
            if (prev.playing) {
                boolean trusted;
                long sinceMs;
                long ours = prev.positionNow();
                long offset = t.positionMs - ours;
                long l = sinceMs = this.lastPollNs > 0L ? (nowNs - this.lastPollNs) / 1000000L : 250L;
                this.stable = this.lastOffset != Long.MIN_VALUE && Math.abs(offset - this.lastOffset) <= 60L + sinceMs / 5L ? ++this.stable : 0;
                this.lastOffset = offset;
                long diff = Math.abs(offset);
                boolean seek = prev.rawPositionMs >= 0L && t.rawPositionMs >= 0L && Math.abs(t.rawPositionMs - prev.rawPositionMs) > 400L && t.updatedFt != prev.updatedFt;
                boolean apiLive = c.source == LFConfig.Source.SPOTIFY && SpotifyWeb.positionFor(t) >= 0L;
                boolean bl = trusted = seek && this.stable >= 1 || fresh && this.stable >= 2 && !apiLive;
                if (trusted && diff > (long)(this.forceSync ? 120 : 250)) {
                    LyricsFlow.LOG.debug("[SkyLyrics sync] re-sync {} ms (ours {} -> windows {}), fresh={}, seek={}, stable={}", new Object[]{offset, ours, t.positionMs, fresh, seek, this.stable});
                    t = Track.at(t, t.positionMs);
                    this.lastOffset = 0L;
                } else {
                    t = prev.keepClock(t);
                }
            } else {
                this.stable = 0;
                this.lastOffset = Long.MIN_VALUE;
                if (!fresh && t.rawPositionMs >= 0L) {
                    long from = Math.abs(t.rawPositionMs - prev.positionMs) < 3000L ? prev.positionMs : t.rawPositionMs;
                    t = Track.at(t, from);
                }
            }
        }
        if (c.source == LFConfig.Source.SPOTIFY && t.playing) {
            SpotifyWeb.start();
            long at = SpotifyWeb.sampleAt();
            long api = SpotifyWeb.positionFor(t);
            if (api >= 0L && at != this.lastApiAt) {
                boolean big;
                this.lastApiAt = at;
                long off = api - t.positionNow();
                this.apiOffs[this.apiN++ % this.apiOffs.length] = off;
                int n = Math.min(this.apiN, this.apiOffs.length);
                long[] s = Arrays.copyOf(this.apiOffs, n);
                Arrays.sort(s);
                long med = s[n / 2];
                boolean bl = big = Math.abs(off) > 1500L;
                if (big || n >= 3 && Math.abs(med) > 90L) {
                    LyricsFlow.LOG.debug("[SkyLyrics sync] spotify re-sync {} ms", (Object)(big ? off : med));
                    t = Track.at(t, t.positionNow() + (big ? off : med));
                    this.apiN = 0;
                }
            }
        } else {
            this.apiN = 0;
        }
        this.lastPollNs = nowNs;
        this.forceSync = false;
        if (!t.playing) {
            this.pausedUpdatedFt = t.updatedFt;
        }
        this.track = t;
        if (prev != null && t.playing != this.lastPlaying) {
            this.events.add(new Event(t.playing ? EventType.RESUME : EventType.PAUSE, t, System.nanoTime()));
        }
        this.lastPlaying = t.playing;
    }

    private void fetchLyrics(Track t, long seq) {
        this.lyricsState = LyricsState.SEARCHING;
        this.lyricsPool.submit(() -> {
            Lyrics l = MediaManager.readCache(t);
            int attempt = 1;
            while (l == null || !l.synced) {
                if (seq != this.songSeq) {
                    return;
                }
                Lyrics got = null;
                try {
                    got = LyricsFetcher.fetch(t.artist, t.title, t.album, t.durationMs);
                }
                catch (Throwable throwable) {
                }
                if (got != null) {
                    if (l == null || got.synced) {
                        l = got;
                    }
                    MediaManager.writeCache(t, got);
                }
                if (l != null && l.synced || attempt >= 5) break;
                long until = System.currentTimeMillis() + 1500L * (long)attempt;
                while (System.currentTimeMillis() < until) {
                    if (seq != this.songSeq) {
                        return;
                    }
                    try {
                        Thread.sleep(100L);
                    }
                    catch (InterruptedException e) {
                        return;
                    }
                }
                ++attempt;
            }
            if (seq != this.songSeq) {
                return;
            }
            if (l != null && !l.synced) {
                l = null;
            }
            this.lyrics = l;
            this.lyricsState = l == null ? LyricsState.NOT_FOUND : LyricsState.FOUND;
        });
    }

    private static Path cacheFile(Track t) throws Exception {
        Path dir = ConfigManager.dir().resolve("cache");
        Files.createDirectories(dir, new FileAttribute[0]);
        byte[] h = MessageDigest.getInstance("SHA-1").digest((t.key() + "|v6").getBytes(StandardCharsets.UTF_8));
        return dir.resolve(HexFormat.of().formatHex(h, 0, 10) + ".json");
    }

    private static Lyrics readCache(Track t) {
        try {
            Path f = MediaManager.cacheFile(t);
            if (!Files.exists(f, new LinkOption[0])) {
                return null;
            }
            CacheEntry e = (CacheEntry)GSON.fromJson(Files.readString(f, StandardCharsets.UTF_8), CacheEntry.class);
            if (e == null || e.texts.isEmpty()) {
                return null;
            }
            ArrayList<long[]> times = new ArrayList<long[]>();
            for (int i = 0; i < e.texts.size(); ++i) {
                times.add(new long[]{e.starts.get(i), e.ends.get(i)});
            }
            ArrayList<List<Lyrics.Word>> words = null;
            if (e.words != null && e.words.size() == e.texts.size()) {
                words = new ArrayList<List<Lyrics.Word>>();
                for (int i = 0; i < e.words.size(); ++i) {
                    ArrayList<Lyrics.Word> w = new ArrayList<Lyrics.Word>();
                    for (int k = 0; k < e.words.get(i).size(); ++k) {
                        w.add(Lyrics.Word.of(e.words.get(i).get(k), e.wordStarts.get(i).get(k), e.wordEnds.get(i).get(k)));
                    }
                    words.add(w);
                }
            }
            return new Lyrics(times, e.texts, e.synced, e.source, e.duration, words);
        }
        catch (Exception ex) {
            return null;
        }
    }

    private static void writeCache(Track t, Lyrics l) {
        try {
            CacheEntry e = new CacheEntry();
            e.synced = l.synced;
            e.source = l.source;
            e.duration = t.durationMs;
            if (l.wordSynced) {
                e.words = new ArrayList<List<String>>();
                e.wordStarts = new ArrayList<List<Long>>();
                e.wordEnds = new ArrayList<List<Long>>();
            }
            for (Lyrics.Line line : l.lines) {
                e.starts.add(line.start);
                e.ends.add(line.end);
                e.texts.add(line.text);
                if (!l.wordSynced) continue;
                ArrayList<String> w = new ArrayList<String>();
                ArrayList<Long> ws = new ArrayList<Long>();
                ArrayList<Long> we = new ArrayList<Long>();
                for (Lyrics.Word x : line.words) {
                    w.add(x.text);
                    ws.add(x.start);
                    we.add(x.end);
                }
                e.words.add(w);
                e.wordStarts.add(ws);
                e.wordEnds.add(we);
            }
            Files.writeString(MediaManager.cacheFile(t), (CharSequence)GSON.toJson((Object)e), StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (Exception exception) {
        }
    }

    public static enum LyricsState {
        NONE,
        SEARCHING,
        FOUND,
        NOT_FOUND;

    }

    public record Event(EventType type, Track track, long atNanos) {
    }

    public static enum EventType {
        SONG,
        PAUSE,
        RESUME,
        STOP;

    }

    private static final class CacheEntry {
        boolean synced;
        String source;
        long duration;
        List<Long> starts = new ArrayList<Long>();
        List<Long> ends = new ArrayList<Long>();
        List<String> texts = new ArrayList<String>();
        List<List<String>> words;
        List<List<Long>> wordStarts;
        List<List<Long>> wordEnds;

        private CacheEntry() {
        }
    }
}


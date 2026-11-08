package dev.lyricsflow.lyrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.lyricsflow.lyrics.Lyrics;
import dev.lyricsflow.media.TitleCleaner;
import dev.lyricsflow.util.Http;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LyricsFetcher {
    private static final Pattern LRC = Pattern.compile("\\[(\\d{1,3}):(\\d{1,2})(?:[.:](\\d{1,3}))?\\]");
    private static final ExecutorService POOL = Executors.newFixedThreadPool(6, r -> {
        Thread t = new Thread(r, "SkyLyrics-Search");
        t.setDaemon(true);
        return t;
    });
    private static final Pattern SPEED = Pattern.compile("(?i)\\b(sped\\s*up|speed\\s*up|slowed(\\s*(down|\\+|and|&)?\\s*reverb)?|nightcore|daycore)\\b");
    private static final Pattern REMIX = Pattern.compile("(?i)\\b(remix|rmx|bootleg|mashup|flip|vip mix)\\b");
    private static final ThreadLocal<Double> MAX_DIFF = ThreadLocal.withInitial(() -> 3.0);
    private static final String MXM = "https://apic-desktop.musixmatch.com/ws/1.1/";
    private static final Map<String, String> MXM_HEADERS = Map.of("authority", "apic-desktop.musixmatch.com", "cookie", "AWSELBCORS=0; AWSELB=0");
    private static volatile String mxmToken;
    private static volatile long mxmTokenAt;
    private static final Map<String, String> NE_HEADERS;
    private static final Pattern YRC_LINE;
    private static final Pattern YRC_WORD;
    private static final Pattern WORD_TAG;

    private LyricsFetcher() {
    }

    public static Lyrics fetch(String artist, String title, String album, long durationMs) {
        Lyrics w;
        String raw;
        long deadline = System.currentTimeMillis() + 15000L;
        List<String[]> variants = TitleCleaner.variants(artist, title);
        ArrayList<Callable<Lyrics>> first = new ArrayList<Callable<Lyrics>>();
        ArrayList<Callable<Lyrics>> more = new ArrayList<Callable<Lyrics>>();
        String[] v0 = variants.get(0);
        String[] vLast = variants.get(variants.size() - 1);
        first.add(() -> LyricsFetcher.exact(v0, durationMs));
        first.add(() -> LyricsFetcher.search(v0, durationMs));
        first.add(() -> LyricsFetcher.bestOf(Http.get("https://lrclib.net/api/search?" + Http.query(Map.of("q", (vLast[0] + " " + vLast[1]).trim())), null), durationMs, vLast));
        first.add(() -> LyricsFetcher.bestByTitle(Http.get("https://lrclib.net/api/search?" + Http.query(Map.of("track_name", v0[1])), null), durationMs, v0));
        first.add(() -> LyricsFetcher.netease(v0, durationMs));
        for (int i = 1; i < variants.size(); ++i) {
            String[] v = variants.get(i);
            more.add(() -> LyricsFetcher.exact(v, durationMs));
            more.add(() -> LyricsFetcher.search(v, durationMs));
            if (i != 1) continue;
            more.add(() -> LyricsFetcher.bestByTitle(Http.get("https://lrclib.net/api/search?" + Http.query(Map.of("track_name", v[1])), null), durationMs, v));
            more.add(() -> LyricsFetcher.netease(v, durationMs));
        }
        String string = raw = title == null ? "" : title;
        if (SPEED.matcher(raw).find()) {
            String[] clean = new String[]{TitleCleaner.mainArtist(artist), TitleCleaner.clean(SPEED.matcher(raw).replaceAll(" "))};
            ArrayList<Callable<Lyrics>> sp = new ArrayList<Callable<Lyrics>>();
            sp.add(() -> LyricsFetcher.exact(v0, durationMs));
            sp.add(() -> LyricsFetcher.stretched(clean, durationMs));
            return LyricsFetcher.firstSynced(sp, deadline);
        }
        boolean remix = REMIX.matcher(raw).find();
        Future<Lyrics> wordsF = POOL.submit(() -> {
            Http.DEADLINE.set(deadline);
            if (remix) {
                MAX_DIFF.set(2.0);
            }
            try {
                Lyrics lyrics = LyricsFetcher.neteaseWords(v0, durationMs);
                return lyrics;
            }
            catch (Exception e) {
                Lyrics lyrics = null;
                return lyrics;
            }
            finally {
                Http.DEADLINE.remove();
                MAX_DIFF.remove();
            }
        });
        Future<Lyrics> mxmF = POOL.submit(() -> {
            Http.DEADLINE.set(deadline);
            if (remix) {
                MAX_DIFF.set(2.0);
            }
            try {
                Lyrics lyrics = LyricsFetcher.musixmatchWords(v0, durationMs);
                return lyrics;
            }
            catch (Exception e) {
                Lyrics lyrics = null;
                return lyrics;
            }
            finally {
                Http.DEADLINE.remove();
                MAX_DIFF.remove();
            }
        });
        Lyrics l = LyricsFetcher.firstSynced(first, deadline, remix);
        if (l == null && !more.isEmpty()) {
            l = LyricsFetcher.firstSynced(more, deadline, remix);
        }
        boolean fromMxm = false;
        try {
            w = mxmF.get(Math.max(1L, Math.min(4000L, deadline - System.currentTimeMillis())), TimeUnit.MILLISECONDS);
            if (w != null && w.lines.size() >= 3) {
                l = w;
                fromMxm = true;
            }
        }
        catch (Exception ignored) {
            mxmF.cancel(true);
        }
        if (!(fromMxm || l != null && l.wordSynced)) {
            try {
                w = wordsF.get(Math.max(1L, Math.min(4000L, deadline - System.currentTimeMillis())), TimeUnit.MILLISECONDS);
                if (w != null && w.wordSynced && w.lines.size() >= 3) {
                    l = w;
                }
            }
            catch (Exception ignored) {
                wordsF.cancel(true);
            }
        }
        if (l == null && System.currentTimeMillis() < deadline - 1500L) {
            l = LyricsFetcher.firstSynced(first, deadline, 10.0);
        }
        if (l != null && !LyricsFetcher.sameScript(v0, l)) {
            l = null;
        }
        return l;
    }

    private static Lyrics neonWay(String artist, String title, String album, long durationMs) throws Exception {
        JsonObject o;
        String v;
        Http.Response r;
        if (title == null || title.isBlank()) {
            return null;
        }
        String ar = artist == null ? "" : artist;
        String synced = null;
        LinkedHashMap<String, String> q = new LinkedHashMap<String, String>();
        q.put("track_name", title);
        q.put("artist_name", ar);
        if (album != null && !album.isBlank()) {
            q.put("album_name", album);
        }
        if (durationMs > 0L) {
            q.put("duration", String.valueOf(Math.round((double)durationMs / 1000.0)));
        }
        if ((r = Http.get("https://lrclib.net/api/get?" + Http.query(q), null)).code() == 200 && !(v = LyricsFetcher.str(o = JsonParser.parseString((String)r.body()).getAsJsonObject(), "syncedLyrics")).isBlank()) {
            synced = v;
        }
        if (synced == null) {
            String clean = title.replaceAll("\\s*[-\u2013]\\s.*$", "").replaceAll("\\s*[(\\[].*?[)\\]]", "").trim();
            LinkedHashMap<String, String> sq = new LinkedHashMap<String, String>();
            sq.put("track_name", clean.isEmpty() ? title : clean);
            sq.put("artist_name", ar);
            Http.Response sr = Http.get("https://lrclib.net/api/search?" + Http.query(sq), null);
            if (sr.code() == 200) {
                double best = Double.MAX_VALUE;
                for (JsonElement e : JsonParser.parseString((String)sr.body()).getAsJsonArray()) {
                    JsonObject o2 = e.getAsJsonObject();
                    String sl = LyricsFetcher.str(o2, "syncedLyrics");
                    if (sl.isBlank()) continue;
                    double d = durationMs <= 0L ? 0.0 : (o2.has("duration") && !o2.get("duration").isJsonNull() ? Math.abs(o2.get("duration").getAsDouble() - (double)durationMs / 1000.0) : 30.0);
                    double d2 = d;
                    if (!(d2 < best) || !(d2 < 8.0)) continue;
                    best = d2;
                    synced = sl;
                }
            }
        }
        if (synced == null) {
            return null;
        }
        Lyrics l = LyricsFetcher.parseLrc(synced, durationMs);
        if (l != null && !LyricsFetcher.sameScript(new String[]{ar, title}, l)) {
            return null;
        }
        return l;
    }

    private static double maxDiff() {
        return MAX_DIFF.get();
    }

    private static String mxmToken(boolean fresh) throws Exception {
        String t = mxmToken;
        if (!fresh && t != null && System.currentTimeMillis() - mxmTokenAt < 21600000L) {
            return t;
        }
        Http.Response r = Http.get("https://apic-desktop.musixmatch.com/ws/1.1/token.get?app_id=web-desktop-app-v1.0&t=" + Long.toString(System.nanoTime(), 36), MXM_HEADERS);
        if (r.code() != 200) {
            return null;
        }
        JsonObject msg = JsonParser.parseString((String)r.body()).getAsJsonObject().getAsJsonObject("message");
        if (msg.getAsJsonObject("header").get("status_code").getAsInt() != 200) {
            return null;
        }
        t = msg.getAsJsonObject("body").get("user_token").getAsString();
        if (t == null || t.isBlank() || t.startsWith("UpgradeOnly")) {
            return null;
        }
        mxmToken = t;
        mxmTokenAt = System.currentTimeMillis();
        return t;
    }

    private static JsonObject mxmCall(String method, Map<String, String> params) throws Exception {
        for (int attempt = 0; attempt < 2; ++attempt) {
            String token = LyricsFetcher.mxmToken(attempt > 0);
            if (token == null) {
                return null;
            }
            LinkedHashMap<String, String> q = new LinkedHashMap<String, String>(params);
            q.put("app_id", "web-desktop-app-v1.0");
            q.put("usertoken", token);
            q.put("t", Long.toString(System.nanoTime(), 36));
            Http.Response r = Http.get(MXM + method + "?" + Http.query(q), MXM_HEADERS);
            if (r.code() != 200) {
                return null;
            }
            JsonObject msg = JsonParser.parseString((String)r.body()).getAsJsonObject().getAsJsonObject("message");
            int code = msg.getAsJsonObject("header").get("status_code").getAsInt();
            if (code != 401) {
                if (code != 200 || !msg.get("body").isJsonObject()) {
                    return null;
                }
                return msg.getAsJsonObject("body");
            }
            mxmToken = null;
        }
        return null;
    }

    private static Lyrics musixmatchWords(String[] v, long durationMs) throws Exception {
        JsonObject st;
        Lyrics l;
        JsonObject rs;
        double diff;
        boolean artistOk;
        JsonObject body = LyricsFetcher.mxmCall("matcher.track.get", Map.of("q_artist", v[0], "q_track", v[1]));
        if (body == null || !body.has("track")) {
            return null;
        }
        JsonObject tr = body.getAsJsonObject("track");
        String gotTitle = LyricsFetcher.norm(LyricsFetcher.str(tr, "track_name"));
        String gotArtist = LyricsFetcher.norm(LyricsFetcher.str(tr, "artist_name"));
        String wantTitle = LyricsFetcher.norm(v[1]);
        String wantArtist = LyricsFetcher.norm(v[0]);
        boolean titleOk = !wantTitle.isEmpty() && (gotTitle.equals(wantTitle) || gotTitle.startsWith(wantTitle) || wantTitle.startsWith(gotTitle));
        boolean bl = artistOk = wantArtist.isEmpty() || gotArtist.contains(wantArtist) || wantArtist.contains(gotArtist);
        if (!titleOk || !artistOk) {
            return null;
        }
        if (durationMs > 0L && tr.has("track_length") && tr.get("track_length").getAsDouble() > 0.0 && (diff = Math.abs(tr.get("track_length").getAsDouble() - (double)durationMs / 1000.0)) > LyricsFetcher.maxDiff()) {
            return null;
        }
        String id = tr.get("track_id").getAsString();
        if (tr.has("has_richsync") && tr.get("has_richsync").getAsInt() == 1 && (rs = LyricsFetcher.mxmCall("track.richsync.get", Map.of("track_id", id))) != null && rs.has("richsync") && (l = LyricsFetcher.parseRichsync(rs.getAsJsonObject("richsync").get("richsync_body").getAsString(), durationMs)) != null && LyricsFetcher.sameScript(v, l)) {
            return l;
        }
        if (tr.has("has_subtitles") && tr.get("has_subtitles").getAsInt() == 1 && (st = LyricsFetcher.mxmCall("track.subtitle.get", Map.of("track_id", id, "subtitle_format", "lrc"))) != null && st.has("subtitle") && (l = LyricsFetcher.parseLrc(st.getAsJsonObject("subtitle").get("subtitle_body").getAsString(), durationMs)) != null && l.synced && l.lines.size() >= 3 && LyricsFetcher.sameScript(v, l)) {
            return l;
        }
        return null;
    }

    public static Lyrics parseRichsync(String json, long durationMs) {
        JsonArray arr = JsonParser.parseString((String)json).getAsJsonArray();
        ArrayList<long[]> times = new ArrayList<long[]>();
        ArrayList<String> texts = new ArrayList<String>();
        ArrayList<List<Lyrics.Word>> words = new ArrayList<List<Lyrics.Word>>();
        for (JsonElement e : arr) {
            JsonObject o = e.getAsJsonObject();
            double ts = o.get("ts").getAsDouble();
            double te = o.get("te").getAsDouble();
            JsonArray parts = o.getAsJsonArray("l");
            ArrayList<Lyrics.Word> ws = new ArrayList<Lyrics.Word>();
            StringBuilder cur = new StringBuilder();
            double curStart = -1.0;
            ArrayList<double[]> starts = new ArrayList<double[]>();
            ArrayList<String> wtext = new ArrayList<String>();
            for (JsonElement pe : parts) {
                JsonObject p = pe.getAsJsonObject();
                String c = p.get("c").getAsString();
                double at = ts + p.get("o").getAsDouble();
                if (c.isBlank()) {
                    if (cur.length() <= 0) continue;
                    wtext.add(cur.toString());
                    starts.add(new double[]{curStart});
                    cur.setLength(0);
                    continue;
                }
                if (cur.length() == 0) {
                    curStart = at;
                }
                cur.append(c.strip());
                if (!c.endsWith(" ")) continue;
                wtext.add(cur.toString());
                starts.add(new double[]{curStart});
                cur.setLength(0);
            }
            if (cur.length() > 0) {
                wtext.add(cur.toString());
                starts.add(new double[]{curStart});
            }
            if (wtext.isEmpty()) continue;
            for (int i = 0; i < wtext.size(); ++i) {
                long s0 = Math.round(((double[])starts.get(i))[0] * 1000.0);
                long e0 = i + 1 < wtext.size() ? Math.round(((double[])starts.get(i + 1))[0] * 1000.0) : Math.round(te * 1000.0);
                ws.add(Lyrics.Word.of((String)wtext.get(i), s0, Math.max(s0 + 60L, e0)));
            }
            String text = o.has("x") ? o.get("x").getAsString().trim() : String.join((CharSequence)" ", wtext);
            times.add(new long[]{Math.round(ts * 1000.0), Math.round(te * 1000.0)});
            texts.add(text.isEmpty() ? String.join((CharSequence)" ", wtext) : text);
            words.add(ws);
        }
        if (texts.size() < 3) {
            return null;
        }
        return new Lyrics(times, texts, true, "Musixmatch", durationMs, words);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Lyrics stretched(String[] v, long durationMs) throws Exception {
        if (durationMs <= 0L) {
            return null;
        }
        MAX_DIFF.set(1.0E9);
        try {
            Http.Response r;
            LinkedHashMap<String, String> q = new LinkedHashMap<String, String>();
            q.put("track_name", v[1]);
            if (!v[0].isEmpty()) {
                q.put("artist_name", v[0]);
            }
            if ((r = Http.get("https://lrclib.net/api/search?" + Http.query(q), null)).code() != 200) {
                Lyrics lyrics = null;
                return lyrics;
            }
            String wantTitle = LyricsFetcher.norm(v[1]);
            String wantArtist = LyricsFetcher.norm(v[0]);
            JsonObject best = null;
            double bestScore = Double.MAX_VALUE;
            for (JsonElement e : JsonParser.parseString((String)r.body()).getAsJsonArray()) {
                double k;
                JsonObject o = e.getAsJsonObject();
                if (!o.has("syncedLyrics") || o.get("syncedLyrics").isJsonNull() || !o.has("duration") || o.get("duration").isJsonNull()) continue;
                String name = LyricsFetcher.norm(LyricsFetcher.str(o, "trackName"));
                String art = LyricsFetcher.norm(LyricsFetcher.str(o, "artistName"));
                if (SPEED.matcher(LyricsFetcher.str(o, "trackName")).find() || !name.equals(wantTitle) && !name.startsWith(wantTitle) && !wantTitle.startsWith(name) || !wantArtist.isEmpty() && !art.contains(wantArtist) && !wantArtist.contains(art) || (k = (double)durationMs / (o.get("duration").getAsDouble() * 1000.0)) < 0.55 || k > 1.6) continue;
                int n = name.equals(wantTitle) ? 0 : 10;
                int n2 = REMIX.matcher(LyricsFetcher.str(o, "trackName")).find() ? 50 : 0;
                double score = n + n2;
                if (!(score < bestScore)) continue;
                bestScore = score;
                best = o;
            }
            if (best == null) {
                return null;
            }
            long orig = (long)(best.get("duration").getAsDouble() * 1000.0);
            Lyrics l = LyricsFetcher.parseLrc(best.get("syncedLyrics").getAsString(), orig);
            Lyrics lyrics = l == null ? null : LyricsFetcher.scaled(l, (double)durationMs / (double)orig, durationMs);
            return lyrics;
        }
        finally {
            MAX_DIFF.remove();
        }
    }

    private static Lyrics scaled(Lyrics l, double k, long durationMs) {
        ArrayList<long[]> times = new ArrayList<long[]>();
        ArrayList<String> texts = new ArrayList<String>();
        ArrayList<List<Lyrics.Word>> words = l.wordSynced ? new ArrayList<List<Lyrics.Word>>() : null;
        for (Lyrics.Line line : l.lines) {
            times.add(new long[]{Math.round((double)line.start * k), Math.round((double)line.end * k)});
            texts.add(line.text);
            if (words == null) continue;
            ArrayList<Lyrics.Word> w = new ArrayList<Lyrics.Word>();
            for (Lyrics.Word x : line.words) {
                w.add(Lyrics.Word.of(x.text, Math.round((double)x.start * k), Math.round((double)x.end * k)));
            }
            words.add(w);
        }
        return new Lyrics(times, texts, true, l.source + " (stretched)", durationMs, words);
    }

    private static Lyrics firstSynced(List<Callable<Lyrics>> tasks, long deadline) {
        return LyricsFetcher.firstSynced(tasks, deadline, false);
    }

    private static Lyrics firstSynced(List<Callable<Lyrics>> tasks, long deadline, boolean strict) {
        return LyricsFetcher.firstSynced(tasks, deadline, strict ? 2.0 : 3.0);
    }

    private static Lyrics firstSynced(List<Callable<Lyrics>> tasks, long deadline, double maxDiff) {
        ArrayList<Future<Lyrics>> fs = new ArrayList<Future<Lyrics>>();
        for (Callable<Lyrics> t : tasks) {
            fs.add(POOL.submit(() -> {
                Http.DEADLINE.set(deadline);
                MAX_DIFF.set(maxDiff);
                try {
                    Lyrics l = (Lyrics)t.call();
                    Lyrics lyrics = l != null && l.synced ? l : null;
                    return lyrics;
                }
                catch (Exception e) {
                    Lyrics lyrics = null;
                    return lyrics;
                }
                finally {
                    Http.DEADLINE.remove();
                    MAX_DIFF.remove();
                }
            }));
        }
        Lyrics found = null;
        for (Future future : fs) {
            if (found != null) {
                future.cancel(true);
                continue;
            }
            try {
                long wait = Math.max(1L, deadline - System.currentTimeMillis());
                found = (Lyrics)future.get(wait, TimeUnit.MILLISECONDS);
            }
            catch (Exception exception) {}
        }
        return found;
    }

    private static Lyrics exact(String[] v, long durationMs) throws Exception {
        Http.Response r;
        LinkedHashMap<String, String> q = new LinkedHashMap<String, String>();
        q.put("artist_name", v[0]);
        q.put("track_name", v[1]);
        if (durationMs > 0L) {
            q.put("duration", String.valueOf(Math.round((double)durationMs / 1000.0)));
        }
        if ((r = Http.get("https://lrclib.net/api/get?" + Http.query(q), null)).code() != 200) {
            return null;
        }
        return LyricsFetcher.fromLrclib(JsonParser.parseString((String)r.body()).getAsJsonObject(), durationMs);
    }

    private static Lyrics search(String[] v, long durationMs) throws Exception {
        LinkedHashMap<String, String> q = new LinkedHashMap<String, String>();
        if (v[0].isEmpty()) {
            q.put("q", v[1]);
        } else {
            q.put("track_name", v[1]);
            q.put("artist_name", v[0]);
        }
        return LyricsFetcher.bestOf(Http.get("https://lrclib.net/api/search?" + Http.query(q), null), durationMs, v);
    }

    private static Lyrics bestOf(Http.Response r, long durationMs, String[] v) {
        if (r.code() != 200) {
            return null;
        }
        JsonArray arr = JsonParser.parseString((String)r.body()).getAsJsonArray();
        JsonObject best = null;
        double bestScore = Double.MAX_VALUE;
        String wantTitle = LyricsFetcher.norm(v[1]);
        String wantArtist = LyricsFetcher.norm(TitleCleaner.mainArtist(v[0]));
        for (JsonElement e : arr) {
            boolean artistOk;
            double score;
            JsonObject o = e.getAsJsonObject();
            if (o.has("instrumental") && o.get("instrumental").getAsBoolean()) continue;
            boolean synced = o.has("syncedLyrics") && !o.get("syncedLyrics").isJsonNull();
            double d = score = synced ? 0.0 : 1000.0;
            if (durationMs > 0L && o.has("duration") && !o.get("duration").isJsonNull()) {
                double dd = Math.abs(o.get("duration").getAsDouble() - (double)durationMs / 1000.0);
                if (dd > LyricsFetcher.maxDiff()) continue;
                score += dd * 4.0;
            }
            String name = LyricsFetcher.norm(LyricsFetcher.str(o, "trackName"));
            String art = LyricsFetcher.norm(LyricsFetcher.str(o, "artistName"));
            boolean titleOk = !name.isEmpty() && (name.contains(wantTitle) || wantTitle.contains(name));
            boolean bl = artistOk = wantArtist.isEmpty() || !art.isEmpty() && (art.contains(wantArtist) || wantArtist.contains(art));
            if (!titleOk && !artistOk) continue;
            if (!titleOk) {
                score += 200.0;
            }
            if (!artistOk) {
                score += 300.0;
            }
            if (!(score < bestScore)) continue;
            bestScore = score;
            best = o;
        }
        if (best == null || bestScore > 1200.0) {
            return null;
        }
        return LyricsFetcher.fromLrclib(best, durationMs);
    }

    private static Lyrics bestByTitle(Http.Response r, long durationMs, String[] v) {
        if (r.code() != 200) {
            return null;
        }
        JsonArray arr = JsonParser.parseString((String)r.body()).getAsJsonArray();
        String wantArtist = LyricsFetcher.norm(TitleCleaner.mainArtist(v[0]));
        String wantTitle = LyricsFetcher.norm(v[1]);
        JsonObject best = null;
        double bestScore = Double.MAX_VALUE;
        for (JsonElement e : arr) {
            double dd;
            JsonObject o = e.getAsJsonObject();
            if (o.has("instrumental") && o.get("instrumental").getAsBoolean()) continue;
            String name = LyricsFetcher.str(o, "trackName");
            String art = LyricsFetcher.str(o, "artistName");
            if (!LyricsFetcher.norm(name).equals(wantTitle) && !LyricsFetcher.norm(name).startsWith(wantTitle)) continue;
            boolean artistOk = wantArtist.isEmpty() || LyricsFetcher.norm(art).contains(wantArtist) || wantArtist.contains(LyricsFetcher.norm(art));
            double d = dd = durationMs > 0L && o.has("duration") && !o.get("duration").isJsonNull() ? Math.abs(o.get("duration").getAsDouble() - (double)durationMs / 1000.0) : 0.0;
            if (!artistOk && dd > 2.5 || dd > LyricsFetcher.maxDiff()) continue;
            boolean synced = o.has("syncedLyrics") && !o.get("syncedLyrics").isJsonNull();
            double d2 = (double)(synced ? 0 : 500) + dd * 4.0;
            int n = artistOk ? 0 : 50;
            double score = d2 + (double)n;
            if (!(score < bestScore)) continue;
            bestScore = score;
            best = o;
        }
        return best == null || durationMs > 0L && bestScore > 560.0 ? null : LyricsFetcher.fromLrclib(best, durationMs);
    }

    private static Lyrics netease(String[] v, long durationMs) {
        try {
            String q = (v[0] + " " + v[1]).trim();
            Http.Response r = Http.get("https://music.163.com/api/search/get?" + Http.query(Map.of("s", q, "type", "1", "limit", "10")), NE_HEADERS);
            if (r.code() != 200) {
                return null;
            }
            JsonObject root = JsonParser.parseString((String)r.body()).getAsJsonObject();
            if (!root.has("result") || !root.getAsJsonObject("result").has("songs")) {
                return null;
            }
            String wantTitle = LyricsFetcher.norm(v[1]);
            String wantArtist = LyricsFetcher.norm(TitleCleaner.mainArtist(v[0]));
            long bestId = -1L;
            double bestScore = Double.MAX_VALUE;
            for (JsonElement e : root.getAsJsonObject("result").getAsJsonArray("songs")) {
                double dd;
                JsonObject o = e.getAsJsonObject();
                String name = LyricsFetcher.norm(LyricsFetcher.str(o, "name"));
                if (!name.equals(wantTitle) && !name.startsWith(wantTitle) && !wantTitle.startsWith(name)) continue;
                StringBuilder arts = new StringBuilder();
                if (o.has("artists")) {
                    for (JsonElement a : o.getAsJsonArray("artists")) {
                        arts.append(LyricsFetcher.norm(LyricsFetcher.str(a.getAsJsonObject(), "name"))).append('|');
                    }
                }
                boolean artistOk = wantArtist.isEmpty() || arts.toString().contains(wantArtist);
                double d = dd = durationMs > 0L && o.has("duration") ? (double)Math.abs(o.get("duration").getAsLong() - durationMs) / 1000.0 : 0.0;
                if (!artistOk && dd > 2.5 || dd > LyricsFetcher.maxDiff()) continue;
                double d2 = dd * 4.0 + (double)(artistOk ? 0 : 50);
                int n = name.equals(wantTitle) ? 0 : 20;
                double score = d2 + (double)n;
                if (!(score < bestScore)) continue;
                bestScore = score;
                bestId = o.get("id").getAsLong();
            }
            if (bestId < 0L) {
                return null;
            }
            Http.Response lr = Http.get("https://music.163.com/api/song/lyric?id=" + bestId + "&lv=1&kv=1&tv=-1", NE_HEADERS);
            if (lr.code() != 200) {
                return null;
            }
            JsonObject lo = JsonParser.parseString((String)lr.body()).getAsJsonObject();
            if (!lo.has("lrc") || !lo.getAsJsonObject("lrc").has("lyric")) {
                return null;
            }
            String lrc = lo.getAsJsonObject("lrc").get("lyric").getAsString();
            StringBuilder clean = new StringBuilder();
            for (String line : lrc.split("\\r?\\n")) {
                String text = line.replaceAll("^(\\[[^\\]]*\\])+", "");
                if (text.matches(".*[\\u4e00-\\u9fff].*[:\uff1a].*") || text.matches("(?i)\\s*(lyrics|composer|producer|arranger|written by)\\s*[:\uff1a].*")) continue;
                clean.append(line).append('\n');
            }
            Lyrics l = LyricsFetcher.parseLrc(clean.toString(), durationMs);
            if (l == null || l.lines.size() < 3) {
                return null;
            }
            return l;
        }
        catch (Exception e) {
            return null;
        }
    }

    private static long neteaseId(String[] v, long durationMs) throws Exception {
        String q = (v[0] + " " + v[1]).trim();
        Http.Response r = Http.get("https://music.163.com/api/search/get?" + Http.query(Map.of("s", q, "type", "1", "limit", "10")), NE_HEADERS);
        if (r.code() != 200) {
            return -1L;
        }
        JsonObject root = JsonParser.parseString((String)r.body()).getAsJsonObject();
        if (!root.has("result") || !root.getAsJsonObject("result").has("songs")) {
            return -1L;
        }
        String wantTitle = LyricsFetcher.norm(v[1]);
        String wantArtist = LyricsFetcher.norm(TitleCleaner.mainArtist(v[0]));
        long bestId = -1L;
        double bestScore = Double.MAX_VALUE;
        for (JsonElement e : root.getAsJsonObject("result").getAsJsonArray("songs")) {
            double dd;
            JsonObject o = e.getAsJsonObject();
            String name = LyricsFetcher.norm(LyricsFetcher.str(o, "name"));
            if (!name.equals(wantTitle) && !name.startsWith(wantTitle) && !wantTitle.startsWith(name)) continue;
            StringBuilder arts = new StringBuilder();
            if (o.has("artists")) {
                for (JsonElement a : o.getAsJsonArray("artists")) {
                    arts.append(LyricsFetcher.norm(LyricsFetcher.str(a.getAsJsonObject(), "name"))).append('|');
                }
            }
            boolean artistOk = wantArtist.isEmpty() || arts.toString().contains(wantArtist);
            double d = dd = durationMs > 0L && o.has("duration") ? (double)Math.abs(o.get("duration").getAsLong() - durationMs) / 1000.0 : 0.0;
            if (!artistOk || dd > Math.min(LyricsFetcher.maxDiff(), 6.0)) continue;
            int n = name.equals(wantTitle) ? 0 : 20;
            double score = dd * 4.0 + (double)n;
            if (!(score < bestScore)) continue;
            bestScore = score;
            bestId = o.get("id").getAsLong();
        }
        return bestId;
    }

    private static Lyrics neteaseWords(String[] v, long durationMs) throws Exception {
        long id = LyricsFetcher.neteaseId(v, durationMs);
        if (id < 0L) {
            return null;
        }
        Http.Response lr = Http.get("https://music.163.com/api/song/lyric/v1?id=" + id + "&cp=false&tv=0&lv=0&rv=0&kv=0&yv=0&ytv=0&yrv=0", NE_HEADERS);
        if (lr.code() != 200) {
            return null;
        }
        JsonObject lo = JsonParser.parseString((String)lr.body()).getAsJsonObject();
        if (!lo.has("yrc") || !lo.getAsJsonObject("yrc").has("lyric")) {
            return null;
        }
        Lyrics y = LyricsFetcher.parseYrc(lo.getAsJsonObject("yrc").get("lyric").getAsString(), durationMs);
        return y != null && LyricsFetcher.sameScript(v, y) ? y : null;
    }

    public static Lyrics parseYrc(String yrc, long durationMs) {
        ArrayList<long[]> times = new ArrayList<long[]>();
        ArrayList<String> texts = new ArrayList<String>();
        ArrayList<List<Lyrics.Word>> words = new ArrayList<List<Lyrics.Word>>();
        for (String raw : yrc.split("\\r?\\n")) {
            String line;
            Matcher m = YRC_LINE.matcher(raw.trim());
            if (!m.matches()) continue;
            long ls = Long.parseLong(m.group(1));
            long ld = Long.parseLong(m.group(2));
            Matcher w = YRC_WORD.matcher(m.group(3));
            ArrayList<Lyrics.Word> lw = new ArrayList<Lyrics.Word>();
            StringBuilder text = new StringBuilder();
            StringBuilder cur = new StringBuilder();
            long cs = -1L;
            long ce = -1L;
            while (w.find()) {
                long ws = Long.parseLong(w.group(1));
                long wd = Long.parseLong(w.group(2));
                String piece = w.group(3);
                if (cs < 0L) {
                    cs = ws;
                }
                cur.append(piece);
                ce = ws + wd;
                if (piece.endsWith(" ") || piece.matches(".*[\\u3000-\\u9fff\\uac00-\\ud7af].*")) {
                    String t = cur.toString().trim();
                    if (!t.isEmpty()) {
                        lw.add(Lyrics.Word.of(t, cs, ce));
                    }
                    cur.setLength(0);
                    cs = -1L;
                }
                text.append(piece);
            }
            String t = cur.toString().trim();
            if (!t.isEmpty()) {
                lw.add(Lyrics.Word.of(t, cs, ce));
            }
            if ((line = text.toString().trim()).isEmpty() || lw.isEmpty() || line.matches(".*[\\u4e00-\\u9fff].*[:\uff1a].*")) continue;
            times.add(new long[]{ls, ls + ld});
            texts.add(line);
            words.add(lw);
        }
        if (texts.size() < 3) {
            return null;
        }
        return new Lyrics(times, texts, true, "netease words", durationMs, words);
    }

    private static String str(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : "";
    }

    static boolean sameScript(String[] v, Lyrics l) {
        String title = v[0] + " " + v[1];
        int tl = 0;
        int tn = 0;
        for (int i = 0; i < title.length(); ++i) {
            char ch = title.charAt(i);
            if (!Character.isLetter(ch)) continue;
            if (Character.UnicodeScript.of(ch) == Character.UnicodeScript.LATIN) {
                ++tl;
                continue;
            }
            ++tn;
        }
        if (tl == 0 || tn > tl) {
            return true;
        }
        int ll = 0;
        int ln = 0;
        for (Lyrics.Line line : l.lines) {
            for (int i = 0; i < line.text.length(); ++i) {
                char ch = line.text.charAt(i);
                if (!Character.isLetter(ch)) continue;
                if (Character.UnicodeScript.of(ch) == Character.UnicodeScript.LATIN) {
                    ++ll;
                    continue;
                }
                ++ln;
            }
        }
        return ll + ln == 0 || (double)ln <= (double)(ll + ln) * 0.5;
    }

    private static String norm(String s) {
        String n = TitleCleaner.fold(s).replaceAll("[^\\p{L}\\p{N}]+", "");
        if (n.isEmpty() && s != null) {
            n = s.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
        }
        return n;
    }

    private static Lyrics fromLrclib(JsonObject o, long durationMs) {
        Lyrics l;
        if (durationMs <= 0L && o.has("duration") && !o.get("duration").isJsonNull()) {
            durationMs = (long)(o.get("duration").getAsDouble() * 1000.0);
        }
        if (o.has("syncedLyrics") && !o.get("syncedLyrics").isJsonNull() && (l = LyricsFetcher.parseLrc(o.get("syncedLyrics").getAsString(), durationMs)) != null) {
            return l;
        }
        if (o.has("plainLyrics") && !o.get("plainLyrics").isJsonNull()) {
            return LyricsFetcher.spread(o.get("plainLyrics").getAsString(), durationMs, "lrclib");
        }
        return null;
    }

    public static Lyrics parseLrc(String lrc, long durationMs) {
        ArrayList<long[]> times = new ArrayList<long[]>();
        ArrayList<String> texts = new ArrayList<String>();
        ArrayList<Object[]> entries = new ArrayList<Object[]>();
        for (String raw : lrc.split("\\r?\\n")) {
            Matcher m = LRC.matcher(raw);
            ArrayList<Long> stamps = new ArrayList<Long>();
            int end = 0;
            while (m.find() && m.start() == end) {
                long ms = Long.parseLong(m.group(1)) * 60000L + Long.parseLong(m.group(2)) * 1000L;
                if (m.group(3) != null) {
                    String f = m.group(3);
                    ms += f.length() == 1 ? Long.parseLong(f) * 100L : (f.length() == 2 ? Long.parseLong(f) * 10L : Long.parseLong(f));
                }
                stamps.add(ms);
                end = m.end();
            }
            if (stamps.isEmpty()) continue;
            String body = raw.substring(end);
            String text = body.replaceAll("<\\d+:\\d+(?:\\.\\d+)?>", "").trim();
            List<Lyrics.Word> words = LyricsFetcher.enhancedWords(body);
            Iterator iterator = stamps.iterator();
            while (iterator.hasNext()) {
                long s = (Long)iterator.next();
                entries.add(new Object[]{s, text, stamps.size() == 1 ? words : null});
            }
        }
        entries.sort((a, b) -> Long.compare((Long)a[0], (Long)b[0]));
        ArrayList<List<Lyrics.Word>> wl = new ArrayList<List<Lyrics.Word>>();
        boolean anyWords = false;
        for (int i = 0; i < entries.size(); ++i) {
            long[] lArray;
            long e;
            String t = (String)((Object[])entries.get(i))[1];
            if (t.isEmpty() || t.equals("\u266a")) continue;
            long s = (Long)((Object[])entries.get(i))[0];
            long l = e = i + 1 < entries.size() ? (Long)((Object[])entries.get(i + 1))[0] : -1L;
            if (e > s) {
                long[] lArray2 = new long[2];
                lArray2[0] = s;
                lArray = lArray2;
                lArray2[1] = e;
            } else {
                long[] lArray3 = new long[1];
                lArray = lArray3;
                lArray3[0] = s;
            }
            times.add(lArray);
            texts.add(t);
            List w = (List)((Object[])entries.get(i))[2];
            if (w != null && !w.isEmpty()) {
                anyWords = true;
                Lyrics.Word last = (Lyrics.Word)w.get(w.size() - 1);
                if (last.end <= last.start) {
                    w.set(w.size() - 1, Lyrics.Word.of(last.text, last.start, e > last.start ? Math.min(e, last.start + 1500L) : last.start + 600L));
                }
            }
            wl.add(w);
        }
        if (texts.isEmpty()) {
            return null;
        }
        return new Lyrics(times, texts, true, "lrclib", durationMs, anyWords ? wl : null);
    }

    private static List<Lyrics.Word> enhancedWords(String body) {
        Matcher m = WORD_TAG.matcher(body);
        ArrayList<long[]> at = new ArrayList<long[]>();
        ArrayList<int[]> pos = new ArrayList<int[]>();
        while (m.find()) {
            long ms = Long.parseLong(m.group(1)) * 60000L + Long.parseLong(m.group(2)) * 1000L;
            if (m.group(3) != null) {
                String f = m.group(3);
                ms += f.length() == 1 ? Long.parseLong(f) * 100L : (f.length() == 2 ? Long.parseLong(f) * 10L : Long.parseLong(f.substring(0, 3)));
            }
            at.add(new long[]{ms});
            pos.add(new int[]{m.start(), m.end()});
        }
        if (at.size() < 2) {
            return null;
        }
        ArrayList<Lyrics.Word> out = new ArrayList<Lyrics.Word>();
        for (int i = 0; i < at.size(); ++i) {
            int to;
            int from = ((int[])pos.get(i))[1];
            String seg = body.substring(from, to = i + 1 < pos.size() ? ((int[])pos.get(i + 1))[0] : body.length()).trim();
            if (seg.isEmpty()) continue;
            String[] parts = seg.split("\\s+");
            long st = ((long[])at.get(i))[0];
            long en = i + 1 < at.size() ? ((long[])at.get(i + 1))[0] : st + (long)parts.length * 450L;
            for (int k = 0; k < parts.length; ++k) {
                long a = st + (en - st) * (long)k / (long)parts.length;
                long b = st + (en - st) * (long)(k + 1) / (long)parts.length;
                out.add(Lyrics.Word.of(parts[k], a, b));
            }
        }
        return out.isEmpty() ? null : out;
    }

    public static Lyrics spread(String plain, long durationMs, String source) {
        ArrayList<String> lines = new ArrayList<String>();
        for (String s : plain.split("\\r?\\n")) {
            String t = s.trim();
            if (t.isEmpty() || t.startsWith("[")) continue;
            lines.add(t);
        }
        if (lines.isEmpty()) {
            return null;
        }
        long start = 9000L;
        long end = durationMs > 30000L ? durationMs - 6000L : start + (long)lines.size() * 3600L;
        double step = Math.max(1800.0, (double)(end - start) / (double)lines.size());
        ArrayList<long[]> times = new ArrayList<long[]>();
        for (int i = 0; i < lines.size(); ++i) {
            times.add(new long[]{start + (long)((double)i * step)});
        }
        return new Lyrics(times, lines, false, source, durationMs);
    }

    static {
        NE_HEADERS = Map.of("Referer", "https://music.163.com/", "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36");
        YRC_LINE = Pattern.compile("^\\[(\\d+),(\\d+)\\](.*)$");
        YRC_WORD = Pattern.compile("\\((\\d+),(\\d+),\\d+\\)([^(]*)");
        WORD_TAG = Pattern.compile("<(\\d+):(\\d+)(?:\\.(\\d+))?>");
    }
}


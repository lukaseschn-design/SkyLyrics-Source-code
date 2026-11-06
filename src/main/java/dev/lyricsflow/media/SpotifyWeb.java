/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.class_156
 */
package dev.lyricsflow.media;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.lyricsflow.SkyLyrics;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.media.Track;
import dev.lyricsflow.util.Http;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_156;

public final class SpotifyWeb {
    public static final String REDIRECT = "http://127.0.0.1:8789/callback";
    private static final String SCOPES = "user-read-playback-state user-read-currently-playing user-modify-playback-state";
    private static String clientId = "";
    private static String refresh = "";
    private static String access = "";
    private static long accessUntil;
    private static volatile String state;
    private static volatile String error;
    private static boolean loaded;
    private static volatile String sTitle;
    private static volatile String sArtist;
    private static volatile String sAlbum;
    private static volatile String sCover;
    private static volatile long sPos;
    private static volatile long sAt;
    private static volatile long sDur;
    private static volatile boolean sPlaying;
    private static long nextPoll;
    private static int fails;
    private static boolean threadStarted;

    private SpotifyWeb() {
    }

    public static synchronized String clientId() {
        SpotifyWeb.load();
        return clientId;
    }

    public static synchronized void setClientId(String id) {
        SpotifyWeb.load();
        String string = id = id == null ? "" : id.trim();
        if (id.equals(clientId)) {
            return;
        }
        clientId = id;
        refresh = "";
        access = "";
        accessUntil = 0L;
        state = "off";
        SpotifyWeb.save();
    }

    public static boolean connected() {
        SpotifyWeb.load();
        return !refresh.isEmpty();
    }

    public static String state() {
        SpotifyWeb.load();
        return !refresh.isEmpty() && !"error".equals(state) ? "on" : state;
    }

    public static String error() {
        return error;
    }

    public static synchronized void disconnect() {
        SpotifyWeb.load();
        refresh = "";
        access = "";
        accessUntil = 0L;
        state = "off";
        error = "";
        sPos = -1L;
        SpotifyWeb.save();
    }

    private static Path file() {
        return ConfigManager.dir().resolve("spotify.json");
    }

    private static synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        try {
            if (!Files.exists(SpotifyWeb.file(), new LinkOption[0])) {
                return;
            }
            JsonObject o = JsonParser.parseString((String)Files.readString(SpotifyWeb.file())).getAsJsonObject();
            if (o.has("clientId")) {
                clientId = o.get("clientId").getAsString();
            }
            if (o.has("refreshToken")) {
                refresh = o.get("refreshToken").getAsString();
            }
            if (!refresh.isEmpty()) {
                state = "on";
            }
        }
        catch (Throwable e) {
            SkyLyrics.LOG.warn("[SkyLyrics] Could not read spotify.json: {}", (Object)e.toString());
        }
    }

    private static synchronized void save() {
        try {
            JsonObject o = new JsonObject();
            o.addProperty("_info", "Spotify login of SkyLyrics (only used to read the exact song position). Delete this file to log out.");
            o.addProperty("clientId", clientId);
            o.addProperty("refreshToken", refresh);
            Files.writeString(SpotifyWeb.file(), (CharSequence)o.toString(), new OpenOption[0]);
        }
        catch (Throwable e) {
            SkyLyrics.LOG.warn("[SkyLyrics] Could not save spotify.json: {}", (Object)e.toString());
        }
    }

    public static void login() {
        SpotifyWeb.load();
        if (clientId.isEmpty()) {
            state = "error";
            error = "no_client_id";
            return;
        }
        if ("waiting".equals(state)) {
            return;
        }
        state = "waiting";
        error = "";
        Thread t = new Thread(SpotifyWeb::loginFlow, "SkyLyrics Spotify login");
        t.setDaemon(true);
        t.start();
    }

    private static void loginFlow() {
        try (ServerSocket server = new ServerSocket(8789, 4, InetAddress.getByName("127.0.0.1"));){
            server.setSoTimeout(180000);
            byte[] rnd = new byte[48];
            SecureRandom sr = new SecureRandom();
            sr.nextBytes(rnd);
            String verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(rnd);
            String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII)));
            byte[] st = new byte[12];
            sr.nextBytes(st);
            String stateTag = Base64.getUrlEncoder().withoutPadding().encodeToString(st);
            String url = "https://accounts.spotify.com/authorize?" + Http.query(Map.of("client_id", clientId, "response_type", "code", "redirect_uri", REDIRECT, "scope", SCOPES, "code_challenge_method", "S256", "code_challenge", challenge, "state", stateTag));
            class_156.method_668().method_670(url);
            String code = null;
            String err = null;
            while (code == null && err == null) {
                Socket s = server.accept();
                try {
                    String path;
                    s.setSoTimeout(5000);
                    String first = SpotifyWeb.readLine(s.getInputStream());
                    String string = path = first.split(" ").length > 1 ? first.split(" ")[1] : "";
                    if (!path.startsWith("/callback")) {
                        SpotifyWeb.reply(s, 404, "Not found");
                        continue;
                    }
                    Map<String, String> q = SpotifyWeb.parseQuery(path.contains("?") ? path.substring(path.indexOf(63) + 1) : "");
                    if (!stateTag.equals(q.get("state"))) {
                        SpotifyWeb.reply(s, 400, "Wrong state, try again from the game.");
                        continue;
                    }
                    if (q.containsKey("error")) {
                        err = q.get("error");
                    } else {
                        code = q.get("code");
                    }
                    SpotifyWeb.reply(s, 200, (String)(err == null ? "SkyLyrics is connected to Spotify. You can close this tab and go back to the game." : "Spotify login was cancelled (" + err + ")."));
                }
                finally {
                    if (s == null) continue;
                    s.close();
                }
            }
            if (err != null) {
                throw new IllegalStateException(err);
            }
            Http.Response r = Http.send("POST", "https://accounts.spotify.com/api/token", null, Http.query(Map.of("grant_type", "authorization_code", "code", code, "redirect_uri", REDIRECT, "client_id", clientId, "code_verifier", verifier)));
            if (r.code() != 200) {
                throw new IllegalStateException("token " + r.code() + " " + r.body());
            }
            SpotifyWeb.takeTokens(r.body());
            state = "on";
            error = "";
            fails = 0;
            SkyLyrics.LOG.info("[SkyLyrics] Connected to Spotify.");
        }
        catch (BindException e) {
            state = "error";
            error = "port";
        }
        catch (SocketTimeoutException e) {
            state = "error";
            error = "timeout";
        }
        catch (Throwable e) {
            SkyLyrics.LOG.warn("[SkyLyrics] Spotify login failed: {}", (Object)e.toString());
            state = "error";
            error = "login";
        }
    }

    private static synchronized void takeTokens(String body) {
        JsonObject o = JsonParser.parseString((String)body).getAsJsonObject();
        access = o.get("access_token").getAsString();
        accessUntil = System.currentTimeMillis() + (o.has("expires_in") ? o.get("expires_in").getAsLong() : 3600L) * 1000L - 60000L;
        if (o.has("refresh_token")) {
            refresh = o.get("refresh_token").getAsString();
        }
        SpotifyWeb.save();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static String token() throws Exception {
        Class<SpotifyWeb> clazz = SpotifyWeb.class;
        synchronized (SpotifyWeb.class) {
            if (!access.isEmpty() && System.currentTimeMillis() < accessUntil) {
                // ** MonitorExit[var0] (shouldn't be in output)
                return access;
            }
            // ** MonitorExit[var0] (shouldn't be in output)
            Http.Response r = Http.send("POST", "https://accounts.spotify.com/api/token", null, Http.query(Map.of("grant_type", "refresh_token", "refresh_token", refresh, "client_id", clientId)));
            if (r.code() == 400 || r.code() == 401) {
                Class<SpotifyWeb> clazz2 = SpotifyWeb.class;
                synchronized (SpotifyWeb.class) {
                    refresh = "";
                    access = "";
                    SpotifyWeb.save();
                    // ** MonitorExit[var1_2] (shouldn't be in output)
                    state = "error";
                    error = "expired";
                    return null;
                }
            }
            if (r.code() != 200) {
                return null;
            }
            SpotifyWeb.takeTokens(r.body());
            return access;
        }
    }

    static synchronized void start() {
        if (threadStarted) {
            return;
        }
        threadStarted = true;
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    SpotifyWeb.poll();
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
                try {
                    Thread.sleep(100L);
                }
                catch (InterruptedException e) {
                    return;
                }
            }
        }, "SkyLyrics Spotify position");
        t.setDaemon(true);
        t.start();
    }

    static boolean setRepeat(int mode) {
        SpotifyWeb.load();
        if (refresh.isEmpty()) {
            return false;
        }
        try {
            String tok = SpotifyWeb.token();
            if (tok == null) {
                return false;
            }
            String st = mode == 1 ? "track" : (mode == 2 ? "context" : "off");
            Http.Response r = Http.send("PUT", "https://api.spotify.com/v1/me/player/repeat?state=" + st, Map.of("Authorization", "Bearer " + tok), null);
            return r.code() / 100 == 2;
        }
        catch (Throwable e) {
            return false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void poll() {
        SpotifyWeb.load();
        if (refresh.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now < nextPoll) {
            return;
        }
        nextPoll = now + 1000L;
        try {
            String tok = SpotifyWeb.token();
            if (tok == null) {
                return;
            }
            long t0 = System.nanoTime();
            Http.Response r = Http.get("https://api.spotify.com/v1/me/player/currently-playing", Map.of("Authorization", "Bearer " + tok));
            long t1 = System.nanoTime();
            if (r.code() == 204) {
                sPos = -1L;
                return;
            }
            if (r.code() == 429) {
                nextPoll = now + 10000L;
                return;
            }
            if (r.code() == 401) {
                Class<SpotifyWeb> clazz = SpotifyWeb.class;
                synchronized (SpotifyWeb.class) {
                    access = "";
                    // ** MonitorExit[var8_6] (shouldn't be in output)
                    return;
                }
            }
            if (r.code() != 200) {
                nextPoll = now + Math.min(30000L, 2000L * (long)(++fails));
                return;
            }
            fails = 0;
            JsonObject o = JsonParser.parseString((String)r.body()).getAsJsonObject();
            if (!o.has("item") || o.get("item").isJsonNull() || !o.has("progress_ms") || o.get("progress_ms").isJsonNull()) {
                sPos = -1L;
                return;
            }
            JsonObject item = o.getAsJsonObject("item");
            String artist = "";
            if (item.has("artists") && item.getAsJsonArray("artists").size() > 0) {
                artist = item.getAsJsonArray("artists").get(0).getAsJsonObject().get("name").getAsString();
            } else if (item.has("show")) {
                artist = item.getAsJsonObject("show").get("name").getAsString();
            }
            boolean playing = o.has("is_playing") && o.get("is_playing").getAsBoolean();
            long rtt = (t1 - t0) / 1000000L;
            long pos = o.get("progress_ms").getAsLong() + (playing ? rtt / 2L : 0L);
            sTitle = item.get("name").getAsString();
            sArtist = artist;
            String album = "";
            String cover = "";
            if (item.has("album") && item.get("album").isJsonObject()) {
                JsonObject al = item.getAsJsonObject("album");
                if (al.has("name")) {
                    album = al.get("name").getAsString();
                }
                if (al.has("images") && al.getAsJsonArray("images").size() > 0) {
                    cover = al.getAsJsonArray("images").get(0).getAsJsonObject().get("url").getAsString();
                }
            }
            sAlbum = album;
            sCover = cover;
            sDur = item.has("duration_ms") ? item.get("duration_ms").getAsLong() : 0L;
            sPlaying = playing;
            sAt = t1;
            sPos = pos;
            if (!"on".equals(state)) {
                state = "on";
                error = "";
            }
        }
        catch (Throwable e) {
            nextPoll = now + Math.min(30000L, 2000L * (long)(++fails));
        }
    }

    static long positionFor(Track t) {
        long p = sPos;
        if (p < 0L || t == null) {
            return -1L;
        }
        long age = (System.nanoTime() - sAt) / 1000000L;
        if (age > 4000L) {
            return -1L;
        }
        if (!SpotifyWeb.same(t.title, sTitle)) {
            return -1L;
        }
        if (sDur > 0L && t.durationMs > 0L && Math.abs(sDur - t.durationMs) > 3000L) {
            return -1L;
        }
        return sPlaying ? p + age : p;
    }

    static Track nowPlaying() {
        SpotifyWeb.start();
        long p = sPos;
        if (p < 0L || sTitle.isEmpty()) {
            return null;
        }
        long age = (System.nanoTime() - sAt) / 1000000L;
        if (age > 6000L) {
            return null;
        }
        long pos = sPlaying ? p + age : p;
        return new Track(sTitle, sArtist, sAlbum, sDur, pos, sPlaying, "Spotify", pos);
    }

    static String coverUrl() {
        return sCover;
    }

    static void command(String method, String path) {
        try {
            String tok = SpotifyWeb.token();
            if (tok == null) {
                return;
            }
            Http.send(method, "https://api.spotify.com/v1/me/player/" + path, Map.of("Authorization", "Bearer " + tok), null);
            nextPoll = 0L;
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    static boolean isPlaying() {
        return sPlaying;
    }

    static long sampleAt() {
        return sAt;
    }

    private static boolean same(String a, String b) {
        String x = SpotifyWeb.norm(a);
        String y = SpotifyWeb.norm(b);
        return !x.isEmpty() && (x.equals(y) || x.startsWith(y) || y.startsWith(x));
    }

    private static String norm(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).replaceAll("[\\s\\p{Punct}]+", "");
    }

    private static String readLine(InputStream in) throws Exception {
        int c;
        StringBuilder sb = new StringBuilder();
        while ((c = in.read()) != -1 && c != 10 && sb.length() < 8192) {
            if (c == 13) continue;
            sb.append((char)c);
        }
        return sb.toString();
    }

    private static void reply(Socket s, int code, String text) throws Exception {
        String html = "<!doctype html><meta charset=utf-8><title>SkyLyrics</title><body style=\"background:#14111c;color:#eee;font:16px sans-serif;display:grid;place-items:center;height:90vh\"><p>" + text + "</p>";
        byte[] b = html.getBytes(StandardCharsets.UTF_8);
        OutputStream out = s.getOutputStream();
        out.write(("HTTP/1.1 " + code + " OK\r\nContent-Type: text/html; charset=utf-8\r\nContent-Length: " + b.length + "\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
        out.write(b);
        out.flush();
    }

    private static Map<String, String> parseQuery(String q) {
        HashMap<String, String> m = new HashMap<String, String>();
        for (String part : q.split("&")) {
            int i = part.indexOf(61);
            if (i <= 0) continue;
            m.put(URLDecoder.decode(part.substring(0, i), StandardCharsets.UTF_8), URLDecoder.decode(part.substring(i + 1), StandardCharsets.UTF_8));
        }
        return m;
    }

    static {
        state = "off";
        error = "";
        sTitle = "";
        sArtist = "";
        sAlbum = "";
        sCover = "";
        sPos = -1L;
    }
}


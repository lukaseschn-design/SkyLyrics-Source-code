/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.class_310
 */
package dev.lyricsflow.media;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.lyricsflow.LyricsFlow;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.config.LFConfig;
import dev.lyricsflow.util.Http;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import net.minecraft.class_310;

public final class CosmeticSync {
    private static final String TOPIC = "https://ntfy.sh/lyricsflow-cosmetics-v1-7f3a9c";
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "SkyLyrics cosmetics");
        t.setDaemon(true);
        return t;
    });
    private static final Map<UUID, String> others = new ConcurrentHashMap<UUID, String>();
    private static final ExecutorService SEND = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "SkyLyrics cosmetics send");
        t.setDaemon(true);
        return t;
    });
    private static volatile boolean busy;
    private static volatile boolean sending;
    private static volatile String sent;
    private static long sentAt;
    private static long readAt;
    private static String pending;
    private static long pendingAt;
    private static volatile boolean live;
    private static volatile boolean liveStarted;

    private CosmeticSync() {
    }

    public static String of(UUID id) {
        return others.getOrDefault(id, "none");
    }

    public static void tick(class_310 mc) {
        LFConfig c = ConfigManager.get();
        if (mc.field_1724 == null || mc.field_1687 == null || !c.enabled) {
            return;
        }
        CosmeticSync.startLive(mc.method_1548().method_44717());
        long now = System.currentTimeMillis();
        ArrayList<String> w = new ArrayList<String>();
        for (String x : c.worn()) {
            if (!x.matches("[a-z0-9_]{1,40}")) continue;
            w.add(x);
        }
        String mine = w.isEmpty() ? "none" : String.join((CharSequence)",", w);
        UUID me = mc.method_1548().method_44717();
        if (!(me == null || mine.equals(sent) && now - sentAt <= 18000000L)) {
            if (!mine.equals(pending)) {
                pending = mine;
                pendingAt = now;
                return;
            }
            if (now - pendingAt < 250L || sending) {
                return;
            }
            sending = true;
            sentAt = now;
            String send = mine;
            String id = me.toString().replace("-", "");
            SEND.execute(() -> {
                try {
                    Http.Response r = Http.send("POST", TOPIC, Map.of("Content-Type", "text/plain"), "lf1 " + id + " " + send);
                    if (r.code() == 200) {
                        sent = send;
                    }
                }
                catch (Throwable e) {
                    LyricsFlow.LOG.info("[SkyLyrics] Could not share the cosmetic: {}", (Object)e.toString());
                }
                finally {
                    sending = false;
                }
            });
        }
        if (busy) {
            return;
        }
        if (now - readAt > (live ? 60000L : 5000L)) {
            readAt = now;
            busy = true;
            EXEC.execute(() -> {
                try {
                    CosmeticSync.read(me);
                }
                finally {
                    busy = false;
                }
            });
        }
    }

    private static synchronized void startLive(UUID me) {
        if (liveStarted || me == null) {
            return;
        }
        liveStarted = true;
        Thread t = new Thread(() -> {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).build();
            long wait = 2000L;
            while (true) {
                try {
                    HttpRequest req = HttpRequest.newBuilder(URI.create("https://ntfy.sh/lyricsflow-cosmetics-v1-7f3a9c/json?since=12h")).header("User-Agent", "SkyLyrics/1.0 (Minecraft mod)").GET().build();
                    HttpResponse<Stream<String>> r = client.send(req, HttpResponse.BodyHandlers.ofLines());
                    if (r.statusCode() == 200) {
                        live = true;
                        wait = 2000L;
                        r.body().forEach(line -> CosmeticSync.take(line, me));
                    }
                }
                catch (Throwable req) {
                }
                finally {
                    live = false;
                }
                try {
                    Thread.sleep(wait);
                }
                catch (InterruptedException e) {
                    return;
                }
                wait = Math.min(60000L, wait * 2L);
            }
        }, "SkyLyrics cosmetics live");
        t.setDaemon(true);
        t.start();
    }

    private static void take(String line, UUID me) {
        try {
            if (line == null || line.isBlank()) {
                return;
            }
            JsonObject o = JsonParser.parseString((String)line).getAsJsonObject();
            if (!(o.has("event") && "message".equals(o.get("event").getAsString()) && o.has("message"))) {
                return;
            }
            String[] p = o.get("message").getAsString().trim().split(" ");
            if (!(p.length == 3 && p[0].equals("lf1") && p[1].matches("[0-9a-f]{32}") && p[2].matches("[a-z0-9_,]{1,220}"))) {
                return;
            }
            UUID id = UUID.fromString(p[1].replaceFirst("(.{8})(.{4})(.{4})(.{4})(.{12})", "$1-$2-$3-$4-$5"));
            if (id.equals(me)) {
                return;
            }
            if ("none".equals(p[2])) {
                others.remove(id);
            } else {
                others.put(id, p[2]);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void read(UUID me) {
        try {
            Http.Response r = Http.get("https://ntfy.sh/lyricsflow-cosmetics-v1-7f3a9c/json?poll=1&since=12h", null);
            if (r.code() != 200) {
                return;
            }
            ConcurrentHashMap<UUID, String> next = new ConcurrentHashMap<UUID, String>();
            for (String line : r.body().split("\n")) {
                UUID id;
                String[] p;
                JsonObject o;
                if (line.isBlank() || !(o = JsonParser.parseString((String)line).getAsJsonObject()).has("event") || !"message".equals(o.get("event").getAsString()) || !o.has("message") || (p = o.get("message").getAsString().trim().split(" ")).length != 3 || !p[0].equals("lf1") || !p[1].matches("[0-9a-f]{32}") || !p[2].matches("[a-z0-9_,]{1,220}") || (id = UUID.fromString(p[1].replaceFirst("(.{8})(.{4})(.{4})(.{4})(.{12})", "$1-$2-$3-$4-$5"))).equals(me)) continue;
                next.put(id, p[2]);
            }
            next.values().removeIf("none"::equals);
            others.keySet().retainAll(next.keySet());
            others.putAll(next);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    static {
        sent = null;
    }
}


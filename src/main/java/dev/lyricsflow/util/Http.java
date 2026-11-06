/*
 * Decompiled with CFR 0.152.
 */
package dev.lyricsflow.util;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeoutException;

public final class Http {
    public static final String USER_AGENT = "SkyLyrics/1.0 (Minecraft mod)";
    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4L)).followRedirects(HttpClient.Redirect.NORMAL).build();
    public static final ThreadLocal<Long> DEADLINE = new ThreadLocal();

    private Http() {
    }

    public static String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }

    public static String query(Map<String, String> params) {
        StringJoiner j = new StringJoiner("&");
        params.forEach((k, v) -> {
            if (v != null) {
                j.add(Http.enc(k) + "=" + Http.enc(v));
            }
        });
        return j.toString();
    }

    public static Response get(String url, Map<String, String> headers) throws Exception {
        long ms = 8000L;
        Long dl = DEADLINE.get();
        if (dl != null && (ms = Math.min(ms, dl - System.currentTimeMillis())) < 300L) {
            throw new TimeoutException("lyrics search time is up");
        }
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofMillis(ms)).GET().header("User-Agent", USER_AGENT);
        if (headers != null) {
            headers.forEach(b::header);
        }
        HttpResponse<String> r = CLIENT.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return new Response(r.statusCode(), r.body());
    }

    public static byte[] getBytes(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10L)).GET().header("User-Agent", USER_AGENT).build();
        HttpResponse<byte[]> r = CLIENT.send(req, HttpResponse.BodyHandlers.ofByteArray());
        return r.statusCode() == 200 ? r.body() : null;
    }

    public static Response send(String method, String url, Map<String, String> headers, String form) throws Exception {
        HttpRequest.BodyPublisher body = form == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(form);
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10L)).method(method, body).header("User-Agent", USER_AGENT);
        if (form != null) {
            b.header("Content-Type", "application/x-www-form-urlencoded");
        }
        if (headers != null) {
            headers.forEach(b::header);
        }
        HttpResponse<String> r = CLIENT.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return new Response(r.statusCode(), r.body());
    }

    public record Response(int code, String body) {
    }
}


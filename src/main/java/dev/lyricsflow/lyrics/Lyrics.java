/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 */
package dev.lyricsflow.lyrics;

import dev.lyricsflow.config.ConfigManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_310;

public final class Lyrics {
    private static final ThreadLocal<Double> SONG_RATE = ThreadLocal.withInitial(() -> 4.5);
    public final List<Line> lines;
    public final boolean synced;
    public final String source;
    public final boolean wordSynced;

    public static String show(String s) {
        return s != null && ConfigManager.get().allCaps ? Lyrics.upper(s) : s;
    }

    public static String upper(String s) {
        if (s == null) {
            return null;
        }
        Locale loc = Locale.ROOT;
        try {
            String lang = class_310.method_1551().field_1690.field_1883;
            if (lang != null && (lang.startsWith("tr") || lang.startsWith("az"))) {
                loc = Locale.forLanguageTag("tr");
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return s.toUpperCase(loc);
    }

    static int syllables(String w) {
        int n = 0;
        boolean prev = false;
        for (int i = 0; i < w.length(); ++i) {
            boolean v;
            char c = Character.toLowerCase(w.charAt(i));
            boolean bl = v = "aeiouy\u0131\u00f6\u00fc\u00e2\u00ea\u00ee\u00f4\u00fb\u00e0\u00e8\u00ec\u00f2\u00f9\u00e1\u00e9\u00ed\u00f3\u00fa\u00e4\u00eb\u00ef\u00e3\u00f5\u00e5\u00e6\u00f8\u0153".indexOf(c) >= 0;
            if (v && !prev) {
                ++n;
            }
            prev = v;
        }
        if (n == 0) {
            int letters = 0;
            for (int i = 0; i < w.length(); ++i) {
                if (!Character.isLetterOrDigit(w.charAt(i))) continue;
                ++letters;
            }
            n = Math.max(1, (int)Math.round((double)letters / 2.5));
        }
        return n;
    }

    private static double songRate(List<long[]> times, List<String> texts, long durationMs) {
        ArrayList<Double> r = new ArrayList<Double>();
        for (int i = 0; i < texts.size(); ++i) {
            String[] parts;
            long gap;
            long e;
            long s = times.get(i)[0];
            long l = times.get(i).length > 1 ? times.get(i)[1] : (e = i + 1 < texts.size() ? times.get(i + 1)[0] : -1L);
            if (e <= s || (gap = e - s) < 700L || gap > 8000L || (parts = texts.get(i).trim().split("\\s+")).length < 2) continue;
            int syl = 0;
            for (String p : parts) {
                syl += Lyrics.syllables(p);
            }
            r.add((double)syl / ((double)gap / 1000.0));
        }
        if (r.size() < 3) {
            return 4.5;
        }
        Collections.sort(r);
        double busy = (Double)r.get(Math.min(r.size() - 1, (int)((double)r.size() * 0.75)));
        return Math.max(2.5, Math.min(10.0, busy * 1.05));
    }

    public Lyrics(List<long[]> times, List<String> texts, boolean synced, String source, long durationMs) {
        this(times, texts, synced, source, durationMs, null);
    }

    public Lyrics(List<long[]> times, List<String> texts, boolean synced, String source, long durationMs, List<List<Word>> words) {
        boolean ws;
        ArrayList<Line> l = new ArrayList<Line>();
        SONG_RATE.set(Lyrics.songRate(times, texts, durationMs));
        for (int i = 0; i < texts.size(); ++i) {
            List<Word> w;
            long e;
            long s = times.get(i)[0];
            long l2 = times.get(i).length > 1 ? times.get(i)[1] : (i + 1 < texts.size() ? times.get(i + 1)[0] : (e = durationMs > s ? Math.min(durationMs, s + 6000L) : s + 5000L));
            if (e - s > 9000L) {
                e = s + 9000L;
            }
            List<Word> list = w = words != null && i < words.size() ? words.get(i) : null;
            if (w != null && !w.isEmpty()) {
                e = Math.max(e, w.get((int)(w.size() - 1)).end);
            }
            l.add(new Line(l.size(), texts.get(i), s, e, w));
        }
        this.lines = Collections.unmodifiableList(l);
        this.synced = synced;
        this.source = source;
        boolean bl = ws = words != null;
        if (ws) {
            for (List<Word> w : words) {
                if (w != null && !w.isEmpty()) continue;
                ws = false;
                break;
            }
        }
        this.wordSynced = ws && !l.isEmpty();
    }

    public Line lineAt(long t) {
        Line cur = null;
        for (Line l : this.lines) {
            if (l.start > t) break;
            cur = l;
        }
        if (cur != null && t > cur.end) {
            return null;
        }
        return cur;
    }

    public Line lastStartedBefore(long t) {
        Line cur = null;
        for (Line l : this.lines) {
            if (l.start > t) break;
            cur = l;
        }
        return cur;
    }

    public static final class Word {
        public final String text;
        public volatile long start;
        public volatile long end;
        final long origStart;
        final long origEnd;

        Word(String text, long start, long end) {
            this.text = text;
            this.start = start;
            this.end = end;
            this.origStart = start;
            this.origEnd = end;
        }

        public static Word of(String text, long start, long end) {
            return new Word(text, start, end);
        }
    }

    public static final class Line {
        public final int index;
        public final String text;
        public final long start;
        public final long end;
        public final List<Word> words;
        public final boolean estimated;

        Line(int index, String text, long start, long end) {
            this(index, text, start, end, null);
        }

        Line(int index, String text, long start, long end, List<Word> words) {
            this.index = index;
            this.text = text;
            this.start = start;
            this.end = end;
            this.estimated = words == null || words.isEmpty();
            this.words = !this.estimated ? Collections.unmodifiableList(new ArrayList<Word>(words)) : Line.splitWords(text, start, end);
        }

        public void retime(double k) {
            if (!this.estimated || this.words.isEmpty()) {
                return;
            }
            long limit = this.end - 40L;
            for (Word w : this.words) {
                w.start = Math.min(limit, this.start + Math.round((double)(w.origStart - this.start) * k));
                w.end = Math.min(limit, this.start + Math.round((double)(w.origEnd - this.start) * k));
            }
        }

        public long guessedEnd() {
            return this.words.isEmpty() ? this.end : this.words.get((int)(this.words.size() - 1)).origEnd;
        }

        private static List<Word> splitWords(String text, long start, long end) {
            String[] parts = text.trim().split("\\s+");
            ArrayList<Word> out = new ArrayList<Word>();
            if (parts.length == 0 || parts[0].isEmpty()) {
                return out;
            }
            long gap = Math.max(300L, end - start);
            int[] syl = new int[parts.length];
            int total = 0;
            for (int i = 0; i < parts.length; ++i) {
                syl[i] = Lyrics.syllables(parts[i]);
                total += syl[i];
            }
            double rate = SONG_RATE.get();
            double lineRate = (double)total / ((double)gap / 1000.0);
            long lead = 0L;
            long sing = lineRate >= rate * 0.9 ? (long)((double)gap * 0.74) : Math.max((long)((double)total / rate * 1000.0), (long)((double)gap * 0.6));
            sing = Math.max(Math.min(350L, gap), Math.min(sing, (long)((double)gap * 0.82)));
            double weight = 0.0;
            for (int n : syl) {
                weight += (double)n + 0.6;
            }
            double t = start + lead;
            long early = 60L;
            for (int i = 0; i < parts.length; ++i) {
                double d = (double)sing * (((double)syl[i] + 0.6) / weight);
                long prev = out.isEmpty() ? start - 1000L : ((Word)out.get((int)(out.size() - 1))).start;
                long ws = i == 0 ? start : Math.max(prev + 130L, (long)t - 60L);
                out.add(new Word(parts[i], ws, Math.max(ws + 60L, (long)(t + d) - 60L)));
                t += d;
            }
            return Collections.unmodifiableList(out);
        }

        public static Line notes(int index, String text, long start, long end) {
            return new Line(index, text, start, end);
        }

        public int wordAt(long t) {
            if (this.words.isEmpty() || t < this.start) {
                return -1;
            }
            for (int i = this.words.size() - 1; i >= 0; --i) {
                if (t < this.words.get((int)i).start) continue;
                return i;
            }
            return -1;
        }
    }
}


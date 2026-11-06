/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_156
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_315
 *  net.minecraft.class_332
 *  net.minecraft.class_4063
 *  net.minecraft.class_4066
 *  net.minecraft.class_437
 */
package dev.lyricsflow.gui;

import dev.lyricsflow.Keys;
import dev.lyricsflow.app.Core;
import dev.lyricsflow.app.config.Config;
import dev.lyricsflow.app.cosmetic.DeathFx;
import dev.lyricsflow.app.sky.SkyImage;
import dev.lyricsflow.app.sky.SkyPreset;
import dev.lyricsflow.app.sky.WorldSky;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.config.LFConfig;
import dev.lyricsflow.gui.HudEditorScreen;
import dev.lyricsflow.gui.Icons;
import dev.lyricsflow.gui.SkyLyricsScreen;
import dev.lyricsflow.gui.NowPlaying;
import dev.lyricsflow.gui.PlayerScreen;
import dev.lyricsflow.gui.Ui;
import dev.lyricsflow.gui.UiSounds;
import dev.lyricsflow.media.AppVolume;
import dev.lyricsflow.media.MediaManager;
import dev.lyricsflow.media.SpotifyWeb;
import dev.lyricsflow.media.Track;
import dev.lyricsflow.perf.AutoPerf;
import dev.lyricsflow.perf.FpsBoost;
import dev.lyricsflow.perf.Profiles;
import dev.lyricsflow.render.Chroma;
import dev.lyricsflow.render.Cosmetics;
import dev.lyricsflow.render.ExtraHud;
import dev.lyricsflow.render.Fonts;
import dev.lyricsflow.render.Gfx;
import dev.lyricsflow.render.Themes;
import dev.lyricsflow.render.WorldLyrics;
import dev.lyricsflow.util.Anim;
import dev.lyricsflow.util.Col;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_332;
import net.minecraft.class_4063;
import net.minecraft.class_4066;
import net.minecraft.class_437;

final class Pages {
    private static int skyPage;
    private static final int[] DARK;
    private static final int[] BRIGHT;
    private static final int[] PLAIN_PALETTE;
    private static final int[] SKY_PALETTE;
    private static final int[] AURORA_PALETTE;
    private static int cosSlot;
    private static String pasteStatus;
    private static final int[] TARGETS;

    private Pages() {
    }

    private static LFConfig c() {
        return ConfigManager.get();
    }

    private static String pct(double v) {
        return Math.round(v * 100.0) + "%";
    }

    private static String ms(double v) {
        long m = Math.round(v);
        return m == 0L ? "0 ms" : (m > 0L ? "+" : "") + String.format(Locale.ROOT, "%.2f s", (double)m / 1000.0);
    }

    private static String mult(double v) {
        return String.format(Locale.ROOT, "%.2fx", v);
    }

    static List<Ui.Widget> build(SkyLyricsScreen.Tab tab, SkyLyricsScreen screen, boolean heroVisible) {
        ArrayList<Ui.Widget> l = new ArrayList<Ui.Widget>();
        switch (tab) {
            case NOW: {
                Pages.now(l, heroVisible);
                break;
            }
            case LYRICS: {
                Pages.lyrics(l, screen);
                break;
            }
            case STYLE: {
                Pages.style(l, screen);
                break;
            }
            case SKY: {
                Pages.sky(l, screen);
                break;
            }
            case COSMETICS: {
                Pages.cosmetics(l, screen);
                break;
            }
            case DISPLAY: {
                Pages.display(l);
                break;
            }
            case KEYS: {
                Pages.keys(l);
                break;
            }
            case EXTRA: {
                Pages.extra(l, screen);
                break;
            }
            case SAVED: {
                Pages.saved(l, screen);
                break;
            }
            case PERF: {
                Pages.perf(l, screen);
            }
        }
        l.add(new Ui.Spacer(8.0f));
        return l;
    }

    private static void now(List<Ui.Widget> l, boolean heroVisible) {
        if (!heroVisible) {
            l.add(new Ui.Custom(w -> Float.valueOf(NowPlaying.height(w.floatValue())), (g, x, y, w, h, mx, my, a) -> NowPlaying.renderHero(g, x, y, w, h, mx, my, a), (mx, my, x, y, w, h) -> {
                if (!Ui.inside(mx, my, x, y, w, h)) {
                    return false;
                }
                NowPlaying.click(mx, my);
                return true;
            }));
        }
        l.add(new Ui.Card().add(new Ui.Button("lyricsflow.opt.open_player", "lyricsflow.opt.open_player.desc", () -> Ui.tr("lyricsflow.button.open", new Object[0]), "music", true, () -> class_310.method_1551().method_1507((class_437)new PlayerScreen()))));
        l.add(new Ui.Section("lyricsflow.section.quick"));
        l.add(new Ui.Card().add(new Ui.Toggle("lyricsflow.opt.enabled", "lyricsflow.opt.enabled.desc", () -> Pages.c().enabled, v -> {
            Pages.c().enabled = v;
        })).add(new Ui.Toggle("lyricsflow.opt.lyrics_on", "lyricsflow.opt.lyrics_on.desc", () -> Pages.c().lyricsOn, v -> {
            Pages.c().lyricsOn = v;
            WorldLyrics.clear();
        })).add(new Ui.Choice("lyricsflow.opt.lyrics_style", null, new String[]{"lyricsflow.style.sky", "lyricsflow.style.floating"}, () -> Math.min(1, Pages.c().lyricsStyle.ordinal()), i -> {
            Pages.c().lyricsStyle = LFConfig.LyricsStyle.values()[i];
            WorldLyrics.clear();
        })).add(new Ui.Toggle("lyricsflow.opt.sky_follow", "lyricsflow.opt.sky_follow.desc", () -> Pages.c().skyFollow, v -> {
            Pages.c().skyFollow = v;
        })).add(new Ui.Slider("lyricsflow.opt.scale", 0.4, 2.5, 0.05, () -> Pages.c().lyricsScale, v -> {
            Pages.c().lyricsScale = (float)v;
        }, Pages::mult)).add(new Ui.Toggle("lyricsflow.opt.top_bar", "lyricsflow.opt.top_bar.desc", () -> Pages.c().showTopBar, v -> {
            Pages.c().showTopBar = v;
        })).add(new Ui.Toggle("lyricsflow.opt.chroma", "lyricsflow.opt.chroma.desc", () -> Pages.c().chroma, v -> {
            Pages.c().chroma = v;
        })).add(new Ui.Choice("lyricsflow.opt.chroma_color", null, (String[])Arrays.stream(Chroma.COLORS).map(n -> "lyricsflow.chroma." + n).toArray(String[]::new), () -> Pages.c().chromaColor, i -> {
            Pages.c().chromaColor = i;
        })).add(new Ui.Slider("lyricsflow.opt.music_volume", 0.0, 1.0, 0.01, () -> Math.max(0.0f, AppVolume.get()), v -> AppVolume.set((float)v), v -> AppVolume.get() < 0.0f ? "\u2014" : Math.round(v * 100.0) + "%")));
        Pages.source(l);
    }

    private static void lyrics(List<Ui.Widget> l, SkyLyricsScreen screen) {
        l.add(new Ui.Section("lyricsflow.section.general"));
        l.add(new Ui.Card().add(new Ui.Toggle("lyricsflow.opt.lyrics_on", "lyricsflow.opt.lyrics_on.desc", () -> Pages.c().lyricsOn, v -> {
            Pages.c().lyricsOn = v;
            WorldLyrics.clear();
        })).add(new Ui.Choice("lyricsflow.opt.lyrics_style", "lyricsflow.opt.lyrics_style.desc", new String[]{"lyricsflow.style.sky", "lyricsflow.style.floating"}, () -> Math.min(1, Pages.c().lyricsStyle.ordinal()), i -> {
            Pages.c().lyricsStyle = LFConfig.LyricsStyle.values()[i];
            WorldLyrics.clear();
            screen.rebuildSoon();
        })));
        l.add(new Ui.Section("lyricsflow.section.size"));
        Ui.Card size = new Ui.Card().add(new Ui.Slider("lyricsflow.opt.scale", 0.4, 2.5, 0.05, () -> Pages.c().lyricsScale, v -> {
            Pages.c().lyricsScale = (float)v;
        }, Pages::mult));
        if (Pages.c().lyricsStyle == LFConfig.LyricsStyle.FLOATING) {
            size.add(new Ui.Slider("lyricsflow.opt.distance", 0.4, 2.0, 0.05, () -> Pages.c().lyricsDistance, v -> {
                Pages.c().lyricsDistance = (float)v;
            }, Pages::mult)).add(new Ui.Toggle("lyricsflow.opt.follow_view", "lyricsflow.opt.follow_view.desc", () -> Pages.c().followView, v -> {
                Pages.c().followView = v;
            })).add(new Ui.Slider("lyricsflow.opt.float_height", -1.0, 4.0, 0.25, () -> Pages.c().floatHeight, v -> {
                Pages.c().floatHeight = (float)v;
            }, v -> (v > 0.0 ? "+" : "") + String.format(Locale.ROOT, "%.2f", v).replaceAll("0+$", "").replaceAll("\\.$", "")));
        }
        if (Pages.c().lyricsStyle == LFConfig.LyricsStyle.SKY) {
            size.add(new Ui.Slider("lyricsflow.opt.distance", 30.0, 300.0, 10.0, () -> Pages.c().skyDistance, v -> {
                Pages.c().skyDistance = (float)v;
            }, v -> Math.round(v) + "m")).add(new Ui.Toggle("lyricsflow.opt.sky_words", "lyricsflow.opt.sky_words.desc", () -> Pages.c().skyWords, v -> {
                Pages.c().skyWords = v;
            }));
        }
        l.add(size.add(new Ui.Slider("lyricsflow.opt.opacity", 0.2, 1.0, 0.05, () -> Pages.c().textOpacity, v -> {
            Pages.c().textOpacity = (float)v;
        }, Pages::pct)).add(new Ui.Slider("lyricsflow.opt.max_lines", 1.0, 3.0, 1.0, () -> Pages.c().maxLines, v -> {
            Pages.c().maxLines = (int)Math.round(v);
        }, v -> String.valueOf((int)Math.round(v)))));
        l.add(new Ui.Section("lyricsflow.section.animation"));
        l.add(new Ui.Card().add(new Ui.Choice("lyricsflow.opt.enter", null, new String[]{"lyricsflow.enter.word", "lyricsflow.enter.fade", "lyricsflow.enter.rise", "lyricsflow.enter.neon"}, () -> Pages.c().enterAnim.ordinal(), i -> {
            Pages.c().enterAnim = LFConfig.EnterAnim.values()[i];
        })).add(new Ui.Choice("lyricsflow.opt.exit", null, new String[]{"lyricsflow.exit.float_up", "lyricsflow.exit.drift", "lyricsflow.exit.fade"}, () -> Pages.c().exitAnim.ordinal(), i -> {
            Pages.c().exitAnim = LFConfig.ExitAnim.values()[i];
        })).add(new Ui.Slider("lyricsflow.opt.anim_speed", 0.5, 2.0, 0.05, () -> Pages.c().animSpeed, v -> {
            Pages.c().animSpeed = (float)v;
        }, Pages::mult)));
        l.add(new Ui.Section("lyricsflow.section.advanced"));
        l.add(new Ui.Card().add(new Ui.Toggle("lyricsflow.opt.underground", "lyricsflow.opt.underground.desc", () -> Pages.c().showUnderground, v -> {
            Pages.c().showUnderground = v;
        })).add(new Ui.Toggle("lyricsflow.opt.music_notes", "lyricsflow.opt.music_notes.desc", () -> Pages.c().musicNotes, v -> {
            Pages.c().musicNotes = v;
        })));
    }

    private static void style(List<Ui.Widget> l, SkyLyricsScreen screen) {
        l.add(new Ui.Section("lyricsflow.section.font"));
        List<Fonts.LyricFont> fonts = Fonts.lyricsAll();
        l.add(new Ui.Card().add(new Ui.Chips("lyricsflow.section.font", null, (String[])fonts.stream().map(Fonts.LyricFont::name).toArray(String[]::new), () -> Math.max(0, Pages.indexOf(fonts, Pages.c().font)), i -> {
            Pages.c().font = ((Fonts.LyricFont)fonts.get(i)).id();
            WorldLyrics.clear();
        }).fonts((String[])fonts.stream().map(Fonts.LyricFont::id).toArray(String[]::new)).reset(() -> {
            Pages.c().font = new LFConfig().font;
            WorldLyrics.clear();
        })).add(new Ui.Choice("lyricsflow.opt.text_shadow", null, new String[]{"lyricsflow.shadow.off", "lyricsflow.shadow.soft", "lyricsflow.shadow.strong"}, () -> Pages.c().textShadow, i -> {
            Pages.c().textShadow = i;
        })).add(new Ui.Toggle("lyricsflow.opt.all_caps", null, () -> Pages.c().allCaps, v -> {
            Pages.c().allCaps = v;
            WorldLyrics.clear();
        })));
        l.add(new Ui.Section("lyricsflow.section.look"));
        l.add(new Ui.Card().add(new Ui.Colors("lyricsflow.opt.colour", "lyricsflow.opt.colour.desc", () -> Pages.c().singleColor, v -> {
            Pages.c().singleColor = v;
        })).add(new Ui.Toggle("lyricsflow.opt.beat_glow", "lyricsflow.opt.beat_glow.desc", () -> Pages.c().beatGlow, v -> {
            Pages.c().beatGlow = v;
        })));
        Pages.themeGrid(l);
    }

    private static void themeGrid(List<Ui.Widget> l) {
        l.add(new Ui.Section("lyricsflow.section.text_color"));
        List<Themes.Theme> all = Themes.ALL;
        IntSupplier cur = () -> {
            for (int i = 0; i < all.size(); ++i) {
                if (!((Themes.Theme)all.get(i)).id().equals(Pages.c().theme)) continue;
                return i;
            }
            return 0;
        };
        IntConsumer pick = i -> {
            Pages.c().theme = ((Themes.Theme)all.get(Math.floorMod(i, all.size()))).id();
            Pages.c().customTheme = false;
            Pages.c().singleColor = 0;
        };
        l.add(new Ui.Card().add(new Ui.Chips("lyricsflow.section.text_color", null, (String[])all.stream().map(Themes.Theme::key).toArray(String[]::new), () -> Pages.c().customTheme || Pages.c().singleColor != 0 ? -1 : cur.getAsInt(), pick).dots(all.stream().mapToInt(Themes.Theme::a).toArray()).reset(() -> {
            LFConfig d = new LFConfig();
            Pages.c().theme = d.theme;
            Pages.c().customTheme = false;
            Pages.c().singleColor = 0;
        })));
        Ui.Card custom = new Ui.Card();
        custom.add(new Ui.Toggle("lyricsflow.opt.custom_theme", "lyricsflow.opt.custom_theme.desc", () -> Pages.c().customTheme, v -> {
            Pages.c().customTheme = v;
            if (v.booleanValue()) {
                Themes.Theme t = Themes.byId(Pages.c().theme);
                Pages.c().customMain = t.main();
                Pages.c().customA = t.a();
                Pages.c().customB = t.b();
            }
        }));
        custom.add(Pages.hueSlider("lyricsflow.opt.custom_main", () -> Pages.c().customMain, v -> {
            Pages.c().customMain = v;
        }, 0.12f, 1.0f));
        custom.add(Pages.hueSlider("lyricsflow.opt.custom_a", () -> Pages.c().customA, v -> {
            Pages.c().customA = v;
        }, 0.7f, 1.0f));
        custom.add(Pages.hueSlider("lyricsflow.opt.custom_b", () -> Pages.c().customB, v -> {
            Pages.c().customB = v;
        }, 0.6f, 1.0f));
        l.add(custom);
    }

    private static Ui.Widget hueSlider(String label, final IntSupplier get, IntConsumer set, float sat, float val) {
        return new Ui.Slider(label, 0.0, 358.0, 2.0, () -> Math.min(358, Math.round(Col.toHsv(get.getAsInt())[0] * 360.0f)), v -> {
            set.accept(Col.hsv((float)(v / 360.0), sat, val));
            Pages.c().customTheme = true;
        }, v -> Math.round(v) + "\u00b0"){

            @Override
            public void render(class_332 g, float ox, float oy, double mx, double my, float a) {
                super.render(g, ox, oy, mx, my, a);
                Gfx.circle(g, ox + this.x + Gfx.textWidth(Ui.tr(this.label, new Object[0]), Ui.FB, 8.4f) + 10.0f, oy + this.y + 9.5f, 3.6f, Col.alpha(get.getAsInt(), a));
            }
        };
    }

    private static Ui.Widget fontGrid() {
        List<Fonts.LyricFont> fonts = Fonts.lyricsAll();
        float[] hover = new float[fonts.size()];
        float cellH = 34.0f;
        return new Ui.Custom(w -> Float.valueOf((float)Math.ceil((double)fonts.size() / 2.0) * (cellH + 6.0f) + 4.0f), (g, x, y, w, h, mx, my, a) -> {
            float cw = (w - 6.0f) / 2.0f;
            Themes.Theme th = Themes.current();
            for (int i = 0; i < fonts.size(); ++i) {
                Fonts.LyricFont f = (Fonts.LyricFont)fonts.get(i);
                float fx = x + (float)(i % 2) * (cw + 6.0f);
                float fy = y + (float)(i / 2) * (cellH + 6.0f);
                boolean sel = Pages.c().font.equals(f.id());
                boolean over = Ui.inside(mx, my, fx, fy, cw, cellH);
                if (over && hover[i] < 0.05f) {
                    UiSounds.play(UiSounds.HOVER, 1.2f);
                }
                hover[i] = Anim.approach(hover[i], over ? 1.0f : 0.0f, 14.0f);
                float lift = -hover[i] * 1.2f;
                if (sel) {
                    Gfx.glow(g, fx, fy + lift, cw, cellH, 8.0f, 4.0f, Col.alpha(Themes.accent(), a * 0.35f));
                }
                Gfx.round(g, fx, fy + lift, cw, cellH, 8.0f, Col.alpha(Col.lerp(-15001562, -14541264, hover[i]), a));
                Gfx.roundOutline(g, fx, fy + lift, cw, cellH, 8.0f, sel ? 1.2f : 0.5f, Col.alpha(sel ? Themes.accent() : 0x14FFFFFF, a));
                float size = 14.5f;
                String name = Gfx.ellipsize(f.name(), f.id(), size, cw - 24.0f);
                if (sel) {
                    Gfx.neon(g, name, f.id(), fx + cw / 2.0f - 4.0f, fy + lift + (cellH - size) / 2.0f - 1.0f, size, th.main(), th.a(), th.b(), a, true, 0.4f);
                    Gfx.circle(g, fx + cw - 10.0f, fy + lift + 10.0f, 5.0f, Col.alpha(Themes.accent(), a));
                    Icons.draw(g, "check", fx + cw - 10.0f, fy + lift + 10.0f, 6.5f, Col.alpha(-1, a));
                    continue;
                }
                Gfx.textCentered(g, name, f.id(), fx + cw / 2.0f - 4.0f, fy + lift + (cellH - size) / 2.0f - 1.0f, size, Col.alpha(Col.lerp(-6515020, -856070, hover[i]), a));
            }
        }, (mx, my, x, y, w, h) -> {
            float cw = (w - 6.0f) / 2.0f;
            for (int i = 0; i < fonts.size(); ++i) {
                float fx = x + (float)(i % 2) * (cw + 6.0f);
                float fy = y + (float)(i / 2) * (cellH + 6.0f);
                if (!Ui.inside(mx, my, fx, fy, cw, cellH)) continue;
                Pages.c().font = ((Fonts.LyricFont)fonts.get(i)).id();
                ConfigManager.save();
                WorldLyrics.clear();
                UiSounds.play(UiSounds.TOGGLE_ON, 0.9f + (float)i * 0.02f);
                return true;
            }
            return false;
        });
    }

    private static Config sc() {
        return Core.cfg();
    }

    private static int[] argb(int[] rgb) {
        int[] o = new int[rgb.length];
        for (int i = 0; i < rgb.length; ++i) {
            o[i] = 0xFF000000 | rgb[i];
        }
        return o;
    }

    private static void sky(List<Ui.Widget> l, SkyLyricsScreen screen) {
        Config c = Pages.sc();
        l.add(new Ui.Section("lyricsflow.ui.group.general"));
        l.add(new Ui.Card().add(new Ui.Toggle("lyricsflow.ui.sky.enabled", "lyricsflow.ui.sky.enabled.sub", () -> c.sky >= 0, v -> {
            c.sky = v != false ? Math.max(0, c.lastSky) : -1;
            screen.rebuildSoon();
        })).add(new Ui.Toggle("lyricsflow.ui.sky.snow", "lyricsflow.ui.sky.snow.sub", () -> c.snow, v -> {
            c.snow = v;
        })).add(new Ui.Toggle("lyricsflow.ui.sky.random", "lyricsflow.ui.sky.shuffle.sub", () -> c.skyShuffle, v -> {
            c.skyShuffle = v;
            if (v.booleanValue()) {
                SkyPreset.shuffle();
                screen.rebuildSoon();
            }
        })));
        l.add(new Ui.Section("lyricsflow.ui.sky.picture"));
        Ui.Card pic = new Ui.Card().add(new Ui.Toggle("lyricsflow.ui.sky.picture.on", "lyricsflow.ui.sky.picture.on.sub", () -> c.skyImage, v -> {
            c.skyImage = v;
            screen.rebuildSoon();
        }));
        List<String> pics = SkyImage.files();
        pic.add(new Ui.Button("lyricsflow.ui.sky.picture.folder", null, () -> Ui.tr("lyricsflow.ui.sky.picture.folder", new Object[0]), "download", false, SkyImage::openFolder));
        pic.add(new Ui.Button("lyricsflow.ui.sky.picture.reload", null, () -> Ui.tr("lyricsflow.ui.sky.picture.reload", new Object[0]), "refresh", false, () -> {
            SkyImage.reload();
            screen.rebuildSoon();
        }));
        l.add(pic);
        if (!pics.isEmpty()) {
            float cellH = 58.0f;
            float[] hov = new float[pics.size()];
            l.add(new Ui.Custom(w -> {
                int cols = w.floatValue() > 330.0f ? 3 : 2;
                return Float.valueOf((float)Math.ceil((double)pics.size() / (double)cols) * (cellH + 6.0f) + 2.0f);
            }, (g, x, y, w, h, mx, my, a) -> {
                int cols = w > 330.0f ? 3 : 2;
                float cw = (w - (float)(6 * (cols - 1))) / (float)cols;
                String cur = pics.contains(c.skyImageFile) ? c.skyImageFile : (String)pics.get(0);
                for (int i = 0; i < pics.size(); ++i) {
                    String name = (String)pics.get(i);
                    float fx = x + (float)(i % cols) * (cw + 6.0f);
                    float fy = y + (float)(i / cols) * (cellH + 6.0f);
                    boolean on = c.skyImage && name.equals(cur);
                    boolean over = Ui.inside(mx, my, fx, fy, cw, cellH);
                    hov[i] = Anim.approach(hov[i], over ? 1.0f : 0.0f, 14.0f);
                    float ih = cellH - 14.0f;
                    Gfx.round(g, fx - 1.0f, fy - 1.0f, cw + 2.0f, cellH + 2.0f, 7.0f, Col.alpha(on ? Themes.accent() : Col.lerp(-14541264, -13883584, hov[i]), a));
                    class_2960 id = SkyImage.thumb(name);
                    if (id != null) {
                        Gfx.image(g, id, 160, 80, fx + 2.0f, fy + 2.0f, cw - 4.0f, ih - 2.0f, Col.alpha(-1, a));
                    } else {
                        Gfx.round(g, fx + 2.0f, fy + 2.0f, cw - 4.0f, ih - 2.0f, 5.0f, Col.alpha(-15396065, a));
                    }
                    Gfx.text(g, Gfx.ellipsize(name, Ui.F, 6.4f, cw - 8.0f), Ui.F, fx + 4.0f, fy + ih + 2.5f, 6.4f, Col.alpha(on ? -856070 : -6515020, a));
                }
            }, (mx, my, x, y, w, h) -> {
                int cols = w > 330.0f ? 3 : 2;
                float cw = (w - (float)(6 * (cols - 1))) / (float)cols;
                for (int i = 0; i < pics.size(); ++i) {
                    float fx = x + (float)(i % cols) * (cw + 6.0f);
                    float fy = y + (float)(i / cols) * (cellH + 6.0f);
                    if (!Ui.inside(mx, my, fx, fy, cw, cellH)) continue;
                    c.skyImageFile = (String)pics.get(i);
                    c.skyImage = true;
                    Core.saveConfig();
                    UiSounds.play(UiSounds.TOGGLE_ON, 0.9f + (float)i * 0.03f);
                    return true;
                }
                return false;
            }));
        }
        l.add(new Ui.Info(() -> {
            Object info = Ui.tr("lyricsflow.ui.sky.picture.info", "4096 x 2048");
            if (c.skyImage && SkyImage.files().isEmpty()) {
                info = (String)info + "\n" + Ui.tr("lyricsflow.ui.sky.picture.none", new Object[0]);
            } else if (!SkyImage.size().isEmpty()) {
                info = (String)info + "\n" + Ui.tr("lyricsflow.ui.sky.picture.loaded", SkyImage.size());
            }
            return info;
        }));
        l.add(new Ui.Section("lyricsflow.ui.sky.style"));
        l.add(new Ui.SkyGrid(SkyPreset.order(), () -> c.sky >= 0 ? SkyPreset.get(c.sky) : null, p -> {
            c.sky = p.ordinal();
            c.lastSky = p.ordinal();
            screen.rebuildSoon();
        }));
        SkyPreset cur = SkyPreset.get(c.sky >= 0 ? c.sky : c.lastSky);
        if (cur == SkyPreset.CUSTOM && c.sky >= 0) {
            Class<SkyPreset> A = SkyPreset.class;
            SkyPreset.Weather[] ws = SkyPreset.Weather.values();
            String[] wn = (String[])Arrays.stream(ws).map(w -> w == SkyPreset.Weather.NONE ? "lyricsflow.skyed.noweather" : "lyricsflow.feat." + w.name().toLowerCase(Locale.ROOT)).toArray(String[]::new);
            l.add(new Ui.Section("lyricsflow.skyed.title"));
            l.add(new Ui.Card().add(new Ui.Colors("lyricsflow.skyed.top", null, () -> 0xFF000000 | c.customTop, v -> {
                c.customTop = v & 0xFFFFFF;
                SkyPreset.applyCustom();
            }, DARK, false)).add(new Ui.Colors("lyricsflow.skyed.horizon", null, () -> 0xFF000000 | c.customHorizon, v -> {
                c.customHorizon = v & 0xFFFFFF;
                SkyPreset.applyCustom();
            }, DARK, false)).add(new Ui.Colors("lyricsflow.skyed.aurora1", null, () -> 0xFF000000 | c.customAurora1, v -> {
                c.customAurora1 = v & 0xFFFFFF;
                SkyPreset.applyCustom();
            }, BRIGHT, false)).add(new Ui.Colors("lyricsflow.skyed.aurora2", null, () -> 0xFF000000 | c.customAurora2, v -> {
                c.customAurora2 = v & 0xFFFFFF;
                SkyPreset.applyCustom();
            }, BRIGHT, false)).add(new Ui.Toggle("lyricsflow.feat.aurora", null, () -> c.customAurora, v -> {
                c.customAurora = v;
                SkyPreset.applyCustom();
            })).add(new Ui.Toggle("lyricsflow.feat.stars", null, () -> c.customStars, v -> {
                c.customStars = v;
                SkyPreset.applyCustom();
            })).add(new Ui.Toggle("lyricsflow.feat.moon", null, () -> c.customMoon, v -> {
                c.customMoon = v;
                SkyPreset.applyCustom();
            })).add(new Ui.Toggle("lyricsflow.feat.shooting", null, () -> c.customShooting, v -> {
                c.customShooting = v;
                SkyPreset.applyCustom();
            })).add(new Ui.Chips("lyricsflow.skyed.weather", null, wn, () -> Math.floorMod(c.customWeather, ws.length), i -> {
                c.customWeather = i;
                SkyPreset.applyCustom();
            })));
        }
        l.add(new Ui.Section("lyricsflow.ui.sky.tune"));
        String[] pals = new String[WorldSky.PALETTE_KEYS.length + 1];
        pals[0] = "lyricsflow.ui.palette.original";
        for (int i2 = 0; i2 < WorldSky.PALETTE_KEYS.length; ++i2) {
            pals[i2 + 1] = "lyricsflow.ui.palette." + WorldSky.PALETTE_KEYS[i2];
        }
        l.add(new Ui.Card().add(new Ui.Chips("lyricsflow.ui.sky.palette", null, pals, () -> c.skyPalette + 1, i -> {
            c.skyPalette = i - 1;
        })).add(new Ui.Slider("lyricsflow.ui.sky.speed", 0.25, 2.0, 0.05, () -> c.skySpeed, v -> {
            c.skySpeed = (float)v;
        }, Pages::mult)).add(new Ui.Slider("lyricsflow.ui.sky.lively", 0.0, 1.0, 0.05, () -> c.skyLively, v -> {
            c.skyLively = (float)v;
        }, Pages::pct)).add(new Ui.Slider("lyricsflow.ui.sky.pattern", 0.2, 1.5, 0.05, () -> c.skyPattern, v -> {
            c.skyPattern = (float)v;
        }, Pages::pct)).add(new Ui.Slider("lyricsflow.opt.skystrength", 0.1, 1.0, 0.05, () -> c.skyStrength, v -> {
            c.skyStrength = (float)v;
        }, Pages::pct)).add(new Ui.Slider("lyricsflow.opt.weatheramount", 0.0, 2.0, 0.05, () -> c.weatherAmount, v -> {
            c.weatherAmount = (float)v;
        }, Pages::pct)));
        l.add(new Ui.Section("lyricsflow.ui.sky.behaviour"));
        l.add(new Ui.Card().add(new Ui.Toggle("lyricsflow.opt.realweather", "lyricsflow.ui.sky.realweather.sub", () -> c.realWeather, v -> {
            c.realWeather = v;
        })).add(new Ui.Toggle("lyricsflow.opt.hideclouds", "lyricsflow.ui.sky.hideclouds.sub", () -> c.hideClouds, v -> {
            c.hideClouds = v;
        })).add(new Ui.Toggle("lyricsflow.extras.skyday", "lyricsflow.ui.sky.skyday.sub", () -> c.skyDay, v -> {
            c.skyDay = v;
            if (v.booleanValue()) {
                c.fxDayNight = false;
            }
        })).add(new Ui.Toggle("lyricsflow.extras.const", "lyricsflow.ui.sky.const.sub", () -> c.fxConstellations, v -> {
            c.fxConstellations = v;
        })).add(new Ui.Toggle("lyricsflow.extras.skymusic", "lyricsflow.ui.sky.music.sub", () -> c.fxSkyMusic, v -> {
            c.fxSkyMusic = v;
        })));
    }

    private static void cosmetics(List<Ui.Widget> l, SkyLyricsScreen screen) {
        l.add(new Ui.Section("lyricsflow.section.cosmetics"));
        l.add(new Ui.Info(() -> Ui.tr("lyricsflow.opt.cosmetic.desc", new Object[0])));
        String[] ids = Cosmetics.IDS;
        l.add(new Ui.Info(() -> Ui.tr("lyricsflow.opt.cosmetic.slots", new Object[0])));
        l.add(new Ui.CosmeticSlots(i -> Pages.c().slot(i), () -> cosSlot, i -> {
            cosSlot = i;
        }, i -> Pages.c().setSlot(i, "none")));
        l.add(new Ui.CosmeticGrid(ids, () -> Math.max(0, Arrays.asList(ids).indexOf(Pages.c().slot(cosSlot))), i -> Pages.c().setSlot(cosSlot, ids[i])).badge(i -> {
            for (int k = 0; k < 5; ++k) {
                if (k == cosSlot || "none".equals(ids[i]) || !ids[i].equals(Pages.c().slot(k))) continue;
                return k + 1;
            }
            return 0;
        }));
        l.add(new Ui.Info(() -> Ui.tr("lyricsflow.info.cosmetic_share", new Object[0])));
        Config sc = Pages.sc();
        l.add(new Ui.Section("lyricsflow.ui.style.death"));
        String[] de = new String[7];
        for (int i2 = 0; i2 < de.length; ++i2) {
            de[i2] = "lyricsflow.ui.death." + i2;
        }
        Ui.Card death = new Ui.Card().add(new Ui.Chips("lyricsflow.ui.style.death", "lyricsflow.ui.style.death.sub", de, () -> Math.floorMod(sc.deathFx, de.length), i -> {
            sc.deathFx = i;
            screen.rebuildSoon();
        }));
        if (sc.deathFx > 0) {
            death.add(new Ui.Toggle("lyricsflow.ui.style.deathothers", "lyricsflow.ui.style.deathothers.sub", () -> sc.deathOthers, v -> {
                sc.deathOthers = v;
            })).add(new Ui.Button("lyricsflow.ui.style.deathtest", "lyricsflow.ui.style.deathtest.sub", () -> Ui.tr("lyricsflow.ui.style.deathtest", new Object[0]), "play", true, () -> {
                class_310.method_1551().method_1507(null);
                DeathFx.test();
            }));
        }
        l.add(death);
    }

    private static void display(List<Ui.Widget> l) {
        Config sc = Pages.sc();
        l.add(new Ui.Section("lyricsflow.ui.display.menu"));
        l.add(new Ui.Card().add(new Ui.Choice("lyricsflow.ui.menu.mode", "lyricsflow.ui.menu.world.sub", new String[]{"lyricsflow.ui.menu.normal", "lyricsflow.ui.menu.world"}, () -> sc.menuMode, i -> {
            sc.menuMode = i;
        })));
        l.add(new Ui.Section("lyricsflow.section.top_bar"));
        String[] uiFonts = (String[])Fonts.UI.stream().map(f -> "lyricsflow.uifont." + f.id()).toArray(String[]::new);
        l.add(new Ui.Card().add(new Ui.Toggle("lyricsflow.opt.top_bar", "lyricsflow.opt.top_bar.desc", () -> Pages.c().showTopBar, v -> {
            Pages.c().showTopBar = v;
        })).add(new Ui.Toggle("lyricsflow.opt.custom_hotbar", "lyricsflow.opt.custom_hotbar.desc", () -> Pages.c().customHotbar, v -> {
            Pages.c().customHotbar = v;
        })).add(new Ui.Choice("lyricsflow.opt.top_bar_style", "lyricsflow.opt.top_bar_style.desc", new String[]{"lyricsflow.topbar.karaoke", "lyricsflow.topbar.simple"}, () -> Pages.c().topBarStyle.ordinal(), i -> {
            Pages.c().topBarStyle = LFConfig.TopBarStyle.values()[i];
        })).add(new Ui.Slider("lyricsflow.opt.top_bar_scale", 0.6, 1.8, 0.05, () -> Pages.c().topBarScale, v -> {
            Pages.c().topBarScale = (float)v;
        }, Pages::mult)).add(new Ui.Toggle("lyricsflow.opt.clock", null, () -> Pages.c().clock, v -> {
            Pages.c().clock = v;
        })).add(new Ui.Toggle("lyricsflow.opt.ping", null, () -> Pages.c().ping, v -> {
            Pages.c().ping = v;
        })).add(new Ui.Toggle("lyricsflow.opt.fps", null, () -> Pages.c().fps, v -> {
            Pages.c().fps = v;
        })));
        l.add(new Ui.Section("lyricsflow.section.behaviour"));
        l.add(new Ui.Card().add(new Ui.Toggle("lyricsflow.opt.only_playing", "lyricsflow.opt.only_playing.desc", () -> Pages.c().showOnlyWhenPlaying, v -> {
            Pages.c().showOnlyWhenPlaying = v;
        })).add(new Ui.Toggle("lyricsflow.opt.hide_paused", "lyricsflow.opt.hide_paused.desc", () -> Pages.c().hideWhenStopped, v -> {
            Pages.c().hideWhenStopped = v;
        })).add(new Ui.Toggle("lyricsflow.opt.dim_idle", "lyricsflow.opt.dim_idle.desc", () -> Pages.c().dimWhenIdle, v -> {
            Pages.c().dimWhenIdle = v;
        })).add(new Ui.Toggle("lyricsflow.opt.song_chime", "lyricsflow.opt.song_chime.desc", () -> Pages.c().songChime, v -> {
            Pages.c().songChime = v;
        })));
    }

    private static int indexOf(List<Fonts.LyricFont> list, String id) {
        for (int i = 0; i < list.size(); ++i) {
            if (!list.get(i).id().equals(id)) continue;
            return i;
        }
        return -1;
    }

    private static void source(List<Ui.Widget> l) {
        MediaManager mm = MediaManager.get();
        l.add(new Ui.Section("lyricsflow.section.status"));
        l.add(new Ui.Card().add(new Ui.Status(() -> Ui.tr(mm.statusKey(), new Object[0]), () -> {
            Track t = mm.track();
            return t == null ? Ui.tr("lyricsflow.status.hint", new Object[0]) : t.title + "  \u00b7  " + t.artist;
        }, () -> {
            Track t = mm.track();
            return t == null ? -9541498 : (t.playing ? -11870592 : -11414);
        })));
        l.add(new Ui.Section("lyricsflow.section.connection"));
        l.add(new Ui.Card().add(new Ui.Choice("lyricsflow.opt.source", "lyricsflow.opt.source.desc", new String[]{"lyricsflow.source.spotify", "lyricsflow.source.youtube", "lyricsflow.source.ytmusic"}, () -> switch (Pages.c().source) {
            case LFConfig.Source.YOUTUBE -> 1;
            case LFConfig.Source.YTMUSIC -> 2;
            default -> 0;
        }, i -> {
            Pages.c().source = (new LFConfig.Source[]{LFConfig.Source.SPOTIFY, LFConfig.Source.YOUTUBE, LFConfig.Source.YTMUSIC})[i];
            mm.reconnect();
        })).add(new Ui.Button("lyricsflow.opt.reconnect", null, () -> Ui.tr("lyricsflow.button.reconnect", new Object[0]), "refresh", false, mm::reconnect)));
        l.add(new Ui.Info(() -> Ui.tr("lyricsflow.info.automatic", new Object[0])));
        l.add(new Ui.Section("lyricsflow.section.spotify_exact"));
        l.add(new Ui.Card().add(new Ui.Status(() -> Ui.tr("lyricsflow.spotify.state." + SpotifyWeb.state(), new Object[0]), () -> {
            String e = SpotifyWeb.error();
            return "error".equals(SpotifyWeb.state()) && !e.isEmpty() ? Ui.tr("lyricsflow.spotify.err." + e, new Object[0]) : Ui.tr("lyricsflow.spotify.redirect", "http://127.0.0.1:8789/callback");
        }, () -> switch (SpotifyWeb.state()) {
            case "on" -> -11870592;
            case "waiting" -> -11414;
            case "error" -> -495247;
            default -> -9541498;
        })).add(new Ui.Button("lyricsflow.opt.spotify_dashboard", "lyricsflow.opt.spotify_dashboard.desc", () -> Ui.tr("lyricsflow.button.open", new Object[0]), "music", false, () -> class_156.method_668().method_670("https://developer.spotify.com/dashboard"))).add(new Ui.Button("lyricsflow.opt.spotify_copy_redirect", "lyricsflow.opt.spotify_copy_redirect.desc", () -> Ui.tr("lyricsflow.button.copy", new Object[0]), "copy", false, () -> {
            class_310.method_1551().field_1774.method_1455("http://127.0.0.1:8789/callback");
            Pages.note("lyricsflow.opt.spotify_copied");
        })).add(new Ui.TextField("lyricsflow.opt.spotify_client", "lyricsflow.opt.spotify_client.desc", "lyricsflow.opt.spotify_client.hint", SpotifyWeb::clientId, SpotifyWeb::setClientId)).add(new Ui.Button("lyricsflow.opt.spotify_login", null, () -> Ui.tr(SpotifyWeb.connected() ? "lyricsflow.button.disconnect" : "lyricsflow.button.connect", new Object[0]), "refresh", !SpotifyWeb.connected(), () -> {
            if (SpotifyWeb.connected()) {
                SpotifyWeb.disconnect();
            } else {
                SpotifyWeb.login();
            }
        })));
        l.add(new Ui.Info(() -> Ui.tr("lyricsflow.info.spotify_exact", new Object[0])));
        l.add(new Ui.Section("lyricsflow.section.playback"));
        l.add(new Ui.Card().add(new Ui.Slider("lyricsflow.opt.sync_offset", -2000.0, 2000.0, 50.0, () -> Pages.c().syncOffsetMs, v -> {
            Pages.c().syncOffsetMs = (int)Math.round(v);
        }, Pages::ms)).add(new Ui.Button("lyricsflow.opt.refetch", "lyricsflow.opt.refetch.desc", () -> Ui.tr("lyricsflow.button.retry", new Object[0]), "refresh", false, mm::refetch)));
        l.add(new Ui.Info(() -> Ui.tr("lyricsflow.info.sync_offset", new Object[0])));
    }

    private static void extra(List<Ui.Widget> l, SkyLyricsScreen screen) {
        l.add(new Ui.Section("lyricsflow.section.extra_hud"));
        l.add(new Ui.Card().add(new Ui.Toggle("lyricsflow.opt.hud_keys", "lyricsflow.opt.hud_keys.desc", () -> Pages.c().hudKeys, v -> {
            Pages.c().hudKeys = v;
        })).add(new Ui.Toggle("lyricsflow.opt.hud_cps", "lyricsflow.opt.hud_cps.desc", () -> Pages.c().hudCps, v -> {
            Pages.c().hudCps = v;
        })).add(new Ui.Toggle("lyricsflow.opt.hud_player", "lyricsflow.opt.hud_player.desc", () -> Pages.c().hudPlayer, v -> {
            Pages.c().hudPlayer = v;
            screen.rebuildSoon();
        })));
        if (Pages.c().hudPlayer) {
            l.add(new Ui.Card().add(new Ui.Toggle("lyricsflow.opt.hud_player_lyrics", "lyricsflow.opt.hud_player_lyrics.desc", () -> Pages.c().hudPlayerLyrics, v -> {
                Pages.c().hudPlayerLyrics = v;
            })));
        }
        l.add(new Ui.Section("lyricsflow.section.extra_look"));
        l.add(new Ui.Card().add(new Ui.Choice("lyricsflow.opt.hud_color", "lyricsflow.opt.hud_color.desc", new String[]{"lyricsflow.hud_color.font", "lyricsflow.hud_color.cover"}, () -> Pages.c().hudColorMode, i -> {
            Pages.c().hudColorMode = i;
        })).add(new Ui.Choice("lyricsflow.opt.hud_style", "lyricsflow.opt.hud_style.desc", new String[]{"lyricsflow.hud_style.round", "lyricsflow.hud_style.minecraft"}, () -> Pages.c().hudRound ? 0 : 1, i -> {
            Pages.c().hudRound = i == 0;
        })).add(new Ui.Button("lyricsflow.opt.hud_move", "lyricsflow.opt.hud_move.desc", () -> Ui.tr("lyricsflow.button.open", new Object[0]), "move", true, () -> class_310.method_1551().method_1507((class_437)new HudEditorScreen(screen)))).add(new Ui.Button("lyricsflow.opt.hud_reset", null, () -> Ui.tr("lyricsflow.button.reset", new Object[0]), "refresh", false, () -> {
            ExtraHud.resetPositions();
            ConfigManager.save();
        })));
        l.add(new Ui.Info(() -> Ui.tr("lyricsflow.info.hud_chat", new Object[0])));
        l.add(new Ui.Section("lyricsflow.section.hotbar"));
        Ui.Card hb = new Ui.Card().add(new Ui.Toggle("lyricsflow.opt.custom_hotbar", "lyricsflow.opt.custom_hotbar.desc", () -> Pages.c().customHotbar, v -> {
            Pages.c().customHotbar = v;
            screen.rebuildSoon();
        }));
        if (Pages.c().customHotbar) {
            hb.add(new Ui.Colors("lyricsflow.opt.hotbar_color", "lyricsflow.opt.hotbar_color.desc", () -> Pages.c().hotbarColor, v -> {
                Pages.c().hotbarColor = v;
            })).add(new Ui.Colors("lyricsflow.opt.hotbar_bar_color", "lyricsflow.opt.hotbar_bar_color.desc", () -> Pages.c().hotbarBarColor, v -> {
                Pages.c().hotbarBarColor = v;
            }));
        }
        l.add(hb);
    }

    private static void keys(List<Ui.Widget> l) {
        l.add(new Ui.Section("lyricsflow.section.keys"));
        l.add(new Ui.Card().add(new Ui.KeyBind("key.lyricsflow.menu", null, Keys.openMenu)).add(new Ui.KeyBind("key.lyricsflow.player", null, Keys.openPlayer)).add(new Ui.KeyBind("key.lyricsflow.onoff", "lyricsflow.opt.toggle_key.desc", Keys.toggle)).add(new Ui.Toggle("lyricsflow.opt.media_keys", "lyricsflow.opt.media_keys.desc", () -> Pages.c().mediaKeys, v -> {
            Pages.c().mediaKeys = v;
        })).add(new Ui.KeyBind("key.lyricsflow.play_pause", null, Keys.playPause)).add(new Ui.KeyBind("key.lyricsflow.next", null, Keys.next)).add(new Ui.KeyBind("key.lyricsflow.previous", null, Keys.previous)).add(new Ui.KeyBind("key.lyricsflow.earlier", null, Keys.lyricsEarlier)).add(new Ui.KeyBind("key.lyricsflow.later", null, Keys.lyricsLater)));
        l.add(new Ui.Info(() -> Ui.tr("lyricsflow.info.keys", new Object[0])));
        l.add(new Ui.Section("lyricsflow.section.sounds"));
        l.add(new Ui.Card().add(new Ui.Toggle("lyricsflow.opt.ui_sounds", "lyricsflow.opt.ui_sounds.desc", () -> Pages.c().uiSounds, v -> {
            Pages.c().uiSounds = v;
        })));
    }

    private static void saved(List<Ui.Widget> l, SkyLyricsScreen screen) {
        MediaManager mm = MediaManager.get();
        l.add(new Ui.Section("lyricsflow.section.timing"));
        l.add(new Ui.Card().add(new Ui.Slider("lyricsflow.opt.sync_offset", -2000.0, 2000.0, 50.0, () -> Pages.c().syncOffsetMs, v -> {
            Pages.c().syncOffsetMs = (int)Math.round(v);
        }, Pages::ms)).add(new Ui.Button("lyricsflow.opt.clear_song_offsets", "lyricsflow.opt.clear_song_offsets.desc", () -> Ui.tr("lyricsflow.button.clear_n", Pages.c().songOffsets.size()), "trash", false, () -> {
            Pages.c().songOffsets.clear();
            ConfigManager.save();
            screen.rebuildSoon();
        })).add(new Ui.Button("lyricsflow.opt.refetch", "lyricsflow.opt.refetch.desc", () -> Ui.tr("lyricsflow.button.retry", new Object[0]), "refresh", false, mm::refetch)).add(new Ui.Button("lyricsflow.opt.reconnect", null, () -> Ui.tr("lyricsflow.button.reconnect", new Object[0]), "refresh", false, mm::reconnect)));
        l.add(new Ui.Section("lyricsflow.section.profiles"));
        l.add(new Ui.Card().add(new Ui.Button("lyricsflow.opt.save_profile", "lyricsflow.opt.save_profile.desc", () -> Ui.tr("lyricsflow.button.save", new Object[0]), "download", true, () -> {
            Pages.c().profiles.put(Pages.freeName("lyricsflow.profile.name"), ConfigManager.snapshot());
            ConfigManager.save();
            screen.rebuildSoon();
        })).add(new Ui.Button("lyricsflow.opt.paste_profile", "lyricsflow.opt.paste_profile.desc", () -> Ui.tr("lyricsflow.button.paste", new Object[0]), "paste", false, () -> {
            String json = ConfigManager.importCode(class_310.method_1551().field_1774.method_1460());
            if (json == null) {
                pasteStatus = Ui.tr("lyricsflow.profile.paste_bad", new Object[0]);
                UiSounds.play(UiSounds.TOGGLE_OFF, 0.8f);
            } else {
                String name = Pages.freeName("lyricsflow.profile.pasted");
                Pages.c().profiles.put(name, json);
                ConfigManager.save();
                pasteStatus = Ui.tr("lyricsflow.profile.paste_ok", name);
                UiSounds.play(UiSounds.TOGGLE_ON, 1.1f);
            }
            screen.rebuildSoon();
        })));
        if (pasteStatus != null) {
            String ps = pasteStatus;
            l.add(new Ui.Info(() -> ps));
        }
        if (Pages.c().profiles.isEmpty()) {
            l.add(new Ui.Info(() -> Ui.tr("lyricsflow.info.no_profiles", new Object[0])));
        } else {
            for (Map.Entry<String, String> e : new ArrayList<Map.Entry<String, String>>(Pages.c().profiles.entrySet())) {
                String[] name = new String[]{e.getKey()};
                String[] draft = new String[]{e.getKey()};
                Ui.Card card = new Ui.Card();
                card.add(new Ui.TextField("lyricsflow.opt.profile_name", null, "lyricsflow.profile.name_hint", () -> draft[0], v -> {
                    draft[0] = v;
                    String nn = v.trim();
                    if (nn.isEmpty() || nn.equals(name[0]) || Pages.c().profiles.containsKey(nn)) {
                        return;
                    }
                    Pages.renameProfile(name[0], nn);
                    name[0] = nn;
                }));
                card.add(new Ui.Button("lyricsflow.opt.load_profile", null, () -> Ui.tr("lyricsflow.button.load", new Object[0]), "check", true, () -> {
                    ConfigManager.applySnapshot(Pages.c().profiles.get(name[0]));
                    WorldLyrics.clear();
                    screen.rebuildSoon();
                }));
                card.add(new Ui.Button("lyricsflow.opt.copy_profile", "lyricsflow.opt.copy_profile.desc", () -> Ui.tr("lyricsflow.button.copy", new Object[0]), "copy", false, () -> {
                    String json = Pages.c().profiles.get(name[0]);
                    if (json == null) {
                        return;
                    }
                    class_310.method_1551().field_1774.method_1455(ConfigManager.exportCode(json));
                    pasteStatus = Ui.tr("lyricsflow.profile.copied", name[0]);
                    screen.rebuildSoon();
                }));
                card.add(new Ui.Button("lyricsflow.opt.delete_profile", null, () -> Ui.tr("lyricsflow.button.delete", new Object[0]), "trash", false, () -> {
                    Pages.c().profiles.remove(name[0]);
                    ConfigManager.save();
                    screen.rebuildSoon();
                }));
                l.add(card);
                l.add(new Ui.Spacer(4.0f));
            }
        }
        l.add(new Ui.Section("lyricsflow.section.reset"));
        l.add(new Ui.Card().add(new Ui.Button("lyricsflow.opt.reset_all", "lyricsflow.opt.reset_all.desc", () -> Ui.tr("lyricsflow.button.reset", new Object[0]), "refresh", false, () -> {
            if (Pages.c().fpsBoost) {
                FpsBoost.apply(false);
            }
            ConfigManager.reset();
            Core.resetConfig();
            SkyImage.reload();
            AutoPerf.reset();
            WorldLyrics.clear();
            Fonts.clearCaches();
            dev.lyricsflow.app.hud.Gfx.clearWidths();
            Ui.syncFont();
            Pages.note("lyricsflow.opt.reset_all.done");
            screen.rebuildSoon();
        })));
    }

    private static String freeName(String key) {
        int n = Pages.c().profiles.size() + 1;
        String name = Ui.tr(key, n);
        while (Pages.c().profiles.containsKey(name)) {
            name = Ui.tr(key, ++n);
        }
        return name;
    }

    private static void renameProfile(String from, String to) {
        LinkedHashMap<String, String> m = new LinkedHashMap<String, String>();
        for (Map.Entry<String, String> e : Pages.c().profiles.entrySet()) {
            m.put(e.getKey().equals(from) ? to : e.getKey(), e.getValue());
        }
        Pages.c().profiles.clear();
        Pages.c().profiles.putAll(m);
        ConfigManager.save();
    }

    private static void perf(List<Ui.Widget> l, SkyLyricsScreen screen) {
        l.add(new Ui.Section("lyricsflow.section.perf_now"));
        l.add(new Ui.Card().add(new Ui.Custom(w -> Float.valueOf(50.0f), (g, x, y, w, h, mx, my, a) -> {
            String num = Integer.toString(AutoPerf.fps());
            Gfx.text(g, num, Ui.FB, x + 4.0f, y + 9.0f, 22.0f, Col.alpha(-856070, a));
            Gfx.text(g, "FPS", Ui.F, x + 4.0f + Gfx.textWidth(num, Ui.FB, 22.0f) + 6.0f, y + 24.0f, 8.0f, Col.alpha(-6515020, a));
            int lv = AutoPerf.level();
            String st = !Pages.c().autoPerf ? Ui.tr("lyricsflow.perf.auto_off", new Object[0]) : (lv == 0 ? Ui.tr("lyricsflow.perf.auto_full", new Object[0]) : Ui.tr("lyricsflow.perf.auto_level", lv, 6));
            String cost = Ui.tr("lyricsflow.perf.cost", String.format(Locale.ROOT, "%.1f", AutoPerf.modMs()));
            Gfx.text(g, cost, Ui.F, x + w - 4.0f - Gfx.textWidth(cost, Ui.F, 7.2f), y + 15.0f, 7.2f, Col.alpha(-6515020, a));
            Gfx.text(g, st, Ui.F, x + w - 4.0f - Gfx.textWidth(st, Ui.F, 7.2f), y + 27.0f, 7.2f, Col.alpha(-6515020, a));
        }, null)));
        l.add(new Ui.Section("lyricsflow.section.perf_auto"));
        String[] targets = new String[TARGETS.length];
        for (int i2 = 0; i2 < TARGETS.length; ++i2) {
            targets[i2] = TARGETS[i2] == 0 ? "lyricsflow.perf.target_auto" : TARGETS[i2] + " FPS";
        }
        l.add(new Ui.Card().add(new Ui.Toggle("lyricsflow.perf.auto", "lyricsflow.perf.auto.desc", () -> Pages.c().autoPerf, v -> {
            Pages.c().autoPerf = v;
            AutoPerf.reset();
        })).add(new Ui.Choice("lyricsflow.perf.target", "lyricsflow.perf.target.desc", targets, () -> {
            for (int i = 0; i < TARGETS.length; ++i) {
                if (TARGETS[i] != Pages.c().targetFps) continue;
                return i;
            }
            return 1;
        }, i -> {
            Pages.c().targetFps = TARGETS[i];
            AutoPerf.reset();
        })).add(new Ui.Choice("lyricsflow.perf.quality", null, new String[]{"lyricsflow.perf.quality.0", "lyricsflow.perf.quality.1", "lyricsflow.perf.quality.2"}, () -> Pages.c().quality, i -> {
            Pages.c().quality = i;
            Pages.sc().quality = i;
        })).add(new Ui.Choice("lyricsflow.perf.profile", null, new String[]{"Chill", "PvP"}, () -> Pages.c().profile, i -> {
            Profiles.set(i);
            Pages.note(i == 0 ? "lyricsflow.perf.profile.chill" : "lyricsflow.perf.profile.pvp");
            screen.rebuildSoon();
        })).add(new Ui.Toggle("lyricsflow.perf.boost", "lyricsflow.perf.boost.desc", () -> Pages.c().fpsBoost, FpsBoost::apply)));
        class_315 o = class_310.method_1551().field_1690;
        l.add(new Ui.Section("lyricsflow.section.perf_game"));
        l.add(new Ui.Card().add(new Ui.Slider("lyricsflow.perf.render_distance", 2.0, 32.0, 1.0, () -> ((Integer)o.method_42503().method_41753()).intValue(), v -> {
            o.method_42503().method_41748((Object)((int)Math.round(v)));
            o.method_1640();
        }, v -> Math.round(v) + " " + Ui.tr("lyricsflow.perf.chunks", new Object[0]))).add(new Ui.Slider("lyricsflow.perf.max_fps", 30.0, 260.0, 10.0, () -> ((Integer)o.method_42524().method_41753()).intValue(), v -> {
            o.method_42524().method_41748((Object)((int)Math.round(v)));
            o.method_1640();
        }, v -> v >= 260.0 ? Ui.tr("lyricsflow.perf.unlimited", new Object[0]) : Math.round(v) + " FPS")).add(new Ui.Choice("lyricsflow.perf.clouds", null, new String[]{"lyricsflow.perf.off", "lyricsflow.perf.fast", "lyricsflow.perf.fancy"}, () -> ((class_4063)o.method_42528().method_41753()).ordinal(), i -> {
            o.method_42528().method_41748((Object)class_4063.values()[i]);
            o.method_1640();
        })).add(new Ui.Choice("lyricsflow.perf.particles", null, new String[]{"lyricsflow.perf.all", "lyricsflow.perf.decreased", "lyricsflow.perf.minimal"}, () -> ((class_4066)o.method_42475().method_41753()).ordinal(), i -> {
            o.method_42475().method_41748((Object)class_4066.values()[i]);
            o.method_1640();
        })).add(new Ui.Toggle("lyricsflow.perf.shadows", null, () -> (Boolean)o.method_42435().method_41753(), v -> {
            o.method_42435().method_41748(v);
            o.method_1640();
        })).add(new Ui.Toggle("lyricsflow.perf.vsync", "lyricsflow.perf.vsync.desc", () -> (Boolean)o.method_42433().method_41753(), v -> {
            o.method_42433().method_41748(v);
            o.method_1640();
        })));
        l.add(new Ui.Section("lyricsflow.section.perf_effects"));
        l.add(new Ui.Card().add(new Ui.Toggle("lyricsflow.perf.sky_color", null, () -> Pages.sc().fxSkyColor, v -> {
            Pages.c().fxSkyColor = v;
            Pages.sc().fxSkyColor = v;
        })).add(new Ui.Toggle("lyricsflow.perf.weather", null, () -> Pages.sc().fxWeather, v -> {
            Pages.c().fxWeather = v;
            Pages.sc().fxWeather = v;
        })).add(new Ui.Toggle("lyricsflow.perf.aurora", null, () -> Pages.sc().fxAurora, v -> {
            Pages.c().fxAurora = v;
            Pages.sc().fxAurora = v;
        })).add(new Ui.Toggle("lyricsflow.perf.stars", null, () -> Pages.sc().fxStars, v -> {
            Pages.c().fxStars = v;
            Pages.sc().fxStars = v;
        })).add(new Ui.Toggle("lyricsflow.perf.glow", "lyricsflow.perf.glow.desc", () -> Pages.c().glow, v -> {
            Pages.c().glow = v;
        })).add(new Ui.Button("lyricsflow.perf.all_off", null, () -> Ui.tr("lyricsflow.button.apply", new Object[0]), null, false, () -> {
            Profiles.setAll(Pages.c(), false);
            ConfigManager.save();
            Pages.note("lyricsflow.perf.all_off.done");
            screen.rebuildSoon();
        })).add(new Ui.Button("lyricsflow.perf.all_on", null, () -> Ui.tr("lyricsflow.button.apply", new Object[0]), null, true, () -> {
            Profiles.setAll(Pages.c(), true);
            ConfigManager.save();
            Pages.note("lyricsflow.perf.all_on.done");
            screen.rebuildSoon();
        })));
    }

    private static void note(String key) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1705 != null) {
            mc.field_1705.method_1758((class_2561)class_2561.method_43470((String)Ui.tr(key, new Object[0])), false);
        }
    }

    static {
        DARK = Pages.argb(new int[]{131848, 460058, 656920, 1378328, 201240, 1053982, 1839672, 2758216, 4068404, 668480, 1784412, 4852242});
        BRIGHT = Pages.argb(new int[]{10719743, 13127912, 15757496, 15756906, 16296026, 16310384, 5957792, 3856584, 8046840, 5929200, 15263984, 0xFFFFFF});
        PLAIN_PALETTE = new int[]{-2084816, -1013212, -863174, -12793766, -12793640, -12948240, -7451928, -1021264, -855310, -16119286};
        SKY_PALETTE = new int[]{-16777216, -16053473, -15592914, -14020560, -16045520, -15459284, -14017974, -11916694, -10870704, -15050134, -14005622, -9826280};
        AURORA_PALETTE = new int[]{-5726977, -3649304, -1019720, -1021334, -481190, -466832, -10819424, -12920632, -8730376, -10848016, -1513232, -1};
        TARGETS = new int[]{0, 60, 90, 120, 144, 240};
    }
}


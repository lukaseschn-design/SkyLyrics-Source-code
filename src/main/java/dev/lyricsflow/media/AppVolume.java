package dev.lyricsflow.media;

import com.sun.jna.Memory;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import dev.lyricsflow.LyricsFlow;
import dev.lyricsflow.config.ConfigManager;
import dev.lyricsflow.config.LFConfig;
import dev.lyricsflow.media.Com;
import dev.lyricsflow.media.MediaManager;
import dev.lyricsflow.media.Track;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AppVolume {
    private static final Memory CLSID_ENUMERATOR = Com.guid("bcde0395-e52f-467c-8e3d-c4579291692e");
    private static final Memory IID_ENUMERATOR = Com.guid("a95664d2-9614-4f35-a746-de8db63617e6");
    private static final Memory IID_SESSION_MANAGER2 = Com.guid("77aa99a0-1bd6-484f-8bc7-2c654c9a9b6f");
    private static final Memory IID_SESSION_CONTROL2 = Com.guid("bfb7ff88-7239-4fc9-8fa2-07c950be9c6d");
    private static final Memory IID_SIMPLE_VOLUME = Com.guid("87ce5498-68d6-44e5-9215-6da47ef883d8");
    private static final Set<String> BROWSERS = Set.of("chrome.exe", "msedge.exe", "firefox.exe", "opera.exe", "opera_gx.exe", "brave.exe", "vivaldi.exe", "browser.exe", "arc.exe", "zen.exe", "librewolf.exe", "waterfox.exe");
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "SkyLyrics volume");
        t.setDaemon(true);
        return t;
    });
    private static final AtomicBoolean busy = new AtomicBoolean();
    private static volatile float volume = -1.0f;
    private static volatile boolean muted;
    private static volatile long lastRead;
    private static volatile float pendingSet;
    private static boolean inited;
    private static boolean failed;
    private static NativeLibrary ole32;
    private static NativeLibrary kernel32;

    private AppVolume() {
    }

    public static boolean supported() {
        return Platform.isWindows() && !failed;
    }

    private static boolean noSong() {
        Track t = MediaManager.get().track();
        return t == null || t.isEmpty();
    }

    public static float get() {
        if (!AppVolume.supported() || AppVolume.noSong()) {
            return -1.0f;
        }
        if (System.currentTimeMillis() - lastRead > 1000L && busy.compareAndSet(false, true)) {
            EXEC.execute(() -> {
                try {
                    AppVolume.apply(-1.0f, null);
                }
                finally {
                    busy.set(false);
                }
            });
        }
        return volume;
    }

    public static boolean muted() {
        return muted;
    }

    public static void set(float v) {
        if (!AppVolume.supported() || AppVolume.noSong()) {
            return;
        }
        volume = v = Math.max(0.0f, Math.min(1.0f, v));
        pendingSet = v;
        EXEC.execute(() -> {
            float p = pendingSet;
            if (p < 0.0f) {
                return;
            }
            pendingSet = -1.0f;
            AppVolume.apply(p, null);
        });
    }

    public static void toggleMute() {
        boolean m;
        if (!AppVolume.supported() || AppVolume.noSong()) {
            return;
        }
        muted = m = !muted;
        EXEC.execute(() -> AppVolume.apply(-1.0f, m));
    }

    private static boolean init() {
        if (inited) {
            return !failed;
        }
        inited = true;
        try {
            ole32 = NativeLibrary.getInstance((String)"ole32");
            kernel32 = NativeLibrary.getInstance((String)"kernel32");
            ole32.getFunction("CoInitializeEx").invokeInt(new Object[]{null, 0});
            return true;
        }
        catch (Throwable t) {
            LyricsFlow.LOG.warn("App volume unavailable", t);
            failed = true;
            return false;
        }
    }

    private static boolean wanted(String exe) {
        LFConfig.Source s = ConfigManager.get().source;
        if (s == LFConfig.Source.YOUTUBE) {
            return BROWSERS.contains(exe);
        }
        if (s == LFConfig.Source.YTMUSIC) {
            return BROWSERS.contains(exe) || exe.contains("youtube music") || exe.contains("youtube-music") || exe.contains("ytmusic");
        }
        return exe.equals("spotify.exe");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static String exeName(int pid) {
        Memory buf;
        Pointer h;
        block4: {
            String string;
            h = kernel32.getFunction("OpenProcess").invokePointer(new Object[]{4096, 0, pid});
            if (h == null) {
                return "";
            }
            try {
                buf = new Memory(2048L);
                IntByReference size = new IntByReference(1024);
                int ok = kernel32.getFunction("QueryFullProcessImageNameW").invokeInt(new Object[]{h, 0, buf, size});
                if (ok != 0) break block4;
                string = "";
            }
            catch (Throwable throwable) {
                kernel32.getFunction("CloseHandle").invokeInt(new Object[]{h});
                throw throwable;
            }
            kernel32.getFunction("CloseHandle").invokeInt(new Object[]{h});
            return string;
        }
        String path = buf.getWideString(0L);
        int i = Math.max(path.lastIndexOf(92), path.lastIndexOf(47));
        String string = path.substring(i + 1).toLowerCase(Locale.ROOT);
        kernel32.getFunction("CloseHandle").invokeInt(new Object[]{h});
        return string;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void apply(float setVolume, Boolean setMute) {
        block25: {
            ArrayList<Pointer> held;
            Pointer sessions;
            Pointer manager;
            Pointer device;
            Pointer enumerator;
            block24: {
                block23: {
                    block22: {
                        PointerByReference r;
                        block21: {
                            if (!AppVolume.init()) {
                                return;
                            }
                            enumerator = null;
                            device = null;
                            manager = null;
                            sessions = null;
                            held = new ArrayList<Pointer>();
                            r = new PointerByReference();
                            int hr = ole32.getFunction("CoCreateInstance").invokeInt(new Object[]{CLSID_ENUMERATOR, null, 23, IID_ENUMERATOR, r});
                            if (hr >= 0) break block21;
                            for (Pointer p : held) {
                                Com.release(p);
                            }
                            Com.release(sessions);
                            Com.release(manager);
                            Com.release(device);
                            Com.release(enumerator);
                            return;
                        }
                        enumerator = r.getValue();
                        device = Com.outPtr(enumerator, 4, 0, 1);
                        if (device != null) break block22;
                        for (Pointer p : held) {
                            Com.release(p);
                        }
                        Com.release(sessions);
                        Com.release(manager);
                        Com.release(device);
                        Com.release(enumerator);
                        return;
                    }
                    manager = Com.outPtr(device, 3, IID_SESSION_MANAGER2, 23, null);
                    if (manager != null) break block23;
                    for (Pointer p : held) {
                        Com.release(p);
                    }
                    Com.release(sessions);
                    Com.release(manager);
                    Com.release(device);
                    Com.release(enumerator);
                    return;
                }
                sessions = Com.outPtr(manager, 5, new Object[0]);
                if (sessions != null) break block24;
                for (Pointer p : held) {
                    Com.release(p);
                }
                Com.release(sessions);
                Com.release(manager);
                Com.release(device);
                Com.release(enumerator);
                return;
            }
            try {
                int count = Com.outInt(sessions, 3);
                float found = -1.0f;
                boolean anyMuted = false;
                for (int i = 0; i < count; ++i) {
                    IntByReference m;
                    FloatByReference f;
                    Pointer vol;
                    Pointer ctl = Com.outPtr(sessions, 4, i);
                    if (ctl == null) continue;
                    held.add(ctl);
                    Pointer ctl2 = Com.queryInterface(ctl, IID_SESSION_CONTROL2);
                    if (ctl2 == null) continue;
                    held.add(ctl2);
                    int pid = Com.outInt(ctl2, 14);
                    if (pid <= 0 || !AppVolume.wanted(AppVolume.exeName(pid)) || (vol = Com.queryInterface(ctl, IID_SIMPLE_VOLUME)) == null) continue;
                    held.add(vol);
                    if (setVolume >= 0.0f) {
                        Com.call(vol, 3, Float.valueOf(setVolume), null);
                    }
                    if (setMute != null) {
                        Com.call(vol, 5, setMute != false ? 1 : 0, null);
                    }
                    if (Com.call(vol, 4, f = new FloatByReference()) >= 0) {
                        found = Math.max(found, f.getValue());
                    }
                    if (Com.call(vol, 6, m = new IntByReference()) < 0 || m.getValue() == 0) continue;
                    anyMuted = true;
                }
                if (pendingSet < 0.0f) {
                    volume = found;
                }
                muted = anyMuted;
                lastRead = System.currentTimeMillis();
            }
            catch (Throwable t) {
                try {
                    LyricsFlow.LOG.debug("App volume failed", t);
                    lastRead = System.currentTimeMillis();
                    break block25;
                }
                catch (Throwable throwable) {
                    throw throwable;
                }
                finally {
                    for (Pointer p : held) {
                        Com.release(p);
                    }
                    Com.release(sessions);
                    Com.release(manager);
                    Com.release(device);
                    Com.release(enumerator);
                }
            }
            for (Pointer p : held) {
                Com.release(p);
            }
            Com.release(sessions);
            Com.release(manager);
            Com.release(device);
            Com.release(enumerator);
        }
    }

    static {
        pendingSet = -1.0f;
    }
}


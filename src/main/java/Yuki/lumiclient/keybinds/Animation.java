package Yuki.lumiclient.keybinds;

public final class Animation {
    private Animation() {
    }

    public static double approach(double value, double target, double seconds) {
        return target + (value - target) * Math.exp(-18.0 * Math.max(0.0, seconds));
    }

    public static double entrance(double seconds) {
        double t = Math.clamp(seconds / 0.24, 0.0, 1.0);
        return 1.0 - Math.pow(1.0 - t, 3.0);
    }
}

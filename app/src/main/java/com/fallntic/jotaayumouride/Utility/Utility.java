package com.fallntic.jotaayumouride.Utility;

public class Utility {

    public static String convertDuration(long duration) {
        long minutes = (duration / 1000) / 60;
        long seconds = (duration / 1000) % 60;
        String converted = String.format("%d:%02d", minutes, seconds);
        return converted;
    }

    public static String formatDuration(int seconds) {
        int p1 = seconds % 60;
        int p2 = seconds / 60;
        int p3 = p2 % 60;
        p2 = p2 / 60;

        if (p2 > 0)
            return p2 + ":" + p3 + ":" + p1;
        else
            return p3 + ":" + p1;
    }
}

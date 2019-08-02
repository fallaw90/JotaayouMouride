package com.fallntic.jotaayumouride.Utility;

public class Utility {

    public static String convertDuration(int duration) {
        long minutes = (duration / 1000) / 60;
        long seconds = (duration / 1000) % 60;
        String converted = String.format("%d:%02d", minutes, seconds);
        return converted;


    }
}

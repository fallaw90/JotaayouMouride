package com.fallntic.jotaayumouride.Utility;

import com.fallntic.jotaayumouride.Model.Song;

import java.util.Comparator;

public class SortBySongName implements Comparator <Song> {

    @Override
    public int compare(Song song, Song t1) {
        return song.getAudioTitle().compareToIgnoreCase(t1.getAudioTitle());
    }
}

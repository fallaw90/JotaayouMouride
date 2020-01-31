package com.fallntic.jotaayumouride.utility;

import com.fallntic.jotaayumouride.model.Song;

import java.util.Comparator;

@SuppressWarnings("unused")
class SortBySongName implements Comparator<Song> {

    @Override
    public int compare(Song song, Song t1) {
        return song.getAudioTitle().compareToIgnoreCase(t1.getAudioTitle());
    }
}

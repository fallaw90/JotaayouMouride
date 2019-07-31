package com.fallntic.jotaayumouride.Model;

import com.google.firebase.database.Exclude;

public class UploadSong {
    public String songTitle;
    public String getSongDuration;
    public String getSongLink;
    public String mKey;

    public UploadSong(String songTitle, String getSongDuration, String getSongLink) {

        if (songTitle.trim().equals("")){
            songTitle = "No title";
        }
        this.songTitle = songTitle;
        this.getSongDuration = getSongDuration;
        this.getSongLink = getSongLink;
        this.mKey = mKey;
    }

    public UploadSong() {}

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getGetSongDuration() {
        return getSongDuration;
    }

    public void setGetSongDuration(String getSongDuration) {
        this.getSongDuration = getSongDuration;
    }

    public String getGetSongLink() {
        return getSongLink;
    }

    public void setGetSongLink(String getSongLink) {
        this.getSongLink = getSongLink;
    }

    @Exclude
    public String getmKey() {
        return mKey;
    }

    @Exclude
    public void setmKey(String mKey) {
        this.mKey = mKey;
    }
}

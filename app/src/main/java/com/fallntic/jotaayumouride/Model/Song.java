package com.fallntic.jotaayumouride.Model;

import com.google.firebase.database.Exclude;

public class Song {
    public String audioTitle;
    public String audioDuration;
    public String audioUri;
    public String audioID;
    public boolean isPlaying;

    public Song(String audioID, String audioTitle, String audioDuration, String audioUri) {

        if (audioTitle.trim().equals("")) {
            audioTitle = "No title";
        }
        this.audioTitle = audioTitle;
        this.audioDuration = audioDuration;
        this.audioUri = audioUri;
        this.audioID = audioID;
    }

    public Song() {
    }

    public String getAudioTitle() {
        return audioTitle;
    }

    public void setAudioTitle(String songTitle) {
        this.audioTitle = songTitle;
    }

    public long getAudioDuration() {
        String str_duration = audioDuration.replace(":", "");
        return Long.parseLong(str_duration);
    }

    public void setAudioDuration(String songDuration) {
        this.audioDuration = songDuration;
    }

    public String getAudioUri() {
        return audioUri;
    }

    public void setAudioUri(String stringUri) {
        this.audioUri = stringUri;
    }

    @Exclude
    public String getAudioID() {
        return audioID;
    }

    @Exclude
    public void setAudioID(String audioID) {
        this.audioID = audioID;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }
}

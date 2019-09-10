package com.fallntic.jotaayumouride.Model;

import com.google.firebase.database.Exclude;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Song implements Comparable<Song>{
    public String audioTitle;
    public String audioDuration;
    public String audioUri;
    public String audioID;
    public String date;
    public boolean playing;
    public String userID;

    public Song(String audioID, String audioTitle, String audioDuration, String audioUri) {

        if (audioTitle.trim().equals("")) {
            audioTitle = "No title";
        }
        this.audioTitle = audioTitle;
        this.audioDuration = audioDuration;
        this.audioUri = audioUri;
        this.audioID = audioID;
        this.playing = false;

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        this.date = formatter.format(date);
    }

    public Song() {
    }

    public String getAudioTitle() {
        return audioTitle;
    }

    public void setAudioTitle(String songTitle) {
        this.audioTitle = songTitle;
    }

    public String getAudioDuration() {
        return audioDuration;
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
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    @Override
    public int compareTo(Song song) {
        return this.audioTitle.compareToIgnoreCase(song.getAudioTitle());
    }
}

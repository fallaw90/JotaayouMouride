package com.fallntic.jotaayumouride.model;

import android.annotation.SuppressLint;

import com.google.firebase.database.Exclude;

import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("unused")
public class Song implements Comparable<Song>{
    public String audioTitle;
    private String audioDuration;
    public String audioUri;
    private String audioID;
    private String date;
    private boolean playing;
    private String userID;

    public Song(String audioID, String audioTitle, String audioDuration, String audioUri) {

        if (audioTitle.trim().equals("")) {
            audioTitle = "No title";
        }
        this.audioTitle = audioTitle;
        this.audioDuration = audioDuration;
        this.audioUri = audioUri;
        this.audioID = audioID;
        this.playing = false;

        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        this.date = formatter.format(date);
    }

    public Song() {
    }

    public String getAudioTitle() {
        return audioTitle;
    }

    @SuppressWarnings("unused")
    public void setAudioTitle(String songTitle) {
        this.audioTitle = songTitle;
    }

    public String getAudioDuration() {
        return audioDuration;
    }

    @SuppressWarnings("unused")
    public void setAudioDuration(String songDuration) {
        this.audioDuration = songDuration;
    }

    public String getAudioUri() {
        return audioUri;
    }

    @SuppressWarnings("unused")
    public void setAudioUri(String stringUri) {
        this.audioUri = stringUri;
    }

    @Exclude
    public String getAudioID() {
        return audioID;
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public void setDate(String date) {
        this.date = date;
    }

    public String getUserID() {
        return userID;
    }

    @SuppressWarnings("unused")
    public void setUserID(String userID) {
        this.userID = userID;
    }

    @Override
    public int compareTo(Song song) {
        return this.audioTitle.compareToIgnoreCase(song.getAudioTitle());
    }


}

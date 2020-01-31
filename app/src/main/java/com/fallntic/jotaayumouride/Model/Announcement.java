package com.fallntic.jotaayumouride.model;

import android.annotation.SuppressLint;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("unused")
public class Announcement implements Serializable, Comparable<Announcement> {

    private String mDate;
    private String announcementID;
    private String userName;
    private String note;

    public Announcement(String announcementID, String userName, String note) {
        this.announcementID = announcementID;
        this.userName = userName;
        this.note = note;

        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        this.mDate = formatter.format(date);
    }

    public Announcement() {
    }

    public String getAnnouncementID() {
        return announcementID;
    }

    public String getUserName() {
        return userName;
    }

    public String getNote() {
        return note;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public void setAnnouncementID(String announcementID) {
        this.announcementID = announcementID;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public int compareTo(Announcement announcement) {
        return this.mDate.compareTo(announcement.getDate());
    }
}

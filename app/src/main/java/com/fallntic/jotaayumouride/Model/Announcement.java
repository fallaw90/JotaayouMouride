package com.fallntic.jotaayumouride.Model;

import com.fallntic.jotaayumouride.DataHolder;

import java.io.Serializable;

public class Announcement implements Serializable {

    public String mDate;
    private String announcementID;
    private String userName;
    private String note;

    public Announcement(String announcementID, String userName, String note) {
        this.announcementID = announcementID;
        this.userName = userName;
        this.note = note;
        this.mDate = DataHolder.getCurrentDate();
    }

    public Announcement() {
    }

    public String getAnnouncementID() {
        return announcementID;
    }

    public void setAnnouncementID(String announcementID) {
        this.announcementID = announcementID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String mDate) {
        this.mDate = mDate;
    }
}

package com.fallntic.jotaayumouride.Model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Announcement implements Serializable, Comparable<Announcement> {

    public String mDate;
    private String announcementID;
    private String userName;
    private String note;

    public Announcement(String announcementID, String userName, String note) {
        this.announcementID = announcementID;
        this.userName = userName;
        this.note = note;

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        this.mDate = formatter.format(date);
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

    @Override
    public int compareTo(Announcement announcement) {
        int i = this.mDate.compareTo(announcement.getDate());
        return i;
    }
}

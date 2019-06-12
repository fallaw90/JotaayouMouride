package com.fallntic.jotaayumouride;

import java.io.Serializable;

public class Event implements Serializable {

    private String dahiraID;
    private String eventID;
    private String dahiraName;
    private String title;
    private String note;
    private String date;
    private String location;
    private String startTime;
    private String endTime;
    private String guest;

    public Event(String dahiraID, String eventID, String dahiraName, String title, String note,
                 String date, String location, String startTime, String endTime, String guest) {

        this.dahiraID = dahiraID;
        this.eventID = eventID;
        this.dahiraName = dahiraName;
        this.title = title;
        this.note = note;
        this.date = date;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.guest = guest;
    }

    public Event(){}

    public String getDahiraID() {
        return dahiraID;
    }

    public void setDahiraID(String dahiraID) {
        this.dahiraID = dahiraID;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getDahiraName() {
        return dahiraName;
    }

    public void setDahiraName(String dahiraName) {
        this.dahiraName = dahiraName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getGuest() {
        return guest;
    }

    public void setGuest(String guest) {
        this.guest = guest;
    }
}

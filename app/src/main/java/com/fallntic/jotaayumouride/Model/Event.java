package com.fallntic.jotaayumouride.model;

import java.io.Serializable;

@SuppressWarnings("ALL")
public class Event implements Serializable, Comparable<Event> {

    private String eventID;
    private String userName;
    private String date;
    private String title;
    private String note;
    private String location;
    private String startTime;
    private String endTime;

    public Event(String eventID, String userName, String date, String title,
                 String note, String location, String startTime, String endTime) {
        this.eventID = eventID;
        this.userName = userName;
        this.date = date;
        this.title = title;
        this.note = note;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @SuppressWarnings("unused")
    public Event() {
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    @Override
    public int compareTo(Event event) {
        return this.date.compareTo(event.getDate());
    }
}

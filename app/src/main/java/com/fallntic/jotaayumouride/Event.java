package com.fallntic.jotaayumouride;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Event implements Serializable {

    private String dahiraID;
    private List<String> listUserID = new ArrayList<String>();
    private List<String> listUserName = new ArrayList<String>();
    private List<String> listDate = new ArrayList<String>();
    private List<String> listTitle = new ArrayList<String>();
    private List<String> listNote = new ArrayList<String>();
    private List<String> listLocation = new ArrayList<String>();
    private List<String> listStartTime = new ArrayList<String>();
    private List<String> listEndTime = new ArrayList<String>();

    public Event(String dahiraID, List<String> listUserID, List<String> listUserName,
                 List<String> listDate, List<String> listTitle, List<String> listNote,
                 List<String> listLocation, List<String> listStartTime, List<String> listEndTime) {
        this.dahiraID = dahiraID;
        this.listUserID = listUserID;
        this.listUserName = listUserName;
        this.listDate = listDate;
        this.listTitle = listTitle;
        this.listNote = listNote;
        this.listLocation = listLocation;
        this.listStartTime = listStartTime;
        this.listEndTime = listEndTime;
    }

    public Event(){}

    public String getDahiraID() {
        return dahiraID;
    }

    public void setDahiraID(String dahiraID) {
        this.dahiraID = dahiraID;
    }

    public List<String> getListUserID() {
        return listUserID;
    }

    public void setListUserID(List<String> listUserID) {
        this.listUserID = listUserID;
    }

    public List<String> getListUserName() {
        return listUserName;
    }

    public void setListUserName(List<String> listUserName) {
        this.listUserName = listUserName;
    }

    public List<String> getListDate() {
        return listDate;
    }

    public void setListDate(List<String> listDate) {
        this.listDate = listDate;
    }

    public List<String> getListTitle() {
        return listTitle;
    }

    public void setListTitle(List<String> listTitle) {
        this.listTitle = listTitle;
    }

    public List<String> getListNote() {
        return listNote;
    }

    public void setListNote(List<String> listNote) {
        this.listNote = listNote;
    }

    public List<String> getListLocation() {
        return listLocation;
    }

    public void setListLocation(List<String> listLocation) {
        this.listLocation = listLocation;
    }

    public List<String> getListStartTime() {
        return listStartTime;
    }

    public void setListStartTime(List<String> listStartTime) {
        this.listStartTime = listStartTime;
    }

    public List<String> getListEndTime() {
        return listEndTime;
    }

    public void setListEndTime(List<String> listEndTime) {
        this.listEndTime = listEndTime;
    }
}

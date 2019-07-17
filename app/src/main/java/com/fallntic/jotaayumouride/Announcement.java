package com.fallntic.jotaayumouride;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Announcement implements Serializable {

    private String dahiraID;
    private List<String> listUserID = new ArrayList<String>();
    private List<String> listUserName = new ArrayList<String>();
    private List<String> listDate = new ArrayList<String>();
    private List<String> listNote = new ArrayList<String>();

    public Announcement(String dahiraID, List<String> listUserID, List<String> listUserName, List<String> listDate, List<String> listNote) {
        this.dahiraID = dahiraID;
        this.listUserID = listUserID;
        this.listUserName = listUserName;
        this.listDate = listDate;
        this.listNote = listNote;
    }

    public Announcement(){}

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

    public List<String> getListNote() {
        return listNote;
    }

    public void setListNote(List<String> listNote) {
        this.listNote = listNote;
    }
}

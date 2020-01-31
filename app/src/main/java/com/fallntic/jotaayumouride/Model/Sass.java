package com.fallntic.jotaayumouride.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Sass implements Serializable {

    private List<String> listDahiraID = new ArrayList<>();
    private List<String> listDate = new ArrayList<>();
    private List<String> listSass = new ArrayList<>();
    private List<String> listUserName = new ArrayList<>();

    public Sass(List<String> listDahiraID, List<String> listDate, List<String> listSass, List<String> listUserName) {
        this.listDahiraID = listDahiraID;
        this.listDate = listDate;
        this.listSass = listSass;
        this.listUserName = listUserName;
    }

    public Sass() {
    }

    public List<String> getListDahiraID() {
        return listDahiraID;
    }


    public List<String> getListDate() {
        return listDate;
    }

    public List<String> getListSass() {
        return listSass;
    }

    public List<String> getListUserName() {
        return listUserName;
    }

    public void setListDahiraID(List<String> listDahiraID) {
        this.listDahiraID = listDahiraID;
    }

    public void setListDate(List<String> listDate) {
        this.listDate = listDate;
    }

    public void setListSass(List<String> listSass) {
        this.listSass = listSass;
    }

    public void setListUserName(List<String> listUserName) {
        this.listUserName = listUserName;
    }
}

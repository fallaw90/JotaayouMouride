package com.fallntic.jotaayumouride;

import java.util.ArrayList;
import java.util.List;

public class Sass {

    private List<String> listDahiraID = new ArrayList<String>();
    private List<String> listDate = new ArrayList<String>();
    private List<String> listSass = new ArrayList<String>();

    public Sass(List<String> listDahiraID, List<String> listDate, List<String> listSass) {
        this.listDahiraID = listDahiraID;
        this.listDate = listDate;
        this.listSass = listSass;
    }

    public Sass() {}

    public List<String> getListDahiraID() {
        return listDahiraID;
    }

    public void setListDahiraID(List<String> listDahiraID) {
        this.listDahiraID = listDahiraID;
    }

    public List<String> getListDate() {
        return listDate;
    }

    public void setListDate(List<String> listDate) {
        this.listDate = listDate;
    }

    public List<String> getListSass() {
        return listSass;
    }

    public void setListSass(List<String> listSass) {
        this.listSass = listSass;
    }
}

package com.fallntic.jotaayumouride;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Events  implements Serializable {

    private List<String> listDahiraID = new ArrayList<String>();
    private List<String> listDates = new ArrayList<String>();
    private List<String> listTitles = new ArrayList<String>();
    private List<String> listNotes = new ArrayList<String>();
    private List<String> listGuests = new ArrayList<String>();
    private List<String> listLocations = new ArrayList<String>();
    private List<String> listStartDates = new ArrayList<String>();
    private List<String> listEndDates = new ArrayList<String>();

    public Events(List<String> listDahiraID, List<String> listDates, List<String> listTitles,
                  List<String> listNotes, List<String> listGuests, List<String> listLocations,
                  List<String> listStartDates, List<String> listEndDates) {

        this.listDahiraID = listDahiraID;
        this.listDates = listDates;
        this.listTitles = listTitles;
        this.listNotes = listNotes;
        this.listGuests = listGuests;
        this.listLocations = listLocations;
        this.listStartDates = listStartDates;
        this.listEndDates = listEndDates;
    }

    public Events(){}

    public List<String> getListDahiraID() {
        return listDahiraID;
    }

    public void setListDahiraID(List<String> listDahiraID) {
        this.listDahiraID = listDahiraID;
    }

    public List<String> getListDates() {
        return listDates;
    }

    public void setListDates(List<String> listDates) {
        this.listDates = listDates;
    }

    public List<String> getListTitles() {
        return listTitles;
    }

    public void setListTitles(List<String> listTitles) {
        this.listTitles = listTitles;
    }

    public List<String> getListNotes() {
        return listNotes;
    }

    public void setListNotes(List<String> listNotes) {
        this.listNotes = listNotes;
    }

    public List<String> getListGuests() {
        return listGuests;
    }

    public void setListGuests(List<String> listGuests) {
        this.listGuests = listGuests;
    }

    public List<String> getListLocations() {
        return listLocations;
    }

    public void setListLocations(List<String> listLocations) {
        this.listLocations = listLocations;
    }

    public List<String> getListStartDates() {
        return listStartDates;
    }

    public void setListStartDates(List<String> listStartDates) {
        this.listStartDates = listStartDates;
    }

    public List<String> getListEndDates() {
        return listEndDates;
    }

    public void setListEndDates(List<String> listEndDates) {
        this.listEndDates = listEndDates;
    }
}

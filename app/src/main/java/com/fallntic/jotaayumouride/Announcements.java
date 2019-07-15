package com.fallntic.jotaayumouride;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Announcements  implements Serializable {

    private List<String> listDates = new ArrayList<String>();
    private List<String> listNotes = new ArrayList<String>();

    public Announcements(List<String> listDates, List<String> listNotes) {
        this.listDates = listDates;
        this.listNotes = listNotes;
    }

    public Announcements(){}

    public List<String> getListDates() {
        return listDates;
    }

    public void setListDates(List<String> listDates) {
        this.listDates = listDates;
    }

    public List<String> getListNotes() {
        return listNotes;
    }

    public void setListNotes(List<String> listNotes) {
        this.listNotes = listNotes;
    }
}

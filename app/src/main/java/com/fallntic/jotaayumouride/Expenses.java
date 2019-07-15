package com.fallntic.jotaayumouride;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Expenses  implements Serializable {

    private List<String> listDates = new ArrayList<String>();
    private List<String> listLabels = new ArrayList<String>();
    private List<String> listNotes = new ArrayList<String>();
    private List<String> listPrices = new ArrayList<String>();

    public Expenses(List<String> listDates, List<String> listLabels,
                    List<String> listNotes, List<String> listPrices) {
        this.listDates = listDates;
        this.listLabels = listLabels;
        this.listNotes = listNotes;
        this.listPrices = listPrices;
    }

    public Expenses(){}

    public List<String> getListDates() {
        return listDates;
    }

    public void setListDates(List<String> listDates) {
        this.listDates = listDates;
    }

    public List<String> getListLabels() {
        return listLabels;
    }

    public void setListLabels(List<String> listLabels) {
        this.listLabels = listLabels;
    }

    public List<String> getListNotes() {
        return listNotes;
    }

    public void setListNotes(List<String> listNotes) {
        this.listNotes = listNotes;
    }

    public List<String> getListPrices() {
        return listPrices;
    }

    public void setListPrices(List<String> listPrices) {
        this.listPrices = listPrices;
    }
}

package com.fallntic.jotaayumouride;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Adiya implements Serializable {

    private List<String> listDahiraID = new ArrayList<String>();
    private List<String> listDate = new ArrayList<String>();
    private List<String> listAdiya = new ArrayList<String>();
    private List<String> listUserName = new ArrayList<String>();

    public Adiya(List<String> listDahiraID, List<String> listDate, List<String> listAdiya, List<String> listUserName) {
        this.listDahiraID = listDahiraID;
        this.listDate = listDate;
        this.listAdiya = listAdiya;
        this.listUserName = listUserName;
    }

    public Adiya(){}

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

    public List<String> getListAdiya() {
        return listAdiya;
    }

    public void setListAdiya(List<String> listAdiya) {
        this.listAdiya = listAdiya;
    }

    public List<String> getListUserName() {
        return listUserName;
    }

    public void setListUserName(List<String> listUserName) {
        this.listUserName = listUserName;
    }
}

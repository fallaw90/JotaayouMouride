package com.fallntic.jotaayumouride.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Social implements Serializable {

    private List<String> listDahiraID = new ArrayList<String>();
    private List<String> listDate = new ArrayList<String>();
    private List<String> listSocial = new ArrayList<String>();
    private List<String> listUserName = new ArrayList<String>();

    public Social(List<String> listDahiraID, List<String> listDate, List<String> listSocial, List<String> listUserName) {
        this.listDahiraID = listDahiraID;
        this.listDate = listDate;
        this.listSocial = listSocial;
        this.listUserName = listUserName;
    }

    public Social() {
    }

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

    public List<String> getListSocial() {
        return listSocial;
    }

    public void setListSocial(List<String> listSocial) {
        this.listSocial = listSocial;
    }

    public List<String> getListUserName() {
        return listUserName;
    }

    public void setListUserName(List<String> listUserName) {
        this.listUserName = listUserName;
    }
}

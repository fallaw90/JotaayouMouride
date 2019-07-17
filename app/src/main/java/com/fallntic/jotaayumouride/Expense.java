package com.fallntic.jotaayumouride;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Expense implements Serializable {

    private String dahiraID;
    private List<String> listUserName = new ArrayList<String>();
    private List<String> listUserID = new ArrayList<String>();
    private List<String> listDate = new ArrayList<String>();
    private List<String> listNote = new ArrayList<String>();
    private List<String> listPrice = new ArrayList<String>();
    private List<String> listTypeOfExpense = new ArrayList<String>();

    public Expense(String dahiraID, List<String> listUserID, List<String> listUserName, List<String> listDate,
                   List<String> listNote, List<String> listPrice, List<String> listTypeOfExpense) {
        this.dahiraID = dahiraID;
        this.listUserID = listUserID;
        this.listUserName = listUserName;
        this.listDate = listDate;
        this.listNote = listNote;
        this.listPrice = listPrice;
        this.listTypeOfExpense = listTypeOfExpense;
    }

    public Expense(){}

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

    public List<String> getListPrice() {
        return listPrice;
    }

    public void setListPrice(List<String> listPrice) {
        this.listPrice = listPrice;
    }

    public List<String> getListTypeOfExpense() {
        return listTypeOfExpense;
    }

    public void setListTypeOfExpense(List<String> listTypeOfExpense) {
        this.listTypeOfExpense = listTypeOfExpense;
    }
}

package com.fallntic.jotaayumouride;


import java.io.Serializable;

public class Expense implements Serializable {

    private String dahiraID;
    private String spentID;
    private String dahiraName;
    private String item;
    private double price;
    private String date;
    private String note;
    private double totalExpenses;

    public Expense(String dahiraID, String spentID, String dahiraName, String item, double price, String date, String note, double totalExpenses) {

        this.dahiraID = dahiraID;
        this.spentID = spentID;
        this.dahiraName = dahiraName;
        this.item = item;
        this.date = date;
        this.price = price;
        this.date = date;
        this.note = note;
        this.totalExpenses = totalExpenses;
    }

    public String getDahiraID() {
        return dahiraID;
    }

    public void setDahiraID(String dahiraID) {
        this.dahiraID = dahiraID;
    }

    public String getSpentID() {
        return spentID;
    }

    public void setSpentID(String spentID) {
        this.spentID = spentID;
    }

    public String getDahiraName() {
        return dahiraName;
    }

    public void setDahiraName(String dahiraName) {
        this.dahiraName = dahiraName;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public double getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(double totalExpenses) {
        this.totalExpenses = totalExpenses;
    }
}

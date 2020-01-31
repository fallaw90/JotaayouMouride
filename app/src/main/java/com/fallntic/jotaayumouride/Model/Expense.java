package com.fallntic.jotaayumouride.model;

import java.io.Serializable;

@SuppressWarnings("unused")
public class Expense implements Serializable {

    private String expenseID;
    private String userName;
    private String date;
    private String note;
    private String price;
    private String typeOfExpense;

    public Expense(String expenseID, String userName, String date,
                   String note, String price, String typeOfExpense) {
        this.expenseID = expenseID;
        this.userName = userName;
        this.date = date;
        this.note = note;
        this.price = price;
        this.typeOfExpense = typeOfExpense;
    }

    public Expense() {
    }

    public String getExpenseID() {
        return expenseID;
    }

    public void setDahiraID(String expenseID) {
        this.expenseID = expenseID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public void setExpenseID(String expenseID) {
        this.expenseID = expenseID;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTypeOfExpense() {
        return typeOfExpense;
    }

    public void setTypeOfExpense(String typeOfExpense) {
        this.typeOfExpense = typeOfExpense;
    }
}

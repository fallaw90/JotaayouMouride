package com.fallntic.jotaayumouride;


import java.io.Serializable;


public class Announce implements Serializable {

    private String dahiraID;
    private String annonceID;
    private String dahiraName;
    private String title;
    private String note;
    private String date;
    private String lieu;
    private String heure;

    public Announce(String dahiraID, String annonceID, String dahiraName, String title, String note,
                    String date, String lieu, String heure) {

        this.dahiraID = dahiraID;
        this.dahiraName = dahiraName;
        this.title = title;
        this.note = note;
        this.date = date;
        this.lieu = lieu;
        this.heure = heure;
    }

    public Announce(){}

    public String getDahiraID() {
        return dahiraID;
    }

    public void setDahiraID(String dahiraID) {
        this.dahiraID = dahiraID;
    }

    public String getAnnonceID() {
        return annonceID;
    }

    public void setAnnonceID(String annonceID) {
        this.annonceID = annonceID;
    }

    public String getDahiraName() {
        return dahiraName;
    }

    public void setDahiraName(String dahiraName) {
        this.dahiraName = dahiraName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public String getHeure() {
        return heure;
    }

    public void setHeure(String heure) {
        this.heure = heure;
    }
}

package com.fallntic.jotaayumouride.model;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings({"ALL", "unused"})
public class Dahira implements Serializable, Comparable<Dahira> {

    private String dahiraID;
    private String dahiraName;
    private String dieuwrine;
    private String dahiraPhoneNumber;
    private String siege;
    private String totalAdiya;
    private String totalSass;
    private String totalSocial;
    private String totalMember;
    private String imageUri;
    private double currentSizeStorage;
    private double dedicatedSizeStorage;
    private List<String> listCommissions;
    private List<String> listResponsibles;

    public Dahira() {
    }

    public Dahira(String dahiraID, String dahiraName, String dieuwrine, String dahiraPhoneNumber,
                  String siege, String totalAdiya, String totalSass, String totalSocial,
                  String totalMember, String imageUri, List<String> listCommissions, List<String> listResponsibles) {

        this.dahiraID = dahiraID;
        this.dahiraName = dahiraName;
        this.dieuwrine = dieuwrine;
        this.dahiraPhoneNumber = dahiraPhoneNumber;
        this.siege = siege;
        this.totalAdiya = totalAdiya;
        this.totalSass = totalSass;
        this.totalSocial = totalSocial;
        this.totalMember = totalMember;
        this.listCommissions = listCommissions;
        this.listResponsibles = listResponsibles;
        this.imageUri = imageUri;
        this.currentSizeStorage = 0;
        this.dedicatedSizeStorage = 250;
    }

    public String getDahiraID() {
        return dahiraID;
    }

    public void setDahiraID(String dahiraID) {
        this.dahiraID = dahiraID;
    }

    public String getDahiraName() {
        return dahiraName;
    }

    public void setDahiraName(String dahiraName) {
        this.dahiraName = dahiraName;
    }

    public String getDieuwrine() {
        return dieuwrine;
    }

    public void setDieuwrine(String dieuwrine) {
        this.dieuwrine = dieuwrine;
    }

    public String getDahiraPhoneNumber() {
        return dahiraPhoneNumber;
    }

    public void setDahiraPhoneNumber(String dahiraPhoneNumber) {
        this.dahiraPhoneNumber = dahiraPhoneNumber;
    }

    public String getSiege() {
        return siege;
    }

    public void setSiege(String siege) {
        this.siege = siege;
    }


    public String getTotalAdiya() {
        return totalAdiya;
    }

    public void setTotalAdiya(String totalAdiya) {
        this.totalAdiya = totalAdiya;
    }

    public String getTotalSass() {
        return totalSass;
    }

    public void setTotalSass(String totalSass) {
        this.totalSass = totalSass;
    }

    public String getTotalSocial() {
        return totalSocial;
    }

    public void setTotalSocial(String totalSocial) {
        this.totalSocial = totalSocial;
    }

    public String getTotalMember() {
        return totalMember;
    }

    public void setTotalMember(String totalMember) {
        this.totalMember = totalMember;
    }

    public List<String> getListCommissions() {
        return listCommissions;
    }

    public void setListCommissions(List<String> listCommissions) {
        this.listCommissions = listCommissions;
    }

    public List<String> getListResponsibles() {
        return listResponsibles;
    }

    public void setListResponsibles(List<String> listResponsibles) {
        this.listResponsibles = listResponsibles;
    }

    /*@Override
    public int compareTo(Dahira dahira) {
        return this.dahiraName.compareToIgnoreCase(dahira.getDahiraName());
    }*/

    @Override
    public int compareTo(Dahira dahira) {
        if (getDahiraName() == null || dahira.getDahiraName() == null) {
            return 0;
        }
        return getDahiraName().compareToIgnoreCase(dahira.getDahiraName());
    }

    public double getCurrentSizeStorage() {
        return currentSizeStorage;
    }

    public void setCurrentSizeStorage(double currentSizeStorage) {
        this.currentSizeStorage = currentSizeStorage;
    }

    public double getDedicatedSizeStorage() {
        return dedicatedSizeStorage;
    }

    public void setDedicatedSizeStorage(double dedicatedSizeStorage) {
        this.dedicatedSizeStorage = dedicatedSizeStorage;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }


}

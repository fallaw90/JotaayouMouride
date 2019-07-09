package com.fallntic.jotaayumouride;

import java.io.Serializable;
import java.util.List;

public class Dahira implements Serializable {

    private String dahiraID;
    private String dahiraName;
    private String dieuwrine;
    private String dahiraPhoneNumber;
    private String siege;
    private String totalAdiya;
    private String totalSass;
    private String totalSocial;
    private String totalMember;
    private List<String> listCommissions;
    private List<String> listResponsibles;


    public Dahira(String dahiraID, String dahiraName, String dieuwrine, String dahiraPhoneNumber,
                  String siege, String totalAdiya, String totalSass, String totalSocial,
                  List<String> listCommissions, List<String> listResponsibles) {

        this.dahiraID = dahiraID;
        this.dahiraName = dahiraName;
        this.dieuwrine = dieuwrine;
        this.dahiraPhoneNumber = dahiraPhoneNumber;
        this.siege = siege;
        this.totalAdiya = totalAdiya;
        this.totalSass = totalSass;
        this.totalSocial = totalSocial;
        this.listCommissions = listCommissions;
        this.listResponsibles = listResponsibles;
    }

    public Dahira() {}

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
}

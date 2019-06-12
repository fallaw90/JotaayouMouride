package com.fallntic.jotaayumouride;

import java.io.Serializable;

public class Dahira implements Serializable {

    private String dahiraID;
    private String dahiraName;
    private String dieuwrine;
    private String phoneNumber;
    private String siege;
    private String adiya;
    private String sass;
    private String social;
    private String totalMember;

    public Dahira(String dahiraName, String dieuwrine, String phoneNumber, String siege,
                  String sass, String adiya, String social) {

        this.dahiraID = "";
        this.dahiraName = dahiraName;
        this.dieuwrine = dieuwrine;
        this.phoneNumber = phoneNumber;
        this.siege = siege;
        this.sass = sass;
        this.adiya = adiya;
        this.social = social;
        this.totalMember = "";
    }

    public Dahira(){}

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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSiege() {
        return siege;
    }

    public void setSiege(String siege) {
        this.siege = siege;
    }

    public String getAdiya() {
        return adiya;
    }

    public void setAdiya(String adiya) {
        this.adiya = adiya;
    }

    public String getSass() {
        return sass;
    }

    public void setSass(String sass) {
        this.sass = sass;
    }

    public String getSocial() {
        return social;
    }

    public void setSocial(String social) {
        this.social = social;
    }

    public String getTotalMember() {
        return totalMember;
    }

    public void setTotalMember(String totalMember) {
        this.totalMember = totalMember;
    }

}

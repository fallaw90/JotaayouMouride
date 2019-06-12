package com.fallntic.jotaayumouride;

import java.io.Serializable;

public class User implements Serializable {

    private String userID;
    private String dahiraID;
    private String role;
    private String userName;
    private String email;
    private String phoneNumber;
    private String dahiraName;
    private String commission;
    private String adresse;
    private double sass;
    private double adiya;
    private double social;

    public User(String userID, String dahiraID, String dahiraName, String UserName, String phoneNumber,
                String email, String address, String commission, String role) {

        this.userID = userID;
        this.dahiraID = dahiraID;
        this.dahiraName = dahiraName;
        this.userName = UserName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.adresse = address;
        this.commission = commission;
        this.role = role;
    }

    public User(){}

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDahiraID() {
        return dahiraID;
    }

    public void setDahiraID(String dahiraID) {
        this.userID = dahiraID;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return userName;
    }

    public void setName(String name) {
        this.userName = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDahira() {
        return dahiraName;
    }

    public void setDahira(String dahira) {
        this.dahiraName = dahira;
    }

    public String getCommission() {
        return commission;
    }

    public void setCommission(String commission) {
        this.commission = commission;
    }

    public String getAddress() {
        return adresse;
    }

    public void setAddress(String address) {
        this.adresse = address;
    }

    public double getSass() {
        return sass;
    }

    public void setSass(double sass) {
        this.sass = sass;
    }

    public double getAdiya() {
        return adiya;
    }

    public void setAdiya(double adiya) {
        this.adiya = adiya;
    }

    public double getSocial() {
        return social;
    }

    public void setSocial(double social) {
        this.social = social;
    }
}

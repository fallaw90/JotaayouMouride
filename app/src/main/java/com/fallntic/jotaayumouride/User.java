package com.fallntic.jotaayumouride;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {

    private String userID;
    private String userName;
    private String userPhoneNumber;
    private String email;
    private String address;
    private List<String> listDahiraID = new ArrayList<String>();
    private List<String> listUpdatedDahiraID = new ArrayList<String>();
    private List<String> listCommissions = new ArrayList<String>();
    private List<String> listAdiya = new ArrayList<String>();
    private List<String> listSass = new ArrayList<String>();
    private List<String> listSocial = new ArrayList<String>();
    private List<String> listRoles = new ArrayList<String>();

    public User(String userID, String userName, String userPhoneNumber, String email, String address,
                List<String> listDahiraID, List<String> listUpdatedDahiraID, List<String> listCommissions,
                List<String> listAdiya, List<String> listSass, List<String> listSocial, List<String> listRoles) {
        this.userID = userID;
        this.userName = userName;
        this.userPhoneNumber = userPhoneNumber;
        this.email = email;
        this.address = address;
        this.listDahiraID = listDahiraID;
        this.listUpdatedDahiraID = listUpdatedDahiraID;
        this.listCommissions = listCommissions;
        this.listAdiya = listAdiya;
        this.listSass = listSass;
        this.listSocial = listSocial;
        this.listRoles = listRoles;
    }

    public User(){}

    public User(String userID, String userName, String userPhoneNumber,
                String email, String address) {
        this.userID = userID;
        this.userName = userName;
        this.userPhoneNumber = userPhoneNumber;
        this.email = email;
        this.address = address;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<String> getListDahiraID() {
        return listDahiraID;
    }

    public void setListDahiraID(List<String> listDahiraID) {
        this.listDahiraID = listDahiraID;
    }

    public List<String> getListCommissions() {
        return listCommissions;
    }

    public void setListCommissions(List<String> listCommissions) {
        this.listCommissions = listCommissions;
    }

    public List<String> getListAdiya() {
        return listAdiya;
    }

    public void setListAdiya(List<String> listAdiya) {
        this.listAdiya = listAdiya;
    }

    public List<String> getListSass() {
        return listSass;
    }

    public void setListSass(List<String> listSass) {
        this.listSass = listSass;
    }

    public List<String> getListSocial() {
        return listSocial;
    }

    public void setListSocial(List<String> listSocial) {
        this.listSocial = listSocial;
    }

    public List<String> getListRoles() {
        return listRoles;
    }

    public void setListRoles(List<String> listRoles) {
        this.listRoles = listRoles;
    }

    public List<String> getListUpdatedDahiraID() {
        return listUpdatedDahiraID;
    }

    public void setListUpdatedDahiraID(List<String> listUpdatedDahiraID) {
        this.listUpdatedDahiraID = listUpdatedDahiraID;
    }
}
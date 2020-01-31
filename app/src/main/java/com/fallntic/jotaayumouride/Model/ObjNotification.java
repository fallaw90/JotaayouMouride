package com.fallntic.jotaayumouride.model;

import java.io.Serializable;

@SuppressWarnings("unused")
public class ObjNotification implements Serializable {

    private String notificationID;
    private String userID;
    private String DahiraID;
    private String title;
    private String message;

    public ObjNotification(String notificationID, String userID, String dahiraID, String title, String message) {
        this.notificationID = notificationID;
        this.userID = userID;
        this.DahiraID = dahiraID;
        this.title = title;
        this.message = message;
    }

    public ObjNotification() {
    }

    public String getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(String notificationID) {
        this.notificationID = notificationID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDahiraID() {
        return DahiraID;
    }

    public void setDahiraID(String dahiraID) {
        DahiraID = dahiraID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

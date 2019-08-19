package com.fallntic.jotaayumouride.Model;

import java.io.Serializable;

public class ProfileImage implements Serializable {

    private String userID;
    private String uri;

    public ProfileImage(String userID, String uri) {
        this.userID = userID;
        this.uri = uri;
    }

    public ProfileImage() {
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}

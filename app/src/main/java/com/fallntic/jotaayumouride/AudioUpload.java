package com.fallntic.jotaayumouride;

import java.io.Serializable;

public class AudioUpload implements Serializable {
    private String fileName;
    private String uri;
    private String dahiraID;
    private String userName;
    private String dateUploaded;

    public AudioUpload(String fileName, String uri, String dahiraID, String userName) {
        this.fileName = fileName;
        this.uri = uri;
        this.dahiraID = dahiraID;
        this.userName = userName;
        this.dateUploaded = DataHolder.getCurrentDate();
    }

    public AudioUpload() {}

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getDahiraID() {
        return dahiraID;
    }

    public void setDahiraID(String dahiraID) {
        this.dahiraID = dahiraID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDateUploaded() {
        return dateUploaded;
    }
}

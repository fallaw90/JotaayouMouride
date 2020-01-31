package com.fallntic.jotaayumouride.model;

import java.io.Serializable;

@SuppressWarnings("unused")
public class UploadPdf implements Comparable<UploadPdf>, Serializable {

    private String name;
    private String url;

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public UploadPdf() {
    }

    public UploadPdf(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public int compareTo(UploadPdf uploadPdf) {
        return this.name.compareToIgnoreCase(uploadPdf.getName());
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

package com.fallntic.jotaayumouride.Model;

import java.io.Serializable;

public class UploadPdf implements Comparable<UploadPdf>, Serializable {

    public String name;
    public String url;

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
}

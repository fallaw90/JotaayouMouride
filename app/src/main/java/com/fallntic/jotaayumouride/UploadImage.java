package com.fallntic.jotaayumouride;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

@IgnoreExtraProperties
public class UploadImage {

    public String dahiraID;
    public List<String> listNameImages;

    public UploadImage() { }

    public UploadImage(String dahiraID, List<String> listNameImages) {
        this.dahiraID = dahiraID;
        this.listNameImages = listNameImages;
    }

    public String getDahiraID() {
        return dahiraID;
    }

    public void setDahiraID(String dahiraID) {
        this.dahiraID = dahiraID;
    }

    public List<String> getListNameImages() {
        return listNameImages;
    }

    public void setListNameImages(List<String> listNameImages) {
        this.listNameImages = listNameImages;
    }
}
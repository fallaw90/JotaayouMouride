package com.fallntic.jotaayumouride.Model;

import java.io.Serializable;
import java.util.List;

public class ListImageObject implements Serializable {

    private String dahiraID;
    private List<Image> listImage;

    public ListImageObject(String dahiraID, List<Image> listImage) {
        this.dahiraID = dahiraID;
        this.listImage = listImage;
    }

    public ListImageObject() {}

    public String getDahiraID() {
        return dahiraID;
    }

    public void setDahiraID(String dahiraID) {
        this.dahiraID = dahiraID;
    }

    public List<Image> getListImage() {
        return listImage;
    }

    public void setListImage(List<Image> listImage) {
        this.listImage = listImage;
    }
}

package com.fallntic.jotaayumouride.Model;

public class Image {

    private String imageID;
    private String dahiraID;
    private String uri;
    private String imageName;

    public Image(String imageID, String dahiraID, String uri, String imageName) {
        this.imageID = imageID;
        this.dahiraID = dahiraID;
        this.uri = uri;
        this.imageName = imageName;
    }

    public Image() {}

    public String getImageID() {
        return imageID;
    }

    public void setImageID(String imageID) {
        this.imageID = imageID;
    }

    public String getDahiraID() {
        return dahiraID;
    }

    public void setDahiraID(String dahiraID) {
        this.dahiraID = dahiraID;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}

package com.fallntic.jotaayumouride.Model;

public class PubImage {

    private String id_ad;
    private String date_saved;
    private String date_end;
    private String title;
    private String description;
    private String image_uri;

    public PubImage() {
    }

    public String getId_ad() {
        return id_ad;
    }

    public void setId_ad(String id_ad) {
        this.id_ad = id_ad;
    }

    public String getDate_saved() {
        return date_saved;
    }

    public void setDate_saved(String date_saved) {
        this.date_saved = date_saved;
    }

    public String getDate_end() {
        return date_end;
    }

    public void setDate_end(String date_end) {
        this.date_end = date_end;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage_uri() {
        return image_uri;
    }

    public void setImage_uri(String image_uri) {
        this.image_uri = image_uri;
    }
}

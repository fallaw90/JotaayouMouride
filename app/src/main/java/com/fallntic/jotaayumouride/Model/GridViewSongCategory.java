package com.fallntic.jotaayumouride.model;

public class GridViewSongCategory {

    private int imageId;
    private String nameAudio;

    public GridViewSongCategory(int imageId, String nameAudio) {
        this.imageId = imageId;
        this.nameAudio = nameAudio;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getNameAudio() {
        return nameAudio;
    }

    public void setNameAudio(String nameAudio) {
        this.nameAudio = nameAudio;
    }
}

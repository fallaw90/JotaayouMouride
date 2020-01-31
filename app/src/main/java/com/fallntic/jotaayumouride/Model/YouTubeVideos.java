package com.fallntic.jotaayumouride.model;

@SuppressWarnings("unused")
public class YouTubeVideos {
    private String videoUrl;

    public YouTubeVideos(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
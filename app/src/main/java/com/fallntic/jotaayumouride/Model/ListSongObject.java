package com.fallntic.jotaayumouride.model;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unused")
public class ListSongObject implements Serializable {
    private String documentID;
    private List<Song> listSong;

    public ListSongObject(String documentID, List<Song> listSong) {
        this.documentID = documentID;
        this.listSong = listSong;
    }

    public ListSongObject(){}

    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public List<Song> getListSong() {
        return listSong;
    }

    public void setListSong(List<Song> listSong) {
        this.listSong = listSong;
    }


}

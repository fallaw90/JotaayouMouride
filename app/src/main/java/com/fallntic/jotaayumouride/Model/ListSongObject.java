package com.fallntic.jotaayumouride.Model;

import java.io.Serializable;
import java.util.List;

public class ListSongObject implements Serializable {
    String documentID;
    List<Song> listSong;

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

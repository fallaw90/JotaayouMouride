package com.fallntic.jotaayumouride.Utility;

import com.fallntic.jotaayumouride.Dahira;
import com.fallntic.jotaayumouride.Model.Song;
import com.fallntic.jotaayumouride.ObjNotification;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class MyStaticVariables {

    public static final String TITLE_ANNOUNCEMENT_NOTIFICATION = "Nouvelle Annoncement";
    public static final String TITLE_EXPENSE_NOTIFICATION = "Nouvelle Dépense";
    public static String displayDahira = "";

    //Contains all songs that belong to one dahira
    public static List<Song> listSong;

    //Contains all user dahira
    public static List<Dahira> myListDahira;

    //Contains all existing dahira
    public static List<Dahira> allListDahira;


    public static ObjNotification objNotification;


    //*************************** Firebase ****************************
    public static FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    public static FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    public static StorageReference storageReference;
    public static CollectionReference collectionReference;



}

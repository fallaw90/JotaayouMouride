package com.fallntic.jotaayumouride.Utility;

import android.content.Context;

import androidx.annotation.NonNull;

import com.fallntic.jotaayumouride.Model.Song;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;

public class MyStaticFunctions {

    /**
     * Get all songs in repertory audio from firestore and save the songs in
     * the static variable listSong in MyStaticVariables class.
     *
     * @param context
     */
    public static void getSongList(final Context context) {
        //Retrieve all songs from FirebaseFirestore
        if (MyStaticVariables.listSong == null) {
            MyStaticVariables.listSong = new ArrayList<>();

            MyStaticVariables.collectionReference = MyStaticVariables.firestore.collection("dahiras")
                    .document(dahira.getDahiraID()).collection("audios");
            MyStaticVariables.collectionReference.get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                            if (!queryDocumentSnapshots.isEmpty()) {
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot documentSnapshot : list) {
                                    Song song = documentSnapshot.toObject(Song.class);
                                    if (song.getAudioUri() != null)
                                        MyStaticVariables.listSong.add(song);
                                    else {
                                        String uploadID = documentSnapshot.getId();
                                        MyStaticVariables.collectionReference.document(uploadID).delete();
                                    }
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    toastMessage(context, "Erreur de telechargement du repertoire audio.");
                }
            });
        }
    }
}

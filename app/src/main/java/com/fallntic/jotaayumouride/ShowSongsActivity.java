package com.fallntic.jotaayumouride;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Model.UploadSong;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;

public class ShowSongsActivity extends AppCompatActivity {

    ProgressBar progressBar;
    List<UploadSong> listUploadSong;
    FirebaseStorage firebaseStorage;
    DatabaseReference databaseReference;
    ValueEventListener valueEventListener;
    MediaPlayer mediaPlayer;
    SongsAdapter songsAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_songs);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listUploadSong = new ArrayList<>();

        songsAdapter = new SongsAdapter(this, listUploadSong);
        recyclerView.setAdapter(songsAdapter);


        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("gallery")
                .child("audio")
                .child(dahira.getDahiraID());

        valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listUploadSong.clear();
                for (DataSnapshot dss : dataSnapshot.getChildren()){
                    UploadSong uploadSong = dss.getValue(UploadSong.class);
                    uploadSong.setmKey(dss.getKey());
                    listUploadSong.add(uploadSong);
                }

                songsAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                toastMessage(ShowSongsActivity.this, "" + databaseError.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(valueEventListener);
    }

    public void playSong(List<UploadSong> arrayListSongs, int adapterPosition) throws IOException {
        UploadSong uploadSong = arrayListSongs.get(adapterPosition);

        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(uploadSong.getSongLink);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {

                mediaPlayer.start();
            }
        });

        mediaPlayer.prepareAsync();
    }
}

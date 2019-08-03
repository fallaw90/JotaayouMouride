package com.fallntic.jotaayumouride;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Model.Audio;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.dahira;

public class RecordingListActivity extends AppCompatActivity {
    private final String TAG = "RecordingListActivity";

    private Toolbar toolbar;
    private RecyclerView recyclerViewRecordings;
    private ArrayList<Audio> recordingArraylist;
    private AnnouncementAudioAdapter announcementAudioAdapter;
    private TextView textViewNoRecordings;
    private FirebaseFirestore firestore;

    private CollectionReference collectionReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_list);

        firestore = FirebaseFirestore.getInstance();
        collectionReference = firestore.collection("announcements")
                .document(dahira.getDahiraID()).collection("audios");

        initViews();

        getListSong();
    }

    @SuppressLint("WrongConstant")
    private void initViews() {
        recordingArraylist = new ArrayList<Audio>();
        /** setting up the toolbar  **/
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Jotaayou Mouride");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setSupportActionBar(toolbar);

        /** enabling back button ***/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /** setting up recyclerView **/
        recyclerViewRecordings = findViewById(R.id.recyclerViewRecordings);
        recyclerViewRecordings.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerViewRecordings.setHasFixedSize(true);

        textViewNoRecordings = findViewById(R.id.textViewNoRecordings);

    }

    public void getListSong() {

        collectionReference.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        //progressBar_mainLoader.setVisibility(View.GONE);
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : list) {
                                Audio audio = documentSnapshot.toObject(Audio.class);
                                if (audio.getAudioID() != null)
                                    recordingArraylist.add(audio);
                                else {
                                    String uploadID = documentSnapshot.getId();
                                    collectionReference.document(uploadID).delete();
                                }
                            }
                            textViewNoRecordings.setVisibility(View.GONE);
                            recyclerViewRecordings.setVisibility(View.VISIBLE);
                            setAdapterToRecyclerView();

                        } else {
                            textViewNoRecordings.setVisibility(View.VISIBLE);
                            recyclerViewRecordings.setVisibility(View.GONE);
                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //progressBar_mainLoader.setVisibility(View.GONE);
            }
        });
    }


    private void setAdapterToRecyclerView() {
        announcementAudioAdapter = new AnnouncementAudioAdapter(this, recordingArraylist);
        recyclerViewRecordings.setAdapter(announcementAudioAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }

    }

}
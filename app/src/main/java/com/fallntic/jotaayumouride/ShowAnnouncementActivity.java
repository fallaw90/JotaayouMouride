package com.fallntic.jotaayumouride;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Model.Announcement;
import com.fallntic.jotaayumouride.Model.Song;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;

public class ShowAnnouncementActivity extends AppCompatActivity {
    private final String TAG = "ListAnnouncementActivity";

    private TextView textViewDahiraName, textViewDelete;
    private boolean isAnnouncementExist = false;
    private RecyclerView recyclerViewAnnoucement;
    private Toolbar toolbar;
    private AnnouncementAdapter announcementAdapter;
    private List<Object> listAnnouncement;
    private FirebaseFirestore db;
    private FirebaseStorage firebaseStorage;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_announcement);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initialization();

        if (!isConnected(this)) {
            finish();
            Intent intent = new Intent(this, LoginActivity.class);
            showAlertDialog(this, "Oops! Pas de connexion, " +
                    "verifier votre connexion internet puis reesayez SVP", intent);
        }


        textViewDahiraName.setText("Liste des annonces du dahira " + dahira.getDahiraName());


        showListAnnouncements();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(ShowAnnouncementActivity.this, DahiraInfoActivity.class));
            }
        });

        enableSwipeToDelete();

    }

    public void initialization() {

        //**********Toolbar*******************************************
        toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Mes annonces");
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        //*************************************************************

        recyclerViewAnnoucement = findViewById(R.id.recyclerview_announcement);
        textViewDahiraName = findViewById(R.id.textView_dahiraName);
        textViewDelete = findViewById(R.id.textView_delete);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        firebaseStorage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    private void showListAnnouncements() {

        //Attach adapter to recyclerView

        //Attach adapter to recyclerView
        recyclerViewAnnoucement.setHasFixedSize(true);
        recyclerViewAnnoucement.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAnnoucement.setVisibility(View.VISIBLE);
        listAnnouncement = new ArrayList<>();
        announcementAdapter = new AnnouncementAdapter(ShowAnnouncementActivity.this, listAnnouncement);
        recyclerViewAnnoucement.setAdapter(announcementAdapter);

        db.collection("announcements").document(dahira.getDahiraID())
                .collection("text").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : list) {
                                Announcement announcement = documentSnapshot.toObject(Announcement.class);
                                listAnnouncement.add(announcement);
                            }
                            isAnnouncementExist = true;
                        }
                        getListAudio();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    public void getListAudio() {
        db.collection("announcements").document(dahira.getDahiraID())
                .collection("audios").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : list) {
                                Song song = documentSnapshot.toObject(Song.class);
                                listAnnouncement.add(song);
                            }
                            isAnnouncementExist = true;
                        }
                        if (!isAnnouncementExist) {
                            textViewDahiraName.setText("La liste des annonces du dahira " +
                                    dahira.getDahiraName() + " est vide. Cliquez sue l'icone " +
                                    "(+) pour envoyer une nouvelle annonce.");
                            textViewDelete.setVisibility(View.GONE);
                            return;
                        } else {
                            announcementAdapter.notifyDataSetChanged();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                toastMessage(ShowAnnouncementActivity.this, "Error : " + e.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_menu, menu);

        MenuItem iconAdd;
        iconAdd = menu.findItem(R.id.icon_add);
        iconAdd.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.icon_add:
                DahiraInfoActivity.chooseMethodAnnouncement(ShowAnnouncementActivity.this);
                break;
        }
        return true;
    }

    private void enableSwipeToDelete() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                final int position = viewHolder.getAdapterPosition();

                final Object object = listAnnouncement.get(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(ShowAnnouncementActivity.this, R.style.alertDialog);
                builder.setTitle("Supprimer annonce!");
                builder.setMessage("Etes vous sure de vouloir supprimer cette annonce?");
                builder.setCancelable(false);
                builder.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        announcementAdapter.removeItem(position);
                        //Remove item in FirebaseFireStore

                        if (object instanceof Announcement) {
                            Announcement announcement = (Announcement) object;
                            removeTextAnnouncement(announcement);
                        } else if (object instanceof Song) {
                            Song song = (Song) object;
                            removeAudioAnnouncement(song);
                        }
                    }
                });

                builder.setNegativeButton("NON", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(ShowAnnouncementActivity.this,
                                ShowAnnouncementActivity.class));
                    }
                });
                builder.show();
            }

        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerViewAnnoucement);
    }

    public void removeTextAnnouncement(Announcement announcement) {
        db.collection("announcements").document(dahira.getDahiraID())
                .collection("text").document(announcement.getAnnouncementID())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Snackbar snackbar = Snackbar.make(coordinatorLayout,
                                "Annonce supprimee.", Snackbar.LENGTH_LONG);
                        snackbar.setActionTextColor(Color.GREEN);
                        snackbar.show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar snackbar = Snackbar.make(coordinatorLayout,
                        "Erreur de la suppression de l'annonce. " + e.getMessage(), Snackbar.LENGTH_LONG);
                snackbar.setActionTextColor(Color.GREEN);
                snackbar.show();
                startActivity(new Intent(ShowAnnouncementActivity.this, ShowAnnouncementActivity.class));
            }
        });
    }

    public void removeAudioAnnouncement(final Song song) {
        db.collection("announcements").document(dahira.getDahiraID())
                .collection("audios").document(song.getAudioID())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        removeInFirebaseStorage(song);

                        Snackbar snackbar = Snackbar.make(coordinatorLayout,
                                "Annonce supprimee.", Snackbar.LENGTH_LONG);
                        snackbar.setActionTextColor(Color.GREEN);
                        snackbar.show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar snackbar = Snackbar.make(coordinatorLayout,
                        "Erreur de la suppression de l'annonce. " + e.getMessage(), Snackbar.LENGTH_LONG);
                snackbar.setActionTextColor(Color.GREEN);
                snackbar.show();
                startActivity(new Intent(ShowAnnouncementActivity.this, ShowAnnouncementActivity.class));
            }
        });
    }

    public void removeInFirebaseStorage(Song song) {
        if (song.audioUri != null) {

            StorageReference storageRef = firebaseStorage
                    .getReferenceFromUrl(song.audioUri);

            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @SuppressLint("LongLogTag")
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    Log.d(TAG, "onSuccess: deleted file");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @SuppressLint("LongLogTag")
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Log.d(TAG, "onFailure: did not delete file");
                }
            });
        }
    }
}


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

import com.fallntic.jotaayumouride.adapter.AnnouncementAdapter;
import com.fallntic.jotaayumouride.model.Announcement;
import com.fallntic.jotaayumouride.model.Song;
import com.fallntic.jotaayumouride.utility.SwipeToDeleteCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.dismissProgressDialog;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.dahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.indexOnlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.objNotification;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.onlineUser;

public class ShowAnnouncementActivity extends AppCompatActivity {
    private final String TAG = "ListAnnouncementActivity";

    private TextView textViewDahiraName, textViewDelete;
    private boolean isAnnouncementExist = false;
    private RecyclerView recyclerViewAnnoucement;
    private AnnouncementAdapter announcementAdapter;
    private List<Object> listAnnouncement;
    private FirebaseFirestore db;
    private FirebaseStorage firebaseStorage;
    private CoordinatorLayout coordinatorLayout;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_announcement);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //**************************Toolbar*********************
        Toolbar toolbar = findViewById(R.id.toolbar);
        //toolbar.setLogo(R.mipmap.logo);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.logo);

        initialization();

        checkInternetConnection(this);


        textViewDahiraName.setText("Liste des annonces du dahira " + dahira.getDahiraName());


        showListAnnouncements();


        if (onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur")) {
            enableSwipeToDelete();
            textViewDelete.setVisibility(View.VISIBLE);
        }

        HomeActivity.loadBannerAd(this);
    }

    private void initialization() {

        recyclerViewAnnoucement = findViewById(R.id.recyclerview_announcement);
        textViewDahiraName = findViewById(R.id.textView_dahiraName);
        textViewDelete = findViewById(R.id.textView_delete);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        firebaseStorage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (objNotification != null) {
            objNotification = null;
            startActivity(new Intent(ShowAnnouncementActivity.this, HomeActivity.class));
            finish();
        } else {
            startActivity(new Intent(ShowAnnouncementActivity.this, DahiraInfoActivity.class));
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.destroy();
        }
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.resume();
        }
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
                            sortAnnouncementByDate();
                        }
                        getListAudio();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    private void getListAudio() {
        db.collection("announcements").document(dahira.getDahiraID())
                .collection("audios").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : list) {
                                Song song = documentSnapshot.toObject(Song.class);
                                listAnnouncement.add(song);
                            }
                            isAnnouncementExist = true;
                            sortAnnouncementByDate();
                        }
                        if (!isAnnouncementExist) {
                            textViewDahiraName.setText("La liste des annonces du dahira " +
                                    dahira.getDahiraName() + " est vide. Cliquez sue l'icone " +
                                    "(+) pour envoyer une nouvelle annonce.");
                            textViewDelete.setVisibility(View.GONE);
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
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem iconAdd;
        iconAdd = menu.findItem(R.id.icon_add);
        iconAdd.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                break;

            case R.id.icon_add:
                DahiraInfoActivity.chooseMethodAnnouncement(ShowAnnouncementActivity.this);
                break;

            case R.id.instructions:
                startActivity(new Intent(this, InstructionsActivity.class));
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

    private void removeTextAnnouncement(Announcement announcement) {
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

    private void removeAudioAnnouncement(final Song song) {
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

    private void removeInFirebaseStorage(Song song) {
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

    private void sortAnnouncementByDate() {
        Collections.sort(listAnnouncement, new Comparator<Object>() {
            @SuppressLint("SimpleDateFormat")
            final DateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            @SuppressLint("Assert")
            @Override
            public int compare(Object obj1, Object obj2) {
                try {
                    Announcement a1 = null, a2 = null;
                    Song s1 = null, s2 = null;
                    if (obj1 instanceof Announcement)
                        a1 = (Announcement) obj1;
                    else
                        s1 = (Song) obj1;

                    if (obj2 instanceof Announcement)
                        a2 = (Announcement) obj2;
                    else
                        s2 = (Song) obj2;

                    if (a1 != null && a2 != null)
                        return Objects.requireNonNull(f.parse(a2.getDate())).compareTo(f.parse(a1.getDate()));

                    if (a1 != null && s2 != null) {
                        assert false;
                        return f.parse(a2.getDate()).compareTo(f.parse(s1.getDate()));
                    }


                    if (a2 != null && s1 != null)
                        return Objects.requireNonNull(f.parse(a2.getDate())).compareTo(f.parse(s1.getDate()));

                    if (s1 != null && s2 != null)
                        return Objects.requireNonNull(f.parse(s2.getDate())).compareTo(f.parse(s1.getDate()));

                    return 0;

                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
    }
}

package com.fallntic.jotaayumouride;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.model.Image;
import com.fallntic.jotaayumouride.model.Song;
import com.fallntic.jotaayumouride.services.OnClearFromRecentService;
import com.fallntic.jotaayumouride.utility.MyStaticVariables;
import com.fallntic.jotaayumouride.utility.SwipeToDeleteCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.createChannel;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.setMyAdapter;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showAlertDialog;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.stopCurrentPlayingMediaPlayer;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.updateStorageSize;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.broadcastReceiverMediaPlayer;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.dahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.fab_search;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.indexOnlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.iv_next;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.iv_play;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.iv_previous;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listImage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listSong;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listTracks;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.mAdapter;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.notificationManagerMediaPlayer;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.pb_loader;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.pb_main_loader;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.recycler;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.seekBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.tb_title;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.toolbar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.toolbar_bottom;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.tv_empty;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.tv_time;


@SuppressWarnings("ALL")
public class ShowSongsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ShowSongsActivity";

    private CoordinatorLayout coordinatorLayout;
    private TextView textViewTitle, textViewDeleteInstruction;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_songs);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.logo);

        //Initialisation des vues
        initializeViews();

        // Check internet connection
        checkInternetConnection(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(this);
            registerReceiver(broadcastReceiverMediaPlayer, new IntentFilter("TRACKS_TRACKS"));
            startService(new Intent(this, OnClearFromRecentService.class));
        }

        //******************Check if user is admin and update storage size
        if (listSong == null || listSong.size() <= 0) {
            if (userIsAdmin()) {
                tv_empty.setText("Votre repertoire audio est vide. Cliquez sur l'icone (+) pour " +
                        "enregistrer ou ajouter un audio dans votre repertoire.");
            } else {
                tv_empty.setText("Le repertoire audio du dahira " + dahira.getDahiraName() + " est vide.");
            }

            fab_search.setVisibility(View.GONE);
            toolbar_bottom.setVisibility(View.GONE);
        } else {
            //********************************** Stop Media Player if playing ****************************
            stopCurrentPlayingMediaPlayer();

            //***********************Populate listTracks for Notification Media**************************
            listTracks = new ArrayList<>();
            listTracks.addAll(listSong);
            if (userIsAdmin()) {
                enableSwipeToDelete();
                if (dahira.getDedicatedSizeStorage() <= 0)
                    updateStorageSize(dahira.getCurrentSizeStorage(), 200);
                else {
                    getSizeStorage();
                }
            } else {
                textViewDeleteInstruction.setVisibility(View.GONE);
            }

            //****************************************Display al songs**************************************
            textViewTitle.setText("Bienvenu dans le repertoire audio du dahira " + dahira.getDahiraName());
            tv_empty.setVisibility(View.GONE);

            setMyAdapter(this, listSong);
        }

        HomeActivity.loadBannerAd(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_back) {
            updateStorageSize(dahira.getCurrentSizeStorage());
            startActivity(new Intent(ShowSongsActivity.this, DahiraInfoActivity.class));
        }
    }

    @Override
    public void onBackPressed() {
        updateStorageSize(dahira.getCurrentSizeStorage());
        startActivity(new Intent(ShowSongsActivity.this, DahiraInfoActivity.class));
    }

    private boolean userIsAdmin() {
        return onlineUser != null && indexOnlineUser >= 0 && onlineUser.getListDahiraID().contains(dahira.getDahiraID()) &&
                onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur");
    }

    private void initializeViews() {
        toolbar_bottom = findViewById(R.id.bottom_toolbar);
        tb_title = findViewById(R.id.tb_title);
        tv_empty = findViewById(R.id.tv_empty);
        iv_play = findViewById(R.id.iv_play);
        iv_next = findViewById(R.id.iv_next);
        iv_previous = findViewById(R.id.iv_previous);
        pb_loader = findViewById(R.id.pb_loader);
        pb_main_loader = findViewById(R.id.pb_main_loader);
        recycler = findViewById(R.id.recyclerView_song);
        seekBar = findViewById(R.id.seekbar);
        tv_time = findViewById(R.id.tv_time);
        fab_search = findViewById(R.id.fab_search);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        textViewTitle = findViewById(R.id.tv_title);
        textViewDeleteInstruction = findViewById(R.id.tv_deleteInstruction);
        findViewById(R.id.button_back).setOnClickListener(this);
    }

    private void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ShowSongsActivity.this);
        final View view = getLayoutInflater().inflate(R.layout.dialog_search_song, null);
        builder.setTitle(R.string.rechercher);
        builder.setView(view);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText et_search = view.findViewById(R.id.et_search);
                String search = et_search.getText().toString().trim();
                List<Song> listSongFound = new ArrayList<>();
                if (search.length() > 0) {
                    for (Song song : listSong) {
                        if (song.getAudioTitle().contains(search)) {
                            listSongFound.add(song);
                        }
                    }
                    if (listSongFound.size() > 0) {
                        setMyAdapter(ShowSongsActivity.this, listSongFound);
                    } else {
                        Toast.makeText(ShowSongsActivity.this, "Song non trouve", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ShowSongsActivity.this, "Veuillez remplir le champ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.create().show();
    }

    private void enableSwipeToDelete() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int position = viewHolder.getAdapterPosition();
                final Song song = listSong.get(position);
                MyStaticVariables.collectionReference = MyStaticVariables.firestore.collection("dahiras")
                        .document(dahira.getDahiraID()).collection("audios");
                AlertDialog.Builder builder = new AlertDialog.Builder(ShowSongsActivity.this, R.style.alertDialog);
                builder.setTitle("Supprimer audio!");
                builder.setMessage("Etes vous sure de vouloir supprimer ce fichier?");
                builder.setCancelable(false);
                builder.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Update totalAdiya dahira
                        mAdapter.removeItem(position);
                        if (song.getAudioID() != null) {
                            //Remove item in FirebaseFireStore
                            MyStaticVariables.collectionReference.document(song.getAudioID()).delete();
                        }
                        //Remove item in FirebaseStorage
                        removeInFirebaseStorage(song);
                        Snackbar snackbar = Snackbar.make(coordinatorLayout, "Fichier audio supprime.", Snackbar.LENGTH_LONG);
                        snackbar.setActionTextColor(Color.GREEN);
                        snackbar.show();
                    }
                });
                builder.setNegativeButton("NON", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(ShowSongsActivity.this, ShowSongsActivity.class));
                    }
                });
                builder.show();
            }
        };
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recycler);
    }

    private void removeInFirebaseStorage(Song song) {
        if (song.audioUri != null) {
            StorageReference storageRef = MyStaticVariables.firebaseStorage
                    .getReferenceFromUrl(song.audioUri);
            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    startActivity(new Intent(ShowSongsActivity.this, ShowSongsActivity.class));
                    Log.d(TAG, "onSuccess: deleted file");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Log.d(TAG, "onFailure: did not delete file");
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem iconAdd;
        iconAdd = menu.findItem(R.id.icon_add);
        if (onlineUser.getListDahiraID().contains(dahira.getDahiraID()) && indexOnlineUser >= 0) {
            if (onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur"))
                iconAdd.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(this, HomeActivity.class));
                break;

            case R.id.icon_add:
                if (dahira.getCurrentSizeStorage() < dahira.getDedicatedSizeStorage())
                    startActivity(new Intent(this, AddAudioActivity.class));
                else
                    showAlertDialog(this, "Memoire insuffisante! Veuillez contacter +1 (320) 803-0902 via WhatSapp si vous avez besoin plus de memoire.");
                break;

            case R.id.instructions:
                startActivity(new Intent(this, InstructionsActivity.class));
                break;

            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
        return true;
    }

    private void getSizeStorage() {
        FirebaseStorage storage = FirebaseStorage.getInstance(); // 1
        StorageReference storageRef = storage.getReference();
        StorageReference reference;

        dahira.setCurrentSizeStorage(0);
        for (final Song song : listSong) {
            reference = storageRef.child("gallery").child("audios").child(dahira.getDahiraID()).child(song.getAudioID());
            reference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    dahira.setCurrentSizeStorage(dahira.getCurrentSizeStorage() + storageMetadata.getSizeBytes() / 1048576);
                    if (dahira.getCurrentSizeStorage() < dahira.getDedicatedSizeStorage())
                        textViewTitle.setText("Bienvenu dans le repertoire audio du dahira " + dahira.getDahiraName() + "\nMemoire disponible " + (dahira.getDedicatedSizeStorage() - dahira.getCurrentSizeStorage()) + " Mo.");
                    else
                        textViewTitle.setText("Memoire insuffisante! Veuillez contacter +1 (320) 803-0902 via WhatSapp pour reserver plus de memoire.\"");
                    Log.i("Size = ", String.valueOf(storageMetadata.getSizeBytes()));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {

                }
            });
        }

        for (final Image image : listImage) {
            reference = storageRef.child("gallery").child("picture").child(dahira.getDahiraID()).child(image.getImageName());
            reference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    dahira.setCurrentSizeStorage(dahira.getCurrentSizeStorage() + storageMetadata.getSizeBytes() / 1048576);
                    if (dahira.getCurrentSizeStorage() < dahira.getDedicatedSizeStorage())
                        textViewTitle.setText("Bienvenu dans le repertoire audio du dahira " + dahira.getDahiraName() + "\nMemoire disponible " + (dahira.getDedicatedSizeStorage() - dahira.getCurrentSizeStorage()) + " Mo.");
                    else
                        textViewTitle.setText("Memoire insuffisante! Veuillez contacter +1 (320) 803-0902 via WhatSapp pour reserver plus de memoire.\"");
                    Log.i("Size = ", String.valueOf(storageMetadata.getSizeBytes()));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {

                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //**********Notification Music********
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManagerMediaPlayer.cancelAll();
        }
        try {
            unregisterReceiver(broadcastReceiverMediaPlayer);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
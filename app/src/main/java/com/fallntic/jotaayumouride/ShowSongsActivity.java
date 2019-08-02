package com.fallntic.jotaayumouride;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Model.Audio;
import com.fallntic.jotaayumouride.Utility.Utility;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.indexOnlineUser;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;

public class ShowSongsActivity extends AppCompatActivity {
    private final String TAG = "ShowSongsActivity";
    StorageTask uploadTask;
    private List<Audio> listAudio;
    private MediaPlayer mediaPlayer;
    private RecyclerView recyclerView;
    private SongsAdapter songsAdapter;
    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private CollectionReference collectionReference;
    private int currentIndex;
    private TextView textView_titleToolbar, textView_durationToolbar;
    private ImageView imageViewPlay, imageViewNext, imageViewPrevious;
    private ProgressBar progressBar_loader, progressBar_mainLoader;
    private SeekBar seekBar;
    private boolean firstLaunch = true;
    private long currentSongLength;
    private Toolbar toolbarUp, toolbarBottom;
    private CoordinatorLayout coordinatorLayout;
    private RelativeLayout relativeLayout_song;
    private RelativeLayout relativeLayout_progressBar;
    private TextView textView_titleLayoutSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_songs);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toolbarUp = findViewById(R.id.toolbar);
        toolbarUp.setTitle("Jotaayou Mouride");
        toolbarUp.setSubtitle("Repertoire Audio");
        setSupportActionBar(toolbarUp);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Initialisation des vues
        initialization();

        textView_titleLayoutSong.setText("Repertoire audio du dahira " + dahira.getDahiraName());

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Retrieve all songs from FirebaseFirestore
        firebaseStorage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();
        collectionReference = firestore.collection("dahiras")
                .document(dahira.getDahiraID()).collection("audios");

        getListSong();

        songsAdapter = new SongsAdapter(this, listAudio, new SongsAdapter.RecyclerItemClickListener() {
            @Override
            public void onClickListener(Audio audi, int position) {
                firstLaunch = false;
                changeSelectedSong(position);
                prepareSong(audi);
            }
        });

        recyclerView.setAdapter(songsAdapter);

        //Initialisation du media player
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //Lancer la chanson
                togglePlay(mp);
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (currentIndex + 1 < listAudio.size()) {
                    Audio next = listAudio.get(currentIndex + 1);
                    changeSelectedSong(currentIndex + 1);
                    prepareSong(next);
                } else {
                    Audio next = listAudio.get(0);
                    changeSelectedSong(0);
                    prepareSong(next);
                }
            }
        });

        //Gestion de la seekbar
        handleSeekbar();

        //Controle de la chanson
        pushPlay();
        pushPrevious();
        pushNext();

        toolbarUp.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dahira != null)
                    startActivity(new Intent(ShowSongsActivity.this, DahiraInfoActivity.class));
                else
                    startActivity(new Intent(ShowSongsActivity.this, MainActivity.class));
            }
        });

        enableSwipeToDelete();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        if (dahira != null)
            startActivity(new Intent(ShowSongsActivity.this, DahiraInfoActivity.class));
        else
            startActivity(new Intent(ShowSongsActivity.this, MainActivity.class));
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_menu, menu);

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
            case R.id.icon_add:
                startActivity(new Intent(this, GalleryAudioActivity.class));
                break;
        }
        return true;
    }

    public void getListSong() {
        //progressBar_mainLoader.setVisibility(View.VISIBLE);
        showProgressBar();

        collectionReference.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        hideProgressBar();
                        //progressBar_mainLoader.setVisibility(View.GONE);
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : list) {
                                Audio audio = documentSnapshot.toObject(Audio.class);
                                if (audio.getAudioID() != null)
                                    listAudio.add(audio);
                                else {
                                    String uploadID = documentSnapshot.getId();
                                    collectionReference.document(uploadID).delete();
                                }
                            }

                        } else if (onlineUser.getListDahiraID().contains(dahira.getDahiraID()) &&
                                onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur")) {
                            showAlertDialog(ShowSongsActivity.this,
                                    "Repertoir Audio vide. Cliquez sur l'icone (+) " +
                                            "pour ajouter ou enregistrer un audio.");
                        } else {
                            Intent intent = new Intent(ShowSongsActivity.this, DahiraInfoActivity.class);
                            showAlertDialog(ShowSongsActivity.this,
                                    "Repertoir Audio vide. Seuls les administrateurs " +
                                            "de ce dahira peuvent ajouter des audios.", intent);
                        }

                        currentIndex = 0;
                        songsAdapter.notifyDataSetChanged();
                        songsAdapter.setSelectedPosition(0);
                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressBar();
                //progressBar_mainLoader.setVisibility(View.GONE);
            }
        });
    }

    private void prepareSong(Audio audio) {

        if (audio.audioDuration == null) {
            toastMessage(ShowSongsActivity.this, "Fichier invalide");
            return;
        }
        String str_duration = audio.audioDuration.replace(":", "");
        currentSongLength = Integer.parseInt(str_duration);
        progressBar_loader.setVisibility(View.VISIBLE);
        textView_titleToolbar.setVisibility(View.GONE);
        imageViewPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.selector_play));
        textView_titleToolbar.setText(audio.getAudioTitle());
        textView_durationToolbar.setText(Utility.convertDuration(Integer.parseInt(str_duration)));
        mediaPlayer.reset();


        try {
            mediaPlayer.setDataSource(audio.getAudioUri());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void removeInFirebaseStorage(Audio audio) {
        if (audio.audioUri != null) {

            StorageReference storageRef = firebaseStorage
                    .getReferenceFromUrl(audio.audioUri);

            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
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

    private void togglePlay(MediaPlayer mp) {

        if (mp.isPlaying()) {
            mp.stop();
            mp.reset();
        } else {
            progressBar_loader.setVisibility(View.GONE);
            textView_titleToolbar.setVisibility(View.VISIBLE);
            mp.start();
            imageViewPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.selector_pause));
            final Handler mHandler = new Handler();
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    seekBar.setMax(mediaPlayer.getDuration());
                    //int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    textView_durationToolbar.setText(Utility.convertDuration(mediaPlayer.getCurrentPosition()));
                    mHandler.postDelayed(this, 1000);

                }
            });
        }
    }

    private void initialization() {
        listAudio = new ArrayList<>();
        textView_titleToolbar = findViewById(R.id.tb_title);
        textView_durationToolbar = findViewById(R.id.tv_time);
        imageViewPlay = findViewById(R.id.iv_play);
        imageViewNext = findViewById(R.id.iv_next);
        imageViewPrevious = findViewById(R.id.iv_previous);
        progressBar_loader = findViewById(R.id.pb_loader);
        progressBar_mainLoader = findViewById(R.id.pb_main_loader);
        recyclerView = findViewById(R.id.recyclerView);
        seekBar = findViewById(R.id.seekbar);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        relativeLayout_song = findViewById(R.id.relativeLayout_song);
        relativeLayout_progressBar = findViewById(R.id.relativeLayout_progressBar);
        toolbarBottom = findViewById(R.id.bottom_toolbar);
        textView_titleLayoutSong = findViewById(R.id.textView_titleLayoutSong);
    }

    private void changeSelectedSong(int index) {
        songsAdapter.notifyItemChanged(songsAdapter.getSelectedPosition());
        currentIndex = index;
        songsAdapter.setSelectedPosition(currentIndex);
        songsAdapter.notifyItemChanged(currentIndex);
    }

    private void handleSeekbar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void pushPlay() {
        imageViewPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying() && mediaPlayer != null) {
                    imageViewPlay.setImageDrawable(ContextCompat.getDrawable(ShowSongsActivity.this, R.drawable.selector_play));
                    mediaPlayer.pause();
                } else {
                    if (firstLaunch) {
                        Audio audio = listAudio.get(0);
                        changeSelectedSong(0);
                        prepareSong(audio);
                    } else {
                        mediaPlayer.start();
                        firstLaunch = false;
                    }
                    imageViewPlay.setImageDrawable(ContextCompat.getDrawable(ShowSongsActivity.this, R.drawable.selector_pause));
                }

            }
        });
    }

    private void pushPrevious() {

        imageViewPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstLaunch = false;
                if (mediaPlayer != null) {

                    if (currentIndex - 1 >= 0) {
                        Audio previous = listAudio.get(currentIndex - 1);
                        changeSelectedSong(currentIndex - 1);
                        prepareSong(previous);
                    } else {
                        changeSelectedSong(listAudio.size() - 1);
                        prepareSong(listAudio.get(listAudio.size() - 1));
                    }

                }
            }
        });

    }

    private void pushNext() {

        imageViewNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstLaunch = false;
                if (mediaPlayer != null) {

                    if (currentIndex + 1 < listAudio.size()) {
                        Audio next = listAudio.get(currentIndex + 1);
                        changeSelectedSong(currentIndex + 1);
                        prepareSong(next);
                    } else {
                        changeSelectedSong(0);
                        prepareSong(listAudio.get(0));
                    }

                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        super.onDestroy();
    }

    private void enableSwipeToDelete() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                final int position = viewHolder.getAdapterPosition();
                final Audio audio = listAudio.get(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(ShowSongsActivity.this, R.style.alertDialog);
                builder.setTitle("Supprimer audio!");
                builder.setMessage("Etes vous sure de vouloir supprimer ce fichier?");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Update totalAdiya dahira
                        songsAdapter.removeItem(position);
                        if (audio.getAudioID() != null) {
                            //Remove item in FirebaseFireStore
                            collectionReference.document(audio.getAudioID()).delete();
                        }
                        //Remove item in FirebaseStorage
                        removeInFirebaseStorage(audio);

                        Snackbar snackbar = Snackbar.make(coordinatorLayout, "Fichier audio supprime.", Snackbar.LENGTH_LONG);
                        snackbar.setActionTextColor(Color.GREEN);
                        snackbar.show();
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(ShowSongsActivity.this, ShowSongsActivity.class));
                    }
                });
                builder.show();
            }

        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);
    }

    public void hideProgressBar() {
        relativeLayout_song.setVisibility(View.VISIBLE);
        relativeLayout_progressBar.setVisibility(View.GONE);
    }

    public void showProgressBar() {
        relativeLayout_song.setVisibility(View.GONE);
        relativeLayout_progressBar.setVisibility(View.VISIBLE);
    }
}

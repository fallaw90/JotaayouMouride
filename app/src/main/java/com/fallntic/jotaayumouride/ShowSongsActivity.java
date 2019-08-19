package com.fallntic.jotaayumouride;

import android.annotation.SuppressLint;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Adapter.SongAdapter;
import com.fallntic.jotaayumouride.Model.Song;
import com.fallntic.jotaayumouride.Utility.MyStaticVariables;
import com.fallntic.jotaayumouride.Utility.SwipeToDeleteCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.HomeActivity.loadInterstitialAd;
import static com.fallntic.jotaayumouride.Utility.DataHolder.dahira;
import static com.fallntic.jotaayumouride.Utility.DataHolder.indexOnlineUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.convertDuration;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listSong;


public class ShowSongsActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "ShowSongsActivity";
    int lastProgress = 0;
    private RecyclerView recycler;
    private SongAdapter mAdapter;
    private int currentIndex = 0;
    private Toolbar toolbar, toolbar_bottom;
    private CoordinatorLayout coordinatorLayout;
    private TextView tb_title, tv_empty, tv_time, tv_duration, textViewTitle, textViewDeleteInstruction;
    private ImageView iv_play, iv_next, iv_previous;
    private MediaPlayer mediaPlayer;
    private ProgressBar pb_loader, pb_main_loader;
    private SeekBar seekBar;
    private boolean firstLaunch = true;
    private long currentSongLength;
    private FloatingActionButton fab_search;
    private boolean isPlaying = false;
    private double startTime = 0;
    private Handler myHandler = new Handler();
    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            //seekBar.setMax(mediaPlayer.getDuration());
            startTime = mediaPlayer.getCurrentPosition();
            tv_time.setText(convertDuration(mediaPlayer.getCurrentPosition()));
            seekBar.setProgress((int) startTime);
            myHandler.postDelayed(this, 500);
        }
    };

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_songs);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setLogo(R.mipmap.logo);
        setSupportActionBar(toolbar);

        //Initialisation des vues
        initializeViews();

        checkInternetConnection(this);

        if (listSong == null || listSong.size() <= 0) {
            if (onlineUser.getListDahiraID().contains(dahira.getDahiraID()) &&
                    onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur")) {
                tv_empty.setText("Votre repertoire audio est vide. Cliquez sur l'icone pour " +
                        "enregistrer ou ajouter un audio dans votre repertoire.");
            } else {
                tv_empty.setText("Le repertoire audio du dahira " + dahira.getDahiraName() + " est vide.");
            }

            tv_empty.setVisibility(View.VISIBLE);
            fab_search.setVisibility(View.GONE);
            toolbar_bottom.setVisibility(View.GONE);
        }
        textViewTitle.setText("Bienvenu dans le repertoire audio du dahira " + dahira.getDahiraName());

        if (indexOnlineUser == -1 || !onlineUser.getListDahiraID().contains(dahira.getDahiraID()) ||
                !onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur"))
            textViewDeleteInstruction.setVisibility(View.GONE);

        setMyAdapter(listSong);

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
                if (currentIndex + 1 < listSong.size()) {
                    Song next = listSong.get(currentIndex + 1);
                    changeSelectedSong(currentIndex + 1);
                    prepareSong(next);
                } else {
                    Song next = listSong.get(0);
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

        //Gestion du click sur le bouton rechercher
        fab_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog();
            }
        });

        enableSwipeToDelete();

        loadInterstitialAd(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_back:
                finish();
                startActivity(new Intent(ShowSongsActivity.this, DahiraInfoActivity.class));
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying())
                mediaPlayer.stop();

            mediaPlayer.release();
        }

        if (myHandler != null) {
            myHandler.removeCallbacks(UpdateSongTime);
            myHandler = null;
        }

    }

    private void setMyAdapter(List<Song> songList) {
        //Requête récupérant les chansons
        recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mAdapter = new SongAdapter(getApplicationContext(), listSong, new SongAdapter.RecyclerItemClickListener() {
            @Override
            public void onClickListener(Song song, int position) {
                firstLaunch = false;
                changeSelectedSong(position);
                prepareSong(song);
            }
        });
        recycler.setAdapter(mAdapter);
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

    private void prepareSong(Song song) {
        String str_duration = song.getAudioDuration().replace(":", "");
        currentSongLength = Integer.parseInt(str_duration);
        pb_loader.setVisibility(View.VISIBLE);
        tb_title.setVisibility(View.GONE);
        iv_play.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.selector_play));
        tb_title.setText(song.getAudioTitle());
        tv_time.setText(song.getAudioDuration());
        mediaPlayer.reset();

        try {
            mediaPlayer.setDataSource(song.getAudioUri());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void togglePlay(MediaPlayer mp) {
        seekBar.setMax(mediaPlayer.getDuration());
        if (mp.isPlaying()) {
            mp.stop();
            mp.reset();
        } else {
            pb_loader.setVisibility(View.GONE);
            tb_title.setVisibility(View.VISIBLE);
            mp.start();
            iv_play.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.selector_pause));

            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            myHandler.postDelayed(UpdateSongTime, 100);
        }
    }

    private void initializeViews() {

        toolbar_bottom = findViewById(R.id.bottom_toolbar);
        tb_title = findViewById(R.id.tb_title);
        tv_duration = findViewById(R.id.tv_duration);
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

    private void changeSelectedSong(int index) {
        mAdapter.notifyItemChanged(mAdapter.getSelectedPosition());
        currentIndex = index;
        mAdapter.setSelectedPosition(currentIndex);
        mAdapter.notifyItemChanged(currentIndex);
    }

    private void pushPlay() {
        iv_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mediaPlayer.isPlaying() && mediaPlayer != null) {
                    iv_play.setImageDrawable(ContextCompat.getDrawable(ShowSongsActivity.this, R.drawable.selector_play));
                    mediaPlayer.pause();
                } else {
                    if (firstLaunch) {
                        Song song = listSong.get(0);
                        changeSelectedSong(0);
                        prepareSong(song);
                    } else {
                        mediaPlayer.start();
                        firstLaunch = false;
                    }
                    iv_play.setImageDrawable(ContextCompat.getDrawable(ShowSongsActivity.this, R.drawable.selector_pause));
                }

            }
        });
    }

    private void pushPrevious() {

        iv_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstLaunch = false;
                if (mediaPlayer != null) {

                    if (currentIndex - 1 >= 0) {
                        Song previous = listSong.get(currentIndex - 1);
                        changeSelectedSong(currentIndex - 1);
                        prepareSong(previous);
                    } else {
                        changeSelectedSong(listSong.size() - 1);
                        prepareSong(listSong.get(listSong.size() - 1));
                    }

                }
            }
        });

    }

    private void pushNext() {

        iv_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstLaunch = false;
                if (mediaPlayer != null) {

                    if (currentIndex + 1 < listSong.size()) {
                        Song next = listSong.get(currentIndex + 1);
                        changeSelectedSong(currentIndex + 1);
                        prepareSong(next);
                    } else {
                        changeSelectedSong(0);
                        prepareSong(listSong.get(0));
                    }

                }
            }
        });

    }

    public void createDialog() {

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
                        setMyAdapter(listSongFound);
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

    @Override
    protected void onDestroy() {
        releaseMediaPlayer();

        super.onDestroy();
    }

    public void releaseMediaPlayer(){
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }

            if (mediaPlayer == null)
                mediaPlayer = new MediaPlayer();
            if (myHandler == null)
                myHandler = new Handler();
            if (myHandler != null)
                myHandler.removeCallbacks(UpdateSongTime);

        } catch (Exception e) {
        }
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
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
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
        itemTouchhelper.attachToRecyclerView(recycler);
    }

    public void removeInFirebaseStorage(Song song) {
        if (song.audioUri != null) {

            StorageReference storageRef = MyStaticVariables.firebaseStorage
                    .getReferenceFromUrl(song.audioUri);

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
            case R.id.icon_add:
                startActivity(new Intent(this, AddAudioActivity.class));
                break;

            case R.id.instructions:
                startActivity(new Intent(this, InstructionsActivity.class));
                break;
        }
        return true;
    }

}

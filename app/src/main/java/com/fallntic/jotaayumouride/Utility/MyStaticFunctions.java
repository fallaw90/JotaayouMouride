package com.fallntic.jotaayumouride.Utility;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fallntic.jotaayumouride.Adapter.SongAdapter;
import com.fallntic.jotaayumouride.DahiraInfoActivity;
import com.fallntic.jotaayumouride.HomeActivity;
import com.fallntic.jotaayumouride.Model.Image;
import com.fallntic.jotaayumouride.Model.ListImageObject;
import com.fallntic.jotaayumouride.Model.ListSongObject;
import com.fallntic.jotaayumouride.Model.Song;
import com.fallntic.jotaayumouride.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static com.fallntic.jotaayumouride.HomeActivity.loadInterstitialAd;
import static com.fallntic.jotaayumouride.MainActivity.TAG;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.UpdateSongTime;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.collectionReference;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.currentIndex;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.currentSongLength;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.dahira;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.displayDahira;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.displayEvent;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.fab_search;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.firestore;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.firstLaunch;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.iv_next;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.iv_play;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.iv_previous;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAllDahira;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAllEvent;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAudiosAM;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAudiosHT;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAudiosHTDK;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAudiosQuran;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAudiosRadiass;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAudiosSerigneMbayeDiakhate;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAudiosSerigneMoussaKa;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listDahiraFound;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listExpenses;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listImage;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listSong;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listUser;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.mAdapter;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.mediaPlayer;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.myHandler;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.myListDahira;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.myListEvents;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.pb_loader;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.recycler;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.seekBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.tb_title;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.tv_time;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.updateStorage;

public class MyStaticFunctions {

    /**
     * Get all songs in repertory audio from firestore and save the songs in
     * the static variable listSong in MyStaticVariables class.
     *
     * @param context
     */
    public static void getListSongs(final Context context) {
        //Retrieve all songs from FirebaseFirestore
        if (listSong == null) {
            listSong = new ArrayList<>();

            collectionReference = firestore.collection("dahiras")
                    .document(dahira.getDahiraID()).collection("audios");
            collectionReference.get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot documentSnapshot : list) {
                                    Song song = documentSnapshot.toObject(Song.class);
                                    if (song.getAudioUri() != null)
                                        listSong.add(song);
                                    else {
                                        String uploadID = documentSnapshot.getId();
                                        MyStaticVariables.collectionReference.document(uploadID).delete();
                                    }
                                }
                                Collections.sort(listSong);
                            }

                            getListImages(context);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    toastMessage(context, "Erreur de telechargement du repertoire audio.");
                }
            });
        }
    }

    public static void getListImages(final Context context) {
        if (listImage == null) {
            listImage = new ArrayList<>();
            showProgressBar();
            firestore.collection("images").whereEqualTo("dahiraID", dahira.getDahiraID()).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            hideProgressBar();
                            if (!queryDocumentSnapshots.isEmpty()) {
                                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    ListImageObject listImageObject = documentSnapshot.toObject(ListImageObject.class);
                                    listImage.addAll(listImageObject.getListImage());
                                    break;
                                }
                                Log.d(TAG, "Image name downloaded");
                            }

                            context.startActivity(new Intent(context, DahiraInfoActivity.class));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Error downloading image name");
                            toastMessage(context, "Erreur de telechargement du repertoire audio.");
                        }
                    });
        } else
            getSizeStorage(context);
    }

    public static void checkInternetConnection(Context context) {
        if (!isConnected(context)) {
            Intent intent = new Intent(context, HomeActivity.class);
            showAlertDialog(context, "Oops! Pas de connexion, " +
                    "verifier votre connexion internet puis reesayez SVP.", intent);
            return;
        }
    }

    public static boolean isConnected(Context context) {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = Objects.requireNonNull(cm).getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();

            return connected;

        } catch (Exception e) {
            Log.e("Connectivity Exception", Objects.requireNonNull(e.getMessage()));
        }

        return connected;
    }

    //************************** ProgressBar ************************************
    public static void showProgressBar() {
        relativeLayoutData.setVisibility(View.GONE);
        relativeLayoutProgressBar.setVisibility(View.VISIBLE);
    }

    public static void hideProgressBar() {
        relativeLayoutData.setVisibility(View.VISIBLE);
        relativeLayoutProgressBar.setVisibility(View.GONE);
    }

    public static void showImage(final Context context, String uri, ImageView imageView) {
        GlideApp.with(context)
                .load(uri)
                .placeholder(R.drawable.logo_web)
                .centerCrop()
                .into(imageView);
    }

    public static void saveProfileImage(final Context context, final String uri) {
        final Map<String, Object> mapUri = new HashMap<>();
        mapUri.put("imageUri", uri);
        onlineUser.setImageUri(uri);
        showProgressBar();
        firestore.collection("users").document(onlineUser.getUserID())
                .update(mapUri).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hideProgressBar();
                Log.d(TAG, "Image name saved");
                toastMessage(context, "Photo profil enregistre.");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressBar();
                Log.d(TAG, "Error downloading image name");
            }
        });
    }

    public static void saveLogoDahira(final Context context, final String uri) {
        final Map<String, Object> mapUri = new HashMap<>();
        mapUri.put("imageUri", uri);

        firestore.collection("dahiras").document(dahira.getDahiraID())
                .update(mapUri).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "Image name saved");
                toastMessage(context, "Logo enregistre!");
                //context.startActivity(new Intent(context, HomeActivity.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Error downloading image name");
            }
        });
    }

    //********************Size Storage**********************
    public static void getSizeStorage(Context context) {
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
                    Log.i("Size = ", String.valueOf(storageMetadata.getSizeBytes()));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {

                }
            });
        }
    }

    public static void getSizeSongsStorage(Song song) {
        FirebaseStorage storage = FirebaseStorage.getInstance(); // 1
        StorageReference storageRef = storage.getReference();
        StorageReference reference;

        reference = storageRef.child("gallery").child("audios").child(dahira.getDahiraID()).child(song.getAudioID());
        reference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                dahira.setCurrentSizeStorage(dahira.getCurrentSizeStorage() + storageMetadata.getSizeBytes() / 1048576);
                Log.i("Size = ", String.valueOf(storageMetadata.getSizeBytes()));
                updateStorageSize(dahira.getCurrentSizeStorage());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });
    }

    public static void getSizeImagesStorage(Image image) {
        FirebaseStorage storage = FirebaseStorage.getInstance(); // 1
        StorageReference storageRef = storage.getReference();
        StorageReference reference;
        reference = storageRef.child("gallery").child("picture").child(dahira.getDahiraID()).child(image.getImageName());
        reference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                dahira.setCurrentSizeStorage(dahira.getCurrentSizeStorage() + storageMetadata.getSizeBytes() / 1048576);
                Log.i("Size = ", String.valueOf(storageMetadata.getSizeBytes()));
                updateStorageSize(dahira.getCurrentSizeStorage());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });
    }

    public static void updateStorageSize(final double currentSize, final double dedicatedSize) {
        final Map<String, Object> mapSizeStorage = new HashMap<>();
        mapSizeStorage.put("currentSizeStorage", currentSize);
        mapSizeStorage.put("dedicatedSizeStorage", dedicatedSize);

        dahira.setDedicatedSizeStorage(dedicatedSize);

        firestore.collection("dahiras").document(dahira.getDahiraID())
                .update(mapSizeStorage).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "Image name saved");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Error downloading image name");
            }
        });
    }

    public static void updateStorageSize(final double currentSize) {
        final Map<String, Object> mapSizeStorage = new HashMap<>();
        mapSizeStorage.put("currentSizeStorage", currentSize);

        firestore.collection("dahiras").document(dahira.getDahiraID())
                .update(mapSizeStorage).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                updateStorage = false;
                Log.d(TAG, "Image name saved");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Error downloading image name");
            }
        });
    }

    //***************************** Mmedia Player *******************************************
    public static void setMyAdapter(final Context context, final List<Song> listSong) {

        if (myHandler == null)
            myHandler = new Handler();
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();


        //Requête récupérant les chansons
        recycler.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = new SongAdapter(context, listSong, new SongAdapter.RecyclerItemClickListener() {

            @Override
            public void onClickListener(Song song, int position) {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                }
                firstLaunch = false;
                changeSelectedSong(position);
                prepareSong(context, song);
            }

            @Override
            public boolean onLongClickListener(Song song, int position) {
                downloadFile(context, song.getAudioTitle(), song.getAudioUri());
                toastMessage(context, "Telechargement en cours ...");
                return true;
            }
        });
        recycler.setAdapter(mAdapter);


        //Initialisation du media player
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //Lancer la chanson
                togglePlay(context, mp);
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (currentIndex + 1 < listSong.size()) {
                    Song next = listSong.get(currentIndex + 1);
                    changeSelectedSong(currentIndex + 1);
                    prepareSong(context, next);
                } else {
                    Song next = listSong.get(0);
                    changeSelectedSong(0);
                    prepareSong(context, next);
                }
            }
        });


        //Gestion de la seekbar
        handleSeekbar();

        //Controle de la chanson
        pushPlay(context, listSong);
        pushPrevious(context, listSong);
        pushNext(context, listSong);

        //Gestion du click sur le bouton rechercher
        fab_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchSong(context, listSong);
            }
        });
    }

    public static void handleSeekbar() {
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

    public static void prepareSong(Context context, Song song) {

        String str_duration = song.getAudioDuration().replace(":", "");
        currentSongLength = Integer.parseInt(str_duration);
        pb_loader.setVisibility(View.GONE);
        tb_title.setVisibility(View.GONE);
        iv_play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.selector_play));
        tb_title.setText(song.getAudioTitle());
        tv_time.setText(song.getAudioDuration());
        mediaPlayer.reset();

        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                }
                mediaPlayer.setDataSource(song.getAudioUri());
                mediaPlayer.prepareAsync();
                loadInterstitialAd(context);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void togglePlay(Context context, MediaPlayer mp) {
        seekBar.setMax(mediaPlayer.getDuration());
        if (mp != null && mp.isPlaying()) {
            mp.stop();
            mp.reset();
        } else {
            pb_loader.setVisibility(View.GONE);
            tb_title.setVisibility(View.GONE);
            mp.start();
            iv_play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.selector_pause));

            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            myHandler.postDelayed(UpdateSongTime, 100);
        }
    }

    public static void changeSelectedSong(int index) {
        mAdapter.notifyItemChanged(mAdapter.getSelectedPosition());
        currentIndex = index;
        mAdapter.setSelectedPosition(currentIndex);
        mAdapter.notifyItemChanged(currentIndex);
    }

    public static void pushPlay(final Context context, final List<Song> listSong) {
        iv_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    iv_play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.selector_play));
                    mediaPlayer.pause();
                } else {
                    if (firstLaunch) {
                        Song song = listSong.get(0);
                        changeSelectedSong(0);
                        prepareSong(context, song);
                    } else {
                        mediaPlayer.start();
                        firstLaunch = false;
                    }
                    iv_play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.selector_pause));
                }
            }
        });
    }

    public static void pushPrevious(final Context context, final List<Song> listSong) {

        iv_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstLaunch = false;
                if (mediaPlayer != null) {

                    if (currentIndex - 1 >= 0) {
                        Song previous = listSong.get(currentIndex - 1);
                        changeSelectedSong(currentIndex - 1);
                        prepareSong(context, previous);
                    } else {
                        changeSelectedSong(listSong.size() - 1);
                        prepareSong(context, listSong.get(listSong.size() - 1));
                    }

                }
            }
        });

    }

    public static void pushNext(final Context context, final List<Song> listSong) {

        iv_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstLaunch = false;
                if (mediaPlayer != null) {

                    if (currentIndex + 1 < listSong.size()) {
                        Song next = listSong.get(currentIndex + 1);
                        changeSelectedSong(currentIndex + 1);
                        prepareSong(context, next);
                    } else {
                        changeSelectedSong(0);
                        prepareSong(context, listSong.get(0));
                    }

                }
            }
        });

    }

    public static void searchSong(final Context context, final List<Song> listSong) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.dialog_search_song, null);
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
                        if (song.getAudioTitle().contains(search) || song.getAudioTitle().equals(search)) {
                            listSongFound.add(song);
                        }

                        String[] titleSong = song.audioTitle.split("-");
                        for (String title : titleSong) {
                            if (title.contains(search) || title.equals(search)) {
                                listSongFound.add(song);
                            }
                        }
                    }
                    if (listSongFound.size() > 0) {
                        setMyAdapter(context, listSongFound);
                    } else {
                        Toast.makeText(context, "Song non trouve", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(context, "Veuillez remplir le champ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);

    }

    public static void setMediaPlayer() {

        //startTime = 0;
        firstLaunch = true;
        //currentIndex = 0;

        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }

            if (mediaPlayer == null)
                mediaPlayer = new MediaPlayer();

            if (myHandler == null)
                myHandler = new Handler();

            myHandler.removeCallbacks(UpdateSongTime);

        } catch (Exception ignored) {
        }
    }

    public static String convertDuration(long duration) {
        long minutes = (duration / 1000) / 60;
        long seconds = (duration / 1000) % 60;
        String converted = String.format("%d:%02d", minutes, seconds);
        return converted;
    }

    public static String formatDuration(int seconds) {
        int p1 = seconds % 60;
        int p2 = seconds / 60;
        int p3 = p2 % 60;
        p2 = p2 / 60;

        if (p2 > 0)
            return p2 + ":" + p3 + ":" + p1;
        else
            return p3 + ":" + p1;
    }

    public static void getListAudios(final Context context, final List<Song> listSong, String collection, String documentID) {
        //Retrieve all songs from FirebaseFirestore
        if (listSong.isEmpty()) {
            showProgressBar();
            MyStaticVariables.collectionReference = MyStaticVariables.firestore.collection(collection);
            MyStaticVariables.collectionReference.whereEqualTo("documentID", documentID).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            ListSongObject listSongObject = null;
                            if (!queryDocumentSnapshots.isEmpty()) {
                                hideProgressBar();
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot documentSnapshot : list) {
                                    listSongObject = documentSnapshot.toObject(ListSongObject.class);
                                    listSong.addAll(listSongObject.getListSong());
                                    break;
                                }

                                Collections.sort(listSong);
                                setMyAdapter(context, listSong);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    hideProgressBar();
                    toastMessage(context, "Erreur de telechargement du repertoire audio.");
                }
            });
        } else {
            setMyAdapter(context, listSong);
        }
    }

    public static void downloadFile(Context context, String fileName, String url) {

        //Load ads
        DownloadManager downloadmanager = (DownloadManager) context.
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, DIRECTORY_DOWNLOADS, fileName);

        if (downloadmanager != null) {
            downloadmanager.enqueue(request);
        }
    }

    public static void toastMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }


    //*******************Moved Functions ******************************
    public static void showProgressDialog(Context context, String str) {
        dismissProgressDialog();
        MyStaticVariables.progressDialog = new ProgressDialog(context);
        MyStaticVariables.progressDialog.setMessage(str);
        MyStaticVariables.progressDialog.setCancelable(false);
        MyStaticVariables.progressDialog.setCanceledOnTouchOutside(false);
        MyStaticVariables.progressDialog.show();
    }

    public static void dismissProgressDialog() {
        if (MyStaticVariables.progressDialog != null) {
            MyStaticVariables.progressDialog.dismiss();
        }
    }

    public static void logout(Context context) {
        MyStaticVariables.typeOfContribution = "";
        MyStaticVariables.indexOnlineUser = -1;
        MyStaticVariables.indexSelectedUser = -1;
        MyStaticVariables.actionSelected = "";
        displayDahira = "";
        displayEvent = "";

        onlineUser = null;
        MyStaticVariables.selectedUser = null;
        dahira = null;
        listSong = null;
        listAudiosQuran = null;
        listAudiosSerigneMbayeDiakhate = null;
        listAudiosSerigneMoussaKa = null;
        listAudiosHT = null;
        listAudiosHTDK = null;
        listAudiosAM = null;
        listAudiosRadiass = null;
        listUser = null;
        myListEvents = null;
        listAllEvent = null;
        myListDahira = null;
        listAllDahira = null;
        listDahiraFound = null;
        listExpenses = null;
        listImage = null;
        FirebaseAuth.getInstance().signOut();
        context.startActivity(new Intent(context, HomeActivity.class));
    }

    public static void showAlertDialog(final Context context, String message) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.alert_dialog, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        final TextView textViewAlertDialog = dialogView.findViewById(R.id.textView_alertDialog);
        final Button buttonAlertDialog = dialogView.findViewById(R.id.button_dialog);

        textViewAlertDialog.setText(message);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonAlertDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }

    public static void showAlertDialog(final Context context, String message, final Intent intent) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.alert_dialog, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        final TextView textViewAlertDialog = dialogView.findViewById(R.id.textView_alertDialog);
        final Button buttonAlertDialog = dialogView.findViewById(R.id.button_dialog);

        textViewAlertDialog.setText(message);
        final AlertDialog alertDialogMessage = dialogBuilder.create();
        alertDialogMessage.show();

        buttonAlertDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(intent);
                alertDialogMessage.dismiss();
            }
        });
    }

    public static boolean hasValidationErrors(String name, EditText editTextName, String phoneNumber,
                                              EditText editTextPhoneNumber, String address, EditText editTextAddress,
                                              String adiya, EditText editTextAdiya, String sass, EditText editTextSass,
                                              String social, EditText editTextSocial) {

        if (name.isEmpty()) {
            editTextName.setError("Ce champ est obligatoir!");
            editTextName.requestFocus();
            return true;
        }

        if (phoneNumber.isEmpty()) {
            editTextPhoneNumber.setError("Champ obligatoire");
            editTextPhoneNumber.requestFocus();
            return true;
        }

        if (!phoneNumber.isEmpty() && (!phoneNumber.matches("[0-9]+") ||
                phoneNumber.length() != 9 || !checkPrefix(phoneNumber))) {
            editTextPhoneNumber.setError("Numero de telephone incorrect");
            editTextPhoneNumber.requestFocus();
            return true;
        }

        if (address.isEmpty()) {
            editTextAddress.setError("Ce champ est obligatoir!");
            editTextAddress.requestFocus();
            return true;
        }

        if (adiya.isEmpty() || !isDouble(adiya)) {
            editTextAdiya.setError("Valeur incorrecte!");
            editTextAdiya.requestFocus();
            return true;
        }

        if (sass.isEmpty() || !isDouble(sass)) {
            editTextSass.setError("Valeur incorrecte!");
            editTextSass.requestFocus();
            return true;
        }

        if (social.isEmpty() || !isDouble(social)) {
            editTextSocial.setError("Valeur incorrecte!");
            editTextSocial.requestFocus();
            return true;
        }

        return false;
    }

    public static boolean hasValidationErrors(String dahiraName, EditText editTextDahiraName, String dieuwrine,
                                              EditText editTextDieuwrine, String dahiraPhoneNumber, EditText editTextDahiraPhoneNumber,
                                              String siege, EditText editTextSiege, String totalAdiya, EditText editTextAdiya,
                                              String totalSass, EditText editTextSass, String totalSocial, EditText editTextSocial) {

        if (dahiraName.isEmpty()) {
            editTextDahiraName.setError("Nom dahira obligatoire");
            editTextDahiraName.requestFocus();
            return true;
        }

        if (dieuwrine.isEmpty()) {
            editTextDieuwrine.setError("Champ obligatoire");
            editTextDieuwrine.requestFocus();
            return true;
        }

        if (dahiraPhoneNumber.isEmpty()) {
            editTextDahiraPhoneNumber.setError("Champ obligatoire");
            editTextDahiraPhoneNumber.requestFocus();
            return true;
        }

        if (!dahiraPhoneNumber.isEmpty() && (!dahiraPhoneNumber.matches("[0-9]+") ||
                dahiraPhoneNumber.length() != 9 || !checkPrefix(dahiraPhoneNumber))) {
            editTextDahiraPhoneNumber.setError("Numero de telephone incorrect");
            editTextDahiraPhoneNumber.requestFocus();
            return true;
        }

        if (siege.isEmpty()) {
            editTextSiege.setError("Champ obligatoire");
            editTextSiege.requestFocus();
            return true;
        }

        if (totalAdiya.isEmpty()) {
            editTextSiege.setError("Non montant, entrer 0");
            editTextAdiya.requestFocus();
            return true;
        } else if (!isDouble(totalAdiya)) {
            editTextAdiya.setText("Valeur listAdiya incorrecte");
            editTextAdiya.requestFocus();
            return true;
        }

        if (totalSass.isEmpty()) {
            editTextSiege.setError("Non montant, entrer 0");
            editTextSass.requestFocus();
            return true;
        } else if (!isDouble(totalSass)) {
            editTextSass.setText("Valeur sass incorrecte");
            editTextSass.requestFocus();
            return true;
        }

        if (totalSocial.isEmpty()) {
            editTextSiege.setError("Non montant, entrer 0");
            editTextSass.requestFocus();
            return true;
        } else if (!isDouble(totalSocial)) {
            editTextSocial.setText("Valeur sociale incorrecte");
            editTextSocial.requestFocus();
            return true;
        }

        return false;
    }

    public static boolean checkPrefix(String str) {
        String prefix = str.substring(0, 2);
        boolean validatePrefix;
        switch (prefix) {
            case "70":
                validatePrefix = true;
                break;
            case "76":
                validatePrefix = true;
                break;
            case "77":
                validatePrefix = true;
                break;
            case "78":
                validatePrefix = true;
                break;
            default:
                validatePrefix = false;
                break;
        }

        return validatePrefix;
    }

    public static boolean isDouble(String str) {
        str = str.replace(",", ".");
        double value;
        try {
            value = Double.parseDouble(str);
            return true;
            // it means it is double
        } catch (Exception e1) {
            // this means it is not double
            e1.printStackTrace();
            return false;
        }
    }

    public static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("dd/MM/yyyy");
        String strDate = mdformat.format(calendar.getTime());
        return strDate;
    }

    public static void getDate(Context context, final EditText editText) {
        int mYear, mMonth, mDay;
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, R.style.DatePickerDialog,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        if (monthOfYear < 12)
                            monthOfYear++;
                        else if (monthOfYear == 12)
                            monthOfYear = 1;

                        String mDate = dayOfMonth + "/" + monthOfYear + "/" + year;
                        editText.setText(mDate);
                    }
                }, mYear, mMonth, mDay);

        datePickerDialog.show();
    }

    public static void getTime(Context context, final EditText editText, String title) {
        TimePickerDialog timePickerDialog;
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        timePickerDialog = new TimePickerDialog(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {

                        editText.setText(String.format(hourOfDay + ":" + minutes));
                    }
                }, currentHour, currentMinute, true);
        timePickerDialog.setTitle(title);
        timePickerDialog.show();
    }

    public static void createNewCollection(final Context context, final String collectionName,
                                           String documentName, Object data) {
        FirebaseFirestore.getInstance().collection(collectionName).document(documentName)
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(Constraints.TAG, "New collection " + collectionName + " set successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(Constraints.TAG, "Error initContributions function line 351");
                    }
                });
        MyStaticVariables.actionSelected = "";
    }

    public static void updateDocument(final Context context, final String collectionName, String documentID, String field, String value) {
        showProgressDialog(context, "Mis a jour " + collectionName + " en cours ...");
        FirebaseFirestore.getInstance().collection(collectionName).document(documentID)
                .update(field, value)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dismissProgressDialog();
                        Log.d(Constraints.TAG, collectionName + "updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissProgressDialog();
                        Log.d(Constraints.TAG, "Error updated " + collectionName);
                    }
                });
    }

    public static void updateDocument(final Context context, final String collectionName, String documentID, String field, List<String> listValue) {
        showProgressDialog(context, "Mis a jour " + collectionName + " en cours ...");
        FirebaseFirestore.getInstance().collection(collectionName).document(documentID)
                .update(field, listValue)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dismissProgressDialog();
                        Log.d(Constraints.TAG, collectionName + " updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissProgressDialog();
                        Log.d(Constraints.TAG, "Error updated " + collectionName);
                    }
                });
    }

    public static void deleteDocument(final Context context, String collectionName, String documentID) {
        FirebaseFirestore.getInstance().collection(collectionName).document(documentID).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //toastMessage(context, task.getException().getMessage());
                        } else {
                            //toastMessage(context, task.getException().getMessage());
                        }
                    }
                });
    }

    public static void call(Context context, Activity activity, String phoneNumber) {
        try {
            if (Build.VERSION.SDK_INT > 22) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CALL_PHONE}, 101);
                    return;
                }

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                context.startActivity(callIntent);

            } else {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                context.startActivity(callIntent);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

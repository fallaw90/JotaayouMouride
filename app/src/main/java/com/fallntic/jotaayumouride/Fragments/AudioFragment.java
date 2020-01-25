package com.fallntic.jotaayumouride.Fragments;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Adapter.SongAdapter;
import com.fallntic.jotaayumouride.Interfaces.Playable;
import com.fallntic.jotaayumouride.Model.ListSongObject;
import com.fallntic.jotaayumouride.Model.Song;
import com.fallntic.jotaayumouride.Notifications.CreateNotificationMusic;
import com.fallntic.jotaayumouride.R;
import com.fallntic.jotaayumouride.Services.OnClearFromRecentService;
import com.fallntic.jotaayumouride.Utility.MyStaticFunctions;
import com.fallntic.jotaayumouride.Utility.MyStaticVariables;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static com.fallntic.jotaayumouride.HomeActivity.loadInterstitialAd;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.convertDuration;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAudiosAM;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAudiosHT;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAudiosHTDK;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAudiosMagal2019HT;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAudiosMagal2019HTDK;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAudiosMixedWolofal;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAudiosRadiass;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAudiosSerigneMbayeDiakhate;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAudiosSerigneMoussaKa;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAudiosZikr;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutProgressBar;


public class AudioFragment extends Fragment implements View.OnClickListener, Playable {

    private static final String TAG = "AudioFragment";
    private View view;

    //************************MediaPlayer********************************************
    private SongAdapter mAdapter;
    private RecyclerView recycler;
    private int currentIndex = 0;
    private Toolbar toolbar, toolbar_bottom;
    private TextView tb_title, tv_empty, tv_duration;
    private ImageView iv_play, iv_next, iv_previous;
    private ProgressBar pb_loader, pb_main_loader;
    private long currentSongLength;
    private FloatingActionButton fab_search;
    private double startTime = 0;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private TextView tv_time;
    private SeekBar seekBar;
    private boolean firstLaunch = true;
    private Handler myHandler;
    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            //seekBar.setMax(mediaPlayer.getDuration());
            if (mediaPlayer != null) {
                try {
                    startTime = mediaPlayer.getCurrentPosition();
                    tv_time.setText(convertDuration(mediaPlayer.getCurrentPosition()));
                    seekBar.setProgress((int) startTime);
                    myHandler.postDelayed(this, 500);
                } catch (Exception ignored) {
                }
            }
        }
    };

    //************* Notification Music ********************
    private NotificationManager notificationManager;
    private List<Song> listTracks = new ArrayList<>();
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = Objects.requireNonNull(intent.getExtras()).getString("actionname");
            if (action != null) {
                switch (action) {
                    case CreateNotificationMusic.ACTION_PREVIUOS:
                        //toastMessage(getContext(), "CreateNotificationMusic.ACTION_PREVIUOS");
                        pushPrevious();
                        break;
                    case CreateNotificationMusic.ACTION_PLAY:
                        //toastMessage(getContext(), "CreateNotificationMusic.ACTION_PLAY");
                        pushPlay();
                        break;
                    case CreateNotificationMusic.ACTION_NEXT:
                        //toastMessage(getContext(), "CreateNotificationMusic.ACTION_NEXT");
                        pushNext();
                        break;
                }
            }
        }
    };

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_audio, container, false);

        initViewsMainKhassida();

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.iv_khassaide:
                chooseKhassaide();
                break;

            case R.id.iv_mixed:
                chooseWolofal();
                break;

            case R.id.iv_zikr:
                setLayoutMedia();
                if (listAudiosZikr == null)
                    listAudiosZikr = new ArrayList<>();
                MyStaticFunctions.getListAudios(getContext(), listAudiosZikr, "audios", "zikr");
                break;

            case R.id.button_back:
                setLayoutMainKhassida();
                break;
        }
    }

    private void setLayoutMedia() {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.view = inflater.inflate(R.layout.layout_media, null);
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(this.view);
        initViewsMedia();

        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        if (myHandler == null)
            myHandler = new Handler();
    }

    private void setLayoutMainKhassida() {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.view = inflater.inflate(R.layout.fragment_audio, null);
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(this.view);
        initViewsMainKhassida();

        if (myHandler != null) {
            myHandler.removeCallbacks(UpdateSongTime);
        }

        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViewsMainKhassida() {
        view.findViewById(R.id.iv_khassaide).setOnClickListener(this);
        view.findViewById(R.id.iv_mixed).setOnClickListener(this);
        view.findViewById(R.id.iv_zikr).setOnClickListener(this);
    }

    private void initViewsMedia() {
        toolbar_bottom = view.findViewById(R.id.bottom_toolbar);
        tb_title = view.findViewById(R.id.tb_title);
        tv_duration = view.findViewById(R.id.tv_duration);
        tv_empty = view.findViewById(R.id.tv_empty);
        iv_play = view.findViewById(R.id.iv_play);
        iv_next = view.findViewById(R.id.iv_next);
        iv_previous = view.findViewById(R.id.iv_previous);
        pb_loader = view.findViewById(R.id.pb_loader);
        pb_main_loader = view.findViewById(R.id.pb_main_loader);
        recycler = view.findViewById(R.id.recyclerView_quran);
        seekBar = view.findViewById(R.id.seekbar);
        tv_time = view.findViewById(R.id.tv_time);
        fab_search = view.findViewById(R.id.fab_search);
        recycler = view.findViewById(R.id.recyclerView_quran);
        view.findViewById(R.id.button_back).setOnClickListener(this);

        if (myHandler == null)
            myHandler = new Handler();
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();

        initViewsProgressBar();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
            Objects.requireNonNull(getContext()).registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
            getContext().startService(new Intent(getContext(), OnClearFromRecentService.class));
        }
    }

    public void initViewsProgressBar() {
        relativeLayoutData = view.findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = view.findViewById(R.id.relativeLayout_progressBar);
        progressBar = view.findViewById(R.id.progressBar);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            setLayoutMainKhassida();
        }
    }

    private void chooseKourelMagal2019() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_magal2019, null);
        dialogBuilder.setView(dialogView);

        Button buttonHT = dialogView.findViewById(R.id.button_ht);
        Button buttonHTDKH = dialogView.findViewById(R.id.button_htdkh);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonHT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLayoutMedia();
                if (listAudiosMagal2019HT == null)
                    listAudiosMagal2019HT = new ArrayList<>();
                getListAudios(getContext(), listAudiosMagal2019HT, "magal2019HT");
                alertDialog.dismiss();
            }
        });

        buttonHTDKH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLayoutMedia();
                if (listAudiosMagal2019HTDK == null)
                    listAudiosMagal2019HTDK = new ArrayList<>();
                getListAudios(getContext(), listAudiosMagal2019HTDK, "magal2019HTDKH");
                alertDialog.dismiss();
            }
        });
    }

    private void chooseKhassaide() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_audio_khassida, null);
        dialogBuilder.setView(dialogView);

        Button buttonMagal2019 = dialogView.findViewById(R.id.button_magal2019);
        Button buttonHT = dialogView.findViewById(R.id.button_ht);
        Button buttonHTDKH = dialogView.findViewById(R.id.button_htdkh);
        Button buttonAhlouMinane = dialogView.findViewById(R.id.button_am);
        Button buttonRadiass = dialogView.findViewById(R.id.button_radiass);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonMagal2019.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseKourelMagal2019();
                alertDialog.dismiss();
            }
        });

        buttonHT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLayoutMedia();
                if (listAudiosHT == null)
                    listAudiosHT = new ArrayList<>();
                getListAudios(getContext(), listAudiosHT, "ht");
                alertDialog.dismiss();
            }
        });

        buttonHTDKH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLayoutMedia();
                if (listAudiosHTDK == null)
                    listAudiosHTDK = new ArrayList<>();
                getListAudios(getContext(), listAudiosHTDK, "htdk");
                alertDialog.dismiss();
            }
        });

        buttonAhlouMinane.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLayoutMedia();
                if (listAudiosAM == null)
                    listAudiosAM = new ArrayList<>();
                getListAudios(getContext(), listAudiosAM, "am");
                alertDialog.dismiss();
            }
        });

        buttonRadiass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLayoutMedia();
                if (listAudiosRadiass == null)
                    listAudiosRadiass = new ArrayList<>();
                getListAudios(getContext(), listAudiosRadiass, "moustaphaGningue");
                alertDialog.dismiss();
            }
        });
    }

    private void chooseWolofal() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_audio_wolofal, null);
        dialogBuilder.setView(dialogView);

        Button buttonSerigneMoussaKa = dialogView.findViewById(R.id.button_serigne_moussa_ka);
        Button buttonSerigneMbayeDiakhate = dialogView.findViewById(R.id.button_serigne_mbaye_diakhate);
        Button buttonMixedWolofal = dialogView.findViewById(R.id.button_mixed_wolofal);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonSerigneMoussaKa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLayoutMedia();
                if (listAudiosSerigneMoussaKa == null)
                    listAudiosSerigneMoussaKa = new ArrayList<>();
                setLayoutMedia();
                MyStaticFunctions.getListAudios(getContext(), listAudiosSerigneMoussaKa, "audios", "serigneMoussaKa");
                alertDialog.dismiss();
            }
        });

        buttonSerigneMbayeDiakhate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLayoutMedia();
                if (listAudiosSerigneMbayeDiakhate == null)
                    listAudiosSerigneMbayeDiakhate = new ArrayList<>();
                MyStaticFunctions.getListAudios(getContext(), listAudiosSerigneMbayeDiakhate, "audios", "serigneMbayeDiakhate");
                alertDialog.dismiss();
            }
        });

        buttonMixedWolofal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLayoutMedia();
                if (listAudiosMixedWolofal == null)
                    listAudiosMixedWolofal = new ArrayList<>();
                MyStaticFunctions.getListAudios(getContext(), listAudiosMixedWolofal, "audios", "mixedWolofal");
                alertDialog.dismiss();
            }
        });
    }

    //*********************************** SET ADAPTER *************************************

    private void getListAudios(final Context context, final List<Song> listSong, String documentID) {
        //Retrieve all songs from FirebaseFirestore
        if (listTracks == null || listTracks.size() > 0) {
            listTracks = new ArrayList<>();
        }
        if (listSong.isEmpty()) {
            showProgressBar();
            MyStaticVariables.collectionReference = MyStaticVariables.firestore.collection("audios");
            MyStaticVariables.collectionReference.whereEqualTo("documentID", documentID).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            hideProgressBar();
                            ListSongObject listSongObject = null;
                            if (!queryDocumentSnapshots.isEmpty()) {
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot documentSnapshot : list) {
                                    listSongObject = documentSnapshot.toObject(ListSongObject.class);
                                    listSong.addAll(listSongObject.getListSong());
                                    break;
                                }
                                Collections.sort(listSong);
                                listTracks.addAll(listSong);
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
            listTracks.addAll(listSong);
            setMyAdapter(context, listTracks);
        }
    }

    public void handleSeekbar() {
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

    public void prepareSong(Context context, Song song) {

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

    public void setMyAdapter(final Context context, final List<Song> listSong) {

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

    public void changeSelectedSong(int index) {
        mAdapter.notifyItemChanged(mAdapter.getSelectedPosition());
        currentIndex = index;
        mAdapter.setSelectedPosition(currentIndex);
        mAdapter.notifyItemChanged(currentIndex);
    }

    public void togglePlay(Context context, MediaPlayer mp) {
        seekBar.setMax(mediaPlayer.getDuration());
        if (mp != null && mp.isPlaying()) {
            mp.stop();
            mp.reset();
            onTrackPause();
        } else {
            pb_loader.setVisibility(View.GONE);
            tb_title.setVisibility(View.GONE);
            if (mp != null) {
                mp.start();
                onTrackPlay();
            }
            iv_play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.selector_pause));

            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            myHandler.postDelayed(UpdateSongTime, 100);
        }
    }

    public void pushPlay(final Context context, final List<Song> listSong) {
        iv_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    iv_play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.selector_play));
                    mediaPlayer.pause();
                    onTrackPause();
                } else {
                    if (firstLaunch) {
                        Song song = listSong.get(0);
                        changeSelectedSong(0);
                        prepareSong(context, song);
                    } else {
                        if (mediaPlayer != null) {
                            mediaPlayer.start();
                        }
                        firstLaunch = false;
                    }
                    iv_play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.selector_pause));
                    onTrackPlay();
                }
            }
        });
    }

    public void pushPrevious(final Context context, final List<Song> listSong) {
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

    public void searchSong(final Context context, final List<Song> listSong) {

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

    public void downloadFile(Context context, String fileName, String url) {
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

    public void pushNext(final Context context, final List<Song> listSong) {
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
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    onTrackPlay();
                } else {
                    onTrackPause();
                }
            }
        });
    }

    //******************************** Notification Music ***********************************
    @Override
    public void onDestroy() {
        super.onDestroy();

        //**********Notification Music********
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.cancelAll();
        }

        getContext().unregisterReceiver(broadcastReceiver);
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CreateNotificationMusic.CHANNEL_ID,
                    "KOD Dev", NotificationManager.IMPORTANCE_LOW);

            notificationManager = getContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onTrackPrevious() {
        CreateNotificationMusic.createNotification(getContext(), listTracks.get(currentIndex),
                R.drawable.ic_pause_black_24dp, currentIndex, listTracks.size() - 1);

    }

    @Override
    public void onTrackPlay() {
        CreateNotificationMusic.createNotification(getContext(), listTracks.get(currentIndex),
                R.drawable.ic_pause_black_24dp, currentIndex, listTracks.size() - 1);
    }

    @Override
    public void onTrackPause() {

        CreateNotificationMusic.createNotification(getContext(), listTracks.get(currentIndex),
                R.drawable.ic_play_arrow_black_24dp, currentIndex, listTracks.size() - 1);
    }

    @Override
    public void onTrackNext() {

        CreateNotificationMusic.createNotification(getContext(), listTracks.get(currentIndex),
                R.drawable.ic_pause_black_24dp, currentIndex, listTracks.size() - 1);

    }

    public void pushNext() {
        if (mediaPlayer != null) {
            if (currentIndex + 1 < listTracks.size()) {
                Song next = listTracks.get(currentIndex + 1);
                changeSelectedSong(currentIndex + 1);
                prepareSong(getContext(), next);
            } else {
                changeSelectedSong(0);
                prepareSong(getContext(), listTracks.get(0));
            }
            onTrackNext();
        }
    }

    public void pushPrevious() {
        if (mediaPlayer != null) {

            if (currentIndex - 1 >= 0) {
                Song previous = listTracks.get(currentIndex - 1);
                changeSelectedSong(currentIndex - 1);
                prepareSong(getContext(), previous);
            } else {
                changeSelectedSong(listTracks.size() - 1);
                prepareSong(getContext(), listTracks.get(listTracks.size() - 1));
            }
        }
        onTrackPrevious();
    }

    public void pushPlay() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            iv_play.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.selector_play));
            mediaPlayer.pause();
            onTrackPause();
        } else {
            if (firstLaunch) {
                Song song = listTracks.get(0);
                changeSelectedSong(0);
                prepareSong(getContext(), song);
            } else {
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
                firstLaunch = false;
            }
            iv_play.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.selector_pause));
            onTrackPlay();
        }
    }
}

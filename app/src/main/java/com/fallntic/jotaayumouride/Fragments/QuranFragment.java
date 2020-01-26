package com.fallntic.jotaayumouride.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fallntic.jotaayumouride.Model.ListSongObject;
import com.fallntic.jotaayumouride.Model.Song;
import com.fallntic.jotaayumouride.R;
import com.fallntic.jotaayumouride.Services.OnClearFromRecentService;
import com.fallntic.jotaayumouride.Utility.MyStaticVariables;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.createChannel;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.setMyAdapter;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.stopCurrentPlayingMediaPlayer;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.broadcastReceiver;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.fab_search;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.iv_next;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.iv_play;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.iv_previous;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAudiosQuran;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listTracks;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.mediaPlayer;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.myHandler;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.notificationManager;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.pb_loader;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.pb_main_loader;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.recycler;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.seekBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.tb_title;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.toolbar_bottom;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.tv_duration;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.tv_empty;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.tv_time;


public class QuranFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "PDFFragment";
    private View view;

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_quran, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initViewsMainQuran();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(getContext());
            Objects.requireNonNull(getContext()).registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
            getContext().startService(new Intent(getContext(), OnClearFromRecentService.class));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_sudais:
                if (listAudiosQuran == null)
                    listAudiosQuran = new ArrayList<>();
                setLayoutMedia();
                getListAudios(getContext(), listAudiosQuran, "sudais");
                break;

            case R.id.button_back:
                setLayoutMainQuran();
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

        stopCurrentPlayingMediaPlayer();
    }

    private void setLayoutMainQuran() {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.view = inflater.inflate(R.layout.fragment_quran, null);
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(this.view);
        initViewsMainQuran();
    }

    private void initViewsMainQuran() {
        view.findViewById(R.id.ib_sudais).setOnClickListener(this);
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

    private void initViewsProgressBar() {
        relativeLayoutData = view.findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = view.findViewById(R.id.relativeLayout_progressBar);
        progressBar = view.findViewById(R.id.progressBar);
    }

    //*********************************** SET ADAPTER *************************************

    private void getListAudios(final Context context, final List<Song> listSong, String documentID) {
        //Retrieve all songs from FirebaseFirestore
        if (listTracks == null || listTracks.size() > 0) {
            listTracks = new ArrayList<>();
        }
        if (listSong.isEmpty()) {
            showProgressBar();
            MyStaticVariables.collectionReference = MyStaticVariables.firestore.collection("quran");
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

    //******************************** Notification Music ***********************************
    @Override
    public void onDestroy() {
        super.onDestroy();

        //**********Notification Music********
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.cancelAll();
        }

        if (broadcastReceiver != null)
            Objects.requireNonNull(getContext()).unregisterReceiver(broadcastReceiver);
    }
}

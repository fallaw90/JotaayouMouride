package com.fallntic.jotaayumouride;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.fallntic.jotaayumouride.Model.ListSongObject;
import com.fallntic.jotaayumouride.Model.Song;
import com.fallntic.jotaayumouride.Utility.MyStaticFunctions;
import com.fallntic.jotaayumouride.Utility.MyStaticVariables;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.fallntic.jotaayumouride.Utility.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.setMyAdapter;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.UpdateSongTime;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.fab_search;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.iv_next;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.iv_play;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.iv_previous;
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
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.mediaPlayer;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.myHandler;
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


public class AudioFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "AudioFragment";
    private View view;

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
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

    private void getListAudios(final Context context, final List<Song> listSong, String documentID) {
        //Retrieve all songs from FirebaseFirestore
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
}

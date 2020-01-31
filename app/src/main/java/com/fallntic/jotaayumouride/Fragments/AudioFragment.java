package com.fallntic.jotaayumouride.fragments;

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
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.fallntic.jotaayumouride.R;
import com.fallntic.jotaayumouride.services.OnClearFromRecentService;

import java.util.ArrayList;
import java.util.Objects;

import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.createChannel;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.getListAudios;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.stopCurrentPlayingMediaPlayer;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.broadcastReceiverMediaPlayer;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.fab_search;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.isTabAudioOpened;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.iv_next;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.iv_play;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.iv_previous;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAudiosAM;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAudiosHT;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAudiosHTDK;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAudiosMagal2019HT;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAudiosMagal2019HTDK;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAudiosMixedWolofal;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAudiosQuran;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAudiosRadiass;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAudiosSerigneMbayeDiakhate;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAudiosSerigneMoussaKa;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAudiosZikr;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listTracks;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.mediaPlayer;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.myHandler;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.pb_loader;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.pb_main_loader;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.recycler;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.seekBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.songChosen;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.tb_title;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.toolbar_bottom;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.tv_duration;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.tv_empty;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.tv_time;


@SuppressWarnings("ALL")
public class AudioFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "AudioFragment";
    private View view;

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
                songChosen = "zikr";
                getListAudios(getContext(), listAudiosZikr, "zikr");
                break;

            case R.id.button_back:
                setMainInitialFragmentLayout();
                break;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            if (listTracks != null && listAudiosQuran != null && songChosen != null) {
                if (listTracks.size() == listAudiosQuran.size() && isTabAudioOpened) {
                    stopCurrentPlayingMediaPlayer();
                    setLayoutMedia();
                    refreshPlayList();
                }
            }
        }
    }

    private void refreshPlayList() {
        switch (songChosen) {
            case "zikr":
                getListAudios(getContext(), listAudiosZikr, "zikr");
                break;
            case "magal2019HT":
                getListAudios(getContext(), listAudiosMagal2019HT, "magal2019HT");
                break;
            case "magal2019HTDKH":
                getListAudios(getContext(), listAudiosMagal2019HTDK, "magal2019HTDKH");
                break;
            case "ht":
                getListAudios(getContext(), listAudiosHT, "ht");
                break;
            case "htdk":
                getListAudios(getContext(), listAudiosHTDK, "htdk");
                break;
            case "ahlouMinan":
                getListAudios(getContext(), listAudiosAM, "ahlouMinan");
                break;
            case "moustaphaGningue":
                getListAudios(getContext(), listAudiosRadiass, "moustaphaGningue");
                break;
            case "serigneMoussaKa":
                getListAudios(getContext(), listAudiosSerigneMoussaKa, "serigneMoussaKa");
                break;
            case "serigneMbayeDiakhate":
                getListAudios(getContext(), listAudiosSerigneMbayeDiakhate, "serigneMbayeDiakhate");
                break;
            case "mixedWolofal":
                getListAudios(getContext(), listAudiosMixedWolofal, "mixedWolofal");
                break;
            default:
                setMainInitialFragmentLayout();
                break;
        }
    }

    private void setLayoutMedia() {
        LayoutInflater inflater = (LayoutInflater) Objects.requireNonNull(getActivity()).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            this.view = inflater.inflate(R.layout.layout_media, null);
        }
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(this.view);
        initViewsMedia();
        stopCurrentPlayingMediaPlayer();
        isTabAudioOpened = true;
    }

    private void setMainInitialFragmentLayout() {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.view = inflater.inflate(R.layout.fragment_audio, null);
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(this.view);
        initViewsMainKhassida();
        songChosen = null;
        isTabAudioOpened = false;
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
            createChannel(getContext());
            Objects.requireNonNull(getContext()).registerReceiver(broadcastReceiverMediaPlayer, new IntentFilter("TRACKS_TRACKS"));
            getContext().startService(new Intent(getContext(), OnClearFromRecentService.class));
        }


    }

    public void initViewsProgressBar() {
        relativeLayoutData = view.findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = view.findViewById(R.id.relativeLayout_progressBar);
        progressBar = view.findViewById(R.id.progressBar);
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
                songChosen = "magal2019HT";
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
                songChosen = "magal2019HTDKH";
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
                songChosen = "ht";
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
                songChosen = "htdk";
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
                songChosen = "ahlouMinan";
                getListAudios(getContext(), listAudiosAM, "ahlouMinan");
                alertDialog.dismiss();
            }
        });

        buttonRadiass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLayoutMedia();
                if (listAudiosRadiass == null)
                    listAudiosRadiass = new ArrayList<>();
                songChosen = "moustaphaGningue";
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
                songChosen = "serigneMoussaKa";
                getListAudios(getContext(), listAudiosSerigneMoussaKa, "serigneMoussaKa");
                alertDialog.dismiss();
            }
        });

        buttonSerigneMbayeDiakhate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLayoutMedia();
                if (listAudiosSerigneMbayeDiakhate == null)
                    listAudiosSerigneMbayeDiakhate = new ArrayList<>();
                songChosen = "serigneMbayeDiakhate";
                getListAudios(getContext(), listAudiosSerigneMbayeDiakhate, "serigneMbayeDiakhate");
                alertDialog.dismiss();
            }
        });

        buttonMixedWolofal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLayoutMedia();
                if (listAudiosMixedWolofal == null)
                    listAudiosMixedWolofal = new ArrayList<>();
                songChosen = "mixedWolofal";
                getListAudios(getContext(), listAudiosMixedWolofal, "mixedWolofal");
                alertDialog.dismiss();
            }
        });
    }
}
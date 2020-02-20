package com.fallntic.jotaayumouride.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.fallntic.jotaayumouride.R;
import com.fallntic.jotaayumouride.adapter.SongCategory;
import com.fallntic.jotaayumouride.adapter.ViewHolderSongCategory;
import com.fallntic.jotaayumouride.model.GridViewSongCategory;
import com.fallntic.jotaayumouride.services.OnClearFromRecentService;

import java.util.ArrayList;
import java.util.Objects;

import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.createChannel;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.getListAudios;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.stopCurrentPlayingMediaPlayer;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.broadcastReceiverMediaPlayer;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.fab_search;
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
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.tv_empty;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.tv_time;


@SuppressWarnings("ALL")
public class AudioFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "AudioFragment";
    private View view;
    private static GridView gridView;
    private Button buttonBack;

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_audio, container, false);

        gridView = view.findViewById(R.id.gridview);
        gridView.setAdapter(new SongCategory(getActivity()));
        gridView.setOnItemClickListener(this);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createChannel(getContext());
                Objects.requireNonNull(getContext()).registerReceiver(broadcastReceiverMediaPlayer, new IntentFilter("TRACKS_TRACKS"));
                getContext().startService(new Intent(getContext(), OnClearFromRecentService.class));
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("LOG_TAG", "prepare() failed");
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkInternetConnection(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        checkInternetConnection(getActivity());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ViewHolderSongCategory holder = (ViewHolderSongCategory) view.getTag();
        GridViewSongCategory gridViewSongCategory = (GridViewSongCategory) holder.imageViewSongCategory.getTag();

        switch (holder.textViewSongCategory.getText().toString()) {
            case "Khassaïde":
                chooseKhassaide();
                break;

            case "Wolofal":
                chooseWolofal();
                break;

            case "Zikr":
                setLayoutMedia();
                if (listAudiosZikr == null)
                    listAudiosZikr = new ArrayList<>();
                songChosen = "zikr";
                getListAudios(getContext(), listAudiosZikr, "audios", "zikr");
                break;

            case "Coran":
                chooseCoran();
                break;
        }
    }

    private void refreshPlayList() {
        switch (songChosen) {
            case "zikr":
                getListAudios(getContext(), listAudiosZikr, "audios", "zikr");
                break;
            case "magal2019HT":
                getListAudios(getContext(), listAudiosMagal2019HT, "audios", "magal2019HT");
                break;
            case "magal2019HTDKH":
                getListAudios(getContext(), listAudiosMagal2019HTDK, "audios", "magal2019HTDKH");
                break;
            case "ht":
                getListAudios(getContext(), listAudiosHT, "audios", "ht");
                break;
            case "htdk":
                getListAudios(getContext(), listAudiosHTDK, "audios", "htdk");
                break;
            case "ahlouMinan":
                getListAudios(getContext(), listAudiosAM, "audios", "ahlouMinan");
                break;
            case "moustaphaGningue":
                getListAudios(getContext(), listAudiosRadiass, "audios", "moustaphaGningue");
                break;
            case "serigneMoussaKa":
                getListAudios(getContext(), listAudiosSerigneMoussaKa, "audios", "serigneMoussaKa");
                break;
            case "serigneMbayeDiakhate":
                getListAudios(getContext(), listAudiosSerigneMbayeDiakhate, "audios", "serigneMbayeDiakhate");
                break;
            case "mixedWolofal":
                getListAudios(getContext(), listAudiosMixedWolofal, "audios", "mixedWolofal");
                break;
            case "sudais":
                getListAudios(getContext(), listAudiosMixedWolofal, "quran", "sudais");
                break;
            default:
                setMainAudiolFragmentLayout();
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
        initViewsMedia(this.view);
        stopCurrentPlayingMediaPlayer();

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMainAudiolFragmentLayout();
            }
        });
    }

    private void setMainAudiolFragmentLayout() {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        this.view = inflater.inflate(R.layout.fragment_audio, null);
        rootView.addView(this.view);

        gridView = view.findViewById(R.id.gridview);
        gridView.setAdapter(new SongCategory(getActivity()));
        gridView.setOnItemClickListener(this);

    }

    private void initViewsMedia(View view) {
        toolbar_bottom = view.findViewById(R.id.bottom_toolbar);
        tb_title = view.findViewById(R.id.tb_title);
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
        buttonBack = view.findViewById(R.id.button_back);

        if (myHandler == null)
            myHandler = new Handler();
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();

        initViewsProgressBar();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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
                getListAudios(getContext(), listAudiosMagal2019HT, "audios", "magal2019HT");
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
                getListAudios(getContext(), listAudiosMagal2019HTDK, "audios", "magal2019HTDKH");
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
                getListAudios(getContext(), listAudiosHT, "audios", "ht");
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
                getListAudios(getContext(), listAudiosHTDK, "audios", "htdk");
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
                getListAudios(getContext(), listAudiosAM, "audios", "ahlouMinan");
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
                getListAudios(getContext(), listAudiosRadiass, "audios", "moustaphaGningue");
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
                getListAudios(getContext(), listAudiosSerigneMoussaKa, "audios", "serigneMoussaKa");
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
                getListAudios(getContext(), listAudiosSerigneMbayeDiakhate, "audios", "serigneMbayeDiakhate");
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
                getListAudios(getContext(), listAudiosMixedWolofal, "audios", "mixedWolofal");
                alertDialog.dismiss();
            }
        });
    }

    private void chooseCoran() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_audio_coran, null);
        dialogBuilder.setView(dialogView);

        Button buttonSudais = dialogView.findViewById(R.id.button_sudais);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonSudais.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLayoutMedia();
                if (listAudiosQuran == null)
                    listAudiosQuran = new ArrayList<>();
                setLayoutMedia();
                songChosen = "sudais";
                getListAudios(getContext(), listAudiosQuran, "quran", "sudais");
                alertDialog.dismiss();
            }
        });
    }
}
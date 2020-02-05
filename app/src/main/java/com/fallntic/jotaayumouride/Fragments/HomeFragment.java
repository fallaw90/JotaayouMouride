package com.fallntic.jotaayumouride.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.R;
import com.fallntic.jotaayumouride.adapter.VideoAdapter;
import com.fallntic.jotaayumouride.model.YouTubeVideos;
import com.fallntic.jotaayumouride.utility.MyStaticVariables;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

import static android.app.Activity.RESULT_OK;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showImage;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.firebaseAuth;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.firebaseStorage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.mediaPlayer;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.storageReference;

public class HomeFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 375;
    private static final int PICK_AUDIO_REQUEST = 254;
    public static String imageUriYobalouBessBi, uriBayitDuJour, uriAudio, fileName, audioDuration, uriPrayerTime, titlePrayerTime;
    public static String descriptionVideoDeLaSemaine, titleVideoDeLaSemaine, codeLinkVideoDeLaSemaine;
    public static Uri fileURI;
    //Youtube
    private final Vector<YouTubeVideos> youtubeVideos = new Vector<>();
    public SeekBar seekBar;
    public Handler myHandler = new Handler();
    private View view;
    private FirebaseFirestore firestore;
    private ImageView imageViewBayitDuJour, imageViewYobalouBessBi, imageViewPrayerTime;
    private Button buttonImageBayitDuJour, buttonAudioYobalouBessBi;
    private UploadTask uploadTask;
    private boolean isButtonClicked, isFileSelected;
    private String choice = "";
    private TextView textViewTitleVideoDeLaSemaine, textViewDescriptionVideoDeLaSemaine, textViewTitlePrayerTime;
    private RecyclerView recyclerViewerVideoDeLaSemaine;
    //****************************
    private RelativeLayout relativeLayoutData, relativeLayoutProgressBar;
    private ProgressBar progressBar;
    private RelativeLayout relativeLayoutYobalouBessBi, relativeLayoutPubYobalouBessBi;
    private ImageView imageViewPlay;
    private TextView textViewSongName;
    private TextView textViewDuration;
    private TextView textViewDate, textViewSelectedFile;
    private boolean isPlaying;
    private MediaPlayer mPlayer;
    public final Runnable UpdateSongTime = new Runnable() {
        public void run() {
            //seekBar.setMax(mediaPlayer.getDuration());
            if (mPlayer != null) {
                try {
                    double startTime = mPlayer.getCurrentPosition();
                    seekBar.setProgress((int) startTime);
                    myHandler.postDelayed(this, 500);
                } catch (Exception ignored) {
                }

            }
        }
    };

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);

        buttonImageBayitDuJour = view.findViewById(R.id.button_upload_bayit_du_jour);
        textViewDescriptionVideoDeLaSemaine = view.findViewById(R.id.textView_description_video_semaine);
        imageViewBayitDuJour = view.findViewById(R.id.imageView_bayit_du_jour);
        textViewTitleVideoDeLaSemaine = view.findViewById(R.id.textView_title_video_semaine);
        recyclerViewerVideoDeLaSemaine = view.findViewById(R.id.recyclerView_videoDeLaSemaine);
        imageViewPlay = view.findViewById(R.id.imageViewPlay);
        textViewTitlePrayerTime = view.findViewById(R.id.textView_prayer_time);
        imageViewPrayerTime = view.findViewById(R.id.imageView_prayer_time);
        imageViewYobalouBessBi = view.findViewById(R.id.imageView_audio);
        seekBar = view.findViewById(R.id.seekBar);
        textViewSongName = view.findViewById(R.id.textViewRecordingname);
        textViewDuration = view.findViewById(R.id.textView_duration);
        textViewDate = view.findViewById(R.id.textView_date);
        textViewSelectedFile = view.findViewById(R.id.textView_selectedFile);
        buttonAudioYobalouBessBi = view.findViewById(R.id.button_audio_yobalou_bess_bi);
        relativeLayoutPubYobalouBessBi = view.findViewById(R.id.rel_pub_yobalou_bess_bi);
        relativeLayoutYobalouBessBi = view.findViewById(R.id.rel_yobalou_bess_bi);

        initViewsProgressBar(view);

        if (youtubeVideos.size() > 0)
            youtubeVideos.clear();
        youtubeVideos.add(new YouTubeVideos("<iframe scrolling=\"no\" width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/" + codeLinkVideoDeLaSemaine + "\" frameborder=\"1\"  allowfullscreen></iframe>"));


        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        getVideoDeLaSemaine();
        getPrayerTime();
        getBayitDuJour();
        getYobalouBessBi();

        return view;
    }

    private void initViewsProgressBar(View view) {
        relativeLayoutData = view.findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = view.findViewById(R.id.relativeLayout_progressBar);
        progressBar = view.findViewById(R.id.progressBar);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        buttonImageBayitDuJour.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if (!isButtonClicked) {
                    chooseImage();
                    isButtonClicked = true;
                    choice = "bayit_du_jour";
                } else if (choice.equals("bayit_du_jour")) {
                    if (isFileSelected) {
                        isFileSelected = false;
                        saveFile("bayit_du_jour", "Bayit du jour");
                        buttonImageBayitDuJour.setText("Choisir une image bayit du jour");
                    } else {
                        isButtonClicked = false;
                    }
                }
            }
        });

        buttonAudioYobalouBessBi.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if (!isButtonClicked) {
                    chooseAudio();
                    isButtonClicked = true;
                    choice = "song_yobalou_bess_bi";
                } else if (choice.equals("song_yobalou_bess_bi")) {
                    if (isFileSelected) {
                        isFileSelected = false;
                        saveFile("yobalou_bess_bi", "yobalou_bess_bi");
                        buttonAudioYobalouBessBi.setText("Ajouter un song yobalou bess bi");
                    } else {
                        isButtonClicked = false;
                        buttonAudioYobalouBessBi.setText("Enregistrer ce fichier");
                    }
                }
            }
        });

        imageViewYobalouBessBi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onlineUser != null && onlineUser.getUserPhoneNumber().equals("+13208030902")) {
                    choice = "image_yobalou_bess_bi";
                    chooseImage();
                }
            }
        });

        imageViewPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    stopPlaying();
                } else {
                    startPlaying(uriAudio);
                    Log.d("isPlayin", "False");
                }
            }
        });

        imageViewPrayerTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onlineUser != null && onlineUser.getUserPhoneNumber().equals("+13208030902")) {
                    choice = "prayer_time";
                    chooseImage();
                }
            }
        });

        handleSeekbar();
        //getYoutubeLink();
    }

    public void getPrayerTime() {
        if (uriPrayerTime == null) {
            showProgressBar();
            DocumentReference docRef = firestore.collection("images")
                    .document("prayer_time");
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    hideProgressBar();
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            uriPrayerTime = document.getString("imageURI");
                            titlePrayerTime = document.getString("title");
                            showPrayerTime();
                        }
                    } else {
                        Log.d("LOGGER", "get failed with ", task.getException());
                    }
                }
            });
        } else if (!uriBayitDuJour.equals("")) {
            showPrayerTime();
        }
    }

    public void getVideoDeLaSemaine() {
        if (codeLinkVideoDeLaSemaine == null) {
            showProgressBar();
            DocumentReference docRef = firestore.collection("videos")
                    .document("video_de_la_semaine");
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    hideProgressBar();
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            codeLinkVideoDeLaSemaine = document.getString("code");
                            titleVideoDeLaSemaine = document.getString("title");
                            descriptionVideoDeLaSemaine = document.getString("description");
                            showVideoDeLaSemaine();
                        }
                    } else {
                        Log.d("LOGGER", "get failed with ", task.getException());
                        toastMessage(getContext(), "Erreur chargement reessayez svp.");
                    }
                }
            });
        } else if (!codeLinkVideoDeLaSemaine.equals("")) {
            showVideoDeLaSemaine();
        }
    }

    public void getBayitDuJour() {
        if (uriBayitDuJour == null) {
            showProgressBar();
            DocumentReference docRef = firestore.collection("bayit_du_jour")
                    .document("bayit_du_jour");
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    hideProgressBar();
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            uriBayitDuJour = document.getString("imageURI");
                            showBayitDuJour();
                        }
                    } else {
                        Log.d("LOGGER", "get failed with ", task.getException());
                        toastMessage(getContext(), "Erreur chargement reessayez svp.");
                    }
                }
            });
        } else if (!uriBayitDuJour.equals("")) {
            showBayitDuJour();
        }
    }

    public void getYobalouBessBi() {
        if (uriAudio == null) {
            showProgressBar();
            DocumentReference docRef = firestore.collection("yobalou_bess_bi")
                    .document("yobalou_bess_bi");
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    hideProgressBar();
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            imageUriYobalouBessBi = document.getString("imageURI");
                            fileName = document.getString("title");
                            uriAudio = document.getString("audioURI");
                            audioDuration = document.getString("duration");
                            showYobalouBessBi();
                        }
                    } else {
                        Log.d("LOGGER", "get failed with ", task.getException());
                        toastMessage(getContext(), "Erreur chargement reessayez svp.");
                    }
                }
            });
        } else if (!imageUriYobalouBessBi.equals("")) {
            showYobalouBessBi();
        }
    }

    public void showProgressBar() {
        if (relativeLayoutData != null && relativeLayoutProgressBar != null) {
            relativeLayoutData.setVisibility(View.GONE);
            relativeLayoutProgressBar.setVisibility(View.VISIBLE);
        }
    }

    public void hideProgressBar() {
        if (relativeLayoutData != null && relativeLayoutProgressBar != null) {
            relativeLayoutData.setVisibility(View.VISIBLE);
            relativeLayoutProgressBar.setVisibility(View.GONE);
        }
    }

    public void showVideoDeLaSemaine() {
        if (codeLinkVideoDeLaSemaine != null && !codeLinkVideoDeLaSemaine.equals("")) {
            textViewTitleVideoDeLaSemaine.setText(titleVideoDeLaSemaine);
            textViewDescriptionVideoDeLaSemaine.setText(descriptionVideoDeLaSemaine);
            recyclerViewerVideoDeLaSemaine.setHasFixedSize(true);
            recyclerViewerVideoDeLaSemaine.setLayoutManager(new LinearLayoutManager(getContext()));
            VideoAdapter videoAdapter = new VideoAdapter(youtubeVideos);
            recyclerViewerVideoDeLaSemaine.setAdapter(videoAdapter);
        } else {
            textViewTitleVideoDeLaSemaine.setVisibility(View.GONE);
            recyclerViewerVideoDeLaSemaine.setVisibility(View.GONE);
        }
    }

    public void showPrayerTime() {
        if (uriPrayerTime != null && !uriPrayerTime.equals("")) {
            textViewTitlePrayerTime.setText(titlePrayerTime);
            showImage(getActivity(), uriPrayerTime, imageViewPrayerTime);
        } else {
            textViewTitlePrayerTime.setVisibility(View.GONE);
            imageViewPrayerTime.setVisibility(View.GONE);
        }
    }

    public void showYobalouBessBi() {
        if (onlineUser != null && firebaseAuth != null && firebaseAuth.getCurrentUser() != null && relativeLayoutYobalouBessBi != null) {
            if (onlineUser.getUserPhoneNumber().equals("+13208030902")) {
                relativeLayoutPubYobalouBessBi.setVisibility(View.VISIBLE);
            }
        }

        if (uriAudio != null && !uriAudio.equals("")) {
            textViewDate.setVisibility(View.GONE);
            textViewSongName.setText(fileName);
            textViewDuration.setText(audioDuration);
            showImage(getActivity(), imageUriYobalouBessBi, imageViewYobalouBessBi);
        } else {
            relativeLayoutYobalouBessBi.setVisibility(View.GONE);
        }
    }

    public void showBayitDuJour() {
        if (imageViewBayitDuJour != null && uriBayitDuJour != null && !uriBayitDuJour.equals("")) {
            showImage(getActivity(), uriBayitDuJour, imageViewBayitDuJour);
        } else {
            imageViewBayitDuJour.setVisibility(View.GONE);
        }

        if (onlineUser != null && firebaseAuth != null && firebaseAuth.getCurrentUser()
                != null && buttonImageBayitDuJour != null) {

            if (onlineUser.getUserPhoneNumber().equals("+13208030902")
                    || onlineUser.getUserPhoneNumber().equals("+1221769009029")
                    || onlineUser.getUserPhoneNumber().equals("+1221783896272")) {
                buttonImageBayitDuJour.setVisibility(View.VISIBLE);
            }
        }
    }

    private String getDurationFromMilli(int durationInMillis) {

        Date date = new Date(durationInMillis);
        SimpleDateFormat simpleDate = new SimpleDateFormat("mm:ss", Locale.getDefault());

        return simpleDate.format(date);
    }

    private int findSongDuration(Uri audioUri) {
        int timeInMilliSec;
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(getActivity(), audioUri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            timeInMilliSec = Integer.parseInt(time);
            retriever.release();
            return timeInMilliSec;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    public void chooseAudio() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.ACTION_GET_CONTENT, true);
        intent.setType("audio/*");
        startActivityForResult(intent, PICK_AUDIO_REQUEST);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && Objects.requireNonNull(data).getData() != null) {
            fileURI = data.getData();
            isFileSelected = true;
            if (choice.equals("image_yobalou_bess_bi")) {
                saveFile("yobalou_bess_bi", "image_yobalou_bess_bi");
                showImage(getActivity(), fileURI.toString(), imageViewYobalouBessBi);
            } else if (choice.equals("bayit_du_jour")) {
                showImage(getActivity(), fileURI.toString(), imageViewBayitDuJour);
                buttonImageBayitDuJour.setText("Enregistrer cette image");
            } else if (choice.equals("prayer_time")) {
                showImage(getActivity(), fileURI.toString(), imageViewPrayerTime);
                saveFile("images", "prayer_time");
            }
        }

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && Objects.requireNonNull(data).getData() != null) {
            fileURI = data.getData();
            fileName = getFileName(fileURI);
            textViewSelectedFile.setText(fileName);
            buttonAudioYobalouBessBi.setText("Enregistrer ce fichier");
            isFileSelected = true;
            int millis = findSongDuration(fileURI);
            audioDuration = getDurationFromMilli(millis);
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (Objects.requireNonNull(fileURI.getScheme()).equals("content")) {
            try (Cursor cursor = getActivity().getContentResolver().query(uri, null, null,
                    null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
            if (result == null) {
                result = uri.getPath();
                int cut = Objects.requireNonNull(result).lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result;
    }

    private void saveFile(final String folder, String imageName) {
        showProgressBar();

        final StorageReference fileToUpload;
        if (choice.equals("prayer_time")) {
            fileToUpload = storageReference.child(folder).child(imageName).child(imageName);
        } else {
            fileToUpload = storageReference.child(folder).child(imageName);
        }

        uploadTask = (UploadTask) fileToUpload.putFile(fileURI)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileToUpload.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                hideProgressBar();
                                if (uri != null) {
                                    if (choice.equals("bayit_du_jour")) {
                                        uriBayitDuJour = uri.toString();
                                        showImage(getActivity(), uriBayitDuJour, imageViewBayitDuJour);
                                        saveFileToFirebaseFirestore(getActivity(), "bayit_du_jour", "bayit_du_jour", uri.toString());
                                    } else if (choice.equals("yobalou_bess_bi")) {
                                        if (choice.equals("image_yobalou_bess_bi"))
                                            showImage(getActivity(), imageUriYobalouBessBi, imageViewYobalouBessBi);
                                        imageUriYobalouBessBi = uri.toString();
                                        saveFileToFirebaseFirestore(getActivity(), "yobalou_bess_bi", "yobalou_bess_bi", uri.toString());
                                    } else if (choice.equals("prayer_time")) {
                                        saveFileToFirebaseFirestore(getActivity(), "images", "prayer_time", uri.toString());
                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                hideProgressBar();
                            }
                        });
                    }
                });
    }

    public void saveFileToFirebaseFirestore(final Context context, String collecion, String document, final String uri) {
        showProgressBar();
        final Map<String, Object> mapUri = new HashMap<>();
        if (choice.equals("song_yobalou_bess_bi")) {
            uriAudio = uri;
            fileName = fileName.replace(".mp3", "");
            mapUri.put("uriAudio", uri);
            mapUri.put("duration", audioDuration);
            mapUri.put("title", fileName);
        } else
            mapUri.put("imageURI", uri);

        firestore.collection(collecion).document(document)
                .update(mapUri).addOnCompleteListener(new OnCompleteListener<Void>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hideProgressBar();
                toastMessage(context, "Fichier enregistre avec succes!");
                if (choice.equals("song_yobalou_bess_bi")) {
                    textViewDuration.setText(audioDuration);
                    textViewSongName.setText(fileName);
                    textViewSelectedFile.setText("Aucun fichier sélectionné");
                }
                isButtonClicked = false;
                isFileSelected = false;
                choice = "";
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressBar();
                toastMessage(context, "Erreur enregistrement image!");
            }
        });
    }

    //**************************************************************************////////*
    public void handleSeekbar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mPlayer != null && fromUser && isPlaying) {
                    try {
                        mPlayer.seekTo(progress);
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }

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

    private void stopPlaying() {
        try {
            mPlayer.stop();
            mPlayer.release();
            isPlaying = false;
            imageViewPlay.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_play));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (MyStaticVariables.isPlaying) {
            if (mediaPlayer != null) {
                mediaPlayer.start();
                MyStaticVariables.isPlaying = false;
            }
        }
    }

    private void startPlaying(String audioURI) {
        if (audioURI != null) {
            mPlayer = new MediaPlayer();

            try {
                if (mediaPlayer != null && !MyStaticVariables.isPlaying) {
                    mediaPlayer.pause();
                    MyStaticVariables.isPlaying = true;
                }
                mPlayer.setDataSource(audioURI);
                mPlayer.prepare();
                mPlayer.start();
                isPlaying = true;
                seekBar.setMax(mPlayer.getDuration());
                seekBar.setProgress(mPlayer.getCurrentPosition());
                myHandler.postDelayed(UpdateSongTime, 100);
                if (imageViewPlay != null)
                    imageViewPlay.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.selector_stop));
            } catch (IOException e) {
                Log.e("LOG_TAG", "prepare() failed");
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }

            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (imageViewPlay != null)
                        imageViewPlay.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_play));
                    if (MyStaticVariables.isPlaying) {
                        if (mediaPlayer != null) {
                            mediaPlayer.start();
                            MyStaticVariables.isPlaying = false;
                        }
                    }
                    isPlaying = false;
                }
            });

        } else {
            toastMessage(getActivity(), "Lien audio non disponible.");
        }
    }


}
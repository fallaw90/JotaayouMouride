package com.fallntic.jotaayumouride.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.HomeActivity;
import com.fallntic.jotaayumouride.R;
import com.fallntic.jotaayumouride.adapter.AdvertisementAdapter;
import com.fallntic.jotaayumouride.model.PubImage;
import com.fallntic.jotaayumouride.utility.MyStaticVariables;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.isConnected;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.onTrackPause;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.onTrackPlay;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.firebaseStorage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.firestore;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listPubImage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.storageReference;

public class PubFragment extends Fragment {

    private RecyclerView recyclerViewPubImage;
    public static String videoUri;
    private View view;
    private static int PICK_VIDEO_REQUEST = 258;
    private VideoView videoView;
    private Button butttonVideoPub;
    private UploadTask uploadTask;
    private TextView textViewSelectedFile;
    private boolean isFileSelected, isVideoPlaying;
    private MediaController mediaController;
    private RelativeLayout relativeLayout;
    private ScrollView scrollView;
    private FrameLayout frameLayout;
    private ImageView imageViewPlay;
    private boolean isVideoLaunched, mainMediaPlayerExist;

    public PubFragment() {
        // Required empty public constructor
    }

    public void getListPubImage(final Context context) {
        if (firestore != null && listPubImage == null) {
            listPubImage = new ArrayList<>();
            firestore.collection("advertisements")
                    .document("my_ads")
                    .collection("image_ads")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot documentSnapshot : list) {
                                    PubImage pubImage = documentSnapshot.toObject(PubImage.class);
                                    listPubImage.add(pubImage);
                                }
                                if (listPubImage.size() > 0)
                                    showListPubImage();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            toastMessage(context, "Error charging pubs!");
                        }
                    });
        } else if (listPubImage.size() > 0) {
            showListPubImage();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_pub, container, false);

        if (!isConnected(getContext())) {
            toastMessage(getContext(), "Verifier votre connexion SVP.");
            startActivity(new Intent(getContext(), HomeActivity.class));
        }

        recyclerViewPubImage = view.findViewById(R.id.recyclerview_pub);
        videoView = view.findViewById(R.id.video_view);
        butttonVideoPub = view.findViewById(R.id.button_video_pub);
        textViewSelectedFile = view.findViewById(R.id.textView_selectedFile);
        relativeLayout = view.findViewById(R.id.relativeLayout_data);
        scrollView = view.findViewById(R.id.myScrollView);
        frameLayout = view.findViewById(R.id.frameLayout);
        imageViewPlay = view.findViewById(R.id.imageViewPlay);

        mediaController = new MediaController(getActivity());

        initViewsProgressBar(view);

        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        if (onlineUser != null && onlineUser.getUserPhoneNumber().equals("+13208030902")) {
            butttonVideoPub.setVisibility(View.VISIBLE);
            textViewSelectedFile.setVisibility(View.VISIBLE);
        } else {
            butttonVideoPub.setVisibility(View.GONE);
            textViewSelectedFile.setVisibility(View.GONE);
        }

        butttonVideoPub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isFileSelected) {
                    chooseVideo();
                } else {
                    saveFile();
                }
            }
        });

        imageViewPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVideoPlaying) {

                    isVideoPlaying = false;
                    videoView.stopPlayback();
                    imageViewPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_play));

                    if (mainMediaPlayerExist) {
                        MyStaticVariables.mediaPlayer.start();
                        MyStaticVariables.isPlaying = true;
                        onTrackPlay(getActivity());
                    }
                } else {
                    if (MyStaticVariables.isPlaying) {
                        MyStaticVariables.mediaPlayer.pause();
                        MyStaticVariables.isPlaying = false;
                        mainMediaPlayerExist = true;
                        onTrackPause(getActivity());
                    }
                    playVideo();
                    imageViewPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.selector_stop));
                }
            }
        });

        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaController.hide();
                if (isVideoPlaying) {
                    imageViewPlay.setVisibility(View.VISIBLE);
                    imageViewPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.selector_stop));
                } else {
                    imageViewPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_play));
                }
            }
        });

        frameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mediaController.hide();
                if (isVideoPlaying) {
                    imageViewPlay.setVisibility(View.VISIBLE);
                    imageViewPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.selector_stop));
                } else {
                    imageViewPlay.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_play));
                }
                return false;
            }
        });

        getListPubImage(getContext());
        getVideoPub();

        scrollView.setFocusableInTouchMode(true);
        scrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);

        return view;
    }

    private void chooseVideo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.ACTION_GET_CONTENT, true);
        intent.setType("video/*");
        startActivityForResult(Intent.createChooser(intent, "Select a video"), PICK_VIDEO_REQUEST);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && Objects.requireNonNull(data).getData() != null) {
            videoUri = data.getData().toString();
            isFileSelected = true;
            textViewSelectedFile.setText(getFileName(Uri.parse(videoUri)));
            butttonVideoPub.setText("Enregistrer cet fichier");
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void playVideo() {
        try {
            if (videoView != null && videoUri != null && !videoUri.equals("")) {
                videoView.setVideoURI(Uri.parse(videoUri));
                mediaController.setAnchorView(videoView);
                videoView.setMediaController(mediaController);
                mediaController.hide();
                videoView.requestFocus();
                videoView.start();
                isVideoPlaying = true;
                isVideoLaunched = true;

                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setLooping(true);
                    }
                });
            } else {
                isVideoPlaying = false;
                videoView.setVisibility(View.GONE);
                imageViewPlay.setVisibility(View.GONE);
            }
        } catch (IllegalStateException e) {
            System.out.println("IllegalStateException:  " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (Objects.requireNonNull(Uri.parse(videoUri).getScheme()).equals("content")) {
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

    private void saveFile() {
        showProgressBar();
        final StorageReference fileToUpload = storageReference.child("videos").child("video_pub").child("video_pub");

        uploadTask = (UploadTask) fileToUpload.putFile(Uri.parse(videoUri))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileToUpload.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                hideProgressBar();
                                if (uri != null) {
                                    saveFileToFirebaseFirestore(uri.toString());
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

    public void initViewsProgressBar(View view) {
        relativeLayoutData = view.findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = view.findViewById(R.id.relativeLayout_progressBar);
        progressBar = view.findViewById(R.id.progressBar);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        new MyTask().execute();

    }

    private void showListPubImage() {
        //Attach adapter to recyclerView
        recyclerViewPubImage.setHasFixedSize(true);
        recyclerViewPubImage.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewPubImage.setVisibility(View.VISIBLE);
        AdvertisementAdapter advertisementAdapter = new AdvertisementAdapter(getContext(), listPubImage);
        recyclerViewPubImage.setAdapter(advertisementAdapter);

        scrollView.fullScroll(ScrollView.FOCUS_UP);
    }

    public void saveFileToFirebaseFirestore(String videoUri) {
        showProgressBar();
        final Map<String, Object> mapUri = new HashMap<>();
        mapUri.put("videoURI", videoUri);

        firestore.collection("videos").document("video_pub")
                .update(mapUri).addOnCompleteListener(new OnCompleteListener<Void>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hideProgressBar();
                isFileSelected = false;
                butttonVideoPub.setText("Ajouter une vidéo pub");
                toastMessage(getActivity(), "Fichier enregistre avec succes!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressBar();
                toastMessage(getActivity(), "Erreur enregistrement image!");
            }
        });
    }

    public void getVideoPub() {
        if (videoUri == null) {
            showProgressBar();
            DocumentReference docRef = firestore.collection("videos")
                    .document("video_pub");
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    hideProgressBar();
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            videoUri = document.getString("videoURI");
                        }
                    } else {
                        Log.d("LOGGER", "get failed with ", task.getException());
                        toastMessage(getContext(), "Erreur chargement reessayez svp.");
                    }
                }
            });
        }
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
        getVideoPub();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (MyStaticVariables.isPlaying) {
                MyStaticVariables.mediaPlayer.pause();
                MyStaticVariables.isPlaying = false;
                mainMediaPlayerExist = true;
                onTrackPause(getActivity());
            }
            playVideo();
        } else if (videoView != null && isVideoPlaying) {
            videoView.stopPlayback();
            if (mainMediaPlayerExist) {
                try {
                    MyStaticVariables.mediaPlayer.start();
                    MyStaticVariables.isPlaying = true;
                    onTrackPlay(getActivity());
                    mainMediaPlayerExist = false;
                } catch (IllegalStateException e) {
                    System.out.println("IllegalStateException:  " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("Exception: " + e.getMessage());
                }

            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class MyTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //getListPubImage(getActivity());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //showListPubImage();
        }
    }
}
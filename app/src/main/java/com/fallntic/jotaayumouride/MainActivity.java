package com.fallntic.jotaayumouride;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fallntic.jotaayumouride.model.Dahira;
import com.fallntic.jotaayumouride.model.Event;
import com.fallntic.jotaayumouride.model.ObjNotification;
import com.fallntic.jotaayumouride.model.User;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.DahiraInfoActivity.getExistingExpenses;
import static com.fallntic.jotaayumouride.HomeActivity.interstitialAd;
import static com.fallntic.jotaayumouride.fragments.PubFragment.videoUri;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.dismissProgressDialog;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.TITLE_ANNOUNCEMENT_NOTIFICATION;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.TITLE_CONTRIBUTION_NOTIFICATION;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.TITLE_EVENT_NOTIFICATION;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.TITLE_EXPENSE_NOTIFICATION;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.audioDuration;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.codeLinkVideoDeLaSemaine;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.dahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.descriptionVideoDeLaSemaine;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.displayEvent;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.fileName;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.firebaseAuth;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.firebaseUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.firestore;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.imageUriYobalouBessBi;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.indexOnlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAllEvent;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.objNotification;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.titlePrayerTime;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.titleVideoDeLaSemaine;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.uriAudio;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.uriBayitDuJour;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.uriPrayerTime;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.userID;

@SuppressWarnings("LoopStatementThatDoesntLoop")
public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    private static final String CHANNEL_FIREBASE = "jotaayou_mouride";
    private static final String CHANNEL_Name = "Jotaayou Mouride";
    private static final String CHANNEL_DESC = "Jotaayou Mouride notifications";

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setLogo(R.mipmap.logo);
        setSupportActionBar(toolbar);

        textView = findViewById(R.id.textView);

        checkInternetConnection(this);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userID = firebaseAuth.getUid();

        if (firebaseUser != null) {
            createChannel();
            objNotification = (ObjNotification) getIntent().getSerializableExtra("objNotification");
            new MyTask().execute();
        } else {
            new MyTask().execute();
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }

        //************************************* adMob **********************************************
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        interstitialAd = new InterstitialAd(this);
        // Insert the Ad Unit ID
        interstitialAd.setAdUnitId(this.getString(R.string.interstitial_unit_id));
        //Load Ad
        interstitialAd.loadAd(new AdRequest.Builder().build());
        //******************************************************************************************
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_FIREBASE, CHANNEL_Name,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void getOnlineUser(final Context context, String userID, final ObjNotification objNotification) {

        if (objNotification != null) {
            userID = objNotification.getUserID();
        }
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("userID", userID).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            onlineUser = documentSnapshot.toObject(User.class);
                        }
                        if (onlineUser != null) {
                            textView.setText("Bienvenue " + onlineUser.getUserName());
                            if (objNotification != null) {
                                getDahira(context, objNotification.getDahiraID());
                            } else {
                                Intent intent = new Intent(context, HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            context.startActivity(new Intent(context, SignUpPhoneActivity.class));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        textView.setText("Erreur reseau reessayez plutard stp.");
                    }
                });

        dismissProgressDialog();
    }

    private void getDahira(final Context context, final String dahiraID) {
        FirebaseFirestore.getInstance().collection("dahiras")
                .whereEqualTo("dahiraID", dahiraID).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                dahira = documentSnapshot.toObject(Dahira.class);
                                indexOnlineUser = onlineUser.getListDahiraID().indexOf(dahira.getDahiraID());
                                break;
                            }
                            if (objNotification != null) {

                                switch (objNotification.getTitle()) {
                                    case TITLE_EXPENSE_NOTIFICATION:
                                        getExistingExpenses(context, objNotification.getDahiraID());
                                        break;
                                    case TITLE_ANNOUNCEMENT_NOTIFICATION:
                                        context.startActivity(new Intent(context, ShowAnnouncementActivity.class));
                                        finish();
                                        break;
                                    case TITLE_EVENT_NOTIFICATION:
                                        displayEvent = "allEvents";
                                        getAllEvents(context);
                                        break;
                                    case TITLE_CONTRIBUTION_NOTIFICATION:
                                        Intent intent = new Intent(context, HomeActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        startActivity(intent);
                                        finish();
                                        break;
                                }
                            } else {
                                Intent intent = new Intent(context, HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                            }
                            Log.d(TAG, "Dahira downloaded");
                        }
                    }
                });
    }

    private void getAllEvents(final Context context) {
        if (listAllEvent == null) {
            listAllEvent = new ArrayList<>();

            firestore.collection("events").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot documentSnapshot : list) {
                                    Event event = documentSnapshot.toObject(Event.class);
                                    listAllEvent.add(event);
                                }
                                if (objNotification != null) {
                                    displayEvent = "allEvents";
                                    Intent intent = new Intent(context, ShowEventActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Intent intent = new Intent(context, HomeActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);
                                    finish();
                                }

                                Log.d(TAG, "Events downloaded.");
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Intent intent = new Intent(context, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                    Log.d(TAG, "Error downloading events");
                }
            });
        }
    }

    @Override
    public AssetManager getAssets() {
        return getResources().getAssets();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
    }

    public void getPrayerTime() {
        DocumentReference docRef = firestore.collection("images")
                .document("prayer_time");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        uriPrayerTime = document.getString("imageURI");
                        titlePrayerTime = document.getString("title");
                    }
                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });
    }

    public void getVideoDeLaSemaine() {
        DocumentReference docRef = firestore.collection("videos")
                .document("video_de_la_semaine");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        codeLinkVideoDeLaSemaine = document.getString("code");
                        titleVideoDeLaSemaine = document.getString("title");
                        descriptionVideoDeLaSemaine = document.getString("description");
                        //showVideoDeLaSemaine();
                    }
                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });
    }

    public void getBayitDuJour() {
        DocumentReference docRef = firestore.collection("bayit_du_jour")
                .document("bayit_du_jour");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        uriBayitDuJour = document.getString("imageURI");
                        //showBayitDuJour();
                    }
                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });
    }

    public void getYobalouBessBi() {
        DocumentReference docRef = firestore.collection("yobalou_bess_bi")
                .document("yobalou_bess_bi");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        imageUriYobalouBessBi = document.getString("imageURI");
                        fileName = document.getString("title");
                        uriAudio = document.getString("audioURI");
                        audioDuration = document.getString("duration");
                        //showYobalouBessBi();
                    }
                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });
    }

    public void getVideoPub() {
        if (videoUri == null) {
            DocumentReference docRef = firestore.collection("videos")
                    .document("video_pub");
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            videoUri = document.getString("videoURI");
                        }
                    } else {
                        Log.d("LOGGER", "get failed with ", task.getException());
                    }
                }
            });
        }
    }

    @SuppressLint("StaticFieldLeak")
    class MyTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getBayitDuJour();
            getYobalouBessBi();
            getPrayerTime();
            getVideoDeLaSemaine();
            getVideoPub();
            //getListPubImage(MainActivity.this);
            if (firebaseUser != null)
                getOnlineUser(MainActivity.this, userID, objNotification);
            return null;
        }
    }

}
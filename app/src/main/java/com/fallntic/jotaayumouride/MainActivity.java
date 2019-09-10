package com.fallntic.jotaayumouride;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fallntic.jotaayumouride.Model.Dahira;
import com.fallntic.jotaayumouride.Model.Event;
import com.fallntic.jotaayumouride.Model.ObjNotification;
import com.fallntic.jotaayumouride.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.DahiraInfoActivity.getExistingExpenses;
import static com.fallntic.jotaayumouride.Utility.DataHolder.dahira;
import static com.fallntic.jotaayumouride.Utility.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.Utility.DataHolder.indexOnlineUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.userID;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.TITLE_ANNOUNCEMENT_NOTIFICATION;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.TITLE_CONTRIBUTION_NOTIFICATION;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.TITLE_EVENT_NOTIFICATION;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.TITLE_EXPENSE_NOTIFICATION;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.displayEvent;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.firebaseAuth;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.firebaseUser;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.firestore;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAllEvent;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.myListEvents;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.objNotification;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    public static final String CHANNEL_ID = "jotaayou_mouride";
    public static final String CHANNEL_Name = "Jotaayou Mouride";
    public static final String CHANNEL_DESC = "Jotaayou Mouride Notifications";

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
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userID = firebaseAuth.getUid();

        if (firebaseUser != null) {
            createChannel();
            objNotification = (ObjNotification) getIntent().getSerializableExtra("objNotification");
            getOnlineUser(this, userID, objNotification);

        } else {
            finish();
            startActivity(new Intent(this, HomeActivity.class));
        }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_Name,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public void getOnlineUser(final Context context, String userID, final ObjNotification objNotification) {

        if (objNotification != null) {
            userID = objNotification.getUserID();
        }

        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("userID", userID).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            onlineUser = documentSnapshot.toObject(User.class);
                        }
                        if (onlineUser != null) {
                            textView.setText("Bienvenu " + onlineUser.getUserName());
                            if (objNotification != null) {
                                getDahira(context, objNotification.getDahiraID());
                            } else {
                                finish();
                                context.startActivity(new Intent(context, HomeActivity.class));
                            }
                        } else {
                            context.startActivity(new Intent(context, SignUpPhoneActivity.class));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        textView.setText("Erreur reseau reessayez plutard stp.");
                    }
                });

        dismissProgressDialog();
    }

    public void getDahira(final Context context, final String dahiraID) {
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

                                if (objNotification.getTitle().equals(TITLE_EXPENSE_NOTIFICATION)) {
                                    getExistingExpenses(context, objNotification.getDahiraID());

                                } else if (objNotification.getTitle().equals(TITLE_ANNOUNCEMENT_NOTIFICATION)) {
                                    context.startActivity(new Intent(context, ShowAnnouncementActivity.class));

                                } else if (objNotification.getTitle().equals(TITLE_EVENT_NOTIFICATION)) {

                                    if (onlineUser.getListDahiraID().contains(objNotification.getDahiraID())) {
                                        displayEvent = "myEvents";
                                        getMyEvents(context, dahira);
                                    } else {
                                        displayEvent = "allEvents";
                                        getAllEvents(context);
                                    }
                                }else if (objNotification.getTitle().equals(TITLE_CONTRIBUTION_NOTIFICATION)){
                                    context.startActivity(new Intent(context, HomeActivity.class));
                                }
                            } else {
                                finish();
                                context.startActivity(new Intent(context, HomeActivity.class));
                            }
                            Log.d(TAG, "Dahira downloaded");
                        }
                    }
                });
    }

    public void getAllEvents(final Context context) {
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
                                    finish();
                                    context.startActivity(new Intent(context, ShowEventActivity.class));
                                } else {
                                    finish();
                                    context.startActivity(new Intent(context, HomeActivity.class));
                                }

                                Log.d(TAG, "Events downloaded.");
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    context.startActivity(new Intent(context, HomeActivity.class));
                    Log.d(TAG, "Error downloading events");
                }
            });
        }
    }

    public void getMyEvents(final Context context, Dahira dahira) {
        if (myListEvents == null || myListEvents.size() <= 0) {
            myListEvents = new ArrayList<>();

            textView.setText("Chargement des evenements en cours ...");
            firestore.collection("dahiras")
                    .document(dahira.getDahiraID())
                    .collection("myEvents")
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    dismissProgressDialog();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Event event = documentSnapshot.toObject(Event.class);
                            myListEvents.add(event);
                        }
                    }
                    if (objNotification != null) {
                        displayEvent = "myEvents";
                        finish();
                        context.startActivity(new Intent(context, ShowEventActivity.class));
                    } else {
                        finish();
                        context.startActivity(new Intent(context, HomeActivity.class));
                    }
                    Log.d(TAG, "Even downloaded");
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            textView.setText("Erreur reseau reessayez plutard svp ...");
                            Log.d(TAG, "Error downloading event");
                        }
                    });
        }
    }

}
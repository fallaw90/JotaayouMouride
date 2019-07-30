package com.fallntic.jotaayumouride;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.fallntic.jotaayumouride.DataHolder.actionSelected;
import static com.fallntic.jotaayumouride.DataHolder.createNewCollection;
import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.event;
import static com.fallntic.jotaayumouride.DataHolder.getCurrentDate;
import static com.fallntic.jotaayumouride.DataHolder.getDate;
import static com.fallntic.jotaayumouride.DataHolder.getTime;
import static com.fallntic.jotaayumouride.DataHolder.indexEventSelected;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.showProgressDialog;

public class CreateEventActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CreateNewEventActivity";

    private TextView textViewTitle;
    private EditText editTextTitleEvent;
    private EditText editTextDate;
    private EditText editTextLocation;
    private EditText editTextNote;
    private EditText editTextStartTime;
    private EditText editTextEndTime;


    private String mDate;
    private String title;
    private String note;
    private String location;
    private String startTime;
    private String endTime;

    private Button buttonDelete;

    public static void updateEvent(final Context context) {
        showProgressDialog(context, "Enregistrement de votre evenement en cours ...");
        FirebaseFirestore.getInstance().collection("events")
                .document(dahira.getDahiraID())
                .update("listUserID", event.getListUserID(),
                        "listUserName", event.getListUserName(),
                        "listDate", event.getListDate(),
                        "listTitle", event.getListTitle(),
                        "listNote", event.getListNote(),
                        "listLocation", event.getListLocation(),
                        "listStartTime", event.getListStartTime(),
                        "listEndTime", event.getListEndTime())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dismissProgressDialog();
                        Log.d(TAG, "Event updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissProgressDialog();
                        actionSelected = "";
                        Intent intent = new Intent(context, DataHolder.class);
                        showAlertDialog(context, "Erreur lors de l'enregistrement de votre evenement." +
                                "\nReessayez plutard SVP", intent);
                        Log.d(TAG, "Error updated event");
                    }
                });
    }

    public static boolean hasValidationErrors(String title, EditText editTextTitleEvent,
                                              String mDate, EditText editTextDate,
                                              String location, EditText editTextLocation,
                                              String startTime, EditText editTextStartTime,
                                              String endTime, EditText editTextEndTime,
                                              String note, EditText editTextNote) {

        if (title.isEmpty()) {
            editTextTitleEvent.setError("Entrez un titre");
            editTextTitleEvent.requestFocus();
            return true;
        }
        if (mDate.isEmpty()) {
            editTextDate.setError("Entrez une date");
            editTextDate.requestFocus();
            return true;
        }
        if (location.isEmpty()) {
            editTextLocation.setError("Entrez le lieu de votre evenement");
            editTextLocation.requestFocus();
            return true;
        }
        if (startTime.isEmpty()) {
            editTextStartTime.setError("Entrez l'eheure du debut votre evenement");
            editTextStartTime.requestFocus();
            return true;
        }
        if (endTime.isEmpty()) {
            editTextEndTime.setError("Entrez l'eheure de la fin de votre evenement");
            editTextEndTime.requestFocus();
            return true;
        }
        if (note.isEmpty()) {
            editTextNote.setError("Entrez une description de votre evenement");
            editTextNote.requestFocus();
            return true;
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Ajouter un evenement");
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (!isConnected(this)) {
            finish();
            Intent intent = new Intent(this, MainActivity.class);
            showAlertDialog(this, "Oops! Pas de connexion, " +
                    "verifier votre connexion internet puis reesayez SVP", intent);
        }

        textViewTitle = findViewById(R.id.textView_title);
        editTextTitleEvent = findViewById(R.id.editText_titleEvent);
        editTextDate = findViewById(R.id.editText_date);
        editTextLocation = findViewById(R.id.editText_location);
        editTextStartTime = findViewById(R.id.editText_startTime);
        editTextEndTime = findViewById(R.id.editText_endTime);
        editTextNote = findViewById(R.id.editText_note);
        buttonDelete = findViewById(R.id.button_delete);

        textViewTitle.setText("Creation d'un nouveau evenement pour le dahira " + dahira.getDahiraName());
        editTextDate.setText(getCurrentDate());

        if (actionSelected.equals("updateEvent")) {
            toolbar.setSubtitle("Mettre a jour mon evenement");
            textViewTitle.setText("Modifier cet evenement pour le dahira " + dahira.getDahiraName());
            editTextDate.setText(event.getListDate().get(indexEventSelected));
            editTextTitleEvent.setText(event.getListTitle().get(indexEventSelected));
            editTextDate.setText(event.getListDate().get(indexEventSelected));
            editTextLocation.setText(event.getListLocation().get(indexEventSelected));
            editTextStartTime.setText(event.getListStartTime().get(indexEventSelected));
            editTextEndTime.setText(event.getListEndTime().get(indexEventSelected));
            editTextNote.setText(event.getListNote().get(indexEventSelected));
            buttonDelete.setVisibility(View.VISIBLE);
        }

        findViewById(R.id.editText_startTime).setOnClickListener(this);
        findViewById(R.id.editText_endTime).setOnClickListener(this);
        findViewById(R.id.editText_date).setOnClickListener(this);
        findViewById(R.id.button_save).setOnClickListener(this);
        findViewById(R.id.button_cancel).setOnClickListener(this);
        findViewById(R.id.button_delete).setOnClickListener(this);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(CreateEventActivity.this, ListEventActivity.class));
            }
        });

    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_save:
                saveEvent(this);
                break;

            case R.id.button_cancel:
                finish();
                break;
            case R.id.button_delete:
                updateEvent(this);
                break;
            case R.id.editText_date:
                getDate(this, editTextDate);
                break;
            case R.id.editText_startTime:
                getTime(this, editTextStartTime, "Heure du debut de votre evenement");
                break;
            case R.id.editText_endTime:
                getTime(this, editTextEndTime, "Heure de la fin de votre evenement");
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, DahiraInfoActivity.class));
    }

    public void saveEvent(final Context context) {
        mDate = editTextDate.getText().toString().trim();
        title = editTextTitleEvent.getText().toString().trim();
        note = editTextNote.getText().toString().trim();
        location = editTextLocation.getText().toString().trim();
        startTime = editTextStartTime.getText().toString().trim();
        endTime = editTextEndTime.getText().toString().trim();

        if (!hasValidationErrors(title, editTextTitleEvent, mDate, editTextDate, location, editTextLocation,
                startTime, editTextStartTime, endTime, editTextEndTime, note, editTextNote)) {

            if (event == null)
                event = new Event();

            event.setDahiraID(dahira.getDahiraID());
            event.getListUserID().add(onlineUser.getUserID());
            event.getListUserName().add(onlineUser.getUserName());
            event.getListDate().add(mDate);
            event.getListTitle().add(title);
            event.getListStartTime().add(startTime);
            event.getListEndTime().add(endTime);
            event.getListNote().add(note);
            event.getListLocation().add(location);

            showProgressDialog(context, "Enregistrement de votre evenement en cours ...");
            FirebaseFirestore.getInstance().collection("events").
                    document(dahira.getDahiraID()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            dismissProgressDialog();
                            if (documentSnapshot.exists()) {
                                updateEvent(CreateEventActivity.this);
                                Intent intent = new Intent(context, ListEventActivity.class);
                                showAlertDialog(context, "Evenement enregistre avec succe.", intent);
                                Log.d(TAG, "Collection evenement updated");
                            } else {
                                createNewCollection(context, "events",
                                        dahira.getDahiraID(), event);
                                Intent intent = new Intent(context, ListEventActivity.class);
                                showAlertDialog(CreateEventActivity.this,
                                        "Evenement enregistre avec succe.", intent);
                            }

                            title = "Evénement à venir";
                            String message = "Dahira " + dahira.getDahiraName() + " vient de " +
                                    "publier un nouveau événement. \nCliquez pour plus de détails.";

                            NotificationHelper.sendNotificationToAllUsers(context, title, message);

                            Log.d(TAG, "Collection evenement created");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dismissProgressDialog();
                            Log.d(TAG, e.toString());
                        }
                    });
        }
    }
}

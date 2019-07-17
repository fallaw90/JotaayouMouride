package com.fallntic.jotaayumouride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

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
import static com.fallntic.jotaayumouride.DataHolder.logout;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.showProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;

public class CreateNewEventActivity extends AppCompatActivity implements View.OnClickListener  {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_event);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Ajouter un evenement");
        setSupportActionBar(toolbar);

        if (!isConnected(this)){
            Intent intent = new Intent(this, LoginActivity.class);
            logout();
            showAlertDialog(this,"Oops! Pas de connexion, verifier votre connexion internet puis reesayez SVP", intent);
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

        if (actionSelected.equals("updateEvent")){
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

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.button_save:
                if (actionSelected.equals("addNewEvent")) {
                    saveEvent(this);
                }
                else if (actionSelected.equals("updateEvent")) {
                    updateEvent(this, event);
                }

                break;

            case R.id.button_cancel:
                actionSelected = "";
                finish();
                break;
            case R.id.button_delete:
                actionSelected = "deleteEvent";
                updateEvent(this, event);
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
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    public void saveEvent(final Context context){
        mDate = editTextDate.getText().toString().trim();
        title = editTextTitleEvent.getText().toString().trim();
        note = editTextNote.getText().toString().trim();
        location = editTextLocation.getText().toString().trim();
        startTime = editTextStartTime.getText().toString().trim();
        endTime = editTextEndTime.getText().toString().trim();

        if (!hasValidationErrors(title, editTextTitleEvent, mDate, editTextDate, location, editTextLocation,
                startTime, editTextStartTime, endTime, editTextEndTime, note, editTextNote)){

            showProgressDialog(context,"Enregistrement de votre evenement en cours ...");
            FirebaseFirestore.getInstance().collection("events").document(dahira.getDahiraID()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            dismissProgressDialog();
                            if (documentSnapshot.exists()) {
                                event = documentSnapshot.toObject(Event.class);
                                updateEvent(CreateNewEventActivity.this, event);
                                Log.d(TAG, "Collection evenement updated");
                            }
                            else {
                                event = newEventObject(dahira.getDahiraID(), mDate, title, note, location, startTime, endTime);
                                createNewCollection(context, "events", dahira.getDahiraID(), event);
                                Intent intent = new Intent(context, ListEventActivity.class);
                                showAlertDialog(CreateNewEventActivity.this, "Evenement cree avec succe.", intent);
                            }
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

            //actionSelected = "";
        }
    }

    public void updateEvent(final Context context, Event event){
        mDate = editTextDate.getText().toString().trim();
        title = editTextTitleEvent.getText().toString().trim();
        note = editTextNote.getText().toString().trim();
        location = editTextLocation.getText().toString().trim();
        startTime = editTextStartTime.getText().toString().trim();
        endTime = editTextEndTime.getText().toString().trim();

        if (actionSelected.equals("addNewEvent")){
            event.getListUserID().add(onlineUser.getUserID());
            event.getListUserName().add(onlineUser.getUserName());
            event.getListDate().add(mDate);
            event.getListTitle().add(title);
            event.getListNote().add(note);
            event.getListLocation().add(location);
            event.getListStartTime().add(startTime);
            event.getListEndTime().add(endTime);
        }
        else if (actionSelected.equals("updateEvent")){
            event.getListDate().set(indexEventSelected, mDate);
            event.getListTitle().set(indexEventSelected, title);
            event.getListNote().set(indexEventSelected, note);
            event.getListLocation().set(indexEventSelected, location);
            event.getListStartTime().set(indexEventSelected, startTime);
            event.getListEndTime().set(indexEventSelected, endTime);
        }
        else if (actionSelected.equals("deleteEvent")){
            event.getListDate().remove(indexEventSelected);
            event.getListTitle().remove(indexEventSelected);
            event.getListNote().remove(indexEventSelected);
            event.getListLocation().remove(indexEventSelected);
            event.getListStartTime().remove(indexEventSelected);
            event.getListEndTime().remove(indexEventSelected);
        }
        showProgressDialog(context,"Enregistrement de votre evenement en cours ...");
        FirebaseFirestore.getInstance().collection("events").document(dahira.getDahiraID())
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
                        Intent intent = new Intent(context, ListEventActivity.class);
                        if (actionSelected.equals("updateEvent")){
                            showAlertDialog(CreateNewEventActivity.this, "Evenement modifie avec succe.", intent);
                            Log.d(TAG, "Event updated");
                        }
                        else if (actionSelected.equals("deleteEvent")){
                            showAlertDialog(context, "Evenement supprime avec succe.", intent);
                            Log.d(TAG, "Event deleted");
                        }
                        else if (actionSelected.equals("addNewEvent")){
                            showAlertDialog(context, "Evenement cree avec succe.", intent);
                            Log.d(TAG, "New event added");
                        }
                        actionSelected = "";
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissProgressDialog();
                        actionSelected = "";
                        Intent intent = new Intent(CreateNewEventActivity.this, ListEventActivity.class);
                        showAlertDialog(CreateNewEventActivity.this, "Erreur lors de l'enregistrement de votre evenement." +
                                "\nReessayez plutard SVP", intent);
                        Log.d(TAG, "Error updated event");
                    }
                });
    }

    public Event newEventObject(String dahiraID, String mDate, String title, String note, String location,
                                String startTime, String endTime){

        List<String> listUserID = new ArrayList<String>();
        listUserID.add(onlineUser.getUserID());

        List<String> listUserName = new ArrayList<String>();
        listUserName.add(onlineUser.getUserName());

        List<String> listDate = new ArrayList<String>();
        listDate.add(mDate);

        List<String> listTitle = new ArrayList<String>();
        listTitle.add(title);

        List<String> listNote = new ArrayList<String>();
        listNote.add(note);

        List<String> listLocation = new ArrayList<String>();
        listLocation.add(location);

        List<String> listStartTime = new ArrayList<String>();
        listStartTime.add(startTime);

        List<String> listEndTime = new ArrayList<String>();
        listEndTime.add(endTime);

        Event event = new Event(dahiraID, listUserID, listUserName, listDate, listTitle,
                                listNote, listLocation, listStartTime, listEndTime);

        return event;
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
        if(mDate.isEmpty()) {
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
}

package com.fallntic.jotaayumouride;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fallntic.jotaayumouride.Model.Event;
import com.fallntic.jotaayumouride.Model.ObjNotification;
import com.fallntic.jotaayumouride.Utility.MyStaticVariables;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import static com.fallntic.jotaayumouride.Notifications.FirebaseNotificationHelper.sendNotificationToAllUsers;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.getCurrentDate;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.getDate;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.getTime;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.showAlertDialog;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.dahira;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.displayEvent;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.firestore;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.myListEvents;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutProgressBar;

public class CreateEventActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CreateNewEventActivity";


    private EditText ed_titleEvent, ed_date, ed_location, ed_note, ed_startTime, ed_endTime;
    private String mDate, title, note, location, startTime, endTime;
    private TextView tv_title;
    private Button btn_delete;
    private Toolbar toolbar;


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

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //toolbar.setLogo(R.mipmap.logo);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.logo);

        checkInternetConnection(this);

        initViews();
        tv_title.setText("Creation d'un nouveau evenement pour le dahira " + dahira.getDahiraName());
        ed_date.setText(getCurrentDate());

        hideSoftKeyboard();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem iconBack;
        iconBack = menu.findItem(R.id.icon_back);

        iconBack.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                startActivity(new Intent(this, HomeActivity.class));
                break;

            case R.id.icon_back:
                finish();
                startActivity(new Intent(this, DahiraInfoActivity.class));
                break;

            case R.id.instructions:
                startActivity(new Intent(this, InstructionsActivity.class));
                break;
        }
        return true;
    }

    private void initViews(){
        tv_title = findViewById(R.id.textView_title);
        ed_titleEvent = findViewById(R.id.editText_titleEvent);
        ed_date = findViewById(R.id.editText_date);
        ed_location = findViewById(R.id.editText_location);
        ed_startTime = findViewById(R.id.editText_startTime);
        ed_endTime = findViewById(R.id.editText_endTime);
        ed_note = findViewById(R.id.editText_note);
        btn_delete = findViewById(R.id.button_delete);

        findViewById(R.id.editText_startTime).setOnClickListener(this);
        findViewById(R.id.editText_endTime).setOnClickListener(this);
        findViewById(R.id.editText_date).setOnClickListener(this);
        findViewById(R.id.button_save).setOnClickListener(this);
        findViewById(R.id.button_cancel).setOnClickListener(this);

        initViewsProgressBar();
    }

    public  void initViewsProgressBar() {
        relativeLayoutData = findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    protected void onDestroy() {
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
            case R.id.editText_date:
                getDate(this, ed_date);
                break;
            case R.id.editText_startTime:
                getTime(this, ed_startTime, "Heure du debut de votre evenement");
                break;
            case R.id.editText_endTime:
                getTime(this, ed_endTime, "Heure de la fin de votre evenement");
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, DahiraInfoActivity.class));
    }

    public void saveEvent(final Context context) {
        mDate = ed_date.getText().toString().trim();
        title = ed_titleEvent.getText().toString().trim();
        note = ed_note.getText().toString().trim();
        location = ed_location.getText().toString().trim();
        startTime = ed_startTime.getText().toString().trim();
        endTime = ed_endTime.getText().toString().trim();

        if (!hasValidationErrors(title, ed_titleEvent, mDate, ed_date, location, ed_location,
                startTime, ed_startTime, endTime, ed_endTime, note, ed_note)) {

            final String eventID = onlineUser.getUserName() + System.currentTimeMillis();
            Event event = new Event(eventID, onlineUser.getUserName(), mDate, title, note,
                                location, startTime, endTime);

            saveToDahiraDocument(context, event);
        }
    }

    public void saveToDahiraDocument(final Context context, final Event event){
        showProgressBar();
        FirebaseFirestore.getInstance().collection("dahiras").
                document(dahira.getDahiraID())
                .collection("myEvents")
                .document(event.getEventID())
                .set(event)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //send notification.
                       saveToEventCollection(context, event);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideProgressBar();
                        toastMessage(context, "Erreur d'enregistrement de votre evenement.");
                        startActivity(new Intent(context, DahiraInfoActivity.class));
                    }
                });
    }

    public void saveToEventCollection(final Context context, final Event event){
        showProgressBar();
        firestore.collection("events")
                .document(event.getEventID())
                .set(event)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideProgressBar();
                        //send notification.
                        MyStaticVariables.objNotification = new ObjNotification(event.getEventID(),
                                onlineUser.getUserID(), dahira.getDahiraID(),
                                MyStaticVariables.TITLE_EVENT_NOTIFICATION, note);

                        sendNotificationToAllUsers(context, MyStaticVariables.objNotification);

                        myListEvents.add(event);
                        displayEvent = "myEvents";
                        final Intent intent = new Intent(context, ShowEventActivity.class);
                        showAlertDialog(context, "Evenement enregistre.", intent);
                        Log.d(TAG, "Event saved.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideProgressBar();
                        toastMessage(context, "Erreur d'enregistrement de votre evenement.");
                        startActivity(new Intent(context, DahiraInfoActivity.class));
                    }
                });
    }

    public void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}

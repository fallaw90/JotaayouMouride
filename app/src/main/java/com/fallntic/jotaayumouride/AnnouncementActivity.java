package com.fallntic.jotaayumouride;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fallntic.jotaayumouride.Model.Announcement;
import com.fallntic.jotaayumouride.Utility.MyStaticVariables;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.fallntic.jotaayumouride.DataHolder.actionSelected;
import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.getCurrentDate;
import static com.fallntic.jotaayumouride.DataHolder.getDate;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.logout;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.MainActivity.progressBar;
import static com.fallntic.jotaayumouride.MainActivity.relativeLayoutProgressBar;
import static com.fallntic.jotaayumouride.NotificationHelper.sendNotificationToSpecificUsers;

public class AnnouncementActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "AnnouncementActivity";

    private TextView textViewTitle;
    private EditText editTextDate;
    private EditText editTextNote;

    public static boolean hasValidationErrors(String mDate, EditText editTextDate,
                                              String note, EditText editTextNote) {
        if (mDate.isEmpty()) {
            editTextDate.setError("Entrez une date");
            editTextDate.requestFocus();
            return true;
        }
        if (note.isEmpty()) {
            editTextNote.setError("Tapez votre annonce ici");
            editTextNote.requestFocus();
            return true;
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Ajouter une annonce");
        setSupportActionBar(toolbar);

        if (!isConnected(this)) {
            logout(this);
            finish();
            Intent intent = new Intent(this, MainActivity.class);
            showAlertDialog(this, "Oops! Pas de connexion, verifier votre connexion internet puis reesayez SVP", intent);
        }


        ListUserActivity.scrollView = findViewById(R.id.scrollView);
        //ProgressBar from static variable MainActivity
        relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        progressBar = findViewById(R.id.progressBar);
        relativeLayoutProgressBar.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        textViewTitle = findViewById(R.id.textView_title);
        editTextDate = findViewById(R.id.editText_date);
        editTextNote = findViewById(R.id.editText_note);

        textViewTitle.setText("Creer une annonce pour le dahira " + dahira.getDahiraName());
        editTextDate.setText(getCurrentDate());

        findViewById(R.id.editText_date).setOnClickListener(this);
        findViewById(R.id.button_save).setOnClickListener(this);
        findViewById(R.id.button_cancel).setOnClickListener(this);
        findViewById(R.id.button_delete).setOnClickListener(this);

        hideSoftKeyboard();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, DahiraInfoActivity.class));
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.editText_date:
                getDate(this, editTextDate);
                break;

            case R.id.button_save:
                    saveAnnouncement(this);
                break;

            case R.id.button_cancel:
                actionSelected = "";
                finish();
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void saveAnnouncement(final Context context) {
        final String note = editTextNote.getText().toString().trim();


        if (!note.isEmpty()) {
            ListUserActivity.showProgressBar();

            final Intent intent = new Intent(context, ShowAnnouncementActivity.class);

            final String announcementID = onlineUser.getUserName() + System.currentTimeMillis();
            Announcement announcement = new Announcement(announcementID, onlineUser.getUserName(), note);
            FirebaseFirestore.getInstance().collection("announcements")
                    .document(dahira.getDahiraID())
                    .collection("text")
                    .document(announcementID)
                    .set(announcement).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    //send notification.

                    MyStaticVariables.objNotification = new ObjNotification(announcementID,
                            onlineUser.getUserID(), dahira.getDahiraID(), MyStaticVariables.TITLE_ANNOUNCEMENT_NOTIFICATION, note);

                    sendNotificationToSpecificUsers(context, MyStaticVariables.objNotification);

                    showAlertDialog(context, "Annonce envoyee.", intent);
                    Log.d(TAG, "Announcement saved.");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    showAlertDialog(context, "Erreur d'envoie de votre annonce." +
                            "Reessayez plutard SVP.", intent);
                    Log.d(TAG, "Error : " + e.getMessage());
                }
            });
        } else {
            editTextNote.setError("Ecrivez votre message ici.");
            editTextNote.requestFocus();
            return;
        }
    }

    public void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}

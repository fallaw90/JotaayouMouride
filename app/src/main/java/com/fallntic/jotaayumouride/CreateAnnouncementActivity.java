package com.fallntic.jotaayumouride;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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

import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.actionSelected;
import static com.fallntic.jotaayumouride.DataHolder.announcement;
import static com.fallntic.jotaayumouride.DataHolder.createNewCollection;
import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.getCurrentDate;
import static com.fallntic.jotaayumouride.DataHolder.getDate;
import static com.fallntic.jotaayumouride.DataHolder.indexAnnouncementSelected;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.logout;
import static com.fallntic.jotaayumouride.DataHolder.objNotification;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.MainActivity.progressBar;
import static com.fallntic.jotaayumouride.MainActivity.relativeLayoutProgressBar;
import static com.fallntic.jotaayumouride.NotificationHelper.sendNotificationToSpecificUsers;

public class CreateAnnouncementActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "CreateAnnouncementActivity";

    private TextView textViewTitle;
    private EditText editTextDate;
    private EditText editTextNote;

    private String mDate;
    private String title;
    private String message;

    private Button buttonDelete;

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
        setContentView(R.layout.activity_create_announcement);
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
        buttonDelete = findViewById(R.id.button_delete);

        textViewTitle.setText("Creer une annonce pour le dahira " + dahira.getDahiraName());
        editTextDate.setText(getCurrentDate());

        if (actionSelected.equals("updateAnnouncement")) {
            toolbar.setSubtitle("Modifier mon annonce");
            textViewTitle.setText("Modifier cette annonce pour le dahira " + dahira.getDahiraName());
            editTextDate.setText(announcement.getListDate().get(indexAnnouncementSelected));
            editTextNote.setText(announcement.getListNote().get(indexAnnouncementSelected));
            buttonDelete.setVisibility(View.VISIBLE);
        }

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
                if (actionSelected.equals("addNewAnnouncement"))
                    saveAnnouncement(this);
                else if (actionSelected.equals("updateAnnouncement"))
                    updateAnnouncement(this);
                break;

            case R.id.button_cancel:
                actionSelected = "";
                finish();
                break;

            case R.id.button_delete:
                actionSelected = "deleteAnnouncement";
                updateAnnouncement(this);
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
        if (announcement == null)
            announcement = new Announcement();

        mDate = editTextDate.getText().toString().trim();
        message = editTextNote.getText().toString().trim();

        objNotification = new ObjNotification();

        if (!hasValidationErrors(mDate, editTextDate, message, editTextNote)) {

            ListUserActivity.showProgressBar();
            FirebaseFirestore.getInstance().collection("announcements").document(dahira.getDahiraID()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @SuppressLint("LongLogTag")
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            ListUserActivity.hideProgressBar();
                            if (documentSnapshot.exists()) {
                                announcement = documentSnapshot.toObject(Announcement.class);
                                updateAnnouncement(CreateAnnouncementActivity.this);
                                Log.d(TAG, "Collection evenement updated");
                            } else {
                                announcement = createNewAnnouncementObject(mDate, message);
                                createNewCollection(context, "announcements", dahira.getDahiraID(), announcement);
                                Log.d(TAG, "Announcement deleted");
                            }

                            title = "Nouvelle Annoncement";
                            sendNotificationToSpecificUsers(context, title, message);
                            //Intent intent = new Intent(context, ListAnnouncementActivity.class);
                            //showAlertDialog(context, "Annonce enregistre avec succe", intent);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @SuppressLint("LongLogTag")
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            ListUserActivity.hideProgressBar();
                            Log.d(TAG, e.toString());
                        }
                    });
        }
    }

    public Announcement createNewAnnouncementObject(String mDate, String note) {

        List<String> listUserID = new ArrayList<String>();
        listUserID.add(onlineUser.getUserID());

        List<String> listUserName = new ArrayList<String>();
        listUserName.add(onlineUser.getUserName());

        List<String> listDate = new ArrayList<String>();
        listDate.add(mDate);

        List<String> listNote = new ArrayList<String>();
        listNote.add(note);

        Announcement announcement = new Announcement(dahira.getDahiraID(), listUserID, listUserName, listDate, listNote);

        return announcement;
    }

    public void updateAnnouncement(final Context context) {
        mDate = editTextDate.getText().toString().trim();
        message = editTextNote.getText().toString().trim();

        if (actionSelected.equals("addNewAnnouncement")) {
            announcement.getListUserID().add(onlineUser.getUserID());
            announcement.getListUserName().add(onlineUser.getUserName());
            announcement.getListDate().add(mDate);
            announcement.getListNote().add(message);
        } else if (actionSelected.equals("updateAnnouncement")) {
            announcement.getListDate().set(indexAnnouncementSelected, mDate);
            announcement.getListNote().set(indexAnnouncementSelected, message);
        } else if (actionSelected.equals("deleteAnnouncement")) {
            announcement.getListUserID().remove(indexAnnouncementSelected);
            announcement.getListUserName().remove(indexAnnouncementSelected);
            announcement.getListDate().remove(indexAnnouncementSelected);
            announcement.getListNote().remove(indexAnnouncementSelected);
        }

        ListUserActivity.showProgressBar();
        FirebaseFirestore.getInstance().collection("announcements")
                .document(dahira.getDahiraID())
                .update("listUserID", announcement.getListUserID(),
                        "listUserName", announcement.getListUserName(),
                        "listDate", announcement.getListDate(),
                        "listNote", announcement.getListNote())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onSuccess(Void aVoid) {
                        ListUserActivity.hideProgressBar();
                        Intent intent = new Intent(context, ListAnnouncementActivity.class);
                        if (actionSelected.equals("updateAnnouncement")) {
                            showAlertDialog(CreateAnnouncementActivity.this,
                                    "Annonce modifiee avec succe", intent);
                            Log.d(TAG, "Announcement updated");
                        } else if (actionSelected.equals("deleteAnnouncement")) {
                            showAlertDialog(context, "Annonce supprimee avec succe", intent);
                            Log.d(TAG, "Announcement deleted");
                        } else if (actionSelected.equals("addNewAnnouncement")) {
                            toastMessage(context, "Annonce ajoutee avec succe");
                            startActivity(intent);
                            Log.d(TAG, "New announcement added");
                        }
                        actionSelected = "";
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ListUserActivity.hideProgressBar();
                        actionSelected = "";
                        Intent intent = new Intent(context, ListAnnouncementActivity.class);
                        showAlertDialog(context, "Erreur lors de l'enregistrement de votre annonce." +
                                "\nReessayez plutard SVP", intent);
                        Log.d(TAG, "Error updated event");
                    }
                });
    }

    public void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}

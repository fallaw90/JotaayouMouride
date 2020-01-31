package com.fallntic.jotaayumouride;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fallntic.jotaayumouride.model.Announcement;
import com.fallntic.jotaayumouride.model.ObjNotification;
import com.fallntic.jotaayumouride.utility.MyStaticVariables;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import static com.fallntic.jotaayumouride.notifications.FirebaseNotificationHelper.sendNotificationToSpecificUsers;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.dismissProgressDialog;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showAlertDialog;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.actionSelected;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.dahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutData;

public class CreateAnnouncementActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "CreateAnnouncementActivity";

    private TextView textViewTitle;
    private EditText editTextNote;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_announcement);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //***************** Set logo **********************
        //getSupportActionBar().setLogo(R.mipmap.logo);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.logo);

        checkInternetConnection(this);

        firestore = FirebaseFirestore.getInstance();

        initViews();
        textViewTitle.setText("Creer une annonce pour le dahira " + dahira.getDahiraName());

        HomeActivity.loadBannerAd(this);

        hideSoftKeyboard();
    }

    private void initViews() {
        textViewTitle = findViewById(R.id.textView_title);
        editTextNote = findViewById(R.id.editText_note);

        findViewById(R.id.button_save).setOnClickListener(this);
        findViewById(R.id.button_cancel).setOnClickListener(this);

        initViewsProgressBar();
    }

    private void initViewsProgressBar() {
        relativeLayoutData = findViewById(R.id.relativeLayout_data);
        MyStaticVariables.relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        MyStaticVariables.progressBar = findViewById(R.id.progressBar);
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
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.destroy();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.resume();
        }
    }

    private void saveAnnouncement(final Context context) {
        final String note = editTextNote.getText().toString().trim();


        if (!note.isEmpty()) {
            showProgressBar();

            final Intent intent = new Intent(context, ShowAnnouncementActivity.class);

            final String announcementID = onlineUser.getUserName() + System.currentTimeMillis();
            Announcement announcement = new Announcement(announcementID, onlineUser.getUserName(), note);
            firestore.collection("announcements")
                    .document(dahira.getDahiraID())
                    .collection("text")
                    .document(announcementID)
                    .set(announcement).addOnSuccessListener(new OnSuccessListener<Void>() {
                @SuppressLint("LongLogTag")
                @Override
                public void onSuccess(Void aVoid) {
                    hideProgressBar();
                    //send notificationMediaPlayer.
                    MyStaticVariables.objNotification = new ObjNotification(announcementID,
                            onlineUser.getUserID(), dahira.getDahiraID(), MyStaticVariables.TITLE_ANNOUNCEMENT_NOTIFICATION, note);

                    sendNotificationToSpecificUsers(MyStaticVariables.objNotification);

                    showAlertDialog(context, "Annonce envoyee.", intent);
                    Log.d(TAG, "Announcement saved.");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @SuppressLint("LongLogTag")
                @Override
                public void onFailure(@NonNull Exception e) {
                    hideProgressBar();
                    showAlertDialog(context, "Erreur d'envoie de votre annonce." +
                            "Reessayez plutard SVP.", intent);
                    Log.d(TAG, "Error : " + e.getMessage());
                }
            });
        } else {
            editTextNote.setError("Ecrivez votre message ici.");
            editTextNote.requestFocus();
        }
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
        return true;
    }
}
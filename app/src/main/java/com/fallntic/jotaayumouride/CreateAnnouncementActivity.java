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

import com.fallntic.jotaayumouride.Model.Announcement;
import com.fallntic.jotaayumouride.Model.ObjNotification;
import com.fallntic.jotaayumouride.Utility.MyStaticVariables;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Objects;

import static com.fallntic.jotaayumouride.Notifications.FirebaseNotificationHelper.sendNotificationToSpecificUsers;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.dismissProgressDialog;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.showAlertDialog;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.actionSelected;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.dahira;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.firestore;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutData;

public class CreateAnnouncementActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "CreateAnnouncementActivity";

    private TextView textViewTitle;
    private EditText editTextNote;
    private Toolbar toolbar;

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

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //***************** Set logo **********************
        //getSupportActionBar().setLogo(R.mipmap.logo);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.logo);

        checkInternetConnection(this);

        initViews();
        textViewTitle.setText("Creer une annonce pour le dahira " + dahira.getDahiraName());

        HomeActivity.loadBannerAd(this, this);

        hideSoftKeyboard();
    }

    private void initViews() {
        textViewTitle = findViewById(R.id.textView_title);
        editTextNote = findViewById(R.id.editText_note);

        findViewById(R.id.button_save).setOnClickListener(this);
        findViewById(R.id.button_cancel).setOnClickListener(this);

        initViewsProgressBar();
    }

    public  void initViewsProgressBar() {
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

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void saveAnnouncement(final Context context) {
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

                    sendNotificationToSpecificUsers(context, MyStaticVariables.objNotification);

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
            return;
        }
    }

    public void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                startActivity(new Intent(this, HomeActivity.class));
                break;
        }
        return true;
    }
}

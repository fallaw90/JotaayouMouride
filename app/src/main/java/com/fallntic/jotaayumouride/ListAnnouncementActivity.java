package com.fallntic.jotaayumouride;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import static com.fallntic.jotaayumouride.DataHolder.actionSelected;
import static com.fallntic.jotaayumouride.DataHolder.announcement;
import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.event;
import static com.fallntic.jotaayumouride.DataHolder.indexOnlineUser;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.logout;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;

public class ListAnnouncementActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "ListAnnouncementActivity";

    private TextView textViewDahiraName;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerViewAnnoucement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_announcement);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Liste des annonces");
        setSupportActionBar(toolbar);

        if (!isConnected(this)){
            Intent intent = new Intent(this, LoginActivity.class);
            logout();
            showAlertDialog(this,"Oops! Pas de connexion, verifier votre connexion internet puis reesayez SVP", intent);
        }

        recyclerViewAnnoucement = findViewById(R.id.recyclerview_announcement);
        textViewDahiraName = findViewById(R.id.textView_dahiraName);
        textViewDahiraName.setText("Liste des annonces du dahira " + dahira.getDahiraName());

        showListAnnouncements();

        findViewById(R.id.button_back).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button_back:
                startActivity(new Intent(this, DahiraInfoActivity.class));
                break;
        }
    }

    private void showListAnnouncements() {

        //Attach adapter to recyclerView
        Intent intent = new Intent(ListAnnouncementActivity.this, DahiraInfoActivity.class);
        if (announcement.getDahiraID() != null){
            if (announcement.getDahiraID().equals(dahira.getDahiraID())){
                recyclerViewAnnoucement.setHasFixedSize(true);
                recyclerViewAnnoucement.setLayoutManager(new LinearLayoutManager(this));
                recyclerViewAnnoucement.setVisibility(View.VISIBLE);
                final AnnouncementAdapter announcementAdapter = new AnnouncementAdapter(ListAnnouncementActivity.this,
                        announcement.getListUserName(), announcement.getListDate(), announcement.getListNote());

                recyclerViewAnnoucement.setAdapter(announcementAdapter);
                announcementAdapter.notifyDataSetChanged();
            }
            else {
                showAlertDialog(ListAnnouncementActivity.this, "Dahira " + dahira.getDahiraName() +
                        " n'a aucun evenement enregistre pour le moment", intent);
            }
        }
        else {
            showAlertDialog(ListAnnouncementActivity.this, "Dahira " + dahira.getDahiraName() +
                    " n'a aucun evenement enregistre pour le moment", intent);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list_announcement, menu);

        MenuItem menuItemAddAannouncement;

        menuItemAddAannouncement = menu.findItem(R.id.addAnnouncement);
        menuItemAddAannouncement.setVisible(false);

        if (onlineUser.getListDahiraID().contains(dahira.getDahiraID())){
            menuItemAddAannouncement.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addAnnouncement:
                actionSelected = "addNewAnnouncement";
                startActivity(new Intent(this, CreateAnnouncementActivity.class));
                break;

            case R.id.logout:
                logout();
                finish();
                startActivity(new Intent(this, MainActivity.class));
                break;
        }
        return true;
    }
}

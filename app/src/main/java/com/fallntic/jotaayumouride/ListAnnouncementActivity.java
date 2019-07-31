package com.fallntic.jotaayumouride;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import static com.fallntic.jotaayumouride.DataHolder.actionSelected;
import static com.fallntic.jotaayumouride.DataHolder.announcement;
import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.notificationBody;
import static com.fallntic.jotaayumouride.DataHolder.notificationTitle;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;

public class ListAnnouncementActivity extends AppCompatActivity {
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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (!isConnected(this)) {
            finish();
            Intent intent = new Intent(this, LoginActivity.class);
            showAlertDialog(this, "Oops! Pas de connexion, " +
                    "verifier votre connexion internet puis reesayez SVP", intent);
        }

        recyclerViewAnnoucement = findViewById(R.id.recyclerview_announcement);
        textViewDahiraName = findViewById(R.id.textView_dahiraName);
        textViewDahiraName.setText("Liste des annonces du dahira " + dahira.getDahiraName());


        showListAnnouncements();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(ListAnnouncementActivity.this, DahiraInfoActivity.class));
            }
        });

        notificationTitle = null;
        notificationBody = null;
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    private void showListAnnouncements() {

        //Attach adapter to recyclerView
        Intent intent = new Intent(ListAnnouncementActivity.this, DahiraInfoActivity.class);
        if (announcement != null) {
            if (announcement.getDahiraID().equals(dahira.getDahiraID())) {
                recyclerViewAnnoucement.setHasFixedSize(true);
                recyclerViewAnnoucement.setLayoutManager(new LinearLayoutManager(this));
                recyclerViewAnnoucement.setVisibility(View.VISIBLE);
                final AnnouncementAdapter announcementAdapter = new AnnouncementAdapter(ListAnnouncementActivity.this,
                        announcement.getListUserName(), announcement.getListDate(), announcement.getListNote());

                recyclerViewAnnoucement.setAdapter(announcementAdapter);
                announcementAdapter.notifyDataSetChanged();
            } else {
                showAlertDialog(ListAnnouncementActivity.this, "Dahira " + dahira.getDahiraName() +
                        " n'a aucun evenement enregistre pour le moment", intent);
            }
        } else {
            if (notificationBody != null && notificationTitle != null)
                intent = new Intent(ListAnnouncementActivity.this, MainActivity.class);

            showAlertDialog(ListAnnouncementActivity.this, "Dahira " + dahira.getDahiraName() +
                    " n'a aucun evenement enregistre pour le moment", intent);
        }
        notificationTitle = null;
        notificationBody = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_menu, menu);

        MenuItem iconAdd;
        iconAdd = menu.findItem(R.id.icon_add);
        iconAdd.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.icon_add:
                actionSelected = "addNewAnnouncement";
                startActivity(new Intent(this, CreateAnnouncementActivity.class));
                break;
        }
        return true;
    }
}

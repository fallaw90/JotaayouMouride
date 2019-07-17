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

public class ListEventActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "ListEventActivity";

    private TextView textViewDahiraName;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerViewEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_event);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Liste des evenements");
        setSupportActionBar(toolbar);

        if (!isConnected(this)){
            Intent intent = new Intent(this, LoginActivity.class);
            logout();
            showAlertDialog(this,"Oops! Pas de connexion, verifier votre connexion internet puis reesayez SVP", intent);
        }

        recyclerViewEvent = findViewById(R.id.recyclerview_event);
        textViewDahiraName = findViewById(R.id.textView_dahiraName);
        textViewDahiraName.setText("Liste des evenements du dahira " + dahira.getDahiraName());

        showListEvents();

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

    private void showListEvents() {

        //Attach adapter to recyclerView
        Intent intent = new Intent(ListEventActivity.this, DahiraInfoActivity.class);
        if (event.getDahiraID() != null){
            if (event.getDahiraID().equals(dahira.getDahiraID())){
                recyclerViewEvent.setHasFixedSize(true);
                recyclerViewEvent.setLayoutManager(new LinearLayoutManager(this));
                recyclerViewEvent.setVisibility(View.VISIBLE);
                final EventAdapter eventAdapter = new EventAdapter(ListEventActivity.this,
                        event.getListUserName(), event.getListDate(), event.getListTitle(), event.getListNote(),
                        event.getListLocation(), event.getListStartTime(), event.getListEndTime());

                recyclerViewEvent.setAdapter(eventAdapter);
                eventAdapter.notifyDataSetChanged();
            }
            else {
                showAlertDialog(ListEventActivity.this, "Dahira " + dahira.getDahiraName() +
                        " n'a aucun evenement enregistre pour le moment", intent);
            }
        }
        else {
            showAlertDialog(ListEventActivity.this, "Dahira " + dahira.getDahiraName() +
                    " n'a aucun evenement enregistre pour le moment", intent);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list_event, menu);

        MenuItem menuItemAddEvent;

        menuItemAddEvent = menu.findItem(R.id.addEvent);
        menuItemAddEvent.setVisible(false);

        if (onlineUser.getListDahiraID().contains(dahira.getDahiraID())){
            if (onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur")){
                menuItemAddEvent.setVisible(true);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addEvent:
                actionSelected = "addNewEvent";
                startActivity(new Intent(this, CreateNewEventActivity.class));
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

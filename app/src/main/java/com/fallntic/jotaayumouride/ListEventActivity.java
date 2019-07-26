package com.fallntic.jotaayumouride;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.fallntic.jotaayumouride.CreateEventActivity.updateEvent;
import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.event;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;

public class ListEventActivity extends AppCompatActivity {
    private final String TAG = "ListEventActivity";

    private TextView textViewDahiraName;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerViewEvent;
    private final EventAdapter eventAdapter = new EventAdapter(ListEventActivity.this);
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_event);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Liste des evenements");
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
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

        recyclerViewEvent = findViewById(R.id.recyclerview_event);
        textViewDahiraName = findViewById(R.id.textView_dahiraName);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        textViewDahiraName.setText("Liste des evenements du dahira " + dahira.getDahiraName());

        showListEvents();
        enableSwipeToDeleteAndUndo();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    private void showListEvents() {

        //Attach adapter to recyclerView
        recyclerViewEvent.setHasFixedSize(true);
        recyclerViewEvent.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEvent.setVisibility(View.VISIBLE);

        recyclerViewEvent.setAdapter(eventAdapter);
        eventAdapter.notifyDataSetChanged();
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
                startActivity(new Intent(this, CreateEventActivity.class));
                break;
        }
        return true;
    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                final int position = viewHolder.getAdapterPosition();
                final String userName = event.getListUserName().get(position);
                final String mDate = event.getListDate().get(position);
                final String title = event.getListTitle().get(position);
                final String note = event.getListNote().get(position);
                final String location = event.getListLocation().get(position);
                final String startTime = event.getListStartTime().get(position);
                final String endTime = event.getListEndTime().get(position);
                final String userID = event.getListUserID().get(position);

                //Update totalAdiya dahira
                eventAdapter.removeItem(position);
                updateEvent(ListEventActivity.this);

                Snackbar snackbar = null;
                snackbar = Snackbar.make(coordinatorLayout,
                        "Depense supprime.", Snackbar.LENGTH_LONG);

                if (snackbar != null) {
                    snackbar.setAction("Annuler la suppression", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            eventAdapter.restoreItem(userName, mDate, title, note, location,
                                    startTime, endTime, userID, position);

                            updateEvent(ListEventActivity.this);

                            recyclerViewEvent.scrollToPosition(position);
                        }
                    });

                    snackbar.setActionTextColor(Color.YELLOW);
                    snackbar.show();
                }
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerViewEvent);
    }
}

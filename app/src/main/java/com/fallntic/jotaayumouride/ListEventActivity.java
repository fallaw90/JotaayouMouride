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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.CreateEventActivity.updateEvent;
import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.event;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.loadEvent;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.showProgressDialog;

public class ListEventActivity extends AppCompatActivity {
    private final String TAG = "ListEventActivity";

    private TextView textViewDahiraName;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static final String NODE_EVENTS = "events";
    private RecyclerView recyclerViewMyEvent;
    private CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerViewAllEvent;
    private FirebaseAuth mAuth;
    private List<Event> eventList;
    private AllEventAdapter allEventAdapter;
    private MyEventAdapter myEventAdapter;

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

        if (DataHolder.loadEvent.equals("myEvents")){
            loadMyEvents();
            enableSwipeToDeleteAndUndo();
        }
        else if (DataHolder.loadEvent.equals("allEvents")) {
            loadAllEvents();
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DataHolder.notificationTitle != null && DataHolder.notificationBody != null) {
                    DataHolder.notificationTitle = null;
                    DataHolder.notificationBody = null;
                    startActivity(new Intent(ListEventActivity.this, MainActivity.class));
                }
                else
                    finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (DataHolder.notificationTitle != null || DataHolder.notificationBody != null) {
            DataHolder.notificationTitle = null;
            DataHolder.notificationBody = null;
            startActivity(new Intent(ListEventActivity.this, MainActivity.class));
        }
        else if (loadEvent.equals("allEvents")){
            startActivity(new Intent(ListEventActivity.this, MainActivity.class));
        }
        else
            finish();
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    private void loadMyEvents() {

        recyclerViewMyEvent = findViewById(R.id.recyclerview_event);
        textViewDahiraName = findViewById(R.id.textView_dahiraName);
        textViewDahiraName.setText("Liste des evenements du dahira " + dahira.getDahiraName());

        //Attach adapter to recyclerView
        recyclerViewMyEvent.setHasFixedSize(true);
        recyclerViewMyEvent.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMyEvent.setVisibility(View.VISIBLE);

        myEventAdapter = new MyEventAdapter(this);
        recyclerViewMyEvent.setAdapter(myEventAdapter);
        myEventAdapter.notifyDataSetChanged();
    }

    private void loadAllEvents() {
        eventList = new ArrayList<>();
        recyclerViewAllEvent = findViewById(R.id.recyclerview_event);
        recyclerViewAllEvent.setLayoutManager(new LinearLayoutManager(this));

        showProgressDialog(this, "Chargement des evenements en cours ...");
        db.collection("events").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        dismissProgressDialog();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : list) {
                                Event event = documentSnapshot.toObject(Event.class);
                                eventList.add(event);
                            }

                            allEventAdapter = new AllEventAdapter(ListEventActivity.this, eventList);
                            recyclerViewAllEvent.setAdapter(allEventAdapter);
                        }
                        allEventAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_menu, menu);

        MenuItem iconAdd;
        iconAdd = menu.findItem(R.id.icon_add);
        if (loadEvent.equals("myEvents"))
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
                coordinatorLayout = findViewById(R.id.coordinatorLayout);

                //Update totalAdiya dahira
                myEventAdapter.removeItem(position);
                updateEvent(ListEventActivity.this);

                Snackbar snackbar = null;
                snackbar = Snackbar.make(coordinatorLayout,
                        "Depense supprime.", Snackbar.LENGTH_LONG);

                if (snackbar != null) {
                    snackbar.setAction("Annuler la suppression", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            myEventAdapter.restoreItem(userName, mDate, title, note, location,
                                    startTime, endTime, userID, position);

                            updateEvent(ListEventActivity.this);

                            recyclerViewMyEvent.scrollToPosition(position);
                        }
                    });

                    snackbar.setActionTextColor(Color.YELLOW);
                    snackbar.show();
                }
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerViewMyEvent);
    }
}

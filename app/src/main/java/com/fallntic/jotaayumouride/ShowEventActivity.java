package com.fallntic.jotaayumouride;

import android.content.Context;
import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Adapter.EventAdapter;
import com.fallntic.jotaayumouride.Model.Dahira;
import com.fallntic.jotaayumouride.Model.Event;
import com.fallntic.jotaayumouride.Utility.SwipeToDeleteCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;

import static com.fallntic.jotaayumouride.HomeActivity.loadInterstitialAd;
import static com.fallntic.jotaayumouride.Utility.DataHolder.dahira;
import static com.fallntic.jotaayumouride.Utility.DataHolder.deleteDocument;
import static com.fallntic.jotaayumouride.Utility.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.Utility.DataHolder.getCurrentDate;
import static com.fallntic.jotaayumouride.Utility.DataHolder.indexOnlineUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.displayEvent;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.firestore;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAllEvent;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.myListEvents;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.objNotification;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutProgressBar;

public class ShowEventActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "ShowEventActivity";

    private TextView textViewDahiraName;
    private TextView textViewDelete;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerViewMyEvent;
    private CoordinatorLayout coordinatorLayout;
    private EventAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_event);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setLogo(R.mipmap.logo);
        setSupportActionBar(toolbar);

        checkInternetConnection(this);

        if (objNotification != null) {
            getDahira(objNotification.getDahiraID());
            if (onlineUser.getListDahiraID().contains(objNotification.getDahiraID()))
                indexOnlineUser = onlineUser.getListDahiraID().indexOf(objNotification.getDahiraID());
        }

        initViews();
        loadEvents(this);

        loadInterstitialAd(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_back:
                finish();
                startActivity(new Intent(this, HomeActivity.class));
                break;
        }
    }

    private void initViews() {
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        textViewDelete = findViewById(R.id.textView_deleteInstruction);
        recyclerViewMyEvent = findViewById(R.id.recyclerview_event);
        textViewDahiraName = findViewById(R.id.textView_dahiraName);
        findViewById(R.id.button_back).setOnClickListener(this);
        initViewsProgressBar();
    }

    public  void initViewsProgressBar() {
        relativeLayoutData = findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        if (displayEvent.equals("allEvents") || objNotification != null){
            objNotification = null;
            startActivity(new Intent(ShowEventActivity.this, HomeActivity.class));
        }
        if (displayEvent.equals("myEvents") ){
            startActivity(new Intent(ShowEventActivity.this, DahiraInfoActivity.class));
        }
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    private void loadEvents(Context context) {

        if (myListEvents != null || listAllEvent != null) {

            sortByDate();
            removePastEvent();

            if (displayEvent.equals("myEvents")) {
                eventAdapter = new EventAdapter(this, myListEvents);
                textViewDahiraName.setText("Liste des evenements du dahira " + dahira.getDahiraName());
                enableSwipeToDelete();

            } else if (displayEvent.equals("allEvents")) {
                eventAdapter = new EventAdapter(this, listAllEvent);
                textViewDelete.setVisibility(View.GONE);
                textViewDahiraName.setText("Liste des evenements du dahira de tous les dahiras");
            }
            //Attach adapter to recyclerView
            recyclerViewMyEvent.setHasFixedSize(true);
            recyclerViewMyEvent.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewMyEvent.setVisibility(View.VISIBLE);

            recyclerViewMyEvent.setAdapter(eventAdapter);
            eventAdapter.notifyDataSetChanged();
        } else
            toastMessage(context, "myListEvents and listAllEvent null");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem iconAdd;
        iconAdd = menu.findItem(R.id.icon_add);
        if (displayEvent != null && displayEvent.equals("myEvents") && indexOnlineUser >= 0 && onlineUser.getListRoles()
                .get(indexOnlineUser).equals("Administrateur")) {

            iconAdd.setVisible(true);

        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.icon_add:
                if (objNotification != null) {
                    objNotification = null;
                }
                startActivity(new Intent(this, CreateEventActivity.class));
                break;

            case R.id.instructions:
                startActivity(new Intent(this, InstructionsActivity.class));
                break;
        }
        return true;
    }

    private void enableSwipeToDelete() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                final int position = viewHolder.getAdapterPosition();

                final Event event = myListEvents.get(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(ShowEventActivity.this, R.style.alertDialog);
                builder.setTitle("Supprimer evenement!");
                builder.setMessage("Etes vous sure de vouloir supprimer cet evenement?");
                builder.setCancelable(false);
                builder.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        eventAdapter.removeItem(position);
                        //Remove item in FirebaseFireStore

                        removeEvent(event);
                    }
                });

                builder.setNegativeButton("NON", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(ShowEventActivity.this,
                                ShowEventActivity.class));
                    }
                });
                builder.show();
            }

        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerViewMyEvent);
    }

    public void removeEvent(final Event event) {
        showProgressBar();
        db.collection("events").document(dahira.getDahiraID())
                .collection("myEvents").document(event.getEventID())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideProgressBar();
                        deleteDocument(ShowEventActivity.this, "events", event.getEventID());

                        Snackbar snackbar = Snackbar.make(coordinatorLayout,
                                "Evenement supprimee.", Snackbar.LENGTH_LONG);
                        snackbar.setActionTextColor(Color.GREEN);
                        snackbar.show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressBar();
                Snackbar snackbar = Snackbar.make(coordinatorLayout,
                        "Erreur de la suppression de l'evenement. " + e.getMessage(), Snackbar.LENGTH_LONG);
                snackbar.setActionTextColor(Color.GREEN);
                snackbar.show();
                startActivity(new Intent(ShowEventActivity.this, ShowEventActivity.class));
            }
        });
    }

    public void getDahira(final String dahiraID) {
        showProgressBar();
        firestore.collection("dahiras")
                .whereEqualTo("dahiraID", dahiraID).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        hideProgressBar();
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            dahira = documentSnapshot.toObject(Dahira.class);
                            break;
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideProgressBar();
                    }
                });
    }

    private void sortByDate(){
        Collections.sort(listAllEvent, new Comparator<Event>() {
            DateFormat f = new SimpleDateFormat("dd/MM/yyyy");
            @Override
            public int compare(Event event1, Event event2) {
                try {
                    return f.parse(event2.getDate()).compareTo(f.parse(event1.getDate()));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
    }

    private void removePastEvent(){
        DateFormat f = new SimpleDateFormat("dd/MM/yyyy");
        for (int i = 0; i < listAllEvent.size(); i++){
            try {
                int k = f.parse(listAllEvent.get(i).getDate()).compareTo(f.parse(getCurrentDate()));
                if (k < 0){
                    listAllEvent.remove(i);
                }
            } catch (ParseException e) {
                i--;
                throw new IllegalArgumentException(e);
            }
        }
    }
}

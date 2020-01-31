package com.fallntic.jotaayumouride;

import android.annotation.SuppressLint;
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

import com.fallntic.jotaayumouride.adapter.EventAdapter;
import com.fallntic.jotaayumouride.model.Event;
import com.fallntic.jotaayumouride.utility.SwipeToDeleteCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.deleteDocument;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.dismissProgressDialog;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.getCurrentDate;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.dahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.displayEvent;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.indexOnlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAllEvent;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.myListEvents;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.objNotification;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutProgressBar;

@SuppressWarnings("unused")
public class ShowEventActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "ShowEventActivity";

    private TextView textViewDahiraName;
    private TextView textViewDelete;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerViewMyEvent;
    private CoordinatorLayout coordinatorLayout;
    private EventAdapter eventAdapter;

    private static void sortEventByDate(ArrayList arrayList) {
        if (arrayList == null)
            arrayList = new ArrayList<>();
        Collections.sort(arrayList, new Comparator<Event>() {
            @SuppressLint("SimpleDateFormat")
            final DateFormat f = new SimpleDateFormat("dd/MM/yyyy");

            @Override
            public int compare(Event event1, Event event2) {
                try {
                    return Objects.requireNonNull(f.parse(event2.getDate())).compareTo(f.parse(event1.getDate()));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_event);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        //toolbar.setLogo(R.mipmap.logo);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.logo);

        checkInternetConnection(this);

        initViews();

        loadEvents(this);


        HomeActivity.loadBannerAd(this);
    }

    private void initViews() {
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        textViewDelete = findViewById(R.id.textView_deleteInstruction);
        recyclerViewMyEvent = findViewById(R.id.recyclerview_event);
        textViewDahiraName = findViewById(R.id.textView_dahiraName);
        findViewById(R.id.button_back).setOnClickListener(this);
        initViewsProgressBar();
    }

    private void initViewsProgressBar() {
        relativeLayoutData = findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    public void onBackPressed() {
        if (displayEvent.equals("allEvents") || objNotification != null){
            objNotification = null;
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
        if (displayEvent.equals("myEvents") ){
            startActivity(new Intent(ShowEventActivity.this, DahiraInfoActivity.class));
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.destroy();
        }
        dismissProgressDialog();
        super.onDestroy();
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
    public void onClick(View v) {
        if (v.getId() == R.id.button_back) {
            finish();
            if (displayEvent.equals("allEvents") || objNotification != null) {
                objNotification = null;
                startActivity(new Intent(ShowEventActivity.this, HomeActivity.class));
            }
            if (displayEvent.equals("myEvents")) {
                startActivity(new Intent(ShowEventActivity.this, DahiraInfoActivity.class));
            }
        }
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
            case android.R.id.home:
                finish();
                startActivity(new Intent(this, HomeActivity.class));
                break;

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

    private void removeEvent(final Event event) {
        showProgressBar();
        db.collection("dahiras").document(dahira.getDahiraID())
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

    @SuppressLint("SetTextI18n")
    private void loadEvents(Context context) {

        if (myListEvents != null || listAllEvent != null) {

            sortEventByDate((ArrayList) myListEvents);
            removePastEvent();

            if (displayEvent.equals("myEvents")) {
                eventAdapter = new EventAdapter(this, myListEvents);
                textViewDahiraName.setText("Liste des evenements du dahira " + dahira.getDahiraName());
                enableSwipeToDelete();

            } else if (displayEvent.equals("allEvents")) {
                sortEventByDate((ArrayList) listAllEvent);
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

    private void removePastEvent(){
        @SuppressLint("SimpleDateFormat") DateFormat f = new SimpleDateFormat("dd/MM/yyyy");
        for (int i = 0; i < listAllEvent.size(); i++){
            try {
                int k = Objects.requireNonNull(f.parse(listAllEvent.get(i).getDate())).compareTo(f.parse(getCurrentDate()));
                if (k < 0){
                    listAllEvent.remove(i);
                }
            } catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}
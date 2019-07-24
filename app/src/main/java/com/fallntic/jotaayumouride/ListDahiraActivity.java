package com.fallntic.jotaayumouride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fallntic.jotaayumouride.DataHolder.actionSelected;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.hasValidationErrors;
import static com.fallntic.jotaayumouride.DataHolder.indexOnlineUser;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.logout;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.showProfileImage;
import static com.fallntic.jotaayumouride.DataHolder.showProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.DataHolder.userID;

public class ListDahiraActivity extends AppCompatActivity implements View.OnClickListener,
        DrawerMenu, NavigationView.OnNavigationItemSelectedListener {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private RecyclerView recyclerViewDahira;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private View navHeader;
    private CircleImageView navImageView;
    private TextView textViewNavUserName;
    private TextView textViewNavEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_dahira);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        if (!isConnected(this)){
            finish();
            Intent intent = new Intent(this, LoginActivity.class);
            showAlertDialog(this,"Oops! Pas de connexion, " +
                    "verifier votre connexion internet puis reesayez SVP", intent);
        }

        recyclerViewDahira = findViewById(R.id.recyclerview_dahiras);

        //********************** Drawer Menu **************************
        setDrawerMenu();
        //*************************************************************

        if (actionSelected.equals("searchDahira")) {
            dialogSearchDahira();
            actionSelected = "allDahira";
        }

        if (actionSelected.equals("myDahira")){
            toolbar.setSubtitle("Mes dahiras");
            getMyDahiras();
        }

        if (actionSelected.equals("allDahira")){
            toolbar.setSubtitle("Liste des dahiras a Dakar");
            getAllDahiras();
        }

        findViewById(R.id.button_backToProfile).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        actionSelected = "";
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        actionSelected = "";
        startActivity(new Intent(this, ProfileActivity.class));
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){

            case R.id.button_backToProfile:
                actionSelected = "";
                finish();
                startActivity(new Intent(ListDahiraActivity.this, ProfileActivity.class));
                break;
        }
    }

    private void getMyDahiras() {

        //Attach adapter to recyclerView
        recyclerViewDahira.setHasFixedSize(true);
        recyclerViewDahira.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDahira.setVisibility(View.VISIBLE);
        final List<Dahira> dahiraList = new ArrayList<>();
        final DahiraAdapter dahiraAdapter = new DahiraAdapter(this, dahiraList);
        recyclerViewDahira.setAdapter(dahiraAdapter);
        showProgressDialog(this, "Chargement de vos dahiras ...");
        db.collection("dahiras").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        dismissProgressDialog();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : list) {
                                //documentSnapshot = dahira in list
                                Dahira dahira = documentSnapshot.toObject(Dahira.class);
                                if (onlineUser.getListDahiraID().contains(dahira.getDahiraID())){
                                    dahiraList.add(dahira);
                                }
                            }
                            dahiraAdapter.notifyDataSetChanged();
                        }
                        else {
                            toastMessage(getApplicationContext(),"Vous n'etes associe a auccun dahira!");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissProgressDialog();
                        toastMessage(getApplicationContext(),"Error charging dahira!");
                    }
                });
    }

    private void getAllDahiras() {

        //Attach adapter to recyclerView
        recyclerViewDahira.setHasFixedSize(true);
        recyclerViewDahira.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDahira.setVisibility(View.VISIBLE);
        final List<Dahira> dahiraList = new ArrayList<>();
        final DahiraAdapter dahiraAdapter = new DahiraAdapter(this, dahiraList);
        recyclerViewDahira.setAdapter(dahiraAdapter);

        showProgressDialog(this, "Chargement de vos dahiras ...");
        db.collection("dahiras").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        dismissProgressDialog();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : list) {
                                //documentSnapshot = dahira in list
                                Dahira dahira = documentSnapshot.toObject(Dahira.class);
                                dahira.setDahiraID(documentSnapshot.getId());
                                dahiraList.add(dahira);
                            }
                            dahiraAdapter.notifyDataSetChanged();
                        }
                        else {
                            toastMessage(getApplicationContext(),"Vous n'etes associe a auccun dahira!");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissProgressDialog();
                        toastMessage(getApplicationContext(),"Error charging dahira!");
                    }
                });
    }

    private void searchDahira(final String name, final String phoneNumber) {

        //Attach adapter to recyclerView
        recyclerViewDahira.setHasFixedSize(true);
        recyclerViewDahira.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDahira.setVisibility(View.VISIBLE);
        final List<Dahira> dahiraList = new ArrayList<>();
        final DahiraAdapter dahiraAdapter = new DahiraAdapter(this, dahiraList);
        recyclerViewDahira.setAdapter(dahiraAdapter);

        showProgressDialog(this, "Recherche du dahira en cours ...");
        db.collection("dahiras").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        dismissProgressDialog();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : list) {
                                //documentSnapshot equals dahira in list
                                Dahira dahira = documentSnapshot.toObject(Dahira.class);
                                if (name != null && !name.equals("")){
                                    if (name.equals(dahira.getDahiraName())){
                                        dahiraList.add(dahira);
                                    }
                                    else {
                                        String[] splitSearchName = name.split(" ");
                                        String dahiraName = dahira.getDahiraName();
                                        dahiraName = dahiraName.toLowerCase();
                                        for (String name : splitSearchName){
                                            name = name.toLowerCase();
                                            if(dahiraName.contains(name)){
                                                dahiraList.add(dahira);
                                            }
                                        }
                                    }
                                }
                                if (phoneNumber != null && !phoneNumber.equals("")) {
                                    if (phoneNumber.equals(dahira.getDahiraPhoneNumber())) {
                                        dahiraList.add(dahira);
                                    }
                                }
                            }

                            if (dahiraList.isEmpty()){
                                Intent intent = new Intent(ListDahiraActivity.this, ListDahiraActivity.class);
                                showAlertDialog(ListDahiraActivity.this, "Dahira non trouve.", intent);
                            }
                            else {
                                dahiraAdapter.notifyDataSetChanged();
                            }
                            dismissProgressDialog();
                        }
                        else {
                            dismissProgressDialog();
                            Intent intent = new Intent(ListDahiraActivity.this, ListDahiraActivity.class);
                            showAlertDialog(ListDahiraActivity.this, "Dahira non trouve.", intent);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissProgressDialog();
                        toastMessage(getApplicationContext(), "Error search dahira in ListDahiraActivity!");
                    }
                });
    }

    private void dialogSearchDahira() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_search, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        final EditText editTextDialogName = (EditText) dialogView.findViewById(R.id.editText_dialogName);
        final EditText editTextDialogPhoneNumber = (EditText) dialogView.findViewById(R.id.editText_dialogPhoneNumber);
        Button buttonSearch = (Button) dialogView.findViewById(R.id.button_dialogSearch);
        Button buttonCancel = (Button) dialogView.findViewById(R.id.button_cancel);

        editTextDialogName.setHint("Nom du dahira");
        editTextDialogPhoneNumber.setHint("Numero telephone du dahira");

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setTitle("Rechercher un dahira");
        alertDialog.show();

        buttonSearch.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = editTextDialogName.getText().toString().trim();
                String phoneNumber = editTextDialogPhoneNumber.getText().toString().trim();

                if(!hasValidationErrors(userName, editTextDialogName, phoneNumber, editTextDialogPhoneNumber)){
                    searchDahira(userName, phoneNumber);
                    alertDialog.dismiss();
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_profile:
                startActivity(new Intent(this, ProfileActivity.class));
                break;

            case R.id.nav_displayMyDahira:
                actionSelected = "myDahira";
                startActivity(new Intent(this, ListDahiraActivity.class));
                break;

            case R.id.nav_addDahira:
                startActivity(new Intent(this, CreateDahiraActivity.class));
                break;

            case R.id.nav_displayAllDahira:
                actionSelected = "allDahira";
                startActivity(new Intent(this, ListDahiraActivity.class));
                break;

            case R.id.nav_searchDahira:
                actionSelected = "searchDahira";
                startActivity(new Intent(this, ListDahiraActivity.class));
                break;

            case R.id.nav_setting:
                startActivity(new Intent(this, SettingProfileActivity.class));
                break;

            case R.id.nav_logout:
                toastMessage(this, "Logged out");
                logout(this);
                finish();
                startActivity(new Intent(this, MainActivity.class));
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.icon_add:
                startActivity(new Intent(this, CreateDahiraActivity.class));
                break;
        }
        return true;
    }

    public void setDrawerMenu(){
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navHeader = navigationView.getHeaderView(0);
        navImageView = navHeader.findViewById(R.id.nav_imageView);
        textViewNavUserName = (TextView) navHeader.findViewById(R.id.textView_navUserName);
        textViewNavEmail = (TextView) navHeader.findViewById(R.id.textView_navEmail);
        toggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        showProfileImage(this, userID, navImageView);
        textViewNavUserName.setText(onlineUser.getUserName());
        textViewNavEmail.setText(onlineUser.getEmail());

        if (actionSelected.equals("myDahira"))
            navigationView.setCheckedItem(R.id.nav_displayMyDahira);
        else if (actionSelected.equals("allDahira"))
            navigationView.setCheckedItem(R.id.nav_displayAllDahira);
        else if (actionSelected.equals("searchDahira"))
            navigationView.setCheckedItem(R.id.nav_searchDahira);
        else if (actionSelected.equals("searchUser"))
            navigationView.setCheckedItem(R.id.nav_searchUser);

        hideMenuItem();
    }

    @Override
    public void hideMenuItem() {
        Menu nav_Menu = navigationView.getMenu();
        nav_Menu.findItem(R.id.nav_setting).setTitle("Modifier mon profil");

        nav_Menu.findItem(R.id.nav_displayUsers).setVisible(false);
        nav_Menu.findItem(R.id.nav_addUser).setVisible(false);
        nav_Menu.findItem(R.id.nav_searchUser).setVisible(false);

        nav_Menu.findItem(R.id.nav_finance).setVisible(false);
        nav_Menu.findItem(R.id.nav_gallery).setVisible(false);
        nav_Menu.findItem(R.id.nav_release).setVisible(false);
        nav_Menu.findItem(R.id.nav_contact).setVisible(false);
    }
}

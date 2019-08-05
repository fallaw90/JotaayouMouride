package com.fallntic.jotaayumouride;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Utility.MyStaticVariables;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fallntic.jotaayumouride.DataHolder.actionSelected;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.logout;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.showImage;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.MainActivity.progressBar;
import static com.fallntic.jotaayumouride.MainActivity.relativeLayoutProgressBar;

public class ListDahiraActivity extends AppCompatActivity implements DrawerMenu,
        NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerViewDahira;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private View navHeader;
    private CircleImageView navImageView;
    private TextView textViewNavUserName;
    private TextView textViewNavEmail;
    DahiraAdapter dahiraAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_dahira);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!isConnected(this)) {
            finish();
            Intent intent = new Intent(this, LoginActivity.class);
            showAlertDialog(this, "Oops! Pas de connexion, " +
                    "verifier votre connexion internet puis reesayez SVP", intent);
        }

        init();

        //********************** Drawer Menu **************************
        setDrawerMenu();
        //*************************************************************

        if (actionSelected.equals("searchDahira")) {
            dialogSearchDahira();
            actionSelected = "";
        }

        if (MyStaticVariables.displayDahira.equals("myDahira")) {
            toolbar.setSubtitle("Mes dahiras");
            displayMyDahiras();
        }

        if (MyStaticVariables.displayDahira.equals("allDahira")) {
            toolbar.setSubtitle("Liste des dahiras a Dakar");
            displayAllDahiras();
        }
    }

    private void init() {

        recyclerViewDahira = findViewById(R.id.recyclerview_dahiras);

        //ProgressBar from static variable MainActivity
        relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        progressBar = findViewById(R.id.progressBar);
        relativeLayoutProgressBar.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        actionSelected = "";
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(this, ProfileActivity.class));
    }

    private void displayMyDahiras() {
        if (MyStaticVariables.myListDahira.size() > 0) {
            //Attach adapter to recyclerView
            recyclerViewDahira.setHasFixedSize(true);
            recyclerViewDahira.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewDahira.setVisibility(View.VISIBLE);
            dahiraAdapter = new DahiraAdapter(this, MyStaticVariables.myListDahira);
            recyclerViewDahira.setAdapter(dahiraAdapter);
            dahiraAdapter.notifyDataSetChanged();
        } else {
            showAlertDialog(ListDahiraActivity.this, "Vous n'etes membre d'un " +
                    " aucun dahira pour le moment. Contactez l'administrateur de votre dahira " +
                    "pour vous ajouter en tant que membre. Ou bien, creer un dahira si vous etes " +
                    "administrateur.");
        }
    }

    private void displayAllDahiras() {
        if (MyStaticVariables.allListDahira.size() > 0) {
            //Attach adapter to recyclerView
            recyclerViewDahira.setHasFixedSize(true);
            recyclerViewDahira.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewDahira.setVisibility(View.VISIBLE);
            dahiraAdapter = new DahiraAdapter(this, MyStaticVariables.allListDahira);
            recyclerViewDahira.setAdapter(dahiraAdapter);
            dahiraAdapter.notifyDataSetChanged();
        } else {
            showAlertDialog(ListDahiraActivity.this, "Auccun dahira n'est " +
                    "enregiste dans le plateforme pour le moment, creer un dahira si vous etes " +
                    "administrateur.");
        }
    }

    private void searchDahira(final String searchName) {

        //Attach adapter to recyclerView
        recyclerViewDahira.setHasFixedSize(true);
        recyclerViewDahira.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDahira.setVisibility(View.VISIBLE);
        final List<Dahira> dahiraList = new ArrayList<>();
        final DahiraAdapter dahiraAdapter = new DahiraAdapter(this, dahiraList);
        recyclerViewDahira.setAdapter(dahiraAdapter);

        for (Dahira dahira : MyStaticVariables.allListDahira) {
            if (searchName != null && !searchName.equals("")) {
                String[] splitSearchName = searchName.split(" ");
                String dahiraName = dahira.getDahiraName();
                dahiraName = dahiraName.toLowerCase();
                for (String search : splitSearchName) {
                    search = search.toLowerCase();
                    if (dahiraName.contains(search)) {
                        dahiraList.add(dahira);
                    }
                }
            }
        }
        if (dahiraList.isEmpty()) {
            Intent intent = new Intent(ListDahiraActivity.this, ListDahiraActivity.class);
            showAlertDialog(ListDahiraActivity.this, "Dahira non trouve.", intent);
        } else {
            Intent intent = new Intent(ListDahiraActivity.this, ListDahiraActivity.class);
            showAlertDialog(ListDahiraActivity.this, "Dahira non trouve.", intent);
        }
    }

    private void dialogSearchDahira() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_search, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        final EditText editTextDialogName = dialogView.findViewById(R.id.editText_dialogName);
        final EditText editTextDialogPhoneNumber = dialogView.findViewById(R.id.editText_dialogPhoneNumber);
        final TextView textView = dialogView.findViewById(R.id.textView_dialogOr);
        Button buttonSearch = dialogView.findViewById(R.id.button_dialogSearch);
        Button buttonCancel = dialogView.findViewById(R.id.button_cancel);

        editTextDialogName.setHint("Nom du dahira");
        editTextDialogPhoneNumber.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setTitle("Rechercher un dahira");
        alertDialog.show();

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dahiraName = editTextDialogName.getText().toString().trim();

                if (dahiraName.isEmpty()) {
                    editTextDialogName.setError("Donner le nom du dahira!");
                    editTextDialogName.requestFocus();
                    return;
                } else {
                    searchDahira(dahiraName);
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

            case R.id.nav_home:
                startActivity(new Intent(this, MainActivity.class));
                break;

            case R.id.nav_profile:
                startActivity(new Intent(this, ProfileActivity.class));
                break;

            case R.id.nav_displayMyDahira:
                MyStaticVariables.displayDahira = "myDahira";
                startActivity(new Intent(this, ListDahiraActivity.class));
                break;

            case R.id.nav_addDahira:
                startActivity(new Intent(this, CreateDahiraActivity.class));
                break;

            case R.id.nav_displayAllDahira:
                MyStaticVariables.displayDahira = "allDahira";
                startActivity(new Intent(this, ListDahiraActivity.class));
                break;

            case R.id.nav_searchDahira:
                actionSelected = "searchDahira";
                MyStaticVariables.displayDahira = "allDahira";
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

        MenuItem iconBack;
        iconBack = menu.findItem(R.id.icon_back);

        iconBack.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.icon_back:
                finish();
                startActivity(new Intent(this, ProfileActivity.class));
                break;
        }
        return true;
    }

    public void setDrawerMenu() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
        navHeader = navigationView.getHeaderView(0);
        navImageView = navHeader.findViewById(R.id.nav_imageView);
        textViewNavUserName = navHeader.findViewById(R.id.textView_navUserName);
        textViewNavEmail = navHeader.findViewById(R.id.textView_navEmail);
        toggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        showImage(this, "profileImage", onlineUser.getUserID(), navImageView);
        textViewNavUserName.setText(onlineUser.getUserName());
        textViewNavEmail.setText(onlineUser.getEmail());

        if (MyStaticVariables.displayDahira.equals("myDahira"))
            navigationView.setCheckedItem(R.id.nav_displayMyDahira);
        else if (MyStaticVariables.displayDahira.equals("allDahira"))
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

        nav_Menu.findItem(R.id.nav_home).setVisible(false);

        nav_Menu.findItem(R.id.nav_displayUsers).setVisible(false);
        nav_Menu.findItem(R.id.nav_addUser).setVisible(false);
        nav_Menu.findItem(R.id.nav_searchUser).setVisible(false);

        nav_Menu.findItem(R.id.nav_finance).setVisible(false);
        nav_Menu.findItem(R.id.nav_gallery).setVisible(false);
        nav_Menu.findItem(R.id.nav_release).setVisible(false);
        nav_Menu.findItem(R.id.nav_contact).setVisible(false);
    }
}

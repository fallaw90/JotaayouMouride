package com.fallntic.jotaayumouride;

import android.content.Context;
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

import com.fallntic.jotaayumouride.Adapter.DahiraAdapter;
import com.fallntic.jotaayumouride.Model.Dahira;
import com.fallntic.jotaayumouride.Utility.MyStaticVariables;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fallntic.jotaayumouride.Utility.DataHolder.actionSelected;
import static com.fallntic.jotaayumouride.Utility.DataHolder.logout;
import static com.fallntic.jotaayumouride.Utility.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.Utility.DataHolder.showImage;
import static com.fallntic.jotaayumouride.Utility.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.displayDahira;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAllDahira;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listDahiraFound;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.myListDahira;

public class ListDahiraActivity extends AppCompatActivity implements DrawerMenu,
        NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private static RecyclerView recyclerViewDahira;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private View navHeader;
    private CircleImageView navImageView;
    private TextView textViewNavUserName;
    private TextView textViewNavEmail;
    private DahiraAdapter dahiraAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_dahira);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkInternetConnection(this);

        init();

        //********************** Drawer Menu **************************
        setDrawerMenu();
        //*************************************************************

        if (displayDahira.equals("searchDahira")) {
            displayDahiras(listDahiraFound);
        }

        else if (displayDahira.equals("myDahira")) {
            displayDahiras(myListDahira);
        }

        else if (displayDahira.equals("allDahira")) {
            displayDahiras(listAllDahira);
        }
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

    private void init() {

        recyclerViewDahira = findViewById(R.id.recyclerview_dahiras);
        findViewById(R.id.button_back).setOnClickListener(this);
        //ProgressBar from static variable MainActivity
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
        startActivity(new Intent(this, HomeActivity.class));
    }

    private void displayDahiras(List<Dahira> listDahira) {
        if (listDahira.size() > 0) {
            //Attach adapter to recyclerView
            recyclerViewDahira.setHasFixedSize(true);
            recyclerViewDahira.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewDahira.setVisibility(View.VISIBLE);
            dahiraAdapter = new DahiraAdapter(this, listDahira);
            recyclerViewDahira.setAdapter(dahiraAdapter);
            dahiraAdapter.notifyDataSetChanged();
        }
    }

    public static void searchDahira(Context context, final String searchName) {

        if (listDahiraFound == null)
            listDahiraFound = new ArrayList<>();

        if (listAllDahira == null) {
            listAllDahira = new ArrayList<>();
        }

        for (Dahira dahira : listAllDahira) {
            if (searchName != null && !searchName.equals("")) {
                String[] splitSearchName = searchName.split(" ");
                String dahiraName = dahira.getDahiraName();
                dahiraName = dahiraName.toLowerCase();
                for (String search : splitSearchName) {
                    search = search.toLowerCase();
                    if (dahiraName.contains(search)) {
                        listDahiraFound.add(dahira);
                    }
                }
            }
        }
        if (listDahiraFound.isEmpty()) {
            showAlertDialog(context, "Dahira non trouve.");
        } else {
            displayDahira = "searchDahira";
            Intent intent = new Intent(context, ListDahiraActivity.class);
            context.startActivity(intent);
        }
    }

    public static void dialogSearchDahira(final Context context) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
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
                    searchDahira(context, dahiraName);
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
                startActivity(new Intent(this, HomeActivity.class));
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
                dialogSearchDahira(this);
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
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem iconLogo;
        iconLogo = menu.findItem(R.id.logo);

        iconLogo.setVisible(true);

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
                startActivity(new Intent(this, HomeActivity.class));
                break;

            case R.id.instructions:
                startActivity(new Intent(this, InstructionsActivity.class));
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

        if (actionSelected == null)
            actionSelected = "";

        if (MyStaticVariables.displayDahira.equals("myDahira"))
            navigationView.setCheckedItem(R.id.nav_displayMyDahira);
        else if (MyStaticVariables.displayDahira.equals("allDahira"))
            navigationView.setCheckedItem(R.id.nav_displayAllDahira);
        else if (displayDahira.equals("searchDahira"))
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

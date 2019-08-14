package com.fallntic.jotaayumouride;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
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

import com.fallntic.jotaayumouride.Adapter.UserAdapter;
import com.fallntic.jotaayumouride.Model.User;
import com.fallntic.jotaayumouride.Utility.MyStaticVariables;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fallntic.jotaayumouride.CreateDahiraActivity.getCurrentCountryCode;
import static com.fallntic.jotaayumouride.Utility.DataHolder.actionSelected;
import static com.fallntic.jotaayumouride.Utility.DataHolder.call;
import static com.fallntic.jotaayumouride.Utility.DataHolder.dahira;
import static com.fallntic.jotaayumouride.Utility.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.Utility.DataHolder.hasValidationErrors;
import static com.fallntic.jotaayumouride.Utility.DataHolder.hasValidationErrorsSearch;
import static com.fallntic.jotaayumouride.Utility.DataHolder.indexOnlineUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.logout;
import static com.fallntic.jotaayumouride.Utility.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.selectedUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.Utility.DataHolder.showImage;
import static com.fallntic.jotaayumouride.Utility.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.*;

public class ListUserActivity extends AppCompatActivity implements DrawerMenu,
        NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private TextView textViewDahiraname;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private RecyclerView recyclerViewUser;
    private UserAdapter userAdapter;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private View navHeader;
    private CircleImageView navImageView;
    private TextView textViewNavUserName;
    private TextView textViewNavEmail;

    public static ScrollView scrollView;

    @Override
    protected void onDestroy() {
        actionSelected = "";
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        actionSelected = "";
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            startActivity(new Intent(this, DahiraInfoActivity.class));
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initViews();

        //********************** Drawer Menu **************************
        setDrawerMenu();
        //*************************************************************

        checkInternetConnection(this);

        textViewDahiraname.setText("Dahira " + dahira.getDahiraName() + "\nListe de tous les membres");

        showListUser();

        if (actionSelected.equals("addNewMember")) {
            addNewMember();
        } else if (actionSelected.equals("searchUser")) {
            dialogSearchUser();
        }
    }

    private void initViews(){
        recyclerViewUser = findViewById(R.id.recyclerview_users);
        scrollView = findViewById(R.id.scrollView);
        textViewDahiraname = findViewById(R.id.textView_dahiraName);

        findViewById(R.id.button_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_back:
                finish();
                actionSelected = "";
                startActivity(new Intent(this, DahiraInfoActivity.class));
                break;
        }
    }

    private void showListUser() {

        Collections.sort(listUser);
        //Attach adapter to recyclerView
        recyclerViewUser.setHasFixedSize(true);
        recyclerViewUser.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUser.setVisibility(View.VISIBLE);
        userAdapter = new UserAdapter(this, listUser);
        recyclerViewUser.setAdapter(userAdapter);
    }

    private void addNewMember() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_member, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        final EditText editTextPhoneNumber = dialogView.findViewById(R.id.editText_dialogPhoneNumber);
        final EditText editTextEmail = dialogView.findViewById(R.id.editText_dialogEmail);

        Button buttonAdd = dialogView.findViewById(R.id.button_dialogAdd);
        Button buttonCancel = dialogView.findViewById(R.id.button_dialogCancel);

        dialogBuilder.setTitle("Ajouter un nouveau membre");
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionSelected = "";
                String phoneNumber = editTextPhoneNumber.getText().toString().trim();
                String email = editTextEmail.getText().toString().trim();

                if (!hasValidationErrorsSearch(phoneNumber, editTextPhoneNumber, email, editTextEmail)) {

                    if (!phoneNumber.isEmpty()) {
                        phoneNumber = "221" + phoneNumber;
                        getNewMemberToUpdate("userPhoneNumber", phoneNumber);
                    } else if (!email.isEmpty()) {
                        getNewMemberToUpdate("email", email);
                    }

                    alertDialog.dismiss();
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionSelected = "";
                startActivity(new Intent(ListUserActivity.this, DahiraInfoActivity.class));
                alertDialog.dismiss();
            }
        });
    }

    public void getNewMemberToUpdate(String field, String value) {
        //showProgressBar();
        db.collection("users").whereEqualTo(field, value).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        //hideProgressBar();
                        if (queryDocumentSnapshots.isEmpty()) {
                            showAlertDialog(ListUserActivity.this,
                                    "Utilisateur inconnu!\n Pour ajouter un membre, " +
                                            "assurez vous que la personne s'est inscrit d'abord.");
                        } else {
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                User newMember = documentSnapshot.toObject(User.class);
                                if (newMember.getListDahiraID().contains(dahira.getDahiraID())) {
                                    showAlertDialog(ListUserActivity.this,
                                            "Cet utilisateur est deja membre du dahira " +
                                                    dahira.getDahiraName() + ".");
                                    return;
                                } else {
                                    updateNewMember(newMember);
                                    updateDahira();
                                    break;
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //hideProgressBar();
                        toastMessage(getApplicationContext(), "Error add new user!");
                        return;
                    }
                });
    }

    private void updateNewMember(User user) {

        user.getListDahiraID().add(dahira.getDahiraID());
        user.getListUpdatedDahiraID().add(dahira.getDahiraID());
        user.getListRoles().add("N/A");
        user.getListCommissions().add("N/A");
        user.getListAdiya().add("00");
        user.getListSass().add("00");
        user.getListSocial().add("00");

        //showProgressBar();
        db.collection("users").document(user.getUserID())
                .update("listDahiraID", user.getListDahiraID(),
                        "listUpdatedDahiraID", user.getListUpdatedDahiraID(),
                        "listCommissions", user.getListCommissions(),
                        "listAdiya", user.getListAdiya(),
                        "listSass", user.getListSass(),
                        "listSocial", user.getListSocial(),
                        "listRoles", user.getListRoles())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                       // hideProgressBar();
                        System.out.println("User updated");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
               // hideProgressBar();
            }
        });
    }

    private void updateDahira() {
        int totalMember = Integer.parseInt(dahira.getTotalMember());
        totalMember++;
        dahira.setTotalMember(Integer.toString(totalMember));

        //showProgressBar();
        db.collection("dahiras").document(dahira.getDahiraID())
                .update("totalMember", dahira.getTotalMember())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //hideProgressBar();
                        Intent intent = new Intent(ListUserActivity.this, ListUserActivity.class);
                        showAlertDialog(ListUserActivity.this, "Votre nouveau membre a ete ajoute avec succes. \n Selectionnez le sur la liste des membres pour mettre a jour son profil.", intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
               // hideProgressBar();
            }
        });
    }

    private void dialogSearchUser() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_search, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        final EditText editTextDialogName = dialogView.findViewById(R.id.editText_dialogName);
        final EditText editTextDialogPhoneNumber = dialogView.findViewById(R.id.editText_dialogPhoneNumber);
        Button buttonSearch = dialogView.findViewById(R.id.button_dialogSearch);
        Button buttonCancel = dialogView.findViewById(R.id.button_cancel);

        editTextDialogName.setHint("Nom du membre");
        editTextDialogPhoneNumber.setHint("Numero telephone du membre");

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setTitle("Rechercher un membre");
        alertDialog.show();

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = editTextDialogName.getText().toString().trim();
                String phoneNumber = editTextDialogPhoneNumber.getText().toString().trim();

                if (!hasValidationErrors(userName, editTextDialogName, phoneNumber, editTextDialogPhoneNumber)) {
                    phoneNumber = getCurrentCountryCode(ListUserActivity.this) + phoneNumber;
                    searchUser(userName, phoneNumber);
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
            case R.id.instructions:
                startActivity(new Intent(this, InstructionsActivity.class));
                break;

            case R.id.icon_back:
                finish();
                actionSelected = "";
                startActivity(new Intent(this, HomeActivity.class));
                break;
        }

        return true;
    }

    private void searchUser(final String name, final String phoneNumber) {

        //Attach adapter to recyclerView
        recyclerViewUser.setHasFixedSize(true);
        recyclerViewUser.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUser.setVisibility(View.VISIBLE);
        final List<User> listUsers = new ArrayList<>();
        final UserAdapter userAdapter = new UserAdapter(this, listUsers);
        recyclerViewUser.setAdapter(userAdapter);

        //showProgressBar();
        db.collection("users").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        //hideProgressBar();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : list) {
                                //documentSnapshot equals dahira in list
                                User member = documentSnapshot.toObject(User.class);
                                if (member.getListDahiraID().contains(dahira.getDahiraID())) {
                                    if (name != null && !name.equals("") && member.getUserID() != null) {
                                        if (name.equals(member.getUserName())) {
                                            listUsers.add(member);
                                        } else {
                                            String[] splitSearchName = name.split(" ");
                                            String userName = member.getUserName();
                                            userName = userName.toLowerCase();
                                            for (String name : splitSearchName) {
                                                name = name.toLowerCase();
                                                if (userName.contains(name)) {
                                                    listUsers.add(member);
                                                }
                                            }
                                        }
                                    }
                                    if (phoneNumber != null && !phoneNumber.equals("")) {
                                        if (phoneNumber.equals(member.getUserPhoneNumber())) {
                                            listUsers.add(member);
                                        }
                                    }
                                }
                            }

                            if (listUsers.isEmpty()) {
                                Intent intent = new Intent(ListUserActivity.this, ListUserActivity.class);
                                showAlertDialog(ListUserActivity.this, "Membre non trouve!", intent);
                            } else {
                                userAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Intent intent = new Intent(ListUserActivity.this, ListUserActivity.class);
                            showAlertDialog(ListUserActivity.this, "Membre non trouve!", intent);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                       // hideProgressBar();
                        toastMessage(getApplicationContext(), "Error search dahira in ListUserActivity!");
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                call(this, this, selectedUser.getUserPhoneNumber());
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.nav_home:
                startActivity(new Intent(this, HomeActivity.class));
                break;

            case R.id.nav_displayUsers:
                startActivity(new Intent(this, ListUserActivity.class));
                break;

            case R.id.nav_searchUser:
                dialogSearchUser();
                break;

            case R.id.nav_addUser:
                addNewMember();
                break;

            case R.id.nav_displayMyDahira:
                MyStaticVariables.displayDahira = "myDahira";
                startActivity(new Intent(this, ListDahiraActivity.class));
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

            case R.id.nav_addExpense:
                actionSelected = "addNewExpense";
                startActivity(new Intent(this, CreateExpenseActivity.class));
                break;

            case R.id.nav_displayExpenses:
                if (listExpenses == null) {
                    showAlertDialog(this, "La liste des depenses de votre dahira est vide!");
                } else
                    startActivity(new Intent(this, ListExpenseActivity.class));
                break;

            case R.id.nav_addAnnouncement:
                actionSelected = "addNewAnnouncement";
                startActivity(new Intent(this, CreateAnnouncementActivity.class));
                break;

            case R.id.nav_addEvent:
                actionSelected = "addNewEvent";
                startActivity(new Intent(this, CreateEventActivity.class));
                break;

            case R.id.nav_displayEvent:
                if (myListEvents == null) {
                    navigationView.setCheckedItem(R.id.nav_displayEvent);
                    showAlertDialog(this, "La liste de vos evenements est vide!");
                } else
                    startActivity(new Intent(this, ListEventActivity.class));
                break;

            case R.id.nav_photo:
                startActivity(new Intent(this, ShowImagesActivity.class));
                break;

            case R.id.nav_audio:
                startActivity(new Intent(this, ShowSongsActivity.class));
                break;

            case R.id.nav_callDahira:
                navigationView.setCheckedItem(R.id.nav_callDahira);
                call(this, this, dahira.getDahiraPhoneNumber());
                break;

            case R.id.nav_setting:
                startActivity(new Intent(this, UpdateDahiraActivity.class));
                break;

            case R.id.nav_logout:
                logout(this);
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
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
        navigationView.setCheckedItem(R.id.nav_displayUsers);
        hideMenuItem();
    }

    @Override
    public void hideMenuItem() {
        Menu nav_Menu = navigationView.getMenu();
        nav_Menu.findItem(R.id.nav_setting).setTitle("Modifier mon dahira");

        if (!onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur")) {
            nav_Menu.findItem(R.id.nav_addUser).setVisible(false);
            nav_Menu.findItem(R.id.nav_addExpense).setVisible(false);
            nav_Menu.findItem(R.id.nav_addEvent).setVisible(false);
            nav_Menu.findItem(R.id.nav_setting).setVisible(false);
        }

        nav_Menu.findItem(R.id.nav_addContribution).setVisible(false);
        nav_Menu.findItem(R.id.nav_displayAdiya).setVisible(false);
        nav_Menu.findItem(R.id.nav_displaySass).setVisible(false);
        nav_Menu.findItem(R.id.nav_displaySocial).setVisible(false);
        nav_Menu.findItem(R.id.nav_callUser).setVisible(false);
        nav_Menu.findItem(R.id.nav_searchDahira).setVisible(false);
        nav_Menu.findItem(R.id.nav_video).setVisible(false);
        nav_Menu.findItem(R.id.nav_removeDahira).setVisible(false);
    }
}

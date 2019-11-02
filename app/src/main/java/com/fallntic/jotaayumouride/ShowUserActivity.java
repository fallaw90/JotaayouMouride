package com.fallntic.jotaayumouride;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Patterns;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Adapter.UserAdapter;
import com.fallntic.jotaayumouride.Model.User;
import com.fallntic.jotaayumouride.Utility.MyStaticVariables;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.fallntic.jotaayumouride.DahiraInfoActivity.getListUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.actionSelected;
import static com.fallntic.jotaayumouride.Utility.DataHolder.call;
import static com.fallntic.jotaayumouride.Utility.DataHolder.dahira;
import static com.fallntic.jotaayumouride.Utility.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.Utility.DataHolder.indexOnlineUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.selectedUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.Utility.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listUser;

public class ShowUserActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewDahiraname;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private RecyclerView recyclerViewUser;
    private UserAdapter userAdapter;


    public static ScrollView scrollView;

    @Override
    protected void onDestroy() {
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.destroy();
        }
        actionSelected = "";
        dismissProgressDialog();
        super.onDestroy();
    }

    public static boolean hasValidationErrorsSearch(String phoneNumber, EditText editTextPhoneNumber,
                                                    String email, EditText editTextEmail) {

        if (phoneNumber.isEmpty() && email.isEmpty()) {
            editTextPhoneNumber.setError("Entrer un numero ou une adresse email");
            editTextPhoneNumber.requestFocus();
            return true;
        }

        if (!phoneNumber.isEmpty() && (!phoneNumber.contains("+"))) {
            editTextPhoneNumber.setError("Inclure l'indicatif svp (exemple: +221771866656)");
            editTextPhoneNumber.requestFocus();
            return true;
        }

        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Adresse email incorrect");
            editTextEmail.requestFocus();
            return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        actionSelected = "";
        finish();
        startActivity(new Intent(this, DahiraInfoActivity.class));
    }

    private void initViews() {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setLogo(R.mipmap.logo);
        setSupportActionBar(toolbar);

        initViews();

        checkInternetConnection(this);

        textViewDahiraname.setText("Dahira " + dahira.getDahiraName() + "\nListe de tous les membres");


        if (actionSelected.equals("addNewMember")) {
            addNewMember();
        } else if (actionSelected.equals("searchUser")) {
            dialogSearchUser();
        } else if (actionSelected.equals("displayUsers"))
            showListUser();


        HomeActivity.loadBannerAd(this, this);
    }

    private void showListUser() {
        if (listUser == null)
            listUser = new ArrayList<>();

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
        final CountryCodePicker ccp = dialogView.findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(editTextPhoneNumber);

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

                if (!phoneNumber.isEmpty()) {
                    if (phoneNumber.contains("+")) {
                        editTextPhoneNumber.setError("Ne pas inclure l'indicatif svp.");
                        editTextPhoneNumber.requestFocus();
                        return;
                    } else {
                        phoneNumber = ccp.getFullNumberWithPlus();
                        getNewMemberToUpdate("userPhoneNumber", phoneNumber);
                        alertDialog.dismiss();
                    }
                } else if (!email.isEmpty()) {
                    if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        getNewMemberToUpdate("email", email);
                        alertDialog.dismiss();
                    } else {
                        editTextEmail.setError("Adresse email incorrect");
                        editTextEmail.requestFocus();
                        return;
                    }
                } else {
                    toastMessage(ShowUserActivity.this, "Entrez un numero de telephone ou un adresse email svp.");
                    return;
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionSelected = "";
                startActivity(new Intent(ShowUserActivity.this, DahiraInfoActivity.class));
                alertDialog.dismiss();
            }
        });
    }

    public void getNewMemberToUpdate(String field, String value) {
        //showProgressBar();
        actionSelected = "displayUsers";
        final Intent intent = new Intent(ShowUserActivity.this, ShowUserActivity.class);
        db.collection("users").whereEqualTo(field, value).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        //hideProgressBar();
                        if (queryDocumentSnapshots.isEmpty()) {
                            showAlertDialog(ShowUserActivity.this,
                                    "Utilisateur inconnu!\n Pour ajouter un membre, " +
                                            "assurez vous que la personne s'est inscrit d'abord.", intent);
                        } else {
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                User newMember = documentSnapshot.toObject(User.class);
                                if (newMember.getListDahiraID().contains(dahira.getDahiraID())) {
                                    showAlertDialog(ShowUserActivity.this,
                                            "Cet utilisateur est deja membre du dahira " +
                                                    dahira.getDahiraName() + ".", intent);
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

    private void updateNewMember(final User user) {

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
                        if (listUser == null)
                            listUser = new ArrayList<>();
                        listUser.add(user);
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
                        Intent intent = new Intent(ShowUserActivity.this, ShowUserActivity.class);
                        showAlertDialog(ShowUserActivity.this, "Votre nouveau membre a ete ajoute avec succes. \n Selectionnez le sur la liste des membres pour mettre a jour son profil.", intent);
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
        final CountryCodePicker ccp;
        ccp = dialogView.findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(editTextDialogPhoneNumber);

        editTextDialogName.setHint("Nom du membre");

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setTitle("Rechercher un membre");
        alertDialog.show();

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = editTextDialogName.getText().toString().trim();
                String phoneNumber = editTextDialogPhoneNumber.getText().toString().trim();

                if (!phoneNumber.isEmpty()) {
                    if (phoneNumber.contains("+")) {
                        editTextDialogPhoneNumber.setError("Ne pas inclure l'indicatif svp.");
                        editTextDialogPhoneNumber.requestFocus();
                        return;
                    } else {
                        phoneNumber = ccp.getFullNumberWithPlus();
                        searchUser("", phoneNumber);
                        alertDialog.dismiss();
                    }
                } else if (!userName.isEmpty()) {
                    searchUser(userName, "");
                    alertDialog.dismiss();
                } else {
                    toastMessage(ShowUserActivity.this, "Entrez le nom du membre ou son numero de telephone svp.");
                    return;
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionSelected = "displayUsers";
                startActivity(new Intent(ShowUserActivity.this, ShowUserActivity.class));
                alertDialog.dismiss();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem iconAdd, search_user;
        iconAdd = menu.findItem(R.id.icon_add);
        search_user = menu.findItem(R.id.search_user);

        search_user.setVisible(true);
        if (onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur")) {
            iconAdd.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.instructions:
                startActivity(new Intent(this, InstructionsActivity.class));
                break;

            case R.id.search_user:
                actionSelected = "searchUser";
                MyStaticVariables.displayDahira = "allDahira";
                getListUser(ShowUserActivity.this);
                break;

            case R.id.icon_add:
                actionSelected = "addNewMember";
                getListUser(this);
                startActivity(new Intent(this, ShowUserActivity.class));
                break;
        }

        return true;
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
                                Intent intent = new Intent(ShowUserActivity.this, ShowUserActivity.class);
                                showAlertDialog(ShowUserActivity.this, "Membre non trouve!", intent);
                            } else {
                                userAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Intent intent = new Intent(ShowUserActivity.this, ShowUserActivity.class);
                            showAlertDialog(ShowUserActivity.this, "Membre non trouve!", intent);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // hideProgressBar();
                        toastMessage(getApplicationContext(), "Error search dahira in ShowUserActivity!");
                    }
                });
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
}

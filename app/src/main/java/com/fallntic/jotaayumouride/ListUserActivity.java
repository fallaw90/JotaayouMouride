package com.fallntic.jotaayumouride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.showProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.DataHolder.user;

public class ListUserActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewDahiraname;
    private List<User> listUser;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private RecyclerView recyclerViewUser;
    private UserAdapter userAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Liste des membre du dahira ");
        setSupportActionBar(toolbar);

        if (!DataHolder.isConnected(this)){
            toastMessage(this, "Oops! Vous n'avez pas de connexion internet.");
            finish();
        }

        recyclerViewUser = findViewById(R.id.recyclerview_users);

        textViewDahiraname = findViewById(R.id.textView_dahiraName);
        textViewDahiraname.setText("Dahira " + dahira.getDahiraName());

        showListUser();

        findViewById(R.id.button_backToProfile).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){

            case R.id.button_backToProfile:
                startActivity(new Intent(ListUserActivity.this, ProfileActivity.class));
                break;
        }
    }

    private void showListUser() {

        //Attach adapter to recyclerView
        recyclerViewUser.setHasFixedSize(true);
        recyclerViewUser.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUser.setVisibility(View.VISIBLE);
        listUser = new ArrayList<>();
        userAdapter = new UserAdapter(this, listUser);
        recyclerViewUser.setAdapter(userAdapter);

        showProgressDialog(this, "Chargement des membres en cours ...");
        db.collection("users").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        dismissProgressDialog();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : list) {

                                User user = documentSnapshot.toObject(User.class);
                                for (String id_dahira : user.getListDahiraID()) {
                                    if (id_dahira.equals(dahira.getDahiraID())) {
                                        listUser.add(user);
                                    }
                                }
                            }
                            userAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuItem menuAddMember, menuSetting;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list_user, menu);

        menuAddMember = menu.findItem(R.id.addMember);
        menuSetting = menu.findItem(R.id.setting);
        int index = user.getListDahiraID().indexOf(dahira.getDahiraID());
        if (!user.getListRoles().get(index).equals("Administrateur")){
            menuAddMember.setVisible(false);
            menuSetting.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addUser:
                addNewMember();
                break;

            case R.id.searchUser:
                dialogSearchUser();
                break;
            case R.id.logout:
                logout();
                finish();
                startActivity(new Intent(this, MainActivity.class));
                break;
        }
        return true;
    }

    private void addNewMember() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_member, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        final EditText editTextPhoneNumber = (EditText) dialogView.findViewById(R.id.editText_dialogPhoneNumber);
        final EditText editTextEmail = (EditText) dialogView.findViewById(R.id.editText_dialogEmail);

        Button buttonAdd = (Button) dialogView.findViewById(R.id.button_dialogAdd);
        Button buttonCancel = (Button) dialogView.findViewById(R.id.button_dialogCancel);

        dialogBuilder.setTitle("Ajouter un nouveau membre");
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonAdd.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = editTextPhoneNumber.getText().toString().trim();
                String email = editTextEmail.getText().toString().trim();

                if(!hasValidationErrors(editTextPhoneNumber, editTextEmail, phoneNumber, email)){

                    if (!phoneNumber.isEmpty()) {
                        getNewMemberToUpdate("userPhoneNumber", phoneNumber);
                    }
                    else if (!email.isEmpty()) {
                        getNewMemberToUpdate("email", email);
                    }

                    alertDialog.dismiss();
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ListUserActivity.this, DahiraInfoActivity.class));
                alertDialog.dismiss();
            }
        });
    }

    public void getNewMemberToUpdate(String field, String value) {
        showProgressDialog(this,"Chargement de vos informations ...");
        db.collection("users").whereEqualTo(field, value).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        dismissProgressDialog();
                        if (queryDocumentSnapshots.isEmpty()){
                            toastMessage(getApplicationContext(),"Utilisateur inconnu! SVP dites a votre membre de s'inscrire d'abord");
                        }
                        else{
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                User newMember = documentSnapshot.toObject(User.class);
                                if (newMember.getListDahiraID().contains(dahira.getDahiraID())){
                                    toastMessage(getApplicationContext(),newMember.getUserName() + " figure deja dans la liste de vos membre.");
                                    return;
                                }
                                else {
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
                        dismissProgressDialog();
                        toastMessage(getApplicationContext(),"Error add new user!");
                        return;
                    }
                });
    }

    private void updateDahira(){
        showProgressDialog(this,"Mis a jour de votre dahira");
        int totalMember = Integer.parseInt(dahira.getTotalMember());
        totalMember++;
        dahira.setTotalMember(Integer.toString(totalMember));

        db.collection("dahiras").document(dahira.getDahiraID())
                .update("totalMember", dahira.getTotalMember())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dismissProgressDialog();
                        toastMessage(getApplicationContext(), "Dahira updated.");
                    }
                });
    }

    private void updateNewMember(User user){
        showProgressDialog(this,"Ajout du nouveau membre en cours ...");
        user.getListDahiraID().add(dahira.getDahiraID());
        user.getListUpdatedDahiraID().add(dahira.getDahiraID());
        user.getListRoles().add("N/A");
        user.getListCommissions().add("N/A");
        user.getListAdiya().add("N/A");
        user.getListSass().add("N/A");
        user.getListSocial().add("N/A");

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
                        dismissProgressDialog();
                        toastMessage(getApplicationContext(),"Membre ajoute avec succes.");
                    }
                });
    }

    private void searchUser(final String name, final String phoneNumber) {

        //Attach adapter to recyclerView
        recyclerViewUser.setHasFixedSize(true);
        recyclerViewUser.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUser.setVisibility(View.VISIBLE);
        final List<User> listUsers = new ArrayList<>();
        final UserAdapter userAdapter = new UserAdapter(this, listUsers);
        recyclerViewUser.setAdapter(userAdapter);

        showProgressDialog(this, "Recherche du membre en cours ...");
        db.collection("users").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        dismissProgressDialog();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : list) {
                                //documentSnapshot equals dahira in list
                                User member = documentSnapshot.toObject(User.class);
                                if (name != null && !name.equals("")){
                                    if (name.equals(member.getUserName())){
                                        listUsers.add(member);
                                    }
                                    else {
                                        String[] splitSearchName = name.split(" ");
                                        String dahiraName = member.getUserName();
                                        dahiraName = dahiraName.toLowerCase();
                                        for (String name : splitSearchName){
                                            name = name.toLowerCase();
                                            if(dahiraName.contains(name)){
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

                            if (listUsers.isEmpty()){
                                toastMessage(getApplicationContext(), "Membre non trouve!");
                                startActivity(new Intent(ListUserActivity.this, ListUserActivity.class));
                            }
                            else {
                                userAdapter.notifyDataSetChanged();
                            }
                            dismissProgressDialog();
                        }
                        else {
                            dismissProgressDialog();
                            toastMessage(getApplicationContext(),"Membre non trouve!");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissProgressDialog();
                        toastMessage(getApplicationContext(),"Error search dahira in ListUserActivity!");
                    }
                });
    }

    private void dialogSearchUser() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_search, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        final EditText editTextDialogName = (EditText) dialogView.findViewById(R.id.editText_dialogName);
        final EditText editTextDialogPhoneNumber = (EditText) dialogView.findViewById(R.id.editText_dialogPhoneNumber);
        Button buttonSearch = (Button) dialogView.findViewById(R.id.button_dialogSearch);
        Button buttonCancel = (Button) dialogView.findViewById(R.id.button_cancel);

        editTextDialogName.setHint("Nom du membre");
        editTextDialogPhoneNumber.setHint("Numero telephone du membre");

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setTitle("Rechercher un membre");
        alertDialog.show();

        buttonSearch.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = editTextDialogName.getText().toString().trim();
                String phoneNumber = editTextDialogPhoneNumber.getText().toString().trim();

                if(!hasValidationErrors(userName, editTextDialogName, phoneNumber, editTextDialogPhoneNumber)){
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

    private boolean hasValidationErrors(EditText editTextPhoneNumber, EditText editTextEmail,
                                        String phoneNumber, String email) {

        if (phoneNumber.isEmpty() && email.isEmpty()) {
            editTextPhoneNumber.setError("Entrer le numero ou l'adresse email du membre");
            editTextPhoneNumber.requestFocus();
            return true;
        }

        if(!phoneNumber.isEmpty() && (!phoneNumber.matches("[0-9]+") || phoneNumber.length() != 9 || !checkPrefix(phoneNumber))) {
            editTextPhoneNumber.setError("Numero de telephone incorrect");
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

    private boolean hasValidationErrors(String name, EditText editTextName, String phoneNumber, EditText editTextPhoneNumber) {

        if (name.isEmpty() && phoneNumber.isEmpty()) {
            if (name.isEmpty()) {
                editTextName.setError("Entrer le nom du dahira!");
                editTextName.requestFocus();
                return true;
            }
            else {
                if (phoneNumber.isEmpty()) {
                    editTextPhoneNumber.setError("Entrer le numero du membre!");
                    editTextPhoneNumber.requestFocus();
                    return true;
                }
            }
        }

        if(!phoneNumber.isEmpty() && (!phoneNumber.matches("[0-9]+") || phoneNumber.length() != 9 || !checkPrefix(phoneNumber))) {
            editTextPhoneNumber.setError("Numero de telephone incorrect");
            editTextPhoneNumber.requestFocus();
            return true;
        }

        return false;
    }

    public boolean checkPrefix(String str){
        String prefix = str.substring(0,2);
        boolean validatePrefix;
        switch(prefix){
            case "70":
                validatePrefix = true;
                break;
            case "76":
                validatePrefix = true;
                break;
            case "77":
                validatePrefix = true;
                break;
            case "78":
                validatePrefix = true;
                break;
            default:
                validatePrefix = false;
                break;
        }

        return validatePrefix;
    }

    public void logout(){
        user = null;
        dahira = null;
        FirebaseAuth.getInstance().signOut();
    }

}

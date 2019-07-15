package com.fallntic.jotaayumouride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.hasValidationErrors;
import static com.fallntic.jotaayumouride.DataHolder.hasValidationErrorsSearch;
import static com.fallntic.jotaayumouride.DataHolder.logout;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.showProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;

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
        toolbar.setSubtitle("Liste des membres");
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

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list_user, menu);

        MenuItem menuAddMember;
        menuAddMember = menu.findItem(R.id.addUser);
        int indexOnlineUser = onlineUser.getListDahiraID().indexOf(dahira.getDahiraID());
        if (!onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur")){
            menuAddMember.setVisible(false);
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

                if(!hasValidationErrorsSearch(phoneNumber, editTextPhoneNumber, email, editTextEmail)){

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
                            showAlertDialog(ListUserActivity.this,
                                    "Utilisateur inconnu!\n Pour ajouter un membre, " +
                                            "assurez vous que l'utisateur s'est deja inscrit d'abord.");
                        }
                        else{
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                User newMember = documentSnapshot.toObject(User.class);
                                if (newMember.getListDahiraID().contains(dahira.getDahiraID())){
                                    showAlertDialog(ListUserActivity.this,
                                            "Cet utilisateur est deja membre du dahira " +
                                                    dahira.getDahiraName() + ".");
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

    private void updateNewMember(User user){
        showProgressDialog(this,"Ajout du nouveau membre en cours ...");
        user.getListDahiraID().add(dahira.getDahiraID());
        user.getListUpdatedDahiraID().add(dahira.getDahiraID());
        user.getListRoles().add("N/A");
        user.getListCommissions().add("N/A");
        user.getListAdiya().add("00");
        user.getListSass().add("00");
        user.getListSocial().add("00");

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
                        System.out.println("User updated");
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
                        Intent intent = new Intent(ListUserActivity.this, ListUserActivity.class);
                        showAlertDialog(ListUserActivity.this, "Votre nouveau membre a ete ajoute avec succes. \n Selectionnez le sur la liste des membres pour mettre a jour son profil.", intent);
                    }
                });
        dismissProgressDialog();
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
                                if (name != null && !name.equals("") && member.getUserID() != null){
                                    if (name.equals(member.getUserName())){
                                        listUsers.add(member);
                                    }
                                    else {
                                        String[] splitSearchName = name.split(" ");
                                        String userName = member.getUserName();
                                        userName = userName.toLowerCase();
                                        for (String name : splitSearchName){
                                            name = name.toLowerCase();
                                            if(userName.contains(name)){
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
                                Intent intent = new Intent(ListUserActivity.this, ListUserActivity.class);
                                showAlertDialog(ListUserActivity.this, "Membre non trouve!", intent);
                            }
                            else {
                                userAdapter.notifyDataSetChanged();
                            }
                            dismissProgressDialog();
                        }
                        else {
                            dismissProgressDialog();
                            Intent intent = new Intent(ListUserActivity.this, ListUserActivity.class);
                            showAlertDialog(ListUserActivity.this,"Membre non trouve!", intent);
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
}

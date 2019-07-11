package com.fallntic.jotaayumouride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.showProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.user;

public class DahiraInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewDahiraName, textViewDieuwrine, textViewSiege, textViewtotalMembers,
            textViewTotalAdiya, textViewTotalSass, textViewTotalSocial, textViewPhoneNumber;

    private ImageView imageView;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private int index;
    private Menu menu;
    private MenuItem menuAddMember, menuSetting;

    User newMember = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dahira_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Votre Dahira");
        setSupportActionBar(toolbar);

        if (!DataHolder.isConnected(this)){
            toastMessage("Oops! Vous n'avez pas de connexion internet!");
            finish();
        }

        textViewDahiraName = (TextView) findViewById(R.id.textView_dahiraName);
        textViewDieuwrine = (TextView) findViewById(R.id.textView_dieuwrine);
        textViewSiege = (TextView) findViewById(R.id.textView_siege);
        textViewPhoneNumber = (TextView) findViewById(R.id.textView_dahiraPhoneNumber);
        textViewTotalAdiya = (TextView) findViewById(R.id.textView_totalAdiya);
        textViewTotalSass = (TextView) findViewById(R.id.textView_totalSass);
        textViewTotalSocial = (TextView) findViewById(R.id.textView_totalSocial);
        textViewtotalMembers = (TextView) findViewById(R.id.textView_totalMembers);
        imageView = (ImageView) findViewById(R.id.imageView);

        textViewDahiraName.setText("Dahira " + dahira.getDahiraName());
        textViewDieuwrine.setText("Dieuwrine " + dahira.getDieuwrine());
        textViewSiege.setText("Siege " + dahira.getSiege());
        textViewPhoneNumber.setText("Telephone: " + dahira.getDahiraPhoneNumber());
        textViewtotalMembers.setText("Nombre de participant: " + dahira.getTotalMember());
        textViewTotalAdiya.setText("Total Adiya dans la caisse: " + dahira.getTotalAdiya() + " FCFA");
        textViewTotalSass.setText("Total Sass dans la caisse: " + dahira.getTotalSass() + " FCFA");
        textViewTotalSocial.setText("Total Social dans la caisse: " + dahira.getTotalSocial() + " FCFA");

        DataHolder.showLogoDahira(this, imageView);

        findViewById(R.id.button_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_back:
                finish();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dahira_info, menu);

        menuAddMember = menu.findItem(R.id.addMember);
        menuSetting = menu.findItem(R.id.setting);
        index = user.getListDahiraID().indexOf(dahira.getDahiraID()); 
        if (!user.getListRoles().get(index).equals("Administrateur")){
            menuAddMember.setVisible(false);
            menuSetting.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addMember:
                addNewMember();
                break;

            case R.id.displayMember:
                startActivity(new Intent(this, ListUserActivity.class));
                break;

            case R.id.displayAnnounce:
                //Code here
                break;

            case R.id.displayEvent:
                //More code here
                break;

            case R.id.setting:
                startActivity(new Intent(this, UpdateDahiraActivity.class));
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
                startActivity(new Intent(DahiraInfoActivity.this, DahiraInfoActivity.class));
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
                            toastMessage("Utilisateur inconnu! SVP dites a votre membre de s'inscrire d'abord");
                        }
                        else{
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                newMember = documentSnapshot.toObject(User.class);
                                if (newMember.getListDahiraID().contains(dahira.getDahiraID())){
                                    toastMessage(newMember.getUserName() + " figure deja dans la liste de vos membre.");
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
                        toastMessage("Error add new user!");
                        return;
                    }
                });
    }

    private void updateDahira(){
        showProgressDialog(this,"Mis a jour de votre dahira");
        int totalMember = Integer.parseInt(dahira.getTotalMember());
        totalMember++;
        dahira.setTotalMember(Integer.toString(totalMember));

        toastMessage(dahira.getTotalMember());

        db.collection("dahiras").document(dahira.getDahiraID())
                .update("totalMember", dahira.getTotalMember())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dismissProgressDialog();
                        toastMessage("Dahira updated.");
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
                        toastMessage("Membre ajoute avec succes.");
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

        if(!phoneNumber.isEmpty() && (!phoneNumber.matches("[0-9]+") || phoneNumber.length() != 9)) {
            editTextPhoneNumber.setError("Numero de telephone incorrect");
            editTextPhoneNumber.requestFocus();
            return true;
        }
        if(!phoneNumber.isEmpty()) {
            String prefix = phoneNumber.substring(0, 2);
            boolean validatePrefix;
            switch (prefix) {
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
            if (!validatePrefix) {
                editTextPhoneNumber.setError("Numero de telephone incorrect");
                editTextPhoneNumber.requestFocus();
                return true;
            }
        }

        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Adresse email incorrect");
            editTextEmail.requestFocus();
            return true;
        }

        return false;
    }

    public void logout(){
        user = null;
        dahira = null;
        FirebaseAuth.getInstance().signOut();
    }

    public void toastMessage(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}

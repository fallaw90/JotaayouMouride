package com.fallntic.jotaayumouride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import static com.fallntic.jotaayumouride.DataHolder.hasValidationErrorsSearch;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.logout;
import static com.fallntic.jotaayumouride.DataHolder.showProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;

public class DahiraInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewDahiraName, textViewDieuwrine, textViewSiege, textViewtotalMembers,
            textViewTotalAdiya, textViewTotalSass, textViewTotalSocial, textViewPhoneNumber;

    private ImageView imageView;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    int indexOnlineUser = onlineUser.getListDahiraID().indexOf(dahira.getDahiraID());
    User newMember = new User();
    private boolean imageSaved = true, dahiraSaved = true, dahiraUpdated = true, userSaved = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dahira_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Mon Dahira");
        setSupportActionBar(toolbar);

        if (!DataHolder.isConnected(this)){
            toastMessage(getApplicationContext(),"Oops! Vous n'avez pas de connexion internet!");
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
        textViewDieuwrine.setText("Dieuwrine: " + dahira.getDieuwrine());
        textViewSiege.setText("Siege: " + dahira.getSiege());
        textViewPhoneNumber.setText("Telephone: " + dahira.getDahiraPhoneNumber());
        textViewtotalMembers.setText("Nombre de participant: " + dahira.getTotalMember());
        textViewTotalAdiya.setText("Total Adiya dans la caisse: " + dahira.getTotalAdiya() + " FCFA");
        textViewTotalSass.setText("Total Sass dans la caisse: " + dahira.getTotalSass() + " FCFA");
        textViewTotalSocial.setText("Total Social dans la caisse: " + dahira.getTotalSocial() + " FCFA");

        DataHolder.showLogoDahira(this, imageView);

        findViewById(R.id.button_back).setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(DahiraInfoActivity.this, ListDahiraActivity.class));
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_back:
                startActivity(new Intent(this, ListDahiraActivity.class));
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dahira_info, menu);

        MenuItem menuItemAddMember, menuItemSetting, menuItemDisplayMember, menuItemCreateAnnounce,
                menuItemCreateEvent;

        menuItemAddMember = menu.findItem(R.id.addMember);
        menuItemSetting = menu.findItem(R.id.setting);
        menuItemCreateAnnounce = menu.findItem(R.id.createAnnounce);
        menuItemCreateEvent = menu.findItem(R.id.createEvent);
        menuItemDisplayMember = menu.findItem(R.id.displayMember);

        if (onlineUser.getListDahiraID().contains(dahira.getDahiraID())){
            if (!onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur")){
                menuItemAddMember.setVisible(false);
                menuItemSetting.setVisible(false);
                menuItemCreateAnnounce.setVisible(false);
                menuItemCreateEvent.setVisible(false);
            }
        }
        else {
            menuItemAddMember.setVisible(false);
            menuItemSetting.setVisible(false);
            menuItemCreateAnnounce.setVisible(false);
            menuItemDisplayMember.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addMember:
                alertDialogAddNewMember();
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

            case R.id.createAnnounce:
                //Code here
                break;

            case R.id.createEvent:
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

    private void alertDialogAddNewMember() {

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
                    if (userSaved && dahiraUpdated){
                        showAlertDialog(DahiraInfoActivity.this,
                                newMember.getUserName() + " a ete ajoute avec succes.");
                    }
                    else {
                        showAlertDialog(DahiraInfoActivity.this,
                                "Erreur service reseau!\n Membre non ajoute");
                    }
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
                            toastMessage(getApplicationContext(),"Utilisateur inconnu! SVP dites a votre membre de s'inscrire d'abord");
                        }
                        else{
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                newMember = documentSnapshot.toObject(User.class);
                                if (newMember.getListDahiraID().contains(dahira.getDahiraID())){
                                    showAlertDialog(DahiraInfoActivity.this,
                                            newMember.getUserName() + " figure deja dans la liste de vos membres.");
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
                        dismissProgressDialog();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        userSaved = false;
                        dismissProgressDialog();
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
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dahiraUpdated = false;
                        dismissProgressDialog();
                    }
                });
    }
}

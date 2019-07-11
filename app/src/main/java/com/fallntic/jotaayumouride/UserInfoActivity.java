package com.fallntic.jotaayumouride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.showProfileImage;
import static com.fallntic.jotaayumouride.DataHolder.showProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.DataHolder.user;

public class UserInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewName;
    private TextView textViewDahiraName;
    private TextView textViewAdress;
    private TextView textViewPhoneNumber;
    private TextView textViewEmail;
    private TextView textViewRole;
    private TextView textViewCommission;
    private TextView textViewAdiya;
    private TextView textViewSass;
    private TextView textViewSocial;
    private ImageView imageView;

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    boolean boolAdiya = false, boolSass = false, boolSocial = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Info du membre");
        setSupportActionBar(toolbar);

        if (!DataHolder.isConnected(this)){
            toastMessage(this,"Oops! Vous n'avez pas de connexion internet!");
            finish();
        }

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        textViewName = (TextView) findViewById(R.id.textView_userName);
        textViewDahiraName = (TextView) findViewById(R.id.textView_dahiraName);
        textViewPhoneNumber = (TextView) findViewById(R.id.textView_phoneNumber);
        textViewAdress = (TextView) findViewById(R.id.textView_address);
        textViewEmail = (TextView) findViewById(R.id.textView_email);
        textViewCommission = (TextView) findViewById(R.id.textView_commission);
        textViewRole = (TextView) findViewById(R.id.textView_role);
        textViewAdiya = (TextView) findViewById(R.id.totalAdiya);
        textViewAdiya = (TextView) findViewById(R.id.textView_commission);
        textViewSass = (TextView) findViewById(R.id.totalSass);
        textViewSocial = (TextView) findViewById(R.id.totalSocial);
        imageView = (ImageView) findViewById(R.id.imageView);

        int index = user.getListDahiraID().indexOf(dahira.getDahiraID());

        showProfileImage(this, imageView);

        textViewName.setText(user.getUserName());
        textViewDahiraName.setText(dahira.getDahiraName());
        textViewPhoneNumber.setText(user.getUserPhoneNumber());
        textViewAdress.setText(user.getAddress());
        textViewEmail.setText(user.getEmail());
        textViewCommission.setText(user.getListCommissions().get(index));
        textViewAdiya.setText(user.getListAdiya().get(index));
        textViewSass.setText(user.getListSocial().get(index));
        textViewSocial.setText(user.getListSass().get(index));
        textViewRole.setText(user.getListRoles().get(index));

        findViewById(R.id.button_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button_back:
                startActivity(new Intent(UserInfoActivity.this, ListUserActivity.class));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_user_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.addContributions:
                chooseContribution();
                break;

            case R.id.setting:
                startActivity(new Intent(this, SettingUserActivity.class));
                break;

            case R.id.logout:
                logout();
                finish();
                startActivity(new Intent(this, MainActivity.class));
                break;
        }
        return true;
    }

    public void logout(){
        user = null;
        dahira = null;
        FirebaseAuth.getInstance().signOut();
    }

    private void chooseContribution() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_choose_contribution, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        final Button buttonAddAdiya = (Button) dialogView.findViewById(R.id.button_dialogAddAdiya);
        final Button buttonAddSass = (Button) dialogView.findViewById(R.id.button_dialogAddSass);
        final Button buttonAddSocial = (Button) dialogView.findViewById(R.id.button_dialogAddSocial);
        final Button buttonCancel = (Button) dialogView.findViewById(R.id.button_dialogCancel);

        dialogBuilder.setTitle("Ajouter une contribution");
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonAddAdiya.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolAdiya = true;
                addContribution();
                alertDialog.dismiss();
            }
        });

        buttonAddSass.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolSass = true;
                addContribution();
                alertDialog.dismiss();
            }
        });

        buttonAddSocial.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolSocial = true;
                addContribution();
                alertDialog.dismiss();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

    }

    private void addContribution() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_contribution, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        final EditText editTextDialogContribution = (EditText) dialogView.findViewById(R.id.editText_dialogContribution);
        Button buttonSave = (Button) dialogView.findViewById(R.id.button_dialogSave);
        Button buttonCancel = (Button) dialogView.findViewById(R.id.button_cancel);

        if (boolAdiya){
            dialogBuilder.setTitle(user.getUserName() + "\ndahira " + dahira.getDahiraName());
            editTextDialogContribution.setHint("Montant adiya (Chiffre seulement)");
        }
        if (boolSass){
            dialogBuilder.setTitle("Ajouter adiya pour " + user.getUserName() + " membre du dahira " + dahira.getDahiraName());
            editTextDialogContribution.setHint("Montant adiya (Chiffre seulement)");
        }
        if (boolSocial){
            dialogBuilder.setTitle("Ajouter adiya pour " + user.getUserName() + " membre du dahira " + dahira.getDahiraName());
            editTextDialogContribution.setHint("Montant adiya (Chiffre seulement)");
        }

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonSave.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = user.getListDahiraID().indexOf(dahira.getDahiraID());
                String value = editTextDialogContribution.getText().toString().trim();
                value = value.replace(",", ".");

                if(!hasValidationErrors(editTextDialogContribution, value)){

                    if (boolAdiya){
                        double totalAdiyaUser = Double.parseDouble(user.getListAdiya().get(index));
                        double totalAdiyaDahira = Double.parseDouble(dahira.getTotalAdiya());
                        totalAdiyaUser += Double.parseDouble(value);
                        totalAdiyaDahira += Double.parseDouble(value);
                        dahira.setTotalAdiya(Double.toString(totalAdiyaDahira));
                        user.getListAdiya().set(index, Double.toString(totalAdiyaUser));
                        updateContribution("listAdiya", user.getListAdiya(), "adiya");
                        updateDahira("totalAdiya", dahira.getTotalAdiya());
                    }

                    if (boolSass){
                        double totalSassUser = Double.parseDouble(user.getListSass().get(index));
                        double totalSassDahira = Double.parseDouble(dahira.getTotalSass());
                        totalSassUser += Double.parseDouble(value);
                        totalSassDahira += Double.parseDouble(value);
                        dahira.setTotalAdiya(Double.toString(totalSassDahira));
                        user.getListSass().set(index, Double.toString(totalSassUser));
                        updateContribution("listSass", user.getListSass(), "sass");
                        updateDahira("totalSass", dahira.getTotalSass());
                    }

                    if (boolSocial){
                        double totalSocialUser = Double.parseDouble(user.getListSocial().get(index));
                        double totalSocialDahira = Double.parseDouble(dahira.getTotalSocial());
                        totalSocialUser += Double.parseDouble(value);
                        totalSocialDahira += Double.parseDouble(value);
                        dahira.setTotalSocial(Double.toString(totalSocialDahira));
                        user.getListSocial().set(index, Double.toString(totalSocialUser));
                        updateContribution("listSocial", user.getListSocial(), "social");
                        updateDahira("totalSocial", dahira.getTotalSocial());
                    }

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

    private void updateContribution(String field, List<String> value, final String typeContribution){

        showProgressDialog(this, "Enregistrement " + typeContribution + " en cours ...");
        db.collection("users").document(user.getUserID())
                .update(field, value)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dismissProgressDialog();
                        toastMessage(getApplicationContext(),typeContribution + " ajoute avec succes!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissProgressDialog();
                        toastMessage(getApplicationContext(),"Error adding " + typeContribution + "!");
                    }
                });
    }

    private void updateDahira(String field, String value){

        showProgressDialog(this,"Mis a jour du dahira en cours ...");
        db.collection("dahiras").document(dahira.getDahiraID())
                .update(field, value)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dismissProgressDialog();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissProgressDialog();
                        toastMessage(getApplicationContext(),"Error updating dahira!");
                    }
                });

        startActivity(new Intent(UserInfoActivity.this, UserInfoActivity.class));
    }

    private boolean hasValidationErrors(EditText editTextValue, String value) {

        if (value.isEmpty() || !isDouble(value)) {
            editTextValue.setError("Valeur incorrect!");
            editTextValue.requestFocus();
            return true;
        }

        return false;
    }

    public boolean isDouble(String str){
        str = str.replace(",", ".");
        double value;
        try {
            value = Double.parseDouble(str);
            return true;
            // it means it is double
        } catch (Exception e1) {
            // this means it is not double
            e1.printStackTrace();
            return false;
        }
    }
}

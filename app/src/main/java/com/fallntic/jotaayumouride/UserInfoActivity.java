package com.fallntic.jotaayumouride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
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
import static com.fallntic.jotaayumouride.DataHolder.user;

public class UserInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewName;
    private TextView textViewAdress;
    private TextView textViewPhoneNumber;
    private TextView textViewEmail;
    private TextView textViewRole;
    private TextView textViewCommission;
    private ImageView imageViewProfile;

    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText editTextValue;

    boolean boolAdiya = false, boolSass = false, boolSocial = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Info du membre");
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        textViewName = (TextView) findViewById(R.id.textView_userName);
        textViewPhoneNumber = (TextView) findViewById(R.id.textView_phoneNumber);
        textViewAdress = (TextView) findViewById(R.id.textView_address);
        textViewEmail = (TextView) findViewById(R.id.textView_email);
        textViewCommission = (TextView) findViewById(R.id.textView_commission);
        textViewRole = (TextView) findViewById(R.id.textView_role);
        imageViewProfile = (ImageView) findViewById(R.id.imageView);

        int index = user.getListDahiraID().indexOf(dahira.getDahiraID());

        showImage();

        textViewName.setText(user.getUserName());
        textViewPhoneNumber.setText(user.getUserPhoneNumber());
        textViewAdress.setText(user.getAddress());
        textViewEmail.setText(user.getEmail());
        textViewCommission.setText(user.getListCommissions().get(index));
        textViewRole.setText(user.getListRoles().get(index));

        findViewById(R.id.button_cancel).setOnClickListener(this);
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
        inflater.inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.addContributions:
                chooseContribution();
                break;

            case R.id.setting:
                startActivity(new Intent(this, SettingProfileActivity.class));
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

    public void showImage(){
        // Reference to the image file in Cloud Storage
        StorageReference storageReference;
        storageReference = firebaseStorage.getReference();
        final StorageReference profileImageReference = storageReference.child("images").child(user.getUserID());

        showProgressDialog("Chargement de l'image ...");
        // Download directly from StorageReference using Glide
        GlideApp.with(UserInfoActivity.this)
                .load(profileImageReference)
                .placeholder(R.drawable.icon_camera)
                .into(imageViewProfile);

        dismissProgressDialog();
    }

    private void chooseContribution() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_member, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        final Button buttonAddAdiya = (Button) dialogView.findViewById(R.id.button_dialogAddAdiya);
        final Button buttonAddSass = (Button) dialogView.findViewById(R.id.button_dialogAddSass);
        final Button buttonAddSocial = (Button) dialogView.findViewById(R.id.button_dialogAddSocial);
        final Button buttonCancel = (Button) dialogView.findViewById(R.id.button_cancel);

        dialogBuilder.setTitle("Ajouter une contribution");
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonAddAdiya.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolAdiya = true;
                addContribution();
            }
        });

        buttonAddSass.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolSass = true;
                addContribution();
            }
        });

        buttonAddSocial.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolSocial = true;
                addContribution();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserInfoActivity.this, ListUserActivity.class));
            }
        });

        alertDialog.dismiss();
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
            dialogBuilder.setTitle("Ajouter adiya pour " + user.getUserName() + " membre du dahira " + dahira.getDahiraName());
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

                if(!hasValidationErrors(value)){

                    if (boolAdiya){
                        double totalAdiyaUser = Double.parseDouble(user.getListAdiya().get(index));
                        double totalAdiyaDahira = Double.parseDouble(dahira.getTotalAdiya());
                        totalAdiyaUser += Double.parseDouble(value);
                        totalAdiyaDahira += Double.parseDouble(value);
                        dahira.setTotalAdiya(Double.toString(totalAdiyaDahira));
                        user.getListAdiya().add(index, Double.toString(totalAdiyaUser));
                        updateContribution("listAdiya", user.getListAdiya(), "adiya");
                        updateDahira("totalAdiya", dahira.getTotalAdiya());
                    }

                    if (boolSass){
                        double totalSassUser = Double.parseDouble(user.getListSass().get(index));
                        double totalSassDahira = Double.parseDouble(dahira.getTotalSass());
                        totalSassUser += Double.parseDouble(value);
                        totalSassDahira += Double.parseDouble(value);
                        dahira.setTotalAdiya(Double.toString(totalSassDahira));
                        user.getListSass().add(index, Double.toString(totalSassUser));
                        updateContribution("listSass", user.getListSass(), "sass");
                        updateDahira("totalSass", dahira.getTotalSass());
                    }

                    if (boolSocial){
                        double totalSocialUser = Double.parseDouble(user.getListSocial().get(index));
                        double totalSocialDahira = Double.parseDouble(dahira.getTotalSocial());
                        totalSocialUser += Double.parseDouble(value);
                        totalSocialDahira += Double.parseDouble(value);
                        dahira.setTotalSocial(Double.toString(totalSocialDahira));
                        user.getListSocial().add(index, Double.toString(totalSocialUser));
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
                startActivity(new Intent(UserInfoActivity.this, ListUserActivity.class));
                alertDialog.dismiss();
            }
        });
    }

    private void updateContribution(String field, List<String> value, final String typeContribution){

        showProgressDialog("Enregistrement " + typeContribution + " en cours ...");
        db.collection("users").document(user.getUserID())
                .update(field, value)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dismissProgressDialog();
                        toastMessage(typeContribution + " ajoute avec succes!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissProgressDialog();
                        toastMessage("Error adding " + typeContribution + "!");
                    }
                });
    }

    private void updateDahira(String field, String value){

        showProgressDialog("Mis a jour du dahira en cours ...");
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
                        toastMessage("Error updating dahira!");
                    }
                });
    }

    private boolean hasValidationErrors(String value) {

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

    public void toastMessage(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void showProgressDialog(String str){
        progressDialog.setMessage(str);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        dismissProgressDialog();
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}

package com.fallntic.jotaayumouride;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.user;

public class UpdateAdminActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "CreateDahiraActivity";

    private FirebaseFirestore db;
    private ProgressDialog progressDialog;
    final Handler handler = new Handler();

    private EditText editTextUserName;
    private EditText editTextPhoneNumber;
    private EditText editTextAddress;
    private EditText editTextAdiya;
    private EditText editTextSass;
    private EditText editTextSocial;
    private TextView textViewDahiraName;
    private ImageView imageViewProfile;
    private Spinner spinnerCommission;
    private String commission;


    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_admin);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Mettre a jour votre profile");
        setSupportActionBar(toolbar);

        if (!DataHolder.isConnected(this)){
            toastMessage("Oops! Vous n'avez pas de connexion internet!");
            finish();
        }

        db = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(this);

        editTextUserName = findViewById(R.id.editText_userName);
        editTextPhoneNumber = findViewById(R.id.editText_userPhoneNumber);
        editTextAddress = findViewById(R.id.editText_address);
        editTextAdiya = findViewById(R.id.editText_adiyaVerse);
        editTextSass = findViewById(R.id.editText_sassVerse);
        editTextSocial = findViewById(R.id.editText_socialVerse);
        textViewDahiraName = findViewById(R.id.textView_dahiraName);
        imageViewProfile = findViewById(R.id.imageView);

        textViewDahiraName.setText("Dahira " + dahira.getDahiraName());
        editTextUserName.setText(user.getUserName());
        editTextPhoneNumber.setText(user.getUserPhoneNumber());
        editTextAddress.setText(user.getAddress());

        showImage();
        //getData();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setSpinner();
            }
        }, 5000);

        findViewById(R.id.button_save).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button_save:
                updateAdmin();
                break;
        }
    }

    @Override
    public void onBackPressed() {}

    public void toastMessage(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void updateAdmin(){

        String name = editTextUserName.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String adiya = editTextAdiya.getText().toString().trim();
        String sass = editTextSass.getText().toString().trim();
        String social = editTextSocial.getText().toString().trim();
        String role = "administrateur";

        adiya = adiya.replace(",", ".");
        sass = sass.replace(",", ".");
        social = social.replace(",", ".");

        if(!hasValidationErrors(name, phoneNumber, address, adiya, sass, social)){
            user.getListUpdatedDahiraID().add(dahira.getDahiraID());
            user.getListCommissions().add(commission);
            user.getListRoles().add(role);
            user.getListAdiya().add(adiya);
            user.getListSass().add(sass);
            user.getListSocial().add(social);

            dahira.setTotalAdiya(adiya);
            dahira.setTotalSass(sass);
            dahira.setTotalSocial(social);

            updateDahira();
            updateUser();

            startActivity(new Intent(UpdateAdminActivity.this, ListDahiraActivity.class));
        }
    }

    private void updateUser(){

        db.collection("users").document(user.getUserID())
                .update("listUpdatedDahiraID", user.getListUpdatedDahiraID(),
                        "userName", user.getUserName(),
                        "userPhoneNumber", user.getUserPhoneNumber(),
                        "address", user.getAddress(),
                        "listCommissions", user.getListCommissions(),
                        "listAdiya", user.getListAdiya(),
                        "listSass", user.getListSass(),
                        "listSocial", user.getListSocial(),
                        "listRoles", user.getListRoles())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        toastMessage("User updated.");
                    }
                });
    }

    private void updateDahira(){

        int totalMember = Integer.parseInt(dahira.getTotalMember());

       dahira.setTotalMember(Integer.toString(totalMember++));

        db.collection("dahiras").document(dahira.getDahiraID())
                .update("totalAdiya", dahira.getTotalAdiya(),
                        "totalSass", dahira.getTotalSass(),
                        "totalSocial", dahira.getTotalSocial(),
                        "totalMember", dahira.getTotalMember())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        toastMessage("Dahira updated.");
                    }
                });
    }

    public void setSpinner(){
        spinnerCommission = findViewById(R.id.spinner_commission);

        List<String> listCommissionDahira = dahira.getListCommissions();
        listCommissionDahira.add(0, "N/A");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, listCommissionDahira);
        spinnerCommission.setAdapter(adapter);

        spinnerCommission.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                commission = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView <?> parent) {
            }
        });
    }

    public void showImage(){
        // Reference to the image file in Cloud Storage
        FirebaseStorage firebaseStorage;
        firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference;
        storageReference = firebaseStorage.getReference();
        final StorageReference profileImageReference = storageReference.child("profile_image").child(user.getUserID());

        showProgressDialog("Chargement de l'image ...");
        // Download directly from StorageReference using Glide
        GlideApp.with(UpdateAdminActivity.this)
                .load(profileImageReference)
                .placeholder(R.drawable.profile_image)
                .into(imageViewProfile);

        dismissProgressDialog();
    }

    private boolean hasValidationErrors(String name, String phoneNumber, String address,
                                        String adiya, String sass, String social) {

        if (name.isEmpty()) {
            editTextUserName.setError("Ce champ est obligatoir!");
            editTextUserName.requestFocus();
            return true;
        }

        if (phoneNumber.isEmpty()) {
            editTextPhoneNumber.setError("Ce champ est obligatoir!");
            editTextPhoneNumber.requestFocus();
            return true;
        }

        if(!phoneNumber.matches("[0-9]+") || phoneNumber.length() != 9) {
            editTextPhoneNumber.setError("Numero de telephone incorrect");
            editTextPhoneNumber.requestFocus();
            return true;
        }

        String prefix = phoneNumber.substring(0,2);
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
        if(!validatePrefix) {
            editTextPhoneNumber.setError("Numero de telephone incorrect");
            editTextPhoneNumber.requestFocus();
            return true;
        }

        if (address.isEmpty()) {
            editTextAddress.setError("Ce champ est obligatoir!");
            editTextAddress.requestFocus();
            return true;
        }

        if (adiya.isEmpty() || !isDouble(adiya)) {
            editTextAdiya.setError("Valeur incorrecte!");
            editTextAdiya.requestFocus();
            return true;
        }

        if (sass.isEmpty() || !isDouble(sass)) {
            editTextSass.setError("Valeur incorrecte!");
            editTextSass.requestFocus();
            return true;
        }

        if (social.isEmpty() || !isDouble(social)) {
            editTextSocial.setError("Valeur incorrecte!");
            editTextSocial.requestFocus();
            return true;
        }

        return false;
    }

    public boolean isDouble(String str){
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

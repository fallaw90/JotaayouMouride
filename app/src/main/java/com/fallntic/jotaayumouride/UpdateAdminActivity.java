package com.fallntic.jotaayumouride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.hasValidationErrors;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.logout;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.saveContribution;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.showProfileImage;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;

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

        if (!isConnected(this)){
            Intent intent = new Intent(this, LoginActivity.class);
            logout();
            showAlertDialog(this,"Oops! Pas de connexion, verifier votre connexion internet puis reesayez SVP", intent);
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
        editTextUserName.setText(onlineUser.getUserName());
        editTextPhoneNumber.setText(onlineUser.getUserPhoneNumber());
        editTextAddress.setText(onlineUser.getAddress());

        showProfileImage(this, onlineUser.getUserID(), imageViewProfile);
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

    private void updateAdmin(){

        String name = editTextUserName.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String adiya = editTextAdiya.getText().toString().trim();
        String sass = editTextSass.getText().toString().trim();
        String social = editTextSocial.getText().toString().trim();
        String role = "Administrateur";

        adiya = adiya.replace(",", ".");
        sass = sass.replace(",", ".");
        social = social.replace(",", ".");

        if(!hasValidationErrors(name, editTextUserName, phoneNumber, editTextPhoneNumber,
                address, editTextAddress, adiya, editTextAdiya, sass, editTextSass, social, editTextSocial)){

            onlineUser.getListUpdatedDahiraID().add(dahira.getDahiraID());
            onlineUser.getListCommissions().add(commission);
            onlineUser.getListRoles().add(role);
            onlineUser.getListAdiya().add(adiya);
            onlineUser.getListSass().add(sass);
            onlineUser.getListSocial().add(social);

            dahira.setTotalAdiya(adiya);
            dahira.setTotalSass(sass);
            dahira.setTotalSocial(social);

            updateDahira();
            updateUserCollection();

            Intent intent = new Intent(this, ProfileActivity.class);
            double value;

            adiya = adiya.replace(",", ".");
            value = Double.parseDouble(adiya);
            if (value != 0){
                saveContribution(this, "listAdiya", onlineUser.getUserID(), adiya);
            }

            sass = sass.replace(",", ".");
            value = Double.parseDouble(sass);
            if (value != 0){
                saveContribution(this, "sass", onlineUser.getUserID(), sass);
            }

            social = social.replace(",", ".");
            value = Double.parseDouble(social);
            if (value != 0){
                saveContribution(this, "social", onlineUser.getUserID(), social);
            }

            startActivity(new Intent(UpdateAdminActivity.this, ProfileActivity.class));
        }
    }

    private void updateUserCollection(){

        db.collection("users").document(onlineUser.getUserID())
                .update("listUpdatedDahiraID", onlineUser.getListUpdatedDahiraID(),
                        "userName", onlineUser.getUserName(),
                        "userPhoneNumber", onlineUser.getUserPhoneNumber(),
                        "address", onlineUser.getAddress(),
                        "listCommissions", onlineUser.getListCommissions(),
                        "listAdiya", onlineUser.getListAdiya(),
                        "listSass", onlineUser.getListSass(),
                        "listSocial", onlineUser.getListSocial(),
                        "listRoles", onlineUser.getListRoles())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        toastMessage(getApplicationContext(), "User updated.");
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
                        toastMessage(getApplicationContext(),"Dahira updated.");
                    }
                });
    }
}

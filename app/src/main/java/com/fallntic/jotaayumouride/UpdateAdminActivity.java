package com.fallntic.jotaayumouride;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
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

import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.checkPrefix;
import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.hasValidationErrors;
import static com.fallntic.jotaayumouride.DataHolder.isDouble;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.showProfileImage;
import static com.fallntic.jotaayumouride.DataHolder.showProgressDialog;

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
            showAlertDialog(this, "Oops! Vous n'avez pas de connexion internet!");
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
            updateUser();

            startActivity(new Intent(UpdateAdminActivity.this, ListDahiraActivity.class));
        }
    }

    private void updateUser(){

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

}

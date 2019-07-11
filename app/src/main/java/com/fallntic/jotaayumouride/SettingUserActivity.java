package com.fallntic.jotaayumouride;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.showProfileImage;
import static com.fallntic.jotaayumouride.DataHolder.user;

public class SettingUserActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "CreateDahiraActivity";

    private RadioGroup radioRoleGroup;
    private RadioButton radioRoleButton;
    private boolean commissionsSaved = true;

    private FirebaseFirestore db;

    private EditText editTextUserName;
    private EditText editTextPhoneNumber;
    private EditText editTextAddress;
    private EditText editTextAdiya;
    private EditText editTextSass;
    private EditText editTextSocial;
    private ImageView imageView;
    private Spinner spinnerCommission;
    private String commission;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_user);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!DataHolder.isConnected(this)){
            toastMessage("Oops! Vous n'avez pas de connexion internet!");
            finish();
        }

        db = FirebaseFirestore.getInstance();

        editTextUserName = findViewById(R.id.editText_userName);
        editTextPhoneNumber = findViewById(R.id.editText_userPhoneNumber);
        editTextAddress = findViewById(R.id.editText_address);
        editTextAdiya = findViewById(R.id.editText_adiyaVerse);
        editTextSass = findViewById(R.id.editText_sassVerse);
        editTextSocial = findViewById(R.id.editText_socialVerse);
        imageView = (ImageView) findViewById(R.id.imageView);

        editTextUserName.setText(user.getUserName());
        editTextUserName.setEnabled(false);
        editTextPhoneNumber.setText(user.getUserPhoneNumber());
        editTextPhoneNumber.setEnabled(false);
        editTextAddress.setText(user.getAddress());
        editTextAddress.setEnabled(false);

        index = user.getListDahiraID().indexOf(dahira.getDahiraID());

        editTextAdiya.setText(user.getListAdiya().get(index));
        editTextSass.setText(user.getListSass().get(index));
        editTextSocial.setText(user.getListSocial().get(index));

        radioRoleGroup = (RadioGroup) findViewById(R.id.radioGroup);
        // get selected radio button from radioGroup
        int selectedId = radioRoleGroup.getCheckedRadioButtonId();
        // find the radiobutton by returned id
        radioRoleButton = (RadioButton) findViewById(selectedId);

        showProfileImage(this, imageView);
        //Select a commission
        setSpinner();

        findViewById(R.id.button_update).setOnClickListener(this);
        findViewById(R.id.button_cancel).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button_update:
                updateData();
                break;

            case R.id.button_cancel:
                startActivity(new Intent(SettingUserActivity.this, UserInfoActivity.class));
                break;
        }
    }

    public void updateData(){

        String name = editTextUserName.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String adiya = editTextAdiya.getText().toString().trim();
        String sass = editTextSass.getText().toString().trim();
        String social = editTextSocial.getText().toString().trim();
        String role = (String) radioRoleButton.getText();

        adiya = adiya.replace(",", ".");
        sass = sass.replace(",", ".");
        social = social.replace(",", ".");

        if(!hasValidationErrors(name, phoneNumber, address, adiya, sass, social)){
            user.setUserName(name);
            user.setUserPhoneNumber(phoneNumber);
            user.setAddress(address);
            user.getListAdiya().set(index, adiya);
            user.getListSass().set(index, sass);
            user.getListSocial().set(index, social);
            user.getListCommissions().set(index, commission);
            user.getListRoles().set(index, role);

            db.collection("users").document(user.getUserID())
                    .update("userName", user.getUserName(),
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

            startActivity(new Intent(SettingUserActivity.this, UserInfoActivity.class));
        }
    }

    private boolean hasValidationErrors(String name, String phoneNumber, String address, String adiya, String sass, String social) {

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


    public void setSpinner(){
        spinnerCommission = findViewById(R.id.spinner_commission);

        List<String> listCommissionDahira = dahira.getListCommissions();
        listCommissionDahira.add(0, "N/A");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listCommissionDahira);
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

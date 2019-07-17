package com.fallntic.jotaayumouride;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.hasValidationErrors;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.logout;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.selectedUser;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.showProfileImage;

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
    private Button buttonDelete;
    private String commission;
    private int indexSelectedUser = selectedUser.getListDahiraID().indexOf(dahira.getDahiraID());
    private int indexOnlineUser = onlineUser.getListDahiraID().indexOf(dahira.getDahiraID());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_user);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!isConnected(this)){
            Intent intent = new Intent(this, LoginActivity.class);
            logout();
            showAlertDialog(this,"Oops! Pas de connexion, verifier votre connexion internet puis reesayez SVP", intent);
        }

        db = FirebaseFirestore.getInstance();

        editTextUserName = findViewById(R.id.editText_userName);
        editTextPhoneNumber = findViewById(R.id.editText_userPhoneNumber);
        editTextAddress = findViewById(R.id.editText_address);
        editTextAdiya = findViewById(R.id.editText_adiyaVerse);
        editTextSass = findViewById(R.id.editText_sassVerse);
        editTextSocial = findViewById(R.id.editText_socialVerse);
        imageView = (ImageView) findViewById(R.id.imageView);
        buttonDelete = findViewById(R.id.button_delete);

        editTextUserName.setText(selectedUser.getUserName());
        editTextPhoneNumber.setText(selectedUser.getUserPhoneNumber());
        editTextAddress.setText(selectedUser.getAddress());

        editTextAdiya.setText(selectedUser.getListAdiya().get(indexSelectedUser));
        editTextSass.setText(selectedUser.getListSass().get(indexSelectedUser));
        editTextSocial.setText(selectedUser.getListSocial().get(indexSelectedUser));

        radioRoleGroup = (RadioGroup) findViewById(R.id.radioGroup);
        // get selected radio button from radioGroup
        int selectedId = radioRoleGroup.getCheckedRadioButtonId();
        // find the radiobutton by returned id
        radioRoleButton = (RadioButton) findViewById(selectedId);

        showProfileImage(this, selectedUser.getUserID(), imageView);
        //Select a commission
        setSpinner();

        if (selectedUser.getUserID().equals(onlineUser.getUserID())){
            radioRoleGroup.setVisibility(View.GONE);
            buttonDelete.setVisibility(View.GONE);
        }
        else if (onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur")){
            radioRoleGroup.setVisibility(View.GONE);
            editTextUserName.setEnabled(false);
            editTextPhoneNumber.setEnabled(false);
            editTextAddress.setEnabled(false);
        }

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

        if(!hasValidationErrors(name, editTextUserName, phoneNumber, editTextPhoneNumber, address,
                editTextAddress, adiya, editTextAdiya, sass, editTextSass, social, editTextSocial)){
            selectedUser.setUserName(name);
            selectedUser.setUserPhoneNumber(phoneNumber);
            selectedUser.setAddress(address);
            selectedUser.getListAdiya().set(indexSelectedUser, adiya);
            selectedUser.getListSass().set(indexSelectedUser, sass);
            selectedUser.getListSocial().set(indexSelectedUser, social);
            selectedUser.getListCommissions().set(indexSelectedUser, commission);
            selectedUser.getListRoles().set(indexSelectedUser, role);

            db.collection("users").document(selectedUser.getUserID())
                    .update("userName", selectedUser.getUserName(),
                            "userPhoneNumber", selectedUser.getUserPhoneNumber(),
                            "address", selectedUser.getAddress(),
                            "listCommissions", selectedUser.getListCommissions(),
                            "listAdiya", selectedUser.getListAdiya(),
                            "listSass", selectedUser.getListSass(),
                            "listSocial", selectedUser.getListSocial(),
                            "listRoles", selectedUser.getListRoles())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            showAlertDialog(SettingUserActivity.this, "Enregistrement reussi.");
                        }
                    });

            startActivity(new Intent(SettingUserActivity.this, UserInfoActivity.class));
        }
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

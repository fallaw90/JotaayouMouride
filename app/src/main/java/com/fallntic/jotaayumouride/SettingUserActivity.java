package com.fallntic.jotaayumouride;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.selectedUser;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.showImage;

public class SettingUserActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "CreateDahiraActivity";

    private RadioGroup radioRoleGroup;
    private RadioButton radioRoleButton;
    private boolean commissionsSaved = true;

    private FirebaseFirestore db;

    private EditText editTextUserName;
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

    public static boolean hasValidationErrors(String name, EditText editTextUserName, String address,
                                              EditText editTextAddress, String adiya, EditText editTextAdiya,
                                              String sass, EditText editTextSass, String social, EditText editTextSocial) {

        if (name.isEmpty()) {
            editTextUserName.setError("Champ obligatoir!");
            editTextUserName.requestFocus();
            return true;
        }

        if (address.isEmpty()) {
            editTextAddress.setError("Champ obligatoir!");
            editTextAddress.requestFocus();
            return true;
        }

        if (adiya.isEmpty()) {
            editTextAdiya.setError("Champ obligatoir! Entrez 0 si non adiya");
            editTextAdiya.requestFocus();
            return true;
        }

        if (sass.isEmpty()) {
            editTextSass.setError("Champ obligatoir! Entrez 0 si non sass");
            editTextSass.requestFocus();
            return true;
        }

        if (social.isEmpty()) {
            editTextSocial.setError("Champ obligatoir! Entrez 0 si non social");
            editTextSocial.requestFocus();
            return true;
        }

        return false;
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_user);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (!isConnected(this)){
            finish();
            Intent intent = new Intent(this, LoginActivity.class);
            showAlertDialog(this,"Oops! Pas de connexion, " +
                    "verifier votre connexion internet puis reesayez SVP", intent);
        }

        db = FirebaseFirestore.getInstance();

        editTextUserName = findViewById(R.id.editText_userName);
        editTextAddress = findViewById(R.id.editText_address);
        editTextAdiya = findViewById(R.id.editText_adiyaVerse);
        editTextSass = findViewById(R.id.editText_sassVerse);
        editTextSocial = findViewById(R.id.editText_socialVerse);
        imageView = findViewById(R.id.imageView);
        buttonDelete = findViewById(R.id.button_delete);

        editTextUserName.setText(selectedUser.getUserName());
        editTextAddress.setText(selectedUser.getAddress());

        editTextAdiya.setText(selectedUser.getListAdiya().get(indexSelectedUser));
        editTextSass.setText(selectedUser.getListSass().get(indexSelectedUser));
        editTextSocial.setText(selectedUser.getListSocial().get(indexSelectedUser));

        radioRoleGroup = findViewById(R.id.radioGroup);
        // get selected radio button from radioGroup
        int selectedId = radioRoleGroup.getCheckedRadioButtonId();
        // find the radiobutton by returned id
        radioRoleButton = findViewById(selectedId);



        showImage(this, "profileImage", selectedUser.getUserID(), imageView);
        //Select a commission
        setSpinner();

        if (selectedUser.getUserID().equals(onlineUser.getUserID())){
            radioRoleGroup.setVisibility(View.GONE);
            buttonDelete.setVisibility(View.GONE);
        }
        else if (onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur")){
            radioRoleGroup.setVisibility(View.GONE);
            editTextUserName.setEnabled(false);
            editTextAddress.setEnabled(false);
        }

        hideSoftKeyboard();

        findViewById(R.id.button_update).setOnClickListener(this);
        findViewById(R.id.button_cancel).setOnClickListener(this);
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

    public void updateData(){

        String name = editTextUserName.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String adiya = editTextAdiya.getText().toString().trim();
        String sass = editTextSass.getText().toString().trim();
        String social = editTextSocial.getText().toString().trim();
        String role = (String) radioRoleButton.getText();

        adiya = adiya.replace(",", ".");
        sass = sass.replace(",", ".");
        social = social.replace(",", ".");

        if(!hasValidationErrors(name, editTextUserName, address, editTextAddress,
                adiya, editTextAdiya, sass, editTextSass, social, editTextSocial)){
            selectedUser.setUserName(name);
            selectedUser.setAddress(address);
            selectedUser.getListAdiya().set(indexSelectedUser, adiya);
            selectedUser.getListSass().set(indexSelectedUser, sass);
            selectedUser.getListSocial().set(indexSelectedUser, social);
            selectedUser.getListCommissions().set(indexSelectedUser, commission);
            selectedUser.getListRoles().set(indexSelectedUser, role);

            db.collection("users").document(selectedUser.getUserID())
                    .update("userName", selectedUser.getUserName(),
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

    public void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}

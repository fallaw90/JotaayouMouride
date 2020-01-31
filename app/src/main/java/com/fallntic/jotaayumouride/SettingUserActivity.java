package com.fallntic.jotaayumouride;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fallntic.jotaayumouride.utility.MyStaticFunctions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Objects;

import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.dismissProgressDialog;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showAlertDialog;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.dahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.selectedUser;

@SuppressWarnings("unused")
public class SettingUserActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "CreateDahiraActivity";

    private RadioGroup radioRoleGroup;

    private EditText editTextUserName;
    private EditText editTextAddress;
    private EditText editTextAdiya;
    private EditText editTextSass;
    private EditText editTextSocial;
    private ImageView imageView;
    private Button buttonDelete;
    private String commission;
    private final int indexSelectedUser = selectedUser.getListDahiraID().indexOf(dahira.getDahiraID());
    private final int indexOnlineUser = onlineUser.getListDahiraID().indexOf(dahira.getDahiraID());

    private FirebaseFirestore firestore;

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
        switch (v.getId()) {
            case R.id.button_update:
                updateData();
                break;

            case R.id.button_cancel:
                startActivity(new Intent(SettingUserActivity.this, UserInfoActivity.class));
                break;

            case R.id.button_delete:

                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.alertDialog);
                builder.setTitle("Supprimer ce membre!");
                builder.setMessage("Etes vous sure de vouloir supprimer " + selectedUser.getUserName() + " dans le dahira " + dahira.getDahiraName() + "?");
                builder.setCancelable(false);
                builder.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedUser.getListDahiraID().remove(indexSelectedUser);
                        selectedUser.getListUpdatedDahiraID().remove(indexSelectedUser);
                        selectedUser.getListRoles().remove(indexSelectedUser);
                        selectedUser.getListCommissions().remove(indexSelectedUser);
                        selectedUser.getListAdiya().remove(indexSelectedUser);
                        selectedUser.getListSass().remove(indexSelectedUser);
                        selectedUser.getListSocial().remove(indexSelectedUser);
                        deleteUser();
                    }
                });

                builder.setNegativeButton("NON", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.show();

                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_user);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        //toolbar.setLogo(R.mipmap.logo);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.logo);

        checkInternetConnection(this);
        initViews();

        firestore = FirebaseFirestore.getInstance();

        displayViews();

        hideSoftKeyboard();

        if (onlineUser.getUserID().equals(selectedUser.getUserID()) || selectedUser.getListRoles().get(indexSelectedUser).equals("Administrateur"))
            findViewById(R.id.button_delete).setVisibility(View.GONE);
    }

    private void initViewsProgressBar() {
        relativeLayoutData = findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initViews() {

        editTextUserName = findViewById(R.id.editText_userName);
        editTextAddress = findViewById(R.id.editText_address);
        editTextAdiya = findViewById(R.id.editText_adiyaVerse);
        editTextSass = findViewById(R.id.editText_sassVerse);
        editTextSocial = findViewById(R.id.editText_socialVerse);
        imageView = findViewById(R.id.imageView);
        buttonDelete = findViewById(R.id.button_delete);

        initViewsProgressBar();

        radioRoleGroup = findViewById(R.id.radioGroup);

        findViewById(R.id.button_update).setOnClickListener(this);
        findViewById(R.id.button_cancel).setOnClickListener(this);
        findViewById(R.id.button_delete).setOnClickListener(this);
    }

    private void displayViews() {

        editTextUserName.setText(selectedUser.getUserName());
        editTextAddress.setText(selectedUser.getAddress());

        editTextAdiya.setText(selectedUser.getListAdiya().get(indexSelectedUser));
        editTextSass.setText(selectedUser.getListSass().get(indexSelectedUser));
        editTextSocial.setText(selectedUser.getListSocial().get(indexSelectedUser));

        editTextAdiya.setEnabled(false);
        editTextSass.setEnabled(false);
        editTextSocial.setEnabled(false);

        //Select a commission
        setSpinner();

        if (!selectedUser.getUserID().equals(onlineUser.getUserID())) {
            editTextUserName.setEnabled(false);
            editTextAddress.setEnabled(false);
        }
        if (!onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur")) {
            radioRoleGroup.setVisibility(View.GONE);
            buttonDelete.setVisibility(View.GONE);
        }

        MyStaticFunctions.showImage(this, selectedUser.getImageUri(), imageView);
    }

    private void setSpinner() {
        Spinner spinnerCommission = findViewById(R.id.spinner_commission);

        List<String> listCommissionDahira = dahira.getListCommissions();
        listCommissionDahira.add(0, "N/A");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listCommissionDahira);
        spinnerCommission.setAdapter(adapter);

        spinnerCommission.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                commission = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updateData() {
        // get selected radio button from radioGroup
        int selectedId = radioRoleGroup.getCheckedRadioButtonId();
        // find the radiobutton by returned id
        RadioButton radioRoleButton = findViewById(selectedId);

        String name = editTextUserName.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String role = (String) radioRoleButton.getText();

        if (!hasValidationErrors(name, address)) {
            selectedUser.setUserName(name);
            selectedUser.setAddress(address);
            selectedUser.getListCommissions().set(indexSelectedUser, commission);
            selectedUser.getListRoles().set(indexSelectedUser, role);

            showProgressBar();
            firestore.collection("users").document(selectedUser.getUserID())
                    .update("userName", selectedUser.getUserName(),
                            "address", selectedUser.getAddress(),
                            "listCommissions", selectedUser.getListCommissions(),
                            "listRoles", selectedUser.getListRoles())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            hideProgressBar();
                            toastMessage(SettingUserActivity.this, "Enregistrement reussi");
                            finish();
                            /*
                            Intent intent = new Intent(SettingUserActivity.this, UserInfoActivity.class);
                            showAlertDialog(SettingUserActivity.this, "Enregistrement reussi.", intent);
                            */
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    hideProgressBar();
                    startActivity(new Intent(SettingUserActivity.this, UserInfoActivity.class));
                }
            });
        }
    }

    private void deleteUser() {
        showProgressBar();
        firestore.collection("users").document(selectedUser.getUserID())
                .set(selectedUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideProgressBar();
                        updateDahira();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressBar();
                startActivity(new Intent(SettingUserActivity.this, UserInfoActivity.class));
            }
        });
    }

    private void updateDahira() {
        int totalMember = Integer.parseInt(dahira.getTotalMember());
        totalMember--;
        dahira.setTotalMember(Integer.toString(totalMember));

        //showProgressBar();
        firestore.collection("dahiras").document(dahira.getDahiraID())
                .update("totalMember", dahira.getTotalMember())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //hideProgressBar();
                        Intent intent = new Intent(SettingUserActivity.this, ShowUserActivity.class);
                        showAlertDialog(SettingUserActivity.this, "Membre supprime avec succes.", intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // hideProgressBar();
            }
        });
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem iconBack;
        iconBack = menu.findItem(R.id.icon_back);
        iconBack.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                break;

            case R.id.icon_back:
                finish();
                break;

            case R.id.instructions:
                startActivity(new Intent(this, InstructionsActivity.class));
                break;
        }
        return true;
    }

    private boolean hasValidationErrors(String name, String address) {

        if (name.isEmpty()) {
            editTextUserName.setError("Ce champ est obligatoir!");
            editTextUserName.requestFocus();
            return true;
        }

        if (address.isEmpty()) {
            editTextAddress.setError("Champs obligatoir!");
            editTextAddress.requestFocus();
            return true;
        }

        return false;
    }
}
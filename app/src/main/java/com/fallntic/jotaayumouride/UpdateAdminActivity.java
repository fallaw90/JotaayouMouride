package com.fallntic.jotaayumouride;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fallntic.jotaayumouride.Utility.MyStaticFunctions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.Utility.DataHolder.dahira;
import static com.fallntic.jotaayumouride.Utility.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.Utility.DataHolder.indexOnlineUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.isDouble;
import static com.fallntic.jotaayumouride.Utility.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.progressDialog;
import static com.fallntic.jotaayumouride.Utility.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.firestore;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.myListDahira;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutProgressBar;

public class UpdateAdminActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "CreateDahiraActivity";

    final Handler handler = new Handler();

    private EditText editTextUserName;
    private EditText editTextAdiya;
    private EditText editTextSass;
    private EditText editTextSocial;
    private TextView textViewDahiraName;
    private ImageView imageViewProfile;
    private Spinner spinnerCommission;
    private String name, amountAdiya, amountSass, amountSocial, role, commission;
    private String totalAdiya, totalSass, totalSocial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_admin);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Mettre a jour votre profile");
        setSupportActionBar(toolbar);
        //***************** Set logo **********************
        getSupportActionBar().setLogo(R.mipmap.logo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        checkInternetConnection(this);

        initViews();
        displayViews();

        MyStaticFunctions.showImage(this, onlineUser.getImageUri(), imageViewProfile);
        //getData();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setSpinner();
            }
        }, 5000);


        hideSoftKeyboard();
    }

    private void displayViews() {
        textViewDahiraName.setText("Enregistrez vous en tant que membre du dahira " + dahira.getDahiraName() + " que vous venez de creer pour terminer la creation de votre dahira.");
        editTextUserName.setText(onlineUser.getUserName());
    }

    private void initViews() {
        editTextUserName = findViewById(R.id.editText_userName);
        editTextUserName.setEnabled(false);
        editTextAdiya = findViewById(R.id.editText_adiyaVerse);
        editTextSass = findViewById(R.id.editText_sassVerse);
        editTextSocial = findViewById(R.id.editText_socialVerse);
        textViewDahiraName = findViewById(R.id.textView_dahiraName);
        imageViewProfile = findViewById(R.id.imageView);
        ShowUserActivity.scrollView = findViewById(R.id.scrollView);

        initViewsProgressBar();

        findViewById(R.id.button_save).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_save:
                updateAdmin();
                break;
        }
    }

    @Override
    public void onBackPressed() {
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
            case R.id.icon_back:
                finish();
                break;
        }
        return true;
    }

    public void setSpinner() {
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
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updateAdmin() {

        name = editTextUserName.getText().toString().trim();
        amountAdiya = editTextAdiya.getText().toString().trim();
        amountSass = editTextSass.getText().toString().trim();
        amountSocial = editTextSocial.getText().toString().trim();
        role = "Administrateur";

        if (amountAdiya.contains(","))
            amountAdiya = amountAdiya.replace(",", ".");
        if (amountSass.contains(","))
            amountSass = amountSass.replace(",", ".");
        if (amountSocial.contains(","))
            amountSocial = amountSocial.replace(",", ".");

        if (!hasValidationErrors()) {

            onlineUser.getListUpdatedDahiraID().add(dahira.getDahiraID());
            onlineUser.getListCommissions().add(commission);
            onlineUser.getListRoles().add(role);
            onlineUser.getListAdiya().add(amountAdiya);
            onlineUser.getListSass().add(amountSass);
            onlineUser.getListSocial().add(amountSocial);

            double valueAdiya, valueSass, valueSocial;

            valueAdiya = Double.parseDouble(amountAdiya);
            totalAdiya = String.valueOf(valueAdiya + Double.parseDouble(dahira.getTotalAdiya()));

            valueSass = Double.parseDouble(amountSass);
            totalSass = String.valueOf(valueSass + Double.parseDouble(dahira.getTotalSass()));

            valueSocial = Double.parseDouble(amountSocial);
            totalSocial = String.valueOf(valueSocial + Double.parseDouble(dahira.getTotalSocial()));

            updateUserCollection();
        }
    }

    private void updateUserCollection() {

        showProgressBar();
        firestore.collection("users").document(onlineUser.getUserID())
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
                        hideProgressBar();
                        updateDahira();
                        toastMessage(getApplicationContext(), "Enregistrement reussi.");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressBar();
            }
        });
    }

    private void updateDahira() {

        int totalMember = Integer.parseInt(dahira.getTotalMember());

        dahira.setTotalMember(Integer.toString(totalMember++));
        dahira.setTotalAdiya(totalAdiya);
        dahira.setTotalSass(totalSass);
        dahira.setTotalSocial(totalSocial);

        if (myListDahira == null) {
            myListDahira = new ArrayList<>();
            myListDahira.add(dahira);
        } else {
            myListDahira.add(dahira);
        }

        showProgressBar();
        firestore.collection("dahiras").document(dahira.getDahiraID())
                .update("totalAdiya", totalAdiya,
                        "totalSass", totalSass,
                        "totalSocial", totalSocial,
                        "totalMember", dahira.getTotalMember())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideProgressBar();
                        indexOnlineUser = onlineUser.getListDahiraID().indexOf(dahira.getDahiraID());
                        startActivity(new Intent(UpdateAdminActivity.this, HomeActivity.class));
                        toastMessage(getApplicationContext(), "Enregistrement reussi.");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressBar();
            }
        });
    }

    public void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void initViewsProgressBar() {
        progressDialog = new ProgressDialog(this);
        relativeLayoutData = findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        progressBar = findViewById(R.id.progressBar);
    }

    public boolean hasValidationErrors() {
        if (amountAdiya.isEmpty() || !isDouble(amountAdiya)) {
            editTextAdiya.setError("Valeur incorrecte!");
            editTextAdiya.requestFocus();
            return true;
        }

        if (amountSass.isEmpty() || !isDouble(amountSass)) {
            editTextSass.setError("Valeur incorrecte!");
            editTextSass.requestFocus();
            return true;
        }

        if (amountSocial.isEmpty() || !isDouble(amountSocial)) {
            editTextSocial.setError("Valeur incorrecte!");
            editTextSocial.requestFocus();
            return true;
        }

        return false;
    }
}

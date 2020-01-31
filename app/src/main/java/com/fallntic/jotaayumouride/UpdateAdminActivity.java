package com.fallntic.jotaayumouride;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

import com.fallntic.jotaayumouride.model.Dahira;
import com.fallntic.jotaayumouride.utility.MyStaticFunctions;
import com.fallntic.jotaayumouride.utility.MyStaticVariables;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.dismissProgressDialog;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.isDouble;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.dahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.indexOnlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.myListDahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.progressDialog;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutProgressBar;

@SuppressWarnings("unused")
public class UpdateAdminActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "CreateDahiraActivity";

    private final Handler handler = new Handler();

    private EditText editTextUserName;
    private EditText editTextAdiya;
    private EditText editTextSass;
    private EditText editTextSocial;
    private TextView textViewDahiraName;
    private ImageView imageViewProfile;
    private String amountAdiya;
    private String amountSass;
    private String amountSocial;
    private String commission;
    private String totalAdiya, totalSass, totalSocial;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_admin);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Mettre a jour votre profile");
        setSupportActionBar(toolbar);
        //***************** Set logo **********************
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.logo);

        checkInternetConnection(this);

        initViews();
        displayViews();

        firestore = FirebaseFirestore.getInstance();

        new MyTask().execute();

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

    @SuppressLint("SetTextI18n")
    private void displayViews() {
        textViewDahiraName.setText("Completez votre inscription du dahira " + dahira.getDahiraName() + " pour terminer.");
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
        if (v.getId() == R.id.button_save) {
            updateAdmin();
        }
    }

    @Override
    public void onBackPressed() {
    }

    private void setSpinner() {
        Spinner spinnerCommission = findViewById(R.id.spinner_commission);

        List<String> listCommissionDahira = new ArrayList<>();
        if (dahira != null && dahira.getListCommissions().size() > 0)
            listCommissionDahira = dahira.getListCommissions();

        listCommissionDahira.add(0, "N/A");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
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

        amountAdiya = editTextAdiya.getText().toString().trim();
        amountSass = editTextSass.getText().toString().trim();
        amountSocial = editTextSocial.getText().toString().trim();
        String role = "Administrateur";

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
        if (firestore != null) {
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
    }

    private void updateDahira() {

        int totalMember = Integer.parseInt(dahira.getTotalMember());

        dahira.setTotalMember(Integer.toString(totalMember));
        dahira.setTotalAdiya(totalAdiya);
        dahira.setTotalSass(totalSass);
        dahira.setTotalSocial(totalSocial);

        if (myListDahira != null)
            myListDahira.add(dahira);

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

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void initViewsProgressBar() {
        progressDialog = new ProgressDialog(this);
        relativeLayoutData = findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        progressBar = findViewById(R.id.progressBar);
    }

    private boolean hasValidationErrors() {
        if (amountAdiya.isEmpty() || isDouble(amountAdiya)) {
            editTextAdiya.setError("Valeur incorrecte!");
            editTextAdiya.requestFocus();
            return true;
        }

        if (amountSass.isEmpty() || isDouble(amountSass)) {
            editTextSass.setError("Valeur incorrecte!");
            editTextSass.requestFocus();
            return true;
        }

        if (amountSocial.isEmpty() || isDouble(amountSocial)) {
            editTextSocial.setError("Valeur incorrecte!");
            editTextSocial.requestFocus();
            return true;
        }

        return false;
    }

    public void getMyDahira() {
        if (MyStaticVariables.myListDahira == null || myListDahira.isEmpty()) {
            myListDahira = new ArrayList<>();
            firestore.collection("dahiras").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot documentSnapshot : list) {
                                    Dahira dahira = documentSnapshot.toObject(Dahira.class);
                                    if (dahira != null && onlineUser.getListDahiraID().contains(dahira.getDahiraID())) {
                                        myListDahira.add(dahira);
                                    }
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });
        }
    }

    @SuppressLint("StaticFieldLeak")
    class MyTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getMyDahira();
            return null;
        }
    }
}

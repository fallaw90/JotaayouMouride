package com.fallntic.jotaayumouride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.logout;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.showProfileImage;
import static com.fallntic.jotaayumouride.DataHolder.showProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.DataHolder.userID;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ProfileActivity";
    private static final int CHOOSE_IMAGE = 101;

    private TextView textViewName;
    private TextView textViewAdress;
    private TextView textViewPhoneNumber;
    private TextView textViewEmail;
    private ImageView imageViewProfile;
    private LinearLayout linearLayoutVerificationNeeded;
    private LinearLayout linearLayoutVerified;
    private SwipeRefreshLayout swipeLayout;

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String dahiraToUpdate;

    public static boolean boolMyDahiras;
    public static boolean boolAllDahiras;

    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Mon profil");
        setSupportActionBar(toolbar);

        //Retrieve userID and picture
        mAuth = FirebaseAuth.getInstance();

        String email = mAuth.getCurrentUser().getEmail();
        if (!isConnected(this)){
            Intent intent = new Intent(this, LoginActivity.class);
            showAlertDialog(this,"Oops! Pas de connexion, verifier votre connexion internet puis reesayez SVP", intent);
        }

        userID = mAuth.getCurrentUser().getUid();

        firebaseUser = mAuth.getCurrentUser();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        textViewAdress = (TextView) findViewById(R.id.textView_userAddress);
        textViewEmail = (TextView) findViewById(R.id.textView_email);
        textViewName = (TextView) findViewById(R.id.textView_userName);
        textViewPhoneNumber = (TextView) findViewById(R.id.textView_userPhoneNumber);
        imageViewProfile = (ImageView) findViewById(R.id.imageView);
        linearLayoutVerificationNeeded = (LinearLayout) findViewById(R.id.linearLayout_verificationNeeded);
        linearLayoutVerified = (LinearLayout) findViewById(R.id.linearLayout_verified);

        loadUserInformation();

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                startActivity(new Intent(ProfileActivity.this, ProfileActivity.class));
                swipeLayout.setRefreshing(false);
            }
        });

        boolMyDahiras = false;
        boolAllDahiras = false;

        findViewById(R.id.button_verifyEmail).setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button_verifyEmail:
                firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        logout();
                        finish();
                        startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                        toastMessage(getApplicationContext(),"Verification Email envoyee");
                    }
                });
                break;
        }
    }

    private void loadUserInformation() {
        showProfileImage(this, userID, imageViewProfile);
        if (firebaseUser != null && firebaseUser.isEmailVerified()) {
            //Get the current user info
            getUser();
            linearLayoutVerificationNeeded.setVisibility(View.GONE);
        }
        else{
            linearLayoutVerified.setVisibility(View.GONE);
            showAlertDialog(ProfileActivity.this, "Inscription reussi! Merci de verifier votre email.");
        }
    }

    public void getUser() {
        if (onlineUser.getUserID() == null){
            showProgressDialog(this,"Chargement de vos informations ...");
            db.collection("users").whereEqualTo("userID", userID).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            dismissProgressDialog();
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                                onlineUser = documentSnapshot.toObject(User.class);

                                textViewName.setText(DataHolder.onlineUser.getUserName());
                                textViewPhoneNumber.setText(DataHolder.onlineUser.getUserPhoneNumber());
                                textViewAdress.setText(DataHolder.onlineUser.getAddress());
                                textViewEmail.setText(DataHolder.onlineUser.getEmail());
                                getDahiraToUpdate();
                            }
                        }
                    });
        }
        else {
            textViewName.setText(DataHolder.onlineUser.getUserName());
            textViewPhoneNumber.setText(DataHolder.onlineUser.getUserPhoneNumber());
            textViewAdress.setText(DataHolder.onlineUser.getAddress());
            textViewEmail.setText(DataHolder.onlineUser.getEmail());
            getDahiraToUpdate();
        }
    }

    public boolean isAdminUpdated() {
        boolean updated = true;
        if (!onlineUser.getListDahiraID().isEmpty()) {
            if (onlineUser.getListUpdatedDahiraID().isEmpty()) {
                dahiraToUpdate = onlineUser.getListDahiraID().get(0);
                updated = false;
            }
            else {
                for (String dahiraID : onlineUser.getListDahiraID()) {
                    if (!onlineUser.getListUpdatedDahiraID().contains(dahiraID)) {
                        dahiraToUpdate = dahiraID;
                        updated = false;
                        break;
                    }
                }
            }
        }
        return updated;
    }

    public void getDahiraToUpdate() {
        if (!isAdminUpdated()){
            showProgressDialog(this,"Chargement du dahira ...");
            db.collection("dahiras").whereEqualTo("dahiraID", dahiraToUpdate).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            dismissProgressDialog();
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                dahira = documentSnapshot.toObject(Dahira.class);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dismissProgressDialog();
                            toastMessage(getApplicationContext(), "Error chargement dahiraToUpdate!");
                            Log.d(TAG, e.toString());
                        }
                    });
            showProgressDialog(this, "Patientez svp ...");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismissProgressDialog();
                    startActivity(new Intent(ProfileActivity.this, UpdateAdminActivity.class));
                }
            }, 3000);
        }
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

            case R.id.myDahiras:
                boolMyDahiras = true;
                startActivity(new Intent(this, ListDahiraActivity.class));
                break;

            case R.id.allDahiras:
                boolAllDahiras = true;
                startActivity(new Intent(this, ListDahiraActivity.class));
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
}
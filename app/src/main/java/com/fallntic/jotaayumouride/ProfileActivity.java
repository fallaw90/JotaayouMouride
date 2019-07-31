package com.fallntic.jotaayumouride;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fallntic.jotaayumouride.DataHolder.actionSelected;
import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.logout;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.showImage;
import static com.fallntic.jotaayumouride.DataHolder.showProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.DataHolder.userID;
import static com.fallntic.jotaayumouride.MainActivity.progressBar;
import static com.fallntic.jotaayumouride.MainActivity.relativeLayoutProgressBar;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener,
        DrawerMenu, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "ProfileActivity";
    private static final int CHOOSE_IMAGE = 101;

    private TextView textViewName;
    private TextView textViewAdress;
    private TextView textViewPhoneNumber;
    private TextView textViewEmail;
    private CircleImageView imageViewProfile;
    private LinearLayout linearLayoutVerificationNeeded;
    private LinearLayout linearLayoutVerified;
    private SwipeRefreshLayout swipeLayout;

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String dahiraToUpdate;

    final Handler handler = new Handler();

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private View navHeader;
    private CircleImageView navImageView;
    private TextView textViewNavUserName;
    private TextView textViewNavEmail;
    private LinearLayout linEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setSubtitle("Mon profil");
        setSupportActionBar(toolbar);

        //Check if device has internet connection
        if (!isConnected(this)) {
            finish();
            Intent intent = new Intent(this, LoginActivity.class);
            showAlertDialog(this, "Oops! Pas de connexion, verifier votre connexion internet puis reesayez SVP", intent);
        }

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        firebaseUser = mAuth.getCurrentUser();

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        textViewAdress = findViewById(R.id.textView_userAddress);
        textViewEmail = findViewById(R.id.textView_email);
        textViewName = findViewById(R.id.textView_userName);
        textViewPhoneNumber = findViewById(R.id.textView_userPhoneNumber);
        imageViewProfile = findViewById(R.id.imageView);
        linearLayoutVerificationNeeded = findViewById(R.id.linearLayout_verificationNeeded);
        linearLayoutVerified = findViewById(R.id.linearLayout_verified);
        linEmail = findViewById(R.id.lin_email);

        swipeLayout = findViewById(R.id.swipeToRefresh);

        //ProgressBar from static variable MainActivity
        relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        progressBar = findViewById(R.id.progressBar);
        relativeLayoutProgressBar.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        //********************** Drawer Menu *************************
        setDrawerMenu();
        //************************************************************

        loadUserInformation();

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                startActivity(new Intent(ProfileActivity.this, ProfileActivity.class));
                swipeLayout.setRefreshing(false);
            }
        });

        actionSelected = "";

        saveTokenID();

        findViewById(R.id.button_verifyEmail).setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginPhoneActivity.class));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_verifyEmail:
                firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        logout(ProfileActivity.this);
                        finish();
                        startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                        toastMessage(getApplicationContext(), "Verification Email envoyee");
                    }
                });
                break;
        }
    }

    private void loadUserInformation() {

        if (firebaseUser != null) {
            if (firebaseUser.getEmail() != null && !firebaseUser.getEmail().equals("")) {
                if (firebaseUser.isEmailVerified()) {
                    //Get the current user info
                    getUser();
                    linearLayoutVerificationNeeded.setVisibility(View.GONE);
                } else {
                    linearLayoutVerified.setVisibility(View.GONE);
                }
            }
            else if (firebaseUser.getPhoneNumber() != null && !firebaseUser.getPhoneNumber().equals("")){
                toastMessage(this, firebaseUser.getPhoneNumber());
                linearLayoutVerificationNeeded.setVisibility(View.GONE);
                textViewNavEmail.setVisibility(View.GONE);
                linEmail.setVisibility(View.GONE);
                getUser();
            }
            showImage(this, "profileImage", userID, imageViewProfile);
        }
    }


    public void getUser() {
        if (onlineUser == null) {
            setLayoutInvisible();
            db.collection("users").whereEqualTo("userID", userID).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            setLayoutVisible();
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                                onlineUser = documentSnapshot.toObject(User.class);

                                textViewName.setText(DataHolder.onlineUser.getUserName());
                                textViewPhoneNumber.setText(DataHolder.onlineUser.getUserPhoneNumber());
                                textViewAdress.setText(DataHolder.onlineUser.getAddress());
                                textViewEmail.setText(DataHolder.onlineUser.getEmail());
                                textViewNavUserName.setText(onlineUser.getUserName());
                                textViewNavEmail.setText(onlineUser.getEmail());
                                getDahiraToUpdate();
                            }
                        }
                    });
        } else {
            setDrawerMenu();
            textViewName.setText(onlineUser.getUserName());
            textViewPhoneNumber.setText(onlineUser.getUserPhoneNumber());
            textViewAdress.setText(onlineUser.getAddress());
            textViewEmail.setText(onlineUser.getEmail());
            textViewNavUserName.setText(onlineUser.getUserName());
            textViewNavEmail.setText(onlineUser.getEmail());
            getDahiraToUpdate();
        }
    }

    public boolean isAdminUpdated() {
        boolean updated = true;
        if (!onlineUser.getListDahiraID().isEmpty()) {
            if (onlineUser.getListUpdatedDahiraID().isEmpty()) {
                dahiraToUpdate = onlineUser.getListDahiraID().get(0);
                updated = false;
            } else {
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
        if (!isAdminUpdated()) {
            setLayoutInvisible();
            db.collection("dahiras").whereEqualTo("dahiraID", dahiraToUpdate).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            setLayoutVisible();
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                dahira = documentSnapshot.toObject(Dahira.class);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            setLayoutVisible();
                            toastMessage(getApplicationContext(), "Error chargement dahiraToUpdate!");
                            Log.d(TAG, e.toString());
                        }
                    });
            showProgressDialog(this, "Patientez svp ...");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setLayoutVisible();
                    startActivity(new Intent(ProfileActivity.this, UpdateAdminActivity.class));
                }
            }, 3000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_menu, menu);

        MenuItem iconBack;
        iconBack = menu.findItem(R.id.icon_back);

        //iconBack.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item))
            return true;
        /*
        switch (item.getItemId()){
            case R.id.icon_back:
                finish();
                break;
        }
        */
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.nav_home:
                startActivity(new Intent(this, MainActivity.class));
                break;

            case R.id.nav_profile:
                startActivity(new Intent(this, ProfileActivity.class));
                break;

            case R.id.nav_displayMyDahira:
                DataHolder.displayDahira = "myDahira";
                startActivity(new Intent(this, ListDahiraActivity.class));
                break;

            case R.id.nav_addDahira:
                startActivity(new Intent(this, CreateDahiraActivity.class));
                break;

            case R.id.nav_displayAllDahira:
                DataHolder.displayDahira = "allDahira";
                startActivity(new Intent(this, ListDahiraActivity.class));
                break;

            case R.id.nav_displayAllEvent:
                DataHolder.loadEvent = "allEvents";
                startActivity(new Intent(this, ListEventActivity.class));
                break;

            case R.id.nav_searchDahira:
                actionSelected = "searchDahira";
                DataHolder.displayDahira = "allDahira";
                startActivity(new Intent(this, ListDahiraActivity.class));
                break;

            case R.id.nav_setting:
                startActivity(new Intent(this, SettingProfileActivity.class));
                break;

            case R.id.nav_logout:
                toastMessage(this, "Logged out");
                logout(this);
                finish();
                startActivity(new Intent(this, MainActivity.class));
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void hideMenuItem() {
        Menu nav_Menu = navigationView.getMenu();
        nav_Menu.findItem(R.id.nav_setting).setTitle("Modifier mon profil");

        nav_Menu.findItem(R.id.nav_home).setVisible(false);

        nav_Menu.findItem(R.id.nav_displayUsers).setVisible(false);
        nav_Menu.findItem(R.id.nav_addUser).setVisible(false);
        nav_Menu.findItem(R.id.nav_searchUser).setVisible(false);

        nav_Menu.findItem(R.id.nav_finance).setVisible(false);
        nav_Menu.findItem(R.id.nav_gallery).setVisible(false);
        nav_Menu.findItem(R.id.nav_contact).setVisible(false);

        nav_Menu.findItem(R.id.nav_addAnnouncement).setVisible(false);
        nav_Menu.findItem(R.id.nav_displayAnnouncement).setVisible(false);
        nav_Menu.findItem(R.id.nav_addEvent).setVisible(false);
        nav_Menu.findItem(R.id.nav_displayEvent).setVisible(false);

    }

    public void setDrawerMenu() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navHeader = navigationView.getHeaderView(0);
        navImageView = navHeader.findViewById(R.id.nav_imageView);
        textViewNavUserName = navHeader.findViewById(R.id.textView_navUserName);
        textViewNavEmail = navHeader.findViewById(R.id.textView_navEmail);
        toggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        showImage(this, "profileImage", userID, navImageView);

        navigationView.setCheckedItem(R.id.nav_profile);
        hideMenuItem();
    }

    public void saveTokenID() {

        FirebaseMessaging.getInstance().subscribeToTopic("JotaayouMouride");

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token_id = task.getResult().getToken();

                        Map<String, Object> tokenMap = new HashMap<>();
                        tokenMap.put("tokenID", token_id);
                        //toastMessage(ProfileActivity.this, token_id);

                        FirebaseFirestore.getInstance().collection("users")
                                .document(userID).update(tokenMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        dismissProgressDialog();
                                    }
                                });

                        // Log and toast
                        Log.d(TAG, token_id);
                    }
                });
    }

    public void setLayoutInvisible(){
        swipeLayout.setVisibility(View.GONE);
        relativeLayoutProgressBar.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void setLayoutVisible(){
        swipeLayout.setVisibility(View.VISIBLE);
        relativeLayoutProgressBar.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }
}
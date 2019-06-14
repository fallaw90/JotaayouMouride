package com.fallntic.jotaayumouride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ProfileAdminActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int CHOOSE_IMAGE = 101;

    private TextView textViewName;
    private TextView textViewVerifyEmail;
    private ImageView imageViewProfile;
    private LinearLayout linearLayoutVerificationNeeded;
    private LinearLayout linearLayoutVerified;
    private SwipeRefreshLayout swipeLayout;

    private ProgressDialog progressDialog;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseFirestore db;

    private RecyclerView recyclerViewDahira;
    private DahiraAdapter dahiraAdapter;
    private List<Dahira> dahiraList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_admin);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);

        textViewName = (TextView) findViewById(R.id.textView_userName);
        imageViewProfile = (ImageView) findViewById(R.id.imageView);
        textViewVerifyEmail = (TextView) findViewById(R.id.textView_verifyEmail);

        //Retrieve userID and picture
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        String userID = mAuth.getCurrentUser().getUid();



        //Check user's auth
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        loadUserInformation(userID);



        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                startActivity(new Intent(ProfileAdminActivity.this, ProfileAdminActivity.class));
                swipeLayout.setRefreshing(false);
            }
        });

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
    public void onClick(View v) {

        switch(v.getId()){
            case R.id.button_verifyEmail:
                firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        logout();
                        finish();
                        startActivity(new Intent(ProfileAdminActivity.this, LoginActivity.class));
                        toastMessage("Verification Email envoye");
                    }
                });
                break;
        }
    }


    private void loadUserInformation(String userID) {

        //Upload image from firestore
        progressDialog.setMessage("Chargement de l'image ...");
        if (progressDialog != null) {
            progressDialog.show();
        }

        // Reference to the image file in Cloud Storage
        final StorageReference profileImageReference = storageReference.child("images").child(userID);

        // Download directly from StorageReference using Glide
        GlideApp.with(ProfileAdminActivity.this)
                .load(profileImageReference)
                .placeholder(R.drawable.icon_camera)
                .into(imageViewProfile);

        linearLayoutVerificationNeeded = (LinearLayout) findViewById(R.id.linearLayout_verificationNeeded);
        linearLayoutVerified = (LinearLayout) findViewById(R.id.linearLayout_verified);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        if (firebaseUser != null) {

            if (firebaseUser.isEmailVerified()) {
                linearLayoutVerified.setVisibility(View.VISIBLE);
                toastMessage("Email verified");

                showListDahira();

            }
            else {
                linearLayoutVerificationNeeded.setVisibility(View.VISIBLE);
                toastMessage("Email non verified");
            }
        }
    }

    private void showListDahira() {

        //Attach adapter to recyclerView
        recyclerViewDahira = findViewById(R.id.recyclerview_dahiras);
        recyclerViewDahira.setHasFixedSize(true);
        recyclerViewDahira.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDahira.setVisibility(View.VISIBLE);
        dahiraList = new ArrayList<>();
        dahiraAdapter = new DahiraAdapter(this, dahiraList);
        recyclerViewDahira.setAdapter(dahiraAdapter);

        progressBar = findViewById(R.id.progressbar);
        db = FirebaseFirestore.getInstance();
        db.collection("dahiras").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        progressBar.setVisibility(View.GONE);

                        if (!queryDocumentSnapshots.isEmpty()) {

                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                            for (DocumentSnapshot documentSnapshot : list) {

                                Dahira dahira = documentSnapshot.toObject(Dahira.class);
                                dahira.setDahiraID(documentSnapshot.getId());
                                dahiraList.add(dahira);

                            }

                            dahiraAdapter.notifyDataSetChanged();

                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.logout:

                logout();
                finish();
                startActivity(new Intent(this, MainActivity.class));

                break;
        }

        return true;
    }

    public void logout(){
        DataHolder.dahira = null;
        DataHolder.dahiraID = "";
        DataHolder.userID = "";
        DataHolder.listDahiraID.removeAll(DataHolder.listDahiraID);
        DataHolder.listCommission.removeAll(DataHolder.listCommission);
        DataHolder.listResponsible.removeAll(DataHolder.listResponsible);
        DataHolder.dahira = null;

        FirebaseAuth.getInstance().signOut();

    }

    public void reloadActivity(){
        Intent i = new Intent(ProfileAdminActivity.this, ProfileAdminActivity.class);
        finish();
        overridePendingTransition(0, 0);
        startActivity(i);
        overridePendingTransition(0, 0);
    }

    public void toastMessage(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}

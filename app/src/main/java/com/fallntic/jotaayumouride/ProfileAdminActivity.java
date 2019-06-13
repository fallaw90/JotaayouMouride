package com.fallntic.jotaayumouride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

public class ProfileAdminActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int CHOOSE_IMAGE = 101;

    private TextView textViewName;
    private TextView textViewVerifyEmail;
    private ImageView imageViewProfile;
    private LinearLayout linearLayoutVerificationNeeded;
    private ScrollView scrollView;

    private ProgressDialog progressDialog;

    private String profileImageUrl;

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_admin);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        String userID = mAuth.getCurrentUser().getUid();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        textViewName = (TextView) findViewById(R.id.textView_userName);
        imageViewProfile = (ImageView) findViewById(R.id.imageView);
        textViewVerifyEmail = (TextView) findViewById(R.id.textView_verifyEmail);
        linearLayoutVerificationNeeded = (LinearLayout) findViewById(R.id.linearLayout_verificationNeeded);
        scrollView = (ScrollView) findViewById(R.id.scrollView);

        loadUserInformation(userID);

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
                        toastMessage("Verification Email envoye");
                    }
                });
                break;
        }
    }


    private void loadUserInformation(String userID) {

        //Upload image from firestore
        progressDialog.setMessage("Chargement de l'image ...");
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.show();
        }

        // Reference to the image file in Cloud Storage
        final StorageReference profileImageReference = storageReference.child("images").child(userID);

        // Download directly from StorageReference using Glide
        GlideApp.with(ProfileAdminActivity.this)
                .load(profileImageReference)
                .into(imageViewProfile);

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        if (firebaseUser != null) {

            if (firebaseUser.isEmailVerified()) {
                linearLayoutVerificationNeeded.setVisibility(View.INVISIBLE);
                scrollView.setVisibility(View.VISIBLE);
            }
            else {
                linearLayoutVerificationNeeded.setVisibility(View.VISIBLE);
                scrollView.setVisibility(View.INVISIBLE);
            }
        }
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

                DataHolder.dahira = null;
                DataHolder.dahiraID = "";
                DataHolder.listDahiraID.removeAll(DataHolder.listDahiraID);
                DataHolder.listCommission.removeAll(DataHolder.listCommission);
                DataHolder.listResponsible.removeAll(DataHolder.listResponsible);
                DataHolder.dahira = null;

                FirebaseAuth.getInstance().signOut();

                finish();
                startActivity(new Intent(this, MainActivity.class));


                break;
        }

        return true;
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

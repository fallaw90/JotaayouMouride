package com.fallntic.jotaayumouride;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import static com.fallntic.jotaayumouride.Utility.DataHolder.*;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutProgressBar;

public class SettingProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "UpdateUserActivity";

    private ProgressDialog progressDialog;

    private EditText editTextUserName;
    private EditText editTextAddress;

    private ImageView imageView;
    private Uri uri;
    private final int PICK_IMAGE_REQUEST = 71;
    private boolean imageSaved = true, userSaved = true;

    //Firebase
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_profile);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Parametres");
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        checkInternetConnection(this);

        progressDialog = new ProgressDialog(this);

        initViews();
        displayViews();

        hideSoftKeyboard();
    }

    private void initViews(){

        editTextUserName = findViewById(R.id.editText_userName);
        editTextAddress = findViewById(R.id.editText_address);
        imageView = findViewById(R.id.imageView);

        findViewById(R.id.button_update).setOnClickListener(this);
        findViewById(R.id.imageView).setOnClickListener(this);

        initViewsProgressBar();
    }

    public  void initViewsProgressBar() {
        relativeLayoutData = findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        progressBar = findViewById(R.id.progressBar);
    }

    private void displayViews(){
        editTextUserName.setText(onlineUser.getUserName());
        editTextAddress.setText(onlineUser.getAddress());

        showImage(this, "profileImage", onlineUser.getUserID(), imageView);
    }

    @Override
    protected void onDestroy() {
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
            case R.id.imageView:
                checkPermission();
                chooseImage();
                break;
            case R.id.button_update:
                updateData();
                break;
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Choisir une image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null ) {
            try {
                uri = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {
        if(uri != null && onlineUser.getUserID() != null) {
            showProgressBar();
            final StorageReference ref = storageReference.child("profileImage").child(userID);
            ref.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            hideProgressBar();
                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!urlTask.isSuccessful());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressBar();
                            imageSaved = false;
                            System.out.println("Failed "+e.getMessage());
                        }
                    });
        }
    }

    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);

        }
    }


    public void updateData(){

        String name = editTextUserName.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();

        if(!hasValidationErrors(name, address)){
            onlineUser.setUserName(name);
            onlineUser.setAddress(address);

            showProgressBar();
            db.collection("users").document(onlineUser.getUserID())
                    .update("userName", onlineUser.getUserName(),
                            "address", onlineUser.getAddress())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            hideProgressBar();
                            uploadImage();
                            showAlertDialog(SettingProfileActivity.this, "Enregistrement reussi");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressBar();
                            System.out.println("Error update data");
                        }
                    });

            startActivity(new Intent(SettingProfileActivity.this, HomeActivity.class));
        }
    }


    private boolean hasValidationErrors(String name, String address) {

        if (name.isEmpty()) {
            editTextUserName.setError("Ce champ est obligatoir!");
            editTextUserName.requestFocus();
            return true;
        }

        if (address.isEmpty()) {
            editTextAddress.setError("Ce champ est obligatoir!");
            editTextAddress.requestFocus();
            return true;
        }

        return false;
    }


    public void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}

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
import android.util.Log;
import android.view.View;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.createNewCollection;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.showProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.DataHolder.userID;

public class SignUpPhoneActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SignUpActivity";
    private final int PICK_IMAGE_REQUEST = 71;
    private String userName;
    private String userAddress;
    private ImageView imageView;
    private EditText editTextUserName;
    private EditText editTextUserAddress;
    private List<String> listSass = new ArrayList<String>();
    private List<String> listRoles = new ArrayList<String>();
    private List<String> listAdiya = new ArrayList<String>();
    private List<String> listSocial = new ArrayList<String>();
    private List<String> listDahiraID = new ArrayList<String>();
    private List<String> listCommissions = new ArrayList<String>();
    private List<String> listUpdatedDahiraID = new ArrayList<String>();
    private Uri uri;
    private boolean imageSaved = true, userSaved = true;

    private ProgressDialog progressDialog;

    private FirebaseUser firebaseUser;

    //Firebase
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_phone);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (!isConnected(this)) {
            finish();
            Intent intent = new Intent(this, LoginActivity.class);
            showAlertDialog(this, "Oops! Pas de connexion, verifier votre connexion internet puis reesayez SVP", intent);
        }

        //Initialize Firestore object
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        firebaseUser = mAuth.getCurrentUser();

        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        progressDialog = new ProgressDialog(this);

        //User info
        imageView = findViewById(R.id.imageView);
        editTextUserName = findViewById(R.id.editText_name);
        editTextUserAddress = findViewById(R.id.editText_address);

        //Check access gallery permission
        checkPermission();

        findViewById(R.id.button_back).setOnClickListener(this);
        findViewById(R.id.button_signUp).setOnClickListener(this);
        findViewById(R.id.imageView).setOnClickListener(this);
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

        switch (v.getId()) {
            case R.id.imageView:
                checkPermission();
                chooseImage();
                break;
            case R.id.button_signUp:
                registration();
                break;
            case R.id.button_back:
                finish();
                break;
        }
    }

    private void registration() {
        //Info user
        userName = editTextUserName.getText().toString().trim();
        userAddress = editTextUserAddress.getText().toString().trim();

        if (!hasValidationErrors(userName, userAddress)) {
            saveUser();
            uploadImage(userID);
            Intent intent = new Intent(SignUpPhoneActivity.this, ProfileActivity.class);
            startActivity(intent);
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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            try {
                uri = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(String userID) {
        if (uri != null) {
            showProgressDialog(this, "Enregistrement de votre image cours ...");
            final StorageReference ref = storageReference.child("profileImage").child(userID);
            ref.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!urlTask.isSuccessful()) ;
                            dismissProgressDialog();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dismissProgressDialog();
                            imageSaved = false;
                            toastMessage(getApplicationContext(), "Failed " + e.getMessage());
                        }
                    });
        }
    }

    private void saveUser() {

        User user = new User(userID, userName, firebaseUser.getPhoneNumber(), "", userAddress, listDahiraID,
                listUpdatedDahiraID, listCommissions, listAdiya, listSass, listSocial, listRoles);

        showProgressDialog(this, "Enregistrement de vos informations personnelles cours ...");
        //Save user in firestore database
        db.collection("users").document(userID).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dismissProgressDialog();
                        setAllNewCollection();
                        //toastMessage("Utilisateur enregistre avec succes");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissProgressDialog();
                        userSaved = false;
                        toastMessage(getApplicationContext(), "Error adding user!");
                        Log.d(TAG, e.toString());
                    }
                });
    }

    public void setAllNewCollection() {
        if (onlineUser.getUserID() != null)
            userID = onlineUser.getUserID();

        db.collection("listAdiya").document(userID).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (!documentSnapshot.exists()) {
                            initAllCollections();
                            Log.d(TAG, "Collections (Adiya, Sass and Social) created!");
                        } else {
                            Log.d(TAG, "Collections Adiya, Sass and Social exist already!");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissProgressDialog();
                        Log.d(TAG, "Error creating collections Adiya, Sass and Social!");
                        Log.d(TAG, e.toString());
                    }
                });
    }

    public void initAllCollections() {
        Adiya adiya = new Adiya(new ArrayList<String>(), new ArrayList<String>(),
                new ArrayList<String>(), new ArrayList<String>());
        Sass sass = new Sass(new ArrayList<String>(), new ArrayList<String>(),
                new ArrayList<String>(), new ArrayList<String>());
        Social social = new Social(new ArrayList<String>(), new ArrayList<String>(),
                new ArrayList<String>(), new ArrayList<String>());

        createNewCollection(this, "adiya", userID, adiya);
        createNewCollection(this, "sass", userID, sass);
        createNewCollection(this, "social", userID, social);
    }

    private boolean hasValidationErrors(String userName, String userAddress) {

        if (userName.isEmpty()) {
            editTextUserName.setError("Veuillez remplir votre nom");
            editTextUserName.requestFocus();
            return true;
        }

        if (userAddress.isEmpty()) {
            editTextUserAddress.setError("Veuillez fournir votre adresse");
            editTextUserAddress.requestFocus();
            return true;
        }

        return false;
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);

        }
    }

}
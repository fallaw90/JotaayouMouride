package com.fallntic.jotaayumouride;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fallntic.jotaayumouride.Utility.MyStaticFunctions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Objects;

import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.saveProfileImage;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.firebaseStorage;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.firestore;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.storageReference;

public class SettingProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "UpdateUserActivity";

    private EditText editTextUserName;
    private EditText editTextAddress;

    private ImageView imageView;
    private Uri imageUri;
    private final int PICK_IMAGE_REQUEST = 71;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_profile);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Parametres");
        //toolbar.setLogo(R.mipmap.logo);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.logo);

        checkInternetConnection(this);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        checkPermission();

        initViews();
        displayViews();

        hideSoftKeyboard();
    }

    private void initViews() {

        editTextUserName = findViewById(R.id.editText_userName);
        editTextAddress = findViewById(R.id.editText_address);
        imageView = findViewById(R.id.imageView);

        findViewById(R.id.button_update).setOnClickListener(this);
        findViewById(R.id.imageView).setOnClickListener(this);

        initViewsProgressBar();
    }

    public void initViewsProgressBar() {
        relativeLayoutData = findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        progressBar = findViewById(R.id.progressBar);
    }

    private void displayViews() {
        editTextUserName.setText(onlineUser.getUserName());
        editTextAddress.setText(onlineUser.getAddress());

        MyStaticFunctions.showImage(this, onlineUser.getImageUri(), imageView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView:
                if (onlineUser != null && onlineUser.getUserID() != null && !onlineUser.getUserID().equals("")) {
                    chooseImage();
                } else {
                    toastMessage(this, "Une erreur est survenue, merci de reessayer plus tard");
                }
                break;
            case R.id.button_update:
                if (onlineUser != null && onlineUser.getUserID() != null && !onlineUser.getUserID().equals("")) {
                    updateData();
                } else {
                    toastMessage(this, "Une erreur est survenue, merci de reessayer plus tard");
                }
                break;
        }
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        showProgressBar();
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data.getData() != null) {
            final Uri fileUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            final StorageReference fileToUpload = storageReference
                    .child("profileImage").child(onlineUser.getUserID());
            fileToUpload.putFile(fileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileToUpload.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    hideProgressBar();
                                    saveProfileImage(SettingProfileActivity.this, uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressBar();
                            toastMessage(SettingProfileActivity.this, "Une erreur est survenue, merci de reessayer plus tard");
                        }
                    });
        }
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

    public void updateData() {

        String name = editTextUserName.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();

        if (!hasValidationErrors(name, address)) {
            onlineUser.setUserName(name);
            onlineUser.setAddress(address);

            showProgressBar();
            firestore.collection("users").document(onlineUser.getUserID())
                    .update("userName", name,
                            "address", address,
                            "imageUri", onlineUser.getImageUri())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            hideProgressBar();
                            toastMessage(SettingProfileActivity.this, "Enregistrement reussi");
                            startActivity(new Intent(SettingProfileActivity.this, HomeActivity.class));
                            finish();
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
            finish();
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

    public void hideSoftKeyboard() {
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
}

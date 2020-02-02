package com.fallntic.jotaayumouride;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fallntic.jotaayumouride.utility.MyStaticFunctions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.saveProfileImage;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.firebaseStorage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.storageReference;

@SuppressWarnings("unused")
public class SettingProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "UpdateUserActivity";

    private EditText editTextUserName;
    private EditText editTextAddress;

    private ImageView imageView;
    private final int PICK_IMAGE_REQUEST = 71;
    private byte[] uploadBytes;
    private double mProgress = 0;

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();


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

    private void initViewsProgressBar() {
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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && Objects.requireNonNull(data).getData() != null) {
            final Uri fileUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);
                imageView.setImageBitmap(bitmap);
                uploadNewPhoto(fileUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);

        }
    }

    private void updateData() {

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

            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
        return true;
    }

    @SuppressWarnings("ConstantConditions")
    private void uploadNewPhoto(Bitmap bitmap) {
        Log.d(TAG, "uploadNewPhoto: uploading a new image bitmap to storage");
        BackgroundImageResize resize = new BackgroundImageResize(bitmap);
        Uri uri = null;
        resize.execute(uri);
    }

    private void uploadNewPhoto(Uri imagePath) {
        Log.d(TAG, "uploadNewPhoto: uploading a new image uri to storage.");
        BackgroundImageResize resize = new BackgroundImageResize(null);
        resize.execute(imagePath);
    }

    public byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    private void executeUploadTask() {
        Toast.makeText(SettingProfileActivity.this, "uploading image", Toast.LENGTH_SHORT).show();
        //***************************************************************************************
        final String imageName = onlineUser.getUserName() + " " + onlineUser.getUserID();
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("profileImage/" + imageName);

        final UploadTask uploadTask = storageReference.putBytes(uploadBytes);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if (taskSnapshot.getMetadata() != null) {
                    if (taskSnapshot.getMetadata().getReference() != null) {
                        Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Toast.makeText(SettingProfileActivity.this, "Post Success", Toast.LENGTH_SHORT).show();
                                saveProfileImage(SettingProfileActivity.this, uri.toString());
                            }
                        });
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SettingProfileActivity.this, "could not upload photo", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double currentProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                if (currentProgress > (mProgress + 15)) {
                    mProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    Log.d(TAG, "onProgress: upload is " + mProgress + "& done");
                    Toast.makeText(SettingProfileActivity.this, mProgress + "%", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //*********************************** Resize and upload Image ********************************************
    @SuppressLint("StaticFieldLeak")
    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]> {
        Bitmap mBitmap;

        public BackgroundImageResize(Bitmap bitmap) {
            if (bitmap != null) {
                this.mBitmap = bitmap;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(SettingProfileActivity.this, "compressing image", Toast.LENGTH_SHORT).show();
            showProgressBar();
        }

        @Override
        protected byte[] doInBackground(Uri... params) {
            Log.d(TAG, "doInBackground: started.");
            if (mBitmap == null) {
                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), params[0]);
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: IOException: " + e.getMessage());
                }
            }
            byte[] bytes;
            bytes = getBytesFromBitmap(mBitmap, 15);
            return bytes;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            uploadBytes = bytes;
            hideProgressBar();
            executeUploadTask();
        }
    }
}
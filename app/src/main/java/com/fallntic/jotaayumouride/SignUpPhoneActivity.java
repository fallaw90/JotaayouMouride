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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fallntic.jotaayumouride.Model.Adiya;
import com.fallntic.jotaayumouride.Model.Sass;
import com.fallntic.jotaayumouride.Model.Social;
import com.fallntic.jotaayumouride.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikelau.countrypickerx.Country;
import com.mikelau.countrypickerx.CountryPickerCallbacks;
import com.mikelau.countrypickerx.CountryPickerDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.Utility.DataHolder.createNewCollection;
import static com.fallntic.jotaayumouride.Utility.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.Utility.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.showProgressDialog;
import static com.fallntic.jotaayumouride.Utility.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.Utility.DataHolder.userID;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.saveProfileImage;

public class SignUpPhoneActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SignUpActivity";
    private final int PICK_IMAGE_REQUEST = 71;
    private String userName;
    private String userAddress, country, city, imageUri;
    private ImageView imageView;
    private EditText editTextCountry;
    private EditText editTextCity;
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

    private CountryPickerDialog countryPicker;

    private UploadTask uploadTask;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_phone);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //***************** Set logo **********************
        getSupportActionBar().setLogo(R.mipmap.logo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        checkInternetConnection(this);

        initViews();

        //Initialize Firestore object
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        firebaseUser = mAuth.getCurrentUser();

        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        progressDialog = new ProgressDialog(this);

    }

    private void initViews() {
        //User info
        imageView = findViewById(R.id.imageView);
        editTextUserName = findViewById(R.id.editText_name);
        editTextUserAddress = findViewById(R.id.editText_address);
        editTextCountry = findViewById(R.id.editText_country);
        editTextCity = findViewById(R.id.editText_city);

        findViewById(R.id.button_back).setOnClickListener(this);
        findViewById(R.id.button_signUp).setOnClickListener(this);
        findViewById(R.id.imageView).setOnClickListener(this);
        findViewById(R.id.editText_country).setOnClickListener(this);
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
                //Check access gallery permission
                checkPermission();
                chooseImage();
                break;
            case R.id.editText_country:
                getCountry();
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
        country = editTextCountry.getText().toString().trim();
        city = editTextCity.getText().toString().trim();

        if (!hasValidationErrors(userName, userAddress, city)) {
            userAddress = userAddress.concat("\n" + city + ", " + country);
            saveUser();
        }
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data.getData() != null) {

            fileUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {
        final StorageReference fileToUpload = storageReference
                .child("profileImage").child(onlineUser.getUserID());
        uploadTask = (UploadTask) fileToUpload.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileToUpload.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                saveProfileImage(SignUpPhoneActivity.this, uri.toString());
                            }
                        });
                    }
                });
    }

    private void saveUser() {
        String token_id = FirebaseInstanceId.getInstance().getToken();
        onlineUser = new User(userID, userName, firebaseUser.getPhoneNumber(), "", userAddress, token_id, imageUri, listDahiraID,
                listUpdatedDahiraID, listCommissions, listAdiya, listSass, listSocial, listRoles);

        showProgressDialog(this, "Enregistrement de vos informations personnelles cours ...");
        //Save user in firestore database
        db.collection("users").document(onlineUser.getUserID()).set(onlineUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        uploadImage();
                        dismissProgressDialog();
                        setAllNewCollection();
                        startActivity(new Intent(SignUpPhoneActivity.this, HomeActivity.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissProgressDialog();
                        userSaved = false;
                        toastMessage(getApplicationContext(), "Error adding user!");
                        Log.d(TAG, e.toString());
                        startActivity(new Intent(SignUpPhoneActivity.this, HomeActivity.class));
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

    private boolean hasValidationErrors(String userName, String userAddress, String city) {

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

        if (city.isEmpty()) {
            editTextCity.setError("Champ obligatoire");
            editTextCity.requestFocus();
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

    public void getCountry() {
        /* Name of your Custom JSON list */
        int resourceId = getResources().getIdentifier("country_avail", "raw", getApplicationContext().getPackageName());

        countryPicker = new CountryPickerDialog(SignUpPhoneActivity.this, new CountryPickerCallbacks() {
            @Override
            public void onCountrySelected(Country country, int flagResId) {
                /* Get Country Name: country.getCountryName(context); */
                editTextCountry.setText(country.getCountryName(SignUpPhoneActivity.this));
                /* Call countryPicker.dismiss(); to prevent memory leaks */
                countryPicker.dismiss();
            }

        /* Set to false if you want to disable Dial Code in the results and true if you want to show it
        Set to zero if you don't have a custom JSON list of countries in your raw file otherwise use
        resourceId for your customly available countries */
        }, false, 0);
        countryPicker.show();
    }
}
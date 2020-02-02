package com.fallntic.jotaayumouride;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
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

import com.fallntic.jotaayumouride.model.Adiya;
import com.fallntic.jotaayumouride.model.Sass;
import com.fallntic.jotaayumouride.model.Social;
import com.fallntic.jotaayumouride.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;
import com.mikelau.countrypickerx.Country;
import com.mikelau.countrypickerx.CountryPickerCallbacks;
import com.mikelau.countrypickerx.CountryPickerDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.createNewCollection;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.dismissProgressDialog;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.saveProfileImage;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showAlertDialog;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.userID;

@SuppressWarnings("unused")
public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SignUpActivity";

    private String pwd;
    private String email;
    private String confPwd;
    private String userName;
    private String userAddress;
    private final List<String> listSass = new ArrayList<>();
    private final List<String> listRoles = new ArrayList<>();
    private ImageView imageView;
    private EditText editTextCity;
    private EditText editTextEmail;
    private EditText editTextCountry;
    private EditText editTextPassword;
    private EditText editTextUserName;
    private EditText editTextUserAddress;
    private EditText editTextUserPhoneNumber;
    private EditText editTextConfirmPassword;
    private CountryCodePicker ccp;
    private final List<String> listAdiya = new ArrayList<>();
    private final List<String> listSocial = new ArrayList<>();
    private final List<String> listDahiraID = new ArrayList<>();
    private final List<String> listCommissions = new ArrayList<>();
    private final List<String> listUpdatedDahiraID = new ArrayList<>();
    private String userPhoneNumber;
    private String city;
    private boolean userSaved = true;

    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private CountryPickerDialog countryPicker;
    private String imageUri;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        //toolbar.setLogo(R.mipmap.logo);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.logo);

        initViews();

        checkInternetConnection(this);

        ccp = findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(editTextUserPhoneNumber);

        //Initialize Firestore object
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        //Firebase
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        //Check access gallery permission
        checkPermission();

        hideSoftKeyboard();
    }

    private void initViews() {

        //User info
        imageView = findViewById(R.id.imageView);
        editTextUserName = findViewById(R.id.editText_name);
        editTextUserPhoneNumber = findViewById(R.id.editText_phoneNumber);
        editTextEmail = findViewById(R.id.editText_email);
        editTextPassword = findViewById(R.id.editText_password);
        editTextConfirmPassword = findViewById(R.id.editText_confirmPassword);
        editTextUserAddress = findViewById(R.id.editText_address);
        editTextCity = findViewById(R.id.editText_city);
        editTextCountry = findViewById(R.id.editText_country);

        findViewById(R.id.button_back).setOnClickListener(this);
        findViewById(R.id.button_signUp).setOnClickListener(this);
        findViewById(R.id.imageView).setOnClickListener(this);

        initViewsProgressBar();
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.imageView:
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
        userPhoneNumber = editTextUserPhoneNumber.getText().toString().trim();
        email = editTextEmail.getText().toString().trim();
        pwd = editTextPassword.getText().toString().trim();
        confPwd = editTextConfirmPassword.getText().toString().trim();
        String country = editTextCountry.getText().toString().trim();
        city = editTextCity.getText().toString().trim();

        if (!hasValidationErrors()) {
            userPhoneNumber = ccp.getFullNumberWithPlus();
            userAddress = userAddress.concat("\n" + city + ", " + country);

            showProgressBar();
            db.collection("users").whereEqualTo("userPhoneNumber", userPhoneNumber).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            hideProgressBar();
                            if (task.isSuccessful()) {
                                if (Objects.requireNonNull(task.getResult()).isEmpty()) {
                                    saveAllData();
                                } else {
                                    showAlertDialog(SignUpActivity.this,
                                            "Numero telephone deja utilise");
                                }
                            } else {
                                hideProgressBar();
                                toastMessage(getApplicationContext(), "Error");
                            }
                        }
                    });
        }
    }

    private void saveAllData() {
        mAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //while (!task.isSuccessful());
                if (task.isSuccessful()) {
                    //Get ID of current user.
                    userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                    //Save user info on the FireBase database
                    saveUser();
                    if (isRegistrationSuccessful()) {
                        Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                        startActivity(intent);
                    }
                } else {
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        showAlertDialog(SignUpActivity.this, "Adresse email deja utilise");

                    } else {
                        toastMessage(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage());
                    }
                }
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && Objects.requireNonNull(data).getData() != null) {

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
                .child("profileImage").child(userName + " " + userID);
        UploadTask uploadTask = (UploadTask) fileToUpload.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileToUpload.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                saveProfileImage(SignUpActivity.this, imageUri);
                            }
                        });
                    }
                });
    }

    private void saveUser() {
        onlineUser = new User(userID, userName, userPhoneNumber, email, userAddress, "", imageUri, listDahiraID,
                listUpdatedDahiraID, listCommissions, listAdiya, listSass, listSocial, listRoles);

        showProgressBar();
        //Save user in firestore database
        db.collection("users").document(userName + userID)
                .set(onlineUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideProgressBar();
                        uploadImage();
                        setAllNewCollection();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideProgressBar();
                        userSaved = false;
                        toastMessage(getApplicationContext(), "Error adding user!");
                        Log.d(TAG, e.toString());
                    }
                });
    }

    private boolean isRegistrationSuccessful() {
        if (userSaved) {
            return true;
        } else {
            deleteUser();
            deleteProfileImage();
            Objects.requireNonNull(mAuth.getCurrentUser()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        showAlertDialog(SignUpActivity.this,
                                "Erreur inscription! Reessayez SVP.");
                    } else {
                        toastMessage(getApplicationContext(),
                                "Erreur inscription! Contactez votre administrateur SVP.");
                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                    }
                }
            });
            return false;
        }
    }

    private void deleteUser() {
        showProgressBar();
        db.collection("users").document(userID).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            dismissProgressDialog();
                        } else {
                            hideProgressBar();
                            toastMessage(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage());
                        }
                    }
                });
    }

    private void deleteProfileImage() {
        showProgressBar();
        //mStorage defined on the onCreate function
        storageReference.child("images").child(userID).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            hideProgressBar();
                            toastMessage(getApplicationContext(), "Image supprimee");
                        } else {
                            hideProgressBar();
                            toastMessage(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage());
                        }
                    }
                });
    }

    private void setAllNewCollection() {
        showProgressBar();
        db.collection("listAdiya").document(userID).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        hideProgressBar();
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
                        hideProgressBar();
                        Log.d(TAG, "Error creating collections Adiya, Sass and Social!");
                        Log.d(TAG, e.toString());
                    }
                });
    }

    private void initAllCollections() {
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

    private boolean hasValidationErrors() {

        if (userName.isEmpty()) {
            editTextUserName.setError("Veuillez remplir votre nom");
            editTextUserName.requestFocus();
            return true;
        }

        if (userPhoneNumber.isEmpty()) {
            editTextUserPhoneNumber.setError("Veuillez entrer numero de telephone");
            editTextUserPhoneNumber.requestFocus();
            return true;
        }
        if (!userPhoneNumber.contains("+")) {
            editTextUserPhoneNumber.setError("Pas d'indicatif svp");
            editTextUserPhoneNumber.requestFocus();
            return true;
        }

        if (city.isEmpty()) {
            editTextCity.setError("Champ obligatoir");
            editTextCity.requestFocus();
            return true;
        }

        if (email.isEmpty()) {
            editTextEmail.setError("Adresse email obligatoire");
            editTextEmail.requestFocus();
            return true;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Adresse email incorrect");
            editTextEmail.requestFocus();
            return true;
        }

        if (pwd.isEmpty()) {
            editTextPassword.setError("Mot de passe obligatoire");
            editTextPassword.requestFocus();
            return true;
        }

        if (pwd.length() < 6) {
            editTextPassword.setError("Longueur minimal 6");
            editTextPassword.requestFocus();
            return true;
        }

        if (confPwd.isEmpty()) {
            editTextConfirmPassword.setError("Confirmer le mot de passe");
            editTextConfirmPassword.requestFocus();
            return true;
        }

        if (!pwd.equals(confPwd)) {
            editTextConfirmPassword.setError("Mot de passe incorrect");
            editTextConfirmPassword.requestFocus();
            return true;
        }

        if (userAddress.isEmpty()) {
            editTextUserAddress.setError("Veuillez fournir votre adresse");
            editTextUserAddress.requestFocus();
            return true;
        }

        return false;
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

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void initViewsProgressBar() {
        relativeLayoutData = findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        progressBar = findViewById(R.id.progressBar);
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
                break;

            case R.id.icon_back:
                finish();
                startActivity(new Intent(this, LoginActivity.class));
                break;

            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
        return true;
    }

    private void getCountry() {
        /* Name of your Custom JSON list */
        countryPicker = new CountryPickerDialog(SignUpActivity.this, new CountryPickerCallbacks() {
            @Override
            public void onCountrySelected(Country country, int flagResId) {
                /* Get Country Name: country.getCountryName(context); */
                editTextCountry.setText(country.getCountryName(SignUpActivity.this));
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
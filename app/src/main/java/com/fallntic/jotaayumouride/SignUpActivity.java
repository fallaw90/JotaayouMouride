package com.fallntic.jotaayumouride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.checkPrefix;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.logout;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.userID;
import static com.fallntic.jotaayumouride.DataHolder.createNewCollection;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.showProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SignUpActivity";

    private String pwd;
    private String email;
    private String confPwd;
    private String userName;
    private String userAddress;
    private String userPhoneNumber;
    private ImageView imageView;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextUserName;
    private EditText editTextUserAddress;
    private EditText editTextUserPhoneNumber;
    private EditText editTextConfirmPassword;
    private List<String> listSass = new ArrayList<String>();
    private List<String> listRoles = new ArrayList<String>();
    private List<String> listAdiya = new ArrayList<String>();
    private List<String> listSocial = new ArrayList<String>();
    private List<String> listDahiraID = new ArrayList<String>();
    private List<String> listCommissions = new ArrayList<String>();
    private List<String> listUpdatedDahiraID = new ArrayList<String>();

    private Uri uri;
    private final int PICK_IMAGE_REQUEST = 71;
    private boolean imageSaved = true, userSaved = true;

    private ProgressDialog progressDialog;

    //Firebase
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!isConnected(this)){
            Intent intent = new Intent(this, LoginActivity.class);
            logout();
            showAlertDialog(this,"Oops! Pas de connexion, verifier votre connexion internet puis reesayez SVP", intent);
        }

        //Initialize Firestore object
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        progressDialog = new ProgressDialog(this);

        //User info
        imageView = (ImageView) findViewById(R.id.imageView);
        editTextUserName = findViewById(R.id.editText_name);
        editTextUserPhoneNumber = findViewById(R.id.editText_phoneNumber);
        editTextEmail = findViewById(R.id.editText_email);
        editTextPassword = findViewById(R.id.editText_password);
        editTextConfirmPassword = findViewById(R.id.editText_confirmPassword);
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
    public void onClick(View v) {

        switch(v.getId()){
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
        userPhoneNumber = editTextUserPhoneNumber.getText().toString().trim();
        email = editTextEmail.getText().toString().trim();
        pwd = editTextPassword.getText().toString().trim();
        confPwd = editTextConfirmPassword.getText().toString().trim();

        if(!hasValidationErrors(userName, userPhoneNumber, email, pwd, confPwd, userAddress)) {
            showProgressDialog(this,"Creation de votre compte ...");
            db.collection("users").whereEqualTo("userPhoneNumber", userPhoneNumber).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                dismissProgressDialog();
                                if (task.getResult().isEmpty()){
                                    saveAllData();
                                }else{
                                    showAlertDialog(SignUpActivity.this,
                                            "Numero telephone deja utilise");
                                    return;
                                }
                            }
                            else {
                                dismissProgressDialog();
                                toastMessage(getApplicationContext(),"Error");
                            }
                        }
                    });
        }
        else{
            return;
        }
    }

    public void saveAllData(){
        mAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                dismissProgressDialog();
                //while (!task.isSuccessful());
                if (task.isSuccessful()) {
                    //Get ID of current user.
                    userID = mAuth.getCurrentUser().getUid();
                    //Upload image
                    uploadImage(userID);
                    //Save user info on the FireBase database
                    saveUser();
                    if(isRegistrationSuccessful()){
                        finish();
                        Intent intent = new Intent(SignUpActivity.this, ProfileActivity.class);
                        startActivity(intent);
                    }
                } else {
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        showAlertDialog(SignUpActivity.this, "Adresse email deja utilise");

                    } else {
                        toastMessage(getApplicationContext(), task.getException().getMessage());
                    }
                }
            }
        });
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

    private void uploadImage(String userID) {
        if(uri != null) {
            showProgressDialog(this, "Enregistrement de votre image cours ...");
            final StorageReference ref = storageReference.child("profileImage").child(userID);
            ref.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!urlTask.isSuccessful());
                            dismissProgressDialog();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dismissProgressDialog();
                            imageSaved = false;
                            toastMessage(getApplicationContext(),"Failed "+e.getMessage());
                        }
                    });
        }
    }

    private void saveUser(){
        User user =  new User(userID, userName, userPhoneNumber, email, userAddress, listDahiraID,
                listUpdatedDahiraID, listCommissions, listAdiya, listSass, listSocial, listRoles);

        showProgressDialog(this,"Enregistrement de vos informations personnelles cours ...");
        //Save user in firestore database
        db.collection("users").document(userID)
                .set(user)
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
                        toastMessage(getApplicationContext(),"Error adding user!");
                        Log.d(TAG, e.toString());
                    }
                });
    }

    private boolean isRegistrationSuccessful(){
        if(userSaved && imageSaved){
            return true;
        }
        else{
            deleteUser();
            deleteProfileImage();
            mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        showAlertDialog(SignUpActivity.this,
                                "Erreur inscription! Reessayez SVP.");
                    }
                    else {
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
        showProgressDialog(this,"Chargement en cours ..");
        db.collection("users").document(userID).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            dismissProgressDialog();
                        }
                        else {
                            dismissProgressDialog();
                            toastMessage(getApplicationContext(), task.getException().getMessage());
                        }
                    }
                });
    }

    private void deleteProfileImage() {
        showProgressDialog(this,"Chargement en cours ..");
        //storageReference defined on the onCreate function
        storageReference.child("images").child(userID).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            dismissProgressDialog();
                            toastMessage(getApplicationContext(), "Image supprimee");
                        }
                        else {
                            dismissProgressDialog();
                            toastMessage(getApplicationContext(), task.getException().getMessage());
                        }
                    }
                });
    }

    public void setAllNewCollection(){
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

    public void initAllCollections(){
        Adiya adiya = new Adiya(new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
        Sass sass = new Sass(new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
        Social social = new Social(new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
        createNewCollection(this,"adiya", userID, adiya);
        createNewCollection(this,"sass", userID, sass);
        createNewCollection(this,"social", userID, social);
    }

    private boolean hasValidationErrors(String userName, String userPhoneNumber, String email,
                                        String pwd, String confPwd, String userAddress) {

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

        if(!userPhoneNumber.isEmpty() && (!userPhoneNumber.matches("[0-9]+") ||
                userPhoneNumber.length() != 9 || !checkPrefix(userPhoneNumber))) {
            editTextUserPhoneNumber.setError("Numero de telephone incorrect");
            editTextUserPhoneNumber.requestFocus();
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

}
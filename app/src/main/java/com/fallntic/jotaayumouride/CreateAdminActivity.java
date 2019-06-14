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
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateAdminActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CreateDahiraActivity";

    private EditText editTextUserName;
    private EditText editTextUserPhoneNumber;
    private EditText editTextUserAddress;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Spinner spinnerCommission;
    private String commission = "";

    private ImageView imageView;
    private Uri uri;
    private final int PICK_IMAGE_REQUEST = 71;

    private boolean accountCreated = true;
    private boolean dahiraSaved = true;
    private boolean userSaved = true;
    private boolean commissionsSaved = true;
    private boolean imageSaved = true;

    private ProgressDialog progressDialog;

    //Firebase
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_admin);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        //Initialize Firestore object
        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();



        //User info
        imageView = (ImageView) findViewById(R.id.imageView);
        editTextUserName = findViewById(R.id.editText_name);
        editTextUserPhoneNumber = findViewById(R.id.editText_phoneNumber);
        editTextEmail = findViewById(R.id.editText_email);
        editTextPassword = findViewById(R.id.editText_password);
        editTextConfirmPassword = findViewById(R.id.editText_confirmPassword);
        editTextUserAddress = findViewById(R.id.editText_address);
        spinnerCommission = findViewById(R.id.spinner_commission);
        imageView = findViewById(R.id.imageView);

        checkPermission();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, DataHolder.listCommission);

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCommission.setAdapter(arrayAdapter);

        spinnerCommission.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                commission = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView <?> parent) {
            }
        });

        findViewById(R.id.button_back).setOnClickListener(this);
        findViewById(R.id.button_finish).setOnClickListener(this);
        findViewById(R.id.imageView).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){
            case R.id.imageView:
                checkPermission();
                chooseImage();
                break;
            case R.id.button_finish:
                registration();
                break;
            case R.id.button_back:
                finish();
                break;
        }
    }


    private void registration() {
        //Info user
        final String userName = editTextUserName.getText().toString().trim();
        final String userAddress = editTextUserAddress.getText().toString().trim();
        final String userPhoneNumber = editTextUserPhoneNumber.getText().toString().trim();
        final String email = editTextEmail.getText().toString().trim();
        final String pwd = editTextPassword.getText().toString().trim();
        final String confPwd = editTextConfirmPassword.getText().toString().trim();
        final String role = "admin";

        if(!hasValidationErrors(userName, userPhoneNumber, email, pwd, confPwd, userAddress)) {
            progressDialog.setMessage("Creation de votre compte cours ...");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            if (progressDialog != null) {
                progressDialog.show();
            }
            mAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    while (!task.isSuccessful());
                    if (task.isSuccessful()) {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.show();
                        }
                        //Get ID of current user.
                        DataHolder.userID = mAuth.getCurrentUser().getUid();

                        //Upload image
                        uploadImage(DataHolder.userID);

                        //Create a collection reference objects.
                        CollectionReference dbDahiras = db.collection("dahiras");

                        //Get ID of the dahira
                        DataHolder.dahiraID = db.collection("dahiras").document().getId();
                        DataHolder.listDahiraID.add(DataHolder.dahiraID);

                        //Save dahira info on the FireBase database
                        saveDahira(dbDahiras, DataHolder.dahiraID);

                        //Save user info on the FireBase database
                        saveUser(DataHolder.userID, DataHolder.dahiraID, DataHolder.dahira.getDahiraName(), userName, email,
                                userPhoneNumber, userAddress, commission, role);

                        //Save list commission on the FireBase database
                        saveCommissions(DataHolder.dahiraID, DataHolder.listCommission, DataHolder.listResponsible);

                        if(isRegistrationSuccessful()){
                            progressDialog.setMessage("Chargement en cours ...");
                            progressDialog.setCancelable(false);
                            progressDialog.setCanceledOnTouchOutside(false);
                            if (progressDialog != null) {
                                progressDialog.show();
                            }
                            startActivity(new Intent(CreateAdminActivity.this, ProfileAdminActivity.class));
                        }
                        else{
                            startActivity(new Intent(CreateAdminActivity.this, LoginActivity.class));
                        }

                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            toastMessage("Adresse email deja utilise");

                        } else {
                            toastMessage(task.getException().getMessage());
                        }
                        accountCreated = false;
                    }
                }
            });
        }
        else{
            return;
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

    private void uploadImage(String userID) {

        if(uri != null) {

            progressDialog.setMessage("Enregistrement de votre image cours ...");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            if (progressDialog != null) {
                progressDialog.show();
            }
            final StorageReference ref = storageReference.child("images").child(userID);
            ref.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!urlTask.isSuccessful());
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            //toastMessage("Image enregistree avec succes");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            imageSaved = false;
                            toastMessage("Failed "+e.getMessage());
                        }
                    });
        }
    }

    private void saveDahira(CollectionReference dbDahiras, String dahiraID){

        progressDialog.setMessage("Enregistrement de votre dahira cours ...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        if (progressDialog != null) {
            progressDialog.show();
        }
        DataHolder.dahira.setDahiraID(dahiraID);
        dbDahiras.document(dahiraID).set(DataHolder.dahira);
        dbDahiras.document(dahiraID).set(DataHolder.dahira)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        //toastMessage("Dahira ajoute avec succes");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        dahiraSaved = false;
                        toastMessage("Error adding user!");
                        Log.d(TAG, e.toString());
                    }
                });
    }

    private void saveUser(String userID, String dahiraID, String dahiraName, String userName, String email, String phoneNumber,
                          String address, String commission, String role){

        //Map user's info
        Map<String, Object> user = new HashMap<>();
        user.put("userID", userID);
        user.put("dahiraID", DataHolder.listDahiraID);
        user.put("dahiraName", dahiraName);
        user.put("name", userName);
        user.put("email", email);
        user.put("phoneNumber", phoneNumber);
        user.put("address", address);
        user.put("commission", commission);
        user.put("role", role);

        progressDialog.setMessage("Enregistrement de vos informations personnelles cours ...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        if (progressDialog != null) {
            progressDialog.show();
        }
        //Save user in firestore database
        db.collection("dahiras").document(dahiraID)
                .collection("members").document(userID)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        //toastMessage("Utilisateur enregistre avec succes");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        userSaved = false;
                        toastMessage("Error adding user!");
                        Log.d(TAG, e.toString());
                    }
                });
    }

    private void saveCommissions(String dahiraID, ArrayList<String> listCommission, ArrayList<String> listResponsible){

        if (listCommission.size() > 1){
            //Map user's info
            Map<String, Object> commissions = new HashMap<>();
            commissions.put("dahiraID", dahiraID);
            commissions.put("listCommission", listCommission);
            commissions.put("listResponsible", listResponsible);

            progressDialog.setMessage("Enregistrement des commissions cours ...");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            if (progressDialog != null) {
                progressDialog.show();
            }
            //Save user in firestore database
            db.collection("dahiras").document(dahiraID)
                    .collection("commission").document(dahiraID).set(commissions)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            //toastMessage("Liste commission enregistree avec succes");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            commissionsSaved = false;
                            toastMessage("Error adding list commission!");
                            Log.d(TAG, e.toString());
                        }
                    });
        }

    }

    private boolean isRegistrationSuccessful(){
        if(accountCreated && userSaved && dahiraSaved && commissionsSaved && imageSaved){
            return true;
        }
        else{
            deleteDahira(DataHolder.dahiraID);
            deletePrifleImage(DataHolder.userID);
            mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        toastMessage("Erreur inscription! Reessayez SVP.");
                    }
                    else {
                        toastMessage("Erreur inscription! Contactez votre administrateur SVP.");
                        startActivity(new Intent(CreateAdminActivity.this, LoginActivity.class));
                    }
                }
            });

            return false;
        }
    }

    private void deleteDahira(String dahiraID) {
        db.collection("dahiras").document(dahiraID).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            toastMessage("Dahira supprime");
                        }
                        else {
                            toastMessage(task.getException().getMessage());
                        }
                    }
                });
    }

    private void deletePrifleImage(String userID) {
        FirebaseStorage firebaseStorage;
        StorageReference storageReference;
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        storageReference.child("images").child(userID).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            toastMessage("Image supprimee");
                        }
                        else {
                            toastMessage(task.getException().getMessage());
                        }
                    }
                });
    }

    private boolean hasValidationErrors(String userName, String userPhoneNumber, String email,
                                        String pwd, String confPwd, String userAddress) {

        if (userName.isEmpty()) {
            editTextUserName.setError("Veuillez remplir votre nom");
            editTextUserName.requestFocus();
            return true;
        }

        if (userPhoneNumber.isEmpty()) {
            editTextUserPhoneNumber.setError("Numero de telephone obligatoire");
            editTextUserPhoneNumber.requestFocus();
            return true;
        }

        if(!userPhoneNumber.matches("[0-9]+") || userPhoneNumber.length() != 9) {
            editTextUserPhoneNumber.setError("Numero de telephone incorrect");
            editTextUserPhoneNumber.requestFocus();
            return true;
        }

        String prefix = userPhoneNumber.substring(0,2);
        boolean validatePrefix;
        switch(prefix){
            case "70":
                validatePrefix = true;
                break;
            case "76":
                validatePrefix = true;
                break;
            case "77":
                validatePrefix = true;
                break;
            case "78":
                validatePrefix = true;
                break;
            default:
                validatePrefix = false;
                break;
        }
        if(!validatePrefix) {
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

    public void toastMessage(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}
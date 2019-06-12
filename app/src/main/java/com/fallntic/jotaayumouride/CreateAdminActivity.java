package com.fallntic.jotaayumouride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreateAdminActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CreateDahiraActivity";
    //private static final int CHOOSE_IMAGE = 101;

    private EditText editTextUserName;
    private EditText editTextUserPhoneNumber;
    private EditText editTextUserAddress;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Spinner spinnerCommission;
    private String commission = "";

    private ImageView imageView;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;

    //Firebase
    FirebaseStorage storage;
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

        mAuth = FirebaseAuth.getInstance();
        //Initialize Firestore object
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

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

        DataHolder.listCommission.add(0, "Selectionner une commission");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, DataHolder.listCommission);

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCommission.setAdapter(arrayAdapter);

        spinnerCommission.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                commission = parent.getItemAtPosition(position).toString();

                if (commission.equals("Selectionner une commission")){
                    commission = "";
                }
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
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(String userID) {

        if(filePath != null) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Eenregistrement de l'image...");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/"+ userID);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            toastMessage("Image enregistree avec succes");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            toastMessage("Failed "+e.getMessage());
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("En cours "+(int)progress+"%");
                        }
                    });
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

            //progressBar.setVisibility(View.VISIBLE);

            mAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    //progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        //Get ID of current user.
                        String userID = mAuth.getCurrentUser().getUid();

                        //Upload image
                        uploadImage(userID);

                        //Create a collection reference objects.
                        CollectionReference dbDahiras = db.collection("dahiras");

                        //Get ID of the dahira
                        String dahiraID = db.collection("dahiras").document().getId();

                        //Save dahira info on the FireBase database
                        saveDahira(dbDahiras, dahiraID);

                        //Save user info on the FireBase database
                        saveUser(userID, dahiraID, DataHolder.dahira.getDahiraName(), userName, email,
                                userPhoneNumber, userAddress, commission, role);

                        //Save list commission on the FireBase database
                        saveCommissions(dahiraID, DataHolder.listCommission, DataHolder.listResponsible);

                        finish();
                        startActivity(new Intent(CreateAdminActivity.this, ProfileActivity.class));

                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            toastMessage("Adresse email deja utilise");

                        } else {
                            toastMessage(task.getException().getMessage());
                        }

                    }
                }
            });
        }
    }

    private void saveDahira(CollectionReference dbDahiras, String dahiraID){

        DataHolder.dahira.setDahiraID(dahiraID);
        dbDahiras.document(dahiraID).set(DataHolder.dahira)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        toastMessage("Dahira ajoute avec succes");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
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
        user.put("dahiraID", dahiraID);
        user.put("dahiraName", dahiraName);
        user.put("name", userName);
        user.put("email", email);
        user.put("phoneNumber", phoneNumber);
        user.put("address", address);
        user.put("commission", commission);
        user.put("role", role);

        //Save user in firestore database
        db.collection("users").document(userID).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        toastMessage("Utilisateur enregistre avec succes");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        toastMessage("Error adding user!");
                        Log.d(TAG, e.toString());
                    }
                });
    }

    private void saveCommissions(String dahiraID, ArrayList<String> listCommission, ArrayList<String> listResponsible){

        if (listCommission.size() > 1){

            listCommission.remove(0);
            //Map user's info
            Map<String, Object> commissions = new HashMap<>();
            commissions.put("dahiraID", dahiraID);
            commissions.put("listCommission", listCommission);
            commissions.put("listResponsible", listResponsible);

            //Save user in firestore database
            db.collection("commissions").document().set(commissions)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            toastMessage("Liste commission enregistree avec succes");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            toastMessage("Error adding list commission!");
                            Log.d(TAG, e.toString());
                        }
                    });
        }

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
    
    public void toastMessage(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}

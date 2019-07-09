package com.fallntic.jotaayumouride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "RegisterActivity";
    private static final int CHOOSE_IMAGE = 101;

    private EditText editTextName;
    private EditText editTextPhoneNumber;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private EditText editTextAddress;
    private EditText editTextCommission;
    private ImageView imageViewProfile;
    private RadioGroup radioRoleGroup;
    private RadioButton radioRoleButton;

    ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    Uri uriProfileImage;

    String profileImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

        editTextName = findViewById(R.id.editText_name);
        editTextPhoneNumber = findViewById(R.id.editText_phoneNumber);
        editTextEmail = findViewById(R.id.editText_email);
        editTextPassword = findViewById(R.id.editText_password);
        editTextConfirmPassword = findViewById(R.id.editText_confirmPassword);
        editTextAddress = findViewById(R.id.editText_address);
        editTextCommission = findViewById(R.id.editText_commission);
        imageViewProfile = (ImageView) findViewById(R.id.imageView);
        radioRoleGroup=(RadioGroup)findViewById(R.id.radioGroup);


        findViewById(R.id.button_signUp).setOnClickListener(this);
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
            case R.id.button_signUp:
                registerUser();
                break;
            case R.id.textView_login:
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }

    }

    private void registerUser() {
        final String name = editTextName.getText().toString().trim();
        final String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        final String email = editTextEmail.getText().toString().trim();
        final String pwd = editTextPassword.getText().toString().trim();
        final String confPwd = editTextConfirmPassword.getText().toString().trim();
        final String address = editTextAddress.getText().toString().trim();
        final String commission = editTextCommission.getText().toString().trim();
        // get selected radio button from radioGroup
        int selectedId = radioRoleGroup.getCheckedRadioButtonId();
        // find the radiobutton by returned id
        radioRoleButton = (RadioButton) findViewById(selectedId);
        String role = (String) radioRoleButton.getText();

        validationFields(name, phoneNumber, email, pwd, confPwd, address);

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    //Get ID of current user.
                    String user_id = mAuth.getCurrentUser().getUid();
                    //Save ID on the realtime DB
                    //saveUser(name, userPhoneNumber, address, commission, role);

                    finish();
                    startActivity(new Intent(RegisterActivity.this, ProfileActivity.class));
                } else {

                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), "You are already registered", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
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
        user.put("userPhoneNumber", phoneNumber);
        user.put("address", address);
        user.put("commission", commission);
        user.put("role", role);

        //Save user in firestore database
        db.collection("users").document(userID).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        toastMessage("User added");
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

    private void validationFields(String name, String phoneNumber, String email, String pwd, String confPwd, String address) {

        if (name.isEmpty()) {
            editTextName.setError("Nom obligatoire");
            editTextName.requestFocus();
            return;
        }

        if (phoneNumber.isEmpty()) {
            editTextPhoneNumber.setError("Numero de telephone obligatoire");
            editTextPhoneNumber.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            editTextEmail.setError("Adresse email obligatoire");
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Adresse email incorrect");
            editTextEmail.requestFocus();
            return;
        }

        if (pwd.isEmpty()) {
            editTextPassword.setError("Mot de passe obligatoire");
            editTextPassword.requestFocus();
            return;
        }

        if (confPwd.isEmpty()) {
            editTextConfirmPassword.setError("Confirmer le mot de passe");
            editTextConfirmPassword.requestFocus();
            return;
        }

        if (!pwd.equals(confPwd)) {
            editTextConfirmPassword.setError("Mot de passe incorrect");
            editTextConfirmPassword.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            editTextAddress.setError("Adresse obligatoire");
            editTextAddress.requestFocus();
            return;
        }
    }

    public boolean isInt(String s) {
        try {
            int i = Integer.parseInt(s);
            return true;
        }
        catch(NumberFormatException er)
        { return false; }
    }

    public void toastMessage(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}

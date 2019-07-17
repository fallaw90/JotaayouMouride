package com.fallntic.jotaayumouride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.logout;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.showProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.R.id.textView_signUp;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private BroadcastReceiver myReceiver = null;
    private static final String TAG = "LoginActivity" ;
    FirebaseAuth mAuth;
    EditText editTextEmail, editTextPassword;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Se connecter");
        setSupportActionBar(toolbar);

        if (!isConnected(this)){
            Intent intent = new Intent(this, LoginActivity.class);
            logout();
            showAlertDialog(this,"Oops! Pas de connexion, verifier votre connexion internet puis reesayez SVP", intent);
        }

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);

        findViewById(textView_signUp).setOnClickListener(this);
        findViewById(R.id.buttonLogin).setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (DataHolder.isConnected(this)){

            if (mAuth.getCurrentUser() != null) {
                String email = mAuth.getCurrentUser().getEmail();
                if (isConnected(this)) {
                    startActivity(new Intent(this, ProfileActivity.class));
                }
                else {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case textView_signUp:
                startActivity(new Intent(this, SignUpActivity.class));
                break;
            case R.id.buttonLogin:
                if (DataHolder.isConnected(this)){
                    userLogin();
                }
                else {
                    toastMessage(getApplicationContext(), "Oops! Vous n'avez pas de connexion internet!");
                }
                break;
        }
    }

    private void userLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (!hasValidationErrorsLogin(email, editTextEmail, password, editTextPassword)){
            showProgressDialog(this, "Connection en cours ...");
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        dismissProgressDialog();
                        finish();
                        Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else {
                        dismissProgressDialog();
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private boolean hasValidationErrorsLogin(String email, EditText editTextEmail, String password,
                                             EditText editTextPassword) {

        if (email.isEmpty()) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return true;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email");
            editTextEmail.requestFocus();
            return true;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return true;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Minimum lenght of password should be 6");
            editTextPassword.requestFocus();
            return true;
        }

        return false;
    }

    public boolean isEmailExist(String email){
        final boolean[] isEmailExist = new boolean[1];
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {

                        isEmailExist[0] = task.getResult().getSignInMethods().isEmpty();
                        isEmailExist[0] = true;
                        if (isEmailExist[0]) {
                            Log.e("TAG", "Email not exist!");
                        } else {
                            Log.e("TAG", "Email exist!");
                        }

                    }
                });
        return isEmailExist[0];
    }

}

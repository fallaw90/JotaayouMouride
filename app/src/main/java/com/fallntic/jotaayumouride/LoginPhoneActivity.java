package com.fallntic.jotaayumouride;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static com.fallntic.jotaayumouride.DataHolder.checkPrefix;

public class LoginPhoneActivity extends AppCompatActivity implements View.OnClickListener {


    private EditText editTextMobile;
    private TextView textViewSignUpEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_phone);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Se connecter");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        editTextMobile = findViewById(R.id.editTextMobile);
        textViewSignUpEmail = findViewById(R.id.textView_signUpEmail);

        findViewById(R.id.buttonContinue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mobile = editTextMobile.getText().toString().trim();

                if (mobile.isEmpty() || mobile.length() != 9 || !checkPrefix(mobile)) {
                    editTextMobile.setError("Numero telephone incorrect");
                    editTextMobile.requestFocus();
                    return;
                }

                Intent intent = new Intent(LoginPhoneActivity.this, VerifyPhoneActivity.class);
                intent.putExtra("mobile", mobile);
                startActivity(intent);
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginPhoneActivity.this, MainActivity.class));
            }
        });

        hideSoftKeyboard();

        findViewById(R.id.textView_signUpEmail).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.textView_signUpEmail:
                finish();
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }
    }

    public void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
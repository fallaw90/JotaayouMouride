package com.fallntic.jotaayumouride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

public class UpdateDahiraActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextDahiraName;
    private EditText editTextDieuwrine;
    private EditText editTextPhoneNumber;
    private EditText editTextSiege;
    private EditText editTextAdiya;
    private EditText editTextSass;
    private EditText editTextSocial;
    private TextView textViewTotalMember;

    private FirebaseFirestore db;

    private Dahira dahira;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_dahira);

        dahira = (Dahira) getIntent().getSerializableExtra("dahira");
        db = FirebaseFirestore.getInstance();

        editTextDahiraName = findViewById(R.id.editText_dahiraName);
        editTextDieuwrine = findViewById(R.id.editText_dieuwrine);
        editTextPhoneNumber = findViewById(R.id.editText_phoneNumber);
        editTextSiege = findViewById(R.id.editText_siege);
        editTextAdiya = findViewById(R.id.editText_adiya);
        editTextSass = findViewById(R.id.editText_sass);
        editTextSocial = findViewById(R.id.editText_social);
        textViewTotalMember = findViewById(R.id.textView_totalMember);

        editTextDahiraName.setText(dahira.getDahiraName());
        editTextDieuwrine.setText(dahira.getDieuwrine());
        editTextPhoneNumber.setText(dahira.getPhoneNumber());
        editTextSiege.setText(dahira.getSiege());
        editTextAdiya.setText(dahira.getAdiya());
        editTextSass.setText(dahira.getSass());
        textViewTotalMember.setText(dahira.getTotalMember());


        findViewById(R.id.button_update).setOnClickListener(this);
        findViewById(R.id.button_delete).setOnClickListener(this);
    }

    private boolean hasValidationErrors(String dahiraName, String dieuwrine, String phoneNumber, String siege) {
        if (dahiraName.isEmpty()) {
            editTextDahiraName.setError("Name required");
            editTextDahiraName.requestFocus();
            return true;
        }

        if (dieuwrine.isEmpty()) {
            editTextDieuwrine.setError("Brand required");
            editTextDieuwrine.requestFocus();
            return true;
        }

        if (phoneNumber.isEmpty()) {
            editTextPhoneNumber.setError("Description required");
            editTextPhoneNumber.requestFocus();
            return true;
        }

        if (siege.isEmpty()) {
            editTextSiege.setError("Price required");
            editTextSiege.requestFocus();
            return true;
        }

        return false;
    }


    private void updateDahira() {
        String name = editTextDahiraName.getText().toString().trim();
        String dieuwrine = editTextDieuwrine.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String siege = editTextSiege.getText().toString().trim();
        String adiya = editTextAdiya.getText().toString().trim();
        String sass = editTextSass.getText().toString().trim();
        String social = editTextSocial.getText().toString().trim();

        if (!hasValidationErrors(name, dieuwrine, phoneNumber, siege)) {

            Dahira dahira = new Dahira(name, dieuwrine, phoneNumber, siege, adiya, sass, social);


            db.collection("dahiras").document(dahira.getDahiraID())
                    .update(
                            "dahiraName", dahira.getDahiraName(),
                            "dieuwrine", dahira.getDieuwrine(),
                            "phoneNumber", dahira.getPhoneNumber(),
                            "siege", dahira.getSiege(),
                            "adiya", dahira.getAdiya(),
                            "sass", dahira.getSass(),
                            "social", dahira.getSocial()
                    )
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(UpdateDahiraActivity.this, "Dahira Updated", Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void deleteProduct() {
        db.collection("dahiras").document(dahira.getDahiraID()).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(UpdateDahiraActivity.this, "Dahira deleted", Toast.LENGTH_LONG).show();
                            finish();
                            startActivity(new Intent(UpdateDahiraActivity.this, ProfileAdminActivity.class));
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_update:
                updateDahira();
                break;
            case R.id.button_delete:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Are you sure about this?");
                builder.setMessage("La suppression est permenante...");

                builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteProduct();
                    }
                });

                builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog ad = builder.create();
                ad.show();

                break;
        }
    }
}
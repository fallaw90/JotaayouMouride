package com.fallntic.jotaayumouride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.fallntic.jotaayumouride.DataHolder.*;
public class AddContributionActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView textViewTitle;
    private EditText editTextDate;
    private EditText editTextAmount;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private String mDate = getCurrentDate();
    private String amount;
    private String typeOfContribution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contribution);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setSubtitle("Ajouter une cotisation");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        textViewTitle = (TextView) findViewById(R.id.textView_title);
        editTextDate = (EditText) findViewById(R.id.editText_date);
        editTextAmount = (EditText) findViewById(R.id.editText_amount);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        textViewTitle.setText(selectedUser.getUserName() + " membre du dahira " + dahira.getDahiraName());
        editTextDate.setText(mDate);

        findViewById(R.id.editText_date).setOnClickListener(this);
        findViewById(R.id.button_save).setOnClickListener(this);
        findViewById(R.id.button_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.editText_date:
                getDate(this, editTextDate);
                break;

            case R.id.button_save:
                boolAddToDahira = true;
                addContribution();
                break;

            case R.id.button_cancel:
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

    public void addContribution(){
        mDate = editTextDate.getText().toString().trim();
        amount = editTextAmount.getText().toString().trim();
        amount = amount.replace(",", ".");
        // get selected radio button from radioGroup
        int selectedId = radioGroup.getCheckedRadioButtonId();
        // find the radiobutton by returned id
        radioButton = (RadioButton) findViewById(selectedId);
        typeOfContribution = (String) radioButton.getText();
        toastMessage(this, typeOfContribution);
        if (!hasValidationErrors(amount, editTextAmount)){
            double value = Double.parseDouble(amount);

            if (typeOfContribution.equals("adiya")) {

                //Save to adiya collection
                saveContribution(this, "adiya",
                        selectedUser.getUserID(), amount, mDate);

                //Update totalAdiya user
                double totalAdiyaUserVerse = Double.parseDouble(selectedUser.getListAdiya()
                        .get(indexSelectedUser)) + value;
                selectedUser.getListAdiya().set(indexSelectedUser, Double.toString(totalAdiyaUserVerse));
                updateDocument(this, "users", selectedUser.getUserID(),
                        "listAdiya", selectedUser.getListAdiya());
            }
            if (typeOfContribution.equals("sass")) {
                saveContribution(AddContributionActivity.this, "sass",
                        selectedUser.getUserID(), amount, mDate);

                //Update totalSass user
                double totalSassUserVerse = Double.parseDouble(selectedUser.getListSass()
                        .get(indexSelectedUser)) + value;
                selectedUser.getListSass().set(indexSelectedUser, Double.toString(totalSassUserVerse));
                updateDocument(this, "users", selectedUser.getUserID(),
                        "listSass", selectedUser.getListSass());

            }

            if (typeOfContribution.equals("social")) {
                saveContribution(AddContributionActivity.this, "social",
                        selectedUser.getUserID(), amount, mDate);

                //Update totalSocial user
                double totalSocialUserVerse = Double.parseDouble(selectedUser.getListSocial()
                        .get(indexSelectedUser)) + value;
                selectedUser.getListSocial().set(indexSelectedUser, Double.toString(totalSocialUserVerse));
                updateDocument(this, "users", selectedUser.getUserID(),
                        "listSocial", selectedUser.getListSocial());
            }
        }
    }

    private boolean hasValidationErrors(String value,EditText editTextValue) {

        if (value.isEmpty() || !isDouble(value)) {
            editTextValue.setError("Valeur incorrect!");
            editTextValue.requestFocus();
            return true;
        }

        return false;
    }
}

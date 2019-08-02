package com.fallntic.jotaayumouride;

import android.annotation.SuppressLint;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.actionSelected;
import static com.fallntic.jotaayumouride.DataHolder.createNewCollection;
import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.expense;
import static com.fallntic.jotaayumouride.DataHolder.getCurrentDate;
import static com.fallntic.jotaayumouride.DataHolder.getDate;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.isDouble;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.updateDocument;
import static com.fallntic.jotaayumouride.MainActivity.progressBar;
import static com.fallntic.jotaayumouride.MainActivity.relativeLayoutProgressBar;
import static com.fallntic.jotaayumouride.NotificationHelper.sendNotificationToSpecificUsers;

public class CreateExpenseActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "CreateExpenseActivity";

    private TextView textViewTitle;
    private EditText editTextDate;
    private EditText editTextNote;
    private EditText editTextPrice;

    private String price;
    private String mDate;
    private String note;
    private String typeOfExpense;
    private String title;

    private RadioGroup radioRoleGroup;
    private RadioButton radioRoleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_expense);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Ajouter une depense");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (!isConnected(this)) {
            finish();
            Intent intent = new Intent(this, LoginActivity.class);
            showAlertDialog(this, "Oops! Pas de connexion, verifier votre connexion internet puis reesayez SVP", intent);
        }

        ListUserActivity.scrollView = findViewById(R.id.scrollView);
        //ProgressBar from static variable MainActivity
        relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        progressBar = findViewById(R.id.progressBar);
        relativeLayoutProgressBar.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        textViewTitle = findViewById(R.id.textView_title);
        editTextDate = findViewById(R.id.editText_date);
        editTextNote = findViewById(R.id.editText_note);
        editTextPrice = findViewById(R.id.editText_price);
        radioRoleGroup = findViewById(R.id.radioGroup);

        textViewTitle.setText("Ajouter une depense pour le dahira " + dahira.getDahiraName());
        editTextDate.setText(getCurrentDate());

        findViewById(R.id.editText_date).setOnClickListener(this);
        findViewById(R.id.button_save).setOnClickListener(this);
        findViewById(R.id.button_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.editText_date:
                getDate(this, editTextDate);
                break;

            case R.id.button_save:
                saveExpense(this);
                break;

            case R.id.button_cancel:
                actionSelected = "";
                finish();
                break;
        }
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

    public void saveExpense(final Context context) {
        mDate = editTextDate.getText().toString().trim();
        note = editTextNote.getText().toString().trim();
        price = editTextPrice.getText().toString().trim();

        // get selected radio button from radioGroup
        int selectedId = radioRoleGroup.getCheckedRadioButtonId();
        // find the radiobutton by returned id
        radioRoleButton = findViewById(selectedId);
        typeOfExpense = (String) radioRoleButton.getText();

        if (!hasValidationErrors(mDate, editTextDate, note, editTextNote, price, editTextPrice)) {

            if (expense == null)
                expense = new Expense();

            expense.setDahiraID(dahira.getDahiraID());
            expense.getListUserID().add(onlineUser.getUserID());
            expense.getListUserName().add(onlineUser.getUserName());
            expense.getListDate().add(mDate);
            expense.getListNote().add(note);
            expense.getListPrice().add(price);
            expense.getListTypeOfExpense().add(typeOfExpense);

            ListUserActivity.showProgressBar();
            FirebaseFirestore.getInstance().collection("expenses").document(dahira.getDahiraID()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @SuppressLint("LongLogTag")
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            ListUserActivity.hideProgressBar();
                            if (documentSnapshot.exists()) {
                                updateExpense(CreateExpenseActivity.this);
                                updateDahira(CreateExpenseActivity.this, price, typeOfExpense, true);
                                Log.d(TAG, "Collection evenement updated");
                            } else {
                                expense = createNewExpenseObject(mDate, note, price, typeOfExpense);
                                createNewCollection(context, "expenses", dahira.getDahiraID(), expense);
                                updateDahira(CreateExpenseActivity.this, price, typeOfExpense, true);
                                Intent intent = new Intent(context, ListExpenseActivity.class);
                                showAlertDialog(context, "Depense ajoutee avec succe", intent);
                                Log.d(TAG, "New Expense added");
                            }

                            title = "Nouvelle Dépense";
                            String message = "Une some de " + price + " FCFA" + " a été dépensée dans le compte " +
                                    typeOfExpense + " de votre dahira " + dahira.getDahiraName() + " par " +
                                    onlineUser.getUserName();

                            ObjNotification objNotification = new ObjNotification();
                            sendNotificationToSpecificUsers(context, objNotification);

                            Log.d(TAG, "Collection evenement created");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @SuppressLint("LongLogTag")
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            ListUserActivity.hideProgressBar();
                            Log.d(TAG, e.toString());
                        }
                    });
        }
    }

    public void updateExpense(final Context context) {

        ListUserActivity.showProgressBar();
        FirebaseFirestore.getInstance().collection("expenses")
                .document(dahira.getDahiraID())
                .update("listUserID", expense.getListUserID(),
                        "listUserName", expense.getListUserName(),
                        "listDate", expense.getListDate(),
                        "listNote", expense.getListNote(),
                        "listPrice", expense.getListPrice(),
                        "listTypeOfExpense", expense.getListTypeOfExpense())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onSuccess(Void aVoid) {
                        ListUserActivity.hideProgressBar();
                        Intent intent = new Intent(context, ListExpenseActivity.class);
                        showAlertDialog(context, "Depense enregistre avec succe", intent);
                        Log.d(TAG, "Expense updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ListUserActivity.hideProgressBar();
                        Intent intent = new Intent(context, ListExpenseActivity.class);
                        showAlertDialog(context, "Erreur lors de l'enregistrement de votre " + typeOfExpense + "." +
                                "\nReessayez plutard SVP", intent);
                        Log.d(TAG, "Error updated expense");
                    }
                });
    }

    public Expense createNewExpenseObject(String mDate, String note, String price, String typeOfExpense) {

        List<String> listUserID = new ArrayList<String>();
        listUserID.add(onlineUser.getUserID());

        List<String> listUserName = new ArrayList<String>();
        listUserName.add(onlineUser.getUserName());

        List<String> listDate = new ArrayList<String>();
        listDate.add(mDate);

        List<String> listNote = new ArrayList<String>();
        listNote.add(note);

        List<String> listPrice = new ArrayList<String>();
        listPrice.add(price);

        List<String> listTypeOfExpense = new ArrayList<String>();
        listTypeOfExpense.add(typeOfExpense);

        Expense expense = new Expense(dahira.getDahiraID(), listUserID, listUserName,
                listDate, listNote, listPrice, listTypeOfExpense);

        return expense;
    }

    public static void updateDahira(Context context, String price, String typeOfExpense, boolean isItemRestored) {
        double total;
        final double value = Double.parseDouble(price);
        typeOfExpense = typeOfExpense.toLowerCase();

        if (typeOfExpense.equals("adiya")) {

            if (!isDouble(dahira.getTotalAdiya()))
                dahira.setTotalAdiya("0");

            if (isItemRestored)
                total = Double.parseDouble(dahira.getTotalAdiya()) - value;
            else
                total = Double.parseDouble(dahira.getTotalAdiya()) + value;

            dahira.setTotalAdiya(Double.toString(total));
            updateDocument(context, "dahiras", dahira.getDahiraID(),
                    "totalAdiya", dahira.getTotalAdiya());
        } else if (typeOfExpense.equals("sass")) {

            if (!isDouble(dahira.getTotalSass()))
                dahira.setTotalSass("0");

            if (isItemRestored)
                total = Double.parseDouble(dahira.getTotalSass()) - value;
            else
                total = Double.parseDouble(dahira.getTotalSass()) + value;

            dahira.setTotalSass(Double.toString(total));
            updateDocument(context, "dahiras", dahira.getDahiraID(),
                    "totalSass", dahira.getTotalSass());
        } else if (typeOfExpense.equals("social")) {

            if (!isDouble(dahira.getTotalSocial()))
                dahira.setTotalSocial("0");

            if (isItemRestored)
                total = Double.parseDouble(dahira.getTotalSocial()) - value;
            else
                total = Double.parseDouble(dahira.getTotalSocial()) + value;

            dahira.setTotalSocial(Double.toString(total));
            updateDocument(context, "dahiras", dahira.getDahiraID(),
                    "totalSocial", dahira.getTotalSocial());
        }
    }

    public static boolean hasValidationErrors(String mDate, EditText editTextDate,
                                              String note, EditText editTextNote,
                                              String price, EditText editTextPrice) {
        if (mDate.isEmpty()) {
            editTextDate.setError("Entrez une date");
            editTextDate.requestFocus();
            return true;
        }
        if (note.isEmpty()) {
            editTextNote.setError("Details de votre depense SVP!");
            editTextNote.requestFocus();
            return true;
        }

        if (price.isEmpty()) {
            editTextPrice.setError("Entrez le prix total");
            editTextPrice.requestFocus();
            return true;
        } else if (!isDouble(price)) {
            editTextPrice.setError("Prix incorrect!");
            editTextPrice.requestFocus();
            return true;
        }

        return false;
    }

}

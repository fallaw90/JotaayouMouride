package com.fallntic.jotaayumouride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.actionSelected;
import static com.fallntic.jotaayumouride.DataHolder.createNewCollection;
import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.event;
import static com.fallntic.jotaayumouride.DataHolder.expense;
import static com.fallntic.jotaayumouride.DataHolder.getCurrentDate;
import static com.fallntic.jotaayumouride.DataHolder.getDate;
import static com.fallntic.jotaayumouride.DataHolder.indexExpenseSelected;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.isDouble;
import static com.fallntic.jotaayumouride.DataHolder.logout;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.showProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.DataHolder.updateDocument;

public class CreateExpenseActivity extends AppCompatActivity implements View.OnClickListener  {
    public static final String TAG = "CreateExpenseActivity";

    private TextView textViewTitle;
    private EditText editTextDate;
    private EditText editTextNote;
    private EditText editTextPrice;

    private String price;
    private String oldPrice = "";
    private String mDate;
    private String note;
    private String typeOfExpense;

    private Button buttonDelete;

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

        if (!isConnected(this)){
            Intent intent = new Intent(this, LoginActivity.class);
            logout();
            showAlertDialog(this,"Oops! Pas de connexion, verifier votre connexion internet puis reesayez SVP", intent);
        }

        textViewTitle = findViewById(R.id.textView_title);
        editTextDate = findViewById(R.id.editText_date);
        editTextNote = findViewById(R.id.editText_note);
        editTextPrice = findViewById(R.id.editText_price);
        buttonDelete = findViewById(R.id.button_delete);

        radioRoleGroup = (RadioGroup) findViewById(R.id.radioGroup);
        // get selected radio button from radioGroup
        int selectedId = radioRoleGroup.getCheckedRadioButtonId();
        // find the radiobutton by returned id
        radioRoleButton = (RadioButton) findViewById(selectedId);

        textViewTitle.setText("Ajouter une depense pour le dahira " + dahira.getDahiraName());
        editTextDate.setText(getCurrentDate());

        if (actionSelected.equals("updateExpense")){
            toolbar.setSubtitle("Modifier cette depense");
            textViewTitle.setText("Modifier cette depense pour le dahira " + dahira.getDahiraName());
            editTextDate.setText(expense.getListDate().get(indexExpenseSelected));
            editTextNote.setText(expense.getListNote().get(indexExpenseSelected));
            editTextPrice.setText(expense.getListPrice().get(indexExpenseSelected));
            typeOfExpense = expense.getListTypeOfExpense().get(indexExpenseSelected);
            oldPrice = expense.getListPrice().get(indexExpenseSelected);
            buttonDelete.setVisibility(View.VISIBLE);
        }

        findViewById(R.id.editText_date).setOnClickListener(this);
        findViewById(R.id.button_save).setOnClickListener(this);
        findViewById(R.id.button_cancel).setOnClickListener(this);
        findViewById(R.id.button_delete).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.editText_date:
                getDate(this, editTextDate);
                break;

            case R.id.button_save:
                if (actionSelected.equals("addNewExpense"))
                    saveExpense(this);
                else if (actionSelected.equals("updateExpense"))
                    updateExpense(this);
                break;

            case R.id.button_cancel:
                actionSelected = "";
                finish();
                break;

            case R.id.button_delete:
                actionSelected = "deleteExpense";
                updateExpense(this);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    public void saveExpense(final Context context){
        mDate = editTextDate.getText().toString().trim();
        note = editTextNote.getText().toString().trim();
        price = editTextPrice.getText().toString().trim();
        typeOfExpense = (String) radioRoleButton.getText();

        if (!hasValidationErrors(mDate, editTextDate, note, editTextNote, price, editTextPrice)){

            showProgressDialog(context,"Enregistrement de votre " + typeOfExpense +" en cours ...");
            FirebaseFirestore.getInstance().collection("expenses").document(dahira.getDahiraID()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @SuppressLint("LongLogTag")
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            dismissProgressDialog();
                            if (documentSnapshot.exists()) {
                                expense = documentSnapshot.toObject(Expense.class);
                                updateExpense(CreateExpenseActivity.this);
                                Log.d(TAG, "Collection evenement updated");
                            }
                            else {
                                expense = createNewExpenseObject(mDate, note, price, typeOfExpense);
                                createNewCollection(context, "expenses", dahira.getDahiraID(), expense);
                                subExpenseOnDahira(price);
                                Intent intent = new Intent(context, ListExpenseActivity.class);
                                showAlertDialog(context, "Depense ajoutee avec succe", intent);
                                Log.d(TAG, "New Expense added");
                            }
                            Log.d(TAG, "Collection evenement created");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @SuppressLint("LongLogTag")
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dismissProgressDialog();
                            Log.d(TAG, e.toString());
                        }
                    });
        }
    }

    public void updateExpense(final Context context){
        mDate = editTextDate.getText().toString().trim();
        note = editTextNote.getText().toString().trim();
        price = editTextPrice.getText().toString().trim();
        typeOfExpense = (String) radioRoleButton.getText();

        if (!hasValidationErrors(mDate, editTextDate, note, editTextNote, price, editTextPrice)) {

            if (actionSelected.equals("addNewExpense")) {
                expense.getListUserID().add(onlineUser.getUserID());
                expense.getListUserName().add(onlineUser.getUserName());
                expense.getListDate().add(mDate);
                expense.getListNote().add(note);
                expense.getListPrice().add(price);
                expense.getListTypeOfExpense().add(typeOfExpense);
            } else if (actionSelected.equals("updateExpense")) {
                expense.getListDate().set(indexExpenseSelected, mDate);
                expense.getListNote().set(indexExpenseSelected, note);
                expense.getListPrice().set(indexExpenseSelected, price);
                expense.getListTypeOfExpense().set(indexExpenseSelected, typeOfExpense);
            } else if (actionSelected.equals("deleteExpense")) {
                expense.getListUserID().remove(indexExpenseSelected);
                expense.getListUserName().remove(indexExpenseSelected);
                expense.getListDate().remove(indexExpenseSelected);
                expense.getListNote().remove(indexExpenseSelected);
                expense.getListPrice().remove(indexExpenseSelected);
                expense.getListTypeOfExpense().remove(indexExpenseSelected);
            }
            showProgressDialog(context, "Enregistrement de votre " + typeOfExpense +" en cours ...");
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

                            dismissProgressDialog();
                            Intent intent = new Intent(context, ListExpenseActivity.class);
                            if (actionSelected.equals("updateExpense")) {
                                addExpenseOnDahira(oldPrice);
                                subExpenseOnDahira(price);
                                showAlertDialog(context, "Depense modifiee avec succe", intent);
                                Log.d(TAG, "Expense updated");
                            } else if (actionSelected.equals("deleteExpense")) {
                                addExpenseOnDahira(oldPrice);
                                showAlertDialog(context, "Depense supprimee avec succe", intent);
                                Log.d(TAG, "Expense deleted");
                            } else if (actionSelected.equals("addNewExpense")) {
                                subExpenseOnDahira(price);
                                showAlertDialog(context, "Depense ajoutee avec succe", intent);
                                Log.d(TAG, "New Expense added");
                            }
                            actionSelected = "";
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @SuppressLint("LongLogTag")
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dismissProgressDialog();
                            actionSelected = "";
                            Intent intent = new Intent(context, ListExpenseActivity.class);
                            showAlertDialog(context, "Erreur lors de l'enregistrement de votre " + typeOfExpense +"." +
                                    "\nReessayez plutard SVP", intent);
                            Log.d(TAG, "Error updated expense");
                        }
                    });
        }
    }

    public Expense createNewExpenseObject(String mDate, String note, String price, String typeOfExpense){

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

    private void addExpenseOnDahira(String price){

        double valueExpense = Double.parseDouble(price);

        if (typeOfExpense.equals("Adiya")) {
            double totalAdiyaDahira = Double.parseDouble(dahira.getTotalAdiya());
            String resultValue = Double.toString(totalAdiyaDahira + valueExpense);
            updateDocument(CreateExpenseActivity.this, "dahiras", dahira.getDahiraID(),
                    "totalAdiya", resultValue);
        }
        else if (typeOfExpense.equals("Sass")) {
            double totalSassDahira = Double.parseDouble(dahira.getTotalAdiya());
            String resultValue = Double.toString(totalSassDahira + valueExpense);
            updateDocument(CreateExpenseActivity.this, "dahiras", dahira.getDahiraID(),
                    "totalSass", resultValue);
        }
        else if (typeOfExpense.equals("Social")) {
            double totalSocialDahira = Double.parseDouble(dahira.getTotalAdiya());
            String resultValue = Double.toString(totalSocialDahira + valueExpense);
            updateDocument(CreateExpenseActivity.this, "dahiras", dahira.getDahiraID(),
                    "totalSocial", resultValue);
        }
        getCurrentDahira();
    }

    private void subExpenseOnDahira(String price) {

        if (price.equals(""))
            price = "00";

        double valueExpense = Double.parseDouble(price);

        if (typeOfExpense.equals("Adiya")) {
            double totalAdiyaDahira = Double.parseDouble(dahira.getTotalAdiya());
            String resultValue = Double.toString(totalAdiyaDahira - valueExpense);
            updateDocument(CreateExpenseActivity.this, "dahiras", dahira.getDahiraID(),
                    "totalAdiya", resultValue);
        }
        else if (typeOfExpense.equals("Sass")) {
            double totalSassDahira = Double.parseDouble(dahira.getTotalAdiya());
            String resultValue = Double.toString(totalSassDahira - valueExpense);
            updateDocument(CreateExpenseActivity.this, "dahiras", dahira.getDahiraID(),
                    "totalSass", resultValue);
        }
        else if (typeOfExpense.equals("Social")) {
            double totalSocialDahira = Double.parseDouble(dahira.getTotalAdiya());
            String resultValue = Double.toString(totalSocialDahira - valueExpense);
            updateDocument(CreateExpenseActivity.this, "dahiras", dahira.getDahiraID(),
                    "totalSocial", resultValue);
        }
        getCurrentDahira();
    }

    public void getCurrentDahira() {
            showProgressDialog(this, "Chargement de votre dhira en cours ...");
        FirebaseFirestore.getInstance().collection("dahiras").whereEqualTo("dahiraID", dahira.getDahiraID()).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            dismissProgressDialog();
                            if (!queryDocumentSnapshots.isEmpty()) {
                                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    dahira = documentSnapshot.toObject(Dahira.class);
                                }
                                Log.d(TAG, "Dahira downloaded");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dismissProgressDialog();
                            Log.d(TAG, "Error downloading dahira");
                        }
                    });
        dismissProgressDialog();
    }

    public static boolean hasValidationErrors(String mDate, EditText editTextDate,
                                              String note, EditText editTextNote,
                                              String price, EditText editTextPrice) {
        if(mDate.isEmpty()) {
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
        }
        else if (!isDouble(price)) {
            editTextPrice.setError("Prix incorrect!");
            editTextPrice.requestFocus();
            return true;
        }

        return false;
    }

}

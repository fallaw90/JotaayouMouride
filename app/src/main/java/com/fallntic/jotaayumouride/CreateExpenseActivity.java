package com.fallntic.jotaayumouride;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fallntic.jotaayumouride.Model.Expense;
import com.fallntic.jotaayumouride.Model.ObjNotification;
import com.fallntic.jotaayumouride.Utility.MyStaticVariables;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import static com.fallntic.jotaayumouride.Utility.DataHolder.actionSelected;
import static com.fallntic.jotaayumouride.Utility.DataHolder.dahira;
import static com.fallntic.jotaayumouride.Utility.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.Utility.DataHolder.getCurrentDate;
import static com.fallntic.jotaayumouride.Utility.DataHolder.getDate;
import static com.fallntic.jotaayumouride.Utility.DataHolder.isDouble;
import static com.fallntic.jotaayumouride.Utility.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.Utility.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.Utility.DataHolder.updateDocument;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.TITLE_EXPENSE_NOTIFICATION;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listExpenses;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.objNotification;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutProgressBar;
import static com.fallntic.jotaayumouride.Utility.NotificationHelper.sendNotificationToSpecificUsers;

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

    private RadioGroup radioRoleGroup;
    private RadioButton radioRoleButton;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_expense);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setLogo(R.mipmap.logo);
        setSupportActionBar(toolbar);

        initViews();

        displayViews();

        checkInternetConnection(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem iconBack;
        iconBack = menu.findItem(R.id.icon_back);

        iconBack.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.icon_back:
                finish();
                startActivity(new Intent(this, DahiraInfoActivity.class));
                break;

            case R.id.instructions:
                startActivity(new Intent(this, InstructionsActivity.class));
                break;
        }
        return true;
    }

    private void initViews() {
        textViewTitle = findViewById(R.id.textView_title);
        editTextDate = findViewById(R.id.editText_date);
        editTextNote = findViewById(R.id.editText_note);
        editTextPrice = findViewById(R.id.editText_price);
        radioRoleGroup = findViewById(R.id.radioGroup);

        findViewById(R.id.editText_date).setOnClickListener(this);
        findViewById(R.id.button_save).setOnClickListener(this);
        findViewById(R.id.button_cancel).setOnClickListener(this);

        initViewsProgressBar();
    }

    public static void updateDahira(Context context, String price, String typeOfExpense, boolean isExpenseDeleted, Expense expense) {
        double total;
        final double value = Double.parseDouble(price);
        typeOfExpense = typeOfExpense.toLowerCase();

        if (typeOfExpense.equals("adiya")) {

            if (!isDouble(dahira.getTotalAdiya()))
                dahira.setTotalAdiya("0");

            if (isExpenseDeleted)
                total = Double.parseDouble(dahira.getTotalAdiya()) + value;
            else
                total = Double.parseDouble(dahira.getTotalAdiya()) - value;

            if (total >= 0) {
                dahira.setTotalAdiya(Double.toString(total));
                updateDocument(context, "dahiras", dahira.getDahiraID(), "totalAdiya", dahira.getTotalAdiya());
            } else {
                showAlertDialog(context, "Impossible d'effectuer cette depense. Vous n'avez pas " + value + " FCFA disponible dans votre caisse adiya");
                return;
            }
        } else if (typeOfExpense.equals("sass")) {

            if (!isDouble(dahira.getTotalSass()))
                dahira.setTotalSass("0");

            if (isExpenseDeleted)
                total = Double.parseDouble(dahira.getTotalSass()) + value;
            else {
                total = Double.parseDouble(dahira.getTotalSass()) - value;
            }

            if (total >= 0) {
                dahira.setTotalSass(Double.toString(total));
                updateDocument(context, "dahiras", dahira.getDahiraID(), "totalSass", dahira.getTotalSass());
            } else {
                showAlertDialog(context, "Impossible d'effectuer cette depense. Vous n'avez pas " + value + " FCFA disponible dans votre caisse sass");
                return;
            }


        } else if (typeOfExpense.equals("social")) {

            if (!isDouble(dahira.getTotalSocial()))
                dahira.setTotalSocial("0");

            if (isExpenseDeleted)
                total = Double.parseDouble(dahira.getTotalSocial()) + value;
            else
                total = Double.parseDouble(dahira.getTotalSocial()) - value;

            if (total >= 0) {
                dahira.setTotalSocial(Double.toString(total));
                updateDocument(context, "dahiras", dahira.getDahiraID(), "totalSocial", dahira.getTotalSocial());
            } else {
                showAlertDialog(context, "Impossible d'effectuer cette depense. Vous n'avez pas " + value + " FCFA dans votre caisse social");
                return;
            }
        }

        objNotification = new ObjNotification(expense.getExpenseID(), onlineUser.getUserID(), dahira.getDahiraID(), TITLE_EXPENSE_NOTIFICATION, expense.getNote());
        sendNotificationToSpecificUsers(context, MyStaticVariables.objNotification);

        if (listExpenses == null)
            listExpenses = new ArrayList<>();

        final Intent intent = new Intent(context, ShowExpenseActivity.class);
        Log.d(TAG, "Expense saved.");
        if (!isExpenseDeleted) {
            listExpenses.add(expense);
            showAlertDialog(context, "Depense enregistree avec succes.", intent);
        } else
            showAlertDialog(context, "Depense suprimee avec succes.", intent);
    }


    private void displayViews() {
        textViewTitle.setText("Ajouter une depense pour le dahira " + dahira.getDahiraName());
        editTextDate.setText(getCurrentDate());
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

    public void initViewsProgressBar() {
        relativeLayoutData = findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        progressBar = findViewById(R.id.progressBar);
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

            final String expenseID = onlineUser.getUserName() + System.currentTimeMillis();
            final Expense expense = new Expense(expenseID, onlineUser.getUserName(), mDate, note, price, typeOfExpense);

            showProgressBar();
            FirebaseFirestore.getInstance().collection("dahiras").
                    document(dahira.getDahiraID())
                    .collection("expenses")
                    .document(expenseID)
                    .set(expense)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            hideProgressBar();
                            updateDahira(context, price, typeOfExpense, false, expense);
                            Intent intent = new Intent(CreateExpenseActivity.this, ShowExpenseActivity.class);
                            showAlertDialog(context, "Depense enregistre avec succes.", intent);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressBar();
                            toastMessage(context, "Erreur d'enregistrement de votre depense.");
                            startActivity(new Intent(context, DahiraInfoActivity.class));
                        }
                    });
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

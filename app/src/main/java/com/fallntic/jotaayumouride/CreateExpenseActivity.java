package com.fallntic.jotaayumouride;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fallntic.jotaayumouride.model.Expense;
import com.fallntic.jotaayumouride.model.ObjNotification;
import com.fallntic.jotaayumouride.utility.MyStaticVariables;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

import static com.fallntic.jotaayumouride.notifications.FirebaseNotificationHelper.sendNotificationToSpecificUsers;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.dismissProgressDialog;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.getCurrentDate;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.getDate;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.isDouble;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showAlertDialog;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.updateDocument;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.TITLE_EXPENSE_NOTIFICATION;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.actionSelected;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.dahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listExpenses;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.objNotification;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutProgressBar;

public class CreateExpenseActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "CreateExpenseActivity";

    private TextView textViewTitle;
    private EditText editTextDate;
    private EditText editTextNote;
    private EditText editTextPrice;

    private String price;
    private String typeOfExpense;

    private RadioGroup radioRoleGroup;

    public static void updateDahira(Context context, String price, String typeOfExpense, boolean isExpenseDeleted, Expense expense) {
        double total;
        final double value = Double.parseDouble(price);
        typeOfExpense = typeOfExpense.toLowerCase();

        switch (typeOfExpense) {
            case "adiya":

                if (isDouble(dahira.getTotalAdiya()))
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
                break;
            case "sass":

                if (isDouble(dahira.getTotalSass()))
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


                break;
            case "social":

                if (isDouble(dahira.getTotalSocial()))
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
                break;
        }

        objNotification = new ObjNotification(expense.getExpenseID(), onlineUser.getUserID(), dahira.getDahiraID(), TITLE_EXPENSE_NOTIFICATION, expense.getNote());
        sendNotificationToSpecificUsers(MyStaticVariables.objNotification);

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
            case android.R.id.home:
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                break;

            case R.id.icon_back:
                startActivity(new Intent(this, DahiraInfoActivity.class));
                finish();
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

    private static boolean hasValidationErrors(String mDate, EditText editTextDate,
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
        } else if (isDouble(price)) {
            editTextPrice.setError("Prix incorrect!");
            editTextPrice.requestFocus();
            return true;
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_expense);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        //toolbar.setLogo(R.mipmap.logo);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.logo);

        initViews();

        displayViews();

        checkInternetConnection(this);

        hideSoftKeyboard();

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

    @SuppressLint("SetTextI18n")
    private void displayViews() {
        textViewTitle.setText("Ajouter une depense pour le dahira " + dahira.getDahiraName());
        editTextDate.setText(getCurrentDate());
    }

    private void initViewsProgressBar() {
        relativeLayoutData = findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        progressBar = findViewById(R.id.progressBar);
    }

    private void saveExpense(final Context context) {
        String date = editTextDate.getText().toString().trim();
        String note = editTextNote.getText().toString().trim();
        price = editTextPrice.getText().toString().trim();

        // get selected radio button from radioGroup
        int selectedId = radioRoleGroup.getCheckedRadioButtonId();
        // find the radiobutton by returned id
        RadioButton radioRoleButton = findViewById(selectedId);
        typeOfExpense = (String) radioRoleButton.getText();

        if (!hasValidationErrors(date, editTextDate, note, editTextNote, price, editTextPrice)) {

            final String expenseID = onlineUser.getUserName() + System.currentTimeMillis();
            final Expense expense = new Expense(expenseID, onlineUser.getUserName(), date, note, price, typeOfExpense);

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

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

}
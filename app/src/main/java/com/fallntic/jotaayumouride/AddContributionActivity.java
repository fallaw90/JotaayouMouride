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
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fallntic.jotaayumouride.Model.ObjNotification;
import com.fallntic.jotaayumouride.Utility.MyStaticVariables;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import static com.fallntic.jotaayumouride.Notifications.FirebaseNotificationHelper.sendNotification;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.dismissProgressDialog;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.getCurrentDate;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.getDate;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.isDouble;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.showAlertDialog;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.showProgressDialog;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.updateDocument;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.adiya;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.boolAddToDahira;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.dahira;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.indexSelectedUser;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.objNotification;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.sass;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.selectedUser;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.social;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.typeOfContribution;

public class AddContributionActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "AddContributionActivity";

    private TextView textViewTitle;
    private EditText editTextDate;
    private EditText editTextAmount;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private String mDate = getCurrentDate();
    private String amount;
    private String token;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contribution);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        //toolbar.setLogo(R.mipmap.logo);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.logo);

        checkInternetConnection(this);

        initViews();

        hideSoftKeyboard();

        HomeActivity.loadBannerAd(this, this);
    }

    public static void saveContribution(final Context context, final String value, final String mDate) {

        String notification_message = "Une somme de " + value + " a ete ajoute dans votre " + "compte " + typeOfContribution + " par " + onlineUser.getUserName();

        if (typeOfContribution.equals("adiya")) {
            adiya.getListDahiraID().add(dahira.getDahiraID());
            adiya.getListUserName().add(onlineUser.getUserName());
            adiya.getListAdiya().add(value);
            adiya.getListDate().add(mDate);
            uploadContribution(context, "adiya", adiya, notification_message);
        } else if (typeOfContribution.equals("sass")) {
            sass.getListDahiraID().add(dahira.getDahiraID());
            sass.getListSass().add(value);
            sass.getListDate().add(mDate);
            sass.getListUserName().add(onlineUser.getUserName());
            uploadContribution(context, "sass", sass, notification_message);
        } else if (typeOfContribution.equals("social")) {
            social.getListSocial().add(value);
            social.getListDahiraID().add(dahira.getDahiraID());
            social.getListDate().add(mDate);
            social.getListUserName().add(onlineUser.getUserName());
            uploadContribution(context, "social", social, notification_message);
        }

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
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.destroy();
        }
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.resume();
        }
    }

    public static void uploadContribution(final Context context, final String collectionName, Object data, final String notification_message) {
        showProgressDialog(context, "Enregistrement " + collectionName + " en cours ...");
        FirebaseFirestore.getInstance().collection(collectionName).document(selectedUser.getUserID())
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        notifyUser(context, notification_message);
                        final Intent intent = new Intent(context, ShowContributionActivity.class);
                        showAlertDialog(context, typeOfContribution + " ajoute avec succe!", intent);
                        Log.d(TAG, "New collection " + collectionName + " set successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error initContributions function line 351");
                    }
                });
    }

    static void notifyUser(Context context, String notification_message) {
        String notificationID = System.currentTimeMillis() + "";
        objNotification = new ObjNotification(notificationID,
                selectedUser.getUserID(), dahira.getDahiraID(),
                MyStaticVariables.TITLE_CONTRIBUTION_NOTIFICATION, notification_message);

        sendNotification(context, selectedUser, objNotification);
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
                finish();
                startActivity(new Intent(this, HomeActivity.class));
                break;

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
        editTextAmount = findViewById(R.id.editText_amount);
        radioGroup = findViewById(R.id.radioGroup);

        textViewTitle.setText(selectedUser.getUserName() + " membre du dahira " + dahira.getDahiraName());
        editTextDate.setText(mDate);

        findViewById(R.id.editText_date).setOnClickListener(this);
        findViewById(R.id.button_save).setOnClickListener(this);
        findViewById(R.id.button_cancel).setOnClickListener(this);
    }

    public void addContribution() {
        mDate = editTextDate.getText().toString().trim();
        amount = editTextAmount.getText().toString().trim();
        amount = amount.replace(",", ".");
        // get selected radio button from radioGroup
        int selectedId = radioGroup.getCheckedRadioButtonId();
        // find the radiobutton by returned id
        radioButton = findViewById(selectedId);
        typeOfContribution = (String) radioButton.getText();

        double totalAmountUser, totalAmountDahira;

        if (!hasValidationErrors(amount, editTextAmount)) {

            if (typeOfContribution.equals("adiya")) {
                totalAmountDahira = Double.parseDouble(dahira.getTotalAdiya()) + Double.parseDouble(amount);
                dahira.setTotalAdiya(Double.toString(totalAmountDahira));
                updateDocument(this, "dahiras", dahira.getDahiraID(), "totalAdiya", dahira.getTotalAdiya());

                //Update totalAdiya user
                totalAmountUser = Double.parseDouble(selectedUser.getListAdiya().get(indexSelectedUser)) + Double.parseDouble(amount);
                selectedUser.getListAdiya().set(indexSelectedUser, Double.toString(totalAmountUser));
                updateDocument(this, "users", selectedUser.getUserID(), "listAdiya", selectedUser.getListAdiya());
            } else if (typeOfContribution.equals("sass")) {

                totalAmountDahira = Double.parseDouble(dahira.getTotalSass()) + Double.parseDouble(amount);
                dahira.setTotalSass(Double.toString(totalAmountDahira));
                updateDocument(this, "dahiras", dahira.getDahiraID(), "totalSass", dahira.getTotalSass());

                //Update totalSass user
                totalAmountUser = Double.parseDouble(selectedUser.getListSass().get(indexSelectedUser)) + Double.parseDouble(amount);
                selectedUser.getListSass().set(indexSelectedUser, Double.toString(totalAmountUser));
                updateDocument(this, "users", selectedUser.getUserID(), "listSass", selectedUser.getListSass());
            } else if (typeOfContribution.equals("social")) {
                totalAmountDahira = Double.parseDouble(dahira.getTotalSocial()) + Double.parseDouble(amount);
                dahira.setTotalSocial(Double.toString(totalAmountDahira));
                updateDocument(this, "dahiras", dahira.getDahiraID(), "totalSocial", dahira.getTotalSocial());

                //Update totalSocial user
                totalAmountUser = Double.parseDouble(selectedUser.getListSocial().get(indexSelectedUser)) + Double.parseDouble(amount);
                selectedUser.getListSocial().set(indexSelectedUser, Double.toString(totalAmountUser));
                updateDocument(this, "users", selectedUser.getUserID(), "listSocial", selectedUser.getListSocial());
            }

            saveContribution(AddContributionActivity.this, amount, mDate);

        }
    }

    private boolean hasValidationErrors(String value, EditText editTextValue) {

        if (value.isEmpty() || !isDouble(value)) {
            editTextValue.setError("Valeur incorrect!");
            editTextValue.requestFocus();
            return true;
        }

        return false;
    }

    public void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}

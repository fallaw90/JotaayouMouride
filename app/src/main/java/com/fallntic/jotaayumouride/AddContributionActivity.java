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

import com.fallntic.jotaayumouride.Model.Adiya;
import com.fallntic.jotaayumouride.Model.ObjNotification;
import com.fallntic.jotaayumouride.Model.Sass;
import com.fallntic.jotaayumouride.Model.Social;
import com.fallntic.jotaayumouride.Utility.DataHolder;
import com.fallntic.jotaayumouride.Utility.MyStaticVariables;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.Utility.DataHolder.adiya;
import static com.fallntic.jotaayumouride.Utility.DataHolder.boolAddToDahira;
import static com.fallntic.jotaayumouride.Utility.DataHolder.createNewCollection;
import static com.fallntic.jotaayumouride.Utility.DataHolder.dahira;
import static com.fallntic.jotaayumouride.Utility.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.Utility.DataHolder.getCurrentDate;
import static com.fallntic.jotaayumouride.Utility.DataHolder.getDate;
import static com.fallntic.jotaayumouride.Utility.DataHolder.indexSelectedUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.isDouble;
import static com.fallntic.jotaayumouride.Utility.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.sass;
import static com.fallntic.jotaayumouride.Utility.DataHolder.selectedUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.Utility.DataHolder.showProgressDialog;
import static com.fallntic.jotaayumouride.Utility.DataHolder.social;
import static com.fallntic.jotaayumouride.Utility.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.Utility.DataHolder.updateDocument;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.objNotification;
import static com.fallntic.jotaayumouride.Utility.NotificationHelper.sendNotification;

public class AddContributionActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "AddContributionActivity";

    private TextView textViewTitle;
    private EditText editTextDate;
    private EditText editTextAmount;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private String mDate = getCurrentDate();
    private String amount;
    private String typeOfContribution;
    private String token;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contribution);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setLogo(R.mipmap.logo);
        setSupportActionBar(toolbar);

        checkInternetConnection(this);

        initViews();
    }

    private void initViews(){
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

    public void addContribution(){
        mDate = editTextDate.getText().toString().trim();
        amount = editTextAmount.getText().toString().trim();
        amount = amount.replace(",", ".");
        // get selected radio button from radioGroup
        int selectedId = radioGroup.getCheckedRadioButtonId();
        // find the radiobutton by returned id
        radioButton = findViewById(selectedId);
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

    public static void saveContribution(final Context context, final String nameCollection,
                                 final String documentID, final String value, final String mDate) {

        showProgressDialog(context, "Enregistrement " + nameCollection + " en cours ...");
        FirebaseFirestore.getInstance().collection(nameCollection).document(documentID).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        dismissProgressDialog();
                        if (documentSnapshot.exists()) {
                            if (nameCollection.equals("adiya")) {
                                adiya.getListDahiraID().add(dahira.getDahiraID());
                                adiya.getListUserName().add(onlineUser.getUserName());
                                adiya.getListAdiya().add(value);
                                adiya.getListDate().add(mDate);
                                updateContribution(context, "adiya", documentID, value);
                            } else if (nameCollection.equals("sass")) {
                                sass.getListDahiraID().add(dahira.getDahiraID());
                                sass.getListSass().add(value);
                                sass.getListDate().add(mDate);
                                sass.getListUserName().add(onlineUser.getUserName());
                                updateContribution(context, "sass", documentID, value);
                            } else if (nameCollection.equals("social")) {
                                social.getListSocial().add(value);
                                social.getListDahiraID().add(dahira.getDahiraID());
                                social.getListDate().add(mDate);
                                social.getListUserName().add(onlineUser.getUserName());
                                updateContribution(context, "social", documentID, value);
                            }

                            final Intent intent = new Intent(context, UserInfoActivity.class);
                            showAlertDialog(context, " enregistrement reussi", intent);
                            Log.d(TAG, "Collection " + nameCollection + " updated");
                        } else {
                            //Create new collections listAdiya, sass and social
                            List<String> listDahiraID = new ArrayList<String>();
                            listDahiraID.add(dahira.getDahiraID());
                            List<String> listDate = new ArrayList<String>();
                            listDate.add(mDate);
                            List<String> listUserName = new ArrayList<String>();
                            listUserName.add(onlineUser.getUserName());

                            if (nameCollection.equals("adiya")) {
                                List<String> listAdiya = new ArrayList<String>();
                                listAdiya.add(value);
                                Adiya adiya = new Adiya(listDahiraID, listDate, listAdiya, listUserName);
                                createNewCollection(context, "adiya", documentID, adiya);
                            }
                            if (nameCollection.equals("sass")) {
                                List<String> listSass = new ArrayList<String>();
                                listSass.add(value);
                                Sass sass = new Sass(listDahiraID, listDate, listSass, listUserName);
                                createNewCollection(context, "sass", documentID, sass);
                            }
                            if (nameCollection.equals("social")) {
                                List<String> listSocial = new ArrayList<String>();
                                listSocial.add(value);
                                Social social = new Social(listDahiraID, listDate, listSocial, listUserName);
                                createNewCollection(context, "social", documentID, social);
                            }

                            Log.d(TAG, "New collection " + nameCollection + " created");
                        }

                        notifyUser(context, value+"");
                        final Intent intent = new Intent(context, UserInfoActivity.class);
                        showAlertDialog(context, nameCollection + " ajoute avec succe!", intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissProgressDialog();
                        Log.d(TAG, e.toString());
                    }
                });
    }

    public static void updateContribution(final Context context, final String collectionName,
                                          String documentID, String str_value) {

        showProgressDialog(context, "Enregistrement " + collectionName + " en cours ...");

        //Update totalAdiya dahira
        final double value = Double.parseDouble(str_value);

        if (collectionName.equals("adiya")) {
            FirebaseFirestore.getInstance().collection(collectionName).document(documentID)
                    .update("listDahiraID", adiya.getListDahiraID(),
                            "listDate", adiya.getListDate(),
                            "listAdiya", adiya.getListAdiya(),
                            "listUserName", adiya.getListUserName())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dismissProgressDialog();

                            double totalAdiyaDahira;
                            if (boolAddToDahira)
                                totalAdiyaDahira = Double.parseDouble(dahira.getTotalAdiya()) + value;
                            else
                                totalAdiyaDahira = Double.parseDouble(dahira.getTotalAdiya()) - value;

                            dahira.setTotalAdiya(Double.toString(totalAdiyaDahira));

                            updateDocument(context, "dahiras", dahira.getDahiraID(),
                                    "totalAdiya", dahira.getTotalAdiya());

                            boolAddToDahira = false;

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dismissProgressDialog();
                        }
                    });
        } else if (collectionName.equals("sass")) {
            FirebaseFirestore.getInstance().collection(collectionName).document(documentID)
                    .update("listDahiraID", sass.getListDahiraID(),
                            "listDate", sass.getListDate(),
                            "listSass", sass.getListSass(),
                            "listUserName", sass.getListUserName())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dismissProgressDialog();

                            double totalAdiyaDahira;
                            if (boolAddToDahira)
                                totalAdiyaDahira = Double.parseDouble(dahira.getTotalAdiya()) + value;
                            else
                                totalAdiyaDahira = Double.parseDouble(dahira.getTotalAdiya()) - value;

                            dahira.setTotalAdiya(Double.toString(totalAdiyaDahira));

                            updateDocument(context, "dahiras", dahira.getDahiraID(),
                                    "totalSass", dahira.getTotalSass());

                            boolAddToDahira = false;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dismissProgressDialog();
                        }
                    });

        } else if (collectionName.equals("social")) {
            FirebaseFirestore.getInstance().collection(collectionName).document(documentID)
                    .update("listDahiraID", social.getListDahiraID(),
                            "listDate", social.getListDate(),
                            "listSocial", social.getListSocial(),
                            "listUserName", social.getListUserName())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dismissProgressDialog();

                            double totalAdiyaDahira;
                            if (boolAddToDahira)
                                totalAdiyaDahira = Double.parseDouble(dahira.getTotalAdiya()) + value;
                            else
                                totalAdiyaDahira = Double.parseDouble(dahira.getTotalAdiya()) - value;

                            dahira.setTotalAdiya(Double.toString(totalAdiyaDahira));

                            updateDocument(context, "dahiras", dahira.getDahiraID(),
                                    "totalSocial", dahira.getTotalSocial());

                            boolAddToDahira = false;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dismissProgressDialog();
                        }
                    });
        }
    }

    private static void notifyUser(Context context, String value){
        String notificationID = System.currentTimeMillis()+"";
        objNotification = new ObjNotification(notificationID,
                selectedUser.getUserID(), dahira.getDahiraID(),
                MyStaticVariables.TITLE_CONTRIBUTION_NOTIFICATION,
                "Une somme de " + value + " a ete ajoute dans votre " +
                        "compte " + DataHolder.typeOfContribution + " par " +
                        onlineUser.getUserName());

        sendNotification(context, selectedUser, objNotification);
    }
}

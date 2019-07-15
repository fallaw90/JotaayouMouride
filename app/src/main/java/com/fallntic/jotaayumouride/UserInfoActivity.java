package com.fallntic.jotaayumouride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.fallntic.jotaayumouride.DataHolder.createNewCollection;
import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.getCurrentDate;
import static com.fallntic.jotaayumouride.DataHolder.getPickDate;
import static com.fallntic.jotaayumouride.DataHolder.isDouble;
import static com.fallntic.jotaayumouride.DataHolder.logout;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.saveContribution;
import static com.fallntic.jotaayumouride.DataHolder.selectedUser;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.showProfileImage;
import static com.fallntic.jotaayumouride.DataHolder.showProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.typeOfContribution;
import static com.fallntic.jotaayumouride.DataHolder.updateDocument;

public class UserInfoActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "UserInfoActivity";

    private TextView textViewName;
    private TextView textViewDahiraName;
    private TextView textViewAdress;
    private TextView textViewPhoneNumber;
    private TextView textViewEmail;
    private TextView textViewRole;
    private TextView textViewCommission;
    private TextView textViewAdiya;
    private TextView textViewSass;
    private TextView textViewSocial;
    private ImageView imageView;

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    boolean boolAdiya = false, boolSass = false, boolSocial = false;
    boolean dahiraUpdated = true, contributionSaved = true;

    private int indexOnlineUser = onlineUser.getListDahiraID().indexOf(dahira.getDahiraID());
    private int indexSelectedUser = selectedUser.getListDahiraID().indexOf(dahira.getDahiraID());

    private String mDate = getCurrentDate();
    private Map<String, Object> contribution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Info du membre");
        setSupportActionBar(toolbar);

        if (!DataHolder.isConnected(this)){
            showAlertDialog(this,"Oops! Vous n'avez pas de connexion internet!");
            finish();
        }

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        textViewName = (TextView) findViewById(R.id.textView_userName);
        textViewDahiraName = (TextView) findViewById(R.id.textView_dahiraName);
        textViewPhoneNumber = (TextView) findViewById(R.id.textView_phoneNumber);
        textViewAdress = (TextView) findViewById(R.id.textView_address);
        textViewEmail = (TextView) findViewById(R.id.textView_email);
        textViewCommission = (TextView) findViewById(R.id.textView_commission);
        textViewRole = (TextView) findViewById(R.id.textView_role);
        textViewAdiya = (TextView) findViewById(R.id.totalAdiya);
        textViewSass = (TextView) findViewById(R.id.totalSass);
        textViewSocial = (TextView) findViewById(R.id.totalSocial);
        imageView = (ImageView) findViewById(R.id.imageView);

        showProfileImage(this, selectedUser.getUserID(), imageView);

        textViewName.setText(selectedUser.getUserName());
        textViewDahiraName.setText(dahira.getDahiraName());
        textViewPhoneNumber.setText(selectedUser.getUserPhoneNumber());
        textViewAdress.setText(selectedUser.getAddress());
        textViewEmail.setText(selectedUser.getEmail());
        textViewCommission.setText(selectedUser.getListCommissions().get(indexSelectedUser));
        textViewAdiya.setText(selectedUser.getListAdiya().get(indexSelectedUser));
        textViewSass.setText(selectedUser.getListSass().get(indexSelectedUser));
        textViewSocial.setText(selectedUser.getListSocial().get(indexSelectedUser));
        textViewRole.setText(selectedUser.getListRoles().get(indexSelectedUser));
        if (!selectedUser.getListRoles().get(indexSelectedUser).equals("Administrateur")) {
            textViewRole.setVisibility(View.GONE);
        }

        findViewById(R.id.button_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button_back:
                startActivity(new Intent(UserInfoActivity.this, ListUserActivity.class));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_user_info, menu);

        MenuItem menuAddContributions, menuSetting, menuDetailAdiya, menuDetailSass, menuDetailSocial;
        menuAddContributions = menu.findItem(R.id.addContributions);
        menuSetting = menu.findItem(R.id.setting);

        menuDetailAdiya = menu.findItem(R.id.menu_adiya);
        menuDetailSass = menu.findItem(R.id.menu_sass);
        menuDetailSocial = menu.findItem(R.id.menu_social);
        menuAddContributions.setVisible(false);
        menuSetting.setVisible(false);
        menuDetailAdiya.setVisible(false);
        menuDetailSass.setVisible(false);
        menuDetailSocial.setVisible(false);

        double amountAdiya = Double.parseDouble(selectedUser.getListAdiya().get(indexSelectedUser));
        double amountSass = Double.parseDouble(selectedUser.getListSass().get(indexSelectedUser));
        double amountSocial = Double.parseDouble(selectedUser.getListSocial().get(indexSelectedUser));

        if (onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur")){
            menuAddContributions.setVisible(true);
            menuSetting.setVisible(true);

            if (amountAdiya > 0)
                menuDetailAdiya.setVisible(true);
            if (amountSass > 0)
                menuDetailSass.setVisible(true);
            if (amountSocial > 0)
                menuDetailSocial.setVisible(true);
        }
        else if (onlineUser.getUserID().equals(selectedUser.getUserID())){
            menuSetting.setVisible(true);

            if (amountAdiya > 0)
                menuDetailAdiya.setVisible(true);
            if (amountSass > 0)
                menuDetailSass.setVisible(true);
            if (amountSocial > 0)
                menuDetailSocial.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.addContributions:
                chooseContribution();
                break;

            case R.id.menu_adiya:
                typeOfContribution = "adiya";
                startActivity(new Intent(this, ListContributionActivity.class));
                break;

            case R.id.menu_sass:
                typeOfContribution = "sass";
                startActivity(new Intent(this, ListContributionActivity.class));
                break;

            case R.id.menu_social:
                typeOfContribution = "social";
                startActivity(new Intent(this, ListContributionActivity.class));
                break;

            case R.id.setting:
                startActivity(new Intent(this, SettingUserActivity.class));
                break;

            case R.id.logout:
                logout();
                finish();
                startActivity(new Intent(this, MainActivity.class));
                break;
        }
        return true;
    }

    private void chooseContribution() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_choose_contribution, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        final Button buttonAddAdiya = (Button) dialogView.findViewById(R.id.button_dialogAddAdiya);
        final Button buttonAddSass = (Button) dialogView.findViewById(R.id.button_dialogAddSass);
        final Button buttonAddSocial = (Button) dialogView.findViewById(R.id.button_dialogAddSocial);
        final Button buttonCancel = (Button) dialogView.findViewById(R.id.button_dialogCancel);

        dialogBuilder.setTitle("Ajouter une contribution");
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();



        buttonAddAdiya.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolAdiya = true;
                addContribution();
                alertDialog.dismiss();
            }
        });

        buttonAddSass.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolSass = true;
                addContribution();
                alertDialog.dismiss();
            }
        });

        buttonAddSocial.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolSocial = true;
                addContribution();
                alertDialog.dismiss();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

    }

    private void addContribution() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_contribution, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        final Button buttonDate = (Button) dialogView.findViewById(R.id.button_date);
        final EditText editTextDialogContribution = (EditText) dialogView.findViewById(R.id.editText_dialogContribution);
        final Button buttonSave = (Button) dialogView.findViewById(R.id.button_dialogSave);
        final Button buttonCancel = (Button) dialogView.findViewById(R.id.button_cancel);

        if (boolAdiya){
            dialogBuilder.setTitle(selectedUser.getUserName() + "\ndahira " + dahira.getDahiraName());
            editTextDialogContribution.setHint("Montant listAdiya (Chiffre seulement)");
        }
        if (boolSass){
            dialogBuilder.setTitle("Ajouter listAdiya pour " + selectedUser.getUserName() + " membre du dahira " + dahira.getDahiraName());
            editTextDialogContribution.setHint("Montant listAdiya (Chiffre seulement)");
        }
        if (boolSocial){
            dialogBuilder.setTitle("Ajouter listAdiya pour " + selectedUser.getUserName() + " membre du dahira " + dahira.getDahiraName());
            editTextDialogContribution.setHint("Montant listAdiya (Chiffre seulement)");
        }

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonDate.setText("Dakar, le " + mDate);
        buttonDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDate = getPickDate(UserInfoActivity.this);
            }
        });

        buttonSave.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String typeContribution = "Contribution";
                String value = editTextDialogContribution.getText().toString().trim();
                value = value.replace(",", ".");

                if(!hasValidationErrors(editTextDialogContribution, value)){
                    Intent intent = new Intent(UserInfoActivity.this, UserInfoActivity.class);
                    if (boolAdiya){
                        typeContribution = "Adiya";
                        //Save to listAdiya collection
                        saveContribution(UserInfoActivity.this, "listAdiya", selectedUser.getUserID(), value);

                        //Update totalAdiya user
                        double adiyaUserVerse = Double.parseDouble(value);
                        double totalAdiyaUserVerse = Double.parseDouble(selectedUser.getListAdiya()
                                                    .get(indexSelectedUser)) + adiyaUserVerse;
                        selectedUser.getListAdiya().set(indexSelectedUser, Double.toString(totalAdiyaUserVerse));
                        updateDocument(UserInfoActivity.this, "users", selectedUser.getUserID(),
                                "listAdiya", selectedUser.getListAdiya());

                        //Update totalAdiya dahira
                        double totalAdiyaDahira = Double.parseDouble(dahira.getTotalAdiya()) + adiyaUserVerse;
                        dahira.setTotalAdiya(Double.toString(totalAdiyaDahira));
                        updateDocument(UserInfoActivity.this, "dahiras", dahira.getDahiraID(),
                                "totalAdiya", dahira.getTotalAdiya());
                    }

                    if (boolSass){
                        typeContribution = "Sass";
                        //Save to sass collection
                        saveContribution(UserInfoActivity.this, "sass",
                                selectedUser.getUserID(), value);

                        //Update totalAdiya user
                        double sassUserVerse = Double.parseDouble(value);
                        double totalSassUserVerse = Double.parseDouble(selectedUser.getListSass()
                                                    .get(indexSelectedUser)) + sassUserVerse;
                        selectedUser.getListSass().set(indexSelectedUser, Double.toString(totalSassUserVerse));
                        updateDocument(UserInfoActivity.this, "users", selectedUser.getUserID(),
                                "listSass", selectedUser.getListSass());

                        //Update totalAdiya dahira
                        double totalSassDahira = Double.parseDouble(dahira.getTotalSass()) + sassUserVerse;
                        dahira.setTotalSass(Double.toString(totalSassDahira));
                        updateDocument(UserInfoActivity.this, "dahiras", dahira.getDahiraID(),
                                "totalSass", dahira.getTotalSass());
                    }

                    if (boolSocial){
                        typeContribution = "Social";
                        //Save to sass collection
                        saveContribution(UserInfoActivity.this, "social",
                                selectedUser.getUserID(), value);

                        //Update totalAdiya user
                        double socialUserVerse = Double.parseDouble(value);
                        double totalSocialUserVerse = Double.parseDouble(selectedUser.getListSocial()
                                .get(indexSelectedUser)) + socialUserVerse;
                        selectedUser.getListSocial().set(indexSelectedUser, Double.toString(totalSocialUserVerse));
                        updateDocument(UserInfoActivity.this, "users", selectedUser.getUserID(),
                                "listSocial", selectedUser.getListSocial());

                        //Update totalAdiya dahira
                        double totalSocialDahira = Double.parseDouble(dahira.getTotalSocial()) + socialUserVerse;
                        dahira.setTotalSass(Double.toString(totalSocialDahira));
                        updateDocument(UserInfoActivity.this, "dahiras", dahira.getDahiraID(),
                                "totalSocial", dahira.getTotalSocial());
                    }

                    //showAlertDialog(UserInfoActivity.this, typeContribution + " ajoute avec succe.");
                    alertDialog.dismiss();
                }
            }
        });


        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }
    private boolean hasValidationErrors(EditText editTextValue, String value) {

        if (value.isEmpty() || !isDouble(value)) {
            editTextValue.setError("Valeur incorrect!");
            editTextValue.requestFocus();
            return true;
        }

        return false;
    }
}

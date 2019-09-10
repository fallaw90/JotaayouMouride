package com.fallntic.jotaayumouride;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fallntic.jotaayumouride.Adapter.CommissionListAdapter;
import com.fallntic.jotaayumouride.Model.Dahira;
import com.fallntic.jotaayumouride.Utility.DataHolder;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.hbb20.CountryCodePicker;
import com.mikelau.countrypickerx.Country;
import com.mikelau.countrypickerx.CountryPickerCallbacks;
import com.mikelau.countrypickerx.CountryPickerDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.Utility.DataHolder.dahira;
import static com.fallntic.jotaayumouride.Utility.DataHolder.dahiraID;
import static com.fallntic.jotaayumouride.Utility.DataHolder.isDouble;
import static com.fallntic.jotaayumouride.Utility.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.Utility.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.saveLogoDahira;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.myListDahira;

public class CreateDahiraActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CreateDahiraActivity";
    private static final String DEFAULT_LOCAL = "Senegal";

    private EditText editTextDahiraName;
    private EditText editTextDieuwrine;
    private EditText editTextDahiraPhoneNumber;
    private EditText editTextSiege;
    private EditText editTextCommission;
    private EditText editTextResponsible;
    private EditText editTextAdiya;
    private EditText editTextSass;
    private EditText editTextSocial;
    private EditText editTextCountry;
    private EditText editTextCity;
    private TextView textViewLabelCommission;
    private TextView getTextViewLabelResponsible;
    private TextView textViewUpdateCommission, textViewAreaCode;
    private ImageView imageView;


    private String commission, city, country;
    private String dahiraName;
    private String dieuwrine;
    private String dahiraPhoneNumber;
    private String siege;
    private String totalAdiya;
    private String totalSass;
    private String totalSocial;

    private ListView listViewCommission;

    private ArrayList<String> arrayList;
    private ArrayAdapter<String> arrayAdapter;

    private List<String> listCommissionDahira = new ArrayList<>();
    private List<String> listResponsibles = new ArrayList<>();

    private RelativeLayout relativeLayoutData, relativeLayoutProgressBar;
    private ProgressBar progressBar;

    private boolean imageSaved = true, dahiraSaved = true, dahiraUpdated = true;

    //Firebase
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Toolbar toolbar;

    private CountryPickerDialog countryPicker;

    private UploadTask uploadTask;
    private Uri fileUri;

    private CountryCodePicker ccp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_dahira);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setLogo(R.mipmap.logo);
        setSupportActionBar(toolbar);

        initViews();

        //Check internet connection
        checkInternetConnection(this);

        //Display and modify ListView commissions
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.list_commission, R.id.textView_commission, arrayList);
        listViewCommission.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long l) {
                String com = listCommissionDahira.get(index);
                String resp = listResponsibles.get(index);
                showUpdateDeleteDialog(com, resp, index);
                return true;
            }
        });

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        hideSoftKeyboard();
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
                startActivity(new Intent(this, HomeActivity.class));
                break;

            case R.id.instructions:
                startActivity(new Intent(this, InstructionsActivity.class));
                break;
        }
        return true;
    }

    private void initViews() {
        //Dahira info
        editTextDahiraName = findViewById(R.id.editText_dahiraName);
        editTextDieuwrine = findViewById(R.id.editText_dieuwrine);
        editTextDahiraPhoneNumber = findViewById(R.id.editText_dahiraPhoneNumber);
        editTextSiege = findViewById(R.id.editText_siege);
        editTextCommission = findViewById(R.id.editText_commission);
        editTextResponsible = findViewById(R.id.editText_responsible);
        textViewLabelCommission = findViewById(R.id.textView_labelCommission);
        getTextViewLabelResponsible = findViewById(R.id.textView_labelResponsible);
        listViewCommission = findViewById(R.id.listView_commission);
        editTextResponsible = findViewById(R.id.editText_responsible);
        editTextAdiya = findViewById(R.id.editText_adiya);
        editTextSass = findViewById(R.id.editText_sass);
        editTextSocial = findViewById(R.id.editText_social);
        textViewUpdateCommission = findViewById(R.id.textViewUpdateCommission);
        editTextCountry = findViewById(R.id.editText_country);
        editTextCity = findViewById(R.id.editText_city);
        imageView = findViewById(R.id.imageView);
        ccp = findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(editTextDahiraPhoneNumber);

        findViewById(R.id.button_addCommission).setOnClickListener(this);
        findViewById(R.id.button_save).setOnClickListener(this);
        findViewById(R.id.button_back).setOnClickListener(this);
        findViewById(R.id.imageView).setOnClickListener(this);
        findViewById(R.id.editText_country).setOnClickListener(this);
    }

    public void initViewsProgressBar() {
        relativeLayoutData = findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.imageView:
                checkPermission();
                chooseImage();
                break;
            case R.id.editText_country:
                getCountry();
                break;
            case R.id.button_addCommission:
                showListViewCommissions();
                break;
            case R.id.button_save:
                saveDahira();
                break;
            case R.id.button_back:
                finish();
                break;
            case R.id.textView_login:
                startActivity(new Intent(CreateDahiraActivity.this, LoginActivity.class));
                break;
        }
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data.getData() != null) {

            fileUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {
        final StorageReference fileToUpload = storageReference
                .child("logoDahira").child(dahira.getDahiraID());
        uploadTask = (UploadTask) fileToUpload.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileToUpload.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                dahira.setImageUri(uri.toString());
                                saveLogoDahira(CreateDahiraActivity.this, uri.toString());
                            }
                        });
                    }
                });
    }

    private void saveDahira() {
        //Info dahira
        dahiraName = editTextDahiraName.getText().toString().trim();
        dieuwrine = editTextDieuwrine.getText().toString().trim();
        dahiraPhoneNumber = editTextDahiraPhoneNumber.getText().toString().trim();
        totalAdiya = editTextAdiya.getText().toString().trim();
        totalSass = editTextSass.getText().toString().trim();
        totalSocial = editTextSocial.getText().toString().trim();
        country = editTextCountry.getText().toString().trim();
        city = editTextCity.getText().toString().trim();
        siege = editTextSiege.getText().toString().trim();

        if (totalAdiya == null || totalAdiya.equals("") || totalAdiya.isEmpty())
            totalAdiya = "00";
        else
            totalAdiya = totalAdiya.replace(",", ".");

        if (totalSass == null || totalSass.equals("") || totalSass.isEmpty())
            totalSass = "00";
        else
            totalSass = totalSass.replace(",", ".");

        if (totalSocial == null || totalSocial.equals("") || totalSocial.isEmpty())
            totalSocial = "00";
        else
            totalSocial = totalSocial.replace(",", ".");


        if (!hasValidationErrors()) {
            siege = siege.concat(", " + city + " " + country);
            dahiraPhoneNumber = ccp.getFullNumberWithPlus();
            dahiraID = db.collection("dahiras").document().getId();
            dahira = new Dahira(dahiraID, dahiraName, dieuwrine, dahiraPhoneNumber, siege, totalAdiya,
                    totalSass, totalSocial, "1", "", listCommissionDahira, listResponsibles);

            if (myListDahira == null)
                myListDahira = new ArrayList<>();

            showProgressBar();
            db.collection("dahiras").document(DataHolder.dahiraID).set(dahira)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            hideProgressBar();
                            if (fileUri != null)
                                uploadImage();
                            updateUserListDahiraID();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressBar();
                            dahiraSaved = false;
                            toastMessage(getApplicationContext(), "Error adding dahira!");
                            Log.d(TAG, e.toString());
                        }
                    });
        }
    }

    private void updateUserListDahiraID() {
        onlineUser.getListDahiraID().add(dahiraID);
        showProgressBar();
        db.collection("users").document(DataHolder.onlineUser.getUserID())
                .update("listDahiraID", onlineUser.getListDahiraID())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideProgressBar();
                        final Intent intent = new Intent(CreateDahiraActivity.this, UpdateAdminActivity.class);
                        showAlertDialog(CreateDahiraActivity.this, "Felicitation! Pour terminer la creation " +
                                "de votre dahira, Enregistrez-vous en tant que membre du dahira " + dahira.getDahiraName() +
                                " dans la page suivante.", intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideProgressBar();
                        dahiraUpdated = false;
                    }
                });
    }

    public void showListViewCommissions() {
        String commission = editTextCommission.getText().toString().trim();
        String responsible = editTextResponsible.getText().toString().trim();

        if (commission.isEmpty()) {
            editTextCommission.setError("Veuillez remplir ce champ");
            editTextCommission.requestFocus();
            return;
        }

        if (responsible.isEmpty()) {
            editTextResponsible.setError("Veuillez remplir ce champ");
            editTextResponsible.requestFocus();
            return;
        }

        //Show label commission and label responsible
        textViewLabelCommission.setVisibility(View.VISIBLE);
        getTextViewLabelResponsible.setVisibility(View.VISIBLE);
        textViewUpdateCommission.setVisibility(View.VISIBLE);

        listCommissionDahira.add(commission);
        listResponsibles.add(responsible);

        CommissionListAdapter customAdapter = new CommissionListAdapter(getApplicationContext(),
                listCommissionDahira, listResponsibles);
        listViewCommission.setAdapter(customAdapter);

        setListViewHeightBasedOnChildren(listViewCommission);

        editTextCommission.setText("");
        editTextResponsible.setText("");
        editTextCommission.requestFocus();
    }

    private void setListViewHeightBasedOnChildren(ListView listView) {

        Log.e("Listview Size ", "" + listView.getCount());

        CommissionListAdapter listAdapter = (CommissionListAdapter) listView.getAdapter();

        if (listAdapter == null) {

            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();

    }

    private void showUpdateDeleteDialog(final String commission, String responsible, final int index) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_update_commission, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextCommissione = dialogView.findViewById(R.id.editText_dialogCommission);
        final EditText editTextResponsible = dialogView.findViewById(R.id.editText_dialogResponsible);

        Button buttonUpdate = dialogView.findViewById(R.id.button_dialogUpdate);
        Button buttonDelete = dialogView.findViewById(R.id.button_dialogDelete);

        editTextCommissione.setText(commission);
        editTextResponsible.setText(responsible);

        dialogBuilder.setTitle("Modifier ou supprimer cette commission");
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String c = editTextCommissione.getText().toString().trim();
                String r = editTextResponsible.getText().toString().trim();
                listCommissionDahira.set(index, c);
                listResponsibles.set(index, r);

                CommissionListAdapter customAdapter = new CommissionListAdapter(getApplicationContext(),
                        listCommissionDahira, listResponsibles);
                listViewCommission.setAdapter(customAdapter);
                alertDialog.dismiss();
            }
        });


        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                listCommissionDahira.remove(index);
                listResponsibles.remove(index);

                CommissionListAdapter customAdapter = new CommissionListAdapter(getApplicationContext(),
                        listCommissionDahira, listResponsibles);
                listViewCommission.setAdapter(customAdapter);
                alertDialog.dismiss();
                setListViewHeightBasedOnChildren(listViewCommission);
            }
        });
    }

    public void getCountry() {
        /* Name of your Custom JSON list */
        int resourceId = getResources().getIdentifier("country_avail", "raw", getApplicationContext().getPackageName());

        countryPicker = new CountryPickerDialog(CreateDahiraActivity.this, new CountryPickerCallbacks() {
            @Override
            public void onCountrySelected(Country country, int flagResId) {
                /* Get Country Name: country.getCountryName(context); */
                editTextCountry.setText(country.getCountryName(CreateDahiraActivity.this));
                /* Call countryPicker.dismiss(); to prevent memory leaks */
                countryPicker.dismiss();
            }

        /* Set to false if you want to disable Dial Code in the results and true if you want to show it
        Set to zero if you don't have a custom JSON list of countries in your raw file otherwise use
        resourceId for your customly available countries */
        }, false, 0);
        countryPicker.show();
    }

    public static String getCurrentCountryCode(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryIso = telephonyManager.getSimCountryIso().toUpperCase();
        return "+" + PhoneNumberUtil.getInstance().getCountryCodeForRegion(countryIso);
    }

    public boolean hasValidationErrors() {

        if (dahiraName.isEmpty()) {
            editTextDahiraName.setError("Nom dahira obligatoire");
            editTextDahiraName.requestFocus();
            return true;
        }

        if (dieuwrine.isEmpty()) {
            editTextDieuwrine.setError("Champ obligatoire");
            editTextDieuwrine.requestFocus();
            return true;
        }

        if (dahiraPhoneNumber.isEmpty()) {
            editTextDahiraPhoneNumber.setError("Champ obligatoire");
            editTextDahiraPhoneNumber.requestFocus();
            return true;
        }

        if (!dahiraPhoneNumber.isEmpty() && (!dahiraPhoneNumber.matches("[0-9]+"))) {
            if (dahiraPhoneNumber.contains("+")) {
                editTextDahiraPhoneNumber.setError("Ne pas inclure votre indicatif svp.");
                editTextDahiraPhoneNumber.requestFocus();
                return true;
            }
        }

        if (siege.isEmpty()) {
            editTextSiege.setError("Champ obligatoire");
            editTextSiege.requestFocus();
            return true;
        }

        if (city.isEmpty()) {
            editTextCity.setError("Champ obligatoire");
            editTextCity.requestFocus();
            return true;
        }

        if (totalAdiya.isEmpty()) {
            editTextSiege.setError("Non montant, entrer 0");
            editTextAdiya.requestFocus();
            return true;
        } else if (!isDouble(totalAdiya)) {
            editTextAdiya.setText("Valeur listAdiya incorrecte");
            editTextAdiya.requestFocus();
            return true;
        }

        if (totalSass.isEmpty()) {
            editTextSiege.setError("Non montant, entrer 0");
            editTextSass.requestFocus();
            return true;
        } else if (!isDouble(totalSass)) {
            editTextSass.setText("Valeur sass incorrecte");
            editTextSass.requestFocus();
            return true;
        }

        if (totalSocial.isEmpty()) {
            editTextSiege.setError("Non montant, entrer 0");
            editTextSass.requestFocus();
            return true;
        } else if (!isDouble(totalSocial)) {
            editTextSocial.setText("Valeur sociale incorrecte");
            editTextSocial.requestFocus();
            return true;
        }

        return false;
    }

    public void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}

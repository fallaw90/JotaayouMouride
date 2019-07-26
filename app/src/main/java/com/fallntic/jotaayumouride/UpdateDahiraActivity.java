package com.fallntic.jotaayumouride;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dahiraID;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.hasValidationErrors;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.showImage;
import static com.fallntic.jotaayumouride.DataHolder.showProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;

public class UpdateDahiraActivity extends AppCompatActivity implements View.OnClickListener  {

    private static final String TAG = "CreateDahiraActivity";

    private EditText editTextDahiraName;
    private EditText editTextDieuwrine;
    private EditText editTextDahiraPhoneNumber;
    private EditText editTextSiege;
    private EditText editTextCommission;
    private EditText editTextResponsible;
    private EditText editTextAdiya;
    private EditText editTextSass;
    private EditText editTextSocial;
    private TextView textViewLabelCommission;
    private TextView getTextViewLabelResponsible;
    private TextView textViewUpdateCommission;
    private ImageView imageView;
    private Spinner spinnerCountry, spinnerRegion;


    private String commission, region, country;
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

    private Uri uri;
    private final int PICK_IMAGE_REQUEST = 71;

    private boolean imageSaved = true, dahiraSaved = true;

    //Firebase
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_dahira);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Modifier votre dahira");
        setSupportActionBar(toolbar);

        if (!isConnected(this)){
            finish();
            Intent intent = new Intent(this, LoginActivity.class);
            showAlertDialog(this,"Oops! Pas de connexion, verifier votre connexion internet puis reesayez SVP", intent);
        }

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

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
        spinnerCountry = findViewById(R.id.spinner_country);
        spinnerRegion = findViewById(R.id.spinner_region);
        imageView = findViewById(R.id.imageView);

        editTextDahiraName.setText(dahira.getDahiraName());
        editTextDieuwrine.setText(dahira.getDieuwrine());
        String[] arraySiege = dahira.getSiege().split(",");
        String phoneNumber = dahira.getDahiraPhoneNumber().substring(4);
        editTextDahiraPhoneNumber.setText(phoneNumber);
        editTextSiege.setText(arraySiege[0]);
        editTextAdiya.setText(dahira.getTotalAdiya());
        editTextSass.setText(dahira.getTotalSass());
        editTextSocial.setText(dahira.getTotalSocial());

        showImage(this, "logoDahira", dahira.getDahiraID(), imageView);
        loadListCommissions();

        //Display and modify ListView commissions
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.list_commission,
                R.id.textView_commission, arrayList);

        listViewCommission.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long l) {
                String com = dahira.getListCommissions().get(index);
                String resp = dahira.getListResponsibles().get(index);
                showUpdateDeleteDialog(com, resp, index);
                return true;
            }
        });

        spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                country = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView <?> parent) {
            }
        });

        spinnerRegion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                region = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView <?> parent) {
            }
        });

        hideSoftKeyboard();

        findViewById(R.id.button_addCommission).setOnClickListener(this);
        findViewById(R.id.button_save).setOnClickListener(this);
        findViewById(R.id.button_back).setOnClickListener(this);
        findViewById(R.id.imageView).setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_menu, menu);

        MenuItem iconBack;
        iconBack = menu.findItem(R.id.icon_back);

        iconBack.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.icon_back:
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){
            case R.id.imageView:
                checkPermission();
                chooseImage();
                break;
            case R.id.button_addCommission:
                showListViewCommissions();
                break;
            case R.id.button_save:
                updateData();
                uploadImage();
                break;
            case R.id.button_back:
                finish();
                startActivity(new Intent(UpdateDahiraActivity.this, DahiraInfoActivity.class));
                break;
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Choisir une image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null ) {
            try {
                uri = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {
        if(uri != null) {
            showProgressDialog(this, "Enregistrement de votre image cours ...");
            final StorageReference ref = storageReference.child("logoDahira").child(dahira.getDahiraID());
            ref.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!urlTask.isSuccessful());
                            dismissProgressDialog();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dismissProgressDialog();
                            imageSaved = false;
                            toastMessage(getApplicationContext(), "Failed "+e.getMessage());
                        }
                    });
        }
    }

    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);

        }
    }

    private void updateData(){

        //Info dahira
        dahiraName = editTextDahiraName.getText().toString().trim();
        dieuwrine = editTextDieuwrine.getText().toString().trim();
        dahiraPhoneNumber = editTextDahiraPhoneNumber.getText().toString().trim();
        siege = editTextSiege.getText().toString().trim();
        siege = siege.concat(", " + region + " " + country);
        totalAdiya = editTextAdiya.getText().toString().trim();
        totalSass = editTextSass.getText().toString().trim();
        totalSocial = editTextSocial.getText().toString().trim();

        totalAdiya = totalAdiya.replace(",", ".");
        totalSass = totalSass.replace(",", ".");
        totalSocial = totalSocial.replace(",", ".");

        if(!hasValidationErrors(dahiraName, editTextDahiraName, dieuwrine, editTextDieuwrine,
                dahiraPhoneNumber,  editTextDahiraPhoneNumber, siege, editTextSiege, totalAdiya, editTextAdiya,
                totalSass, editTextSass, totalSocial, editTextSocial)) {

            dahiraPhoneNumber = "+221"+dahiraPhoneNumber;

            List<String> listID = onlineUser.getListDahiraID();
            listID.add(dahiraID);
            onlineUser.setListDahiraID(listID);

            showProgressDialog(this,"Enregistrement de votre dahira cours ...");
            db.collection("dahiras").document(dahira.getDahiraID())
                    .update( "dahiraName", dahiraName,
                            "dieuwrine", dieuwrine,
                            "userPhoneNumber", dahiraPhoneNumber,
                            "siege", siege,
                            "listCommissions", dahira.getListCommissions(),
                            "listResponsibles", dahira.getListResponsibles(),
                            "totalAdiya", totalAdiya,
                            "totalSass", totalSass,
                            "totalSocial", totalSocial
                    )
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            showAlertDialog(UpdateDahiraActivity.this, "Changements enregistre avec succe");
                        }
                    });

            startActivity(new Intent(UpdateDahiraActivity.this, DahiraInfoActivity.class));
        }
    }

    public void showListViewCommissions(){
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

        dahira.getListCommissions().add(commission);
        dahira.getListResponsibles().add(responsible);

        CommissionListAdapter customAdapter = new CommissionListAdapter(getApplicationContext(),
                dahira.getListCommissions(), dahira.getListResponsibles());
        listViewCommission.setAdapter(customAdapter);

        setListViewHeightBasedOnChildren(listViewCommission);

        editTextCommission.setText("");
        editTextResponsible.setText("");
        editTextCommission.requestFocus();
    }

    public void loadListCommissions(){

        CommissionListAdapter customAdapter = new CommissionListAdapter(getApplicationContext(),
                dahira.getListCommissions(), dahira.getListResponsibles());
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

        buttonUpdate.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String c = editTextCommissione.getText().toString().trim();
                String r = editTextResponsible.getText().toString().trim();
                dahira.getListCommissions().set(index, c);
                dahira.getListResponsibles().set(index, r);

                CommissionListAdapter customAdapter = new CommissionListAdapter(getApplicationContext(),
                        dahira.getListCommissions(), dahira.getListResponsibles());
                listViewCommission.setAdapter(customAdapter);
                alertDialog.dismiss();
            }
        });


        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dahira.getListCommissions().remove(index);
                dahira.getListResponsibles().remove(index);

                CommissionListAdapter customAdapter = new CommissionListAdapter(getApplicationContext(),
                        dahira.getListCommissions(), dahira.getListResponsibles());
                listViewCommission.setAdapter(customAdapter);
                alertDialog.dismiss();
                setListViewHeightBasedOnChildren(listViewCommission);
            }
        });
    }

    public void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}

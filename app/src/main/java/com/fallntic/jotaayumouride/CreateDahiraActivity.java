package com.fallntic.jotaayumouride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.dahiraID;
import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.hasValidationErrors;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.logout;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.createNewCollection;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.showProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.DataHolder.userID;

public class CreateDahiraActivity extends AppCompatActivity implements View.OnClickListener  {

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

    private List<String> listCommissionDahira = new ArrayList<>();
    private List<String> listResponsibles = new ArrayList<>();

    private Uri uri;
    private final int PICK_IMAGE_REQUEST = 71;

    private boolean imageSaved = true, dahiraSaved = true, dahiraUpdated = true;

    //Firebase
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_dahira);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Enregistrer un nouveau dahira");
        setSupportActionBar(toolbar);

        //Check internet connection
        if (!isConnected(this)){
            Intent intent = new Intent(this, LoginActivity.class);
            logout();
            showAlertDialog(this,"Oops! Pas de connexion, verifier votre connexion internet puis reesayez SVP", intent);
        }

        //Dahira info
        editTextDahiraName = findViewById(R.id.editText_dahiraName);
        editTextDieuwrine = findViewById(R.id.editText_dieuwrine);
        editTextDahiraPhoneNumber = findViewById(R.id.editText_dahiraPhoneNumber);
        editTextSiege = findViewById(R.id.editText_siege);
        editTextCommission = findViewById(R.id.editText_commission);
        editTextResponsible = findViewById(R.id.editText_responsible);
        textViewLabelCommission = (TextView) findViewById(R.id.textView_labelCommission);
        getTextViewLabelResponsible = (TextView) findViewById(R.id.textView_labelResponsible);
        listViewCommission = (ListView) findViewById(R.id.listView_commission);
        editTextResponsible = findViewById(R.id.editText_responsible);
        editTextAdiya = findViewById(R.id.editText_adiya);
        editTextSass = findViewById(R.id.editText_sass);
        editTextSocial = findViewById(R.id.editText_social);
        textViewUpdateCommission = findViewById(R.id.textViewUpdateCommission);
        spinnerCountry = findViewById(R.id.spinner_country);
        spinnerRegion = findViewById(R.id.spinner_region);
        imageView = (ImageView) findViewById(R.id.imageView);

        editTextAdiya.setText("00,00");
        editTextSass.setText("00,00");
        editTextSocial.setText("00,00");

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

        findViewById(R.id.button_addCommission).setOnClickListener(this);
        findViewById(R.id.button_save).setOnClickListener(this);
        findViewById(R.id.button_back).setOnClickListener(this);
        findViewById(R.id.imageView).setOnClickListener(this);
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

    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
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

    private void saveDahira(){
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
                dahiraPhoneNumber, editTextDahiraPhoneNumber, siege, editTextSiege, totalAdiya, editTextAdiya,
                totalSass, editTextSass, totalSocial, editTextSocial)) {

            dahiraID = db.collection("dahiras").document().getId();
            dahira = new Dahira(dahiraID, dahiraName, dieuwrine, dahiraPhoneNumber, siege, totalAdiya,
                    totalSass, totalSocial,"1", listCommissionDahira, listResponsibles);

            showProgressDialog(this, "Enregistrement de votre dahira cours ...");
            db.collection("dahiras").document(dahiraID).set(dahira)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dismissProgressDialog();
                            saveLogoDahira();
                            updateUserListDahiraID();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dismissProgressDialog();
                            dahiraSaved = false;
                            toastMessage(getApplicationContext(), "Error adding dahira!");
                            Log.d(TAG, e.toString());
                        }
                    });
        }
    }

    private void saveLogoDahira() {
        if(uri != null) {
            DataHolder.showProgressDialog(this, "Enregistrement de votre image cours ...");
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
                            DataHolder.toastMessage(getApplicationContext(),"Failed "+e.getMessage());
                        }
                    });
        }
    }

    private void updateUserListDahiraID(){
        onlineUser.getListDahiraID().add(dahiraID);
        db.collection("users").document(DataHolder.onlineUser.getUserID())
                .update("listDahiraID", onlineUser.getListDahiraID())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        final Intent intent = new Intent(CreateDahiraActivity.this, UpdateAdminActivity.class);
                        showAlertDialog(CreateDahiraActivity.this, "Dahira cree avec succes", intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dahiraUpdated = false;
                    }
                });
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

        final EditText editTextCommissione = (EditText) dialogView.findViewById(R.id.editText_dialogCommission);
        final EditText editTextResponsible = (EditText) dialogView.findViewById(R.id.editText_dialogResponsible);

        Button buttonUpdate = (Button) dialogView.findViewById(R.id.button_dialogUpdate);
        Button buttonDelete = (Button) dialogView.findViewById(R.id.button_dialogDelete);

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
}

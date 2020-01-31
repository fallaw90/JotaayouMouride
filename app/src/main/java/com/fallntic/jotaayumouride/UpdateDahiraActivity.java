package com.fallntic.jotaayumouride;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fallntic.jotaayumouride.adapter.CommissionListAdapter;
import com.fallntic.jotaayumouride.model.Dahira;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.saveLogoDahira;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showImage;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.dahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAllDahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.myListDahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutProgressBar;

@SuppressWarnings("unused")
public class UpdateDahiraActivity extends AppCompatActivity implements View.OnClickListener {

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
    private ImageView imageViewLogo;


    private String dahiraName;
    private String dieuwrine;
    private String dahiraPhoneNumber;
    private String siege;

    private ListView listViewCommission;

    private byte[] uploadBytes;
    private double mProgress = 0;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_dahira);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Modifier votre dahira");
        setSupportActionBar(toolbar);
        //***************** Set logo **********************
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.logo);

        checkInternetConnection(this);

        initViews();

        displayViews();

        listViewCommission.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long l) {
                String com = dahira.getListCommissions().get(index);
                String resp = dahira.getListResponsibles().get(index);
                showUpdateDeleteDialog(com, resp, index);
                return true;
            }
        });

        hideSoftKeyboard();
    }

    private void initViewsProgressBar() {
        relativeLayoutData = findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initViews() {

        //ProgressBar from static variable MainActivity
        ShowUserActivity.scrollView = findViewById(R.id.scrollView);
        initViewsProgressBar();
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
        imageViewLogo = findViewById(R.id.imageView_logo);

        findViewById(R.id.button_addCommission).setOnClickListener(this);
        findViewById(R.id.button_save).setOnClickListener(this);
        findViewById(R.id.button_back).setOnClickListener(this);
        findViewById(R.id.imageView_logo).setOnClickListener(this);
    }

    private void displayViews() {

        editTextDahiraName.setText(dahira.getDahiraName());
        editTextDieuwrine.setText(dahira.getDieuwrine());
        editTextDahiraPhoneNumber.setText(dahira.getDahiraPhoneNumber());
        editTextSiege.setText(dahira.getSiege());
        editTextAdiya.setText(dahira.getTotalAdiya());
        editTextSass.setText(dahira.getTotalSass());
        editTextSocial.setText(dahira.getTotalSocial());

        editTextAdiya.setEnabled(false);
        editTextSass.setEnabled(false);
        editTextSocial.setEnabled(false);

        loadListCommissions();

        showImage(this, dahira.getImageUri(), imageViewLogo);
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
                break;

            case R.id.icon_back:
                finish();
                break;

            case R.id.instructions:
                startActivity(new Intent(this, InstructionsActivity.class));
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.imageView_logo:
                checkPermission();
                chooseImage();
                break;
            case R.id.button_addCommission:
                showListViewCommissions();
                break;
            case R.id.button_save:
                updateData();
                break;
            case R.id.button_back:
                startActivity(new Intent(UpdateDahiraActivity.this, DahiraInfoActivity.class));
                finish();
                break;
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

        if (requestCode == 101 && resultCode == RESULT_OK && Objects.requireNonNull(data).getData() != null) {
            final Uri fileUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);
                imageViewLogo.setImageBitmap(bitmap);
                uploadNewPhoto(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);

        }
    }

    private void updateData() {

        //Info dahira
        dahiraName = editTextDahiraName.getText().toString().trim();
        dieuwrine = editTextDieuwrine.getText().toString().trim();
        dahiraPhoneNumber = editTextDahiraPhoneNumber.getText().toString().trim();
        siege = editTextSiege.getText().toString().trim();

        if (!hasValidationErrors()) {
            dahira.setDahiraName(dahiraName);
            dahira.setDieuwrine(dieuwrine);
            dahira.setDahiraPhoneNumber(dahiraPhoneNumber);
            dahira.setSiege(siege);

            showProgressBar();
            db.collection("dahiras").document(dahira.getDahiraID())
                    .update("dahiraName", dahiraName,
                            "dieuwrine", dieuwrine,
                            "userPhoneNumber", dahiraPhoneNumber,
                            "siege", siege,
                            "listCommissions", dahira.getListCommissions(),
                            "listResponsibles", dahira.getListResponsibles()
                    )
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            hideProgressBar();
                            finish();
                            toastMessage(UpdateDahiraActivity.this, "Changements enregistre avec succe");
                            startActivity(new Intent(UpdateDahiraActivity.this, DahiraInfoActivity.class));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    toastMessage(UpdateDahiraActivity.this, "Erreur enregistrement");
                    startActivity(new Intent(UpdateDahiraActivity.this, DahiraInfoActivity.class));
                    finish();
                }
            });
        }
    }

    private void showListViewCommissions() {
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

    private void loadListCommissions() {

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
        @SuppressLint("InflateParams") final View dialogView = inflater.inflate(R.layout.dialog_update_commission, null);
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

    private boolean hasValidationErrors() {

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

        if (!dahiraPhoneNumber.matches("[0-9,+]+")) {
            editTextDahiraPhoneNumber.setError("Numero incorrect inclure l'indicatif svp.");
            editTextDahiraPhoneNumber.requestFocus();
            return true;
        }

        if (siege.isEmpty()) {
            editTextSiege.setError("Champ obligatoire");
            editTextSiege.requestFocus();
            return true;
        }

        return false;
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @SuppressWarnings("ConstantConditions")
    private void uploadNewPhoto(Bitmap bitmap) {
        Log.d(TAG, "uploadNewPhoto: uploading a new image bitmap to storage");
        BackgroundImageResize resize = new BackgroundImageResize(bitmap);
        Uri uri = null;
        resize.execute(uri);
    }

    private void uploadNewPhoto(Uri imagePath) {
        Log.d(TAG, "uploadNewPhoto: uploading a new image uri to storage.");
        BackgroundImageResize resize = new BackgroundImageResize(null);
        resize.execute(imagePath);
    }

    public byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    private void executeUploadTask() {
        Toast.makeText(UpdateDahiraActivity.this, "uploading image", Toast.LENGTH_SHORT).show();
        //***************************************************************************************
        final String imageName = dahira.getDahiraName() + " " + dahira.getDahiraID();
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("logoDahira/" + imageName);

        final UploadTask uploadTask = storageReference.putBytes(uploadBytes);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if (taskSnapshot.getMetadata() != null) {
                    if (taskSnapshot.getMetadata().getReference() != null) {
                        Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //Toast.makeText(UpdateDahiraActivity.this, "Post Success", Toast.LENGTH_SHORT).show();
                                for (Dahira d : myListDahira) {
                                    if (d.getDahiraID().equals(dahira.getDahiraID())) {
                                        d.setImageUri(uri.toString());
                                        break;
                                    }
                                }
                                if (listAllDahira != null) {
                                    for (Dahira d : listAllDahira) {
                                        if (d.getDahiraID().equals(dahira.getDahiraID())) {
                                            d.setImageUri(uri.toString());
                                            break;
                                        }
                                    }
                                }
                                saveLogoDahira(UpdateDahiraActivity.this, uri.toString());
                            }
                        });
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UpdateDahiraActivity.this, "could not upload photo", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double currentProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                if (currentProgress > (mProgress + 15)) {
                    mProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    Log.d(TAG, "onProgress: upload is " + mProgress + "& done");
                    Toast.makeText(UpdateDahiraActivity.this, mProgress + "%", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //*********************************** Resize and upload Image ********************************************
    @SuppressLint("StaticFieldLeak")
    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]> {
        Bitmap mBitmap;

        public BackgroundImageResize(Bitmap bitmap) {
            if (bitmap != null) {
                this.mBitmap = bitmap;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(UpdateDahiraActivity.this, "compressing image", Toast.LENGTH_SHORT).show();
            showProgressBar();
        }

        @Override
        protected byte[] doInBackground(Uri... params) {
            Log.d(TAG, "doInBackground: started.");

            if (mBitmap == null) {
                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), params[0]);
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: IOException: " + e.getMessage());
                }
            }
            byte[] bytes;
            bytes = getBytesFromBitmap(mBitmap, 15);
            return bytes;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            uploadBytes = bytes;
            hideProgressBar();
            executeUploadTask();
        }
    }
}
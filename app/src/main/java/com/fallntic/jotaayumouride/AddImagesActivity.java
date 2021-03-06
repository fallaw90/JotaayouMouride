package com.fallntic.jotaayumouride;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.adapter.AddImagesAdapter;
import com.fallntic.jotaayumouride.model.Image;
import com.fallntic.jotaayumouride.model.UploadImage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.fallntic.jotaayumouride.R.id.button_finish;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.getSizeImagesStorage;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showAlertDialog;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.dahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listImage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.uploadImages;

public class AddImagesActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "AddImagesActivity";

    private static final int RESULT_LOAD_IMAGE = 1;
    private RecyclerView recyclerViewImage;

    private List<String> fileNameList;
    private List<String> fileDoneList;

    private AddImagesAdapter addImagesAdapter;

    private StorageReference mStorage;
    private Toolbar toolbar;

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private static void updateDocument(String documentID, Map<String, Object> imageMap) {
        FirebaseFirestore.getInstance().collection("images").document(documentID)
                .update(imageMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "images" + " updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error updated " + "images");
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.destroy();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(this, DahiraInfoActivity.class));
    }

    private static void createNewCollection(String documentName, Map<String, Object> imageMap) {
        FirebaseFirestore.getInstance().collection("images").document(documentName)
                .set(imageMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "New collection " + "images" + " set successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error initContributions function line 351");
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_image);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        checkInternetConnection(this);

        mStorage = FirebaseStorage.getInstance().getReference();

        //RecyclerView
        initViews();

        firestore = FirebaseFirestore.getInstance();

        uploadMultipleImages();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        HomeActivity.loadBannerAd(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem iconAdd;
        iconAdd = menu.findItem(R.id.icon_add);

        if (onlineUser.getListDahiraID().contains(dahira.getDahiraID()))
            iconAdd.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.icon_add:
                uploadMultipleImages();
                break;

            case R.id.instructions:
                startActivity(new Intent(this, InstructionsActivity.class));
                break;
        }
        return true;
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Repertoire photo");
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recyclerViewImage = findViewById(R.id.recyclerview_image);
        findViewById(button_finish).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == button_finish) {
            startActivity(new Intent(this, DahiraInfoActivity.class));
            finish();
        }
    }

    private void uploadMultipleImages() {
        fileNameList = new ArrayList<>();
        fileDoneList = new ArrayList<>();
        addImagesAdapter = new AddImagesAdapter(fileNameList, fileDoneList);

        recyclerViewImage.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewImage.setHasFixedSize(true);
        recyclerViewImage.setAdapter(addImagesAdapter);

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Choisir une image"), RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK) {

            if (data.getClipData() != null) {

                int totalItemsSelected = data.getClipData().getItemCount();

                if (totalItemsSelected <= 5) {

                    for (int i = 0; i < totalItemsSelected; i++) {
                        Uri fileUri = data.getClipData().getItemAt(i).getUri();

                        uploadImageToFirebase(i, fileUri);
                    }
                } else {
                    Intent intent = new Intent(AddImagesActivity.this, ShowImagesActivity.class);
                    showAlertDialog(AddImagesActivity.this, "Maximum autorise 5", intent);

                }
            } else if (data.getData() != null) {
                Uri fileUri = data.getData();

                uploadImageToFirebase(0, fileUri);
            }
        }

    }

    private void uploadImageToFirebase(int i, Uri fileUri) {

        final String fileName = getFileName(fileUri);

        fileNameList.add(fileName);
        fileDoneList.add("uploading");
        addImagesAdapter.notifyDataSetChanged();

        final StorageReference fileToUpload = mStorage.child("gallery")
                .child("picture").child(dahira.getDahiraID()).child(fileName);
        final int finalI = i;

        @SuppressWarnings("unused") UploadTask uploadTask = (UploadTask) fileToUpload.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        fileToUpload.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageID = dahira.getDahiraName() + System.currentTimeMillis();
                                Image image = new Image(imageID, dahira.getDahiraID(), uri.toString(), fileName);
                                getSizeImagesStorage(image);
                                listImage.add(image);
                                fileDoneList.remove(finalI);
                                fileDoneList.add(finalI, "done");
                                addImagesAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        saveUploadImages();
                    }
                });
    }

    private String getFileName(Uri uri) {

        String result = null;
        if (Objects.requireNonNull(uri.getScheme()).equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null,
                    null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            toastMessage(AddImagesActivity.this, String.valueOf(Objects.requireNonNull(result).length()));
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void saveUploadImages() {
        if (listImage == null)
            listImage = new ArrayList<>();

        final Map<String, Object> imageMap = new HashMap<>();
        imageMap.put("dahiraID", dahira.getDahiraID());
        imageMap.put("listImage", listImage);

        firestore.collection("images")
                .whereEqualTo("dahiraID", dahira.getDahiraID()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            updateDocument(dahira.getDahiraID(), imageMap);

                            Log.d(TAG, "Image name saved");
                        } else {
                            uploadImages = new UploadImage(dahira.getDahiraID(), fileNameList);
                            createNewCollection(dahira.getDahiraID(), imageMap);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error downloading image name");
                    }
                });
    }

}
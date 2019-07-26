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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.createNewCollection;
import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.updateDocument;
import static com.fallntic.jotaayumouride.DataHolder.uploadImages;
import static com.fallntic.jotaayumouride.R.id.button_finish;

public class AddImagesActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "AddImagesActivity";

    private static final int RESULT_LOAD_IMAGE = 1;
    private RecyclerView recyclerViewImage;

    private List<String> fileNameList;
    private List<String> fileDoneList;

    private AddImagesAdapter addImagesAdapter;

    private StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_image);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setSubtitle("Galerie photo");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mStorage = FirebaseStorage.getInstance().getReference();

        recyclerViewImage = findViewById(R.id.recyclerview_image);

        fileNameList = new ArrayList<>();
        fileDoneList = new ArrayList<>();

        addImagesAdapter = new AddImagesAdapter(fileNameList, fileDoneList);

        //RecyclerView

        recyclerViewImage.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewImage.setHasFixedSize(true);
        recyclerViewImage.setAdapter(addImagesAdapter);

        uploadMultipleImages();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(button_finish).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case button_finish:
                startActivity(new Intent(this, ShowImagesActivity.class));
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_menu, menu);

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
        }
        return true;
    }

    protected void uploadMultipleImages() {
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

                for (int i = 0; i < totalItemsSelected; i++) {

                    Uri fileUri = data.getClipData().getItemAt(i).getUri();

                    String fileName = getFileName(fileUri);

                    fileNameList.add(fileName);
                    fileDoneList.add("uploading");
                    addImagesAdapter.notifyDataSetChanged();

                    StorageReference fileToUpload = mStorage.child("gallery")
                            .child("picture").child(dahira.getDahiraID()).child(fileName);

                    final int finalI = i;
                    fileToUpload.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            fileDoneList.remove(finalI);
                            fileDoneList.add(finalI, "done");

                            addImagesAdapter.notifyDataSetChanged();
                        }
                    });
                }
            } else if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
                    && data != null && data.getData() != null) {
                Uri fileUri = data.getData();

                String fileName = getFileName(fileUri);

                fileNameList.add(fileName);
                fileDoneList.add("uploading");
                addImagesAdapter.notifyDataSetChanged();

                StorageReference fileToUpload = mStorage.child("gallery")
                        .child("picture").child(dahira.getDahiraID()).child(fileName);

                final int finalI = 0;
                fileToUpload.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        fileDoneList.remove(finalI);
                        fileDoneList.add(finalI, "done");

                        addImagesAdapter.notifyDataSetChanged();
                    }
                });
            }
            saveUploadImages();
        }

    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null,
                    null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public void saveUploadImages() {
        FirebaseFirestore.getInstance().collection("uploadImages")
                .whereEqualTo("dahiraID", dahira.getDahiraID()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            uploadImages.getListNameImages().addAll(fileNameList);
                            updateDocument(AddImagesActivity.this,
                                    "uploadImages", dahira.getDahiraID(),
                                    "listNameImages", uploadImages.getListNameImages());

                            Log.d(TAG, "Image name saved");
                        }
                        else {
                            uploadImages = new UploadImage(dahira.getDahiraID(), fileNameList);
                            createNewCollection(AddImagesActivity.this,
                                    "uploadImages", dahira.getDahiraID(), uploadImages);
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












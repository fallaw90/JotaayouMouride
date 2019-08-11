package com.fallntic.jotaayumouride;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
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

import com.fallntic.jotaayumouride.Adapter.AddImagesAdapter;
import com.fallntic.jotaayumouride.Model.Song;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.fallntic.jotaayumouride.R.id.button_finish;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.checkInternetConnection;

public class AddMultipleAudioActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "AddImagesActivity";

    private static final int RESULT_LOAD_IMAGE = 1;
    private RecyclerView recyclerViewImage;

    private List<String> fileNameList;
    private List<String> listDuration;
    private List<String> fileDoneList;
    private List<Song> listSong;
    private Song song;
    UploadTask uploadTask;

    private AddImagesAdapter addImagesAdapter;

    private StorageReference mStorage;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_multiple_audio);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        checkInternetConnection(this);

        mStorage = FirebaseStorage.getInstance().getReference();

        //RecyclerView
        initViews();

        uploadMultipleImages();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

    private void initViews(){
        toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Repertoire photo");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recyclerViewImage = findViewById(R.id.recyclerview_image);
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
        fileNameList = new ArrayList<>();
        fileDoneList = new ArrayList<>();
        addImagesAdapter = new AddImagesAdapter(fileNameList, fileDoneList);

        recyclerViewImage.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewImage.setHasFixedSize(true);
        recyclerViewImage.setAdapter(addImagesAdapter);

        Intent intent = new Intent();
        intent.setType("audio/*");
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

                    final String fileName = getFileName(fileUri);

                    fileNameList.add(fileName);
                    fileDoneList.add("uploading");
                    addImagesAdapter.notifyDataSetChanged();

                    final StorageReference fileToUpload = mStorage.child("audios")
                            .child("serigneMoussaKa").child(fileName);
                    final String duration = getDurationFromMilli(findSongDuration(fileUri));
                    final String songID =  fileName +System.currentTimeMillis();


                    if (listSong == null)
                        listSong = new ArrayList<>();



                    final int finalI = i;

                  uploadTask = (UploadTask) fileToUpload.putFile(fileUri)
                          .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            fileToUpload.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    Song song = new Song(songID, fileName, duration, uri.toString());
                                    listSong.add(song);
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
            }
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
        FirebaseFirestore.getInstance().collection("audio")
                .document("khassida").collection("serigneMoussaKa").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Map<String, Object> songMap = new HashMap<>();
                        songMap.put("listSong", listSong);
                        if (!queryDocumentSnapshots.isEmpty()) {
                            //listSong.addAll(listSong);
                            updateDocument("audios", "serigneMoussaKa", songMap);

                            Log.d(TAG, "Image name saved");
                        } else {
                            createNewCollection("audios", "serigneMoussaKa", songMap);
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

    public static void updateDocument(final String collectionName, String documentID, Map<String, Object> songMap) {
        FirebaseFirestore.getInstance().collection(collectionName).document(documentID)
                .update(songMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, collectionName + " updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error updated " + collectionName);
                    }
                });
    }

    public static void createNewCollection(final String collectionName, String documentName, Object data) {
        FirebaseFirestore.getInstance().collection(collectionName).document(documentName)
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
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

    private String getDurationFromMilli(int durationInMillis) {

        Date date = new Date(durationInMillis);
        SimpleDateFormat simpleDate = new SimpleDateFormat("mm:ss", Locale.getDefault());
        String myTime = simpleDate.format(date);

        return myTime;
    }

    private int findSongDuration(Uri audioUri) {

        int timeInMilliSec = 0;

        try {

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this, audioUri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            timeInMilliSec = Integer.parseInt(time);

            retriever.release();

            return timeInMilliSec;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

}
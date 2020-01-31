package com.fallntic.jotaayumouride;

import android.annotation.SuppressLint;
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

import com.fallntic.jotaayumouride.adapter.AddImagesAdapter;
import com.fallntic.jotaayumouride.model.Song;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
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
import java.util.Objects;

import static com.fallntic.jotaayumouride.R.id.button_finish;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listSong;

@SuppressWarnings("unused")
public class AddMultipleAudioActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "AddMultipleAudioActivity";

    private static final int RESULT_LOAD_IMAGE = 1;
    private RecyclerView recyclerViewImage;

    private List<String> fileNameList;
    private List<String> fileDoneList;

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

        HomeActivity.loadBannerAd(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.destroy();
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
    protected void onResume() {
        super.onResume();
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.resume();
        }
    }

    private void initViews(){
        toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Repertoire photo");
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recyclerViewImage = findViewById(R.id.recyclerview_image);
        findViewById(button_finish).setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.icon_add) {
            uploadMultipleImages();
        }
        return true;
    }

    private void uploadMultipleImages() {
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
            int cut = Objects.requireNonNull(result).lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == button_finish) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
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
                            .child("magal2019HTDKH").child(fileName);


                    if (listSong == null)
                        listSong = new ArrayList<>();

                    final int finalI = i;

                    UploadTask uploadTask = (UploadTask) fileToUpload.putFile(fileUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    fileToUpload.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String id = System.currentTimeMillis() + "";
                                            int millis = findSongDuration(uri);
                                            String duration = getDurationFromMilli(millis);
                                            Song song = new Song(id, fileName, duration, uri.toString());
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

    private void saveUploadImages() {
        Map<String, Object> songMap = new HashMap<>();

        songMap.put("documentID", "magal2019HTDKH");
        songMap.put("listSong", listSong);
        FirebaseFirestore.getInstance().collection("audios")
                .document("magal2019HTDKH").set(songMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Audio name saved");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error downloading image name");
                    }
                });
    }

    private String getDurationFromMilli(int durationInMillis) {

        Date date = new Date(durationInMillis);
        SimpleDateFormat simpleDate = new SimpleDateFormat("mm:ss", Locale.getDefault());

        return simpleDate.format(date);
    }

    private int findSongDuration(Uri audioUri) {

        int timeInMilliSec;

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
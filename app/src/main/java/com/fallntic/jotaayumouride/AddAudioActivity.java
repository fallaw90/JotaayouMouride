package com.fallntic.jotaayumouride;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fallntic.jotaayumouride.Model.Song;
import com.fallntic.jotaayumouride.Utility.MyStaticVariables;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.fallntic.jotaayumouride.HomeActivity.displayInterstitialAd;
import static com.fallntic.jotaayumouride.Utility.DataHolder.dahira;
import static com.fallntic.jotaayumouride.Utility.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.getSizeSongsStorage;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.updateStorageSize;


public class AddAudioActivity extends AppCompatActivity {

    private EditText editTextTitle;
    private TextView textViewSelectedFile;

    private Uri audioUri;

    private FirebaseFirestore firestore;
    private CollectionReference collectionReference;

    private StorageReference mStorage;

    private StorageTask uploadTask;

    private ProgressBar progressBar;

    private String uploadID;
    private TextView textView_titleLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_audio);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setSubtitle("Repertoire Song");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initView();

        textView_titleLayout.setText("Enregistrer un audio dans le repertoire du dahira " +
                dahira.getDahiraName());

        firestore = FirebaseFirestore.getInstance();
        collectionReference = firestore.collection("dahiras")
                .document(dahira.getDahiraID()).collection("audios");

        mStorage = FirebaseStorage.getInstance().getReference()
                .child("gallery")
                .child("audios")
                .child(dahira.getDahiraID());

        displayInterstitialAd(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        startActivity(new Intent(this, DahiraInfoActivity.class));
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(this, DahiraInfoActivity.class));
    }

    public void initView() {
        editTextTitle = findViewById(R.id.editText_titleSong);
        textViewSelectedFile = findViewById(R.id.textView_selectedFile);
        textView_titleLayout = findViewById(R.id.textView_titleLayoutUploadSong);
        //ProgressBar
        progressBar = findViewById(R.id.progressBar);
    }

    public void openAudioFile(View v) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("audio/*");
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data.getData() != null) {

            audioUri = data.getData();
            String fileName = getFileName(audioUri);
            textViewSelectedFile.setText(fileName);
        }
    }

    public String getFileName(Uri uri) {

        String result = null;
        if (audioUri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null,
                    null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }

            if (result == null) {
                result = uri.getPath();
                int cut = result.lastIndexOf('/');

                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }

        }

        return result;
    }

    public void uploadAudioToFirebase(View v) {

        if (textViewSelectedFile.getText().toString().equals("Pas de fichier selectionné")) {
            toastMessage(AddAudioActivity.this, "Sélectionnez un fichier SVP!");
        } else {

            if (uploadTask != null && uploadTask.isInProgress()) {
                toastMessage(AddAudioActivity.this, "Un telechargement est deja en cours");
            } else {
                uploadFile();
            }
        }
    }

    public void uploadFile() {

        if (audioUri != null) {
            String durationTxt;
            toastMessage(AddAudioActivity.this, "Chargement de votre fichier ...");

            uploadID = System.currentTimeMillis() + "." + getFileExtension(audioUri);
            final StorageReference storageReference = mStorage.child(uploadID);

            int durationInMillis = findSongDuration(audioUri);

            if (durationInMillis == 0) {
                durationTxt = "NA";
            }

            durationTxt = getDurationFromMilli(durationInMillis);

            final String finalDurationTxt = durationTxt;

            uploadTask = storageReference.putFile(audioUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    final Song song = new Song(uploadID, editTextTitle.getText().toString(),
                                            finalDurationTxt, uri.toString());

                                    getSizeSongsStorage(song);

                                    if (MyStaticVariables.listSong == null)
                                        MyStaticVariables.listSong = new ArrayList<>();

                                    collectionReference.document(uploadID).set(song)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    progressBar.setVisibility(View.GONE);
                                                    textViewSelectedFile.setText("Aucun fichier selectionne");
                                                    MyStaticVariables.listSong.add(song);
                                                    AlertDialog.Builder builder =
                                                            new AlertDialog.Builder(AddAudioActivity.this, R.style.alertDialog);
                                                    builder.setTitle("Fichier enregistre");
                                                    builder.setMessage("Voulez-vous ajouter un autre fichier song?");
                                                    builder.setCancelable(false);
                                                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                        }
                                                    });

                                                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            updateStorageSize(dahira.getCurrentSizeStorage());
                                                            startActivity(new Intent(AddAudioActivity.this, DahiraInfoActivity.class));
                                                        }
                                                    });
                                                    builder.show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    toastMessage(AddAudioActivity.this,
                                                            e.getMessage());
                                                }
                                            });
                                }
                            });
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            progressBar.setVisibility(View.VISIBLE);
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressBar.setProgress((int) progress);

                        }
                    });
        } else {
            toastMessage(AddAudioActivity.this, "Aucun fichier n'a ete selectione.");
        }
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

    private String getFileExtension(Uri audioUri) {

        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(contentResolver.getType(audioUri));
    }

}

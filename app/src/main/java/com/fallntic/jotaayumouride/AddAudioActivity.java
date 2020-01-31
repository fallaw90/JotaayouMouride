package com.fallntic.jotaayumouride;

import android.annotation.SuppressLint;
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

import com.fallntic.jotaayumouride.model.Song;
import com.fallntic.jotaayumouride.utility.MyStaticVariables;
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
import java.util.Objects;

import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.getSizeSongsStorage;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.updateStorageSize;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.dahira;


@SuppressWarnings("ALL")
public class AddAudioActivity extends AppCompatActivity {

    private EditText editTextTitle;
    private TextView textViewSelectedFile;

    private Uri audioUri;

    private CollectionReference collectionReference;

    private StorageReference mStorage;

    private StorageTask uploadTask;

    private ProgressBar progressBar;

    private String uploadID;
    private TextView textView_titleLayout;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_audio);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setSubtitle("Repertoire Song");
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initView();

        textView_titleLayout.setText("Enregistrer un audio dans le repertoire du dahira " +
                dahira.getDahiraName());

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        collectionReference = firestore.collection("dahiras")
                .document(dahira.getDahiraID()).collection("audios");

        mStorage = FirebaseStorage.getInstance().getReference()
                .child("gallery")
                .child("audios")
                .child(dahira.getDahiraID());

        HomeActivity.loadBannerAd(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        startActivity(new Intent(this, DahiraInfoActivity.class));
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(this, DahiraInfoActivity.class));
    }

    private void initView() {
        editTextTitle = findViewById(R.id.editText_titleSong);
        textViewSelectedFile = findViewById(R.id.textView_selectedFile);
        textView_titleLayout = findViewById(R.id.textView_titleLayoutUploadSong);
        //ProgressBar
        progressBar = findViewById(R.id.progressBar);
    }

    public void openAudioFile() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("audio/*");
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && Objects.requireNonNull(data).getData() != null) {

            audioUri = data.getData();
            String fileName = getFileName(audioUri);
            textViewSelectedFile.setText(fileName);
        }
    }

    private String getFileName(Uri uri) {

        String result = null;
        if (Objects.requireNonNull(audioUri.getScheme()).equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null,
                    null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }

            if (result == null) {
                result = uri.getPath();
                int cut = Objects.requireNonNull(result).lastIndexOf('/');

                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }

        }

        return result;
    }

    private void uploadFile() {

        if (audioUri != null) {
            String durationTxt;
            toastMessage(AddAudioActivity.this, "Chargement de votre fichier ...");

            uploadID = System.currentTimeMillis() + "." + getFileExtension(audioUri);
            final StorageReference storageReference = mStorage.child(uploadID);

            int durationInMillis = findSongDuration(audioUri);

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
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
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

    private String getFileExtension(Uri audioUri) {

        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(contentResolver.getType(audioUri));
    }

    public void uploadAudioToFirebase() {
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

    public void uploadAudioToFirebase(View view) {
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

    public void openAudioFile(View view) {
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
}
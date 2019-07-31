package com.fallntic.jotaayumouride;

import android.content.ContentResolver;
import android.content.Intent;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fallntic.jotaayumouride.Model.UploadSong;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;

public class GalleryAudioActivity extends AppCompatActivity {

    EditText editTextTitle;
    TextView textViewSelectedFile;

    Uri audioUri;

    StorageReference mStorage;
    DatabaseReference databaseReference;

    StorageTask uploadTask;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_audio);

        editTextTitle = findViewById(R.id.editText_titleSong);
        textViewSelectedFile = findViewById(R.id.textView_selectedFile);

        //ProgressBar
        progressBar = findViewById(R.id.progressBar);

        mStorage = FirebaseStorage.getInstance().getReference()
                .child("gallery")
                .child("audio")
                .child(dahira.getDahiraID());

        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("gallery")
                .child("audio")
                .child(dahira.getDahiraID());
    }

    public void openAudioFile(View v){

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data.getData() != null){

            audioUri = data.getData();
            String fileName = getFileName(audioUri);
            textViewSelectedFile.setText(fileName);
        }
    }

    public String getFileName(Uri uri){

        String result = null;
        if(audioUri.getScheme().equals("content")){
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);

            try {
                if (cursor != null && cursor.moveToFirst()){
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }finally {
                cursor.close();
            }

            if (result == null){
                result = uri.getPath();
                int cut = result.lastIndexOf('/');

                if (cut != -1){
                    result = result.substring(cut + 1);
                }
            }

        }

        return result;
    }


    public void uploadAudioToFirebase(View v){

        if (textViewSelectedFile.getText().toString().equals("Pas de fichier selectionné")){
            toastMessage(GalleryAudioActivity.this, "Sélectionnez un fichier SVP!");
        }
        else {

            if (uploadTask != null && uploadTask.isInProgress()){
                toastMessage(GalleryAudioActivity.this, "Un telechargement est deja en cours");
            }
            else {
                uploadFile();
            }
        }
    }

    public void uploadFile(){

        if (audioUri != null){
            String durationTxt;
            toastMessage(GalleryAudioActivity.this, "Chargement de votre fichier ...");

            progressBar.setVisibility(View.VISIBLE);

            final StorageReference storageReference = mStorage
                    .child(System.currentTimeMillis() + "." + getFileExtension(audioUri));

            int durationInMillis = findSongDuration(audioUri);

            if (durationInMillis == 0){
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

                                    UploadSong uploadSong = new UploadSong(editTextTitle.getText().toString(),
                                            finalDurationTxt, uri.toString());

                                    String uploadID = databaseReference.push().getKey();
                                    databaseReference.child(uploadID).setValue(uploadSong);

                                }
                            });

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            progressBar.setProgress((int)progress);

                        }
                    });
        }
        else {
            toastMessage(GalleryAudioActivity.this, "Aucun fichier n'a ete selectione.");
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

        }catch (Exception e){
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

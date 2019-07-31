package com.fallntic.jotaayumouride;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.MainActivity.progressBar;
import static com.fallntic.jotaayumouride.MainActivity.relativeLayoutProgressBar;

public class RecordAudio extends AppCompatActivity {

    //Create placeholder for user's consent to record_audio permission.
    //This will be used in handling callback
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;
    File mAudioFile;
    boolean isRecording = false;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private StorageReference storageReference;
    private RelativeLayout relativeLayout;

    private static File createAudioFile(Context context, String audioName) throws IOException {
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PODCASTS);
        File audio = File.createTempFile(
                audioName,  /* prefix */
                ".3gp",         /* suffix */
                storageDir      /* directory */
        );

        return audio;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_audio);

        storageReference = FirebaseStorage.getInstance().getReference();

        //ProgressBar from static variable MainActivity
        relativeLayout = findViewById(R.id.relativeLayout);
        relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        progressBar = findViewById(R.id.progressBar);
        relativeLayoutProgressBar.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        try {
            mAudioFile = createAudioFile(this, "demo");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startRecording(View view) {
        requestAudioPermissions();
    }

    private void recordAudio() {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(mAudioFile.getAbsolutePath());
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        }

        if (!isRecording) {
            try {
                mRecorder.prepare();
                mRecorder.start();
                isRecording = true;
            } catch (IOException e) {
                Log.e("Audio", "prepare() failed");
            }
        } else if (isRecording) {
            isRecording = false;
            stopRecording();
        }
    }

    public void stopRecording() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
        uploadAudio();
    }

    public void startPlaying(View view) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mAudioFile.getAbsolutePath());
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e("Audio", "prepare() failed");
        }
    }

    public void stopPlaying() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    //Requesting run-time permissions

    @Override
    public void onDestroy() {

        if (mPlayer != null) {
            stopPlaying();
        }

        if (mRecorder != null) {
            stopRecording();
        }

        super.onDestroy();
    }

    private void requestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show();

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);

            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);
            }
        }
        //If permission is granted, then go ahead recording audio
        else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {

            //Go ahead with recording audio now
            recordAudio();
        }
    }

    //Handling callback
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    recordAudio();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permissions Denied to record audio", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void uploadAudio(){

        hideLayout();

        final String fileName = System.currentTimeMillis() + onlineUser.getUserName();
        StorageReference filePath = storageReference.child("Audio").child(dahira.getDahiraID())
                .child(fileName);
        final Uri uri = Uri.fromFile(mAudioFile);

        filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                hideLayout();
                toastMessage(RecordAudio.this, "Message envoye");

                AudioUpload audioUpload = new AudioUpload(fileName, uri.toString(), dahira.getDahiraID(),
                                            onlineUser.getUserName());

                FirebaseFirestore.getInstance().collection("announcements")
                        .document(dahira.getDahiraID())
                        .collection("audios")
                        .document()
                        .set(audioUpload)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                displayLayout();
                                Log.d(TAG, "Audio saved successfully");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                displayLayout();
                                Log.d(TAG, "Error saving audio");
                            }
                        });
            }
        });
    }

    public void hideLayout(){
        relativeLayout.setVisibility(View.GONE);
        relativeLayoutProgressBar.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void displayLayout(){
        relativeLayout.setVisibility(View.VISIBLE);
        relativeLayoutProgressBar.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }
}

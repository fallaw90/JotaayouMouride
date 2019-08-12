package com.fallntic.jotaayumouride;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.fallntic.jotaayumouride.Model.Song;
import com.fallntic.jotaayumouride.Utility.DataHolder;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.fallntic.jotaayumouride.Utility.DataHolder.dahira;
import static com.fallntic.jotaayumouride.Utility.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.checkInternetConnection;

public class RecordAudioActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "RecordAudioActivity";

    private int RECORD_AUDIO_REQUEST_CODE = 123;
    private Toolbar toolbar;
    private Chronometer chronometer;
    private ImageView imageViewRecord, imageViewPlay, imageViewStop;
    private SeekBar seekBar;
    private LinearLayout linearLayoutRecorder, linearLayoutPlay;
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    private String filePath = null;
    private int lastProgress = 0;
    private Handler mHandler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            seekUpdation();
        }
    };
    private boolean isPlaying = false;
    private Button buttonCancel, buttonSend;
    private ProgressBar progressBar;
    private StorageReference mStorage;
    private FirebaseFirestore firestore;
    private CollectionReference collectionReference;
    private StorageTask storageTask;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_audio);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /** setting up the toolbar  **/
        toolbar = findViewById(R.id.toolbar);
        toolbar.setLogo(R.mipmap.logo);
        setSupportActionBar(toolbar);

        //initializingViews
        initViews();
        //***************** Set logo **********************


        checkInternetConnection(this);

        storageReference = FirebaseStorage.getInstance().getReference();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPermissionToRecordAudio();
        }
    }

    private void initViews() {

        linearLayoutRecorder = findViewById(R.id.linearLayoutRecorder);
        chronometer = findViewById(R.id.chronometerTimer);
        chronometer.setBase(SystemClock.elapsedRealtime());
        imageViewRecord = findViewById(R.id.imageViewRecord);
        imageViewStop = findViewById(R.id.imageViewStop);
        imageViewPlay = findViewById(R.id.imageViewPlay);
        linearLayoutPlay = findViewById(R.id.linearLayoutPlay);
        seekBar = findViewById(R.id.seekBar);
        buttonSend = findViewById(R.id.button_send);
        buttonCancel = findViewById(R.id.button_cancel);
        progressBar = findViewById(R.id.progressBar);

        imageViewRecord.setOnClickListener(this);
        imageViewStop.setOnClickListener(this);
        imageViewPlay.setOnClickListener(this);
        buttonSend.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);


        //Init FirebaseFireStore
        firestore = FirebaseFirestore.getInstance();
        collectionReference = firestore.collection("announcements")
                .document(dahira.getDahiraID()).collection("audios");

        //Init FirebaseStorage
        mStorage = FirebaseStorage.getInstance().getReference()
                .child("announcements")
                .child(dahira.getDahiraID())
                .child("audios");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        if (dahira != null)
            startActivity(new Intent(RecordAudioActivity.this, DahiraInfoActivity.class));
        else
            startActivity(new Intent(RecordAudioActivity.this, HomeActivity.class));
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getPermissionToRecordAudio() {
        // 1) Use the support library version ContextCompat.checkSelfPermission(...) to avoid
        // checking the build version since Context.checkSelfPermission(...) is only available
        // in Marshmallow
        // 2) Always check for permission (even if permission has already been granted)
        // since the user can revoke permissions at any time through Settings
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // The permission is NOT already granted.
            // Check if the user has been asked about this permission already and denied
            // it. If so, we want to give more explanation about why the permission is needed.
            // Fire off an async request to actually get the permission
            // This will show the standard permission request dialog UI
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO, Manifest.permission
                    .WRITE_EXTERNAL_STORAGE}, RECORD_AUDIO_REQUEST_CODE);

        }
    }

    // Callback with the request from calling requestPermissions(...)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        // Make sure it's our original READ_CONTACTS request
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.length == 3 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "You must give permissions to use this app. App is exiting.", Toast.LENGTH_SHORT).show();
                finishAffinity();
            }
        }

    }

    @Override
    public void onClick(View view) {
        if (view == imageViewRecord) {
            prepareForRecording();
            startRecording();
        } else if (view == imageViewStop) {
            prepareForStop();
            stopRecording();
        } else if (view == imageViewPlay) {
            if (!isPlaying && filePath != null) {
                isPlaying = true;
                startPlaying();
            } else {
                isPlaying = false;
                stopPlaying();
            }
        } else if (view == buttonSend) {
            uploadAudioToFirebase(view);
        }

    }

    private void prepareForRecording() {
        TransitionManager.beginDelayedTransition(linearLayoutRecorder);
        imageViewRecord.setVisibility(View.GONE);
        imageViewStop.setVisibility(View.VISIBLE);
        linearLayoutPlay.setVisibility(View.GONE);
    }

    private void startRecording() {
        //we use the MediaRecorder class to record

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        /**In the lines below, we create a directory named JotaayouMouride/Audios
         * in the phone storage and the audios are being stored in the Audios folder **/
        File root = android.os.Environment.getExternalStorageDirectory();
        File file = new File(root.getAbsolutePath() + "/JotaayouMouride/Audios");
        if (!file.exists()) {
            file.mkdirs();
        }

        filePath = root.getAbsolutePath() + "/JotaayouMouride/Audios/" +
                (DataHolder.onlineUser.getUserName() + System.currentTimeMillis() + ".mp3");

        Log.d("filename", filePath);
        mRecorder.setOutputFile(filePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        lastProgress = 0;
        seekBar.setProgress(0);
        stopPlaying();
        //starting the chronometer
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    private void stopPlaying() {
        try {
            mPlayer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPlayer = null;
        //showing the play button
        imageViewPlay.setImageResource(R.drawable.ic_play);
        chronometer.stop();
    }

    private void prepareForStop() {
        TransitionManager.beginDelayedTransition(linearLayoutRecorder);
        imageViewRecord.setVisibility(View.VISIBLE);
        imageViewStop.setVisibility(View.GONE);
        linearLayoutPlay.setVisibility(View.VISIBLE);
    }

    private void stopRecording() {

        try {
            mRecorder.stop();
            mRecorder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mRecorder = null;
        //starting the chronometer
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
        //showing the play button
        buttonSend.setVisibility(View.VISIBLE);

        Toast.makeText(this, "Enregistrement termine.", Toast.LENGTH_SHORT).show();
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        Log.d(TAG, "In startPlaying filePath = " + filePath);
        try {
            mPlayer.setDataSource(filePath);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
        //making the imageview pause button
        imageViewPlay.setImageResource(R.drawable.ic_pause);

        seekBar.setProgress(lastProgress);
        mPlayer.seekTo(lastProgress);
        seekBar.setMax(mPlayer.getDuration());
        seekUpdation();
        chronometer.start();

        /** once the audio is complete, timer is stopped here**/
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                imageViewPlay.setImageResource(R.drawable.ic_play);
                isPlaying = false;
                chronometer.stop();
            }
        });

        /** moving the track as per the seekBar's position**/
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mPlayer != null && fromUser) {
                    //here the track's progress is being changed as per the progress bar
                    mPlayer.seekTo(progress);
                    //timer is being updated as per the progress of the seekbar
                    chronometer.setBase(SystemClock.elapsedRealtime() - mPlayer.getCurrentPosition());
                    lastProgress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void seekUpdation() {
        if (mPlayer != null) {
            int mCurrentPosition = mPlayer.getCurrentPosition();
            seekBar.setProgress(mCurrentPosition);
            lastProgress = mCurrentPosition;
        }
        mHandler.postDelayed(runnable, 100);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem itemBack;
        itemBack = menu.findItem(R.id.icon_back);
        itemBack.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.button_back:
                if (dahira != null)
                    startActivity(new Intent(RecordAudioActivity.this, DahiraInfoActivity.class));
                else
                    startActivity(new Intent(RecordAudioActivity.this, HomeActivity.class));
                finish();
                break;

            case R.id.instructions:
                startActivity(new Intent(RecordAudioActivity.this, InstructionsActivity.class));
                finish();
            default:
                return super.onOptionsItemSelected(item);

        }
        return true;
    }

    public void uploadAudioToFirebase(View v) {
        if (storageTask != null && storageTask.isInProgress()) {
            toastMessage(RecordAudioActivity.this, "Un telechargement est deja en cours");
        } else {
            uploadFile();
        }
    }

    public void uploadFile() {
        if (filePath != null) {

            String durationTxt;
            toastMessage(RecordAudioActivity.this, "Enregistrement de votre annonce en cours ...");

            final Uri audioUri = Uri.fromFile(new File(filePath));

            int durationInMillis = findSongDuration(audioUri);
            if (durationInMillis == 0) {
                durationTxt = "NA";
            }

            durationTxt = getDurationFromMilli(durationInMillis);

            final String finalDurationTxt = durationTxt;

            final String songID = DataHolder.onlineUser.getUserName() + System.currentTimeMillis();


            final StorageReference storageReference = this.storageReference.child("announcements")
                    .child("audios").child(dahira.getDahiraID()).child(songID);

            progressBar.setVisibility(View.VISIBLE);
            storageTask = storageReference.putFile(audioUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    Song song = new Song(songID, DataHolder.onlineUser.getUserName(),
                                            finalDurationTxt, uri.toString());

                                    collectionReference.document(songID).set(song)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    progressBar.setVisibility(View.GONE);

                                                    AlertDialog.Builder builder = new AlertDialog.
                                                            Builder(RecordAudioActivity.this, R.style.alertDialog);
                                                    builder.setTitle("Annonce envoyee!");
                                                    builder.setMessage("Cliquez sur OK pour continuer.");
                                                    builder.setCancelable(true);
                                                    builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            startActivity(new Intent(RecordAudioActivity.this,
                                                                    SendAnnouncementActivity.class));
                                                        }
                                                    });
                                                    builder.show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    toastMessage(RecordAudioActivity.this,
                                                            "Error " + e.getMessage() + "Reessayez SVP");
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
}
package com.fallntic.jotaayumouride;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.firebaseStorage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.storageReference;


@SuppressWarnings("ALL")
public class AdvertisementActivity extends AppCompatActivity {
    private static final String TAG = AdvertisementActivity.class.getSimpleName();

    private static final int PICK_IMAGE_REQUEST = 100;

    private ImageView imageView;
    private byte[] uploadBytes;
    private double mProgress = 0;

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        return Bitmap.createScaledBitmap(realImage, width, height, filter);
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }


    private void saveToFirestore(final Context context, final String uri) {
        final Map<String, Object> mapUri = new HashMap<>();
        mapUri.put("image_uri", uri);
        onlineUser.setImageUri(uri);
        firestore.collection("advertisements").document("my_ads").collection("image_ads")
                .add(mapUri).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                Log.d(TAG, "Image name saved");
                toastMessage(context, "Photo profil enregistre.");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error downloading image name");
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertisement);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && Objects.requireNonNull(data).getData() != null) {
            Uri selectedUri = data.getData();
            try {
                Bitmap selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedUri);
                imageView.setImageBitmap(selectedBitmap);
                uploadNewPhoto(selectedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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

    private void executeUploadTask() {
        Toast.makeText(AdvertisementActivity.this, "uploading image", Toast.LENGTH_SHORT).show();


        //***************************************************************************************
        final String imageId = "Jambaar " + System.currentTimeMillis();

        //StorageReference imageId = storageReference.child("advertisements").child("Jambaar " + System.currentTimeMillis());
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("advertisements/" + imageId);

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
                                Toast.makeText(AdvertisementActivity.this, "Post Success", Toast.LENGTH_SHORT).show();
                                saveToFirestore(AdvertisementActivity.this, uri.toString());
                                //createNewPost(imageUrl);
                            }
                        });
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AdvertisementActivity.this, "could not upload photo", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double currentProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                if (currentProgress > (mProgress + 15)) {
                    mProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    Log.d(TAG, "onProgress: upload is " + mProgress + "& done");
                    Toast.makeText(AdvertisementActivity.this, mProgress + "%", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //*********************************** Resize Image ********************************************
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
            Toast.makeText(AdvertisementActivity.this, "compressing image", Toast.LENGTH_SHORT).show();
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
            byte[] bytes = null;
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
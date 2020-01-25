package com.fallntic.jotaayumouride;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.firebaseStorage;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.firestore;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.storageReference;

public class ImageAdvertisementActivity extends AppCompatActivity {
    private static final String TAG = ImageAdvertisementActivity.class.getSimpleName();

    private final int PICK_IMAGE_REQUEST = 100;

    private ImageView imageView;
    private String imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_advertisement);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data.getData() != null) {
            final Uri fileUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            final StorageReference fileToUpload = storageReference.child("advertisements").child("Jambaar " + System.currentTimeMillis());
            fileToUpload.putFile(fileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileToUpload.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    saveToFirestore(ImageAdvertisementActivity.this, uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            toastMessage(ImageAdvertisementActivity.this, "Une erreur est survenue, merci de reessayer plus tard");
                        }
                    });
        }
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
}
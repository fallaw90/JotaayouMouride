package com.fallntic.jotaayumouride;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Adapter.ImageAdapter;
import com.fallntic.jotaayumouride.Model.Image;
import com.fallntic.jotaayumouride.Model.Song;
import com.fallntic.jotaayumouride.Utility.PhotoFullPopupWindow;
import com.fallntic.jotaayumouride.Utility.SwipeToDeleteCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import static com.fallntic.jotaayumouride.Utility.DataHolder.dahira;
import static com.fallntic.jotaayumouride.Utility.DataHolder.indexOnlineUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.updateStorageSize;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.firebaseStorage;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listImage;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listSong;

public class ShowImagesActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ShowImagesActivity";
    //recyclerview object
    private RecyclerView recyclerView;

    //adapter object
    private ImageAdapter imageAdapter;
    private TextView textViewEmpty, textViewDelete, textViewTitle;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_images);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setLogo(R.mipmap.logo);
        setSupportActionBar(toolbar);


        initViews();
        if (onlineUser.getListDahiraID().contains(dahira.getDahiraID()) && onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur")) {
            if (dahira.getDedicatedSizeStorage() <= 0)
                updateStorageSize(dahira.getCurrentSizeStorage(), 200);
            else {
                getSizeStorage();
            }
        } else {
            textViewTitle.setText("Repertoire photo du dahira " + dahira.getDahiraName());
        }

        if (listImage != null && listImage != null && !listImage.isEmpty()) {

            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            imageAdapter = new ImageAdapter(this, listImage, new CustomItemClickListener() {
                @Override
                public void onItemClick(View v, int position) {
                    new PhotoFullPopupWindow(ShowImagesActivity.this, R.layout.popup_photo_full, v, listImage.get(position).getUri(), null);
                    //toastMessage(ShowImagesActivity.this, "A clique");
                }
            });

            recyclerView.setAdapter(imageAdapter);
        } else {
            if (onlineUser.getListDahiraID().contains(dahira.getDahiraID()))
                textViewEmpty.setText("Votre album photo est vide.\n Clique sur l'icone (+) pour ajouter des photo.");
            else
                textViewEmpty.setText("Cette album photo est vide.");

            textViewDelete.setVisibility(View.GONE);
        }

        if (onlineUser != null && indexOnlineUser > -1 && onlineUser.getListDahiraID().contains(dahira.getDahiraID()) &&
                onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur")) {
            enableSwipeToDelete(this);
        } else {
            textViewDelete.setVisibility(View.GONE);
        }

        HomeActivity.showInterstitialAd(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_back:
                updateStorageSize(dahira.getCurrentSizeStorage());
                startActivity(new Intent(ShowImagesActivity.this, DahiraInfoActivity.class));
                break;
        }
    }

    private void initViews() {
        textViewEmpty = findViewById(R.id.textView_empty);
        textViewDelete = findViewById(R.id.textView_delete);
        textViewTitle = findViewById(R.id.textView_title);
        recyclerView = findViewById(R.id.recyclerView);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        findViewById(R.id.button_back).setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        updateStorageSize(dahira.getCurrentSizeStorage());
        startActivity(new Intent(this, DahiraInfoActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem iconAdd;
        iconAdd = menu.findItem(R.id.icon_add);

        if (onlineUser.getListDahiraID().contains(dahira.getDahiraID()) && indexOnlineUser >= 0) {
            if (onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur"))
                iconAdd.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.icon_add:
                if (dahira.getCurrentSizeStorage() < dahira.getDedicatedSizeStorage())
                    startActivity(new Intent(this, AddImagesActivity.class));
                else
                    showAlertDialog(this, "Memoire insuffisante! Veuillez contacter +1 (320) 803-0902 via WhatSapp si vous avez besoin plus de memoire.");
                break;

            case R.id.instructions:
                startActivity(new Intent(this, InstructionsActivity.class));
                break;

        }
        return true;
    }

    private void enableSwipeToDelete(final Context context) {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                final int position = viewHolder.getAdapterPosition();
                final Image image = listImage.get(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(ShowImagesActivity.this, R.style.alertDialog);
                builder.setTitle("Supprimer image!");
                builder.setMessage("Etes vous sure de vouloir supprimer cette image?");
                builder.setCancelable(false);
                builder.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        imageAdapter.removeItem(position);
                        Map<String, Object> imageMap = new HashMap<>();
                        imageMap.put("listImage", listImage);
                        //Remove item in FirebaseFireStore
                        updateDocument(imageMap, image);
                    }
                });
                builder.setNegativeButton("NON", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(ShowImagesActivity.this, ShowImagesActivity.class));
                    }
                });
                builder.show();
            }
        };
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);
    }

    public void updateDocument(Map<String, Object> imageMap, final Image image) {
        FirebaseFirestore.getInstance().collection("images").document(dahira.getDahiraID())
                .update(imageMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "images" + " updated");

                        removeInFirebaseStorage(image);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error updated " + "images");
                    }
                });
    }

    public void removeInFirebaseStorage(final Image image) {
        if (image.getUri() != null) {

            StorageReference storageRef = firebaseStorage
                    .getReferenceFromUrl(image.getUri());

            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @SuppressLint("LongLogTag")
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    startActivity(new Intent(ShowImagesActivity.this, ShowImagesActivity.class));
                    Log.d(TAG, "onSuccess: deleted file");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @SuppressLint("LongLogTag")
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Log.d(TAG, "onFailure: did not delete file");
                }
            });
        }
    }

    public void getSizeStorage() {
        FirebaseStorage storage = FirebaseStorage.getInstance(); // 1
        StorageReference storageRef = storage.getReference();
        StorageReference reference;

        dahira.setCurrentSizeStorage(0);
        for (final Song song : listSong) {
            reference = storageRef.child("gallery").child("audios").child(dahira.getDahiraID()).child(song.getAudioID());
            reference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    dahira.setCurrentSizeStorage(dahira.getCurrentSizeStorage() + storageMetadata.getSizeBytes() / 1048576);
                    if (dahira.getCurrentSizeStorage() < dahira.getDedicatedSizeStorage())
                        textViewTitle.setText("Repertoire photo du dahira " + dahira.getDahiraName() + "\nMemoire disponible " + (dahira.getDedicatedSizeStorage() - dahira.getCurrentSizeStorage()) + " Mo.");
                    else
                        textViewTitle.setText("Memoire insuffisante! Veuillez contacter +1 (320) 803-0902 via WhatSapp pour reserver plus de memoire.\"");
                    Log.i("Size = ", String.valueOf(storageMetadata.getSizeBytes()));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {

                }
            });
        }

        for (final Image image : listImage) {
            reference = storageRef.child("gallery").child("picture").child(dahira.getDahiraID()).child(image.getImageName());
            reference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    dahira.setCurrentSizeStorage(dahira.getCurrentSizeStorage() + storageMetadata.getSizeBytes() / 1048576);
                    if (dahira.getCurrentSizeStorage() < dahira.getDedicatedSizeStorage())
                        textViewTitle.setText("Repertoire photo du dahira " + dahira.getDahiraName() + "\nMemoire disponible " + (dahira.getDedicatedSizeStorage() - dahira.getCurrentSizeStorage()) + " Mo.");
                    else
                        textViewTitle.setText("Memoire insuffisante! Veuillez contacter +1 (320) 803-0902 via WhatSapp pour reserver plus de memoire.\"");
                    Log.i("Size = ", String.valueOf(storageMetadata.getSizeBytes()));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {

                }
            });
        }
    }

}
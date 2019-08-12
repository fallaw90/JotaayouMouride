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
import com.fallntic.jotaayumouride.Utility.PhotoFullPopupWindow;
import com.fallntic.jotaayumouride.Utility.SwipeToDeleteCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import static com.fallntic.jotaayumouride.Utility.DataHolder.dahira;
import static com.fallntic.jotaayumouride.Utility.DataHolder.indexOnlineUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.firebaseStorage;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listImage;

public class ShowImagesActivity extends AppCompatActivity implements View.OnClickListener{
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

        textViewTitle.setText("Repertoire photo du dahira " + dahira.getDahiraName());

        if (listImage != null && listImage != null && !listImage.isEmpty()){

            if (!onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur"))
                textViewDelete.setVisibility(View.GONE);

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
        }
        else{
            if (onlineUser.getListDahiraID().contains(dahira.getDahiraID()))
                textViewEmpty.setText("Votre album photo est vide.\n Clique sur l'icone (+) pour ajouter des photo.");
            else
                textViewEmpty.setText("Cette album photo est vide.");

            textViewDelete.setVisibility(View.GONE);
        }

        enableSwipeToDelete(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_back:
                startActivity(new Intent(ShowImagesActivity.this, DahiraInfoActivity.class));
                break;
        }
    }

    private void initViews(){
        textViewEmpty = findViewById(R.id.textView_empty);
        textViewDelete = findViewById(R.id.textView_delete);
        textViewTitle = findViewById(R.id.textView_title);
        recyclerView = findViewById(R.id.recyclerView);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        findViewById(R.id.button_back).setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
                startActivity(new Intent(this, AddImagesActivity.class));
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
                        updateDocument("images", dahira.getDahiraID(), imageMap, image);
                    }
                });
                builder.setNegativeButton("NON", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(ShowImagesActivity.this,
                                ShowImagesActivity.class));
                    }
                });
                builder.show();
            }
        };
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);
    }

    public void updateDocument(final String collectionName, String documentID, Map<String, Object> imageMap, final Image image) {
        FirebaseFirestore.getInstance().collection(collectionName).document(documentID)
                .update(imageMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, collectionName + " updated");

                        removeInFirebaseStorage(image);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error updated " + collectionName);
                    }
                });
    }

    public void removeInFirebaseStorage(Image image) {
        if (image.getUri() != null) {

            StorageReference storageRef = firebaseStorage
                    .getReferenceFromUrl(image.getUri());

            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @SuppressLint("LongLogTag")
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
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
}
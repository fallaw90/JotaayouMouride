package com.fallntic.jotaayumouride;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class DataHolder {

    public static String dahiraID;
    public static User user = new User();
    public static Dahira dahira = new Dahira();
    private static ProgressDialog progressDialog;

    public static boolean isConnected(Context context) {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();

            return connected;

        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }

        return connected;
    }

    public static void showLogoDahira(Context context, ImageView imageView) {
        FirebaseUser firebaseUser;
        FirebaseStorage firebaseStorage;
        ProgressDialog progressDialog;
        // Reference to the image file in Cloud Storage
        progressDialog = new ProgressDialog(context);
        firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference;
        storageReference = firebaseStorage.getReference();
        StorageReference logoDahiraReference = storageReference.child("logoDahira").child(dahira.getDahiraID());
        showProgressDialog(context, "Chargement de l'image ...");
        // Download directly from StorageReference using Glide
        GlideApp.with(context)
                .load(logoDahiraReference)
                .placeholder(R.drawable.logo_dahira)
                .into(imageView);

        dismissProgressDialog();
    }

    public static void showProfileImage(Context context, ImageView imageView) {
        FirebaseUser firebaseUser;
        FirebaseStorage firebaseStorage;
        ProgressDialog progressDialog;
        // Reference to the image file in Cloud Storage
        firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference;
        storageReference = firebaseStorage.getReference();
        StorageReference logoDahiraReference = storageReference.child("profileImage").child(user.getUserID());
        showProgressDialog(context, "Chargement de l'image ...");
        // Download directly from StorageReference using Glide
        GlideApp.with(context)
                .load(logoDahiraReference)
                .placeholder(R.drawable.profile_image)
                .into(imageView);

        dismissProgressDialog();
    }

    public static void toastMessage(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showProgressDialog(Context context, String str){
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(str);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        dismissProgressDialog();
        progressDialog.show();
    }

    public static void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

}
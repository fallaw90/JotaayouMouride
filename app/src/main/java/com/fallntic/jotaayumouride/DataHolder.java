package com.fallntic.jotaayumouride;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DataHolder {

    public static String dahiraID;
    public static String onlineUserID;
    public static User onlineUser = new User();
    public static User selectedUser = new User();
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

    public static void showProfileImage(Context context, String child, ImageView imageView) {
        FirebaseUser firebaseUser;
        FirebaseStorage firebaseStorage;
        ProgressDialog progressDialog;
        // Reference to the image file in Cloud Storage
        firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference;
        storageReference = firebaseStorage.getReference();
        StorageReference logoDahiraReference = storageReference.child("profileImage").child(child);
        showProgressDialog(context, "Chargement de l'image ...");
        // Download directly from StorageReference using Glide
        GlideApp.with(context)
                .load(logoDahiraReference)
                .placeholder(R.drawable.profile_image)
                .into(imageView);

        dismissProgressDialog();
    }

    public static void toastMessage(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
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

    public static void logout(){
        onlineUser = null;
        selectedUser = null;
        dahira = null;
        ProfileActivity.boolMyDahiras = false;
        ProfileActivity.boolAllDahiras = false;
        FirebaseAuth.getInstance().signOut();
    }

    public static void showAlertDialog(final Context context, String message){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View dialogView = inflater.inflate(R.layout.alert_dialog, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        final TextView textViewAlertDialog = (TextView) dialogView.findViewById(R.id.textView_alertDialog);
        final Button buttonAlertDialog = (Button) dialogView.findViewById(R.id.button_dialog);

        textViewAlertDialog.setText(message);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonAlertDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }

    public static void showAlertDialog(final Context context, String message, final Intent intent){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View dialogView = inflater.inflate(R.layout.alert_dialog, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        final TextView textViewAlertDialog = (TextView) dialogView.findViewById(R.id.textView_alertDialog);
        final Button buttonAlertDialog = (Button) dialogView.findViewById(R.id.button_dialog);

        textViewAlertDialog.setText(message);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonAlertDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(intent);
                alertDialog.dismiss();
            }
        });
    }

    public static boolean hasValidationErrors(String name, EditText editTextName, String phoneNumber,
                                        EditText editTextPhoneNumber, String address, EditText editTextAddress,
                                        String adiya, EditText editTextAdiya, String sass, EditText editTextSass,
                                        String social, EditText editTextSocial) {

        if (name.isEmpty()) {
            editTextName.setError("Ce champ est obligatoir!");
            editTextName.requestFocus();
            return true;
        }

        if (phoneNumber.isEmpty()) {
            editTextPhoneNumber.setError("Champ obligatoire");
            editTextPhoneNumber.requestFocus();
            return true;
        }

        if (!phoneNumber.isEmpty() && (!phoneNumber.matches("[0-9]+") || phoneNumber.length() != 9 || !checkPrefix(phoneNumber))) {
            editTextPhoneNumber.setError("Numero de telephone incorrect");
            editTextPhoneNumber.requestFocus();
            return true;
        }

        if (address.isEmpty()) {
            editTextAddress.setError("Ce champ est obligatoir!");
            editTextAddress.requestFocus();
            return true;
        }

        if (adiya.isEmpty() || !isDouble(adiya)) {
            editTextAdiya.setError("Valeur incorrecte!");
            editTextAdiya.requestFocus();
            return true;
        }

        if (sass.isEmpty() || !isDouble(sass)) {
            editTextSass.setError("Valeur incorrecte!");
            editTextSass.requestFocus();
            return true;
        }

        if (social.isEmpty() || !isDouble(social)) {
            editTextSocial.setError("Valeur incorrecte!");
            editTextSocial.requestFocus();
            return true;
        }

        return false;
    }

    public static boolean hasValidationErrors(String dahiraName, EditText editTextDahiraName, String dieuwrine,
                                        EditText editTextDieuwrine, String dahiraPhoneNumber, EditText editTextDahiraPhoneNumber,
                                        String siege, EditText editTextSiege, String totalAdiya, EditText editTextAdiya,
                                        String totalSass, EditText editTextSass, String totalSocial, EditText editTextSocial) {

        if (dahiraName.isEmpty()) {
            editTextDahiraName.setError("Nom dahira obligatoire");
            editTextDahiraName.requestFocus();
            return true;
        }

        if (dieuwrine.isEmpty()) {
            editTextDieuwrine.setError("Champ obligatoire");
            editTextDieuwrine.requestFocus();
            return true;
        }

        if (dahiraPhoneNumber.isEmpty()) {
            editTextDahiraPhoneNumber.setError("Champ obligatoire");
            editTextDahiraPhoneNumber.requestFocus();
            return true;
        }

        if(!dahiraPhoneNumber.isEmpty() && (!dahiraPhoneNumber.matches("[0-9]+") || dahiraPhoneNumber.length() != 9 || !checkPrefix(dahiraPhoneNumber))) {
            editTextDahiraPhoneNumber.setError("Numero de telephone incorrect");
            editTextDahiraPhoneNumber.requestFocus();
            return true;
        }

        if (siege.isEmpty()) {
            editTextSiege.setError("Champ obligatoire");
            editTextSiege.requestFocus();
            return true;
        }

        if (totalAdiya.isEmpty()) {
            editTextSiege.setError("Non montant, entrer 0");
            editTextAdiya.requestFocus();
            return true;
        }
        else if (!isDouble(totalAdiya)){
            editTextAdiya.setText("Valeur adiya incorrecte");
            editTextAdiya.requestFocus();
            return true;
        }

        if (totalSass.isEmpty()) {
            editTextSiege.setError("Non montant, entrer 0");
            editTextSass.requestFocus();
            return true;
        }
        else if (!isDouble(totalSass)){
            editTextSass.setText("Valeur sass incorrecte");
            editTextSass.requestFocus();
            return true;
        }

        if (totalSocial.isEmpty()) {
            editTextSiege.setError("Non montant, entrer 0");
            editTextSass.requestFocus();
            return true;
        }
        else if (!isDouble(totalSocial)){
            editTextSocial.setText("Valeur sociale incorrecte");
            editTextSocial.requestFocus();
            return true;
        }

        return false;
    }

    public static boolean hasValidationErrorsSearch(String phoneNumber, EditText editTextPhoneNumber,
                                                    String email, EditText editTextEmail) {

        if (phoneNumber.isEmpty() && email.isEmpty()) {
            editTextPhoneNumber.setError("Entrer un numero ou une adresse email");
            editTextPhoneNumber.requestFocus();
            return true;
        }

        if(!phoneNumber.isEmpty() && (!phoneNumber.matches("[0-9]+") || phoneNumber.length() != 9 || !checkPrefix(phoneNumber))) {
            editTextPhoneNumber.setError("Numero de telephone incorrect");
            editTextPhoneNumber.requestFocus();
            return true;
        }

        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Adresse email incorrect");
            editTextEmail.requestFocus();
            return true;
        }

        return false;
    }

    public static boolean hasValidationErrors(String name, EditText editTextName, String phoneNumber,
                                        EditText editTextPhoneNumber) {

        if (name.isEmpty() && phoneNumber.isEmpty()) {
            editTextName.setError("Entrer un nom ou un numero de telephone!");
            editTextName.requestFocus();
            return true;
        }

        if(!phoneNumber.isEmpty() && (!phoneNumber.matches("[0-9]+") || phoneNumber.length() != 9 || !checkPrefix(phoneNumber))) {
            editTextPhoneNumber.setError("Numero de telephone incorrect");
            editTextPhoneNumber.requestFocus();
            return true;
        }

        return false;
    }

    public static boolean checkPrefix(String str){
        String prefix = str.substring(0,2);
        boolean validatePrefix;
        switch(prefix){
            case "70":
                validatePrefix = true;
                break;
            case "76":
                validatePrefix = true;
                break;
            case "77":
                validatePrefix = true;
                break;
            case "78":
                validatePrefix = true;
                break;
            default:
                validatePrefix = false;
                break;
        }

        return validatePrefix;
    }

    public static boolean isDouble(String str){
        str = str.replace(",", ".");
        double value;
        try {
            value = Double.parseDouble(str);
            return true;
            // it means it is double
        } catch (Exception e1) {
            // this means it is not double
            e1.printStackTrace();
            return false;
        }
    }

    public static boolean isOnline(String email){
        final boolean[] connected = new boolean[1];
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                @Override
                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                    if (task.isSuccessful()) {
                        connected[0] = true;
                    } else {
                        connected[0] = false;
                    }
                }
        });

        return connected[0];
    }

    public static boolean isEmailExist(String email){
        final boolean[] isEmailExist = new boolean[1];
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {

                        isEmailExist[0] = task.getResult().getSignInMethods().isEmpty();

                        if (isEmailExist[0]) {
                            Log.e("TAG", "Is New User!");
                        } else {
                            Log.e("TAG", "Is Old User!");
                        }

                    }
                });
        return isEmailExist[0];
    }
}
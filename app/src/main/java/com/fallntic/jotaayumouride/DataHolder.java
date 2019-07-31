package com.fallntic.jotaayumouride;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class DataHolder {

    public static String dahiraID;
    public static String userID;
    public static String actionSelected = "";
    public static String displayDahira = "";
    public static String typeOfContribution = "";
    public static String loadEvent = "";

    /*Set to null one more time in class:
     *      ListAnnouncementActivity line 103
     *      ListExpenseActivity line 139
     */
    public static String notificationTitle = null;
    public static String notificationBody = null;

    public static UploadImage uploadImages = null;

    public static boolean boolAddToDahira;

    public static User onlineUser = null;
    public static User selectedUser = null;
    public static Dahira dahira = null;
    public static Event event = null;
    public static Adiya adiya = null;
    public static Sass sass = null;
    public static Social social = null;
    public static ObjNotification objNotification = null;
    public static Announcement announcement = null;
    public static Expense expense = null;

    public static int indexOnlineUser = -1;
    public static int indexSelectedUser = -1;
    public static int indexEventSelected;
    public static int indexAnnouncementSelected;
    public static int indexExpenseSelected;

    public static ProgressDialog progressDialog;

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

    public static void showImage(Context context, String child1, String child2,
                                 ImageView imageView) {

        FirebaseStorage firebaseStorage;
        firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference;
        storageReference = firebaseStorage.getReference();
        StorageReference imageReference = storageReference.child(child1).child(child2);
        // Download directly from StorageReference using Glide
        GlideApp.with(context)
                .load(imageReference)
                .placeholder(R.drawable.logo_dahira)
                .centerCrop()
                .into(imageView);
    }

    public static void showImage(Context context, String child1, String child2,
                                 String child3, String child4, ImageView imageView) {

        FirebaseStorage firebaseStorage;
        firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference;
        storageReference = firebaseStorage.getReference();
        StorageReference imageReference = storageReference.child(child1).child(child2)
                .child(child3).child(child4);
        // Download directly from StorageReference using Glide
        GlideApp.with(context)
                .load(imageReference)
                .placeholder(R.drawable.image_loading)
                .centerCrop()
                .into(imageView);
    }

    public static void showImage(Context context, String child1,
                                 String child2, CircleImageView imageView) {

        FirebaseStorage firebaseStorage;
        firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference;
        storageReference = firebaseStorage.getReference();
        StorageReference imageReference = storageReference.child(child1).child(child2);
        // Download directly from StorageReference using Glide
        GlideApp.with(context)
                .load(imageReference)
                .centerCrop()
                .into(imageView);
    }

    public static void toastMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showProgressDialog(Context context, String str) {
        dismissProgressDialog();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(str);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public static void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public static void logout(Context context) {
        typeOfContribution = "";
        onlineUser = null;
        selectedUser = null;
        dahira = null;
        event = null;
        announcement = null;
        expense = null;
        indexOnlineUser = -1;
        indexSelectedUser = -1;
        actionSelected = "";
        FirebaseAuth.getInstance().signOut();
        context.startActivity(new Intent(context, MainActivity.class));
    }

    public static void showAlertDialog(final Context context, String message) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.alert_dialog, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        final TextView textViewAlertDialog = dialogView.findViewById(R.id.textView_alertDialog);
        final Button buttonAlertDialog = dialogView.findViewById(R.id.button_dialog);

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

    public static void showAlertDialog(final Context context, String message, final Intent intent) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.alert_dialog, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        final TextView textViewAlertDialog = dialogView.findViewById(R.id.textView_alertDialog);
        final Button buttonAlertDialog = dialogView.findViewById(R.id.button_dialog);

        textViewAlertDialog.setText(message);
        final AlertDialog alertDialogMessage = dialogBuilder.create();
        alertDialogMessage.show();

        buttonAlertDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(intent);
                alertDialogMessage.dismiss();
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

        if (!phoneNumber.isEmpty() && (!phoneNumber.matches("[0-9]+") ||
                phoneNumber.length() != 9 || !checkPrefix(phoneNumber))) {
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

        if (!dahiraPhoneNumber.isEmpty() && (!dahiraPhoneNumber.matches("[0-9]+") ||
                dahiraPhoneNumber.length() != 9 || !checkPrefix(dahiraPhoneNumber))) {
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
        } else if (!isDouble(totalAdiya)) {
            editTextAdiya.setText("Valeur listAdiya incorrecte");
            editTextAdiya.requestFocus();
            return true;
        }

        if (totalSass.isEmpty()) {
            editTextSiege.setError("Non montant, entrer 0");
            editTextSass.requestFocus();
            return true;
        } else if (!isDouble(totalSass)) {
            editTextSass.setText("Valeur sass incorrecte");
            editTextSass.requestFocus();
            return true;
        }

        if (totalSocial.isEmpty()) {
            editTextSiege.setError("Non montant, entrer 0");
            editTextSass.requestFocus();
            return true;
        } else if (!isDouble(totalSocial)) {
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

        if (!phoneNumber.isEmpty() && (!phoneNumber.matches("[0-9]+") ||
                phoneNumber.length() != 9 || !checkPrefix(phoneNumber))) {
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

        if (!phoneNumber.isEmpty() && (!phoneNumber.matches("[0-9]+") ||
                phoneNumber.length() != 9 || !checkPrefix(phoneNumber))) {
            editTextPhoneNumber.setError("Numero de telephone incorrect");
            editTextPhoneNumber.requestFocus();
            return true;
        }

        return false;
    }

    public static boolean checkPrefix(String str) {
        String prefix = str.substring(0, 2);
        boolean validatePrefix;
        switch (prefix) {
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

    public static boolean isDouble(String str) {
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

    public static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("dd/MM/yyyy");
        String strDate = mdformat.format(calendar.getTime());
        return strDate;
    }

    public static void getDate(Context context, final EditText editText) {
        int mYear, mMonth, mDay;
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        String mDate = dayOfMonth + "/" + monthOfYear + "/" + year;
                        editText.setText(mDate);
                    }
                }, mYear, mMonth, mDay);

        datePickerDialog.show();
    }

    public static void getTime(Context context, final EditText editText, String title) {
        TimePickerDialog timePickerDialog;
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        timePickerDialog = new TimePickerDialog(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {

                        editText.setText(String.format(hourOfDay + ":" + minutes));
                    }
                }, currentHour, currentMinute, true);
        timePickerDialog.setTitle(title);
        timePickerDialog.show();
    }

    public static void createNewCollection(final Context context, final String collectionName,
                                           String documentName, Object data) {
        FirebaseFirestore.getInstance().collection(collectionName).document(documentName)
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "New collection " + collectionName + " set successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error initContributions function line 351");
                    }
                });
        actionSelected = "";
    }

    public static void updateDocument(final Context context, final String collectionName,
                                      String documentID, String field, String value) {
        showProgressDialog(context, "Mis a jour " + collectionName + " en cours ...");
        FirebaseFirestore.getInstance().collection(collectionName).document(documentID)
                .update(field, value)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dismissProgressDialog();
                        Log.d(TAG, collectionName + "updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissProgressDialog();
                        Log.d(TAG, "Error updated " + collectionName);
                    }
                });
    }

    public static void updateDocument(final Context context, final String collectionName, String documentID,
                                      String field, List<String> listValue) {
        showProgressDialog(context, "Mis a jour " + collectionName + " en cours ...");
        FirebaseFirestore.getInstance().collection(collectionName).document(documentID)
                .update(field, listValue)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dismissProgressDialog();
                        Log.d(TAG, collectionName + " updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissProgressDialog();
                        Log.d(TAG, "Error updated " + collectionName);
                    }
                });
    }


    public static void deleteDocument(final Context context, String collectionName, String documentID) {
        FirebaseFirestore.getInstance().collection(collectionName).document(documentID).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //toastMessage(context, task.getException().getMessage());
                        } else {
                            //toastMessage(context, task.getException().getMessage());
                        }
                    }
                });
    }

    public static void saveContribution(final Context context, final String nameCollection,
                                        final String documentID, final String value, final String mDate) {

        showProgressDialog(context, "Enregistrement " + nameCollection + " en cours ...");
        FirebaseFirestore.getInstance().collection(nameCollection).document(documentID).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        dismissProgressDialog();
                        if (documentSnapshot.exists()) {
                            if (nameCollection.equals("adiya")) {
                                adiya.getListDahiraID().add(dahira.getDahiraID());
                                adiya.getListUserName().add(onlineUser.getUserName());
                                adiya.getListAdiya().add(value);
                                adiya.getListDate().add(mDate);
                                updateContribution(context, "adiya", documentID, value);
                            } else if (nameCollection.equals("sass")) {
                                sass.getListDahiraID().add(dahira.getDahiraID());
                                sass.getListSass().add(value);
                                sass.getListDate().add(mDate);
                                sass.getListUserName().add(onlineUser.getUserName());
                                updateContribution(context, "sass", documentID, value);
                            } else if (nameCollection.equals("social")) {
                                social.getListSocial().add(value);
                                social.getListDahiraID().add(dahira.getDahiraID());
                                social.getListDate().add(mDate);
                                social.getListUserName().add(onlineUser.getUserName());
                                updateContribution(context, "social", documentID, value);
                            }

                            final Intent intent = new Intent(context, UserInfoActivity.class);
                            showAlertDialog(context, " enregistrement reussi", intent);
                            Log.d(TAG, "Collection " + nameCollection + " updated");
                        } else {
                            //Create new collections listAdiya, sass and social
                            List<String> listDahiraID = new ArrayList<String>();
                            listDahiraID.add(dahira.getDahiraID());
                            List<String> listDate = new ArrayList<String>();
                            listDate.add(mDate);
                            List<String> listUserName = new ArrayList<String>();
                            listUserName.add(onlineUser.getUserName());

                            if (nameCollection.equals("adiya")) {
                                List<String> listAdiya = new ArrayList<String>();
                                listAdiya.add(value);
                                Adiya adiya = new Adiya(listDahiraID, listDate, listAdiya, listUserName);
                                createNewCollection(context, "adiya", documentID, adiya);
                            }
                            if (nameCollection.equals("sass")) {
                                List<String> listSass = new ArrayList<String>();
                                listSass.add(value);
                                Sass sass = new Sass(listDahiraID, listDate, listSass, listUserName);
                                createNewCollection(context, "sass", documentID, sass);
                            }
                            if (nameCollection.equals("social")) {
                                List<String> listSocial = new ArrayList<String>();
                                listSocial.add(value);
                                Social social = new Social(listDahiraID, listDate, listSocial, listUserName);
                                createNewCollection(context, "social", documentID, social);
                            }

                            Log.d(TAG, "New collection " + nameCollection + " created");
                        }
                        final Intent intent = new Intent(context, UserInfoActivity.class);
                        showAlertDialog(context, nameCollection + " ajoute avec succe!", intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissProgressDialog();
                        Log.d(TAG, e.toString());
                    }
                });
    }

    public static void updateContribution(final Context context, final String collectionName,
                                          String documentID, String str_value) {

        showProgressDialog(context, "Enregistrement " + collectionName + " en cours ...");

        //Update totalAdiya dahira
        final double value = Double.parseDouble(str_value);

        if (collectionName.equals("adiya")) {
            FirebaseFirestore.getInstance().collection(collectionName).document(documentID)
                    .update("listDahiraID", adiya.getListDahiraID(),
                            "listDate", adiya.getListDate(),
                            "listAdiya", adiya.getListAdiya(),
                            "listUserName", adiya.getListUserName())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dismissProgressDialog();

                            double totalAdiyaDahira;
                            if (boolAddToDahira)
                                totalAdiyaDahira = Double.parseDouble(dahira.getTotalAdiya()) + value;
                            else
                                totalAdiyaDahira = Double.parseDouble(dahira.getTotalAdiya()) - value;

                            dahira.setTotalAdiya(Double.toString(totalAdiyaDahira));

                            updateDocument(context, "dahiras", dahira.getDahiraID(),
                                    "totalAdiya", dahira.getTotalAdiya());

                            boolAddToDahira = false;

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dismissProgressDialog();
                        }
                    });
        } else if (collectionName.equals("sass")) {
            FirebaseFirestore.getInstance().collection(collectionName).document(documentID)
                    .update("listDahiraID", sass.getListDahiraID(),
                            "listDate", sass.getListDate(),
                            "listSass", sass.getListSass(),
                            "listUserName", sass.getListUserName())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dismissProgressDialog();

                            double totalAdiyaDahira;
                            if (boolAddToDahira)
                                totalAdiyaDahira = Double.parseDouble(dahira.getTotalAdiya()) + value;
                            else
                                totalAdiyaDahira = Double.parseDouble(dahira.getTotalAdiya()) - value;

                            dahira.setTotalAdiya(Double.toString(totalAdiyaDahira));

                            updateDocument(context, "dahiras", dahira.getDahiraID(),
                                    "totalSass", dahira.getTotalSass());

                            boolAddToDahira = false;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dismissProgressDialog();
                        }
                    });

        } else if (collectionName.equals("social")) {
            FirebaseFirestore.getInstance().collection(collectionName).document(documentID)
                    .update("listDahiraID", social.getListDahiraID(),
                            "listDate", social.getListDate(),
                            "listSocial", social.getListSocial(),
                            "listUserName", social.getListUserName())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dismissProgressDialog();

                            double totalAdiyaDahira;
                            if (boolAddToDahira)
                                totalAdiyaDahira = Double.parseDouble(dahira.getTotalAdiya()) + value;
                            else
                                totalAdiyaDahira = Double.parseDouble(dahira.getTotalAdiya()) - value;

                            dahira.setTotalAdiya(Double.toString(totalAdiyaDahira));

                            updateDocument(context, "dahiras", dahira.getDahiraID(),
                                    "totalSocial", dahira.getTotalSocial());

                            boolAddToDahira = false;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dismissProgressDialog();
                        }
                    });
        }
    }

    public static void call(Context context, Activity activity, String phoneNumber) {
        try {
            if (Build.VERSION.SDK_INT > 22) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CALL_PHONE}, 101);
                    return;
                }

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                context.startActivity(callIntent);

            } else {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                context.startActivity(callIntent);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static boolean isOnline(String email) {
        final boolean[] connected = new boolean[1];
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        connected[0] = task.isSuccessful();
                    }
                });

        return connected[0];
    }

    public static boolean isEmailExist(String email) {
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
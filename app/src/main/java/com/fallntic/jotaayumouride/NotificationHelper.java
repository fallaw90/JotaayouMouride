package com.fallntic.jotaayumouride;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.notificationBody;
import static com.fallntic.jotaayumouride.DataHolder.notificationTitle;
import static com.fallntic.jotaayumouride.DataHolder.objNotification;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.NotificationActivity.CHANNEL_ID;

public class NotificationHelper extends IntentService {
    public static final String TAG = "NotificationHelper";


    public static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public static String userID = mAuth.getCurrentUser().getUid();
    public static String dahiraID;

    public NotificationHelper(String name) {
        super(name);
    }


    public static void sendNotification(final Context context, final User user,
                                        final String title, final String message) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://jotaayumourid.firebaseapp.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Api api = retrofit.create(Api.class);

        retrofit2.Call<ResponseBody> call = api.sendNotification(user.getTokenID(), title, message);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {

                    saveNotificationToFirestore(context, user, title, message);

                    Log.d(TAG, response.body().string());
                    //toastMessage(context, "Notification envoyee");
                    //Toast.makeText(context, response.body().string(), Toast.LENGTH_LONG).show();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

    //Used in sendNotification function
    //Class NotificationHelper line 75
    public static void saveNotificationToFirestore(final Context context, User user,
                                                   String title, String message) {

        String notificationID = FirebaseFirestore.getInstance()
                .collection("notifications").document().getId();

        objNotification = new ObjNotification(notificationID, user.getUserID(),
                dahira.getDahiraID(), title, message);

        FirebaseFirestore.getInstance().collection("notifications")
                .document(notificationID).set(objNotification)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dismissProgressDialog();
                        //toastMessage("Utilisateur enregistre avec succes");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissProgressDialog();
                        toastMessage(context, "Error adding user!");
                        Log.d(TAG, e.toString());
                    }
                });
    }

    //Used in class MyFirebaseMessagingService
    //Function onMessageReceived line 23
    public static void displayNotification(Context context, String title, String message) {

        //getOnlineUser(context, userID);

        notificationTitle = title;
        notificationBody = message;

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);

        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                uniqueInt,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap largeIcon = BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.logo_dahira);
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_one)
                .setContentTitle(title)
                .setContentText(message)
                .setLargeIcon(largeIcon)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setSummaryText("Summary Text"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(Color.GREEN)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(context);
        mNotificationMgr.notify(0, notification);
    }

    /*
     * Used on:
     *      CreateAnnouncementActivity line 177
     *      CreateExpenseActivity line 169
     */
    public static void sendNotificationToSpecificUsers(final Context context, final String title, final String message) {

        FirebaseFirestore.getInstance().collection("users").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        dismissProgressDialog();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : list) {

                                User user = documentSnapshot.toObject(User.class);

                                if (user.getListDahiraID().contains(dahira.getDahiraID())) {
                                    sendNotification(context, user, title, message);
                                }

                            }
                        }
                    }
                });
    }

    public static void sendNotificationToAllUsers(final Context context, final String title, final String message) {

        FirebaseFirestore.getInstance().collection("users").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        dismissProgressDialog();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : list) {
                                User user = documentSnapshot.toObject(User.class);
                                sendNotification(context, user, title, message);
                            }
                        }
                    }
                });
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Log.i(TAG, "ENTERED onHandleIntent");
        displayNotification(this, notificationTitle, notificationBody);
    }
}

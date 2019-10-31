package com.fallntic.jotaayumouride.Utility;

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

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.fallntic.jotaayumouride.MainActivity;
import com.fallntic.jotaayumouride.Model.Api;
import com.fallntic.jotaayumouride.Model.ObjNotification;
import com.fallntic.jotaayumouride.Model.User;
import com.fallntic.jotaayumouride.R;
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

import static com.fallntic.jotaayumouride.MainActivity.CHANNEL_ID;
import static com.fallntic.jotaayumouride.Utility.DataHolder.dahira;
import static com.fallntic.jotaayumouride.Utility.DataHolder.dismissProgressDialog;

public class NotificationHelper extends IntentService {
    public static final String TAG = "NotificationHelper";


    public static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public static String userID = mAuth.getCurrentUser().getUid();
    public static String dahiraID;

    public NotificationHelper(String name) {
        super(name);
    }

    public static void sendNotification(final Context context, final User user, final ObjNotification objNotification) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://jotaayumourid.firebaseapp.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Api api = retrofit.create(Api.class);
        retrofit2.Call<ResponseBody> call = api.sendNotification(user.getTokenID(), objNotification.getTitle(), objNotification.getMessage());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    Log.d(TAG, response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    //Used in class MyFirebaseMessagingService
    //Function onMessageReceived line 23
    public static void displayNotification(Context context, ObjNotification objNotification) {


        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("objNotification", objNotification);

        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                uniqueInt,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap largeIcon = BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.logo_dahira);
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_announcement)
                .setContentTitle(objNotification.getTitle())
                .setContentText(objNotification.getMessage())
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
    public static void sendNotificationToSpecificUsers(final Context context, final ObjNotification objNotification) {

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
                                    sendNotification(context, user, objNotification);
                                }

                            }
                        }
                    }
                });
    }

    public static void sendNotificationToAllUsers(final Context context, final ObjNotification objNotification) {

        FirebaseFirestore.getInstance().collection("users").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        dismissProgressDialog();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : list) {
                                User user = documentSnapshot.toObject(User.class);
                                sendNotification(context, user, objNotification);
                            }
                        }
                    }
                });
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Log.i(TAG, "ENTERED onHandleIntent");

        displayNotification(this, MyStaticVariables.objNotification);
    }


}

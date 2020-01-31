package com.fallntic.jotaayumouride.services;

import androidx.annotation.NonNull;

import com.fallntic.jotaayumouride.model.ObjNotification;
import com.fallntic.jotaayumouride.notifications.FirebaseNotificationHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static com.fallntic.jotaayumouride.utility.MyStaticVariables.dahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.objNotification;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.onlineUser;

@SuppressWarnings("unused")
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private final String TAG = "MyFirebaseMessagingService";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getNotification() != null && objNotification != null) {


            String title = remoteMessage.getNotification().getTitle();
            String note = remoteMessage.getNotification().getBody();

            objNotification = new ObjNotification(objNotification.getNotificationID(),
                    onlineUser.getUserID(), dahira.getDahiraID(), title, note);

            //Intent intent = new Intent(MyFirebaseMessagingService.this, FirebaseNotificationHelper.class);
            //intent.putExtra("objNotification", objNotification);

            FirebaseNotificationHelper.displayNotification(MyFirebaseMessagingService.this, objNotification);
        }
    }
}

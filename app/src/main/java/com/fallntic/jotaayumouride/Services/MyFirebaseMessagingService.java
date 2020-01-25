package com.fallntic.jotaayumouride.Services;

import com.fallntic.jotaayumouride.Model.ObjNotification;
import com.fallntic.jotaayumouride.Notifications.FirebaseNotificationHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.dahira;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.objNotification;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.onlineUser;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private final String TAG = "MyFirebaseMessagingService";
    String dahiraID;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
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

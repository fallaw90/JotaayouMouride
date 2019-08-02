package com.fallntic.jotaayumouride;

import com.fallntic.jotaayumouride.Utility.MyStaticVariables;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private final String TAG = "MyFirebaseMessagingService";
    String dahiraID;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getNotification() != null) {


            String title = remoteMessage.getNotification().getTitle();
            String note = remoteMessage.getNotification().getBody();

            MyStaticVariables.objNotification = new ObjNotification(MyStaticVariables.objNotification.getNotificationID(),
                    onlineUser.getUserID(), dahira.getDahiraID(), title, note);

            //Intent intent = new Intent(MyFirebaseMessagingService.this, NotificationHelper.class);
            //intent.putExtra("objNotification", objNotification);

            NotificationHelper.displayNotification(MyFirebaseMessagingService.this, MyStaticVariables.objNotification);
        }
    }
}

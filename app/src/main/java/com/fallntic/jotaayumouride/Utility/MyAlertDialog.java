package com.fallntic.jotaayumouride.utility;

import android.app.Activity;
import android.content.DialogInterface;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

@SuppressWarnings("unused")
class MyAlertDialog {

    private static final String TAG = MyAlertDialog.class.getSimpleName();

    private final Activity mActivity;

    private MyAlertDialog(Activity activity) {
        mActivity = activity;
    }


    public void showDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.e(TAG, "showDialog : onClick");
            }
        });
        builder.create().show();
    }
}

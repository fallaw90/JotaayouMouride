package com.fallntic.jotaayumouride;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.logout;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;


public class MainActivity extends AppCompatActivity {
    private BroadcastReceiver myReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (!isConnected(this)){
            Intent intent = new Intent(this, LoginActivity.class);
            logout();
            showAlertDialog(this,"Oops! Pas de connexion, verifier votre connexion internet puis reesayez SVP", intent);
        }

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try{
            unregisterReceiver(myReceiver);
        } catch (Exception e){
            // already unregistered
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            unregisterReceiver(myReceiver);
        } catch (Exception e){
            // already unregistered
        }
    }

    public void broadcastIntent() {
        registerReceiver(myReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public void toastMessage(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
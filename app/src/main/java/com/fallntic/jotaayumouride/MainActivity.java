package com.fallntic.jotaayumouride;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.fallntic.jotaayumouride.Utility.MyStaticVariables;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fallntic.jotaayumouride.DataHolder.actionSelected;
import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.loadEvent;
import static com.fallntic.jotaayumouride.DataHolder.logout;
import static com.fallntic.jotaayumouride.DataHolder.notificationBody;
import static com.fallntic.jotaayumouride.DataHolder.notificationTitle;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.showImage;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.DataHolder.userID;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DrawerMenu {
    public static final String TAG = "MainActivity";
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private View navHeader;
    private CircleImageView navImageView;
    private TextView textViewNavUserName;
    private TextView textViewNavEmail;
    private TextView textViewOnline;
    public static RelativeLayout relativeLayoutProgressBar;
    public static ProgressBar progressBar;
    private TextView textViewOffline;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public static final String CHANNEL_ID = "jotaayou_mouride";
    public static final String CHANNEL_Name = "Jotaayou Mouride";
    public static final String CHANNEL_DESC = "Jotaayou Mouride Notifications";

    private ObjNotification objNotification;

    public static void getDahira(final Context context, final ObjNotification objNotification) {
        relativeLayoutProgressBar.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        FirebaseFirestore.getInstance().collection("dahiras")
                .whereEqualTo("dahiraID", objNotification.getDahiraID()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        relativeLayoutProgressBar.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                dahira = documentSnapshot.toObject(Dahira.class);
                                break;
                            }

                            if (objNotification.getTitle().equals(MyStaticVariables.TITLE_ANNOUNCEMENT_NOTIFICATION)) {
                                context.startActivity(new Intent(context, ShowAnnouncementActivity.class));

                            } else if (objNotification.getTitle().equals(MyStaticVariables.TITLE_EXPENSE_NOTIFICATION)) {
                                context.startActivity(new Intent(context, ListExpenseActivity.class));
                            }
                        }
                        Log.d(TAG, "Dahira downloaded");
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_menu, menu);

        MenuItem itemLogin;
        itemLogin = menu.findItem(R.id.login);

        if (mAuth.getCurrentUser() == null) {
            itemLogin.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_profile:
                startActivity(new Intent(this, ProfileActivity.class));
                break;

            case R.id.nav_displayMyDahira:
                DataHolder.displayDahira = "myDahira";
                startActivity(new Intent(this, ListDahiraActivity.class));
                break;

            case R.id.nav_addDahira:
                startActivity(new Intent(this, CreateDahiraActivity.class));
                break;

            case R.id.nav_displayAllDahira:
                DataHolder.displayDahira = "allDahira";
                startActivity(new Intent(this, ListDahiraActivity.class));
                break;

            case R.id.nav_displayAllEvent:
                DataHolder.loadEvent = "allEvents";
                startActivity(new Intent(this, ListEventActivity.class));
                break;

            case R.id.nav_searchDahira:
                actionSelected = "searchDahira";
                startActivity(new Intent(this, ListDahiraActivity.class));
                break;

            case R.id.nav_setting:
                startActivity(new Intent(this, SettingProfileActivity.class));
                break;

            case R.id.nav_logout:
                toastMessage(this, "Logged out");
                logout(this);
                finish();
                startActivity(new Intent(this, MainActivity.class));
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.login:
                startActivity(new Intent(this, LoginPhoneActivity.class));
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setSubtitle("Jotaayou Mouride");
        setSupportActionBar(toolbar);

        //********************** Drawer Menu ***********************
        setDrawerMenu();
        //**********************************************************

        relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        progressBar = findViewById(R.id.progressBar);
        relativeLayoutProgressBar.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        if (!isConnected(this)) {
            toastMessage(getApplicationContext(), "Oops! Pas de connexion, " +
                    "verifier votre connexion internet puis reesayez SVP");
            startActivity(new Intent(this, LoginPhoneActivity.class));
            return;
        }

        //*********************************Notification************************************
        createChannel();
        objNotification = (ObjNotification) getIntent().getSerializableExtra("objNotification");
        //******************************************************************

        textViewOffline = findViewById(R.id.textView_notOnline);
        textViewOnline = findViewById(R.id.textView_online);

        if (mAuth.getCurrentUser() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            userID = mAuth.getCurrentUser().getUid();
            getOnlineUser(this, userID, objNotification);
            showImage(this, "profileImage", userID, navImageView);

            //Go to ListEvent when we hit the notification
            if (notificationTitle != null && notificationBody != null &&
                    notificationTitle.equals("Evénement à venir")) {
                loadEvent = "allEvents";
                finish();
                startActivity(new Intent(this, ListEventActivity.class));
            } else if (notificationTitle == null && notificationBody == null) {
                finish();
                startActivity(new Intent(this, ProfileActivity.class));
            }

            //textViewOnline.setVisibility(View.VISIBLE);
        } else {
            textViewOffline.setVisibility(View.VISIBLE);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

            startActivity(new Intent(this, LoginPhoneActivity.class));
        }
    }

    @Override
    public void hideMenuItem() {
        Menu nav_Menu = navigationView.getMenu();
        nav_Menu.findItem(R.id.nav_setting).setTitle("Modifier mon profil");

        nav_Menu.findItem(R.id.nav_displayUsers).setVisible(false);
        nav_Menu.findItem(R.id.nav_addUser).setVisible(false);
        nav_Menu.findItem(R.id.nav_searchUser).setVisible(false);

        nav_Menu.findItem(R.id.nav_finance).setVisible(false);
        nav_Menu.findItem(R.id.nav_gallery).setVisible(false);
        nav_Menu.findItem(R.id.nav_contact).setVisible(false);
        nav_Menu.findItem(R.id.nav_addAnnouncement).setVisible(false);
        nav_Menu.findItem(R.id.nav_displayAnnouncement).setVisible(false);
        nav_Menu.findItem(R.id.nav_addEvent).setVisible(false);
        nav_Menu.findItem(R.id.nav_displayEvent).setVisible(false);
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_Name,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void setDrawerMenu() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
        navHeader = navigationView.getHeaderView(0);
        navImageView = navHeader.findViewById(R.id.nav_imageView);
        textViewNavUserName = navHeader.findViewById(R.id.textView_navUserName);
        textViewNavEmail = navHeader.findViewById(R.id.textView_navEmail);
        toggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setCheckedItem(R.id.nav_home);
        hideMenuItem();
    }

    public void getOnlineUser(final Context context, String userID, final ObjNotification objNotification) {
        relativeLayoutProgressBar.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        if (objNotification != null) {
            userID = objNotification.getUserID();
        }

        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("userID", userID).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        relativeLayoutProgressBar.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            onlineUser = documentSnapshot.toObject(User.class);

                            if (objNotification != null) {
                                getDahira(context, objNotification);
                                break;
                            } else
                                break;
                        }
                        textViewNavUserName.setText(onlineUser.getUserName());
                        textViewNavEmail.setText(onlineUser.getEmail());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        relativeLayoutProgressBar.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                    }
                });

        dismissProgressDialog();
    }


}
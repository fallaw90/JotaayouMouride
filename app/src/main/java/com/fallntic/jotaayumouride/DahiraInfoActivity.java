package com.fallntic.jotaayumouride;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.fallntic.jotaayumouride.Utility.MyStaticFunctions;
import com.fallntic.jotaayumouride.Utility.MyStaticVariables;
import com.google.android.material.navigation.NavigationView;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fallntic.jotaayumouride.DataHolder.actionSelected;
import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.event;
import static com.fallntic.jotaayumouride.DataHolder.expense;
import static com.fallntic.jotaayumouride.DataHolder.indexOnlineUser;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.loadEvent;
import static com.fallntic.jotaayumouride.DataHolder.logout;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.showImage;
import static com.fallntic.jotaayumouride.DataHolder.userID;

public class DahiraInfoActivity extends AppCompatActivity implements View.OnClickListener,
        DrawerMenu, NavigationView.OnNavigationItemSelectedListener {
    private final String TAG = "DahiraInfoActivity";

    private TextView textViewDahiraName, textViewDieuwrine, textViewSiege, textViewtotalMembers,
            textViewTotalAdiya, textViewTotalSass, textViewTotalSocial, textViewPhoneNumber;

    private ImageView imageView;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private View navHeader;
    private CircleImageView navImageView;
    private TextView textViewNavUserName;
    private TextView textViewNavEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dahira_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Mon Dahira");
        setSupportActionBar(toolbar);

        if (!isConnected(this)) {
            finish();
            Intent intent = new Intent(this, LoginActivity.class);
            showAlertDialog(this, "Oops! Pas de connexion, verifier votre connexion internet puis reesayez SVP", intent);
        }

        init();


        //********************** Drawer Menu ***************************************
        setDrawerMenu();
        //**************************************************************************


        //**********************Get list song ***************************
        MyStaticFunctions.getSongList(this);
    }

    private void init() {
        textViewDahiraName = findViewById(R.id.textView_dahiraName);
        textViewDieuwrine = findViewById(R.id.textView_dieuwrine);
        textViewSiege = findViewById(R.id.textView_siege);
        textViewPhoneNumber = findViewById(R.id.textView_dahiraPhoneNumber);
        textViewTotalAdiya = findViewById(R.id.textView_totalAdiya);
        textViewTotalSass = findViewById(R.id.textView_totalSass);
        textViewTotalSocial = findViewById(R.id.textView_totalSocial);
        textViewtotalMembers = findViewById(R.id.textView_totalMembers);
        imageView = findViewById(R.id.imageView);
        showImage(this, "logoDahira", dahira.getDahiraID(), imageView);

        textViewDahiraName.setText("Dahira " + dahira.getDahiraName());
        textViewDieuwrine.setText("Dieuwrine: " + dahira.getDieuwrine());
        textViewSiege.setText("Siege: " + dahira.getSiege());
        textViewPhoneNumber.setText("Telephone: " + dahira.getDahiraPhoneNumber());
        textViewtotalMembers.setText("Nombre de participant: " + dahira.getTotalMember());
        textViewTotalAdiya.setText("Total Adiya dans la caisse: " + dahira.getTotalAdiya() + " FCFA");
        textViewTotalSass.setText("Total Sass dans la caisse: " + dahira.getTotalSass() + " FCFA");
        textViewTotalSocial.setText("Total Social dans la caisse: " + dahira.getTotalSocial() + " FCFA");


        if (!onlineUser.getListDahiraID().contains(dahira.getDahiraID())) {
            textViewTotalAdiya.setVisibility(View.GONE);
            textViewTotalSass.setVisibility(View.GONE);
            textViewTotalSocial.setVisibility(View.GONE);
        }

        findViewById(R.id.button_back).setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MyStaticVariables.displayDahira = "myDahira";
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_back:
                finish();
                //startActivity(new Intent(this, ListDahiraActivity.class));
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callDahira();
            }
        }
    }

    public void callDahira() {
        try {
            if (Build.VERSION.SDK_INT > 22) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 101);
                    return;
                }

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + dahira.getDahiraPhoneNumber()));
                startActivity(callIntent);

            } else {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + dahira.getDahiraPhoneNumber()));
                startActivity(callIntent);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_menu, menu);

        MenuItem iconBack;
        iconBack = menu.findItem(R.id.icon_back);

        iconBack.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item))
            return true;

        switch (item.getItemId()) {
            case R.id.icon_back:
                startActivity(new Intent(DahiraInfoActivity.this, ListDahiraActivity.class));
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public static void chooseMethodAnnouncement(final Context context) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.dialog_record_audio, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        final ImageView imageViewRecord = dialogView.findViewById(R.id.imageView_record);
        final ImageView imageViewWrite = dialogView.findViewById(R.id.imageView_write);
        final Button buttonCancel = dialogView.findViewById(R.id.button_cancel);

        dialogBuilder.setTitle("Enregistrer une annonce");
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        imageViewRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, RecordAudioActivity.class));
                alertDialog.dismiss();
            }
        });

        imageViewWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionSelected = "addNewAnnouncement";
                context.startActivity(new Intent(context, AnnouncementActivity.class));
                alertDialog.dismiss();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        showImage(this, "profileImage", userID, navImageView);
        textViewNavUserName.setText(onlineUser.getUserName());
        textViewNavEmail.setText(onlineUser.getEmail());
        navigationView.setCheckedItem(R.id.nav_displayMyDahira);
        hideMenuItem();
    }

    public void hideMenuItem() {
        Menu nav_Menu = navigationView.getMenu();
        nav_Menu.findItem(R.id.nav_setting).setTitle("Modifier mon dahira");

        if (!onlineUser.getListDahiraID().contains(dahira.getDahiraID())) {
            nav_Menu.findItem(R.id.nav_displayUsers).setVisible(false);
            nav_Menu.findItem(R.id.nav_searchUser).setVisible(false);
            nav_Menu.findItem(R.id.nav_addUser).setVisible(false);
            nav_Menu.findItem(R.id.nav_displayAnnouncement).setVisible(false);
            nav_Menu.findItem(R.id.nav_addAnnouncement).setVisible(false);
            nav_Menu.findItem(R.id.nav_addExpense).setVisible(false);
            nav_Menu.findItem(R.id.nav_displayExpenses).setVisible(false);
            nav_Menu.findItem(R.id.nav_addEvent).setVisible(false);
            nav_Menu.findItem(R.id.nav_setting).setVisible(false);
            nav_Menu.findItem(R.id.nav_searchUser).setVisible(false);
        } else if (!onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur")) {
            nav_Menu.findItem(R.id.nav_setting).setVisible(false);
            nav_Menu.findItem(R.id.nav_addEvent).setVisible(false);
            nav_Menu.findItem(R.id.nav_addExpense).setVisible(false);
        }

        nav_Menu.findItem(R.id.nav_home).setVisible(false);
        nav_Menu.findItem(R.id.nav_displayMyDahira).setVisible(false);
        nav_Menu.findItem(R.id.nav_displayAllDahira).setVisible(false);
        nav_Menu.findItem(R.id.nav_addDahira).setVisible(false);
        nav_Menu.findItem(R.id.nav_searchDahira).setVisible(false);
        nav_Menu.findItem(R.id.nav_displayAllEvent).setVisible(false);
        nav_Menu.findItem(R.id.nav_callUser).setVisible(false);
        nav_Menu.findItem(R.id.nav_addContribution).setVisible(false);
        nav_Menu.findItem(R.id.nav_displayAdiya).setVisible(false);
        nav_Menu.findItem(R.id.nav_displaySass).setVisible(false);
        nav_Menu.findItem(R.id.nav_displaySocial).setVisible(false);
        nav_Menu.findItem(R.id.nav_video).setVisible(false);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.nav_home:
                startActivity(new Intent(this, MainActivity.class));
                break;

            case R.id.nav_profile:
                startActivity(new Intent(this, ProfileActivity.class));
                break;

            case R.id.nav_displayUsers:
                actionSelected = "";
                startActivity(new Intent(this, ListUserActivity.class));
                break;

            case R.id.nav_searchUser:
                actionSelected = "searchUser";
                MyStaticVariables.displayDahira = "allDahira";
                startActivity(new Intent(this, ListUserActivity.class));
                break;

            case R.id.nav_addUser:
                actionSelected = "addNewMember";
                startActivity(new Intent(this, ListUserActivity.class));
                break;

            case R.id.nav_displayMyDahira:
                MyStaticVariables.displayDahira = "myDahira";
                startActivity(new Intent(this, ListDahiraActivity.class));
                break;

            case R.id.nav_displayAllDahira:
                MyStaticVariables.displayDahira = "allDahira";
                startActivity(new Intent(this, ListDahiraActivity.class));
                break;

            case R.id.nav_addExpense:
                startActivity(new Intent(this, CreateExpenseActivity.class));
                break;

            case R.id.nav_displayExpenses:
                if (expense != null && !expense.getListPrice().isEmpty())
                    startActivity(new Intent(this, ListExpenseActivity.class));
                else
                    showAlertDialog(this, "La liste des depenses de votre dahira est vide!");
                break;

            case R.id.nav_addAnnouncement:
                chooseMethodAnnouncement(DahiraInfoActivity.this);
                break;

            case R.id.nav_displayAnnouncement:
                startActivity(new Intent(this, ShowAnnouncementActivity.class));
                //startActivity(new Intent(this, ListAnnouncementActivity.class));
                break;

            case R.id.nav_addEvent:
                startActivity(new Intent(this, CreateEventActivity.class));
                break;

            case R.id.nav_displayEvent:
                if (event == null || event.getListUserID().isEmpty())
                    showAlertDialog(this, "La liste des evenements est vide!");
                else
                    loadEvent = "myEvents";
                startActivity(new Intent(this, ListEventActivity.class));
                break;

            case R.id.nav_photo:
                startActivity(new Intent(this, ShowImagesActivity.class));
                break;

            case R.id.nav_audio:
                startActivity(new Intent(this, ShowSongsActivity.class));
                break;

            case R.id.nav_video:
                showAlertDialog(this, "Cette page est en cours de contruction." +
                        "Revenez plutard SVP.");
                break;

            case R.id.nav_callDahira:
                navigationView.setCheckedItem(R.id.nav_callDahira);
                callDahira();
                break;

            case R.id.nav_setting:
                startActivity(new Intent(this, UpdateDahiraActivity.class));
                break;

            case R.id.nav_logout:
                logout(this);
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}

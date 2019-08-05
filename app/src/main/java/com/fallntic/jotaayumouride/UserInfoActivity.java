package com.fallntic.jotaayumouride;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.fallntic.jotaayumouride.Utility.MyStaticVariables;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fallntic.jotaayumouride.DataHolder.actionSelected;
import static com.fallntic.jotaayumouride.DataHolder.adiya;
import static com.fallntic.jotaayumouride.DataHolder.call;
import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.event;
import static com.fallntic.jotaayumouride.DataHolder.expense;
import static com.fallntic.jotaayumouride.DataHolder.getCurrentDate;
import static com.fallntic.jotaayumouride.DataHolder.indexOnlineUser;
import static com.fallntic.jotaayumouride.DataHolder.indexSelectedUser;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.logout;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.sass;
import static com.fallntic.jotaayumouride.DataHolder.selectedUser;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.showImage;
import static com.fallntic.jotaayumouride.DataHolder.social;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.DataHolder.typeOfContribution;
import static com.fallntic.jotaayumouride.DataHolder.userID;

public class UserInfoActivity extends AppCompatActivity implements View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener {
    private final String TAG = "UserInfoActivity";

    private TextView textViewName;
    private TextView textViewDahiraName;
    private TextView textViewAdress;
    private TextView textViewPhoneNumber;
    private TextView textViewEmail;
    private TextView textViewRole;
    private TextView textViewCommission;
    private TextView textViewAdiya;
    private TextView textViewSass;
    private TextView textViewSocial;
    private ImageView imageView;
    private LinearLayout linearLayoutAdiya;
    private LinearLayout linearLayoutSass;
    private LinearLayout linearLayoutSocial;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    boolean boolAdiya = false, boolSass = false, boolSocial = false;

    private String mDate = getCurrentDate();

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
        setContentView(R.layout.activity_user_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Info du membre");
        setSupportActionBar(toolbar);

        //********************** Drawer Menu **************************
        setDrawerMenu();
        //*************************************************************

        if (!isConnected(this)) {
            finish();
            Intent intent = new Intent(this, LoginActivity.class);
            showAlertDialog(this, "Oops! Pas de connexion, " +
                    "verifier votre connexion internet puis reesayez SVP", intent);
        }

        indexOnlineUser = onlineUser.getListDahiraID().indexOf(dahira.getDahiraID());
        indexSelectedUser = selectedUser.getListDahiraID().indexOf(dahira.getDahiraID());

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        textViewName = findViewById(R.id.textView_userName);
        textViewDahiraName = findViewById(R.id.textView_dahiraName);
        textViewPhoneNumber = findViewById(R.id.textView_phoneNumber);
        textViewAdress = findViewById(R.id.textView_address);
        textViewEmail = findViewById(R.id.textView_email);
        textViewCommission = findViewById(R.id.textView_commission);
        textViewRole = findViewById(R.id.textView_role);
        textViewAdiya = findViewById(R.id.totalAdiya);
        textViewSass = findViewById(R.id.totalSass);
        textViewSocial = findViewById(R.id.totalSocial);
        linearLayoutAdiya = findViewById(R.id.linearLayout_adiya);
        linearLayoutSass = findViewById(R.id.linearLayout_sass);
        linearLayoutSocial = findViewById(R.id.linearLayout_social);
        imageView = findViewById(R.id.imageView);

        showImage(this, "profileImage", selectedUser.getUserID(), imageView);

        getAdiya();
        getSass();
        getSocial();

        textViewName.setText(selectedUser.getUserName());
        textViewDahiraName.setText(dahira.getDahiraName());
        textViewPhoneNumber.setText(selectedUser.getUserPhoneNumber());
        textViewAdress.setText(selectedUser.getAddress());
        textViewEmail.setText(selectedUser.getEmail());
        textViewCommission.setText(selectedUser.getListCommissions().get(indexSelectedUser));
        textViewAdiya.setText(selectedUser.getListAdiya().get(indexSelectedUser));
        textViewSass.setText(selectedUser.getListSass().get(indexSelectedUser));
        textViewSocial.setText(selectedUser.getListSocial().get(indexSelectedUser));
        textViewRole.setText(selectedUser.getListRoles().get(indexSelectedUser));

        if (indexSelectedUser == -1) {
            linearLayoutAdiya.setVisibility(View.GONE);
            linearLayoutSass.setVisibility(View.GONE);
            linearLayoutSocial.setVisibility(View.GONE);
            if (!selectedUser.getListRoles().get(indexSelectedUser).equals("Administrateur")) {
                textViewRole.setVisibility(View.GONE);
            }
        } else if (!selectedUser.getListRoles().get(indexSelectedUser).equals("Administrateur")) {
            linearLayoutAdiya.setVisibility(View.GONE);
            linearLayoutSass.setVisibility(View.GONE);
            linearLayoutSocial.setVisibility(View.GONE);
        }


        findViewById(R.id.button_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_back:
                indexSelectedUser = -1;
                startActivity(new Intent(UserInfoActivity.this, ListUserActivity.class));
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        indexSelectedUser = -1;
        startActivity(new Intent(UserInfoActivity.this, ListUserActivity.class));
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
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

        switch (item.getItemId()){
            case R.id.icon_back:
                startActivity(new Intent(this, ListUserActivity.class));
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
                startActivity(new Intent(this, ListUserActivity.class));
                break;

            case R.id.nav_addUser:
                actionSelected = "addNewUser";
                startActivity(new Intent(this, UserInfoActivity.class));
                break;

            case R.id.nav_displayMyDahira:
                MyStaticVariables.displayDahira = "myDahira";
                startActivity(new Intent(this, ListDahiraActivity.class));
                break;

            case R.id.nav_displayAllDahira:
                MyStaticVariables.displayDahira = "allDahira";
                startActivity(new Intent(this, ListDahiraActivity.class));
                break;

            case R.id.nav_addDahira:
                startActivity(new Intent(this, CreateDahiraActivity.class));
                break;

            case R.id.nav_addContribution:
                startActivity(new Intent(this, AddContributionActivity.class));
                break;

            case R.id.nav_displayAdiya:
                typeOfContribution = "adiya";
                startActivity(new Intent(this, ListContributionActivity.class));
                break;

            case R.id.nav_displaySass:
                typeOfContribution = "sass";
                startActivity(new Intent(this, ListContributionActivity.class));
                break;

            case R.id.nav_displaySocial:
                typeOfContribution = "social";
                startActivity(new Intent(this, ListContributionActivity.class));
                break;

            case R.id.nav_addAnnouncement:
                actionSelected = "addNewAnnouncement";
                startActivity(new Intent(this, AnnouncementActivity.class));
                break;

            case R.id.nav_displayEvent:
                if (event.getListUserID().size() == 0)
                    showAlertDialog(this, "La liste de vos evenements est vide!");
                else
                    startActivity(new Intent(this, ListEventActivity.class));
                break;

            case R.id.nav_addEvent:
                actionSelected = "addNewEvent";
                startActivity(new Intent(this, CreateEventActivity.class));
                break;

            case R.id.nav_displayExpenses:
                if (expense.getListUserID().size() == 0) {
                    showAlertDialog(this, "La liste des depenses de votre dahira est vide!");
                } else
                    startActivity(new Intent(this, ListExpenseActivity.class));
                break;

            case R.id.nav_addExpense:
                actionSelected = "addNewExpense";
                startActivity(new Intent(this, CreateExpenseActivity.class));
                break;

            case R.id.nav_callUser:
                call(this, this, selectedUser.getUserPhoneNumber());
                break;

            case R.id.nav_logout:
                toastMessage(this, "Logged out");
                logout(this);
                finish();
                startActivity(new Intent(this, MainActivity.class));
                break;

            case R.id.nav_setting:
                startActivity(new Intent(this, SettingUserActivity.class));
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setDrawerMenu() {
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
        showImage(this, "profileImage", userID, navImageView);
        textViewNavUserName.setText(onlineUser.getUserName());
        textViewNavEmail.setText(onlineUser.getEmail());
        navigationView.setCheckedItem(R.id.nav_displayMyDahira);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        hideMenuItem();
    }

    private void hideMenuItem() {
        Menu nav_Menu = navigationView.getMenu();
        nav_Menu.findItem(R.id.nav_setting).setTitle("Modifier ce membre");

        if (!onlineUser.getListDahiraID().contains(dahira.getDahiraID())) {
            nav_Menu.findItem(R.id.nav_displayAnnouncement).setVisible(false);
            nav_Menu.findItem(R.id.nav_addAnnouncement).setVisible(false);
            nav_Menu.findItem(R.id.nav_addEvent).setVisible(false);
            nav_Menu.findItem(R.id.nav_setting).setVisible(false);
        } else if (!onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur")) {
            nav_Menu.findItem(R.id.nav_setting).setVisible(false);
            nav_Menu.findItem(R.id.nav_addEvent).setVisible(false);
            nav_Menu.findItem(R.id.nav_finance).setVisible(false);
        }

        nav_Menu.findItem(R.id.nav_home).setVisible(false);
        nav_Menu.findItem(R.id.nav_callDahira).setVisible(false);
        nav_Menu.findItem(R.id.nav_searchUser).setVisible(false);
        nav_Menu.findItem(R.id.nav_searchDahira).setVisible(false);
        nav_Menu.findItem(R.id.nav_addExpense).setVisible(false);
        nav_Menu.findItem(R.id.nav_displayExpenses).setVisible(false);
        nav_Menu.findItem(R.id.nav_release).setVisible(false);
        nav_Menu.findItem(R.id.nav_gallery).setVisible(false);
        nav_Menu.findItem(R.id.nav_addUser).setVisible(false);
        nav_Menu.findItem(R.id.nav_displayAllEvent).setVisible(false);
        nav_Menu.findItem(R.id.nav_addDahira).setVisible(false);
        nav_Menu.findItem(R.id.nav_displayUsers).setVisible(false);
        nav_Menu.findItem(R.id.nav_displayMyDahira).setVisible(false);
        nav_Menu.findItem(R.id.nav_displayAllDahira).setVisible(false);
    }

    public static void getAdiya() {
        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection("adiya").document(selectedUser.getUserID());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                adiya = documentSnapshot.toObject(Adiya.class);
            }
        });
    }

    public static void getSass() {
        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection("sass").document(selectedUser.getUserID());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                sass = documentSnapshot.toObject(Sass.class);
            }
        });
    }

    public static void getSocial() {
        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection("social").document(selectedUser.getUserID());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                social = documentSnapshot.toObject(Social.class);
            }
        });
    }
}

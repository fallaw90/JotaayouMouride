package com.fallntic.jotaayumouride;

import android.annotation.SuppressLint;
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

import com.fallntic.jotaayumouride.model.Adiya;
import com.fallntic.jotaayumouride.model.Sass;
import com.fallntic.jotaayumouride.model.Social;
import com.fallntic.jotaayumouride.utility.MyStaticFunctions;
import com.fallntic.jotaayumouride.utility.MyStaticVariables;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.call;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.dismissProgressDialog;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.logout;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showAlertDialog;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.actionSelected;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.adiya;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.dahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.displayEvent;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.indexOnlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.indexSelectedUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listExpenses;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.myListEvents;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.sass;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.selectedUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.social;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.typeOfContribution;

@SuppressWarnings("unused")
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

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private TextView textViewLabeEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initViews();

        checkInternetConnection(this);

        //********************** Drawer Menu **************************
        if (onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur")) {
            setDrawerMenu();
        }
        //*************************************************************

        displayViews();

        getAdiya();
        getSass();
        getSocial();

        if (!onlineUser.getUserID().equals(selectedUser.getUserID()) &&
                !onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur")) {
            linearLayoutAdiya.setVisibility(View.GONE);
            linearLayoutSass.setVisibility(View.GONE);
            linearLayoutSocial.setVisibility(View.GONE);
        }

    }

    private void initViews() {
        indexOnlineUser = onlineUser.getListDahiraID().indexOf(dahira.getDahiraID());
        indexSelectedUser = selectedUser.getListDahiraID().indexOf(dahira.getDahiraID());

        textViewName = findViewById(R.id.textView_userName);
        textViewDahiraName = findViewById(R.id.textView_dahiraName);
        textViewPhoneNumber = findViewById(R.id.textView_phoneNumber);
        textViewAdress = findViewById(R.id.textView_address);
        textViewEmail = findViewById(R.id.textView_email);
        textViewLabeEmail = findViewById(R.id.textView_labelEmail);
        textViewCommission = findViewById(R.id.textView_commission);
        textViewRole = findViewById(R.id.textView_role);
        textViewAdiya = findViewById(R.id.totalAdiya);
        textViewSass = findViewById(R.id.totalSass);
        textViewSocial = findViewById(R.id.totalSocial);
        linearLayoutAdiya = findViewById(R.id.linearLayout_adiya);
        linearLayoutSass = findViewById(R.id.linearLayout_sass);
        linearLayoutSocial = findViewById(R.id.linearLayout_social);
        imageView = findViewById(R.id.imageView);

        findViewById(R.id.button_back).setOnClickListener(this);
    }

    @SuppressLint("SetTextI18n")
    private void displayViews() {
        textViewName.setText(selectedUser.getUserName());
        textViewDahiraName.setText("Dahira" + dahira.getDahiraName());
        textViewPhoneNumber.setText(selectedUser.getUserPhoneNumber());
        textViewAdress.setText(selectedUser.getAddress());
        textViewEmail.setText(selectedUser.getEmail());
        textViewCommission.setText(selectedUser.getListCommissions().get(indexSelectedUser));
        textViewAdiya.setText(selectedUser.getListAdiya().get(indexSelectedUser));
        textViewSass.setText(selectedUser.getListSass().get(indexSelectedUser));
        textViewSocial.setText(selectedUser.getListSocial().get(indexSelectedUser));
        textViewRole.setText(selectedUser.getListRoles().get(indexSelectedUser));

        if (selectedUser.getEmail() == null || selectedUser.getEmail().equals("")) {
            textViewLabeEmail.setVisibility(View.GONE);
            textViewEmail.setVisibility(View.GONE);
        }


        if (!selectedUser.getListRoles().get(indexSelectedUser).equals("Administrateur")) {
            textViewRole.setVisibility(View.GONE);
        }
        if (!onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur") &&
                !onlineUser.getListDahiraID().get(indexOnlineUser)
                        .equals(selectedUser.getListDahiraID().get(indexSelectedUser))) {
            linearLayoutAdiya.setVisibility(View.GONE);
            linearLayoutSass.setVisibility(View.GONE);
            linearLayoutSocial.setVisibility(View.GONE);
        }

        MyStaticFunctions.showImage(this, selectedUser.getImageUri(), imageView);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_back) {
            actionSelected = "displayUsers";
            startActivity(new Intent(UserInfoActivity.this, ShowUserActivity.class));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        actionSelected = "displayUsers";
        startActivity(new Intent(UserInfoActivity.this, ShowUserActivity.class));
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem iconLogo;
        iconLogo = menu.findItem(R.id.logo);

        iconLogo.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item))
            return true;

        switch (item.getItemId()) {
            case R.id.logo:
                startActivity(new Intent(this, HomeActivity.class));
                break;

            case R.id.instructions:
                startActivity(new Intent(this, InstructionsActivity.class));
                break;

            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.nav_home:
                HomeActivity.loadInterstitialAd(this);
                startActivity(new Intent(this, HomeActivity.class));
                break;

            case R.id.nav_displayUsers:
                HomeActivity.loadInterstitialAd(this);
                startActivity(new Intent(this, ShowUserActivity.class));
                break;

            case R.id.nav_addUser:
                actionSelected = "addNewUser";
                startActivity(new Intent(this, UserInfoActivity.class));
                break;

            case R.id.nav_displayMyDahira:
                HomeActivity.loadInterstitialAd(this);
                MyStaticVariables.displayDahira = "myDahira";
                startActivity(new Intent(this, ShowDahiraActivity.class));
                break;

            case R.id.nav_displayAllDahira:
                HomeActivity.loadInterstitialAd(this);
                MyStaticVariables.displayDahira = "allDahira";
                startActivity(new Intent(this, ShowDahiraActivity.class));
                break;

            case R.id.nav_addDahira:
                startActivity(new Intent(this, CreateDahiraActivity.class));
                break;

            case R.id.nav_addContribution:
                getAdiya();
                startActivity(new Intent(this, AddContributionActivity.class));
                break;

            case R.id.nav_displayAdiya:
                typeOfContribution = "adiya";
                getAdiya();
                getSass();
                getSocial();
                startActivity(new Intent(this, ShowContributionActivity.class));
                break;

            case R.id.nav_displaySass:
                HomeActivity.loadInterstitialAd(this);
                typeOfContribution = "sass";
                getSass();
                startActivity(new Intent(this, ShowContributionActivity.class));
                break;

            case R.id.nav_displaySocial:
                HomeActivity.loadInterstitialAd(this);
                typeOfContribution = "social";
                getSocial();
                startActivity(new Intent(this, ShowContributionActivity.class));
                break;

            case R.id.nav_addAnnouncement:
                actionSelected = "addNewAnnouncement";
                startActivity(new Intent(this, CreateAnnouncementActivity.class));
                break;

            case R.id.nav_displayEvent:
                HomeActivity.loadInterstitialAd(this);
                if (myListEvents == null || myListEvents.size() <= 0)
                    showAlertDialog(this, "La liste de vos evenements est vide!");
                else {
                    displayEvent = "myEvents";
                    startActivity(new Intent(this, ShowEventActivity.class));
                }
                break;

            case R.id.nav_addEvent:
                actionSelected = "addNewEvent";
                startActivity(new Intent(this, CreateEventActivity.class));
                break;

            case R.id.nav_displayExpenses:
                HomeActivity.loadInterstitialAd(this);
                if (listExpenses == null) {
                    showAlertDialog(this, "La liste des depenses de votre dahira est vide!");
                } else
                    startActivity(new Intent(this, ShowExpenseActivity.class));
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
                startActivity(new Intent(this, MainActivity.class));
                finish();
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
        View navHeader = navigationView.getHeaderView(0);
        CircleImageView navImageView = navHeader.findViewById(R.id.nav_imageView);
        TextView textViewNavUserName = navHeader.findViewById(R.id.textView_navUserName);
        TextView textViewNavEmail = navHeader.findViewById(R.id.textView_navEmail);
        toggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        MyStaticFunctions.showImage(this, onlineUser.getImageUri(), navImageView);
        textViewNavUserName.setText(onlineUser.getUserName());
        textViewNavEmail.setText(onlineUser.getEmail());
        navigationView.setCheckedItem(R.id.nav_displayMyDahira);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

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

        if (selectedUser.getListRoles().get(indexSelectedUser).equals("Administrateur"))
            nav_Menu.findItem(R.id.nav_setting).setVisible(false);

        nav_Menu.findItem(R.id.nav_removeDahira).setVisible(false);
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
        nav_Menu.findItem(R.id.nav_removeDahira).setVisible(false);
    }

    private void getAdiya() {
        if (adiya == null) {
            adiya = new Adiya();

            DocumentReference docRef = FirebaseFirestore.getInstance()
                    .collection("adiya").document(selectedUser.getUserID());
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.toObject(Adiya.class) != null)
                        adiya = documentSnapshot.toObject(Adiya.class);
                }
            });
        }
    }

    private void getSass() {
        if (sass == null) {
            sass = new Sass();

            DocumentReference docRef = FirebaseFirestore.getInstance()
                    .collection("sass").document(selectedUser.getUserID());
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.toObject(Sass.class) != null)
                        sass = documentSnapshot.toObject(Sass.class);
                }
            });
        }
    }

    private void getSocial() {
        if (social == null) {
            social = new Social();

            DocumentReference docRef = FirebaseFirestore.getInstance()
                    .collection("social").document(selectedUser.getUserID());
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.toObject(Social.class) != null)
                        social = documentSnapshot.toObject(Social.class);
                }
            });
        }
    }
}
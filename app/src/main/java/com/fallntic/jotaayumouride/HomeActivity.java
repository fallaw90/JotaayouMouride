package com.fallntic.jotaayumouride;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.fallntic.jotaayumouride.adapter.PageAdapter;
import com.fallntic.jotaayumouride.fragments.AboutFragment;
import com.fallntic.jotaayumouride.fragments.AudioFragment;
import com.fallntic.jotaayumouride.fragments.HomeFragment;
import com.fallntic.jotaayumouride.fragments.PDFFragment;
import com.fallntic.jotaayumouride.fragments.ProfileFragment;
import com.fallntic.jotaayumouride.fragments.PubFragment;
import com.fallntic.jotaayumouride.model.Dahira;
import com.fallntic.jotaayumouride.model.Event;
import com.fallntic.jotaayumouride.model.User;
import com.fallntic.jotaayumouride.utility.MyStaticFunctions;
import com.fallntic.jotaayumouride.utility.MyStaticVariables;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fallntic.jotaayumouride.ShowDahiraActivity.dialogSearchDahira;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.dismissProgressDialog;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.isConnected;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.logout;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showAlertDialog;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.stopCurrentPlayingMediaPlayer;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.actionSelected;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.broadcastReceiverMediaPlayer;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.counterHAonPause;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.counterHAonResume;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.dahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.displayDahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.displayEvent;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.firebaseAuth;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.indexOnlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.indexSelectedUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.iv_next;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.iv_play;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.iv_previous;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAllDahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAllEvent;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAudiosAM;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAudiosHT;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAudiosHTDK;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAudiosMagal2019HT;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAudiosMagal2019HTDK;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAudiosMixedWolofal;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAudiosQuran;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAudiosRadiass;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAudiosSerigneMbayeDiakhate;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAudiosSerigneMoussaKa;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listAudiosZikr;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listDahiraFound;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listExpenses;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listImage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listSong;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.mAdapter;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.mediaPlayer;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.myListDahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.myListEvents;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.notificationManagerMediaPlayer;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.pb_loader;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.pb_main_loader;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.seekBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.selectedUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.tb_title;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.toolbar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.toolbar_bottom;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.tv_empty;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.tv_time;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.typeOfContribution;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.wasHAonResume;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.wasHAonStop;

@SuppressWarnings({"LoopStatementThatDoesntLoop", "SuspiciousListRemoveInLoop"})
public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "HomeActivity";

    private TabLayout tabLayout;
    private ViewPager viewPager;
    public PageAdapter pageAdapter;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private CircleImageView navImageView;
    private TextView textViewNavUserName;
    private TextView textViewNavEmail;
    private static String marqueeAd;
    private TextView textViewMarquee;
    private String dahiraToUpdate;

    public static AdView bannerAd;
    private static InterstitialAd interstitialAd;

    private static FirebaseFirestore firestore;
    //********************************* Clean Database *****************************************
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<Dahira> filteredDahiras = new ArrayList<>();

    private void setupOnlineViewPager(ViewPager viewPager) {
        pageAdapter.addFragment(new HomeFragment(), "Home", 0);
        pageAdapter.addFragment(new PubFragment(), "Info", 1);
        pageAdapter.addFragment(new ProfileFragment(), "Profile", 2);
        pageAdapter.addFragment(new AudioFragment(), "Audios", 3);
        pageAdapter.addFragment(new PDFFragment(), "Khassida PDF", 4);
        Objects.requireNonNull(tabLayout.getTabAt(2)).setText("Profil");
        viewPager.setOffscreenPageLimit(4);
        viewPager.setAdapter(pageAdapter);
    }

    private void setupOfflineViewPager(ViewPager viewPager) {
        pageAdapter.addFragment(new HomeFragment(), "Home", 0);
        pageAdapter.addFragment(new PubFragment(), "Info", 1);
        pageAdapter.addFragment(new AboutFragment(), "About", 2);
        pageAdapter.addFragment(new AudioFragment(), "Audios", 3);
        pageAdapter.addFragment(new PDFFragment(), "Khassida PDF", 4);
        Objects.requireNonNull(tabLayout.getTabAt(2)).setText("About");
        viewPager.setOffscreenPageLimit(5);
        viewPager.setAdapter(pageAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem itemLogin, itemLogo;
        itemLogin = menu.findItem(R.id.login);
        itemLogo = menu.findItem(R.id.logo);

        if (firebaseAuth == null || firebaseAuth.getCurrentUser() == null) {
            itemLogin.setVisible(true);
        } else {
            itemLogo.setVisible(true);
        }

        return true;
    }

    private final List<User> allUsers = new ArrayList<>();
    private final List<String> phoneNumbers = new ArrayList<>();
    private final List<String> id_dahira = new ArrayList<>();

    private static void showInterstitialAd() {
        if (interstitialAd.isLoaded() && (onlineUser == null || !onlineUser.hasPaid())) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // If Ads are loaded, show Interstitial else show nothing.
                    interstitialAd.show();
                    interstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdOpened() {
                            super.onAdOpened();
                            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                                mediaPlayer.pause();
                            }
                        }

                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            if (mediaPlayer != null) {
                                mediaPlayer.start();
                            }
                        }
                    });
                }
            }, 5000);
        }
    }

    public static void getAllEvents(final Context context) {
        if (listAllEvent == null) {
            listAllEvent = new ArrayList<>();

            showProgressBar();
            firestore.collection("events").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            hideProgressBar();
                            if (!queryDocumentSnapshots.isEmpty()) {
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot documentSnapshot : list) {
                                    Event event = documentSnapshot.toObject(Event.class);
                                    listAllEvent.add(event);
                                    Log.d(TAG, "Events downloaded.");
                                }

                                //Collections.sort(listAllEvent);
                                Log.d(TAG, "Events downloaded.");
                                if (displayEvent.equals("allEvents") && listAllEvent.size() > 0)
                                    context.startActivity(new Intent(context, ShowEventActivity.class));
                            } else if (displayEvent.equals("allEvents")) {
                                showAlertDialog(context, "Il n'y a auccun evenement " +
                                        "disponible pour le moment.");
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    hideProgressBar();
                    context.startActivity(new Intent(context, HomeActivity.class));
                    Log.d(TAG, "Error downloading events");
                }
            });
        } else if (!listAllEvent.isEmpty() && displayEvent.equals("allEvents")) {
            context.startActivity(new Intent(context, ShowEventActivity.class));
        } else if (displayEvent.equals("allEvents")) {
            showAlertDialog(context, "Il n'y a auccun evenement " +
                    "disponible pour le moment.");
        }
    }

    public static void loadInterstitialAd(final Context context) {

        // Prepare the Interstitial Ad
        interstitialAd = new InterstitialAd(context);

        // Insert the Ad Unit ID
        interstitialAd.setAdUnitId(context.getString(R.string.interstitial_unit_id));
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequest);
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                showInterstitialAd();
            }
        });
    }

    public static void loadBannerAd(Activity activity) {

        bannerAd = activity.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAd.loadAd(adRequest);
    }

    private void initViews() {
        textViewMarquee = findViewById(R.id.marquee_text);
        tabLayout = findViewById(R.id.tablayout);
        viewPager = findViewById(R.id.viewPager);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        View navHeader = navigationView.getHeaderView(0);
        navImageView = navHeader.findViewById(R.id.nav_imageView);
        textViewNavUserName = navHeader.findViewById(R.id.textView_navUserName);
        textViewNavEmail = navHeader.findViewById(R.id.textView_navEmail);
        toggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        if (onlineUser == null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        pageAdapter = new PageAdapter(getSupportFragmentManager());

        initViewsProgressBar();
    }

    private void hideMenuItem() {
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
        nav_Menu.findItem(R.id.nav_removeDahira).setVisible(false);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item))
            return true;

        switch (item.getItemId()) {

            case R.id.login:
                startActivity(new Intent(this, LoginPhoneActivity.class));
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
    public void onPause() {
        // This method should be called in the parent Activity's onPause() method.
        if (bannerAd != null) {
            bannerAd.pause();
        }

        counterHAonPause++;
        //toastMessage(this, "HomeActivity onPause");
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bannerAd != null) {
            bannerAd.resume();
        }

        if (wasHAonStop) {
            counterHAonResume++;
        }

        wasHAonResume = true;
        //toastMessage(this, "HomeActivity onResume");
    }

    private void saveTokenID(final String userID) {

        FirebaseMessaging.getInstance().subscribeToTopic("JotaayouMouride");

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token_id = Objects.requireNonNull(task.getResult()).getToken();

                        Map<String, Object> tokenMap = new HashMap<>();
                        tokenMap.put("tokenID", token_id);
                        //toastMessage(ProfileActivity.this, token_id);

                        FirebaseFirestore.getInstance().collection("users")
                                .document(userID).update(tokenMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        dismissProgressDialog();
                                    }
                                });
                        // Log and toast
                        Log.d(TAG, token_id);
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initViews();

        if (!isConnected(this)) {
            toastMessage(this, "Verifier votre connexion SVP.");
            return;
        }

        firestore = FirebaseFirestore.getInstance();

        //startActivity(new Intent(this, AdvertisementActivity.class));
        //deleteOneUser("+13474795621");

        textViewMarquee.setSelected(true);
        getMarqueeText();

        if (onlineUser != null && firebaseAuth != null && firebaseAuth.getCurrentUser() != null) {
            setDrawerMenu();
            setupOnlineViewPager(viewPager);
            new MyTask().execute();
        } else {
            toolbar.setLogo(R.mipmap.logo);
            setupOfflineViewPager(viewPager);
        }
        resizeMarqueeText();
        changeTab();

        //****************************** adMob ***********************************
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                loadInterstitialAd(HomeActivity.this);
            }
        });

        loadBannerAd(this);
    }

    private void initViewsProgressBar() {
        relativeLayoutData = findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private boolean isAdminUpdated() {
        boolean updated = true;
        if (!onlineUser.getListDahiraID().isEmpty()) {
            if (onlineUser.getListUpdatedDahiraID().isEmpty()) {
                dahiraToUpdate = onlineUser.getListDahiraID().get(0);
                updated = false;
            } else {
                for (String dahiraID : onlineUser.getListDahiraID()) {
                    if (!onlineUser.getListUpdatedDahiraID().contains(dahiraID)) {
                        dahiraToUpdate = dahiraID;
                        updated = false;
                        break;
                    }
                }
            }
        }
        return updated;
    }

    private void getDahiraToUpdate() {
        if (!isAdminUpdated()) {
            firestore.collection("dahiras").whereEqualTo("dahiraID", dahiraToUpdate).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                dahira = documentSnapshot.toObject(Dahira.class);
                                break;
                            }
                            startActivity(new Intent(HomeActivity.this, UpdateAdminActivity.class));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            toastMessage(HomeActivity.this, "Error chargement dahiraToUpdate!");
                            Log.d(TAG, e.toString());
                        }
                    });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //toastMessage(this, "HomeActivity onStop");

        wasHAonStop = true;
    }

    private void changeTab() {

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @SuppressWarnings("StatementWithEmptyBody")
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 0) {

                    //toastMessage(HomeActivity.this, "Evenements Fragment");

                } else if (tab.getPosition() == 1) {
                    //stopCurrentPlayingMediaPlayer();
                    //toastMessage(HomeActivity.this, "About/Profile Fragment");


                } else if (tab.getPosition() == 2) {


                } else if (tab.getPosition() == 3) {
                    pageAdapter.notifyDataSetChanged();
                    //stopCurrentPlayingMediaPlayer();
                    //toastMessage(HomeActivity.this, "Wolofal Fragment");

                } else if (tab.getPosition() == 4) {

                } else {

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void resizeMarqueeText() {
        if (textViewMarquee != null) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textViewMarquee.getLayoutParams();
            params.height = getResources().getDimensionPixelSize(R.dimen.textView_height);
            params.width = getResources().getDimensionPixelSize(R.dimen.textView_width);
            textViewMarquee.setLayoutParams(params);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.nav_home:
                loadInterstitialAd(this);
                startActivity(new Intent(this, HomeActivity.class));
                break;
            case R.id.nav_displayMyDahira:
                MyStaticVariables.displayDahira = "myDahira";
                if (myListDahira == null || myListDahira.size() <= 0) {
                    showAlertDialog(this, "Vous n'etes membre d'un " +
                            " aucun dahira pour le moment. Contactez l'administrateur de votre dahira " +
                            "pour qu'il vous ajoute en tant que membre. Ou bien, creer un dahira si vous etes " +
                            "administrateur.\n Reessayez si vous croyez que ceci est une erreur.");
                } else {
                    startActivity(new Intent(this, ShowDahiraActivity.class));
                }
                break;

            case R.id.nav_addDahira:
                startActivity(new Intent(this, CreateDahiraActivity.class));
                break;

            case R.id.nav_displayAllDahira:
                MyStaticVariables.displayDahira = "allDahira";
                if (listAllDahira == null || listAllDahira.size() <= 0) {
                    showAlertDialog(this, "Aucun dahira disponible pour le moment");
                } else {
                    startActivity(new Intent(this, ShowDahiraActivity.class));
                }
                break;

            case R.id.nav_displayAllEvent:
                loadInterstitialAd(this);
                displayEvent = "allEvents";
                getAllEvents(this);
                break;

            case R.id.nav_searchDahira:
                displayDahira = "searchDahira";
                dialogSearchDahira(this);
                break;

            case R.id.nav_setting:
                startActivity(new Intent(this, SettingProfileActivity.class));
                break;

            case R.id.nav_logout:
                toastMessage(this, "Logged out");
                logout(this);
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setDrawerMenu() {

        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        if (onlineUser.getImageUri() != null) {
            MyStaticFunctions.showImage(this, onlineUser.getImageUri(), navImageView);
        }
        if (navigationView != null)
            navigationView.setCheckedItem(R.id.nav_home);

        hideMenuItem();
    }

    @Override
    public void onDestroy() {
        if (bannerAd != null) {
            bannerAd.destroy();
        }

        stopCurrentPlayingMediaPlayer();


        listAudiosMagal2019HT = null;
        listAudiosMagal2019HTDK = null;
        listAudiosMixedWolofal = null;
        listAudiosZikr = null;
        indexOnlineUser = -1;
        indexSelectedUser = -1;
        typeOfContribution = null;
        actionSelected = null;
        displayDahira = null;
        displayEvent = null;
        onlineUser = null;
        selectedUser = null;
        dahira = null;
        listSong = null;
        listAudiosQuran = null;
        listAudiosSerigneMbayeDiakhate = null;
        listAudiosSerigneMoussaKa = null;
        listAudiosHT = null;
        listAudiosHTDK = null;
        listAudiosAM = null;
        listAudiosRadiass = null;
        listUser = null;
        myListEvents = null;
        listAllEvent = null;
        myListDahira = null;
        listAllDahira = null;
        listDahiraFound = null;
        listExpenses = null;
        listImage = null;
        relativeLayoutProgressBar = null;
        relativeLayoutData = null;
        firestore = null;
        mAdapter = null;
        toolbar = null;
        toolbar_bottom = null;
        tb_title = null;
        tv_empty = null;
        iv_play = null;
        iv_next = null;
        iv_previous = null;
        pb_loader = null;
        pb_main_loader = null;
        tv_time = null;
        seekBar = null;

        //**********Notification Music********
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManagerMediaPlayer = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                Objects.requireNonNull(notificationManagerMediaPlayer).cancelAll();
            }
            unregisterReceiver(broadcastReceiverMediaPlayer);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        //toastMessage(this, "HomeActivity Destroyed");
        super.onDestroy();
    }

    private void getMarqueeText() {
        if (marqueeAd == null && firestore != null) {
            firestore.collection("advertisements").document("marquee_text").get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null) {
                                    marqueeAd = document.getString("text");
                                    if (textViewMarquee != null) {
                                        textViewMarquee.setText(marqueeAd);
                                        textViewMarquee.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    Log.d("LOGGER", "No such document");
                                }
                            } else {
                                Log.d("LOGGER", "get failed with ", task.getException());
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            toastMessage(HomeActivity.this, "Error charging pubs!");
                        }
                    });
        } else {
            if (textViewMarquee != null) {
                textViewMarquee.setText(marqueeAd);
                textViewMarquee.setVisibility(View.VISIBLE);
            }
        }
    }

    public void getMyDahira() {
        if (firestore != null && myListDahira == null) {
            myListDahira = new ArrayList<>();
            firestore.collection("dahiras").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot documentSnapshot : list) {
                                    Dahira dahira = documentSnapshot.toObject(Dahira.class);
                                    if (dahira != null && onlineUser.getListDahiraID().contains(dahira.getDahiraID())) {
                                        myListDahira.add(dahira);
                                    }
                                }
                                Collections.sort(myListDahira);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            toastMessage(HomeActivity.this, "Error charging dahira");
                        }
                    });
        }
    }

    public void getAllDahiras() {
        if (firestore != null && MyStaticVariables.listAllDahira == null || listAllDahira.isEmpty()) {
            listAllDahira = new ArrayList<>();
            firestore.collection("dahiras").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot documentSnapshot : list) {
                                    Dahira dahira = documentSnapshot.toObject(Dahira.class);
                                    if (dahira != null) {
                                        dahira.setDahiraID(documentSnapshot.getId());
                                        listAllDahira.add(dahira);
                                    }
                                }
                                Collections.sort(listAllDahira);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //toastMessage(context, "Error charging dahira!");
                        }
                    });
        }
    }

    public void clearDahiraAndUpdateUser() {
        if (MyStaticVariables.listAllDahira == null || listAllDahira.isEmpty()) {
            listAllDahira = new ArrayList<>();
            firestore.collection("dahiras").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot documentSnapshot : list) {
                                    Dahira dahira = documentSnapshot.toObject(Dahira.class);
                                    if (dahira != null) {
                                        dahira.setDahiraID(documentSnapshot.getId());
                                        listAllDahira.add(dahira);
                                    }
                                }
                                //Sort the dahira by total member
                                Collections.sort(listAllDahira);
                                //Extract dahira by phone number
                                for (int i = listAllDahira.size() - 1; i >= 0; i--) {
                                    if (!phoneNumbers.contains(listAllDahira.get(i).getDahiraPhoneNumber())) {
                                        filteredDahiras.add(listAllDahira.get(i));
                                        phoneNumbers.add(listAllDahira.get(i).getDahiraPhoneNumber());
                                        id_dahira.add(listAllDahira.get(i).getDahiraID());
                                    }
                                }

                                for (Dahira dahira : listAllDahira) {
                                    if (!id_dahira.contains(dahira.getDahiraID())) {
                                        deleteDahira(dahira);
                                    }
                                }
                            }

                            for (User user : allUsers) {
                                for (int i = 0; i < user.getListDahiraID().size(); i++) {
                                    if (!id_dahira.contains(user.getListDahiraID().get(i))) {
                                        //noinspection SuspiciousListRemoveInLoop
                                        user.getListDahiraID().remove(i);
                                        //noinspection SuspiciousListRemoveInLoop
                                        user.getListUpdatedDahiraID().remove(i);
                                        user.getListRoles().remove(i);
                                        user.getListCommissions().remove(i);
                                        user.getListAdiya().remove(i);
                                        user.getListSass().remove(i);
                                        user.getListSocial().remove(i);
                                        updateUser(user);
                                    }
                                }
                            }
                            //Collections.reverse(listAllDahira);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            toastMessage(HomeActivity.this, "Error charging all dahira!");
                        }
                    });
        }
    }

    @SuppressWarnings("unused")
    public void cleanMultipleDahira() {
        if (MyStaticVariables.listAllDahira == null || listAllDahira.isEmpty()) {
            listAllDahira = new ArrayList<>();
            firestore.collection("users").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot documentSnapshot : list) {
                                    User user = documentSnapshot.toObject(User.class);
                                    if (user != null) {
                                        user.setUserID(documentSnapshot.getId());
                                        allUsers.add(user);
                                    }
                                }
                                clearDahiraAndUpdateUser();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            toastMessage(HomeActivity.this, "Error charging all dahira!");
                        }
                    });
        }
    }

    private void updateUser(final User user) {
        firestore.collection("users").document(user.getUserID())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        toastMessage(HomeActivity.this, user.getUserName() + " updated");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    @SuppressWarnings("unused")
    private void updateTotalMemberDahira(final Dahira dahira) {
        firestore.collection("dahiras").document(dahira.getDahiraID())
                .set(dahira)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        toastMessage(HomeActivity.this, dahira.getDahiraName() + " updated");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    private void deleteDahira(final Dahira dahira) {
        showProgressBar();
        firestore.collection("dahiras").document(dahira.getDahiraID())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        toastMessage(HomeActivity.this, dahira.getDahiraName() + "Deleted");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    private void deleteUser(final User user) {
        firestore.collection("users").document(user.getUserID())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        toastMessage(HomeActivity.this, user.getUserName() + " updated");
                        logout(HomeActivity.this);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    @SuppressWarnings("unused")
    public void deleteOneUser(final String phoneNumber) {
        if (allUsers.isEmpty() && firestore != null) {
            firestore.collection("users").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot documentSnapshot : list) {
                                    User user = documentSnapshot.toObject(User.class);
                                    if (user != null) {
                                        if (user.getUserPhoneNumber().equals(phoneNumber))
                                            deleteUser(user);
                                        break;
                                    }
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            toastMessage(HomeActivity.this, "User deleted.!");
                        }
                    });
        }
    }

    @SuppressLint("StaticFieldLeak")
    class MyTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            loadInterstitialAd(HomeActivity.this);
            loadBannerAd(HomeActivity.this);
            getMarqueeText();
            textViewNavUserName.setText(onlineUser.getUserName());
            textViewNavEmail.setText(onlineUser.getEmail());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            saveTokenID(onlineUser.getUserID());
            getDahiraToUpdate();
            getMyDahira();
            getAllDahiras();
            //cleanMultipleDahira();
            return null;
        }
    }
}
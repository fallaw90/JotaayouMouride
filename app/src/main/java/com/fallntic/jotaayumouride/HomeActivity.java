package com.fallntic.jotaayumouride;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
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
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.fallntic.jotaayumouride.Adapter.PageAdapter;
import com.fallntic.jotaayumouride.Fragments.AboutFragment;
import com.fallntic.jotaayumouride.Fragments.AudioFragment;
import com.fallntic.jotaayumouride.Fragments.PDFFragment;
import com.fallntic.jotaayumouride.Fragments.ProfileFragment;
import com.fallntic.jotaayumouride.Fragments.PubFragment;
import com.fallntic.jotaayumouride.Fragments.QuranFragment;
import com.fallntic.jotaayumouride.Model.Dahira;
import com.fallntic.jotaayumouride.Model.Event;
import com.fallntic.jotaayumouride.Utility.MyStaticFunctions;
import com.fallntic.jotaayumouride.Utility.MyStaticVariables;
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
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fallntic.jotaayumouride.ShowDahiraActivity.dialogSearchDahira;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.dismissProgressDialog;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.isConnected;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.logout;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.setMediaPlayer;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.showAlertDialog;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.dahira;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.displayDahira;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.displayEvent;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.firebaseAuth;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.firestore;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAllDahira;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAllEvent;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.mediaPlayer;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.myHandler;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.myListDahira;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutProgressBar;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "HomeActivity";

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private PageAdapter pageAdapter;
    private TabItem tabKourel;
    private TabItem tabWolofal;
    private TabItem tabQuran;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private View navHeader;
    private CircleImageView navImageView;
    private TextView textViewNavUserName;
    private TextView textViewNavEmail;
    private static String marqueeAd;
    private TextView textViewMarquee;
    private String userID;
    private String dahiraToUpdate;

    public static AdView bannerAd;
    public static InterstitialAd interstitialAd;
    public static AdRequest adRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initViews();

        textViewMarquee.setSelected(true);
        getMarqueeText();

        //startActivity(new Intent(this, AddMultipleAudioActivity.class));
        //startActivity(new Intent(this, ImageAdvertisementActivity.class));

        if (!isConnected(this)) {
            toastMessage(this, "Verifier votre connexion SVP.");
            return;
        }

        if (firebaseAuth != null && firebaseAuth.getCurrentUser() != null) {
            userID = firebaseAuth.getCurrentUser().getUid();
            setDrawerMenu();
            saveTokenID(userID);
            setupOnlineViewPager(viewPager);
            getDahiraToUpdate();
            textViewNavUserName.setText(onlineUser.getUserName());
            textViewNavEmail.setText(onlineUser.getEmail());
        } else {
            toolbar.setLogo(R.mipmap.logo);
            setupOfflineViewPager(viewPager);
        }

        changeTab();

        //****************************** adMob ***********************************
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                loadInterstitialAd(HomeActivity.this);
            }
        });

        loadBannerAd(this, this);
    }

    public static void getMyDahira(final Context context) {
        showProgressBar();
        if (MyStaticVariables.myListDahira == null || myListDahira.isEmpty()) {
            myListDahira = new ArrayList<>();
            firestore.collection("dahiras").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            hideProgressBar();
                            if (!queryDocumentSnapshots.isEmpty()) {
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot documentSnapshot : list) {
                                    Dahira dahira = documentSnapshot.toObject(Dahira.class);
                                    if (onlineUser.getListDahiraID().contains(dahira.getDahiraID())) {
                                        myListDahira.add(dahira);
                                    }
                                }
                            }
                            if (myListDahira.isEmpty()) {
                                Intent intent = new Intent(context, HomeActivity.class);
                                showAlertDialog(context, "Vous n'etes membre d'un " +
                                        " aucun dahira pour le moment. Contactez l'administrateur de votre dahira " +
                                        "pour qu'il vous ajouter en tant que membre. Ou bien, creer un dahira si vous etes " +
                                        "administrateur.", intent);
                            } else {
                                context.startActivity(new Intent(context, ShowDahiraActivity.class));
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressBar();
                            toastMessage(context, "Error charging dahira!");
                        }
                    });
        } else {
            context.startActivity(new Intent(context, ShowDahiraActivity.class));
        }
    }

    public static void getAllDahiras(final Context context) {
        showProgressBar();
        if (MyStaticVariables.listAllDahira == null || listAllDahira.isEmpty()) {
            listAllDahira = new ArrayList<>();
            firestore.collection("dahiras").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            hideProgressBar();
                            if (!queryDocumentSnapshots.isEmpty()) {
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot documentSnapshot : list) {
                                    Dahira dahira = documentSnapshot.toObject(Dahira.class);
                                    if (dahira != null) {
                                        dahira.setDahiraID(documentSnapshot.getId());
                                        MyStaticVariables.listAllDahira.add(dahira);
                                    }
                                }
                            }
                            if (listAllDahira.isEmpty()) {
                                showAlertDialog(context, "Auccun dahira" +
                                        " n'est enregistre dans le platforme pour le moment. " +
                                        "Merci de creer votre dahira si vous etes administrateur.");
                            } else if (displayDahira.equals("allDahira")) {
                                context.startActivity(new Intent(context, ShowDahiraActivity.class));
                            } else if (displayDahira.equals("searchDahira")) {
                                dialogSearchDahira(context);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressBar();
                            toastMessage(context, "Error charging dahira!");
                        }
                    });
        } else if (displayDahira.equals("searchDahira")) {
            dialogSearchDahira(context);
        } else
            context.startActivity(new Intent(context, ShowDahiraActivity.class));
    }

    private void setupOnlineViewPager(ViewPager viewPager) {
        pageAdapter.addFragment(new PubFragment(), "Info", 0);
        pageAdapter.addFragment(new ProfileFragment(), "Profile", 1);
        pageAdapter.addFragment(new AudioFragment(), "Audios", 2);
        pageAdapter.addFragment(new PDFFragment(), "Khassida PDF", 3);
        pageAdapter.addFragment(new QuranFragment(), "Quran", 4);
        Objects.requireNonNull(tabLayout.getTabAt(1)).setText("Profil");
        viewPager.setOffscreenPageLimit(4);
        viewPager.setAdapter(pageAdapter);
    }

    private void setupOfflineViewPager(ViewPager viewPager) {
        pageAdapter.addFragment(new PubFragment(), "Info", 0);
        pageAdapter.addFragment(new AboutFragment(), "About", 1);
        pageAdapter.addFragment(new AudioFragment(), "Audios", 2);
        pageAdapter.addFragment(new PDFFragment(), "Khassida PDF", 3);
        pageAdapter.addFragment(new QuranFragment(), "Quran", 4);
        Objects.requireNonNull(tabLayout.getTabAt(1)).setText("About");

        viewPager.setOffscreenPageLimit(4);
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

    public void initViews() {
        textViewMarquee = findViewById(R.id.marquee_text);
        tabLayout = findViewById(R.id.tablayout);
        tabKourel = findViewById(R.id.tab_kourel);
        tabWolofal = findViewById(R.id.tab_wolofal);
        tabQuran = findViewById(R.id.tab_quran);
        viewPager = findViewById(R.id.viewPager);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navHeader = navigationView.getHeaderView(0);
        navImageView = navHeader.findViewById(R.id.nav_imageView);
        textViewNavUserName = navHeader.findViewById(R.id.textView_navUserName);
        textViewNavEmail = navHeader.findViewById(R.id.textView_navEmail);
        toggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        pageAdapter = new PageAdapter(getSupportFragmentManager());
        if (myHandler == null)
            myHandler = new Handler();
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();

        initViewsProgressBar();
    }

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
        nav_Menu.findItem(R.id.nav_removeDahira).setVisible(false);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item))
            return true;

        switch (item.getItemId()) {
            case R.id.logo:
                startActivity(new Intent(this, HomeActivity.class));
                break;

            case R.id.login:
                startActivity(new Intent(this, LoginPhoneActivity.class));
                break;

            case R.id.instructions:
                startActivity(new Intent(this, InstructionsActivity.class));
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void saveTokenID(final String userID) {

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
                        String token_id = task.getResult().getToken();

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

    public void changeTab() {

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 0) {

                    //toastMessage(HomeActivity.this, "Evenements Fragment");

                } else if (tab.getPosition() == 1) {
                    setMediaPlayer();
                    //toastMessage(HomeActivity.this, "About/Profile Fragment");


                } else if (tab.getPosition() == 2) {
                    //toastMessage(HomeActivity.this, "Khassida Fragment");

                } else if (tab.getPosition() == 3) {
                    setMediaPlayer();
                    //toastMessage(HomeActivity.this, "Wolofal Fragment");

                } else {
                    setMediaPlayer();
                    //toastMessage(HomeActivity.this, "Quran Fragment");

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

    public void initViewsProgressBar() {
        relativeLayoutData = findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        progressBar = findViewById(R.id.progressBar);
    }

    public boolean isAdminUpdated() {
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

    public void getDahiraToUpdate() {
        if (!isAdminUpdated()) {
            showProgressBar();
            firestore.collection("dahiras").whereEqualTo("dahiraID", dahiraToUpdate).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            hideProgressBar();
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
                            hideProgressBar();
                            toastMessage(HomeActivity.this, "Error chargement dahiraToUpdate!");
                            Log.d(TAG, e.toString());
                        }
                    });
        }
    }

    @Override
    public void onPause() {
        // This method should be called in the parent Activity's onPause() method.
        if (bannerAd != null) {
            bannerAd.pause();
        }

        super.onPause();
    }

    public static void showInterstitialAd() {
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
                            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                                mediaPlayer.start();
                            }
                        }
                    });
                }
            }, 5000);
        }
    }

    public static void loadBannerAd(Activity activity, Context context) {

        bannerAd = activity.findViewById(R.id.adView);
        adRequest = new AdRequest.Builder().build();
        bannerAd.loadAd(adRequest);
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
                getMyDahira(HomeActivity.this);
                break;

            case R.id.nav_addDahira:
                startActivity(new Intent(this, CreateDahiraActivity.class));
                break;

            case R.id.nav_displayAllDahira:
                MyStaticVariables.displayDahira = "allDahira";
                getAllDahiras(this);
                break;

            case R.id.nav_displayAllEvent:
                loadInterstitialAd(this);
                displayEvent = "allEvents";
                getAllEvents(this);
                break;

            case R.id.nav_searchDahira:
                displayDahira = "searchDahira";
                getAllDahiras(this);
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
        moveTaskToBack(true);
    }

    public void setDrawerMenu() {

        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (onlineUser.getImageUri() != null)
            MyStaticFunctions.showImage(this, onlineUser.getImageUri(), navImageView);
        navigationView.setCheckedItem(R.id.nav_home);

        hideMenuItem();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bannerAd != null) {
            bannerAd.resume();
        }
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textViewMarquee.getLayoutParams();
        params.height = getResources().getDimensionPixelSize(R.dimen.textView_height);
        params.width = getResources().getDimensionPixelSize(R.dimen.textView_width);
        textViewMarquee.setLayoutParams(params);
    }

    @Override
    public void onDestroy() {
        if (bannerAd != null) {
            bannerAd.destroy();
        }

        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception ignored) {
            }
        }

        MyStaticVariables.listSong = null;
        MyStaticVariables.listAudiosQuran = null;
        MyStaticVariables.listAudiosSerigneMbayeDiakhate = null;
        MyStaticVariables.listAudiosSerigneMoussaKa = null;
        MyStaticVariables.listAudiosHT = null;
        MyStaticVariables.listAudiosHTDK = null;
        MyStaticVariables.listAudiosMagal2019HT = null;
        MyStaticVariables.listAudiosMagal2019HTDK = null;
        MyStaticVariables.listAudiosAM = null;
        MyStaticVariables.listAudiosRadiass = null;
        MyStaticVariables.listAudiosMixedWolofal = null;
        MyStaticVariables.listAudiosZikr = null;

        super.onDestroy();
    }

    private void getMarqueeText() {
        if (marqueeAd == null) {
            firestore.collection("advertisements").document("marquee_text").get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null) {
                                    marqueeAd = document.getString("text");
                                    textViewMarquee.setText(marqueeAd);
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
            textViewMarquee.setText(marqueeAd);
        }
    }

}
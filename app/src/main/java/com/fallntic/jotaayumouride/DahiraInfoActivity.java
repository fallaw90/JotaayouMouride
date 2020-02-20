package com.fallntic.jotaayumouride;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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

import com.fallntic.jotaayumouride.interfaces.DrawerMenu;
import com.fallntic.jotaayumouride.model.Event;
import com.fallntic.jotaayumouride.model.Expense;
import com.fallntic.jotaayumouride.model.User;
import com.fallntic.jotaayumouride.utility.MyStaticVariables;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fallntic.jotaayumouride.HomeActivity.getAllEvents;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.deleteDocument;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.dismissProgressDialog;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.logout;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showAlertDialog;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showImage;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showProgressDialog;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.toastMessage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.actionSelected;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.dahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.displayDahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.displayEvent;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.indexOnlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listExpenses;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.myListDahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.myListEvents;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.objNotification;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.onlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutProgressBar;

public class DahiraInfoActivity extends AppCompatActivity implements View.OnClickListener,
        DrawerMenu, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "DahiraInfoActivity";

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private boolean isDahiraEmpty = true;

    private TextView textViewDahiraName;
    private TextView textViewDieuwrine;
    private TextView textViewSiege;
    private TextView textViewPhoneNumber;
    private TextView textViewTotalAdiya;
    private TextView textViewTotalSass;
    private TextView textViewTotalSocial;
    private TextView textViewtotalMembers;
    private ImageView imageView;

    private FirebaseFirestore firestore;

    public static void chooseMethodAnnouncement(final Context context) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") final View dialogView = Objects.requireNonNull(inflater).inflate(R.layout.dialog_record_audio, null);
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
                context.startActivity(new Intent(context, CreateAnnouncementActivity.class));
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

    public static void getExistingExpenses(final Context context, String dahiraID) {
        if (listExpenses == null)
            listExpenses = new ArrayList<>();
        else
            listExpenses.clear();

        if (onlineUser.getListDahiraID().contains(dahiraID)) {
            showProgressDialog(context, "Chargement de vos depenses en cours ...");
            FirebaseFirestore.getInstance().collection("dahiras")
                    .document(dahiraID)
                    .collection("expenses").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            dismissProgressDialog();
                            if (!queryDocumentSnapshots.isEmpty()) {
                                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    Expense expense = documentSnapshot.toObject(Expense.class);
                                    listExpenses.add(expense);
                                }
                                Log.d(TAG, "Expenses downloaded");
                            }
                            if (objNotification == null && listExpenses.size() > 0)
                                context.startActivity(new Intent(context, ShowExpenseActivity.class));
                            else {
                                context.startActivity(new Intent(context, ShowExpenseActivity.class));
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dismissProgressDialog();
                            toastMessage(context, "erreur: " + e.getMessage());
                            Log.d(TAG, "Error downloading Expenses");
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, ShowDahiraActivity.class));
        finish();
    }

    private static void getMyEvents(final Context context) {
        if (myListEvents == null && MyStaticVariables.firestore != null) {
            myListEvents = new ArrayList<>();
            showProgressDialog(context, "Chargement des evenements en cours ...");

            MyStaticVariables.firestore.collection("dahiras")
                    .document(dahira.getDahiraID())
                    .collection("myEvents")
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    dismissProgressDialog();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Event event = documentSnapshot.toObject(Event.class);
                            myListEvents.add(event);
                        }
                        if (displayEvent.equals("myEvents") && myListEvents.size() > 0)
                            context.startActivity(new Intent(context, ShowEventActivity.class));
                        else if (myListEvents.size() <= 0) {
                            showAlertDialog(context, "La liste des evenements est vide!");
                        }

                        Log.d(TAG, "Even downloaded");
                    }
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dismissProgressDialog();
                            Log.d(TAG, "Error downloading event");
                        }
                    });
        } else if (displayEvent.equals("myEvents") && myListEvents.size() > 0)
            context.startActivity(new Intent(context, ShowEventActivity.class));
        else if (myListEvents.size() <= 0) {
            showAlertDialog(context, "La liste des evenements est vide!");
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

    public void getListUser() {
        showProgressBar();
        if (listUser == null) {
            listUser = new ArrayList<>();
            firestore.collection("users").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            hideProgressBar();
                            if (!queryDocumentSnapshots.isEmpty()) {
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot documentSnapshot : list) {
                                    User user = documentSnapshot.toObject(User.class);
                                    if (Objects.requireNonNull(user).getListDahiraID().contains(dahira.getDahiraID())) {
                                        listUser.add(user);
                                    }
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    hideProgressBar();
                }
            });
        }
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
    public void onClick(View v) {
        if (v.getId() == R.id.button_back) {
            startActivity(new Intent(this, ShowDahiraActivity.class));
            finish();
        }
    }

    private void callDahira() {
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

    @SuppressLint("SetTextI18n")
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

        findViewById(R.id.button_back).setOnClickListener(this);

        initViewsProgressBar();
    }

    @SuppressLint("SetTextI18n")
    public void displayView() {
        textViewDahiraName.setText("Dahira " + dahira.getDahiraName());
        textViewDieuwrine.setText("Dieuwrine: " + dahira.getDieuwrine());
        textViewSiege.setText("Siège : " + dahira.getSiege());
        textViewPhoneNumber.setText("Téléphone: " + dahira.getDahiraPhoneNumber());
        textViewtotalMembers.setText("Nombre de participants: " + dahira.getTotalMember());
        textViewTotalAdiya.setText("Total Adiya dans la caisse: " + dahira.getTotalAdiya() + " FCFA");
        textViewTotalSass.setText("Total Sass dans la caisse: " + dahira.getTotalSass() + " FCFA");
        textViewTotalSocial.setText("Total Social dans la caisse: " + dahira.getTotalSocial() + " FCFA");
        showImage(this, dahira.getImageUri(), imageView);

        if (!onlineUser.getListDahiraID().contains(dahira.getDahiraID())) {
            textViewTotalAdiya.setVisibility(View.GONE);
            textViewTotalSass.setVisibility(View.GONE);
            textViewTotalSocial.setVisibility(View.GONE);
            textViewtotalMembers.setVisibility(View.GONE);
        }
    }

    public void setDrawerMenu() {
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
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        showImage(this, onlineUser.getImageUri(), navImageView);
        textViewNavUserName.setText(onlineUser.getUserName());
        textViewNavEmail.setText(onlineUser.getEmail());
        navigationView.setCheckedItem(R.id.nav_displayMyDahira);
        hideMenuItem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dahira_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkInternetConnection(this);

        init();

        firestore = FirebaseFirestore.getInstance();


        //********************** Drawer Menu ***************************************
        setDrawerMenu();
        //**************************************************************************

        displayView();

        new MyTask().execute();

        HomeActivity.loadBannerAd(this);
    }

    public void hideMenuItem() {
        Menu nav_Menu = navigationView.getMenu();
        nav_Menu.findItem(R.id.nav_setting).setTitle("Modifier mon dahira");

        if (onlineUser != null) {
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
                nav_Menu.findItem(R.id.nav_removeDahira).setVisible(false);
            } else if (onlineUser.getListRoles().size() > indexOnlineUser &&
                    !onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur")) {
                nav_Menu.findItem(R.id.nav_setting).setVisible(false);
                nav_Menu.findItem(R.id.nav_addEvent).setVisible(false);
                nav_Menu.findItem(R.id.nav_addExpense).setVisible(false);
                nav_Menu.findItem(R.id.nav_addUser).setVisible(false);
            }
        }

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

    private void initViewsProgressBar() {
        relativeLayoutData = findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.destroy();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.resume();
        }
    }

    private void removeDahiraFromUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DahiraInfoActivity.this, R.style.alertDialog);
        builder.setTitle("Se desabonner du dahira " + dahira.getDahiraName());
        builder.setMessage("Etes vous sure de vouloir quitter le dahira " + dahira.getDahiraName() + "?");
        builder.setCancelable(false);
        builder.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                onlineUser.getListSocial().remove(indexOnlineUser);
                onlineUser.getListSass().remove(indexOnlineUser);
                onlineUser.getListAdiya().remove(indexOnlineUser);
                onlineUser.getListCommissions().remove(indexOnlineUser);
                onlineUser.getListRoles().remove(indexOnlineUser);
                onlineUser.getListUpdatedDahiraID().remove(indexOnlineUser);
                onlineUser.getListDahiraID().remove(indexOnlineUser);
                updateUser(DahiraInfoActivity.this);
                checkIfDahiraCanBeDeleted(DahiraInfoActivity.this);
            }
        });

        builder.setNegativeButton("NON", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    private void updateUser(final Context context) {
        showProgressDialog(context, "Patientez svp ...");
        firestore.collection("users").document(onlineUser.getUserID())
                .update("listDahiraID", onlineUser.getListDahiraID(),
                        "listUpdatedDahiraID", onlineUser.getListUpdatedDahiraID(),
                        "listCommissions", onlineUser.getListCommissions(),
                        "listAdiya", onlineUser.getListAdiya(),
                        "listSass", onlineUser.getListSass(),
                        "listSocial", onlineUser.getListSocial(),
                        "listRoles", onlineUser.getListRoles())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dismissProgressDialog();
                        displayDahira = "myDahira";
                        myListDahira.clear();
                        myListDahira = null;

                        Intent intent = new Intent(context, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        showAlertDialog(context, "Desabonnement reussi.", intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dismissProgressDialog();
                startActivity(new Intent(context, UserInfoActivity.class));
            }
        });
    }

    private void checkIfDahiraCanBeDeleted(final Context context) {
        showProgressBar();
        firestore.collection("users").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        hideProgressBar();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : list) {
                                User user = documentSnapshot.toObject(User.class);
                                if (Objects.requireNonNull(user).getListDahiraID().contains(dahira.getDahiraID()) &&
                                        !user.getUserID().equals(onlineUser.getUserID())) {
                                    isDahiraEmpty = false;
                                }
                            }
                            if (isDahiraEmpty) {
                                deleteDocument(context, "dahiras", dahira.getDahiraID());
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressBar();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.nav_home:
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                break;

            case R.id.nav_displayUsers:
                actionSelected = "displayUsers";
                startActivity(new Intent(this, ShowUserActivity.class));
                break;

            case R.id.nav_searchUser:
                actionSelected = "searchUser";
                startActivity(new Intent(this, ShowUserActivity.class));
                break;

            case R.id.nav_addUser:
                actionSelected = "addNewMember";
                startActivity(new Intent(this, ShowUserActivity.class));
                break;

            case R.id.nav_displayMyDahira:
                MyStaticVariables.displayDahira = "myDahira";
                startActivity(new Intent(this, ShowDahiraActivity.class));
                break;

            case R.id.nav_displayAllDahira:
                MyStaticVariables.displayDahira = "allDahira";
                startActivity(new Intent(this, ShowDahiraActivity.class));
                break;

            case R.id.nav_addExpense:
                startActivity(new Intent(this, CreateExpenseActivity.class));
                break;

            case R.id.nav_displayExpenses:
                HomeActivity.showInterstitialAd(this);
                getExistingExpenses(DahiraInfoActivity.this, dahira.getDahiraID());
                break;

            case R.id.nav_addAnnouncement:
                chooseMethodAnnouncement(DahiraInfoActivity.this);
                break;

            case R.id.nav_displayAnnouncement:
                HomeActivity.showInterstitialAd(this);
                startActivity(new Intent(this, ShowAnnouncementActivity.class));
                break;

            case R.id.nav_addEvent:
                displayEvent = "";
                getMyEvents(this);
                getAllEvents(this);
                startActivity(new Intent(this, CreateEventActivity.class));
                break;

            case R.id.nav_displayEvent:
                HomeActivity.showInterstitialAd(this);
                displayEvent = "myEvents";
                getAllEvents(this);
                getMyEvents(this);
                break;

            case R.id.nav_photo:
                HomeActivity.showInterstitialAd(this);
                startActivity(new Intent(DahiraInfoActivity.this, ShowImagesActivity.class));
                break;

            case R.id.nav_audio:
                HomeActivity.showInterstitialAd(this);
                startActivity(new Intent(DahiraInfoActivity.this, ShowSongsActivity.class));
                break;

            case R.id.nav_video:
                HomeActivity.showInterstitialAd(this);
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

            case R.id.nav_removeDahira:
                removeDahiraFromUser();
                break;

            case R.id.nav_logout:
                logout(this);
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @SuppressLint("StaticFieldLeak")
    class MyTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            showProgressBar();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getListUser();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hideProgressBar();
        }
    }
}
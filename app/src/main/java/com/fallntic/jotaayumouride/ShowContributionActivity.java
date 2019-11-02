package com.fallntic.jotaayumouride;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Adapter.ContributionAdapter;
import com.fallntic.jotaayumouride.Utility.SwipeToDeleteCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.fallntic.jotaayumouride.Adapter.ContributionAdapter.getListAmount;
import static com.fallntic.jotaayumouride.Adapter.ContributionAdapter.getListDate;
import static com.fallntic.jotaayumouride.Adapter.ContributionAdapter.getListUserName;
import static com.fallntic.jotaayumouride.AddContributionActivity.notifyUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.boolAddToDahira;
import static com.fallntic.jotaayumouride.Utility.DataHolder.dahira;
import static com.fallntic.jotaayumouride.Utility.DataHolder.indexSelectedUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.selectedUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.Utility.DataHolder.showProgressDialog;
import static com.fallntic.jotaayumouride.Utility.DataHolder.typeOfContribution;
import static com.fallntic.jotaayumouride.Utility.DataHolder.updateDocument;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.adiya;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.sass;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.social;

public class ShowContributionActivity extends AppCompatActivity {

    private TextView textViewTitle;

    private CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerViewContribution;
    private ContributionAdapter contributionAdapter;
    private List<String> listAmountAdiya;
    private List<String> listDateAdiya;
    private List<String> listUserName;
    private List<String> listDahiraID;
    private double amountDeleted = 0.00, currentAmount = 0.00;
    private Toolbar toolbar;

    public static void uploadContribution(final Context context, final String collectionName, Object data, final String notification_message) {
        showProgressDialog(context, "Enregistrement " + collectionName + " en cours ...");
        FirebaseFirestore.getInstance().collection(collectionName).document(selectedUser.getUserID())
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        notifyUser(context, notification_message);
                        showAlertDialog(context, typeOfContribution + " ajoute avec succe!");
                        Log.d(TAG, "New collection " + collectionName + " set successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error initContributions function line 351");
                    }
                });
    }

    private void initViews() {
        recyclerViewContribution = findViewById(R.id.recyclerview_contribution);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        textViewTitle = findViewById(R.id.textView_title);
    }

    private void displayViews() {
        textViewTitle.setText("Dahira " + dahira.getDahiraName() +
                "\nListe des " + typeOfContribution + "s verses par " + selectedUser.getUserName());
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, UserInfoActivity.class));
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_contribution);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setLogo(R.mipmap.logo);
        setSupportActionBar(toolbar);

        initViews();

        checkInternetConnection(this);

        displayViews();

        showListContribution();

        enableSwipeToDeleteAndUndo();

        HomeActivity.loadBannerAd(this, this);

    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                final int position = viewHolder.getAdapterPosition();
                final String mDate = getListDate().get(position);
                final String userName = getListUserName().get(position);
                final String amount = getListAmount().get(position);

                amountDeleted = Double.parseDouble(amount);


                AlertDialog.Builder builder = new AlertDialog.Builder(ShowContributionActivity.this, R.style.alertDialog);
                builder.setTitle("Supprimer " + typeOfContribution + "!");
                builder.setMessage("Etes vous sure de vouloir supprimer cet " + typeOfContribution + "?");
                builder.setCancelable(false);
                builder.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        contributionAdapter.removeItem(position);
                        //Remove item in FirebaseFireStore
                        boolAddToDahira = true;
                        boolAddToDahira = false;

                        Snackbar snackbar = null;
                        String notification_message = "Une somme de " + amount + "FCFA a ete supprimee dans votre " + "compte " + typeOfContribution + " par " + onlineUser.getUserName();
                        if (typeOfContribution.equals("adiya")) {
                            int index = 0;
                            for (String id_dahira : adiya.getListDahiraID()) {
                                if (id_dahira.equals(dahira.getDahiraID())) {
                                    if (mDate.equals(adiya.getListDate().get(index)) && userName.equals(adiya.getListUserName().get(index)) && amount.equals(adiya.getListAdiya().get(index))) {
                                        adiya.getListDate().remove(index);
                                        adiya.getListAdiya().remove(index);
                                        adiya.getListUserName().remove(index);
                                        adiya.getListDahiraID().remove(index);
                                        break;
                                    }
                                }
                                index++;
                            }

                            currentAmount = Double.parseDouble(selectedUser.getListAdiya().get(indexSelectedUser));
                            selectedUser.getListAdiya().set(indexSelectedUser, String.valueOf(currentAmount - amountDeleted));
                            if (onlineUser.getUserID().equals(selectedUser.getUserID()))
                                onlineUser.getListAdiya().set(indexSelectedUser, String.valueOf(currentAmount - amountDeleted));

                            currentAmount = Double.parseDouble(dahira.getTotalAdiya());
                            dahira.setTotalAdiya(String.valueOf(currentAmount - amountDeleted));

                            //***************************************************************Update Firebase************************************************************
                            updateDocument(ShowContributionActivity.this, "users", selectedUser.getUserID(), "listAdiya", selectedUser.getListAdiya());
                            updateDocument(ShowContributionActivity.this, "dahiras", dahira.getDahiraID(), "totalAdiya", dahira.getTotalAdiya());
                            uploadContribution(ShowContributionActivity.this, "adiya", adiya, notification_message);

                            snackbar = Snackbar.make(coordinatorLayout, "Adiya supprime.", Snackbar.LENGTH_LONG);
                        } else if (typeOfContribution.equals("sass")) {

                            int index = 0;
                            for (String id_dahira : sass.getListDahiraID()) {
                                if (id_dahira.equals(dahira.getDahiraID())) {
                                    if (mDate.equals(sass.getListDate().get(index)) && userName.equals(sass.getListUserName().get(index)) && amount.equals(sass.getListSass().get(index))) {
                                        sass.getListDate().remove(index);
                                        sass.getListSass().remove(index);
                                        sass.getListUserName().remove(index);
                                        sass.getListDahiraID().remove(index);
                                        break;
                                    }
                                }
                                index++;
                            }

                            currentAmount = Double.parseDouble(selectedUser.getListSass().get(indexSelectedUser));
                            if (onlineUser.getUserID().equals(selectedUser.getUserID()))
                                onlineUser.getListSass().set(indexSelectedUser, String.valueOf(currentAmount - amountDeleted));
                            selectedUser.getListSass().set(indexSelectedUser, String.valueOf(currentAmount - amountDeleted));

                            currentAmount = Double.parseDouble(dahira.getTotalSass());
                            dahira.setTotalSass(String.valueOf(currentAmount - amountDeleted));

                            //***************************************************************Update Firebase************************************************************
                            updateDocument(ShowContributionActivity.this, "users", selectedUser.getUserID(), "listSass", selectedUser.getListSass());
                            updateDocument(ShowContributionActivity.this, "dahiras", dahira.getDahiraID(), "totalSass", dahira.getTotalSass());
                            uploadContribution(ShowContributionActivity.this, "sass", sass, notification_message);

                            snackbar = Snackbar.make(coordinatorLayout, "Sass supprime.", Snackbar.LENGTH_LONG);
                        } else if (typeOfContribution.equals("social")) {

                            int index = 0;
                            for (String id_dahira : social.getListDahiraID()) {
                                if (id_dahira.equals(dahira.getDahiraID())) {
                                    if (mDate.equals(social.getListDate().get(index)) && userName.equals(social.getListUserName().get(index)) && amount.equals(social.getListSocial().get(index))) {
                                        social.getListDate().remove(index);
                                        social.getListSocial().remove(index);
                                        social.getListUserName().remove(index);
                                        social.getListDahiraID().remove(index);
                                        break;
                                    }
                                }
                                index++;
                            }

                            currentAmount = Double.parseDouble(selectedUser.getListSocial().get(indexSelectedUser));
                            selectedUser.getListSocial().set(indexSelectedUser, String.valueOf(currentAmount - amountDeleted));
                            if (onlineUser.getUserID().equals(selectedUser.getUserID()))
                                onlineUser.getListSocial().set(indexSelectedUser, String.valueOf(currentAmount - amountDeleted));

                            currentAmount = Double.parseDouble(dahira.getTotalSocial());
                            dahira.setTotalSocial(String.valueOf(currentAmount - amountDeleted));

                            //***************************************************************Update Firebase************************************************************
                            updateDocument(ShowContributionActivity.this, "users", selectedUser.getUserID(), "listSocial", selectedUser.getListSocial());
                            updateDocument(ShowContributionActivity.this, "dahiras", dahira.getDahiraID(), "totalSocial", dahira.getTotalSocial());
                            uploadContribution(ShowContributionActivity.this, "social", social, notification_message);

                            snackbar = Snackbar.make(coordinatorLayout, "Social supprime.", Snackbar.LENGTH_LONG);
                        }
                        snackbar.setActionTextColor(Color.YELLOW);
                        snackbar.show();

                    }
                });

                builder.setNegativeButton("NON", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.show();
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerViewContribution);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem iconBack;
        iconBack = menu.findItem(R.id.icon_back);
        iconBack.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.icon_back:
                finish();
                startActivity(new Intent(this, UserInfoActivity.class));
                break;

            case R.id.instructions:
                startActivity(new Intent(this, InstructionsActivity.class));
                break;
        }
        finish();
        return true;
    }

    private void showListContribution() {

        //Attach adapter to recyclerView
        recyclerViewContribution.setHasFixedSize(true);
        recyclerViewContribution.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewContribution.setVisibility(View.VISIBLE);
        listAmountAdiya = new ArrayList<>();
        listDateAdiya = new ArrayList<>();
        listUserName = new ArrayList<>();
        listDahiraID = new ArrayList<>();
        contributionAdapter = new ContributionAdapter(this, listDateAdiya,
                listAmountAdiya, listUserName);

        recyclerViewContribution.setAdapter(contributionAdapter);

        int index = 0;
        Intent intent = new Intent(this, UserInfoActivity.class);
        if (typeOfContribution.equals("adiya")) {
            if (adiya != null && adiya.getListDahiraID() != null) {
                for (String id_dahira : adiya.getListDahiraID()) {
                    if (id_dahira.equals(dahira.getDahiraID())) {
                        listDateAdiya.add(adiya.getListDate().get(index));
                        listAmountAdiya.add(adiya.getListAdiya().get(index));
                        listUserName.add(adiya.getListUserName().get(index));
                        listDahiraID.add(adiya.getListDahiraID().get(index));
                    }
                    index++;
                }
            } else {
                showAlertDialog(this, selectedUser.getUserName() + " n'a aucun " +
                        typeOfContribution + " enregistre!", intent);
            }
        } else if (typeOfContribution.equals("sass")) {
            if (sass != null && sass.getListDahiraID() != null) {
                for (String id_dahira : sass.getListDahiraID()) {
                    if (id_dahira.equals(dahira.getDahiraID())) {
                        listDateAdiya.add(sass.getListDate().get(index));
                        listAmountAdiya.add(sass.getListSass().get(index));
                        listUserName.add(sass.getListUserName().get(index));
                        listDahiraID.add(sass.getListDahiraID().get(index));
                    }
                    index++;
                }
            } else {
                showAlertDialog(this, selectedUser.getUserName() + " n'a aucun " +
                        typeOfContribution + " enregistre!", intent);
                return;
            }
        } else if (typeOfContribution.equals("social")) {
            if (social != null && social.getListDahiraID() != null) {
                for (String id_dahira : social.getListDahiraID()) {
                    if (id_dahira.equals(dahira.getDahiraID())) {
                        listDateAdiya.add(social.getListDate().get(index));
                        listAmountAdiya.add(social.getListSocial().get(index));
                        listUserName.add(social.getListUserName().get(index));
                        listDahiraID.add(social.getListDahiraID().get(index));
                    }
                    index++;
                }
            } else {
                showAlertDialog(this, selectedUser.getUserName() + " n'a aucun " +
                        typeOfContribution + " enregistre!", intent);
            }
        }

        contributionAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.resume();
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
    protected void onDestroy() {
        super.onDestroy();
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.destroy();
        }
    }
}

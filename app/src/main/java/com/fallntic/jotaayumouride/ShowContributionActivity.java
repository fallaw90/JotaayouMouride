package com.fallntic.jotaayumouride;

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
import static com.fallntic.jotaayumouride.AddContributionActivity.updateContribution;
import static com.fallntic.jotaayumouride.UserInfoActivity.getAdiya;
import static com.fallntic.jotaayumouride.UserInfoActivity.getSass;
import static com.fallntic.jotaayumouride.UserInfoActivity.getSocial;
import static com.fallntic.jotaayumouride.Utility.DataHolder.adiya;
import static com.fallntic.jotaayumouride.Utility.DataHolder.boolAddToDahira;
import static com.fallntic.jotaayumouride.Utility.DataHolder.dahira;
import static com.fallntic.jotaayumouride.Utility.DataHolder.indexOnlineUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.indexSelectedUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.sass;
import static com.fallntic.jotaayumouride.Utility.DataHolder.selectedUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.Utility.DataHolder.social;
import static com.fallntic.jotaayumouride.Utility.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.Utility.DataHolder.typeOfContribution;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.checkInternetConnection;

public class ShowContributionActivity extends AppCompatActivity {

    private TextView textViewTitle;

    private CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerViewContribution;
    private ContributionAdapter contributionAdapter;
    private List<String> listAmountAdiya;
    private List<String> listDateAdiya;
    private List<String> listUserName;
    private double amountDeleted = 0.00, currentAmount = 0.00;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_contribution);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setLogo(R.mipmap.logo);
        setSupportActionBar(toolbar);

        initViews();

        checkInternetConnection(this);

        displayViews();

        getAdiya();
        getSass();
        getSocial();

        showListContribution();
        enableSwipeToDeleteAndUndo();

    }

    private void initViews(){
        recyclerViewContribution = findViewById(R.id.recyclerview_contribution);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        textViewTitle = findViewById(R.id.textView_title);
    }

    private void displayViews(){
        textViewTitle.setText("Dahira " + dahira.getDahiraName() +
                "\nListe des " + typeOfContribution + "s verses par " + selectedUser.getUserName());
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, UserInfoActivity.class));
        super.onBackPressed();
    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                final int position = viewHolder.getAdapterPosition();
                final String mDate = getListDate().get(position);
                final String userName;

                if (getListUserName().isEmpty() && getListUserName().size() > position)
                    userName = getListUserName().get(position);
                else
                    userName = "inconnu";

                amountDeleted = Double.parseDouble(getListAmount().get(position));


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
                        updateContribution(ShowContributionActivity.this, typeOfContribution,
                                selectedUser.getUserID(), String.valueOf(amountDeleted));

                        Snackbar snackbar = null;
                        if (typeOfContribution.equals("adiya")) {
                            currentAmount = Double.parseDouble(selectedUser.getListAdiya().get(indexSelectedUser));
                            selectedUser.getListAdiya().set(indexOnlineUser, String.valueOf(currentAmount - amountDeleted));
                            snackbar = Snackbar.make(coordinatorLayout,
                                    "Adiya supprime.", Snackbar.LENGTH_LONG);
                        } else if (typeOfContribution.equals("sass")) {
                            currentAmount = Double.parseDouble(selectedUser.getListSass().get(indexSelectedUser));
                            selectedUser.getListSass().set(indexOnlineUser, String.valueOf(currentAmount - amountDeleted));
                            snackbar = Snackbar.make(coordinatorLayout,
                                    "Sass supprime.", Snackbar.LENGTH_LONG);
                        } else if (typeOfContribution.equals("social")) {
                            currentAmount = Double.parseDouble(selectedUser.getListSocial().get(indexSelectedUser));
                            selectedUser.getListSocial().set(indexOnlineUser, String.valueOf(currentAmount - amountDeleted));
                            snackbar = Snackbar.make(coordinatorLayout,
                                    "Social supprime.", Snackbar.LENGTH_LONG);
                        }
                        updateUserContribution();
                        snackbar.setActionTextColor(Color.YELLOW);
                        snackbar.show();

                    }
                });

                builder.setNegativeButton("NON", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*
                        startActivity(new Intent(ShowContributionActivity.this,
                                ShowContributionActivity.class));
                                */
                    }
                });
                builder.show();


            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerViewContribution);
    }

    private void showListContribution() {

        //Attach adapter to recyclerView
        recyclerViewContribution.setHasFixedSize(true);
        recyclerViewContribution.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewContribution.setVisibility(View.VISIBLE);
        listAmountAdiya = new ArrayList<>();
        listDateAdiya = new ArrayList<>();
        listUserName = new ArrayList<>();
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

    public void updateUserContribution() {
        FirebaseFirestore.getInstance().collection("users").document(selectedUser.getUserID())
                .set(selectedUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        toastMessage(ShowContributionActivity.this, typeOfContribution + " supprime avec succe!");
                        Log.d(TAG, typeOfContribution + " set successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        toastMessage(ShowContributionActivity.this,
                                "Erreur " + e.getMessage() + " suppression " + typeOfContribution);
                        Log.d(TAG, "Error initContributions function line 351");
                    }
                });
    }
}

package com.fallntic.jotaayumouride;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Adapter.ContributionAdapter;
import com.fallntic.jotaayumouride.Utility.SwipeToDeleteCallback;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

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
import static com.fallntic.jotaayumouride.Utility.DataHolder.sass;
import static com.fallntic.jotaayumouride.Utility.DataHolder.selectedUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.Utility.DataHolder.social;
import static com.fallntic.jotaayumouride.Utility.DataHolder.typeOfContribution;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.checkInternetConnection;

public class ListContributionActivity extends AppCompatActivity {

    private TextView textViewTitle;

    private CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerViewContribution;
    private ContributionAdapter contributionAdapter;
    private List<String> listAmountAdiya;
    private List<String> listDateAdiya;
    private List<String> listUserName;
    private String amountDeleted;
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

                amountDeleted = getListAmount().get(position);

                contributionAdapter.removeItem(position);

                boolAddToDahira = false;
                updateContribution(ListContributionActivity.this, typeOfContribution,
                        selectedUser.getUserID(), amountDeleted);

                Snackbar snackbar = null;
                if (typeOfContribution.equals("adiya")) {
                    snackbar = Snackbar.make(coordinatorLayout,
                            "Adiya supprime.", Snackbar.LENGTH_LONG);
                } else if (typeOfContribution.equals("sass")) {
                    snackbar = Snackbar.make(coordinatorLayout,
                            "Sass supprime.", Snackbar.LENGTH_LONG);
                } else if (typeOfContribution.equals("social")) {
                    snackbar = Snackbar.make(coordinatorLayout,
                            "Social supprime.", Snackbar.LENGTH_LONG);
                }

                if (snackbar != null) {
                    snackbar.setAction("Annuler la suppression", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            boolAddToDahira = true;
                            updateContribution(ListContributionActivity.this, typeOfContribution,
                                    selectedUser.getUserID(), amountDeleted);

                            contributionAdapter.restoreItem(position, mDate, amountDeleted, userName);
                            recyclerViewContribution.scrollToPosition(position);
                        }
                    });
                    snackbar.setActionTextColor(Color.YELLOW);
                    snackbar.show();
                }
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
}

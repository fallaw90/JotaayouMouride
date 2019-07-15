package com.fallntic.jotaayumouride;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.hasValidationErrors;
import static com.fallntic.jotaayumouride.DataHolder.logout;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.selectedUser;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.showProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;
import static com.fallntic.jotaayumouride.DataHolder.typeOfContribution;

public class ListContributionActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewDahiraName;
    private TextView textViewUserName;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private RecyclerView recyclerViewContribution;
    private ContributionAdapter contributionAdapter;
    private List<String> listAmountAdiya;
    private List<String> listDateAdiya;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_contribution);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("List " + typeOfContribution + " verse");
        setSupportActionBar(toolbar);

        if (!DataHolder.isConnected(this)){
            toastMessage(this, "Oops! Vous n'avez pas de connexion internet.");
            finish();
        }

        recyclerViewContribution = findViewById(R.id.recyclerview_contribution);

        textViewUserName = findViewById(R.id.textView_userName);
        textViewDahiraName = findViewById(R.id.textView_dahiraName);
        textViewUserName.setText(selectedUser.getUserName());
        textViewDahiraName.setText("Membre du dahira " + dahira.getDahiraName());

        showListContribution();

        findViewById(R.id.button_back).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){

            case R.id.button_back:
                startActivity(new Intent(ListContributionActivity.this, UserInfoActivity.class));
                break;
        }
    }

    private void showListContribution() {

        //Attach adapter to recyclerView
        recyclerViewContribution.setHasFixedSize(true);
        recyclerViewContribution.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewContribution.setVisibility(View.VISIBLE);
        listAmountAdiya = new ArrayList<>();
        listDateAdiya = new ArrayList<>();
        contributionAdapter = new ContributionAdapter(this, listDateAdiya, listAmountAdiya);
        recyclerViewContribution.setAdapter(contributionAdapter);

        showProgressDialog(this, "Chargement de vos " +typeOfContribution+ " en cours ...");
        db.collection(typeOfContribution).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        dismissProgressDialog();
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : list) {
                                if (documentSnapshot.getId().equals(selectedUser.getUserID())){
                                    int index = 0;
                                    if (typeOfContribution.equals("adiya")){
                                        Adiya adiya = documentSnapshot.toObject(Adiya.class);
                                        for (String id_dahira : adiya.getListDahiraID()) {
                                            if (id_dahira.equals(dahira.getDahiraID())) {
                                                listDateAdiya.add(adiya.getListDate().get(index));
                                                listAmountAdiya.add(adiya.getListAdiya().get(index));
                                            }
                                            index++;
                                        }
                                    }
                                    if (typeOfContribution.equals("sass")){
                                        Sass sass = documentSnapshot.toObject(Sass.class);
                                        for (String id_dahira : sass.getListDahiraID()) {
                                            if (id_dahira.equals(dahira.getDahiraID())) {
                                                listDateAdiya.add(sass.getListDate().get(index));
                                                listAmountAdiya.add(sass.getListSass().get(index));
                                            }
                                            index++;
                                        }
                                    }
                                    if (typeOfContribution.equals("social")){
                                        Social social = documentSnapshot.toObject(Social.class);
                                        for (String id_dahira : social.getListDahiraID()) {
                                            if (id_dahira.equals(dahira.getDahiraID())) {
                                                listDateAdiya.add(social.getListDate().get(index));
                                                listAmountAdiya.add(social.getListSocial().get(index));
                                            }
                                            index++;
                                        }
                                    }
                                }
                            }
                            contributionAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list_contribution, menu);

        MenuItem menuItemAddContribution;
        menuItemAddContribution = menu.findItem(R.id.addContribution);
        menuItemAddContribution.setTitle("Ajouter " + typeOfContribution);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addContribution:

                break;

            case R.id.logout:
                logout();
                finish();
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }
        return true;
    }
}

package com.fallntic.jotaayumouride;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Adapter.DahiraAdapter;
import com.fallntic.jotaayumouride.Model.Dahira;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.showAlertDialog;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.actionSelected;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.displayDahira;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listAllDahira;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listDahiraFound;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.myListDahira;

public class ShowDahiraActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewTitle;
    private static RecyclerView recyclerViewDahira;
    private DahiraAdapter dahiraAdapter;

    public static void searchDahira(Context context, final String searchName) {

        if (listDahiraFound == null)
            listDahiraFound = new ArrayList<>();
        else
            listDahiraFound.clear();

        if (listAllDahira == null) {
            listAllDahira = new ArrayList<>();
        }

        for (Dahira dahira : listAllDahira) {
            if (searchName != null && !searchName.equals("")) {
                String[] splitSearchName = searchName.split(" ");
                String dahiraName = dahira.getDahiraName();
                dahiraName = dahiraName.toLowerCase();
                for (String search : splitSearchName) {
                    search = search.toLowerCase();
                    if (dahiraName.contains(search) && !listDahiraFound.contains(dahira)) {
                        listDahiraFound.add(dahira);
                    }
                }
            }
        }
        if (listDahiraFound.isEmpty()) {
            Intent intent = new Intent(context, HomeActivity.class);
            showAlertDialog(context, "Dahira non trouve.", intent);
        } else {
            displayDahira = "searchDahira";
            Intent intent = new Intent(context, ShowDahiraActivity.class);
            context.startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_back:
                finish();
                startActivity(new Intent(this, HomeActivity.class));
                break;
        }
    }

    public static void dialogSearchDahira(final Context context) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.dialog_search, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        final EditText editTextDialogName = dialogView.findViewById(R.id.editText_dialogName);
        final EditText editTextDialogPhoneNumber = dialogView.findViewById(R.id.editText_dialogPhoneNumber);
        final TextView textView = dialogView.findViewById(R.id.textView_dialogOr);
        final CountryCodePicker ccp = dialogView.findViewById(R.id.ccp);
        Button buttonSearch = dialogView.findViewById(R.id.button_dialogSearch);
        Button buttonCancel = dialogView.findViewById(R.id.button_cancel);

        editTextDialogName.setHint("Nom du dahira");
        editTextDialogPhoneNumber.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
        ccp.setVisibility(View.GONE);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setTitle("Rechercher un dahira");
        alertDialog.show();

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dahiraName = editTextDialogName.getText().toString().trim();

                if (dahiraName.isEmpty()) {
                    editTextDialogName.setError("Donner le nom du dahira!");
                    editTextDialogName.requestFocus();
                    return;
                } else {
                    searchDahira(context, dahiraName);
                    alertDialog.dismiss();
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                if (displayDahira.equals("searchDahira")) {
                    Intent intent = new Intent(context, HomeActivity.class);
                    context.startActivity(intent);
                    displayDahira = "";
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        actionSelected = "";
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.destroy();
        }
        super.onDestroy();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(this, HomeActivity.class));
    }

    private void displayDahiras(List<Dahira> listDahira) {
        if (listDahira != null && listDahira.size() > 0) {
            Collections.sort(listDahira);
            //Attach adapter to recyclerView
            recyclerViewDahira.setHasFixedSize(true);
            recyclerViewDahira.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewDahira.setVisibility(View.VISIBLE);
            dahiraAdapter = new DahiraAdapter(this, listDahira);
            recyclerViewDahira.setAdapter(dahiraAdapter);
            dahiraAdapter.notifyDataSetChanged();
        } else {
            startActivity(new Intent(this, HomeActivity.class));
        }
    }

    private void init() {

        textViewTitle = findViewById(R.id.textView_title);
        recyclerViewDahira = findViewById(R.id.recyclerview_dahiras);
        findViewById(R.id.button_back).setOnClickListener(this);
        //ProgressBar from static variable MainActivity
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_dahira);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        //toolbar.setLogo(R.mipmap.logo);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.logo);


        checkInternetConnection(this);

        init();

        if (displayDahira != null) {
            if (displayDahira.equals("searchDahira")) {
                textViewTitle.setText("Dahiras trouves");
                displayDahiras(listDahiraFound);
            } else if (displayDahira.equals("myDahira")) {
                textViewTitle.setText("Liste des dahiras dont vous etes membre. Cliquez sur un dahira pour continuer.");
                displayDahiras(myListDahira);
            } else if (displayDahira.equals("allDahira")) {
                textViewTitle.setText("Liste des dahiras enregistre. Cliquez sur un dahira pour continuer.");
                displayDahiras(listAllDahira);
            }
        }

        HomeActivity.loadBannerAd(this, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem iconAdd, search_dahira;
        iconAdd = menu.findItem(R.id.icon_add);
        search_dahira = menu.findItem(R.id.search_dahira);

        search_dahira.setVisible(true);
        iconAdd.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                startActivity(new Intent(this, HomeActivity.class));
                break;

            case R.id.search_dahira:
                dialogSearchDahira(this);
                break;

            case R.id.icon_add:
                startActivity(new Intent(this, CreateDahiraActivity.class));
                break;

            case R.id.instructions:
                startActivity(new Intent(this, InstructionsActivity.class));
                break;
        }
        return true;
    }
}

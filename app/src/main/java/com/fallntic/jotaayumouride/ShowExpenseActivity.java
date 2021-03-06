package com.fallntic.jotaayumouride;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.adapter.ExpenseAdapter;
import com.fallntic.jotaayumouride.model.Expense;
import com.fallntic.jotaayumouride.utility.MyStaticFunctions;
import com.fallntic.jotaayumouride.utility.MyStaticVariables;
import com.fallntic.jotaayumouride.utility.SwipeToDeleteCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.dahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listExpenses;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.objNotification;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.relativeLayoutProgressBar;

@SuppressWarnings("unused")
public class ShowExpenseActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "ShowExpenseActivity";

    private TextView textViewTitle, textViewDelete;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerViewExpense;

    private CoordinatorLayout coordinatorLayout;
    private ExpenseAdapter expenseAdapter;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_expense);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        //toolbar.setLogo(R.mipmap.logo);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.logo);

        checkInternetConnection(this);

        initViews();

        textViewTitle.setText("Liste des depense du dahira " + MyStaticVariables.dahira.getDahiraName());

        showListExpenses(listExpenses);

        enableSwipeToDelete(this);

        HomeActivity.loadBannerAd(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_back) {
            if (objNotification != null) {
                objNotification = null;
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } else {
                startActivity(new Intent(this, DahiraInfoActivity.class));
                finish();
            }
        }
    }

    private void initViews() {
        recyclerViewExpense = findViewById(R.id.recyclerview_expense);
        textViewTitle = findViewById(R.id.textView_title);
        textViewDelete = findViewById(R.id.textView_deleteInstruction);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        findViewById(R.id.button_back).setOnClickListener(this);
        initViewsProgressBar();
    }

    private void initViewsProgressBar() {
        relativeLayoutData = findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem iconAdd;
        iconAdd = menu.findItem(R.id.icon_add);
        iconAdd.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;

            case R.id.icon_add:
                startActivity(new Intent(this, CreateExpenseActivity.class));
                break;

            case R.id.instructions:
                startActivity(new Intent(this, InstructionsActivity.class));
                break;

            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.destroy();
        }
        MyStaticFunctions.dismissProgressDialog();
        super.onDestroy();
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
    public void onBackPressed() {
        if (objNotification != null) {
            objNotification = null;
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } else
            startActivity(new Intent(ShowExpenseActivity.this, DahiraInfoActivity.class));
    }

    @SuppressLint("SetTextI18n")
    private void showListExpenses(List<Expense> listExpenses) {
        //Attach adapter to recyclerView
        if (listExpenses != null && listExpenses.size() > 0) {

            sortByDate();

            expenseAdapter = new ExpenseAdapter(this, listExpenses);

            recyclerViewExpense.setHasFixedSize(true);
            recyclerViewExpense.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewExpense.setVisibility(View.VISIBLE);

            recyclerViewExpense.setAdapter(expenseAdapter);
            expenseAdapter.notifyDataSetChanged();
        } else
            textViewDelete.setText("Vous n'avez aucune depense enregistree.");
    }

    private void enableSwipeToDelete(final Context context) {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                final int position = viewHolder.getAdapterPosition();
                final Expense expense = listExpenses.get(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(ShowExpenseActivity.this, R.style.alertDialog);
                builder.setTitle("Supprimer cette depense!");
                builder.setMessage("Etes vous sure de vouloir supprimer cette depense?");
                builder.setCancelable(false);
                builder.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        expenseAdapter.removeItem(position);
                        //Remove item in FirebaseFireStore
                        removeExpense(context, expense);
                    }
                });
                builder.setNegativeButton("NON", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(ShowExpenseActivity.this,
                                ShowExpenseActivity.class));
                    }
                });
                builder.show();
            }
        };
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerViewExpense);
    }

    private void removeExpense(final Context context, final Expense expense) {
        showProgressBar();
        db.collection("dahiras").document(dahira.getDahiraID())
                .collection("expenses").document(expense.getExpenseID())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideProgressBar();
                        CreateExpenseActivity.updateDahira(context, expense.getPrice(),
                                expense.getTypeOfExpense(), true, expense);

                        Snackbar snackbar = Snackbar.make(coordinatorLayout,
                                "Depense supprimee.", Snackbar.LENGTH_LONG);
                        snackbar.setActionTextColor(Color.GREEN);
                        snackbar.show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressBar();
                Snackbar snackbar = Snackbar.make(coordinatorLayout,
                        "Erreur de la suppression de votre depense. " + e.getMessage(), Snackbar.LENGTH_LONG);
                snackbar.setActionTextColor(Color.GREEN);
                snackbar.show();
                startActivity(new Intent(ShowExpenseActivity.this, ShowExpenseActivity.class));
            }
        });
    }

    private void sortByDate(){
        Collections.sort(listExpenses, new Comparator<Expense>() {
            @SuppressLint("SimpleDateFormat")
            final DateFormat f = new SimpleDateFormat("dd/MM/yyyy");
            @Override
            public int compare(Expense expense1, Expense expense2) {
                try {
                    return Objects.requireNonNull(f.parse(expense1.getDate())).compareTo(f.parse(expense2.getDate()));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
    }
}
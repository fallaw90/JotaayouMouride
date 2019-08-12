package com.fallntic.jotaayumouride;

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

import com.fallntic.jotaayumouride.Adapter.ExpenseAdapter;
import com.fallntic.jotaayumouride.Model.Expense;
import com.fallntic.jotaayumouride.Utility.DataHolder;
import com.fallntic.jotaayumouride.Utility.SwipeToDeleteCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import static com.fallntic.jotaayumouride.Utility.DataHolder.dahira;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.checkInternetConnection;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listExpenses;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.objNotification;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutProgressBar;


public class ListExpenseActivity extends AppCompatActivity implements View.OnClickListener{
    private final String TAG = "ListExpenseActivity";

    private TextView textViewTitle;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerViewExpense;

    private CoordinatorLayout coordinatorLayout;
    private ExpenseAdapter expenseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_expense);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setLogo(R.mipmap.logo);
        setSupportActionBar(toolbar);

        checkInternetConnection(this);

        initViews();

        textViewTitle.setText("Liste des depense du dahira " + DataHolder.dahira.getDahiraName());

        showListExpenses(listExpenses);

        enableSwipeToDelete(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_back:
                finish();
                startActivity(new Intent(this, DahiraInfoActivity.class));
                break;
        }
    }

    private void initViews() {
        recyclerViewExpense = findViewById(R.id.recyclerview_expense);
        textViewTitle = findViewById(R.id.textView_title);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        findViewById(R.id.button_back).setOnClickListener(this);
        initViewsProgressBar();
    }

    public  void initViewsProgressBar() {
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
            case R.id.icon_add:
                startActivity(new Intent(this, CreateExpenseActivity.class));
                break;

            case R.id.instructions:
                startActivity(new Intent(this, InstructionsActivity.class));
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        DataHolder.dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (objNotification != null) {
            objNotification = null;
            startActivity(new Intent(ListExpenseActivity.this, HomeActivity.class));
        } else
            startActivity(new Intent(ListExpenseActivity.this, DahiraInfoActivity.class));
        super.onBackPressed();
    }

    private void showListExpenses(List<Expense> listExpenses) {
        //Attach adapter to recyclerView
        if (listExpenses != null && listExpenses.size() > 0) {
            expenseAdapter = new ExpenseAdapter(this, listExpenses);

            recyclerViewExpense.setHasFixedSize(true);
            recyclerViewExpense.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewExpense.setVisibility(View.VISIBLE);

            recyclerViewExpense.setAdapter(expenseAdapter);
            expenseAdapter.notifyDataSetChanged();
        }
    }

    private void enableSwipeToDelete(final Context context) {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                final int position = viewHolder.getAdapterPosition();
                final Expense expense = listExpenses.get(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(ListExpenseActivity.this, R.style.alertDialog);
                builder.setTitle("Supprimer evenement!");
                builder.setMessage("Etes vous sure de vouloir supprimer cet evenement?");
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
                        startActivity(new Intent(ListExpenseActivity.this,
                                ListExpenseActivity.class));
                    }
                });
                builder.show();
            }
        };
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerViewExpense);
    }

    public void removeExpense(final Context context, final Expense expense) {
        showProgressBar();
        db.collection("dahiras").document(dahira.getDahiraID())
                .collection("expenses").document(expense.getExpenseID())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideProgressBar();
                        CreateExpenseActivity.updateDahira(context, expense.getPrice(),
                                expense.getTypeOfExpense(), true);

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
                startActivity(new Intent(ListExpenseActivity.this, ListExpenseActivity.class));
            }
        });
    }

}

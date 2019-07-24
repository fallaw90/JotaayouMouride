package com.fallntic.jotaayumouride;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.fallntic.jotaayumouride.CreateExpenseActivity.updateDahira;
import static com.fallntic.jotaayumouride.DataHolder.*;

public class ListExpenseActivity extends AppCompatActivity {
    private final String TAG = "ListExpenseActivity";

    private TextView textViewTitle;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerViewExpense;

    private final ExpenseAdapter expenseAdapter = new ExpenseAdapter(this);
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_expense);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Liste des depense");
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (!isConnected(this)) {
            finish();
            Intent intent = new Intent(this, LoginActivity.class);
            showAlertDialog(this, "Oops! Pas de connexion, verifier votre connexion internet puis reesayez SVP", intent);
        }

        recyclerViewExpense = findViewById(R.id.recyclerview_expense);
        textViewTitle = findViewById(R.id.textView_title);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        textViewTitle.setText("Liste des depense du dahira " + dahira.getDahiraName());

        showListExpenses();

        enableSwipeToDeleteAndUndo();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_menu, menu);

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
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, DahiraInfoActivity.class));
        super.onBackPressed();
    }

    private void showListExpenses() {

        //Attach adapter to recyclerView
        Intent intent = new Intent(ListExpenseActivity.this, DahiraInfoActivity.class);
        if (expense != null) {
            if (expense.getDahiraID().equals(dahira.getDahiraID())) {
                recyclerViewExpense.setHasFixedSize(true);
                recyclerViewExpense.setLayoutManager(new LinearLayoutManager(this));
                recyclerViewExpense.setVisibility(View.VISIBLE);


                recyclerViewExpense.setAdapter(expenseAdapter);
                expenseAdapter.notifyDataSetChanged();
            } else {
                showAlertDialog(ListExpenseActivity.this, "Dahira " + dahira.getDahiraName() +
                        " n'a aucune depense enregistree pour le moment", intent);
            }
        } else {
            showAlertDialog(ListExpenseActivity.this, "Dahira " + dahira.getDahiraName() +
                    " n'a aucun depense enregistre pour le moment", intent);
        }
    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                final int position = viewHolder.getAdapterPosition();
                final String typeOfExpense = expense.getListTypeOfExpense().get(position);
                final String price = expense.getListPrice().get(position);
                final String userID = expense.getListUserID().get(position);
                final String mDate = expense.getListDate().get(position);
                final String note = expense.getListNote().get(position);
                final String userName = expense.getListUserName().get(position);

                //Update totalAdiya dahira
                expenseAdapter.removeItem(position);
                updateExpenseCollection(ListExpenseActivity.this);
                updateDahira(ListExpenseActivity.this, price, typeOfExpense, false);

                Snackbar snackbar = null;
                snackbar = Snackbar.make(coordinatorLayout,
                        "Depense supprime.", Snackbar.LENGTH_LONG);

                if (snackbar != null) {
                    final String finalTypeOfExpense = typeOfExpense;
                    snackbar.setAction("Annuler la suppression", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            expenseAdapter.restoreItem(price, mDate, finalTypeOfExpense, note, userID,
                                    userName, position);

                            updateExpenseCollection(ListExpenseActivity.this);
                            updateDahira(ListExpenseActivity.this, price, typeOfExpense, true);

                            recyclerViewExpense.scrollToPosition(position);
                        }
                    });

                    snackbar.setActionTextColor(Color.YELLOW);
                    snackbar.show();
                }
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerViewExpense);
    }

    public void updateExpenseCollection(final Context context) {
        showProgressDialog(context, "Mis a jour de votre depense en cours ...");
        FirebaseFirestore.getInstance().collection("expenses")
                .document(dahira.getDahiraID())
                .update("listUserID", expense.getListUserID(),
                        "listUserName", expense.getListUserName(),
                        "listDate", expense.getListDate(),
                        "listNote", expense.getListNote(),
                        "listPrice", expense.getListPrice(),
                        "listTypeOfExpense", expense.getListTypeOfExpense())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onSuccess(Void aVoid) {
                        dismissProgressDialog();
                        Log.d(TAG, "Expense updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissProgressDialog();
                        actionSelected = "";
                        Intent intent = new Intent(context, DahiraInfoActivity.class);
                        showAlertDialog(context, "Erreur lors de l'enregistrement." +
                                "\nReessayez plutard SVP", intent);
                        Log.d(TAG, "Error updated expense");
                    }
                });
    }

}

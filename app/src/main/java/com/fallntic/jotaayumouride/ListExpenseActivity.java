package com.fallntic.jotaayumouride;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import static com.fallntic.jotaayumouride.DataHolder.announcement;
import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.dismissProgressDialog;
import static com.fallntic.jotaayumouride.DataHolder.expense;
import static com.fallntic.jotaayumouride.DataHolder.isConnected;
import static com.fallntic.jotaayumouride.DataHolder.logout;
import static com.fallntic.jotaayumouride.DataHolder.showAlertDialog;
import static com.fallntic.jotaayumouride.DataHolder.toastMessage;

public class ListExpenseActivity extends AppCompatActivity  implements View.OnClickListener{
    private final String TAG = "ListExpenseActivity";

    private TextView textViewDahiraName;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private RecyclerView recyclerViewExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_expense);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Liste des depense");
        setSupportActionBar(toolbar);

        if (!isConnected(this)){
            Intent intent = new Intent(this, LoginActivity.class);
            logout();
            showAlertDialog(this,"Oops! Pas de connexion, verifier votre connexion internet puis reesayez SVP", intent);
        }

        recyclerViewExpense = findViewById(R.id.recyclerview_expense);
        textViewDahiraName = findViewById(R.id.textView_dahiraName);

        textViewDahiraName.setText("Liste des depense du dahira " + dahira.getDahiraName());

        showListExpenses();

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
                startActivity(new Intent(this, DahiraInfoActivity.class));
                break;
        }
    }

    private void showListExpenses() {

        //Attach adapter to recyclerView
        Intent intent = new Intent(ListExpenseActivity.this, DahiraInfoActivity.class);
        if (expense.getListUserID().size() > 0){
            if (expense.getDahiraID().equals(dahira.getDahiraID())){
                recyclerViewExpense.setHasFixedSize(true);
                recyclerViewExpense.setLayoutManager(new LinearLayoutManager(this));
                recyclerViewExpense.setVisibility(View.VISIBLE);
                final ExpenseAdapter expenseAdapter = new ExpenseAdapter(ListExpenseActivity.this,
                        expense.getListUserName(), expense.getListDate(), expense.getListNote(),
                        expense.getListPrice(), expense.getListTypeOfExpense());

                recyclerViewExpense.setAdapter(expenseAdapter);
                expenseAdapter.notifyDataSetChanged();
            }
            else {
                showAlertDialog(ListExpenseActivity.this, "Dahira " + dahira.getDahiraName() +
                        " n'a aucune depense enregistree pour le moment", intent);
            }
        }
        else {
            showAlertDialog(ListExpenseActivity.this, "Dahira " + dahira.getDahiraName() +
                    " n'a aucun depense enregistre pour le moment", intent);
        }
    }
}

package com.fallntic.jotaayumouride.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.R;
import com.fallntic.jotaayumouride.model.Expense;

import java.util.List;


@SuppressWarnings("ALL")
public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private final Context context;
    private final List<Expense> listExpense;

    public ExpenseAdapter(Context context, List<Expense> listExpense) {
        this.context = context;
        this.listExpense = listExpense;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ExpenseViewHolder(
                LayoutInflater.from(context).inflate(R.layout.layout_expense, parent, false)
        );
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {

        Expense expense = listExpense.get(position);

        holder.textViewUserName.setText("Enregistree par " + expense.getUserName());
        holder.textViewtDate.setText(expense.getDate());
        holder.textViewNote.setText(expense.getNote());
        holder.textViewPrice.setText("Prix = " + expense.getPrice() + " FCFA");
        holder.textViewTypeOfExpense.setText("Deduit sur la caisse " + expense.getTypeOfExpense());

    }

    @Override
    public int getItemCount() {
        return listExpense.size();
    }

    public void removeItem(int position) {

        listExpense.remove(position);

        notifyItemRemoved(position);
    }

    @SuppressWarnings("unused")
    public Expense getExpense(int position) {
        return listExpense.get(position);
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        final TextView textViewUserName;
        final TextView textViewtDate;
        final TextView textViewNote;
        final TextView textViewPrice;
        final TextView textViewTypeOfExpense;

        ExpenseViewHolder(View itemView) {
            super(itemView);
            textViewUserName = itemView.findViewById(R.id.textView_userName);
            textViewtDate = itemView.findViewById(R.id.textView_date);
            textViewNote = itemView.findViewById(R.id.textView_note);
            textViewPrice = itemView.findViewById(R.id.textView_price);
            textViewTypeOfExpense = itemView.findViewById(R.id.textView_typeOfExpense);

        }
    }
}
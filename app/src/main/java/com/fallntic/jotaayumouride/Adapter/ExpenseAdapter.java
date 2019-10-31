package com.fallntic.jotaayumouride.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Model.Expense;
import com.fallntic.jotaayumouride.R;

import java.util.List;


public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private Context context;
    private List<Expense> listExpense;

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

    public Expense getExpense(int position) {
        return listExpense.get(position);
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUserName;
        TextView textViewtDate;
        TextView textViewNote;
        TextView textViewPrice;
        TextView textViewTypeOfExpense;

        public ExpenseViewHolder(View itemView) {
            super(itemView);
            textViewUserName = itemView.findViewById(R.id.textView_userName);
            textViewtDate = itemView.findViewById(R.id.textView_date);
            textViewNote = itemView.findViewById(R.id.textView_note);
            textViewPrice = itemView.findViewById(R.id.textView_price);
            textViewTypeOfExpense = itemView.findViewById(R.id.textView_typeOfExpense);

        }
    }
}
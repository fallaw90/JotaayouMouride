package com.fallntic.jotaayumouride;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.fallntic.jotaayumouride.DataHolder.expense;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private Context context;

    private String userName;
    private String mDate;
    private String typeOfExpense;
    private String price;
    private String userID;
    private String note;

    public ExpenseAdapter(Context context) {
        this.context = context;
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

        typeOfExpense = expense.getListTypeOfExpense().get(position);
        price = expense.getListPrice().get(position);
        userID = expense.getListUserID().get(position);
        mDate = expense.getListDate().get(position);
        note = expense.getListNote().get(position);
        userName = expense.getListUserName().get(position);

        holder.textViewUserName.setText("Enregistree par " + userName);
        holder.textViewtDate.setText(mDate);
        holder.textViewNote.setText(note);
        holder.textViewPrice.setText("Prix = " + price + " FCFA");
        holder.textViewTypeOfExpense.setText("Deduit sur la caisse " + typeOfExpense);

    }

    @Override
    public int getItemCount() {
        return expense.getListPrice().size();
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder{
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


    public void removeItem(int position) {

        expense.getListPrice().remove(position);
        expense.getListDate().remove(position);
        expense.getListTypeOfExpense().remove(position);
        expense.getListNote().remove(position);
        expense.getListUserName().remove(position);
        expense.getListUserID().remove(position);

        notifyItemRemoved(position);
    }

    public void restoreItem(String price, String mDate, String type,String note, String userID,
                            String userName, int position) {

        expense.getListPrice().add(position, price);
        expense.getListDate().add(position, mDate);
        expense.getListTypeOfExpense().add(position, type);
        expense.getListNote().add(position, note);
        expense.getListUserID().add(position, userID);
        expense.getListUserName().add(position, userName);

        notifyItemInserted(position);
    }

    public Expense getExpense() {
        return expense;
    }
}
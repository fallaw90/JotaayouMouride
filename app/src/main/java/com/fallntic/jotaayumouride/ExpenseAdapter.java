package com.fallntic.jotaayumouride;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.actionSelected;
import static com.fallntic.jotaayumouride.DataHolder.announcement;
import static com.fallntic.jotaayumouride.DataHolder.expense;
import static com.fallntic.jotaayumouride.DataHolder.indexAnnouncementSelected;
import static com.fallntic.jotaayumouride.DataHolder.indexExpenseSelected;
import static com.fallntic.jotaayumouride.DataHolder.indexOnlineUser;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.DataHolder.typeOfContribution;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private Context context;
    private List<String> listUserName = new ArrayList<String>();
    private List<String> listDate = new ArrayList<String>();
    private List<String> listNote = new ArrayList<String>();
    private List<String> listPrice = new ArrayList<String>();
    private List<String> listTypeOfExpense = new ArrayList<String>();

    private String userName;
    private String mDate;
    private String note;
    private String price;
    private String typeOfExpense;

    public ExpenseAdapter(Context context, List<String> listUserName, List<String> listDate,
                          List<String> listNote, List<String> listPrice, List<String> listTypeOfExpense) {

        this.context = context;
        this.listUserName = listUserName;
        this.listDate = listDate;
        this.listNote = listNote;
        this.listPrice = listPrice;
        this.listTypeOfExpense = listTypeOfExpense;
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

        userName = listUserName.get(position);
        mDate = listDate.get(position);
        note = listNote.get(position);
        price = listPrice.get(position);
        typeOfExpense = listTypeOfExpense.get(position);

        holder.textViewUserName.setText("Enregistree par " + userName);
        holder.textViewtDate.setText(mDate);
        holder.textViewNote.setText(note);
        holder.textViewPrice.setText("Prix = " + price + " FCFA");
        holder.textViewTypeOfExpense.setText("Deduit sur la caisse " + typeOfExpense);

    }

    @Override
    public int getItemCount() {
        return listDate.size();
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
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

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            indexExpenseSelected = getAdapterPosition();
            if (onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur")){
                actionSelected = "updateExpense";
                Intent intent = new Intent(context, CreateExpenseActivity.class);
                context.startActivity(intent);
            }
        }
    }
}
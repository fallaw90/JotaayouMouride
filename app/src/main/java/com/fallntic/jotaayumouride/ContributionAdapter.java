package com.fallntic.jotaayumouride;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.amountContribution;
import static com.fallntic.jotaayumouride.DataHolder.dateContribution;

public class ContributionAdapter extends RecyclerView.Adapter<ContributionAdapter.AdiyaViewHolder> {

    private Context context;
    private List<String> listDateContribution;
    private List<String> listAmountContribution;

    public ContributionAdapter(Context context, List<String> listDateContribution, List<String> listAmountContribution) {
        this.context = context;
        this.listDateContribution = listDateContribution;
        this.listAmountContribution = listAmountContribution;
    }

    @NonNull
    @Override
    public AdiyaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AdiyaViewHolder(
                LayoutInflater.from(context).inflate(R.layout.layout_contribution, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull AdiyaViewHolder holder, int position) {
        dateContribution = listDateContribution.get(position);
        amountContribution = listAmountContribution.get(position);
        holder.textViewDate.setText(dateContribution);
        holder.textViewAmount.setText(amountContribution);

    }

    @Override
    public int getItemCount() {
        return listAmountContribution.size();
    }

    class AdiyaViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView textViewDate;
        TextView textViewAmount;

        public AdiyaViewHolder(View itemView) {
            super(itemView);

            textViewDate = itemView.findViewById(R.id.textView_date);
            textViewAmount = itemView.findViewById(R.id.textView_amountAdiya);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            dateContribution = listDateContribution.get(getAdapterPosition());
            amountContribution = listAmountContribution.get(getAdapterPosition());
            Intent intent = new Intent(context, UserInfoActivity.class);
            context.startActivity(intent);
        }
    }
}
package com.fallntic.jotaayumouride.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Model.Sass;
import com.fallntic.jotaayumouride.R;

import java.util.List;

public class SassAdapter extends RecyclerView.Adapter<SassAdapter.SassViewHolder> {

    List<Sass> listSass;
    private Context context;
    private int selectedPosition;

    public SassAdapter(Context context, List<Sass> listSass) {
        this.context = context;
        this.listSass = listSass;
    }

    @NonNull
    @Override
    public SassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SassViewHolder(
                LayoutInflater.from(context).inflate(R.layout.layout_contribution, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SassViewHolder holder, int position) {
        Sass sass = listSass.get(position);

        holder.textViewDate.setText(sass.getListDate().get(position));
        holder.textViewAmount.setText(sass.getListSass().get(position));
        holder.textViewSavedBy.setText("Enregistré par " + sass.getListUserName().get(position));
    }

    @Override
    public int getItemCount() {
        return listSass.size();
    }

    public void removeItem(int position) {
        if (position < listSass.size()) {
            listSass.remove(position);
            notifyItemRemoved(position);
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    class SassViewHolder extends RecyclerView.ViewHolder {

        TextView textViewDate;
        TextView textViewAmount;
        TextView textViewSavedBy;

        public SassViewHolder(View itemView) {
            super(itemView);

            textViewDate = itemView.findViewById(R.id.textView_date);
            textViewAmount = itemView.findViewById(R.id.textView_amountAdiya);
            textViewSavedBy = itemView.findViewById(R.id.textView_savedBy);
        }
    }
}
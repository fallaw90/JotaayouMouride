package com.fallntic.jotaayumouride.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Model.Social;
import com.fallntic.jotaayumouride.R;

import java.util.List;

public class SocialAdapter extends RecyclerView.Adapter<SocialAdapter.SocialViewHolder> {

    List<Social> listSocial;
    private Context context;
    private int selectedPosition;

    public SocialAdapter(Context context, List<Social> listSocial) {
        this.context = context;
        this.listSocial = listSocial;
    }

    @NonNull
    @Override
    public SocialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SocialViewHolder(
                LayoutInflater.from(context).inflate(R.layout.layout_contribution, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SocialViewHolder holder, int position) {
        Social social = listSocial.get(position);

        holder.textViewDate.setText(social.getListDate().get(position));
        holder.textViewAmount.setText(social.getListSocial().get(position));
        holder.textViewSavedBy.setText("Enregistré par " + social.getListUserName().get(position));
    }

    @Override
    public int getItemCount() {
        return listSocial.size();
    }

    public void removeItem(int position) {
        if (position < listSocial.size()) {
            listSocial.remove(position);
            notifyItemRemoved(position);
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    class SocialViewHolder extends RecyclerView.ViewHolder {

        TextView textViewDate;
        TextView textViewAmount;
        TextView textViewSavedBy;

        public SocialViewHolder(View itemView) {
            super(itemView);

            textViewDate = itemView.findViewById(R.id.textView_date);
            textViewAmount = itemView.findViewById(R.id.textView_amountAdiya);
            textViewSavedBy = itemView.findViewById(R.id.textView_savedBy);
        }
    }
}
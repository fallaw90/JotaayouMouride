package com.fallntic.jotaayumouride.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.R;
import com.fallntic.jotaayumouride.model.Social;

import java.util.List;

@SuppressWarnings("ALL")
public class SocialAdapter extends RecyclerView.Adapter<SocialAdapter.SocialViewHolder> {

    private final List<Social> listSocial;
    private final Context context;
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

        final TextView textViewDate;
        final TextView textViewAmount;
        final TextView textViewSavedBy;

        SocialViewHolder(View itemView) {
            super(itemView);

            textViewDate = itemView.findViewById(R.id.textView_date);
            textViewAmount = itemView.findViewById(R.id.textView_amountAdiya);
            textViewSavedBy = itemView.findViewById(R.id.textView_savedBy);
        }
    }
}
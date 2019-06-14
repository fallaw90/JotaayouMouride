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

public class DahiraAdapter extends RecyclerView.Adapter<DahiraAdapter.DahiraViewHolder> {

    private Context context;
    private List<Dahira> dahiraList;

    public DahiraAdapter(Context context, List<Dahira> dahiraList) {
        this.context = context;
        this.dahiraList = dahiraList;
    }

    @NonNull
    @Override
    public DahiraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DahiraViewHolder(
                LayoutInflater.from(context).inflate(R.layout.layout_dahira, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull DahiraViewHolder holder, int position) {
        Dahira dahira = dahiraList.get(position);

        holder.textViewDahiraName.setText(dahira.getDahiraName());
        holder.textViewDieuwrine.setText("Dieuwrine: " + dahira.getDieuwrine());
        holder.textViewPhoneNumber.setText("Telephone: " + dahira.getPhoneNumber());
        holder.textViewSiege.setText("Siege: " + dahira.getSiege());
    }

    @Override
    public int getItemCount() {
        return dahiraList.size();
    }

    class DahiraViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textViewDahiraName;
        TextView textViewDieuwrine;
        TextView textViewPhoneNumber;
        TextView textViewSiege;

        public DahiraViewHolder(View itemView) {
            super(itemView);

            textViewDahiraName = itemView.findViewById(R.id.textview_dahiraName);
            textViewDieuwrine = itemView.findViewById(R.id.textview_dieuwrine);
            textViewPhoneNumber = itemView.findViewById(R.id.textview_phoneNumber);
            textViewSiege = itemView.findViewById(R.id.textview_siege);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            Dahira dahira = dahiraList.get(getAdapterPosition());
            Intent intent = new Intent(context, UpdateDahiraActivity.class);
            intent.putExtra("dahira", dahira);
            context.startActivity(intent);
        }
    }
}
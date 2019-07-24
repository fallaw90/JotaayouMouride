package com.fallntic.jotaayumouride;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import static com.fallntic.jotaayumouride.DataHolder.dahira;
import static com.fallntic.jotaayumouride.DataHolder.indexOnlineUser;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;

public class DahiraAdapter extends RecyclerView.Adapter<DahiraAdapter.DahiraViewHolder> {

    private Context context;
    private List<Dahira> dahiraList;

    private ImageView imageView;

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
        dahira = dahiraList.get(position);

        holder.textViewDahiraName.setText("Dahira " + dahira.getDahiraName());
        holder.textViewDieuwrine.setText("Dieuwrine: " + dahira.getDieuwrine());
        holder.textViewPhoneNumber.setText("Telephone: " + dahira.getDahiraPhoneNumber());
        holder.textViewSiege.setText("Siege: " + dahira.getSiege());

        DataHolder.showLogoDahira(context, imageView);
    }

    @Override
    public int getItemCount() {
        return dahiraList.size();
    }

    class DahiraViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

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
            imageView = itemView.findViewById(R.id.imageView);

            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            dahira = dahiraList.get(getAdapterPosition());
            if (onlineUser.getListDahiraID().contains(dahira.getDahiraID())){
                indexOnlineUser = onlineUser.getListDahiraID().indexOf(dahira.getDahiraID());
            }
            Intent intent = new Intent(context, DahiraInfoActivity.class);
            context.startActivity(intent);
            dahiraList.clear();
        }

        @Override
        public boolean onLongClick(View view) {
            dahira = dahiraList.get(getAdapterPosition());
            if (onlineUser.getListDahiraID().contains(dahira.getDahiraID())) {
                indexOnlineUser = onlineUser.getListDahiraID().indexOf(dahira.getDahiraID());
            }
            Intent intent = new Intent(context, UpdateDahiraActivity.class);
            context.startActivity(intent);
            return false;
        }
    }
}
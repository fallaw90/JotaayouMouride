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
import static com.fallntic.jotaayumouride.DataHolder.indexAnnouncementSelected;
import static com.fallntic.jotaayumouride.DataHolder.indexEventSelected;
import static com.fallntic.jotaayumouride.DataHolder.indexOnlineUser;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder> {

    private Context context;
    private List<String> listUserName = new ArrayList<String>();
    private List<String> listDate = new ArrayList<String>();
    private List<String> listNote = new ArrayList<String>();

    private String userName;
    private String mDate;
    private String note;

    public AnnouncementAdapter(Context context, List<String> listUserName,
                               List<String> listDate, List<String> listNote) {

        this.context = context;
        this.listUserName = listUserName;
        this.listDate = listDate;
        this.listNote = listNote;
    }

    @NonNull
    @Override
    public AnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AnnouncementViewHolder(
                LayoutInflater.from(context).inflate(R.layout.layout_announcement, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull AnnouncementViewHolder holder, int position) {

        userName = listUserName.get(position);
        mDate = listDate.get(position);
        note = listNote.get(position);

        holder.textViewUserName.setText("Enregistree par " + userName);
        holder.textViewtDate.setText(mDate);
        holder.textViewNote.setText(note);

    }

    @Override
    public int getItemCount() {
        return listDate.size();
    }

    class AnnouncementViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView textViewUserName;
        TextView textViewtDate;
        TextView textViewNote;

        public AnnouncementViewHolder(View itemView) {
            super(itemView);
            textViewUserName = itemView.findViewById(R.id.textView_userName);
            textViewtDate = itemView.findViewById(R.id.textView_date);
            textViewNote = itemView.findViewById(R.id.textView_note);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (announcement.getListUserID().get(indexAnnouncementSelected).equals(onlineUser.getUserID()) ||
                    onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur")){
                indexAnnouncementSelected = getAdapterPosition();
                actionSelected = "updateAnnouncement";
                Intent intent = new Intent(context, CreateAnnouncementActivity.class);
                context.startActivity(intent);
            }
        }
    }
}
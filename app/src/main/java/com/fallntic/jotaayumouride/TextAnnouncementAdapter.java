package com.fallntic.jotaayumouride;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Model.Announcement;

import java.util.List;

public class TextAnnouncementAdapter extends RecyclerView.Adapter<TextAnnouncementAdapter.TextAnnouncementViewHolder> {

    private Context context;
    private List<Announcement> listAnnouncement;

    public TextAnnouncementAdapter(Context context, List<Announcement> listAnnouncement) {

        this.context = context;
        this.listAnnouncement = listAnnouncement;
    }

    @NonNull
    @Override
    public TextAnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TextAnnouncementViewHolder(
                LayoutInflater.from(context).inflate(R.layout.layout_announcement, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull TextAnnouncementViewHolder holder, int position) {

        Announcement announcement = listAnnouncement.get(position);

        holder.textViewUserName.setText("Enregistree par " + announcement.getUserName());
        holder.textViewtDate.setText(announcement.getDate());
        holder.textViewNote.setText(announcement.getNote());

    }

    @Override
    public int getItemCount() {
        return listAnnouncement.size();
    }

    public void removeItem(int position) {

        if (position < listAnnouncement.size()) {
            listAnnouncement.remove(position);
            notifyItemRemoved(position);
        }
    }

    class TextAnnouncementViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUserName;
        TextView textViewtDate;
        TextView textViewNote;

        public TextAnnouncementViewHolder(View itemView) {
            super(itemView);
            textViewUserName = itemView.findViewById(R.id.textView_userName);
            textViewtDate = itemView.findViewById(R.id.textView_date);
            textViewNote = itemView.findViewById(R.id.textView_note);
        }
    }
}

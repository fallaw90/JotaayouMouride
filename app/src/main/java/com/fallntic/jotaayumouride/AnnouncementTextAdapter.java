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

public class AnnouncementTextAdapter extends RecyclerView.Adapter<AnnouncementTextAdapter.AnnouncementTextViewHolder> {

    private Context context;
    private List<Announcement> listAnnouncement;
    private String userName;
    private String mDate;
    private String note;

    public AnnouncementTextAdapter(Context context, List<Announcement> listAnnouncement) {

        this.context = context;
        this.listAnnouncement = listAnnouncement;
    }

    @NonNull
    @Override
    public AnnouncementTextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AnnouncementTextViewHolder(
                LayoutInflater.from(context).inflate(R.layout.layout_announcement, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull AnnouncementTextViewHolder holder, int position) {

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

    class AnnouncementTextViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUserName;
        TextView textViewtDate;
        TextView textViewNote;

        public AnnouncementTextViewHolder(View itemView) {
            super(itemView);
            textViewUserName = itemView.findViewById(R.id.textView_userName);
            textViewtDate = itemView.findViewById(R.id.textView_date);
            textViewNote = itemView.findViewById(R.id.textView_note);
        }
    }
}

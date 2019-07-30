package com.fallntic.jotaayumouride;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.fallntic.jotaayumouride.DataHolder.event;

public class MyEventAdapter extends RecyclerView.Adapter<MyEventAdapter.EventViewHolder> {

    private Context context;

    private String userName;
    private String mDate;
    private String title;
    private String note;
    private String location;
    private String startTime;
    private String endTime;

    public MyEventAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EventViewHolder(
                LayoutInflater.from(context).inflate(R.layout.layout_event, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {

        userName = event.getListUserName().get(position);
        mDate = event.getListDate().get(position);
        title = event.getListTitle().get(position);
        note = event.getListNote().get(position);
        location = event.getListLocation().get(position);
        startTime = event.getListStartTime().get(position);
        endTime = event.getListEndTime().get(position);

        holder.textViewUserName.setText("Enregistre par " + userName);
        holder.textViewTitle.setText(title);
        holder.textViewtDate.setText(mDate);
        holder.textViewLocation.setText(location);
        holder.textViewNote.setText(note);
        holder.textViewStartTime.setText(startTime);
        holder.textViewEndTime.setText(endTime);

    }

    @Override
    public int getItemCount() {
        return event.getListDate().size();
    }

    public void removeItem(int position) {

        event.getListUserName().remove(position);
        event.getListDate().remove(position);
        event.getListTitle().remove(position);
        event.getListNote().remove(position);
        event.getListLocation().remove(position);
        event.getListStartTime().remove(position);
        event.getListEndTime().remove(position);
        event.getListUserID().remove(position);

        notifyItemRemoved(position);
    }

    public void restoreItem(String userName, String mDate, String title, String note, String location,
                            String startTime, String endTime, String userID, int position) {

        event.getListUserName().add(position, userName);
        event.getListDate().add(position, mDate);
        event.getListTitle().add(position, title);
        event.getListNote().add(position, note);
        event.getListLocation().add(position, location);
        event.getListStartTime().add(position, startTime);
        event.getListEndTime().add(position, endTime);
        event.getListUserID().add(position, userID);

        notifyItemInserted(position);
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewtDate;
        TextView textViewLocation;
        TextView textViewNote;
        TextView textViewStartTime;
        TextView textViewEndTime;
        TextView textViewUserName;

        public EventViewHolder(View itemView) {
            super(itemView);

            textViewUserName = itemView.findViewById(R.id.textView_userName);
            textViewTitle = itemView.findViewById(R.id.textView_title);
            textViewtDate = itemView.findViewById(R.id.textView_date);
            textViewLocation = itemView.findViewById(R.id.textView_location);
            textViewNote = itemView.findViewById(R.id.textView_note);
            textViewStartTime = itemView.findViewById(R.id.textView_startTime);
            textViewEndTime = itemView.findViewById(R.id.textView_endTime);
        }
    }
}
package com.fallntic.jotaayumouride;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AllEventAdapter extends RecyclerView.Adapter<AllEventAdapter.EventViewHolder> {


    private String userName;
    private String mDate;
    private String title;
    private String note;
    private String location;
    private String startTime;
    private String endTime;

    private Event objEvent;
    private Context context;
    private List<Event> eventList;

    public AllEventAdapter(Context context, List<Event> eventList){
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        objEvent = eventList.get(position);

        for (int i = 0; i < objEvent.getListUserID().size(); i++) {
            userName = objEvent.getListUserName().get(i);
            mDate = objEvent.getListDate().get(i);
            title = objEvent.getListTitle().get(i);
            note = objEvent.getListNote().get(i);
            location = objEvent.getListLocation().get(i);
            startTime = objEvent.getListStartTime().get(i);
            endTime = objEvent.getListEndTime().get(i);

            holder.textViewUserName.setText("Enregistre par " + userName);
            holder.textViewTitle.setText(title);
            holder.textViewtDate.setText(mDate);
            holder.textViewLocation.setText(location);
            holder.textViewNote.setText(note);
            holder.textViewStartTime.setText(startTime);
            holder.textViewEndTime.setText(endTime);
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder{

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
package com.fallntic.jotaayumouride.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Model.Event;
import com.fallntic.jotaayumouride.R;

import java.util.List;


public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private Context context;
    private List<Event> listEvent;

    public EventAdapter(Context context, List<Event> listEvent) {
        this.context = context;
        this.listEvent = listEvent;
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

        Event event = listEvent.get(position);

        holder.textViewUserName.setText("Enregistre par " + event.getUserName());
        holder.textViewTitle.setText(event.getTitle());
        holder.textViewtDate.setText(event.getDate());
        holder.textViewLocation.setText(event.getLocation());
        holder.textViewNote.setText(event.getNote());
        holder.textViewStartTime.setText(event.getStartTime());
        holder.textViewEndTime.setText(event.getEndTime());

    }

    @Override
    public int getItemCount() {
        return listEvent.size();
    }

    public void removeItem(int position) {

        listEvent.remove(position);

        notifyItemRemoved(position);
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
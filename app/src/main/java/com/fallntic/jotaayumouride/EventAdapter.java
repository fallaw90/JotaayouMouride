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
import static com.fallntic.jotaayumouride.DataHolder.indexEventSelected;
import static com.fallntic.jotaayumouride.DataHolder.indexOnlineUser;
import static com.fallntic.jotaayumouride.DataHolder.onlineUser;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private Context context;
    private List<String> listUserName = new ArrayList<String>();
    private List<String> listDate = new ArrayList<String>();
    private List<String> listTitle = new ArrayList<String>();
    private List<String> listNote = new ArrayList<String>();
    private List<String> listLocation = new ArrayList<String>();
    private List<String> listStartTime = new ArrayList<String>();
    private List<String> listEndTime = new ArrayList<String>();

    private String userName;
    private String mDate;
    private String title;
    private String note;
    private String location;
    private String startTime;
    private String endTime;

    public EventAdapter(Context context, List<String> listUserName, List<String> listDate,
                        List<String> listTitle, List<String> listNote, List<String> listLocation,
                        List<String> listStartTime, List<String> listEndTime) {

        this.context = context;
        this.listUserName = listUserName;
        this.listDate = listDate;
        this.listTitle = listTitle;
        this.listNote = listNote;
        this.listLocation = listLocation;
        this.listStartTime = listStartTime;
        this.listEndTime = listEndTime;
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

        userName = listUserName.get(position);
        mDate = listDate.get(position);
        title = listTitle.get(position);
        note = listNote.get(position);
        location = listLocation.get(position);
        startTime = listStartTime.get(position);
        endTime = listEndTime.get(position);

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
        return listDate.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
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

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            indexEventSelected = getAdapterPosition();
            if (onlineUser.getListRoles().get(indexOnlineUser).equals("Administrateur")){
                actionSelected = "updateEvent";
                Intent intent = new Intent(context, CreateNewEventActivity.class);
                context.startActivity(intent);
            }
        }
    }
}
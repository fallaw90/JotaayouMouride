package com.fallntic.jotaayumouride;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Model.UploadSong;

import java.io.IOException;
import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongsAdapterViewHolder> {

    Context context;
    List<UploadSong> arrayListSongs;

    public SongsAdapter(Context context, List<UploadSong> arrayListSongs){
        this.context = context;
        this.arrayListSongs = arrayListSongs;
    }

    @NonNull
    @Override
    public SongsAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_song, viewGroup, false);
        return new SongsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongsAdapterViewHolder holder, int position) {
        UploadSong uploadSong = arrayListSongs.get(position);
        holder.textViewTitleSong.setText(uploadSong.getSongTitle());
        holder.textViewDuration.setText(uploadSong.getSongDuration);
    }

    @Override
    public int getItemCount() {
        return arrayListSongs.size();
    }

    public class SongsAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView textViewTitleSong;
        TextView textViewDuration;

        public SongsAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewTitleSong = itemView.findViewById(R.id.textView_titleSong);
            textViewDuration = itemView.findViewById(R.id.textView_songDuration);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            try {
                ((ShowSongsActivity)context).playSong(arrayListSongs, getAdapterPosition());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

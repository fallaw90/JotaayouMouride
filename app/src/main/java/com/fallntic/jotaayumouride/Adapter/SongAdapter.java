package com.fallntic.jotaayumouride.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.R;
import com.fallntic.jotaayumouride.model.Song;

import java.util.List;


public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private final Context context;
    private final List<Song> songList;
    private final RecyclerItemClickListener listener;
    private int selectedPosition;

    public SongAdapter(Context context, List<Song> songList, RecyclerItemClickListener listener) {

        this.context = context;
        this.songList = songList;
        this.listener = listener;

    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_row, parent, false);

        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {

        Song song = songList.get(position);
        if (song != null) {

            if (selectedPosition == position) {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
                holder.iv_play_active.setVisibility(View.VISIBLE);
            } else {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                holder.iv_play_active.setVisibility(View.INVISIBLE);
            }

            holder.tv_title.setText(song.getAudioTitle());
            //String duration = utility.convertDuration(song.getAudioDuration());
            holder.tv_duration.setText(song.getAudioDuration());

            holder.bind(song, listener);

        }

    }

    @Override
    public int getItemCount() {
        if (songList != null)
            return songList.size();
        else
            return 0;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public void removeItem(int position) {

        if (position < songList.size()) {
            songList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public interface RecyclerItemClickListener {

        void onClickListener(Song song, int position);

        @SuppressWarnings("unused")
        void onLongClickListener(Song song);
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {

        private final TextView tv_title;
        private final TextView tv_duration;
        private final ImageView iv_play_active;

        SongViewHolder(View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_duration = itemView.findViewById(R.id.tv_duration);
            iv_play_active = itemView.findViewById(R.id.iv_play_active);

        }

        void bind(final Song song, final RecyclerItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClickListener(song, getLayoutPosition());
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    listener.onLongClickListener(song);
                    return true;
                }
            });
        }

    }
}

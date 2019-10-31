package com.fallntic.jotaayumouride.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Model.Song;
import com.fallntic.jotaayumouride.R;

import java.util.List;


public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private Context context;
    private List<Song> songList;
    private RecyclerItemClickListener listener;
    private int selectedPosition;

    public SongAdapter(Context context, List<Song> songList, RecyclerItemClickListener listener) {

        this.context = context;
        this.songList = songList;
        this.listener = listener;

    }

    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_row, parent, false);

        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SongViewHolder holder, int position) {

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
            //String duration = Utility.convertDuration(song.getAudioDuration());
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

        boolean onLongClickListener(Song song, int position);
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_title, tv_artist, tv_duration;
        private ImageView iv_artwork, iv_play_active;

        public SongViewHolder(View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_artist = itemView.findViewById(R.id.tv_artist);
            tv_duration = itemView.findViewById(R.id.tv_duration);
            iv_artwork = itemView.findViewById(R.id.iv_artwork);
            iv_play_active = itemView.findViewById(R.id.iv_play_active);

        }

        public void bind(final Song song, final RecyclerItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClickListener(song, getLayoutPosition());
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    listener.onLongClickListener(song, getLayoutPosition());
                    return true;
                }
            });
        }

    }
}

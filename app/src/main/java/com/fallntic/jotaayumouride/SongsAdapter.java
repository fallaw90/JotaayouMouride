package com.fallntic.jotaayumouride;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Model.Audio;

import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongsAdapterViewHolder> {

    private Context context;
    private List<Audio> arrayListAudios;
    private int selectedPosition;
    private RecyclerItemClickListener listener;

    private String songTitle;
    private String getSongDuration;
    private String getSongLink;
    private String mKey;

    public SongsAdapter(Context context, List<Audio> arrayListAudios, RecyclerItemClickListener listener) {
        this.context = context;
        this.arrayListAudios = arrayListAudios;
        this.listener = listener;
    }

    public SongsAdapter() {
    }

    @NonNull
    @Override
    public SongsAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_song, viewGroup, false);
        return new SongsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongsAdapterViewHolder holder, int position) {
        Audio audio = arrayListAudios.get(position);
        if (audio != null) {

            songTitle = audio.audioTitle;
            getSongDuration = audio.audioDuration;
            getSongLink = audio.audioUri;
            mKey = audio.audioID;

            if (selectedPosition == position) {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.green));
                holder.imageViewPlayActive.setVisibility(View.VISIBLE);
            } else {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                holder.imageViewPlayActive.setVisibility(View.INVISIBLE);
            }

            holder.textViewTitleSong.setText(songTitle);
            holder.textViewDuration.setText(getSongDuration);

            holder.bind(audio, listener);
        }
    }

    @Override
    public int getItemCount() {
        return arrayListAudios.size();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public void removeItem(int position) {

        if (position < arrayListAudios.size()) {
            arrayListAudios.remove(position);
            notifyItemRemoved(position);
        }
    }

    public interface RecyclerItemClickListener {
        void onClickListener(Audio audio, int position);
    }

    public class SongsAdapterViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitleSong;
        TextView textViewDuration;
        ImageView imageViewArtWork;
        ImageView imageViewPlayActive;

        public SongsAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewTitleSong = itemView.findViewById(R.id.tv_title);
            textViewDuration = itemView.findViewById(R.id.tv_duration);
            imageViewArtWork = itemView.findViewById(R.id.iv_artwork);
            imageViewPlayActive = itemView.findViewById(R.id.iv_play_active);
        }

        public void bind(final Audio audio, final RecyclerItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClickListener(audio, getLayoutPosition());
                }
            });
        }
    }
}

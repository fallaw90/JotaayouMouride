package com.fallntic.jotaayumouride.Adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.fallntic.jotaayumouride.Model.Announcement;
import com.fallntic.jotaayumouride.Model.Song;
import com.fallntic.jotaayumouride.R;

import java.io.IOException;
import java.util.List;


public class AnnouncementAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "AnnouncementAdapter";
    private final int TEXT_ANNOUNCEMENT = 1;
    private final int AUDIO_ANNOUNCEMENT = 2;
    boolean samePosition = false;
    private Context context;
    private MediaPlayer mPlayer;
    private boolean isPlaying = false;
    private int last_index = -1;
    private List<Object> listAnnouncement;

    public AnnouncementAdapter(Context context, List<Object> listAnnouncement) {
        this.context = context;
        this.listAnnouncement = listAnnouncement;

    }

    @Override
    public int getItemCount() {
        return listAnnouncement.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (listAnnouncement.get(position) instanceof Song) {
            return AUDIO_ANNOUNCEMENT;
        } else if (listAnnouncement.get(position) instanceof Announcement) {
            return TEXT_ANNOUNCEMENT;
        }

        return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        View view;

        switch (viewType) {
            case AUDIO_ANNOUNCEMENT:
                view = LayoutInflater.from(context).inflate(R.layout.recording_item_layout, parent, false);
                holder = new AnnouncementAudioViewHolder(view);
                break;

            case TEXT_ANNOUNCEMENT:
                view = LayoutInflater.from(context).inflate(R.layout.layout_announcement, parent, false);
                holder = new AnnouncementTextViewHolder(view);
                break;
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AnnouncementAudioViewHolder) {
            setUpData(holder, position);
        }
        if (holder instanceof AnnouncementTextViewHolder) {
            setTextAnnouncement(holder, position);
        }
    }


    private void setUpData(RecyclerView.ViewHolder holder, int position) {

        if (listAnnouncement.get(position) instanceof Song) {
            Song recording = (Song) listAnnouncement.get(position);
            if (recording.getAudioUri() != null && !recording.getAudioUri().equals("")) {

                ((AnnouncementAudioViewHolder) holder).textViewDate.setText(recording.getDate());
                ((AnnouncementAudioViewHolder) holder).textViewName.setText(recording.getAudioTitle());
                ((AnnouncementAudioViewHolder) holder).textViewDuration.setText(recording.getAudioDuration());

                if (recording.isPlaying()) {
                    ((AnnouncementAudioViewHolder) holder).imageViewPlay.setImageResource(R.drawable.ic_pause);
                    TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);
                    ((AnnouncementAudioViewHolder) holder).seekBar.setVisibility(View.VISIBLE);
                    ((AnnouncementAudioViewHolder) holder).seekUpdation(((AnnouncementAudioViewHolder) holder));
                } else {
                    ((AnnouncementAudioViewHolder) holder).imageViewPlay.setImageResource(R.drawable.ic_play);
                    TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);
                }

                ((AnnouncementAudioViewHolder) holder).manageSeekBar(((AnnouncementAudioViewHolder) holder));
            }
        }
        Log.d(TAG, "onBindViewHolder invoked: " + position);
    }

    private void setTextAnnouncement(RecyclerView.ViewHolder holder, int position) {

        if (listAnnouncement.get(position) instanceof Announcement) {

            Announcement announcement = (Announcement) listAnnouncement.get(position);

            if (announcement.getDate() != null && !announcement.getDate().equals("Date")) {

                ((AnnouncementTextViewHolder) holder).textViewUserName.setText(announcement.getUserName());
                ((AnnouncementTextViewHolder) holder).textViewtDate.setText(announcement.getDate());
                ((AnnouncementTextViewHolder) holder).textViewNote.setText(announcement.getNote());
            }
        }
    }

    public void removeItem(int position) {

        if (position < listAnnouncement.size()) {
            listAnnouncement.remove(position);
            notifyItemRemoved(position);
        }
    }

    public class AnnouncementAudioViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewPlay;
        SeekBar seekBar;
        TextView textViewName, textViewDuration, textViewDate;
        AnnouncementAudioViewHolder holder;
        private String recordingUri;
        private int lastProgress = 0;
        private Handler mHandler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                seekUpdation(holder);
            }
        };

        AnnouncementAudioViewHolder(View itemView) {
            super(itemView);

            imageViewPlay = itemView.findViewById(R.id.imageViewPlay);
            seekBar = itemView.findViewById(R.id.seekBar);
            textViewName = itemView.findViewById(R.id.textViewRecordingname);
            textViewDuration = itemView.findViewById(R.id.textView_duration);
            textViewDate = itemView.findViewById(R.id.textView_date);

            imageViewPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int position = getAdapterPosition();

                    if (listAnnouncement.get(position) instanceof Song) {

                        Song recording = (Song) listAnnouncement.get(position);

                        recordingUri = recording.getAudioUri();

                        if (isPlaying) {
                            stopPlaying();
                            if (position == last_index) {
                                recording.setPlaying(false);
                                stopPlaying();
                                notifyItemChanged(position);
                            } else {
                                markAllPaused();
                                recording.setPlaying(true);
                                notifyItemChanged(position);
                                startPlaying(recording, position);
                                last_index = position;
                            }

                        } else {
                            startPlaying(recording, position);
                            recording.setPlaying(true);
                            seekBar.setMax(mPlayer.getDuration());
                            Log.d("isPlayin", "False");
                            notifyItemChanged(position);
                            last_index = position;
                        }
                    }
                }
            });
        }

        void manageSeekBar(AnnouncementAudioViewHolder holder) {
            holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (mPlayer != null && fromUser) {
                        mPlayer.seekTo(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }

        private void markAllPaused() {
            for (int i = 0; i < listAnnouncement.size(); i++) {
                if (listAnnouncement.get(i) instanceof Song) {
                    Song song = (Song) listAnnouncement.get(i);
                    song.setPlaying(false);
                    listAnnouncement.set(i, song);
                }
            }
            notifyDataSetChanged();
        }

        private void seekUpdation(AnnouncementAudioViewHolder holder) {
            this.holder = holder;
            if (mPlayer != null) {
                int mCurrentPosition = mPlayer.getCurrentPosition();
                holder.seekBar.setMax(mPlayer.getDuration());
                holder.seekBar.setProgress(mCurrentPosition);
                lastProgress = mCurrentPosition;
            }
            mHandler.postDelayed(runnable, 100);
        }

        private void stopPlaying() {
            try {
                mPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mPlayer = null;
            isPlaying = false;
        }

        private void startPlaying(final Song song, final int position) {
            mPlayer = new MediaPlayer();
            try {
                mPlayer.setDataSource(recordingUri);
                mPlayer.prepare();
                mPlayer.start();
            } catch (IOException e) {
                Log.e("LOG_TAG", "prepare() failed");
            }
            //showing the pause button
            seekBar.setMax(mPlayer.getDuration());
            isPlaying = true;

            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    song.setPlaying(false);
                    notifyItemChanged(position);
                }
            });
        }

    }

    class AnnouncementTextViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUserName;
        TextView textViewtDate;
        TextView textViewNote;

        AnnouncementTextViewHolder(View itemView) {
            super(itemView);
            textViewUserName = itemView.findViewById(R.id.textView_userName);
            textViewtDate = itemView.findViewById(R.id.textView_date);
            textViewNote = itemView.findViewById(R.id.textView_note);

        }
    }
}
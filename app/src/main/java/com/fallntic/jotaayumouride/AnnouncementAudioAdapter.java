package com.fallntic.jotaayumouride;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Model.Audio;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Manish on 10/8/2017.
 */

public class AnnouncementAudioAdapter extends RecyclerView.Adapter<AnnouncementAudioAdapter.ViewHolder> {

    private ArrayList<Audio> recordingArrayList;
    private Context context;
    private MediaPlayer mPlayer;
    private boolean isPlaying = false;
    private int last_index = -1;

    public AnnouncementAudioAdapter(Context context, ArrayList<Audio> recordingArrayList) {
        this.context = context;
        this.recordingArrayList = recordingArrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.recording_item_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        setUpData(holder, position);
    }

    @Override
    public int getItemCount() {
        return recordingArrayList.size();
    }


    private void setUpData(ViewHolder holder, int position) {

        Audio recording = recordingArrayList.get(position);
        holder.textViewName.setText(recording.getAudioTitle());

        if (recording.isPlaying()) {
            holder.imageViewPlay.setImageResource(R.drawable.ic_pause);
            TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);
            holder.seekBar.setVisibility(View.VISIBLE);
            holder.seekUpdation(holder);
        } else {
            holder.imageViewPlay.setImageResource(R.drawable.ic_play);
            TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);
            holder.seekBar.setVisibility(View.GONE);
        }


        holder.manageSeekBar(holder);

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewPlay;
        SeekBar seekBar;
        TextView textViewName;
        private String recordingUri;
        private int lastProgress = 0;
        private Handler mHandler = new Handler();
        ViewHolder holder;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                seekUpdation(holder);
            }
        };

        public ViewHolder(View itemView) {
            super(itemView);

            imageViewPlay = itemView.findViewById(R.id.imageViewPlay);
            seekBar = itemView.findViewById(R.id.seekBar);
            textViewName = itemView.findViewById(R.id.textViewRecordingname);

            imageViewPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    Audio recording = recordingArrayList.get(position);

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

            });
        }

        public void manageSeekBar(ViewHolder holder) {
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
            for (int i = 0; i < recordingArrayList.size(); i++) {
                recordingArrayList.get(i).setPlaying(false);
                recordingArrayList.set(i, recordingArrayList.get(i));
            }
            notifyDataSetChanged();
        }

        private void seekUpdation(ViewHolder holder) {
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

        private void startPlaying(final Audio audio, final int position) {
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
                    audio.setPlaying(false);
                    notifyItemChanged(position);
                }
            });
        }

    }
}
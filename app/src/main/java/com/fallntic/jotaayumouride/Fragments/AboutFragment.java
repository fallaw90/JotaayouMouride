package com.fallntic.jotaayumouride.fragments;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.R;
import com.fallntic.jotaayumouride.adapter.VideoAdapter;
import com.fallntic.jotaayumouride.model.YouTubeVideos;

import java.util.Vector;


public class AboutFragment extends Fragment {

    private final Vector<YouTubeVideos> youtubeVideos = new Vector<>();
    private TextView textViewAbout;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        textViewAbout = view.findViewById(R.id.about);
        recyclerView = view.findViewById(R.id.recyclerView);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        textViewAbout.setText(Html.fromHtml(getString(R.string.about)));

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        youtubeVideos.add(new YouTubeVideos("<iframe scrolling=\"no\" width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/hU5EETPYB2Y\" frameborder=\"1\"  allowfullscreen></iframe>"));
        VideoAdapter videoAdapter = new VideoAdapter(youtubeVideos);
        recyclerView.setAdapter(videoAdapter);
    }
}

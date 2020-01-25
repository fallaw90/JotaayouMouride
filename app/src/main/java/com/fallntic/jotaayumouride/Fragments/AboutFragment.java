package com.fallntic.jotaayumouride.Fragments;

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

import com.fallntic.jotaayumouride.Adapter.VideoAdapter;
import com.fallntic.jotaayumouride.Model.YouTubeVideos;
import com.fallntic.jotaayumouride.R;

import java.util.Vector;


public class AboutFragment extends Fragment {

    RecyclerView recyclerView;
    Vector<YouTubeVideos> youtubeVideos = new Vector<YouTubeVideos>();
    private View view;
    private TextView mTextViewAbout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_about, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mTextViewAbout = view.findViewById(R.id.about);
        mTextViewAbout.setText(Html.fromHtml(getString(R.string.about)));

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        youtubeVideos.add(new YouTubeVideos("<iframe scrolling=\"no\" width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/hU5EETPYB2Y\" frameborder=\"1\"  allowfullscreen></iframe>"));
        VideoAdapter videoAdapter = new VideoAdapter(youtubeVideos);
        recyclerView.setAdapter(videoAdapter);
    }
}

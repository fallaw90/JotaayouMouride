package com.fallntic.jotaayumouride;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.adapter.VideoAdapter;
import com.fallntic.jotaayumouride.model.YouTubeVideos;

import java.util.Vector;

public class AboutActivity extends AppCompatActivity {

    private final Vector<YouTubeVideos> youtubeVideos = new Vector<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.logo);

        TextView textViewAbout = findViewById(R.id.about);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        textViewAbout.setText(Html.fromHtml(getString(R.string.about)));

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (youtubeVideos.size() > 0)
            youtubeVideos.clear();
        youtubeVideos.add(new YouTubeVideos("<iframe scrolling=\"no\" width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/hU5EETPYB2Y\" frameborder=\"1\"  allowfullscreen></iframe>"));
        VideoAdapter videoAdapter = new VideoAdapter(youtubeVideos);
        recyclerView.setAdapter(videoAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem iconBack, instructions;
        iconBack = menu.findItem(R.id.icon_back);
        instructions = menu.findItem(R.id.instructions);
        instructions.setVisible(false);
        iconBack.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                break;

            case R.id.icon_back:
                finish();
                break;

            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
        return true;
    }
}
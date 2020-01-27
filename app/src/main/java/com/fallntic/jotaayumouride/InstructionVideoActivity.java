package com.fallntic.jotaayumouride;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Adapter.VideoAdapter;
import com.fallntic.jotaayumouride.Model.YouTubeVideos;

import java.util.Objects;
import java.util.Vector;

public class InstructionVideoActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Vector<YouTubeVideos> youtubeVideos = new Vector<YouTubeVideos>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction_video);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        //toolbar.setLogo(R.mipmap.logo);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.logo);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        youtubeVideos.add(new YouTubeVideos("<iframe scrolling=\"no\" width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/tmfeKCkzhMU\" frameborder=\"1\" allowfullscreen></iframe>"));
        youtubeVideos.add(new YouTubeVideos("<iframe scrolling=\"no\" width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/5s-foVIejZQ\" frameborder=\"1\" allowfullscreen></iframe>"));
        youtubeVideos.add(new YouTubeVideos("<iframe scrolling=\"no\" width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/C-K3lBTN6ao\" frameborder=\"1\" allowfullscreen></iframe>"));
        youtubeVideos.add(new YouTubeVideos("<iframe scrolling=\"no\" width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/mxmrVZ0snOQ\" frameborder=\"1\" allowfullscreen></iframe>"));

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
        }
        return true;
    }
}

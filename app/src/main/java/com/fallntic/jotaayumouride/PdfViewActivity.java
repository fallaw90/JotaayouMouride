package com.fallntic.jotaayumouride;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fallntic.jotaayumouride.Model.UploadPdf;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;

import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.hideProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.showProgressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.progressBar;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutData;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.relativeLayoutProgressBar;

public class PdfViewActivity extends AppCompatActivity {
    WebView webview;
    UploadPdf pdf_file;
    String url;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_view);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        //toolbar.setLogo(R.mipmap.logo);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.logo);

        Intent intent = getIntent();
        pdf_file = (UploadPdf) intent.getSerializableExtra("pdf_file");

        initViews();

        webview = findViewById(R.id.web_view);
        webview.requestFocus();
        webview.getSettings().setJavaScriptEnabled(true);

        try {
            url = URLEncoder.encode(pdf_file.getUrl(), "UTF-8");
            webview.loadUrl("https://docs.google.com/gview?embedded=true&url=" + url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webview.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100) {
                    showProgressBar();
                }
                if (progress >= 100) {
                    hideProgressBar();
                }
            }
        });

        HomeActivity.loadBannerAd(this, this);

    }

    private void initViews() {

        initViewsProgressBar();
    }

    private void initViewsProgressBar() {
        relativeLayoutData = findViewById(R.id.relativeLayout_data);
        relativeLayoutProgressBar = findViewById(R.id.relativeLayout_progressBar);
        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.destroy();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (HomeActivity.bannerAd != null) {
            HomeActivity.bannerAd.resume();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem iconBack;
        iconBack = menu.findItem(R.id.icon_back);

        iconBack.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                startActivity(new Intent(this, HomeActivity.class));
                break;

            case R.id.icon_back:
                finish();
                break;

            case R.id.instructions:
                //Load ads
                HomeActivity.loadInterstitialAd(this);
                startActivity(new Intent(this, InstructionsActivity.class));
                break;
        }
        return true;
    }
}

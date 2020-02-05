package com.fallntic.jotaayumouride.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fallntic.jotaayumouride.R;

public class ViewHolderSongCategory {
    public ImageView imageViewSongCategory;
    public TextView textViewSongCategory;

    ViewHolderSongCategory(View v) {
        imageViewSongCategory = v.findViewById(R.id.imageView_songCategory);
        textViewSongCategory = v.findViewById(R.id.textView_songCategory);
    }
}

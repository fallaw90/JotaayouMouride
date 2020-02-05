package com.fallntic.jotaayumouride.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.fallntic.jotaayumouride.R;
import com.fallntic.jotaayumouride.model.GridViewSongCategory;

import java.util.ArrayList;
import java.util.List;

public class SongCategory extends BaseAdapter {

    private List<GridViewSongCategory> mGridViewSongCategories;
    private Context context;

    public SongCategory(Context context) {
        this.context = context;
        this.mGridViewSongCategories = new ArrayList<>();
        Resources resources = context.getResources();
        String[] tempAudioType = resources.getStringArray(R.array.gridview_audio);
        int[] imageViewSongCategory = {R.drawable.khassaide, R.drawable.mix, R.drawable.zikr, R.drawable.coran};
        for (int i = 0; i < imageViewSongCategory.length; i++) {
            GridViewSongCategory tempGridViewSongCategory = new GridViewSongCategory(imageViewSongCategory[i], tempAudioType[i]);
            mGridViewSongCategories.add(tempGridViewSongCategory);
        }
    }

    @Override
    public int getCount() {
        return mGridViewSongCategories.size();
    }

    @Override
    public Object getItem(int position) {
        return mGridViewSongCategories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        ViewHolderSongCategory holder = null;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.single_gridview_item, parent, false);
            holder = new ViewHolderSongCategory(row);
            row.setTag(holder);
        } else {
            holder = (ViewHolderSongCategory) row.getTag();
        }

        GridViewSongCategory temp = mGridViewSongCategories.get(position);
        holder.imageViewSongCategory.setImageResource(temp.getImageId());
        holder.textViewSongCategory.setText(temp.getNameAudio());
        return row;
    }

    public List<GridViewSongCategory> getGridViewSongCategories() {
        return mGridViewSongCategories;
    }

    public void setGridViewSongCategories(List<GridViewSongCategory> gridViewSongCategories) {
        mGridViewSongCategories = gridViewSongCategories;
    }
}


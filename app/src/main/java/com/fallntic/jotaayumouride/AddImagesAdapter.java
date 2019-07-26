package com.fallntic.jotaayumouride;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class AddImagesAdapter extends RecyclerView.Adapter<AddImagesAdapter.ViewHolder>{

    public List<String> fileNameList;
    public List<String> fileDoneList;

    public AddImagesAdapter(List<String> fileNameList, List<String>fileDoneList){

        this.fileDoneList = fileDoneList;
        this.fileNameList = fileNameList;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_add_image, parent, false);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        String fileName = fileNameList.get(position);
        holder.fileNameView.setText(fileName);

        String fileDone = fileDoneList.get(position);

        if(fileDone.equals("uploading")){

            holder.fileDoneView.setImageResource(R.mipmap.progress);

        } else {

            holder.fileDoneView.setImageResource(R.mipmap.checked);
        }
    }

    @Override
    public int getItemCount() {
        return fileNameList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView fileNameView;
        public ImageView fileDoneView;
        View mView;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            fileNameView = mView.findViewById(R.id.upload_filename);
            fileDoneView = mView.findViewById(R.id.upload_loading);
        }
    }
}

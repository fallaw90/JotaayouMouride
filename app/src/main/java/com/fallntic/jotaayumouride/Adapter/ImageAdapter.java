package com.fallntic.jotaayumouride.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.CustomItemClickListener;
import com.fallntic.jotaayumouride.Model.Image;
import com.fallntic.jotaayumouride.R;
import com.fallntic.jotaayumouride.Utility.GlideApp;

import java.util.List;


public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private Context context;
    private List<Image> listImage;
    private CustomItemClickListener listener;

    public ImageAdapter(Context context, List<Image> listImage, CustomItemClickListener listener) {
        this.context = context;
        this.listImage = listImage;
        this.listener = listener;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_images, parent, false);

        final ImageViewHolder imageViewHolder = new ImageViewHolder(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(view, imageViewHolder.getPosition());
            }
        });

        return imageViewHolder;
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        Image image = listImage.get(position);

        GlideApp.with(context)
                .load(image.getUri())
                .centerCrop()
                .into(holder.imageView);

        holder.getView().setAnimation(AnimationUtils.loadAnimation(context, R.anim.zoom_in));
    }

    @Override
    public int getItemCount() {
        return listImage.size();
    }

    public void removeItem(int position) {

        listImage.remove(position);

        notifyItemRemoved(position);
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        protected ImageView imageView;
        private View view;

        public ImageViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            imageView = itemView.findViewById(R.id.imageView);
        }

        public View getView() {
            return view;
        }
    }
}
package com.fallntic.jotaayumouride.utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fallntic.jotaayumouride.R;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.Objects;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class PhotoFullPopupWindow extends PopupWindow {

    final View view;
    final Context mContext;
    final PhotoView photoView;
    final ProgressBar loading;
    final ViewGroup parent;


    @SuppressLint("InflateParams")
    public PhotoFullPopupWindow(Context ctx, View v, String imageUrl, Bitmap bitmap) {
        super(((LayoutInflater) Objects.requireNonNull(ctx.getSystemService(LAYOUT_INFLATER_SERVICE))).inflate(R.layout.popup_photo_full, null), ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        if (Build.VERSION.SDK_INT >= 21) {
            setElevation(5.0f);
        }
        this.mContext = ctx;
        this.view = getContentView();
        ImageButton closeButton = this.view.findViewById(R.id.ib_close);
        setOutsideTouchable(true);

        setFocusable(true);
        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                dismiss();
            }
        });
        //---------Begin customising this popup--------------------

        photoView = view.findViewById(R.id.image);
        loading = view.findViewById(R.id.loading);
        photoView.setMaximumScale(6);
        parent = (ViewGroup) photoView.getParent();
        //----------------------------
        if (bitmap != null) {
            loading.setVisibility(View.GONE);
            parent.setBackground(new BitmapDrawable(mContext.getResources(), Constants.fastblur(Bitmap.createScaledBitmap(bitmap, 50, 50, true))));// ));
            photoView.setImageBitmap(bitmap);
        } else {
            loading.setIndeterminate(true);
            loading.setVisibility(View.VISIBLE);
            GlideApp.with(ctx).asBitmap()
                    .load(imageUrl)

                    .error(R.drawable.logo_dahira)
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            loading.setIndeterminate(false);
                            loading.setBackgroundColor(Color.LTGRAY);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            parent.setBackground(new BitmapDrawable(mContext.getResources(), Constants.fastblur(Bitmap.createScaledBitmap(resource, 50, 50, true))));// ));
                            photoView.setImageBitmap(resource);

                            loading.setVisibility(View.GONE);
                            return false;
                        }
                    })


                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(photoView);

            showAtLocation(v, Gravity.CENTER, 0, 0);
        }
        //------------------------------

    }
}
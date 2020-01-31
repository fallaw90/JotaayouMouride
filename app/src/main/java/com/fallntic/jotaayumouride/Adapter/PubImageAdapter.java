package com.fallntic.jotaayumouride.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.R;
import com.fallntic.jotaayumouride.model.PubImage;

import java.util.List;

import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showImage;

@SuppressWarnings("unused")
public class PubImageAdapter extends RecyclerView.Adapter<PubImageAdapter.DahiraViewHolder> {
    public static final String TAG = "DahiraAdapter";
    private final Context context;
    private final List<PubImage> listPub;

    private ImageView imageView;

    public PubImageAdapter(Context context, List<PubImage> listPub) {
        this.context = context;
        this.listPub = listPub;
    }

    @NonNull
    @Override
    public DahiraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DahiraViewHolder(
                LayoutInflater.from(context).inflate(R.layout.layout_pub, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull DahiraViewHolder holder, int position) {
        PubImage pubImage = listPub.get(position);

        if (holder.textViewDescription != null && holder.textViewTitle != null && imageView != null) {
            holder.textViewTitle.setText(pubImage.getTitle());
            holder.textViewDescription.setText(Html.fromHtml(pubImage.getDescription()));
            showImage(context, pubImage.getImage_uri(), imageView);
        }
    }

    @Override
    public int getItemCount() {
        return listPub.size();
    }

    class DahiraViewHolder extends RecyclerView.ViewHolder {

        final TextView textViewTitle;
        final TextView textViewDescription;

        DahiraViewHolder(View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.textView_title);
            textViewDescription = itemView.findViewById(R.id.textView_description);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
package com.fallntic.jotaayumouride.Adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.Model.PubImage;
import com.fallntic.jotaayumouride.R;

import java.util.List;

import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.showImage;

public class PubImageAdapter extends RecyclerView.Adapter<PubImageAdapter.DahiraViewHolder> {
    public static final String TAG = "DahiraAdapter";
    private Context context;
    private List<PubImage> listPub;

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

        holder.textViewTitle.setText(pubImage.getTitle());
        holder.textViewDescription.setText(Html.fromHtml(pubImage.getDescription()));

        showImage(context, pubImage.getImage_uri(), imageView);
    }

    @Override
    public int getItemCount() {
        return listPub.size();
    }

    class DahiraViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle;
        TextView textViewDescription;

        public DahiraViewHolder(View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.textView_title);
            textViewDescription = itemView.findViewById(R.id.textView_description);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
package com.fallntic.jotaayumouride.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.HomeActivity;
import com.fallntic.jotaayumouride.Model.Dahira;
import com.fallntic.jotaayumouride.R;

import java.util.List;

import static com.fallntic.jotaayumouride.Utility.DataHolder.dahira;
import static com.fallntic.jotaayumouride.Utility.DataHolder.indexOnlineUser;
import static com.fallntic.jotaayumouride.Utility.DataHolder.onlineUser;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.getListSongs;
import static com.fallntic.jotaayumouride.Utility.MyStaticFunctions.showImage;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listImage;
import static com.fallntic.jotaayumouride.Utility.MyStaticVariables.listSong;

public class DahiraAdapter extends RecyclerView.Adapter<DahiraAdapter.DahiraViewHolder> {
    public static final String TAG = "DahiraAdapter";
    private Context context;
    private List<Dahira> dahiraList;

    private ImageView imageView;

    public DahiraAdapter(Context context, List<Dahira> dahiraList) {
        this.context = context;
        this.dahiraList = dahiraList;
    }

    @NonNull
    @Override
    public DahiraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DahiraViewHolder(
                LayoutInflater.from(context).inflate(R.layout.layout_dahira, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull DahiraViewHolder holder, int position) {
        Dahira dahira = dahiraList.get(position);

        holder.textViewDahiraName.setText("Dahira " + dahira.getDahiraName());
        holder.textViewDieuwrine.setText("Dieuwrine: " + dahira.getDieuwrine());
        holder.textViewPhoneNumber.setText("Telephone: " + dahira.getDahiraPhoneNumber());
        holder.textViewSiege.setText("Siege: " + dahira.getSiege());
        holder.textViewTotalMembers.setText("Membres inscrits: " + dahira.getTotalMember());

        showImage(context, dahira.getImageUri(), imageView);
    }

    @Override
    public int getItemCount() {
        return dahiraList.size();
    }

    class DahiraViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textViewDahiraName;
        TextView textViewDieuwrine;
        TextView textViewPhoneNumber;
        TextView textViewSiege;
        TextView textViewTotalMembers;

        public DahiraViewHolder(View itemView) {
            super(itemView);

            textViewDahiraName = itemView.findViewById(R.id.textview_dahiraName);
            textViewDieuwrine = itemView.findViewById(R.id.textview_dieuwrine);
            textViewPhoneNumber = itemView.findViewById(R.id.textview_phoneNumber);
            textViewSiege = itemView.findViewById(R.id.textview_siege);
            imageView = itemView.findViewById(R.id.imageView);
            textViewTotalMembers = itemView.findViewById(R.id.textView_totalMembers);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (listSong != null) {
                listSong.clear();
                listSong = null;
            }
            if (listImage != null) {
                listImage.clear();
                listImage = null;
            }

            dahira = dahiraList.get(getAdapterPosition());

            if (onlineUser.getListDahiraID().contains(dahira.getDahiraID())) {
                indexOnlineUser = onlineUser.getListDahiraID().indexOf(dahira.getDahiraID());
            } else
                indexOnlineUser = -1;

            getListSongs(context);
            HomeActivity.loadInterstitialAd(context);

        }
    }
}
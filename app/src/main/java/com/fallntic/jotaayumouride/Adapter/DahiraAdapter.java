package com.fallntic.jotaayumouride.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.HomeActivity;
import com.fallntic.jotaayumouride.R;
import com.fallntic.jotaayumouride.model.Dahira;

import java.util.List;

import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.getListSongs;
import static com.fallntic.jotaayumouride.utility.MyStaticFunctions.showImage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.dahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.indexOnlineUser;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listImage;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.listSong;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.onlineUser;

public class DahiraAdapter extends RecyclerView.Adapter<DahiraAdapter.DahiraViewHolder> {
    @SuppressWarnings("unused")
    public static final String TAG = "DahiraAdapter";
    private final Context context;
    private final List<Dahira> dahiraList;

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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull DahiraViewHolder holder, int position) {
        Dahira dahira = dahiraList.get(position);

        showImage(context, dahira.getImageUri(), holder.imageView);
        holder.textViewDahiraName.setText("Dahira " + dahira.getDahiraName());
        holder.textViewDieuwrine.setText("Dieuwrine: " + dahira.getDieuwrine());
        holder.textViewPhoneNumber.setText("Telephone: " + dahira.getDahiraPhoneNumber());
        holder.textViewSiege.setText("Siege: " + dahira.getSiege());

        if (onlineUser != null && onlineUser.getUserPhoneNumber().equals("+13208030902")) {
            holder.textViewTotalMembers.setText("Membres inscrits: " + dahira.getTotalMember());
        }
    }

    @Override
    public int getItemCount() {
        return dahiraList.size();
    }

    class DahiraViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final ImageView imageView;
        final TextView textViewDahiraName;
        final TextView textViewDieuwrine;
        final TextView textViewPhoneNumber;
        final TextView textViewSiege;
        final TextView textViewTotalMembers;

        DahiraViewHolder(View itemView) {
            super(itemView);

            textViewDahiraName = itemView.findViewById(R.id.textview_dahiraName);
            textViewDieuwrine = itemView.findViewById(R.id.textview_dieuwrine);
            textViewPhoneNumber = itemView.findViewById(R.id.textview_phoneNumber);
            textViewSiege = itemView.findViewById(R.id.textview_siege);
            imageView = itemView.findViewById(R.id.imageView);
            textViewTotalMembers = itemView.findViewById(R.id.textView_totalMembers);

            if ((dahira != null && onlineUser != null && !onlineUser.getListDahiraID().contains(dahira.getDahiraID()))
                    || !onlineUser.getUserPhoneNumber().equals("+13208030902")) {
                textViewTotalMembers.setVisibility(View.GONE);
            }

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
            HomeActivity.showInterstitialAd(context);
        }
    }
}
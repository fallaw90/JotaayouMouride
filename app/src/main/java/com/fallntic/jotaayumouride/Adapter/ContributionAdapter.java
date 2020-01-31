package com.fallntic.jotaayumouride.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fallntic.jotaayumouride.R;

import java.util.List;

import static com.fallntic.jotaayumouride.utility.MyStaticVariables.adiya;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.dahira;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.sass;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.social;
import static com.fallntic.jotaayumouride.utility.MyStaticVariables.typeOfContribution;

public class ContributionAdapter extends RecyclerView.Adapter<ContributionAdapter.ContributionViewHolder> {

    private static List<String> listDate;
    private static List<String> listAmount;
    private static List<String> listUserName;
    private final Context context;

    public ContributionAdapter(Context context, List<String> listDate, List<String> listAmount, List<String> listUserName) {
        this.context = context;
        ContributionAdapter.listDate = listDate;
        ContributionAdapter.listAmount = listAmount;
        ContributionAdapter.listUserName = listUserName;
    }

    public static List<String> getListDate() {
        return listDate;
    }

    public static List<String> getListAmount() {
        return listAmount;
    }

    public static List<String> getListUserName() {
        return listUserName;
    }

    @NonNull
    @Override
    public ContributionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ContributionViewHolder(
                LayoutInflater.from(context).inflate(R.layout.layout_contribution, parent, false)
        );
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ContributionViewHolder holder, int position) {
        if (!listDate.isEmpty())
            holder.textViewDate.setText(listDate.get(position));

        if (!listAmount.isEmpty())
            holder.textViewAmount.setText(listAmount.get(position));

        if (!listUserName.isEmpty())
            holder.textViewSavedBy.setText("Enregistré par " + listUserName.get(position));

    }

    @Override
    public int getItemCount() {
        return listAmount.size();
    }

    public void removeItem(int position) {

        int indexAmount;
        String dateRemoved = listDate.get(position);
        String amountRemoved = listAmount.get(position);
        listDate.remove(position);
        listAmount.remove(position);

        if (listUserName.isEmpty()) {
            listUserName.size();
        }

        switch (typeOfContribution) {
            case "adiya":
                indexAmount = adiya.getListAdiya().indexOf(amountRemoved);
                if (adiya.getListDahiraID().get(indexAmount).equals(dahira.getDahiraID()) &&
                        adiya.getListDate().get(indexAmount).equals(dateRemoved)) {

                    adiya.getListDahiraID().remove(indexAmount);
                    adiya.getListDate().remove(indexAmount);
                    adiya.getListAdiya().remove(indexAmount);
                    adiya.getListUserName().remove(indexAmount);

                    if (listUserName.isEmpty()) {
                        listUserName.size();
                    }
                }

                break;
            case "sass":
                indexAmount = sass.getListSass().indexOf(amountRemoved);
                if (sass.getListDahiraID().get(indexAmount).equals(dahira.getDahiraID()) &&
                        sass.getListDate().get(indexAmount).equals(dateRemoved)) {

                    sass.getListDahiraID().remove(indexAmount);
                    sass.getListDate().remove(indexAmount);
                    sass.getListSass().remove(indexAmount);
                    sass.getListUserName().remove(indexAmount);

                    if (listUserName.isEmpty()) {
                        listUserName.size();
                    }
                }
                break;
            case "social":
                indexAmount = social.getListSocial().indexOf(amountRemoved);
                if (social.getListDahiraID().get(indexAmount).equals(dahira.getDahiraID()) &&
                        social.getListDate().get(indexAmount).equals(dateRemoved)) {

                    social.getListDahiraID().remove(indexAmount);
                    social.getListDate().remove(indexAmount);
                    social.getListSocial().remove(indexAmount);
                    social.getListUserName().remove(indexAmount);

                    if (listUserName.isEmpty()) {
                        listUserName.size();
                    }
                }
                break;
        }

        notifyItemRemoved(position);
    }

    class ContributionViewHolder extends RecyclerView.ViewHolder {

        final TextView textViewDate;
        final TextView textViewAmount;
        final TextView textViewSavedBy;

        ContributionViewHolder(View itemView) {
            super(itemView);

            textViewDate = itemView.findViewById(R.id.textView_date);
            textViewAmount = itemView.findViewById(R.id.textView_amountAdiya);
            textViewSavedBy = itemView.findViewById(R.id.textView_savedBy);
        }
    }
}
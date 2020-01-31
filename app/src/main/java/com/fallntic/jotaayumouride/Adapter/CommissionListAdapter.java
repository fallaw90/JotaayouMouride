package com.fallntic.jotaayumouride.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fallntic.jotaayumouride.R;

import java.util.List;

public class CommissionListAdapter extends BaseAdapter {

    private final List<String> listCommission;
    private final List<String> listCommissionResponsible;

    private final LayoutInflater inflater;

    public CommissionListAdapter(Context applicationContext, List<String> listCommission, List<String> listCommissionResponsible) {
        this.listCommission = listCommission;
        this.listCommissionResponsible = listCommissionResponsible;
        inflater = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return listCommission.size();
    }

    public Object getItem(int position) {
        return listCommission.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.list_commission, null);
        TextView textViewCommission = view.findViewById(R.id.textView_commission);
        TextView textViewResponsible = view.findViewById(R.id.textView_responsible);

        if (listCommission.size() > i && listCommissionResponsible.size() > i) {
            textViewCommission.setText(listCommission.get(i));
            textViewResponsible.setText(listCommissionResponsible.get(i));
        }

        return view;
    }
}
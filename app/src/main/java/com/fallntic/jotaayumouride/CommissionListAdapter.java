package com.fallntic.jotaayumouride;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CommissionListAdapter extends BaseAdapter {

    Context context;

    ArrayList<String> listCommission = new ArrayList<String>();
    ArrayList<String> listCommissionResponsible = new ArrayList<String>();

    LayoutInflater inflater;

    public CommissionListAdapter(Context applicationContext, ArrayList<String> listCommission, ArrayList<String> listCommissionResponsible) {
        this.context = context;
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

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.list_commission, null);
        TextView textViewCommission = (TextView) view.findViewById(R.id.textView_commission);
        TextView textViewResponsible = (TextView) view.findViewById(R.id.textView_responsible);

        textViewCommission.setText(listCommission.get(i));
        textViewResponsible.setText(listCommissionResponsible.get(i));

        return view;
    }
}
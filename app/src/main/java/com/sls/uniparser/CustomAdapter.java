package com.sls.uniparser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class CustomAdapter extends BaseAdapter {

    private LayoutInflater lInflater;
    private ArrayList<String> mCurrentListEmail;

    CustomAdapter(Context ctx) {

        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCurrentListEmail = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mCurrentListEmail.size();
    }

    @Override
    public Object getItem(int position) {

        return mCurrentListEmail.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }
    public ArrayList<String> getAdapterList()
    {
        return mCurrentListEmail;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.raw_layout, parent, false);
        }
        String tmp = (String) getItem(position);
        TextView rawEmail= (TextView)view.findViewById(R.id.textEmail);
        rawEmail.setText(tmp);

        return view;
    }
}

package com.sls.uniparser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;


public class CustomAdapter extends BaseAdapter {

    private LayoutInflater lInflater;
    private static Set<String> mCurrentSetEmail;

    CustomAdapter(Context ctx, Set<String> setEmails) {

        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mCurrentSetEmail = setEmails;
    }

    @Override
    public int getCount() {
        return mCurrentSetEmail.size();
    }

    @Override
    public Object getItem(int position) {
        Iterator<String> it = mCurrentSetEmail.iterator();
        for (int i = 0; i < position; i++) {
            if (it.hasNext())
                it.next();
        }
        return it.next();
    }

    public void updateListView(String newEmail)
    {
        ArrayList<String> tmp = new ArrayList<>();
        tmp.addAll(mCurrentSetEmail);
        tmp.add(newEmail);
        mCurrentSetEmail.clear();
        mCurrentSetEmail.addAll(tmp);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
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

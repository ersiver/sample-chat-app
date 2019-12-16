package com.breiter.chatter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.breiter.chatter.R;

public class OptionsMenuAdapter extends BaseAdapter {
    private Context context;
    private String[] items;

    public OptionsMenuAdapter(Context context, String[] items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(layoutInflater != null) {
                view = layoutInflater.inflate(R.layout.item_attachment, viewGroup, false);
                TextView itemTextView = view.findViewById(R.id.itemTextView);
                ImageView itemImageView = view.findViewById(R.id.itemImageView);
                itemTextView.setText(items[i]);
                itemImageView.setImageResource(getImg(i));
            }
        }

        return view;
    }

    private int getImg(int index) {
        switch (index) {
            case 0:
                return R.drawable.ic_photo;
            case 1:
                return R.drawable.ic_camera;
            case 2:
                return R.drawable.ic_cancel;
            default:
                return -1;
        }
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int i) {
        return items[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
}


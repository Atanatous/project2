package com.example.user.project2;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class GridViewAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> mImageUrls;

    public GridViewAdapter(Context context, ArrayList<String> urls) {
        this.mContext = context;
        this.mImageUrls = urls;
    }

    public int getCount() {
        return mImageUrls.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(400, 400));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(16, 8, 16, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        for (int i = 0; i < mImageUrls.size(); i++) {
            Log.d(null, mImageUrls.get(i));
        }

        Glide.with(mContext)
                .load(mImageUrls.get(position))
                .into(imageView);

        return imageView;
    }
}

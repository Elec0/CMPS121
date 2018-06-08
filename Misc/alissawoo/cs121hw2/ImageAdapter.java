package com.example.alissawoo.cs121hw2;


import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;


public class ImageAdapter extends BaseAdapter {
    private Context mContext;

    public ImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(350, 350));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 20, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }

    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.bookcase, R.drawable.desk_chair,
            R.drawable.floor_lamp, R.drawable.folding_chair,
            R.drawable.plastic_drawer, R.drawable.plastic_storage_container,
            R.drawable.plastic_table, R.drawable.printer,
            R.drawable.sofa_bed, R.drawable.sample_2,
            R.drawable.waste_basket
    };
}
package com.licitacion.adapters;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.licitacion.MainActivity;
import com.licitacion.R;

import java.util.ArrayList;

public class GalleryAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> urls;
    private LayoutInflater inflater=null;

    public GalleryAdapter(Context context, ArrayList<String> urls){
        this.context = context;
        this.urls = urls;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return urls.size();
    }

    @Override
    public Object getItem(int position) {
        return urls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView img = null;
        ProgressBar pb = null;

        if(convertView == null)
            convertView = inflater.inflate(R.layout.gall, parent, false);

        img = ViewHolder.get(convertView, R.id.gallImageView);
        String url = "file:///" + urls.get(position);
        ProgressBar load = new ProgressBar(MainActivity.context);
        MainActivity.loadImage(url, img, load);

        return convertView;
    }

    public static class ViewHolder {
        @SuppressWarnings("unchecked")
        public static <T extends View> T get(View view, int id) {
            SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
            if (viewHolder == null) {
                viewHolder = new SparseArray<View>();
                view.setTag(viewHolder);
            }
            View childView = viewHolder.get(id);
            if (childView == null) {
                childView = view.findViewById(id);
                viewHolder.put(id, childView);
            }
            return (T) childView;
        }
    }

    public void updateAdapter(ArrayList<String> urls){
        this.urls = urls;
        notifyDataSetChanged();
    }

}

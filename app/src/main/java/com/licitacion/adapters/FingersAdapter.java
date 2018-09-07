package com.licitacion.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.licitacion.MainActivity;
import com.licitacion.R;
import com.licitacion.fragments.MainFragment;
import com.licitacion.objs.UserObj;

import java.util.ArrayList;

public class FingersAdapter extends BaseAdapter {

    private LayoutInflater inflater=null;
    private ArrayList<UserObj> array;

    public FingersAdapter(Activity activity, ArrayList<UserObj> array){
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.array = array;
    }

    @Override
    public int getCount() {
        return array.size();
    }

    @Override
    public Object getItem(int position) {
        return array.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = inflater.inflate(R.layout.finger_row, parent, false);

        ProgressBar load = new ProgressBar(MainActivity.context);

        ImageView pic = ViewHolder.get(convertView, R.id.pic);
        String url = "file:///" + array.get(position).picPath;
        MainActivity.loadImage(url, pic, load);

        ImageView finger = ViewHolder.get(convertView, R.id.finger);
        String url1= "file:///" + array.get(position).fingerPath;
        MainActivity.loadImage(url1, finger, load);

        TextView name = ViewHolder.get(convertView, R.id.name);
        name.setText(array.get(position).name);

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

    public void updateAdapter(ArrayList<UserObj> array){
        this.array = array;
        notifyDataSetChanged();
    }
}
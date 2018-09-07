package com.licitacion.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.licitacion.MainActivity;
import com.licitacion.R;
import com.licitacion.utils.LSB2bit;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PicDetailFragment extends Fragment implements View.OnClickListener {

    private int lay;
    private ImageView pic;
    private TextView desc;
    private String path;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        lay = bundle.getInt("lay");
        path = bundle.getString("path");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("setCurrentFragment", this.getClass().getSimpleName());
        MainActivity.currentFragment = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.pic_frag, container, false);

        pic = (ImageView) rootView.findViewById(R.id.pic);
        String url = "file:///" + path;
        ProgressBar load = new ProgressBar(MainActivity.context);
        MainActivity.loadImage(url, pic, load);

        Bitmap bitmap = getBM();
        desc = (TextView) rootView.findViewById(R.id.desc);
        String[] aux0 = decode(bitmap).split("[_]");
        String body = "Fecha:\n"+dateFormat(Long.parseLong(aux0[2]))+"\n\nLatitud:\n"+aux0[4]+"\n\nLongitud\n"+aux0[6]+
                "\n\nPeso:\n"+aux0[8]+" bytes\nTamaño:\n"+bitmap.getWidth()+"x"+bitmap.getHeight();
        desc.setText(body);
        //s = "user_"+userObj.id+"_"+System.currentTimeMillis()+"_lat_"+0+"_lon_"+0+"_peso_"+image.length();

        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login:

                break;
            case R.id.regis:

                break;
        }
    }

    private String decode(Bitmap bmp){
        byte[] b = null;
        try {
            int[] pixels = new int[bmp.getWidth() * bmp.getHeight()];
            bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
            b = LSB2bit.convertArray(pixels);
        } catch (OutOfMemoryError er) {
            System.out.println( "Image too large, out of memory!");
        }
        final String vvv = LSB2bit.decodeMessage(b, bmp.getWidth(), bmp.getHeight());
        return vvv;
    }

    private Bitmap getBM(){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }

    private static String dateFormat(long time) {
        String date = "";
        date = new SimpleDateFormat("dd MMMM yyyy hh:mm:ss").format(new Date(time));
        return date;
    }

}

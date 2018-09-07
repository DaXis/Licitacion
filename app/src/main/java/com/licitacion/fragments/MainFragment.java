package com.licitacion.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.licitacion.MainActivity;
import com.licitacion.R;
import com.licitacion.adapters.GalleryAdapter;
import com.licitacion.custom.ExpandableHeightGridView;
import com.licitacion.interfaces.LocationTracker;
import com.licitacion.interfaces.ProgressHandler;
import com.licitacion.objs.UserObj;
import com.licitacion.objs.ValidationObj;
import com.licitacion.utils.LSB2bit;
import com.licitacion.utils.MobiProgressBar;
import com.licitacion.utils.ProviderLocationTracker;
import com.licitacion.utils.ScalingUtilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainFragment extends Fragment implements View.OnClickListener, LocationTracker, LocationTracker.LocationUpdateListener {

    private int lay;
    private ArrayList<String> imgs = new ArrayList<>();
    private UserObj userObj;
    private ImageView pic;
    private TextView name, email;
    private Button add_pic;
    private GalleryAdapter galleryAdapter;
    private ExpandableHeightGridView gridView;
    private File image;
    private String img_path;
    private final Handler handler = new Handler();
    private MobiProgressBar progressBar;
    private static final int ACTION_TAKE_PHOTO = 1;
    private ProviderLocationTracker gps;
    private ProviderLocationTracker net;
    private boolean isRunning;
    private LocationUpdateListener listener;
    private static Location lastLoc;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        lay = bundle.getInt("lay");
        userObj = (UserObj)bundle.getSerializable("user");
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
        View rootView = inflater.inflate(R.layout.main_frag, container, false);

        gps = new ProviderLocationTracker(MainActivity.context, ProviderLocationTracker.ProviderType.GPS);
        net = new ProviderLocationTracker(MainActivity.context, ProviderLocationTracker.ProviderType.NETWORK);
        start();

        pic = (ImageView)rootView.findViewById(R.id.pic);
        String url = "file:///" + userObj.picPath;
        ProgressBar load = new ProgressBar(MainActivity.context);
        MainActivity.loadImage(url, pic, load);

        name = (TextView) rootView.findViewById(R.id.name);
        name.setText(userObj.name);
        email = (TextView)rootView.findViewById(R.id.email);
        email.setText(userObj.email);

        add_pic  = (Button) rootView.findViewById(R.id.add_pic);
        add_pic.setOnClickListener(this);

        imgs = getUserImgs();
        galleryAdapter = new GalleryAdapter(getActivity(), imgs);
        gridView = (ExpandableHeightGridView)rootView.findViewById(R.id.gridView);
        gridView.setExpanded(true);
        gridView.setAdapter(galleryAdapter);
        galleryAdapter.notifyDataSetChanged();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                initPicDetFragment((String)galleryAdapter.getItem(position));
            }
        });

        progressBar=new MobiProgressBar(MainActivity.context);

        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_pic:
                //initTakePic(userObj.id+"_pic"+System.currentTimeMillis());
                File file = new File(Environment.getExternalStorageDirectory(), "app_icon.png");
                encodeImg(file.getAbsolutePath());
                break;
        }
    }

    private ArrayList<String> getUserImgs(){
        ArrayList<String> imgs = new ArrayList<>();
        String path = MainActivity.cache.getAbsolutePath();
        File directory = new File(path);
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            if(files[i].getName().contains(userObj.id+"_pic")){
                imgs.add(files[i].getAbsolutePath());
            }
        }
        return imgs;
    }

    public void initTakePic(String name) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(MainActivity.activity.getPackageManager()) != null) {
            File photoFile = createImageFile(name);
            if (photoFile != null) {
                Log.d("photoFile", photoFile.getAbsolutePath());
                /*Uri photoURI = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() +
                        ".provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);*/
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, ACTION_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile(String name) {
        image = new File(MainActivity.cache, name);
        img_path = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTION_TAKE_PHOTO && resultCode == MainActivity.activity.RESULT_OK) {
            encodeImg(compressImg(img_path, 720, 480));
            setPic();
        }
    }

    private Uri encodeImg(String img_path) {
        Log.d("img_path", img_path);
        Uri result=null;
        //EditText text = (EditText) findViewById(R.id.message);
        //String s = text.getText().toString();
        String s = "CaxrqTOzLqBoxQn9|3aag7gzWc06WEg8j|S4g4rp4_4ndr01d_kuku_supa_xD_M4q";
        //String s = null;

        /*if(lastLoc != null){
            s = "user_"+userObj.id+"_"+System.currentTimeMillis()+"_lat_"+round(lastLoc.getLatitude(), 6)+
                    "_lon_"+round(lastLoc.getLongitude(), 6)+"_peso_"+image.length();
        } else {
            s = "user_"+userObj.id+"_"+System.currentTimeMillis()+"_lat_"+0+"_lon_"+0+"_peso_"+image.length();
        }*/

        String absoluteFilePathSource = img_path;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap sourceBitmap = BitmapFactory.decodeFile(img_path, options);
        //selected_photo.setImageBitmap(bitmap);

        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();

        int[] oneD = new int[width * height];
        sourceBitmap.getPixels(oneD, 0, width, 0, 0, width, height);
        int density = sourceBitmap.getDensity();
        sourceBitmap.recycle();
        Log.d("absoluteFilePathSource", absoluteFilePathSource);
        byte[] byteImage = LSB2bit.encodeMessage(oneD, width, height, s, new ProgressHandler() {
            private int mysize;
            private int actualSize;

            public void increment(final int inc) {
                actualSize+=inc;
                if(actualSize%mysize==0)
                    handler.post(mIncrementProgress);
            }

            public void setTotal(final int tot) {
                mysize=tot/50;
                handler.post(mInitializeProgress);
            }

            public void finished() {

            }
        });

        oneD=null;
        sourceBitmap = null;
        int[] oneDMod = LSB2bit.byteArrayToIntArray(byteImage);
        byteImage=null;
        Log.v("Encode", "" + oneDMod[0]);
        Log.v("Encode Alpha", "" + (oneDMod[0] >> 24 & 0xFF));
        Log.v("Encode Red", "" + (oneDMod[0] >> 16 & 0xFF));
        Log.v("Encode Green", "" + (oneDMod[0] >> 8 & 0xFF));
        Log.v("Encode Blue", "" + (oneDMod[0] & 0xFF));

        System.gc();
        Log.v("Free memory", Runtime.getRuntime().freeMemory()+"");
        Log.v("Image mesure", (width*height*32/8)+"");

        Bitmap destBitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);

        destBitmap.setDensity(density);
        int partialProgr=height*width/50;
        int masterIndex = 0;
        for (int j = 0; j < height; j++)
            for (int i = 0; i < width; i++){
                // The unique way to write correctly the sourceBitmap, android bug!!!
                destBitmap.setPixel(i, j, Color.argb(0xFF,
                        oneDMod[masterIndex] >> 16 & 0xFF,
                        oneDMod[masterIndex] >> 8 & 0xFF,
                        oneDMod[masterIndex++] & 0xFF));
                if(masterIndex%partialProgr==0)
                    handler.post(mIncrementProgress);
            }
        handler.post(mSetInderminate);
        Log.v("Encode", "" + destBitmap.getPixel(0, 0));
        Log.v("Encode Alpha", "" + (destBitmap.getPixel(0, 0) >> 24 & 0xFF));
        Log.v("Encode Red", "" + (destBitmap.getPixel(0, 0) >> 16 & 0xFF));
        Log.v("Encode Green", "" + (destBitmap.getPixel(0, 0) >> 8 & 0xFF));
        Log.v("Encode Blue", "" + (destBitmap.getPixel(0, 0) & 0xFF));

        String sdcardState = android.os.Environment.getExternalStorageState();
        String destPath = null;
        int indexSepar = absoluteFilePathSource.lastIndexOf(File.separator);
        int indexPoint = absoluteFilePathSource.lastIndexOf(".");
        if(indexPoint<=1)
            indexPoint = absoluteFilePathSource.length();
        /*String fileNameDest=absoluteFilePathSource.substring(indexSepar+1, indexPoint);
        fileNameDest+="_encode";*/
        if (sdcardState.contentEquals(android.os.Environment.MEDIA_MOUNTED))
            //destPath = Singleton.getCacheCarpet().getAbsolutePath() + File.separator + fileNameDest+".png";
            destPath = img_path;

        OutputStream fout = null;
        try {
            Log.v("Path", destPath);
            fout = new FileOutputStream(destPath);
            destBitmap.compress(Bitmap.CompressFormat.PNG, 100, fout);
            //Media.insertImage(getContentResolver(),destPath, fileNameDest, "MobiStego Encoded");
            result=Uri.parse("file://"+destPath);
            MainActivity.activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
            fout.flush();
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        destBitmap.recycle();
        Log.d("result", result.getPath());
        return result;
    }

    private String compressImg(String path, int DESIREDWIDTH, int DESIREDHEIGHT) {
        String strMyImagePath = null;
        Bitmap scaledBitmap = null;

        try {
            // Part 1: Decode image
            Bitmap unscaledBitmap = ScalingUtilities.decodeFile(path, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);
            if (!(unscaledBitmap.getWidth() <= DESIREDWIDTH && unscaledBitmap.getHeight() <= DESIREDHEIGHT)) {
                // Part 2: Scale image
                scaledBitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, DESIREDWIDTH, DESIREDHEIGHT,
                        ScalingUtilities.ScalingLogic.FIT);
            } else {
                unscaledBitmap.recycle();
                return path;
            }

            //String s = "tmp.png";

            File f = new File(path);

            strMyImagePath = f.getAbsolutePath();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            scaledBitmap.recycle();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (strMyImagePath == null) {
            return path;
        }
        return strMyImagePath;
    }

    final Runnable mIncrementProgress = new Runnable() {
        public void run() {
            progressBar.incrementProgressBy(1);
        }
    };

    final Runnable mInitializeProgress = new Runnable() {
        public void run() {
            progressBar.setMax(100);
        }
    };

    final Runnable mSetInderminate= new Runnable() {
        public void run() {
            progressBar.setMessage("Procesando");
            progressBar.setIndeterminate(true);
        }
    };

    private void setPic() {
        imgs = getUserImgs();
        galleryAdapter.updateAdapter(imgs);
    }

    public void start(){
        if(isRunning){
            return;
        }
        gps.start(this);
        net.start(this);
        isRunning = true;
    }

    public void start(LocationUpdateListener update) {
        start();
        listener = update;
    }

    public void stop(){
        if(isRunning){
            gps.stop();
            net.stop();
            isRunning = false;
            listener = null;
        }
    }

    public boolean hasLocation(){
        //If either has a location, use it
        return gps.hasLocation() || net.hasLocation();
    }

    public boolean hasPossiblyStaleLocation(){
        //If either has a location, use it
        return gps.hasPossiblyStaleLocation() || net.hasPossiblyStaleLocation();
    }

    public Location getLocation(){
        Location ret = gps.getLocation();
        if(ret == null){
            ret = net.getLocation();
        }
        return ret;
    }

    public Location getPossiblyStaleLocation(){
        Location ret = gps.getPossiblyStaleLocation();
        if(ret == null){
            ret = net.getPossiblyStaleLocation();
        }
        return ret;
    }

    public void onUpdate(Location oldLoc, long oldTime, Location newLoc, long newTime) {
        lastLoc = newLoc;
        Log.d("lat lon", lastLoc.getLatitude()+", "+lastLoc.getLongitude());
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    private void initPicDetFragment(String path){
        Bundle bundle = new Bundle();
        bundle.putInt("lay", lay);
        bundle.putString("path", path);
        PicDetailFragment picDetailFragment = new PicDetailFragment();
        picDetailFragment.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .replace(lay, picDetailFragment)
                .addToBackStack(null)
                .commit();
    }

}

package com.licitacion;

//0513

import android.Manifest;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbException;
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbHost;
import com.licitacion.db.DBHelper;
import com.licitacion.fragments.FingerDetailFragment;
import com.licitacion.fragments.LoginFragment;
import com.licitacion.fragments.MainFragment;
import com.licitacion.fragments.RegisterFragment;
import com.licitacion.fragments.ValidationFragment;
import com.licitacion.utils.ForceCloseCatch;
import com.licitacion.utils.Globals;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    public static ReaderCollection readers;
    public static String device_name;
    private FrameLayout mainContent;
    private LoginFragment loginFragment;
    public static Fragment currentFragment;
    private final String TAG = getClass().getSimpleName();
    private Bundle savedInstanceState = null;
    public static Context context;
    public static AppCompatActivity activity;
    private static final String ACTION_USB_PERMISSION = "com.digitalpersona.uareu.dpfpddusbhost.USB_PERMISSION";
    private final int WRITE_EXTERNAL_STORAGE = 12, READ_EXTERNAL_STORAGE = 13,
            CAMERA = 14;
    public static DBHelper dbh;
    public static SQLiteDatabase db;
    public static File cache;
    private static ImageLoaderConfiguration config;
    private static DisplayImageOptions defaultOptions;
    public static String KEY = "3aag7gzWc06WEg8j";
    private static final String DB_PATH = "/data/data/com.licitacion/databases/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        setContentView(R.layout.activity_main);
        Thread.setDefaultUncaughtExceptionHandler(new ForceCloseCatch(this));
        mainContent = (FrameLayout)findViewById(R.id.main_content);
        context = this;
        activity = this;

        //************************
        dbh = new DBHelper(this, "Lic", null, 1);
        db = dbh.getWritableDatabase();

        if(cache == null)
            cache = context.getExternalCacheDir();
        if (!cache.exists()) {
            cache.mkdirs();
        }
        initImageLoader(this);

        initFragments();
        int currentapiVersion = Build.VERSION.SDK_INT;
        //if (currentapiVersion >= 23)
            checkWritePermission();
        //************************

        initFragments();
        //getReaderDevice();
        //getPermission();
    }

    private void getReaderDevice() {
        try {
            Context applContext = getApplicationContext();
            readers = Globals.getInstance().getReaders(applContext);
        } catch (UareUException e) {
            e.printStackTrace();
        }
        int nSize = readers.size();
        if (nSize != 0) {
            device_name = (nSize == 0 ? "" : readers.get(0).GetDescription().name);
        }
        initLoginFragment();
    }

    private void initFragments(){
        loginFragment = new LoginFragment();
    }

    private void removeFragments(){
        if(currentFragment != null){
            Log.d("fragment remove", currentFragment.getClass().toString());
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.remove(currentFragment).commit();
        }
    }

    public void initLoginFragment(){
        if(currentFragment != loginFragment){
            //loginFragment = new LoginFragment();
            removeFragments();
            Bundle bundle = new Bundle();
            bundle.putInt("lay", mainContent.getId());
            if(loginFragment.getArguments() == null)
                loginFragment.setArguments(bundle);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(mainContent.getId(), loginFragment)
                    .addToBackStack(TAG)
                    .commit();
        }
    }

    /*public void initMainFragment(){
        if(currentFragment != mainFragment){
            //mainFragment = new MainFragment();
            removeFragments();
            Bundle bundle = new Bundle();
            bundle.putInt("lay", mainContent.getId());
            if(mainFragment.getArguments() == null)
                mainFragment.setArguments(bundle);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(mainContent.getId(), mainFragment)
                    .addToBackStack(TAG)
                    .commit();
        }
    }*/

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        onCreate(savedInstanceState);
        super.onConfigurationChanged(newConfig);
    }

    private void getPermission(){
        Context applContext = MainActivity.context;
        PendingIntent mPermissionIntent;
        mPermissionIntent = PendingIntent.getBroadcast(applContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        applContext.registerReceiver(mUsbReceiver, filter);

        try {
            if(DPFPDDUsbHost.DPFPDDUsbCheckAndRequestPermissions(applContext, mPermissionIntent, device_name)) {
                CheckDevice();
            }
        } catch (DPFPDDUsbException e) {
            e.printStackTrace();
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null) {
                            CheckDevice();
                        }
                    }
                    else {

                    }
                }
            }
        }
    };

    protected void CheckDevice() {
        try {
            Reader m_reader = Globals.getInstance().getReader(device_name, this);
            m_reader.Open(Reader.Priority.EXCLUSIVE);
            Reader.Capabilities cap = m_reader.GetCapabilities();
            m_reader.Close();
        }
        catch (UareUException e1) {
            e1.printStackTrace();
        }
    }

    @TargetApi(23)
    private void checkWritePermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE);
            return;
        } else {
            checkReadPermission();
        }
    }

    @TargetApi(23)
    private void checkReadPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE);
            return;
        } else {
            checkCameraPermission();
        }
    }

    @TargetApi(23)
    private void checkCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA},
                    CAMERA);
            return;
        } else {
            initLoginFragment();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    checkReadPermission();
                }
                break;
            case READ_EXTERNAL_STORAGE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    checkCameraPermission();
                }
                break;
            case CAMERA:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    initLoginFragment();
                }
                break;
        }
    }

    private static void initImageLoader(Context context) {
        if (defaultOptions == null)
            defaultOptions = new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(R.drawable.ic_add_profile)
                    .showImageOnFail(R.drawable.ic_add_profile)
                    .resetViewBeforeLoading(true)
                    .cacheOnDisk(true)
                    .cacheInMemory(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .considerExifParams(true)
                    .displayer(new FadeInBitmapDisplayer(300))
                    .build();

        config = new ImageLoaderConfiguration.Builder(context)
                //.imageDownloader(new SecureImageDownloader(context, 3000, 3000))
                .threadPriority(Thread.NORM_PRIORITY)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(150 * 1024 * 1024) // 150 Mb
                .memoryCacheExtraOptions(480, 800)
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs()
                .threadPoolSize(10)
                .build();

        ImageLoader.getInstance().init(config);
    }

    public static DisplayImageOptions getDefaultOptions() {
        return defaultOptions;
    }

    public static void loadImage(final String url, final ImageView imageView, final ProgressBar load) {
        ImageLoader.getInstance().displayImage(url, imageView, defaultOptions, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                load.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                String message = null;
                switch (failReason.getType()) {
                    case IO_ERROR:
                        message = "Error de entrada o de salida";
                        break;
                    case DECODING_ERROR:
                        message = "La imagen no pudo ser decodificada";
                        break;
                    case NETWORK_DENIED:
                        message = "La descarga fue denegada";
                        break;
                    case OUT_OF_MEMORY:
                        message = "Out Of Memory error";
                        break;
                    case UNKNOWN:
                        message = "Error desconocido";
                        break;
                }
                Log.i(url, message);
                load.setVisibility(View.GONE);
                //String aux = "http://mvlprdwbsza01.sagarpa.gob.mx:8080/backend/provpimaf/prod/valesdocs/thumb/";
                /*String aux = "http://mvlprdwbsza01.sagarpa.gob.mx:8080/backend/provpimaf/qa/valesdocs/thumb/";
                String pic = imageUri.replace(aux, "");
                pic = pic.replace("_FOTO.jpg", "");
                File file = new File(Singleton.getCacheCarpet(), pic);
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                imageView.setImageBitmap(bitmap);*/
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                load.setVisibility(android.view.View.GONE);
            }
        });
    }

    public static void clearImageCache(String url) {
        MemoryCacheUtils.removeFromCache(url, ImageLoader.getInstance().getMemoryCache());
        DiskCacheUtils.removeFromCache(url, ImageLoader.getInstance().getDiskCache());
    }

    @Override
    public void onBackPressed(){
        if(currentFragment.getClass() == LoginFragment.class) {
            finish();
        } else if(currentFragment.getClass() == FingerDetailFragment.class){
            ((FingerDetailFragment)currentFragment).resetScann();
            super.onBackPressed();
        } else if(currentFragment.getClass() == RegisterFragment.class){
            ((RegisterFragment)currentFragment).resetScann();
            super.onBackPressed();
        } else if(currentFragment.getClass() == ValidationFragment.class){
            ((ValidationFragment)currentFragment).resetScann();
            super.onBackPressed();
        } else
            super.onBackPressed();
    }

    public static void copyBD(){
        try {

            File bd = null;
            if(bd == null)
                bd = new File(Environment.getExternalStorageDirectory(), "base_lic");
            if (!bd.exists()) {
                bd.mkdirs();
            }

            String path = bd.getAbsolutePath()+"/Lic.sqlite";
            Log.d("copy db", path);
            OutputStream databaseOutputStream = new FileOutputStream(path);
            InputStream databaseInputStream;

            byte[] buffer = new byte[1024];
            @SuppressWarnings("unused")
            int length;

            File file = new File(DB_PATH, "Lic");
            databaseInputStream = new FileInputStream(file);

            while ((length = databaseInputStream.read(buffer)) > 0) {
                databaseOutputStream.write(buffer);
            }

            databaseInputStream.close();
            databaseOutputStream.flush();
            databaseOutputStream.close();
            //file.delete();
            //Log.v("copyDataBase()", "copy ended");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}


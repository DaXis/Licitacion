package com.licitacion.fragments;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbHost;
import com.licitacion.MainActivity;
import com.licitacion.R;
import com.licitacion.objs.UserObj;
import com.licitacion.utils.Encript;
import com.licitacion.utils.Globals;
import com.licitacion.utils.LSB2bit;
import com.licitacion.utils.MobiProgressBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.licitacion.interfaces.ProgressHandler;
import com.licitacion.utils.ScalingUtilities;

public class RegisterFragment extends Fragment implements View.OnClickListener {

    private int lay;
    private ImageView pic, finger;
    private TextView pic_txt;
    private EditText name, email, pass;
    private Button regis;
    private Reader.CaptureResult cap_result = null;
    private Reader m_reader = null;
    private Bitmap m_bitmap = null;
    private Engine m_engine = null;
    private Fmd m_fmd = null;
    private int m_DPI = 0;
    private String m_enginError;
    private boolean m_reset = false;
    private String m_textString;
    private String m_text_conclusionString;
    private int m_score = -1;
    private boolean m_first = true, exito = false;
    private boolean m_resultAvailableToDisplay = false;
    private static final int ACTION_TAKE_PHOTO = 1;
    private File image, finger_file, fingerb_file;
    private String img_path;
    private final Handler handler = new Handler();
    private MobiProgressBar progressBar;
    private long time;
    //*************************
    /*private ExecutorService threadPoolExecutor;
    private Runnable runnable;
    private Future longRunningTaskFuture;*/
    //*************************

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        lay = bundle.getInt("lay");
        time = System.currentTimeMillis();
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
        if(m_reset){
            //m_reset = false;
            primaryFunction();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.regis_frag, container, false);

        finger = (ImageView)rootView.findViewById(R.id.finger);
        pic = (ImageView)rootView.findViewById(R.id.pic);
        pic.setOnClickListener(this);
        pic_txt = (TextView) rootView.findViewById(R.id.pic_txt);
        name = (EditText) rootView.findViewById(R.id.name);
        email = (EditText) rootView.findViewById(R.id.email);
        pass = (EditText) rootView.findViewById(R.id.pass);
        regis = (Button)rootView.findViewById(R.id.regis);
        regis.setOnClickListener(this);

        m_bitmap = Globals.GetLastBitmap();
        if (m_bitmap == null)
            m_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.finger);
        finger.setImageBitmap(m_bitmap);
        progressBar=new MobiProgressBar(MainActivity.context);
        UpdateGUI();
        primaryFunction();
        return rootView;
    }

    public void UpdateGUI() {
        if(finger != null){
            finger.setImageBitmap(m_bitmap);
            finger.invalidate();
        }
    }

    private void primaryFunction(){
            try {
                Context applContext = MainActivity.context;
                //if(m_reader == null && m_engine == null){
                    m_reader = Globals.getInstance().getReader(MainActivity.device_name, applContext);
                    m_reader.Open(Reader.Priority.EXCLUSIVE);
                    m_DPI = Globals.GetFirstDPI(m_reader);
                    m_engine = UareUGlobal.GetEngine();
                //}
            } catch (Exception e) {
                //e.printStackTrace();
                //MainActivity.activity.onBackPressed();
                return;
            }

            // loop capture on a separate thread to avoid freezing the UI
        //final ExecutorService threadPoolExecutor = Executors.newSingleThreadExecutor();
        new Thread(new Runnable() {
            /*Runnable runnable = this;
            Future longRunningTaskFuture = threadPoolExecutor.submit(runnable);*/
                @Override
                public void run() {
                    m_reset = false;
                    while (!m_reset) {
                        try {
                            //if(cap_result == null)
                                cap_result = m_reader.Capture(Fid.Format.ANSI_381_2004, Globals.DefaultImageProcessing, m_DPI, -1);
                        } catch (Exception e) {
                            if (!m_reset) {
                                //Log.w("UareUSampleJava", "error during capture: " + e.toString());
                            }
                            //MainActivity.activity.onBackPressed();
                        }

                        m_resultAvailableToDisplay = false;

                        // an error occurred
                        if (cap_result == null || cap_result.image == null) continue;

                        try {
                            m_enginError = "";
                            m_bitmap = Globals.GetBitmapFromRaw(cap_result.image.getViews()[0].getImageData(), cap_result.image.getViews()[0].getWidth(), cap_result.image.getViews()[0].getHeight());
                            finger_file = new File(MainActivity.cache, "finger_"+time);
                            fingerb_file = new File(MainActivity.cache, "fingerb_"+time);
                            saveBM(finger_file, m_bitmap);
                            FileOutputStream fos = new FileOutputStream(fingerb_file);
                            fos.write(cap_result.image.getData());
                            fos.close();
                        } catch (Exception e) {
                            m_enginError = e.toString();
                            //Log.w("UareUSampleJava", "Engine error: " + e.toString());
                        }

                        MainActivity.activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                UpdateGUI();
                            }
                        });
                    }
                }
            }).start();

    }

    private void saveBM(File filename, Bitmap bmp){
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            //e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.regis:
                if(validations())
                    genRegister();
                break;
            case R.id.pic:
                initTakePic("lic_"+time);
                break;
        }
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
        String s = time+"_registro_lic_sagarpa";
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
            //e.printStackTrace();
        }
        destBitmap.recycle();
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
                //e.printStackTrace();
            } catch (Exception e) {
                //e.printStackTrace();
            }

            scaledBitmap.recycle();
        } catch (Throwable e) {
            //e.printStackTrace();
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
        String url = "file:///" + img_path;
        ProgressBar load = new ProgressBar(MainActivity.context);
        MainActivity.loadImage(url, pic, load);
        pic_txt.setVisibility(View.GONE);
    }

    private boolean validations(){
        if(name.getText().length() > 0){
            if(email.getText().length() > 0){
                if(pass.getText().length() > 0){
                    if(image != null){
                        if(finger_file != null){
                            return true;
                        } else
                            return false;
                    } else
                        return false;
                } else
                    return false;
            } else
                return false;
        } else
            return false;
    }

    private void genRegister(){
        UserObj userObj = new UserObj();
        userObj.name = Encript.encrypt(name.getText().toString(), MainActivity.KEY);
        userObj.email = Encript.encrypt(email.getText().toString(), MainActivity.KEY);
        userObj.pass = Encript.encrypt(pass.getText().toString(), MainActivity.KEY);
        userObj.fingerPath = Encript.encrypt(finger_file.getAbsolutePath(), MainActivity.KEY);
        userObj.picPath = Encript.encrypt(image.getAbsolutePath(), MainActivity.KEY);
        exito = MainActivity.dbh.newRegister(MainActivity.db, userObj);
        MainActivity.activity.onBackPressed();
    }

    public void resetScann(){
        m_reset = true;
        try {
            try {
                cap_result = null;
                Globals.ClearLastBitmap();
                m_reader.CancelCapture();
                if(!exito){
                    finger_file.delete();
                    fingerb_file.delete();
                    image.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            m_reader.Close();
        }
        catch (Exception e) {
            Log.w("UareUSampleJava", "error during reader shutdown");
        }
    }

}

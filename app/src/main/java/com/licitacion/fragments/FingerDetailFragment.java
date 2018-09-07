package com.licitacion.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUGlobal;
import com.licitacion.MainActivity;
import com.licitacion.R;
import com.licitacion.adapters.FingersAdapter;
import com.licitacion.objs.UserObj;
import com.licitacion.objs.ValidationObj;
import com.licitacion.utils.Globals;
import com.licitacion.utils.LSB2bit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FingerDetailFragment extends Fragment implements View.OnClickListener {

    private int lay;
    private ImageView finger, pic;
    private Reader.CaptureResult cap_result = null;
    private Reader m_reader = null;
    private Bitmap m_bitmap = null;
    private Engine m_engine = null;
    private Fmd m_fmd = null;
    private int m_DPI = 0;
    private String m_enginError;
    private boolean m_reset = false;
    private String m_textString;
    private String m_text_conclusionString, m_text_conclusionString_b;
    private int m_score = -1;
    private boolean m_first = true, ok = false;
    private static TextView log, fecha;
    private ArrayList<ValidationObj> fingers = new ArrayList<>();
    private UserObj userObj;
    private ValidationObj aux;
    private String path, name, picPath;
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
        path = bundle.getString("path");
        name = bundle.getString("name");
        picPath = bundle.getString("picPath");
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
        View rootView = inflater.inflate(R.layout.detail_frag, container, false);

        finger = (ImageView)rootView.findViewById(R.id.finger);
        pic = (ImageView)rootView.findViewById(R.id.pic);
        log = (TextView)rootView.findViewById(R.id.log);
        log.setText(path);
        fecha = (TextView)rootView.findViewById(R.id.fecha);

        m_bitmap = Globals.GetLastBitmap();
        if (m_bitmap == null)
            m_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.finger);
        finger.setImageBitmap(m_bitmap);
        UpdateGUI();
        primaryFunction();

        return rootView;
    }

    public void UpdateGUI() {
        if(finger != null){
            finger.setImageBitmap(m_bitmap);
            finger.invalidate();
        }
        if(log != null && m_text_conclusionString != null)
            log.setText(m_text_conclusionString);
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
            e.printStackTrace();
            //log.setText("line 105: "+e.toString());
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
                    } catch (final Exception e) {
                        if (!m_reset) {
                            Log.w("UareUSampleJava", "error during capture: " + e.toString());
                            MainActivity.activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //log.setText("line 147: "+e.toString());
                                }
                            });
                        }
                    }

                    // an error occurred
                    if (cap_result == null || cap_result.image == null) continue;

                    try {
                        File file = new File(path);
                        final byte[] byteArray = convertFileToByteArray(file);

                        m_bitmap = Globals.GetBitmapFromRaw(cap_result.image.getViews()[0].getImageData(), cap_result.image.getViews()[0].getWidth(),
                                cap_result.image.getViews()[0].getHeight());

                        m_fmd = m_engine.CreateFmd(byteArray, cap_result.image.getViews()[0].getWidth(),cap_result.image.getViews()[0].getHeight(),
                                m_DPI,0,0, Fmd.Format.ANSI_378_2004);
                        m_score = m_engine.Compare(m_fmd, 0, m_engine.CreateFmd(cap_result.image, Fmd.Format.ANSI_378_2004), 0);

                        if(m_score < (0x7FFFFFFF / 100000)) {
                            ok = true;
                        } else {
                            ok = false;
                        }
                    } catch (final Exception e) {
                        Log.w("UareUSampleJava", "Engine error: " + e.toString());
                        MainActivity.activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //log.setText("line 171: "+e.toString());
                            }
                        });
                    }

                    MainActivity.activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(ok) {
                                m_text_conclusionString = "La huella conincide con "+name;
                                pic.setImageBitmap(getBM());
                                pic.invalidate();
                                fecha.setText(dateFormat(getTime()));
                            } else {
                                m_text_conclusionString = "La huella no coincide con la registrada por " + name;
                                pic.setImageResource(R.drawable.ic_add_profile);
                                fecha.setText("");
                            }
                            UpdateGUI();
                        }
                    });
                }
            }
        }).start();

    }

    public static byte[] convertFileToByteArray(File f) {
        byte[] byteArray = null;
        try {
            InputStream inputStream = new FileInputStream(f);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024*8];
            int bytesRead =0;

            while ((bytesRead = inputStream.read(b)) != -1) {
                bos.write(b, 0, bytesRead);
            }
            byteArray = bos.toByteArray();
        }
        catch (final IOException e) {
            e.printStackTrace();
            MainActivity.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //log.setText("line 221: "+e.toString());
                }
            });
        }
        return byteArray;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

        }
    }

    public void resetScann(){
        m_reset = true;
        try {
            try {
                cap_result = null;
                Globals.ClearLastBitmap();
                m_reader.CancelCapture();
            } catch (Exception e) {
                e.printStackTrace();
            }
            m_reader.Close();
        }
        catch (Exception e) {
            Log.w("UareUSampleJava", "error during reader shutdown");
        }
    }

    private static String dateFormat(long time) {
        String date = "";
        date = new SimpleDateFormat("dd MMMM yyyy hh:mm:ss").format(new Date(time));
        return date;
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
        Bitmap bitmap = BitmapFactory.decodeFile(picPath, options);
        return bitmap;
    }

    private long getTime(){
        String[] aux2 = decode(getBM()).split("[_]");
        return Long.parseLong(aux2[0]);
    }

}

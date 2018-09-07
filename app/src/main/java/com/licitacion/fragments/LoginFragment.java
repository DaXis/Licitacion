package com.licitacion.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.licitacion.MainActivity;
import com.licitacion.R;
import com.licitacion.objs.UserObj;
import com.licitacion.utils.Globals;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private int lay;
    private Button login, regis, fingers, dbButton;
    private TextView dbPath;
    private final int DURACION_SPLASH = 5000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        lay = bundle.getInt("lay");
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
        enableBtns(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.login_frag, container, false);

        login = (Button)rootView.findViewById(R.id.login);
        login.setOnClickListener(this);

        regis = (Button)rootView.findViewById(R.id.regis);
        regis.setOnClickListener(this);

        fingers = (Button)rootView.findViewById(R.id.fingers);
        fingers.setOnClickListener(this);

        dbButton = (Button)rootView.findViewById(R.id.dbButton);
        dbButton.setOnClickListener(this);

        dbPath = (TextView) rootView.findViewById(R.id.dbPath);

        //getReaderDevice();
        enableBtns(true);

        return rootView;
    }

    private void enableBtns(boolean enable){
        if(login != null && regis != null && fingers != null){
            login.setEnabled(enable);
            regis.setEnabled(enable);
            fingers.setEnabled(enable);
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.login:
                initMainFragment();
                /*dbPath.setText("");
                if(getReaderDevice()){
                    enableBtns(false);
                    initValidationFragment();
                } else {
                    enableBtns(false);
                    Log.d("sin leector", "no se a detectado un leector");
                    dbPath.setText("No hay un leector conectado al celular, conecta un lector despues de que se cierre la apliación");
                    closeApp();
                }*/
                break;
            case R.id.regis:
                dbPath.setText("");
                if(getReaderDevice()){
                    enableBtns(false);
                    initRegisFragment();
                } else {
                    enableBtns(false);
                    Log.d("sin leector", "no se a detectado un leector");
                    dbPath.setText("No hay un leector conectado al celular, conecta un lector despues de que se cierre la apliación");
                    closeApp();
                }
                break;
            case R.id.fingers:
                dbPath.setText("");
                if(getReaderDevice()){
                    enableBtns(false);
                    initFingersFragment();
                } else {
                    enableBtns(false);
                    Log.d("sin leector", "no se a detectado un leector");
                    dbPath.setText("No hay un leector conectado al celular, conecta un lector despues de que se cierre la apliación");
                    closeApp();
                }
                break;
            case R.id.dbButton:
                MainActivity.copyBD();
                dbPath.setText("La base de datos se a extraido en la carpeta \"base_lic\"");
                break;
        }
    }

    private boolean getReaderDevice() {
        if(MainActivity.readers.size() == 0){
            try {
                Context applContext = getActivity();
                MainActivity.readers = Globals.getInstance().getReaders(applContext);
            } catch (UareUException e) {
                e.printStackTrace();
                return false;
            }
            int nSize = MainActivity.readers.size();
            if (nSize != 0) {
                MainActivity.device_name = (nSize == 0 ? "" : MainActivity.readers.get(0).GetDescription().name);
                return true;
            } else
                return false;
        } else
            return true;
    }

    private void initRegisFragment(){
        Bundle bundle = new Bundle();
        bundle.putInt("lay", lay);
        RegisterFragment registerFragment = new RegisterFragment();
        registerFragment.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .replace(lay, registerFragment)
                .addToBackStack(null)
                .commit();
    }

    private void initValidationFragment(){
        Bundle bundle = new Bundle();
        bundle.putInt("lay", lay);
        ValidationFragment validationFragment = new ValidationFragment();
        validationFragment.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .replace(lay, validationFragment)
                .addToBackStack(null)
                .commit();
    }

    private void initFingersFragment(){
        Bundle bundle = new Bundle();
        bundle.putInt("lay", lay);
        FingersFragment fingersFragment = new FingersFragment();
        fingersFragment.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .replace(lay, fingersFragment)
                .addToBackStack(null)
                .commit();
    }

    private void closeApp(){
        new Handler().postDelayed(new Runnable(){
            public void run(){
                MainActivity.activity.onBackPressed();
            };
        }, DURACION_SPLASH);
    }

    private void initMainFragment(){
        UserObj userObj = new UserObj();
        userObj.id = 0;
        userObj.name = "jujuju";
        userObj.email = "jujuju";
        userObj.pass = "123456";
        userObj.fingerPath = "";
        userObj.picPath = "";

        Bundle bundle = new Bundle();
        bundle.putInt("lay", lay);
        bundle.putSerializable("user", userObj);
        MainFragment mainFragment = new MainFragment();
        mainFragment.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .replace(lay, mainFragment)
                .addToBackStack(null)
                .commit();
    }

}

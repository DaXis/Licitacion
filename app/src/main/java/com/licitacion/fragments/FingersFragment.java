package com.licitacion.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.licitacion.MainActivity;
import com.licitacion.R;
import com.licitacion.adapters.FingersAdapter;
import com.licitacion.objs.UserObj;
import com.licitacion.objs.ValidationObj;

import java.io.File;

public class FingersFragment extends Fragment implements View.OnClickListener {

    private int lay;
    private FingersAdapter adapter;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fingers_frag, container, false);

        ListView list = (ListView)rootView.findViewById(R.id.list);
        adapter = new FingersAdapter(MainActivity.activity, MainActivity.dbh.getUsers(MainActivity.db));
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserObj userObj = (UserObj)adapter.getItem(position);
                initFingerFragment(getFinger(userObj.fingerPath), userObj.name, userObj.picPath);
            }
        });

        return rootView;
    }

    private String getFinger(String p){
        String[] aux = p.split("[_]");
        String aux1 = aux[1];
        String path = MainActivity.cache.getAbsolutePath();
        File directory = new File(path);
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            if(files[i].getName().contains("fingerb_") && files[i].getName().contains(aux1)){
                return files[i].getAbsolutePath();
            }
        }
        return "";
    }

    private void initFingerFragment(String path, String name, String picPath){
        Bundle bundle = new Bundle();
        bundle.putInt("lay", lay);
        bundle.putString("picPath", picPath);
        bundle.putString("path", path);
        bundle.putString("name", name);
        FingerDetailFragment fingerDetailFragment = new FingerDetailFragment();
        fingerDetailFragment.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .replace(lay, fingerDetailFragment)
                .addToBackStack(null)
                .commit();
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
}

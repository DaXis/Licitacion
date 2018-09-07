package com.licitacion.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.licitacion.MainActivity;
import com.licitacion.fragments.MainFragment;
import com.licitacion.objs.UserObj;
import com.licitacion.utils.Encript;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DBHelper extends SQLiteOpenHelper {

    private Context ctx;

    final String sqlCreate0 = "CREATE TABLE Users (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT UNIQUE, pass TEXT," +
            "fingerPath TEXT, picPath TEXT)";

    final String[] create = {sqlCreate0};

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.ctx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for(int i = 0; i < create.length; i++){
            Log.d("sqlite", create[i]);
            db.execSQL(create[i]);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean newRegister(SQLiteDatabase db, UserObj userObj){
        ContentValues values = new ContentValues();
        values.put("name", userObj.name);
        values.put("email", userObj.email);
        values.put("pass", userObj.pass);
        values.put("fingerPath", userObj.fingerPath);
        values.put("picPath", userObj.picPath);
        db.insert("Users", null, values);
        return true;
    }

    public UserObj getUser(SQLiteDatabase db, String path){
        UserObj userObj = new UserObj();

        String[] campos = new String[] {"id", "name", "email", "pass", "fingerPath" , "picPath"};
        String[] args = new String[] {Encript.encrypt(path, MainActivity.KEY)};

        Cursor c = db.query("Users", campos, "fingerPath=?", args, null, null, null);

        if (c.moveToFirst()) {
            do {
                userObj.id = c.getInt(0);
                userObj.name = Encript.decrypt(c.getString(1), MainActivity.KEY);
                userObj.email = Encript.decrypt(c.getString(2), MainActivity.KEY);
                userObj.pass = Encript.decrypt(c.getString(3), MainActivity.KEY);
                userObj.fingerPath = Encript.decrypt(c.getString(4), MainActivity.KEY);
                userObj.picPath = Encript.decrypt(c.getString(5), MainActivity.KEY);
            } while(c.moveToNext());
        }

        return userObj;
    }

    public ArrayList<UserObj> getUsers(SQLiteDatabase db){
        ArrayList<UserObj> users = new ArrayList<>();

        String[] campos = new String[] {"id", "name", "email", "pass", "fingerPath" , "picPath"};
        Cursor c = db.query("Users", campos, null, null, null, null, null);

        if (c.moveToFirst()) {
            do {
                UserObj userObj = new UserObj();
                userObj.id = c.getInt(0);
                userObj.name = Encript.decrypt(c.getString(1), MainActivity.KEY);
                userObj.email = Encript.decrypt(c.getString(2), MainActivity.KEY);
                userObj.pass = Encript.decrypt(c.getString(3), MainActivity.KEY);
                userObj.fingerPath = Encript.decrypt(c.getString(4), MainActivity.KEY);
                userObj.picPath = Encript.decrypt(c.getString(5), MainActivity.KEY);
                users.add(userObj);
            } while(c.moveToNext());
        }

        Collections.sort(users, new Comparator<UserObj>() {
            public int compare(UserObj v1, UserObj v2) {
                return v1.name.compareTo(v2.name);
            }
        });

        return users;
    }

}

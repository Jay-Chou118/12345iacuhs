package com.example.testcdc;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class DatabaseHelper extends SQLiteOpenHelper {



    String TAG = "DatabaseHelper";


    Context mcontext;
    String dbName;
    String dbPath;


    public DatabaseHelper( Context context,  String name,  int version) {
        super(context, name, null, version);
        this.dbName = name;
        this.mcontext = context;

        this.dbPath = "data/data/" + "com.example.testcdc" + "/databases/";
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


    public void CheckDb(){
        SQLiteDatabase checkDb = null;
        String filePath = dbPath + dbName;

        try{

            checkDb = SQLiteDatabase.openDatabase(filePath,null,0);

        }catch (Exception e){
            e.printStackTrace();
        }

        if(checkDb != null){
            Log.e(TAG, "Database already exists " );

        }else {

            CopyDatabase();
        }

    }

    public void CopyDatabase(){
        this.getReadableDatabase();

        try{
            InputStream ios = mcontext.getAssets().open(dbName);
            OutputStream os = new FileOutputStream(dbPath + dbName);

            byte[] buffer = new byte[1024];

            int len;
            while((len = ios.read(buffer)) > 0){
                os.write(buffer,0,len);
            }
            os.flush();
            ios.close();
            os.close();
        }catch (Exception e ){

            e.printStackTrace();
        }
        Log.e(TAG, "Database Copied  " );
    }

    public void OpenDatabase(){

        String filePath = dbPath;
        SQLiteDatabase.openDatabase(filePath,null,0);

    }
}

package com.example.testcdc.Utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.testcdc.MyApplication;
import com.example.testcdc.R;
import com.example.testcdc.entity.CarTypeEntity;
import com.example.testcdc.entity.MsgInfoEntity;
import com.example.testcdc.entity.SignalInfo;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DataBaseUtil {

    private static final String TAG="MICAN_DataBaseUtil";

    public static boolean checkDataBase(Context context,String dbName) {
        SQLiteDatabase db = null;
        String databaseFilePath = context.getDatabasePath(dbName).getPath();
        try {
            db = SQLiteDatabase.openDatabase(databaseFilePath, null,SQLiteDatabase.OPEN_READONLY);
        }catch (Exception e)
        {
            Log.e(TAG,e.toString());
        }finally {
            if(db != null)
            {
                db.close();
            }
        }

        return db != null;
    }


    public static void copyDataBase(Context context, String dbName) {
        String databaseFilePath = context.getDatabasePath(dbName).getPath();
        FileOutputStream os = null;// 得到数据库文件的写入流
        InputStream is = null;
        byte[] buffer = new byte[8192];
        int count = 0;
        try {
            os = new FileOutputStream(databaseFilePath);
            is = context.getResources().openRawResource(R.raw.basic_database);// 得到数据库文件的数据流
            while ((count = is.read(buffer)) > 0) {
                os.write(buffer, 0, count);
                os.flush();
            }
            is.close();
            os.close();
            Log.i(TAG, "初始化数据库文件成功");
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }


    public static void initDataFromCsv(Context context) {
//        Log.e(TAG,"2222 initDataFromCsv");
        Log.e(TAG, "initDataFromCsv");
        ArrayList<Integer> files = new ArrayList<>();
//        files.add(R.raw.signal_info_1_mx11_e4);
//        files.add(R.raw.signal_info_2_mx11_e4);
//        files.add(R.raw.signal_info_3_mx11_e4);
//        files.add(R.raw.signal_info_4_mx11_e4);
//        files.add(R.raw.signal_info_6_mx11_e4);
//        files.add(R.raw.signal_info_7_mx11_e4);


        //ms11_e3
        files.add(R.raw.signal_info_1_ms11_e3);
        files.add(R.raw.signal_info_2_ms11_e3);
        files.add(R.raw.signal_info_3_ms11_e3);
        files.add(R.raw.signal_info_4_ms11_e3);
        files.add(R.raw.signal_info_5_ms11_e3);
        files.add(R.raw.signal_info_6_ms11_e3);
        files.add(R.raw.signal_info_7_ms11_e3);
        files.add(R.raw.signal_info_8_ms11_e3);
        files.add(R.raw.signal_info_9_ms11_e3);
        files.add(R.raw.signal_info_10_ms11_e3);
        files.add(R.raw.signal_info_14_ms11_e3);
        files.add(R.raw.signal_info_15_ms11_e3);

        //ms11_e4
        files.add(R.raw.signal_info_2_ms11_e4);
        files.add(R.raw.signal_info_3_ms11_e4);
        files.add(R.raw.signal_info_4_ms11_e4);
        files.add(R.raw.signal_info_5_ms11_e4);
        files.add(R.raw.signal_info_9_ms11_e4);
        files.add(R.raw.signal_info_10_ms11_e4);
        files.add(R.raw.signal_info_11_ms11_e4);
        files.add(R.raw.signal_info_12_ms11_e4);
        files.add(R.raw.signal_info_13_ms11_e4);
        files.add(R.raw.signal_info_14_ms11_e4);
        files.add(R.raw.signal_info_15_ms11_e4);

        //ms11_e4u1
        files.add(R.raw.signal_info_2_ms11_e4u1);
        files.add(R.raw.signal_info_3_ms11_e4u1);
        files.add(R.raw.signal_info_4_ms11_e4u1);
        files.add(R.raw.signal_info_5_ms11_e4u1);
        files.add(R.raw.signal_info_9_ms11_e4u1);
        files.add(R.raw.signal_info_10_ms11_e4u1);
        files.add(R.raw.signal_info_11_ms11_e4u1);
        files.add(R.raw.signal_info_12_ms11_e4u1);
        files.add(R.raw.signal_info_13_ms11_e4u1);
        files.add(R.raw.signal_info_14_ms11_e4u1);
        files.add(R.raw.signal_info_15_ms11_e4u1);
        files.add(R.raw.signal_info_33_ms11_e4u1);
        files.add(R.raw.signal_info_34_ms11_e4u1);
        files.add(R.raw.signal_info_35_ms11_e4u1);
        files.add(R.raw.signal_info_36_ms11_e4u1);

        //ms11_e4u2
        files.add(R.raw.signal_info_2_ms11_e4u2);
        files.add(R.raw.signal_info_3_ms11_e4u2);
        files.add(R.raw.signal_info_4_ms11_e4u2);
        files.add(R.raw.signal_info_5_ms11_e4u2);
        files.add(R.raw.signal_info_9_ms11_e4u2);
        files.add(R.raw.signal_info_10_ms11_e4u2);
        files.add(R.raw.signal_info_11_ms11_e4u2);
        files.add(R.raw.signal_info_12_ms11_e4u2);
        files.add(R.raw.signal_info_13_ms11_e4u2);
        files.add(R.raw.signal_info_14_ms11_e4u2);
        files.add(R.raw.signal_info_15_ms11_e4u2);
        files.add(R.raw.signal_info_33_ms11_e4u2);
        files.add(R.raw.signal_info_34_ms11_e4u2);
        files.add(R.raw.signal_info_35_ms11_e4u2);
        files.add(R.raw.signal_info_36_ms11_e4u2);
        files.add(R.raw.signal_info_37_ms11_e4u2);


        //ms11_e4u3_0812
        files.add(R.raw.signal_info_2_ms11_e4u3_0812);
        files.add(R.raw.signal_info_3_ms11_e4u3_0812);
        files.add(R.raw.signal_info_4_ms11_e4u3_0812);
        files.add(R.raw.signal_info_5_ms11_e4u3_0812);
        files.add(R.raw.signal_info_9_ms11_e4u3_0812);
        files.add(R.raw.signal_info_10_ms11_e4u3_0812);
        files.add(R.raw.signal_info_11_ms11_e4u3_0812);
        files.add(R.raw.signal_info_12_ms11_e4u3_0812);
        files.add(R.raw.signal_info_13_ms11_e4u3_0812);
        files.add(R.raw.signal_info_14_ms11_e4u3_0812);
        files.add(R.raw.signal_info_15_ms11_e4u3_0812);


        //ms11_e4u3_0915
        files.add(R.raw.signal_info_2_ms11_e4u3_0915);
        files.add(R.raw.signal_info_3_ms11_e4u3_0915);
        files.add(R.raw.signal_info_4_ms11_e4u3_0915);
        files.add(R.raw.signal_info_5_ms11_e4u3_0915);
        files.add(R.raw.signal_info_9_ms11_e4u3_0915);
        files.add(R.raw.signal_info_10_ms11_e4u3_0915);
        files.add(R.raw.signal_info_11_ms11_e4u3_0915);
        files.add(R.raw.signal_info_12_ms11_e4u3_0915);
        files.add(R.raw.signal_info_13_ms11_e4u3_0915);
        files.add(R.raw.signal_info_14_ms11_e4u3_0915);
        files.add(R.raw.signal_info_15_ms11_e4u3_0915);

        //ms11_e4u3_1015
        files.add(R.raw.signal_info_2_ms11_e4u3_1015);
        files.add(R.raw.signal_info_3_ms11_e4u3_1015);
        files.add(R.raw.signal_info_4_ms11_e4u3_1015);
        files.add(R.raw.signal_info_5_ms11_e4u3_1015);
        files.add(R.raw.signal_info_9_ms11_e4u3_1015);
        files.add(R.raw.signal_info_10_ms11_e4u3_1015);
        files.add(R.raw.signal_info_11_ms11_e4u3_1015);
        files.add(R.raw.signal_info_12_ms11_e4u3_1015);
        files.add(R.raw.signal_info_13_ms11_e4u3_1015);
        files.add(R.raw.signal_info_14_ms11_e4u3_1015);
        files.add(R.raw.signal_info_15_ms11_e4u3_1015);

        //ms11_e4u3_1115
        files.add(R.raw.signal_info_2_ms11_e4u3_1115);
        files.add(R.raw.signal_info_3_ms11_e4u3_1115);
        files.add(R.raw.signal_info_4_ms11_e4u3_1115);
        files.add(R.raw.signal_info_5_ms11_e4u3_1115);
        files.add(R.raw.signal_info_9_ms11_e4u3_1115);
        files.add(R.raw.signal_info_10_ms11_e4u3_1115);
        files.add(R.raw.signal_info_11_ms11_e4u3_1115);
        files.add(R.raw.signal_info_12_ms11_e4u3_1115);
        files.add(R.raw.signal_info_13_ms11_e4u3_1115);
        files.add(R.raw.signal_info_14_ms11_e4u3_1115);
        files.add(R.raw.signal_info_15_ms11_e4u3_1115);

        //ms11_rc04
        files.add(R.raw.signal_info_2_ms11_rc04);
        files.add(R.raw.signal_info_3_ms11_rc04);
        files.add(R.raw.signal_info_4_ms11_rc04);
        files.add(R.raw.signal_info_5_ms11_rc04);
        files.add(R.raw.signal_info_9_ms11_rc04);
        files.add(R.raw.signal_info_10_ms11_rc04);
        files.add(R.raw.signal_info_11_ms11_rc04);
        files.add(R.raw.signal_info_12_ms11_rc04);
        files.add(R.raw.signal_info_13_ms11_rc04);
        files.add(R.raw.signal_info_14_ms11_rc04);
        files.add(R.raw.signal_info_15_ms11_rc04);

        //ms11_rc05
        files.add(R.raw.signal_info_2_ms11_rc05);
        files.add(R.raw.signal_info_3_ms11_rc05);
        files.add(R.raw.signal_info_4_ms11_rc05);
        files.add(R.raw.signal_info_5_ms11_rc05);
        files.add(R.raw.signal_info_9_ms11_rc05);
        files.add(R.raw.signal_info_10_ms11_rc05);
        files.add(R.raw.signal_info_11_ms11_rc05);
        files.add(R.raw.signal_info_12_ms11_rc05);
        files.add(R.raw.signal_info_13_ms11_rc05);
        files.add(R.raw.signal_info_14_ms11_rc05);
        files.add(R.raw.signal_info_15_ms11_rc05);


        //ms11_rc06
        files.add(R.raw.signal_info_2_ms11_rc06);
        files.add(R.raw.signal_info_3_ms11_rc06);
        files.add(R.raw.signal_info_4_ms11_rc06);
        files.add(R.raw.signal_info_5_ms11_rc06);
        files.add(R.raw.signal_info_9_ms11_rc06);
        files.add(R.raw.signal_info_10_ms11_rc06);
        files.add(R.raw.signal_info_11_ms11_rc06);
        files.add(R.raw.signal_info_12_ms11_rc06);
        files.add(R.raw.signal_info_13_ms11_rc06);
        files.add(R.raw.signal_info_14_ms11_rc06);
        files.add(R.raw.signal_info_15_ms11_rc06);

        //ms11_rc07
        files.add(R.raw.signal_info_2_ms11_rc07);
        files.add(R.raw.signal_info_3_ms11_rc07);
        files.add(R.raw.signal_info_4_ms11_rc07);
        files.add(R.raw.signal_info_5_ms11_rc07);
        files.add(R.raw.signal_info_9_ms11_rc07);
        files.add(R.raw.signal_info_10_ms11_rc07);
        files.add(R.raw.signal_info_11_ms11_rc07);
        files.add(R.raw.signal_info_12_ms11_rc07);
        files.add(R.raw.signal_info_13_ms11_rc07);
        files.add(R.raw.signal_info_14_ms11_rc07);
        files.add(R.raw.signal_info_15_ms11_rc07);

        //ms11_rc08
        files.add(R.raw.signal_info_2_ms11_rc08);
        files.add(R.raw.signal_info_3_ms11_rc08);
        files.add(R.raw.signal_info_4_ms11_rc08);
        files.add(R.raw.signal_info_5_ms11_rc08);
        files.add(R.raw.signal_info_9_ms11_rc08);
        files.add(R.raw.signal_info_10_ms11_rc08);
        files.add(R.raw.signal_info_11_ms11_rc08);
        files.add(R.raw.signal_info_12_ms11_rc08);
        files.add(R.raw.signal_info_13_ms11_rc08);
        files.add(R.raw.signal_info_14_ms11_rc08);
        files.add(R.raw.signal_info_15_ms11_rc08);

        //ms11_rc09
        files.add(R.raw.signal_info_2_ms11_rc09);
        files.add(R.raw.signal_info_3_ms11_rc09);
        files.add(R.raw.signal_info_4_ms11_rc09);
        files.add(R.raw.signal_info_5_ms11_rc09);
        files.add(R.raw.signal_info_9_ms11_rc09);
        files.add(R.raw.signal_info_10_ms11_rc09);
        files.add(R.raw.signal_info_11_ms11_rc09);
        files.add(R.raw.signal_info_12_ms11_rc09);
        files.add(R.raw.signal_info_13_ms11_rc09);
        files.add(R.raw.signal_info_14_ms11_rc09);
        files.add(R.raw.signal_info_15_ms11_rc09);

        //ms11_rc10
        files.add(R.raw.signal_info_2_ms11_rc10);
        files.add(R.raw.signal_info_3_ms11_rc10);
        files.add(R.raw.signal_info_4_ms11_rc10);
        files.add(R.raw.signal_info_5_ms11_rc10);
        files.add(R.raw.signal_info_9_ms11_rc10);
        files.add(R.raw.signal_info_10_ms11_rc10);
        files.add(R.raw.signal_info_11_ms11_rc10);
        files.add(R.raw.signal_info_12_ms11_rc10);
        files.add(R.raw.signal_info_13_ms11_rc10);
        files.add(R.raw.signal_info_14_ms11_rc10);
        files.add(R.raw.signal_info_15_ms11_rc10);

        //ms11_rc11
        files.add(R.raw.signal_info_2_ms11_rc11);
        files.add(R.raw.signal_info_3_ms11_rc11);
        files.add(R.raw.signal_info_4_ms11_rc11);
        files.add(R.raw.signal_info_5_ms11_rc11);
        files.add(R.raw.signal_info_9_ms11_rc11);
        files.add(R.raw.signal_info_10_ms11_rc11);
        files.add(R.raw.signal_info_11_ms11_rc11);
        files.add(R.raw.signal_info_12_ms11_rc11);
        files.add(R.raw.signal_info_13_ms11_rc11);
        files.add(R.raw.signal_info_14_ms11_rc11);
        files.add(R.raw.signal_info_15_ms11_rc11);

        //ms11_rc12
        files.add(R.raw.signal_info_2_ms11_rc12);
        files.add(R.raw.signal_info_3_ms11_rc12);
        files.add(R.raw.signal_info_4_ms11_rc12);
        files.add(R.raw.signal_info_5_ms11_rc12);
        files.add(R.raw.signal_info_9_ms11_rc12);
        files.add(R.raw.signal_info_10_ms11_rc12);
        files.add(R.raw.signal_info_11_ms11_rc12);
        files.add(R.raw.signal_info_12_ms11_rc12);
        files.add(R.raw.signal_info_13_ms11_rc12);
        files.add(R.raw.signal_info_14_ms11_rc12);
        files.add(R.raw.signal_info_15_ms11_rc12);

        //ms11_25rc01
        files.add(R.raw.signal_info_2_ms11_25rc01);
        files.add(R.raw.signal_info_3_ms11_25rc01);
        files.add(R.raw.signal_info_4_ms11_25rc01);
        files.add(R.raw.signal_info_5_ms11_25rc01);
        files.add(R.raw.signal_info_9_ms11_25rc01);
        files.add(R.raw.signal_info_10_ms11_25rc01);
        files.add(R.raw.signal_info_11_ms11_25rc01);
        files.add(R.raw.signal_info_12_ms11_25rc01);
        files.add(R.raw.signal_info_13_ms11_25rc01);
        files.add(R.raw.signal_info_14_ms11_25rc01);
        files.add(R.raw.signal_info_15_ms11_25rc01);

        //ms11u_e3
        files.add(R.raw.signal_info_2_ms11u_e3);
        files.add(R.raw.signal_info_3_ms11u_e3);
        files.add(R.raw.signal_info_4_ms11u_e3);
        files.add(R.raw.signal_info_5_ms11u_e3);
        files.add(R.raw.signal_info_9_ms11u_e3);
        files.add(R.raw.signal_info_10_ms11u_e3);
        files.add(R.raw.signal_info_11_ms11u_e3);
        files.add(R.raw.signal_info_12_ms11u_e3);
        files.add(R.raw.signal_info_13_ms11u_e3);
        files.add(R.raw.signal_info_14_ms11u_e3);
        files.add(R.raw.signal_info_15_ms11u_e3);

        files.add(R.raw.signal_info_1_ms11_e3u1);
        files.add(R.raw.signal_info_2_ms11_e3u1);
        files.add(R.raw.signal_info_3_ms11_e3u1);
        files.add(R.raw.signal_info_4_ms11_e3u1);
        files.add(R.raw.signal_info_5_ms11_e3u1);
        files.add(R.raw.signal_info_6_ms11_e3u1);
        files.add(R.raw.signal_info_7_ms11_e3u1);
        files.add(R.raw.signal_info_8_ms11_e3u1);
        files.add(R.raw.signal_info_9_ms11_e3u1);
        files.add(R.raw.signal_info_10_ms11_e3u1);
        files.add(R.raw.signal_info_14_ms11_e3u1);
        files.add(R.raw.signal_info_15_ms11_e3u1);


        files.add(R.raw.signal_info_2_ms11u_e4bugfix);
        files.add(R.raw.signal_info_3_ms11u_e4bugfix);
        files.add(R.raw.signal_info_4_ms11u_e4bugfix);
        files.add(R.raw.signal_info_5_ms11u_e4bugfix);
        files.add(R.raw.signal_info_9_ms11u_e4bugfix);
        files.add(R.raw.signal_info_10_ms11u_e4bugfix);
        files.add(R.raw.signal_info_11_ms11u_e4bugfix);
        files.add(R.raw.signal_info_12_ms11u_e4bugfix);
        files.add(R.raw.signal_info_13_ms11u_e4bugfix);
        files.add(R.raw.signal_info_14_ms11u_e4bugfix);
        files.add(R.raw.signal_info_15_ms11u_e4bugfix);

        files.add(R.raw.signal_info_2_ms11u_e4u1);
        files.add(R.raw.signal_info_3_ms11u_e4u1);
        files.add(R.raw.signal_info_4_ms11u_e4u1);
        files.add(R.raw.signal_info_5_ms11u_e4u1);
        files.add(R.raw.signal_info_9_ms11u_e4u1);
        files.add(R.raw.signal_info_10_ms11u_e4u1);
        files.add(R.raw.signal_info_11_ms11u_e4u1);
        files.add(R.raw.signal_info_12_ms11u_e4u1);
        files.add(R.raw.signal_info_13_ms11u_e4u1);
        files.add(R.raw.signal_info_14_ms11u_e4u1);
        files.add(R.raw.signal_info_15_ms11u_e4u1);

        files.add(R.raw.signal_info_2_ms11u_e4u2);
        files.add(R.raw.signal_info_3_ms11u_e4u2);
        files.add(R.raw.signal_info_4_ms11u_e4u2);
        files.add(R.raw.signal_info_5_ms11u_e4u2);
        files.add(R.raw.signal_info_9_ms11u_e4u2);
        files.add(R.raw.signal_info_10_ms11u_e4u2);
        files.add(R.raw.signal_info_11_ms11u_e4u2);
        files.add(R.raw.signal_info_12_ms11u_e4u2);
        files.add(R.raw.signal_info_13_ms11u_e4u2);
        files.add(R.raw.signal_info_14_ms11u_e4u2);
        files.add(R.raw.signal_info_15_ms11u_e4u2);

        files.add(R.raw.signal_info_2_ms11u_e4u3);
        files.add(R.raw.signal_info_3_ms11u_e4u3);
        files.add(R.raw.signal_info_4_ms11u_e4u3);
        files.add(R.raw.signal_info_5_ms11u_e4u3);
        files.add(R.raw.signal_info_9_ms11u_e4u3);
        files.add(R.raw.signal_info_10_ms11u_e4u3);
        files.add(R.raw.signal_info_11_ms11u_e4u3);
        files.add(R.raw.signal_info_12_ms11u_e4u3);
        files.add(R.raw.signal_info_13_ms11u_e4u3);
        files.add(R.raw.signal_info_14_ms11u_e4u3);
        files.add(R.raw.signal_info_15_ms11u_e4u3);

        files.add(R.raw.signal_info_2_ms11u_sm3);
        files.add(R.raw.signal_info_3_ms11u_sm3);
        files.add(R.raw.signal_info_4_ms11u_sm3);
        files.add(R.raw.signal_info_5_ms11u_sm3);
        files.add(R.raw.signal_info_9_ms11u_sm3);
        files.add(R.raw.signal_info_10_ms11u_sm3);
        files.add(R.raw.signal_info_11_ms11u_sm3);
        files.add(R.raw.signal_info_12_ms11u_sm3);
        files.add(R.raw.signal_info_13_ms11u_sm3);
        files.add(R.raw.signal_info_14_ms11u_sm3);
        files.add(R.raw.signal_info_15_ms11u_sm3);

        files.add(R.raw.signal_info_2_ms11u_25rc02);
        files.add(R.raw.signal_info_3_ms11u_25rc02);
        files.add(R.raw.signal_info_4_ms11u_25rc02);
        files.add(R.raw.signal_info_5_ms11u_25rc02);
        files.add(R.raw.signal_info_9_ms11u_25rc02);
        files.add(R.raw.signal_info_10_ms11u_25rc02);
        files.add(R.raw.signal_info_11_ms11u_25rc02);
        files.add(R.raw.signal_info_12_ms11u_25rc02);
        files.add(R.raw.signal_info_13_ms11u_25rc02);
        files.add(R.raw.signal_info_14_ms11u_25rc02);
        files.add(R.raw.signal_info_15_ms11u_25rc02);

        files.add(R.raw.signal_info_1_mx11_eea15_e3);
        files.add(R.raw.signal_info_2_mx11_eea15_e3);
        files.add(R.raw.signal_info_3_mx11_eea15_e3);
        files.add(R.raw.signal_info_4_mx11_eea15_e3);
        files.add(R.raw.signal_info_6_mx11_eea15_e3);
        files.add(R.raw.signal_info_7_mx11_eea15_e3);
        files.add(R.raw.signal_info_13_mx11_eea15_e3);

        files.add(R.raw.signal_info_1_mx11_eea15_e4);
        files.add(R.raw.signal_info_2_mx11_eea15_e4);
        files.add(R.raw.signal_info_3_mx11_eea15_e4);
        files.add(R.raw.signal_info_4_mx11_eea15_e4);
        files.add(R.raw.signal_info_6_mx11_eea15_e4);
        files.add(R.raw.signal_info_7_mx11_eea15_e4);
        files.add(R.raw.signal_info_13_mx11_eea15_e4);

        files.add(R.raw.signal_info_1_mx11_e4u2);
        files.add(R.raw.signal_info_2_mx11_e4u2);
        files.add(R.raw.signal_info_3_mx11_e4u2);
        files.add(R.raw.signal_info_4_mx11_e4u2);
        files.add(R.raw.signal_info_6_mx11_e4u2);
        files.add(R.raw.signal_info_7_mx11_e4u2);
        files.add(R.raw.signal_info_13_mx11_e4u2);

        files.add(R.raw.signal_info_1_mx11_e4u3);
        files.add(R.raw.signal_info_2_mx11_e4u3);
        files.add(R.raw.signal_info_3_mx11_e4u3);
        files.add(R.raw.signal_info_4_mx11_e4u3);
        files.add(R.raw.signal_info_6_mx11_e4u3);
        files.add(R.raw.signal_info_7_mx11_e4u3);
        files.add(R.raw.signal_info_13_mx11_e4u3);

        files.add(R.raw.signal_info_1_mx11_e4u3);
        files.add(R.raw.signal_info_1_mx11_e4u31);
        files.add(R.raw.signal_info_2_mx11_e4u31);
        files.add(R.raw.signal_info_3_mx11_e4u31);
        files.add(R.raw.signal_info_4_mx11_e4u31);
        files.add(R.raw.signal_info_6_mx11_e4u31);
        files.add(R.raw.signal_info_7_mx11_e4u31);
        files.add(R.raw.signal_info_13_mx11_e4u31);

        files.add(R.raw.signal_info_1_mx11_e3);
        files.add(R.raw.signal_info_2_mx11_e3);
        files.add(R.raw.signal_info_3_mx11_e3);
        files.add(R.raw.signal_info_4_mx11_e3);
        files.add(R.raw.signal_info_6_mx11_e3);
        files.add(R.raw.signal_info_7_mx11_e3);
        files.add(R.raw.signal_info_13_mx11_e3);


        files.add(R.raw.signal_info_1_mx11_e4u1);
        files.add(R.raw.signal_info_2_mx11_e4u1);
        files.add(R.raw.signal_info_3_mx11_e4u1);
        files.add(R.raw.signal_info_4_mx11_e4u1);
        files.add(R.raw.signal_info_6_mx11_e4u1);
        files.add(R.raw.signal_info_7_mx11_e4u1);
        files.add(R.raw.signal_info_13_mx11_e4u1);


        files.add(R.raw.signal_info_1_kunlun20_e2);
        files.add(R.raw.signal_info_2_kunlun20_e2);
        files.add(R.raw.signal_info_3_kunlun20_e2);
        files.add(R.raw.signal_info_4_kunlun20_e2);
        files.add(R.raw.signal_info_6_kunlun20_e2);
        files.add(R.raw.signal_info_7_kunlun20_e2);
        files.add(R.raw.signal_info_13_kunlun20_e2);

        files.add(R.raw.signal_info_1_kunlun20_e3);
        files.add(R.raw.signal_info_2_kunlun20_e3);
        files.add(R.raw.signal_info_3_kunlun20_e3);
        files.add(R.raw.signal_info_4_kunlun20_e3);
        files.add(R.raw.signal_info_6_kunlun20_e3);
        files.add(R.raw.signal_info_7_kunlun20_e3);
        files.add(R.raw.signal_info_13_kunlun20_e3);

        files.add(R.raw.signal_info_1_kunlun20_e3u1);
        files.add(R.raw.signal_info_2_kunlun20_e3u1);
        files.add(R.raw.signal_info_3_kunlun20_e3u1);
        files.add(R.raw.signal_info_4_kunlun20_e3u1);
        files.add(R.raw.signal_info_6_kunlun20_e3u1);
        files.add(R.raw.signal_info_7_kunlun20_e3u1);
        files.add(R.raw.signal_info_13_kunlun20_e3u1);

        files.add(R.raw.msg_info_1_kunlun20_e3u2);
        files.add(R.raw.msg_info_2_kunlun20_e3u2);
        files.add(R.raw.msg_info_3_kunlun20_e3u2);
        files.add(R.raw.msg_info_4_kunlun20_e3u2);
        files.add(R.raw.msg_info_6_kunlun20_e3u2);
        files.add(R.raw.msg_info_7_kunlun20_e3u2);
        files.add(R.raw.msg_info_13_kunlun20_e3u2);

        files.add(R.raw.signal_info_1_kunlun20_e3u2);
        files.add(R.raw.signal_info_2_kunlun20_e3u2);
        files.add(R.raw.signal_info_3_kunlun20_e3u2);
        files.add(R.raw.signal_info_4_kunlun20_e3u2);
        files.add(R.raw.signal_info_6_kunlun20_e3u2);
        files.add(R.raw.signal_info_7_kunlun20_e3u2);
        files.add(R.raw.signal_info_13_kunlun20_e3u2);

        files.add(R.raw.signal_info_1_kunlun20_e4);
        files.add(R.raw.signal_info_2_kunlun20_e4);
        files.add(R.raw.signal_info_3_kunlun20_e4);
        files.add(R.raw.signal_info_4_kunlun20_e4);
        files.add(R.raw.signal_info_6_kunlun20_e4);
        files.add(R.raw.signal_info_7_kunlun20_e4);
        files.add(R.raw.signal_info_13_kunlun20_e4);

//        Log.d(TAG, "files: " + files);
//        SignalInfoDao signalInfoDao = MyApplication.getInstance().getMx11E4Database().signalInfoDao();
//        List<SignalInfo> signalInfos = new ArrayList<>();

        files.forEach(file -> {
            try {
                InputStream is = context.getResources().openRawResource(file);// 得到数据库文件的数据流
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
//                    Log.e(TAG,line);
                    String[] data = line.split("#");   // 使用井号“#”作为分隔符
//                    Log.e(TAG, Arrays.toString(data));
                    SignalInfo signalInfo = new SignalInfo();
                    signalInfo.setName(data[0]);
                    signalInfo.setBUSId(Integer.parseInt(data[1]));
                    signalInfo.setCANId(Integer.parseInt(data[2]));
                    signalInfo.setByteOrder(Boolean.parseBoolean(data[3]));
                    signalInfo.setSigned(Boolean.parseBoolean(data[4]));
                    signalInfo.setBitStart(Integer.parseInt(data[5]));
                    signalInfo.setBitLength(Integer.parseInt(data[6]));
                    signalInfo.setScale(Double.parseDouble(data[7]));
                    signalInfo.setOffset(Double.parseDouble(data[8]));
                    signalInfo.setComment(data[9]);
                    signalInfo.setMinimum(Double.parseDouble(data[10]));
                    signalInfo.setMaximum(Double.parseDouble(data[11]));
                    signalInfo.setInitial(Double.parseDouble(data[12]));
                    signalInfo.choices = data[13].equals("null") ? null : data[13];

                    CarTypeEntity carTypeEntity = MyApplication.getInstance().getDatabase().carTypeDao().getByName(data[14], data[15]);
                    signalInfo.cid = carTypeEntity.id;

//                    Log.d(TAG, "setCarTypeId: " +data[14] + " setSdbId: " + data[15] + " CCCCCCCC "+ signalInfo.cid);
                    MyApplication.getInstance().getDatabase().signalInfoDao().insert(signalInfo);
//                    signalInfos.add(signalInfo);
//                    Log.e(TAG,"插入signal成功");
                }
                is.close();
                reader.close();
//                Log.i(TAG,"读取csv文件成功 " + file);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
//            signalInfoDao.insertAll(signalInfos);
            Log.e(TAG, "signal insert finished");
//            Log.e(TAG, "222222 signal insert finished");
//
        });

        Log.e(TAG, "All signal insert finished");

    }

    public static void initMsgFromCsv(Context ctx) {

//        Log.e(TAG, "2222 initMsgFromCsv " );
        Log.e(TAG, "initMsgFromCsv ");

        ArrayList<Integer> files = new ArrayList<>();

        files.add(R.raw.msg_info_1);
        files.add(R.raw.msg_info_2);
        files.add(R.raw.msg_info_3);
        files.add(R.raw.msg_info_4);
        files.add(R.raw.msg_info_6);
        files.add(R.raw.msg_info_7);

        //ms11_e3
        files.add(R.raw.msg_info_1_ms11_e3);
        files.add(R.raw.msg_info_2_ms11_e3);
        files.add(R.raw.msg_info_3_ms11_e3);
        files.add(R.raw.msg_info_4_ms11_e3);
        files.add(R.raw.msg_info_5_ms11_e3);
        files.add(R.raw.msg_info_6_ms11_e3);
        files.add(R.raw.msg_info_7_ms11_e3);
        files.add(R.raw.msg_info_8_ms11_e3);
        files.add(R.raw.msg_info_9_ms11_e3);
        files.add(R.raw.msg_info_10_ms11_e3);
        files.add(R.raw.msg_info_14_ms11_e3);
        files.add(R.raw.msg_info_15_ms11_e3);

        files.add(R.raw.msg_info_1_ms11_e3u1);
        files.add(R.raw.msg_info_2_ms11_e3u1);
        files.add(R.raw.msg_info_3_ms11_e3u1);
        files.add(R.raw.msg_info_4_ms11_e3u1);
        files.add(R.raw.msg_info_5_ms11_e3u1);
        files.add(R.raw.msg_info_6_ms11_e3u1);
        files.add(R.raw.msg_info_7_ms11_e3u1);
        files.add(R.raw.msg_info_8_ms11_e3u1);
        files.add(R.raw.msg_info_9_ms11_e3u1);
        files.add(R.raw.msg_info_10_ms11_e3u1);
        files.add(R.raw.msg_info_14_ms11_e3u1);
        files.add(R.raw.msg_info_15_ms11_e3u1);


        //ms11_e4
        files.add(R.raw.msg_info_2_ms11_e4);
        files.add(R.raw.msg_info_3_ms11_e4);
        files.add(R.raw.msg_info_4_ms11_e4);
        files.add(R.raw.msg_info_5_ms11_e4);
        files.add(R.raw.msg_info_9_ms11_e4);
        files.add(R.raw.msg_info_10_ms11_e4);
        files.add(R.raw.msg_info_11_ms11_e4);
        files.add(R.raw.msg_info_12_ms11_e4);
        files.add(R.raw.msg_info_13_ms11_e4);
        files.add(R.raw.msg_info_14_ms11_e4);
        files.add(R.raw.msg_info_15_ms11_e4);

        //ms11_e4u1
        files.add(R.raw.msg_info_2_ms11_e4u1);
        files.add(R.raw.msg_info_3_ms11_e4u1);
        files.add(R.raw.msg_info_4_ms11_e4u1);
        files.add(R.raw.msg_info_5_ms11_e4u1);
        files.add(R.raw.msg_info_9_ms11_e4u1);
        files.add(R.raw.msg_info_10_ms11_e4u1);
        files.add(R.raw.msg_info_11_ms11_e4u1);
        files.add(R.raw.msg_info_12_ms11_e4u1);
        files.add(R.raw.msg_info_13_ms11_e4u1);
        files.add(R.raw.msg_info_14_ms11_e4u1);
        files.add(R.raw.msg_info_15_ms11_e4u1);
        files.add(R.raw.msg_info_33_ms11_e4u1);
        files.add(R.raw.msg_info_34_ms11_e4u1);
        files.add(R.raw.msg_info_35_ms11_e4u1);
        files.add(R.raw.msg_info_36_ms11_e4u1);

        //ms11_e4u2
        files.add(R.raw.msg_info_2_ms11_e4u2);
        files.add(R.raw.msg_info_3_ms11_e4u2);
        files.add(R.raw.msg_info_4_ms11_e4u2);
        files.add(R.raw.msg_info_5_ms11_e4u2);
        files.add(R.raw.msg_info_9_ms11_e4u2);
        files.add(R.raw.msg_info_10_ms11_e4u2);
        files.add(R.raw.msg_info_11_ms11_e4u2);
        files.add(R.raw.msg_info_12_ms11_e4u2);
        files.add(R.raw.msg_info_13_ms11_e4u2);
        files.add(R.raw.msg_info_14_ms11_e4u2);
        files.add(R.raw.msg_info_15_ms11_e4u2);
        files.add(R.raw.msg_info_33_ms11_e4u2);
        files.add(R.raw.msg_info_34_ms11_e4u2);
        files.add(R.raw.msg_info_35_ms11_e4u2);
        files.add(R.raw.msg_info_36_ms11_e4u2);
        files.add(R.raw.msg_info_37_ms11_e4u2);

        //ms11_e4u3_0812
        files.add(R.raw.msg_info_2_ms11_e4u3_0812);
        files.add(R.raw.msg_info_3_ms11_e4u3_0812);
        files.add(R.raw.msg_info_4_ms11_e4u3_0812);
        files.add(R.raw.msg_info_5_ms11_e4u3_0812);
        files.add(R.raw.msg_info_9_ms11_e4u3_0812);
        files.add(R.raw.msg_info_10_ms11_e4u3_0812);
        files.add(R.raw.msg_info_11_ms11_e4u3_0812);
        files.add(R.raw.msg_info_12_ms11_e4u3_0812);
        files.add(R.raw.msg_info_13_ms11_e4u3_0812);
        files.add(R.raw.msg_info_14_ms11_e4u3_0812);
        files.add(R.raw.msg_info_15_ms11_e4u3_0812);

        //ms11_e4u3_0915
        files.add(R.raw.msg_info_2_ms11_e4u3_0915);
        files.add(R.raw.msg_info_3_ms11_e4u3_0915);
        files.add(R.raw.msg_info_4_ms11_e4u3_0915);
        files.add(R.raw.msg_info_5_ms11_e4u3_0915);
        files.add(R.raw.msg_info_9_ms11_e4u3_0915);
        files.add(R.raw.msg_info_10_ms11_e4u3_0915);
        files.add(R.raw.msg_info_11_ms11_e4u3_0915);
        files.add(R.raw.msg_info_12_ms11_e4u3_0915);
        files.add(R.raw.msg_info_13_ms11_e4u3_0915);
        files.add(R.raw.msg_info_14_ms11_e4u3_0915);
        files.add(R.raw.msg_info_15_ms11_e4u3_0915);

        //ms11_e4u3_1015
        files.add(R.raw.msg_info_2_ms11_e4u3_1015);
        files.add(R.raw.msg_info_3_ms11_e4u3_1015);
        files.add(R.raw.msg_info_4_ms11_e4u3_1015);
        files.add(R.raw.msg_info_5_ms11_e4u3_1015);
        files.add(R.raw.msg_info_9_ms11_e4u3_1015);
        files.add(R.raw.msg_info_10_ms11_e4u3_1015);
        files.add(R.raw.msg_info_11_ms11_e4u3_1015);
        files.add(R.raw.msg_info_12_ms11_e4u3_1015);
        files.add(R.raw.msg_info_13_ms11_e4u3_1015);
        files.add(R.raw.msg_info_14_ms11_e4u3_1015);
        files.add(R.raw.msg_info_15_ms11_e4u3_1015);

        //ms11_e4u3_1115
        files.add(R.raw.msg_info_2_ms11_e4u3_1115);
        files.add(R.raw.msg_info_3_ms11_e4u3_1115);
        files.add(R.raw.msg_info_4_ms11_e4u3_1115);
        files.add(R.raw.msg_info_5_ms11_e4u3_1115);
        files.add(R.raw.msg_info_9_ms11_e4u3_1115);
        files.add(R.raw.msg_info_10_ms11_e4u3_1115);
        files.add(R.raw.msg_info_11_ms11_e4u3_1115);
        files.add(R.raw.msg_info_12_ms11_e4u3_1115);
        files.add(R.raw.msg_info_13_ms11_e4u3_1115);
        files.add(R.raw.msg_info_14_ms11_e4u3_1115);
        files.add(R.raw.msg_info_15_ms11_e4u3_1115);

        files.add(R.raw.msg_info_2_ms11_rc04);
        files.add(R.raw.msg_info_3_ms11_rc04);
        files.add(R.raw.msg_info_4_ms11_rc04);
        files.add(R.raw.msg_info_5_ms11_rc04);
        files.add(R.raw.msg_info_9_ms11_rc04);
        files.add(R.raw.msg_info_10_ms11_rc04);
        files.add(R.raw.msg_info_11_ms11_rc04);
        files.add(R.raw.msg_info_12_ms11_rc04);
        files.add(R.raw.msg_info_13_ms11_rc04);
        files.add(R.raw.msg_info_14_ms11_rc04);
        files.add(R.raw.msg_info_15_ms11_rc04);

        files.add(R.raw.msg_info_2_ms11_rc05);
        files.add(R.raw.msg_info_3_ms11_rc05);
        files.add(R.raw.msg_info_4_ms11_rc05);
        files.add(R.raw.msg_info_5_ms11_rc05);
        files.add(R.raw.msg_info_9_ms11_rc05);
        files.add(R.raw.msg_info_10_ms11_rc05);
        files.add(R.raw.msg_info_11_ms11_rc05);
        files.add(R.raw.msg_info_12_ms11_rc05);
        files.add(R.raw.msg_info_13_ms11_rc05);
        files.add(R.raw.msg_info_14_ms11_rc05);
        files.add(R.raw.msg_info_15_ms11_rc05);

        //ms11_rc06
        files.add(R.raw.msg_info_2_ms11_rc06);
        files.add(R.raw.msg_info_3_ms11_rc06);
        files.add(R.raw.msg_info_4_ms11_rc06);
        files.add(R.raw.msg_info_5_ms11_rc06);
        files.add(R.raw.msg_info_9_ms11_rc06);
        files.add(R.raw.msg_info_10_ms11_rc06);
        files.add(R.raw.msg_info_11_ms11_rc06);
        files.add(R.raw.msg_info_12_ms11_rc06);
        files.add(R.raw.msg_info_13_ms11_rc06);
        files.add(R.raw.msg_info_14_ms11_rc06);
        files.add(R.raw.msg_info_15_ms11_rc06);

        //ms11_rc07
        files.add(R.raw.msg_info_2_ms11_rc07);
        files.add(R.raw.msg_info_3_ms11_rc07);
        files.add(R.raw.msg_info_4_ms11_rc07);
        files.add(R.raw.msg_info_5_ms11_rc07);
        files.add(R.raw.msg_info_9_ms11_rc07);
        files.add(R.raw.msg_info_10_ms11_rc07);
        files.add(R.raw.msg_info_11_ms11_rc07);
        files.add(R.raw.msg_info_12_ms11_rc07);
        files.add(R.raw.msg_info_13_ms11_rc07);
        files.add(R.raw.msg_info_14_ms11_rc07);
        files.add(R.raw.msg_info_15_ms11_rc07);

        files.add(R.raw.msg_info_2_ms11_rc08);
        files.add(R.raw.msg_info_3_ms11_rc08);
        files.add(R.raw.msg_info_4_ms11_rc08);
        files.add(R.raw.msg_info_5_ms11_rc08);
        files.add(R.raw.msg_info_9_ms11_rc08);
        files.add(R.raw.msg_info_10_ms11_rc08);
        files.add(R.raw.msg_info_11_ms11_rc08);
        files.add(R.raw.msg_info_12_ms11_rc08);
        files.add(R.raw.msg_info_13_ms11_rc08);
        files.add(R.raw.msg_info_14_ms11_rc08);
        files.add(R.raw.msg_info_15_ms11_rc08);

        files.add(R.raw.msg_info_2_ms11_rc09);
        files.add(R.raw.msg_info_3_ms11_rc09);
        files.add(R.raw.msg_info_4_ms11_rc09);
        files.add(R.raw.msg_info_5_ms11_rc09);
        files.add(R.raw.msg_info_9_ms11_rc09);
        files.add(R.raw.msg_info_10_ms11_rc09);
        files.add(R.raw.msg_info_11_ms11_rc09);
        files.add(R.raw.msg_info_12_ms11_rc09);
        files.add(R.raw.msg_info_13_ms11_rc09);
        files.add(R.raw.msg_info_14_ms11_rc09);
        files.add(R.raw.msg_info_15_ms11_rc09);

        files.add(R.raw.msg_info_2_ms11_rc10);
        files.add(R.raw.msg_info_3_ms11_rc10);
        files.add(R.raw.msg_info_4_ms11_rc10);
        files.add(R.raw.msg_info_5_ms11_rc10);
        files.add(R.raw.msg_info_9_ms11_rc10);
        files.add(R.raw.msg_info_10_ms11_rc10);
        files.add(R.raw.msg_info_11_ms11_rc10);
        files.add(R.raw.msg_info_12_ms11_rc10);
        files.add(R.raw.msg_info_13_ms11_rc10);
        files.add(R.raw.msg_info_14_ms11_rc10);
        files.add(R.raw.msg_info_15_ms11_rc10);

        files.add(R.raw.msg_info_2_ms11_rc11);
        files.add(R.raw.msg_info_3_ms11_rc11);
        files.add(R.raw.msg_info_4_ms11_rc11);
        files.add(R.raw.msg_info_5_ms11_rc11);
        files.add(R.raw.msg_info_9_ms11_rc11);
        files.add(R.raw.msg_info_10_ms11_rc11);
        files.add(R.raw.msg_info_11_ms11_rc11);
        files.add(R.raw.msg_info_12_ms11_rc11);
        files.add(R.raw.msg_info_13_ms11_rc11);
        files.add(R.raw.msg_info_14_ms11_rc11);
        files.add(R.raw.msg_info_15_ms11_rc11);

        files.add(R.raw.msg_info_2_ms11_rc12);
        files.add(R.raw.msg_info_3_ms11_rc12);
        files.add(R.raw.msg_info_4_ms11_rc12);
        files.add(R.raw.msg_info_5_ms11_rc12);
        files.add(R.raw.msg_info_9_ms11_rc12);
        files.add(R.raw.msg_info_10_ms11_rc12);
        files.add(R.raw.msg_info_11_ms11_rc12);
        files.add(R.raw.msg_info_12_ms11_rc12);
        files.add(R.raw.msg_info_13_ms11_rc12);
        files.add(R.raw.msg_info_14_ms11_rc12);
        files.add(R.raw.msg_info_15_ms11_rc12);

        files.add(R.raw.msg_info_2_ms11_25rc01);
        files.add(R.raw.msg_info_3_ms11_25rc01);
        files.add(R.raw.msg_info_4_ms11_25rc01);
        files.add(R.raw.msg_info_5_ms11_25rc01);
        files.add(R.raw.msg_info_9_ms11_25rc01);
        files.add(R.raw.msg_info_10_ms11_25rc01);
        files.add(R.raw.msg_info_11_ms11_25rc01);
        files.add(R.raw.msg_info_12_ms11_25rc01);
        files.add(R.raw.msg_info_13_ms11_25rc01);
        files.add(R.raw.msg_info_14_ms11_25rc01);
        files.add(R.raw.msg_info_15_ms11_25rc01);


        files.add(R.raw.msg_info_2_ms11u_e4bugfix);
        files.add(R.raw.msg_info_3_ms11u_e4bugfix);
        files.add(R.raw.msg_info_4_ms11u_e4bugfix);
        files.add(R.raw.msg_info_5_ms11u_e4bugfix);
        files.add(R.raw.msg_info_9_ms11u_e4bugfix);
        files.add(R.raw.msg_info_10_ms11u_e4bugfix);
        files.add(R.raw.msg_info_11_ms11u_e4bugfix);
        files.add(R.raw.msg_info_12_ms11u_e4bugfix);
        files.add(R.raw.msg_info_13_ms11u_e4bugfix);
        files.add(R.raw.msg_info_14_ms11u_e4bugfix);
        files.add(R.raw.msg_info_15_ms11u_e4bugfix);

        files.add(R.raw.msg_info_2_ms11u_e4u1);
        files.add(R.raw.msg_info_3_ms11u_e4u1);
        files.add(R.raw.msg_info_4_ms11u_e4u1);
        files.add(R.raw.msg_info_5_ms11u_e4u1);
        files.add(R.raw.msg_info_9_ms11u_e4u1);
        files.add(R.raw.msg_info_10_ms11u_e4u1);
        files.add(R.raw.msg_info_11_ms11u_e4u1);
        files.add(R.raw.msg_info_12_ms11u_e4u1);
        files.add(R.raw.msg_info_13_ms11u_e4u1);
        files.add(R.raw.msg_info_14_ms11u_e4u1);
        files.add(R.raw.msg_info_15_ms11u_e4u1);

        files.add(R.raw.msg_info_2_ms11u_e4u2);
        files.add(R.raw.msg_info_3_ms11u_e4u2);
        files.add(R.raw.msg_info_4_ms11u_e4u2);
        files.add(R.raw.msg_info_5_ms11u_e4u2);
        files.add(R.raw.msg_info_9_ms11u_e4u2);
        files.add(R.raw.msg_info_10_ms11u_e4u2);
        files.add(R.raw.msg_info_11_ms11u_e4u2);
        files.add(R.raw.msg_info_12_ms11u_e4u2);
        files.add(R.raw.msg_info_13_ms11u_e4u2);
        files.add(R.raw.msg_info_14_ms11u_e4u2);
        files.add(R.raw.msg_info_15_ms11u_e4u2);

        files.add(R.raw.msg_info_2_ms11u_e4u3);
        files.add(R.raw.msg_info_3_ms11u_e4u3);
        files.add(R.raw.msg_info_4_ms11u_e4u3);
        files.add(R.raw.msg_info_5_ms11u_e4u3);
        files.add(R.raw.msg_info_9_ms11u_e4u3);
        files.add(R.raw.msg_info_10_ms11u_e4u3);
        files.add(R.raw.msg_info_11_ms11u_e4u3);
        files.add(R.raw.msg_info_12_ms11u_e4u3);
        files.add(R.raw.msg_info_13_ms11u_e4u3);
        files.add(R.raw.msg_info_14_ms11u_e4u3);
        files.add(R.raw.msg_info_15_ms11u_e4u3);

        files.add(R.raw.msg_info_2_ms11u_e3);
        files.add(R.raw.msg_info_3_ms11u_e3);
        files.add(R.raw.msg_info_4_ms11u_e3);
        files.add(R.raw.msg_info_5_ms11u_e3);
        files.add(R.raw.msg_info_9_ms11u_e3);
        files.add(R.raw.msg_info_10_ms11u_e3);
        files.add(R.raw.msg_info_11_ms11u_e3);
        files.add(R.raw.msg_info_12_ms11u_e3);
        files.add(R.raw.msg_info_13_ms11u_e3);
        files.add(R.raw.msg_info_14_ms11u_e3);
        files.add(R.raw.msg_info_15_ms11u_e3);

        files.add(R.raw.msg_info_2_ms11u_sm3);
        files.add(R.raw.msg_info_3_ms11u_sm3);
        files.add(R.raw.msg_info_4_ms11u_sm3);
        files.add(R.raw.msg_info_5_ms11u_sm3);
        files.add(R.raw.msg_info_9_ms11u_sm3);
        files.add(R.raw.msg_info_10_ms11u_sm3);
        files.add(R.raw.msg_info_11_ms11u_sm3);
        files.add(R.raw.msg_info_12_ms11u_sm3);
        files.add(R.raw.msg_info_13_ms11u_sm3);
        files.add(R.raw.msg_info_14_ms11u_sm3);
        files.add(R.raw.msg_info_15_ms11u_sm3);

        files.add(R.raw.msg_info_2_ms11u_25rc02);
        files.add(R.raw.msg_info_3_ms11u_25rc02);
        files.add(R.raw.msg_info_4_ms11u_25rc02);
        files.add(R.raw.msg_info_5_ms11u_25rc02);
        files.add(R.raw.msg_info_9_ms11u_25rc02);
        files.add(R.raw.msg_info_10_ms11u_25rc02);
        files.add(R.raw.msg_info_11_ms11u_25rc02);
        files.add(R.raw.msg_info_12_ms11u_25rc02);
        files.add(R.raw.msg_info_13_ms11u_25rc02);
        files.add(R.raw.msg_info_14_ms11u_25rc02);
        files.add(R.raw.msg_info_15_ms11u_25rc02);

        files.add(R.raw.msg_info_1_mx11_eea15_e3);
        files.add(R.raw.msg_info_2_mx11_eea15_e3);
        files.add(R.raw.msg_info_3_mx11_eea15_e3);
        files.add(R.raw.msg_info_4_mx11_eea15_e3);
        files.add(R.raw.msg_info_6_mx11_eea15_e3);
        files.add(R.raw.msg_info_7_mx11_eea15_e3);
        files.add(R.raw.msg_info_13_mx11_eea15_e3);

        files.add(R.raw.msg_info_1_mx11_eea15_e4);
        files.add(R.raw.msg_info_2_mx11_eea15_e4);
        files.add(R.raw.msg_info_3_mx11_eea15_e4);
        files.add(R.raw.msg_info_4_mx11_eea15_e4);
        files.add(R.raw.msg_info_6_mx11_eea15_e4);
        files.add(R.raw.msg_info_7_mx11_eea15_e4);
        files.add(R.raw.msg_info_13_mx11_eea15_e4);

        files.add(R.raw.msg_info_1_mx11_e4u2);
        files.add(R.raw.msg_info_2_mx11_e4u2);
        files.add(R.raw.msg_info_3_mx11_e4u2);
        files.add(R.raw.msg_info_4_mx11_e4u2);
        files.add(R.raw.msg_info_6_mx11_e4u2);
        files.add(R.raw.msg_info_7_mx11_e4u2);
        files.add(R.raw.msg_info_13_mx11_e4u2);

        files.add(R.raw.msg_info_1_mx11_e4u3);
        files.add(R.raw.msg_info_2_mx11_e4u3);
        files.add(R.raw.msg_info_3_mx11_e4u3);
        files.add(R.raw.msg_info_4_mx11_e4u3);
        files.add(R.raw.msg_info_6_mx11_e4u3);
        files.add(R.raw.msg_info_7_mx11_e4u3);
        files.add(R.raw.msg_info_13_mx11_e4u3);

        files.add(R.raw.msg_info_1_mx11_e4u31);
        files.add(R.raw.msg_info_2_mx11_e4u31);
        files.add(R.raw.msg_info_3_mx11_e4u31);
        files.add(R.raw.msg_info_4_mx11_e4u31);
        files.add(R.raw.msg_info_6_mx11_e4u31);
        files.add(R.raw.msg_info_7_mx11_e4u31);
        files.add(R.raw.msg_info_13_mx11_e4u31);


        files.add(R.raw.msg_info_1_mx11_e4u1);
        files.add(R.raw.msg_info_2_mx11_e4u1);
        files.add(R.raw.msg_info_3_mx11_e4u1);
        files.add(R.raw.msg_info_4_mx11_e4u1);
        files.add(R.raw.msg_info_6_mx11_e4u1);
        files.add(R.raw.msg_info_7_mx11_e4u1);
        files.add(R.raw.msg_info_13_mx11_e4u1);


        files.add(R.raw.msg_info_1_mx11_e3);
        files.add(R.raw.msg_info_2_mx11_e3);
        files.add(R.raw.msg_info_3_mx11_e3);
        files.add(R.raw.msg_info_4_mx11_e3);
        files.add(R.raw.msg_info_6_mx11_e3);
        files.add(R.raw.msg_info_7_mx11_e3);
        files.add(R.raw.msg_info_13_mx11_e3);


        files.add(R.raw.msg_info_1_kunlun20_e2);
        files.add(R.raw.msg_info_2_kunlun20_e2);
        files.add(R.raw.msg_info_3_kunlun20_e2);
        files.add(R.raw.msg_info_4_kunlun20_e2);
        files.add(R.raw.msg_info_6_kunlun20_e2);
        files.add(R.raw.msg_info_7_kunlun20_e2);
        files.add(R.raw.msg_info_13_kunlun20_e2);

        files.add(R.raw.msg_info_1_kunlun20_e3);
        files.add(R.raw.msg_info_2_kunlun20_e3);
        files.add(R.raw.msg_info_3_kunlun20_e3);
        files.add(R.raw.msg_info_4_kunlun20_e3);
        files.add(R.raw.msg_info_6_kunlun20_e3);
        files.add(R.raw.msg_info_7_kunlun20_e3);
        files.add(R.raw.msg_info_13_kunlun20_e3);

        files.add(R.raw.msg_info_1_kunlun20_e3u1);
        files.add(R.raw.msg_info_2_kunlun20_e3u1);
        files.add(R.raw.msg_info_3_kunlun20_e3u1);
        files.add(R.raw.msg_info_4_kunlun20_e3u1);
        files.add(R.raw.msg_info_6_kunlun20_e3u1);
        files.add(R.raw.msg_info_7_kunlun20_e3u1);
        files.add(R.raw.msg_info_13_kunlun20_e3u1);

        files.add(R.raw.msg_info_1_kunlun20_e4);
        files.add(R.raw.msg_info_2_kunlun20_e4);
        files.add(R.raw.msg_info_3_kunlun20_e4);
        files.add(R.raw.msg_info_4_kunlun20_e4);
        files.add(R.raw.msg_info_6_kunlun20_e4);
        files.add(R.raw.msg_info_7_kunlun20_e4);
        files.add(R.raw.msg_info_13_kunlun20_e4);



//        MsgInfoDao msgInfoDao = MyApplication.getInstance().getMx11E4Database().msgInfoDao();
//        List<MsgInfoEntity> msgInfoEntities = new ArrayList<>();
        files.forEach(file -> {
            try {
                InputStream is = ctx.getResources().openRawResource(file);// 得到数据库文件的数据流
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
//                    Log.e(TAG,line);
                    String[] data = line.split(",");   // 使用逗号作为分隔符
//                    Log.e(TAG, "data " + Arrays.toString(data));
                    MsgInfoEntity msgInfo = new MsgInfoEntity();
                    msgInfo.setName(data[0]);
                    msgInfo.BUSId = Integer.parseInt(data[1]);
                    msgInfo.CANId = Integer.parseInt(data[2]);
                    msgInfo.sendType = data[3];
                    msgInfo.cycleTime = Double.parseDouble(data[4]);
                    msgInfo.comment = data[5];
                    msgInfo.BUSName = data[6];
                    msgInfo.senders = data[7];
                    msgInfo.receivers = data[8];
                    msgInfo.CANType = data[9];

                    msgInfo.cid = MyApplication.getInstance().getDatabase().msgInfoDao().getCidByName(data[10], data[11]);
//                    msgInfoEntities.add(msgInfo);
//                    Log.e(TAG, "msgInfo  :   ZZZZZZZZ" + msgInfo);
                    MyApplication.getInstance().getDatabase().msgInfoDao().insert(msgInfo);
//                    Log.e(TAG,"插入msg成功");
                }
                is.close();
                reader.close();
//                Log.i(TAG,"读取msg csv文件成功 " + file);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
//            msgInfoDao.insertAll(msgInfoEntities);
//            Log.e(TAG, "2222222 msg insert finished");
            Log.e(TAG, "msg insert finished");

        });

        Log.e(TAG, "All message insert finished");
    }


    public static void init_carType() {
        /**********************************************************************************************/
        // 初始化 车型数据库
        List<CarTypeEntity> carTypeEntities = new ArrayList<>();
//        carTypeEntities.add(new CarTypeEntity());

        // Add the first car type
        CarTypeEntity ms11E3 = new CarTypeEntity();
        ms11E3.setCarTypeName("MS11");
        ms11E3.setSDBName("E3");
        carTypeEntities.add(ms11E3);

        CarTypeEntity ms11E4 = new CarTypeEntity();
        ms11E4.setCarTypeName("MS11");
        ms11E4.setSDBName("E4");
        carTypeEntities.add(ms11E4);

        CarTypeEntity ms11E4U1 = new CarTypeEntity();
        ms11E4U1.setCarTypeName("MS11");
        ms11E4U1.setSDBName("E4U1");
        carTypeEntities.add(ms11E4U1);

        CarTypeEntity ms11E4U2 = new CarTypeEntity();
        ms11E4U2.setCarTypeName("MS11");
        ms11E4U2.setSDBName("E4U2");
        carTypeEntities.add(ms11E4U2);

        CarTypeEntity ms11E4U3_0812 = new CarTypeEntity();
        ms11E4U3_0812.setCarTypeName("MS11");
        ms11E4U3_0812.setSDBName("E4U3_0812");
        carTypeEntities.add(ms11E4U3_0812);

        CarTypeEntity ms11E4U3_0915 = new CarTypeEntity();
        ms11E4U3_0915.setCarTypeName("MS11");
        ms11E4U3_0915.setSDBName("E4U3_0915");
        carTypeEntities.add(ms11E4U3_0915);

        CarTypeEntity ms11E4U3_1015 = new CarTypeEntity();
        ms11E4U3_1015.setCarTypeName("MS11");
        ms11E4U3_1015.setSDBName("E4U3_1015");
        carTypeEntities.add(ms11E4U3_1015);

        CarTypeEntity ms11E4U3_1115 = new CarTypeEntity();
        ms11E4U3_1115.setCarTypeName("MS11");
        ms11E4U3_1115.setSDBName("E4U3_1115");
        carTypeEntities.add(ms11E4U3_1115);

        CarTypeEntity ms11RC04 = new CarTypeEntity();
        ms11RC04.setCarTypeName("MS11");
        ms11RC04.setSDBName("RC04");
        carTypeEntities.add(ms11RC04);

        CarTypeEntity ms11RC05 = new CarTypeEntity();
        ms11RC05.setCarTypeName("MS11");
        ms11RC05.setSDBName("RC05");
        carTypeEntities.add(ms11RC05);

        CarTypeEntity ms11RC06 = new CarTypeEntity();
        ms11RC06.setCarTypeName("MS11");
        ms11RC06.setSDBName("RC06");
        carTypeEntities.add(ms11RC06);

        CarTypeEntity ms11RC07 = new CarTypeEntity();
        ms11RC07.setCarTypeName("MS11");
        ms11RC07.setSDBName("RC07");
        carTypeEntities.add(ms11RC07);

        CarTypeEntity ms11RC08 = new CarTypeEntity();
        ms11RC08.setCarTypeName("MS11");
        ms11RC08.setSDBName("RC08");
        carTypeEntities.add(ms11RC08);

        CarTypeEntity ms11RC09 = new CarTypeEntity();
        ms11RC09.setCarTypeName("MS11");
        ms11RC09.setSDBName("RC09");
        carTypeEntities.add(ms11RC09);

        CarTypeEntity ms11RC10 = new CarTypeEntity();
        ms11RC10.setCarTypeName("MS11");
        ms11RC10.setSDBName("RC10");
        carTypeEntities.add(ms11RC10);

        CarTypeEntity ms11RC11 = new CarTypeEntity();
        ms11RC11.setCarTypeName("MS11");
        ms11RC11.setSDBName("RC11");
        carTypeEntities.add(ms11RC11);

        CarTypeEntity ms11RC12 = new CarTypeEntity();
        ms11RC12.setCarTypeName("MS11");
        ms11RC12.setSDBName("RC12");
        carTypeEntities.add(ms11RC12);

        CarTypeEntity ms1125RC01 = new CarTypeEntity();
        ms1125RC01.setCarTypeName("MS11");
        ms1125RC01.setSDBName("25RC01");
        carTypeEntities.add(ms1125RC01);

        CarTypeEntity ms11e4ky = new CarTypeEntity();
        ms11e4ky.setCarTypeName("MS11");
        ms11e4ky.setSDBName("E4-昆易");
        carTypeEntities.add(ms11e4ky);

        CarTypeEntity ms11e3ky = new CarTypeEntity();
        ms11e3ky.setCarTypeName("MS11");
        ms11e3ky.setSDBName("E3-昆易");
        carTypeEntities.add(ms11e3ky);

        CarTypeEntity ms11e3u1ky = new CarTypeEntity();
        ms11e3u1ky.setCarTypeName("MS11");
        ms11e3u1ky.setSDBName("E3U1-昆易");
        carTypeEntities.add(ms11e3u1ky);

        CarTypeEntity ms11e3u1 = new CarTypeEntity();
        ms11e3u1.setCarTypeName("MS11");
        ms11e3u1.setSDBName("E3U1");
        carTypeEntities.add(ms11e3u1);


        CarTypeEntity ms11uE3 = new CarTypeEntity();
        ms11uE3.setCarTypeName("MS11-U");
        ms11uE3.setSDBName("E3");
        carTypeEntities.add(ms11uE3);

        CarTypeEntity ms11uE4bugfix = new CarTypeEntity();
        ms11uE4bugfix.setCarTypeName("MS11-U");
        ms11uE4bugfix.setSDBName("E4-bugfix");
        carTypeEntities.add(ms11uE4bugfix);

        CarTypeEntity ms11uSM3 = new CarTypeEntity();
        ms11uSM3.setCarTypeName("MS11-U");
        ms11uSM3.setSDBName("SM3");
        carTypeEntities.add(ms11uSM3);

        CarTypeEntity ms11u25rc02 = new CarTypeEntity();
        ms11u25rc02.setCarTypeName("MS11-U");
        ms11u25rc02.setSDBName("25RC02");
        carTypeEntities.add(ms11u25rc02);

        CarTypeEntity ms11ue4u3 = new CarTypeEntity();
        ms11ue4u3.setCarTypeName("MS11-U");
        ms11ue4u3.setSDBName("E4U3");
        carTypeEntities.add(ms11ue4u3);

        CarTypeEntity ms11ue4u2 = new CarTypeEntity();
        ms11ue4u2.setCarTypeName("MS11-U");
        ms11ue4u2.setSDBName("E4U2");
        carTypeEntities.add(ms11ue4u2);

        CarTypeEntity ms11ue4u1 = new CarTypeEntity();
        ms11ue4u1.setCarTypeName("MS11-U");
        ms11ue4u1.setSDBName("E4U1");
        carTypeEntities.add(ms11ue4u1);

//        CarTypeEntity mx11E4 = new CarTypeEntity();
//        mx11E4.setCarTypeName("MX11");
//        mx11E4.setSDBName("E4");
//        carTypeEntities.add(mx11E4);

        CarTypeEntity mx11E3 = new CarTypeEntity();
        mx11E3.setCarTypeName("MX11");
        mx11E3.setSDBName("E3");
        carTypeEntities.add(mx11E3);

        CarTypeEntity mx11E4U1 = new CarTypeEntity();
        mx11E4U1.setCarTypeName("MX11");
        mx11E4U1.setSDBName("E4U1");
        carTypeEntities.add(mx11E4U1);

        CarTypeEntity mx11E4U31 = new CarTypeEntity();
        mx11E4U31.setCarTypeName("MX11");
        mx11E4U31.setSDBName("E4U3.1");
        carTypeEntities.add(mx11E4U31);

        CarTypeEntity mx11E4U3 = new CarTypeEntity();
        mx11E4U3.setCarTypeName("MX11");
        mx11E4U3.setSDBName("E4U3");
        carTypeEntities.add(mx11E4U3);

        CarTypeEntity mx11E4U2 = new CarTypeEntity();
        mx11E4U2.setCarTypeName("MX11");
        mx11E4U2.setSDBName("E4U2");
        carTypeEntities.add(mx11E4U2);

        CarTypeEntity mx11EEA15E4 = new CarTypeEntity();
        mx11EEA15E4.setCarTypeName("MX11");
        mx11EEA15E4.setSDBName("EEA1.5_E4");
        carTypeEntities.add(mx11EEA15E4);

        CarTypeEntity mx11EEA15E3 = new CarTypeEntity();
        mx11EEA15E3.setCarTypeName("MX11");
        mx11EEA15E3.setSDBName("EEA1.5_E3");
        carTypeEntities.add(mx11EEA15E3);

        CarTypeEntity kunlun20E3 = new CarTypeEntity();
        kunlun20E3.setCarTypeName("KUNLUN20");
        kunlun20E3.setSDBName("E3");
        carTypeEntities.add(kunlun20E3);

        CarTypeEntity kunlun20E2 = new CarTypeEntity();
        kunlun20E2.setCarTypeName("KUNLUN20");
        kunlun20E2.setSDBName("E2");
        carTypeEntities.add(kunlun20E2);

        CarTypeEntity kunlun20E3U1 = new CarTypeEntity();
        kunlun20E3U1.setCarTypeName("KUNLUN20");
        kunlun20E3U1.setSDBName("E3U1");
        carTypeEntities.add(kunlun20E3U1);

        CarTypeEntity kunlun20E4 = new CarTypeEntity();
        kunlun20E4.setCarTypeName("KUNLUN20");
        kunlun20E4.setSDBName("E4");
        carTypeEntities.add(kunlun20E4);

        CarTypeEntity kunlun20E4U1 = new CarTypeEntity();
        kunlun20E4U1.setCarTypeName("KUNLUN20");
        kunlun20E4U1.setSDBName("E3U2");
        carTypeEntities.add(kunlun20E4U1);

        CarTypeEntity userDefined = new CarTypeEntity();
        userDefined.setCarTypeName("custom");
        userDefined.setSDBName("默认视图");
        carTypeEntities.add(userDefined);


        carTypeEntities.forEach(carTypeEntity -> {
//            carTypeEntity.carTypeName = "MX11";
//            carTypeEntity.SDBName = "E4";
            MyApplication.getInstance().getDatabase().carTypeDao().insert(carTypeEntity);
        });
    }


}

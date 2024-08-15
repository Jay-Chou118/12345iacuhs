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
import java.util.Arrays;
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


    public static void copyDataBase(Context context,String dbName){
        String databaseFilePath = context.getDatabasePath(dbName).getPath();
        FileOutputStream os = null;// 得到数据库文件的写入流
        InputStream is = null;
        byte[] buffer = new byte[8192];
        int count = 0;
        try {
            os = new FileOutputStream(databaseFilePath);
            is = context.getResources().openRawResource(R.raw.mx11_e4);// 得到数据库文件的数据流
            while ((count = is.read(buffer)) > 0) {
                os.write(buffer, 0, count);
                os.flush();
            }
            is.close();
            os.close();
            Log.i(TAG,"初始化数据库文件成功");
        } catch (Exception e) {
            Log.e(TAG,e.toString());
        }
    }



    public static void initDataFromCsv(Context context){
        Log.e(TAG,"initDataFromCsv");
        ArrayList<Integer> files = new ArrayList<>();
        files.add(R.raw.signal_info_1);
        files.add(R.raw.signal_info_2);
        files.add(R.raw.signal_info_3);
        files.add(R.raw.signal_info_4);
        files.add(R.raw.signal_info_6);
        files.add(R.raw.signal_info_7);

        files.forEach(file->{
            try {
                InputStream is = context.getResources().openRawResource(file);// 得到数据库文件的数据流
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String line;
                while ((line =  reader.readLine()) !=null)
                {
                    Log.e(TAG,line);
                    String[] data = line.split("#");   // 使用逗号作为分隔符
                    Log.e(TAG, Arrays.toString(data));
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
                    MyApplication.getInstance().getMx11E4Database().signalInfoDao().insert(signalInfo);
                    Log.e(TAG,"插入signal成功");
                }
                is.close();
                reader.close();
                Log.i(TAG,"读取csv文件成功 " + file);
            } catch (Exception e) {
                Log.e(TAG,e.toString());
            }
        });
    }

    public static void initMsgFromCsv(Context ctx)
    {
        ArrayList<Integer> files = new ArrayList<>();
        files.add(R.raw.msg_info_1);
        files.add(R.raw.msg_info_2);
        files.add(R.raw.msg_info_3);
        files.add(R.raw.msg_info_4);
        files.add(R.raw.msg_info_6);
        files.add(R.raw.msg_info_7);
        files.forEach(file->{
            try {
                InputStream is = ctx.getResources().openRawResource(file);// 得到数据库文件的数据流
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String line;
                while ((line =  reader.readLine()) !=null)
                {
                    String[] data = line.split(",");   // 使用逗号作为分隔符
                    MsgInfoEntity msgInfo = new MsgInfoEntity();
                    msgInfo.name = data[0];
                    msgInfo.BUSId = Integer.parseInt(data[1]);
                    msgInfo.CANId = Integer.parseInt(data[2]);
                    msgInfo.sendType = data[3];
                    msgInfo.cycleTime = Integer.parseInt(data[4]);
                    msgInfo.comment =  data[5];
                    msgInfo.BUSName = data[6];
                    msgInfo.senders = data[7];
                    msgInfo.receivers = data[8];
                    msgInfo.CANType = data[9];
                    msgInfo.namespace = Integer.parseInt(data[10]);
                    MyApplication.getInstance().getMx11E4Database().msgInfoDao().insert(msgInfo);
                    Log.e(TAG,"插入msg成功");
                }
                is.close();
                reader.close();
                Log.i(TAG,"读取msg csv文件成功 " + file);
            } catch (Exception e) {
                Log.e(TAG,e.toString());
            }
        });
    }


    public static void init_database()
    {
        /**********************************************************************************************/
        // 初始化 车型数据库
        List<CarTypeEntity> carTypeEntities = new ArrayList<>();
        carTypeEntities.add(new CarTypeEntity());
        carTypeEntities.forEach(carTypeEntity -> {
            carTypeEntity.carTypeName = "MX11";
            carTypeEntity.SDBName = "E4";
            MyApplication.getInstance().getMx11E4Database().carTypeDao().insert(carTypeEntity);
        });
    }



}

package com.example.testcdc.database;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.testcdc.dao.CarTypeDao;
import com.example.testcdc.dao.MsgInfoDao;
import com.example.testcdc.dao.SignalInfoDao;
import com.example.testcdc.entity.CarTypeEntity;
import com.example.testcdc.entity.MsgInfoEntity;
import com.example.testcdc.entity.SignalInfo;

@Database(entities = {SignalInfo.class, MsgInfoEntity.class, CarTypeEntity.class},version = 1,exportSchema = false)
public abstract class Basic_DataBase extends RoomDatabase {

    public abstract SignalInfoDao signalInfoDao();

    public abstract MsgInfoDao msgInfoDao();

    public abstract CarTypeDao carTypeDao();

    private static volatile Basic_DataBase INSTANCE;

    public static Basic_DataBase CreateDatabase(final Context context){
        if (INSTANCE == null) {
            synchronized (Basic_DataBase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    Basic_DataBase.class, "basic_database")
                            .createFromAsset("basic_database") // 从assets加载数据库文件
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    Log.i(TAG, "数据库创建或打开成功");
                                }
                            })
                            .build();

                }
            }
        }
        return INSTANCE;
    }

}

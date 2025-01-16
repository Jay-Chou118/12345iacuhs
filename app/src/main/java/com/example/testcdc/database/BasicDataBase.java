package com.example.testcdc.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;



import com.example.testcdc.dao.CarTypeDao;
import com.example.testcdc.dao.MsgInfoDao;
import com.example.testcdc.dao.SignalInfoDao;
import com.example.testcdc.entity.CarTypeEntity;
import com.example.testcdc.entity.MsgInfoEntity;
import com.example.testcdc.entity.SignalInfo;

import net.zetetic.database.sqlcipher.SupportOpenHelperFactory;

import java.nio.charset.StandardCharsets;

@Database(entities = {SignalInfo.class, MsgInfoEntity.class, CarTypeEntity.class},version = 1,exportSchema = false)
public abstract class BasicDataBase extends RoomDatabase {

    public abstract SignalInfoDao signalInfoDao();

    public abstract MsgInfoDao msgInfoDao();

    public abstract CarTypeDao carTypeDao();

    private static volatile BasicDataBase INSTANCE;
    static {
        System.loadLibrary("sqlcipher");
    }

    private static final String DB_NAME = "basic_database";
    private static final String DB_PASSWORD = "neo123456";

    public static BasicDataBase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (BasicDataBase.class) {
                if (INSTANCE == null) {
                    SupportOpenHelperFactory factory = new SupportOpenHelperFactory(DB_PASSWORD.getBytes(StandardCharsets.UTF_8));
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            BasicDataBase.class, DB_NAME)
                            .openHelperFactory(factory)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

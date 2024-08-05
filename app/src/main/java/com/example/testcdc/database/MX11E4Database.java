package com.example.testcdc.database;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.testcdc.dao.SignalInfoDao;
import com.example.testcdc.entity.SignalInfo;

@Database(entities = {SignalInfo.class},version = 1,exportSchema = false)
public abstract class MX11E4Database extends RoomDatabase {

    public abstract SignalInfoDao signalInfoDao();

    private static MX11E4Database mx11E4Database = null;

    public static MX11E4Database getInstance(Context ctx)
    {
        if(mx11E4Database == null)
        {
            mx11E4Database =  Room.databaseBuilder(ctx.getApplicationContext(),MX11E4Database.class,"mx11_e4")
                    .allowMainThreadQueries()
                    .addMigrations()
                    .build();
        }
        return mx11E4Database;
    }
}

package com.example.testcdc.database;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.testcdc.dao.CarTypeDao;
import com.example.testcdc.dao.MsgInfoDao;
import com.example.testcdc.dao.SdbDao;
import com.example.testcdc.dao.SignalInfoDao;
import com.example.testcdc.entity.CarTypeEntity;
import com.example.testcdc.entity.MsgInfoEntity;
import com.example.testcdc.entity.SdbEntity;
import com.example.testcdc.entity.SignalInfo;

@Database(entities = {SignalInfo.class, MsgInfoEntity.class, CarTypeEntity.class, SdbEntity.class},version = 1,exportSchema = false)
public abstract class MX11E4Database extends RoomDatabase {

    public abstract SignalInfoDao signalInfoDao();

    public abstract MsgInfoDao msgInfoDao();

    public abstract CarTypeDao carTypeDao();

    public abstract SdbDao sdbDao();
}

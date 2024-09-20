package com.example.testcdc.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

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



}

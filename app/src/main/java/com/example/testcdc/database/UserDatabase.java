package com.example.testcdc.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.testcdc.dao.UserMsgInfoDao;
import com.example.testcdc.dao.UserSignalInfoDao;
import com.example.testcdc.entity.UserMsgEntity;
import com.example.testcdc.entity.UserSignalEntity;



@Database(entities = {UserSignalEntity.class, UserMsgEntity.class},version = 1,exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {

    public abstract UserMsgInfoDao userMsgInfoDao();

    public abstract UserSignalInfoDao userSignalInfoDao();

}

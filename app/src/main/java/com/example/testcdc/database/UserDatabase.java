package com.example.testcdc.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.testcdc.entity.UserMsgEntity;
import com.example.testcdc.entity.UserSignalEntity;



@Database(entities = {UserSignalEntity.class, UserMsgEntity.class},version = 1,exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {




    private static volatile UserDatabase INSTANCE;

    public static void destroyInstance() {
        INSTANCE = null;
    }

}

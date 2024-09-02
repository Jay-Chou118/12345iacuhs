package com.example.testcdc.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;


import com.example.testcdc.entity.SignalInfo;
import com.example.testcdc.entity.UserMsgEntity;
import com.example.testcdc.entity.UserSignalEntity;

import java.util.List;


@Dao
public interface UserSignalInfoDao {

    @Insert
    void insert(UserSignalEntity userSignal);

    @Query("select * from user_signal")
    List<UserSignalEntity> getAll();

    @Delete
    void delete(UserSignalEntity userSignal);

    @Query("DELETE FROM user_signal WHERE channel = :channelId")
    void deleteByChannel(int channelId);

    @Transaction
    default void deleteAllUsers(List<UserSignalEntity> userSignal) {
        for (UserSignalEntity user : userSignal) {
            delete(user);
        }
    }


}

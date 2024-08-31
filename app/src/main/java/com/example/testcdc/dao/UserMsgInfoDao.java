package com.example.testcdc.dao;


import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.testcdc.entity.UserMsgEntity;
import com.example.testcdc.entity.UserSignalEntity;

import java.util.List;

@Dao
public interface UserMsgInfoDao {

    @Insert
    void insert(UserMsgEntity userMsgEntity);

    @Query("select * from user_msg")
    List<UserMsgEntity> getAll();

    @Delete
    void delete(UserMsgEntity userMsgEntity);

    @Transaction
    default void deleteAllUsers(List<UserMsgEntity> userMsgEntity) {
        for (UserMsgEntity user : userMsgEntity) {
            delete(user);
        }
    }

    @Query("DELETE FROM user_msg WHERE channel = :channelId")
    void deleteByChannelId(@NonNull int channelId);


}

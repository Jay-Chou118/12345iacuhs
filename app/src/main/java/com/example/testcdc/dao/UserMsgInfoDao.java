package com.example.testcdc.dao;


import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.testcdc.entity.MsgInfoEntity;
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

    @Query("DELETE FROM user_msg WHERE bus_id = :BusId")
    void deleteByChannel(int BusId);


    @Transaction
    default void deleteAllUsers(List<UserMsgEntity> userMsgEntity) {
        for (UserMsgEntity user : userMsgEntity) {
            delete(user);
        }
    }

    @Query("DELETE FROM user_msg WHERE bus_id = :BUSId")
    void deleteByChannelId(@NonNull int BUSId);

    @Query("select * from user_msg where bus_id=:BUSId ")
    List<UserMsgEntity> getUserMsg(int BUSId);


}

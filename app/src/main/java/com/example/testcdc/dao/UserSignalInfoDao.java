package com.example.testcdc.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;


import com.example.testcdc.entity.SignalInfo;
import com.example.testcdc.entity.UserSignalEntity;

import java.util.ArrayList;
import java.util.List;


@Dao
public interface UserSignalInfoDao {

    @Insert
    void insert(UserSignalEntity userSignal);

    @Query("select * from user_signal")
    List<UserSignalEntity> getAll();

    @Delete
    void delete(UserSignalEntity userSignal);

    @Query("DELETE FROM user_signal WHERE bus_id = :BusId")
    void deleteByChannel(int BusId);

    @Query("SELECT DISTINCT bus_id FROM user_signal")
    List<Integer> getAllBusIds();

    default ArrayList<Integer> getBusIdsAsArrayList() {
        List<Integer> busIds = getAllBusIds();
        return new ArrayList<>(busIds);
    }

    @Query("select * from user_signal where bus_id=:BUSId and can_id=:CANId ")
    List<UserSignalEntity> getUserSignal( int BUSId, int CANId);


    @Transaction
    default void deleteAllUsers(List<UserSignalEntity> userSignal) {
        for (UserSignalEntity user : userSignal) {
            delete(user);
        }
    }





}

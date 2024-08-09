package com.example.testcdc.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.testcdc.entity.SignalInfo;

import java.util.List;

@Dao
public interface SignalInfoDao {

    @Insert
    void insert(SignalInfo signalInfo);

    @Query("select * from signal_info")
    List<SignalInfo> getAll();

    @Query("select * from signal_info where bus_id=:BUSId and can_id=:CANId")
    List<SignalInfo> getSignal(int BUSId, int CANId);
    @Query("select * from signal_info where id=:id")
    SignalInfo getSignalById(long id);

    @Query("select * from signal_info where name=:name limit 1")
    SignalInfo getSignal(String name);
}

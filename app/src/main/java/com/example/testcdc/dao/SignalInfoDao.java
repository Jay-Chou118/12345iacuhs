package com.example.testcdc.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.testcdc.entity.SignalInfo;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface SignalInfoDao {

    @Insert
    void insert(SignalInfo signalInfo);

    @Query("select * from signal_info")
    List<SignalInfo> getAll();

    @Query("select * from signal_info where bus_id=:BUSId and can_id=:CANId")
    List<SignalInfo> getSignal(int BUSId, int CANId);

    @Query("select * from signal_info where bus_id=:BUSId and can_id=:CANId and cid=:cid")
    List<SignalInfo> getSignalBycid(long cid,int BUSId, int CANId);

    @Query("select * from signal_info where id=:id")
    SignalInfo getSignalById(long id);

    @Query("select * from signal_info where name=:name limit 1")
    SignalInfo getSignal(String name);

    @Query("select * from signal_info where bus_id=:BUSId and cid=:cid")
    List<SignalInfo> getSignalByBusId(int BUSId,long cid);

    @Query("SELECT DISTINCT bus_id FROM signal_info WHERE cid = :cid")
    List<Integer> getAllBusIds(long cid);


    default ArrayList<Integer> getBusIdsAsArrayList(long cid) {
        List<Integer> busIds = getAllBusIds(cid);
        return new ArrayList<>(busIds);
    }

    @Query("DELETE FROM signal_info WHERE cid = :cid")
    void deleteBycid(long cid);

}

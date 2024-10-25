package com.example.testcdc.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.testcdc.entity.MsgInfoEntity;
import com.example.testcdc.entity.SignalInfo;
import com.example.testcdc.entity.SignalInfo_getdbc;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface SignalInfoDao {

    @Insert
    void insert(SignalInfo signalInfo);

    @Insert
    void insertAll(List<SignalInfo> signalInfos);

    @Query("select * from signal_info")
    List<SignalInfo> getAll();

    @Query("select * from signal_info where bus_id=:BUSId and can_id=:CANId")
    List<SignalInfo> getSignal(int BUSId, int CANId);

    @Query("select * from signal_info where bus_id=:BUSId and can_id=:CANId and cid =:cid" )
    List<SignalInfo> getSignal(long cid,int BUSId, int CANId);

    @Query("select * from signal_info where bus_id=:BUSId and can_id=:CANId and cid=:cid")
    List<SignalInfo> getSignalBycid(long cid,int BUSId, int CANId);

    @Query("select * from signal_info where id=:id")
    SignalInfo getSignalById(long id);

    @Query("select * from signal_info where name=:name limit 1")
    SignalInfo getSignal(String name);

    @Query("select * from signal_info where bus_id=:BUSId and cid=:cid")
    List<SignalInfo> getSignalByBusIdcid(long cid,int BUSId);


    @Query("SELECT DISTINCT bus_id FROM signal_info WHERE cid = :cid")
    List<Integer> getAllBusIds(long cid);


    default ArrayList<Integer> getBusIdsAsArrayList(long cid) {
        List<Integer> busIds = getAllBusIds(cid);
        return new ArrayList<>(busIds);
    }


    @Query("SELECT EXISTS (SELECT 1 FROM signal_info WHERE cid = :cid)")
    boolean existsBycid(long cid);


    @Query("DELETE FROM signal_info WHERE cid = :cid")
    void deleteBycid(long cid);


    @Query("SELECT name FROM signal_info WHERE cid = :cidValue and can_id = :can_id")
    List<String> getSignalNameByCid(long cidValue,int can_id);

    @Query("select * from signal_info where can_id=:CANId and cid =:cid" )
    List<SignalInfo> getSignal(long cid, int CANId);

    @Query("SELECT name FROM signal_info WHERE cid = :cidValue ")
    List<String> getSignalByCid(long cidValue);


    @Query("SELECT name FROM signal_info WHERE bus_id = :busidValue ")
    List<String> getSignalByBusId(int busidValue);

    @Query("select name,comment,choices from signal_info where bus_id=:BUSId and can_id=:CANId and cid=:cid")
    List<SignalInfo_getdbc> getSignalBy3col(long cid, int BUSId, int CANId);
}

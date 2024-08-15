package com.example.testcdc.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.testcdc.entity.MsgInfoEntity;
import com.example.testcdc.entity.SignalInfo;

import java.util.List;

@Dao
public interface MsgInfoDao {

    @Insert
    void insert(MsgInfoEntity msgInfoEntity);

    @Query("select * from msg_info where bus_id=:BUSId")
    List<MsgInfoEntity> getMsg(int BUSId);

    @Query("select * from msg_info where bus_id=:BUSId and can_id=:CANId limit 1")
    MsgInfoEntity getMsg(int BUSId, int CANId);
}

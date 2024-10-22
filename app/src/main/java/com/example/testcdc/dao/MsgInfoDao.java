package com.example.testcdc.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.testcdc.entity.MsgInfoEntity;

import java.util.List;

@Dao
public interface MsgInfoDao {

    @Insert
    void insert(MsgInfoEntity msgInfoEntity);

    @Insert
    void insertAll(List<MsgInfoEntity> msgInfos);

    @Query("select * from msg_info where bus_id=:BUSId and cid=:cid")
    List<MsgInfoEntity> getMsg(int BUSId,long cid);

    @Query("select * from msg_info where bus_id=:BUSId and can_id=:CANId limit 1")
    MsgInfoEntity getMsg(int BUSId, int CANId);

    @Query("select * from car_type where car_type_name = :cartTypeName and sdb_name = :SDBName limit 1")
    long getCidByName(String cartTypeName, String SDBName);

    @Query("select * from msg_info where  cid = :cid and bus_id =:BUSId")
    List<MsgInfoEntity> getMsgBycidBusId(int BUSId,long cid);

    @Query("select * from msg_info where  cid = :cid")
    List<MsgInfoEntity> getMsgBycid(long cid);

    @Query("SELECT DISTINCT bus_id FROM msg_info WHERE cid = :cid")
    List<Integer> getDistinctBusIdsByCid(long cid);

    @Query("SELECT EXISTS (SELECT 1 FROM msg_info WHERE cid = :cid)")
    boolean existsBycid(long cid);

    @Query("DELETE FROM msg_info WHERE cid = :cid")
    void deleteBycid(long cid);


}

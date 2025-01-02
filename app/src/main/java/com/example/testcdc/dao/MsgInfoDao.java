package com.example.testcdc.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;


import com.example.testcdc.entity.ChannelInf;
import com.example.testcdc.entity.MsgInfoEntity;

import java.util.List;
import java.util.Map;

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

    @Query("select EXISTS (select 1 from msg_info where cid = :cid)")
    boolean existsBycid(long cid);

    @Query("delete from msg_info where cid = :cid")
    void deleteBycid(long cid);

//    @Query("select DISTINCT bus_name , bus_id FROM msg_info WHERE cid = :cid order by bus_id"  )
//    List<Map<String,Object>> getDistinctBusNamesAndIdsByCid(long cid);

//    @RawQuery
//    List<Map<String, Object>> getDistinctBusNamesAndIdsByCid(SupportSQLiteQuery query);

    @Query("select DISTINCT bus_name , bus_id FROM msg_info WHERE cid = :cid order by bus_id"  )
    List<ChannelInf> getDistinctBusNamesAndIdsByCid(long cid);


    @Query("SELECT can_id FROM msg_info WHERE cid = :cid and bus_id = :bus_id and name = :name")
    List<Integer> getCanId(long cid, int bus_id, String name);


}

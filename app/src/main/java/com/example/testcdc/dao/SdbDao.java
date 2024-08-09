package com.example.testcdc.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.testcdc.entity.SdbEntity;

import java.util.List;

@Dao
public interface SdbDao {
    @Query("select * from sdb")
    List<SdbEntity> getAll();

    @Insert
    void insert(SdbEntity sdb);
}

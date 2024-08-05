package com.example.testcdc.dao;

import androidx.room.Dao;
import androidx.room.Insert;

import com.example.testcdc.entity.SignalInfo;

@Dao
public interface SignalInfoDao {

    @Insert
    void insert(SignalInfo signalInfo);
}

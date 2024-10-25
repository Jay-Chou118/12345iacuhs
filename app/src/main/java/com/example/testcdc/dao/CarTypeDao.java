package com.example.testcdc.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.testcdc.entity.CarTypeEntity;

import java.util.List;

@Dao
public interface CarTypeDao {
    @Query("select * from car_type")
    List<CarTypeEntity> getAll();

    @Query("select * from car_type where car_type_name=:cartTypeName and sdb_name=:SDBName limit 1")
    CarTypeEntity getByName(String cartTypeName,String SDBName);

    @Query("select id from car_type where car_type_name = :cartTypeName and sdb_name = :SDBName limit 1")
    long getCidByName(String cartTypeName, String SDBName);

    @Insert
    void insert(CarTypeEntity carType);
}

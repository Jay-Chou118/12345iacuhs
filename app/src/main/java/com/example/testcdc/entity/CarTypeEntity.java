package com.example.testcdc.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "car_type")
public class CarTypeEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;
    @ColumnInfo(name = "car_type_name")
    public String carTypeName;

    @ColumnInfo(name="sdb_name")
    public String SDBName;

    public CarTypeEntity() {
    }
}

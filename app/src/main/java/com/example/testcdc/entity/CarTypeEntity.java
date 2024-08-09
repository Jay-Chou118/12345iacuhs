package com.example.testcdc.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "car_type")
public class CarTypeEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;
    @ColumnInfo(name = "name")
    public String name;

    public CarTypeEntity(String name) {
        this.name = name;
    }

    public CarTypeEntity() {
    }
}

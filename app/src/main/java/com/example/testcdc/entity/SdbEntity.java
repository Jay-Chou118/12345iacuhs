package com.example.testcdc.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "sdb")
public class SdbEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "fk_cid")
    public long carTypeId;

    public SdbEntity(String name, long carTypeId) {
        this.name = name;
        this.carTypeId = carTypeId;
    }

    public SdbEntity() {
    }
}

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

    public long getId() {
        return id;
    }

    public String getCarTypeName() {
        return carTypeName;
    }

    public String getSDBName() {
        return SDBName;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setCarTypeName(String carTypeName) {
        this.carTypeName = carTypeName;
    }

    public void setSDBName(String SDBName) {
        this.SDBName = SDBName;
    }

    public CarTypeEntity() {
    }



    @Override
    public String toString() {
        return "CarTypeEntity{" +
                "id=" + id +
                ", carTypeName='" + carTypeName + '\'' +
                ", SDBName='" + SDBName + '\'' +
                '}';
    }
}

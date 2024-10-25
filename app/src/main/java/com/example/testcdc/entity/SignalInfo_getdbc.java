package com.example.testcdc.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "signal_info",indices = @Index({"bus_id","cid","can_id"}))
public class SignalInfo_getdbc {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name="comment")
    public String comment;
    @ColumnInfo(name="choices")
    public String choices;

    @Ignore
    public List<Double> times = new ArrayList<>();
    @Ignore
    public List<Double> values = new ArrayList<>();


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "SignalInfo{" +
                ", name='" + name + '\'' +
                ", comment='" + comment + '\'' +
                ", choices='" + choices + '\'' +
                '}';
    }
}



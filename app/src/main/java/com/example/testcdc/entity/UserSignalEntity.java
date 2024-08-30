package com.example.testcdc.entity;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_signal")
public class UserSignalEntity {


    @PrimaryKey(autoGenerate = true)
    public long id;
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name="bus_id")
    public int BUSId;
    @ColumnInfo(name="can_id")
    public int CANId;
    @ColumnInfo(name="byte_order")
    public boolean byteOrder;
    @ColumnInfo(name="is_signed")
    public boolean isSigned;
    @ColumnInfo(name="bit_start")
    public int bitStart;
    @ColumnInfo(name="bit_length")
    public int bitLength;
    @ColumnInfo(name="scale")
    public double scale;
    @ColumnInfo(name="offset")
    public double offset;
    @ColumnInfo(name="comment")
    public String comment;

    @ColumnInfo(name="minimum")
    public Double minimum;

    @ColumnInfo(name="maximum")
    public Double maximum;

    @ColumnInfo(name="initial")
    public Double initial;

    @ColumnInfo(name="choices")
    public String choices;

    @ColumnInfo(name="channel")
    public char channel;


    @Override
    public String toString() {
        return "UserSignalEntity{" +
                "name='" + name + '\'' +
                ", BUSId=" + BUSId +
                ", CANId=" + CANId +
                ", byteOrder=" + byteOrder +
                ", isSigned=" + isSigned +
                ", bitStart=" + bitStart +
                ", bitLength=" + bitLength +
                ", scale=" + scale +
                ", offset=" + offset +
                ", comment='" + comment + '\'' +
                ", minimum=" + minimum +
                ", maximum=" + maximum +
                ", initial=" + initial +
                ", choices='" + choices + '\'' +
                ", channel=" + channel +
                '}';
    }
}

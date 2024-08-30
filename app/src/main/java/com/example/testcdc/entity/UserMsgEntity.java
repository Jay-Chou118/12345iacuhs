package com.example.testcdc.entity;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_msg")
public class UserMsgEntity {


    @PrimaryKey(autoGenerate = true)
    public long id;
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name="bus_id")
    public int BUSId;
    @ColumnInfo(name="can_id")
    public int CANId;

    @ColumnInfo(name = "send_type")
    public String sendType;
    @ColumnInfo(name = "cycle_time")
    public double cycleTime;
    @ColumnInfo(name = "comment")
    public String comment;
    @ColumnInfo(name = "bus_name")
    public String BUSName;
    @ColumnInfo(name="senders")
    public String senders;
    @ColumnInfo(name="receivers")
    public String receivers;
    @ColumnInfo(name="can_type")
    public String CANType;

    @ColumnInfo(name="channel")
    public char channel;

    public UserMsgEntity() {
    }

    @Override
    public String toString() {
        return "UserMsgEntity{" +
                "name='" + name + '\'' +
                ", BUSId=" + BUSId +
                ", CANId=" + CANId +
                ", sendType='" + sendType + '\'' +
                ", cycleTime=" + cycleTime +
                ", comment='" + comment + '\'' +
                ", BUSName='" + BUSName + '\'' +
                ", senders='" + senders + '\'' +
                ", receivers='" + receivers + '\'' +
                ", CANType='" + CANType + '\'' +
                ", channel=" + channel +
                '}';
    }
}

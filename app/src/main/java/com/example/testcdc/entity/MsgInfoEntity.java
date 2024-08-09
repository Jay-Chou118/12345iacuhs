package com.example.testcdc.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "msg_info")
public class MsgInfoEntity {

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
    public int cycleTime;
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

    @ColumnInfo(name="namespace")
    public int namespace;

    public MsgInfoEntity(String name, int BUSId, int CANId, String sendType, int cycleTime, String comment, String BUSName, String senders, String receivers, String CANType, int namespace) {
        this.name = name;
        this.BUSId = BUSId;
        this.CANId = CANId;
        this.sendType = sendType;
        this.cycleTime = cycleTime;
        this.comment = comment;
        this.BUSName = BUSName;
        this.senders = senders;
        this.receivers = receivers;
        this.CANType = CANType;
        this.namespace = namespace;
    }

    public MsgInfoEntity() {
    }

    @Override
    public String toString() {
        return "MsgInfoEntity{" +
                "name='" + name + '\'' +
                ", BUSId=" + BUSId +
                ", CANId=" + CANId +
                '}';
    }
}

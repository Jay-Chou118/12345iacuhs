package com.example.testcdc.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
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

    @ColumnInfo(name="cid")
    public long cid;

    public MsgInfoEntity() {
    }

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

    public String getBUSName() {
        return BUSName;
    }

    public void setBUSName(String BUSName) {
        this.BUSName = BUSName;
    }

    public int getBUSId() {
        return BUSId;
    }

    public void setBUSId(int BUSId) {
        this.BUSId = BUSId;
    }

    @Override
    public String toString() {
        return "MsgInfoEntity{" +
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
                ", cid=" + cid +
                '}';
    }
}

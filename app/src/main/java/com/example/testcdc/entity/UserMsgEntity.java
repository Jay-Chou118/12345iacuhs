package com.example.testcdc.entity;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;
import java.util.Map;

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

    @ColumnInfo(name="include_signal")
    public String signals;

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

//    @ColumnInfo(name="channel")
//    public int channel;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCANId() {
        return CANId;
    }

    public void setCANId(int CANId) {
        this.CANId = CANId;
    }

    public String getSignals() {
        return signals;
    }

    public void setSignals(String signals) {
        this.signals = signals;
    }

    public String getSendType() {
        return sendType;
    }

    public void setSendType(String sendType) {
        this.sendType = sendType;
    }

    public double getCycleTime() {
        return cycleTime;
    }

    public void setCycleTime(double cycleTime) {
        this.cycleTime = cycleTime;
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

    public String getSenders() {
        return senders;
    }

    public void setSenders(String senders) {
        this.senders = senders;
    }

    public String getReceivers() {
        return receivers;
    }

    public void setReceivers(String receivers) {
        this.receivers = receivers;
    }

    public String getCANType() {
        return CANType;
    }

    public void setCANType(String CANType) {
        this.CANType = CANType;
    }

    public int getBUSId() {
        return BUSId;
    }

    public void setBUSId(int BUSId) {
        this.BUSId = BUSId;
    }

    public UserMsgEntity() {
    }



}

package com.example.testcdc.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "signal_info")
public class SignalInfo {

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

    @Ignore
    public List<Double> times = new ArrayList<>();
    @Ignore
    public List<Double> values = new ArrayList<>();


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBUSId() {
        return BUSId;
    }

    public void setBUSId(int BUSId) {
        this.BUSId = BUSId;
    }

    public int getCANId() {
        return CANId;
    }

    public void setCANId(int CANId) {
        this.CANId = CANId;
    }

    public boolean isByteOrder() {
        return byteOrder;
    }

    public void setByteOrder(boolean byteOrder) {
        this.byteOrder = byteOrder;
    }

    public boolean isSigned() {
        return isSigned;
    }

    public void setSigned(boolean signed) {
        isSigned = signed;
    }

    public int getBitStart() {
        return bitStart;
    }

    public void setBitStart(int bitStart) {
        this.bitStart = bitStart;
    }

    public int getBitLength() {
        return bitLength;
    }

    public void setBitLength(int bitLength) {
        this.bitLength = bitLength;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public SignalInfo(String name, int BUSId, int CANId, boolean byteOrder, boolean isSigned, int bitStart, int bitLength, double scale, double offset, String comment) {
        this.name = name;
        this.BUSId = BUSId;
        this.CANId = CANId;
        this.byteOrder = byteOrder;
        this.isSigned = isSigned;
        this.bitStart = bitStart;
        this.bitLength = bitLength;
        this.scale = scale;
        this.offset = offset;
        this.comment = comment;
    }

    public SignalInfo() {
    }

    @Override
    public String toString() {
        return "SignalInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", BUSId=" + BUSId +
                ", CANId=" + CANId +
                ", byteOrder=" + byteOrder +
                ", isSigned=" + isSigned +
                ", bitStart=" + bitStart +
                ", bitLength=" + bitLength +
                ", scale=" + scale +
                ", offset=" + offset +
                ", comment='" + comment + '\'' +
                '}';
    }
}

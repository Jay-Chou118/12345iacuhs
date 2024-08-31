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
    public int channel;

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

    public Double getMinimum() {
        return minimum;
    }

    public void setMinimum(Double minimum) {
        this.minimum = minimum;
    }

    public Double getMaximum() {
        return maximum;
    }

    public void setMaximum(Double maximum) {
        this.maximum = maximum;
    }

    public Double getInitial() {
        return initial;
    }

    public void setInitial(Double initial) {
        this.initial = initial;
    }

    public String getChoices() {
        return choices;
    }

    public void setChoices(String choices) {
        this.choices = choices;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

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

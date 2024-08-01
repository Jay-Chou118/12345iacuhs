package com.example.testcdc;

import java.io.Serializable;
import java.util.Arrays;

public class CanMessage implements Cloneable, Serializable {
    long index;
    int CAN_ID;
    byte direct;
    byte CAN_TYPE;
    byte BUS_ID;
    byte dataLength;
    long timestamp;
    byte[] data;

    public CanMessage() {
    }



    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public int getCAN_ID() {
        return CAN_ID;
    }

    public void setCAN_ID(int CAN_ID) {
        this.CAN_ID = CAN_ID;
    }

    public byte getDirect() {
        return direct;
    }

    public void setDirect(byte direct) {
        this.direct = direct;
    }

    public byte getCAN_TYPE() {
        return CAN_TYPE;
    }

    public void setCAN_TYPE(byte CAN_TYPE) {
        this.CAN_TYPE = CAN_TYPE;
    }

    public byte getBUS_ID() {
        return BUS_ID;
    }

    public void setBUS_ID(byte BUS_ID) {
        this.BUS_ID = BUS_ID;
    }

    public byte getDataLength() {
        return dataLength;
    }

    public void setDataLength(byte dataLength) {
        this.dataLength = dataLength;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CanMessage{" +
                "index=" + index +
                ", CAN_ID=" + CAN_ID +
                ", direct=" + direct +
                ", CAN_TYPE=" + CAN_TYPE +
                ", BUS_ID=" + BUS_ID +
                ", dataLength=" + dataLength +
                ", timestamp=" + timestamp +
                ", data=" + Arrays.toString(data) +
                '}';
    }

    @Override
    public CanMessage clone() throws CloneNotSupportedException {

        CanMessage clone = (CanMessage) super.clone();
//        clone.CAN_ID = this.CAN_ID;
//        clone.index = this.index;
//        clone.CAN_TYPE = this.CAN_TYPE;
//        clone.BUS_ID = this.BUS_ID;
//        clone.data = this.data;
//        clone.dataLength = this.dataLength;
//        clone.direct = this.direct;
//        clone.timestamp = this.timestamp;
        return clone;
    }
}

package com.example.testcdc.MiCAN;

import java.util.List;

public class ShowCANMsg {
    long sqlId;
    double timestamp;
    int channel;
    int arbitrationId;
    String name;
    String canType;
    String dir;
    String dlc;

    short[] data;


    public long getSqlId() {
        return sqlId;
    }

    public void setSqlId(long sqlId) {
        this.sqlId = sqlId;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getArbitrationId() {
        return arbitrationId;
    }

    public void setArbitrationId(int arbitrationId) {
        this.arbitrationId = arbitrationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCanType() {
        return canType;
    }

    public void setCanType(String canType) {
        this.canType = canType;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getDlc() {
        return dlc;
    }

    public void setDlc(String dlc) {
        this.dlc = dlc;
    }

    public short[] getData() {
        return data;
    }

    public void setData(short[] data) {
        this.data = data;
    }

    public void setData(byte[] data)
    {
        this.data = new short[data.length];
        for(int i = 0;i<data.length;i++)
        {
            this.data[i] = (short) (data[i] & 0xff);
        }
    }
}

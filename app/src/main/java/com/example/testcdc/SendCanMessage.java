package com.example.testcdc;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class SendCanMessage implements Cloneable, Serializable {

    short period; //2个字节
    byte isReady; //1个字节
    byte slot;//1个字节
    int CanID;//4个字节
    byte  BUSId;
    byte  dataLength;
    byte  FDFormat;
    byte  unused_2;
    byte[] data = new byte[64];

    public SendCanMessage(){

    }

    public short getPeriod() {
        return period;
    }

    public void setPeriod(short period) {
        this.period = period;
    }

    public byte getIsReady() {
        return isReady;
    }

    public void setIsReady(byte isReady) {
        this.isReady = isReady;
    }

    public byte getSlot() {
        return slot;
    }

    public void setSlot(byte slot) {
        this.slot = slot;
    }

    public int getCanID() {
        return CanID;
    }

    public void setCanID(int canID) {
        CanID = canID;
    }

    public byte getBUSId() {
        return BUSId;
    }

    public void setBUSId(byte BUSId) {
        this.BUSId = BUSId;
    }

    public byte getDataLength() {
        return dataLength;
    }

    public void setDataLength(byte dataLength) {
        this.dataLength = dataLength;
    }

    public byte getFDFormat() {
        return FDFormat;
    }

    public void setFDFormat(byte FDFormat) {
        this.FDFormat = FDFormat;
    }

    public byte getUnused_2() {
        return unused_2;
    }

    public void setUnused_2(byte unused_2) {
        this.unused_2 = unused_2;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setDataFromJsonArray(JsonArray rawData)
    {

        int size = Math.min(rawData.size(),data.length);

        for (int i = 0; i< size; i++)
        {

            JsonElement element = rawData.get(i);
            if (element.isJsonPrimitive()) {
                // 获取第i个元素的int值并转换成byte赋值给data[i]
                int value = element.getAsInt();
                data[i] = (byte) value;
            } else {
                throw new IllegalArgumentException("Element at index " + i + " is not an integer.");
            }

        }

    }

    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(76); // 总长度为75字节
        buffer.order(ByteOrder.LITTLE_ENDIAN); // 设置字节序为大端序

        // 将short类型的period写入ByteBuffer
        buffer.putShort(period);
        // 写入isReady, slot, CAN ID, BUS Id, dataLength, FDFormat, unused_2
        buffer.put(isReady);
        buffer.put(slot);
        buffer.putInt(CanID);
        buffer.put(BUSId);
        buffer.put(dataLength);
        buffer.put(FDFormat);
        buffer.put(unused_2);

        // 写入data数组
        buffer.put(data);

        return buffer.array();
    }




    public byte[] appendDataTomCmdData(byte[] mCmdData,byte[] DATA) {

        // 创建一个新的数组，包含mCmdData和DATA
        byte[] result = new byte[mCmdData.length + DATA.length];
        System.arraycopy(mCmdData, 0, result, 0, mCmdData.length);
        System.arraycopy(DATA, 0, result, mCmdData.length, DATA.length);

        return result;
    }


    // 将SendCanMessage对象转换为十六进制字符串表示的数据流，并追加到mCmdData后
    public byte[] toHexStreamAndAppend(byte[] mCmdData) {
        ByteBuffer buffer = ByteBuffer.allocate(mCmdData.length + 4 * 5 + 64); // 大小为mCmdData长度加上所有int类型的字节数和data数组长度

        // 将mCmdData追加到buffer中
        buffer.put(mCmdData);

        // 将period转换为字节
        buffer.putInt(period);

        // 将isReady转换为字节
        buffer.put(isReady);

        // 将slot转换为字节
        buffer.put(slot);

        // 将CanID转换为字节
        buffer.putInt(CanID);

        // 将BUSId转换为字节
        buffer.put(BUSId);

        // 将dataLength转换为字节
        buffer.put(dataLength);

        // 将FDFormat转换为字节
        buffer.put(FDFormat);

        // 将unused_2转换为字节
        buffer.put(unused_2);

        // 添加data字段
        buffer.put(data);

        return buffer.array();
    }


    @Override
    public String toString() {
        return "SendCanMessage{" +
                "perid=" + period +
                ", isReady=" + isReady +
                ", slot=" + slot +
                ", CanID=" + CanID +
                ", BUSId=" + BUSId +
                ", dataLength=" + dataLength +
                ", FDFormat=" + FDFormat +
                ", unused_2=" + unused_2 +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}

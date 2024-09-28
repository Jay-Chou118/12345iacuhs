package com.example.testcdc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class SendCanMessage implements Cloneable, Serializable {

    short period;
    byte isReady;
    byte slot;
    int CanID;
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

    public  static  int getCanMessageLength()
    {
        return Short.BYTES + Byte.BYTES + Byte.BYTES + Integer.BYTES +
                Byte.BYTES + Byte.BYTES + Byte.BYTES + Byte.BYTES +
                (Byte.BYTES * 64);
    }

    // 将SendCanMessage对象转换为十六进制字符串表示的数据流
    public String toHexStream() {
        StringBuilder hexBuilder = new StringBuilder();

        // 将period转换为字节
        byte[] periodBytes = ByteBuffer.allocate(4).putInt(period).array();
        hexBuilder.append(byteArrayToHex(periodBytes));

        // 将isReady转换为字节
        byte[] isReadyBytes = ByteBuffer.allocate(4).putInt(isReady).array();
        hexBuilder.append(byteArrayToHex(isReadyBytes));

        // 将slot转换为字节
        byte[] slotBytes = ByteBuffer.allocate(4).putInt(slot).array();
        hexBuilder.append(byteArrayToHex(slotBytes));

        // 将CanID转换为字节
        byte[] canIDBytes = ByteBuffer.allocate(4).putInt(CanID).array();
        hexBuilder.append(byteArrayToHex(canIDBytes));

        // 将BUSId转换为字节
        byte[] busIdBytes = ByteBuffer.allocate(4).putInt(BUSId).array();
        hexBuilder.append(byteArrayToHex(busIdBytes));

        // 将dataLength转换为字节
        byte[] dataLengthBytes = ByteBuffer.allocate(4).putInt(dataLength).array();
        hexBuilder.append(byteArrayToHex(dataLengthBytes));

        // 将FDFormat转换为字节
        byte[] fdFormatBytes = ByteBuffer.allocate(4).putInt(FDFormat).array();
        hexBuilder.append(byteArrayToHex(fdFormatBytes));

        // 将unused_2转换为字节
        byte[] unused2Bytes = ByteBuffer.allocate(4).putInt(unused_2).array();
        hexBuilder.append(byteArrayToHex(unused2Bytes));

        // 添加data字段的十六进制表示
        hexBuilder.append(byteArrayToHex(data));

        return hexBuilder.toString();
    }

    // 辅助方法：将字节数组转换为十六进制字符串
    private String byteArrayToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

//    // 将十六进制字符串转换为字节数组
//    static byte[] hexStringToByteArray(String s) {
//        s = s.replaceAll("\\s+", ""); // 移除所有空白字符
//        int len = s.length();
//        byte[] data = new byte[len / 2];
//        for (int i = 0; i < len; i += 2) {
//            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
//                    + Character.digit(s.charAt(i+1), 16));
//        }
//        return data;
//    }

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

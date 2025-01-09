package com.example.testcdc;

import android.nfc.Tag;
import android.util.Log;
import android.util.Pair;

import com.example.testcdc.MiCAN.ShowCANMsg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

//public class NoLockShowCANBuffer {
//
//    private final int capacity;
//    private final ShowCANMsg[] buffer;
//
//    // 读指针
//    private volatile int readIndex = 0;
//    // 写指针
//    private volatile int writeIndex = 0;
//
//    public NoLockShowCANBuffer(int size) {
//        Log.e("MICAN_BUFFER","============");
//        capacity = size;
//        buffer = new ShowCANMsg[size];
//    }
//
//    public void clear()
//    {
//        readIndex = 0;
//        writeIndex = 0;
//    }
//
//
//    public boolean write(ShowCANMsg value) {
//        if(isFull()) return false;
//        buffer[writeIndex] = value;
//        writeIndex = (writeIndex + 1) % capacity;
//        return true;
//    }
//
//    public boolean write_deepcopy(ShowCANMsg value, boolean overrideFlag) {
//
//        if(isFull())
//        {
//            if(!overrideFlag) {
//                return false;
//            }else {
//                // 覆盖独写，将readIndex指针往前偏1
//                readIndex = nextIndex(readIndex);
//            }
//        }
//        if (buffer[writeIndex] == null)
//        {
//            buffer[writeIndex] = new ShowCANMsg();
//        }
//        buffer[writeIndex].setSqlId(value.getSqlId());
//        buffer[writeIndex].setTimestamp(value.getTimestamp());
//        buffer[writeIndex].setChannel(value.getChannel());
//        buffer[writeIndex].setArbitrationId(value.getArbitrationId());
//        buffer[writeIndex].setName(value.getName());
//        buffer[writeIndex].setCanType(value.getCanType());
//        buffer[writeIndex].setDir(value.getDir());
//        buffer[writeIndex].setDlc(value.getDlc());
//        buffer[writeIndex].setData(Arrays.copyOf(value.getData(),value.getData().length));
//        buffer[writeIndex].setParsedData(value.getParsedData());
//
//        writeIndex = (writeIndex + 1) % capacity;
//        return true;
//    }
//
//    public int readBuffer(ShowCANMsg[] values,int num) {
//        int curNum = size();
//        int readNum = num > curNum ? curNum : num;
//        for(int i = 0;i<readNum;i++)
//        {
//            values[i] =  buffer[readIndex];
//            readIndex = (readIndex + 1) % capacity;
//        }
//        return readNum;
//    }
//
//    public ShowCANMsg read() {
//        if(isEmpty()) return null;
//        ShowCANMsg t =  buffer[readIndex];
//        readIndex = (readIndex + 1) % capacity;
//        return t;
//    }
//
//    public List<ShowCANMsg> readAll() {
//        int curNum = size();
//        List<ShowCANMsg> t = new ArrayList<>(curNum);
//
//        for(int i = 0;i<curNum;i++)
//        {
//            t.add(buffer[readIndex]);
//            readIndex = (readIndex + 1) % capacity;
//        }
//        return t;
//    }
//
//    public List<ShowCANMsg> readLast(int num) {
//        // 不偏移读指针
//        int curNum = size();
//        List<ShowCANMsg> t = new ArrayList<>(curNum);
//
//        // 判断当前是否有最近的num条数据
//        int lastNum = Math.min(curNum,num);
//        int startIndex = writeIndex > lastNum ? writeIndex - lastNum : capacity + writeIndex - lastNum;
//        for(int i = startIndex ;i< startIndex + lastNum ;i++)
//        {
//            t.add(buffer[i % capacity]);
//        }
//        return t;
//    }
//
//    public List<ShowCANMsg> readBySqlId(long sqlId, int num) {
//
//        // 如果没数据,直接返回空
//        if(isEmpty()) return new ArrayList<>();
//        // 从readIndex指针进行sqlId判断
//        // 不偏移读指针
//        int curNum = size();
//        List<ShowCANMsg> t = new ArrayList<>(curNum);
//
//        long readIndexSqlId = buffer[readIndex].getSqlId();
//        // 没有历史数据了，返回空
//        if(readIndexSqlId > sqlId ) return new ArrayList<>();
//
//        // 截取readIndex 到
//        int readNum =  (int)(sqlId - readIndexSqlId);
//        int startIndex =
//        for(int i = readIndex; i< readIndex + readNum;i++)
//        {
//            t.add(buffer[i % capacity]);
//        }
//
//
//
//        return t;
//    }
//
//
//
//
//
//    private boolean isFull() {
//        return nextIndex(writeIndex) == readIndex;
//    }
//
//    private boolean isEmpty() {
//        return writeIndex == readIndex;
//    }
//
//    public int size()
//    {
//        if(isEmpty()) return 0;
//        if(writeIndex > readIndex) {
//            return writeIndex -readIndex;
//        } else {
//            return writeIndex -readIndex + capacity;
//        }
//    }
//
//    private int remain()
//    {
//        return capacity - size();
//
//    }
//
//    private int nextIndex(int current) {
//        return (current + 1) % capacity;
//    }
//
//    public int getCapacity() {
//        return capacity;
//    }
//}

public class NoLockShowCANBuffer {

    private static final String TAG = "MICAN_NoLockShowCANBuffer";

    private final int capacity;
    private final ShowCANMsg[] buffer;

    // 读指针
    private volatile long readIndex = 0;
    // 写指针
    private volatile long writeIndex = 0;

    public NoLockShowCANBuffer(int size) {
        capacity = size;
        buffer = new ShowCANMsg[size];
    }

    public void clear()
    {
        readIndex = 0;
        writeIndex = 0;
    }

    private int modifyIndex(long index)
    {
        return (int)(index % capacity);
    }

    public boolean write(ShowCANMsg value) {
        if(isFull()) return false;
        buffer[modifyIndex(writeIndex)] = value;
        writeIndex += 1;
        return true;
    }

    public boolean write_deepcopy(ShowCANMsg value, boolean overrideFlag) {

        if(isFull())
        {
            if(!overrideFlag) {
                return false;
            }else {
                // 覆盖独写，将readIndex指针往前偏1
                readIndex += 1;
            }
        }
        int tmpIndex = modifyIndex(writeIndex);
        if (buffer[tmpIndex] == null)
        {
            buffer[tmpIndex] = new ShowCANMsg();
        }
        buffer[tmpIndex].setSqlId(value.getSqlId());
        buffer[tmpIndex].setTimestamp(value.getTimestamp());
        buffer[tmpIndex].setChannel(value.getChannel());
        buffer[tmpIndex].setArbitrationId(value.getArbitrationId());
        buffer[tmpIndex].setName(value.getName());
        buffer[tmpIndex].setCanType(value.getCanType());
        buffer[tmpIndex].setDir(value.getDir());
        buffer[tmpIndex].setDlc(value.getDlc());
        buffer[tmpIndex].setData(Arrays.copyOf(value.getData(),value.getData().length));
        buffer[tmpIndex].setParsedData(value.getParsedData());

        writeIndex += 1;
        return true;
    }

    public List<ShowCANMsg> readLast(int num) {
        // 不偏移读指针
        int curNum = size();
        List<ShowCANMsg> t = new ArrayList<>(curNum);

        // 判断当前是否有最近的num条数据
        int readNum = Math.min(curNum,num);
        long startIndex = writeIndex - readNum;
        for(int i = 0 ;i< readNum ;i++)
        {
            t.add(buffer[modifyIndex(startIndex+i)]);
        }
        return t;
    }

    public Pair<List<ShowCANMsg>, Long> readBySqlId(long sqlId, int num) {

        Log.e("===","sqlId: " + sqlId + "num: " +num);


        long minSqlId = buffer[modifyIndex(readIndex)].getSqlId();
        long maxSqlId = buffer[modifyIndex(writeIndex-1)].getSqlId();

        long readStartSqlId = sqlId - num;
        long readEndSqlId = sqlId - 1;
        Log.e("===","readIndex: " + readIndex + " writeIndex: " + writeIndex);
        Log.e("===","minSqlId: " + minSqlId + " maxSqlId: " + maxSqlId);

        int curNum = size();
        List<ShowCANMsg> t = new ArrayList<>(curNum);
        if(sqlId <= readIndex)
        {
            Log.e(TAG,"该sqlId 已被覆盖,返回空数据");
            return new Pair<>(t,sqlId);
        }


        int readNum = (int)Math.min(sqlId-readIndex,num);
        long startIndex = Math.max(readIndex,sqlId-num);

        for(int i = 0; i< readNum;i++)
        {
            t.add(buffer[modifyIndex(startIndex+i)]);
        }



        return new Pair<>(t,startIndex);
    }

    private boolean isFull() {
        return size() == capacity;
    }

    private boolean isEmpty() {
        return writeIndex == readIndex;
    }

    public int size()
    {
       return (int)(writeIndex - readIndex);
    }

    public int getCapacity() {
        return capacity;
    }
}
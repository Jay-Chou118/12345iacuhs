package com.example.testcdc;

import android.util.Log;

import com.example.testcdc.MiCAN.ShowCANMsg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NoLockShowCANBuffer {

    private final int capacity;
    private final ShowCANMsg[] buffer;
    private volatile int readIndex = 0;
    private volatile int writeIndex = 0;

    public NoLockShowCANBuffer(int size) {
        Log.e("MICAN_BUFFER","============");
        capacity = size;
        buffer = new ShowCANMsg[size];
    }

    public void clear()
    {
        readIndex = 0;
        writeIndex = 0;
    }


    public boolean write(ShowCANMsg value) {
        if(isFull()) return false;
        buffer[writeIndex] = value;
        writeIndex = (writeIndex + 1) % capacity;
        return true;
    }

    public boolean write_deepcopy(ShowCANMsg value) {
        if(isFull()) return false;
        if (buffer[writeIndex] == null)
        {
            buffer[writeIndex] = new ShowCANMsg();
        }
        buffer[writeIndex].setSqlId(value.getSqlId());
        buffer[writeIndex].setTimestamp(value.getTimestamp());
        buffer[writeIndex].setChannel(value.getChannel());
        buffer[writeIndex].setArbitrationId(value.getArbitrationId());
        buffer[writeIndex].setName(value.getName());
        buffer[writeIndex].setCanType(value.getCanType());
        buffer[writeIndex].setDir(value.getDir());
        buffer[writeIndex].setDlc(value.getDlc());
        buffer[writeIndex].setData(Arrays.copyOf(value.getData(),value.getData().length));
        buffer[writeIndex].setParsedData(value.getParsedData());

        writeIndex = (writeIndex + 1) % capacity;
        return true;
    }

    public int readBuffer(ShowCANMsg[] values,int num) {
        int curNum = size();
        int readNum = num > curNum ? curNum : num;
        for(int i = 0;i<readNum;i++)
        {
            values[i] =  buffer[readIndex];
            readIndex = (readIndex + 1) % capacity;
        }
        return readNum;
    }

    public ShowCANMsg read() {
        if(isEmpty()) return null;
        ShowCANMsg t =  buffer[readIndex];
        readIndex = (readIndex + 1) % capacity;
        return t;
    }

    public List<ShowCANMsg> readAll() {
        int curNum = size();
        List<ShowCANMsg> t = new ArrayList<>(curNum);

        for(int i = 0;i<curNum;i++)
        {
            t.add(buffer[readIndex]);
            readIndex = (readIndex + 1) % capacity;
        }
        return t;
    }

    private boolean isFull() {
        return nextIndex(writeIndex) == readIndex;
    }

    private boolean isEmpty() {
        return writeIndex == readIndex;
    }

    public int size()
    {
        if(isEmpty()) return 0;
        if(writeIndex > readIndex) {
            return writeIndex -readIndex;
        } else {
            return writeIndex -readIndex + capacity;
        }
    }

    private int remain()
    {
        return capacity - size();

    }

    private int nextIndex(int current) {
        return (current + 1) % capacity;
    }

    public int getCapacity() {
        return capacity;
    }
}
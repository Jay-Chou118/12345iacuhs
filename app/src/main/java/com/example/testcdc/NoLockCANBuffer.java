package com.example.testcdc;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NoLockCANBuffer {

    private final int capacity;
    private final CanMessage[] buffer;
    private volatile int readIndex = 0;
    private volatile int writeIndex = 0;

    public NoLockCANBuffer(int size) {
        Log.e("MICAN_BUFFER","============");
        capacity = size;
        buffer = new CanMessage[size];
    }

    public void clear()
    {
        readIndex = 0;
        writeIndex = 0;
    }
    public boolean write(CanMessage value) {
        if(isFull()) return false;
        buffer[writeIndex] = value;
        writeIndex = (writeIndex + 1) % capacity;
        return true;
    }

    public boolean write_deepcopy(CanMessage value) {
        if(isFull()) return false;
        if (buffer[writeIndex] == null)
        {
            buffer[writeIndex] = new CanMessage();
        }
        buffer[writeIndex].CAN_ID = value.CAN_ID;
        buffer[writeIndex].index = value.index;
        buffer[writeIndex].CAN_TYPE = value.CAN_TYPE;
        buffer[writeIndex].BUS_ID = value.BUS_ID;
        buffer[writeIndex].dataLength = value.dataLength;
        buffer[writeIndex].direct = value.direct;
        buffer[writeIndex].timestamp = value.timestamp;
        buffer[writeIndex].data = Arrays.copyOf(value.data,value.data.length);
        writeIndex = (writeIndex + 1) % capacity;
        return true;
    }

    public int readBuffer(CanMessage[] values,int num) {
        int curNum = size();
        int readNum = num > curNum ? curNum : num;
        for(int i = 0;i<readNum;i++)
        {
            values[i] =  buffer[readIndex];
            readIndex = (readIndex + 1) % capacity;
        }
        return readNum;
    }

    public CanMessage read() {
        if(isEmpty()) return null;
        CanMessage t =  buffer[readIndex];
        readIndex = (readIndex + 1) % capacity;
        return t;
    }

    public List<CanMessage> readAll() {
        int curNum = size();
        List<CanMessage> t = new ArrayList<>(curNum);

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
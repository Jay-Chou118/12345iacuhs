package com.example.testcdc;

import android.util.Log;

public class NoLockBuffer {

    private final int capacity;
    private final byte[] buffer;
    private volatile int readIndex = 0;
    private volatile int writeIndex = 0;

    public NoLockBuffer(int size) {
        capacity = size;
        buffer = new byte[size];
    }

    public boolean writeBuffer(byte[] values) {
        if (values.length > remain()) {
            return false;
        }
        for(byte value:values)
        {
            // 我们认为缓冲区满的概率为0
            buffer[writeIndex] = value;
            writeIndex = (writeIndex + 1) % capacity;
        }
        return true;
    }

    public boolean writeBuffer(byte[] values,int num) {
        if (num > remain()) {
            return false;
        }
        for(int i = 0;i<num;i++)
        {
            // 我们认为缓冲区满的概率为0
            buffer[writeIndex] = values[i];
            writeIndex = (writeIndex + 1) % capacity;
        }
        return true;
    }

    public boolean readBuffer(byte[] values,int num) {
        if(num > size()) return false;
        for(int i = 0;i<num;i++)
        {
            values[i] =  buffer[readIndex];
            readIndex = (readIndex + 1) % capacity;
        }

        return true;
    }

    public boolean readBuffer(byte[] values) {
        if(values.length > size()) return false;
        for(int i = 0;i<values.length;i++)
        {
            values[i] =  buffer[readIndex];
            readIndex = (readIndex + 1) % capacity;
        }

        return true;
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
}
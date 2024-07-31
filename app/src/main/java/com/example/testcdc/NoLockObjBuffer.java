package com.example.testcdc;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class NoLockObjBuffer<T> {

    private final int capacity;
    private final Object[] buffer;
    private volatile int readIndex = 0;
    private volatile int writeIndex = 0;

    public NoLockObjBuffer(int size) {
        Log.e("MICAN_BUFFER","============");
        capacity = size;
        buffer = new Object[size];
    }

    public boolean write(T value) {
        if(isFull()) return false;
        buffer[writeIndex] = value;
        writeIndex = (writeIndex + 1) % capacity;
        return true;
    }

    public int readBuffer(T[] values,int num) {
        int curNum = size();
        int readNum = num > curNum ? curNum : num;
        for(int i = 0;i<readNum;i++)
        {
            values[i] =  (T) buffer[readIndex];
            readIndex = (readIndex + 1) % capacity;
        }
        return readNum;
    }

    public List<T> readAll() {
        int curNum = size();
        List<T> t = new ArrayList<>(curNum);

        for(int i = 0;i<curNum;i++)
        {
            t.add( (T)buffer[readIndex]);
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
}
package com.example.testcdc.httpServer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

public class FlaskService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 启动Flask
        Python py = Python.getInstance();
        PyObject pyObject = py.getModule("test");
        new Thread(() -> pyObject.callAttr("run")).start(); // 在新线程中运行
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

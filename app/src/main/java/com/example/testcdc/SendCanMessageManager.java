package com.example.testcdc;

import java.util.ArrayList;

public class SendCanMessageManager {

    private ArrayList<SendCanMessage> mPeriodSendConfig = new ArrayList<>();


    public void setupPeriodicMessages() {

    }

    // 添加一个新的 SendCanMessage 对象
    public void addSendCanMessage(SendCanMessage sendCan) {
        mPeriodSendConfig.add(sendCan);
    }

    // 清空所有的周期性发送配置
    public void clearPeriodSendConfig() {
        mPeriodSendConfig.clear();
    }

    // 获取所有周期性发送配置的方法
    public ArrayList<SendCanMessage> getPeriodSendConfig() {
        return mPeriodSendConfig;
    }

    @Override
    public String toString() {
        return "BBBBB current g_sent_list  SendCanMessageManager{" +
                "mPeriodSendConfig=" + mPeriodSendConfig +
                '}';
    }
}

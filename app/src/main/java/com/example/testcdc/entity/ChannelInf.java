package com.example.testcdc.entity;

//
//我真不知道怎么放回这个两个列的数据了
//只能自己定义一个了
//
public class ChannelInf {
    private String bus_name;
    private int bus_id;

    public String getBus_name() {
        return bus_name;
    }

    public void setBus_name(String bus_name) {
        this.bus_name = bus_name;
    }

    public int getBus_id() {
        return bus_id;
    }

    public void setBus_id(int bus_id) {
        this.bus_id = bus_id;
    }
}

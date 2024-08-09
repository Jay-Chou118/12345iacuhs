package com.example.testcdc.MiCAN;

import java.util.List;

public class ShowSignal {

    private String name;

    private int busId;

    private int canId;

    List<Double> times;

    List<Double> values;

    public ShowSignal(String name, int busId, int canId, List<Double> times, List<Double> values) {
        this.name = name;
        this.busId = busId;
        this.canId = canId;
        this.times = times;
        this.values = values;
    }

    public ShowSignal() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBusId(int busId) {
        this.busId = busId;
    }

    public void setCanId(int canId) {
        this.canId = canId;
    }

    public void setTimes(List<Double> times) {
        this.times = times;
    }

    public void setValues(List<Double> values) {
        this.values = values;
    }
}

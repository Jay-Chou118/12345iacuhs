package com.example.testcdc.MiCAN;

import java.util.List;

public class DataWrapper {

    double start_time;
    List<ShowCANMsg> frame_data;

    List<ShowSignal> signal_data;


    public double getStart_time() {
        return start_time;
    }

    public void setStart_time(double start_time) {
        this.start_time = start_time;
    }

    public List<ShowCANMsg> getFrame_data() {
        return frame_data;
    }

    public void setFrame_data(List<ShowCANMsg> frame_data) {
        this.frame_data = frame_data;
    }

    public void setSignal_data(List<ShowSignal> signal_data) {
        this.signal_data = signal_data;
    }
}

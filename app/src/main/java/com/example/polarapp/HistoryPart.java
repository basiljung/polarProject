package com.example.polarapp;

import java.io.Serializable;

public abstract class HistoryPart implements Serializable {
    private String type = new String();
    private String timeStamp = new String();
    private String length = new String();
    //private double length;

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getTimeStamp() {
        return timeStamp;
    }
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getLength() {
        return length;
    }
    public void setLength (String length) {
        this.length = length;
    }


    /*
    public double getLength() {
        return length;
    }
    public void setLength(double length) {
        this.length = length;
    }
*/
    public HistoryPart() {

    }
}

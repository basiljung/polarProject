package com.example.polarapp.activity;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

public class ActivityData implements Serializable, Comparable<ActivityData> {
    private String type = "";
    private Timestamp timestamp = null;
    private int time = 0; // in  seconds
    private double avgHR = 0;
    private int distance = 0; // in meters
    private double avgSpeed = 0;
    private int interval = 0;
    private List<LatLng> locationPoints = null;
    private int deepSleepTime = 0;
    private int nightMoves = 0;

    public ActivityData() {}

    public ActivityData (ActivityData activityData) {
        this.type = activityData.getType();
        this.timestamp = activityData.getTimestamp();
        this.time = activityData.getTime();
        this.avgHR = activityData.getAvgHR();
        this.distance = activityData.getDistance();
        this.avgSpeed = activityData.getAvgSpeed();
        this.interval = activityData.getInterval();
        this.locationPoints = activityData.getLocationPoints();
        this.deepSleepTime = activityData.getDeepSleepTime();
        this.nightMoves = activityData.getNightMoves();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public double getAvgHR() {
        return avgHR;
    }

    public void setAvgHR(double avgHR) {
        this.avgHR = avgHR;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public double getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(double avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public List<LatLng> getLocationPoints() {
        return locationPoints;
    }

    public void setLocationPoints(List<LatLng> locationPoints) {
        this.locationPoints = locationPoints;
    }

    public int getDeepSleepTime() {
        return deepSleepTime;
    }

    public void setDeepSleepTime(int deepSleepTime) {
        this.deepSleepTime = deepSleepTime;
    }

    public int getNightMoves() {
        return nightMoves;
    }

    public void setNightMoves(int nightMoves) {
        this.nightMoves = nightMoves;
    }

    @Override
    public int compareTo(ActivityData activityData) {
        int lastCmp = timestamp.compareTo(activityData.timestamp);
        return (lastCmp != 0 ? lastCmp : timestamp.compareTo(activityData.timestamp));
    }
}

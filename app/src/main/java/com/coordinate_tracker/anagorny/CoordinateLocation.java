package com.coordinate_tracker.anagorny;

/**
 * Created by nagorny on 01.05.17.
 */

public class CoordinateLocation {
    private double longitude;
    private double latitude;

    private double speed;
    private double accuracy;

    public CoordinateLocation(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public CoordinateLocation(double longitude, double latitude, double speed, double accuracy) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.speed = speed;
        this.accuracy = accuracy;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }
}

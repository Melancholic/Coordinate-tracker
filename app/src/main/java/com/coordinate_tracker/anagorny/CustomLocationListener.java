package com.coordinate_tracker.anagorny;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import java.io.FileOutputStream;

import static android.support.v4.app.ActivityCompat.startActivity;

/**
 * Created by sosnov on 20.03.15.
 */
public class CustomLocationListener implements LocationListener {
    private static final String TAG = "CustomLocationListener";

    @Override
    public void onLocationChanged(Location location) {
        Context appCtx = CoordinateTracker.getAppContext();

        double latitude, longitude, speed, accuracy;


        latitude = location.getLatitude();
        longitude = location.getLongitude();
        speed = location.getSpeed();
        accuracy = location.getAccuracy();

        Intent filterRes = new Intent();
        filterRes.setAction("coordinate.tracker.intent.action.LOCATION");
        filterRes.putExtra("latitude", latitude);
        filterRes.putExtra("longitude", longitude);
        filterRes.putExtra("speed", speed);
        filterRes.putExtra("accuracy", accuracy);
        appCtx.sendBroadcast(filterRes);
        Log.d(this.getClass().getName(), "latitude: " + latitude);
        Log.d(this.getClass().getName(), "longitude: " + longitude);
        Log.d(this.getClass().getName(), "speed: " + speed);
        Log.d(this.getClass().getName(),"accuracy: "+accuracy);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "GPS eanbled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "GPS disabled");
    }
}

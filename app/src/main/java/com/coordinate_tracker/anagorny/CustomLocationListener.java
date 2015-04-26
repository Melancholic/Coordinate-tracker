package com.coordinate_tracker.anagorny;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by sosnov on 20.03.15.
 */
public class CustomLocationListener implements LocationListener {
    private static final String TAG = "CustomLocationListener";
    public static final String LAST_LATITUDE_TAG = "last_latitude";
    public static final String LAST_LONGITUDE_TAG = "last_longitude";
    public static final String LAST_ACCURACY_TAG = "last_accuracy";
    public static final String LAST_TIME_TAG = "last_time";

    @Override
    public void onLocationChanged(Location location) {
        Context appCtx = CoordinateTracker.getAppContext();
        Log.d("TEST CONT cstomlocreciv", appCtx.toString());
        double latitude, longitude, speed, accuracy;
        long time;
        Location last_loc = new Location("last location");
        last_loc.setLatitude(Double.parseDouble(StorageAdapter.get(appCtx).getUsersStorage().getString(LAST_LATITUDE_TAG, String.valueOf(location.getLatitude()))));
        last_loc.setLongitude(Double.parseDouble(StorageAdapter.get(appCtx).getUsersStorage().getString(LAST_LONGITUDE_TAG, String.valueOf(location.getLongitude()))));
        last_loc.setAccuracy(Float.parseFloat(StorageAdapter.get(appCtx).getUsersStorage().getString(LAST_ACCURACY_TAG, String.valueOf(location.getAccuracy()))));
        long last_loc_time = Long.parseLong(StorageAdapter.get(appCtx).getUsersStorage().getString(LAST_TIME_TAG, "0"));
        Log.e(this.getClass().getName(), "LAST:   " + last_loc.getLatitude() + " " + last_loc.getLongitude());
        Log.e(this.getClass().getName(), "Distance:  " + location.distanceTo(last_loc));
        // if(location.getAccuracy()<30){
        if (location.getAccuracy() < 10 || location.distanceTo(last_loc) > (location.getAccuracy() + last_loc.getAccuracy()) * 2) { //(location.getAccuracy()+last_loc.getAccuracy())*2
            //    if(isBetterLocation(location,last_loc)){
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            speed = location.getSpeed();
            accuracy = location.getAccuracy();
            time = Calendar.getInstance(TimeZone.getTimeZone("utc")).getTimeInMillis();

            Intent filterRes = new Intent();
            filterRes.setAction("coordinate.tracker.intent.action.LOCATION");
            filterRes.putExtra("latitude", latitude);
            filterRes.putExtra("longitude", longitude);
            filterRes.putExtra("speed", speed);
            filterRes.putExtra("accuracy", accuracy);
            filterRes.putExtra("time", time);
            filterRes.putExtra("need_new_track", (((time - last_loc_time) * 1.0 / 60000) >= 15));
            appCtx.sendBroadcast(filterRes);
            Log.d(this.getClass().getName(), "latitude: " + latitude);
            Log.d(this.getClass().getName(), "longitude: " + longitude);
            Log.d(this.getClass().getName(), "speed: " + speed);
            Log.d(this.getClass().getName(), "accuracy: " + accuracy);
            Log.d(this.getClass().getName(), "provider: " + location.getProvider());
            saveLastLoc(latitude, longitude, accuracy, time);
            Log.d(this.getClass().getName(), "Time different: " + ((time - last_loc_time) * 1.0 / 60000));
        } else {
            Log.d(this.getClass().getName(), "Accuracy is very hight, location not updated");
        }
    }

    private void saveLastLoc(double latitude, double longitude, double accuracy, long time) {
        Context appCtx = CoordinateTracker.getAppContext();
        StorageAdapter.get(appCtx).getUsersStorage().edit()
                .putString(LAST_LATITUDE_TAG, String.valueOf(latitude))
                .putString(LAST_LONGITUDE_TAG, String.valueOf(longitude))
                .putString(LAST_ACCURACY_TAG, String.valueOf(accuracy))
                .putString(LAST_TIME_TAG, String.valueOf(time)).commit();
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

    private static final int FIVE_SECONDS = 1000 * 5;

    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > FIVE_SECONDS;
        boolean isSignificantlyOlder = timeDelta < -FIVE_SECONDS;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

}

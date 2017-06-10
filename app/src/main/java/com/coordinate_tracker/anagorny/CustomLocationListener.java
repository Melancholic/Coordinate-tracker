package com.coordinate_tracker.anagorny;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by sosnov on 20.03.15.
 */
public class CustomLocationListener implements LocationListener {
    private final String LOG_TAG = CoordinateTracker.LOG_TAG +": "+this.getClass().getSimpleName();
    public static final String LAST_LATITUDE_TAG = "last_latitude";
    public static final String LAST_LONGITUDE_TAG = "last_longitude";
    public static final String LAST_ACCURACY_TAG = "last_accuracy";
    public static final String LAST_TIME_TAG = "last_time";
    public static final String LAST_CITY_TAG = "last_city";
    public static final String LAST_SPEED_TAG = "last_speed";

    private Geocoder geocoder = new Geocoder(CoordinateTracker.getAppContext(), Locale.getDefault());

    private SharedPreferences userStore = StorageAdapter.usersStorage();

    @Override
    public void onLocationChanged(Location location) {
        Context appCtx = CoordinateTracker.getAppContext();
        Log.d(LOG_TAG, appCtx.toString());
        double latitude, longitude;
        float speed;
        short accuracy;
        long time;
        Location last_loc = new Location("last location");
        last_loc.setLatitude(Double.parseDouble(StorageAdapter.get(appCtx).getUsersStorage().getString(LAST_LATITUDE_TAG, String.valueOf(location.getLatitude()))));
        last_loc.setLongitude(Double.parseDouble(StorageAdapter.get(appCtx).getUsersStorage().getString(LAST_LONGITUDE_TAG, String.valueOf(location.getLongitude()))));
        last_loc.setAccuracy(Float.parseFloat(StorageAdapter.get(appCtx).getUsersStorage().getString(LAST_ACCURACY_TAG, String.valueOf(location.getAccuracy()))));
        long last_loc_time = Long.parseLong(StorageAdapter.get(appCtx).getUsersStorage().getString(LAST_TIME_TAG, "0"));
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        speed = location.getSpeed();
        accuracy = (short)Math.round(location.getAccuracy());
        time = Calendar.getInstance(TimeZone.getTimeZone("utc")).getTimeInMillis();

        // TODO more conditions dor filters
        if (speed > 0) {
            Intent filterRes = new Intent();
            filterRes.setAction(CoordinateTracker.NEW_LOCATION_INTENT);
            filterRes.putExtra("latitude", latitude);
            filterRes.putExtra("longitude", longitude);
            filterRes.putExtra("speed", speed);
            filterRes.putExtra("accuracy", accuracy);
            filterRes.putExtra("time", time);
            filterRes.putExtra("need_new_track", (((time - last_loc_time) * 1.0 / 60000) >= 15));
            appCtx.sendBroadcast(filterRes);
            saveLastLoc(latitude, longitude, accuracy, time, speed);
            Log.d(LOG_TAG, "Geodata received and successfully sended to handler");
        } else {
            Log.d(LOG_TAG, "Geodata received, but speed is zero, skip...");
        }
    }

    private void saveLastLoc(double latitude, double longitude, double accuracy, long time, float speed) {
        Context appCtx = CoordinateTracker.getAppContext();
        String city;
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            city = addresses.get(0).getLocality();
        } catch (IOException e) {
            city = null;
        }
        userStore.edit()
                .putString(LAST_LATITUDE_TAG, String.valueOf(latitude))
                .putString(LAST_LONGITUDE_TAG, String.valueOf(longitude))
                .putString(LAST_ACCURACY_TAG, String.valueOf(accuracy))
                .putString(LAST_CITY_TAG, city)
                .putFloat(LAST_SPEED_TAG, speed)
                .putString(LAST_TIME_TAG, String.valueOf(time)).apply();
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(LOG_TAG, "GPS eanbled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(LOG_TAG, "GPS disabled");
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
            //Новые кооординаты, всегда лучше отсуствующих
            return true;
        }
        // Новые координаты новее?
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > FIVE_SECONDS;
        boolean isSignificantlyOlder = timeDelta < -FIVE_SECONDS;
        boolean isNewer = timeDelta > 0;

        //Если разница более 5 сек, использовать новые
        if (isSignificantlyNewer) {
            return true;
            // Если новые координаты старше 5 минут, то они устаревшие
        } else if (isSignificantlyOlder) {
            return false;
        }
        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Новые координаты точнее старого?
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        //Определение окончательного результата,
        //опираясь на точность и актуальность
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }


    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

}

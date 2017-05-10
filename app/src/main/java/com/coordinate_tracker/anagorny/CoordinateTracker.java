package com.coordinate_tracker.anagorny;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Created by sosnov on 20.03.15.
 */
public class CoordinateTracker extends Application {
    private static Context context;
    public final static String PACKAGE_NAME = "com.anagorny.coordinate_tracker";
    public static final String CONNECTED_STATUS_TAG = "CONNECTED_STATUS";

    public static final String CONNECTION_ON_INTENT = "coordinate.tracker.intent.action.INET_ON";
    public static final String CONNECTION_OFF_INTENT = "coordinate.tracker.intent.action.INET_OFF";
    public static final String NEW_LOCATION_INTENT = "coordinate.tracker.intent.action.LOCATION";
    public static final String LOG_TAG = "Coordinate-tracker";


    public static boolean isConnected() {
       return StorageAdapter.usersStorage().getBoolean(CONNECTED_STATUS_TAG, false);
    }

    public static boolean isTokenEmpty() {
        return StorageAdapter.usersStorage().getString(Configuration.AUTH_TOKEN_KEY_NAME, "") == null ||
                StorageAdapter.usersStorage().getString(Configuration.AUTH_TOKEN_KEY_NAME, "").isEmpty();
    }

    public synchronized static void setConnected(boolean connectedStatus) {
        StorageAdapter.usersStorage().edit().putBoolean(CONNECTED_STATUS_TAG, connectedStatus).apply();
    }

//    public static boolean isConnected(Context context) {
//        return StorageAdapter.get(context).getUsersStorage().getBoolean(CONNECTED_STATUS_TAG, false);
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        CoordinateTracker.context=getApplicationContext();
    }

    public static Context getAppContext() {
        return CoordinateTracker.context;
    }

}


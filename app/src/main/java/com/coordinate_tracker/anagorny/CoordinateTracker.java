package com.coordinate_tracker.anagorny;

import android.app.Application;
import android.content.Context;

/**
 * Created by sosnov on 20.03.15.
 */
public class CoordinateTracker extends Application {
    private static Context context;
    public final static String PACKAGE_NAME = "com.anagorny.coordinate_tracker";
    public static final String CONNECTED_STATUS_TAG = "CONNECTED_STATUS";

    public static boolean isConnected() {
        return StorageAdapter.usersStorage().getBoolean(CONNECTED_STATUS_TAG, false);
    }

    public static boolean isConnected(Context context) {
        return StorageAdapter.get(context).getUsersStorage().getBoolean(CONNECTED_STATUS_TAG, false);
    }

    @Override
    public void onCreate() {
        CoordinateTracker.context=getApplicationContext();
    }

    public static Context getAppContext() {
        return CoordinateTracker.context;
    }

}


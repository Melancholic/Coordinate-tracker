package com.coordinate_tracker.anagorny;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by sosnov on 20.03.15.
 */
public class CoordinateTracker extends Application {
    private static Context context;

    public static SharedPreferences userStore;

    @Override
    public void onCreate() {
        CoordinateTracker.context=getApplicationContext();
        userStore = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
    }

    public static Context getAppContext() {
        return CoordinateTracker.context;
    }
}

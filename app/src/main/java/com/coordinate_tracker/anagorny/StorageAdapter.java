package com.coordinate_tracker.anagorny;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by sosnov on 17.04.15.
 */
public class StorageAdapter {
    private final String LOG_TAG = "COORDINATE";
    static private StorageAdapter instance;
    private final SharedPreferences locationStore;
    private SharedPreferences userStore;
    private Context context;

    StorageAdapter(Context context) {
        this.context = context;
        userStore = context.getSharedPreferences(CoordinateTracker.PACKAGE_NAME + "_preferences", Application.MODE_PRIVATE);
        locationStore = context.getSharedPreferences(CoordinateTracker.PACKAGE_NAME + "_locations", Application.MODE_PRIVATE);
    }

    static public StorageAdapter get(Context context) {
        instance = new StorageAdapter(context);
        return instance;
    }

    static public StorageAdapter get() {
        instance = new StorageAdapter(CoordinateTracker.getAppContext());
        return instance;
    }

    public SharedPreferences getUsersStorage() {
        Log.d("COORDINATE", "Store CONTEXT" + context
        );
        return this.userStore;
    }

    static SharedPreferences usersStorage() {
        return get().getUsersStorage();
    }

    public SharedPreferences getLocationsStorage() {
        return this.locationStore;
    }
}

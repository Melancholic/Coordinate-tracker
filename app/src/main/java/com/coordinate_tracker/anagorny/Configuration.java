package com.coordinate_tracker.anagorny;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.app.ActivityManager.*;

/**
 * Created by sosnov on 17.03.15.
 */
public class Configuration {
    public static String ID= CoordinateTracker.userStore.getString(Configuration.UUID_TOKEN_KEY_NAME,"");
    public final static String PRIVATE_STORE_NAME="CurrentUser";
    public final static String AUTH_TOKEN_KEY_NAME="AuthToken";
    public final static String UUID_TOKEN_KEY_NAME="UUID";
}

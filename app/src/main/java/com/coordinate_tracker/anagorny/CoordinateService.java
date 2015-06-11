package com.coordinate_tracker.anagorny;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class CoordinateService extends Service implements INetCheckService.ConnectionServiceCallback {
    private final String LOG_TAG = "COORDINATE";
    public static SharedPreferences userStore;
    private Context context = CoordinateTracker.getAppContext();

    public CoordinateService() {
        super();
        userStore = StorageAdapter.usersStorage();
        if (token_empty()) {
            Log.e(LOG_TAG, "Token is empty!");
            stopSelf();
        }
    }

    private boolean token_empty() {
        return userStore.getString(Configuration.AUTH_TOKEN_KEY_NAME, "") == null || userStore.getString(Configuration.AUTH_TOKEN_KEY_NAME, "").isEmpty();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
        Log.d("TEST CONT", context.toString());
        startInetCheck();
        Intent filterRes = new Intent();
        filterRes.setAction("coordinate.tracker.intent.action.INET_ON");
        context.sendBroadcast(filterRes);
    }

    private void startInetCheck() {
        Intent intent = new Intent(context, INetCheckService.class);
        SharedPreferences.Editor editor = userStore.edit();
        // Interval in seconds
        editor.putInt(INetCheckService.TAG_INTERVAL, 30);
        // URL to ping
        editor.putString(INetCheckService.TAG_URL_PING, Configuration.getPingURL());
        // Name of the class that is calling this service
        editor.putString(INetCheckService.TAG_ACTIVITY_NAME, this.getClass().getName());
        editor.commit();
        // Starts the service
        startService(intent);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        someTask();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        StorageAdapter.usersStorage().edit().putBoolean(CoordinateTracker.CONNECTED_STATUS_TAG, false).commit();
        stopService(new Intent(this, INetCheckService.class));
        StorageAdapter.get(context).getUsersStorage().edit()
                .remove(CustomLocationListener.LAST_LATITUDE_TAG)
                .remove(CustomLocationListener.LAST_LONGITUDE_TAG)
                .remove(CustomLocationListener.LAST_ACCURACY_TAG)
                .remove(CustomLocationListener.LAST_TIME_TAG)

                .commit();
        Log.d(LOG_TAG, "onDestroy");
    }


    void someTask() {
        addLocationListener();
    }

    private void addLocationListener() {
        Thread triggerService = new Thread(new Runnable() {
            public void run() {
                try {
                    Looper.prepare();//Initialise the current thread as a looper.
                    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                    Criteria c = new Criteria();
                    c.setAccuracy(Criteria.ACCURACY_FINE);

                    // final String PROVIDER = LocationManager.GPS_PROVIDER;
                    final String PROVIDER = lm.getBestProvider(c, true);

                    CustomLocationListener ll = new CustomLocationListener();
                    lm.requestLocationUpdates(PROVIDER, 5000, 3, ll);
                    Log.d("LOC_SERVICE", "Service RUNNING!");
                    Looper.loop();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, "LocationThread");
        triggerService.start();
    }

    @Override
    public void hasInternetConnection() {
        CoordinateTracker app = (CoordinateTracker) context;
        if (!CoordinateTracker.isConnected()) {
            StorageAdapter.usersStorage().edit().putBoolean(CoordinateTracker.CONNECTED_STATUS_TAG, true).commit();
            Intent filterRes = new Intent();
            filterRes.setAction("coordinate.tracker.intent.action.INET_ON");
            context.sendBroadcast(filterRes);
        }
        Log.d("NET", "INET ON");
        Log.d(LOG_TAG, "NET  " + CoordinateTracker.isConnected());
    }

    @Override
    public void hasNoInternetConnection() {
        CoordinateTracker app = (CoordinateTracker) context;
        if (CoordinateTracker.isConnected()) {
            StorageAdapter.usersStorage().edit().putBoolean(CoordinateTracker.CONNECTED_STATUS_TAG, false).commit();
        }
        Log.d(LOG_TAG, "INET OFF");
        Log.d(LOG_TAG, "NET  " + CoordinateTracker.isConnected());
    }
}

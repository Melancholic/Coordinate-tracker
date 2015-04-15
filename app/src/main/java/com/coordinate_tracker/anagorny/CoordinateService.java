package com.coordinate_tracker.anagorny;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class CoordinateService extends Service {
    final String LOG_TAG = "CoordinateService";
    public static SharedPreferences userStore;


    public CoordinateService() {
        super();
        userStore = CoordinateTracker.userStore;
        if(token_empty()){
            Log.e(LOG_TAG,"Token is empty!");
            stopSelf();
        }
    }

    private boolean token_empty(){
        return userStore.getString(Configuration.AUTH_TOKEN_KEY_NAME,"")==null || userStore.getString(Configuration.AUTH_TOKEN_KEY_NAME,"").isEmpty();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        someTask();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }


    void someTask() {
        //TODO make task
        addLocationListener();
    }

    private void addLocationListener()
    {
        Thread triggerService = new Thread(new Runnable() {
            public void run() {
                try {
                    Looper.prepare();//Initialise the current thread as a looper.
                    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                    Criteria c = new Criteria();
                    c.setAccuracy(Criteria.ACCURACY_FINE);

                    final String PROVIDER = LocationManager.GPS_PROVIDER;//lm.getBestProvider(c, true);

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

}

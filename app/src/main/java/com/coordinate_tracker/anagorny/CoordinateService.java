package com.coordinate_tracker.anagorny;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class CoordinateService extends Service implements INetCheckService.ConnectionServiceCallback {
    private final String LOG_TAG = "COORDINATE";
    public  SharedPreferences userStore = StorageAdapter.usersStorage();
    private Context context = CoordinateTracker.getAppContext();
    private NotificationManager mNotificationManager;
    private Notification.Builder NotifyBuilder;
    private int NotifyID;
    private Timer notifyUpdaterTimer = null;

    public CoordinateService() {
        super();
        if (CoordinateTracker.isTokenEmpty()) {
            Log.e(LOG_TAG, "Token is empty!");
            stopSelf();
        }
    }

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
        Log.d(LOG_TAG, "onCreate " + this.toString());

        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotifyBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("status:");
        NotifyID = new AtomicInteger(0).incrementAndGet();

        Notification notification = NotifyBuilder.build();
        startForeground(NotifyID, notification);
    }



    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
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
        editor.apply();
        // Starts the service
        startService(intent);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        someTask();
        Intent filterRes = new Intent();
        filterRes.setAction(CoordinateTracker.CONNECTION_ON_INTENT);
        return super.onStartCommand(intent, flags, startId);
    }

    void someTask() {
        startInetCheck();
        updateNotify();
        addLocationListener();
    }

    public void onDestroy() {
        notifyUpdaterTimer.cancel();
        super.onDestroy();
        CoordinateTracker.setConnected(false);
        stopService(new Intent(this, INetCheckService.class));
        userStore.edit()
                .remove(CustomLocationListener.LAST_LATITUDE_TAG)
                .remove(CustomLocationListener.LAST_LONGITUDE_TAG)
                .remove(CustomLocationListener.LAST_ACCURACY_TAG)
                .remove(CustomLocationListener.LAST_TIME_TAG)
                .apply();
        Log.d(LOG_TAG, "onDestroy");
    }

    public CoordinateLocation getLastLocation() {
        CoordinateLocation lastLoc = new CoordinateLocation(
                userStore.getFloat(CustomLocationListener.LAST_LATITUDE_TAG, -1f),
                userStore.getFloat(CustomLocationListener.LAST_LONGITUDE_TAG, -1f)
        );
        return lastLoc;
    }

    class NotifyUpdater extends TimerTask {
        private SharedPreferences userStorage = StorageAdapter.usersStorage();

        public NotifyUpdater(SharedPreferences userStorage) {
            super();
            this.userStorage = userStorage;
        }

        @Override
        public void run() {
            IsNotifyNeedUpdate();
        }
    }

    private void updateNotify() {
        notifyUpdaterTimer = new Timer();
        notifyUpdaterTimer.scheduleAtFixedRate(new NotifyUpdater(userStore), 0, 3 * 1000);
    }

    //TODO
    private void IsNotifyNeedUpdate() {
        Log.d(LOG_TAG, "IsNotifyNeedUpdate " + this.toString());
        try {
            NotifyBuilder.setContentText("Service is work...");
            Log.e (LOG_TAG, CoordinateTracker.isConnected() ? "CONNECTED" : "DISCONNECTED");
            // Because the ID remains unchanged, the existing notification is
            // updated.
            mNotificationManager.notify(
                    NotifyID,
                    NotifyBuilder.build()
            );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
        if (!CoordinateTracker.isConnected()) {
            CoordinateTracker.setConnected(true);
            Intent filterRes = new Intent();
            filterRes.setAction(CoordinateTracker.CONNECTION_ON_INTENT);
            context.sendBroadcast(filterRes);
        }
        Log.d("NET", "INET ON");
        Log.d(LOG_TAG, "NET  " + CoordinateTracker.isConnected());
    }

    @Override
    public void hasNoInternetConnection() {
        if (CoordinateTracker.isConnected()) {
            CoordinateTracker.setConnected(false);
            Intent filterRes = new Intent();
            filterRes.setAction(CoordinateTracker.CONNECTION_OFF_INTENT);
            context.sendBroadcast(filterRes);
        }
        Log.d(LOG_TAG, "INET OFF");
        Log.d(LOG_TAG, "NET  " + CoordinateTracker.isConnected());

    }
}

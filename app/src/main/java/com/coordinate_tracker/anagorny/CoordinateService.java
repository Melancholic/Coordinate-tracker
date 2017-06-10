package com.coordinate_tracker.anagorny;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
    private final String LOG_TAG = CoordinateTracker.LOG_TAG +": "+this.getClass().getSimpleName();
    public  SharedPreferences userStore = StorageAdapter.usersStorage();
    private Context context = CoordinateTracker.getAppContext();
    private NotificationManager mNotificationManager;
    private Notification.Builder NotifyBuilder;
    private int NotifyID;
    private Timer notifyUpdaterTimer = null;

    public CoordinateService() {
        super();
        if (CoordinateTracker.isTokenEmpty()) {
            stopSelf();
        }
    }

    public void onCreate() {
        super.onCreate();

        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotifyBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.app_name));
        NotifyID = new AtomicInteger(0).incrementAndGet();

        Notification notification = NotifyBuilder.build();

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        NotifyBuilder.setContentIntent(contentIntent);

        startForeground(NotifyID, notification);

        Log.d(LOG_TAG, "onCreate");
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
                .remove(CustomLocationListener.LAST_CITY_TAG)
                .remove(CustomLocationListener.LAST_SPEED_TAG)
                .apply();
        Log.d(LOG_TAG, "onDestroy");
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
        try {

            NotifyBuilder.setWhen(System.currentTimeMillis());
            String content = "Status: ";
            String info =  "";
            String city = userStore.getString(CustomLocationListener.LAST_CITY_TAG, "");
            int speed = Math.round(userStore.getFloat(CustomLocationListener.LAST_SPEED_TAG, 0)*3.6f);
            int localStoreSize = StorageAdapter.get().getLocationsStorage().getAll().size();

            if(CoordinateTracker.isConnected()) {
                content += "Connected";
            } else {
                content += "Connecting...";
            }

            if (localStoreSize > 0){
                content += " ("+localStoreSize+" records not synced)";
            }

            if (!city.isEmpty()){
                info += city + ", ";
            }

            info += speed + " km/h";

            NotifyBuilder.setContentText(content);
            NotifyBuilder.setContentInfo(info);

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
    }
}

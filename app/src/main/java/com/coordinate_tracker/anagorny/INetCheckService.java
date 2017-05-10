package com.coordinate_tracker.anagorny;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Based of: https://github.com/oscarjiv91/Android-Check-Internet-Connection
 * Created by sosnov on 20/03/15.
 */
public class INetCheckService extends Service {
    private final String LOG_TAG = "COORDINATE";
    public static String TAG_INTERVAL = "interval";
    public static String TAG_URL_PING = "url_ping";
    public static String TAG_ACTIVITY_NAME = "activity_name";


    private int interval;
    private String url_ping;
    private String activity_name;
    private SharedPreferences userStore;
    private Timer mTimer = null;

    ConnectionServiceCallback mConnectionServiceCallback;

    public INetCheckService() {
        super();
        userStore = StorageAdapter.usersStorage();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public interface ConnectionServiceCallback {
        void hasInternetConnection();

        void hasNoInternetConnection();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        interval = userStore.getInt(TAG_INTERVAL, 10);
        url_ping = userStore.getString(TAG_URL_PING, "google.com");
        activity_name = userStore.getString(TAG_ACTIVITY_NAME, "");

        try {
            mConnectionServiceCallback = (ConnectionServiceCallback) Class.forName(activity_name).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new CheckForConnection(), 0, interval * 1000);

        return super.onStartCommand(intent, flags, startId);
    }

    class CheckForConnection extends TimerTask {
        @Override
        public void run() {
            isNetworkAvailable();
        }
    }

    @Override
    public void onDestroy() {
        mTimer.cancel();
        Log.d(LOG_TAG, "onDestroy- InetCheck");
        super.onDestroy();
    }

    private boolean isNetworkAvailable() {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(5000, TimeUnit.MILLISECONDS)
                .readTimeout(7000, TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder()
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Token token=" + StorageAdapter.usersStorage().getString(Configuration.AUTH_TOKEN_KEY_NAME, ""))
                .url(url_ping)
                .get()
                .build();

        try {
            client.newCall(request).execute();
            // PING - OK
            mConnectionServiceCallback.hasInternetConnection();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        // NO PING
        mConnectionServiceCallback.hasNoInternetConnection();
        return false;
    }

}

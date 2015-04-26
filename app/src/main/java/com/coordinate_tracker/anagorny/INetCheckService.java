package com.coordinate_tracker.anagorny;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Based of: https://github.com/oscarjiv91/Android-Check-Internet-Connection
 * Created by Oscar on 20/03/15.
 */
public class INetCheckService extends Service {
    private static final String TAG = "Coordinate-Tracker";
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
        Log.d(TAG, "OnStart- InetCheck");
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
        Log.d(TAG, "onDestroy- InetCheck");
        super.onDestroy();
    }

    private boolean isNetworkAvailable() {
        HttpGet httpGet = new HttpGet(url_ping);
        HttpParams httpParameters = new BasicHttpParams();

        int timeoutConnection = 5000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);

        int timeoutSocket = 7000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
        try {
            httpClient.execute(httpGet);
            mConnectionServiceCallback.hasInternetConnection();
            return true;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mConnectionServiceCallback.hasNoInternetConnection();
        return false;
    }

}
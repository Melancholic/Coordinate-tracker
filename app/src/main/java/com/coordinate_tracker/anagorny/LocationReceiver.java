package com.coordinate_tracker.anagorny;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by sosnov on 20.03.15.
 */
public class LocationReceiver extends BroadcastReceiver {
    private final String LOG_TAG = "COORDINATE";
    double latitude, longitude;
    short speed, accuracy;
    long time;
    private Context context;
    private SharedPreferences storage;
    private final String TARGET_URL = Configuration.getReciveURL();


    @Override
    public void onReceive(final Context context, final Intent calledIntent) {
        Log.d("LOC_RECEIVER", "Location RECEIVED!");
        this.context = context;
        storage = StorageAdapter.get(context.getApplicationContext()).getLocationsStorage();
        latitude = calledIntent.getDoubleExtra("latitude", -1);
        longitude = calledIntent.getDoubleExtra("longitude", -1);
        accuracy = calledIntent.getShortExtra("accuracy", (short) -1);
        speed = calledIntent.getShortExtra("speed", (short) -1);
        time = calledIntent.getLongExtra("time", Calendar.getInstance(TimeZone.getTimeZone("utc")).getTimeInMillis());
        if (calledIntent.getBooleanExtra("need_new_track", false)) {
            //TODO make request for create new track
            Log.d(LOG_TAG, "Create new track...");
        }
        updateRemote();
    }

    private void updateRemote() {
        JSONObject data = new JSONObject();
        try {
            data.put("latitude", latitude);
            data.put("longitude", longitude);
            data.put("accuracy", accuracy);
            data.put("speed", speed);
            data.put("time", time);
            // BAD JSON
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "" + e);
        }

        if (CoordinateTracker.isConnected()) {
            //Service  connected to server
            LocationSendTask task = new LocationSendTask();
            task.execute(TARGET_URL, data.toString());
        } else {
            //Not connected to server< save to local storage
            storage.edit().putString(String.valueOf(time), data.toString()).apply();
        }


    }

    private class LocationSendTask extends AsyncTask<String, String, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... args) {
            String url = args[0];
            String data = args[1];

            JSONObject json = new JSONObject();
            try {
                json.put("success", false);
                json.put("info", "Connection failed!");

                try {
                    RequestBody body = RequestBody.create(HttpAdapter.TYPE_JSON, data);
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .addHeader("Accept", "application/json")
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Authorization", "Token token=" + StorageAdapter.usersStorage().getString(Configuration.AUTH_TOKEN_KEY_NAME, ""))
                            .url(url)
                            .post(body)
                            .build();
                    Log.e(LOG_TAG, "Location sended on " + url);
                    Response response = client.newCall(request).execute();
                    String jsonData = response.body().string();
                    Log.e(LOG_TAG, "Response from: " + url + " : " + (response.isSuccessful() ? "OK" : "BAD") + " (" + response.code() + ")");
                    json = new JSONObject(jsonData);
                    try {
                        // Server Error
                        if (!json.getBoolean("clear")) {
                            Log.i(LOG_TAG, "Serbver error, save location to local store at " + time);
                            storage.edit().putString(String.valueOf(time), data).apply();
                        }
                        // BAD CONNECTION - save to local
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Server error, add location to local storage" );
                        storage.edit().putString(String.valueOf(time), data).apply();
                    }
                    //Unknow JSON
                } catch (IOException e) {
                    json.put("success", false);
                    json.put("info", e.getMessage());
                    storage.edit().putString(String.valueOf(time), data).apply();
                }
                // BAD JSON
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "" + e);
            }
            return json;
        }
    }


}


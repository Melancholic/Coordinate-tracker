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
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by sosnov on 20.03.15.
 */
public class INetCheckReceiver extends BroadcastReceiver {
    private final String LOG_TAG = "COORDINATE";
    private Context context;
    private SharedPreferences storage;
    private final String TARGET_URL = Configuration.getReciveURL();

    @Override
    public void onReceive(final Context context, final Intent calledIntent) {
        Log.d(LOG_TAG, "ON RECEIVE: "+calledIntent.getAction());
        if ( calledIntent.getAction().equals(CoordinateTracker.CONNECTION_ON_INTENT)) {
            this.context = context;
            storage = StorageAdapter.get(context.getApplicationContext()).getLocationsStorage();
            updateRemote();
        } else if (calledIntent.getAction().equals(CoordinateTracker.CONNECTION_OFF_INTENT)) {
            //TODO stop requests
        }

    }

    private void updateRemote() {
        Map<String, String> locations = (Map<String, String>) storage.getAll();
        Log.d(LOG_TAG, "LOCAL: " + locations.toString());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (String key : locations.keySet()) {
            LocationSendTask task = new LocationSendTask();
            task.execute(TARGET_URL, key, locations.get(key));
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(LOG_TAG, "LOCAL: " + storage.getAll().toString());

    }

    private class LocationSendTask extends AsyncTask<String, String, JSONObject> {
        private String key = null;
        private String holder;

        @Override
        protected JSONObject doInBackground(String... args) {
            this.key = args[1];
            this.holder = args[2];
            JSONObject json = new JSONObject();
            try {
                json.put("success", false);
                json.put("info", "Connection failed!");
                try {
                    RequestBody body = RequestBody.create(HttpAdapter.TYPE_JSON, holder);
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .addHeader("Accept", "application/json")
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Authorization", "Token token=" + StorageAdapter.usersStorage().getString(Configuration.AUTH_TOKEN_KEY_NAME, ""))
                            .url(args[0])
                            .post(body)
                            .build();
                    Log.e(LOG_TAG, "Location sended on " + args[0]);
                    Response response = client.newCall(request).execute();
                    String jsonData = response.body().string();
                    Log.e(LOG_TAG, "Response from: " + args[0] + " : " + (response.isSuccessful() ? "OK" : "BAD") + " (" + response.code() + ")");
                    // Success
                    json = new JSONObject(jsonData);
                    // BAD CONNECTION - dont remove to local
                } catch (IOException e) {
                    json.put("success", false);
                    json.put("info", e.getMessage());
                }
                // BAD JSON
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "" + e);
            }
            return json;
        }


        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getBoolean("clear")) {
                    Log.i(LOG_TAG, "Clear location at " + key);
                    storage.edit().remove(key).apply();
                }
                // Response does'nt include clear property (internal error, dont remove from local)
            } catch (JSONException e) {
                Log.e(LOG_TAG, "" + e);
                Log.e(LOG_TAG, "Server error, don't remove from local storage" );

            } finally {
                super.onPostExecute(json);
            }
        }
    }


}


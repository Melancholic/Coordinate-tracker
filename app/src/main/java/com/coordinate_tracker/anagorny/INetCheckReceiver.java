package com.coordinate_tracker.anagorny;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;

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
        this.context = context;
        storage = StorageAdapter.get(context.getApplicationContext()).getLocationsStorage();
        updateRemote();

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
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(args[0]);
            post.addHeader("Authorization", "Token token=" + StorageAdapter.usersStorage().getString(Configuration.AUTH_TOKEN_KEY_NAME, ""));
            String response = null;
            JSONObject json = new JSONObject();
            try {
                try {
                    json.put("success", false);
                    json.put("info", "Connection failed!");
                    StringEntity se = new StringEntity(holder);
                    post.setEntity(se);
                    post.setHeader("Accept", "application/json");
                    post.setHeader("Content-Type", "application/json");

                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    response = client.execute(post, responseHandler);
                    json = new JSONObject(response);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    Log.e("Unknow host!", "" + e);
                } catch (HttpResponseException e) {
                    e.printStackTrace();
                    Log.e("ClientProtocol", "" + e);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("IO", "" + e);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("JSON", "" + e);
            }

            return json;
        }


        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (!json.getBoolean("success")) {
                    Log.e(LOG_TAG, "Server return error: \"" + json.getString("info") + "\"");
                } else {
                    Log.i(LOG_TAG, "Success: " + json.getString("info"));
                    storage.edit().remove(key).commit();
                }
            } catch (Exception e) {

                e.printStackTrace();
            } finally {
                super.onPostExecute(json);

            }
        }
    }


}


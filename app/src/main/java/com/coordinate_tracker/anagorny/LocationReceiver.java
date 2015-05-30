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
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by sosnov on 20.03.15.
 */
public class LocationReceiver extends BroadcastReceiver {
    private final String LOG_TAG = "COORDINATE";
    double latitude, longitude, accuracy, speed ;
    long time;
    private Context context;
    private SharedPreferences storage;
    private final String TARGET_URL = Configuration.getReciveURL();


    @Override
    public void onReceive(final Context context, final Intent calledIntent){
        Log.d("LOC_RECEIVER", "Location RECEIVED!");
        Log.d("TEST CONT loc reciver", context.toString());
        this.context = context;
        storage = StorageAdapter.get(context.getApplicationContext()).getLocationsStorage();
        latitude = calledIntent.getDoubleExtra("latitude", -1);
        longitude = calledIntent.getDoubleExtra("longitude", -1);
        accuracy = calledIntent.getDoubleExtra("accuracy", -1);
        speed = calledIntent.getDoubleExtra("speed", -1);
        time = calledIntent.getLongExtra("time", Calendar.getInstance(TimeZone.getTimeZone("utc")).getTimeInMillis());
        if (calledIntent.getBooleanExtra("need_new_track", false)) {
            //TO DO make request for create new track
            Log.d(LOG_TAG, "Create new track...");
        }
        updateRemote();

    }

    private void updateRemote()
    {
        LocationSendTask task = new LocationSendTask();
        task.execute(TARGET_URL);

    }

    private class LocationSendTask extends AsyncTask<String, String, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... urls) {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(urls[0]);
            post.addHeader("Authorization", "Token token=" + StorageAdapter.usersStorage().getString(Configuration.AUTH_TOKEN_KEY_NAME, ""));
            JSONObject holder = new JSONObject();
            String response = null;
            JSONObject json = new JSONObject();
            try {
                try {
                    json.put("success", false);
                    json.put("info", "Connection failed!");
                    holder.put("latitude", latitude);
                    holder.put("longitude", longitude);
                    holder.put("accuracy", accuracy);
                    holder.put("speed", speed);
                    holder.put("time", time);
                    StringEntity se = new StringEntity(holder.toString());
                    post.setEntity(se);
                    post.setHeader("Accept", "application/json");
                    post.setHeader("Content-Type", "application/json");
                    if (!CoordinateTracker.isConnected()) {
                        Log.e(LOG_TAG, "Save to local (DISCONNECT)");
                        storage.edit().putString(String.valueOf(time), holder.toString()).commit();
                        return json;
                    }
                    json.put("time", String.valueOf(time));
                    json.put("holder", holder.toString());

                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    response = client.execute(post, responseHandler);
                    json = new JSONObject(response);
                } catch (UnknownHostException e) {
                    Log.e(LOG_TAG, "Save to local (UNKNOW HOST)");
                    storage.edit().putString(String.valueOf(time), holder.toString()).commit();
                    Log.e(LOG_TAG, storage.getAll().toString());
                } catch (HttpResponseException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "" + e);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "" + e);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "" + e);
            }

            return json;
        }



        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (!json.getBoolean("success")) {
                    Log.e(LOG_TAG, "Server return error: \"" + json.getString("info") + "\"");

                }else{
                    Log.i(LOG_TAG, "Success: " + json.getString("info"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                super.onPostExecute(json);

            }
        }
    }




}


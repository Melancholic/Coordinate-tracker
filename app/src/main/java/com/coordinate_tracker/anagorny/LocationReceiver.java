package com.coordinate_tracker.anagorny;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by sosnov on 20.03.15.
 */
public class LocationReceiver extends BroadcastReceiver {
    private final String TAG="LOC_RECEIVER";
    double latitude, longitude, accuracy, speed ;
    private Context context;
    //private final String TARGET_URL="http://10.0.2.2:3000/api/v1/geodata/recive";
    private final String TARGET_URL = Configuration.getReciveURL();
    @Override
    public void onReceive(final Context context, final Intent calledIntent){
        this.context=context;
        Log.d("LOC_RECEIVER", "Location RECEIVED!");

        latitude = calledIntent.getDoubleExtra("latitude", -1);
        longitude = calledIntent.getDoubleExtra("longitude", -1);
        accuracy = calledIntent.getDoubleExtra("accuracy",-1);
        speed = calledIntent.getDoubleExtra("speed",-1);
        updateRemote(latitude, longitude);

    }

    private void updateRemote(final double latitude, final double longitude )
    {
        //TOODO ASYNC TASK
        Log.d("LOC_RECEIVER","HERE!");
        LocationSendTask task= new LocationSendTask();
        task.execute(TARGET_URL);                //        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
    }

    private class LocationSendTask extends AsyncTask<String, String, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... urls) {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(urls[0]);
            post.addHeader("Authorization", "Token token="+CoordinateTracker.userStore.getString(Configuration.AUTH_TOKEN_KEY_NAME,""));
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
                    holder.put("time", (Calendar.getInstance(TimeZone.getTimeZone("utc")).getTimeInMillis()))
                    ;
                    StringEntity se = new StringEntity(holder.toString());
                    post.setEntity(se);
                    post.setHeader("Accept", "application/json");
                    post.setHeader("Content-Type", "application/json");

                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    response = client.execute(post, responseHandler);
                    json = new JSONObject(response);

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
                    Log.e(TAG, "Server return error: \""+json.getString("info")+"\"");
                }else{
                    Log.i(TAG, "Success: "+json.getString("info"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                super.onPostExecute(json);

            }
        }
    }




}


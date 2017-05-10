package com.coordinate_tracker.anagorny;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class MainActivity extends ActionBarActivity {
    private final String LOG_TAG = CoordinateTracker.LOG_TAG +": "+this.getClass().getSimpleName();
    private static final String SERVICE_STOP_TEXT = "Kill";
    private static final String SERVICE_START_TEXT = "Start";
    public static SharedPreferences userStore;
    private  Button log_out_but;
    private Button exit_but;
    private Button service_but;
    private Button local_data_but;
    private LinearLayout action_area;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init_params();
        Log.d(LOG_TAG, "onCreate");
    }


    @Override
    protected void onResume(){
        super.onResume();
        if(CoordinateTracker.isTokenEmpty()){
            action_area.setVisibility(View.INVISIBLE);
            service_stop();
            start_login_activity();
        }else{
            action_area.setVisibility(View.VISIBLE);
            service_start();
        }
        Log.d(LOG_TAG, "onResume");
    }

    private void start_login_activity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivityForResult(intent, 0);
    }

    private void service_start() {
        if(!isServiceRunning(CoordinateService.class)){
           startService(new Intent(this, CoordinateService.class));
            service_but.setText(SERVICE_STOP_TEXT);
        }
    }

    private void service_stop() {
        if(isServiceRunning(CoordinateService.class)){
            stopService(new Intent(this, CoordinateService.class));
            service_but.setText(SERVICE_START_TEXT);
        }
    }


    private void logout_action(){
        service_stop();
        SharedPreferences.Editor editor = userStore.edit();
        editor.remove(Configuration.AUTH_TOKEN_KEY_NAME);
        editor.remove(Configuration.UUID_TOKEN_KEY_NAME);
        editor.apply();
        start_login_activity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init_params() {
        userStore = StorageAdapter.usersStorage();
        TextView tv = (TextView) findViewById(R.id.textView1);
        action_area=(LinearLayout)findViewById(R.id.action_area);
        log_out_but=(Button)findViewById(R.id.log_out_but);
        exit_but=(Button)findViewById(R.id.exit_but);
        exit_but.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                System.exit(0);
            }
        });
        local_data_but = (Button) findViewById(R.id.local_data);
        local_data_but.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PrintLocal();
            }
        });
        service_but=(Button)findViewById(R.id.kill_button);
        service_but.setText((!isServiceRunning(CoordinateService.class)) ? SERVICE_START_TEXT : SERVICE_STOP_TEXT);
        service_but.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                service_but_list();
            }
        });
        log_out_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout_action();
            }
        });
        tv.setText(Html.fromHtml("<font color='blue'>Device: " + Configuration.ID + "</font><br>"));
        if (CoordinateTracker.isConnected()) {
            tv.append(Html.fromHtml("Status: <font color='green'>Connected!</font><br>"));
        } else {
            tv.append(Html.fromHtml("Status: <font color='red'>Not connected!</font><br>"));
        }
        tv.setMovementMethod(new ScrollingMovementMethod());

        SharedPreferences.Editor editor = userStore.edit();
        editor.remove(CustomLocationListener.LAST_LATITUDE_TAG);
        editor.remove(CustomLocationListener.LAST_LONGITUDE_TAG);
        editor.remove(CustomLocationListener.LAST_ACCURACY_TAG);
        editor.remove(CustomLocationListener.LAST_TIME_TAG);
        editor.apply();
    }

    private void PrintLocal() {
        TextView tv = (TextView) findViewById(R.id.textView1);

        tv.setText(Html.fromHtml("<font color='blue'>Device: " + Configuration.ID + "</font><br>"));
        if (CoordinateTracker.isConnected()) {
            tv.append(Html.fromHtml("Status: <font color='green'>Connected!</font><br>"));
        } else {
            tv.append(Html.fromHtml("Status: <font color='red'>Not connected!</font><br>"));
        }

        tv.append("\n\n===" + Calendar.getInstance().getTime().toString() + "===");
        SharedPreferences storage = StorageAdapter.get((CoordinateTracker) getApplicationContext()).getLocationsStorage();
        Map<String, String> locations = (Map<String, String>) storage.getAll();
        tv.append("\nTOTAL: " + locations.size() + " records \n\n");
        for (String key : locations.keySet()) {
            tv.append((new Date(Long.parseLong(key))).toString() + " ==> " + locations.get(key));
            tv.append("\n");
        }
        tv.append("\n===============");
    }

    private void service_but_list() {
        if(isServiceRunning(CoordinateService.class)){
             service_stop();
        }else{
            service_start();
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}

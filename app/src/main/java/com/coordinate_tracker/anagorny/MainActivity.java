package com.coordinate_tracker.anagorny;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends ActionBarActivity {
    private static final String SERVICE_STOP_TEXT = "Kill";
    private static final String SERVICE_START_TEXT = "Start";
    public static SharedPreferences userStore;
    private  Button log_out_but;
    private Button exit_but;
    private Button service_but;
    private LinearLayout action_area;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init_params();
    }


    @Override
    protected void onResume(){
        super.onResume();
        if(token_empty()){
            action_area.setVisibility(View.INVISIBLE);
            service_stop();
            start_login_activity();
        }else{
            action_area.setVisibility(View.VISIBLE);
            service_start();
        }
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
        editor.commit();
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
        userStore = StorageAdapter.usersStorage(); //getSharedPreferences(Configuration.PRIVATE_STORE_NAME, MODE_PRIVATE);
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
        tv.setText(Html.fromHtml("<font color='blue'>Device: " + Configuration.ID + "</font><br><hr><br>"));
        tv.setMovementMethod(new ScrollingMovementMethod());
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log=new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
            }
            tv.append(log.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void service_but_list() {
        if(isServiceRunning(CoordinateService.class)){
             service_stop();
        }else{
            service_start();
        }
    }

    private boolean token_empty(){
       return userStore.getString(Configuration.AUTH_TOKEN_KEY_NAME,"")==null || userStore.getString(Configuration.AUTH_TOKEN_KEY_NAME,"").isEmpty();
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

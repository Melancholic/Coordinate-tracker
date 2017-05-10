package com.coordinate_tracker.anagorny;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.savagelook.android.UrlJsonAsyncTask;

import org.json.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
    private final String LOG_TAG = "COORDINATE";
    private static final String TARGET_URL = Configuration.getLoginURL();

    public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // Values for email and password at the time of the login attempt.
    private String mEmail;
    private String mPassword;
    private String devId;
    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText devIdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
        mEmailView = (EditText) findViewById(R.id.email);
        mEmailView.setText(mEmail);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        devIdView = (EditText) findViewById(R.id.uuid);
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        devIdView.setError(null);
        // Store values at the time of the login attempt.
        mEmail = mEmailView.getText().toString();
        mPassword = mPasswordView.getText().toString();
        devId = devIdView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (mPassword.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mEmail)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!mEmail.contains("@")) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (TextUtils.isEmpty(devId)) {
            devIdView.setError(getString(R.string.error_field_required));
            focusView = devIdView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            UserLoginTask UserLoginTask = new UserLoginTask(LoginActivity.this);
            UserLoginTask.setMessageLoading("Connection to server...");
            UserLoginTask.execute(TARGET_URL);
        }
    }

    private String bin2hex(byte[] data) {
        return String.format("%0" + (data.length * 2) + 'x', new BigInteger(1, data));
    }

    private String calculate_hash() {
        String x = this.mEmail + this.mPassword;
        MessageDigest digest = null;
        String hash = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(x.getBytes());

            hash = bin2hex(digest.digest());

            Log.i("Eamorr", "result is " + hash);
        } catch (NoSuchAlgorithmException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        return hash;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    private class UserLoginTask extends UrlJsonAsyncTask {
        private Context cont;

        public UserLoginTask(Context c) {
            super(c);
            cont = c;
        }

        protected JSONObject doInBackground(String... urls) {
            JSONObject data = new JSONObject();
            JSONObject json = new JSONObject();
                try {
                    json.put("success", false);
                    json.put("info", "Something went wrong. Retry!");
                    data.put("logdata", calculate_hash());
                    data.put("uuid", devId);

                    RequestBody body = RequestBody.create(HttpAdapter.TYPE_JSON, data.toString());
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .addHeader("Accept", "application/json")
                            .addHeader("Content-Type", "application/json")
                            .url(urls[0])
                            .post(body)
                            .build();

                    Response response = client.newCall(request).execute();
                    String jsonData = response.body().string();
                    json = new JSONObject(jsonData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            return json;
        }


        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getBoolean("success")) {
                    SharedPreferences.Editor editor = MainActivity.userStore.edit();
                    editor.putString(Configuration.AUTH_TOKEN_KEY_NAME, json.getJSONObject("data").getString("api_token"));
                    editor.putString(Configuration.UUID_TOKEN_KEY_NAME, devId);
                    editor.commit();
                    ((Activity) LoginActivity.this).finish();
                }
                Toast.makeText(context, json.getString("info"), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                ((Activity) context).recreate();
            } finally {
                super.onPostExecute(json);
            }
        }

    }

}

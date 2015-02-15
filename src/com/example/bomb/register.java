package com.example.bomb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class register extends Activity{
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    
    
    String SENDER_ID = "5942005328";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCM Demo";

    TextView mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    Context context;

    String regid;
    Context mContext;
    String newid;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        mContext = this;
        context = getApplicationContext();
		Button start = (Button) findViewById(R.id.startApp);
		final EditText EditId=(EditText) findViewById(R.id.regId);
		start.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String newId = EditId.getText().toString();
				if(newId.equals(""))
				{
					Toast toast = Toast.makeText(mContext, "Please insert your ID",Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
				else
				{
					String checkResult="";
					try {
						checkResult = new doIDCheck().execute(newId).get();
						Log.i("abc",checkResult);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(checkResult.equals("success"))
					{
						checkResult="";
						newid = newId;
						registerInBackground();

					}
					else
					{
						Toast toast = Toast.makeText(mContext, "ID is already exists. Please insert other ID",Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
				}
			}
		});
		
	}
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    Log.i("abc", regid);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend(regid);

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                //mDisplay.append(msg + "\n");
	    		Intent reg = new Intent(mContext, DemoActivity.class);
	    		startActivity(reg);
	    		finish();
            }
        }.execute(null, null, null);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(DemoActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend(String regid) {
      // Your implementation here.
    	Log.i("abc", "in send");
		String checkResult="";
		Log.i("abc",regid);
		String URL = "http://whispering-hamlet-1005.herokuapp.com/addId";
		String result ="";
		DefaultHttpClient client = new DefaultHttpClient();
			// Make NameValuePair for password to server. use POST method.
			// Make connection to server.
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("id", newid));
			nameValuePairs.add(new BasicNameValuePair("gcmkey", regid));
			Log.i("abc", newid);
			Log.i("abc", regid);
			Log.i("abc", "1");
			// Get response and parse entity.
			HttpParams connectionParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(connectionParams, 5000);
			HttpConnectionParams.setSoTimeout(connectionParams, 5000);
			HttpPost httpPost = new HttpPost(URL);
			Log.i("abc", "2");
			UrlEncodedFormEntity entityRequest;
			try {
				entityRequest = new UrlEncodedFormEntity(nameValuePairs, "EUC-KR");
				httpPost.setEntity(entityRequest);
				Log.i("abc", "3");
				HttpResponse responsePost = client.execute(httpPost);
				HttpEntity resEntity = responsePost.getEntity();
				// Parse result to string.
				result = EntityUtils.toString(resEntity);
				client.getConnectionManager().shutdown();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i("abc", result);
		
		
		/*
		try {
			checkResult = new doStartApp().execute(newid, regid).get();
			Log.i("abc", "request end");
			Log.i("abc", checkResult);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(checkResult.equals("success"))
		{
			Log.i("abc", "success");
    		Intent reg = new Intent(mContext, DemoActivity.class);
    		startActivity(reg);
    		finish();
		}
		else
		{
			Log.i("abc", "fail");
			Log.i("abc", checkResult);
			Toast toast = Toast.makeText(mContext, "Start Fail",Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
		*/
    }
	
    public class doIDCheck extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... arg) {
			String ID = new String(arg[0]);
			String URL = "http://whispering-hamlet-1005.herokuapp.com/checkid/"+ID;
				
			DefaultHttpClient client = new DefaultHttpClient();
			try {
				// Make NameValuePair for password to server. use POST method.
				// Make connection to server.
				HttpGet httpget = new HttpGet(URL);
				
				// Get response and parse entity.
				HttpResponse responseGet = client.execute(httpget);
				HttpEntity resEntity = responseGet.getEntity();
					
				// Parse result to string.
				String result = EntityUtils.toString(resEntity);
				client.getConnectionManager().shutdown();
				return result;
					
			} catch (Exception e) {
				e.printStackTrace();
				client.getConnectionManager().shutdown();	// Disconnect.
				return "";
			}
		}
	}
    
    
    public class doStartApp extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... arg) {
			String ID = new String(arg[0]);
			String registerID = new String(arg[1]);
			String URL = "http://whispering-hamlet-1005.herokuapp.com/addId";
				
			DefaultHttpClient client = new DefaultHttpClient();
			try {
				// Make NameValuePair for password to server. use POST method.
				// Make connection to server.
				ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("id", ID));
				nameValuePairs.add(new BasicNameValuePair("key", registerID));
				Log.i("abc", "1");
				// Get response and parse entity.
				HttpParams connectionParams = client.getParams();
				HttpConnectionParams.setConnectionTimeout(connectionParams, 5000);
				HttpConnectionParams.setSoTimeout(connectionParams, 5000);
				HttpPost httpPost = new HttpPost(URL);
				Log.i("abc", "2");
				UrlEncodedFormEntity entityRequest = new UrlEncodedFormEntity(nameValuePairs, "EUC-KR");
				httpPost.setEntity(entityRequest);
				Log.i("abc", "3");
				HttpResponse responsePost = client.execute(httpPost);
				HttpEntity resEntity = responsePost.getEntity();
				// Parse result to string.
				String result = EntityUtils.toString(resEntity);
				client.getConnectionManager().shutdown();
				return result;
					
			} catch (Exception e) {
				e.printStackTrace();
				client.getConnectionManager().shutdown();	// Disconnect.
				return "";
			}
		}
	}
}


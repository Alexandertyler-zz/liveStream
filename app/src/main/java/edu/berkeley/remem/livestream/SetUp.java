package edu.berkeley.remem.livestream;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;


import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;


import com.google.android.gms.common.Scopes;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTubeScopes;


public class SetUp extends Activity {

    private static final int AUTH_REQUEST_CODE = 3;
    private JsonComm jsonComm;
    GetUsernameTask getUsernameTask;

    public static String killAppend = "false";


    //LOCATION INFORMATION
    private static LocationManager locationManager;
    private static LocationListener locationListener;
    private double latitude;
    private double longitude;

    private String BASE_SERVER_URL = "http://contextualvideo-shunshou.rhcloud.com/";

    //oAuth
    public static final String accountEmail = "alexanderhtyler@gmail.com";
    public static final String appName = "remem-video-streaming";

    final HttpTransport httpTransporttransport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = new GsonFactory();
    GoogleAccountCredential credential;
    private String mChosenAccountName;
    Collection<String> scopes = Arrays.asList("https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/youtube", "https://www.googleapis.com/auth/youtube.upload",
            "https://www.googleapis.com/auth/youtube.readonly", "https://www.googleapis.com/auth/youtubepartner",
            "https://www.googleapis.com/auth/youtubepartner-channel-audit");


    String scope =
            "oauth2:https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/youtube https://www.googleapis.com/auth/youtube.upload https://www.googleapis.com/auth/youtube.readonly https://www.googleapis.com/auth/youtubepartner https://www.googleapis.com/auth/youtubepartner-channel-audit";

    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    public static String token;

    GoogleAccountCredential googleCredential;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);

        /*credential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), scopes);
        credential.setBackOff(new ExponentialBackOff());
        credential.setSelectedAccountName("alexanderhtyler@gmail.com");
        startActivityForResult(credential.newChooseAccountIntent(),
                REQUEST_ACCOUNT_PICKER);
        */
        /*Collection<String> SCOPE_LIST = Arrays.asList(Scopes.PROFILE, YouTubeScopes.YOUTUBE, YouTubeScopes.YOUTUBEPARTNER, YouTubeScopes.YOUTUBE_UPLOAD, YouTubeScopes.YOUTUBE_READONLY);
        googleCredential = GoogleAccountCredential.usingOAuth2(this, SCOPE_LIST);
        googleCredential.setBackOff(new ExponentialBackOff());
        startActivityForResult(googleCredential.newChooseAccountIntent(), 2);


        getUsernameTask = new GetUsernameTask(this, accountEmail, scope, googleCredential);
        getUsernameTask.completion = new authComp() {
            @Override
            public void onComplete(String result) {
                token = result;
            }
        };
        getUsernameTask.execute();*/

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {}

            @Override
            public void onProviderEnabled(String s) {}

            @Override
            public void onProviderDisabled(String s) {}

        };

        // Register the listener with the Location Manager to receive location updates
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Get update every 5 seconds
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);
        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Get update every 5 seconds
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void newBroadcast(View view) {
        System.out.println("newBroadcast clicked");
        getUsernameTask = new GetUsernameTask(this, accountEmail, scope, googleCredential);
        getUsernameTask.completion = new authComp() {
            @Override
            public void onComplete(String result) {
                token = result;
            }
        };
        getUsernameTask.execute();
        StartBroadcast startBroadcast = new StartBroadcast();
        startBroadcast.execute(googleCredential);
    }

    public void sendJsonPacket(View view) {
        EditText url = (EditText) findViewById(R.id.urlValue);
        EditText ytId = (EditText) findViewById(R.id.ytID);
        EditText hashtag = (EditText) findViewById(R.id.hashTag);
        EditText mac = (EditText) findViewById(R.id.macAddress);

        //Debug info
        System.out.println("Received data from EditText fields:");
        System.out.println(url.getText().toString());
        System.out.println(ytId.getText().toString());
        System.out.println(hashtag.getText().toString());


        String[] jsonDataSplit = hashtag.getText().toString().split(" |=");
        Map<String, String> jsonMap = new HashMap<String, String>();
        jsonMap.put("hashtag", hashtag.getText().toString());
        jsonMap.put("mac", mac.getText().toString());
        jsonMap.put("ytid", ytId.getText().toString());

        jsonComm = new JsonComm(url.getText().toString(), jsonMap);

        try {
            jsonComm.execute();
        } catch (Exception e) {
            System.out.println(e);
        }

    }


    public void androidSetupPost(View view) {
        EditText hashtag = (EditText) findViewById(R.id.hashTag);
        EditText mac = (EditText) findViewById(R.id.macAddress);
        EditText ytId = (EditText) findViewById(R.id.ytID);
        EditText ytRtmp = (EditText) findViewById(R.id.ytRTMP);


        Map<String, String> jsonMap = new HashMap<String, String>();
        jsonMap.put("hashtag", hashtag.getText().toString());
        jsonMap.put("mac", mac.getText().toString());
        jsonMap.put("ytid", ytId.getText().toString());
        jsonMap.put("ytrtmp", ytRtmp.getText().toString());


        String setupURL = BASE_SERVER_URL + "android_setup_1";

        jsonComm = new JsonComm(setupURL, jsonMap);

        try {
            jsonComm.execute();
            androidAppendPost();
            SystemClock.sleep(5000);
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void killStream() {
        killAppend = "true";
    }

    public void androidAppendPost() {
        while (killAppend.equals("false")) {
            System.out.println("killAppend = " + killAppend);
            System.out.println("long/lat = " + String.valueOf(longitude) + "/" + String.valueOf(latitude));

            EditText ytId = (EditText) findViewById(R.id.ytID);

            Map<String, String> jsonMap = new HashMap<String, String>();
            jsonMap.put("longitude", String.valueOf(longitude));
            jsonMap.put("latitude", String.valueOf(latitude));
            jsonMap.put("ytid", ytId.getText().toString());

            String appendURL = BASE_SERVER_URL + "android_append_2";

            jsonComm = new JsonComm(appendURL, jsonMap);

            jsonComm.completion = new asyncComp() {
                @Override
                public void onComplete(String result) {
                    System.out.println("completion abstract is receiving " + result);
                    killAppend = result;
                }
            };

            try {
                jsonComm.execute();
            } catch (Exception e) {
                System.out.println(e);
            }
            SystemClock.sleep(5000);
            System.out.println("After sleep, should have appended.");
        }
    }


    public void sendLocation(View view) {
        EditText url = (EditText) findViewById(R.id.urlValue);
        EditText ytId = (EditText) findViewById(R.id.ytID);

        //Debug info
        System.out.println("Received data from EditText fields:");
        System.out.println(url.getText().toString());
        System.out.println(ytId.getText().toString());

        Map<String, String> jsonMap = new HashMap<String, String>();

        jsonMap.put("longitude", String.valueOf(longitude));
        jsonMap.put("latitude", String.valueOf(latitude));
        jsonMap.put("ytid", ytId.getText().toString());


        jsonComm = new JsonComm(url.getText().toString(), jsonMap);

        try {
            jsonComm.execute();
        } catch (Exception e) {
            System.out.println("Error is: " + e);
        }

    }

    public interface asyncComp {
        public void onComplete(String result);
    }

    public interface authComp {
        public void onComplete(String result);
    }

    public class StartBroadcast extends AsyncTask<GoogleAccountCredential, Void, Void> {

        @Override
        protected Void doInBackground(GoogleAccountCredential... creds) {
            System.out.println("In StartBroadcast");
            CreateBroadcast createBroadcast = new CreateBroadcast();
            createBroadcast.start(creds[0]);
            return null;
        }
    }

    public class GetUsernameTask extends AsyncTask<Void, Void, String>{
        Activity mActivity;
        String mScope;
        String mEmail;
        public SetUp.authComp completion;
        GoogleAccountCredential gAccount;

        GetUsernameTask(Activity activity, String name, String scope, GoogleAccountCredential gCred) {
            this.mActivity = activity;
            this.mScope = scope;
            this.mEmail = name;
            this.gAccount = gCred;
        }

        /**
         * Executes the asynchronous job. This runs when you call execute()
         * on the AsyncTask instance.
         */
        @Override
        protected String doInBackground(Void... params) {
            String token = null;
            try {
                token = fetchToken();
                if (token != null) {
                    System.out.println(token);
                    completion.onComplete(token);
                }
            } catch (IOException e) {
                // The fetchToken() method handles Google-specific exceptions,
                // so this indicates something went wrong at a higher level.
                // TIP: Check for network connectivity before starting the AsyncTask.
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            }
            return token;
        }

        /**
         * Gets an authentication token from Google and handles any
         * GoogleAuthException that may occur.
         */
        protected String fetchToken() throws IOException, GoogleAuthException {
            try {
                return gAccount.getToken();
            } catch (UserRecoverableAuthException e) {
                // GooglePlayServices.apk is either old, disabled, or not present
                // so we need to show the user some UI in the activity to recover.
                 mActivity.startActivityForResult(e.getIntent(), AUTH_REQUEST_CODE);
            } catch (GoogleAuthException fatalException) {
                // Some other type of unrecoverable exception has occurred.
                // Report and log the error as appropriate for your app.
            }
            //return gAccount.getToken();
            return null;
        }
    }



}

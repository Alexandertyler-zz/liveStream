package edu.berkeley.remem.livestream;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;


public class SetUp extends Activity {

    private static CreateBroadcast createBroadcast;
    private static JsonComm jsonComm;
    private static LocationManager locationManager;
    private static LocationListener locationListener;
    private double latitude;
    private double longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);

        createBroadcast = new CreateBroadcast();

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
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 0, locationListener);
        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Get update every 5 seconds
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, locationListener);
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
        InputStream iStream = getResources().openRawResource(getResources().getIdentifier("client_secrets", "raw", getPackageName()));
        createBroadcast.start(iStream);
    }

    public void sendJsonPacket(View view) {
        EditText url = (EditText) findViewById(R.id.urlValue);
        EditText jsonData = (EditText) findViewById(R.id.jsonData);

        //Debug info
        System.out.println("Received data from EditText fields:");
        System.out.println(url.getText().toString());
        System.out.println(jsonData.getText().toString());


        String[] jsonDataSplit = jsonData.getText().toString().split(" |=");
        Map<String, String> jsonMap = new HashMap<String, String>();
        for (int i=0; i < jsonDataSplit.length-1; ) {
            jsonMap.put(jsonDataSplit[i++], jsonDataSplit[i++]);
        }

        jsonComm = new JsonComm(url.getText().toString(), jsonMap);

        try {
            jsonComm.execute();
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void sendLocation(View view) {
        EditText url = (EditText) findViewById(R.id.urlValue);
        //EditText jsonData = (EditText) findViewById(R.id.jsonData);

        //Debug info
        System.out.println("Received data from EditText fields:");
        System.out.println(url.getText().toString());
        //System.out.println(jsonData.getText().toString());


        //String[] jsonDataSplit = jsonData.getText().toString().split(" |=");
        Map<String, String> jsonMap = new HashMap<String, String>();
        //for (int i=0; i < jsonDataSplit.length-1; ) {
            //jsonMap.put(jsonDataSplit[i++], jsonDataSplit[i++]);
        //}

        jsonMap.put("longitude", String.valueOf(longitude));
        jsonMap.put("latitude", String.valueOf(latitude));

        jsonComm = new JsonComm(url.getText().toString(), jsonMap);

        try {
            jsonComm.execute();
        } catch (Exception e) {
            System.out.println(e);
        }

    }

}

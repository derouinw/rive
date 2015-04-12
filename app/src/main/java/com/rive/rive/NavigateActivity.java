package com.rive.rive;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class NavigateActivity extends ActionBarActivity {
    String KEY = "key=" + getString(R.string.yelp_api_key);
    private static final String REQ = "https://maps.googleapis.com/maps/api/directions/json?";

    RequestQueue queue;

    LocationManager locationManager;
    LocationListener locationListener;
    Location loc;

    String name;
    String origin;
    String destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new android.location.LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                loc = location;
                origin = loc.getLatitude() + "," + loc.getLongitude();
                getDirections();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        Intent intent = getIntent();

        name = intent.getStringExtra(MainActivity.NAME_EXTRA);
        origin = intent.getStringExtra(MainActivity.ORIGIN_EXTRA);
        destination = intent.getStringExtra(MainActivity.DEST_EXTRA);

        TextView tv = (TextView)findViewById(R.id.hintText);
        tv.setText("The name of the place is: " + name);

        queue = Volley.newRequestQueue(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    private void getDirections() {
        String request = REQ + KEY + "&origin=" + origin + "&destination=" + destination;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, request,
                new Response.Listener<String>() {

                    public void onResponse(String response) {
                        System.out.println("Response is: " + response);

                        try {
                            JSONObject jo = new JSONObject(response);
                            JSONObject result = jo.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps").getJSONObject(0);
                            String instructions = result.getString("html_instructions");

                            TextView next = (TextView)findViewById(R.id.nextText);
                            next.setText(Html.fromHtml(instructions));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("That didn't work!");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_navigate, menu);
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

    @Override
    public void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListener);
    }
}

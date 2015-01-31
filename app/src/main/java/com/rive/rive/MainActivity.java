package com.rive.rive;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends ActionBarActivity {
    public static final String API_KEY = "AIzaSyCM039Crt5GFOee0R7-CHeZeL6ORuTOTPI";
    private static final String REQ = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    private static final String LOC_ID = "location=";
    private static final String KEY_ID = "key=";
    private static final String RADIUS_ID = "radius=";
    private static final String TYPES_ID = "types=";
    private static final String MAXPRICE_ID = "maxprice=";

    public static final String NAME_EXTRA = "com.rive.rive.NAME";
    public static final String ORIGIN_EXTRA = "com.rive.rive.ORIGIN";
    public static final String DEST_EXTRA = "com.rive.rive.DESTINATION";

    double lat, lng;
    Location loc;
    JSONObject result;

    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button request = (Button)findViewById(R.id.submit);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                loc = location;
                request.setText("Submit request");
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

    }

    @Override
    public void onStart() {
        super.onStart();

        //apiClient.connect();
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void request(View view) {
        String location = LOC_ID + loc.getLatitude() + "," + loc.getLongitude() + "&";

        boolean isMeal = ((RadioButton)findViewById(R.id.mealButton)).isChecked();
        boolean isCheap = ((RadioButton)findViewById(R.id.cheapButton)).isChecked();
        boolean isClose = ((RadioButton)findViewById(R.id.closeButton)).isChecked();

        String radius = RADIUS_ID + ((isClose) ? "5000" : "10000") + "&";
        String types = TYPES_ID + ((isMeal) ? "restaurant" : "bar") + "&";
        String maxPrice = MAXPRICE_ID + ((isCheap) ? "2" : "3") + "&";
        String key = KEY_ID + API_KEY;

        String request = REQ + location + radius + types + maxPrice + key;
        System.out.println(request);

        final TextView tv = (TextView)findViewById(R.id.text);
        final Button b = (Button)findViewById(R.id.navigate);

        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, request,
                new Response.Listener<String>() {

                    public void onResponse(String response) {
                        System.out.println("Response is: " + response);

                        try {
                            JSONObject jo = new JSONObject(response);
                            result = jo.getJSONArray("results").getJSONObject(0);
                            String name = result.getString("name");

                            lat = result.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                            lng = result.getJSONObject("geometry").getJSONObject("location").getDouble("lng");

                            tv.setText(name + " has been chosen");
                            b.setVisibility(View.VISIBLE);

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

    public void navigate(View view) {
        String destination = lat + "," + lng;
        String origin = loc.getLatitude() + "," + loc.getLongitude();

        Intent intent = new Intent(this, InstructionActivity.class);

        // Data for "hints"
        String name = "";
        try {
            name = result.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        intent.putExtra(NAME_EXTRA, name);
        intent.putExtra(ORIGIN_EXTRA, origin);
        intent.putExtra(DEST_EXTRA, destination);

        startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onStop() {
        super.onStop();
        //apiClient.disconnect();
    }

    private void stopLocationUpdates() {
        locationManager.removeUpdates(locationListener);
    }
}

package com.rive.rive;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;


public class MainActivity extends ActionBarActivity {
    public static final String NAME_EXTRA = "com.rive.rive.NAME";
    public static final String ORIGIN_EXTRA = "com.rive.rive.ORIGIN";
    public static final String DEST_EXTRA = "com.rive.rive.DESTINATION";

    // Yelp api information
    String YELP_CONSUMER_KEY;
    String YELP_CONSUMER_SECRET;
    String YELP_TOKEN;
    String YELP_TOKEN_SECRET;
    String YELP_API_PATH;
    String YELP_SEARCH_PATH;

    // Yelp constants
    String MEAL_CATEGORIES;
    String SNACK_CATEGORIES;
    String RADIUS_CLOSE;
    String RADIUS_FAR;

    double lat, lng;
    Location loc;
    JSONObject result;

    LocationManager locationManager;
    LocationListener locationListener;
    private LocationService locationService;
    private boolean isBound = false;

    OAuthService yelpService;
    Token yelpAccessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        YELP_CONSUMER_KEY = getString(R.string.yelp_consumer_key);
        YELP_CONSUMER_SECRET = getString(R.string.yelp_consumer_secret);
        YELP_TOKEN = getString(R.string.yelp_token);
        YELP_TOKEN_SECRET = getString(R.string.yelp_token_secret);
        YELP_API_PATH = getString(R.string.yelp_api_path);
        YELP_SEARCH_PATH = getString(R.string.yelp_search_path);

        MEAL_CATEGORIES = getString(R.string.yelp_meal_categories);
        SNACK_CATEGORIES = getString(R.string.yelp_snack_categories);
        RADIUS_CLOSE = getString(R.string.yelp_radius_close);
        RADIUS_FAR = getString(R.string.yelp_radius_far);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button request = (Button)findViewById(R.id.submit);
        request.setEnabled(false);

        // Create and initialize yelp OAuth 1.0a service
        yelpService = new ServiceBuilder().provider(YelpAPI.class).apiKey(YELP_CONSUMER_KEY)
                .apiSecret(YELP_CONSUMER_SECRET).build();
        yelpAccessToken = new Token(YELP_TOKEN, YELP_TOKEN_SECRET);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @OverridePutting work
        public void onServiceConnected(ComponentName name, IBinder service) {
            locationService = ((LocationService.LocationBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            locationService = null; // :(
        }
    };

    void doBindService() {
        bindService(new Intent(getApplicationContext(), LocationService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        isBound = true;
    }

    void doUnbindService() {
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        //apiClient.connect();
        doBindService();
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
        boolean isMeal = ((RadioButton)findViewById(R.id.mealButton)).isChecked();
        boolean isCheap = ((RadioButton)findViewById(R.id.cheapButton)).isChecked();
        boolean isClose = ((RadioButton)findViewById(R.id.closeButton)).isChecked();

        String categories, radius;
        categories = (isMeal) ? MEAL_CATEGORIES : SNACK_CATEGORIES;
        radius = (isClose) ? RADIUS_CLOSE : RADIUS_FAR;

        searchYelp(loc, categories, radius);
    }

    public void navigate(View view) {
        String destination = lat + "," + lng;
        String origin = loc.getLatitude() + "," + loc.getLongitude();

        Intent intent = new Intent(this, InstructionActivity.class);

        // Data for "hints"
        /*String name = "";
        try {
            name = result.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        intent.putExtra(NAME_EXTRA, name);
        intent.putExtra(ORIGIN_EXTRA, origin);
        intent.putExtra(DEST_EXTRA, destination);*/

        startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        doUnbindService();
    }

    @Override
    public void onStop() {
        super.onStop();
        //apiClient.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    private void stopLocationUpdates() {
        locationManager.removeUpdates(locationListener);
    }

    /* Yelp API OAuth */

    private void searchYelp(Location location, String categories, String radius) {
        String latitude = String.valueOf(location.getLatitude());
        String longitude = String.valueOf(location.getLongitude());
        String loc = latitude + "," + longitude;

        new SendYelpRequest().execute(YELP_API_PATH + YELP_SEARCH_PATH, "ll", loc, "category_filter", categories, "radius_filter", radius);
    }

    private class SendYelpRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            OAuthRequest request = new OAuthRequest(Verb.GET, params[0]);

            // Key, Value pairs input in params
            for (int i = 1; i < params.length; i+=2) {
                request.addQuerystringParameter(params[i], params[i+1]);
            }

            yelpService.signRequest(yelpAccessToken, request);

            Log.d("SENDING", request.getCompleteUrl());
            Response response = request.send();
            Log.d("YELP", response.getBody());
            return response.getBody();
        }

        @Override
        protected void onPostExecute(String response) {
            String business = "Business not found";

            // Load business data from JSON
            try {
                JSONObject jo = new JSONObject(response);
                JSONArray businesses = jo.getJSONArray("businesses");

                JSONObject first = businesses.getJSONObject(0);
                business = first.getString("name");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            TextView tv = (TextView)findViewById(R.id.text);
            tv.setText(business);

            Button navigate = (Button)findViewById(R.id.navigate);
            navigate.setVisibility(View.VISIBLE);
        }
    }
}

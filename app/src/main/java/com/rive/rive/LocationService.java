package com.rive.rive;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

public class LocationService extends Service {
    public static final String LOCATION_ACTION = "com.rive.rive.LOCATION_ACTION";
    public static final String LOCATION_EXTRA = "com.rive.rive.LOCATION_EXTRA";

    LocationManager locationManager;
    LocationListener locationListener;
    Location loc;

    public LocationService() {
    }

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocationBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }

    @Override
    public void onCreate() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                loc = location;
                Intent intent = new Intent(LOCATION_ACTION);
                intent.putExtra(LOCATION_EXTRA, loc);
                sendBroadcast(intent);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    public Location getLocation() throws Exception {
        if (loc != null)
            return loc;
        else
            throw new Exception("Location not received");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        locationManager.removeUpdates(locationListener);
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocationBinder();
}

package com.cs110.lit.adventour;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.location.LocationListener;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class StartTourActivity extends FragmentActivity implements OnMapReadyCallback {

    // Time before refresh (miliseconds).
    private static final long LOCATION_REFRESH_TIME = 100;
    // Distance change before refresh (meters).
    private static final float LOCATION_REFRESH_DISTANCE = 1.0f;

    // Request Constant.
    private static final int LOCATION_REQUEST_CODE = 0;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_tour);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // for communication with the last/previews view
        Intent intent = getIntent();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        ///// -------------- adding listener ---------------///
        // Acquire a reference to the system Location Manager
        locationManager =
                (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        /////---------- permission check -------------////
        // Register the listener with the Location Manager to receive location updates
        if (!hasMapPermission()) {
            // Ask for permission and return.
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                    LOCATION_REQUEST_CODE);
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, locationListener);

        String locationNetworkProvider = LocationManager.GPS_PROVIDER;

        // Return could be null but addMarkerAtMyLocation checks for this.
        Location lastKnownLocation = locationManager.getLastKnownLocation(locationNetworkProvider);

        ////----------- display the map with marker on current location -----------//
        addMarkerAtMyLocation(lastKnownLocation);
    }

    /**
     * Simple helper function to set a marker at your current location.
     */
    private void addMarkerAtMyLocation(Location loc) {
        if(loc == null)
            return;
        // Add a marker in current location, and move the camera.
        LatLng myLocation =
                new LatLng(loc.getLatitude(), loc.getLongitude());
        mMap.addMarker(new MarkerOptions().position(myLocation).title("Marker in my location"));
        // zoom in to the current location in camera view
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 13));
    }


    /**
     * Function to check map permissions.
     * Returns true if permissions granted. Checks only fine location.
     */
    private boolean hasMapPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Function to request permission.
     * So far, only returns true.
     */
    private boolean requestPermission(String permName, int permission) {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, permName)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permName)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{permName}, permission);
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                // Null, onMapReady not called yet and yet we have permission request.
                if (locationManager == null || locationListener == null)
                    return;

                // If request is cancelled, the result arrays are empty.
                if (hasMapPermission()) {

                    // Set the location listener.
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                            LOCATION_REFRESH_DISTANCE, locationListener);

                    // More GPS setup.
                    String locationNetworkProvider = LocationManager.GPS_PROVIDER;

                    // Return could be null but addMarkerAtMyLocation checks for this.
                    Location lastKnownLocation =
                            locationManager.getLastKnownLocation(locationNetworkProvider);

                    ////----------- display the map with marker on current location -----------//
                    addMarkerAtMyLocation(lastKnownLocation);
                } else {
                    // Permission denied, return silently.
                    return;
                }
                return;
            }
        }
    }
}
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
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;

import com.cs110.lit.adventour.model.ActiveTourCheckpoint;
import com.cs110.lit.adventour.model.Checkpoint;
import com.cs110.lit.adventour.model.Tour;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class StartTourActivity extends FragmentActivity implements OnMapReadyCallback,
    DB.Callback<Tour>{

    /* Constants */
    // Time before refresh (miliseconds).
    private static final long LOCATION_REFRESH_TIME = 100;
    // Distance change before refresh (meters).
    private static final float LOCATION_REFRESH_DISTANCE = 1.0f;

    // Request Constant.
    private static final int LOCATION_REQUEST_CODE = 0;

    /* Google Maps Related */
    private GoogleMap mMap;

    private LocationManager locationManager;
    private LocationListener locationListener;

    /* Tour Model Related */
    private int tourID = 1;

    // Fragment Manager for checkpoint displays.
    public final FragmentManager fManager = getSupportFragmentManager();

    //Make a list of active checkpoints
    private ArrayList<ActiveTourCheckpoint> activePointList =
            new ArrayList<ActiveTourCheckpoint>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_tour);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // For communication with the last/previews view
        Intent intent = getIntent();
        // Grab our database.
        //tourID = intent.getIntExtra("tourID", 0);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        ///// -------------- adding listener ---------------///
        // Acquire a reference to the system Location Manager
        locationManager =
                (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);



        /////---------- permission check -------------////
        // Register the listener with the Location Manager to receive location updates
        if (!hasMapPermission()) {
            // Ask for permission and return.
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                    LOCATION_REQUEST_CODE);
            return;
        }


        // Get tour, son!
        DB.getTourById(tourID, this, this);
    }

    /**
     * Simple helper function to set a marker at your current location.
     */
    private void addMarkerAtMyLocation(LatLng latLng) {
        if(latLng == null)
            return;
        mMap.addMarker(new MarkerOptions().position(latLng));
        // zoom in to the current location in camera view
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
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
                    // Get tour, son!
                    DB.getTourById(tourID, this, this);
                } else {
                    // Permission denied, return silently.
                    return;
                }
                return;
            }
        }
    }

    /* Function called when everything with settings is kosher. Start setting up markers. */
    @Override
    public void onSuccess(Tour tour) {

        //access the list of checkpoints
        ArrayList<Checkpoint> checkpointList = tour.getListOfCheckpoints();

        // go through all the checkpoints and make them activeCheckpoints
        for(Checkpoint points : checkpointList) {

            //Make the checkpoint list a list of active checkpoints
            activePointList.add(new ActiveTourCheckpoint(points.getCheckpoint_id(),
                    points.getLatitude(), points.getLongitude(), points.getTour_id(),
                    points.getTitle(), points.getDescription(),
                    points.getPhoto(),
                    points.getOrder_num(),false, false, false,true));
            ; //bool startPoint, bool FinishPoint, bool Finished, bool Upcoming
        }

        //Now go back and set the first and last activeCheckpoints as the start and finish
        //point
        (activePointList.get(0)).setStartPoint(true);
        (activePointList.get(activePointList.size() - 1)).setFinishPoint(true);


        // Define a listener that responds to location updates
        locationListener = new LocationListener() {

            /*ADDED BY LIZ */
            // Called when a new location is found by the network location provider.
            public void onLocationChanged(Location location) {

                //get the location's current coordinates
                double currentLat = location.getLatitude();
                double currentLong= location.getLongitude();

                // declare an array to store the distance in between
                float[] results = new float[4];

                //Check to see if we are near the next checkpoint
                //results[0] will store the distance between the two
                location.distanceBetween(currentLat,currentLong, (activePointList.get(0)).getLatitude(),
                        (activePointList.get(0)).getLongitude(), results);

                //TODO: figure out which distance we need to be away from to get a notification
                if(results[0] < LOCATION_REFRESH_DISTANCE &&
                        !((activePointList.get(0)).getReachedPoint())){

                    //notify the user they are approaching a checkpoint
                    System.out.println("Suh' dude you're near " +
                            (activePointList.get(0)).getTitle() +
                            ". It's getting LIT fam! ");

                    //reset the distance
                    results[0] = 0;

                    //mark checkpoint as visited
                    //Technically this is a waste if i'm deleting it after
                    (activePointList.get(0)).setReachedPoint(true);
                    (activePointList.get(0)).setuUpcomingPoint(false);

                    //removing the checkpoint from the front of the list
                    activePointList.remove(0);

                }




            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        // Failed requesting tour, silden
        if(tour == null)
            return ;

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, locationListener);

        String locationNetworkProvider = LocationManager.GPS_PROVIDER;

        // Return could be null but addMarkerAtMyLocation checks for this.
        Location lastKnownLocation = locationManager.getLastKnownLocation(locationNetworkProvider);

        ////----------- display the map with marker on current location -----------//
        //addMarkerAtMyLocation(new LatLng(lastKnownLocation.getLatitude(),
        //        lastKnownLocation.getLongitude()));


        // Create lines.
        PolylineOptions lineOptions = new PolylineOptions();

        // Display markers.
        for(Checkpoint points : checkpointList) {
            LatLng latLng = new LatLng(points.getLatitude(), points.getLongitude());
            addMarkerAtMyLocation(latLng);
            lineOptions.add(latLng);
        }

        // Draw the entire line.
        Polyline line = mMap.addPolyline(lineOptions);
        // If you want to set line settings, do them here with polyline object.
        // Ex) line.color(Color.RED);
    }

    @Override
    public void onFailure(Tour tour) {
        System.out.println("Failed to get tours.");
    }
}
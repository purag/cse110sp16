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

import com.cs110.lit.adventour.model.Tour;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<Tour> nearbyTours;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
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
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                // makeUseOfNewLocation(location);
            }
        };

        /////---------- permission check -------------////
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        String locationNetworkProvider = LocationManager.NETWORK_PROVIDER;
        Location lastKnownLocation = locationManager.getLastKnownLocation(locationNetworkProvider);

        ////----------- display the map with marker on current location -----------//
        // Add a marker in current location, and move the camera.
        LatLng myLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(myLocation).title("Marker in my location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,13));

        //print
        System.out.println("Gee I'm trying\n");

        //grab data
        displayNearbyTours(myLocation, mMap);


    }

     private void displayNearbyTours(LatLng myLocation, final GoogleMap mMap){
         //grab data
         DB.getToursNearLoc(myLocation.latitude, myLocation.longitude, 25.0, 10, this, new DB.Callback<ArrayList<Tour>>() {
             @Override
             public void onSuccess(ArrayList<Tour> tours) {
                 for (Tour t : tours) {
                     System.out.println("Tour name: " + t.getTitle());
                     System.out.println("Tour summary: " + t.getSummary());
                     System.out.println("Tour lat/lon: (" + t.getStarting_lat() +
                             "," + t.getStarting_lon() + ")");
                 }

                 //get the tours
                 nearbyTours = tours;

                 //display them
                 for (Tour t : nearbyTours) {
                     LatLng location = new LatLng(t.getStarting_lat(), t.getStarting_lon());
                     mMap.addMarker(new MarkerOptions().position(location)
                             .title(t.getTitle())
                             .snippet(t.getSummary()));
                 }


             }

             @Override
             public void onFailure(ArrayList<Tour> tours) {
                 System.out.println("On failure happened\n");
             }
         });
     }
}

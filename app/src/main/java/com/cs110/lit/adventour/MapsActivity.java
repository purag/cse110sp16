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
import android.support.v4.content.ContextCompat;

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

    // Request Constant.
    private static final int LOCATION_REQUEST_CODE = 0;
    private GoogleMap mMap;
    private ArrayList<Tour> nearbyTours;
    private Location lastKnownLocation;
    private LocationManager locationManager;

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
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                //I wonder if this will work
                displayNearbyTours(new LatLng(location.getLatitude(),location.getLongitude()), mMap);
            }
        };


        String locationNetworkProvider = LocationManager.NETWORK_PROVIDER;

        //check fine
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED /*&& ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED*/) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }      System.out.println("got fine permission");

        //check coarse
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }      System.out.println("got coarse permission");

        Location lastKnownLocation = locationManager.getLastKnownLocation(locationNetworkProvider);

        if(lastKnownLocation == null){
            System.out.println("NULL location");
            ////----------- display the map with marker on current location -----------//
            // Add a marker in current location, and move the camera.
            LatLng myLocation = new LatLng(33.812, 117.919);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(13));
            //mMap.addMarker(new MarkerOptions().position(myLocation).title("Marker in my location"));

            //print
            System.out.println("Gee I'm trying\n");

            //grab data
            displayNearbyTours(myLocation, mMap);
        }
        else{

          ////----------- display the map with marker on current location -----------//
            // Add a marker in current location, and move the camera.
            LatLng myLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(myLocation).title("Marker in my location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

            //print
            System.out.println("Gee I'm trying\n");

            //grab data
            displayNearbyTours(myLocation, mMap);
        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        //show me what you got
        int i = 0;
        for( String s : permissions){
            System.out.println("at request permission result" + s + grantResults[i] + "\n");
            i++;
        }
        System.out.println("Trying again");
        onMapReady(mMap);
        return;

    }


     private void displayNearbyTours(final LatLng myLocation, final GoogleMap mMap){
         mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
         mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));

         System.out.println("attempting to grab data\n");
         DB.getToursNearLoc(myLocation.latitude, myLocation.longitude, 50000.0, 10, this, new DB.Callback<ArrayList<Tour>>() {
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
                     System.out.println("Let's drop some pins");
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

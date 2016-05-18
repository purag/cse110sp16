package com.cs110.lit.adventour;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cs110.lit.adventour.model.Tour;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Request Constant.
    private static final int LOCATION_REQUEST_CODE = 0;
    private GoogleMap mMap;
    private ArrayList<Tour> nearbyTours;
    private Location lastKnownLocation;
    private LocationManager locationManager;
    //private ImageButton listButton;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // for communication with the last/previews view
        Intent intent = getIntent();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());

        /*listButton = (ImageButton) findViewById(R.id.listButton);
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("I'm clicked");
                showListView();
            }
        });*/

        ///// -------------- adding listener ---------------///
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                //I wonder if this will work
                displayNearbyTours(new LatLng(location.getLatitude(), location.getLongitude()), mMap);
            }
        };


        String locationNetworkProvider = LocationManager.NETWORK_PROVIDER;

        //check fine
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED /*&& ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED*/) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }
        System.out.println("got fine permission");

        //check coarse
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }
        System.out.println("got coarse permission");

        Location lastKnownLocation = locationManager.getLastKnownLocation(locationNetworkProvider);

        if (lastKnownLocation == null) {
            System.out.println("NULL location");
            ////----------- display the map with marker on current location -----------//
            // Add a marker in current location, and move the camera.
            LatLng myLocation = new LatLng(33.812, -117.919);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(13));
            //mMap.addMarker(new MarkerOptions().position(myLocation).title("Marker in my location"));

            //print
            System.out.println("Gee I'm trying\n");

            //grab data
            displayNearbyTours(myLocation, mMap);
        } else {

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
        for (String s : permissions) {
            System.out.println("at request permission result" + s + grantResults[i] + "\n");
            i++;
        }
        System.out.println("Trying again");
        onMapReady(mMap);
        return;

    }

    /**
     * load map action
     */
    public void showListView () {
        Intent intent = new Intent(this, BrowseListActivity.class);
        startActivity(intent);
        finish();
    }

    private void displayNearbyTours(final LatLng myLocation, final GoogleMap mMap) {
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));

        System.out.println("attempting to grab data\n");
        DB.getToursNearLoc(myLocation.latitude, myLocation.longitude, 5000.0, 10, this, new DB.Callback<ArrayList<Tour>>() {
            @Override
            public void onSuccess(ArrayList<Tour> tours) {
                System.out.println("success\n");
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
                            .snippet(t.getSummary())
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
                }

            }

            @Override
            public void onFailure(ArrayList<Tour> tours) {
                System.out.println("On failure happened\n");
            }
        });

    }


    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.cs110.lit.adventour/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.cs110.lit.adventour/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }


    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        MyInfoWindowAdapter(){
            myContentsView = getLayoutInflater().inflate(R.layout.custom_window_info_contents, null);
        }

        @Override
        public View getInfoContents(Marker marker) {

            TextView tvTitle = ((TextView)myContentsView.findViewById(R.id.title));
            tvTitle.setText(marker.getTitle());
            TextView tvSnippet = ((TextView)myContentsView.findViewById(R.id.snippet));
            tvSnippet.setText(marker.getSnippet());

            return myContentsView;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            // TODO Auto-generated method stub
            return null;
        }

    }

}

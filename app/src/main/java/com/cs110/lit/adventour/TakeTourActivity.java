package com.cs110.lit.adventour;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cs110.lit.adventour.model.ActiveTourCheckpoint;
import com.cs110.lit.adventour.model.Checkpoint;
import com.cs110.lit.adventour.model.Tour;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/**
 * Created by Izhikevich on 5/20/16.
 */
public class TakeTourActivity extends AppCompatActivity implements OnMapReadyCallback{

    // Request Constant.
    private static final int LOCATION_REQUEST_CODE = 0;
    // Distance change before refresh (meters).
    private static final float LOCATION_REFRESH_DISTANCE = 1.0f;

    private GoogleMap mMap;

    private android.location.LocationListener locationListener;
    private LocationManager locationManager;

    //Make a list of active checkpoints
    private ArrayList<ActiveTourCheckpoint> activePointList =
            new ArrayList<ActiveTourCheckpoint>();


    public static final String TOUR_ID = "tour_id";
    public static final String TOUR_TITLE = "tour_title";
    /* Tour Model Related */
    private int tourID = 7;
    private String tourTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse_map_content);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // for communication with the last/previews view
        Intent intent = getIntent();
        //get the tour id entered, -1 for bad input
        tourID = intent.getIntExtra(TOUR_ID, -1);
        tourTitle = intent.getStringExtra(TOUR_TITLE);
        getSupportActionBar().setTitle(tourTitle);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());

        ///// -------------- adding listener ---------------///
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                //I wonder if this will work
                displayNearbyCheckpoints(new LatLng(location.getLatitude(), location.getLongitude()),
                        mMap);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };


        String locationNetworkProvider = LocationManager.NETWORK_PROVIDER;

        //check fine
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED /*&& ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED*/) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }

        //check coarse
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }

        /* set up refresh and zoom buttons */
        ImageButton zoomIn = (ImageButton) findViewById(R.id.zoomIn);
        ImageButton zoomOut = (ImageButton) findViewById(R.id.zoomOut);
        zoomIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });
        zoomOut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });
        mMap.setMyLocationEnabled(true);

        Location lastKnownLocation = locationManager.getLastKnownLocation(locationNetworkProvider);

        if (lastKnownLocation == null) {
            System.out.println("NULL location");
            ////----------- display the map with marker on current location -----------//
            // Add a marker in current location, and move the camera.
            LatLng myLocation = new LatLng(33.812, -117.919);

            //grab data
            displayNearbyCheckpoints(myLocation, mMap);
        } else {

            ////----------- display the map with marker on current location -----------//
            // Add a marker in current location, and move the camera.
            LatLng myLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

            //grab data
            displayNearbyCheckpoints(myLocation, mMap);
        }

    }

    private void displayNearbyCheckpoints(final LatLng myLocation, final GoogleMap mMap) {
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));

        DB.getTourById(tourID,  this, new DB.Callback<Tour>() {

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
                    //bool startPoint, bool FinishPoint, bool Finished, bool Upcoming
                }

                //Now go back and set the first and last activeCheckpoints as the start and finish
                //point
                (activePointList.get(0)).setStartPoint(true);
                (activePointList.get(activePointList.size() - 1)).setFinishPoint(true);


                // Create lines.
                PolylineOptions lineOptions = new PolylineOptions();

                boolean startPoint = false;
                boolean endPoint = false;

                // Display markers.
                for(Checkpoint points : checkpointList) {
                    if (checkpointList.get(0) == points) {
                        startPoint = true;
                    } else if (checkpointList.get(checkpointList.size() - 1) == points){
                        endPoint = true;
                    }
                    LatLng latLng = new LatLng(points.getLatitude(), points.getLongitude());
                    addMarkerAtMyLocation(latLng,points,startPoint,endPoint);
                    lineOptions.add(latLng);

                    startPoint = false;
                    endPoint = false;
                }

                // Draw the entire line.
                Polyline line = mMap.addPolyline(lineOptions);


                System.out.println("attempting to grab data\n");

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

            }

            @Override
            public void onFailure(Tour tour) {
                System.out.println("On failure happened\n");
            }
        });


    }


    /**
     * Simple helper function to set a marker at your current location.
     */
    private void addMarkerAtMyLocation(LatLng latLng, Checkpoint point,
                                       boolean startPoint, boolean endPoint) {
        if(latLng == null)
            return;

        if(startPoint){
            mMap.addMarker(new MarkerOptions().position(latLng)
                    .title(point.getTitle())
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_start)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        }
        else if(endPoint){
            mMap.addMarker(new MarkerOptions().position(latLng)
                    .title(point.getTitle())
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_end)));
        }
        else {
            mMap.addMarker(new MarkerOptions().position(latLng)
                    .title(point.getTitle())
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
        }

        // zoom in to the current location in camera view
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
        System.err.println("The checkpoint you just added is at " + latLng);
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

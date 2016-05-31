package com.cs110.lit.adventour;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.cs110.lit.adventour.model.ActiveTourCheckpoint;
import com.cs110.lit.adventour.model.Checkpoint;
import com.cs110.lit.adventour.model.Tour;
import com.github.clans.fab.FloatingActionButton;
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
public class TakeTourActivity extends AppCompatActivity implements OnMapReadyCallback,  TakeTourCheckpointFragment.TakeTourCheckpointListener{
    // Request Constant.
    private static final int LOCATION_REQUEST_CODE = 0;
    // Distance change before refresh (meters).
    private static final float LOCATION_REFRESH_DISTANCE = 1.0f;

    private GoogleMap mMap;
    private LocationManager locationManager;

    //Make a list of active checkpoints
    private ArrayList<ActiveTourCheckpoint> activePointList = new ArrayList<>();
    private ArrayList<Checkpoint> checkpointList = new ArrayList<>();
    private ArrayList<Marker> markerList = new ArrayList<>();

    //To keep track of which checkpoint we are on
    private int upComingCheckpoint = 0;

    public static final String TOUR_ID = "tour_id";
    public static final String TOUR_TITLE = "tour_title";
    /* Tour Model Related */
    private int tourID;
    private String tourTitle;

    private String photo;
    private ProgressDialog progressDialog;
    private DialogFragment checkpointFragment;
    public boolean backpressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // launch map view
        setContentView(R.layout.activity_take_tour);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // for communication with the last/previews view
        Intent intent = getIntent();
        //get the tour id entered, -1 for bad input
        tourID = intent.getIntExtra(TOUR_ID, -1);
        tourTitle = intent.getStringExtra(TOUR_TITLE);
        getSupportActionBar().setTitle(tourTitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }


    /* Render the Map*/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.setInfoWindowAdapter(new MyInfoWindowAdapter(this));

        //check fine
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }
        //check coarse
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }

        mMap.setMyLocationEnabled(true);

        /* set up refresh and zoom buttons */
        setUpZoomButtonsOnMap();

        /* display the checkpoints for the particular tour */
        displayNearbyCheckpoints(mMap);

        /* set up FloatingActionButton clicks */
        actionsForFABs();

        ///// -------------- adding listener ---------------///
        // Acquire a reference to the system Location Manager
        //this will help us determine where to render the map near
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        //this will check whether we have reached a checkpoint
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                System.err.println("Location listener was used!!");

                checkUsersLocation(location);
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };


        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    private void setUpZoomButtonsOnMap() {
        ImageButton zoomIn = (ImageButton) findViewById(R.id.zoomIn);
        ImageButton zoomOut = (ImageButton) findViewById(R.id.zoomOut);

        assert zoomIn != null;
        zoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });

        assert zoomOut != null;
        zoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });
    }

    private void displayNearbyCheckpoints(final GoogleMap mMap) {

        DB.getTourById(tourID,  this, new DB.Callback<Tour>() {
            @Override
            public void onSuccess(Tour tour) {
                //fill up the checkpoint lists
                populateCheckpointLists(tour);
                // Create lines.
                PolylineOptions lineOptions = new PolylineOptions();

                // Display markers.
                for(ActiveTourCheckpoint points : activePointList) {

                    LatLng latLng = new LatLng(points.getLatitude(), points.getLongitude());
                    addMarkerAtMyLocation(latLng,points);
                    lineOptions.add(latLng);
                }

                // Draw the entire line.
                Polyline line = mMap.addPolyline(lineOptions);
            }

            @Override
            public void onFailure(Tour tour) {
                System.out.println("On failure happened when rendering the checkpoints on the" +
                        "take tour map\n");
            }
        });

    }

    /* Used to fill up the checkpoint lists with the tour's checkpoint */
    private void populateCheckpointLists(Tour tour) {

        //access the list of checkpoints
        checkpointList = tour.getListOfCheckpoints();

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

    }

    /* Used to see if a user is near a checkpoint */
    private void checkUsersLocation(Location location) {
        //get the location's current coordinates
        double currentLat = location.getLatitude();
        double currentLong = location.getLongitude();

        // declare an array to store the distance in between
        float[] results = new float[4];

        //Check to see if we are near the next checkpoint
        //results[0] will store the distance between the two
        location.distanceBetween(currentLat, currentLong,
                (activePointList.get(upComingCheckpoint)).getLatitude(),
                (activePointList.get(upComingCheckpoint)).getLongitude(), results);

        //TODO: figure out which distance we need to be away from to get a notification
        if (results[0] < /*LOCATION_REFRESH_DISTANCE*/20 &&
                !((activePointList.get(upComingCheckpoint).getReachedPoint()))) {

            movetoNextCheckpoint();
            //reset the distance
            results[0] = 0;

        }
        else{
            System.out.println("too far, reuslt[0] was " + results[0]);
            System.out.println("min distance is " +LOCATION_REFRESH_DISTANCE);

        }
    }


    /* action bar back button override */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                backpressed = true;
                alertWhenPressBackButton();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        backpressed = true;
        alertWhenPressBackButton();
    }

    private void alertWhenPressBackButton() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_warning_black)
                .setTitle("Ending Tour")
                .setMessage("Are you sure you want to end this tour without saving?\nPress Exit on floating menu to save it")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        backToOverviewView(tourID);
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void backToOverviewView(Integer tourID) {
        Intent intent = new Intent(this, OverviewActivity.class);
        intent.putExtra(OverviewActivity.TOUR_ID, tourID.intValue());
        startActivity(intent);
        finish();
    }

    /* button listener for the  floating action bar */
    private void actionsForFABs() {

        FloatingActionButton skipFab = (FloatingActionButton) findViewById(R.id.skip_checkpoint_button);
        if (skipFab != null)
            skipFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    movetoNextCheckpoint();
                    //floatingActionsMenu.close(true);
                }
            });

        FloatingActionButton undoFab = (FloatingActionButton) findViewById(R.id.undo_skip_checkpoint);
        if (undoFab != null)
            undoFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    movetoBackACheckpoint();
                    //floatingActionsMenu.close(true);
                }
            });

        FloatingActionButton ExitFab = (FloatingActionButton) findViewById(R.id.exit_take_tour);
        if (ExitFab != null)
            ExitFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ExitFromTakeTour(false);
                }
            });
    }

    private void ExitFromTakeTour(boolean finishedLastCheckpoint){
        backpressed = true;
        if (!finishedLastCheckpoint) {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_warning_black)
                    .setTitle("Exit Tour")
                    .setMessage("Do you want to save this tour before finish it?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkIfSaveTakenTour();
                            //finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        }
        else {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.icon_good_job)
                    .setTitle("Congratulations!!")
                    .setMessage("You finished this tour! Do you want to save this tour?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkIfSaveTakenTour();
                            //finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        }
    }

    private void movetoNextCheckpoint(){
        //notify the user they are approaching a checkpoint
        //TODO: Insert pop up fragment here to display checkpoint info
        if(backpressed) return;

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("take_tour_checkpoint_dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);


        /* Check to see which photo to display.
           If a photo exists, display that one. Otherwise display google maps
         */
        photo = (activePointList.get(upComingCheckpoint)).getPhoto();

        System.err.println("the current photo is\n " + photo);

        if(photo.equals("http://placehold.it/250x250")){
            //Get the photo from the first checkpoint to load as background
            photo = "https://maps.googleapis.com/maps/api/streetview?size=2400x1200&location=" +
                    Double.toString((activePointList.get(upComingCheckpoint)).getLatitude()) +"," +
                    Double.toString((activePointList.get(upComingCheckpoint)).getLongitude()) +
                    "&heading=200&pitch=10&key=AIzaSyBCQ8q5n2-swQNVzQtxvY8eZv-G7c9DiLc";
        }


        // Create and show the dialog.
        checkpointFragment = TakeTourCheckpointFragment.newInstance(
                (activePointList.get(upComingCheckpoint)).getTitle(),
                (activePointList.get(upComingCheckpoint)).getDescription(),
                photo, this);

        if(checkpointFragment != null) {
            checkpointFragment.show(ft, "take_tour_checkpoint_dialog");
        }

        //edit info for that checkpoint
        (activePointList.get(upComingCheckpoint)).setReachedPoint(true);
        (activePointList.get(upComingCheckpoint)).setuUpcomingPoint(false);

        //To avoid out of index errors
        if(upComingCheckpoint >= activePointList.size()-1){
            ExitFromTakeTour(true);
            return;
        }

        //Only update the markers if they are not the starting or end ones

        // move the view camera to the next checkpoint
        LatLng upcoming_latLng = new LatLng((activePointList.get(upComingCheckpoint+1)).getLatitude(), (activePointList.get(upComingCheckpoint+1)).getLongitude());
        LatLng current_latLng = new LatLng((activePointList.get(upComingCheckpoint)).getLatitude(), (activePointList.get(upComingCheckpoint)).getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(upcoming_latLng, 16));

        //Rerender the map with the updated checkpoints (Whether visited or not)

        if(upComingCheckpoint != 0){
            (markerList.get(upComingCheckpoint)).remove();
            mMap.addMarker(new MarkerOptions().position(current_latLng)
                    .title((activePointList.get(upComingCheckpoint)).getTitle())
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
            (markerList.get(upComingCheckpoint)).remove();
        }

        if(upComingCheckpoint < activePointList.size()-2 ) {
            mMap.addMarker(new MarkerOptions().position(upcoming_latLng)
                    .title((activePointList.get(upComingCheckpoint+1)).getTitle())
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_start)));
        }

        //move onto the next checkpoint
        upComingCheckpoint++;

    }

    private void movetoBackACheckpoint(){
        //notify the user they are approaching a checkpoint
        //TODO: Insert pop up fragment here to display checkpoint info

        //To avoid out of index errors
        if(upComingCheckpoint < 1) return;

        //move back a checkpoint
        upComingCheckpoint--;
        //edit info for that checkpoint
        (activePointList.get(upComingCheckpoint)).setReachedPoint(false);
        (activePointList.get(upComingCheckpoint)).setuUpcomingPoint(true);

        // move the view camera back to the previous upcoming checkpoint
        LatLng previous_latLng = new LatLng((activePointList.get(upComingCheckpoint)).getLatitude(), (activePointList.get(upComingCheckpoint)).getLongitude());
        LatLng up_upcoming_latLng = new LatLng((activePointList.get(upComingCheckpoint+1)).getLatitude(), (activePointList.get(upComingCheckpoint+1)).getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(previous_latLng, 16));

        //Rerender the map with the updated checkpoints (Whether visited or not)
        if(upComingCheckpoint != 0) {
            (markerList.get(upComingCheckpoint)).remove();
            mMap.addMarker(new MarkerOptions().position(previous_latLng)
                    .title((activePointList.get(upComingCheckpoint)).getTitle())
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_start)));
        }
        if(upComingCheckpoint < activePointList.size()-2) {
            (markerList.get(upComingCheckpoint+1)).remove();
            mMap.addMarker(new MarkerOptions().position(up_upcoming_latLng)
                    .title((activePointList.get(upComingCheckpoint+1)).getTitle())
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_gray)));
        }
    }


    private void saveTourToMyTourOnDB(int userID){
        DB.saveTourTakenByUser (tourID, userID, TakeTourActivity.this, new DB.Callback<Integer>() {
            @Override
            public void onSuccess(Integer tour_id) {
                progressDialog.dismiss();
                new AlertDialog.Builder(TakeTourActivity.this)
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).setMessage(tourTitle + " is successfully saved into your tour list").show();
            }

            @Override
            public void onFailure(Integer integer) {
                progressDialog.dismiss();
                new AlertDialog.Builder(TakeTourActivity.this)
                        .setTitle("Upload Failed.")
                        .setMessage("Please try again soon!")
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private void checkIfSaveTakenTour() {
        SharedPreferences myPrefs = getSharedPreferences("Login", -1);
        final int userId = myPrefs.getInt("uid", 0);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading Tour...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        final boolean[] done = {false};

        // Changing to just new function call.
        DB.getToursTakenByUserId (userId, this,  new DB.Callback<ArrayList<Tour>>(){
            @Override
            public void onSuccess(ArrayList<Tour> tours) {
                for (int i = 0; i < tours.size(); i++) {
                    if (!done[0] && tours.get(i).getTour_id() == tourID) {
                        progressDialog.dismiss();
                        done[0] = true;
                        new AlertDialog.Builder(TakeTourActivity.this)
                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                }).setMessage(tourTitle + " has already been saved in your tour list").show();
                    }
                }

                if(!done[0]) {
                    saveTourToMyTourOnDB(userId);
                }
            }

            @Override
            public void onFailure(ArrayList<Tour> tours) {
                //saveTourToMyTourOnDB(userId);
                System.out.println("On failure happened\n");
            }
        });

    }

    /**
     * Simple helper function to set a marker at your current location.
     */
    private void addMarkerAtMyLocation(LatLng latLng, ActiveTourCheckpoint point) {

        Marker marker;

        if(latLng == null)
            return;

        if(point.getStartPoint()){
            marker = mMap.addMarker(new MarkerOptions().position(latLng)
                    .title(point.getTitle())
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.start_flag)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        }
        else if(point.getFinishPoint()){
            marker = mMap.addMarker(new MarkerOptions().position(latLng)
                    .title(point.getTitle())
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.end_flag)));
        }

        else if(point.getReachedPoint()){
            marker = mMap.addMarker(new MarkerOptions().position(latLng)
                    .title(point.getTitle())
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
        }
        else { //else this must be an unvisited checkpoint
            marker = mMap.addMarker(new MarkerOptions().position(latLng)
                    .title(point.getTitle())
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_gray)));
        }

        //Now add this marker into a marker array so we can later update it
        markerList.add(marker);
    }

}

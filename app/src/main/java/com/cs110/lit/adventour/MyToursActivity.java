package com.cs110.lit.adventour;

/**
 * Created by achen on 5/6/16.
 */

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.cs110.lit.adventour.model.Tour;
import com.cs110.lit.adventour.model.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class MyToursActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    // Attributes for action bar
    private DrawerLayout navigationDrawer;
    private ActionBarDrawerToggle navigationToggle;
    private ViewFlipper viewFlipper;
    private SwipeRefreshLayout refreshLayout;

    // Attributes for the list view
    ListView list;
    private final ArrayList<String> TourTitles = new ArrayList<>();
    private final ArrayList<String> TourDescriptions = new ArrayList<>();
    private final ArrayList<String> imageIds = new ArrayList<>();
    private final ArrayList<Integer> TourIDs = new ArrayList<>();
    private final ArrayList<User> TourUsers = new ArrayList<>();

    // Attributes for location
    private static final int LOCATION_REQUEST_CODE = 0;
    private LocationManager locationManager;
    private Location lastKnownLocation;
    private String locationNetworkProvider;

    private GoogleMap mMap;
    private HashMap<String, Integer> markerTable = null;


    // Attributes for log in
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    private TextView NoToursMap;
    private TextView NoToursList;


    /**
     * First function called one activity setup.
     */
    @Override
    @SuppressWarnings("MissingPermission")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_start);

        /* set up views and navigation */
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);


        viewFlipper = (ViewFlipper) findViewById(R.id.browse_view_flipper);

        //disable refresh
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.browse_refresh);
        refreshLayout.setEnabled(false);

        // Create list view
        list = (ListView) findViewById(R.id.browse_list);
        NoToursList = (TextView) findViewById(R.id.NoMyToursList);
        NoToursList.setVisibility(View.GONE);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_create_tour);
        fab.setVisibility(View.GONE);

        // Set up the navigation
        NavigationSetUp();

        // Get User location
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationNetworkProvider = LocationManager.NETWORK_PROVIDER;

        // Check the permissions
        // Checks fine permissions
        if (!hasMapPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_REQUEST_CODE);
            return;
        }
        // Checks coarse permissions
        if (!hasMapPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, LOCATION_REQUEST_CODE);
            return;
        }

        // Get the local location
        lastKnownLocation = locationManager.getLastKnownLocation(locationNetworkProvider);
        if(lastKnownLocation == null) {
            lastKnownLocation = new Location("");
            lastKnownLocation.setLatitude(37);
            lastKnownLocation.setLongitude(-117);
        }

        // Get tours
        getUserTakenTours();

    }

    @Override
    public void onResume () {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("Login", -1);
        if (!prefs.getBoolean("auth", false)) {
            finish();
        }
    }
    /**
     * Function to setup navigation.
     */
    private void NavigationSetUp() {
        navigationDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationToggle = new ActionBarDrawerToggle(this, navigationDrawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        navigationDrawer.addDrawerListener(navigationToggle);
        navigationToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        prefs = getApplicationContext().getSharedPreferences("Login", 0);
        editor = prefs.edit();

        // Get the user information, and show it in the navigation title
        View header = navigationView.getHeaderView(0);
        TextView name = (TextView) header.findViewById(R.id.nav_header_name);
        TextView email = (TextView) header.findViewById(R.id.nav_header_email);
        name.setText(prefs.getString("uname", "User"));
        email.setText(prefs.getString("uemail", "user@example.com"));
    }


    /*List View Related functions */

    /**
     * Function to get tours taken given user id. TODO: Add user id parse.
     */
    private void getUserTakenTours() {
        SharedPreferences myPrefs = getSharedPreferences("Login", -1);
        int uid = myPrefs.getInt("uid", 0);
        System.out.println("My id is" + uid);

        // Changing to just new function call.
        DB.getToursTakenByUserId (uid, this,  new DB.Callback<ArrayList<Tour>>(){
            @Override
             public void onSuccess(ArrayList<Tour> tours) {
            // Get the tours

                if(tours.isEmpty()) {
                    clearAllArrays();
                    CustomList adapter = new CustomList(MyToursActivity.this, TourTitles, TourDescriptions, imageIds, TourUsers);
                    list.setAdapter(adapter);
                    NoToursList.setVisibility(View.VISIBLE);
                    System.out.println("No tours found");
                }
                else{
                    NoToursList.setVisibility(View.GONE);
                for (int i = 0; i < tours.size(); i++) {
                     System.err.println("Getting tour: " + i);
                      final Tour tour = tours.get(i);
                       SetTourInfoForListView(tour);
                  }
                System.err.println("size of array is " + tours.size());
                // Create list items
                CustomList adapter = new
                        CustomList(MyToursActivity.this, TourTitles, TourDescriptions, imageIds, TourUsers);
                list.setAdapter(adapter);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    showOverviewView(TourIDs.get(+position));

                }
            });}
        }

            @Override
            public void onFailure(ArrayList<Tour> tours) {
                clearAllArrays();
                CustomList adapter = new CustomList(MyToursActivity.this, TourTitles, TourDescriptions, imageIds, TourUsers);
                list.setAdapter(adapter);
                NoToursList.setVisibility(View.VISIBLE);
                System.out.println("On failure happened\n");
            }
        });

    }

    private void clearAllArrays() {
        TourTitles.clear();
        TourDescriptions.clear();
        imageIds.clear();
        TourIDs.clear();
        TourUsers.clear();
    }

    /**
     * Given tours, will set up the tours metadata to display in each list container.
     */
    private void SetTourInfoForListView(Tour tour) {
        String tour_title = (tour.getTitle() != null) ? tour.getTitle() : "Unknown";
        String tour_summary = (tour.getSummary() != null) ? tour.getSummary() : "There is no summary available";
        User tour_user = (tour.getUser() != null) ? tour.getUser() : new User(0, "User X", "");

        TourTitles.add(tour_title);
        TourDescriptions.add(tour_summary);
        TourUsers.add(tour_user);
        TourIDs.add(tour.getTour_id());

        String TourPhoto;
        if ( tour.getPhoto() != null && tour.getPhoto().compareTo("http://placehold.it/250x250") != 0) {
            TourPhoto = tour.getPhoto();
        }
        else {
            TourPhoto = "https://maps.googleapis.com/maps/api/streetview?size=1200x600&location=" +
                    Double.toString(tour.getStarting_lat()) +"," + Double.toString(tour.getStarting_lon()) +
                    "&heading=200&pitch=10&key=AIzaSyBCQ8q5n2-swQNVzQtxvY8eZv-G7c9DiLc";
        }
        imageIds.add(TourPhoto);
    }


    /**
     * Create option menus menu using managers.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_mytours_actions, menu);

        // Configure the search info and add any event listeners...
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * Case selection for option menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (navigationToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.action_map_view:
                // switch to the map view using view flipper
                item.setChecked(!item.isChecked());
                item.setIcon(item.isChecked() ? R.drawable.ic_view_list_white : R.drawable.ic_map_white);
                viewFlipper.showNext();
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /* Functions Related to Navigation */

    /**
     * Function called when navigation item is clicked.
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_my_tours:
                break;

            case R.id.nav_browse:
                launchBrowseView();
                break;

            case R.id.nav_log_out:
                // handle log out
                editor.clear();
                editor.commit();
                Intent login = new Intent(this, LoginActivity.class);
                startActivity(login);
                finish();
                break;

//            case R.id.nav_share:
//                break;
//
//            case R.id.nav_send:
//                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void launchBrowseView(){
        Intent intent = new Intent(this, BrowseViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Show overview of a tour when click list item
     * TODO: Send data to overview activity
     */
    public void showOverviewView(Integer tourID) {
        Intent intent = new Intent(this, OverviewActivity.class);
        intent.putExtra(OverviewActivity.TOUR_ID, tourID.intValue());
        startActivity(intent);
    }

    /* Map View Code */
    /**
     * Required function called by google maps when maps is done loading.
     * Due to call back method design, the function houses
     * map clicking functionality
     */
    @Override
    @SuppressWarnings("MissingPermission")
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        NoToursMap = (TextView) findViewById(R.id.NoMyToursMap);
        NoToursMap.setVisibility(View.GONE);

        setInfoWindowListenerForMap();
        setUpZoomButtonsOnMap();

        // Check fine GPS permissions and then coarse GPS permissions

        mMap.setMyLocationEnabled(true);

        if (lastKnownLocation == null) {
            LatLng myLocation = new LatLng(33.812, -117.919);
            // Grab data
            displayMyToursInMap(myLocation, mMap);
        } else {
            ////----------- display the map with marker on current location -----------//
            // Add a marker in current location, and move the camera.
            LatLng myLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            // Grab data
            displayMyToursInMap(myLocation, mMap);
        }
    }

    private void setInfoWindowListenerForMap() {
        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter(this));
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Integer tourId;
                if (markerTable == null) {
                    return;
                } else {
                    tourId = markerTable.get(marker.getId());
                    //check validity of ID
                    if (tourId == null) {
                        return;
                    } else {
                        showOverviewView(tourId);
                    }
                }
            }
        });
    }

    private void setUpZoomButtonsOnMap() {
        // Zoom in and Zoom out buttons.
        ImageButton zoomIn = (ImageButton) findViewById(R.id.zoomIn);
        ImageButton zoomOut = (ImageButton) findViewById(R.id.zoomOut);

        // Listeners for zoom and zoom out buttons.
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

    /**
     * Function to display nearby tours given longitude and latitude. Sets up metadata
     * in map view.
     */
    private void displayMyToursInMap(final LatLng myLocation, final GoogleMap mMap) {
        if (mMap == null) return;
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 10));

        SharedPreferences myPrefs = getSharedPreferences("Login", -1);
        int uid = myPrefs.getInt("uid", 0);

        // Data base call for near tours.
        DB.getToursTakenByUserId (uid, this,  new DB.Callback<ArrayList<Tour>>(){
                    @Override
                    public void onSuccess(ArrayList<Tour> tours) {
                        System.out.println("success for map");

                        markerTable = new HashMap<String, Integer>();

                        // Display them
                        if(tours.isEmpty()) {
                            mMap.clear();
                            NoToursMap.setVisibility(View.VISIBLE);
                        }
                        else {
                            NoToursMap.setVisibility(View.GONE);
                            for (Tour t : tours) {
                                LatLng location = new LatLng(t.getStarting_lat(), t.getStarting_lon());

                                Marker newMarker = mMap.addMarker(new MarkerOptions().position(location)
                                        .title(t.getTitle())
                                        .snippet(t.getSummary())
                                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
                                markerTable.put(newMarker.getId(), t.getTour_id());

                            }
                        }
                    }

                    @Override
                    public void onFailure(ArrayList<Tour> tours) {
                        mMap.clear();
                        NoToursMap.setVisibility(View.VISIBLE);
                        System.out.println("On failure happened\n");
                    }
                });
    }

    /* Functions for Permissions checking. */

    /**
     * Function to check map permissions.
     * Returns true if permissions granted. Checks given permission level.
     */
    private boolean hasMapPermission(String permissionLevel) {
        return ActivityCompat.checkSelfPermission(this, permissionLevel)
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


    /**
     * Required function that will be called after permisison requests.
     * Gets called some time, no gurantee when. If permissions fails, ask again!
     */
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
        Intent intent = new Intent(this, MyToursActivity.class);
        startActivity(intent);
        finish();

        return;

    }
}

package com.cs110.lit.adventour;
/**
 * BrowseViewActivity
 * This activity works as a home page for Adventour.
 * It gets the current location of the user and show a list of tours nearby
 * The view switches between a list view and a map view
 */

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BrowseViewActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {
    // Attributes for the list view
    ListView list;
    private final ArrayList<String> TourTitles = new ArrayList<>();
    private final ArrayList<String> TourDescriptions = new ArrayList<>();
    private final ArrayList<String> imageIds = new ArrayList<>();
    private final ArrayList<Integer> TourIDs = new ArrayList<>();
    private final ArrayList<User> TourUsers = new ArrayList<>();


    // Attributes for action bar
    private DrawerLayout navigationDrawer;
    private ActionBarDrawerToggle navigationToggle;
    private ViewFlipper viewFlipper;

    // Attributes for location
    private static final int LOCATION_REQUEST_CODE = 0;
    private LocationManager locationManager;
    private Location currentLocation;
    private String locationNetworkProvider;
    double LastUsedLatitude;
    double LastUsedLongitude;
    private LatLng lastUsedLng;

    private GoogleMap mMap;
    private TextView NoToursMap;
    private TextView NoToursList;
    private HashMap<String, Integer> markerTable = null;

    // Attributes for user information
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    // Attributes for search
    private String searchQuery;


    ////////////////////////////////////////////////////////////////
    /////// ------------ Functions Start HERE ----------------//////
    ////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_start);

        /* set up views and navigation */
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        NavigationSetUps();

        viewFlipper = (ViewFlipper) findViewById(R.id.browse_view_flipper);
        list = (ListView) findViewById(R.id.browse_list);

        NoToursList = (TextView) findViewById(R.id.NoToursList);
        NoToursList.setVisibility(View.GONE);

        /* set up location manager and location permissions */
        if (!setUpCurrentLocation()) return;


        LastUsedLatitude = currentLocation.getLatitude();
        LastUsedLongitude = currentLocation.getLongitude();
        GetNearbyToursForList(currentLocation.getLatitude(), currentLocation.getLongitude());

        /* Allow user to refresh the list by pushing down */
        SwipeDownToRefreshLastKnownList();

        /* handle search intent */
        handleIntent(getIntent());

        ClickFloatingButtonToLaunchCreateTour();
    }

    @Override
    public void onResume () {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("Login", -1);
        if (!prefs.getBoolean("auth", false)) {
            finish();
        }
    }


    ////////////////////////////////////////////////////////////////
    /////// ------------------ SET UPS -----------------------//////
    ////////////////////////////////////////////////////////////////
    private boolean setUpCurrentLocation() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationNetworkProvider = LocationManager.NETWORK_PROVIDER;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
            return false;
        }
        currentLocation = locationManager.getLastKnownLocation(locationNetworkProvider);
        if (currentLocation == null) {
            currentLocation = new Location("");
            currentLocation.setLatitude(32.881375);
            currentLocation.setLongitude(-117.233035);
        }

        return true;
    }

    private void NavigationSetUps() {
        navigationDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationToggle = new ActionBarDrawerToggle(this, navigationDrawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        navigationDrawer.addDrawerListener(navigationToggle);
        navigationToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
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

    private void SwipeDownToRefreshLastKnownList() {
        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) findViewById(R.id.browse_refresh);
        assert refreshLayout != null;
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RefreshView(LastUsedLatitude, LastUsedLongitude);
                refreshLayout.setRefreshing(false);
            }
        });
    }

    private void RefreshView(double latitude, double longitude) {
        LastUsedLatitude = latitude;
        LastUsedLongitude = longitude;
        // refresh List
        GetNearbyToursForList(latitude, longitude);
        // refresh Map
        lastUsedLng = new LatLng(latitude, longitude);
        displayNearbyToursInMap(lastUsedLng, mMap);
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
        //Intent intent = new Intent(this, BrowseViewActivity.class);
        //startActivity(intent);
        //finish();

        PostLocationRefreshView();

        return;

    }

    private void PostLocationRefreshView(){
        if( setUpCurrentLocation()){
            RefreshView(currentLocation.getLatitude(),currentLocation.getLongitude());
        }
        else{
            System.err.println("FATAL ERROR");
        }

    }

    ////////////////////////////////////////////////////////////////
    /////// ----------- Call for other Activities ------------//////
    ////////////////////////////////////////////////////////////////
    private void ClickFloatingButtonToLaunchCreateTour() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_create_tour);
        if (fab != null)
            fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCreateTour();
            }
        });
    }

    private void launchCreateTour() {
        Intent intent = new Intent(this, CreateTourActivity.class);
        startActivity(intent);
    }

    public void showOverviewView(Integer tourID) {
        Intent intent = new Intent(this, OverviewActivity.class);
        intent.putExtra(OverviewActivity.TOUR_ID, tourID.intValue());
        startActivity(intent);
    }

    private void logOutFromBrowseView() {
        editor.clear();
        editor.commit();
        Intent login = new Intent(this, LoginActivity.class);
        startActivity(login);
        finish();
    }

    private void launchViewTours(){
        Intent intent = new Intent(this, MyToursActivity.class);
        startActivity(intent);
    }

    //////////////////////////////////////////////////////////
    /////////------- Code Handles Search --------------///////
    //////////////////////////////////////////////////////////
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            searchQuery = intent.getStringExtra(SearchManager.QUERY);
            Address searchLocation = getLocationFromAddress(searchQuery);
            if (searchLocation != null) {
                RefreshView(searchLocation.getLatitude(), searchLocation.getLongitude());
            }
        }
    }

    public Address getLocationFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address.size() == 0)
                return null;
            return address.get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    ///////////////////////////////////////////////////////////
    ///--------------THIS IS FOR LIST VIEW -----------------///
    ///////////////////////////////////////////////////////////
    private void GetNearbyToursForList(double latitude, double longitude) {
        DB.getToursNearLoc(latitude, longitude, 25, 10, this, new DB.Callback<ArrayList<Tour>>() {
            @Override
            public void onSuccess(ArrayList<Tour> tours) {
                clearAllArrays();
                NoToursList.setVisibility(View.GONE);
                //get the tours
                for (int i = 0; i < tours.size(); i++) {
                    final Tour tour = tours.get(i);
                    SetTourInfoForListView(tour);
                }

                // create list items
                CustomList adapter = new CustomList(BrowseViewActivity.this, TourTitles, TourDescriptions, imageIds, TourUsers);
                list.setAdapter(adapter);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        //Toast.makeText(BrowseViewActivity.this, "You Clicked at " + TourTitles.get(+position), Toast.LENGTH_SHORT).show();
                        showOverviewView(TourIDs.get(+position));
                    }
                });
            }

            @Override
            public void onFailure(ArrayList<Tour> tours) {
                clearAllArrays();
                CustomList adapter = new CustomList(BrowseViewActivity.this, TourTitles, TourDescriptions, imageIds, TourUsers);
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

    private void SetTourInfoForListView(Tour tour) {
        String tour_title = (tour.getTitle() != null) ? tour.getTitle() : "Unknown";
        String tour_summary = (tour.getSummary() != null) ? tour.getSummary() : "There is no summary available";
        User tour_user = (tour.getUser() != null) ? tour.getUser() : new User(0, "User X", "");

        TourTitles.add(tour_title);
        TourDescriptions.add(tour_summary);
        TourUsers.add(tour_user);
        TourIDs.add(tour.getTour_id());

        //if (tours.get(i).getImage() != null)
        //  imageIds.add(tours.get(i).getImage());
        //else
        imageIds.add("https://maps.googleapis.com/maps/api/streetview?size=2400x1200&location=" +
                Double.toString(tour.getStarting_lat()) +"," + Double.toString(tour.getStarting_lon()) +
                "&heading=200&pitch=10&key=AIzaSyBCQ8q5n2-swQNVzQtxvY8eZv-G7c9DiLc");
    }

    ///////////////////////////////////////////////////////////
    ///-----------THIS IS FOR THE MAP VIEW -----------------///
    //////////////////////////////////////////////////////////
    @Override
    public void onMapReady(GoogleMap googleMap) {

        // launch the map
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        NoToursMap = (TextView) findViewById(R.id.NoTours);
        NoToursMap.setVisibility(View.GONE);

        setInfoWindowListenerForMap();

        setUpZoomButtonsOnMap();

        /* display nearby tours on map view */
        if (currentLocation == null && lastUsedLng == null) {
            LatLng myLocation = new LatLng(32.881375, -117.233035);
            displayNearbyToursInMap(myLocation, mMap);
        } else {
            //----------- display the map with marker on current location -----------//
            if (lastUsedLng == null) {
                LatLng myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                displayNearbyToursInMap(myLocation, mMap);
            } else {
                displayNearbyToursInMap(lastUsedLng, mMap);
            }
        }
    }

    private void setInfoWindowListenerForMap() {
        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter(this));
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Integer tourId;
                if (markerTable != null) {
                    tourId = markerTable.get(marker.getId());
                    //check validity of ID
                    if (tourId != null)
                        showOverviewView(tourId);
                }
            }
        });
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

    private void displayNearbyToursInMap(final LatLng myLocation, final GoogleMap mMap) {
        if (mMap == null) return;
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,13));

        System.out.println("attempting to grab data\n");
        DB.getToursNearLoc(myLocation.latitude, myLocation.longitude, 25, 10, this, new DB.Callback<ArrayList<Tour>>() {
            @Override
            public void onSuccess(ArrayList<Tour> tours) {
                NoToursMap.setVisibility(View.GONE);
                System.out.println("success\n");
                markerTable = new HashMap<String, Integer>();
                //display them
                for (Tour t : tours) {
                    // System.out.println("Let's drop some pins");
                    LatLng location = new LatLng(t.getStarting_lat(), t.getStarting_lon());

                    Marker newMarker = mMap.addMarker(new MarkerOptions().position(location)
                            .title(t.getTitle())
                            .snippet(t.getSummary())
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
                    markerTable.put(newMarker.getId(), t.getTour_id());

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

    ///////////////////////////////////////////////////////////
    ///--------------OPTIONS AND NAVIGATION ----------------///
    ///////////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_list_actions, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        // Configure the search info and add any event listeners...
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (navigationToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.action_search:

                // search action
                return true;
            case R.id.action_map_view:
                // switch to the map view using view flipper
                item.setChecked(!item.isChecked());
                item.setIcon(item.isChecked() ? R.drawable.ic_view_list_white : R.drawable.ic_map_white);
                viewFlipper.showNext();
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
                return true;
            case R.id.action_refresh:
                // refresh
                RefreshView(currentLocation.getLatitude(), currentLocation.getLongitude());
                System.out.println("Dont think it is this one!!");
                return true;
            case R.id.action_help:
                // help action
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_my_tours) {
            //handle shift to view my tours
            launchViewTours();

        } else if (id == R.id.nav_browse) {

        } else if (id == R.id.nav_log_out) {
            // handle log out
            logOutFromBrowseView();
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}

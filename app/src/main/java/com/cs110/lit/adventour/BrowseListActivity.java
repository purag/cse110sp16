package com.cs110.lit.adventour;

/**
 * Created by achen on 5/6/16.
 */

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.cs110.lit.adventour.model.Tour;

import java.util.ArrayList;

public class BrowseListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    ListView list;
    private final ArrayList<String> TourTitle = new ArrayList<>();
    private final ArrayList<String> TourDescription = new ArrayList<>();
    private final ArrayList<Integer> imageId = new ArrayList<>();

    /**
     * To record that a user session has been registered.
     */
    SharedPreferences prefs;

    /**
     * The object in which we record the user's active session.
     */
    SharedPreferences.Editor editor;

    private static final int LOCATION_REQUEST_CODE = 0;
    private DrawerLayout navigationDrawer;
    private ActionBarDrawerToggle navigationToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_browse_list);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setBackgroundDrawable(new ColorDrawable(0xFF160203));


        // create list view
        list = (ListView)findViewById(R.id.browse_list);


        // ---------- Navigation Stuff --------------//
        navigationDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationToggle = new ActionBarDrawerToggle(this, navigationDrawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        navigationDrawer.addDrawerListener(navigationToggle);
        navigationToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        prefs =  getApplicationContext().getSharedPreferences("Login", 0);
        editor = prefs.edit();

        View header = navigationView.getHeaderView(0);
        TextView name = (TextView) header.findViewById(R.id.nav_header_name);
        TextView email = (TextView) header.findViewById(R.id.nav_header_email);
        name.setText(prefs.getString("uname", "User"));
        email.setText(prefs.getString("uemail", "user@example.com"));



        // --------- Find current location --------------//
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        String locationNetworkProvider = LocationManager.NETWORK_PROVIDER;

        // -------- Check permission -----------//
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

        Location mlocation = locationManager.getLastKnownLocation(locationNetworkProvider);

        // added some test vaiable just for testing!!
        this.TourTitle.add("Garfield");
        this.TourDescription.add("Test object");
        this.imageId.add(R.drawable.cat3);

        NearbyTours(mlocation);

    }

    private void NearbyTours(Location myLocation) {
        //grab data
        double testLatitude = 33;
        double testLongitude = 117;
        double testDist = 5000;
        int testLim = 10;

        DB.getToursNearLoc(testLatitude, testLongitude, testDist, testLim, this, new DB.Callback<ArrayList<Tour>>() {
        //DB.getToursNearLoc(myLocation.getLatitude(), myLocation.getLongitude(), 25, 10, this, new DB.Callback<ArrayList<Tour>>() {
            @Override
            public void onSuccess(ArrayList<Tour> tours){
                //get the tours
                for (int i = 0; i< tours.size(); i++) {
                    if (tours.get(i).getTitle() != null)
                        TourTitle.add(tours.get(i).getTitle());
                    else
                        TourTitle.add("Unknown");
                    if (tours.get(i).getSummary() != null)
                        TourDescription.add(tours.get(i).getSummary());
                    else
                        TourDescription.add("There is no summary available");
                    //if (tours.get(i).getImage() != null)
                      //  imageId.add(tours.get(i).getImage());
                    //else
                    imageId.add(R.drawable.logo_400);
                }

                // create list items
                CustomList adapter = new CustomList(BrowseListActivity.this, TourTitle, TourDescription, imageId);
                list.setAdapter(adapter);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        //Toast.makeText(BrowseListActivity.this, "You Clicked at " + TourTitle.get(+position), Toast.LENGTH_SHORT).show();
                        showOverviewView();
                    }
                });
            }

            @Override
            public void onFailure(ArrayList<Tour> tours) {
                System.out.println("On failure happened\n");
            }
        });

    }

    /**
     * Create option menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        // Configure the search info and add any event listeners...
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * Case selection for option menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println("selected an item: " + item.getItemId());

        if (navigationToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.action_search:
                // search action
                return true;
            case R.id.action_map_view:
                // jump to the map view
                showMapView();
                return true;
            case R.id.action_refresh:
                // refresh
                return true;
            case R.id.action_help:
                // help action
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //-------------------Functions related to navigation stuff ----------------//
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_my_tour) {
            // Handle the camera action
        } else if (id == R.id.nav_browse) {

        } else if (id == R.id.nav_create) {

        } else if (id == R.id.nav_log_out) {
            editor.clear();
            editor.commit();
            Intent login = new Intent(this, LoginActivity.class);
            startActivity(login);
            finish();
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * load map action
     */
    public void showMapView () {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    /**
     * Test if the map activity works properly
     */
    public void showOverviewView() {
        Intent intent = new Intent(this, OverviewActivity.class);
        startActivity(intent);
    }

}

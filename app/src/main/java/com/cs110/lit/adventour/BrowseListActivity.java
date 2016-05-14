package com.cs110.lit.adventour;

/**
 * Created by achen on 5/6/16.
 */

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cs110.lit.adventour.model.Tour;

import java.util.ArrayList;

public class BrowseListActivity extends AppCompatActivity implements OnQueryTextListener {
    ListView list;
    private ArrayList<Tour> nearbyTours;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_list);

        // --------- Find current location --------------//
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        String locationNetworkProvider = LocationManager.NETWORK_PROVIDER;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location mlocation = locationManager.getLastKnownLocation(locationNetworkProvider);


        ArrayList<String> TourTitle = new ArrayList<>();
        TourTitle.add("Garfield");

        ArrayList<String> TourDescription = new ArrayList<>();
        TourDescription.add("Test object");

        ArrayList<Integer> imageId = new ArrayList<>();
        imageId.add(R.drawable.cat1);


        NearbyTours(mlocation, TourTitle, TourDescription, imageId);
            CustomList adapter = new
                CustomList(BrowseListActivity.this, TourTitle, TourDescription, imageId);


        // create list view
        list = (ListView)findViewById(R.id.browse_list);

        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //Toast.makeText(BrowseListActivity.this, "You Clicked at " +TourTitle[+ position], Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void NearbyTours(Location myLocation, final ArrayList<String> TourTitle, final ArrayList<String> TourDescription, final ArrayList<Integer> imageId){
        //grab data
        DB.getToursNearLoc(myLocation.getLatitude(), myLocation.getLongitude(), 300.0, 10, this, new DB.Callback<ArrayList<Tour>>() {
            @Override
            public void onSuccess(ArrayList<Tour> tours) {
                for (Tour t : tours) {
                    if (t.getTitle() != null)
                        TourTitle.add(t.getTitle());
                    else
                        TourTitle.add("Unknown");
                    if (t.getSummary() != null)
                        TourDescription.add(t.getSummary());
                    else
                        TourDescription.add("There is no summary available");
                    //if (t.getImage() != null)
                    //    imageId.add(t.getImage());
                    //else
                        imageId.add(R.drawable.logo_400);

                }
                //get the tours
                nearbyTours = tours;
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

        // Configure the search info and add any event listeners...
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * Case selection for option menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.action_search:
                // search action
                return true;
            case R.id.action_location_found:
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


    @Override
    public boolean onQueryTextChange(String newText)
    {
        // this is your adapter that will be filtered
        if (TextUtils.isEmpty(newText))
        {
            list.clearTextFilter();
        }
        else
        {
            list.setFilterText(newText.toString());
        }

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * load map action
     */
    public void showMapView () {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

}

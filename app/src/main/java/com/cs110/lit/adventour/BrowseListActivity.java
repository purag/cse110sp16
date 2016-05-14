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
import android.widget.Toast;

public class BrowseListActivity extends AppCompatActivity implements OnQueryTextListener {
    ListView list;

    // NOTE:
    // Those data are from the database, need to implement function to grab those data
    // TourTitle array, TourDescription array and imageId array
    // should be the same size!!!!

    String[] TourTitle = {
            "Garfield",
            "Pusheen",
            "Doriamon",
            "eeve",
            "foxmon",
            "Squirtle",
            "bobabso"
    };

    String[] TourDescription = {
            "\nFirst Object test description. This is Garfield. this is a very very very very very very " +
                    "very very very very very very very very very very very very very very very very very " +
                    "very very very very very very very very very long text for the description\n",
            "\nSecond Object test description. This is Pusheen\n",
            "\nThird Object test description. This is Doriamon\n",
            "\nFirst Object test description. This is Garfield\n",
            "\nSecond Object test description. This is Pusheen\n",
            "\nThird Object test description. This is Doriamon\n",
            "\nThird Object test description. This is Doriamon\n"
    };

    Integer[] imageId = {
            R.drawable.cat1,
            R.drawable.cat2,
            R.drawable.cat3,
            R.drawable.eevee,
            R.drawable.foxmon,
            R.drawable.squirtle,
            R.drawable.bobabso
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_list);

        CustomList adapter = new
                CustomList(BrowseListActivity.this, TourTitle, TourDescription, imageId);

        // --------- Find current location --------------//
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        String locationNetworkProvider = LocationManager.NETWORK_PROVIDER;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location mlocation = locationManager.getLastKnownLocation(locationNetworkProvider);


        // create list view
        list = (ListView)findViewById(R.id.browse_list);

        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(BrowseListActivity.this, "You Clicked at " +TourTitle[+ position], Toast.LENGTH_SHORT).show();

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

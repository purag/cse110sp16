package com.cs110.lit.adventour;

/**
 * Created by achen on 5/6/16.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

public class BrowseListActivity extends AppCompatActivity {
    ListView list;
    SearchView mSearchView;

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
    } ;

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
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        CustomList adapter = new
                CustomList(BrowseListActivity.this, TourTitle, TourDescription, imageId);
        list=(ListView)findViewById(R.id.browse_list);
        mSearchView = (SearchView) findViewById(R.id.searchView);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    list.clearTextFilter();
                } else {
                    Log.i("WSM",newText);
                    list.setFilterText(newText.toString());
                }
                return true;

            }
        });

        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(BrowseListActivity.this, "You Clicked at " +TourTitle[+ position], Toast.LENGTH_SHORT).show();

            }
        });

        Intent intent = getIntent();

    }

    /**
     * Test if the map activity works properly
     */
    public void showMapView (View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

}

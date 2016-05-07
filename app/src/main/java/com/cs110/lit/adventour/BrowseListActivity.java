package com.cs110.lit.adventour;

/**
 * Created by achen on 5/6/16.
 */

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

public class BrowseListActivity extends Activity implements OnQueryTextListener {
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
            "squirtle",
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
            R.drawable.eeve,
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
        list=(ListView)findViewById(R.id.browse_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(BrowseListActivity.this, "You Clicked at " +TourTitle[+ position], Toast.LENGTH_SHORT).show();

            }
        });

        // for communication with the last/previews view
        Intent intent = getIntent();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService( Context.SEARCH_SERVICE );
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_item_search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
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

}

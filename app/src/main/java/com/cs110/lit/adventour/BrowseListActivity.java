package com.cs110.lit.adventour;

/**
 * Created by achen on 5/6/16.
 */
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.app.Activity;

public class BrowseListActivity extends Activity {
    ListView list;
    String[] TourTitle = {
            "Garfield",
            "Pusheen",
            "Doriamon"
    } ;

    String[] TourDescription = {
            "First Object test description. This is Garfield",
            "Second Object test description. This is Pusheen",
            "Third Object test description. This is Doriamon"
    };

    Integer[] imageId = {
            R.drawable.cat1,
            R.drawable.cat2,
            R.drawable.cat3

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

}

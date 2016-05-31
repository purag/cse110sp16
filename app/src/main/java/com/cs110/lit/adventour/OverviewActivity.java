package com.cs110.lit.adventour;

/**
 * Created by achen on 5/7/16.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cs110.lit.adventour.model.Checkpoint;
import com.cs110.lit.adventour.model.Tour;
import com.cs110.lit.adventour.model.User;

import java.util.ArrayList;

public class OverviewActivity extends AppCompatActivity {

    public static final String TOUR_ID = "tour_id";
    private int tourID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_overview);

        setSupportActionBar((Toolbar) findViewById(R.id.collapsing_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        //get the tour id entered, -1 for bad input
        Intent intent = getIntent();
        tourID = intent.getIntExtra(TOUR_ID, -1);
        DB.getTourById(tourID, this, new DB.Callback<Tour>() {

            @Override
            public void onSuccess(Tour tour) {

                String tourTitle = tour.getTitle();
                User tourCreator = tour.getUser();
                String tourCreatorName = tourCreator.getUser_name();
                String tourSummary = tour.getSummary();
                ArrayList<Checkpoint> checkpoints = tour.getListOfCheckpoints();

                setTitle(tourTitle);
                getSupportActionBar().setTitle(tourTitle);
                setSummaryCard(tourCreatorName, tourSummary);

                String photo = checkpoints.get(0).getPhoto();
                if(photo != null) {
                    loadBackdrop(photo);
                }
                else {
                    //Get the photo from the first checkpoint to load as background
                    String firstPhoto = "https://maps.googleapis.com/maps/api/streetview?size=2400x1200&location=" +
                            Double.toString(checkpoints.get(0).getLatitude()) +"," +
                            Double.toString(checkpoints.get(0).getLongitude()) +
                            "&heading=200&pitch=10&key=AIzaSyBCQ8q5n2-swQNVzQtxvY8eZv-G7c9DiLc";
                    loadBackdrop(firstPhoto);
                }

                displayCheckpoints(checkpoints);
            }

            @Override
            public void onFailure(Tour tour) {
                System.out.println("On failure happened\n");
            }
        });
    }

    /**
     * Set the title of the collapsing toolbar in tour metadata view
     * @param title
     */
    private void setTitle(String title) {
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);
        collapsingToolbar.setTitle(title);
    }

    /**
     * Set the title and description of the summary card in tour metadata view
     *
     * @param name
     * @param summary
     */
    private void setSummaryCard(String name, String summary) {
        //set title field of summary card
        TextView userName = (TextView) findViewById(R.id.tour_metadata_summary_card_user_name);
        userName.setText(name);

        //set summary field of summary card
        TextView summaryText = (TextView) findViewById(R.id.tour_metadata_summary_card_description);
        summaryText.setText(summary);
    }

    /**
     * display the selected checkpoint image in the metadata view
     */
    private void loadBackdrop(String photo) {
        final ImageView imageView = (ImageView) findViewById(R.id.tour_metadata_bg_image);
        Glide.with(this).load(photo).into(imageView);
    }

    /**
     * display all of the checkpoints in the recyclerview
     */
    private void displayCheckpoints(ArrayList<Checkpoint> checkpoints) {
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        RecyclerView checkpointList = (RecyclerView) findViewById(R.id.tour_metadata_checkpoints_list);
        CheckpointListAdapter adapter = new CheckpointListAdapter(checkpoints);
        checkpointList.setAdapter(adapter);
        checkpointList.setLayoutManager(layoutManager);

    }

    /**
     * load map action
     */
    public void showTakeTour (View view) {
        Intent intent = new Intent(this, TakeTourActivity.class);
        intent.putExtra(TakeTourActivity.TOUR_ID, tourID);
        intent.putExtra(TakeTourActivity.TOUR_TITLE,getSupportActionBar().getTitle());
        startActivity(intent);
        finish();
    }

}
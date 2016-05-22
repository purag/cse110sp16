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
import android.view.View;
import android.widget.TextView;

import com.cs110.lit.adventour.model.Checkpoint;
import com.cs110.lit.adventour.model.Tour;

import java.util.ArrayList;

public class OverviewActivity extends AppCompatActivity {
    private CollapsingToolbarLayout collapsingToolbarLayout = null;

    public static final String TOUR_ID = "tour_id";
    private int tourID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_overview);

        //get the tour id entered, -1 for bad input
        Intent intent = getIntent();
        tourID = intent.getIntExtra(TOUR_ID, -1);
        DB.getTourById(tourID, this, new DB.Callback<Tour>() {
            @Override
            public void onSuccess(Tour tour) {

                String tourTitle = tour.getTitle();
                String tourSummary = tour.getSummary();
                ArrayList<Checkpoint> checkpoints = tour.getListOfCheckpoints();

                setTitle(tourTitle);
                getSupportActionBar().setTitle(tourTitle);
                setSummaryCard(tourTitle, tourSummary);
                loadBackdrop();
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
                (CollapsingToolbarLayout) findViewById(R.id.tour_metadata_collapsing_toolbar);
        collapsingToolbar.setTitle(title);
    }

    /**
     * Set the title and description of the summary card in tour metadata view
     *
     * @param title
     * @param summary
     */
    private void setSummaryCard(String title, String summary) {
        //set title field of summary card
        TextView summaryTitle = (TextView) findViewById(R.id.tour_metadata_summary_card_title);
        summaryTitle.setText(title);

        //set summary field of summary card
        TextView summaryText = (TextView) findViewById(R.id.tour_metadata_summary_card_description);
        summaryText.setText(summary);
    }

    /**
     * display the selected checkpoint image in the metadata view
     */
    private void loadBackdrop() {
        //final ImageView imageView = (ImageView) findViewById(R.id.tour_metadata_bg_image);
    }

    /**
     * display all of the checkpoints in the recyclerview
     */
    private void displayCheckpoints(ArrayList<Checkpoint> checkpoints) {
        RecyclerView checkpointList = (RecyclerView) findViewById(R.id.tour_metadata_checkpoints_list);
        CheckpointListAdapter adapter = new CheckpointListAdapter(checkpoints);
        checkpointList.setAdapter(adapter);
        checkpointList.setLayoutManager(new LinearLayoutManager(this));

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
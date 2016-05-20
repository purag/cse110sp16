package com.cs110.lit.adventour;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.cs110.lit.adventour.model.Checkpoint;
import com.cs110.lit.adventour.model.Tour;

import java.util.ArrayList;

/**
 * Created by achen on 5/20/16.
 */
public class CheckpointActivity extends AppCompatActivity {
    public static final String CHECKPOINT_ID = "checkpoint_id";
    public static final String TITLE_ID = "checkpoint_title";
    public static final String SUMMARY_ID = "checkpoint_summary";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkpoint_metadata);

        //get the tour id entered, -1 for bad input
        Intent intent = getIntent();
        final int checkpointID = intent.getIntExtra(CHECKPOINT_ID, -1);

        final String title = intent.getStringExtra(TITLE_ID);
        final String summary = intent.getStringExtra(SUMMARY_ID);

        setTitle(title);
        setSummary(summary);
    }

    private void setTitle(String title) {
        TextView titleView = (TextView) findViewById(R.id.checkpoint_metadata_title);
        titleView.setText("Checkpoint clicked!");
    }

    private void setSummary(String summary) {
        TextView summaryView = (TextView) findViewById(R.id.checkpoint_metadata_summary);
        summaryView.setText("will add summary info");
    }
}

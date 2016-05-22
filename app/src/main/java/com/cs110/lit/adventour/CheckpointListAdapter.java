package com.cs110.lit.adventour;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.cs110.lit.adventour.model.Checkpoint;

import java.util.ArrayList;


/**
 * Created by achen on 5/19/16.
 */
public class CheckpointListAdapter extends RecyclerView.Adapter<CheckpointListAdapter.ViewHolder> {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView;
        public View mView;
        public int mCheckpointID;
        public String title;
        public String summary;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            mView = itemView;
            nameTextView = (TextView) itemView.findViewById(R.id.tour_metadata_checkpoint_title);

        }
    }

    // Store a member variable for the contacts
    private ArrayList<Checkpoint> mCheckpoints;

    // Pass in the contact array into the constructor
    public CheckpointListAdapter(ArrayList<Checkpoint> checkpoints) {
        mCheckpoints = checkpoints;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public CheckpointListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.card_layout, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final CheckpointListAdapter.ViewHolder vHolder, int position) {
        // Get the data model based on position
        Checkpoint checkpoint = mCheckpoints.get(position);
        vHolder.mCheckpointID = checkpoint.getCheckpoint_id();

        //TODO: fix this gross workaround for not having knowing how to get the checkpoint by id
        vHolder.title = checkpoint.getTitle();
        vHolder.summary = checkpoint.getDescription();

        // Set item views based on the data model
        TextView textView = vHolder.nameTextView;
        textView.setText(checkpoint.getTitle());

        //add a click listener
        vHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Toast.makeText(context, "You clicked at " + vHolder.title, Toast.LENGTH_SHORT).show();

                //TODO Fix this broken code
                /*Intent intent = new Intent(context, CheckpointActivity.class);
                intent.putExtra(CheckpointActivity.CHECKPOINT_ID, vHolder.mCheckpointID);

                intent.putExtra(CheckpointActivity.TITLE_ID, vHolder.title);
                intent.putExtra(CheckpointActivity.SUMMARY_ID, vHolder.summary);
                context.startActivity(intent);*/
            }
        });

    }

    // Return the total count of items
    @Override
    public int getItemCount() {
        return mCheckpoints.size();
    }
}

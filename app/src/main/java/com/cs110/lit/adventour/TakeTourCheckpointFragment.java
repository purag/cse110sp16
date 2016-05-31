package com.cs110.lit.adventour;

/**
 * Created by Anjali on 5/27/2016.
 */

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class TakeTourCheckpointFragment extends DialogFragment {
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST = 1888;
    private Bitmap tourPhoto;

    private String title;
    private String summary;

    private TextView checkpointTitle;
    private TextView checkpointSummary;

    private TakeTourCheckpointListener tListener;

    public boolean continueClicked = false;


    public TakeTourCheckpointFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title Parameter 1.
     * @return A new instance of fragment TakeTourCheckpointFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TakeTourCheckpointFragment newInstance(String title, String summary, String photo, TakeTourCheckpointListener tListener) {

        TakeTourCheckpointFragment fragment = new TakeTourCheckpointFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("summary", summary);
        args.putString("photo", photo);
        fragment.settListener(tListener);
        fragment.setArguments(args);
        return fragment;
    }

    public void settListener(TakeTourCheckpointListener tListener) {
        this.tListener = tListener;
    }
    /*
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_take_tour_checkpoint, container, false);

        Bundle b = this.getArguments();

        checkpointSummary = (TextView) v.findViewById(R.id.checkpoint_metadata_summary);
        checkpointTitle = (TextView) v.findViewById(R.id.checkpoint_metadata_title);

        checkpointTitle.setText(String.format("Title: %s", b.get("title")));
        checkpointSummary.setText(String.format("Summary: %s", b.get("summary")));

        final ImageView imageView = (ImageView) v.findViewById(R.id.tour_metadata_img);
        Glide.with(this).load( b.get("photo")).into(imageView);

        Button continueBtn = (Button) v.findViewById(R.id.take_tour_continue);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("clicked continue");
                dismiss();
                //getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return v;
    }

    public boolean getContinueClicked() {
        return continueClicked;
    }

    @Override
    public void onResume() {
        super.onResume();

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        getDialog().getWindow().setLayout(9 * width / 10, 9 * height / 10);
        getDialog().setCanceledOnTouchOutside(true);

        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener()
        {
            @Override
            public boolean onKey(android.content.DialogInterface dialog, int keyCode,android.view.KeyEvent event) {
                if ((keyCode ==  android.view.KeyEvent.KEYCODE_BACK))
                {
                    if (event.getAction() !=  KeyEvent.ACTION_DOWN)
                        return true;

                    /*mListener.onBackPressed();*/
                }

                return false; // pass on to be processed as normal
            }
        });
    }

    /*
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CreateTourMetadataListener) {
            mListener = (CreateTourMetadataListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    */

    @Override
    public void onDetach() {
        super.onDetach();
        /*mListener = null;*/
    }

    public interface TakeTourCheckpointListener {
        // TODO: Update argument type and name

    }

}

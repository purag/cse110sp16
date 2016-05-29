package com.cs110.lit.adventour;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.cs110.lit.adventour.model.*;

import java.util.ArrayList;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreateTourMetadataFragment.CreateTourMetadataListener} interface
 * to handle interaction events.
 * Use the {@link CreateTourMetadataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateTourMetadataFragment extends DialogFragment {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST = 1888;
    private Bitmap tourPhoto;

    private CreateTourMetadataListener mListener;

    public CreateTourMetadataFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment CreateTourMetadataFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateTourMetadataFragment newInstance(CreateTourMetadataListener mListener) {
        CreateTourMetadataFragment fragment = new CreateTourMetadataFragment();
        fragment.setmListener(mListener);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void setmListener(CreateTourMetadataListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle("Tell us about this tour!");

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_create_tour_metadata, container, false);

        final EditText tourTitleInput = (EditText) v.findViewById(R.id.create_tour_title_input);
        final EditText tourSummaryInput = (EditText) v.findViewById(R.id.create_tour_summary_input);

        Button cancelBtn = (Button) v.findViewById(R.id.create_tour_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onBackPressed();
            }
        });

        Button continueBtn = (Button) v.findViewById(R.id.create_tour_continue);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput(tourTitleInput, tourSummaryInput)) {
                    mListener.onTourMetadataFinish(new Tour(
                        0,
                        new User(),
                        tourTitleInput.getText().toString(),
                        tourSummaryInput.getText().toString(),
                        new ArrayList<Checkpoint>()
                    ));
                }
            }
        });

        return v;
    }

    private boolean validateInput(EditText tourTitleInput, EditText tourSummaryInput) {
        boolean valid = true;

        String tourTitle = tourTitleInput.getText().toString();
        String tourSummary = tourSummaryInput.getText().toString();

        if (TextUtils.isEmpty(tourTitle)) {
            tourTitleInput.setError("Title is required.");
            valid = false;
        } else if (tourTitle.length() < 10) {
            tourTitleInput.setError("Title should be a bit longer.");
            valid = false;
        }

        if (TextUtils.isEmpty(tourSummary)) {
            tourSummaryInput.setError("Summary is required.");
            valid = false;
        } else if (tourSummary.length() < 50) {
            tourSummaryInput.setError("Summary should be more detailed.");
            valid = false;
        }

        return valid;
    }

    @Override
    public void onResume() {
        super.onResume();

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        getDialog().getWindow().setLayout(9 * width / 10, 9 * height / 10);
        getDialog().setCanceledOnTouchOutside(true);
        setCancelable(false);

        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener()
        {
            @Override
            public boolean onKey(android.content.DialogInterface dialog, int keyCode,android.view.KeyEvent event) {
                if ((keyCode ==  android.view.KeyEvent.KEYCODE_BACK))
                {
                    if (event.getAction() !=  KeyEvent.ACTION_DOWN)
                        return true;

                    mListener.onBackPressed();
                }

                return false; // pass on to be processed as normal
            }
        });
    }

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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface CreateTourMetadataListener {
        // TODO: Update argument type and name
        void onTourMetadataFinish(Tour t);
        void onBackPressed();
    }
}

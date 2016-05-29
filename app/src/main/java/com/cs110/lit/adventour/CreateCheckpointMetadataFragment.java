package com.cs110.lit.adventour;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.cs110.lit.adventour.model.Checkpoint;
import com.cs110.lit.adventour.model.Tour;
import com.cs110.lit.adventour.model.User;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreateCheckpointMetadataFragment.CreateCheckpointMetadataListener} interface
 * to handle interaction events.
 * Use the {@link CreateCheckpointMetadataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateCheckpointMetadataFragment extends DialogFragment {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST = 1888;
    private Bitmap tourPhoto;
    private boolean cancelable;

    private CreateCheckpointMetadataListener mListener;

    public CreateCheckpointMetadataFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment CreateTourMetadataFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateCheckpointMetadataFragment newInstance(CreateCheckpointMetadataListener mListener, boolean cancelable) {
        CreateCheckpointMetadataFragment fragment = new CreateCheckpointMetadataFragment();
        fragment.setmListener(mListener);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void setmListener(CreateCheckpointMetadataListener mListener) {
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
        getDialog().setTitle("Create a checkpoint!");

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_create_checkpoint_metadata, container, false);

        final EditText checkpointTitleInput = (EditText) v.findViewById(R.id.create_checkpoint_title_input);
        final EditText checkpointDescInput = (EditText) v.findViewById(R.id.create_checkpoint_summary_input);

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
                if (validateInput(checkpointTitleInput, checkpointDescInput)) {
                    mListener.onCheckpointMetadataFinish(new Checkpoint(
                        0, 0.0, 0.0, 0,
                        checkpointTitleInput.getText().toString(),
                        checkpointDescInput.getText().toString(),
                        "", 0
                    ));
                }
            }
        });

        return v;
    }

    private boolean validateInput(EditText checkpointTitleInput, EditText checkpointDescInput) {
        boolean valid = true;

        String checkpointTitle = checkpointTitleInput.getText().toString();
        String checkpointDesc = checkpointDescInput.getText().toString();

        if (TextUtils.isEmpty(checkpointTitle)) {
            checkpointTitleInput.setError("Title is required.");
            valid = false;
        } else if (checkpointTitle.length() < 10) {
            checkpointTitleInput.setError("Title should be a bit longer.");
            valid = false;
        }

        if (TextUtils.isEmpty(checkpointDesc)) {
            checkpointDescInput.setError("Description is required.");
            valid = false;
        } else if (checkpointDesc.length() < 50) {
            checkpointDescInput.setError("Description should be more detailed.");
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
        setCancelable(cancelable);

        if (!cancelable) {
            getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                        if (event.getAction() != KeyEvent.ACTION_DOWN)
                            return true;

                        mListener.onBackPressed();
                    }

                    return false; // pass on to be processed as normal
                }
            });
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CreateCheckpointMetadataListener) {
            mListener = (CreateCheckpointMetadataListener) context;
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
    public interface CreateCheckpointMetadataListener {
        // TODO: Update argument type and name
        void onCheckpointMetadataFinish(Checkpoint c);
        void onBackPressed();
    }
}

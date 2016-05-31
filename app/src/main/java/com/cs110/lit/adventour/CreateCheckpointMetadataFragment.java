package com.cs110.lit.adventour;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.cs110.lit.adventour.model.Checkpoint;
import com.cs110.lit.adventour.model.Tour;
import com.cs110.lit.adventour.model.User;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreateCheckpointMetadataFragment.CreateCheckpointMetadataListener} interface
 * to handle interaction events.
 * Use the {@link CreateCheckpointMetadataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateCheckpointMetadataFragment extends DialogFragment {

    private Bitmap checkpointPhoto;

    final private int PICK_IMAGE = 1;
    final private int CAPTURE_IMAGE = 2;
    ImageView imgView;
    private String cameraPhotoPath;

    private boolean checkpointPhotoSet = false;

    private boolean cancelable;

    private CreateCheckpointMetadataListener mListener;

    private ProgressDialog progressDialog;
    private String checkpointTitle;
    private String checkpointDesc;

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
        fragment.cancelable = cancelable;
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

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        final EditText checkpointTitleInput = (EditText) v.findViewById(R.id.create_checkpoint_title_input);
        final EditText checkpointDescInput = (EditText) v.findViewById(R.id.create_checkpoint_summary_input);

        imgView = (ImageView) v.findViewById(R.id.checkpoint_metadata_img);

        Button cancelBtn = (Button) v.findViewById(R.id.create_tour_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cancelable)
                    dismiss();
                else
                    mListener.onBackPressed();
            }
        });

        Button continueBtn = (Button) v.findViewById(R.id.create_tour_continue);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput(checkpointTitleInput, checkpointDescInput)) {
                    progressDialog.setMessage("Uploading photo...");
                    progressDialog.show();
                    new CheckpointPhotoUpload().execute(checkpointPhoto);
                }
            }
        });

        if (ActivityCompat.checkSelfPermission(
                getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

        if (ActivityCompat.checkSelfPermission(
                getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);

        FloatingActionButton cameraFab = (FloatingActionButton) v.findViewById(R.id.fab_add_photo_camera);
        cameraFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("here");
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, setImageUri());
                getActivity().startActivityForResult(intent, CAPTURE_IMAGE);
            }
        });

        FloatingActionButton galleryFab = (FloatingActionButton) v.findViewById(R.id.fab_add_photo_gallery);
        galleryFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.putExtra("return-data", true);
                getActivity().startActivityForResult(Intent.createChooser(intent, "Select a Photo"), PICK_IMAGE);
            }
        });

        return v;
    }

    private Uri setImageUri() {
        File file = new File(Environment.getExternalStorageDirectory(),
                "adventour_" + System.currentTimeMillis() + ".jpg");
        Uri imgUri = Uri.fromFile(file);
        this.cameraPhotoPath = file.getAbsolutePath();
        return imgUri;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == PICK_IMAGE) {
                try {
                    InputStream is = getActivity().getContentResolver().openInputStream(data.getData());

                    /* get the bounds of the image without loading it */
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(is, null, options);

                    /* calculate the sample size so we can load it in downsized */
                    options.inSampleSize = calculateInSampleSize(options, 300, 200);
                    is.close();
                    is = getActivity().getContentResolver().openInputStream(data.getData());
                    options.inJustDecodeBounds = false;
                    checkpointPhoto = BitmapFactory.decodeStream(is, null, options);
                    is.close();

                    System.out.println(options.outWidth);
                    System.out.println(options.outHeight);

                    imgView.setImageBitmap(checkpointPhoto);
                    checkpointPhotoSet = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (requestCode == CAPTURE_IMAGE) {
                /* get the bounds of the image without loading it */
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(cameraPhotoPath, options);

                /* calculate the sample size so we can load it in downsized */
                options.inSampleSize = calculateInSampleSize(options, 300, 200);
                options.inJustDecodeBounds = false;
                checkpointPhoto = BitmapFactory.decodeFile(cameraPhotoPath, options);

                System.out.println(options.outWidth);
                System.out.println(options.outHeight);

                imgView.setImageBitmap(checkpointPhoto);
                checkpointPhotoSet = true;

            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private boolean validateInput(EditText checkpointTitleInput, EditText checkpointDescInput) {
        boolean valid = true;

        checkpointTitle = checkpointTitleInput.getText().toString();
        checkpointDesc = checkpointDescInput.getText().toString();

        if (TextUtils.isEmpty(checkpointTitle)) {
            checkpointTitleInput.setError("Title is required.");
            valid = false;
        } /*else if (checkpointTitle.length() < 10) {
            checkpointTitleInput.setError("Title should be a bit longer.");
            valid = false;
        }*/

        if (TextUtils.isEmpty(checkpointDesc)) {
            checkpointDescInput.setError("Description is required.");
            valid = false;
        } /*else if (checkpointDesc.length() < 50) {
            checkpointDescInput.setError("Description should be more detailed.");
            valid = false;
        }*/

        if (valid && !checkpointPhotoSet) {
            new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.ic_warning_black)
                    .setTitle("Checkpoint photo is required.")
                    .setPositiveButton("OK", null)
                    .show();
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

    private class CheckpointPhotoUpload extends AsyncTask<Bitmap, Void, String> {

        @Override
        protected String doInBackground(Bitmap... params) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            params[0].compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);
            byte [] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        }

        @Override
        protected void onPostExecute(String s) {
            DB.uploadPhoto(s, getActivity(), new DB.Callback<String> () {
                @Override
                public void onSuccess(String photoUrl) {
                    progressDialog.dismiss();
                    System.out.println(photoUrl);
                    Checkpoint c = new Checkpoint(0, 0.0, 0.0, 0, checkpointTitle, checkpointDesc, "", 0);
                    c.setPhoto(photoUrl);
                    mListener.onCheckpointMetadataFinish(c);
                }

                @Override
                public void onFailure(String photoUrl) {
                    progressDialog.dismiss();
                    new AlertDialog.Builder(getActivity())
                        .setTitle("Couldn't upload photo.")
                        .setMessage("Please try again soon!")
                        .setPositiveButton("OK", null)
                        .show();
                }
            });
        }

        @Override protected void onPreExecute() {}
        @Override protected void onProgressUpdate(Void... values) {}
    }
}

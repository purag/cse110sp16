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
import android.graphics.Matrix;
import android.media.ExifInterface;
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
import com.github.clans.fab.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreateTourMetadataFragment.CreateTourMetadataListener} interface
 * to handle interaction events.
 * Use the {@link CreateTourMetadataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateTourMetadataFragment extends DialogFragment {

    private Bitmap tourPhoto;

    final private int PICK_IMAGE = 1;
    final private int CAPTURE_IMAGE = 2;
    ImageView imgView;
    private String cameraPhotoPath;

    private boolean tourPhotoSet = false;

    private CreateTourMetadataListener mListener;

    private ProgressDialog progressDialog;
    private String tourTitle;
    private String tourSummary;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle("Tell us about this tour!");

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_create_tour_metadata, container, false);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        final EditText tourTitleInput = (EditText) v.findViewById(R.id.create_tour_title_input);
        final EditText tourSummaryInput = (EditText) v.findViewById(R.id.create_tour_summary_input);

        imgView = (ImageView) v.findViewById(R.id.tour_metadata_img);

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
                    progressDialog.setMessage("Uploading photo...");
                    progressDialog.show();

                    if(tourPhoto != null && tourPhoto.getWidth() < tourPhoto.getHeight()){
                        Matrix matrix = new Matrix();
                        matrix.postRotate(0);
                        Bitmap.createBitmap(tourPhoto, 0, 0, tourPhoto.getWidth(), tourPhoto.getHeight(), matrix, true);
                    }
                    new TourPhotoUpload().execute(tourPhoto);
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
                    tourPhoto = BitmapFactory.decodeStream(is, null, options);
                    is.close();

                    System.out.println(options.outWidth);
                    System.out.println(options.outHeight);

                    imgView.setImageBitmap(tourPhoto);
                    tourPhotoSet = true;
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
                tourPhoto = BitmapFactory.decodeFile(cameraPhotoPath, options);

                try {
                    ExifInterface ei = new ExifInterface(cameraPhotoPath);
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            tourPhoto = rotateImage(tourPhoto, 90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            tourPhoto = rotateImage(tourPhoto, 180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            tourPhoto = rotateImage(tourPhoto, 270);
                            break;
                        case ExifInterface.ORIENTATION_NORMAL:
                            // don't need to rotate?
                            break;
                    }
                } catch (Exception e) {
                    // We'll just deal with an unrotated image.
                }

                System.out.println(options.outWidth);
                System.out.println(options.outHeight);

                imgView.setImageBitmap(tourPhoto);
                tourPhotoSet = true;

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

    public static Bitmap rotateImage (Bitmap source, float angle) {
        Bitmap rotated;

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        rotated = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        return rotated;
    }

    private boolean validateInput(EditText tourTitleInput, EditText tourSummaryInput) {
        boolean valid = true;

        tourTitleInput.setError(null);
        tourTitleInput.setError(null);

        tourTitle = tourTitleInput.getText().toString();
        tourSummary = tourSummaryInput.getText().toString();

        if (TextUtils.isEmpty(tourTitle)) {
            tourTitleInput.setError("Title is required.");
            valid = false;
        } /*else if (tourTitle.length() < 10) {
            tourTitleInput.setError("Title should be a bit longer.");
            valid = false;
        }*/

        if (TextUtils.isEmpty(tourSummary)) {
            tourSummaryInput.setError("Summary is required.");
            valid = false;
        } /*else if (tourSummary.length() < 50) {
            tourSummaryInput.setError("Summary should be more detailed.");
            valid = false;
        }*/

        if (valid && !tourPhotoSet) {
            new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_warning_black)
                .setTitle("Tour photo is required.")
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
        getDialog().setCanceledOnTouchOutside(false);
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

    private class TourPhotoUpload extends AsyncTask<Bitmap, Void, String> {

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
                    Tour t = new Tour(0, null, tourTitle, tourSummary, new ArrayList<Checkpoint>(), photoUrl);
                    t.setPhoto(photoUrl);
                    mListener.onTourMetadataFinish(t);
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

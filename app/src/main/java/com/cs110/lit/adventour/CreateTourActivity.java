package com.cs110.lit.adventour;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.cs110.lit.adventour.model.Checkpoint;
import com.cs110.lit.adventour.model.Tour;
import com.cs110.lit.adventour.model.User;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class CreateTourActivity extends AppCompatActivity implements OnMapReadyCallback,
        CreateTourMetadataFragment.CreateTourMetadataListener,
        CreateCheckpointMetadataFragment.CreateCheckpointMetadataListener {

    // Attributes for location
    private static final int LOCATION_REQUEST_CODE = 0;
    // Distance change before refresh (meters).
    private static final float LOCATION_REFRESH_DISTANCE = 1.0f;

    private ProgressDialog progressDialog;

    private SharedPreferences prefs;

    private LocationManager locationManager;
    private Location currentLocation;
    private LatLng currentLatLng;
    private String locationNetworkProvider;

    private DialogFragment openFragment;

    private Tour tour;
    private ArrayList<Checkpoint> checkpoints = new ArrayList<>();

    private GoogleMap mMap;
    private PolylineOptions lineOptions = new PolylineOptions();
    private Polyline line;
    private List<LatLng> points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tour);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Preparing map...");
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void showTourMetadataDialog () {
        // Create and show the dialog.
        openFragment = CreateTourMetadataFragment.newInstance(this);
        openFragment.show(getSupportFragmentManager(), "tour_metadata_dialog");
    }

    private void showCheckpointMetadataDialog (boolean cancelable) {
        openFragment = CreateCheckpointMetadataFragment.newInstance(this, cancelable);
        openFragment.show(getSupportFragmentManager(), "checkpoint_metadata_dialog");
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        line = mMap.addPolyline(lineOptions);
        points = line.getPoints();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }

        mMap.setMyLocationEnabled(true);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationNetworkProvider = LocationManager.NETWORK_PROVIDER;

        locationManager.requestLocationUpdates(locationNetworkProvider, 500, 0,
                new android.location.LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        currentLocation = location;
                        currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                        System.out.println("location provider status: " + status);
                    }

                    @Override
                    public void onProviderEnabled(String provider) {}

                    @Override
                    public void onProviderDisabled(String provider) {}
                });

        currentLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        LatLng curLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLatLng, 14));

        progressDialog.dismiss();

        if (openFragment != null)
            openFragment.dismiss();

        showTourMetadataDialog();

        FloatingActionButton createCheckpt = (FloatingActionButton) findViewById(R.id.fab_touropts_create_checkpt);
        if (createCheckpt != null)
            createCheckpt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCheckpointMetadataDialog(true);
                }
            });

        FloatingActionButton endTour = (FloatingActionButton) findViewById(R.id.fab_touropts_end_tour);
        if (endTour != null)
            endTour.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

        FloatingActionButton finishTour = (FloatingActionButton) findViewById(R.id.fab_touropts_finish_tour);
        if (finishTour != null)
            finishTour.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressDialog.setMessage("Uploading Tour...");
                    progressDialog.setCancelable(false);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    new TourUpload().execute(tour);
                }
            });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        openFragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_warning_black)
            .setTitle("Ending Tour")
            .setMessage("Are you sure you want to end this tour? It won't be saved!")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            })
            .setNegativeButton("No", null)
            .show();
    }

    @Override
    public void onTourMetadataFinish(Tour t) {
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(t.getTitle());

        prefs = getApplicationContext().getSharedPreferences("Login", 0);
        t.setUser(new User(prefs.getInt("uid", -1), prefs.getString("uname", ""), ""));

        tour = t;
        openFragment.dismiss();

        showCheckpointMetadataDialog(false);
    }

    @Override
    public void onCheckpointMetadataFinish(Checkpoint c) {
        c.setLatitude(currentLocation.getLatitude());
        c.setLongitude(currentLocation.getLongitude());

        boolean first = false;

        if (tour.getListOfCheckpoints().size() == 0)
            first = true;

        c.setOrder_num(tour.getListOfCheckpoints().size() + 1);
        tour.getListOfCheckpoints().add(c);

        points.add(currentLatLng);
        line.setPoints(points);

        if (first)
            mMap.addMarker(new MarkerOptions().position(currentLatLng)
                .title(c.getTitle())
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.start_flag)));
        else
            mMap.addMarker(new MarkerOptions().position(currentLatLng)
                .title(c.getTitle())
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));

        openFragment.dismiss();
    }

    private class TourUpload extends AsyncTask<Tour, Void, String> {
        @Override
        protected String doInBackground(Tour... params) {
            Gson gson = new Gson();
            return gson.toJson(params[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            DB.uploadTour(s, CreateTourActivity.this, new DB.Callback<Integer> () {
                @Override
                public void onSuccess(Integer tour_id) {
                    progressDialog.dismiss();
                    Intent intent = new Intent(CreateTourActivity.this, OverviewActivity.class);
                    intent.putExtra(OverviewActivity.TOUR_ID, tour_id.intValue());
                    CreateTourActivity.this.startActivity(intent);
                    CreateTourActivity.this.finish();
                }

                @Override
                public void onFailure(Integer integer) {
                    progressDialog.dismiss();
                    new AlertDialog.Builder(CreateTourActivity.this)
                        .setTitle("Upload Failed.")
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

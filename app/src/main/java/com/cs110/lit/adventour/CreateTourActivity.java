package com.cs110.lit.adventour;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
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

    private DialogFragment tourMetadataFragment;
    private DialogFragment checkpointMetadataFragment;

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

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void showTourMetadataDialog () {
        // Create and show the dialog.
        tourMetadataFragment = CreateTourMetadataFragment.newInstance(this);
        tourMetadataFragment.show(getSupportFragmentManager(), "tour_metadata_dialog");
    }

    private void showCheckpointMetadataDialog (boolean cancelable) {
        checkpointMetadataFragment = CreateCheckpointMetadataFragment.newInstance(this, cancelable);
        checkpointMetadataFragment.show(getSupportFragmentManager(), "checkpoint_metadata_dialog");
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

        currentLocation = locationManager.getLastKnownLocation(locationNetworkProvider);
        LatLng curLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLatLng, 14));

        progressDialog.dismiss();

        if (tourMetadataFragment != null)
            tourMetadataFragment.dismiss();
        if (checkpointMetadataFragment != null)
            checkpointMetadataFragment.dismiss();

        showTourMetadataDialog();

        FloatingActionButton createCheckpt = (FloatingActionButton) findViewById(R.id.fab_touropts_create_checkpt);
        createCheckpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCheckpointMetadataDialog(false);
            }
        });

        FloatingActionButton endTour = (FloatingActionButton) findViewById(R.id.fab_touropts_end_tour);
        endTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
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

        // prefs = getApplicationContext().getSharedPreferences("Login", 0);
        // t.setUser(new User(prefs.getInt("uid", -1), prefs.getString("uname", ""), ""));

        tour = t;
        tourMetadataFragment.dismiss();

        showCheckpointMetadataDialog(true);
    }

    @Override
    public void onCheckpointMetadataFinish(Checkpoint c) {
        c.setLatitude(currentLocation.getLatitude());
        c.setLongitude(currentLocation.getLongitude());

        boolean first = false;

        if (tour.getListOfCheckpoints().size() == 0)
            first = true;

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

        checkpointMetadataFragment.dismiss();
    }
}

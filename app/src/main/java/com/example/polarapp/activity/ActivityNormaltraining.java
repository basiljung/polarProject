package com.example.polarapp.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.polarapp.R;
import com.example.polarapp.polar.PolarSDK;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.SphericalUtil;

import java.util.List;

public class ActivityNormaltraining extends AppCompatActivity implements PolarSDK.CallbackInterfaceActivity,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    private Toolbar toolbar;
    private Chronometer chronometer;
    private Button startChronometer,pauseChronometer,resetChronometer;
    private boolean runningChronometer;
    private long pauseOffset;
    private GoogleMap map;
    private Polyline gpsTrack;
    private SupportMapFragment mapFragment;
    private GoogleApiClient googleApiClient;
    private LatLng lastKnownLatLng;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private TextView hrData;
    private PolarSDK polarSDK;
    private double totalDistance = 0;
    private List<LatLng> points; // Polylines, we need to save them in out database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normaltraining);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //***************Chronometer implementation**************************
        chronometer = findViewById(R.id.chronometer);
        startChronometer = findViewById(R.id.startChronometer);
        pauseChronometer = findViewById(R.id.pauseChronometer);
        resetChronometer = findViewById(R.id.resetChronometer);

        startChronometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                startChronometer(root);
            }
        });
        pauseChronometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                pauseChronometer(root);
            }
        });
        resetChronometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                resetChronometer(root);
            }
        });

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        polarSDK = (PolarSDK) getApplicationContext();
        polarSDK.setCallbackInterfaceActivity(this);

        hrData = findViewById(R.id.hrData);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

    }

    public void startChronometer(View v){
        if(!runningChronometer){
            chronometer.setBase(SystemClock.elapsedRealtime()- pauseOffset);
            chronometer.start();
            runningChronometer = true;
            resetChronometer.setVisibility(View.INVISIBLE);
        }
    }

    public void resetChronometer(View v){
        chronometer.setBase(SystemClock.elapsedRealtime());
        runningChronometer = false;
        pauseOffset = 0;
        resetChronometer.setVisibility(View.INVISIBLE);
    }
    public void pauseChronometer(View v){
        if(runningChronometer){
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            runningChronometer = false;
            resetChronometer.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        getDeviceLocation();

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.CYAN);
        polylineOptions.width(4);
        gpsTrack = map.addPolyline(polylineOptions);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (googleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastKnownLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        updateTrack();
    }

    protected void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleApiClient, this);
    }

    private void updateTrack() {
        points = gpsTrack.getPoints();
        points.add(lastKnownLatLng);
        gpsTrack.setPoints(points);
        if (points.size() >= 2) {
            double distance = SphericalUtil.computeDistanceBetween(points.get(points.size()-2), points.get(points.size()-1));
            totalDistance = totalDistance + distance;
            Toast.makeText(getApplicationContext(), "The total distance is " + totalDistance, Toast.LENGTH_SHORT).show();
            Log.d("MyApp", "The total distance is " + totalDistance);
        }
    }

    private void getDeviceLocation() {
        try {
            Task locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        Location initialLocation = (Location) task.getResult();
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(initialLocation.getLatitude(),
                                        initialLocation.getLongitude()), 15));
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void hrUpdateData(int hr) {
        hrData.setText(String.valueOf(hr));
    }
}

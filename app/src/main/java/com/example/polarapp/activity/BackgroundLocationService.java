package com.example.polarapp.activity;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class BackgroundLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private final LocationServiceBinder binder = new LocationServiceBinder();
    private final String TAG = "BackgroundLocationService";
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    private NotificationManager notificationManager;

    private Polyline gpsTrack;
    private SupportMapFragment mapFragment;
    private GoogleApiClient googleApiClient;
    private LatLng lastKnownLatLng;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest locationRequest;

    private final long LOCATION_INTERVAL = 5000;
    private final long LOCATION_INTERVAL_FASTEST = 3000;
    private final long LOCATION_DISTANCE = 10;

    private double totalDistance = 0.0; // m
    private double actualSpeed = 0.0; //km/h
    private double averageSpeed = 0.0; //km/h
    private Integer heartRateAverage = 0; //per trainigssession
    private double totalTimeInSec; // sec
    private double totalTimeInMin; // min
    private double totalTimeInHour; // min
    private Timestamp startTimestamp = null; // timestamp
    private List<LatLng> points; // Polylines, we need to save them in out database
    private ArrayList<Integer> hrList;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /*private class LocationListener implements android.location.LocationListener {
        private Location lastLocation = null;
        private final String TAG = "LocationListener";
        private Location mLastLocation;

        public LocationListener(String provider) {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            mLastLocation = location;
            Log.i(TAG, "LocationChanged: " + location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + status);
        }
    }*/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        //startGoogleAPI();
        startForeground(12345678, getNotification());
    }

    public void startGoogleAPI() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        googleApiClient.connect();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationManager != null) {
            try {
                //mLocationManager.removeUpdates(mLocationListener);
                stopForeground(true);
                stopLocationUpdates();
                googleApiClient.disconnect();
            } catch (Exception ex) {
                Log.i(TAG, "fail to remove location listners, ignore", ex);
            }
        }
    }

    private void sendMessageToActivity(LatLng lastLocation){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("ServiceToActivityAction");
        broadcastIntent.putExtra("ServiceToActivityKey", lastLocation);
        sendBroadcast(broadcastIntent);
    }

    private Notification getNotification() {
        NotificationChannel channel = new NotificationChannel("channel_01", "My Channel", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        Notification.Builder builder = new Notification.Builder(getApplicationContext(), "channel_01").setAutoCancel(true);
        return builder.build();
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

    protected void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        updateTrack(location);
    }

    public void updateTrack(Location location) {
        lastKnownLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        sendMessageToActivity(lastKnownLatLng);
        //points = gpsTrack.getPoints();
        //points.add(lastKnownLatLng);
        //gpsTrack.setPoints(points);
        //if (points.size() >= 2) {
            //double distance = SphericalUtil.computeDistanceBetween(points.get(points.size() - 2), points.get(points.size() - 1));
            //totalDistance = totalDistance + distance;
            //actualSpeed = totalDistance / totalTimeInHour;
            //Toast.makeText(getApplicationContext(), "Latitude: " + location.getLatitude() + " - Longitude: "+ location.getLongitude(), Toast.LENGTH_SHORT).show();
            //Log.d("BackgroundLocationService", "Latitude: " + lastKnownLatLng.latitude + " - Longitude: "+ lastKnownLatLng.longitude);
       // }
    }

    public class LocationServiceBinder extends Binder {
        public BackgroundLocationService getService() {
            return BackgroundLocationService.this;
        }
    }

}

package com.example.polarapp.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.polarapp.BuildConfig;
import com.example.polarapp.MainActivity;
import com.example.polarapp.R;
import com.example.polarapp.polar.PolarSDK;
import com.example.polarapp.preferencesmanager.ProfilePreferencesManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.android.SphericalUtil;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;

public class NormalTrainingActivity extends AppCompatActivity implements PolarSDK.CallbackInterfaceActivity,
        OnMapReadyCallback {

    private ProfilePreferencesManager profilePreferencesManager;
    private static final String PROFILE_USER_ID = "profile_user_id";
    private DecimalFormat df = new DecimalFormat();

    private Toolbar toolbar;
    private Chronometer chronometer;
    private Button startChronometer, stopChronometer, resetChronometer, saveTrainingBtn;
    private boolean runningChronometer;
    private long pauseOffset;
    private GoogleMap map;
    private Polyline gpsTrack;
    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private TextView hrData, txtAverageSpeed, txtDistance;
    private PolarSDK polarSDK;
    private double totalDistance = 0.0; // m
    private double actualSpeed = 0.0; //km/h
    private double averageSpeed = 0.0; //km/h
    private Integer heartRateAverage = 0; //per trainigssession
    private double totalTimeInSec; // sec
    private double totalTimeInMin; // min
    private double totalTimeInHour; // min
    private Timestamp startTimestamp = null; // timestamp
    private List<LatLng> points = new ArrayList<>(); // Polylines, we need to save them in out database
    private ArrayList<Integer> hrList;

    public BackgroundLocationService gpsService;
    public boolean mTracking = false;

    private LocationUpdateData serviceReceiver;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            String name = className.getClassName();
            if (name.endsWith("BackgroundLocationService")) {
                gpsService = ((BackgroundLocationService.LocationServiceBinder) service).getService();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            if (className.getClassName().equals("BackgroundLocationService")) {
                gpsService = null;
            }
        }
    };

    public class LocationUpdateData extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle notificationData = intent.getExtras();
            LatLng location = (LatLng) notificationData.get("ServiceToActivityKey");
            Log.d("BackgroundServiceBroadcastReceived", "Latitude: " + location.latitude + " - Longitude: " + location.longitude);

            if (runningChronometer) {
                points = gpsTrack.getPoints();
                points.add(location);
                gpsTrack.setPoints(points);
                if (points.size() >= 2) {
                    double distance = SphericalUtil.computeDistanceBetween(points.get(points.size() - 2), points.get(points.size() - 1));
                    totalDistance = totalDistance + distance;
                    actualSpeed = totalDistance / totalTimeInHour;
                    Log.d("BroadcastReceiver", "Total distance: " + totalDistance);
                }
                long tT = SystemClock.elapsedRealtime() - chronometer.getBase();
                totalTimeInHour = (tT / 1000.0) / 60.0 / 60.0;
                averageSpeed = (totalDistance / 1000.0) / totalTimeInHour;
                txtDistance.setText((int) totalDistance + " m");
                txtAverageSpeed.setText(df.format(averageSpeed) + " km/h");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_training);

        serviceReceiver = new LocationUpdateData();
        IntentFilter intentSFilter = new IntentFilter("ServiceToActivityAction");
        registerReceiver(serviceReceiver, intentSFilter);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);
        final Intent intent = new Intent(this.getApplicationContext(), BackgroundLocationService.class);
        this.getApplication().startService(intent);
        this.getApplication().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        df.setMaximumFractionDigits(2);
        df.setDecimalFormatSymbols(otherSymbols);

        profilePreferencesManager = new ProfilePreferencesManager(getBaseContext());

        //***************Chronometer implementation**************************
        chronometer = findViewById(R.id.chronometer);
        startChronometer = findViewById(R.id.startChronometer);
        stopChronometer = findViewById(R.id.stopChronometer);
        resetChronometer = findViewById(R.id.resetChronometer);
        saveTrainingBtn = findViewById(R.id.saveTrainingBtn);
        hrData = findViewById(R.id.txtHRData);
        txtAverageSpeed = findViewById(R.id.txtAverageSpeed);
        txtDistance = findViewById(R.id.txtDistance);
        hrList = new ArrayList<>();

        startChronometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                startChronometer();
                checkPermissions();
            }
        });

        stopChronometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                stopChronometer();
            }
        });

        resetChronometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                resetChronometer();
            }
        });

        saveTrainingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                saveTraining();
            }
        });

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        polarSDK = (PolarSDK) getApplicationContext();
        polarSDK.setCallbackInterfaceActivity(this);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void checkPermissions() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        gpsService.startGoogleAPI();
                        mTracking = true;
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                            intent.setData(uri);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void saveTraining() {
        Integer sum = 0;
        for (int i = 0; i < hrList.size(); i++) {
            sum += hrList.get(i);
        }
        if (hrList.size() > 0) {
            heartRateAverage = sum / hrList.size();
        }

        averageSpeed = (totalDistance / 1000.0) / totalTimeInHour;
        Log.i("MyApp", "average speed: " + averageSpeed);
        Log.i("MyApp", "average hr " + heartRateAverage);
        Log.i("MyApp", "total time in min " + totalTimeInMin);
        Log.i("MyApp", "total time in sec " + totalTimeInSec);
        Log.i("MyApp", "total distance in m " + totalDistance);
        Log.i("MyApp", "location points... in progress ");
        if (!isOnline()) {
            showDialog();
        } else {
            saveInDB();
        }
    }

    private void backToMainActivity() {
        Intent intent = new Intent(NormalTrainingActivity.this, MainActivity.class);
        startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    public void saveInDB() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> activity1 = new HashMap<>();
        Log.d("MyApp", "Time: " + startTimestamp.getTime());

        activity1.put("UUID", profilePreferencesManager.getStringProfileValue(PROFILE_USER_ID));
        activity1.put("type", "run");
        activity1.put("timestamp", startTimestamp.getTime());
        activity1.put("time", (int) Math.ceil(totalTimeInMin));
        activity1.put("distance", (int) totalDistance);
        activity1.put("avgSpeed", df.format(averageSpeed));
        activity1.put("locationPoints", points);
        activity1.put("avgHR", df.format(heartRateAverage));
        activity1.put("interval", 1);

        db.collection("activities")
                .add(activity1)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("MyApp", "DocumentSnapshot written with ID: " + documentReference.getId());
                        backToMainActivity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("MyApp", "Error adding document", e);
                    }
                });
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Internet connection needed to save the training!")
                .setCancelable(false)
                .setPositiveButton("Connect to Internet", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(Settings.ACTION_SETTINGS));
                        if (isOnline()) {
                            saveInDB();
                        }
                    }
                })
                .setNegativeButton("Quit without saving", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        backToMainActivity();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    public void startChronometer() {
        Calendar cal = Calendar.getInstance();
        startTimestamp = new Timestamp(cal.getTimeInMillis());
        if (!runningChronometer) {
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            chronometer.start();
            runningChronometer = true;
            startChronometer.setVisibility(View.INVISIBLE);
            stopChronometer.setVisibility(View.VISIBLE);
            resetChronometer.setVisibility(View.INVISIBLE);
            saveTrainingBtn.setVisibility(View.INVISIBLE);
        }
    }

    public void stopChronometer() {
        long tT;
        if (runningChronometer) {
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            tT = SystemClock.elapsedRealtime() - chronometer.getBase();
            totalTimeInSec = (tT / 1000.0);
            totalTimeInMin = totalTimeInSec / 60.0;
            totalTimeInHour = totalTimeInSec / 60.0 / 60.0;
            runningChronometer = false;
            startChronometer.setVisibility(View.VISIBLE);
            stopChronometer.setVisibility(View.INVISIBLE);
            resetChronometer.setVisibility(View.VISIBLE);
            saveTrainingBtn.setVisibility(View.VISIBLE);
        }
    }

    public void resetChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        runningChronometer = false;
        pauseOffset = 0;
        startChronometer.setVisibility(View.VISIBLE);
        stopChronometer.setVisibility(View.INVISIBLE);
        resetChronometer.setVisibility(View.INVISIBLE);
        saveTrainingBtn.setVisibility(View.INVISIBLE);
        points.clear();
        hrList.clear();
        gpsTrack.setPoints(points);
        txtAverageSpeed.setText("0 km/h");
        totalDistance = 0;
        txtDistance.setText((int) totalDistance + " m");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        getDeviceLocation();

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        polylineOptions.width(4);
        gpsTrack = map.addPolyline(polylineOptions);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    }

    private void getDeviceLocation() {
        try {
            Task locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
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
        if (runningChronometer) {
            hrList.add(hr);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        final Intent intent = new Intent(this.getApplication(), BackgroundLocationService.class);
        this.getApplication().stopService(intent);
        this.getApplication().unbindService(serviceConnection);
        unregisterReceiver(serviceReceiver);
    }
}

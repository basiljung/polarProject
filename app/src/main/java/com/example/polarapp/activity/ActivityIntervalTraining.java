package com.example.polarapp.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.polarapp.BuildConfig;
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
import java.util.Locale;
import java.util.Map;

import butterknife.ButterKnife;

public class ActivityIntervalTraining extends AppCompatActivity implements PolarSDK.CallbackInterfaceActivity, OnMapReadyCallback {

    private ProfilePreferencesManager profilePreferencesManager;
    private static final String PROFILE_USER_ID = "profile_user_id";
    private DecimalFormat df = new DecimalFormat();

    private Toolbar toolbar;
    private String pickerTime;
    private TextView hrData, textViewTimer, lapcounter, txtAverageSpeed, txtDistance;
    private Button pauseStartBtn, resetBtn, resetLapsBtn, savetrainingBtn, nextLapButton;

    private CountDownTimer countDownTimer;
    private boolean runningTimer;
    private long TimeLeftInMillis = 0;

    private GoogleMap map;
    private Polyline gpsTrack;
    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PolarSDK polarSDK;

    private double lapDistance = 0.0;
    private List<LatLng> lapPoints = new ArrayList<>();
    private ArrayList<Integer> hrList = new ArrayList<>();

    private double totalDistance = 0.0;
    private double totalAvgSpeed = 0.0;
    private static List<LatLng> points = new ArrayList<>(); // Polylines, we need to save them in out database
    private static ArrayList<Integer> globalHRList = new ArrayList<>();

    private double averageSpeed = 0.0; //km/h
    private double heartRateAverage = 0.0; //per trainigssession
    private double totalTimeInSec; // sec
    private double totalTimeInMin; // min
    private double totalTimeInHour; // hour
    private double timeSetInSec; // sec
    private double timeSetInMin; // min
    private double timeSetInHour; // hour

    private Timestamp startTimestamp = null; // timestamp

    private int lapCount = 0;

    public BackgroundService gpsService;
    public boolean mTracking = false;

    private ActivityIntervalTraining.LocationUpdateData serviceReceiver;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            String name = className.getClassName();
            if (name.endsWith("BackgroundService")) {
                gpsService = ((BackgroundService.LocationServiceBinder) service).getService();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            if (className.getClassName().equals("BackgroundService")) {
                gpsService = null;
            }
        }
    };

    public class LocationUpdateData extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle notificationData = intent.getExtras();
            LatLng location = (LatLng) notificationData.get("ServiceToActivityKey");

            if (runningTimer) {
                lapPoints = gpsTrack.getPoints();
                lapPoints.add(location);
                gpsTrack.setPoints(lapPoints);
                if (lapPoints.size() >= 2) {
                    double distance = SphericalUtil.computeDistanceBetween(lapPoints.get(lapPoints.size() - 2), lapPoints.get(lapPoints.size() - 1));
                    lapDistance = lapDistance + distance;
                    Toast.makeText(getApplicationContext(), "Total distance: " + lapDistance, Toast.LENGTH_SHORT).show();
                    Log.d("BackgroundService", "Total distance: " + lapDistance);
                }
                double tT = (timeSetInSec * 1000) - TimeLeftInMillis;
                double auxTime = (tT / 1000.0) / 60.0 / 60.0;
                averageSpeed = (lapDistance/1000.0) / auxTime;
                txtDistance.setText((int) lapDistance + " m");
                txtAverageSpeed.setText(df.format(averageSpeed) + " km/h");
            }
        }

        public void saveLapData() {
            Log.d("MyAppSavePre", "Distance: " + totalDistance + ", size points: " + points.size() + ", lap distance: " + lapDistance);
            totalDistance = totalDistance + lapDistance;
            for (int i=0;i<hrList.size(); i++) {
                globalHRList.add(hrList.get(i));
            }
            for (int i=0;i<lapPoints.size(); i++) {
                points.add(lapPoints.get(i));
            }
            Log.d("MyAppSavePost", "Distance: " + totalDistance + ", size points: " + points.size());
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intervaltraining);

        serviceReceiver = new ActivityIntervalTraining.LocationUpdateData();
        IntentFilter intentSFilter = new IntentFilter("ServiceToActivityAction");
        registerReceiver(serviceReceiver, intentSFilter);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);
        final Intent intent = new Intent(this.getApplicationContext(), BackgroundService.class);
        this.getApplication().startService(intent);
        this.getApplication().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        df.setMaximumFractionDigits(2);
        df.setDecimalFormatSymbols(otherSymbols);

        profilePreferencesManager = new ProfilePreferencesManager(getBaseContext());

        Intent pickerIntent = getIntent();
        pickerTime = pickerIntent.getStringExtra("Picker_Time");
        String[] times = pickerTime.split(":");
        for (int i = 0; i < times.length; i++) {
            if (times[i].length() == 1) {
                times[i] = "0" + times[i];
            }
        }
        pickerTime = times[0] + ":" + times[1];
        TimeLeftInMillis = Integer.valueOf(times[0]) * 60 * 1000 + Integer.valueOf(times[1]) * 1000;
        timeSetInSec = TimeLeftInMillis / 1000.0;
        timeSetInMin = TimeLeftInMillis / 1000.0 / 60.0;
        timeSetInHour = TimeLeftInMillis / 1000.0 / 60.0 / 60.0;

        hrData = findViewById(R.id.txtHRData);
        txtAverageSpeed = findViewById(R.id.txtAverageSpeed);
        txtDistance = findViewById(R.id.txtDistance);
        textViewTimer = findViewById(R.id.txtVTimePicker);
        lapcounter = findViewById(R.id.lapcounter);
        resetLapsBtn = findViewById(R.id.resetLapsBtn);
        nextLapButton = findViewById(R.id.nextLapButton);
        textViewTimer.setText(pickerTime);
        pauseStartBtn = findViewById(R.id.pauseStartBtn);
        resetBtn = findViewById(R.id.resetBtn);

        polarSDK = (PolarSDK) getApplicationContext();
        polarSDK.setCallbackInterfaceActivity(this);

        savetrainingBtn = findViewById(R.id.saveTrainingBtn);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        hrList = new ArrayList<>();

        pauseStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                if (runningTimer) {
                    pauseTimer(root);
                } else {
                    checkPermissions();
                    startTimer(root);
                }
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                resetTimer(root);
            }
        });

        resetLapsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                lapCount = 0;
                lapDistance = 0;
                totalDistance = 0;
                totalAvgSpeed = 0;
                hrList.clear();
                globalHRList.clear();
                points.clear();
                lapPoints.clear();
                txtDistance.setText(totalDistance + " m");
                txtAverageSpeed.setText("0 km/h");
                gpsTrack.setPoints(points);
                String[] times = pickerTime.split(":");
                TimeLeftInMillis = Integer.valueOf(times[0]) * 60 * 1000 + Integer.valueOf(times[1]) * 1000;
                updateCountDownText();
                lapcounter.setText("Done laps: " + lapCount);
                pauseStartBtn.setText("Start");
                pauseStartBtn.setVisibility(View.VISIBLE);
                resetBtn.setVisibility(View.INVISIBLE);
                nextLapButton.setVisibility(View.INVISIBLE);
                savetrainingBtn.setVisibility(View.INVISIBLE);
                resetLapsBtn.setVisibility(View.INVISIBLE);
            }
        });

        nextLapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lapDistance = 0;
                hrList.clear();
                lapPoints.clear();
                txtDistance.setText(lapDistance + " m");
                txtAverageSpeed.setText("0 km/h");
                String[] times = pickerTime.split(":");
                TimeLeftInMillis = Integer.valueOf(times[0]) * 60 * 1000 + Integer.valueOf(times[1]) * 1000;
                updateCountDownText();
                pauseStartBtn.setText("Start");
                pauseStartBtn.setVisibility(View.VISIBLE);
                resetBtn.setVisibility(View.INVISIBLE);
                nextLapButton.setVisibility(View.INVISIBLE);
                savetrainingBtn.setVisibility(View.INVISIBLE);
                resetLapsBtn.setVisibility(View.INVISIBLE);
                saveLapData();
            }
        });

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        savetrainingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                saveLapData();
                saveTraining();
            }
        });
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

    public void saveInDB() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> activity1 = new HashMap<>();

        activity1.put("UUID", profilePreferencesManager.getStringProfileValue(PROFILE_USER_ID));
        activity1.put("type", "run");
        activity1.put("timestamp", startTimestamp.getTime());
        activity1.put("time", (int) Math.ceil(totalTimeInMin));
        activity1.put("distance", (int) totalDistance);
        activity1.put("avgSpeed", df.format(totalAvgSpeed));
        activity1.put("locationPoints", points);
        activity1.put("avgHR", df.format(heartRateAverage));
        activity1.put("laps", lapCount);

        db.collection("activities")
                .add(activity1)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("MyApp", "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("MyApp", "Error adding document", e);
                    }
                });
    }

    private void updateCountDownText() {
        int minutes = (int) (TimeLeftInMillis / 1000) / 60;
        int seconds = (int) (TimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        textViewTimer.setText(timeLeftFormatted);
    }

    private void startTimer(View root) {
        Calendar cal = Calendar.getInstance();
        startTimestamp = new Timestamp(cal.getTimeInMillis());
        countDownTimer = new CountDownTimer(TimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                runningTimer = false;
                pauseStartBtn.setText("Start");
                pauseStartBtn.setVisibility(View.INVISIBLE);
                lapCount++;
                lapcounter.setText("Done laps: " + lapCount);
                totalTimeInSec = timeSetInSec * lapCount;
                totalTimeInMin = timeSetInMin * lapCount;
                totalTimeInHour = timeSetInHour * lapCount;
                resetBtn.setVisibility(View.INVISIBLE);
                nextLapButton.setVisibility(View.VISIBLE);
                resetLapsBtn.setVisibility(View.VISIBLE);
                savetrainingBtn.setVisibility(View.VISIBLE);
            }
        }.start();
        runningTimer = true;
        pauseStartBtn.setText("Pause");
        resetBtn.setVisibility(View.INVISIBLE);
        resetLapsBtn.setVisibility(View.INVISIBLE);
        nextLapButton.setVisibility(View.INVISIBLE);
        savetrainingBtn.setVisibility(View.INVISIBLE);
    }

    private void saveLapData() {
        Log.d("MyAppSavePre", "Distance: " + totalDistance + ", size points: " + points.size() + ", lap distance: " + lapDistance);
        totalDistance = totalDistance + lapDistance;
        for (int i=0;i<hrList.size(); i++) {
            globalHRList.add(hrList.get(i));
        }
        for (int i=0;i<lapPoints.size(); i++) {
            points.add(lapPoints.get(i));
        }
        Log.d("MyAppSavePost", "Distance: " + totalDistance + ", size points: " + points.size());
    }

    private void pauseTimer(View root) {
        countDownTimer.cancel();
        runningTimer = false;
        pauseStartBtn.setText("Start");
        resetBtn.setVisibility(View.VISIBLE);
        resetLapsBtn.setVisibility(View.INVISIBLE);
        savetrainingBtn.setVisibility(View.INVISIBLE);
        nextLapButton.setVisibility(View.INVISIBLE);
    }

    private void resetTimer(View root) {
        hrList.clear();
        lapDistance = 0;
        lapPoints.clear();
        gpsTrack.setPoints(points);
        txtDistance.setText(lapDistance + " m");
        txtAverageSpeed.setText("0 km/h");
        String[] times = pickerTime.split(":");
        TimeLeftInMillis = Integer.valueOf(times[0]) * 60 * 1000 + Integer.valueOf(times[1]) * 1000;
        updateCountDownText();
        resetBtn.setVisibility(View.INVISIBLE);
        resetLapsBtn.setVisibility(View.INVISIBLE);
        savetrainingBtn.setVisibility(View.INVISIBLE);
        pauseStartBtn.setVisibility(View.VISIBLE);
    }

    private void saveTraining() {
        int sum = 0;
        for (int i = 0; i < globalHRList.size(); i++) {
            sum += globalHRList.get(i);
        }
        if (globalHRList.size() > 0) {
            heartRateAverage = sum / globalHRList.size();
        }

        totalAvgSpeed = (totalDistance / 1000.0) / totalTimeInHour;
        Log.i("MyApp", "average speed: " + totalAvgSpeed + "");
        Log.i("MyApp", "average hr " + heartRateAverage);
        Log.i("MyApp", "total time in min " + totalTimeInMin);
        Log.i("MyApp", "total time in sec " + totalTimeInSec);
        Log.i("MyApp", "total distance in m " + totalDistance);
        Log.i("MyApp", "laps " + lapCount);
        Log.i("MyApp", "location points... in progress ");
        saveInDB();
        finish();
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
        if (runningTimer) {
            hrList.add(hr);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        final Intent intent = new Intent(this.getApplication(), BackgroundService.class);
        this.getApplication().stopService(intent);
        this.getApplication().unbindService(serviceConnection);
        unregisterReceiver(serviceReceiver);
    }
}

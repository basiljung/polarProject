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
import java.util.Locale;
import java.util.Map;

import butterknife.ButterKnife;

public class IntervalTrainingActivity extends AppCompatActivity implements PolarSDK.CallbackInterfaceActivity, OnMapReadyCallback {

    private ProfilePreferencesManager profilePreferencesManager;
    private static final String PROFILE_USER_ID = "profile_user_id";
    private DecimalFormat df = new DecimalFormat();

    private Toolbar toolbar;
    private String pickerTime;
    private TextView txtHRData, textViewTimer, lapCounter, txtAverageSpeed, txtDistance;
    private Button pauseStartButton, resetButton, resetLapsButton, saveTrainingButton, nextLapButton;

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
    private ArrayList<Integer> lapHRList = new ArrayList<>();

    private double totalDistance = 0.0;
    private double totalAvgSpeed = 0.0;
    private List<LatLng> totalPoints = new ArrayList<>(); // Polylines, we need to save them in out database
    private ArrayList<Integer> totalHRList = new ArrayList<>();

    private double averageSpeed = 0.0; // km/h

    private double heartRateAverage = 0.0; // per trainigssession
    private double totalTimeInSec; // sec
    private double totalTimeInMin; // min
    private double totalTimeInHour; // hour
    private double timeSetInSec; // sec
    private double timeSetInMin; // min
    private double timeSetInHour; // hour

    private Timestamp startTimestamp = null; // timestamp

    private int lapCount = 0;

    public BackgroundLocationService gpsService;
    public boolean mTracking = false;

    private IntervalTrainingActivity.LocationUpdateData serviceReceiver;

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

            if (runningTimer) {
                lapPoints = gpsTrack.getPoints();
                lapPoints.add(location);
                gpsTrack.setPoints(lapPoints);
                if (lapPoints.size() >= 2) {
                    double distance = SphericalUtil.computeDistanceBetween(lapPoints.get(lapPoints.size() - 2), lapPoints.get(lapPoints.size() - 1));
                    lapDistance = lapDistance + distance;
                    Toast.makeText(getApplicationContext(), "Total distance: " + lapDistance, Toast.LENGTH_SHORT).show();
                    Log.d("BackgroundLocationService", "Total distance: " + lapDistance);
                }
                double tT = (timeSetInSec * 1000) - TimeLeftInMillis;
                double auxTime = (tT / 1000.0) / 60.0 / 60.0;
                averageSpeed = (lapDistance / 1000.0) / auxTime;
                txtDistance.setText((int) lapDistance + " m");
                txtAverageSpeed.setText(df.format(averageSpeed) + " km/h");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interval_training);

        serviceReceiver = new IntervalTrainingActivity.LocationUpdateData();
        IntentFilter intentSFilter = new IntentFilter("ServiceToActivityAction");
        registerReceiver(serviceReceiver, intentSFilter);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        df.setMaximumFractionDigits(2);
        df.setDecimalFormatSymbols(otherSymbols);

        profilePreferencesManager = new ProfilePreferencesManager(getBaseContext());

        ButterKnife.bind(this);
        final Intent intent = new Intent(this.getApplicationContext(), BackgroundLocationService.class);
        this.getApplication().startService(intent);
        this.getApplication().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

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

        txtHRData = findViewById(R.id.txtHRData);
        txtAverageSpeed = findViewById(R.id.txtAverageSpeed);
        txtDistance = findViewById(R.id.txtDistance);
        textViewTimer = findViewById(R.id.txtVTimePicker);
        lapCounter = findViewById(R.id.lapCounter);
        resetLapsButton = findViewById(R.id.resetLapsBtn);
        nextLapButton = findViewById(R.id.nextLapButton);
        textViewTimer.setText(pickerTime);
        pauseStartButton = findViewById(R.id.pauseStartBtn);
        resetButton = findViewById(R.id.resetBtn);

        polarSDK = (PolarSDK) getApplicationContext();
        polarSDK.setCallbackInterfaceActivity(this);

        saveTrainingButton = findViewById(R.id.saveTrainingBtn);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        pauseStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                if (runningTimer) {
                    pauseTimer();
                } else {
                    checkPermissions();
                    startTimer();
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                resetTimer();
            }
        });

        resetLapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                resetLaps();
            }
        });

        nextLapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveLapData();
                nextLapAction();
            }
        });

        saveTrainingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                saveLapData();
                saveTraining();
            }
        });


        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void startTimer() {
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
                pauseStartButton.setText("Start");
                lapCount++;
                lapCounter.setText("Done laps: " + lapCount);
                totalTimeInSec = timeSetInSec * lapCount;
                totalTimeInMin = timeSetInMin * lapCount;
                totalTimeInHour = timeSetInHour * lapCount;

                pauseStartButton.setVisibility(View.INVISIBLE);
                resetButton.setVisibility(View.INVISIBLE);
                nextLapButton.setVisibility(View.VISIBLE);
                resetLapsButton.setVisibility(View.VISIBLE);
                saveTrainingButton.setVisibility(View.VISIBLE);
            }
        }.start();

        runningTimer = true;
        pauseStartButton.setText("Pause");
        resetButton.setVisibility(View.INVISIBLE);
        resetLapsButton.setVisibility(View.INVISIBLE);
        nextLapButton.setVisibility(View.INVISIBLE);
        saveTrainingButton.setVisibility(View.INVISIBLE);
    }

    private void pauseTimer() {
        countDownTimer.cancel();
        runningTimer = false;

        pauseStartButton.setText("Start");
        resetButton.setVisibility(View.VISIBLE);
        resetLapsButton.setVisibility(View.INVISIBLE);
        saveTrainingButton.setVisibility(View.INVISIBLE);
        nextLapButton.setVisibility(View.INVISIBLE);
    }

    private void resetTimer() {
        lapHRList.clear();
        lapDistance = 0;
        lapPoints.clear();
        gpsTrack.setPoints(totalPoints);
        txtDistance.setText((int) lapDistance + " m");
        txtAverageSpeed.setText("0 km/h");

        String[] times = pickerTime.split(":");
        TimeLeftInMillis = Integer.valueOf(times[0]) * 60 * 1000 + Integer.valueOf(times[1]) * 1000;
        updateCountDownText();

        resetButton.setVisibility(View.INVISIBLE);
        resetLapsButton.setVisibility(View.INVISIBLE);
        saveTrainingButton.setVisibility(View.INVISIBLE);
        pauseStartButton.setVisibility(View.VISIBLE);
    }

    private void nextLapAction() {
        lapDistance = 0;
        lapHRList.clear();
        lapPoints.clear();
        txtDistance.setText((int) lapDistance + " m");
        txtAverageSpeed.setText("0 km/h");

        String[] times = pickerTime.split(":");
        TimeLeftInMillis = Integer.valueOf(times[0]) * 60 * 1000 + Integer.valueOf(times[1]) * 1000;
        updateCountDownText();

        pauseStartButton.setText("Start");
        pauseStartButton.setVisibility(View.VISIBLE);
        resetButton.setVisibility(View.INVISIBLE);
        nextLapButton.setVisibility(View.INVISIBLE);
        saveTrainingButton.setVisibility(View.INVISIBLE);
        resetLapsButton.setVisibility(View.INVISIBLE);
    }

    private void resetLaps() {
        lapCount = 0;
        lapDistance = 0;
        totalDistance = 0;
        totalAvgSpeed = 0;
        lapHRList.clear();
        totalHRList.clear();
        totalPoints.clear();
        lapPoints.clear();
        txtDistance.setText((int) lapDistance + " m");
        txtAverageSpeed.setText("0 km/h");
        gpsTrack.setPoints(totalPoints);

        String[] times = pickerTime.split(":");
        TimeLeftInMillis = Integer.valueOf(times[0]) * 60 * 1000 + Integer.valueOf(times[1]) * 1000;
        updateCountDownText();

        lapCounter.setText("Done laps: " + lapCount);
        pauseStartButton.setText("Start");
        pauseStartButton.setVisibility(View.VISIBLE);
        resetButton.setVisibility(View.INVISIBLE);
        nextLapButton.setVisibility(View.INVISIBLE);
        saveTrainingButton.setVisibility(View.INVISIBLE);
        resetLapsButton.setVisibility(View.INVISIBLE);
    }

    private void saveTraining() {
        int sum = 0;
        for (int i = 0; i < totalHRList.size(); i++) {
            sum += totalHRList.get(i);
        }
        if (totalHRList.size() > 0) {
            heartRateAverage = sum / totalHRList.size();
        }

        totalAvgSpeed = (totalDistance / 1000.0) / totalTimeInHour;
        Log.i("MyApp", "average speed: " + totalAvgSpeed);
        Log.i("MyApp", "average hr " + heartRateAverage);
        Log.i("MyApp", "total time in min " + totalTimeInMin);
        Log.i("MyApp", "total time in sec " + totalTimeInSec);
        Log.i("MyApp", "total distance in m " + totalDistance);
        Log.i("MyApp", "laps " + lapCount);
        Log.i("MyApp", "location totalPoints... in progress ");

        if (!isOnline()) {
            showDialog();
        } else {
            saveInDB();
        }
    }

    private void backToMainActivity() {
        Intent intent = new Intent(IntervalTrainingActivity.this, MainActivity.class);
        startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
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
        activity1.put("locationPoints", totalPoints);
        activity1.put("avgHR", df.format(heartRateAverage));
        activity1.put("interval", lapCount);

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

    private void saveLapData() {
        Log.d("MyAppSavePre", "Distance: " + totalDistance + ", size totalPoints: " + totalPoints.size() + ", lap distance: " + lapDistance);
        totalDistance = totalDistance + lapDistance;
        for (int i = 0; i < lapHRList.size(); i++) {
            totalHRList.add(lapHRList.get(i));
        }
        for (int i = 0; i < lapPoints.size(); i++) {
            totalPoints.add(lapPoints.get(i));
        }
        Log.d("MyAppSavePost", "Distance: " + totalDistance + ", size totalPoints: " + totalPoints.size());
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

    private void updateCountDownText() {
        int minutes = (int) (TimeLeftInMillis / 1000) / 60;
        int seconds = (int) (TimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        textViewTimer.setText(timeLeftFormatted);
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
        txtHRData.setText(String.valueOf(hr));
        if (runningTimer) {
            lapHRList.add(hr);
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

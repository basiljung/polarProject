package com.example.polarapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.polarapp.R;
import com.example.polarapp.polar.PolarSDK;
import com.example.polarapp.preferencesmanager.ProfilePreferencesManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.tasks.*;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.android.SphericalUtil;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public class ActivityIntervalTraining extends AppCompatActivity implements PolarSDK.CallbackInterfaceActivity,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private ProfilePreferencesManager profilePreferencesManager;
    private static final String PROFILE_USER_ID = "profile_user_id";
    private DecimalFormat df = new DecimalFormat();

    private Toolbar toolbar;
    private String pickerTime;
    private TextView hrData, textViewTimer,lapcounter, txtAverageSpeed, txtDistance;
    private Button pauseStartBtn, resetBtn,resetLapsBtn, savetrainingBtn;
    private CountDownTimer countDownTimer;
    private boolean runningTimer;
    private long TimeLeftInMillis = 0;
    private GoogleMap map;
    private Polyline gpsTrack;
    private SupportMapFragment mapFragment;
    private GoogleApiClient googleApiClient;
    private LatLng lastKnownLatLng;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PolarSDK polarSDK;
    private double totalDistance = 0.0; // m
    private double averageSpeed = 0.0; //km/h
    private Integer heartRateAverage = 0; //per trainigssession
    private double totalTimeInSec; // sec
    private double totalTimeInMin; // min
    private double totalTimeInHour; // hour
    private double timeSetInSec; // sec
    private double timeSetInMin; // min
    private double timeSetInHour; // hour
    private Timestamp startTimestamp = null; // timestamp
    private List<LatLng> points; // Polylines, we need to save them in out database
    private int lapcount = 0;
    private ArrayList<Integer> hrList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intervaltraining);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        df.setMaximumFractionDigits(2);
        df.setDecimalFormatSymbols(otherSymbols);

        profilePreferencesManager = new ProfilePreferencesManager(getBaseContext());

        Intent intent = getIntent();
        pickerTime = intent.getStringExtra("Picker_Time");
        String[] times = pickerTime.split(":");
        for (int i = 0; i < times.length; i++) {
            if (times[i].length() == 1) {
                times[i] = "0" + times[i];
            }
        }
        pickerTime = times[0] + ":" + times[1];
        TimeLeftInMillis = Integer.valueOf(times[0]) * 60 * 1000 + Integer.valueOf(times[1]) * 1000;
        timeSetInSec = TimeLeftInMillis/1000.0;
        timeSetInMin = TimeLeftInMillis/1000.0/60.0;
        timeSetInHour = TimeLeftInMillis/1000.0/60.0/60.0;

        hrData = findViewById(R.id.txtHRData);
        txtAverageSpeed = findViewById(R.id.txtAverageSpeed);
        txtDistance = findViewById(R.id.txtDistance);
        textViewTimer = findViewById(R.id.txtVTimePicker);
        lapcounter = findViewById(R.id.lapcounter);
        resetLapsBtn = findViewById(R.id.resetLapsBtn);
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
                lapcount = 0;
                lapcounter.setText("done laps: "+lapcount+"");
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
        
        savetrainingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                saveTraining();
            }
        });
    }

    public void saveInDB(){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> activity1 = new HashMap<>();

        activity1.put("UUID", profilePreferencesManager.getStringProfileValue(PROFILE_USER_ID));
        activity1.put("type", "run");
        activity1.put("timestamp", startTimestamp.getTime());
        activity1.put("time", (int) totalTimeInSec);
        activity1.put("distance",(int) totalDistance);
        activity1.put("avgSpeed", df.format(averageSpeed));
        activity1.put("locationPoints", points);
        activity1.put("avgHR", heartRateAverage);
        activity1.put("laps", lapcount);

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
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
                lapcount++;
                lapcounter.setText("done laps: "+lapcount+"");
                totalTimeInSec = timeSetInSec*lapcount;
                totalTimeInMin = timeSetInMin*lapcount;
                totalTimeInHour = timeSetInHour*lapcount;
                resetBtn.setVisibility(View.VISIBLE);
                resetLapsBtn.setVisibility(View.VISIBLE);
                savetrainingBtn.setVisibility(View.VISIBLE);
            }
        }.start();
        runningTimer = true;
        pauseStartBtn.setText("Pause");
        resetBtn.setVisibility(View.INVISIBLE);
    }

    private void pauseTimer(View root) {
        countDownTimer.cancel();
        runningTimer = false;
        pauseStartBtn.setText("Start");
        resetBtn.setVisibility(View.VISIBLE);
        resetLapsBtn.setVisibility(View.VISIBLE);
    }

    private void resetTimer(View root) {
        String[] times = pickerTime.split(":");
        TimeLeftInMillis = Integer.valueOf(times[0]) * 60 * 1000 + Integer.valueOf(times[1]) * 1000;
        updateCountDownText();
        resetBtn.setVisibility(View.INVISIBLE);
        resetLapsBtn.setVisibility(View.INVISIBLE);
        pauseStartBtn.setVisibility(View.VISIBLE);
    }
    private void saveTraining(){
        Integer sum = 0;
        for (int i = 0; i < hrList.size(); i++) {
            sum += hrList.get(i);
        }
        if(hrList.size()>0){
            heartRateAverage = sum/hrList.size();
        }
        double totalDistanceInKm = totalDistance/1000.0;
        averageSpeed = totalDistanceInKm/totalTimeInHour;
        Log.i("MyApp","average speed: "+averageSpeed+"");
        Log.i("MyApp","average hr " + heartRateAverage);
        Log.i("MyApp","total time in min " + totalTimeInMin);
        Log.i("MyApp","total time in sec " + totalTimeInSec);
        Log.i("MyApp","total distance in m " + totalDistance);
        Log.i("MyApp","laps " + lapcount);
        Log.i("MyApp","location points... in progress ");
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
            double distance = SphericalUtil.computeDistanceBetween(points.get(points.size() - 2), points.get(points.size() - 1));
            totalDistance = totalDistance + distance;
            Toast.makeText(getApplicationContext(), "The total distance is " + totalDistance, Toast.LENGTH_SHORT).show();
            //Log.d("MyApp", "The total distance is " + totalDistance);
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
        if(runningTimer){
            hrList.add(hr);
            DecimalFormat f = new DecimalFormat("#0.00");
            String averageSpeedString = f.format(averageSpeed);
            String totalDistanceString = f.format(totalDistance);
            txtDistance.setText(totalDistanceString);
            txtAverageSpeed.setText(averageSpeedString);
        }
    }
}

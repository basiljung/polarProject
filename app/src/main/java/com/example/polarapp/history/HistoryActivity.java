package com.example.polarapp.history;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.polarapp.R;
import com.example.polarapp.activity.ActivityData;
import com.example.polarapp.activity.ActivityDataAdapter;
import com.example.polarapp.preferencesmanager.ProfilePreferencesManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Toolbar toolbar;
    private ProfilePreferencesManager profilePreferencesManager;
    private ArrayList<ActivityData> allActivityDataArrayList = new ArrayList<>();
    private ArrayList<ActivityData> runActivityDataArrayList = new ArrayList<>();
    private ArrayList<ActivityData> sleepActivityDataArrayList = new ArrayList<>();
    private ListView listView = null;
    private TabLayout tabLayout;
    private ActivityDataAdapter activityDataAdapter;
    private FirebaseFirestore db;

    private Dialog runDialog;
    private Dialog sleepDialog;
    private static final String PROFILE_USER_ID = "profile_user_id";
    private boolean isRunDialogFirstTime = true;
    private boolean isSleepDialogFirstTime = true;
    private SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);

    // Names in Database
    private static final String ACTIVITY_UUID = "UUID";
    private static final String ACTIVITY_TYPE = "type";
    private static final String ACTIVITY_TIMESTAMP = "timestamp";
    private static final String ACTIVITY_TIME = "time";
    private static final String ACTIVITY_DISTANCE = "distance";
    private static final String ACTIVITY_AVG_SPEED = "avgSpeed";
    private static final String ACTIVITY_LOCATION_POINTS = "locationPoints";
    private static final String ACTIVITY_AVG_HR = "avgHR";
    private static final String ACTIVITY_INTERVAL = "interval";
    private static final String ACTIVITY_DEEP_SLEEP_TIME = "deepSleepTime";
    private static final String ACTIVITY_NIGHT_MOVES = "nightMoves";

    // Google Maps
    private GoogleMap map;
    private Polyline gpsTrack;
    private SupportMapFragment mapFragment;
    private GoogleApiClient googleApiClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        allActivityDataArrayList = new ArrayList<>();
        runActivityDataArrayList = new ArrayList<>();
        sleepActivityDataArrayList = new ArrayList<>();

        runDialog = new Dialog(this);
        sleepDialog = new Dialog(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        tabLayout = findViewById(R.id.tabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        setListViewAdapter(0);
                        break;
                    case 1:
                        setListViewAdapter(1);
                        break;
                    case 2:
                        setListViewAdapter(2);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        listView = findViewById(R.id.historyList);
        setListViewAdapter(0);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                int tabPosition = tabLayout.getSelectedTabPosition();
                ActivityData selectedActivity;
                switch (tabPosition) {
                    case 0:
                        selectedActivity = new ActivityData(allActivityDataArrayList.get(pos));
                        if (selectedActivity.getType().toLowerCase().equals("sleep")) {
                            showSleepPopup(selectedActivity);
                        } else {
                            showRunPopup(selectedActivity);
                        }
                        break;
                    case 1:
                        selectedActivity = new ActivityData(runActivityDataArrayList.get(pos));
                        showRunPopup(selectedActivity);
                        break;
                    case 2:
                        selectedActivity = new ActivityData(sleepActivityDataArrayList.get(pos));
                        showSleepPopup(selectedActivity);
                        break;
                }
            }
        });

        db = FirebaseFirestore.getInstance();

        profilePreferencesManager = new ProfilePreferencesManager(getApplication().getBaseContext());

        Log.d("MyApp", "Data UUID : " + profilePreferencesManager.getStringProfileValue(PROFILE_USER_ID));

        final Query activity = db.collection("activities").whereEqualTo("UUID", profilePreferencesManager.getStringProfileValue(PROFILE_USER_ID));

        activity
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("MyApp", document.getId() + " => " + document.getData());

                                ActivityData activityData = new ActivityData();

                                String type = document.get(ACTIVITY_TYPE).toString();
                                Timestamp timestamp = new Timestamp(Long.parseLong(document.get(ACTIVITY_TIMESTAMP).toString()));
                                Log.d("MyApp", "Date: " + new Date(timestamp.getTime()));
                                int time = Integer.parseInt(document.get(ACTIVITY_TIME).toString());
                                double avgHR = Double.valueOf(document.get(ACTIVITY_AVG_HR).toString());
                                activityData.setType(type);
                                activityData.setTimestamp(timestamp);
                                activityData.setTime(time);
                                activityData.setAvgHR(avgHR);
                                if (type.equals("sleep")) {
                                    int deepSleepTime = Integer.parseInt(document.get(ACTIVITY_DEEP_SLEEP_TIME).toString());
                                    int nightMoves = Integer.parseInt(document.get(ACTIVITY_NIGHT_MOVES).toString());
                                    activityData.setDeepSleepTime(deepSleepTime);
                                    activityData.setNightMoves(nightMoves);
                                    sleepActivityDataArrayList.add(activityData);
                                } else {
                                    int distance = Integer.parseInt(document.get(ACTIVITY_DISTANCE).toString());
                                    double avgSpeed = Double.parseDouble(document.get(ACTIVITY_AVG_SPEED).toString());
                                    List<LatLng> locationPoints;
                                    try {
                                        locationPoints = new ArrayList<>((Collection<? extends LatLng>) document.get(ACTIVITY_LOCATION_POINTS)); // Try if works
                                    } catch (NullPointerException e) {
                                        locationPoints = null;
                                    }
                                    int interval = Integer.parseInt(document.get(ACTIVITY_INTERVAL).toString());


                                    activityData.setDistance(distance);
                                    activityData.setAvgSpeed(avgSpeed);
                                    activityData.setLocationPoints(locationPoints);
                                    activityData.setInterval(interval);
                                    runActivityDataArrayList.add(activityData);
                                }
                                Collections.sort(allActivityDataArrayList, Collections.reverseOrder());
                                Collections.sort(runActivityDataArrayList, Collections.reverseOrder());
                                Collections.sort(sleepActivityDataArrayList, Collections.reverseOrder());
                                allActivityDataArrayList.add(activityData);
                                activityDataAdapter.notifyDataSetChanged();

                                Log.d("MyApp", document.getId() + " => " + document.getData());
                            }
                        }
                    }
                });
    }

    private void showRunPopup(ActivityData activity) {
        if (isRunDialogFirstTime) {
            runDialog.setContentView(R.layout.run_history_popup_layout);
            isRunDialogFirstTime = false;
        }
        TextView typeText = runDialog.findViewById(R.id.typeText);
        TextView timestampText = runDialog.findViewById(R.id.timestampText);
        TextView timeText = runDialog.findViewById(R.id.timeText);
        TextView intervalsText = runDialog.findViewById(R.id.intervalsText);
        TextView distanceText = runDialog.findViewById(R.id.distanceText);
        TextView avgSpeedText = runDialog.findViewById(R.id.avgSpeedText);
        TextView avgHRText = runDialog.findViewById(R.id.avgHRText);
        List<LatLng> locationData = activity.getLocationPoints();
        final List<LatLng> locationPoints = new ArrayList<>();

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (locationData != null) {
            for (Object locationObj : locationData) {
                Map<String, Object> location = (Map<String, Object>) locationObj;
                LatLng latLng = new LatLng((Double) location.get("latitude"), (Double) location.get("longitude"));
                Log.d("MyApp", "Lat: " + latLng.latitude + ", Lon: " + latLng.longitude);
                locationPoints.add(latLng);
            }
        } else {
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
                            Toast.makeText(getApplicationContext(), "No GPS data to show", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (SecurityException e) {
                Log.e("Exception: %s", e.getMessage());
            }
        }

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;

                PolylineOptions polylineOptions = new PolylineOptions();
                for (int i = 0; i < locationPoints.size(); i++) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(locationPoints.get(0).latitude,
                                    locationPoints.get(0).longitude), 15));
                    polylineOptions.add(new LatLng(locationPoints.get(i).latitude, locationPoints.get(i).longitude));
                }
                polylineOptions.color(Color.CYAN).width(4);
                map.addPolyline(polylineOptions);
            }
        });

        Date date = new Date(activity.getTimestamp().getTime());

        typeText.setText(activity.getType().toUpperCase());
        timestampText.setText(sdf.format(date));
        timeText.setText(activity.getTime() + " min");
        intervalsText.setText(String.valueOf(activity.getInterval()));
        distanceText.setText(activity.getDistance() + " m");
        avgSpeedText.setText(activity.getAvgSpeed() + " km/h");
        avgHRText.setText(activity.getAvgHR() + " bpm");


        runDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        runDialog.show();
    }

    private void showSleepPopup(ActivityData activity) {
        if (isSleepDialogFirstTime) {
            sleepDialog.setContentView(R.layout.sleep_history_popup_layout);
            isSleepDialogFirstTime = false;
        }
        TextView typeText = sleepDialog.findViewById(R.id.typeText);
        TextView timestampText = sleepDialog.findViewById(R.id.timestampText);
        TextView timeText = sleepDialog.findViewById(R.id.timeText);
        TextView deepSleepText = sleepDialog.findViewById(R.id.deepSleepText);
        TextView nightMoves = sleepDialog.findViewById(R.id.nightMovesText);
        TextView avgHRText = sleepDialog.findViewById(R.id.avgHRText);

        Date date = new Date(activity.getTimestamp().getTime());

        typeText.setText(activity.getType().toUpperCase());
        timestampText.setText(sdf.format(date));
        timeText.setText(activity.getTime() + " min");
        deepSleepText.setText(activity.getDeepSleepTime() + " min");
        nightMoves.setText(activity.getNightMoves() + " moves");
        avgHRText.setText(activity.getAvgHR() + " bpm");

        sleepDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        sleepDialog.show();
    }

    private void setListViewAdapter(int option) {
        switch (option) {
            case 0:
                activityDataAdapter = new ActivityDataAdapter(getApplicationContext(), allActivityDataArrayList);
                listView.setAdapter(activityDataAdapter);
                break;
            case 1:
                activityDataAdapter = new ActivityDataAdapter(getApplicationContext(), runActivityDataArrayList);
                listView.setAdapter(activityDataAdapter);
                break;
            case 2:
                activityDataAdapter = new ActivityDataAdapter(getApplicationContext(), sleepActivityDataArrayList);
                listView.setAdapter(activityDataAdapter);
                break;
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
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}















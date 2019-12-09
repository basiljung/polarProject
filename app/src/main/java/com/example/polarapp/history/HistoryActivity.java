package com.example.polarapp.history;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.polarapp.R;
import com.example.polarapp.activity.*;
import com.example.polarapp.preferencesmanager.ProfilePreferencesManager;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.*;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.*;

import java.sql.Timestamp;
import java.util.*;

public class HistoryActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ProfilePreferencesManager profilePreferencesManager;
    private ArrayList<ActivityData> allActivityDataArrayList = new ArrayList<>();
    private ArrayList<ActivityData> runActivityDataArrayList = new ArrayList<>();
    private ArrayList<ActivityData> sleepActivityDataArrayList = new ArrayList<>();
    private ListView listView = null;
    private TabLayout tabLayout;
    private ActivityDataAdapter activityDataAdapter;
    private FirebaseFirestore db;

    private static final String PROFILE_USER_ID = "profile_user_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        allActivityDataArrayList = new ArrayList<>();
        runActivityDataArrayList = new ArrayList<>();
        sleepActivityDataArrayList = new ArrayList<>();

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

                                String type = document.get("type").toString();
                                Timestamp timestamp = new Timestamp(Long.parseLong(document.get("timestamp").toString()));
                                Log.d("MyApp", "Date: " + new Date(timestamp.getTime()));
                                int time = Integer.parseInt(document.get("time").toString());
                                //double avgHR = Double.valueOf(document.get("avgHR").toString());
                                double avgHR = 0;
                                activityData.setType(type);
                                activityData.setTimestamp(timestamp);
                                activityData.setTime(time);
                                activityData.setAvgHR(avgHR);
                                if (type.equals("sleep")) {
                                    int deepSleepTime = Integer.parseInt(document.get("deepSleepTime").toString());
                                    int nightMoves = Integer.parseInt(document.get("nightMoves").toString());
                                    activityData.setDeepSleepTime(deepSleepTime);
                                    activityData.setNightMoves(nightMoves);
                                    sleepActivityDataArrayList.add(activityData);
                                } else {
                                    double distance = Double.parseDouble(document.get("distance").toString());
                                    double avgSpeed = Double.parseDouble(document.get("avgSpeed").toString());
                                    List<LatLng> locationPoints;
                                    try {
                                        locationPoints = new ArrayList<>((Collection<? extends LatLng>) document.get("locationPoints")); // Try if works
                                    } catch (NullPointerException e) {
                                        locationPoints = null;
                                    }

                                    activityData.setDistance(distance);
                                    activityData.setAvgSpeed(avgSpeed);
                                    activityData.setLocationPoints(locationPoints);
                                    runActivityDataArrayList.add(activityData);
                                }
                                Collections.sort(allActivityDataArrayList);
                                Collections.sort(runActivityDataArrayList);
                                Collections.sort(sleepActivityDataArrayList);
                                allActivityDataArrayList.add(activityData);
                                activityDataAdapter.notifyDataSetChanged();

                                Log.d("MyApp", document.getId() + " => " + document.getData());
                            }
                        }
                    }
                });
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
}















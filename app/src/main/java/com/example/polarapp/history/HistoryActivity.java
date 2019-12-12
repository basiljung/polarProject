package com.example.polarapp.history;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.polarapp.R;
import com.example.polarapp.activity.ActivityData;
import com.example.polarapp.activity.ActivityDataAdapter;
import com.example.polarapp.preferencesmanager.ProfilePreferencesManager;
import com.google.android.gms.maps.model.LatLng;
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

    private Dialog runDialog;
    private Dialog sleepDialog;
    private static final String PROFILE_USER_ID = "profile_user_id";
    private boolean isRunDialogFirstTime = true;
    private boolean isSleepDialogFirstTime = true;
    private SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd/MM/yyyy", Locale.ENGLISH);

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
                            showSleepPopup(view, selectedActivity);
                        } else {
                            showRunPopup(view, selectedActivity);
                        }
                        Toast.makeText(getApplicationContext(), "Selected item time: " + selectedActivity.getTime(), Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        selectedActivity = new ActivityData(runActivityDataArrayList.get(pos));
                        showRunPopup(view, selectedActivity);
                        Toast.makeText(getApplicationContext(), "Selected item time: " + selectedActivity.getTime(), Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        selectedActivity = new ActivityData(sleepActivityDataArrayList.get(pos));
                        showSleepPopup(view, selectedActivity);
                        Toast.makeText(getApplicationContext(), "Selected item time: " + selectedActivity.getTime(), Toast.LENGTH_SHORT).show();
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

                                String type = document.get("type").toString();
                                Timestamp timestamp = new Timestamp(Long.parseLong(document.get("timestamp").toString()));
                                Log.d("MyApp", "Date: " + new Date(timestamp.getTime()));
                                int time = Integer.parseInt(document.get("time").toString());
                                double avgHR = Double.valueOf(document.get("avgHR").toString());
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
                                    int distance = Integer.parseInt(document.get("distance").toString());
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

    private void showRunPopup(View v, ActivityData activity) {
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

        Date date = new Date(activity.getTimestamp().getTime());

        typeText.setText(activity.getType());
        timestampText.setText(sdf.format(date));
        timeText.setText(activity.getTime() + " min");
        //intervalsText.setText(activity.getInterval());
        distanceText.setText(activity.getDistance() + " m");
        avgSpeedText.setText(activity.getAvgSpeed() + " km/h");
        avgHRText.setText(activity.getAvgHR() + " bpm");

        runDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        runDialog.show();
    }

    private void showSleepPopup(View v, ActivityData activity) {
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

        typeText.setText(activity.getType());
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
}















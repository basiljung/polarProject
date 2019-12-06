package com.example.polarapp.history;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.polarapp.R;
import com.example.polarapp.activity.ActivityData;
import com.example.polarapp.activity.ActivityDataAdapter;
import com.example.polarapp.preferencesmanager.ProfilePreferencesManager;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ProfilePreferencesManager profilePreferencesManager;
    ArrayList<ActivityData> activityDataArrayList = new ArrayList<>();
    ListView listView = null;

    private static final String PROFILE_USER_ID = "profile_user_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        listView = findViewById(R.id.historyList);
        final ActivityDataAdapter activityDataAdapter = new ActivityDataAdapter(this, activityDataArrayList);
        listView.setAdapter(activityDataAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        profilePreferencesManager = new ProfilePreferencesManager(getApplication().getBaseContext());

        Log.d("jolo", "Data UUID : " + profilePreferencesManager.getStringProfileValue(PROFILE_USER_ID));

        final Query activity = db.collection("activities").whereEqualTo("UUID", profilePreferencesManager.getStringProfileValue(PROFILE_USER_ID));

        activity
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("jolo", document.getId() + " => " + document.getData());

                                ActivityData activityData = new ActivityData();

                                String type = document.get("type").toString();
                                long timestamp = Long.parseLong(document.get("timestamp").toString());
                                int time = Integer.parseInt(document.get("time").toString());
                                activityData.setType(type);
                                activityData.setTimestamp(timestamp);
                                activityData.setTime(time);
                                if (type.equals("sleep")) {
                                    int deepSleepTime = Integer.parseInt(document.get("deepSleepTime").toString());
                                    int nightMoves = Integer.parseInt(document.get("nightMoves").toString());
                                    activityData.setDeepSleepTime(deepSleepTime);
                                    activityData.setNightMoves(nightMoves);
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
                                }
                                activityDataArrayList.add(activityData);
                                activityDataAdapter.notifyDataSetChanged();

                                Log.d("jolo", document.getId() + " => " + document.getData());
                            }
                        }
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
}















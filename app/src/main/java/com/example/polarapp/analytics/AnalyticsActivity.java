package com.example.polarapp.analytics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.example.polarapp.R;
import com.example.polarapp.activity.ActivityData;
import com.example.polarapp.devices.SearchDevicesFragment;
import com.example.polarapp.preferencesmanager.ProfilePreferencesManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.*;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.*;
import com.google.firebase.firestore.*;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings("ALL")
public class AnalyticsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ProfilePreferencesManager profilePreferencesManager;
    private ArrayList<ActivityData> activityDataArrayList = new ArrayList<>();
    private ArrayList<ActivityData> runActivityArrayList = new ArrayList<>();
    private ArrayList<ActivityData> sleepActivityArrayList = new ArrayList<>();
    private BarChart barChart;
    private SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd/MM/yyyy", Locale.ENGLISH);
    private ArrayList<String> xLabelDates = new ArrayList<>();
    private Spinner weekSpinner;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final String PROFILE_USER_ID = "profile_user_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        barChart = findViewById(R.id.barchart);

        profilePreferencesManager = new ProfilePreferencesManager(getApplication().getBaseContext());

        weekSpinner = findViewById(R.id.weekSpinner);

        final List<String> spinnerList = new ArrayList<>();
        for (int i = 1; i < 54; i++) {
            spinnerList.add("Week " + i);
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weekSpinner.setAdapter(dataAdapter);

        weekSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                generateChart(pos + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        weekSpinner.post(new Runnable() {
            @Override
            public void run() {
                Calendar calendar = Calendar.getInstance();
                weekSpinner.setSelection(calendar.get(Calendar.WEEK_OF_YEAR) - 1);
            }
        });


        GetActivitiesAsync getActivitiesAsync = new GetActivitiesAsync();
        getActivitiesAsync.execute();
    }

    private void generateChart(int weekOfYear) {
        xLabelDates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.WEEK_OF_YEAR, weekOfYear);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        xLabelDates.add(sdf.format(calendar.getTime()));
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        xLabelDates.add(sdf.format(calendar.getTime()));
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        xLabelDates.add(sdf.format(calendar.getTime()));
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        xLabelDates.add(sdf.format(calendar.getTime()));
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        xLabelDates.add(sdf.format(calendar.getTime()));
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        xLabelDates.add(sdf.format(calendar.getTime()));
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        xLabelDates.add(sdf.format(calendar.getTime()));

        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setMaxVisibleValueCount(7);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(true);
        barChart.setScaleXEnabled(false);
        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);

        ArrayList<ActivityData> activities1 = getWeekActivities(weekOfYear, 0);
        ArrayList<ActivityData> activities2 = getWeekActivities(weekOfYear, 1);

        ArrayList<BarEntry> runEntries = new ArrayList<>();
        runEntries.add(new BarEntry(0, (float) activities1.get(0).getTime()));
        runEntries.add(new BarEntry(1, (float) activities1.get(1).getTime()));
        runEntries.add(new BarEntry(2, (float) activities1.get(2).getTime()));
        runEntries.add(new BarEntry(3, (float) activities1.get(3).getTime()));
        runEntries.add(new BarEntry(4, (float) activities1.get(4).getTime()));
        runEntries.add(new BarEntry(5, (float) activities1.get(5).getTime()));
        runEntries.add(new BarEntry(6, (float) activities1.get(6).getTime()));

        ArrayList<BarEntry> sleepEntries = new ArrayList<>();
        sleepEntries.add(new BarEntry(0, (float) activities2.get(0).getTime()));
        sleepEntries.add(new BarEntry(1, (float) activities2.get(1).getTime()));
        sleepEntries.add(new BarEntry(2, (float) activities2.get(2).getTime()));
        sleepEntries.add(new BarEntry(3, (float) activities2.get(3).getTime()));
        sleepEntries.add(new BarEntry(4, (float) activities2.get(4).getTime()));
        sleepEntries.add(new BarEntry(5, (float) activities2.get(5).getTime()));
        sleepEntries.add(new BarEntry(6, (float) activities2.get(6).getTime()));

        BarDataSet barDataSet1 = new BarDataSet(runEntries, "Run Times (in mins.)");
        barDataSet1.setColor(Color.RED);

        BarDataSet barDataSet2 = new BarDataSet(sleepEntries, "Sleep Times (in mins.)");
        barDataSet2.setColor(Color.GREEN);

        BarData data = new BarData(barDataSet1, barDataSet2);
        barChart.setData(data);

        float groupSpace = 0.1f;
        float barSpace = 0.02f;
        float barWidth = 0.43f;

        data.setBarWidth(barWidth);
        barChart.groupBars(0f, groupSpace, barSpace);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setLabelRotationAngle(-60);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(barChart.getMaxVisibleCount());
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabelDates));

        YAxis leftAxis = barChart.getAxisLeft();
        barChart.getAxisRight().setEnabled(false);
        leftAxis.setDrawZeroLine(true);
        leftAxis.setAxisMinimum(0f);

        barChart.setExtraOffsets(8f, 6f, 4f, 8f);
        barChart.notifyDataSetChanged();
        barChart.invalidate();
    }

    private void getDatabaseData(QueryDocumentSnapshot document) {
        Log.d("jolo", document.getId() + " => " + document.getData());
        Calendar calendar = Calendar.getInstance();

        ActivityData activityData = new ActivityData();

        String type = document.get("type").toString();
        Timestamp timestamp = new Timestamp(Long.parseLong(document.get("timestamp").toString()));
        long timestampLong = timestamp.getTime();
        calendar.setTimeInMillis(timestampLong);
        int week = calendar.get(Calendar.WEEK_OF_YEAR);
        Log.d("MyApp", "Week of year of the data is : " + week);
        Log.d("MyApp", "Date: " + new Date(timestamp.getTime()));
        int time = Integer.parseInt(document.get("time").toString());
        activityData.setType(type);
        activityData.setTimestamp(timestamp);
        activityData.setTime(time);
        if (type.equals("sleep")) {
            int deepSleepTime = Integer.parseInt(document.get("deepSleepTime").toString());
            int nightMoves = Integer.parseInt(document.get("nightMoves").toString());
            activityData.setDeepSleepTime(deepSleepTime);
            activityData.setNightMoves(nightMoves);
            sleepActivityArrayList.add(activityData);
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
            runActivityArrayList.add(activityData);
        }

        Collections.sort(runActivityArrayList);
        Collections.sort(sleepActivityArrayList);

        activityDataArrayList.add(activityData);

        Log.d("jolo", document.getId() + " => " + document.getData());
    }

    private ArrayList<ActivityData> getWeekActivities(int weekOfYear, int option) {
        ArrayList<ActivityData> data = new ArrayList<>();
        if (option == 0) { // Run activities
            Calendar calendar = Calendar.getInstance();
            for (int i = 0; i < runActivityArrayList.size(); i++) {
                ActivityData actualActivity = runActivityArrayList.get(i);
                calendar.setTimeInMillis(actualActivity.getTimestamp().getTime());
                Log.d("MyApp", "Week of year: " + calendar.get(Calendar.WEEK_OF_YEAR));
                if (calendar.get(Calendar.WEEK_OF_YEAR) == weekOfYear) {
                    Log.d("MyApp", "Week of year accepted: " + calendar.get(Calendar.WEEK_OF_YEAR));
                    data.add(actualActivity);
                }
            }
        } else { // Sleep activities
            Calendar calendar = Calendar.getInstance();
            for (int i = 0; i < sleepActivityArrayList.size(); i++) {
                ActivityData actualActivity = sleepActivityArrayList.get(i);
                calendar.setTimeInMillis(actualActivity.getTimestamp().getTime());
                if (calendar.get(Calendar.WEEK_OF_YEAR) == weekOfYear) {
                    data.add(actualActivity);
                }
            }
        }
        Collections.sort(data);
        data = sortByDay(data);
        return data;
    }

    private ArrayList<ActivityData> sortByDay(ArrayList<ActivityData> data) {
        Log.d("MyAppTrial", "Size of activities: " + activityDataArrayList.size());
        ArrayList<ActivityData> sortedData = new ArrayList<>();
        ActivityData aux = new ActivityData();
        for (int i = 0; i < 7; i++) {
            sortedData.add(aux);
        }
        for (int i = 0; i < data.size(); i++) {
            Timestamp timestamp = data.get(i).getTimestamp();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp.getTime());
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            if (dayOfWeek == 0)
                dayOfWeek = 7;
            sortedData.set(dayOfWeek - 1, data.get(i));
        }
        return sortedData;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    class GetActivitiesAsync extends AsyncTask<Void, Void, Void> {
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("MyApp", "Starting AsyncTask");
        }

        protected Void doInBackground(Void... args) {
            Log.d("jolo", "Data UUID : " + profilePreferencesManager.getStringProfileValue(PROFILE_USER_ID));
            final Query activity = db.collection("activities").whereEqualTo("UUID", profilePreferencesManager.getStringProfileValue(PROFILE_USER_ID));

            activity
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d("MyAPpp", "Here");

                                    Log.d("jolo", document.getId() + " => " + document.getData());
                                    Calendar calendar = Calendar.getInstance();

                                    ActivityData activityData = new ActivityData();

                                    String type = document.get("type").toString();
                                    Timestamp timestamp = new Timestamp(Long.parseLong(document.get("timestamp").toString()));
                                    long timestampLong = timestamp.getTime();
                                    calendar.setTimeInMillis(timestampLong);
                                    int week = calendar.get(Calendar.WEEK_OF_YEAR);
                                    Log.d("MyApp", "Week of year of the data is : " + week);
                                    Log.d("MyApp", "Date: " + new Date(timestamp.getTime()));
                                    int time = Integer.parseInt(document.get("time").toString());
                                    activityData.setType(type);
                                    activityData.setTimestamp(timestamp);
                                    activityData.setTime(time);
                                    if (type.equals("sleep")) {
                                        int deepSleepTime = Integer.parseInt(document.get("deepSleepTime").toString());
                                        int nightMoves = Integer.parseInt(document.get("nightMoves").toString());
                                        activityData.setDeepSleepTime(deepSleepTime);
                                        activityData.setNightMoves(nightMoves);
                                        sleepActivityArrayList.add(activityData);
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
                                        runActivityArrayList.add(activityData);
                                    }

                                    Collections.sort(runActivityArrayList);
                                    Collections.sort(sleepActivityArrayList);

                                    activityDataArrayList.add(activityData);

                                    Log.d("jolo", document.getId() + " => " + document.getData());
                                }
                                String l = weekSpinner.getSelectedItem().toString();
                                Log.d("MyApp", "Selected item onPostExecute: " + l);
                                String[] parts = l.split(" ");
                                int week = Integer.parseInt(parts[1]);
                                barChart.setVisibility(View.VISIBLE);
                                generateChart(week);
                            }
                        }
                    });
            return null;
        }

        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            Log.d("MyApp", "AsyncTask finished");
        }
    }
}

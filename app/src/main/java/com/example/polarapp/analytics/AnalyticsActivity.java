package com.example.polarapp.analytics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.graphics.Color;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.example.polarapp.R;
import com.example.polarapp.activity.ActivityData;
import com.example.polarapp.preferencesmanager.ProfilePreferencesManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.*;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.*;
import com.google.firebase.firestore.*;

import java.sql.Timestamp;
import java.text.*;
import java.util.*;

@SuppressWarnings("ALL")
public class AnalyticsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private BarChart barChart;
    private Spinner weekSpinner;
    private TextView avgRunText, avgSleepText, maxRunText, maxSleepText, compareWeekRunText, compareWeekSleepText;

    private ProfilePreferencesManager profilePreferencesManager;
    private static final String PROFILE_USER_ID = "profile_user_id";
    private SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd/MM/yyyy", Locale.ENGLISH);
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<String> xLabelDates = new ArrayList<>();

    private ArrayList<ActivityData> allUserActivitiesDataArrayList = new ArrayList<>();
    private ArrayList<ActivityData> allUserRunActivitiesArrayList = new ArrayList<>();
    private ArrayList<ActivityData> allUserSleepActivitiesArrayList = new ArrayList<>();
    private ArrayList<ActivityData> userRunCurrentWeekActivitiesList = new ArrayList<>();
    private ArrayList<ActivityData> userSleepCurrentWeekActivitiesList = new ArrayList<>();
    private ArrayList<ActivityData> userRunCurrentWeekBeforeActivitiesList = new ArrayList<>();
    private ArrayList<ActivityData> userSleepCurrentWeekBeforeActivitiesList = new ArrayList<>();
    private ArrayList<ActivityData> userRunCurrentMonthActivitiesList = new ArrayList<>();
    private ArrayList<ActivityData> userSleepCurrentMonthActivitiesList = new ArrayList<>();

    private final List<String> daysOfWeekList = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thurday", "Friday", "Saturday", "Sunday");

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

        avgRunText = findViewById(R.id.avgRunText);
        avgSleepText = findViewById(R.id.avgSleepText);
        maxRunText = findViewById(R.id.maxRunText);
        maxSleepText = findViewById(R.id.maxSleepText);
        compareWeekRunText = findViewById(R.id.compareWeekRunText);
        compareWeekSleepText = findViewById(R.id.compareWeekSleepText);

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
                barChart.highlightValue(null);
                generateChart(pos + 1);
                calculateWeeklyInfo();
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

    public class CustomMarkerView extends MarkerView implements IMarker {

        private TextView tvTime, tvDistDeepSleep, tvSpeedNightMoves, tvAvgHR;

        public CustomMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            tvTime = (TextView) findViewById(R.id.tvTime);
            tvDistDeepSleep = (TextView) findViewById(R.id.tvDistDeepSleep);
            tvSpeedNightMoves = (TextView) findViewById(R.id.tvSpeedNightMoves);
            tvAvgHR = (TextView) findViewById(R.id.tvAvgHR);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            e.getData();
            ActivityData activityData = new ActivityData();
            if (highlight.getDataSetIndex() == 0) {
                activityData = userRunCurrentWeekActivitiesList.get((int) e.getX());
                tvTime.setText("Total time: " + activityData.getTime());
                tvDistDeepSleep.setText("Total distance: " + activityData.getDistance());
                tvSpeedNightMoves.setText("Avg speed: " + activityData.getAvgSpeed());
                tvAvgHR.setText("Avg Heart Rate: " + activityData.getAvgHR());
            } else if (highlight.getDataSetIndex() == 1) {
                activityData = userSleepCurrentWeekActivitiesList.get((int) e.getX());
                tvTime.setText("Total time: " + activityData.getTime());
                tvDistDeepSleep.setText("Total deep sleep time: " + activityData.getDeepSleepTime());
                tvSpeedNightMoves.setText("Total night moves: " + activityData.getNightMoves());
                tvAvgHR.setText("Avg Heart Rate: " + activityData.getAvgHR());
            }
            // Check how to show Picker ONLY when there is data for that day and kind of activity
            super.refreshContent(e, highlight);
        }

        private MPPointF mOffset;

        @Override
        public MPPointF getOffset() {
            if (mOffset == null) {
                mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
            }

            return mOffset;
        }
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
        barChart.setHighlightPerTapEnabled(true);
        barChart.setHighlightPerDragEnabled(true);
        barChart.setDrawValueAboveBar(true);
        barChart.setMaxVisibleValueCount(7);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(true);
        barChart.setScaleXEnabled(false);
        CustomMarkerView mv = new CustomMarkerView(getApplicationContext(), R.layout.marker_layout);
        mv.setChartView(barChart);
        barChart.setMarkerView(mv);
        barChart.setDrawMarkers(true);
        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);

        userRunCurrentWeekActivitiesList = getWeekActivities(weekOfYear, 0);
        userSleepCurrentWeekActivitiesList = getWeekActivities(weekOfYear, 1);

        if (weekOfYear > 1) {
            userRunCurrentWeekBeforeActivitiesList = getWeekActivities(weekOfYear - 1, 0);
            userSleepCurrentWeekBeforeActivitiesList = getWeekActivities(weekOfYear - 1, 1);
        }

        ArrayList<BarEntry> runEntries = new ArrayList<>();
        runEntries.add(new BarEntry(0, (float) userRunCurrentWeekActivitiesList.get(0).getTime()));
        runEntries.add(new BarEntry(1, (float) userRunCurrentWeekActivitiesList.get(1).getTime()));
        runEntries.add(new BarEntry(2, (float) userRunCurrentWeekActivitiesList.get(2).getTime()));
        runEntries.add(new BarEntry(3, (float) userRunCurrentWeekActivitiesList.get(3).getTime()));
        runEntries.add(new BarEntry(4, (float) userRunCurrentWeekActivitiesList.get(4).getTime()));
        runEntries.add(new BarEntry(5, (float) userRunCurrentWeekActivitiesList.get(5).getTime()));
        runEntries.add(new BarEntry(6, (float) userRunCurrentWeekActivitiesList.get(6).getTime()));

        ArrayList<BarEntry> sleepEntries = new ArrayList<>();
        sleepEntries.add(new BarEntry(0, (float) userSleepCurrentWeekActivitiesList.get(0).getTime()));
        sleepEntries.add(new BarEntry(1, (float) userSleepCurrentWeekActivitiesList.get(1).getTime()));
        sleepEntries.add(new BarEntry(2, (float) userSleepCurrentWeekActivitiesList.get(2).getTime()));
        sleepEntries.add(new BarEntry(3, (float) userSleepCurrentWeekActivitiesList.get(3).getTime()));
        sleepEntries.add(new BarEntry(4, (float) userSleepCurrentWeekActivitiesList.get(4).getTime()));
        sleepEntries.add(new BarEntry(5, (float) userSleepCurrentWeekActivitiesList.get(5).getTime()));
        sleepEntries.add(new BarEntry(6, (float) userSleepCurrentWeekActivitiesList.get(6).getTime()));

        BarDataSet barDataSet1 = new BarDataSet(runEntries, "Running Time (in mins)");
        barDataSet1.setColor(Color.RED);

        BarDataSet barDataSet2 = new BarDataSet(sleepEntries, "Sleeping Time (in mins)");
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
        Log.d("MyApp", document.getId() + " => " + document.getData());
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
            allUserSleepActivitiesArrayList.add(activityData);
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
            allUserRunActivitiesArrayList.add(activityData);
        }

        Collections.sort(allUserRunActivitiesArrayList);
        Collections.sort(allUserSleepActivitiesArrayList);

        allUserActivitiesDataArrayList.add(activityData);

        Log.d("jolo", document.getId() + " => " + document.getData());
    }

    private ArrayList<ActivityData> getWeekActivities(int weekOfYear, int option) {
        ArrayList<ActivityData> data = new ArrayList<>();
        if (option == 0) { // Run activities
            Calendar calendar = Calendar.getInstance();
            for (int i = 0; i < allUserRunActivitiesArrayList.size(); i++) {
                ActivityData actualActivity = allUserRunActivitiesArrayList.get(i);
                calendar.setTimeInMillis(actualActivity.getTimestamp().getTime());
                Log.d("MyApp", "Week of year: " + calendar.get(Calendar.WEEK_OF_YEAR));
                if (calendar.get(Calendar.WEEK_OF_YEAR) == weekOfYear) {
                    Log.d("MyApp", "Week of year accepted: " + calendar.get(Calendar.WEEK_OF_YEAR));
                    data.add(actualActivity);
                }
            }
        } else { // Sleep activities
            Calendar calendar = Calendar.getInstance();
            for (int i = 0; i < allUserSleepActivitiesArrayList.size(); i++) {
                ActivityData actualActivity = allUserSleepActivitiesArrayList.get(i);
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
        Log.d("MyAppTrial", "Size of activities: " + allUserActivitiesDataArrayList.size());
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

    private void calculateWeeklyInfo() {
        // Average Time Running and Sleeping in the week (single data per day)
        int totalRunTime = 0;
        int totalSleepTime = 0;
        int runDays = 0;
        int sleepDays = 0;
        for (int i = 0; i < 7; i++) {
            int runTime = userRunCurrentWeekActivitiesList.get(i).getTime();
            if (runTime > 0) {
                runDays++;
                totalRunTime += runTime;
            }
            int sleepTime = userSleepCurrentWeekActivitiesList.get(i).getTime();
            if (sleepTime > 0) {
                sleepDays++;
                totalSleepTime += sleepTime;
            }
        }

        if (runDays == 0) {
            Log.d("MyApp", "No data available for this week");
            avgRunText.setText("No data available for this week");
        } else {
            Log.d("MyApp", "Running average during the week: " + (float) totalRunTime / runDays + " minutes");
            avgRunText.setText("Running average during the week: " + (float) totalRunTime / runDays + " minutes");
        }
        if (sleepDays == 0) {
            Log.d("MyApp", "No data available for this week");
            avgSleepText.setText("No data available for this week");
        } else {
            Log.d("MyApp", "Sleeping average during the week: " + (float) totalSleepTime / sleepDays + " minutes");
            avgSleepText.setText("Sleeping average during the week: " + (float) totalSleepTime / sleepDays + " minutes");
        }

        // Max time running and sleeping in the week
        int runDayMax = -1;
        int runMaxTime = 0;
        int sleepDayMax = -1;
        int sleepMaxTime = 0;
        for (int i = 0; i < 7; i++) {
            int runTime = userRunCurrentWeekActivitiesList.get(i).getTime();
            if (runTime > 0) {
                if (runTime > runMaxTime) {
                    runMaxTime = runTime;
                    runDayMax = i;
                }
            }
            int sleepTime = userSleepCurrentWeekActivitiesList.get(i).getTime();
            if (sleepTime > 0) {
                if (sleepTime > sleepMaxTime) {
                    sleepMaxTime = sleepTime;
                    sleepDayMax = i;
                }
            }
        }

        if (runDayMax != -1) {
            Log.d("MyApp", "Max run time of: " + runMaxTime + " minutes on " + daysOfWeekList.get(runDayMax));
            maxRunText.setText("Max run time of: " + runMaxTime + " minutes on " + daysOfWeekList.get(runDayMax));
        } else {
            Log.d("MyApp", "No data available in this week");
            maxRunText.setText("No data available in this week");
        }
        if (sleepDayMax != -1) {
            Log.d("MyApp", "Max sleep time of: " + sleepMaxTime + " minutes on " + daysOfWeekList.get(sleepDayMax));
            maxSleepText.setText("Max sleep time of: " + sleepMaxTime + " minutes on " + daysOfWeekList.get(sleepDayMax));
        } else {
            Log.d("MyApp", "No data available in this week");
            maxSleepText.setText("No data available in this week");
        }

        // Compare some data with the week before
        int totalRunTimeWB = 0;
        int totalSleepTimeWB = 0;
        int runDaysWB = 0;
        int sleepDaysWB = 0;
        for (int i = 0; i < 7; i++) {
            int runTimeWB = userRunCurrentWeekBeforeActivitiesList.get(i).getTime();
            if (runTimeWB > 0) {
                runDaysWB++;
                totalRunTimeWB += runTimeWB;
            }
            int sleepTimeWB = userSleepCurrentWeekBeforeActivitiesList.get(i).getTime();
            if (sleepTimeWB > 0) {
                sleepDaysWB++;
                totalSleepTimeWB += sleepTimeWB;
            }
        }

        DecimalFormat df = new DecimalFormat("#.##");

        if (runDaysWB == 0 || runDays == 0) {
            Log.d("MyApp", "No data available for this week or week before");
            compareWeekRunText.setText("No data available for this week or week before");
        } else {

            float avgRunPercentage = (((float) totalRunTime / runDays) / ((float) totalRunTimeWB / runDaysWB)) * 100;
            String compareRunTxt = "";

            if (avgRunPercentage > 100) {
                avgRunPercentage -= 100;
                compareRunTxt = "You have run " + df.format(avgRunPercentage) + "% more than last week";
            } else if (avgRunPercentage < 100) {
                avgRunPercentage -= 100;
                compareRunTxt = "You have run " + df.format(Math.abs(avgRunPercentage)) + "% less than last week";
            } else { // == 100
                compareRunTxt = "You have run the same than last week";
            }

            Log.d("MyApp", compareRunTxt);
            compareWeekRunText.setText(compareRunTxt);
        }
        if (sleepDaysWB == 0 || sleepDays == 0) {
            Log.d("MyApp", "No data available for this week or week before");
            compareWeekSleepText.setText("No data available for this week or week before");
        } else {
            float avgSleepPercentage = (((float) totalSleepTime / sleepDays) / ((float) totalSleepTimeWB / sleepDaysWB)) * 100;
            String compareSleepTxt = "";
            if (avgSleepPercentage > 100) {
                avgSleepPercentage -= 100;
                compareSleepTxt = "You have slept " + df.format(avgSleepPercentage) + "% more than last week";
            } else if (avgSleepPercentage < 100) {
                avgSleepPercentage -= 100;
                compareSleepTxt = "You have slept " + df.format(Math.abs(avgSleepPercentage)) + "% less than last week";
            } else {
                compareSleepTxt = "You have slept the same than last week";
            }
            Log.d("MyApp", compareSleepTxt);
            compareWeekSleepText.setText(compareSleepTxt);
        }
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
                                    Log.d("MyApp", document.getId() + " => " + document.getData());
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
                                        allUserSleepActivitiesArrayList.add(activityData);
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
                                        allUserRunActivitiesArrayList.add(activityData);
                                    }

                                    Collections.sort(allUserRunActivitiesArrayList);
                                    Collections.sort(allUserSleepActivitiesArrayList);

                                    allUserActivitiesDataArrayList.add(activityData);

                                    Log.d("MyApp", document.getId() + " => " + document.getData());
                                }
                                String l = weekSpinner.getSelectedItem().toString();
                                Log.d("MyApp", "Selected item onPostExecute: " + l);
                                String[] parts = l.split(" ");
                                int week = Integer.parseInt(parts[1]);
                                barChart.setVisibility(View.VISIBLE);
                                generateChart(week);
                                calculateWeeklyInfo();
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

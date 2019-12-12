package com.example.polarapp.analytics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.*;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.*;

import java.sql.Timestamp;
import java.text.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AnalyticsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private BarChart barChart;
    private Spinner weekSpinner;
    private TextView avgRunThisWeekText, avgSleepThisWeekText,
            totalRunThisWeekText, totalSleepThisWeekText,
            maxRunThisWeekText, maxSleepThisWeekText, avgHRThisWeekText;
    private TextView avgRunTimeDifferenceLastWeekText, avgSleepTimeDifferenceLastWeekText,
            totalRunTimeDifferenceLastWeekText, totalSleepTimeDifferenceLastWeekText, avgHRDifferenceLastWeekText;
    private TextView avgRunTimeDifferenceMonthText, avgSleepTimeDifferenceMonthText,
            totalRunTimeDifferenceMonthText, totalSleepTimeDifferenceMonthText, avgHRDifferenceMonthText;
    private DecimalFormat df = new DecimalFormat();

    private TabLayout tabLayout;
    private LinearLayout linearLayoutThisWeek, linearLayoutLastWeek, linearLayoutMonth;


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
    private Runnable runnable;

    private ProfilePreferencesManager profilePreferencesManager;
    private static final String PROFILE_USER_ID = "profile_user_id";

    // HR Zones calculus values
    private static final String PROFILE_USER_BIRTH = "profile_user_birth";
    private final int maxHeartRateBeats = 220;
    //private final String[] hrLevelNames = {"Zone 1", "Zone 2", "Zone 3", "Zone 4", "Zone 5"};
    private int[] hrLevelMinValues = {0, 0, 0, 0, 0, 0};
    private int actualHRLevel = -1;
    private Dialog hrInfoDialog;
    private ImageButton infoImageButton;
    private LinearLayout level1InfoLayout, level2InfoLayout, level3InfoLayout, level4InfoLayout, level5InfoLayout;
    private TextView level1InfoText, level2InfoText, level3InfoText, level4InfoText, level5InfoText;
    private ImageButton level1ImageButton, level2ImageButton, level3ImageButton, level4ImageButton, level5ImageButton;

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

        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        df.setMaximumFractionDigits(2);
        df.setDecimalFormatSymbols(otherSymbols);

        profilePreferencesManager = new ProfilePreferencesManager(getApplication().getBaseContext());

        hrInfoDialog = new Dialog(this);
        infoImageButton = findViewById(R.id.infoImageButton);
        infoImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });

        getHRTrainingZones();

        barChart = findViewById(R.id.barchart);

        final ScrollView scrollView = findViewById(R.id.scrollView);

        runnable = new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        };

        linearLayoutThisWeek = findViewById(R.id.linearLayoutThisWeek);
        avgRunThisWeekText = findViewById(R.id.avgRunThisWeekText);
        avgSleepThisWeekText = findViewById(R.id.avgSleepThisWeekText);
        totalRunThisWeekText = findViewById(R.id.totalRunThisWeekText);
        totalSleepThisWeekText = findViewById(R.id.totalSleepThisWeekText);
        maxRunThisWeekText = findViewById(R.id.maxRunThisWeekText);
        maxSleepThisWeekText = findViewById(R.id.maxSleepThisWeekText);
        avgHRThisWeekText = findViewById(R.id.avgHRThisWeekText);

        linearLayoutLastWeek = findViewById(R.id.linearLayoutLastWeek);
        avgRunTimeDifferenceLastWeekText = findViewById(R.id.avgRunTimeDifferenceLastWeekText);
        avgSleepTimeDifferenceLastWeekText = findViewById(R.id.avgSleepTimeDifferenceLastWeekText);
        totalRunTimeDifferenceLastWeekText = findViewById(R.id.totalRunTimeDifferenceLastWeekText);
        totalSleepTimeDifferenceLastWeekText = findViewById(R.id.totalSleepTimeDifferenceLastWeekText);
        avgHRDifferenceLastWeekText = findViewById(R.id.avgHRDifferenceLastWeekText);

        linearLayoutMonth = findViewById(R.id.linearLayoutMonth);
        avgRunTimeDifferenceMonthText = findViewById(R.id.avgRunTimeDifferenceMonthText);
        avgSleepTimeDifferenceMonthText = findViewById(R.id.avgSleepTimeDifferenceMonthText);
        totalRunTimeDifferenceMonthText = findViewById(R.id.totalRunTimeDifferenceMonthText);
        totalSleepTimeDifferenceMonthText = findViewById(R.id.totalSleepTimeDifferenceMonthText);
        avgHRDifferenceMonthText = findViewById(R.id.avgHRDifferenceMonthText);

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
                calculateAnalytics();
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

        tabLayout = findViewById(R.id.tabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: // This week analytics
                        linearLayoutThisWeek.setVisibility(View.VISIBLE);
                        linearLayoutLastWeek.setVisibility(View.GONE);
                        linearLayoutMonth.setVisibility(View.GONE);
                        break;
                    case 1: // Last week analytics
                        linearLayoutThisWeek.setVisibility(View.GONE);
                        linearLayoutLastWeek.setVisibility(View.VISIBLE);
                        linearLayoutMonth.setVisibility(View.GONE);
                        break;
                    case 2: // This month analytics
                        linearLayoutThisWeek.setVisibility(View.GONE);
                        linearLayoutLastWeek.setVisibility(View.GONE);
                        linearLayoutMonth.setVisibility(View.VISIBLE);
                        break;
                }
                scrollView.post(runnable);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                scrollView.post(runnable);
            }
        });
    }

    public void showPopup(View v) {
        hrInfoDialog.setContentView(R.layout.hr_popup_layout);

        level1InfoLayout = hrInfoDialog.findViewById(R.id.level1InfoLayout);
        level2InfoLayout = hrInfoDialog.findViewById(R.id.level2InfoLayout);
        level3InfoLayout = hrInfoDialog.findViewById(R.id.level3InfoLayout);
        level4InfoLayout = hrInfoDialog.findViewById(R.id.level4InfoLayout);
        level5InfoLayout = hrInfoDialog.findViewById(R.id.level5InfoLayout);

        level1InfoText = hrInfoDialog.findViewById(R.id.level1InfoText);
        level2InfoText = hrInfoDialog.findViewById(R.id.level2InfoText);
        level3InfoText = hrInfoDialog.findViewById(R.id.level3InfoText);
        level4InfoText = hrInfoDialog.findViewById(R.id.level4InfoText);
        level5InfoText = hrInfoDialog.findViewById(R.id.level5InfoText);

        level1ImageButton = hrInfoDialog.findViewById(R.id.level1ImageButton);
        level2ImageButton = hrInfoDialog.findViewById(R.id.level2ImageButton);
        level3ImageButton = hrInfoDialog.findViewById(R.id.level3ImageButton);
        level4ImageButton = hrInfoDialog.findViewById(R.id.level4ImageButton);
        level5ImageButton = hrInfoDialog.findViewById(R.id.level5ImageButton);
        level1ImageButton.setVisibility(View.GONE);
        level2ImageButton.setVisibility(View.GONE);
        level3ImageButton.setVisibility(View.GONE);
        level4ImageButton.setVisibility(View.GONE);
        level5ImageButton.setVisibility(View.GONE);

        if (actualHRLevel == 1) {
            level1ImageButton.setVisibility(View.VISIBLE);
        } else if (actualHRLevel == 2) {
            level2ImageButton.setVisibility(View.VISIBLE);
        } else if (actualHRLevel == 3) {
            level3ImageButton.setVisibility(View.VISIBLE);
        } else if (actualHRLevel == 4) {
            level4ImageButton.setVisibility(View.VISIBLE);
        } else if (actualHRLevel == 5) {
            level5ImageButton.setVisibility(View.VISIBLE);
        }

        level1InfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (level1InfoText.getVisibility() == View.GONE) {
                    level1InfoText.setVisibility(View.VISIBLE);
                } else {
                    level1InfoText.setVisibility(View.GONE);
                }
                level2InfoText.setVisibility(View.GONE);
                level3InfoText.setVisibility(View.GONE);
                level4InfoText.setVisibility(View.GONE);
                level5InfoText.setVisibility(View.GONE);
            }
        });
        level2InfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                level1InfoText.setVisibility(View.GONE);
                if (level2InfoText.getVisibility() == View.GONE) {
                    level2InfoText.setVisibility(View.VISIBLE);
                } else {
                    level2InfoText.setVisibility(View.GONE);
                }
                level3InfoText.setVisibility(View.GONE);
                level4InfoText.setVisibility(View.GONE);
                level5InfoText.setVisibility(View.GONE);
            }
        });
        level3InfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                level1InfoText.setVisibility(View.GONE);
                level2InfoText.setVisibility(View.GONE);
                if (level3InfoText.getVisibility() == View.GONE) {
                    level3InfoText.setVisibility(View.VISIBLE);
                } else {
                    level3InfoText.setVisibility(View.GONE);
                }
                level4InfoText.setVisibility(View.GONE);
                level5InfoText.setVisibility(View.GONE);
            }
        });
        level4InfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                level1InfoText.setVisibility(View.GONE);
                level2InfoText.setVisibility(View.GONE);
                level3InfoText.setVisibility(View.GONE);
                if (level4InfoText.getVisibility() == View.GONE) {
                    level4InfoText.setVisibility(View.VISIBLE);
                } else {
                    level4InfoText.setVisibility(View.GONE);
                }
                level5InfoText.setVisibility(View.GONE);
            }
        });
        level5InfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                level1InfoText.setVisibility(View.GONE);
                level2InfoText.setVisibility(View.GONE);
                level3InfoText.setVisibility(View.GONE);
                level4InfoText.setVisibility(View.GONE);
                if (level5InfoText.getVisibility() == View.GONE) {
                    level5InfoText.setVisibility(View.VISIBLE);
                } else {
                    level5InfoText.setVisibility(View.GONE);
                }
            }
        });

        hrInfoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        hrInfoDialog.show();
    }

    private void getHRTrainingZones() {
        String birthDate = profilePreferencesManager.getStringProfileValue(PROFILE_USER_BIRTH);
        String[] date = birthDate.split("/");
        int userAge = getAge(Integer.parseInt(date[2]), Integer.parseInt(date[1]), Integer.parseInt(date[0]));
        int maxUserHR = maxHeartRateBeats - userAge;
        hrLevelMinValues[0] = (int) (maxUserHR * 0.5);
        hrLevelMinValues[1] = (int) (maxUserHR * 0.6);
        hrLevelMinValues[2] = (int) (maxUserHR * 0.7);
        hrLevelMinValues[3] = (int) (maxUserHR * 0.8);
        hrLevelMinValues[4] = (int) (maxUserHR * 0.9);
        hrLevelMinValues[5] = maxUserHR;
    }

    private int getAge(int year, int monthOfYear, int dayOfMonth) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate today = LocalDate.now();
            LocalDate birthDate = LocalDate.of(year, monthOfYear, dayOfMonth);
            Period p = Period.between(birthDate, today);
            return p.getYears();
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());

            Calendar birthDate = new GregorianCalendar(year, monthOfYear, dayOfMonth);
            Calendar currentDate = new GregorianCalendar(cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));

            long end = birthDate.getTimeInMillis();
            long start = currentDate.getTimeInMillis();

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(TimeUnit.MILLISECONDS.toMillis(Math.abs(end - start)));
            return (c.get(Calendar.YEAR) - 1970);
        }
    }

    public class CustomMarkerView extends MarkerView implements IMarker {
        private TextView tvTime, tvDistDeepSleep, tvSpeedNightMoves, tvAvgHR;

        public CustomMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            tvTime = findViewById(R.id.tvTime);
            tvDistDeepSleep = findViewById(R.id.tvDistDeepSleep);
            tvSpeedNightMoves = findViewById(R.id.tvSpeedNightMoves);
            tvAvgHR = findViewById(R.id.tvAvgHR);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            e.getData();
            ActivityData activityData;
            if (highlight.getDataSetIndex() == 0) {
                activityData = new ActivityData(userRunCurrentWeekActivitiesList.get((int) e.getX()));
                tvTime.setText("Time: " + activityData.getTime() + " min");
                tvDistDeepSleep.setText("Distance: " + activityData.getDistance() + " m");
                tvSpeedNightMoves.setText("Avg Speed: " + df.format(activityData.getAvgSpeed()) + " km/h");
                tvAvgHR.setText("Avg Heart Rate: " + df.format(activityData.getAvgHR()) + " bpm");
            } else if (highlight.getDataSetIndex() == 1) {
                activityData = new ActivityData(userSleepCurrentWeekActivitiesList.get((int) e.getX()));
                tvTime.setText("Time: " + activityData.getTime() + " min");
                tvDistDeepSleep.setText("Deep sleep time: " + activityData.getDeepSleepTime() + " min");
                tvSpeedNightMoves.setText("Night moves: " + activityData.getNightMoves() + " moves");
                tvAvgHR.setText("Avg Heart Rate: " + df.format(activityData.getAvgHR()) + " bpm");
            }
            super.refreshContent(e, highlight);
        }

        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2), -(getHeight()));
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
        barChart.setScaleYEnabled(false);
        CustomMarkerView mv = new CustomMarkerView(getApplicationContext(), R.layout.marker_layout);
        mv.setChartView(barChart);
        barChart.setMarkerView(mv);
        barChart.setDrawMarkers(true);
        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);

        userRunCurrentWeekActivitiesList = new ArrayList<>(getWeekActivities(weekOfYear, 0));
        userSleepCurrentWeekActivitiesList = new ArrayList<>(getWeekActivities(weekOfYear, 1));

        if (weekOfYear > 1) {
            userRunCurrentWeekBeforeActivitiesList = new ArrayList<>(getWeekActivities(weekOfYear - 1, 0));
            userSleepCurrentWeekBeforeActivitiesList = new ArrayList<>(getWeekActivities(weekOfYear - 1, 1));
        }

        int month = calendar.get(Calendar.MONTH);
        userRunCurrentMonthActivitiesList = new ArrayList<>(getMonthActivities(month, weekOfYear, 0));
        userSleepCurrentMonthActivitiesList = new ArrayList<>(getMonthActivities(month, weekOfYear, 1));

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

    private ArrayList<ActivityData> getWeekActivities(int weekOfYear, int option) {
        ArrayList<ActivityData> data = new ArrayList<>();
        if (option == 0) { // Run activities
            Calendar calendar = Calendar.getInstance();
            for (int i = 0; i < allUserRunActivitiesArrayList.size(); i++) {
                ActivityData actualActivity = new ActivityData(allUserRunActivitiesArrayList.get(i));
                calendar.setTimeInMillis(actualActivity.getTimestamp().getTime());
                Log.d("MyApp", "Week of year: " + calendar.get(Calendar.WEEK_OF_YEAR) + ", Time: " + actualActivity.getTime());
                if (calendar.get(Calendar.WEEK_OF_YEAR) == weekOfYear) {
                    Log.d("MyApp", "Week of year accepted: " + calendar.get(Calendar.WEEK_OF_YEAR));
                    data.add(actualActivity);
                }
            }
        } else { // Sleep activities
            Calendar calendar = Calendar.getInstance();
            for (int i = 0; i < allUserSleepActivitiesArrayList.size(); i++) {
                ActivityData actualActivity = new ActivityData(allUserSleepActivitiesArrayList.get(i));
                calendar.setTimeInMillis(actualActivity.getTimestamp().getTime());
                if (calendar.get(Calendar.WEEK_OF_YEAR) == weekOfYear) {
                    data.add(actualActivity);
                }
            }
        }
        for (int i = 0; i < data.size(); i++) {
            Log.d("MyAppTrial", "Time of data list: " + data.get(i).getTime());
        }
        data = new ArrayList<>(sortWeeklyDataByDay(data));
        return data;
    }

    private int positionIfDayOfMonthAdded(List<Integer> list, int dayNumber) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == dayNumber) {
                return i;
            }
        }
        return -1; // Not addded
    }

    private ArrayList<ActivityData> getMonthActivities(int month, int weekOfYear, int option) {
        ArrayList<ActivityData> data = new ArrayList<>();
        if (option == 0) { // Run activities
            Calendar calendar = Calendar.getInstance();
            List<Integer> dayOfMonthAdded = new ArrayList<Integer>();
            for (int i = 0; i < allUserRunActivitiesArrayList.size(); i++) {
                ActivityData actualActivity = new ActivityData(allUserRunActivitiesArrayList.get(i));
                calendar.setTimeInMillis(actualActivity.getTimestamp().getTime());
                Log.d("MyAppMonthTry", "Selected Week of year: " + weekOfYear + ", selected month of year: " + month);
                Log.d("MyAppMonthTry", "Week of year: " + calendar.get(Calendar.WEEK_OF_YEAR) + ", Month of year: " + calendar.get(Calendar.MONTH));

                if (calendar.get(Calendar.WEEK_OF_YEAR) < weekOfYear && calendar.get(Calendar.MONTH) == month) {
                    int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                    Log.d("MyAppMonthTry", "Week of year accepted: " + calendar.get(Calendar.WEEK_OF_YEAR) + ", Month of year: " + calendar.get(Calendar.MONTH));
                    int position = positionIfDayOfMonthAdded(dayOfMonthAdded, dayOfMonth);
                    if (position != -1) {
                        int time = data.get(position).getTime() + actualActivity.getTime();
                        actualActivity.setTime(time);
                        int distance = data.get(position).getDistance() + actualActivity.getDistance();
                        actualActivity.setDistance(distance);
                        double avgSpeed = (data.get(position).getAvgSpeed() + actualActivity.getAvgSpeed()) / 2;
                        actualActivity.setAvgSpeed(avgSpeed);
                        double avgHR = (data.get(position).getAvgHR() + actualActivity.getAvgHR()) / 2;
                        actualActivity.setAvgHR(avgHR);
                        data.set(position, actualActivity);
                    } else {
                        dayOfMonthAdded.add(dayOfMonth);
                        data.add(actualActivity);
                        Log.d("MyAppMonthTry", "Days added: " + dayOfMonthAdded.size() + ", Data size: " + data.size());
                    }
                }
            }
        } else { // Sleep activities
            Calendar calendar = Calendar.getInstance();
            for (int i = 0; i < allUserSleepActivitiesArrayList.size(); i++) {
                ActivityData actualActivity = new ActivityData(allUserSleepActivitiesArrayList.get(i));
                calendar.setTimeInMillis(actualActivity.getTimestamp().getTime());
                if (calendar.get(Calendar.WEEK_OF_YEAR) < weekOfYear && calendar.get(Calendar.MONTH) == month) {
                    data.add(actualActivity);
                }
            }
        }
        for (int i = 0; i < data.size(); i++) {
            Log.d("MyAppTrial", "Time of data list: " + data.get(i).getTime());
        }
        return data;
    }

    private ArrayList<ActivityData> sortWeeklyDataByDay(ArrayList<ActivityData> data) {
        boolean[] isDayAddedList = {false, false, false, false, false, false, false};
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
            if (isDayAddedList[dayOfWeek - 1] && !data.get(i).getType().toLowerCase().equals("sleep")) {
                ActivityData dayDataToUpdate = new ActivityData(sortedData.get(dayOfWeek - 1));
                int time = data.get(i).getTime() + dayDataToUpdate.getTime();
                dayDataToUpdate.setTime(time);
                int distance = data.get(i).getDistance() + dayDataToUpdate.getDistance();
                dayDataToUpdate.setDistance(distance);
                double avgSpeed = (data.get(i).getAvgSpeed() + dayDataToUpdate.getAvgSpeed()) / 2;
                dayDataToUpdate.setAvgSpeed(avgSpeed);
                double avgHR = (data.get(i).getAvgHR() + dayDataToUpdate.getAvgHR()) / 2;
                dayDataToUpdate.setAvgHR(avgHR);
                sortedData.set(dayOfWeek - 1, dayDataToUpdate);
            } else {
                sortedData.set(dayOfWeek - 1, data.get(i));
                isDayAddedList[dayOfWeek - 1] = true;
            }
        }
        return sortedData;
    }

    private void calculateAnalytics() {
        // Data to know if there's data in the interval [day/month]
        int runDaysThisWeek = 0;
        int sleepDaysThisWeek = 0;
        int runDaysLastWeek = 0;
        int sleepDaysLastWeek = 0;
        int runDaysMonth = 0;
        int sleepDaysMonth = 0;

        // Average Time Running and Sleeping per week (single data per day)
        int totalRunTimeThisWeek = 0; // Total running time during this week
        int totalSleepTimeThisWeek = 0; // Total sleeping time during this week
        double totalHRThisWeek = 0; // Total avgHR during this week
        int totalRunTimeLastWeek = 0; // Total running time during last week
        int totalSleepTimeLastWeek = 0; // Total sleeping time during last week
        double totalHRLastWeek = 0; // Total avgHR during last week
        int totalRunTimeMonth = 0; // Total running time during last week
        int totalSleepTimeMonth = 0; // Total sleeping time during last week
        double totalHRMonth = 0; // Total avgHR during last week

        // Max time running and sleeping in the week
        int runMaxTimeThisWeek = 0;
        int runDayMaxThisWeek = -1;
        int sleepMaxTimeThisWeek = 0;
        int sleepDayMaxThisWeek = -1;

        // Comparison between this week and last week values
        int runTimeWeeklyDifference = 0;
        int sleepTimeWeeklyDifference = 0;
        double avgRunTimeWeeklyDifference = 0;
        double avgSleepTimeWeeklyDifference = 0;
        double avgHRTimeWeeklyDifference = 0;

        // Comparison between this week and whole month values
        int runTimeMonthlyDifference = 0;
        int sleepTimeMonthlyDifference = 0;
        double avgRunTimeMonthlyDifference = 0;
        double avgSleepTimeMonthlyDifference = 0;
        double avgHRTimeMonthlyDifference = 0;

        for (int i = 0; i < 7; i++) {
            int runTimeThisWeek = userRunCurrentWeekActivitiesList.get(i).getTime();
            int sleepTimeThisWeek = userSleepCurrentWeekActivitiesList.get(i).getTime();
            double hrDataThisWeek = userRunCurrentWeekActivitiesList.get(i).getAvgHR();

            int runTimeLastWeek = userRunCurrentWeekBeforeActivitiesList.get(i).getTime();
            int sleepTimeLastWeek = userSleepCurrentWeekBeforeActivitiesList.get(i).getTime();
            double hrDataLastWeek = userRunCurrentWeekBeforeActivitiesList.get(i).getAvgHR();

            if (runTimeThisWeek > 0) {
                if (runTimeThisWeek > runMaxTimeThisWeek) {
                    runMaxTimeThisWeek = runTimeThisWeek;
                    runDayMaxThisWeek = i;
                }
                runDaysThisWeek++;
                totalRunTimeThisWeek += runTimeThisWeek;
                totalHRThisWeek += hrDataThisWeek;
            }

            if (sleepTimeThisWeek > 0) {
                if (sleepTimeThisWeek > sleepMaxTimeThisWeek) {
                    sleepMaxTimeThisWeek = sleepTimeThisWeek;
                    sleepDayMaxThisWeek = i;
                }
                sleepDaysThisWeek++;
                totalSleepTimeThisWeek += sleepTimeThisWeek;
            }

            if (runTimeLastWeek > 0) {
                runDaysLastWeek++;
                totalRunTimeLastWeek += runTimeLastWeek;
                totalHRLastWeek += hrDataLastWeek;
            }

            if (sleepTimeLastWeek > 0) {
                sleepDaysLastWeek++;
                totalSleepTimeLastWeek += sleepTimeLastWeek;
            }
        }

        generateImageButtonColor(totalHRThisWeek / runDaysThisWeek);

        for (int i = 0; i < userRunCurrentMonthActivitiesList.size(); i++) {
            int runTimeMonth = userRunCurrentMonthActivitiesList.get(i).getTime();
            double hrDataMonth = userRunCurrentMonthActivitiesList.get(i).getAvgHR();
            if (runTimeMonth > 0) {
                runDaysMonth++;
                totalRunTimeMonth += runTimeMonth;
                totalHRMonth += hrDataMonth;
            }
        }

        for (int i = 0; i < userSleepCurrentMonthActivitiesList.size(); i++) {
            int sleepTimeMonth = userSleepCurrentMonthActivitiesList.get(i).getTime();
            if (sleepTimeMonth > 0) {
                sleepDaysMonth++;
                totalSleepTimeMonth += sleepTimeMonth;
            }
        }

        if (runDaysThisWeek > 0) {
            avgRunThisWeekText.setText(df.format((float) totalRunTimeThisWeek / runDaysThisWeek) + " minutes");
            totalRunThisWeekText.setText(totalRunTimeThisWeek + " minutes");
            maxRunThisWeekText.setText(runMaxTimeThisWeek + " minutes on " + daysOfWeekList.get(runDayMaxThisWeek));
            avgHRThisWeekText.setText(df.format(totalHRThisWeek / runDaysThisWeek) + " bpm");
        } else {
            avgRunThisWeekText.setText("No data available");
            totalRunThisWeekText.setText("No data available");
            maxRunThisWeekText.setText("No data available");
            avgHRThisWeekText.setText("No data available");
        }

        if (sleepDaysThisWeek > 0) {
            avgSleepThisWeekText.setText(df.format((float) totalSleepTimeThisWeek / sleepDaysThisWeek) + " minutes");
            totalSleepThisWeekText.setText(totalSleepTimeThisWeek + " minutes");
            maxSleepThisWeekText.setText(sleepMaxTimeThisWeek + " minutes on " + daysOfWeekList.get(sleepDayMaxThisWeek));
        } else {
            avgSleepThisWeekText.setText("No data available");
            totalSleepThisWeekText.setText("No data available");
            maxSleepThisWeekText.setText("No data available");
        }

        if (runDaysThisWeek > 0 && runDaysLastWeek > 0) {
            avgRunTimeWeeklyDifference = (((double) totalRunTimeThisWeek / runDaysThisWeek) / ((double) totalRunTimeLastWeek / runDaysLastWeek)) * 100;
            avgRunTimeDifferenceLastWeekText.setText(getWeeklyCompareText(avgRunTimeWeeklyDifference, 0));
            avgHRTimeWeeklyDifference = (((double) totalHRThisWeek / runDaysThisWeek) / ((double) totalHRLastWeek / runDaysLastWeek)) * 100;
            avgHRDifferenceLastWeekText.setText(getWeeklyCompareText(avgHRTimeWeeklyDifference, 2));
            runTimeWeeklyDifference = totalRunTimeThisWeek - totalRunTimeLastWeek;
            if (runTimeWeeklyDifference > 0) {
                totalRunTimeDifferenceLastWeekText.setText(runTimeWeeklyDifference + " minutes more");
            } else if (runTimeWeeklyDifference < 0) {
                totalRunTimeDifferenceLastWeekText.setText(Math.abs(runTimeWeeklyDifference) + " minutes less");
            } else {
                totalRunTimeDifferenceLastWeekText.setText("same time");
            }
        } else {
            avgRunTimeDifferenceLastWeekText.setText("Not enough data available");
            avgHRDifferenceLastWeekText.setText("Not enough data available");
            totalRunTimeDifferenceLastWeekText.setText("Not enough data available");
        }

        if (sleepDaysThisWeek > 0 && sleepDaysLastWeek > 0) {
            avgSleepTimeWeeklyDifference = (((double) totalSleepTimeThisWeek / sleepDaysThisWeek) / ((double) totalSleepTimeLastWeek / sleepDaysLastWeek)) * 100;
            avgSleepTimeDifferenceLastWeekText.setText(getWeeklyCompareText(avgSleepTimeWeeklyDifference, 1));
            sleepTimeWeeklyDifference = totalSleepTimeThisWeek - totalSleepTimeLastWeek;
            if (sleepTimeWeeklyDifference > 0) {
                totalSleepTimeDifferenceLastWeekText.setText(sleepTimeWeeklyDifference + " minutes more");
            } else if (sleepTimeWeeklyDifference < 0) {
                totalSleepTimeDifferenceLastWeekText.setText(Math.abs(sleepTimeWeeklyDifference) + " minutes less");
            } else {
                totalSleepTimeDifferenceLastWeekText.setText("same time");
            }
        } else {
            avgSleepTimeDifferenceLastWeekText.setText("Not enough data available");
            totalSleepTimeDifferenceLastWeekText.setText("Not enough data available");
        }

        if (runDaysThisWeek > 0 && runDaysMonth > 0) {
            avgRunTimeMonthlyDifference = (((double) totalRunTimeThisWeek / runDaysThisWeek) / ((double) totalRunTimeMonth / runDaysMonth)) * 100;
            avgRunTimeDifferenceMonthText.setText(getWeeklyCompareText(avgRunTimeMonthlyDifference, 0));
            avgHRTimeMonthlyDifference = (((double) totalHRThisWeek / runDaysThisWeek) / ((double) totalHRMonth / runDaysMonth)) * 100;
            avgHRDifferenceMonthText.setText(getWeeklyCompareText(avgHRTimeMonthlyDifference, 2));
            runTimeMonthlyDifference = totalRunTimeThisWeek - totalRunTimeMonth;
            if (runTimeMonthlyDifference > 0) {
                totalRunTimeDifferenceMonthText.setText(runTimeMonthlyDifference + " minutes more");
            } else if (runTimeMonthlyDifference < 0) {
                totalRunTimeDifferenceMonthText.setText(Math.abs(runTimeMonthlyDifference) + " minutes less");
            } else {
                totalRunTimeDifferenceMonthText.setText("same time");
            }
        } else {
            avgRunTimeDifferenceMonthText.setText("Not enough data available");
            avgHRDifferenceMonthText.setText("Not enough data available");
            totalRunTimeDifferenceMonthText.setText("Not enough data available");
        }

        if (sleepDaysThisWeek > 0 && sleepDaysMonth > 0) {
            avgSleepTimeMonthlyDifference = (((double) totalSleepTimeThisWeek / sleepDaysThisWeek) / ((double) totalSleepTimeMonth / sleepDaysMonth)) * 100;
            avgSleepTimeDifferenceMonthText.setText(getWeeklyCompareText(avgSleepTimeMonthlyDifference, 1));
            sleepTimeMonthlyDifference = totalSleepTimeThisWeek - totalSleepTimeLastWeek;
            if (sleepTimeMonthlyDifference > 0) {
                totalSleepTimeDifferenceMonthText.setText(sleepTimeMonthlyDifference + " minutes more");
            } else if (sleepTimeMonthlyDifference < 0) {
                totalSleepTimeDifferenceMonthText.setText(Math.abs(sleepTimeMonthlyDifference) + " minutes less");
            } else {
                totalSleepTimeDifferenceMonthText.setText("same time");
            }
        } else {
            avgSleepTimeDifferenceMonthText.setText("Not enough data available");
            totalSleepTimeDifferenceMonthText.setText("Not enough data available");
        }
    }

    private void generateImageButtonColor(double hrData) {
        if (hrData > hrLevelMinValues[0] && hrData <= hrLevelMinValues[1]) { // Level 1
            actualHRLevel = 1;
            infoImageButton.setColorFilter(getResources().getColor(R.color.HRLevel1Color));
        } else if (hrData > hrLevelMinValues[1] && hrData <= hrLevelMinValues[2]) { // Level 2
            actualHRLevel = 2;
            infoImageButton.setColorFilter(getResources().getColor(R.color.HRLevel2Color));
        } else if (hrData > hrLevelMinValues[2] && hrData <= hrLevelMinValues[3]) { // Level 3
            actualHRLevel = 3;
            infoImageButton.setColorFilter(getResources().getColor(R.color.HRLevel3Color));
        } else if (hrData > hrLevelMinValues[3] && hrData <= hrLevelMinValues[4]) { // Level 4
            actualHRLevel = 4;
            infoImageButton.setColorFilter(getResources().getColor(R.color.HRLevel4Color));
        } else if (hrData > hrLevelMinValues[4] && hrData <= hrLevelMinValues[5]) { // Level 5
            actualHRLevel = 5;
            infoImageButton.setColorFilter(getResources().getColor(R.color.HRLevel5Color));
        } else if (hrData > hrLevelMinValues[5]) { // Over Level 5
            actualHRLevel = 6;
            infoImageButton.setColorFilter(getResources().getColor(R.color.HRLevel5Color));
        } else { // Below Level 1
            actualHRLevel = 0;
            infoImageButton.setColorFilter(getResources().getColor(R.color.colorBlack));
        }
    }

    private String getWeeklyCompareText(double difference, int option) {
        String text = "";
        if (option == 0) { // Run
            if (difference > 100) {
                text = df.format(difference - 100) + "% more";
            } else if (difference < 100) {
                text = df.format(Math.abs(difference - 100)) + "% less";
            } else {
                text = "the same";
            }
        } else if (option == 1) { // Sleep
            if (difference > 100) {
                text = df.format(difference - 100) + "% more";
            } else if (difference < 100) {
                text = df.format(Math.abs(difference - 100)) + "% less";
            } else {
                text = "the same";
            }
        } else if (option == 2) { // HR
            if (difference > 100) {
                text = df.format(difference - 100) + "% higher";
            } else if (difference < 100) {
                text = df.format(Math.abs(difference - 100)) + "% lower";
            } else {
                text = "the same";
            }
        }
        return text;
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
            Log.d("MyApp", "Data UUID : " + profilePreferencesManager.getStringProfileValue(PROFILE_USER_ID));
            final Query activity = db.collection("activities").whereEqualTo("UUID", profilePreferencesManager.getStringProfileValue(PROFILE_USER_ID));
            allUserActivitiesDataArrayList = new ArrayList<>();
            allUserRunActivitiesArrayList = new ArrayList<>();
            allUserSleepActivitiesArrayList = new ArrayList<>();

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

                                    String type = document.get(ACTIVITY_TYPE).toString();
                                    Timestamp timestamp = new Timestamp(Long.parseLong(document.get(ACTIVITY_TIMESTAMP).toString()));
                                    calendar.setTimeInMillis(timestamp.getTime());
                                    int week = calendar.get(Calendar.WEEK_OF_YEAR);
                                    Log.d("MyApp", "Week of year of the data is : " + week);
                                    Log.d("MyApp", "Date: " + new Date(timestamp.getTime()));
                                    int time = Integer.parseInt(document.get(ACTIVITY_TIME).toString());
                                    double avgHR = Double.valueOf(document.get(ACTIVITY_AVG_HR).toString());
                                    Log.d("MyApp", "Value of HRDataAVG: " + avgHR);
                                    activityData.setType(type);
                                    activityData.setTimestamp(timestamp);
                                    activityData.setTime(time);
                                    activityData.setAvgHR(avgHR);
                                    if (type.equals("sleep")) {
                                        int deepSleepTime = Integer.parseInt(document.get(ACTIVITY_DEEP_SLEEP_TIME).toString());
                                        int nightMoves = Integer.parseInt(document.get(ACTIVITY_NIGHT_MOVES).toString());
                                        activityData.setDeepSleepTime(deepSleepTime);
                                        activityData.setNightMoves(nightMoves);
                                        allUserSleepActivitiesArrayList.add(activityData);
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
                                calculateAnalytics();
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

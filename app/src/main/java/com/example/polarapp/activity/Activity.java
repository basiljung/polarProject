package com.example.polarapp.activity;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.example.polarapp.R;
import com.example.polarapp.history.HistoryActivity;
import com.example.polarapp.polar.PolarSDK;
import com.example.polarapp.ui.HomeFragment;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.List;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class Activity extends AppCompatActivity implements TimePickerFragment.TimerListener {

    private Toolbar toolbar;
    private Button startNormal, startInterval,pickerInterval ;
    private String intervaltimeViewPicker;
    private TimePickerFragment timePickerFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstpage);

        startNormal = findViewById(R.id.startNormal);
        startInterval = findViewById(R.id.startInterval);
        pickerInterval = findViewById(R.id.pickerInterval);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        startNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                openNormalTrainingActivity();
            }
        });
        startInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                openIntervalTrainingActivity();
            }
        });
        pickerInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                openPickerdialog();
            }
        });



    }

    public void openNormalTrainingActivity() {
        Intent intent = new Intent(this, ActivityNormaltraining.class);
        startActivity(intent);
    }
    public void openIntervalTrainingActivity() {
        Intent intent = new Intent(this, ActivityIntervaltraining.class);
        String message = intervaltimeViewPicker;
        Log.i("MyApp",message);
        intent.putExtra("Picker_Time", message);
        startActivity(intent);
        Log.i("MyApp","test");
    }
    public void openPickerdialog(){
        DialogFragment timePicker = new TimePickerFragment(Activity.this);
        timePicker.show(getSupportFragmentManager(),"time picker");
        startInterval.setEnabled(true);
        startInterval.setVisibility(View.VISIBLE);
    }

    @Override
    public void applyTimeChange(String pickerTime) {
        intervaltimeViewPicker = pickerTime;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}

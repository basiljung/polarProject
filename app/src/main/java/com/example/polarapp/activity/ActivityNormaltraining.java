package com.example.polarapp.activity;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.polarapp.R;

public class ActivityNormaltraining extends AppCompatActivity {

    private Toolbar toolbar;
    private Chronometer chronometer;
    private Button startChronometer,pauseChronometer,resetChronometer;
    private boolean runningChronometer;
    private long pauseOffset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normaltraining);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //***************Chronometer implementation**************************
        chronometer = findViewById(R.id.chronometer);
        startChronometer = findViewById(R.id.startChronometer);
        pauseChronometer = findViewById(R.id.pauseChronometer);
        resetChronometer = findViewById(R.id.resetChronometer);

        startChronometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                startChronometer(root);
            }
        });
        pauseChronometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                pauseChronometer(root);
            }
        });
        resetChronometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                resetChronometer(root);
            }
        });

    }

    public void startChronometer(View v){
        if(!runningChronometer){
            chronometer.setBase(SystemClock.elapsedRealtime()- pauseOffset);
            chronometer.start();
            runningChronometer = true;
            resetChronometer.setVisibility(View.INVISIBLE);
        }
    }

    public void resetChronometer(View v){
        chronometer.setBase(SystemClock.elapsedRealtime());
        runningChronometer = false;
        pauseOffset = 0;
        resetChronometer.setVisibility(View.INVISIBLE);
    }
    public void pauseChronometer(View v){
        if(runningChronometer){
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            runningChronometer = false;
            resetChronometer.setVisibility(View.VISIBLE);
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

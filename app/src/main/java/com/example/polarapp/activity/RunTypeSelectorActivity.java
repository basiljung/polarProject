package com.example.polarapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.example.polarapp.R;

public class RunTypeSelectorActivity extends AppCompatActivity implements TimePickerFragment.TimerListener {

    private Toolbar toolbar;
    private Button startNormal, startInterval, pickerInterval;
    private String intervalTimeViewPicker;
    private TimePickerFragment timePickerFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_type_selector);

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
                openPickerDialog();
            }
        });
    }

    public void openNormalTrainingActivity() {
        Intent intent = new Intent(this, NormalTrainingActivity.class);
        startActivity(intent);
    }

    public void openIntervalTrainingActivity() {
        Intent intent = new Intent(this, IntervalTrainingActivity.class);
        String message = intervalTimeViewPicker;
        Log.i("MyApp", message);
        intent.putExtra("Picker_Time", message);
        startActivity(intent);
        Log.i("MyApp", "test");
    }

    public void openPickerDialog() {
        DialogFragment timePicker = new TimePickerFragment(RunTypeSelectorActivity.this);
        timePicker.show(getSupportFragmentManager(), "time picker");
        startInterval.setEnabled(true);
    }

    @Override
    public void applyTimeChange(String pickerTime) {
        intervalTimeViewPicker = pickerTime;
        startInterval.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

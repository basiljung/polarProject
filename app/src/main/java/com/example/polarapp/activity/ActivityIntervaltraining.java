package com.example.polarapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.polarapp.R;
import com.google.firebase.firestore.model.value.IntegerValue;

import java.util.Locale;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class ActivityIntervaltraining extends AppCompatActivity {

    private Toolbar toolbar;
    private String pickerTime;
    private TextView textViewHeartRate,textViewTimer;
    private Button pauseStartBtn, resetBtn;
    private CountDownTimer countDownTimer;
    private boolean runningTimer;
    private long TimeLeftInMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intervaltraining);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        Intent intent = getIntent();
        pickerTime = intent.getStringExtra("Picker_Time");
        String[] times = pickerTime.split(":");
        for (int i=0; i< times.length;i++){
            if (times[i].length()==1) {
                times[i] = "0"+times[i];
            }
        }
        pickerTime = times[0]+":"+times[1];
        TimeLeftInMillis = Integer.valueOf(times[0])*60*1000 + Integer.valueOf(times[1])*1000;

        textViewHeartRate = findViewById(R.id.hrData);
        textViewTimer = findViewById(R.id.txtVTimePicker);
        textViewTimer.setText(pickerTime);
        pauseStartBtn =findViewById(R.id.pauseStartBtn);
        resetBtn = findViewById(R.id.resetBtn);

        pauseStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                if(runningTimer){
                    pauseTimer(root);
                }else{
                    startTimer(root);
                }
            }
        });
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                resetTimer(root);
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

    private void updateCountDownText(){
        int minutes = (int)(TimeLeftInMillis/1000)/60;
        int seconds = (int)(TimeLeftInMillis/1000)%60;

        String timeLeftFormatted = String.format(Locale.getDefault(),"%02d:%02d",minutes,seconds);
        textViewTimer.setText(timeLeftFormatted);
    }

    private void startTimer(View root) {
        countDownTimer = new CountDownTimer(TimeLeftInMillis,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }
            @Override
            public void onFinish() {
                runningTimer = false;
                pauseStartBtn.setText("Start");
                pauseStartBtn.setVisibility(View.INVISIBLE);
                resetBtn.setVisibility(View.VISIBLE);
            }
        }.start();
        runningTimer = true;
        pauseStartBtn.setText("Pause");
        resetBtn.setVisibility(View.INVISIBLE);
    }

    private void pauseTimer(View root) {
        countDownTimer.cancel();
        runningTimer = false;
        pauseStartBtn.setText("Start");
        resetBtn.setVisibility(View.VISIBLE);
    }
    private void resetTimer(View root) {
        String[] times = pickerTime.split(":");
        TimeLeftInMillis = Integer.valueOf(times[0])*60*1000 + Integer.valueOf(times[1])*1000;
        updateCountDownText();
        resetBtn.setVisibility(View.INVISIBLE);
        pauseStartBtn.setVisibility(View.VISIBLE);
    }
}

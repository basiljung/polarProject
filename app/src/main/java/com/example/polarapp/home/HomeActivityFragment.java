package com.example.polarapp.home;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.polarapp.R;

import java.util.Locale;

public class HomeActivityFragment extends Fragment implements TimePickerFragment.TimerListener {
    private static  final long START_TIME_IN_MIllIS = 600000;
    private TextView textViewTimer;
    Button startpauseTimer, resetTimer;
    private CountDownTimer countDownTimer;
    private boolean runningTimer;
    private long TimeLeftInMillis = START_TIME_IN_MIllIS;
    //*******************
    private Chronometer chronometer;
    private boolean runningChronometer;
    private long pauseOffset;
    Button startChronometer, pauseChronometer, resetChronometer;
    //*********
    private TextView textViewPicker;


    View root;
    @Override
    public View onCreateView (@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        //*********Stopwatch implementation*********************
        root = inflater.inflate(R.layout.fragment_activity, container, false);
        textViewTimer = root.findViewById(R.id.timer);
        startpauseTimer = root.findViewById(R.id.startpauseTimer);
        resetTimer = root.findViewById(R.id.resetTimer);
        startpauseTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                if(runningTimer){
                    pauseTimer(root);
                }else{
                    startTimer(root);
                }
            }
        });
        resetTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                resetTimer(root);
            }
        });
        updateCountDownText();
        //***************Chronometer implementation**************************
        chronometer = root.findViewById(R.id.chronometer);
        startChronometer = root.findViewById(R.id.startChronometer);
        pauseChronometer = root.findViewById(R.id.pauseChronometer);
        resetChronometer = root.findViewById(R.id.resetChronometer);

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
        //***********timepicker****************
        textViewPicker = root.findViewById(R.id.txtVTimePicker);
        Button timePickerBtn = root.findViewById(R.id.pickerBtn);
        timePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                TimePickerFragment timePickerFragment = new TimePickerFragment(HomeActivityFragment.this);
                timePickerFragment.show(getFragmentManager(),"time picker");
            }
        });
        return root;
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
                startpauseTimer.setText("Start");
                startpauseTimer.setVisibility(View.INVISIBLE);
                resetTimer.setVisibility(View.VISIBLE);
            }
        }.start();
        runningTimer = true;
        startpauseTimer.setText("Pause");
        resetTimer.setVisibility(View.INVISIBLE);
    }

    private void pauseTimer(View root) {
        countDownTimer.cancel();
        runningTimer = false;
        startpauseTimer.setText("Start");
        resetTimer.setVisibility(View.VISIBLE);
    }
    private void resetTimer(View root) {
        TimeLeftInMillis = START_TIME_IN_MIllIS;
        updateCountDownText();
        resetTimer.setVisibility(View.INVISIBLE);
        startpauseTimer.setVisibility(View.VISIBLE);
    }

    private void updateCountDownText(){
        int minutes = (int)(TimeLeftInMillis/1000)/60;
        int seconds = (int)(TimeLeftInMillis/1000)%60;

        String timeLeftFormatted = String.format(Locale.getDefault(),"%02d:%02d",minutes,seconds);
        textViewTimer.setText(timeLeftFormatted);
    }

    public void startChronometer(View v){
        if(!runningChronometer){
            chronometer.setBase(SystemClock.elapsedRealtime()- pauseOffset);
            chronometer.start();
            runningChronometer = true;
        }
    }

    public void resetChronometer(View v){
        chronometer.setBase(SystemClock.elapsedRealtime());
        runningChronometer = false;
        pauseOffset = 0;
    }
    public void pauseChronometer(View v){
        if(runningChronometer){
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            runningChronometer = false;
        }
    }

    @Override
    public void applyTimeChage(String pickerTime) {
        textViewPicker.setText(pickerTime);
    }
}

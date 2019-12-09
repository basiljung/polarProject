package com.example.polarapp.sleep;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;


import com.example.polarapp.R;

import java.util.Calendar;

public class SleepActivity extends AppCompatActivity {


    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep);

        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);

        final TimePicker timePicker = findViewById(R.id.timePicker);
        final EditText sleepFor = findViewById(R.id.editText5);







        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String time = ("Alarm Set for " + timePicker.getCurrentHour() + ":" + timePicker.getCurrentMinute());
                Toast.makeText(SleepActivity.this, time, Toast.LENGTH_LONG).show();



            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String alarmAfter = ("Alarm after " + sleepFor.getText());
                Toast.makeText(SleepActivity.this, alarmAfter, Toast.LENGTH_LONG).show();
                int myNum = 0;

                try {
                    myNum = Integer.parseInt(sleepFor.getText().toString());
                } catch(NumberFormatException nfe) {
                    System.out.println("Could not parse " + nfe);
                }

                final int finalMyNum = myNum;
                new CountDownTimer(finalMyNum * 1000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                        //here you can have your logic to set text to edittext
                        Log.d("numero", String.valueOf(finalMyNum));
                    }

                    public void onFinish() {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "munChannelNimi")
                                .setSmallIcon(R.drawable.launch_screen)
                                .setContentTitle("YOLO")
                                .setContentText("alarm")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        builder.build();

                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        //NotificationManager.notify().

                        mNotificationManager.notify(001, builder.build());
                        /*
                        NotificationManager mNotificationManager =

                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        mNotificationManager.notify(001, mBuilder.build());

                         */
                    }
                }.start();
            }
        });
    }



    @Override
    protected void onStart() {
        //googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        //googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        //if (googleApiClient.isConnected()) {
            //startLocationUpdates();
        //}
    }
}

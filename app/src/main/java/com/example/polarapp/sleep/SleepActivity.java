package com.example.polarapp.sleep;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
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
        timePicker.setIs24HourView(true);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String time = ("Alarm Set for " + timePicker.getCurrentHour() + ":" + timePicker.getCurrentMinute());
                Toast.makeText(SleepActivity.this, time, Toast.LENGTH_LONG).show();

                //will fire in 60 seconds
                /*
                long when = System.currentTimeMillis() + 5000L;
                AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(SleepActivity.this, MyReceiver.class);
                intent.putExtra("myAction", "mDoNotify");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(SleepActivity.this, 0, intent, 0);
                am.set(AlarmManager.RTC_WAKEUP, when, pendingIntent);
                */

                //will fire in 60 seconds
                long when = System.currentTimeMillis() + 60000L;

                AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(SleepActivity.this, MyReceiver.class);
                intent.putExtra("myAction", "mDoNotify");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(SleepActivity.this, 0, intent, 0);
                am.set(AlarmManager.RTC_WAKEUP, when, pendingIntent);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String alarmAfter = ("Alarm after " + timePicker.getCurrentHour() + ":" + timePicker.getCurrentMinute());
                Toast.makeText(SleepActivity.this, alarmAfter, Toast.LENGTH_LONG).show();
                int myNum = 0;

                final int finalMyNum = myNum;

                new CountDownTimer(timePicker.getCurrentHour()*60*60*1000 + timePicker.getCurrentMinute() * 60 * 1000, 1000 * 60 * 10) {
                    public void onTick(long millisUntilFinished) {
                        //for every 10 minutes
                        Log.d("numero", String.valueOf(finalMyNum));
                    }

                    public void onFinish() {
                        Toast.makeText(SleepActivity.this, "DONE!", Toast.LENGTH_LONG).show();
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
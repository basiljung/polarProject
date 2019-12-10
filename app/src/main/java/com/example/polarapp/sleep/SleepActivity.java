package com.example.polarapp.sleep;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
                Date currentTime = Calendar.getInstance().getTime();

                int currentHour = currentTime.getHours();
                int currentMinute = currentTime.getMinutes();

                int alarmHour = timePicker.getCurrentHour();
                int alarmMinute = timePicker.getCurrentMinute();

                int differenceHours = alarmHour - currentHour;
                int differenceMinutes = alarmMinute - currentMinute;

                if(differenceHours < 0 ) {
                    differenceHours += 24;
                }

                if(differenceMinutes < 0) {
                    differenceMinutes += 60;
                    differenceHours -= 1;
                }

                scheduleNotification(getNotification("Wake Up!"), differenceHours*60*60*1000 + differenceMinutes * 60 * 1000);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String alarmAfter = ("Alarm after " + timePicker.getCurrentHour() + ":" + timePicker.getCurrentMinute());
                Toast.makeText(SleepActivity.this, alarmAfter, Toast.LENGTH_LONG).show();
                int myNum = 0;
                final int finalMyNum = myNum;

                scheduleNotification(getNotification("Wake Up!"), timePicker.getCurrentHour()*60*60*1000 + timePicker.getCurrentMinute() * 60 * 1000);

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


    private void scheduleNotification(Notification notification, int delay) {
        //Channel Set-up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("1001", "alarm", NotificationManager.IMPORTANCE_HIGH);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(this, MyReceiver.class);
        notificationIntent.putExtra(MyReceiver.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(MyReceiver.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    //Notification Builder
    private Notification getNotification(String content) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setChannelId("1001");
        builder.setContentTitle("Polaris");
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.ic_menu_settings);
        return builder.build();
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
    }
}
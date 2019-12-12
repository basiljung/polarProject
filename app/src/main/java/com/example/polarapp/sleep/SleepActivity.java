package com.example.polarapp.sleep;

import androidx.annotation.NonNull;
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
import com.example.polarapp.preferencesmanager.ProfilePreferencesManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.example.polarapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SleepActivity extends AppCompatActivity {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    public int differenceMillis;
    private Timestamp startTimestamp = null; // timestamp
    private ProfilePreferencesManager profilePreferencesManager;
    private static final String PROFILE_USER_ID = "profile_user_id";
    public String documentID;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep);
        final TextView timer = findViewById(R.id.timer);
        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);
        final TimePicker timePicker = findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        profilePreferencesManager = new ProfilePreferencesManager(getBaseContext());

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String time = ("Alarm Set for " + timePicker.getCurrentHour() + ":" + timePicker.getCurrentMinute());
                timer.setText(time);
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

                int differenceMillis = differenceHours*60*60*1000 + differenceMinutes * 60 * 1000;

                scheduleNotification(getNotification("Wake Up!"), differenceMillis);
                createDatabase();
                startThread(differenceMillis);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String alarmAfter = ("Alarm after " + timePicker.getCurrentHour() + ":" + timePicker.getCurrentMinute());
                timer.setText(alarmAfter);
                Toast.makeText(SleepActivity.this, alarmAfter, Toast.LENGTH_LONG).show();
                int myNum = 0;
                final int finalMyNum = myNum;
                int differenceMillis = timePicker.getCurrentHour()*60*60*1000 + timePicker.getCurrentMinute() * 60 * 1000;

                scheduleNotification(getNotification("Wake Up!"), differenceMillis);
                createDatabase();
                startThread(differenceMillis);

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
        builder.setSmallIcon(R.drawable.trial_logo);
        return builder.build();
    }

    public void createDatabase() {
        Log.d("sleepActivity", "Database Created");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> sleep = new HashMap<>();
        sleep.put("UUID", profilePreferencesManager.getStringProfileValue(PROFILE_USER_ID));
        sleep.put("type", "sleep");
        sleep.put("HRArray", Arrays.asList(45,55));

        db.collection("activities")
                .add(sleep)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        documentID = documentReference.getId();
                        Log.d("sleepActivity", "DocumentSnapshot written with ID: " + documentID);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("sleepActivity", "Error adding document", e);
                    }
                });
    }

    public void startThread(final int differenceMillis) {

        new Thread( new Runnable() {

            @Override
            public void run() {

                try {
                    for(int i = 0; i < differenceMillis / 1000; i++) {
                        Thread.sleep(1000);
                        Log.d("sleepActivity", "differenceMillis: "+ differenceMillis);
                        Log.d("sleepActivity", "counter: " + i);
                        Log.d("sleepActivity", "Sleep ID: " + documentID);
                        if(i % 10 == 1)
                        {
                            DocumentReference sleepRef = db.collection("activities").document(documentID);
                            sleepRef.update("HRArray", FieldValue.arrayUnion(i));
                            Log.d("sleepActivity", "10 sec");
                        }
                        //update database
                    }
                    Log.d("sleepActivity", "READY");
                } catch (InterruptedException e) {}
        }}).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
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
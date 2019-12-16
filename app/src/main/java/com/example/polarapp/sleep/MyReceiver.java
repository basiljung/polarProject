package com.example.polarapp.sleep;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.example.polarapp.MainActivity;
import com.example.polarapp.R;

import java.util.Timer;
import java.util.TimerTask;

public class MyReceiver extends BroadcastReceiver {
    private static String NOTIFICATION_CHANNEL_ID = "101";
    private static int NOTIFICATION_ID = 1;
    private Context context;
    private MediaPlayer mp;
    private AudioManager am;
    private int userVolume = 0;

    public void onReceive(Context context, Intent intent) {
        this.context = context;
        boolean cancel = intent.getBooleanExtra("cancel", false);
        if (cancel) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTIFICATION_ID);
            if (mp.isPlaying()) {
                mp.stop();
                am.setStreamVolume(AudioManager.STREAM_MUSIC, userVolume, 0);
            }
        } else {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notification", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("Game Notifications");
                notificationChannel.enableLights(true);
                notificationChannel.setVibrationPattern(new long[]{200, 200});
                notificationChannel.enableVibration(false);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            Intent content = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, content, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent broadcastIntent = new Intent(context, MyReceiver.class);
            broadcastIntent.putExtra("cancel", true);
            PendingIntent actionIntent = PendingIntent.getBroadcast(context, 0,
                    broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Builder notificationBuilder = new Notification.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_polaris_logo)
                    .setContentTitle("Wake Up!")
                    .setAutoCancel(true)
                    .setSound(defaultSound)
                    .setContentIntent(contentIntent)
                    .setWhen(System.currentTimeMillis())
                    .setPriority(Notification.PRIORITY_MAX)
                    .addAction(R.mipmap.ic_polaris_logo, "Stop Alarm", actionIntent);

            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
            playAlarm();
        }
    }

    public void playAlarm() {
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alert == null) {
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (alert == null) {
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        mp = MediaPlayer.create(context, alert);
        am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        userVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        mp.setLooping(true);
        mp.start();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (mp.isPlaying()) {
                    mp.stop();
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, userVolume, 0);
                }
            }
        }, 30000);
    }
}
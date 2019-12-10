package com.example.polarapp.sleep;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;

import com.example.polarapp.R;

import androidx.core.app.NotificationCompat;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //you might want to check what's inside the Intent
        if(intent.getStringExtra("myAction") != null &&
                intent.getStringExtra("myAction").equals("notify")){
            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                    //example for large icon
                    //.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                    .setContentTitle("Polaris")
                    .setContentText("Wake up")
                    .setOngoing(false)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);
            Intent i = new Intent(context, SleepActivity.class);
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(
                            context,
                            0,
                            i,
                            PendingIntent.FLAG_ONE_SHOT
                    );
            // example for blinking LED
            //builder.setLights(0xFFb71c1c, 1000, 2000);
            Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            builder.setSound(uri);
            builder.setContentIntent(pendingIntent);
            manager.notify(12345, builder.build());
        }

    }
}
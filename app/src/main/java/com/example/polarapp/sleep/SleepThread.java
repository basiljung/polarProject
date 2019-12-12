package com.example.polarapp.sleep;
import android.util.Log;
import android.widget.TextView;

public class SleepThread extends Thread {
    int seconds;

    SleepThread(int seconds) {
        this.seconds = seconds;
        Log.d("yolo", "inside SleepThread");
        run();
    }

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(1000);
                Log.d("yolo", "thread running");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /*
        Log.d("yolo", "inside run :" + seconds);
        for(int i = 0; i < seconds / 10; i++) {
            Log.d("yolo", "start thread: " + i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } interrupt();
        */
    }
}
package com.example.polarapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.polarapp.activity.Activity;
import com.example.polarapp.activity.ActivityActivity;
import com.example.polarapp.analytics.AnalyticsActivity;
import com.example.polarapp.history.HistoryActivity;
import com.example.polarapp.R;
import com.example.polarapp.preferencesmanager.DevicePreferencesManager;

public class HomeFragment extends Fragment {

    private ImageView trainingImage, sleepImage, historyImage, analyticsImage;
    private View root;
    private DevicePreferencesManager devicePreferencesManager;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);

        devicePreferencesManager = new DevicePreferencesManager(getActivity().getBaseContext());

        trainingImage = root.findViewById(R.id.trainingImage);
        sleepImage = root.findViewById(R.id.sleepImage);
        historyImage = root.findViewById(R.id.historyImage);
        analyticsImage = root.findViewById(R.id.analyticsImage);
        trainingImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Selected Training", Toast.LENGTH_SHORT).show();
                openActivityActivity();
                // Start activity with the timer, distance and all the stuff we need.
                // Use also on it the PolarSDK function with an interface.
                // Add onPause, onResume, etc in the activity, and call specific functions
                // of the PolarSDK, really important.
            }
        });
        sleepImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Selected Sleep", Toast.LENGTH_SHORT).show();
                openSleepActivity();
                // Should be something similar to the activity, but you will need to record the sleep.
                // Add onPause, onResume, etc in the activity, and call specific functions
                // of the PolarSDK, really important.
            }
        });
        historyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Selected History", Toast.LENGTH_SHORT).show();
                openHistoryActivity();
                // Start activity where we'll load all the data saved about the training history.
                // Showing the last activities, with distance, profile, HR and other things.
            }
        });
        analyticsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Selected Analytics", Toast.LENGTH_SHORT).show();
                openAnalyticsActivity();
                // Should be something similar to the activity, but you will need to record the sleep.
                // Add onPause, onResume, etc in the activity, and call specific functions
                // of the PolarSDK, really important.
            }
        });
        return root;
    }

    public void openHistoryActivity() {
        Intent myIntent = new Intent(HomeFragment.this.getActivity(), HistoryActivity.class);
        startActivity(myIntent);
    }

    public void openActivityActivity() {
        //if (devicePreferencesManager.getConnectedDevices() == 1) {
        Intent myIntent = new Intent(HomeFragment.this.getActivity(), Activity.class);
        startActivity(myIntent);
        //} else {
        //    Toast.makeText(getContext(), "Please, connect one Polar device first", Toast.LENGTH_SHORT).show();
        //}
    }

    public void openSleepActivity() {
        //Intent myIntent = new Intent(HomeFragment.this.getActivity(), MapsActivity.class);
        //startActivity(myIntent);
    }

    public void openAnalyticsActivity() {
        Intent myIntent = new Intent(HomeFragment.this.getActivity(), AnalyticsActivity.class);
        startActivity(myIntent);
    }
}
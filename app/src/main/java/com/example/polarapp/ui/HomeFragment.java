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

import com.example.polarapp.history.HistoryActivity;
import com.example.polarapp.R;
import com.example.polarapp.sleep.SleepActivity;

public class HomeFragment extends Fragment {

    private ImageView trainingImage, sleepImage, historyImage;
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);
        trainingImage = root.findViewById(R.id.trainingImage);
        sleepImage = root.findViewById(R.id.sleepImage);
        historyImage = root.findViewById(R.id.historyImage);
        trainingImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Selected Training", Toast.LENGTH_SHORT).show();

                // Start activity with the timer, distance and all the stuff we need.
                // Use also on it the PolarSDK function with an interface.
                // Add onPause, onResume, etc in the activity, and call specific functions
                // of the PolarSDK, really important.
            }
        });
        sleepImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSleepActivity();
                // Should be something similar to the activity, but you will need to record the sleep.
                // Add onPause, onResume, etc in the activity, and call specific functions
                // of the PolarSDK, really important.
            }
        });
        historyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openHistoryActivity();
                // Start activity where we'll load all the data saved about the training history.
                // Showing the last activities, with distance, profile, HR and other things.
            }
        });
        return root;
    }

    public void openSleepActivity() {
        Intent myIntent = new Intent(HomeFragment.this.getActivity(), SleepActivity.class);
        startActivity(myIntent);
    }

    public void openHistoryActivity() {
        //Intent intent = new Intent(this, HistoryActivity.class);
        Intent myIntent = new Intent(HomeFragment.this.getActivity(), HistoryActivity.class);
        startActivity(myIntent);
    }
}
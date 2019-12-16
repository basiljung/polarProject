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

import com.example.polarapp.R;
import com.example.polarapp.activity.RunTypeSelectorActivity;
import com.example.polarapp.analytics.AnalyticsActivity;
import com.example.polarapp.history.HistoryActivity;
import com.example.polarapp.preferencesmanager.DevicePreferencesManager;
import com.example.polarapp.sleep.SleepActivity;

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
                openActivityActivity();
            }
        });
        sleepImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSleepActivity();
            }
        });
        historyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openHistoryActivity();
            }
        });
        analyticsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAnalyticsActivity();
            }
        });
        return root;
    }

    public void openHistoryActivity() {
        Intent myIntent = new Intent(HomeFragment.this.getActivity(), HistoryActivity.class);
        startActivity(myIntent);
    }

    public void openActivityActivity() {
        if (devicePreferencesManager.getConnectedDevices() == 1) {
            Intent myIntent = new Intent(HomeFragment.this.getActivity(), RunTypeSelectorActivity.class);
            startActivity(myIntent);
        } else {
            Toast.makeText(getContext(), "Please, connect one Polar device first", Toast.LENGTH_SHORT).show();
        }
    }

    public void openSleepActivity() {
        Intent myIntent = new Intent(HomeFragment.this.getActivity(), SleepActivity.class);
        startActivity(myIntent);
    }

    public void openAnalyticsActivity() {
        Intent myIntent = new Intent(HomeFragment.this.getActivity(), AnalyticsActivity.class);
        startActivity(myIntent);
    }
}
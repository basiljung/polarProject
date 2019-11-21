package com.example.polarapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.polarapp.HistoryActivity;
import com.example.polarapp.R;

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
            }
        });
        sleepImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Selected Sleep", Toast.LENGTH_SHORT).show();
            }
        });
        historyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Selected History LOL", Toast.LENGTH_SHORT).show();
                openHistoryActivity();
            }
        });
        return root;
    }

    public void openHistoryActivity() {
        //Intent intent = new Intent(this, HistoryActivity.class);
        Intent myIntent = new Intent(HomeFragment.this.getActivity(), HistoryActivity.class);
        startActivity(myIntent);
    }
}
package com.example.polarapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.polarapp.R;
import com.example.polarapp.home.HomeActivityFragment;

public class HomeFragment extends Fragment {

    ImageView trainingImage, sleepImage, historyImage;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        trainingImage = root.findViewById(R.id.trainingImage);
        sleepImage = root.findViewById(R.id.sleepImage);
        historyImage = root.findViewById(R.id.historyImage);
        trainingImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Selected Training", Toast.LENGTH_SHORT).show();
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeActivityFragment()).commit();
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
                Toast.makeText(getContext(), "Selected History", Toast.LENGTH_SHORT).show();
            }
        });
        return root;
    }
}
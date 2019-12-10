package com.example.polarapp.ui;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.polarapp.R;

public class SettingsFragment extends Fragment {

    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_settings, container, false);
        TextView textView = root.findViewById(R.id.text_tools);
        textView.setText("Hola");
        return root;
    }
}
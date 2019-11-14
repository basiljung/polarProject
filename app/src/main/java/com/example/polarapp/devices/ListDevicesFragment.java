package com.example.polarapp.devices;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.polarapp.R;

public class ListDevicesFragment extends Fragment {

    private View root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_list_devices, container, false);
        Toast.makeText(getContext(), "List devices pressed", Toast.LENGTH_SHORT).show();

        return root;
    }
}

package com.example.polarapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.polarapp.R;
import com.example.polarapp.devices.ListDevicesFragment;
import com.example.polarapp.devices.ScanDevicesFragment;
import com.example.polarapp.polar.PolarSDK;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DeviceListFragment extends Fragment {

    private BottomNavigationView bottomNavigationView;
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_device_list, container, false);

        bottomNavigationView = root.findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.device_list_list:
                        getFragmentManager().beginTransaction().replace(R.id.fragment_container_devices, new ListDevicesFragment()).commit();
                        break;
                    case R.id.device_list_scan:
                        getFragmentManager().beginTransaction().replace(R.id.fragment_container_devices, new ScanDevicesFragment()).commit();
                        break;
                }
                return true;
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.device_list_list);
        return root;
    }
}
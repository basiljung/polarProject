package com.example.polarapp.ui;

import android.os.Bundle;
import android.view.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.polarapp.R;
import com.example.polarapp.devices.SearchDevicesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DevicesFragment extends Fragment {

    private BottomNavigationView bottomNavigationView;
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_device, container, false);

        bottomNavigationView = root.findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.device_list_list:
                        getFragmentManager().beginTransaction().replace(R.id.fragment_container_devices, new SearchDevicesFragment()).commit();
                        break;
                }
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.device_list_list);
        return root;
    }
}
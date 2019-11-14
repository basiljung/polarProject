package com.example.polarapp.devices;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.example.polarapp.R;
import com.example.polarapp.polar.PolarSDK;
import java.util.ArrayList;
import polar.com.sdk.api.model.PolarDeviceInfo;

public class ScanDevicesFragment extends Fragment implements PolarSDK.CallbackInterfaceDevices {

    private View root;
    private PolarSDK polarSDK;
    private ListView listView;
    private DevicesArrayAdapter devicesArrayAdapter;
    private ArrayList<PolarDeviceInfo> polarDeviceInfoArrayList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_scan_devices, container, false);
        polarDeviceInfoArrayList = new ArrayList<>();
        listView = root.findViewById(R.id.devices_list_view);

        Toast.makeText(getContext(), "Scan devices pressed", Toast.LENGTH_SHORT).show();
        polarSDK = new PolarSDK(getContext(), this);
        polarSDK.startAPI();
        polarSDK.scanDevices();
        return root;
    }

    @Override
    public void scanDevice(PolarDeviceInfo polarDeviceInfo) {
        Log.d("MyApp", "polar device found id: " + polarDeviceInfo.deviceId + " address: " + polarDeviceInfo.address + " rssi: " + polarDeviceInfo.rssi + " name: " + polarDeviceInfo.name + " isConnectable: " + polarDeviceInfo.isConnectable);
        polarDeviceInfoArrayList.add(polarDeviceInfo);
        devicesArrayAdapter = new DevicesArrayAdapter(getContext(), polarDeviceInfoArrayList);
        listView.setAdapter(devicesArrayAdapter);
    }

    @Override
    public void deviceConnected(boolean ok) {
        // Do stuff to send it to the list of connected devices
    }
}

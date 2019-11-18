package com.example.polarapp.devices;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.polarapp.R;
import com.example.polarapp.polar.PolarSDK;
import java.util.ArrayList;
import polar.com.sdk.api.model.PolarDeviceInfo;

public class ScanDevicesFragment extends Fragment implements PolarSDK.CallbackInterfaceDevices, DevicesArrayAdapter.ButtonConnectCallback {

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

        polarSDK = new PolarSDK(getContext(), this);
        polarSDK.startAPI();
        polarSDK.scanDevices();
        return root;
    }

    @Override
    public void scanDevice(PolarDeviceInfo polarDeviceInfo) {
        Log.d("MyApp", "polar device found id: " + polarDeviceInfo.deviceId + " address: " + polarDeviceInfo.address + " rssi: " + polarDeviceInfo.rssi + " name: " + polarDeviceInfo.name + " isConnectable: " + polarDeviceInfo.isConnectable);
        polarDeviceInfoArrayList.add(polarDeviceInfo);
        devicesArrayAdapter = new DevicesArrayAdapter(this, getContext(), polarDeviceInfoArrayList);
        listView.setAdapter(devicesArrayAdapter);
    }

    @Override
    public void deviceConnected(boolean ok, View v) {
        Button button = v.findViewById(R.id.connectButton);
        if (ok) {
            button.setText("Connected");
            button.setClickable(false);
        } else {
            button.setText("Not connected");
            button.setClickable(false);
        }
    }

    @Override
    public void onClickButtonListView(View v, String id, int position) {
        Toast.makeText(getContext(), "Pressed button to connect", Toast.LENGTH_SHORT).show();
        //polarDeviceInfoArrayList.remove(position);
        //devicesArrayAdapter = new DevicesArrayAdapter(this, getContext(), polarDeviceInfoArrayList);
        //listView.setAdapter(devicesArrayAdapter);
        //Log.d("MyApp", textView.getText().toString());
        polarSDK.connectDevice(id, v);

        // Create AsyncTask to ask for the connection. On finish, update deleting the item, and adding it to the connected devices list

    }
}

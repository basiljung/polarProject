package com.example.polarapp.devices;

import android.os.*;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.*;
import android.widget.*;

import com.example.polarapp.R;
import com.example.polarapp.polar.PolarSDK;
import com.example.polarapp.preferencesmanager.DevicePreferenceManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

import polar.com.sdk.api.model.PolarDeviceInfo;

public class ListDevicesFragment extends Fragment implements PolarSDK.CallbackInterfaceDevices, DevicesArrayAdapter.ButtonConnectCallback {

    private View root;
    private PolarSDK polarSDK;
    private ListView listView;
    private DevicesArrayAdapter devicesArrayAdapter;
    private ArrayList<PolarDeviceInfo> polarDeviceInfoArrayList = new ArrayList<>();
    private PolarDeviceInfo polarDeviceInfo;
    private TextView textBattery;
    private MaterialButton button;
    private LinearLayout batteryLayout;
    private boolean isBatteryReceived = false;
    private boolean isDeviceDisconnected = false;
    private boolean isDeviceConnected = false;
    private DevicePreferenceManager devicePreferenceManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_list_devices, container, false);
        devicePreferenceManager = new DevicePreferenceManager(getActivity().getBaseContext());

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
        devicesArrayAdapter = new DevicesArrayAdapter(this, getActivity().getBaseContext(), polarDeviceInfoArrayList);
        listView.setAdapter(devicesArrayAdapter);
    }

    @Override
    public void deviceConnected(boolean ok) {
        isDeviceConnected = ok;
    }

    @Override
    public void deviceDisconnected(boolean ok) {
        isDeviceDisconnected = ok;
    }

    @Override
    public void batteryDataReceived(int batteryLevel) {
        devicePreferenceManager.setBatteryLevel(batteryLevel);
        isBatteryReceived = true;
    }


    @Override
    public void onClickButtonListView(View v, String id, PolarDeviceInfo pdi) {
        polarDeviceInfo = pdi;
        button = v.findViewById(R.id.connectButton);
        batteryLayout = v.findViewById(R.id.batteryLayout);
        textBattery = v.findViewById(R.id.polarDeviceBattery);
        String textButton = button.getText().toString();
        if (textButton.toLowerCase().equals("connect")) {
            new DeviceConnectionAsync().execute(new DeviceConnectionParams(id, true));
        } else if (textButton.toLowerCase().equals("disconnect")) {
            new DeviceConnectionAsync().execute(new DeviceConnectionParams(id, false));
        }
    }

    private static class DeviceConnectionParams {
        String id;
        boolean action;

        DeviceConnectionParams(String id, boolean action) {
            this.id = id;
            this.action = action;
        }
    }

    class DeviceConnectionAsync extends AsyncTask<DeviceConnectionParams, Void, Void> {
        private boolean action;
        private int devicesCount;

        protected void onPreExecute() {
            super.onPreExecute();
            button.setText("Connecting...");
        }

        protected Void doInBackground(DeviceConnectionParams... args) {
            action = args[0].action;
            devicesCount = devicePreferenceManager.getConnectedDevices();
            if (action == true && devicesCount < 1) {
                polarSDK.connectDevice(args[0].id);
                while (!isBatteryReceived && !isDeviceConnected);
                devicePreferenceManager.setConnectedDevices(devicesCount + 1);
                devicePreferenceManager.setID(args[0].id);
            } else if (action == false) {
                polarSDK.disconnectDevice(args[0].id);
                while (!isDeviceDisconnected);
                devicePreferenceManager.setConnectedDevices(devicesCount - 1);
            } else {
                Log.d("MyApp", "You can only connect 1 device at the same time"); //Make a toast here
                action = false;
            }
            return null;
        }

        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if (action == true) {
                batteryLayout.setVisibility(View.VISIBLE);
                textBattery.setText(devicePreferenceManager.getBatteryLevel() + "%");
                button.setText("Disconnect");
            } else if (action == false) {
                batteryLayout.setVisibility(View.INVISIBLE);
                button.setText("Connect");
            }
        }
    }
}
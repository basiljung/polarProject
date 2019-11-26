package com.example.polarapp.devices;

import android.os.*;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.*;
import android.widget.*;

import com.example.polarapp.R;
import com.example.polarapp.polar.PolarSDK;
import com.example.polarapp.preferencesmanager.DevicePreferencesManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

import polar.com.sdk.api.model.PolarDeviceInfo;

public class SearchDevicesFragment extends Fragment implements PolarSDK.CallbackInterfaceDevices, DevicesArrayAdapter.ButtonConnectCallback {

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
    private DevicePreferencesManager devicePreferencesManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_search_devices, container, false);
        devicePreferencesManager = new DevicePreferencesManager(getActivity().getBaseContext());

        polarDeviceInfoArrayList = new ArrayList<>();
        listView = root.findViewById(R.id.devices_list_view);

        polarSDK = (PolarSDK) getActivity().getApplicationContext();
        polarSDK.setCallbackInterfaceDevices(this);
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
        devicePreferencesManager.setBatteryLevel(batteryLevel);
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
            devicesCount = devicePreferencesManager.getConnectedDevices();
            if (action == true && devicesCount < 1) { // Connect
                polarSDK.connectDevice(args[0].id, false);
                while (!(isBatteryReceived && isDeviceConnected)) ;
                isBatteryReceived = false;
                isDeviceConnected = false;
                devicePreferencesManager.setConnectedDevices(devicesCount + 1);
                devicePreferencesManager.setDeviceID(args[0].id);
            } else if (action == false) { // Disconnect
                polarSDK.disconnectDevice(args[0].id);
                while (!isDeviceDisconnected) ;
                isDeviceDisconnected = false;
                devicePreferencesManager.setBatteryLevel(-1);
                devicePreferencesManager.setConnectedDevices(devicesCount - 1);
                devicePreferencesManager.setDeviceID("");
            } else {
                Log.d("MyApp", "You can only connect 1 device at the same time");
                //Make a toast here
                action = false;
            }
            return null;
        }

        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if (action == true) {
                batteryLayout.setVisibility(View.VISIBLE);
                Log.d("MyApp", "isBatteryReceived onPost: " + isBatteryReceived);
                textBattery.setText(devicePreferencesManager.getBatteryLevel() + "%");
                button.setText("Disconnect");
            } else {
                batteryLayout.setVisibility(View.INVISIBLE);
                button.setText("Connect");
            }
        }
    }
}